package com.cmx.workermanagemnt.cmx.repository.excel;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.cmx.workermanagemnt.cmx.config.StorageProperties;
import com.cmx.workermanagemnt.cmx.domain.WorkerRating;

@SpringBootTest
class ExcelWorkerRatingRepositoryIntegrationTest {

	@Autowired
	private ExcelWorkerRatingRepository ratingRepository;

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
	void calculatesAverageRating() {
		saveRating("worker-1", 4);
		saveRating("worker-1", 5);

		assertThat(ratingRepository.calculateAverageRating("worker-1")).isEqualTo(4.5);
		assertThat(ratingRepository.countByWorkerId("worker-1")).isEqualTo(2);
		assertThat(ratingRepository.calculateAverageRating("missing")).isZero();
	}

	private void saveRating(String workerId, int score) {
		WorkerRating rating = new WorkerRating();
		rating.setId(java.util.UUID.randomUUID().toString());
		rating.setWorkerId(workerId);
		rating.setScore(score);
		rating.setCreatedAt(java.time.Instant.now());
		ratingRepository.save(rating);
	}
}
