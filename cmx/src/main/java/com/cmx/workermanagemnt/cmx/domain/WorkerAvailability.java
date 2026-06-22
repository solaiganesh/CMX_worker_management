package com.cmx.workermanagemnt.cmx.domain;

import java.time.Instant;
import java.time.LocalDate;

public class WorkerAvailability {

	private String id;
	private String workerId;
	private LocalDate availabilityDate;
	private boolean available;
	private Instant updatedAt;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getWorkerId() {
		return workerId;
	}

	public void setWorkerId(String workerId) {
		this.workerId = workerId;
	}

	public LocalDate getAvailabilityDate() {
		return availabilityDate;
	}

	public void setAvailabilityDate(LocalDate availabilityDate) {
		this.availabilityDate = availabilityDate;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
