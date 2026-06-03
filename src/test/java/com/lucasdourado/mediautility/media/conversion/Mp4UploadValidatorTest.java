package com.lucasdourado.mediautility.media.conversion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class Mp4UploadValidatorTest {

	private static final long MAX_SIZE_BYTES = 1024L; // 1KB for testing
	private Mp4UploadValidator validator;

	@BeforeEach
	void setUp() {
		validator = new Mp4UploadValidator(MAX_SIZE_BYTES);
	}

	@Test
	void validatesCorrectMp4MultipartFile() {
		byte[] validHeader = new byte[] {0, 0, 0, 12, 'f', 't', 'y', 'p', 'm', 'p', '4', '2'};
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"video.mp4",
				"video/mp4",
				validHeader);

		validator.validate(file); // Should pass without exception
	}

	@Test
	void validatesCorrectMp4Stream() {
		byte[] validHeader = new byte[] {0, 0, 0, 12, 'f', 't', 'y', 'p', 'm', 'p', '4', '2'};
		ByteArrayInputStream inputStream = new ByteArrayInputStream(validHeader);

		validator.validate(inputStream, "video.mp4", "video/mp4", validHeader.length);
	}

	@Test
	void rejectsNullOrEmptyMultipartFile() {
		assertThatThrownBy(() -> validator.validate((MockMultipartFile) null))
				.isInstanceOf(Mp4ValidationException.class)
				.hasMessageContaining("File is missing or empty")
				.extracting(ex -> ((Mp4ValidationException) ex).getReason())
				.isEqualTo(Mp4ValidationException.ErrorReason.MISSING_OR_EMPTY);

		MockMultipartFile emptyFile = new MockMultipartFile("file", "video.mp4", "video/mp4", new byte[0]);
		assertThatThrownBy(() -> validator.validate(emptyFile))
				.isInstanceOf(Mp4ValidationException.class)
				.hasMessageContaining("File is missing or empty")
				.extracting(ex -> ((Mp4ValidationException) ex).getReason())
				.isEqualTo(Mp4ValidationException.ErrorReason.MISSING_OR_EMPTY);
	}

	@Test
	void rejectsInvalidExtension() {
		byte[] validHeader = new byte[] {0, 0, 0, 12, 'f', 't', 'y', 'p', 'm', 'p', '4', '2'};
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"video.avi",
				"video/mp4",
				validHeader);

		assertThatThrownBy(() -> validator.validate(file))
				.isInstanceOf(Mp4ValidationException.class)
				.hasMessageContaining("Invalid file extension")
				.extracting(ex -> ((Mp4ValidationException) ex).getReason())
				.isEqualTo(Mp4ValidationException.ErrorReason.INVALID_EXTENSION);
	}

	@Test
	void rejectsNullFilename() {
		byte[] validHeader = new byte[] {0, 0, 0, 12, 'f', 't', 'y', 'p', 'm', 'p', '4', '2'};
		MockMultipartFile file = new MockMultipartFile(
				"file",
				null,
				"video/mp4",
				validHeader);

		assertThatThrownBy(() -> validator.validate(file))
				.isInstanceOf(Mp4ValidationException.class)
				.hasMessageContaining("Invalid file extension")
				.extracting(ex -> ((Mp4ValidationException) ex).getReason())
				.isEqualTo(Mp4ValidationException.ErrorReason.INVALID_EXTENSION);
	}

	@Test
	void rejectsInvalidContentType() {
		byte[] validHeader = new byte[] {0, 0, 0, 12, 'f', 't', 'y', 'p', 'm', 'p', '4', '2'};
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"video.mp4",
				"video/quicktime",
				validHeader);

		assertThatThrownBy(() -> validator.validate(file))
				.isInstanceOf(Mp4ValidationException.class)
				.hasMessageContaining("Invalid content type")
				.extracting(ex -> ((Mp4ValidationException) ex).getReason())
				.isEqualTo(Mp4ValidationException.ErrorReason.INVALID_CONTENT_TYPE);
	}

	@Test
	void rejectsExceededSizeLimit() {
		byte[] largePayload = new byte[(int) MAX_SIZE_BYTES + 1];
		// Fill in the first 8 bytes so signature check passes if size check were skipped
		largePayload[4] = 'f';
		largePayload[5] = 't';
		largePayload[6] = 'y';
		largePayload[7] = 'p';

		MockMultipartFile file = new MockMultipartFile(
				"file",
				"video.mp4",
				"video/mp4",
				largePayload);

		assertThatThrownBy(() -> validator.validate(file))
				.isInstanceOf(Mp4ValidationException.class)
				.hasMessageContaining("File size limit exceeded")
				.extracting(ex -> ((Mp4ValidationException) ex).getReason())
				.isEqualTo(Mp4ValidationException.ErrorReason.SIZE_LIMIT_EXCEEDED);
	}

	@Test
	void rejectsMissingInputStream() {
		assertThatThrownBy(() -> validator.validate(null, "video.mp4", "video/mp4", 100))
				.isInstanceOf(Mp4ValidationException.class)
				.hasMessageContaining("stream is missing")
				.extracting(ex -> ((Mp4ValidationException) ex).getReason())
				.isEqualTo(Mp4ValidationException.ErrorReason.MISSING_OR_EMPTY);
	}

	@Test
	void rejectsTooShortHeader() {
		byte[] shortHeader = new byte[] {0, 0, 0, 4, 'f'};
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"video.mp4",
				"video/mp4",
				shortHeader);

		assertThatThrownBy(() -> validator.validate(file))
				.isInstanceOf(Mp4ValidationException.class)
				.hasMessageContaining("too short")
				.extracting(ex -> ((Mp4ValidationException) ex).getReason())
				.isEqualTo(Mp4ValidationException.ErrorReason.INVALID_HEADER);
	}

	@Test
	void rejectsInvalidMagicBytesSignature() {
		byte[] invalidHeader = new byte[] {0, 0, 0, 12, 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"video.mp4",
				"video/mp4",
				invalidHeader);

		assertThatThrownBy(() -> validator.validate(file))
				.isInstanceOf(Mp4ValidationException.class)
				.hasMessageContaining("Invalid file signature")
				.extracting(ex -> ((Mp4ValidationException) ex).getReason())
				.isEqualTo(Mp4ValidationException.ErrorReason.INVALID_HEADER);
	}

	@Test
	void validatesStreamWithoutConsumingItWhenMarkIsSupported() throws IOException {
		byte[] validHeader = new byte[] {0, 0, 0, 12, 'f', 't', 'y', 'p', 'm', 'p', '4', '2'};
		ByteArrayInputStream inputStream = new ByteArrayInputStream(validHeader);

		validator.validate(inputStream, "video.mp4", "video/mp4", validHeader.length);

		// Assert that the stream was reset and we can read the original bytes from the start
		byte[] readBytes = inputStream.readAllBytes();
		assertThat(readBytes).isEqualTo(validHeader);
	}

	@Test
	void wrapsIoExceptionWhenReadingStreamFails() {
		InputStream throwingStream = new InputStream() {
			@Override
			public int read() throws IOException {
				throw new IOException("Simulated IO failure");
			}

			@Override
			public byte[] readNBytes(int len) throws IOException {
				throw new IOException("Simulated IO failure");
			}
		};

		assertThatThrownBy(() -> validator.validate(throwingStream, "video.mp4", "video/mp4", 100))
				.isInstanceOf(Mp4ValidationException.class)
				.hasMessageContaining("Failed to read file header")
				.hasCauseInstanceOf(IOException.class)
				.satisfies(ex -> {
					Mp4ValidationException validationEx = (Mp4ValidationException) ex;
					assertThat(validationEx.getReason()).isEqualTo(Mp4ValidationException.ErrorReason.INVALID_HEADER);
				});
	}
}
