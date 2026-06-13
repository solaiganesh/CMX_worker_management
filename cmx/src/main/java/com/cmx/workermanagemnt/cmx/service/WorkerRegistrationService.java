package com.cmx.workermanagemnt.cmx.service;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.cmx.workermanagemnt.cmx.domain.WorkerBasicInfo;
import com.cmx.workermanagemnt.cmx.domain.WorkerSkills;
import com.cmx.workermanagemnt.cmx.exception.DuplicateWorkerException;
import com.cmx.workermanagemnt.cmx.exception.WorkerNotFoundException;
import com.cmx.workermanagemnt.cmx.repository.WorkerBasicInfoRepository;
import com.cmx.workermanagemnt.cmx.repository.WorkerSkillsRepository;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerBasicInfoDto;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerRegistrationRequest;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerResponse;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerSkillsDto;
import com.cmx.workermanagemnt.cmx.web.mapper.WorkerMapper;

@Service
public class WorkerRegistrationService {

	private final WorkerBasicInfoRepository basicInfoRepository;
	private final WorkerSkillsRepository skillsRepository;
	private final TranslationService translationService;

	public WorkerRegistrationService(WorkerBasicInfoRepository basicInfoRepository,
			WorkerSkillsRepository skillsRepository, TranslationService translationService) {
		this.basicInfoRepository = basicInfoRepository;
		this.skillsRepository = skillsRepository;
		this.translationService = translationService;
	}

	public WorkerResponse register(WorkerRegistrationRequest request, Locale inputLocale, Locale responseLocale) {
		WorkerRegistrationRequest englishRequest = translationService.toEnglish(request, inputLocale);

		if (basicInfoRepository.existsByMobileNumber(englishRequest.getBasicInfo().getMobileNumber())) {
			throw new DuplicateWorkerException("DUPLICATE_MOBILE", "basicInfo.mobileNumber",
					"Mobile number already registered");
		}
		if (basicInfoRepository.existsByEmail(englishRequest.getBasicInfo().getEmail())) {
			throw new DuplicateWorkerException("DUPLICATE_EMAIL", "basicInfo.email", "Email already registered");
		}

		String workerId = UUID.randomUUID().toString();
		Instant now = Instant.now();

		WorkerBasicInfo basicInfo = WorkerMapper.toBasicInfoEntity(englishRequest.getBasicInfo());
		basicInfo.setId(workerId);
		basicInfo.setCreatedAt(now);
		basicInfo.setUpdatedAt(now);

		WorkerSkills skills = WorkerMapper.toSkillsEntity(englishRequest.getSkills());
		skills.setUserId(workerId);

		try {
			basicInfoRepository.save(basicInfo);
			skillsRepository.save(skills);
		}
		catch (RuntimeException ex) {
			basicInfoRepository.deleteById(workerId);
			skillsRepository.deleteByUserId(workerId);
			throw ex;
		}

		return translationService.fromEnglish(basicInfo, skills, responseLocale);
	}

	public WorkerResponse getById(String id, Locale responseLocale) {
		WorkerBasicInfo basicInfo = basicInfoRepository.findById(id)
				.orElseThrow(() -> new WorkerNotFoundException(id));
		WorkerSkills skills = skillsRepository.findByUserId(id)
				.orElseThrow(() -> new WorkerNotFoundException(id));
		return translationService.fromEnglish(basicInfo, skills, responseLocale);
	}
}
