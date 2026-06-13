package com.cmx.workermanagemnt.cmx.service.translation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cmx.workermanagemnt.cmx.config.TranslationProperties;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

class GoogleTranslationClientTest {

	private MockWebServer mockWebServer;
	private GoogleTranslationClient client;

	@BeforeEach
	void setUp() throws IOException {
		mockWebServer = new MockWebServer();
		mockWebServer.start();
		TranslationProperties properties = new TranslationProperties();
		properties.getGoogle().setApiKey("test-api-key");
		properties.getGoogle().setBaseUrl(mockWebServer.url("/language/translate/v2").toString());
		properties.getCache().setEnabled(true);
		client = new GoogleTranslationClient(properties);
	}

	@AfterEach
	void tearDown() throws IOException {
		mockWebServer.shutdown();
	}

	@Test
	void translatesBatchViaGoogleApi() {
		mockWebServer.enqueue(new MockResponse()
				.setHeader("Content-Type", "application/json")
				.setBody("""
						{
						  "data": {
						    "translations": [
						      {"translatedText": "Chennai"},
						      {"translatedText": "Welding"}
						    ]
						  }
						}
						"""));

		List<String> translated = client.translateBatch(List.of("चेन्नई", "वेल्डिंग"), "hi", "en");

		assertThat(translated).containsExactly("Chennai", "Welding");
		assertThat(client.translateBatch(List.of("चेन्नई", "वेल्डिंग"), "hi", "en"))
				.containsExactly("Chennai", "Welding");
		assertThat(mockWebServer.getRequestCount()).isEqualTo(1);
	}

	@Test
	void returnsSameTextsWhenSourceAndTargetMatch() {
		List<String> translated = client.translateBatch(List.of("Chennai"), "en", "en");

		assertThat(translated).containsExactly("Chennai");
		assertThat(mockWebServer.getRequestCount()).isZero();
	}
}
