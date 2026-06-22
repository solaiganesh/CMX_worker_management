package com.cmx.workermanagemnt.cmx.web.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WorkerAvailabilityResponse {

	private String workerId;
	private List<Entry> entries = new ArrayList<>();

	public String getWorkerId() {
		return workerId;
	}

	public void setWorkerId(String workerId) {
		this.workerId = workerId;
	}

	public List<Entry> getEntries() {
		return entries;
	}

	public void setEntries(List<Entry> entries) {
		this.entries = entries != null ? entries : new ArrayList<>();
	}

	public static class Entry {

		private LocalDate date;
		private boolean available;
		private Instant updatedAt;

		public LocalDate getDate() {
			return date;
		}

		public void setDate(LocalDate date) {
			this.date = date;
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
}
