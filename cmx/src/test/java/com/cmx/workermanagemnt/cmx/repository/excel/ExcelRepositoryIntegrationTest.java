package com.cmx.workermanagemnt.cmx.repository.excel;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.cmx.workermanagemnt.cmx.config.StorageProperties;
import com.cmx.workermanagemnt.cmx.domain.AvailabilityStatus;
import com.cmx.workermanagemnt.cmx.domain.Gender;
import com.cmx.workermanagemnt.cmx.domain.SkillLevel;
import com.cmx.workermanagemnt.cmx.domain.WorkType;
import com.cmx.workermanagemnt.cmx.domain.WorkerBasicInfo;
import com.cmx.workermanagemnt.cmx.domain.WorkerSkills;

@SpringBootTest
class ExcelRepositoryIntegrationTest {

	@Autowired
	private ExcelFileSupport excelFileSupport;

	@Autowired
	private StorageProperties storageProperties;

	@Autowired
	private ExcelWorkerBasicInfoRepository basicInfoRepository;

	@Autowired
	private ExcelWorkerSkillsRepository skillsRepository;

	@BeforeEach
	void setUp(@TempDir java.nio.file.Path tempDir) {
		storageProperties.getExcel().setBasePath(tempDir.toString());
		excelFileSupport.initializeWorkbooks();
	}

	@Test
	void savesAndLoadsWorkerRecords() {
		WorkerBasicInfo basicInfo = new WorkerBasicInfo();
		basicInfo.setId("worker-1");
		basicInfo.setFullName("Rajesh Kumar");
		basicInfo.setMobileNumber("+919876543210");
		basicInfo.setEmail("rajesh@example.com");
		basicInfo.setDateOfBirth(LocalDate.of(1990, 5, 15));
		basicInfo.setGender(Gender.MALE);
		basicInfo.setCity("Chennai");
		basicInfo.setState("Tamil Nadu");
		basicInfo.setAddress("12 MG Road");
		basicInfo.setPincode("600001");
		basicInfo.setPrimaryLanguage("hi");
		basicInfo.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
		basicInfo.setCreatedAt(Instant.parse("2026-06-13T10:00:00Z"));
		basicInfo.setUpdatedAt(Instant.parse("2026-06-13T10:00:00Z"));

		WorkerSkills skills = new WorkerSkills();
		skills.setUserId("worker-1");
		skills.setPrimarySkill("Welding");
		skills.getSecondarySkills().add("Painting");
		skills.setExperienceYears(new BigDecimal("5.5"));
		skills.setSkillLevel(SkillLevel.INTERMEDIATE);
		skills.getCertifications().add("ITI Welding");
		skills.getToolsOwned().add("Arc welder");
		skills.setWorkType(WorkType.FULL_TIME);
		skills.getLanguagesSpoken().add("hi");

		basicInfoRepository.save(basicInfo);
		skillsRepository.save(skills);

		WorkerBasicInfo loadedBasic = basicInfoRepository.findById("worker-1").orElseThrow();
		WorkerSkills loadedSkills = skillsRepository.findByUserId("worker-1").orElseThrow();

		assertThat(loadedBasic.getCity()).isEqualTo("Chennai");
		assertThat(loadedSkills.getSecondarySkills()).containsExactly("Painting");
		assertThat(basicInfoRepository.existsByMobileNumber("+919876543210")).isTrue();
	}
}
