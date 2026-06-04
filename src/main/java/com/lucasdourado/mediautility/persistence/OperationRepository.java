package com.lucasdourado.mediautility.persistence;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lucasdourado.mediautility.operations.Operation;
import com.lucasdourado.mediautility.operations.OperationStatus;

/**
 * Persistence boundary for operation metadata.
 */
public interface OperationRepository extends JpaRepository<Operation, Long> {

	List<Operation> findByStatusAndExpiresAtBeforeAndResultFileIsNotNull(
			OperationStatus status,
			Instant time);
}
