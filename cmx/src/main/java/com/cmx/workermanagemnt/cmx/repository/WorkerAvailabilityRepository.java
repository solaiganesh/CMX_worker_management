package com.cmx.workermanagemnt.cmx.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.cmx.workermanagemnt.cmx.domain.WorkerAvailability;

public interface WorkerAvailabilityRepository {

	WorkerAvailability save(WorkerAvailability availability);

	List<WorkerAvailability> findByWorkerId(String workerId);

	Optional<WorkerAvailability> findByWorkerIdAndDate(String workerId, LocalDate date);

	boolean isAvailableOnDate(String workerId, LocalDate date);
}
