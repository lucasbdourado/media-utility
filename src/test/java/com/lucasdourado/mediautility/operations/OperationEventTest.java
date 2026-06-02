package com.lucasdourado.mediautility.operations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class OperationEventTest {

	@Test
	void eventTypeRepresentsStartedCompletedAndFailed() {
		assertEquals(OperationEventType.STARTED, OperationEventType.valueOf("STARTED"));
		assertEquals(OperationEventType.COMPLETED, OperationEventType.valueOf("COMPLETED"));
		assertEquals(OperationEventType.FAILED, OperationEventType.valueOf("FAILED"));
		assertEquals(3, OperationEventType.values().length);
	}

	@Test
	void createsStartedEventWithOperationTypeSnapshot() {
		Operation operation = Operation.create(OperationType.CONVERSION, Instant.parse("2026-06-02T12:00:00Z"));
		Instant occurredAt = Instant.parse("2026-06-02T12:01:00Z");

		OperationEvent event = OperationEvent.started(operation, occurredAt);

		assertNull(event.getId());
		assertSame(operation, event.getOperation());
		assertEquals(OperationType.CONVERSION, event.getOperationType());
		assertEquals(OperationEventType.STARTED, event.getEventType());
		assertEquals(occurredAt, event.getOccurredAt());
		assertNull(event.getFailureReason());
	}

	@Test
	void createsCompletedEventWithoutFailureReason() {
		Operation operation = Operation.create(OperationType.URL_DOWNLOAD, Instant.parse("2026-06-02T12:00:00Z"));
		Instant occurredAt = Instant.parse("2026-06-02T12:05:00Z");

		OperationEvent event = OperationEvent.completed(operation, occurredAt);

		assertSame(operation, event.getOperation());
		assertEquals(OperationType.URL_DOWNLOAD, event.getOperationType());
		assertEquals(OperationEventType.COMPLETED, event.getEventType());
		assertEquals(occurredAt, event.getOccurredAt());
		assertNull(event.getFailureReason());
	}

	@Test
	void createsFailedEventWithOptionalFailureReason() {
		Operation operation = Operation.create(OperationType.URL_DOWNLOAD, Instant.parse("2026-06-02T12:00:00Z"));
		Instant occurredAt = Instant.parse("2026-06-02T12:02:00Z");

		OperationEvent event = OperationEvent.failed(operation, occurredAt, "Unsupported URL");

		assertSame(operation, event.getOperation());
		assertEquals(OperationType.URL_DOWNLOAD, event.getOperationType());
		assertEquals(OperationEventType.FAILED, event.getEventType());
		assertEquals(occurredAt, event.getOccurredAt());
		assertEquals("Unsupported URL", event.getFailureReason());
	}
}
