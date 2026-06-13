# Implementation Tasks — CMX Labour Registration

Tasks reflect the Excel-backed, multi-language, Java 17 architecture. Ordered by dependency.

Legend: `[ ]` todo · `[x]` done · `[-]` deferred/post-MVP

---

## Phase 0 — Project Foundation

- [x] Set `java.version=17` in `pom.xml`
- [x] Set Spring Boot parent to **3.4.x** (Java 17 compatible; replace current 4.1.0 / Java 26)
- [x] Add dependencies:
  - `spring-boot-starter-web`
  - `spring-boot-starter-validation`
  - `spring-boot-starter-actuator`
  - `apache-poi-ooxml` (Excel read/write)
  - `springdoc-openapi-starter-webmvc-ui`
  - `spring-boot-starter-test`
- [x] Configure `application.properties` (`cmx.storage.*`, `spring.messages.basename`, server port)
- [x] Create package structure under `com.cmx.workermanagemnt.cmx` (see [architecture.md](./architecture.md))
- [x] Add `data/` to `.gitignore`; commit sample/template workbooks optionally
- [x] Update root README with run instructions and API overview

---

## Phase 1 — Domain & Repository Abstraction

- [x] Domain POJOs: `WorkerBasicInfo`, `WorkerSkills`
- [x] Enums: `Gender`, `AvailabilityStatus`, `SkillLevel`, `WorkType`
- [x] Repository interfaces: `WorkerBasicInfoRepository`, `WorkerSkillsRepository`
- [x] `ExcelFileSupport` — create file with headers, open workbook, append row, find row by id
- [x] `ExcelWorkerBasicInfoRepository` — implements interface using POI
- [x] `ExcelWorkerSkillsRepository` — implements interface using POI
- [x] `@ConditionalOnProperty(cmx.storage.type=excel)` on Excel implementations
- [x] Startup initializer: ensure Excel files exist with correct header rows
- [x] Unit tests: row mapping, pipe-delimited list fields, file creation (@TempDir)

---

## Phase 2 — Cross-Cutting (Logging & Exceptions)

- [x] `CorrelationIdFilter` — generate UUID, set MDC, echo in response header
- [x] `RequestLoggingFilter` — log method, URI, status, duration; mask mobile/email
- [x] `ErrorResponse` record/class with `code`, `message`, `correlationId`, `timestamp`, `fieldErrors`
- [x] Domain exceptions: `WorkerNotFoundException`, `DuplicateWorkerException`, `StorageException`
- [x] `GlobalExceptionHandler` (`@RestControllerAdvice`) — single handler for all exceptions
- [x] Wire `MessageSource` for localized error messages
- [x] Test: MockMvc triggers validation error → consistent error envelope with correlation ID

---

## Phase 3 — Multi-Language Support

- [x] `TranslationService` interface + `PropertyFileTranslationService` implementation
- [x] `messages_en.properties`, `messages_hi.properties` (and others as needed) — enum labels, API messages
- [x] `translations/` property files for skill/city reference translations (en ↔ hi, etc.)
- [x] Locale resolver: `Accept-Language` header → `Locale`
- [x] Write path: `toEnglish()` before persist on registration
- [x] Read path: `fromEnglish()` after load on GET
- [x] Unit tests: known skill/city translated; unknown values passthrough; enum label lookup

---

## Phase 4 — Registration API

- [x] Request DTOs: `WorkerRegistrationRequest` (`basicInfo` + `skills` nested objects)
- [x] Response DTO: `WorkerResponse` (combined, includes computed `age`)
- [x] Bean validation: `@NotBlank`, `@Email`, `@Past` on DOB, `@Pattern` on mobile/pincode
- [x] `WorkerRegistrationService`:
  - Generate UUID `id`
  - Check duplicate mobile/email
  - Translate input to English
  - Save basic info then skills (compensating delete on skills failure if needed)
  - Set `created_at` / `updated_at`
- [x] `WorkerController`:
  - `POST /api/v1/workers/register` → 201 + `Location`
  - `GET /api/v1/workers/{id}` → localized response
  - `GET /api/v1/health` → 200
- [x] OpenAPI annotations on controller and DTOs
- [x] MockMvc tests: happy path register + get, duplicate mobile 409, validation 400, not found 404

---

## Phase 5 — Hardening & Dev Experience

- [ ] Actuator health endpoint configured
- [x] Logback pattern includes correlation ID
- [x] Document API with example curl commands in README
- [ ] Optional: `docs/api-examples.http` for manual testing
- [x] `./mvnw verify` passes in CI

---

## Phase 6 — Google Cloud Translation (Multi-Language)

Replace property-file lookups with dynamic translation for any supported language, while keeping English as the canonical storage format in Excel. Builds on the existing `TranslationService` contract — no changes to `WorkerRegistrationService` or controller flow.

**Goals**

- **Write:** `Content-Language: {locale}` → translate translatable fields to English before persist.
- **Read:** `Accept-Language: {locale}` → translate stored English fields to the requested locale in the response.
- **Multi-language:** support 100+ languages via Google Cloud Translation (BCP-47 tags), not one properties file per locale.

**Prerequisites (GCP — manual, outside repo)**

- [ ] Create GCP project; enable **Cloud Translation API**
- [ ] Create API key or service account credentials; store in env / secret manager (never commit)
- [ ] Confirm billing account attached (required even for free tier: 500K characters/month)

**Configuration & provider switching**

- [x] Add `TranslationProperties` (`cmx.translation.*`) bound via `@ConfigurationProperties`
  - `provider` — `property-file` (default) | `google` | `composite`
  - `google.api-key` — from `${GOOGLE_TRANSLATE_API_KEY}` env var
  - `google.project-id` — optional, for Advanced/v3 if used later
  - `cache.enabled`, `cache.max-size`, `cache.ttl` — Caffeine settings
- [x] Document new properties in README and [architecture.md](./architecture.md)
- [x] `@ConditionalOnProperty` on each `TranslationService` implementation (mirror `cmx.storage.type` pattern)

**Dependencies**

- [x] Add Google Cloud Translation client (REST via Spring `RestClient`)
- [x] Add Caffeine cache dependency for translation result caching

**Core implementation**

- [x] `GoogleTranslationClient` — thin wrapper around Google API
  - `translateBatch(List<String> texts, String sourceLang, String targetLang)` — one API call per request direction
  - Cache key `{sourceLang}|{targetLang}|{text}` built into client
  - Short-circuit when source and target language match
- [x] `GoogleCloudTranslationService implements TranslationService`
  - Reuse same translatable field set as `PropertyFileTranslationService`:
    - basic: `city`, `state`, `address`
    - skills: `primarySkill`, `secondarySkills`, `certifications`, `toolsOwned`
  - **Do not translate:** `fullName`, `mobileNumber`, `email`, URLs, enums, `languagesSpoken`, numeric fields
  - `toEnglish()`: batch translate all non-empty translatable fields → English
  - `fromEnglish()`: batch translate all non-empty translatable fields → target locale
  - Passthrough when `sourceLocale` / `targetLocale` is English (same as today)
- [x] In-client Caffeine cache (replaces separate `CachedTranslationService` decorator)
- [x] `CompositeTranslationService`
  - Try property-file hit first for known controlled vocabulary (skills, cities)
  - Fall back to Google for unknown / free-text values (e.g. custom addresses)
  - Activated when `cmx.translation.provider=composite`

**Error handling & resilience**

- [x] New `TranslationException` → `GlobalExceptionHandler` maps to 503 with code `TRANSLATION_UNAVAILABLE`
- [x] Configurable fallback: `on-failure=passthrough` (default for read) vs `fail` (strict for write)
- [x] Log translation failures with correlation ID; never log full PII payloads
- [x] Timeout on Google HTTP client (e.g. 5s)

**Testing**

- [x] Unit tests with mocked `GoogleTranslationClient` — verify field selection, batching, English short-circuit
- [x] Unit tests for cache hit/miss (MockWebServer)
- [x] Keep existing `PropertyFileTranslationServiceTest` as regression for `property-file` provider
- [x] `GoogleTranslationClientTest` with MockWebServer stubbing Google API (no live key in CI)
- [-] Optional manual test profile: `@Tag("google-live")` with real API key for local verification

**Observability & cost control**

- [x] Log character count estimate per request (debug level) for cost monitoring
- [-] Actuator metric or counter: `cmx.translation.requests`, `cmx.translation.characters` (optional)

**Documentation**

- [x] README: GCP setup steps, env vars, example curl with `Content-Language: hi` / `Accept-Language: ta`
- [x] Update architecture.md “Future: Google Translate” section → describe provider switch and hybrid model

**Rollout**

1. Ship with `cmx.translation.provider=property-file` unchanged (zero regression).
2. Enable `google` or `composite` in staging with API key.
3. Load-test register + GET; validate cache reduces repeat GET cost.
4. Production: start with `composite`; monitor free-tier usage in GCP console.

---

## Phase 7 — Worker Search & Ratings

Enable clients to **discover workers** by age, experience, and city, and to **submit and aggregate ratings**. Builds on existing worker registration data plus a new ratings store. Keeps repository abstraction (Excel v1, SQL later).

**Personas**

| Persona | Needs |
|---------|-------|
| **Employer / client** | Search workers by city, age range, experience; see summary profile with contact info and average rating |
| **Client / peer** | Rate a worker after engagement using name or phone number |

**Design decisions (lock before implementation)**

| Topic | Decision |
|-------|----------|
| Rating scale | 1–5 stars (integer or half-star — default **1–5 integer**) |
| Overall rating | **Computed** on read: `average(score)` from all ratings for `worker_id`; optional cached aggregate column later |
| Rate by name | Name is **not unique** — if multiple matches → **409 AMBIGUOUS_WORKER** with hint to use phone; if exactly one match → accept |
| Rate by phone | Phone is unique (enforced at registration) — preferred identifier |
| Search city | Match against **English canonical** city in Excel; translate `city` in request body via `TranslationService.toEnglish` when `Content-Language` ≠ en |
| Search age | Filter on computed age from `date_of_birth` (`minAge`, `maxAge` in body — apply only if not null) |
| Search experience | Filter on `experience_years` from skills workbook (`minExperience`, `maxExperience` in body — apply only if not null) |
| Search method | **`POST /api/v1/workers/search`** with JSON body; each filter field optional — **null = ignore** (AND logic across non-null fields) |
| Search response shape | **Summary DTO** (not full `WorkerResponse`) — name, contact, phone, experience, rating |
| Auth | None in v1 (same as registration); document abuse risk for open rating endpoint |

---

### Phase 7A — Ratings domain & service

**Data model — Table 3: Worker Ratings (`workers_ratings.xlsx`)**

| Field | Type | Required | Notes |
|-------|------|----------|-------|
| `id` | UUID | auto | Primary key |
| `worker_id` | UUID | yes | FK → `workers_basic_info.id` |
| `score` | integer | yes | 1–5 |
| `reviewer_name` | string | no | Optional; who submitted the rating |
| `comment` | string | no | Optional short text (max 500 chars) |
| `created_at` | datetime | auto | |

- [x] Domain POJO: `WorkerRating`
- [x] Repository interface: `WorkerRatingRepository`
  - `WorkerRating save(WorkerRating rating)`
  - `List<WorkerRating> findByWorkerId(String workerId)`
  - `double calculateAverageRating(String workerId)` — returns 0.0 when no ratings
  - `int countByWorkerId(String workerId)`
- [x] `ExcelWorkerRatingRepository` + `ExcelFileSupport` workbook init (`workers_ratings.xlsx` header row)
- [x] `@ConditionalOnProperty(cmx.storage.type=excel)` on Excel implementation
- [x] Config: `cmx.storage.excel.ratings-file=workers_ratings.xlsx`
- [x] Startup initializer: ensure ratings workbook exists

**Rating service**

- [x] `WorkerRatingService`
  - Resolve worker by **phone** (primary) or **full name** (exact match, case-insensitive)
  - Validate score range 1–5
  - Persist rating row linked to `worker_id`
  - Return updated aggregate: `{ averageRating, ratingCount }`
- [x] Exceptions:
  - `WorkerNotFoundException` — no worker for phone/name
  - `AmbiguousWorkerException` — multiple workers for name (409)
  - `InvalidRatingException` — score out of range (400 via Bean Validation)

**Submit rating API**

- [x] `POST /api/v1/workers/ratings`
- [ ] Request body (`WorkerRatingRequest`):
  ```json
  {
    "workerPhone": "+919876543210",
    "workerName": null,
    "score": 4,
    "reviewerName": "ABC Contractors",
    "comment": "Good work"
  }
  ```
  - Require **exactly one** of `workerPhone` or `workerName` (validation)
  - Prefer `workerPhone` when both supplied → 400 or ignore name (document)
- [ ] Response (`WorkerRatingResponse`):
  ```json
  {
    "workerId": "uuid",
    "workerName": "Rajesh Kumar",
    "averageRating": 4.2,
    "ratingCount": 5,
    "submittedScore": 4
  }
  ```
- [x] `GlobalExceptionHandler` — `AmbiguousWorkerException` → 409, `InvalidRatingException` → 400
- [x] i18n message keys for new error codes

**Tests (7A)**

- [x] Unit: average calculation (0 ratings, 1 rating, multiple ratings)
- [x] Integration: submit by phone → rating row in Excel; submit by ambiguous name → 409
- [x] MockMvc: validation errors (missing identifier, invalid score)
- [-] OpenAPI annotations

---

### Phase 7B — Worker search API

**Search request (`WorkerSearchRequest`) — POST body; all fields optional**

Apply a filter **only when the field is not null**. Omit or send `null` to skip that criterion. Non-null filters combine with **AND** logic.

| Field | Type | Example | Applied when |
|-------|------|---------|--------------|
| `city` | string | `"Chennai"` | not null — exact match on English canonical city |
| `minAge` | integer | `25` | not null — inclusive lower bound on computed age |
| `maxAge` | integer | `45` | not null — inclusive upper bound on computed age |
| `minExperience` | decimal | `3` | not null — inclusive lower bound on `experience_years` |
| `maxExperience` | decimal | `10` | not null — inclusive upper bound on `experience_years` |
| `minRating` | decimal | `4.0` | not null — filter by computed average rating |
| `page` | integer | `0` | default `0` if null |
| `size` | integer | `20` | default `20` if null; max 100 |

- [x] `POST /api/v1/workers/search`
- [x] Request DTO: `WorkerSearchRequest` with Bean Validation (`@Min`, `@Max` on page/size; `@DecimalMin` on ranges when present)
- [ ] Example body (only city + experience filters active):
  ```json
  {
    "city": "Chennai",
    "minAge": null,
    "maxAge": null,
    "minExperience": 3,
    "maxExperience": null,
    "minRating": null,
    "page": 0,
    "size": 20
  }
  ```
- [x] Empty body `{}` or all-null filters → return **all workers** (paginated)
- [x] Header: `Accept-Language` — localize `city` in results
- [x] Header: `Content-Language` (optional) — translate `city` in request body to English before filter when not null

**Search response — summary DTO (`WorkerSearchResult`)**

Each item includes:

| Field | Source |
|-------|--------|
| `id` | basic info |
| `fullName` | basic info (not translated) |
| `email` | basic info — **contact detail** |
| `mobileNumber` | basic info — **phone** |
| `city` | basic info (localized) |
| `experienceYears` | skills |
| `averageRating` | computed from ratings repo |
| `ratingCount` | computed from ratings repo |

- [x] Wrapper: `WorkerSearchResponse { items, total, page, size }`
- [x] Do **not** expose full address/skills in search v1 (keep response minimal; full profile via `GET /workers/{id}`)

**Repository & service**

- [x] Extend `WorkerBasicInfoRepository`:
  - `List<WorkerBasicInfo> findAll()` — scan Excel (v1)
  - `findByMobileNumber`, `findByFullNameIgnoreCase`
- [x] `WorkerSearchService`
  - Join basic info + skills by `id` / `user_id`
  - For each candidate worker, apply **only non-null** filters:
    - `city` → match English city (after optional translation of request `city`)
    - `minAge` / `maxAge` → computed from `date_of_birth`
    - `minExperience` / `maxExperience` → from skills
    - `minRating` → from `WorkerRatingRepository.calculateAverageRating`
  - Attach `averageRating` / `ratingCount` per worker via `WorkerRatingRepository`
  - Apply pagination in memory (Excel v1)
  - Map to `WorkerSearchResult` + localize `city` per `Accept-Language`

**Tests (7B)**

- [x] Integration: seed workers → POST search with city + minExperience → correct subset
- [x] POST with `{}` returns paginated all workers
- [x] Null fields ignored via integration tests
- [x] `minRating` filter excludes unrated or low-rated workers
- [x] MockMvc: `Accept-Language: hi` localizes city in search results
- [-] Age filter dedicated test
- [-] Pagination edge-case tests

**Documentation**

- [x] README: POST search and rating curl examples
- [-] Update [product-requirements.md](./product-requirements.md) — FR-6 Search, FR-7 Ratings
- [-] Update [architecture.md](./architecture.md) — third workbook, search flow diagram

---

### Phase 7 — Definition of Done

1. `POST /api/v1/workers/ratings` accepts score by phone (or unique name); rating persisted in `workers_ratings.xlsx`.
2. Overall `averageRating` and `ratingCount` returned after each submission and available on search results.
3. `POST /api/v1/workers/search` accepts optional filters in JSON body; **null fields are ignored**; non-null filters use AND logic.
4. Search response includes name, email, phone, experience, average rating (summary only).
5. Ambiguous name on rating → 409; unknown worker → 404; invalid score → 400.
6. `./mvnw verify` green; PII masked in logs for search/rating endpoints.

**Implementation order**

1. Ratings workbook + repository (7A storage)
2. `WorkerRatingService` + submit API (7A)
3. `WorkerSearchService` + search API (7B) — depends on ratings aggregate
4. Docs + tests

**Future enhancements (defer)**

- [-] One rating per reviewer phone per worker (dedupe)
- [-] Denormalized `average_rating` column on basic info for faster search at scale
- [-] Full-text name search on worker discovery
- [-] Rate by worker `id` (when client already has it from search)
- [-] SQL indexes on city, experience, rating when migrating off Excel

---

## Post-MVP Backlog

- [-] `PUT /api/v1/workers/{id}` — update basic info and/or skills
- [-] Worker search & ratings — implemented in **Phase 7**
- [-] `GET /api/v1/workers` — list all with pagination (superseded by Phase 7 search)
- [-] SQL repository implementation (JPA + PostgreSQL)
- [-] Excel → SQL migration utility
- [-] Spring Security / JWT authentication
- [-] Google Cloud Translation — implemented in **Phase 6** (enable via `cmx.translation.provider`)
- [-] File upload for profile photo and portfolio (S3/local storage)
- [-] File locking or migration to SQL before multi-instance deployment

---

## Definition of Done (v0.1)

1. Worker can register via `POST /api/v1/workers/register`; rows appear in both Excel files linked by `id` / `user_id`.
2. All persisted values in Excel are English.
3. `GET /api/v1/workers/{id}` returns data localized per `Accept-Language`.
4. All exceptions return the standard `ErrorResponse` from `GlobalExceptionHandler`.
5. Request logs include correlation ID; mobile/email not logged in plain text.
6. Repository interfaces have no POI imports in the service layer.
7. `./mvnw verify` green on Java 17.
