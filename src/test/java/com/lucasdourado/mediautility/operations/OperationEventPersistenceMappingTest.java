package com.lucasdourado.mediautility.operations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

class OperationEventPersistenceMappingTest {

	@Test
	void operationEventIsMappedAsJpaEntity() {
		Entity entity = OperationEvent.class.getAnnotation(Entity.class);
		Table table = OperationEvent.class.getAnnotation(Table.class);

		assertNotNull(entity);
		assertNotNull(table);
		assertEquals("operation_events", table.name());
	}

	@Test
	void operationRelationIsRequired() throws NoSuchFieldException {
		Field operation = OperationEvent.class.getDeclaredField("operation");
		ManyToOne manyToOne = operation.getAnnotation(ManyToOne.class);
		JoinColumn joinColumn = operation.getAnnotation(JoinColumn.class);

		assertNotNull(manyToOne);
		assertEquals(false, manyToOne.optional());
		assertNotNull(joinColumn);
		assertEquals("operation_id", joinColumn.name());
		assertEquals(false, joinColumn.nullable());
	}

	@Test
	void eventEnumsAreStoredAsStrings() throws NoSuchFieldException {
		assertEnumStoredAsString("operationType");
		assertEnumStoredAsString("eventType");
	}

	@Test
	void occurredAtIsRequiredAndFailureReasonIsBounded() throws NoSuchFieldException {
		Field occurredAt = OperationEvent.class.getDeclaredField("occurredAt");
		Column occurredAtColumn = occurredAt.getAnnotation(Column.class);

		assertNotNull(occurredAtColumn);
		assertEquals(false, occurredAtColumn.nullable());

		Field failureReason = OperationEvent.class.getDeclaredField("failureReason");
		Column failureReasonColumn = failureReason.getAnnotation(Column.class);

		assertNotNull(failureReasonColumn);
		assertEquals(1024, failureReasonColumn.length());
	}

	private static void assertEnumStoredAsString(String fieldName) throws NoSuchFieldException {
		Field field = OperationEvent.class.getDeclaredField(fieldName);
		Enumerated enumerated = field.getAnnotation(Enumerated.class);

		assertNotNull(enumerated);
		assertEquals(EnumType.STRING, enumerated.value());
	}
}
