package com.cmx.workermanagemnt.cmx.exception;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cmx.workermanagemnt.cmx.logging.CorrelationIdFilter;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	private final MessageSource messageSource;

	public GlobalExceptionHandler(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, Locale locale) {
		List<com.cmx.workermanagemnt.cmx.exception.ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(error -> new com.cmx.workermanagemnt.cmx.exception.ErrorResponse.FieldError(
						error.getField(), resolveFieldMessage(error, locale)))
				.collect(Collectors.toList());

		log.warn("Validation failed: {}", fieldErrors);
		return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
				messageSource.getMessage("error.validation", null, "Validation failed", locale), fieldErrors);
	}

	@ExceptionHandler(DuplicateWorkerException.class)
	public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateWorkerException ex, Locale locale) {
		log.warn("Duplicate worker: {}", ex.getCode());
		List<com.cmx.workermanagemnt.cmx.exception.ErrorResponse.FieldError> fieldErrors = List.of(
				new com.cmx.workermanagemnt.cmx.exception.ErrorResponse.FieldError(ex.getField(), ex.getMessage()));
		return buildResponse(HttpStatus.CONFLICT, ex.getCode(), ex.getMessage(), fieldErrors);
	}

	@ExceptionHandler(WorkerNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(WorkerNotFoundException ex, Locale locale) {
		log.warn("Worker not found: {}", ex.getMessage());
		String message = messageSource.getMessage("error.worker.not_found", null, ex.getMessage(), locale);
		return buildResponse(HttpStatus.NOT_FOUND, "WORKER_NOT_FOUND", message, List.of());
	}

	@ExceptionHandler(AmbiguousWorkerException.class)
	public ResponseEntity<ErrorResponse> handleAmbiguousWorker(AmbiguousWorkerException ex, Locale locale) {
		log.warn("Ambiguous worker lookup: {}", ex.getMessage());
		String message = messageSource.getMessage("error.worker.ambiguous", null, ex.getMessage(), locale);
		return buildResponse(HttpStatus.CONFLICT, "AMBIGUOUS_WORKER", message, List.of());
	}

	@ExceptionHandler(InvalidRatingException.class)
	public ResponseEntity<ErrorResponse> handleInvalidRating(InvalidRatingException ex, Locale locale) {
		log.warn("Invalid rating: {}", ex.getMessage());
		String message = messageSource.getMessage("error.rating.invalid", null, ex.getMessage(), locale);
		return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_RATING", message, List.of());
	}

	@ExceptionHandler(TranslationException.class)
	public ResponseEntity<ErrorResponse> handleTranslation(TranslationException ex, Locale locale) {
		log.error("Translation error", ex);
		String message = messageSource.getMessage("error.translation", null, "Translation service unavailable", locale);
		return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "TRANSLATION_UNAVAILABLE", message, List.of());
	}

	@ExceptionHandler(StorageException.class)
	public ResponseEntity<ErrorResponse> handleStorage(StorageException ex, Locale locale) {
		log.error("Storage error", ex);
		String message = messageSource.getMessage("error.storage", null, "Storage operation failed", locale);
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_ERROR", message, List.of());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, Locale locale) {
		log.error("Unhandled error", ex);
		String message = messageSource.getMessage("error.internal", null, "Internal server error", locale);
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", message, List.of());
	}

	private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String code, String message,
			List<com.cmx.workermanagemnt.cmx.exception.ErrorResponse.FieldError> fieldErrors) {
		ErrorResponse body = new ErrorResponse(code, message, currentCorrelationId(), Instant.now(), fieldErrors);
		return ResponseEntity.status(status).body(body);
	}

	private String resolveFieldMessage(FieldError error, Locale locale) {
		if (error.getDefaultMessage() != null) {
			return error.getDefaultMessage();
		}
		return messageSource.getMessage(error, locale);
	}

	private String currentCorrelationId() {
		String correlationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
		return correlationId != null ? correlationId : "";
	}
}
