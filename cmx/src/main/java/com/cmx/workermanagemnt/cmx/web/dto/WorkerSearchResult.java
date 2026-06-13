package com.cmx.workermanagemnt.cmx.web.dto;

import java.math.BigDecimal;

public class WorkerSearchResult {

	private String id;
	private String fullName;
	private String email;
	private String mobileNumber;
	private String city;
	private BigDecimal experienceYears;
	private double averageRating;
	private int ratingCount;

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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public BigDecimal getExperienceYears() {
		return experienceYears;
	}

	public void setExperienceYears(BigDecimal experienceYears) {
		this.experienceYears = experienceYears;
	}

	public double getAverageRating() {
		return averageRating;
	}

	public void setAverageRating(double averageRating) {
		this.averageRating = averageRating;
	}

	public int getRatingCount() {
		return ratingCount;
	}

	public void setRatingCount(int ratingCount) {
		this.ratingCount = ratingCount;
	}
}
