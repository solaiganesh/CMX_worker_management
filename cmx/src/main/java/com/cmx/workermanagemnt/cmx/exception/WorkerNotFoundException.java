package com.cmx.workermanagemnt.cmx.exception;

public class WorkerNotFoundException extends RuntimeException {

	public WorkerNotFoundException(String id) {
		super("Worker not found: " + id);
	}
}
