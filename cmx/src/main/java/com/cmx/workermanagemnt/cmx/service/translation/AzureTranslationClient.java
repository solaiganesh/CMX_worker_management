package com.cmx.workermanagemnt.cmx.service.translation;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.cmx.workermanagemnt.cmx.config.TranslationProperties;
import com.cmx.workermanagemnt.cmx.exception.TranslationException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Component
@ConditionalOnExpression("'${cmx.translation.provider}' == 'azure' or ('${cmx.translation.provider}' == 'composite' and '${cmx.translation.cloud-provider:google}' == 'azure')")
public class AzureTranslationClient implements BatchTranslationClient {

	private static final Logger log = LoggerFactory.getLogger(AzureTranslationClient.class);

	private final RestClient restClient;
	private final TranslationProperties properties;
	private final Cache<String, String> cache;

	public AzureTranslationClient(TranslationProperties properties) {
		this.properties = properties;
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout((int) properties.getAzure().getTimeout().toMillis());
		requestFactory.setReadTimeout((int) properties.getAzure().getTimeout().toMillis());
		this.restClient = RestClient.builder()
				.baseUrl(properties.getAzure().getBaseUrl())
				.requestFactory(requestFactory)
				.build();
		this.cache = buildCache(properties);
	}

	@Override
	public List<String> translateBatch(List<String> texts, String sourceLanguage, String targetLanguage) {
		if (texts == null || texts.isEmpty()) {
			return List.of();
		}
		if (sourceLanguage.equalsIgnoreCase(targetLanguage)) {
			return new ArrayList<>(texts);
		}

		List<String> results = new ArrayList<>(texts.size());
		List<String> uncachedTexts = new ArrayList<>();
		List<Integer> uncachedIndexes = new ArrayList<>();
		boolean cacheEnabled = properties.getCache().isEnabled();

		for (int i = 0; i < texts.size(); i++) {
			String text = texts.get(i);
			if (!StringUtils.hasText(text)) {
				results.add(text);
				continue;
			}
			if (cacheEnabled) {
				String cached = cache.getIfPresent(cacheKey(sourceLanguage, targetLanguage, text));
				if (cached != null) {
					results.add(cached);
					continue;
				}
			}
			results.add(null);
			uncachedTexts.add(text);
			uncachedIndexes.add(i);
		}

		if (uncachedTexts.isEmpty()) {
			return results;
		}

		List<String> translated = callAzureApi(uncachedTexts, sourceLanguage, targetLanguage);
		for (int i = 0; i < uncachedIndexes.size(); i++) {
			int resultIndex = uncachedIndexes.get(i);
			String translatedText = translated.get(i);
			results.set(resultIndex, translatedText);
			if (cacheEnabled) {
				String cacheKey = cacheKey(sourceLanguage, targetLanguage, texts.get(resultIndex));
				cache.put(cacheKey, translatedText);
			}
		}

		log.debug("Translated {} characters from {} to {} via Azure ({} cache misses)",
				uncachedTexts.stream().mapToInt(String::length).sum(),
				sourceLanguage,
				targetLanguage,
				uncachedTexts.size());
		return results;
	}

	private List<String> callAzureApi(List<String> texts, String sourceLanguage, String targetLanguage) {
		String apiKey = properties.getAzure().getApiKey();
		if (!StringUtils.hasText(apiKey)) {
			throw new TranslationException("Azure Translator API key is not configured (cmx.translation.azure.api-key)");
		}

		List<AzureTranslateRequest> requestBody = texts.stream()
				.map(AzureTranslateRequest::new)
				.toList();

		try {
			AzureTranslateResponse[] response = restClient.post()
					.uri(uriBuilder -> uriBuilder
							.path("/translate")
							.queryParam("api-version", "3.0")
							.queryParam("from", sourceLanguage)
							.queryParam("to", targetLanguage)
							.build())
					.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.header("Ocp-Apim-Subscription-Key", apiKey)
					.headers(headers -> applyRegionHeader(headers))
					.body(requestBody)
					.retrieve()
					.body(AzureTranslateResponse[].class);

			if (response == null || response.length != texts.size()) {
				throw new TranslationException("Azure Translator API returned unexpected number of translations");
			}
			List<String> translated = new ArrayList<>(response.length);
			for (AzureTranslateResponse item : response) {
				if (item == null || item.translations() == null || item.translations().isEmpty()) {
					throw new TranslationException("Empty response from Azure Translator API");
				}
				translated.add(item.translations().get(0).text());
			}
			return translated;
		}
		catch (RestClientException ex) {
			throw new TranslationException("Azure Translator API call failed", ex);
		}
	}

	private void applyRegionHeader(HttpHeaders headers) {
		String region = properties.getAzure().getRegion();
		if (StringUtils.hasText(region)) {
			headers.add("Ocp-Apim-Subscription-Region", region);
		}
	}

	private Cache<String, String> buildCache(TranslationProperties properties) {
		if (!properties.getCache().isEnabled()) {
			return Caffeine.newBuilder().maximumSize(0).build();
		}
		Caffeine<Object, Object> builder = Caffeine.newBuilder()
				.maximumSize(properties.getCache().getMaxSize());
		Duration ttl = properties.getCache().getTtl();
		if (ttl != null && !ttl.isZero() && !ttl.isNegative()) {
			builder.expireAfterWrite(ttl);
		}
		return builder.build();
	}

	private String cacheKey(String sourceLanguage, String targetLanguage, String text) {
		return sourceLanguage + "|" + targetLanguage + "|" + text;
	}

	private record AzureTranslateRequest(String Text) {
	}

	private record AzureTranslateResponse(List<AzureTranslation> translations) {
	}

	private record AzureTranslation(String text, String to) {
	}
}
