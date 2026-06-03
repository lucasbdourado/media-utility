package com.lucasdourado.mediautility.media.download;

/**
 * Domain-specific runtime exception thrown when a media download operation fails,
 * times out, or produces invalid output.
 */
public class DownloadException extends RuntimeException {

	public DownloadException(String message) {
		super(message);
	}

	public DownloadException(String message, Throwable cause) {
		super(message, cause);
	}
}
