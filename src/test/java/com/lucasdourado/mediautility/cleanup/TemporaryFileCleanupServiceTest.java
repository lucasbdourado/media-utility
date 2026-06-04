package com.lucasdourado.mediautility.cleanup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import com.lucasdourado.mediautility.operations.Operation;
import com.lucasdourado.mediautility.operations.OperationStatus;
import com.lucasdourado.mediautility.operations.OperationType;
import com.lucasdourado.mediautility.operations.ResultFileMetadata;
import com.lucasdourado.mediautility.persistence.OperationRepository;
import com.lucasdourado.mediautility.storage.TemporaryStorageService;

class TemporaryFileCleanupServiceTest {

	private static final Instant NOW = Instant.parse("2026-06-03T12:00:00Z");
	private final Clock clock = Clock.fixed(NOW, ZoneId.of("UTC"));

	private OperationRepository operationRepository;
	private TemporaryStorageService temporaryStorageService;
	private TemporaryFileCleanupService cleanupService;

	@BeforeEach
	void setUp() {
		operationRepository = mock(OperationRepository.class);
		temporaryStorageService = mock(TemporaryStorageService.class);
		cleanupService = new TemporaryFileCleanupService(operationRepository, temporaryStorageService, clock);
	}

	@Test
	void successfullyCleansUpExpiredOperations() {
		Operation expiredOp1 = Operation.create(OperationType.CONVERSION, NOW.minusSeconds(7200));
		ReflectionTestUtils.setField(expiredOp1, "id", 1L);
		ResultFileMetadata file1 = new ResultFileMetadata("file1.mp3", "audio/mpeg", 100L, "keys/file1.mp3");
		expiredOp1.complete(file1, NOW.minusSeconds(7100), NOW.minusSeconds(3600));

		Operation expiredOp2 = Operation.create(OperationType.URL_DOWNLOAD, NOW.minusSeconds(7200));
		ReflectionTestUtils.setField(expiredOp2, "id", 2L);
		ResultFileMetadata file2 = new ResultFileMetadata("file2.mp4", "video/mp4", 500L, "keys/file2.mp4");
		expiredOp2.complete(file2, NOW.minusSeconds(7100), NOW.minusSeconds(1800));

		List<Operation> expiredOps = List.of(expiredOp1, expiredOp2);
		when(operationRepository.findByStatusAndExpiresAtBeforeAndResultFileIsNotNull(
				eq(OperationStatus.COMPLETED), eq(NOW)))
				.thenReturn(expiredOps);

		cleanupService.cleanupExpiredFiles();

		// Verify physical files deletion
		verify(temporaryStorageService).delete("keys/file1.mp3");
		verify(temporaryStorageService).delete("keys/file2.mp4");

		// Verify database saving with cleared result files
		ArgumentCaptor<Operation> operationCaptor = ArgumentCaptor.forClass(Operation.class);
		verify(operationRepository, times(2)).save(operationCaptor.capture());

		List<Operation> savedOperations = operationCaptor.getAllValues();
		assertThat(savedOperations).hasSize(2);
		assertThat(savedOperations.get(0).getResultFile()).isNull();
		assertThat(savedOperations.get(1).getResultFile()).isNull();
	}

	@Test
	void doesNothingWhenNoExpiredOperationsFound() {
		when(operationRepository.findByStatusAndExpiresAtBeforeAndResultFileIsNotNull(
				eq(OperationStatus.COMPLETED), eq(NOW)))
				.thenReturn(Collections.emptyList());

		cleanupService.cleanupExpiredFiles();

		verifyNoInteractions(temporaryStorageService);
		verify(operationRepository, never()).save(any(Operation.class));
	}

	@Test
	void continuesProcessingEvenWhenOneFileDeletionFails() {
		Operation expiredOp1 = Operation.create(OperationType.CONVERSION, NOW.minusSeconds(7200));
		ReflectionTestUtils.setField(expiredOp1, "id", 1L);
		ResultFileMetadata file1 = new ResultFileMetadata("file1.mp3", "audio/mpeg", 100L, "keys/file1.mp3");
		expiredOp1.complete(file1, NOW.minusSeconds(7100), NOW.minusSeconds(3600));

		Operation expiredOp2 = Operation.create(OperationType.URL_DOWNLOAD, NOW.minusSeconds(7200));
		ReflectionTestUtils.setField(expiredOp2, "id", 2L);
		ResultFileMetadata file2 = new ResultFileMetadata("file2.mp4", "video/mp4", 500L, "keys/file2.mp4");
		expiredOp2.complete(file2, NOW.minusSeconds(7100), NOW.minusSeconds(1800));

		List<Operation> expiredOps = List.of(expiredOp1, expiredOp2);
		when(operationRepository.findByStatusAndExpiresAtBeforeAndResultFileIsNotNull(
				eq(OperationStatus.COMPLETED), eq(NOW)))
				.thenReturn(expiredOps);

		// Throw exception on first delete, second should still succeed
		doThrow(new RuntimeException("Disk write protected/error")).when(temporaryStorageService).delete("keys/file1.mp3");

		cleanupService.cleanupExpiredFiles();

		// Verify both delete calls were attempted
		verify(temporaryStorageService).delete("keys/file1.mp3");
		verify(temporaryStorageService).delete("keys/file2.mp4");

		// Only expiredOp2 should be saved with cleared resultFile because expiredOp1 failed during process
		ArgumentCaptor<Operation> operationCaptor = ArgumentCaptor.forClass(Operation.class);
		verify(operationRepository, times(1)).save(operationCaptor.capture());

		List<Operation> savedOperations = operationCaptor.getAllValues();
		assertThat(savedOperations).hasSize(1);
		assertThat(savedOperations.get(0).getId()).isEqualTo(2L);
		assertThat(savedOperations.get(0).getResultFile()).isNull();

		// Check that expiredOp1 result file was NOT cleared (retaining eligibility for retry)
		assertThat(expiredOp1.getResultFile()).isNotNull();
	}
}
