package com.lucasdourado.mediautility.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.lucasdourado.mediautility.operations.OperationStatus;
import com.lucasdourado.mediautility.operations.OperationType;

@WebMvcTest(OperationApiController.class)
@Import(CorsConfiguration.class)
class OperationApiControllerTest {

	private static final Instant CREATED_AT = Instant.parse("2026-06-02T12:00:00Z");
	private static final Instant COMPLETED_AT = Instant.parse("2026-06-02T12:03:00Z");
	private static final Instant EXPIRES_AT = Instant.parse("2026-06-02T13:03:00Z");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OperationApiPort operationApiPort;

	@Test
	void acceptsCorsPreflightFromAnyOrigin() throws Exception {
		mockMvc.perform(options("/api/operations/downloads")
						.header(HttpHeaders.ORIGIN, "https://any-origin.example")
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type"))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://any-origin.example"))
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("POST")))
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, containsString("content-type")));
	}

	@Test
	void createsConversionOperationFromMultipartFile() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "clip.mp4", "video/mp4", "video".getBytes(StandardCharsets.UTF_8));
		when(operationApiPort.createConversion(any())).thenReturn(pending(123L, OperationType.CONVERSION));

		mockMvc.perform(multipart("/api/operations/conversions").file(file))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/operations/123"))
				.andExpect(jsonPath("$.operationId").value(123))
				.andExpect(jsonPath("$.type").value("CONVERSION"))
				.andExpect(jsonPath("$.status").value("PENDING"))
				.andExpect(jsonPath("$.createdAt").value("2026-06-02T12:00:00Z"))
				.andExpect(jsonPath("$.completedAt").doesNotExist())
				.andExpect(jsonPath("$.expiresAt").doesNotExist())
				.andExpect(jsonPath("$.result").doesNotExist())
				.andExpect(jsonPath("$.error").doesNotExist())
				.andExpect(jsonPath("$.links.status").value("/api/operations/123"));

		verify(operationApiPort).createConversion(file);
	}

	@Test
	void rejectsMissingConversionFileAsValidationError() throws Exception {
		mockMvc.perform(multipart("/api/operations/conversions"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.message").value("Request validation failed."))
				.andExpect(jsonPath("$.details[0].field").value("file"))
				.andExpect(jsonPath("$.details[0].message").value("MP4 file is required."));
	}

	@Test
	void rejectsNonMp4ConversionFileAsUnsupportedMediaType() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "clip.mov", "video/quicktime", "video".getBytes(StandardCharsets.UTF_8));

		mockMvc.perform(multipart("/api/operations/conversions").file(file))
				.andExpect(status().isUnsupportedMediaType())
				.andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"));
	}

	@Test
	void createsDownloadOperationFromPublicHttpUrl() throws Exception {
		when(operationApiPort.createDownload(URI.create("https://example.com/video.mp4")))
				.thenReturn(pending(124L, OperationType.URL_DOWNLOAD));

		mockMvc.perform(post("/api/operations/downloads")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"url\":\"https://example.com/video.mp4\"}"))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/operations/124"))
				.andExpect(jsonPath("$.operationId").value(124))
				.andExpect(jsonPath("$.type").value("URL_DOWNLOAD"))
				.andExpect(jsonPath("$.status").value("PENDING"));

		verify(operationApiPort).createDownload(URI.create("https://example.com/video.mp4"));
	}

	@Test
	void rejectsBlankDownloadUrlAsValidationError() throws Exception {
		mockMvc.perform(post("/api/operations/downloads")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"url\":\"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.details[0].field").value("url"));
	}

	@Test
	void rejectsNonHttpDownloadUrlAsValidationError() throws Exception {
		mockMvc.perform(post("/api/operations/downloads")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"url\":\"ftp://example.com/video.mp4\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.details[0].field").value("url"))
				.andExpect(jsonPath("$.details[0].message").value("URL must be an absolute http or https URL."));
	}

	@Test
	void returnsCompletedOperationWithSafeResultMetadata() throws Exception {
		when(operationApiPort.getOperation(125L)).thenReturn(completed(125L));

		MvcResult result = mockMvc.perform(get("/api/operations/125"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.operationId").value(125))
				.andExpect(jsonPath("$.type").value("CONVERSION"))
				.andExpect(jsonPath("$.status").value("COMPLETED"))
				.andExpect(jsonPath("$.result.fileName").value("audio.mp3"))
				.andExpect(jsonPath("$.result.contentType").value("audio/mpeg"))
				.andExpect(jsonPath("$.result.sizeBytes").value(123456))
				.andExpect(jsonPath("$.result.downloadUrl").value("/api/operations/125/result"))
				.andExpect(jsonPath("$.result.internalPath").doesNotExist())
				.andReturn();

		String json = result.getResponse().getContentAsString();
		assertThat(json)
				.doesNotContain("internalPath")
				.doesNotContain("operations/operation-125")
				.doesNotContain("C:\\");
	}

	@Test
	void returnsFailedOperationWithPublicErrorShape() throws Exception {
		when(operationApiPort.getOperation(126L)).thenReturn(new PublicOperationResponse(
				126L,
				OperationType.URL_DOWNLOAD,
				OperationStatus.FAILED,
				CREATED_AT,
				COMPLETED_AT,
				null,
				null,
				new PublicErrorResponse(PublicErrorCode.CONFLICT, "Operation failed."),
				new OperationLinks("/api/operations/126")));

		mockMvc.perform(get("/api/operations/126"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("FAILED"))
				.andExpect(jsonPath("$.error.code").value("CONFLICT"))
				.andExpect(jsonPath("$.error.message").value("Operation failed."));
	}

	@Test
	void downloadsResultDirectlyWhenAvailable() throws Exception {
		when(operationApiPort.getResult(127L)).thenReturn(new OperationApiPort.ResultDownload(
				new ByteArrayResource("audio".getBytes(StandardCharsets.UTF_8)),
				"audio.mp3",
				MediaType.parseMediaType("audio/mpeg"),
				5L));

		mockMvc.perform(get("/api/operations/127/result"))
				.andExpect(status().isOk())
				.andExpect(content().contentType("audio/mpeg"))
				.andExpect(header().string("Content-Disposition", containsString("filename=\"audio.mp3\"")))
				.andExpect(content().bytes("audio".getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	void mapsNotFoundConflictPayloadTooLargeAndInternalErrorsToPublicShape() throws Exception {
		when(operationApiPort.getOperation(404L)).thenThrow(new ApiException(
				HttpStatus.NOT_FOUND,
				new PublicErrorResponse(PublicErrorCode.NOT_FOUND, "Operation was not found.")));
		when(operationApiPort.getResult(409L)).thenThrow(new ApiException(
				HttpStatus.CONFLICT,
				new PublicErrorResponse(PublicErrorCode.CONFLICT, "Result is not available.")));
		when(operationApiPort.getResult(413L)).thenThrow(new ApiException(
				HttpStatus.PAYLOAD_TOO_LARGE,
				new PublicErrorResponse(PublicErrorCode.PAYLOAD_TOO_LARGE, "Upload exceeds the configured limit.")));
		when(operationApiPort.getOperation(500L)).thenThrow(new RuntimeException("boom"));

		mockMvc.perform(get("/api/operations/404"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("NOT_FOUND"));
		mockMvc.perform(get("/api/operations/409/result"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.code").value("CONFLICT"));
		mockMvc.perform(get("/api/operations/413/result"))
				.andExpect(status().isPayloadTooLarge())
				.andExpect(jsonPath("$.code").value("PAYLOAD_TOO_LARGE"));
		mockMvc.perform(get("/api/operations/500"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
	}

	private static PublicOperationResponse pending(Long operationId, OperationType type) {
		return PublicOperationResponse.pending(operationId, type, CREATED_AT);
	}

	private static PublicOperationResponse completed(Long operationId) {
		return new PublicOperationResponse(
				operationId,
				OperationType.CONVERSION,
				OperationStatus.COMPLETED,
				CREATED_AT,
				COMPLETED_AT,
				EXPIRES_AT,
				new PublicResultMetadata("audio.mp3", "audio/mpeg", 123456L, "/api/operations/" + operationId + "/result"),
				null,
				new OperationLinks("/api/operations/" + operationId));
	}
}
