package com.cmx.workermanagemnt.cmx.repository.excel;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.cmx.workermanagemnt.cmx.config.StorageProperties;
import com.cmx.workermanagemnt.cmx.domain.WorkerAvailability;

@SpringBootTest
class ExcelWorkerAvailabilityRepositoryIntegrationTest {

	@Autowired
	private ExcelWorkerAvailabilityRepository availabilityRepository;

	@Autowired
	private StorageProperties storageProperties;

	@Autowired
	private ExcelFileSupport excelFileSupport;

	@BeforeEach
	void setUp(@TempDir java.nio.file.Path tempDir) {
		storageProperties.getExcel().setBasePath(tempDir.toString());
		excelFileSupport.initializeWorkbooks();
	}

	@Test
	void upsertsAvailabilityByWorkerAndDate() {
		LocalDate date = LocalDate.of(2026, 6, 15);
		save("worker-1", date, true);
		assertThat(availabilityRepository.isAvailableOnDate("worker-1", date)).isTrue();

		save("worker-1", date, false);
		assertThat(availabilityRepository.isAvailableOnDate("worker-1", date)).isFalse();
		assertThat(availabilityRepository.findByWorkerId("worker-1")).hasSize(1);
	}

	private void save(String workerId, LocalDate date, boolean available) {
		WorkerAvailability availability = new WorkerAvailability();
		availability.setWorkerId(workerId);
		availability.setAvailabilityDate(date);
		availability.setAvailable(available);
		availability.setUpdatedAt(java.time.Instant.now());
		availabilityRepository.save(availability);
	}
}
