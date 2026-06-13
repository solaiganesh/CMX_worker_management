package com.cmx.workermanagemnt.cmx.service.translation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
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
import com.cmx.workermanagemnt.cmx.domain.WorkerBasicInfo;
import com.cmx.workermanagemnt.cmx.domain.WorkerSkills;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerBasicInfoDto;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerRegistrationRequest;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerResponse;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerSkillsDto;

@ExtendWith(MockitoExtension.class)
class GoogleCloudTranslationServiceTest {

	@Mock
	private GoogleTranslationClient client;

	private GoogleCloudTranslationService service;

	@BeforeEach
	void setUp() {
		service = new GoogleCloudTranslationService(client, new TranslationProperties());
	}

	@Test
	void skipsTranslationWhenLocaleIsEnglish() {
		WorkerRegistrationRequest request = sampleRequest("Chennai", "Welding");

		WorkerRegistrationRequest result = service.toEnglish(request, Locale.ENGLISH);

		assertThat(result).isSameAs(request);
	}

	@Test
	void batchesTranslatableFieldsToEnglishOnWrite() {
		WorkerRegistrationRequest request = sampleRequest("चेन्नई", "वेल्डिंग");
		when(client.translateBatch(List.of("चेन्नई", "वेल्डिंग"), "hi", "en"))
				.thenReturn(List.of("Chennai", "Welding"));

		WorkerRegistrationRequest result = service.toEnglish(request, Locale.forLanguageTag("hi"));

		assertThat(result.getBasicInfo().getCity()).isEqualTo("Chennai");
		assertThat(result.getSkills().getPrimarySkill()).isEqualTo("Welding");
		verify(client).translateBatch(eq(List.of("चेन्नई", "वेल्डिंग")), eq("hi"), eq("en"));
	}

	@Test
	void batchesTranslatableFieldsFromEnglishOnRead() {
		WorkerBasicInfo basicInfo = new WorkerBasicInfo();
		basicInfo.setCity("Chennai");
		basicInfo.setState("Tamil Nadu");
		basicInfo.setAddress("12 MG Road");

		WorkerSkills skills = new WorkerSkills();
		skills.setPrimarySkill("Welding");

		when(client.translateBatch(List.of("Chennai", "Tamil Nadu", "12 MG Road", "Welding"), "en", "hi"))
				.thenReturn(List.of("चेन्नई", "तमिल नाडु", "12 एमजी रोड", "वेल्डिंग"));

		WorkerResponse response = service.fromEnglish(basicInfo, skills, Locale.forLanguageTag("hi"));

		assertThat(response.getCity()).isEqualTo("चेन्नई");
		assertThat(response.getPrimarySkill()).isEqualTo("वेल्डिंग");
	}

	private WorkerRegistrationRequest sampleRequest(String city, String primarySkill) {
		WorkerRegistrationRequest request = new WorkerRegistrationRequest();
		WorkerBasicInfoDto basic = new WorkerBasicInfoDto();
		basic.setCity(city);
		request.setBasicInfo(basic);
		WorkerSkillsDto skills = new WorkerSkillsDto();
		skills.setPrimarySkill(primarySkill);
		request.setSkills(skills);
		return request;
	}
}
