package com.lucasdourado.mediautility.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.lucasdourado.mediautility.media.download.UrlDownloader;
import com.lucasdourado.mediautility.operations.Operation;
import com.lucasdourado.mediautility.operations.OperationEvent;
import com.lucasdourado.mediautility.operations.ResultFileMetadata;
import com.lucasdourado.mediautility.persistence.OperationRepository;
import com.lucasdourado.mediautility.persistence.OperationEventRepository;
import com.lucasdourado.mediautility.storage.TemporaryStorageService;

/**
 * Component that executes media download asynchronously in a background thread.
 * Separated from OperationService to avoid self-invocation proxying limitations.
 */
@Component
public class BackgroundDownloadExecutor {

	private static final Logger log = LoggerFactory.getLogger(BackgroundDownloadExecutor.class);

	private final OperationRepository operationRepository;
	private final OperationEventRepository operationEventRepository;
	private final UrlDownloader urlDownloader;
	private final TemporaryStorageService temporaryStorageService;
	private final Duration retentionDuration;

	public BackgroundDownloadExecutor(
			OperationRepository operationRepository,
			OperationEventRepository operationEventRepository,
			UrlDownloader urlDownloader,
			TemporaryStorageService temporaryStorageService,
			@Value("${media-utility.storage.retention:1h}") Duration retentionDuration) {
		this.operationRepository = operationRepository;
		this.operationEventRepository = operationEventRepository;
		this.urlDownloader = urlDownloader;
		this.temporaryStorageService = temporaryStorageService;
		this.retentionDuration = retentionDuration;
	}

	@Async
	public void executeDownload(Long operationId, URI url) {
		log.info("Starting background download for operation {} from URL {}", operationId, url);
		Operation operation = operationRepository.findById(operationId)
				.orElseThrow(() -> new IllegalArgumentException("Operation not found: " + operationId));

		operation.markProcessing();
		operation = operationRepository.save(operation);

		Path tempTarget = null;
		try {
			tempTarget = Files.createTempFile("media-utility-download-", ".mp4");

			urlDownloader.download(url, tempTarget);

			String filename = getFilenameFromUrl(url);
			ResultFileMetadata resultMetadata;
			try (InputStream content = Files.newInputStream(tempTarget)) {
				resultMetadata = temporaryStorageService.storeResult(
						String.valueOf(operationId),
						filename,
						"video/mp4",
						content);
			}

			Instant completedAt = Instant.now();
			Instant expiresAt = completedAt.plus(retentionDuration);
			operation.complete(resultMetadata, completedAt, expiresAt);
			operationRepository.save(operation);
			operationEventRepository.save(OperationEvent.completed(operation, completedAt));
			log.info("Successfully completed download for operation {}", operationId);
		}
		catch (Exception e) {
			log.error("Download failed for operation " + operationId, e);
			Instant completedAt = Instant.now();
			String reason = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
			operation.fail(reason, completedAt);
			operationRepository.save(operation);
			operationEventRepository.save(OperationEvent.failed(operation, completedAt, reason));
		}
		finally {
			if (tempTarget != null) {
				try {
					Files.deleteIfExists(tempTarget);
				}
				catch (IOException ex) {
					log.warn("Failed to delete temporary file: {}", tempTarget, ex);
				}
			}
		}
	}

	private String getFilenameFromUrl(URI url) {
		if (url == null) {
			return "download.mp4";
		}
		String path = url.getPath();
		if (path == null || path.isBlank()) {
			return "download.mp4";
		}

		// Find the last segment
		int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
		String filename = (lastSlash >= 0) ? path.substring(lastSlash + 1) : path;

		if (filename.isBlank()) {
			return "download.mp4";
		}

		// Strip path traversals
		filename = filename.replace("..", "").replace("/", "").replace("\\", "");

		// Sanitize to only safe filename characters
		filename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");

		// Ensure it ends with lowercase .mp4
		String lowercase = filename.toLowerCase(Locale.ROOT);
		if (lowercase.endsWith(".mp4")) {
			if (!filename.endsWith(".mp4")) {
				filename = filename.substring(0, filename.length() - 4) + ".mp4";
			}
		} else {
			int lastDot = filename.lastIndexOf('.');
			if (lastDot > 0) {
				filename = filename.substring(0, lastDot) + ".mp4";
			} else {
				filename = filename + ".mp4";
			}
		}

		if (filename.isBlank() || filename.equals(".mp4")) {
			return "download.mp4";
		}

		return filename;
	}
}
