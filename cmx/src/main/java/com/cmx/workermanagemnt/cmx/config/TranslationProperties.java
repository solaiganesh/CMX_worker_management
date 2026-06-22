package com.cmx.workermanagemnt.cmx.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cmx.translation")
public class TranslationProperties {

	private String provider = "property-file";
	/**
	 * Cloud backend used when {@code provider} is {@code composite}: {@code google} (default) or {@code azure}.
	 */
	private String cloudProvider = "google";
	private Google google = new Google();
	private Azure azure = new Azure();
	private Cache cache = new Cache();
	private OnFailure onFailure = new OnFailure();

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getCloudProvider() {
		return cloudProvider;
	}

	public void setCloudProvider(String cloudProvider) {
		this.cloudProvider = cloudProvider;
	}

	public Google getGoogle() {
		return google;
	}

	public void setGoogle(Google google) {
		this.google = google;
	}

	public Azure getAzure() {
		return azure;
	}

	public void setAzure(Azure azure) {
		this.azure = azure;
	}

	public Cache getCache() {
		return cache;
	}

	public void setCache(Cache cache) {
		this.cache = cache;
	}

	public OnFailure getOnFailure() {
		return onFailure;
	}

	public void setOnFailure(OnFailure onFailure) {
		this.onFailure = onFailure;
	}

	public static class Google {

		private String apiKey = "";
		private String projectId = "";
		private String baseUrl = "https://translation.googleapis.com/language/translate/v2";
		private Duration timeout = Duration.ofSeconds(5);

		public String getApiKey() {
			return apiKey;
		}

		public void setApiKey(String apiKey) {
			this.apiKey = apiKey;
		}

		public String getProjectId() {
			return projectId;
		}

		public void setProjectId(String projectId) {
			this.projectId = projectId;
		}

		public String getBaseUrl() {
			return baseUrl;
		}

		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}

		public Duration getTimeout() {
			return timeout;
		}

		public void setTimeout(Duration timeout) {
			this.timeout = timeout;
		}
	}

	public static class Azure {

		private String apiKey = "";
		private String region = "";
		private String baseUrl = "https://api.cognitive.microsofttranslator.com";
		private Duration timeout = Duration.ofSeconds(5);

		public String getApiKey() {
			return apiKey;
		}

		public void setApiKey(String apiKey) {
			this.apiKey = apiKey;
		}

		public String getRegion() {
			return region;
		}

		public void setRegion(String region) {
			this.region = region;
		}

		public String getBaseUrl() {
			return baseUrl;
		}

		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}

		public Duration getTimeout() {
			return timeout;
		}

		public void setTimeout(Duration timeout) {
			this.timeout = timeout;
		}
	}

	public static class Cache {

		private boolean enabled = true;
		private long maxSize = 10_000;
		private Duration ttl = Duration.ofHours(24);

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public long getMaxSize() {
			return maxSize;
		}

		public void setMaxSize(long maxSize) {
			this.maxSize = maxSize;
		}

		public Duration getTtl() {
			return ttl;
		}

		public void setTtl(Duration ttl) {
			this.ttl = ttl;
		}
	}

	public static class OnFailure {

		/**
		 * {@code passthrough} returns original text; {@code fail} throws {@code TranslationException}.
		 */
		private String read = "passthrough";
		private String write = "fail";

		public String getRead() {
			return read;
		}

		public void setRead(String read) {
			this.read = read;
		}

		public String getWrite() {
			return write;
		}

		public void setWrite(String write) {
			this.write = write;
		}

		public boolean shouldFailOnRead() {
			return "fail".equalsIgnoreCase(read);
		}

		public boolean shouldFailOnWrite() {
			return "fail".equalsIgnoreCase(write);
		}
	}
}
