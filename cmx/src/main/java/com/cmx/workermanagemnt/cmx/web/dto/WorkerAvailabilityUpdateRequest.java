package com.cmx.workermanagemnt.cmx.web.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public class WorkerAvailabilityUpdateRequest {

	@NotEmpty
	@Valid
	private List<WorkerAvailabilityEntryDto> entries = new ArrayList<>();

	public List<WorkerAvailabilityEntryDto> getEntries() {
		return entries;
	}

	public void setEntries(List<WorkerAvailabilityEntryDto> entries) {
		this.entries = entries != null ? entries : new ArrayList<>();
	}
}
