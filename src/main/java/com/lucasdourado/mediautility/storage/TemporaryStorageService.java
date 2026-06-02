package com.lucasdourado.mediautility.storage;

import java.io.InputStream;
import java.nio.file.Path;

import com.lucasdourado.mediautility.operations.ResultFileMetadata;

/**
 * Stores temporary operation result files and resolves their server-side keys.
 */
public interface TemporaryStorageService extends StorageBoundary {

	ResultFileMetadata storeResult(
			String operationId,
			String fileName,
			String contentType,
			InputStream content);

	Path resolve(String internalPath);

	void delete(String internalPath);
}
