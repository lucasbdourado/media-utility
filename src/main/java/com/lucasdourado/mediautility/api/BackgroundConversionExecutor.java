package com.lucasdourado.mediautility.api;

import java.io.IOException;
import java.io.InputStream;
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

import com.lucasdourado.mediautility.media.conversion.Mp4ToMp3Converter;
import com.lucasdourado.mediautility.operations.Operation;
import com.lucasdourado.mediautility.operations.OperationEvent;
import com.lucasdourado.mediautility.operations.ResultFileMetadata;
import com.lucasdourado.mediautility.persistence.OperationRepository;
import com.lucasdourado.mediautility.persistence.OperationEventRepository;
import com.lucasdourado.mediautility.storage.TemporaryStorageService;

/**
 * Helper component that executes media conversion asynchronously in a background thread.
 * This is separated from OperationService to avoid Spring @Async self-invocation proxy limitations.
 */
@Component
public class BackgroundConversionExecutor {

	private static final Logger log = LoggerFactory.getLogger(BackgroundConversionExecutor.class);

	private final OperationRepository operationRepository;
	private final OperationEventRepository operationEventRepository;
	private final Mp4ToMp3Converter mp4ToMp3Converter;
	private final TemporaryStorageService temporaryStorageService;
	private final Duration retentionDuration;

	public BackgroundConversionExecutor(
			OperationRepository operationRepository,
			OperationEventRepository operationEventRepository,
			Mp4ToMp3Converter mp4ToMp3Converter,
			TemporaryStorageService temporaryStorageService,
			@Value("${media-utility.storage.retention:1h}") Duration retentionDuration) {
		this.operationRepository = operationRepository;
		this.operationEventRepository = operationEventRepository;
		this.mp4ToMp3Converter = mp4ToMp3Converter;
		this.temporaryStorageService = temporaryStorageService;
		this.retentionDuration = retentionDuration;
	}

	@Async
	public void executeConversion(Long operationId, Path sourcePath, String originalFilename) {
		log.info("Starting background conversion for operation {} from source {}", operationId, sourcePath);
		Operation operation = operationRepository.findById(operationId)
				.orElseThrow(() -> new IllegalArgumentException("Operation not found: " + operationId));

		operation.markProcessing();
		operation = operationRepository.save(operation);

		Path targetPath = null;
		try {
			targetPath = Files.createTempFile("media-utility-convert-", ".mp3");

			mp4ToMp3Converter.convert(sourcePath, targetPath);

			String targetFilename = replaceExtension(originalFilename);
			ResultFileMetadata resultMetadata;
			try (InputStream content = Files.newInputStream(targetPath)) {
				resultMetadata = temporaryStorageService.storeResult(
						String.valueOf(operationId),
						targetFilename,
						"audio/mpeg",
						content);
			}

			Instant completedAt = Instant.now();
			Instant expiresAt = completedAt.plus(retentionDuration);
			operation.complete(resultMetadata, completedAt, expiresAt);
			operationRepository.save(operation);
			operationEventRepository.save(OperationEvent.completed(operation, completedAt));
			log.info("Successfully completed conversion for operation {}", operationId);
		}
		catch (Exception e) {
			log.error("Conversion failed for operation " + operationId, e);
			Instant completedAt = Instant.now();
			String reason = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
			operation.fail(reason, completedAt);
			operationRepository.save(operation);
			operationEventRepository.save(OperationEvent.failed(operation, completedAt, reason));
		}
		finally {
			deleteFileQuietly(sourcePath);
			deleteFileQuietly(targetPath);
		}
	}

	private String replaceExtension(String originalFilename) {
		if (originalFilename == null || originalFilename.isBlank()) {
			return "result.mp3";
		}
		if (originalFilename.toLowerCase(Locale.ROOT).endsWith(".mp4")) {
			return originalFilename.substring(0, originalFilename.length() - 4) + ".mp3";
		}
		return originalFilename + ".mp3";
	}

	private void deleteFileQuietly(Path path) {
		if (path != null) {
			try {
				Files.deleteIfExists(path);
			}
			catch (IOException ex) {
				log.warn("Failed to delete temporary file: {}", path, ex);
			}
		}
	}
}
