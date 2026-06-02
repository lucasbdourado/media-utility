package com.lucasdourado.mediautility.storage;

/**
 * Raised when a temporary storage operation cannot be completed safely.
 */
public class StorageException extends RuntimeException {

	public StorageException(String message) {
		super(message);
	}

	public StorageException(String message, Throwable cause) {
		super(message, cause);
	}
}
