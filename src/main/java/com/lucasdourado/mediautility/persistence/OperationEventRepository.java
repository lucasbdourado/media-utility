package com.lucasdourado.mediautility.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lucasdourado.mediautility.operations.OperationEvent;

/**
 * Persistence boundary for operation event metadata.
 */
public interface OperationEventRepository extends JpaRepository<OperationEvent, Long> {
}
