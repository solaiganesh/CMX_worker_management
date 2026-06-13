package com.cmx.workermanagemnt.cmx.service.translation;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.cmx.workermanagemnt.cmx.service.PropertiesLoader;

import jakarta.annotation.PostConstruct;

@Component
@ConditionalOnExpression("'${cmx.translation.provider:property-file}' == 'property-file' or '${cmx.translation.provider:property-file}' == 'composite'")
public class PropertyFileDictionary {

	private final Map<String, Map<String, String>> englishToLocale = new HashMap<>();
	private final Map<String, Map<String, String>> localeToEnglish = new HashMap<>();

	@PostConstruct
	void loadTranslations() {
		loadLocale("hi", "translations/values_hi.properties");
	}

	public String toEnglish(String localizedValue, String language) {
		if (!StringUtils.hasText(localizedValue)) {
			return localizedValue;
		}
		return localeToEnglish.getOrDefault(language, Map.of()).getOrDefault(localizedValue, localizedValue);
	}

	public String fromEnglish(String englishValue, String language) {
		if (!StringUtils.hasText(englishValue)) {
			return englishValue;
		}
		return englishToLocale.getOrDefault(language, Map.of()).getOrDefault(englishValue, englishValue);
	}

	public boolean hasToEnglishMapping(String localizedValue, String language) {
		if (!StringUtils.hasText(localizedValue)) {
			return false;
		}
		String mapped = localeToEnglish.getOrDefault(language, Map.of()).get(localizedValue);
		return mapped != null && !mapped.equals(localizedValue);
	}

	public boolean hasFromEnglishMapping(String englishValue, String language) {
		if (!StringUtils.hasText(englishValue)) {
			return false;
		}
		String mapped = englishToLocale.getOrDefault(language, Map.of()).get(englishValue);
		return mapped != null && !mapped.equals(englishValue);
	}

	private void loadLocale(String language, String resourcePath) {
		try {
			ClassPathResource resource = new ClassPathResource(resourcePath);
			Map<String, String> forward = new HashMap<>();
			Map<String, String> reverse = new HashMap<>();
			PropertiesLoader.load(resource.getInputStream()).forEach((english, localized) -> {
				forward.put(english, localized);
				reverse.put(localized, english);
			});
			englishToLocale.put(language, forward);
			localeToEnglish.put(language, reverse);
		}
		catch (Exception ex) {
			throw new IllegalStateException("Failed to load translations: " + resourcePath, ex);
		}
	}
}
