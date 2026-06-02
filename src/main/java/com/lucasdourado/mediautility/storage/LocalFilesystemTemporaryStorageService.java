package com.lucasdourado.mediautility.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.lucasdourado.mediautility.operations.ResultFileMetadata;

/**
 * Local filesystem implementation that persists root-relative storage keys.
 */
@Service
public class LocalFilesystemTemporaryStorageService implements TemporaryStorageService {

	private static final String OPERATIONS_PREFIX = "operations";

	private final Path storageRoot;

	public LocalFilesystemTemporaryStorageService(@Value("${media-utility.storage.root}") Path storageRoot) {
		this.storageRoot = normalizeRoot(storageRoot);
	}

	@Override
	public ResultFileMetadata storeResult(
			String operationId,
			String fileName,
			String contentType,
			InputStream content) {
		Objects.requireNonNull(content, "content must not be null");

		String safeOperationId = requireSinglePathSegment(operationId, "operationId");
		String safeFileName = requireSinglePathSegment(fileName, "fileName");
		String internalPath = toStorageKey(OPERATIONS_PREFIX, safeOperationId, safeFileName);
		Path target = resolve(internalPath);

		try {
			Files.createDirectories(target.getParent());
			Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
			long sizeBytes = Files.size(target);

			return new ResultFileMetadata(safeFileName, contentType, sizeBytes, internalPath);
		}
		catch (IOException ex) {
			throw new StorageException("Could not store temporary result file.", ex);
		}
	}

	@Override
	public Path resolve(String internalPath) {
		Path relativePath = normalizeRelativeKey(internalPath);
		Path resolved = storageRoot.resolve(relativePath).normalize();
		if (!resolved.startsWith(storageRoot)) {
			throw new StorageException("Storage key resolves outside the configured storage root.");
		}
		return resolved;
	}

	@Override
	public void delete(String internalPath) {
		Path target = resolve(internalPath);
		try {
			Files.deleteIfExists(target);
		}
		catch (IOException ex) {
			throw new StorageException("Could not delete temporary result file.", ex);
		}
	}

	private static Path normalizeRoot(Path storageRoot) {
		Objects.requireNonNull(storageRoot, "storageRoot must not be null");
		Path normalizedRoot = storageRoot.toAbsolutePath().normalize();
		if (normalizedRoot.toString().isBlank()) {
			throw new StorageException("Storage root must not be blank.");
		}
		try {
			Files.createDirectories(normalizedRoot);
			if (!Files.isDirectory(normalizedRoot, LinkOption.NOFOLLOW_LINKS)) {
				throw new StorageException("Storage root must be a directory.");
			}
			return normalizedRoot;
		}
		catch (IOException ex) {
			throw new StorageException("Could not initialize temporary storage root.", ex);
		}
	}

	private static Path normalizeRelativeKey(String internalPath) {
		if (internalPath == null || internalPath.isBlank()) {
			throw new StorageException("Storage key must not be blank.");
		}
		String normalizedKey = internalPath.replace('\\', '/');
		if (normalizedKey.contains(":")) {
			throw new StorageException("Storage key must not contain drive or scheme separators.");
		}
		Path relativePath = Path.of(normalizedKey).normalize();
		if (relativePath.isAbsolute() || relativePath.startsWith("..") || relativePath.toString().isBlank()) {
			throw new StorageException("Storage key must be root-relative.");
		}
		return relativePath;
	}

	private static String requireSinglePathSegment(String value, String fieldName) {
		if (value == null || value.isBlank()) {
			throw new StorageException(fieldName + " must not be blank.");
		}
		String normalized = value.trim();
		if (normalized.contains(":")) {
			throw new StorageException(fieldName + " must not contain drive or scheme separators.");
		}
		Path path = Path.of(normalized);
		if (path.isAbsolute() || path.getNameCount() != 1 || ".".equals(normalized) || "..".equals(normalized)) {
			throw new StorageException(fieldName + " must be a single path segment.");
		}
		return normalized;
	}

	private static String toStorageKey(String first, String second, String third) {
		return first + "/" + second + "/" + third;
	}
}
