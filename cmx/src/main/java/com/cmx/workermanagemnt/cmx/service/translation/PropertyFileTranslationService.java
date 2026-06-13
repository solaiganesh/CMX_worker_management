package com.cmx.workermanagemnt.cmx.service.translation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

import com.cmx.workermanagemnt.cmx.domain.WorkerBasicInfo;
import com.cmx.workermanagemnt.cmx.domain.WorkerSkills;
import com.cmx.workermanagemnt.cmx.service.TranslationService;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerBasicInfoDto;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerRegistrationRequest;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerResponse;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerSkillsDto;

public class PropertyFileTranslationService implements TranslationService {

	private final PropertyFileDictionary dictionary;

	public PropertyFileTranslationService(PropertyFileDictionary dictionary) {
		this.dictionary = dictionary;
	}

	@Override
	public WorkerRegistrationRequest toEnglish(WorkerRegistrationRequest request, Locale sourceLocale) {
		if (TranslatableWorkerFields.isEnglish(sourceLocale)) {
			return request;
		}
		String language = sourceLocale.getLanguage();
		WorkerRegistrationRequest english = TranslatableWorkerFields.copyRequest(request);
		WorkerBasicInfoDto basic = english.getBasicInfo();
		basic.setCity(toEnglishValue(basic.getCity(), language));
		basic.setState(toEnglishValue(basic.getState(), language));
		basic.setAddress(toEnglishValue(basic.getAddress(), language));

		WorkerSkillsDto skills = english.getSkills();
		skills.setPrimarySkill(toEnglishValue(skills.getPrimarySkill(), language));
		skills.setSecondarySkills(toEnglishList(skills.getSecondarySkills(), language));
		skills.setCertifications(toEnglishList(skills.getCertifications(), language));
		skills.setToolsOwned(toEnglishList(skills.getToolsOwned(), language));
		return english;
	}

	@Override
	public WorkerResponse fromEnglish(WorkerBasicInfo basicInfo, WorkerSkills skills, Locale targetLocale) {
		if (TranslatableWorkerFields.isEnglish(targetLocale)) {
			return WorkerResponse.fromDomain(basicInfo, skills);
		}
		String language = targetLocale.getLanguage();
		WorkerBasicInfo localizedBasic = TranslatableWorkerFields.copyBasicInfo(basicInfo);
		localizedBasic.setCity(fromEnglishValue(basicInfo.getCity(), language));
		localizedBasic.setState(fromEnglishValue(basicInfo.getState(), language));
		localizedBasic.setAddress(fromEnglishValue(basicInfo.getAddress(), language));

		WorkerSkills localizedSkills = TranslatableWorkerFields.copySkills(skills);
		localizedSkills.setPrimarySkill(fromEnglishValue(skills.getPrimarySkill(), language));
		localizedSkills.setSecondarySkills(fromEnglishList(skills.getSecondarySkills(), language));
		localizedSkills.setCertifications(fromEnglishList(skills.getCertifications(), language));
		localizedSkills.setToolsOwned(fromEnglishList(skills.getToolsOwned(), language));
		return WorkerResponse.fromDomain(localizedBasic, localizedSkills);
	}

	String toEnglishValue(String localizedValue, String language) {
		return dictionary.toEnglish(localizedValue, language);
	}

	String fromEnglishValue(String englishValue, String language) {
		return dictionary.fromEnglish(englishValue, language);
	}

	boolean hasToEnglishMapping(String localizedValue, String language) {
		return dictionary.hasToEnglishMapping(localizedValue, language);
	}

	boolean hasFromEnglishMapping(String englishValue, String language) {
		return dictionary.hasFromEnglishMapping(englishValue, language);
	}

	private List<String> fromEnglishList(List<String> englishValues, String language) {
		if (englishValues == null) {
			return List.of();
		}
		return englishValues.stream()
				.map(value -> fromEnglishValue(value, language))
				.collect(Collectors.toCollection(ArrayList::new));
	}

	private List<String> toEnglishList(List<String> localizedValues, String language) {
		if (localizedValues == null) {
			return List.of();
		}
		return localizedValues.stream()
				.map(value -> toEnglishValue(value, language))
				.collect(Collectors.toCollection(ArrayList::new));
	}
}
