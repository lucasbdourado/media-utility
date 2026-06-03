package com.lucasdourado.mediautility.api;

public record PublicResultMetadata(
		String fileName,
		String contentType,
		Long sizeBytes,
		String downloadUrl) {
}
