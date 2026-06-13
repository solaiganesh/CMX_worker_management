package com.cmx.workermanagemnt.cmx.web.mapper;

import com.cmx.workermanagemnt.cmx.domain.WorkerBasicInfo;
import com.cmx.workermanagemnt.cmx.domain.WorkerSkills;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerBasicInfoDto;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerSkillsDto;

public final class WorkerMapper {

	private WorkerMapper() {
	}

	public static WorkerBasicInfo toBasicInfoEntity(WorkerBasicInfoDto dto) {
		WorkerBasicInfo entity = new WorkerBasicInfo();
		entity.setFullName(dto.getFullName());
		entity.setMobileNumber(dto.getMobileNumber());
		entity.setEmail(dto.getEmail());
		entity.setProfilePhotoUrl(dto.getProfilePhotoUrl());
		entity.setDateOfBirth(dto.getDateOfBirth());
		entity.setGender(dto.getGender());
		entity.setCity(dto.getCity());
		entity.setState(dto.getState());
		entity.setAddress(dto.getAddress());
		entity.setPincode(dto.getPincode());
		entity.setLatitude(dto.getLatitude());
		entity.setLongitude(dto.getLongitude());
		entity.setPrimaryLanguage(dto.getPrimaryLanguage());
		entity.setAvailabilityStatus(dto.getAvailabilityStatus());
		return entity;
	}

	public static WorkerSkills toSkillsEntity(WorkerSkillsDto dto) {
		WorkerSkills entity = new WorkerSkills();
		entity.setPrimarySkill(dto.getPrimarySkill());
		entity.setSecondarySkills(dto.getSecondarySkills());
		entity.setExperienceYears(dto.getExperienceYears());
		entity.setSkillLevel(dto.getSkillLevel());
		entity.setCertifications(dto.getCertifications());
		entity.setToolsOwned(dto.getToolsOwned());
		entity.setWorkType(dto.getWorkType());
		entity.setLanguagesSpoken(dto.getLanguagesSpoken());
		entity.setPortfolioImages(dto.getPortfolioImages());
		return entity;
	}
}
