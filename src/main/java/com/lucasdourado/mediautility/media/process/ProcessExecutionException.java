package com.lucasdourado.mediautility.media.process;

/**
 * Raised when a process request is invalid or cannot be launched safely.
 */
public class ProcessExecutionException extends RuntimeException {

	public ProcessExecutionException(String message) {
		super(message);
	}

	public ProcessExecutionException(String message, Throwable cause) {
		super(message, cause);
	}
}
