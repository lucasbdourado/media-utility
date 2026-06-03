package com.lucasdourado.mediautility.api;

import java.net.URI;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

/**
 * Delegation boundary for future operation orchestration.
 */
public interface OperationApiPort {

	PublicOperationResponse createConversion(MultipartFile file);

	PublicOperationResponse createDownload(URI url);

	PublicOperationResponse getOperation(Long operationId);

	ResultDownload getResult(Long operationId);

	record ResultDownload(Resource resource, String fileName, MediaType contentType, long contentLength) {
	}
}
