package com.cmx.workermanagemnt.cmx.service.translation;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
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
@ConditionalOnExpression("'${cmx.translation.provider}' == 'google' or '${cmx.translation.provider}' == 'composite'")
public class GoogleTranslationClient {

	private static final Logger log = LoggerFactory.getLogger(GoogleTranslationClient.class);

	private final RestClient restClient;
	private final TranslationProperties properties;
	private final Cache<String, String> cache;

	public GoogleTranslationClient(TranslationProperties properties) {
		this.properties = properties;
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout((int) properties.getGoogle().getTimeout().toMillis());
		requestFactory.setReadTimeout((int) properties.getGoogle().getTimeout().toMillis());
		this.restClient = RestClient.builder()
				.baseUrl(properties.getGoogle().getBaseUrl())
				.requestFactory(requestFactory)
				.build();
		this.cache = buildCache(properties);
	}

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

		List<String> translated = callGoogleApi(uncachedTexts, sourceLanguage, targetLanguage);
		for (int i = 0; i < uncachedIndexes.size(); i++) {
			int resultIndex = uncachedIndexes.get(i);
			String translatedText = translated.get(i);
			results.set(resultIndex, translatedText);
			if (cacheEnabled) {
				String cacheKey = cacheKey(sourceLanguage, targetLanguage, texts.get(resultIndex));
				cache.put(cacheKey, translatedText);
			}
		}

		log.debug("Translated {} characters from {} to {} ({} cache misses)",
				uncachedTexts.stream().mapToInt(String::length).sum(),
				sourceLanguage,
				targetLanguage,
				uncachedTexts.size());
		return results;
	}

	private List<String> callGoogleApi(List<String> texts, String sourceLanguage, String targetLanguage) {
		String apiKey = properties.getGoogle().getApiKey();
		if (!StringUtils.hasText(apiKey)) {
			throw new TranslationException("Google Translate API key is not configured (cmx.translation.google.api-key)");
		}

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("q", texts);
		requestBody.put("source", sourceLanguage);
		requestBody.put("target", targetLanguage);
		requestBody.put("format", "text");

		try {
			GoogleTranslateResponse response = restClient.post()
					.uri(uriBuilder -> uriBuilder.queryParam("key", apiKey).build())
					.body(requestBody)
					.retrieve()
					.body(GoogleTranslateResponse.class);

			if (response == null || response.data() == null || response.data().translations() == null) {
				throw new TranslationException("Empty response from Google Translate API");
			}
			if (response.data().translations().size() != texts.size()) {
				throw new TranslationException("Google Translate API returned unexpected number of translations");
			}
			return response.data().translations().stream()
					.map(GoogleTranslation::translatedText)
					.toList();
		}
		catch (RestClientException ex) {
			throw new TranslationException("Google Translate API call failed", ex);
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

	private record GoogleTranslateResponse(GoogleTranslateData data) {
	}

	private record GoogleTranslateData(List<GoogleTranslation> translations) {
	}

	private record GoogleTranslation(String translatedText) {
	}
}
