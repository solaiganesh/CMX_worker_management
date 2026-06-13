package com.cmx.workermanagemnt.cmx.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.cmx.workermanagemnt.cmx.domain.WorkerBasicInfo;
import com.cmx.workermanagemnt.cmx.domain.WorkerRating;
import com.cmx.workermanagemnt.cmx.exception.AmbiguousWorkerException;
import com.cmx.workermanagemnt.cmx.exception.WorkerNotFoundException;
import com.cmx.workermanagemnt.cmx.repository.WorkerBasicInfoRepository;
import com.cmx.workermanagemnt.cmx.repository.WorkerRatingRepository;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerRatingRequest;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerRatingResponse;

@Service
public class WorkerRatingService {

	private final WorkerBasicInfoRepository basicInfoRepository;
	private final WorkerRatingRepository ratingRepository;

	public WorkerRatingService(WorkerBasicInfoRepository basicInfoRepository, WorkerRatingRepository ratingRepository) {
		this.basicInfoRepository = basicInfoRepository;
		this.ratingRepository = ratingRepository;
	}

	public WorkerRatingResponse submitRating(WorkerRatingRequest request) {
		WorkerBasicInfo worker = resolveWorker(request);

		WorkerRating rating = new WorkerRating();
		rating.setId(UUID.randomUUID().toString());
		rating.setWorkerId(worker.getId());
		rating.setScore(request.getScore());
		rating.setReviewerName(request.getReviewerName());
		rating.setComment(request.getComment());
		rating.setCreatedAt(Instant.now());
		ratingRepository.save(rating);

		WorkerRatingResponse response = new WorkerRatingResponse();
		response.setWorkerId(worker.getId());
		response.setWorkerName(worker.getFullName());
		response.setSubmittedScore(request.getScore());
		response.setAverageRating(roundAverage(ratingRepository.calculateAverageRating(worker.getId())));
		response.setRatingCount(ratingRepository.countByWorkerId(worker.getId()));
		return response;
	}

	private WorkerBasicInfo resolveWorker(WorkerRatingRequest request) {
		if (StringUtils.hasText(request.getWorkerPhone())) {
			return basicInfoRepository.findByMobileNumber(request.getWorkerPhone())
					.orElseThrow(() -> new WorkerNotFoundException(request.getWorkerPhone()));
		}
		String workerName = request.getWorkerName().trim();
		var matches = basicInfoRepository.findByFullNameIgnoreCase(workerName);
		if (matches.isEmpty()) {
			throw new WorkerNotFoundException(workerName);
		}
		if (matches.size() > 1) {
			throw new AmbiguousWorkerException(workerName, matches.size());
		}
		return matches.get(0);
	}

	static double roundAverage(double average) {
		return Math.round(average * 10.0) / 10.0;
	}
}
