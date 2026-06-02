package com.lucasdourado.mediautility.operations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class OperationTest {

	@Test
	void createsPendingOperationForType() {
		Instant createdAt = Instant.parse("2026-06-02T12:00:00Z");

		Operation operation = Operation.create(OperationType.CONVERSION, createdAt);

		assertNull(operation.getId());
		assertEquals(OperationType.CONVERSION, operation.getType());
		assertEquals(OperationStatus.PENDING, operation.getStatus());
		assertEquals(createdAt, operation.getCreatedAt());
		assertNull(operation.getCompletedAt());
		assertNull(operation.getExpiresAt());
		assertNull(operation.getFailureReason());
		assertNull(operation.getResultFile());
		assertFalse(operation.isCompleted());
		assertFalse(operation.isFailed());
	}

	@Test
	void marksOperationAsProcessing() {
		Operation operation = Operation.create(OperationType.URL_DOWNLOAD, Instant.parse("2026-06-02T12:00:00Z"));

		operation.markProcessing();

		assertEquals(OperationStatus.PROCESSING, operation.getStatus());
		assertFalse(operation.isCompleted());
		assertFalse(operation.isFailed());
	}

	@Test
	void completesOperationWithResultMetadataAndExpiration() {
		Operation operation = Operation.create(OperationType.CONVERSION, Instant.parse("2026-06-02T12:00:00Z"));
		ResultFileMetadata result = new ResultFileMetadata(
				"result.mp3",
				"audio/mpeg",
				1024L,
				"operations/operation-1/result.mp3");
		Instant completedAt = Instant.parse("2026-06-02T12:03:00Z");
		Instant expiresAt = Instant.parse("2026-06-02T13:03:00Z");

		operation.complete(result, completedAt, expiresAt);

		assertEquals(OperationStatus.COMPLETED, operation.getStatus());
		assertSame(result, operation.getResultFile());
		assertEquals(completedAt, operation.getCompletedAt());
		assertEquals(expiresAt, operation.getExpiresAt());
		assertNull(operation.getFailureReason());
		assertTrue(operation.isCompleted());
		assertFalse(operation.isFailed());
	}

	@Test
	void failsOperationWithFailureReasonWithoutResultMetadata() {
		Operation operation = Operation.create(OperationType.URL_DOWNLOAD, Instant.parse("2026-06-02T12:00:00Z"));
		Instant completedAt = Instant.parse("2026-06-02T12:01:00Z");

		operation.fail("Unsupported URL", completedAt);

		assertEquals(OperationStatus.FAILED, operation.getStatus());
		assertEquals("Unsupported URL", operation.getFailureReason());
		assertEquals(completedAt, operation.getCompletedAt());
		assertNull(operation.getExpiresAt());
		assertNull(operation.getResultFile());
		assertFalse(operation.isCompleted());
		assertTrue(operation.isFailed());
	}

	@Test
	void resultFileMetadataKeepsInternalPathServerSideOnly() {
		ResultFileMetadata result = new ResultFileMetadata(
				"download.mp4",
				"video/mp4",
				2048L,
				"operations/operation-2/download.mp4");

		assertEquals("download.mp4", result.getFileName());
		assertEquals("video/mp4", result.getContentType());
		assertEquals(2048L, result.getSizeBytes());
		assertEquals("operations/operation-2/download.mp4", result.getInternalPath());
	}
}
