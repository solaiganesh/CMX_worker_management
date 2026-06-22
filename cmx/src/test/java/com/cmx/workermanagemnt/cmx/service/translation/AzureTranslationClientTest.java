package com.cmx.workermanagemnt.cmx.service.translation;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cmx.workermanagemnt.cmx.config.TranslationProperties;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

class AzureTranslationClientTest {

	private MockWebServer mockWebServer;
	private AzureTranslationClient client;

	@BeforeEach
	void setUp() throws IOException {
		mockWebServer = new MockWebServer();
		mockWebServer.start();
		TranslationProperties properties = new TranslationProperties();
		properties.getAzure().setApiKey("test-api-key");
		properties.getAzure().setRegion("eastus");
		properties.getAzure().setBaseUrl(mockWebServer.url("/").toString().replaceAll("/$", ""));
		properties.getCache().setEnabled(true);
		client = new AzureTranslationClient(properties);
	}

	@AfterEach
	void tearDown() throws IOException {
		mockWebServer.shutdown();
	}

	@Test
	void translatesBatchViaAzureApi() throws InterruptedException {
		mockWebServer.enqueue(new MockResponse()
				.setHeader("Content-Type", "application/json")
				.setBody("""
						[
						  {
						    "translations": [
						      {"text": "Chennai", "to": "en"}
						    ]
						  },
						  {
						    "translations": [
						      {"text": "Welding", "to": "en"}
						    ]
						  }
						]
						"""));

		List<String> translated = client.translateBatch(List.of("चेन्नई", "वेल्डिंग"), "hi", "en");

		assertThat(translated).containsExactly("Chennai", "Welding");
		assertThat(client.translateBatch(List.of("चेन्नई", "वेल्डिंग"), "hi", "en"))
				.containsExactly("Chennai", "Welding");
		assertThat(mockWebServer.getRequestCount()).isEqualTo(1);
		var request = mockWebServer.takeRequest();
		assertThat(request.getHeader("Ocp-Apim-Subscription-Key")).isEqualTo("test-api-key");
		assertThat(request.getHeader("Ocp-Apim-Subscription-Region")).isEqualTo("eastus");
		assertThat(request.getPath()).contains("/translate");
	}

	@Test
	void returnsSameTextsWhenSourceAndTargetMatch() {
		List<String> translated = client.translateBatch(List.of("Chennai"), "en", "en");

		assertThat(translated).containsExactly("Chennai");
		assertThat(mockWebServer.getRequestCount()).isZero();
	}
}
