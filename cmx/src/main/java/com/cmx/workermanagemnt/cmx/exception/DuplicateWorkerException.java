package com.cmx.workermanagemnt.cmx.exception;

public class DuplicateWorkerException extends RuntimeException {

	private final String code;
	private final String field;

	public DuplicateWorkerException(String code, String field, String message) {
		super(message);
		this.code = code;
		this.field = field;
	}

	public String getCode() {
		return code;
	}

	public String getField() {
		return field;
	}
}
