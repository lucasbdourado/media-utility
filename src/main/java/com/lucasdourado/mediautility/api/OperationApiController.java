package com.lucasdourado.mediautility.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import jakarta.validation.Valid;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/operations")
class OperationApiController {

	private final ObjectProvider<OperationApiPort> operationApiPort;

	OperationApiController(ObjectProvider<OperationApiPort> operationApiPort) {
		this.operationApiPort = operationApiPort;
	}

	@PostMapping(path = "/conversions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	ResponseEntity<PublicOperationResponse> createConversion(@RequestPart(value = "file", required = false) MultipartFile file) {
		validateMp4File(file);
		PublicOperationResponse response = port().createConversion(file);
		return ResponseEntity.created(URI.create(response.links().status())).body(response);
	}

	@PostMapping(path = "/downloads", consumes = MediaType.APPLICATION_JSON_VALUE)
	ResponseEntity<PublicOperationResponse> createDownload(@Valid @RequestBody UrlDownloadRequest request) {
		URI url = validatePublicHttpUrl(request.url());
		PublicOperationResponse response = port().createDownload(url);
		return ResponseEntity.created(URI.create(response.links().status())).body(response);
	}

	@GetMapping("/{operationId}")
	PublicOperationResponse getOperation(@PathVariable Long operationId) {
		return port().getOperation(operationId);
	}

	@GetMapping("/{operationId}/result")
	ResponseEntity<?> getResult(@PathVariable Long operationId) {
		OperationApiPort.ResultDownload result = port().getResult(operationId);
		return ResponseEntity.ok()
				.contentType(result.contentType())
				.contentLength(result.contentLength())
				.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
						.filename(result.fileName())
						.build()
						.toString())
				.body(result.resource());
	}

	private OperationApiPort port() {
		OperationApiPort port = operationApiPort.getIfAvailable();
		if (port == null) {
			throw new ApiException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					new PublicErrorResponse(PublicErrorCode.INTERNAL_ERROR, "Operation service is not available."));
		}
		return port;
	}

	private void validateMp4File(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new ApiException(
					HttpStatus.BAD_REQUEST,
					PublicErrorResponse.validation("Request validation failed.", "file", "MP4 file is required."));
		}

		String filename = file.getOriginalFilename();
		String contentType = file.getContentType();
		if (!"video/mp4".equals(contentType) || filename == null
				|| !filename.toLowerCase(Locale.ROOT).endsWith(".mp4")) {
			throw new ApiException(
					HttpStatus.UNSUPPORTED_MEDIA_TYPE,
					new PublicErrorResponse(
							PublicErrorCode.UNSUPPORTED_MEDIA_TYPE,
							"Only MP4 files with content type video/mp4 are supported."));
		}
	}

	private URI validatePublicHttpUrl(String url) {
		try {
			URI uri = new URI(url);
			String scheme = uri.getScheme();
			if (!uri.isAbsolute() || uri.getHost() == null
					|| (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
				throw invalidUrl();
			}
			return uri;
		}
		catch (URISyntaxException ex) {
			throw invalidUrl();
		}
	}

	private ApiException invalidUrl() {
		return new ApiException(
				HttpStatus.BAD_REQUEST,
				PublicErrorResponse.validation(
						"Request validation failed.",
						"url",
						"URL must be an absolute http or https URL."));
	}
}
