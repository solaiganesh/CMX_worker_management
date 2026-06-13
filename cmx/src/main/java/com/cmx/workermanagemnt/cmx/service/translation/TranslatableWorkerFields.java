package com.cmx.workermanagemnt.cmx.service.translation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.util.StringUtils;

import com.cmx.workermanagemnt.cmx.domain.WorkerBasicInfo;
import com.cmx.workermanagemnt.cmx.domain.WorkerSkills;
import com.cmx.workermanagemnt.cmx.service.LocaleSupport;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerBasicInfoDto;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerRegistrationRequest;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerSkillsDto;

public final class TranslatableWorkerFields {

	private TranslatableWorkerFields() {
	}

	public static boolean isEnglish(Locale locale) {
		return LocaleSupport.isEnglish(locale);
	}

	public static WorkerRegistrationRequest copyRequest(WorkerRegistrationRequest request) {
		WorkerRegistrationRequest copy = new WorkerRegistrationRequest();
		copy.setBasicInfo(copyBasicDto(request.getBasicInfo()));
		copy.setSkills(copySkillsDto(request.getSkills()));
		return copy;
	}

	public static WorkerBasicInfo copyBasicInfo(WorkerBasicInfo source) {
		WorkerBasicInfo copy = new WorkerBasicInfo();
		copy.setId(source.getId());
		copy.setFullName(source.getFullName());
		copy.setMobileNumber(source.getMobileNumber());
		copy.setEmail(source.getEmail());
		copy.setProfilePhotoUrl(source.getProfilePhotoUrl());
		copy.setDateOfBirth(source.getDateOfBirth());
		copy.setGender(source.getGender());
		copy.setCity(source.getCity());
		copy.setState(source.getState());
		copy.setAddress(source.getAddress());
		copy.setPincode(source.getPincode());
		copy.setLatitude(source.getLatitude());
		copy.setLongitude(source.getLongitude());
		copy.setPrimaryLanguage(source.getPrimaryLanguage());
		copy.setAvailabilityStatus(source.getAvailabilityStatus());
		copy.setCreatedAt(source.getCreatedAt());
		copy.setUpdatedAt(source.getUpdatedAt());
		return copy;
	}

	public static WorkerSkills copySkills(WorkerSkills source) {
		WorkerSkills copy = new WorkerSkills();
		copy.setUserId(source.getUserId());
		copy.setPrimarySkill(source.getPrimarySkill());
		copy.setSecondarySkills(new ArrayList<>(source.getSecondarySkills()));
		copy.setExperienceYears(source.getExperienceYears());
		copy.setSkillLevel(source.getSkillLevel());
		copy.setCertifications(new ArrayList<>(source.getCertifications()));
		copy.setToolsOwned(new ArrayList<>(source.getToolsOwned()));
		copy.setWorkType(source.getWorkType());
		copy.setLanguagesSpoken(new ArrayList<>(source.getLanguagesSpoken()));
		copy.setPortfolioImages(new ArrayList<>(source.getPortfolioImages()));
		return copy;
	}

	public static List<String> collectFromRequest(WorkerRegistrationRequest request) {
		List<String> texts = new ArrayList<>();
		WorkerBasicInfoDto basic = request.getBasicInfo();
		WorkerSkillsDto skills = request.getSkills();
		addIfHasText(texts, basic.getCity());
		addIfHasText(texts, basic.getState());
		addIfHasText(texts, basic.getAddress());
		addIfHasText(texts, skills.getPrimarySkill());
		addListIfPresent(texts, skills.getSecondarySkills());
		addListIfPresent(texts, skills.getCertifications());
		addListIfPresent(texts, skills.getToolsOwned());
		return texts;
	}

	public static void applyToRequest(WorkerRegistrationRequest request, List<String> translated) {
		int index = 0;
		WorkerBasicInfoDto basic = request.getBasicInfo();
		WorkerSkillsDto skills = request.getSkills();
		index = applyIfHasText(basic::setCity, basic.getCity(), translated, index);
		index = applyIfHasText(basic::setState, basic.getState(), translated, index);
		index = applyIfHasText(basic::setAddress, basic.getAddress(), translated, index);
		index = applyIfHasText(skills::setPrimarySkill, skills.getPrimarySkill(), translated, index);
		index = applyList(skills::setSecondarySkills, skills.getSecondarySkills(), translated, index);
		index = applyList(skills::setCertifications, skills.getCertifications(), translated, index);
		applyList(skills::setToolsOwned, skills.getToolsOwned(), translated, index);
	}

	public static List<String> collectFromDomain(WorkerBasicInfo basicInfo, WorkerSkills skills) {
		List<String> texts = new ArrayList<>();
		addIfHasText(texts, basicInfo.getCity());
		addIfHasText(texts, basicInfo.getState());
		addIfHasText(texts, basicInfo.getAddress());
		addIfHasText(texts, skills.getPrimarySkill());
		addListIfPresent(texts, skills.getSecondarySkills());
		addListIfPresent(texts, skills.getCertifications());
		addListIfPresent(texts, skills.getToolsOwned());
		return texts;
	}

	public static WorkerBasicInfo applyToBasicInfo(WorkerBasicInfo basicInfo, List<String> translated) {
		int index = 0;
		index = applyIfHasText(basicInfo::setCity, basicInfo.getCity(), translated, index);
		index = applyIfHasText(basicInfo::setState, basicInfo.getState(), translated, index);
		applyIfHasText(basicInfo::setAddress, basicInfo.getAddress(), translated, index);
		return basicInfo;
	}

	public static WorkerSkills applyToSkills(WorkerSkills skills, List<String> translated, int startIndex) {
		int index = startIndex;
		index = applyIfHasText(skills::setPrimarySkill, skills.getPrimarySkill(), translated, index);
		index = applyList(skills::setSecondarySkills, skills.getSecondarySkills(), translated, index);
		index = applyList(skills::setCertifications, skills.getCertifications(), translated, index);
		applyList(skills::setToolsOwned, skills.getToolsOwned(), translated, index);
		return skills;
	}

	public static int countBasicInfoFields(WorkerBasicInfo basicInfo) {
		int count = 0;
		if (StringUtils.hasText(basicInfo.getCity())) {
			count++;
		}
		if (StringUtils.hasText(basicInfo.getState())) {
			count++;
		}
		if (StringUtils.hasText(basicInfo.getAddress())) {
			count++;
		}
		return count;
	}

	private static WorkerBasicInfoDto copyBasicDto(WorkerBasicInfoDto source) {
		WorkerBasicInfoDto copy = new WorkerBasicInfoDto();
		copy.setFullName(source.getFullName());
		copy.setMobileNumber(source.getMobileNumber());
		copy.setEmail(source.getEmail());
		copy.setProfilePhotoUrl(source.getProfilePhotoUrl());
		copy.setDateOfBirth(source.getDateOfBirth());
		copy.setGender(source.getGender());
		copy.setCity(source.getCity());
		copy.setState(source.getState());
		copy.setAddress(source.getAddress());
		copy.setPincode(source.getPincode());
		copy.setLatitude(source.getLatitude());
		copy.setLongitude(source.getLongitude());
		copy.setPrimaryLanguage(source.getPrimaryLanguage());
		copy.setAvailabilityStatus(source.getAvailabilityStatus());
		return copy;
	}

	private static WorkerSkillsDto copySkillsDto(WorkerSkillsDto source) {
		WorkerSkillsDto copy = new WorkerSkillsDto();
		copy.setPrimarySkill(source.getPrimarySkill());
		copy.setSecondarySkills(source.getSecondarySkills() != null ? new ArrayList<>(source.getSecondarySkills()) : new ArrayList<>());
		copy.setExperienceYears(source.getExperienceYears());
		copy.setSkillLevel(source.getSkillLevel());
		copy.setCertifications(source.getCertifications() != null ? new ArrayList<>(source.getCertifications()) : new ArrayList<>());
		copy.setToolsOwned(source.getToolsOwned() != null ? new ArrayList<>(source.getToolsOwned()) : new ArrayList<>());
		copy.setWorkType(source.getWorkType());
		copy.setLanguagesSpoken(source.getLanguagesSpoken() != null ? new ArrayList<>(source.getLanguagesSpoken()) : new ArrayList<>());
		copy.setPortfolioImages(source.getPortfolioImages() != null ? new ArrayList<>(source.getPortfolioImages()) : new ArrayList<>());
		return copy;
	}

	private static void addIfHasText(List<String> texts, String value) {
		if (StringUtils.hasText(value)) {
			texts.add(value);
		}
	}

	private static void addListIfPresent(List<String> texts, List<String> values) {
		if (values == null) {
			return;
		}
		for (String value : values) {
			addIfHasText(texts, value);
		}
	}

	private static int applyIfHasText(java.util.function.Consumer<String> setter, String original, List<String> translated,
			int index) {
		if (!StringUtils.hasText(original)) {
			return index;
		}
		setter.accept(translated.get(index));
		return index + 1;
	}

	private static int applyList(java.util.function.Consumer<List<String>> setter, List<String> originals, List<String> translated,
			int index) {
		if (originals == null || originals.isEmpty()) {
			return index;
		}
		List<String> localized = new ArrayList<>(originals.size());
		for (int i = 0; i < originals.size(); i++) {
			if (StringUtils.hasText(originals.get(i))) {
				localized.add(translated.get(index));
				index++;
			}
			else {
				localized.add(originals.get(i));
			}
		}
		setter.accept(localized);
		return index;
	}
}
