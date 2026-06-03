package com.lucasdourado.mediautility.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.lucasdourado.mediautility.media.conversion.Mp4ToMp3Converter;
import com.lucasdourado.mediautility.media.conversion.Mp4UploadValidator;
import com.lucasdourado.mediautility.media.conversion.Mp4ValidationException;
import com.lucasdourado.mediautility.media.download.UrlDownloadValidator;
import com.lucasdourado.mediautility.media.download.UrlValidationException;
import com.lucasdourado.mediautility.operations.Operation;
import com.lucasdourado.mediautility.operations.OperationStatus;
import com.lucasdourado.mediautility.operations.OperationType;
import com.lucasdourado.mediautility.operations.ResultFileMetadata;
import com.lucasdourado.mediautility.persistence.OperationRepository;
import com.lucasdourado.mediautility.storage.TemporaryStorageService;
import java.net.URI;

class OperationServiceTest {

	private static final Instant NOW = Instant.parse("2026-06-03T12:00:00Z");
	private final Clock clock = Clock.fixed(NOW, ZoneId.of("UTC"));

	private OperationRepository operationRepository;
	private Mp4UploadValidator mp4UploadValidator;
	private UrlDownloadValidator urlDownloadValidator;
	private BackgroundConversionExecutor backgroundConversionExecutor;
	private OperationService operationService;

	@BeforeEach
	void setUp() {
		operationRepository = mock(OperationRepository.class);
		mp4UploadValidator = mock(Mp4UploadValidator.class);
		urlDownloadValidator = mock(UrlDownloadValidator.class);
		backgroundConversionExecutor = mock(BackgroundConversionExecutor.class);
		operationService = new OperationService(operationRepository, mp4UploadValidator, urlDownloadValidator, backgroundConversionExecutor, clock);
	}

	@Nested
	class CreateConversionTests {

		@Test
		void successfullyCreatesConversionAndTriggersAsync() throws IOException {
			MockMultipartFile file = new MockMultipartFile(
					"file", "video.mp4", "video/mp4", "fake mp4 content".getBytes(StandardCharsets.UTF_8));

			Operation savedOperation = mock(Operation.class);
			when(savedOperation.getId()).thenReturn(42L);
			when(operationRepository.save(any(Operation.class))).thenReturn(savedOperation);

			PublicOperationResponse response = operationService.createConversion(file);

			assertThat(response.operationId()).isEqualTo(42L);
			assertThat(response.type()).isEqualTo(OperationType.CONVERSION);
			assertThat(response.status()).isEqualTo(OperationStatus.PENDING);
			assertThat(response.createdAt()).isEqualTo(NOW);

			// Verify validator was called
			verify(mp4UploadValidator).validate(file);

			// Verify operation was saved in database
			ArgumentCaptor<Operation> operationCaptor = ArgumentCaptor.forClass(Operation.class);
			verify(operationRepository).save(operationCaptor.capture());
			Operation capturedOperation = operationCaptor.getValue();
			assertThat(capturedOperation.getType()).isEqualTo(OperationType.CONVERSION);
			assertThat(capturedOperation.getStatus()).isEqualTo(OperationStatus.PENDING);

			// Verify async conversion executor was triggered
			ArgumentCaptor<Path> pathCaptor = ArgumentCaptor.forClass(Path.class);
			verify(backgroundConversionExecutor).executeConversion(eq(42L), pathCaptor.capture(), eq("video.mp4"));

			Path tempSource = pathCaptor.getValue();
			assertThat(Files.exists(tempSource)).isTrue();
			assertThat(Files.readString(tempSource)).isEqualTo("fake mp4 content");

			// Clean up temp file created during test
			Files.deleteIfExists(tempSource);
		}

		@Test
		void throwsPayloadTooLargeWhenSizeLimitExceeded() {
			MockMultipartFile file = new MockMultipartFile("file", "large.mp4", "video/mp4", new byte[0]);
			doThrow(new Mp4ValidationException(Mp4ValidationException.ErrorReason.SIZE_LIMIT_EXCEEDED, "File too large"))
					.when(mp4UploadValidator).validate(file);

			assertThatThrownBy(() -> operationService.createConversion(file))
					.isInstanceOf(ApiException.class)
					.satisfies(ex -> {
						ApiException apiException = (ApiException) ex;
						assertThat(apiException.getStatus()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
						assertThat(apiException.getError().code()).isEqualTo(PublicErrorCode.PAYLOAD_TOO_LARGE);
						assertThat(apiException.getError().message()).isEqualTo("File too large");
					});

			verifyNoInteractions(operationRepository, backgroundConversionExecutor);
		}

		@Test
		void throwsUnsupportedMediaTypeWhenInvalidExtension() {
			MockMultipartFile file = new MockMultipartFile("file", "bad.mov", "video/quicktime", new byte[0]);
			doThrow(new Mp4ValidationException(Mp4ValidationException.ErrorReason.INVALID_EXTENSION, "Unsupported extension"))
					.when(mp4UploadValidator).validate(file);

			assertThatThrownBy(() -> operationService.createConversion(file))
					.isInstanceOf(ApiException.class)
					.satisfies(ex -> {
						ApiException apiException = (ApiException) ex;
						assertThat(apiException.getStatus()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
						assertThat(apiException.getError().code()).isEqualTo(PublicErrorCode.UNSUPPORTED_MEDIA_TYPE);
					});

			verifyNoInteractions(operationRepository, backgroundConversionExecutor);
		}

		@Test
		void throwsBadRequestValidationWhenHeaderIsInvalid() {
			MockMultipartFile file = new MockMultipartFile("file", "corrupt.mp4", "video/mp4", new byte[0]);
			doThrow(new Mp4ValidationException(Mp4ValidationException.ErrorReason.INVALID_HEADER, "Invalid signature"))
					.when(mp4UploadValidator).validate(file);

			assertThatThrownBy(() -> operationService.createConversion(file))
					.isInstanceOf(ApiException.class)
					.satisfies(ex -> {
						ApiException apiException = (ApiException) ex;
						assertThat(apiException.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
						assertThat(apiException.getError().code()).isEqualTo(PublicErrorCode.VALIDATION_ERROR);
						assertThat(apiException.getError().details()).hasSize(1);
						assertThat(apiException.getError().details().get(0).field()).isEqualTo("file");
						assertThat(apiException.getError().details().get(0).message()).isEqualTo("Invalid signature");
					});

			verifyNoInteractions(operationRepository, backgroundConversionExecutor);
		}
	}

	@Nested
	class CreateDownloadTests {

		@Test
		void callsValidatorAndThrowsUnsupportedOperationWhenValid() throws Exception {
			URI url = new URI("https://www.youtube.com/watch?v=dQw4w9WgXcQ");

			assertThatThrownBy(() -> operationService.createDownload(url))
					.isInstanceOf(UnsupportedOperationException.class)
					.hasMessageContaining("URL download operation is not supported yet");

			verify(urlDownloadValidator).validate(url);
		}

		@Test
		void throwsBadRequestValidationWhenUrlIsInvalid() throws Exception {
			URI url = new URI("http://localhost");
			doThrow(new UrlValidationException(UrlValidationException.ErrorReason.SSRF_ATTEMPT, "URL is not allowed."))
					.when(urlDownloadValidator).validate(url);

			assertThatThrownBy(() -> operationService.createDownload(url))
					.isInstanceOf(ApiException.class)
					.satisfies(ex -> {
						ApiException apiException = (ApiException) ex;
						assertThat(apiException.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
						assertThat(apiException.getError().code()).isEqualTo(PublicErrorCode.VALIDATION_ERROR);
						assertThat(apiException.getError().details()).hasSize(1);
						assertThat(apiException.getError().details().get(0).field()).isEqualTo("url");
						assertThat(apiException.getError().details().get(0).message()).isEqualTo("URL is not allowed.");
					});
		}
	}

	@Nested
	class GetOperationTests {

		@Test
		void returnsPendingOperation() {
			Operation operation = Operation.create(OperationType.CONVERSION, NOW);
			ReflectionTestUtils.setField(operation, "id", 1L);
			when(operationRepository.findById(1L)).thenReturn(Optional.of(operation));

			PublicOperationResponse response = operationService.getOperation(1L);

			assertThat(response.status()).isEqualTo(OperationStatus.PENDING);
			assertThat(response.result()).isNull();
			assertThat(response.error()).isNull();
		}

		@Test
		void returnsCompletedOperationWithResult() {
			Operation operation = Operation.create(OperationType.CONVERSION, NOW);
			ReflectionTestUtils.setField(operation, "id", 1L);
			ResultFileMetadata resultFile = new ResultFileMetadata("audio.mp3", "audio/mpeg", 1000L, "keys/audio.mp3");
			operation.complete(resultFile, NOW.plusSeconds(10), NOW.plusSeconds(3600));

			when(operationRepository.findById(1L)).thenReturn(Optional.of(operation));

			PublicOperationResponse response = operationService.getOperation(1L);

			assertThat(response.status()).isEqualTo(OperationStatus.COMPLETED);
			assertThat(response.completedAt()).isEqualTo(NOW.plusSeconds(10));
			assertThat(response.expiresAt()).isEqualTo(NOW.plusSeconds(3600));
			assertThat(response.result()).isNotNull();
			assertThat(response.result().fileName()).isEqualTo("audio.mp3");
			assertThat(response.result().contentType()).isEqualTo("audio/mpeg");
			assertThat(response.result().sizeBytes()).isEqualTo(1000L);
			assertThat(response.result().downloadUrl()).isEqualTo("/api/operations/1/result");
			assertThat(response.error()).isNull();
		}

		@Test
		void returnsFailedOperationWithError() {
			Operation operation = Operation.create(OperationType.CONVERSION, NOW);
			ReflectionTestUtils.setField(operation, "id", 1L);
			operation.fail("FFmpeg crash", NOW.plusSeconds(5));

			when(operationRepository.findById(1L)).thenReturn(Optional.of(operation));

			PublicOperationResponse response = operationService.getOperation(1L);

			assertThat(response.status()).isEqualTo(OperationStatus.FAILED);
			assertThat(response.completedAt()).isEqualTo(NOW.plusSeconds(5));
			assertThat(response.error()).isNotNull();
			assertThat(response.error().code()).isEqualTo(PublicErrorCode.CONFLICT);
			assertThat(response.error().message()).isEqualTo("FFmpeg crash");
			assertThat(response.result()).isNull();
		}

		@Test
		void throwsNotFoundWhenOperationDoesNotExist() {
			when(operationRepository.findById(99L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> operationService.getOperation(99L))
					.isInstanceOf(ApiException.class)
					.satisfies(ex -> {
						ApiException apiException = (ApiException) ex;
						assertThat(apiException.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
						assertThat(apiException.getError().code()).isEqualTo(PublicErrorCode.NOT_FOUND);
					});
		}
	}

	@Nested
	class BackgroundConversionExecutorTests {

		private Mp4ToMp3Converter mp4ToMp3Converter;
		private TemporaryStorageService temporaryStorageService;
		private BackgroundConversionExecutor executor;

		@BeforeEach
		void setUpExecutor() {
			mp4ToMp3Converter = mock(Mp4ToMp3Converter.class);
			temporaryStorageService = mock(TemporaryStorageService.class);
			executor = new BackgroundConversionExecutor(
					operationRepository,
					mp4ToMp3Converter,
					temporaryStorageService,
					Duration.ofHours(1));
		}

		@Test
		void executesSuccessfulConversion(@TempDir Path tempDir) throws IOException {
			Path sourcePath = tempDir.resolve("input.mp4");
			Files.writeString(sourcePath, "mp4 data");

			Operation operation = Operation.create(OperationType.CONVERSION, NOW);
			when(operationRepository.findById(1L)).thenReturn(Optional.of(operation));
			
			List<OperationStatus> savedStatuses = new ArrayList<>();
			when(operationRepository.save(any(Operation.class))).thenAnswer(invocation -> {
				Operation op = invocation.getArgument(0);
				savedStatuses.add(op.getStatus());
				return op;
			});

			ResultFileMetadata storedMetadata = new ResultFileMetadata("input.mp3", "audio/mpeg", 500L, "keys/input.mp3");
			when(temporaryStorageService.storeResult(eq("1"), eq("input.mp3"), eq("audio/mpeg"), any(InputStream.class)))
					.thenReturn(storedMetadata);

			executor.executeConversion(1L, sourcePath, "input.mp4");

			// Verify status transitions
			assertThat(savedStatuses).containsExactly(OperationStatus.PROCESSING, OperationStatus.COMPLETED);
			assertThat(operation.getStatus()).isEqualTo(OperationStatus.COMPLETED);
			assertThat(operation.getResultFile()).isEqualTo(storedMetadata);
			assertThat(operation.getExpiresAt()).isAfter(NOW);

			// Verify converter was called
			verify(mp4ToMp3Converter).convert(eq(sourcePath), any(Path.class));

			// Verify temp files cleaned up
			assertThat(Files.exists(sourcePath)).isFalse();
		}

		@Test
		void handlesConversionFailure(@TempDir Path tempDir) throws IOException {
			Path sourcePath = tempDir.resolve("input.mp4");
			Files.writeString(sourcePath, "mp4 data");

			Operation operation = Operation.create(OperationType.CONVERSION, NOW);
			when(operationRepository.findById(1L)).thenReturn(Optional.of(operation));

			List<OperationStatus> savedStatuses = new ArrayList<>();
			when(operationRepository.save(any(Operation.class))).thenAnswer(invocation -> {
				Operation op = invocation.getArgument(0);
				savedStatuses.add(op.getStatus());
				return op;
			});

			doThrow(new RuntimeException("FFmpeg failed"))
					.when(mp4ToMp3Converter).convert(eq(sourcePath), any(Path.class));

			executor.executeConversion(1L, sourcePath, "input.mp4");

			// Verify status transitions to FAILED
			assertThat(savedStatuses).containsExactly(OperationStatus.PROCESSING, OperationStatus.FAILED);
			assertThat(operation.getStatus()).isEqualTo(OperationStatus.FAILED);
			assertThat(operation.getFailureReason()).isEqualTo("FFmpeg failed");

			// Verify source file cleaned up
			assertThat(Files.exists(sourcePath)).isFalse();
		}
	}
}
