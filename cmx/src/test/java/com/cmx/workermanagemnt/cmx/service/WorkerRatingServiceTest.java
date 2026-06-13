package com.cmx.workermanagemnt.cmx.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cmx.workermanagemnt.cmx.domain.WorkerBasicInfo;
import com.cmx.workermanagemnt.cmx.domain.WorkerRating;
import com.cmx.workermanagemnt.cmx.exception.AmbiguousWorkerException;
import com.cmx.workermanagemnt.cmx.exception.WorkerNotFoundException;
import com.cmx.workermanagemnt.cmx.repository.WorkerBasicInfoRepository;
import com.cmx.workermanagemnt.cmx.repository.WorkerRatingRepository;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerRatingRequest;
import com.cmx.workermanagemnt.cmx.web.dto.WorkerRatingResponse;

@ExtendWith(MockitoExtension.class)
class WorkerRatingServiceTest {

	@Mock
	private WorkerBasicInfoRepository basicInfoRepository;

	@Mock
	private WorkerRatingRepository ratingRepository;

	@InjectMocks
	private WorkerRatingService workerRatingService;

	@Test
	void submitsRatingByPhone() {
		WorkerBasicInfo worker = new WorkerBasicInfo();
		worker.setId("worker-1");
		worker.setFullName("Rajesh Kumar");
		when(basicInfoRepository.findByMobileNumber("+919876543210")).thenReturn(Optional.of(worker));
		when(ratingRepository.save(any(WorkerRating.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(ratingRepository.calculateAverageRating("worker-1")).thenReturn(4.0);
		when(ratingRepository.countByWorkerId("worker-1")).thenReturn(1);

		WorkerRatingRequest request = new WorkerRatingRequest();
		request.setWorkerPhone("+919876543210");
		request.setScore(4);

		WorkerRatingResponse response = workerRatingService.submitRating(request);

		assertThat(response.getWorkerId()).isEqualTo("worker-1");
		assertThat(response.getAverageRating()).isEqualTo(4.0);
		assertThat(response.getRatingCount()).isEqualTo(1);
	}

	@Test
	void throwsWhenNameMatchesMultipleWorkers() {
		WorkerBasicInfo first = new WorkerBasicInfo();
		first.setId("1");
		WorkerBasicInfo second = new WorkerBasicInfo();
		second.setId("2");
		when(basicInfoRepository.findByFullNameIgnoreCase("Rajesh Kumar")).thenReturn(List.of(first, second));

		WorkerRatingRequest request = new WorkerRatingRequest();
		request.setWorkerName("Rajesh Kumar");
		request.setScore(5);

		assertThatThrownBy(() -> workerRatingService.submitRating(request))
				.isInstanceOf(AmbiguousWorkerException.class);
	}

	@Test
	void throwsWhenWorkerNotFoundByPhone() {
		when(basicInfoRepository.findByMobileNumber("+919999999999")).thenReturn(Optional.empty());

		WorkerRatingRequest request = new WorkerRatingRequest();
		request.setWorkerPhone("+919999999999");
		request.setScore(3);

		assertThatThrownBy(() -> workerRatingService.submitRating(request))
				.isInstanceOf(WorkerNotFoundException.class);
	}
}
