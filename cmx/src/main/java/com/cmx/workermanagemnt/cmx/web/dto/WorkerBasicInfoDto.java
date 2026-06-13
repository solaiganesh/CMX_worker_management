package com.cmx.workermanagemnt.cmx.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.cmx.workermanagemnt.cmx.domain.AvailabilityStatus;
import com.cmx.workermanagemnt.cmx.domain.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

public class WorkerBasicInfoDto {

	@NotBlank
	private String fullName;

	@NotBlank
	@Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid mobile number")
	private String mobileNumber;

	@NotBlank
	@Email
	private String email;

	private String profilePhotoUrl;

	@NotNull
	@Past
	private LocalDate dateOfBirth;

	@NotNull
	private Gender gender;

	@NotBlank
	private String city;

	@NotBlank
	private String state;

	@NotBlank
	private String address;

	@NotBlank
	@Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be 6 digits")
	private String pincode;

	private BigDecimal latitude;
	private BigDecimal longitude;

	@NotBlank
	private String primaryLanguage;

	@NotNull
	private AvailabilityStatus availabilityStatus;

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
}
