package com.lucasdourado.mediautility.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.lucasdourado.mediautility.operations.ResultFileMetadata;

class LocalFilesystemTemporaryStorageServiceTest {

	@TempDir
	Path storageRoot;

	@Test
	void storesResultUnderConfiguredRootWithRootRelativeMetadata() throws IOException {
		LocalFilesystemTemporaryStorageService storage = new LocalFilesystemTemporaryStorageService(storageRoot);

		ResultFileMetadata metadata = storage.storeResult(
				"operation-1",
				"result.mp3",
				"audio/mpeg",
				new ByteArrayInputStream("media".getBytes(StandardCharsets.UTF_8)));

		assertEquals("result.mp3", metadata.getFileName());
		assertEquals("audio/mpeg", metadata.getContentType());
		assertEquals(5L, metadata.getSizeBytes());
		assertEquals("operations/operation-1/result.mp3", metadata.getInternalPath());
		assertFalse(Path.of(metadata.getInternalPath()).isAbsolute());
		assertEquals("media", Files.readString(storageRoot.resolve("operations/operation-1/result.mp3")));
	}

	@Test
	void resolvesInternalPathOnlyInsideStorageRoot() {
		LocalFilesystemTemporaryStorageService storage = new LocalFilesystemTemporaryStorageService(storageRoot);

		Path resolved = storage.resolve("operations/operation-1/result.mp4");

		assertTrue(resolved.startsWith(storageRoot.toAbsolutePath().normalize()));
		assertEquals(storageRoot.resolve("operations/operation-1/result.mp4").toAbsolutePath().normalize(), resolved);
	}

	@Test
	void rejectsTraversalWhenResolvingInternalPath() {
		LocalFilesystemTemporaryStorageService storage = new LocalFilesystemTemporaryStorageService(storageRoot);

		assertThrows(StorageException.class, () -> storage.resolve("../outside.mp4"));
		assertThrows(StorageException.class, () -> storage.resolve("operations/../.."));
		assertThrows(StorageException.class, () -> storage.resolve("C:/outside.mp4"));
	}

	@Test
	void rejectsUnsafeOperationAndFileSegmentsWhenStoring() {
		LocalFilesystemTemporaryStorageService storage = new LocalFilesystemTemporaryStorageService(storageRoot);

		assertThrows(StorageException.class, () -> storage.storeResult(
				"../operation-1",
				"result.mp4",
				"video/mp4",
				new ByteArrayInputStream(new byte[] { 1 })));
		assertThrows(StorageException.class, () -> storage.storeResult(
				"operation-1",
				"../result.mp4",
				"video/mp4",
				new ByteArrayInputStream(new byte[] { 1 })));
	}

	@Test
	void deletesExistingFileAndTreatsMissingFileAsSuccess() throws IOException {
		LocalFilesystemTemporaryStorageService storage = new LocalFilesystemTemporaryStorageService(storageRoot);
		ResultFileMetadata metadata = storage.storeResult(
				"operation-1",
				"result.mp3",
				"audio/mpeg",
				new ByteArrayInputStream("media".getBytes(StandardCharsets.UTF_8)));
		Path storedFile = storage.resolve(metadata.getInternalPath());

		storage.delete(metadata.getInternalPath());
		storage.delete(metadata.getInternalPath());

		assertFalse(Files.exists(storedFile));
	}

	@Test
	void rejectsTraversalWhenDeleting() {
		LocalFilesystemTemporaryStorageService storage = new LocalFilesystemTemporaryStorageService(storageRoot);

		assertThrows(StorageException.class, () -> storage.delete("../outside.mp4"));
	}
}
