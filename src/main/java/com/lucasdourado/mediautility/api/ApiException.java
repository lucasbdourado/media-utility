package com.lucasdourado.mediautility.api;

import org.springframework.http.HttpStatusCode;

public class ApiException extends RuntimeException {

	private final HttpStatusCode status;
	private final PublicErrorResponse error;

	public ApiException(HttpStatusCode status, PublicErrorResponse error) {
		super(error.message());
		this.status = status;
		this.error = error;
	}

	public HttpStatusCode getStatus() {
		return status;
	}

	public PublicErrorResponse getError() {
		return error;
	}
}
