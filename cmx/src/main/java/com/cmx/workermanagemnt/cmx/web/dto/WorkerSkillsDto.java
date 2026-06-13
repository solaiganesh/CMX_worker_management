package com.cmx.workermanagemnt.cmx.web.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.cmx.workermanagemnt.cmx.domain.SkillLevel;
import com.cmx.workermanagemnt.cmx.domain.WorkType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class WorkerSkillsDto {

	@NotBlank
	private String primarySkill;

	private List<String> secondarySkills = new ArrayList<>();

	@NotNull
	@PositiveOrZero
	private BigDecimal experienceYears;

	@NotNull
	private SkillLevel skillLevel;

	private List<String> certifications = new ArrayList<>();
	private List<String> toolsOwned = new ArrayList<>();

	@NotNull
	private WorkType workType;

	private List<String> languagesSpoken = new ArrayList<>();
	private List<String> portfolioImages = new ArrayList<>();

	public String getPrimarySkill() {
		return primarySkill;
	}

	public void setPrimarySkill(String primarySkill) {
		this.primarySkill = primarySkill;
	}

	public List<String> getSecondarySkills() {
		return secondarySkills;
	}

	public void setSecondarySkills(List<String> secondarySkills) {
		this.secondarySkills = secondarySkills != null ? secondarySkills : new ArrayList<>();
	}

	public BigDecimal getExperienceYears() {
		return experienceYears;
	}

	public void setExperienceYears(BigDecimal experienceYears) {
		this.experienceYears = experienceYears;
	}

	public SkillLevel getSkillLevel() {
		return skillLevel;
	}

	public void setSkillLevel(SkillLevel skillLevel) {
		this.skillLevel = skillLevel;
	}

	public List<String> getCertifications() {
		return certifications;
	}

	public void setCertifications(List<String> certifications) {
		this.certifications = certifications != null ? certifications : new ArrayList<>();
	}

	public List<String> getToolsOwned() {
		return toolsOwned;
	}

	public void setToolsOwned(List<String> toolsOwned) {
		this.toolsOwned = toolsOwned != null ? toolsOwned : new ArrayList<>();
	}

	public WorkType getWorkType() {
		return workType;
	}

	public void setWorkType(WorkType workType) {
		this.workType = workType;
	}

	public List<String> getLanguagesSpoken() {
		return languagesSpoken;
	}

	public void setLanguagesSpoken(List<String> languagesSpoken) {
		this.languagesSpoken = languagesSpoken != null ? languagesSpoken : new ArrayList<>();
	}

	public List<String> getPortfolioImages() {
		return portfolioImages;
	}

	public void setPortfolioImages(List<String> portfolioImages) {
		this.portfolioImages = portfolioImages != null ? portfolioImages : new ArrayList<>();
	}
}
