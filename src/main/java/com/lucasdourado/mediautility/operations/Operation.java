package com.lucasdourado.mediautility.operations;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Shared domain model for conversion and URL download operation metadata.
 */
@Entity
@Table(name = "operations")
public class Operation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OperationType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OperationStatus status;

	@Column(nullable = false)
	private Instant createdAt;

	private Instant completedAt;

	private Instant expiresAt;

	@Column(length = 1024)
	private String failureReason;

	@Embedded
	private ResultFileMetadata resultFile;

	protected Operation() {
	}

	private Operation(OperationType type, Instant createdAt) {
		this.type = type;
		this.status = OperationStatus.PENDING;
		this.createdAt = createdAt;
	}

	public static Operation create(OperationType type, Instant createdAt) {
		return new Operation(type, createdAt);
	}

	public void markProcessing() {
		this.status = OperationStatus.PROCESSING;
	}

	public void complete(ResultFileMetadata resultFile, Instant completedAt, Instant expiresAt) {
		this.status = OperationStatus.COMPLETED;
		this.resultFile = resultFile;
		this.completedAt = completedAt;
		this.expiresAt = expiresAt;
		this.failureReason = null;
	}

	public void clearResultFile() {
		this.resultFile = null;
	}

	public void fail(String failureReason, Instant completedAt) {
		this.status = OperationStatus.FAILED;
		this.failureReason = failureReason;
		this.completedAt = completedAt;
		this.expiresAt = null;
		this.resultFile = null;
	}

	public Long getId() {
		return id;
	}

	public OperationType getType() {
		return type;
	}

	public OperationStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getCompletedAt() {
		return completedAt;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public ResultFileMetadata getResultFile() {
		return resultFile;
	}

	public boolean isCompleted() {
		return status == OperationStatus.COMPLETED;
	}

	public boolean isFailed() {
		return status == OperationStatus.FAILED;
	}
}
