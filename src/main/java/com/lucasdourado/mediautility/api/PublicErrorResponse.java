package com.lucasdourado.mediautility.api;

import java.util.List;

public record PublicErrorResponse(PublicErrorCode code, String message, List<PublicErrorDetail> details) {

	public PublicErrorResponse(PublicErrorCode code, String message) {
		this(code, message, List.of());
	}

	public static PublicErrorResponse validation(String message, String field, String detailMessage) {
		return new PublicErrorResponse(
				PublicErrorCode.VALIDATION_ERROR,
				message,
				List.of(new PublicErrorDetail(field, detailMessage)));
	}
}
