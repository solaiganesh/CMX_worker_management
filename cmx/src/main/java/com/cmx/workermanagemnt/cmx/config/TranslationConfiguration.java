package com.cmx.workermanagemnt.cmx.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cmx.workermanagemnt.cmx.service.TranslationService;
import com.cmx.workermanagemnt.cmx.service.translation.BatchTranslationClient;
import com.cmx.workermanagemnt.cmx.service.translation.CloudTranslationService;
import com.cmx.workermanagemnt.cmx.service.translation.CompositeTranslationService;
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
	TranslationService googleTranslationService(BatchTranslationClient client, TranslationProperties properties) {
		return new CloudTranslationService(client, properties);
	}

	@Bean
	@ConditionalOnProperty(name = "cmx.translation.provider", havingValue = "azure")
	TranslationService azureTranslationService(BatchTranslationClient client, TranslationProperties properties) {
		return new CloudTranslationService(client, properties);
	}

	@Bean
	@ConditionalOnProperty(name = "cmx.translation.provider", havingValue = "composite")
	TranslationService compositeTranslationService(PropertyFileDictionary dictionary, BatchTranslationClient client,
			TranslationProperties properties) {
		PropertyFileTranslationService propertyFileTranslationService = new PropertyFileTranslationService(dictionary);
		return new CompositeTranslationService(propertyFileTranslationService, client, properties);
	}
}
