package com.lucasdourado.mediautility.media.download;

/**
 * Exception thrown when URL validation fails.
 */
public class UrlValidationException extends RuntimeException {

	private final ErrorReason reason;

	public UrlValidationException(ErrorReason reason, String message) {
		super(message);
		this.reason = reason;
	}

	public UrlValidationException(ErrorReason reason, String message, Throwable cause) {
		super(message, cause);
		this.reason = reason;
	}

	public ErrorReason getReason() {
		return reason;
	}

	public enum ErrorReason {
		MISSING_OR_EMPTY,
		INVALID_SYNTAX,
		INVALID_SCHEME,
		MISSING_HOST,
		SSRF_ATTEMPT
	}
}
