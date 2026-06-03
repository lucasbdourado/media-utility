package com.lucasdourado.mediautility.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.lucasdourado.mediautility.media.conversion.Mp4UploadValidator;
import com.lucasdourado.mediautility.media.conversion.Mp4ValidationException;
import com.lucasdourado.mediautility.media.download.UrlDownloadValidator;
import com.lucasdourado.mediautility.media.download.UrlValidationException;
import com.lucasdourado.mediautility.operations.Operation;
import com.lucasdourado.mediautility.operations.OperationStatus;
import com.lucasdourado.mediautility.operations.OperationType;
import com.lucasdourado.mediautility.persistence.OperationRepository;

/**
 * Implementation of the OperationApiPort boundary, coordinating MP4 to MP3 conversion
 * and retrieving operations status metadata.
 */
@Service
public class OperationService implements OperationApiPort {

	private static final Logger log = LoggerFactory.getLogger(OperationService.class);

	private final OperationRepository operationRepository;
	private final Mp4UploadValidator mp4UploadValidator;
	private final UrlDownloadValidator urlDownloadValidator;
	private final BackgroundConversionExecutor backgroundConversionExecutor;
	private final BackgroundDownloadExecutor backgroundDownloadExecutor;
	private final Clock clock;

	@Autowired
	public OperationService(
			OperationRepository operationRepository,
			Mp4UploadValidator mp4UploadValidator,
			UrlDownloadValidator urlDownloadValidator,
			BackgroundConversionExecutor backgroundConversionExecutor,
			BackgroundDownloadExecutor backgroundDownloadExecutor) {
		this(operationRepository, mp4UploadValidator, urlDownloadValidator, backgroundConversionExecutor, backgroundDownloadExecutor, Clock.systemUTC());
	}

	OperationService(
			OperationRepository operationRepository,
			Mp4UploadValidator mp4UploadValidator,
			UrlDownloadValidator urlDownloadValidator,
			BackgroundConversionExecutor backgroundConversionExecutor,
			BackgroundDownloadExecutor backgroundDownloadExecutor,
			Clock clock) {
		this.operationRepository = operationRepository;
		this.mp4UploadValidator = mp4UploadValidator;
		this.urlDownloadValidator = urlDownloadValidator;
		this.backgroundConversionExecutor = backgroundConversionExecutor;
		this.backgroundDownloadExecutor = backgroundDownloadExecutor;
		this.clock = clock;
	}

	@Override
	public PublicOperationResponse createConversion(MultipartFile file) {
		try {
			mp4UploadValidator.validate(file);
		}
		catch (Mp4ValidationException ex) {
			throw mapValidationException(ex);
		}

		Instant createdAt = clock.instant();
		Operation operation = Operation.create(OperationType.CONVERSION, createdAt);
		operation = operationRepository.save(operation);

		Path tempSource = null;
		try {
			tempSource = Files.createTempFile("media-utility-upload-", ".mp4");
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, tempSource, StandardCopyOption.REPLACE_EXISTING);
			}
		}
		catch (IOException e) {
			if (tempSource != null) {
				try {
					Files.deleteIfExists(tempSource);
				}
				catch (IOException ex) {
					// Ignore
				}
			}
			throw new ApiException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					new PublicErrorResponse(PublicErrorCode.INTERNAL_ERROR, "Failed to store uploaded file: " + e.getMessage()));
		}

		backgroundConversionExecutor.executeConversion(
				operation.getId(),
				tempSource,
				file.getOriginalFilename());

		return PublicOperationResponse.pending(operation.getId(), OperationType.CONVERSION, createdAt);
	}

	@Override
	public PublicOperationResponse createDownload(URI url) {
		try {
			urlDownloadValidator.validate(url);
		}
		catch (UrlValidationException ex) {
			throw mapValidationException(ex);
		}

		Instant createdAt = clock.instant();
		Operation operation = Operation.create(OperationType.URL_DOWNLOAD, createdAt);
		operation = operationRepository.save(operation);

		backgroundDownloadExecutor.executeDownload(operation.getId(), url);

		return PublicOperationResponse.pending(operation.getId(), OperationType.URL_DOWNLOAD, createdAt);
	}

	@Override
	public PublicOperationResponse getOperation(Long operationId) {
		Operation operation = operationRepository.findById(operationId)
				.orElseThrow(() -> new ApiException(
						HttpStatus.NOT_FOUND,
						new PublicErrorResponse(PublicErrorCode.NOT_FOUND, "Operation not found: " + operationId)));

		PublicResultMetadata publicResult = null;
		if (operation.getResultFile() != null) {
			publicResult = new PublicResultMetadata(
					operation.getResultFile().getFileName(),
					operation.getResultFile().getContentType(),
					operation.getResultFile().getSizeBytes(),
					"/api/operations/" + operation.getId() + "/result");
		}

		PublicErrorResponse publicError = null;
		if (operation.getStatus() == OperationStatus.FAILED) {
			publicError = new PublicErrorResponse(
					PublicErrorCode.CONFLICT,
					operation.getFailureReason() != null ? operation.getFailureReason() : "Operation failed.");
		}

		return new PublicOperationResponse(
				operation.getId(),
				operation.getType(),
				operation.getStatus(),
				operation.getCreatedAt(),
				operation.getCompletedAt(),
				operation.getExpiresAt(),
				publicResult,
				publicError,
				new OperationLinks("/api/operations/" + operation.getId()));
	}

	@Override
	public ResultDownload getResult(Long operationId) {
		throw new UnsupportedOperationException("Retrieving conversion result is not supported yet.");
	}

	private ApiException mapValidationException(Mp4ValidationException ex) {
		switch (ex.getReason()) {
			case SIZE_LIMIT_EXCEEDED:
				return new ApiException(
						HttpStatus.PAYLOAD_TOO_LARGE,
						new PublicErrorResponse(PublicErrorCode.PAYLOAD_TOO_LARGE, ex.getMessage()));
			case INVALID_EXTENSION:
			case INVALID_CONTENT_TYPE:
				return new ApiException(
						HttpStatus.UNSUPPORTED_MEDIA_TYPE,
						new PublicErrorResponse(PublicErrorCode.UNSUPPORTED_MEDIA_TYPE, ex.getMessage()));
			case MISSING_OR_EMPTY:
			case INVALID_HEADER:
			default:
				return new ApiException(
						HttpStatus.BAD_REQUEST,
						PublicErrorResponse.validation("Request validation failed.", "file", ex.getMessage()));
		}
	}

	private ApiException mapValidationException(UrlValidationException ex) {
		return new ApiException(
				HttpStatus.BAD_REQUEST,
				PublicErrorResponse.validation("Request validation failed.", "url", ex.getMessage()));
	}
}

