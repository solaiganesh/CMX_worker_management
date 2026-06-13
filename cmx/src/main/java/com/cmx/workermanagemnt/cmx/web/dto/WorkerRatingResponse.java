package com.cmx.workermanagemnt.cmx.web.dto;

public class WorkerRatingResponse {

	private String workerId;
	private String workerName;
	private double averageRating;
	private int ratingCount;
	private int submittedScore;

	public String getWorkerId() {
		return workerId;
	}

	public void setWorkerId(String workerId) {
		this.workerId = workerId;
	}

	public String getWorkerName() {
		return workerName;
	}

	public void setWorkerName(String workerName) {
		this.workerName = workerName;
	}

	public double getAverageRating() {
		return averageRating;
	}

	public void setAverageRating(double averageRating) {
		this.averageRating = averageRating;
	}

	public int getRatingCount() {
		return ratingCount;
	}

	public void setRatingCount(int ratingCount) {
		this.ratingCount = ratingCount;
	}

	public int getSubmittedScore() {
		return submittedScore;
	}

	public void setSubmittedScore(int submittedScore) {
		this.submittedScore = submittedScore;
	}
}
