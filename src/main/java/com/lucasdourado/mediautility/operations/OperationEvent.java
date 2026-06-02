package com.lucasdourado.mediautility.operations;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Persistent event record for operation metrics.
 */
@Entity
@Table(name = "operation_events")
public class OperationEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "operation_id", nullable = false)
	private Operation operation;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OperationType operationType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OperationEventType eventType;

	@Column(nullable = false)
	private Instant occurredAt;

	@Column(length = 1024)
	private String failureReason;

	protected OperationEvent() {
	}

	private OperationEvent(
			Operation operation,
			OperationType operationType,
			OperationEventType eventType,
			Instant occurredAt,
			String failureReason) {
		this.operation = operation;
		this.operationType = operationType;
		this.eventType = eventType;
		this.occurredAt = occurredAt;
		this.failureReason = failureReason;
	}

	public static OperationEvent started(Operation operation, Instant occurredAt) {
		return create(operation, OperationEventType.STARTED, occurredAt, null);
	}

	public static OperationEvent completed(Operation operation, Instant occurredAt) {
		return create(operation, OperationEventType.COMPLETED, occurredAt, null);
	}

	public static OperationEvent failed(Operation operation, Instant occurredAt, String failureReason) {
		return create(operation, OperationEventType.FAILED, occurredAt, failureReason);
	}

	private static OperationEvent create(
			Operation operation,
			OperationEventType eventType,
			Instant occurredAt,
			String failureReason) {
		return new OperationEvent(operation, operation.getType(), eventType, occurredAt, failureReason);
	}

	public Long getId() {
		return id;
	}

	public Operation getOperation() {
		return operation;
	}

	public OperationType getOperationType() {
		return operationType;
	}

	public OperationEventType getEventType() {
		return eventType;
	}

	public Instant getOccurredAt() {
		return occurredAt;
	}

	public String getFailureReason() {
		return failureReason;
	}
}
