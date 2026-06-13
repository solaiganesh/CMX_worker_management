package com.cmx.workermanagemnt.cmx.service;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.cmx.workermanagemnt.cmx.service.translation.GoogleTranslationClient;
import com.cmx.workermanagemnt.cmx.service.translation.PropertyFileDictionary;

@Component
public class CityTranslationSupport {

	private final ObjectProvider<PropertyFileDictionary> propertyFileDictionary;
	private final ObjectProvider<GoogleTranslationClient> googleTranslationClient;

	public CityTranslationSupport(ObjectProvider<PropertyFileDictionary> propertyFileDictionary,
			ObjectProvider<GoogleTranslationClient> googleTranslationClient) {
		this.propertyFileDictionary = propertyFileDictionary;
		this.googleTranslationClient = googleTranslationClient;
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
		GoogleTranslationClient client = googleTranslationClient.getIfAvailable();
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
		GoogleTranslationClient client = googleTranslationClient.getIfAvailable();
		if (client != null) {
			return client.translateBatch(List.of(city), "en", language).get(0);
		}
		return city;
	}
}
