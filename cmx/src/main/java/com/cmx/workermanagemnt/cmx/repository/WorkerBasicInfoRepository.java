package com.cmx.workermanagemnt.cmx.repository;

import java.util.List;
import java.util.Optional;

import com.cmx.workermanagemnt.cmx.domain.WorkerBasicInfo;

public interface  WorkerBasicInfoRepository {

	WorkerBasicInfo save(WorkerBasicInfo info);

	Optional<WorkerBasicInfo> findById(String id);

	List<WorkerBasicInfo> findAll();

	Optional<WorkerBasicInfo> findByMobileNumber(String mobileNumber);

	List<WorkerBasicInfo> findByFullNameIgnoreCase(String fullName);

	boolean existsByMobileNumber(String mobileNumber);

	boolean existsByEmail(String email);

	void deleteById(String id);
}
