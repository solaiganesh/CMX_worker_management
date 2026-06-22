package com.cmx.workermanagemnt.cmx.service.translation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cmx.workermanagemnt.cmx.config.TranslationProperties;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerBasicInfoDto;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerRegistrationRequest;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerSkillsDto;

@ExtendWith(MockitoExtension.class)
class CompositeTranslationServiceTest {

	@Mock
	private BatchTranslationClient cloudTranslationClient;

	private PropertyFileDictionary dictionary;
	private PropertyFileTranslationService propertyFileTranslationService;
	private CompositeTranslationService service;

	@BeforeEach
	void setUp() throws Exception {
		dictionary = new PropertyFileDictionary();
		dictionary.loadTranslations();
		propertyFileTranslationService = new PropertyFileTranslationService(dictionary);
		service = new CompositeTranslationService(propertyFileTranslationService, cloudTranslationClient,
				new TranslationProperties());
	}

	@Test
	void usesPropertyFileForKnownValuesWithoutCallingCloud() {
		WorkerRegistrationRequest request = new WorkerRegistrationRequest();
		WorkerBasicInfoDto basic = new WorkerBasicInfoDto();
		basic.setCity("चेन्नई");
		request.setBasicInfo(basic);
		WorkerSkillsDto skills = new WorkerSkillsDto();
		skills.setPrimarySkill("वेल्डिंग");
		request.setSkills(skills);

		WorkerRegistrationRequest result = service.toEnglish(request, Locale.forLanguageTag("hi"));

		assertThat(result.getBasicInfo().getCity()).isEqualTo("Chennai");
		assertThat(result.getSkills().getPrimarySkill()).isEqualTo("Welding");
		verify(cloudTranslationClient, never()).translateBatch(anyList(), eq("hi"), eq("en"));
	}

	@Test
	void fallsBackToCloudForUnknownValues() {
		WorkerRegistrationRequest request = new WorkerRegistrationRequest();
		WorkerBasicInfoDto basic = new WorkerBasicInfoDto();
		basic.setAddress("Unknown street address");
		request.setBasicInfo(basic);
		WorkerSkillsDto skills = new WorkerSkillsDto();
		request.setSkills(skills);

		when(cloudTranslationClient.translateBatch(List.of("Unknown street address"), "hi", "en"))
				.thenReturn(List.of("Unknown street address in English"));

		WorkerRegistrationRequest result = service.toEnglish(request, Locale.forLanguageTag("hi"));

		assertThat(result.getBasicInfo().getAddress()).isEqualTo("Unknown street address in English");
		verify(cloudTranslationClient).translateBatch(List.of("Unknown street address"), "hi", "en");
	}
}
