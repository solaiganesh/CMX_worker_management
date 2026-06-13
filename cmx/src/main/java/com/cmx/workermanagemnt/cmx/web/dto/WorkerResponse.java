package com.cmx.workermanagemnt.cmx.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import com.cmx.workermanagemnt.cmx.domain.AvailabilityStatus;
import com.cmx.workermanagemnt.cmx.domain.Gender;
import com.cmx.workermanagemnt.cmx.domain.SkillLevel;
import com.cmx.workermanagemnt.cmx.domain.WorkType;
import com.cmx.workermanagemnt.cmx.domain.WorkerBasicInfo;
import com.cmx.workermanagemnt.cmx.domain.WorkerSkills;

public class WorkerResponse {

	private String id;
	private String fullName;
	private String mobileNumber;
	private String email;
	private String profilePhotoUrl;
	private LocalDate dateOfBirth;
	private int age;
	private Gender gender;
	private String city;
	private String state;
	private String address;
	private String pincode;
	private BigDecimal latitude;
	private BigDecimal longitude;
	private String primaryLanguage;
	private AvailabilityStatus availabilityStatus;
	private Instant createdAt;
	private Instant updatedAt;
	private String primarySkill;
	private List<String> secondarySkills = new ArrayList<>();
	private BigDecimal experienceYears;
	private SkillLevel skillLevel;
	private List<String> certifications = new ArrayList<>();
	private List<String> toolsOwned = new ArrayList<>();
	private WorkType workType;
	private List<String> languagesSpoken = new ArrayList<>();
	private List<String> portfolioImages = new ArrayList<>();

	public static WorkerResponse fromDomain(WorkerBasicInfo basicInfo, WorkerSkills skills) {
		WorkerResponse response = new WorkerResponse();
		response.setId(basicInfo.getId());
		response.setFullName(basicInfo.getFullName());
		response.setMobileNumber(basicInfo.getMobileNumber());
		response.setEmail(basicInfo.getEmail());
		response.setProfilePhotoUrl(basicInfo.getProfilePhotoUrl());
		response.setDateOfBirth(basicInfo.getDateOfBirth());
		if (basicInfo.getDateOfBirth() != null) {
			response.setAge(Period.between(basicInfo.getDateOfBirth(), LocalDate.now()).getYears());
		}
		response.setGender(basicInfo.getGender());
		response.setCity(basicInfo.getCity());
		response.setState(basicInfo.getState());
		response.setAddress(basicInfo.getAddress());
		response.setPincode(basicInfo.getPincode());
		response.setLatitude(basicInfo.getLatitude());
		response.setLongitude(basicInfo.getLongitude());
		response.setPrimaryLanguage(basicInfo.getPrimaryLanguage());
		response.setAvailabilityStatus(basicInfo.getAvailabilityStatus());
		response.setCreatedAt(basicInfo.getCreatedAt());
		response.setUpdatedAt(basicInfo.getUpdatedAt());

		if (skills != null) {
			response.setPrimarySkill(skills.getPrimarySkill());
			response.setSecondarySkills(new ArrayList<>(skills.getSecondarySkills()));
			response.setExperienceYears(skills.getExperienceYears());
			response.setSkillLevel(skills.getSkillLevel());
			response.setCertifications(new ArrayList<>(skills.getCertifications()));
			response.setToolsOwned(new ArrayList<>(skills.getToolsOwned()));
			response.setWorkType(skills.getWorkType());
			response.setLanguagesSpoken(new ArrayList<>(skills.getLanguagesSpoken()));
			response.setPortfolioImages(new ArrayList<>(skills.getPortfolioImages()));
		}
		return response;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getProfilePhotoUrl() {
		return profilePhotoUrl;
	}

	public void setProfilePhotoUrl(String profilePhotoUrl) {
		this.profilePhotoUrl = profilePhotoUrl;
	}

	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPincode() {
		return pincode;
	}

	public void setPincode(String pincode) {
		this.pincode = pincode;
	}

	public BigDecimal getLatitude() {
		return latitude;
	}

	public void setLatitude(BigDecimal latitude) {
		this.latitude = latitude;
	}

	public BigDecimal getLongitude() {
		return longitude;
	}

	public void setLongitude(BigDecimal longitude) {
		this.longitude = longitude;
	}

	public String getPrimaryLanguage() {
		return primaryLanguage;
	}

	public void setPrimaryLanguage(String primaryLanguage) {
		this.primaryLanguage = primaryLanguage;
	}

	public AvailabilityStatus getAvailabilityStatus() {
		return availabilityStatus;
	}

	public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {
		this.availabilityStatus = availabilityStatus;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

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
		this.secondarySkills = secondarySkills;
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
		this.certifications = certifications;
	}

	public List<String> getToolsOwned() {
		return toolsOwned;
	}

	public void setToolsOwned(List<String> toolsOwned) {
		this.toolsOwned = toolsOwned;
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
		this.languagesSpoken = languagesSpoken;
	}

	public List<String> getPortfolioImages() {
		return portfolioImages;
	}

	public void setPortfolioImages(List<String> portfolioImages) {
		this.portfolioImages = portfolioImages;
	}
}
