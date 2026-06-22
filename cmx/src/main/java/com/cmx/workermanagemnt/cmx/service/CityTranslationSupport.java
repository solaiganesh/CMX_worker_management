package com.cmx.workermanagemnt.cmx.service;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.cmx.workermanagemnt.cmx.service.translation.BatchTranslationClient;
import com.cmx.workermanagemnt.cmx.service.translation.PropertyFileDictionary;

@Component
public class CityTranslationSupport {

	private final ObjectProvider<PropertyFileDictionary> propertyFileDictionary;
	private final ObjectProvider<BatchTranslationClient> cloudTranslationClient;

	public CityTranslationSupport(ObjectProvider<PropertyFileDictionary> propertyFileDictionary,
			ObjectProvider<BatchTranslationClient> cloudTranslationClient) {
		this.propertyFileDictionary = propertyFileDictionary;
		this.cloudTranslationClient = cloudTranslationClient;
	}

	public String toEnglish(String city, Locale sourceLocale) {
		if (!StringUtils.hasText(city) || LocaleSupport.isEnglish(sourceLocale)) {
			return city;
		}
		String language = sourceLocale.getLanguage();
		PropertyFileDictionary dictionary = propertyFileDictionary.getIfAvailable();
		if (dictionary != null) {
			String fromProperty = dictionary.toEnglish(city, language);
			if (!fromProperty.equals(city)) {
				return fromProperty;
			}
		}
		BatchTranslationClient client = cloudTranslationClient.getIfAvailable();
		if (client != null) {
			return client.translateBatch(List.of(city), language, "en").get(0);
		}
		return city;
	}

	public String fromEnglish(String city, Locale targetLocale) {
		if (!StringUtils.hasText(city) || LocaleSupport.isEnglish(targetLocale)) {
			return city;
		}
		String language = targetLocale.getLanguage();
		PropertyFileDictionary dictionary = propertyFileDictionary.getIfAvailable();
		if (dictionary != null) {
			String fromProperty = dictionary.fromEnglish(city, language);
			if (!fromProperty.equals(city)) {
				return fromProperty;
			}
		}
		BatchTranslationClient client = cloudTranslationClient.getIfAvailable();
		if (client != null) {
			return client.translateBatch(List.of(city), "en", language).get(0);
		}
		return city;
	}
}
