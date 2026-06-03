package com.lucasdourado.mediautility.api;

import jakarta.validation.constraints.NotBlank;

public record UrlDownloadRequest(@NotBlank(message = "URL is required.") String url) {
}
