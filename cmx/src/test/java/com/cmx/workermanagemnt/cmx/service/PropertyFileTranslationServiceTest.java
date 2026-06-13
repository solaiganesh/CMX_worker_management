package com.cmx.workermanagemnt.cmx.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.cmx.workermanagemnt.cmx.domain.AvailabilityStatus;
import com.cmx.workermanagemnt.cmx.domain.Gender;
import com.cmx.workermanagemnt.cmx.domain.SkillLevel;
import com.cmx.workermanagemnt.cmx.domain.WorkType;
import com.cmx.workermanagemnt.cmx.domain.WorkerBasicInfo;
import com.cmx.workermanagemnt.cmx.domain.WorkerSkills;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerBasicInfoDto;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerRegistrationRequest;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerResponse;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerSkillsDto;

@SpringBootTest
class PropertyFileTranslationServiceTest {

	@Autowired
	private TranslationService translationService;

	@Test
	void translatesEnglishToHindiOnRead() {
		WorkerBasicInfo basicInfo = new WorkerBasicInfo();
		basicInfo.setId("1");
		basicInfo.setCity("Chennai");
		basicInfo.setState("Tamil Nadu");
		basicInfo.setAddress("12 MG Road");

		WorkerSkills skills = new WorkerSkills();
		skills.setUserId("1");
		skills.setPrimarySkill("Welding");
		skills.getSecondarySkills().add("Painting");

		WorkerResponse response = translationService.fromEnglish(basicInfo, skills, Locale.forLanguageTag("hi"));

		assertThat(response.getCity()).isEqualTo("चेन्नई");
		assertThat(response.getPrimarySkill()).isEqualTo("वेल्डिंग");
	}

	@Test
	void keepsEnglishWhenLocaleIsEnglish() {
		WorkerBasicInfo basicInfo = new WorkerBasicInfo();
		basicInfo.setCity("Chennai");

		WorkerSkills skills = new WorkerSkills();
		skills.setPrimarySkill("Welding");

		WorkerResponse response = translationService.fromEnglish(basicInfo, skills, Locale.ENGLISH);

		assertThat(response.getCity()).isEqualTo("Chennai");
		assertThat(response.getPrimarySkill()).isEqualTo("Welding");
	}

	@Test
	void translatesHindiInputToEnglishOnWrite() {
		WorkerRegistrationRequest request = new WorkerRegistrationRequest();
		WorkerBasicInfoDto basic = new WorkerBasicInfoDto();
		basic.setCity("चेन्नई");
		basic.setState("तमिल नाडु");
		basic.setAddress("12 एमजी रोड");
		request.setBasicInfo(basic);

		WorkerSkillsDto skills = new WorkerSkillsDto();
		skills.setPrimarySkill("वेल्डिंग");
		request.setSkills(skills);

		WorkerRegistrationRequest english = translationService.toEnglish(request, Locale.forLanguageTag("hi"));

		assertThat(english.getBasicInfo().getCity()).isEqualTo("Chennai");
		assertThat(english.getSkills().getPrimarySkill()).isEqualTo("Welding");
	}
}
