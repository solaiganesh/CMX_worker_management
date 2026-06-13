package com.cmx.workermanagemnt.cmx.web.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.util.StringUtils;

public class WorkerRatingRequest {

	private String workerPhone;
	private String workerName;

	@NotNull
	@Min(1)
	@Max(5)
	private Integer score;

	@Size(max = 200)
	private String reviewerName;

	@Size(max = 500)
	private String comment;

	@AssertTrue(message = "Exactly one of workerPhone or workerName must be provided")
	public boolean isWorkerIdentifierValid() {
		boolean hasPhone = StringUtils.hasText(workerPhone);
		boolean hasName = StringUtils.hasText(workerName);
		return hasPhone ^ hasName;
	}

	public String getWorkerPhone() {
		return workerPhone;
	}

	public void setWorkerPhone(String workerPhone) {
		this.workerPhone = workerPhone;
	}

	public String getWorkerName() {
		return workerName;
	}

	public void setWorkerName(String workerName) {
		this.workerName = workerName;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public String getReviewerName() {
		return reviewerName;
	}

	public void setReviewerName(String reviewerName) {
		this.reviewerName = reviewerName;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
