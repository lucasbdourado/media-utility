package com.lucasdourado.mediautility.operations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

class OperationPersistenceMappingTest {

	@Test
	void operationIsMappedAsJpaEntity() {
		Entity entity = Operation.class.getAnnotation(Entity.class);
		Table table = Operation.class.getAnnotation(Table.class);

		assertNotNull(entity);
		assertNotNull(table);
		assertEquals("operations", table.name());
	}

	@Test
	void operationEnumsAreStoredAsStrings() throws NoSuchFieldException {
		assertEnumStoredAsString("type");
		assertEnumStoredAsString("status");
	}

	@Test
	void resultMetadataIsEmbeddableAndMapsInternalPathColumn() throws NoSuchFieldException {
		assertTrue(ResultFileMetadata.class.isAnnotationPresent(Embeddable.class));

		Field internalPath = ResultFileMetadata.class.getDeclaredField("internalPath");
		Column column = internalPath.getAnnotation(Column.class);

		assertNotNull(column);
		assertEquals("result_internal_path", column.name());
	}

	private static void assertEnumStoredAsString(String fieldName) throws NoSuchFieldException {
		Field field = Operation.class.getDeclaredField(fieldName);
		Enumerated enumerated = field.getAnnotation(Enumerated.class);

		assertNotNull(enumerated);
		assertEquals(EnumType.STRING, enumerated.value());
	}
}
