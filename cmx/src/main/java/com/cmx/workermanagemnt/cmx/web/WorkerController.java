package com.cmx.workermanagemnt.cmx.web;

import java.net.URI;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cmx.workermanagemnt.cmx.service.LocaleSupport;
import com.cmx.workermanagemnt.cmx.service.WorkerAvailabilityService;
import com.cmx.workermanagemnt.cmx.service.WorkerRatingService;
import com.cmx.workermanagemnt.cmx.service.WorkerRegistrationService;
import com.cmx.workermanagemnt.cmx.service.WorkerSearchService;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerAvailabilityResponse;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerAvailabilityUpdateRequest;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerRatingRequest;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerRatingResponse;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerRegistrationRequest;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerResponse;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerSearchRequest;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerSearchResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class WorkerController {

	private final WorkerRegistrationService workerRegistrationService;
	private final WorkerSearchService workerSearchService;
	private final WorkerRatingService workerRatingService;
	private final WorkerAvailabilityService workerAvailabilityService;

	public WorkerController(WorkerRegistrationService workerRegistrationService, WorkerSearchService workerSearchService,
			WorkerRatingService workerRatingService, WorkerAvailabilityService workerAvailabilityService) {
		this.workerRegistrationService = workerRegistrationService;
		this.workerSearchService = workerSearchService;
		this.workerRatingService = workerRatingService;
		this.workerAvailabilityService = workerAvailabilityService;
	}

	@PostMapping("/workers/register")
	public ResponseEntity<WorkerResponse> register(
			@Valid @RequestBody WorkerRegistrationRequest request,
			@RequestHeader(value = "Content-Language", defaultValue = "en") String contentLanguage,
			@RequestHeader(value = "Accept-Language", defaultValue = "en") String acceptLanguage) {
		Locale inputLocale = LocaleSupport.resolveLocale(contentLanguage);
		Locale responseLocale = LocaleSupport.resolveLocale(acceptLanguage);
		WorkerResponse response = workerRegistrationService.register(request, inputLocale, responseLocale);
		return ResponseEntity.created(URI.create("/api/v1/workers/" + response.getId())).body(response);
	}

	@GetMapping("/workers/{id}")
	public WorkerResponse getWorker(
			@PathVariable String id,
			@RequestHeader(value = "Accept-Language", defaultValue = "en") String acceptLanguage) {
		return workerRegistrationService.getById(id, LocaleSupport.resolveLocale(acceptLanguage));
	}

	@PostMapping("/workers/search")
	public WorkerSearchResponse searchWorkers(
			@Valid @RequestBody WorkerSearchRequest request,
			@RequestHeader(value = "Content-Language", defaultValue = "en") String contentLanguage,
			@RequestHeader(value = "Accept-Language", defaultValue = "en") String acceptLanguage) {
		return workerSearchService.search(request, LocaleSupport.resolveLocale(contentLanguage),
				LocaleSupport.resolveLocale(acceptLanguage));
	}

	@PostMapping("/workers/ratings")
	public ResponseEntity<WorkerRatingResponse> submitRating(@Valid @RequestBody WorkerRatingRequest request) {
		WorkerRatingResponse response = workerRatingService.submitRating(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PutMapping("/workers/{id}/availability")
	public WorkerAvailabilityResponse updateAvailability(
			@PathVariable String id,
			@Valid @RequestBody WorkerAvailabilityUpdateRequest request) {
		return workerAvailabilityService.updateAvailability(id, request);
	}

	@GetMapping("/workers/{id}/availability")
	public WorkerAvailabilityResponse getAvailability(
			@PathVariable String id,
			@RequestParam(required = false) LocalDate from,
			@RequestParam(required = false) LocalDate to) {
		return workerAvailabilityService.getAvailability(id, from, to);
	}

	@GetMapping("/health")
	public Map<String, String> health() {
		return Map.of("status", "UP");
	}
}
