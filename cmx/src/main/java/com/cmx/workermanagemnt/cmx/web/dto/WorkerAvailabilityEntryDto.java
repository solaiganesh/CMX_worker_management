package com.cmx.workermanagemnt.cmx.web.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public class WorkerAvailabilityEntryDto {

	@NotNull
	private LocalDate date;

	@NotNull
	private Boolean available;

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public Boolean getAvailable() {
		return available;
	}

	public void setAvailable(Boolean available) {
		this.available = available;
	}
}
