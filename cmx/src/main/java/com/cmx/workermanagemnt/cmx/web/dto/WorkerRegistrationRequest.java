package com.cmx.workermanagemnt.cmx.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class WorkerRegistrationRequest {

	@NotNull
	@Valid
	private WorkerBasicInfoDto basicInfo;

	@NotNull
	@Valid
	private WorkerSkillsDto skills;

	public WorkerBasicInfoDto getBasicInfo() {
		return basicInfo;
	}

	public void setBasicInfo(WorkerBasicInfoDto basicInfo) {
		this.basicInfo = basicInfo;
	}

	public WorkerSkillsDto getSkills() {
		return skills;
	}

	public void setSkills(WorkerSkillsDto skills) {
		this.skills = skills;
	}
}
