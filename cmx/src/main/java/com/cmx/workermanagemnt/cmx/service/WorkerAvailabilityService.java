package com.cmx.workermanagemnt.cmx.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cmx.workermanagemnt.cmx.domain.WorkerAvailability;
import com.cmx.workermanagemnt.cmx.exception.WorkerNotFoundException;
import com.cmx.workermanagemnt.cmx.repository.WorkerAvailabilityRepository;
import com.cmx.workermanagemnt.cmx.repository.WorkerBasicInfoRepository;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerAvailabilityEntryDto;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerAvailabilityResponse;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerAvailabilityUpdateRequest;

@Service
public class WorkerAvailabilityService {

	private final WorkerBasicInfoRepository basicInfoRepository;
	private final WorkerAvailabilityRepository availabilityRepository;

	public WorkerAvailabilityService(WorkerBasicInfoRepository basicInfoRepository,
			WorkerAvailabilityRepository availabilityRepository) {
		this.basicInfoRepository = basicInfoRepository;
		this.availabilityRepository = availabilityRepository;
	}

	public WorkerAvailabilityResponse updateAvailability(String workerId, WorkerAvailabilityUpdateRequest request) {
		ensureWorkerExists(workerId);
		Instant now = Instant.now();
		for (WorkerAvailabilityEntryDto entry : request.getEntries()) {
			WorkerAvailability availability = new WorkerAvailability();
			availability.setWorkerId(workerId);
			availability.setAvailabilityDate(entry.getDate());
			availability.setAvailable(entry.getAvailable());
			availability.setUpdatedAt(now);
			availabilityRepository.save(availability);
		}
		return getAvailability(workerId, null, null);
	}

	public WorkerAvailabilityResponse getAvailability(String workerId, LocalDate from, LocalDate to) {
		ensureWorkerExists(workerId);
		List<WorkerAvailability> entries = availabilityRepository.findByWorkerId(workerId).stream()
				.filter(entry -> isWithinRange(entry.getAvailabilityDate(), from, to))
				.sorted(Comparator.comparing(WorkerAvailability::getAvailabilityDate))
				.collect(Collectors.toList());
		return toResponse(workerId, entries);
	}

	private void ensureWorkerExists(String workerId) {
		basicInfoRepository.findById(workerId).orElseThrow(() -> new WorkerNotFoundException(workerId));
	}

	private boolean isWithinRange(LocalDate date, LocalDate from, LocalDate to) {
		if (date == null) {
			return false;
		}
		if (from != null && date.isBefore(from)) {
			return false;
		}
		if (to != null && date.isAfter(to)) {
			return false;
		}
		return true;
	}

	private WorkerAvailabilityResponse toResponse(String workerId, List<WorkerAvailability> entries) {
		WorkerAvailabilityResponse response = new WorkerAvailabilityResponse();
		response.setWorkerId(workerId);
		response.setEntries(entries.stream().map(entry -> {
			WorkerAvailabilityResponse.Entry dto = new WorkerAvailabilityResponse.Entry();
			dto.setDate(entry.getAvailabilityDate());
			dto.setAvailable(entry.isAvailable());
			dto.setUpdatedAt(entry.getUpdatedAt());
			return dto;
		}).collect(Collectors.toList()));
		return response;
	}
}
