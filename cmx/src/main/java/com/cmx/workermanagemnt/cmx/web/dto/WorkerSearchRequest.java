package com.cmx.workermanagemnt.cmx.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class WorkerSearchRequest {

	private String city;

	@Min(0)
	private Integer minAge;

	@Min(0)
	private Integer maxAge;

	@DecimalMin("0.0")
	private BigDecimal minExperience;

	@DecimalMin("0.0")
	private BigDecimal maxExperience;

	@DecimalMin("0.0")
	private BigDecimal minRating;

	@Min(0)
	private Integer page;

	@Min(1)
	@Max(100)
	private Integer size;

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public Integer getMinAge() {
		return minAge;
	}

	public void setMinAge(Integer minAge) {
		this.minAge = minAge;
	}

	public Integer getMaxAge() {
		return maxAge;
	}

	public void setMaxAge(Integer maxAge) {
		this.maxAge = maxAge;
	}

	public BigDecimal getMinExperience() {
		return minExperience;
	}

	public void setMinExperience(BigDecimal minExperience) {
		this.minExperience = minExperience;
	}

	public BigDecimal getMaxExperience() {
		return maxExperience;
	}

	public void setMaxExperience(BigDecimal maxExperience) {
		this.maxExperience = maxExperience;
	}

	public BigDecimal getMinRating() {
		return minRating;
	}

	public void setMinRating(BigDecimal minRating) {
		this.minRating = minRating;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}
}
