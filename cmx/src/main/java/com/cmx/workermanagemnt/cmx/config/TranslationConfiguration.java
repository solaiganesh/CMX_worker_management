package com.cmx.workermanagemnt.cmx.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cmx.workermanagemnt.cmx.service.TranslationService;
import com.cmx.workermanagemnt.cmx.service.translation.CompositeTranslationService;
import com.cmx.workermanagemnt.cmx.service.translation.GoogleCloudTranslationService;
import com.cmx.workermanagemnt.cmx.service.translation.GoogleTranslationClient;
import com.cmx.workermanagemnt.cmx.service.translation.PropertyFileDictionary;
import com.cmx.workermanagemnt.cmx.service.translation.PropertyFileTranslationService;

@Configuration
public class TranslationConfiguration {

	@Bean
	@ConditionalOnProperty(name = "cmx.translation.provider", havingValue = "property-file", matchIfMissing = true)
	TranslationService propertyFileTranslationService(PropertyFileDictionary dictionary) {
		return new PropertyFileTranslationService(dictionary);
	}

	@Bean
	@ConditionalOnProperty(name = "cmx.translation.provider", havingValue = "google")
	TranslationService googleTranslationService(GoogleTranslationClient client, TranslationProperties properties) {
		return new GoogleCloudTranslationService(client, properties);
	}

	@Bean
	@ConditionalOnProperty(name = "cmx.translation.provider", havingValue = "composite")
	TranslationService compositeTranslationService(PropertyFileDictionary dictionary, GoogleTranslationClient client,
			TranslationProperties properties) {
		PropertyFileTranslationService propertyFileTranslationService = new PropertyFileTranslationService(dictionary);
		return new CompositeTranslationService(propertyFileTranslationService, client, properties);
	}
}
