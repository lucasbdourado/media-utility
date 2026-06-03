package com.lucasdourado.mediautility.media.conversion;

/**
 * Exception thrown when MP4 upload validation fails.
 */
public class Mp4ValidationException extends RuntimeException {

	private final ErrorReason reason;

	public Mp4ValidationException(ErrorReason reason, String message) {
		super(message);
		this.reason = reason;
	}

	public Mp4ValidationException(ErrorReason reason, String message, Throwable cause) {
		super(message, cause);
		this.reason = reason;
	}

	public ErrorReason getReason() {
		return reason;
	}

	public enum ErrorReason {
		MISSING_OR_EMPTY,
		INVALID_EXTENSION,
		INVALID_CONTENT_TYPE,
		SIZE_LIMIT_EXCEEDED,
		INVALID_HEADER
	}
}
