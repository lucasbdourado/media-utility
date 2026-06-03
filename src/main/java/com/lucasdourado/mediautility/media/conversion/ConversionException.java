package com.lucasdourado.mediautility.media.conversion;

/**
 * Domain-specific runtime exception thrown when a media conversion operation fails,
 * times out, or produces invalid output.
 */
public class ConversionException extends RuntimeException {

    public ConversionException(String message) {
        super(message);
    }

    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}
