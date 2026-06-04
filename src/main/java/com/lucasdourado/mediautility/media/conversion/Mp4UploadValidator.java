package com.lucasdourado.mediautility.media.conversion;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * Validator component for MP4 file uploads.
 */
@Component
public class Mp4UploadValidator {

	private final long maxSizeBytes;

	public Mp4UploadValidator(@Value("${media-utility.upload.max-size-bytes:104857600}") long maxSizeBytes) {
		this.maxSizeBytes = maxSizeBytes;
	}

	/**
	 * Validates a MultipartFile representing an MP4 upload.
	 *
	 * @param file the uploaded file
	 * @throws Mp4ValidationException if validation fails
	 */
	public void validate(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new Mp4ValidationException(Mp4ValidationException.ErrorReason.MISSING_OR_EMPTY, "File is missing or empty.");
		}

		String filename = file.getOriginalFilename();
		String contentType = file.getContentType();
		long size = file.getSize();

		try (InputStream inputStream = file.getInputStream()) {
			validate(inputStream, filename, contentType, size);
		} catch (IOException e) {
			throw new Mp4ValidationException(
					Mp4ValidationException.ErrorReason.INVALID_HEADER,
					"Failed to read file stream.",
					e);
		}
	}

	/**
	 * Validates an InputStream along with filename, content-type and size.
	 *
	 * @param inputStream the input stream containing the file content
	 * @param filename the name of the file
	 * @param contentType the content type of the file
	 * @param sizeBytes the size of the file in bytes
	 * @throws Mp4ValidationException if validation fails
	 */
	public void validate(InputStream inputStream, String filename, String contentType, long sizeBytes) {
		if (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".mp4")) {
			throw new Mp4ValidationException(
					Mp4ValidationException.ErrorReason.INVALID_EXTENSION,
					"Invalid file extension. Only .mp4 is supported.");
		}

		if (!"video/mp4".equals(contentType)) {
			throw new Mp4ValidationException(
					Mp4ValidationException.ErrorReason.INVALID_CONTENT_TYPE,
					"Invalid content type. Only video/mp4 is supported.");
		}

		if (sizeBytes > maxSizeBytes) {
			throw new Mp4ValidationException(
					Mp4ValidationException.ErrorReason.SIZE_LIMIT_EXCEEDED,
					"File size limit exceeded.");
		}

		if (inputStream == null) {
			throw new Mp4ValidationException(
					Mp4ValidationException.ErrorReason.MISSING_OR_EMPTY,
					"File content stream is missing.");
		}

		boolean markSupported = inputStream.markSupported();
		if (markSupported) {
			inputStream.mark(8);
		}

		byte[] header;
		try {
			header = inputStream.readNBytes(8);
		} catch (IOException e) {
			throw new Mp4ValidationException(
					Mp4ValidationException.ErrorReason.INVALID_HEADER,
					"Failed to read file header.",
					e);
		} finally {
			if (markSupported) {
				try {
					inputStream.reset();
				} catch (IOException e) {
					// Ignore or log reset failure
				}
			}
		}

		if (header.length < 8) {
			throw new Mp4ValidationException(
					Mp4ValidationException.ErrorReason.INVALID_HEADER,
					"File is too short to be a valid MP4.");
		}

		// Assert ftyp signature in bytes 4-7
		if (header[4] != 0x66 || header[5] != 0x74 || header[6] != 0x79 || header[7] != 0x70) {
			throw new Mp4ValidationException(
					Mp4ValidationException.ErrorReason.INVALID_HEADER,
					"Invalid file signature. Expected 'ftyp' box.");
		}
	}
}
