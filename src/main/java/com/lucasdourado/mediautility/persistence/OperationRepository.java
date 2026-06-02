package com.lucasdourado.mediautility.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lucasdourado.mediautility.operations.Operation;

/**
 * Persistence boundary for operation metadata.
 */
public interface OperationRepository extends JpaRepository<Operation, Long> {
}
