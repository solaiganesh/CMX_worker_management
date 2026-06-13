package com.cmx.workermanagemnt.cmx.repository;

import java.util.List;

import com.cmx.workermanagemnt.cmx.domain.WorkerRating;

public interface WorkerRatingRepository {

	WorkerRating save(WorkerRating rating);

	List<WorkerRating> findByWorkerId(String workerId);

	double calculateAverageRating(String workerId);

	int countByWorkerId(String workerId);
}
