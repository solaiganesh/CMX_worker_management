package com.cmx.workermanagemnt.cmx.exception;

public class AmbiguousWorkerException extends RuntimeException {

	public AmbiguousWorkerException(String workerName, int matchCount) {
		super("Multiple workers found for name '" + workerName + "' (" + matchCount + " matches). Use workerPhone instead.");
	}
}
