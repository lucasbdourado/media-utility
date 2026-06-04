package com.lucasdourado.mediautility.cleanup;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lucasdourado.mediautility.operations.Operation;
import com.lucasdourado.mediautility.operations.OperationStatus;
import com.lucasdourado.mediautility.persistence.OperationRepository;
import com.lucasdourado.mediautility.storage.TemporaryStorageService;

/**
 * Scheduled service responsible for cleaning up expired operations' temporary files and metadata.
 */
@Service
public class TemporaryFileCleanupService {

	private static final Logger log = LoggerFactory.getLogger(TemporaryFileCleanupService.class);

	private final OperationRepository operationRepository;
	private final TemporaryStorageService temporaryStorageService;
	private final Clock clock;

	@Autowired
	public TemporaryFileCleanupService(
			OperationRepository operationRepository,
			TemporaryStorageService temporaryStorageService) {
		this(operationRepository, temporaryStorageService, Clock.systemUTC());
	}

	public TemporaryFileCleanupService(
			OperationRepository operationRepository,
			TemporaryStorageService temporaryStorageService,
			Clock clock) {
		this.operationRepository = operationRepository;
		this.temporaryStorageService = temporaryStorageService;
		this.clock = clock;
	}

	@Scheduled(fixedDelayString = "${media-utility.cleanup.fixed-delay:60000}")
	@Transactional
	public void cleanupExpiredFiles() {
		Instant now = clock.instant();
		log.debug("Starting expired temporary file cleanup sweep at {}", now);

		List<Operation> expiredOperations = operationRepository
				.findByStatusAndExpiresAtBeforeAndResultFileIsNotNull(OperationStatus.COMPLETED, now);

		if (expiredOperations.isEmpty()) {
			return;
		}

		log.info("Found {} expired completed operation(s) with temporary files to clean up", expiredOperations.size());

		for (Operation operation : expiredOperations) {
			try {
				String internalPath = operation.getResultFile().getInternalPath();
				log.info("Cleaning up expired temporary file for operation id={}: internalPath={}", operation.getId(), internalPath);

				try {
					temporaryStorageService.delete(internalPath);
				} catch (Exception e) {
					log.error("Failed to delete physical file for operation id={} at path {}: {}", 
							operation.getId(), internalPath, e.getMessage(), e);
					// We continue to database updates even if file delete fails (or we can choose to retry next time).
					// If the file is not deleted, leaving metadata on DB keeps it eligible for cleanup in the next run.
					// Let's throw to skip saving, or just log error and retry later by not saving this operation.
					throw e;
				}

				operation.clearResultFile();
				operationRepository.save(operation);
				log.info("Successfully cleared metadata for expired operation id={}", operation.getId());
			} catch (Exception e) {
				log.error("Failed to process cleanup for operation id={}: {}", operation.getId(), e.getMessage());
			}
		}

		log.debug("Finished expired temporary file cleanup sweep");
	}
}
