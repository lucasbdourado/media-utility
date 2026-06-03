package com.lucasdourado.mediautility.api;

import java.time.Instant;

import com.lucasdourado.mediautility.operations.OperationStatus;
import com.lucasdourado.mediautility.operations.OperationType;

public record PublicOperationResponse(
		Long operationId,
		OperationType type,
		OperationStatus status,
		Instant createdAt,
		Instant completedAt,
		Instant expiresAt,
		PublicResultMetadata result,
		PublicErrorResponse error,
		OperationLinks links) {

	public static PublicOperationResponse pending(Long operationId, OperationType type, Instant createdAt) {
		return new PublicOperationResponse(
				operationId,
				type,
				OperationStatus.PENDING,
				createdAt,
				null,
				null,
				null,
				null,
				new OperationLinks("/api/operations/" + operationId));
	}
}
