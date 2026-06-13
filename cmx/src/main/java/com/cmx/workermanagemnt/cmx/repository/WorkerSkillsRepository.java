package com.cmx.workermanagemnt.cmx.repository;

import java.util.List;
import java.util.Optional;

import com.cmx.workermanagemnt.cmx.domain.WorkerSkills;

public interface WorkerSkillsRepository {

	WorkerSkills save(WorkerSkills skills);

	Optional<WorkerSkills> findByUserId(String userId);

	List<WorkerSkills> findAll();

	void deleteByUserId(String userId);
}
