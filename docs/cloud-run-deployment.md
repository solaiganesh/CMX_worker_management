# Deploy CMX Worker Management to Google Cloud Run

Step-by-step guide to deploy the Spring Boot API on **Google Cloud Run** with **Cloud Storage** for persistent Excel workbook storage.

---

## Architecture

```
Client → Cloud Run (cmx-worker-management)
              │
              ├── Spring Boot API (port 8080)
              └── /app/data  ← GCS FUSE volume mount
                        │
                        └── gs://YOUR-PROJECT-cmx-data/
                              ├── workers_basic_info.xlsx
                              ├── workers_skills.xlsx
                              ├── workers_ratings.xlsx
                              └── workers_availability.xlsx
```

The application continues to use the existing Excel repository layer. Cloud Run mounts a GCS bucket at `/app/data`; the app reads and writes `.xlsx` files there as if they were on a local disk. Workbooks survive container restarts, redeployments, and scale-to-zero.

---

## Prerequisites

| Requirement | Notes |
|-------------|-------|
| Google Cloud account | With billing enabled |
| [gcloud CLI](https://cloud.google.com/sdk/docs/install) | Authenticated locally |
| GCP project | e.g. `my-cmx-project` |
| Owner/Editor role | Or sufficient IAM to create Run, Storage, Artifact Registry, Cloud Build resources |

### Install and authenticate gcloud

```bash
gcloud auth login
gcloud auth application-default login
gcloud config set project YOUR_PROJECT_ID
```

Verify:

```bash
gcloud config get-value project
```

---

## Quick deploy (automated script)

From the repository root:

```bash
# Optional: copy and edit environment defaults
cp .env.cloudrun.example .env.cloudrun

export PROJECT_ID=YOUR_PROJECT_ID
export REGION=asia-south1          # pick a region near your users
export BUCKET_NAME=${PROJECT_ID}-cmx-data

chmod +x scripts/deploy-cloud-run.sh
./scripts/deploy-cloud-run.sh
```

The script will:

1. Enable required GCP APIs
2. Create Artifact Registry repo (if missing)
3. Create Cloud Storage bucket (if missing)
4. Grant the Cloud Run service account access to the bucket
5. Build the Docker image via Cloud Build
6. Deploy to Cloud Run with the GCS volume mounted at `/app/data`

On success it prints the **service URL** and health check path.

---

## Manual step-by-step deployment

### Step 1 — Set variables

```bash
export PROJECT_ID=YOUR_PROJECT_ID
export REGION=asia-south1
export SERVICE_NAME=cmx-worker-management
export REPO_NAME=cmx-repo
export IMAGE_NAME=cmx-worker-management
export BUCKET_NAME=${PROJECT_ID}-cmx-data
export IMAGE=${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPO_NAME}/${IMAGE_NAME}:latest
```

### Step 2 — Enable APIs

```bash
gcloud services enable \
  run.googleapis.com \
  artifactregistry.googleapis.com \
  cloudbuild.googleapis.com \
  storage.googleapis.com \
  --project=$PROJECT_ID
```

### Step 3 — Create Artifact Registry repository

```bash
gcloud artifacts repositories create $REPO_NAME \
  --repository-format=docker \
  --location=$REGION \
  --description="CMX Worker Management images" \
  --project=$PROJECT_ID
```

Skip if the repository already exists.

### Step 4 — Create Cloud Storage bucket

```bash
gcloud storage buckets create gs://${BUCKET_NAME} \
  --location=$REGION \
  --uniform-bucket-level-access \
  --project=$PROJECT_ID
```

This bucket stores all Excel workbooks permanently.

### Step 5 — Grant Cloud Run access to the bucket

Cloud Run uses the **Compute Engine default service account** unless you specify another:

```bash
export PROJECT_NUMBER=$(gcloud projects describe $PROJECT_ID --format='value(projectNumber)')
export RUNTIME_SA="${PROJECT_NUMBER}-compute@developer.gserviceaccount.com"

gcloud storage buckets add-iam-policy-binding gs://${BUCKET_NAME} \
  --member="serviceAccount:${RUNTIME_SA}" \
  --role="roles/storage.objectAdmin" \
  --project=$PROJECT_ID
```

`roles/storage.objectAdmin` allows read/write of Excel files in the bucket.

### Step 6 — Build and push the container image

From the **repository root** (build context is `./cmx`):

```bash
gcloud builds submit ./cmx --tag $IMAGE --project=$PROJECT_ID
```

This uses the existing `cmx/Dockerfile` (Java 17, multi-stage Maven build).

**Alternative — local Docker build:**

```bash
gcloud auth configure-docker ${REGION}-docker.pkg.dev
docker build -t $IMAGE ./cmx
docker push $IMAGE
```

### Step 7 — Deploy to Cloud Run with GCS volume

```bash
gcloud run deploy $SERVICE_NAME \
  --image $IMAGE \
  --region $REGION \
  --platform managed \
  --allow-unauthenticated \
  --port 8080 \
  --memory 1Gi \
  --cpu 1 \
  --timeout 60 \
  --execution-environment gen2 \
  --add-volume name=cmx-data,type=cloud-storage,bucket=${BUCKET_NAME} \
  --add-volume-mount volume=cmx-data,mount-path=/app/data \
  --set-env-vars "PORT=8080,CMX_STORAGE_EXCEL_BASE_PATH=/app/data,CMX_TRANSLATION_PROVIDER=property-file" \
  --project=$PROJECT_ID
```

| Flag | Purpose |
|------|---------|
| `--execution-environment gen2` | Required for Cloud Storage volume mounts |
| `--add-volume ... cloud-storage` | Mounts GCS bucket via Cloud Storage FUSE |
| `--add-volume-mount ... /app/data` | Same path the app uses for Excel files |
| `CMX_STORAGE_EXCEL_BASE_PATH=/app/data` | Tells the app where workbooks live |

### Step 8 — Verify deployment

```bash
export SERVICE_URL=$(gcloud run services describe $SERVICE_NAME \
  --region $REGION --project=$PROJECT_ID --format='value(status.url)')

curl -s "${SERVICE_URL}/api/v1/health"
```

Expected:

```json
{"status":"UP"}
```

Register a test worker:

```bash
curl -X POST "${SERVICE_URL}/api/v1/workers/register" \
  -H "Content-Type: application/json" \
  -H "Accept-Language: en" \
  -d '{
    "basicInfo": {
      "fullName": "Rajesh Kumar",
      "mobileNumber": "+919876543210",
      "email": "rajesh@example.com",
      "dateOfBirth": "1990-05-15",
      "gender": "MALE",
      "city": "Chennai",
      "state": "Tamil Nadu",
      "address": "12 MG Road",
      "pincode": "600001",
      "primaryLanguage": "hi",
      "availabilityStatus": "AVAILABLE"
    },
    "skills": {
      "primarySkill": "Welding",
      "experienceYears": 5,
      "skillLevel": "INTERMEDIATE",
      "workType": "FULL_TIME",
      "languagesSpoken": ["hi"]
    }
  }'
```

### Step 9 — Confirm Excel files in Cloud Storage

After the first API call that creates workbooks, list bucket contents:

```bash
gcloud storage ls gs://${BUCKET_NAME}/
```

You should see files such as:

```
gs://YOUR-PROJECT-cmx-data/workers_basic_info.xlsx
gs://YOUR-PROJECT-cmx-data/workers_skills.xlsx
gs://YOUR-PROJECT-cmx-data/workers_ratings.xlsx
gs://YOUR-PROJECT-cmx-data/workers_availability.xlsx
```

---

## Environment variables

| Variable | Cloud Run value | Description |
|----------|-----------------|-------------|
| `PORT` | `8080` | Set by Cloud Run; app listens on this port |
| `CMX_STORAGE_EXCEL_BASE_PATH` | `/app/data` | Excel workbook directory (GCS mount) |
| `CMX_TRANSLATION_PROVIDER` | `property-file` | Translation backend |
| `GOOGLE_TRANSLATE_API_KEY` | secret | Required if provider is `google` or `composite` |

### Using Secret Manager for API keys

```bash
echo -n "your-api-key" | gcloud secrets create google-translate-api-key --data-file=-

gcloud run services update $SERVICE_NAME \
  --region $REGION \
  --set-secrets "GOOGLE_TRANSLATE_API_KEY=google-translate-api-key:latest" \
  --update-env-vars "CMX_TRANSLATION_PROVIDER=composite"
```

---

## Redeploy after code changes

```bash
gcloud builds submit ./cmx --tag $IMAGE --project=$PROJECT_ID

gcloud run deploy $SERVICE_NAME \
  --image $IMAGE \
  --region $REGION \
  --project=$PROJECT_ID
```

Volume mount and env vars persist on the service unless you change them.

---

## CI/CD with Cloud Build trigger

The repo includes `cloudbuild.yaml` at the root. To deploy via Cloud Build:

1. Ensure bucket `${PROJECT_ID}-cmx-data` exists and IAM is configured (Steps 4–5).
2. Run:

```bash
gcloud builds submit --config cloudbuild.yaml --project=$PROJECT_ID
```

3. Optional: create a **Cloud Build Trigger** on your `main` branch in GCP Console → Cloud Build → Triggers.

---

## Recommended Cloud Run settings

| Setting | Recommended | Notes |
|---------|-------------|-------|
| Memory | `1Gi` | Spring Boot + Apache POI |
| CPU | `1` | Increase for high traffic |
| Min instances | `0` (dev) / `1` (prod) | `1` avoids cold starts |
| Max instances | `10` | Adjust for load |
| Timeout | `60s` | REST API should respond quickly |
| Concurrency | `80` (default) | OK for this workload |

---

## Storage behaviour and limitations

### How it works

- On startup, `ExcelStorageInitializer` creates empty workbooks under `/app/data` if they do not exist.
- With the GCS mount, those files are stored in your bucket and **persist across restarts**.
- You can download or back up workbooks directly from GCS.

### GCS FUSE limitations (important)

| Topic | Guidance |
|-------|----------|
| Concurrent writes | Avoid running multiple Cloud Run instances with heavy simultaneous Excel writes; prefer `--max-instances 1` or migrate to SQL for high write concurrency |
| Latency | Slightly higher than local disk; acceptable for MVP |
| File locking | POI/Excel is not ideal for multi-writer scenarios on any shared filesystem |
| Production scale | Plan SQL migration (`docs/tasks.md` backlog) for multi-instance deployments |

For MVP and moderate traffic, **Gen2 + GCS volume + max-instances 1** is a practical setup.

---

## View logs

```bash
gcloud run services logs read $SERVICE_NAME --region $REGION --project=$PROJECT_ID
```

Or: **GCP Console → Cloud Run → cmx-worker-management → Logs**

---

## Troubleshooting

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| Container fails to start | Wrong port | Ensure `PORT=8080` and deploy with `--port 8080` |
| Permission denied on `/app/data` | Missing bucket IAM | Re-run Step 5 (`storage.objectAdmin` for runtime SA) |
| Excel files not in bucket | Volume not mounted | Redeploy with `--execution-environment gen2` and volume flags |
| 403 on service URL | Auth required | Add `--allow-unauthenticated` or call with identity token |
| Data lost after deploy | Volume missing on update | Include `--add-volume` flags on every deploy that changes storage |
| Out of memory | POI + Spring | Increase `--memory 1Gi` or `2Gi` |

---

## Security checklist (production)

- [ ] Remove `--allow-unauthenticated`; use IAM or Cloud API Gateway + auth
- [ ] Store translation API keys in Secret Manager
- [ ] Restrict bucket access to Cloud Run service account only
- [ ] Enable Cloud Run ingress controls (internal / VPC if needed)
- [ ] Set up bucket lifecycle rules or backup policy for Excel data
- [ ] Plan migration to Cloud SQL for production scale

---

## Local Docker vs Cloud Run

| | Local Docker Compose | Cloud Run |
|--|---------------------|-----------|
| Storage | Docker volume `cmx-data` | GCS bucket via FUSE mount |
| Port | `8080` | `8080` (via `PORT`) |
| Persistence | Survives container restart | Survives restart + redeploy |
| Deploy | `docker compose up` | `./scripts/deploy-cloud-run.sh` |

---

## Related files in this repository

| File | Purpose |
|------|---------|
| `cmx/Dockerfile` | Container image definition |
| `docker-compose.yml` | Local development with Docker volume |
| `scripts/deploy-cloud-run.sh` | One-command Cloud Run deploy |
| `cloudbuild.yaml` | Cloud Build CI/CD pipeline |
| `.env.cloudrun.example` | Example environment variables |
| `docs/api-reference.md` | Full API documentation |

---

## Definition of done

1. Cloud Run service responds `200` on `/api/v1/health`
2. Worker registration succeeds via the public URL
3. Excel files appear in `gs://YOUR-PROJECT-cmx-data/`
4. Data persists after redeploying a new revision
