package com.lucasdourado.mediautility.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
class ApiExceptionHandler {

	@ExceptionHandler(ApiException.class)
	ResponseEntity<PublicErrorResponse> handleApiException(ApiException ex) {
		return ResponseEntity.status(ex.getStatus()).body(ex.getError());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<PublicErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
		List<PublicErrorDetail> details = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(error -> new PublicErrorDetail(error.getField(), error.getDefaultMessage()))
				.toList();
		return ResponseEntity.badRequest()
				.body(new PublicErrorResponse(PublicErrorCode.VALIDATION_ERROR, "Request validation failed.", details));
	}

	@ExceptionHandler({
			MissingServletRequestPartException.class,
			MissingServletRequestParameterException.class,
			MultipartException.class
	})
	ResponseEntity<PublicErrorResponse> handleBadRequest(Exception ex) {
		return ResponseEntity.badRequest()
				.body(new PublicErrorResponse(PublicErrorCode.VALIDATION_ERROR, "Request validation failed."));
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	ResponseEntity<PublicErrorResponse> handleUnsupportedMediaType() {
		return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
				.body(new PublicErrorResponse(
						PublicErrorCode.UNSUPPORTED_MEDIA_TYPE,
						"Request media type is not supported."));
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	ResponseEntity<PublicErrorResponse> handlePayloadTooLarge() {
		return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
				.body(new PublicErrorResponse(PublicErrorCode.PAYLOAD_TOO_LARGE, "Upload exceeds the configured limit."));
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<PublicErrorResponse> handleUnexpected() {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new PublicErrorResponse(PublicErrorCode.INTERNAL_ERROR, "Unexpected backend failure."));
	}
}
