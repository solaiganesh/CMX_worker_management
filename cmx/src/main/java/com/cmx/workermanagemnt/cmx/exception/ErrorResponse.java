package com.cmx.workermanagemnt.cmx.exception;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
		String code,
		String message,
		String correlationId,
		Instant timestamp,
		List<FieldError> fieldErrors) {

	public record FieldError(String field, String message) {
	}
}
