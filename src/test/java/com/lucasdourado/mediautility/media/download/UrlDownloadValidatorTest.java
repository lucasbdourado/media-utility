package com.lucasdourado.mediautility.media.download;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UrlDownloadValidatorTest {

	private UrlDownloadValidator validator;

	@BeforeEach
	void setUp() {
		validator = new UrlDownloadValidator();
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"http://8.8.8.8",
			"https://1.1.1.1",
			"https://104.244.42.1/status",
			"http://200.100.50.25:8080/path?query=val"
	})
	void validatesCorrectPublicIpUrls(String url) {
		validator.validate(url); // Should pass without exception
	}

	@Test
	void rejectsNullOrEmptyUrls() {
		assertThatThrownBy(() -> validator.validate((String) null))
				.isInstanceOf(UrlValidationException.class)
				.hasMessageContaining("missing or empty")
				.extracting(ex -> ((UrlValidationException) ex).getReason())
				.isEqualTo(UrlValidationException.ErrorReason.MISSING_OR_EMPTY);

		assertThatThrownBy(() -> validator.validate(""))
				.isInstanceOf(UrlValidationException.class)
				.hasMessageContaining("missing or empty")
				.extracting(ex -> ((UrlValidationException) ex).getReason())
				.isEqualTo(UrlValidationException.ErrorReason.MISSING_OR_EMPTY);

		assertThatThrownBy(() -> validator.validate("   "))
				.isInstanceOf(UrlValidationException.class)
				.hasMessageContaining("missing or empty")
				.extracting(ex -> ((UrlValidationException) ex).getReason())
				.isEqualTo(UrlValidationException.ErrorReason.MISSING_OR_EMPTY);

		assertThatThrownBy(() -> validator.validate((URI) null))
				.isInstanceOf(UrlValidationException.class)
				.hasMessageContaining("missing or empty")
				.extracting(ex -> ((UrlValidationException) ex).getReason())
				.isEqualTo(UrlValidationException.ErrorReason.MISSING_OR_EMPTY);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"not-a-url",
			"http//missing-colon.com",
			"http://",
			"https://:"
	})
	void rejectsMalformedUrls(String url) {
		assertThatThrownBy(() -> validator.validate(url))
				.isInstanceOf(UrlValidationException.class)
				.extracting(ex -> ((UrlValidationException) ex).getReason())
				.isIn(UrlValidationException.ErrorReason.INVALID_SYNTAX, UrlValidationException.ErrorReason.MISSING_HOST);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"ftp://example.com",
			"file:///etc/passwd",
			"mailto:someone@example.com",
			"gopher://example.com"
	})
	void rejectsUnsupportedSchemes(String url) {
		assertThatThrownBy(() -> validator.validate(url))
				.isInstanceOf(UrlValidationException.class)
				.hasMessageContaining("absolute http or https URL")
				.extracting(ex -> ((UrlValidationException) ex).getReason())
				.isEqualTo(UrlValidationException.ErrorReason.INVALID_SCHEME);
	}

	@Test
	void rejectsMissingHost() {
		assertThatThrownBy(() -> validator.validate("https:///path/to/file"))
				.isInstanceOf(UrlValidationException.class)
				.hasMessageContaining("valid host")
				.extracting(ex -> ((UrlValidationException) ex).getReason())
				.isEqualTo(UrlValidationException.ErrorReason.MISSING_HOST);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"http://localhost",
			"https://localhost/path",
			"http://localhost.localdomain",
			"http://127.0.0.1",
			"https://127.0.0.1:8443",
			"http://127.255.0.1",
			"http://[::1]",
			"http://[0:0:0:0:0:0:0:1]"
	})
	void rejectsLoopbackAddresses(String url) {
		assertThatThrownBy(() -> validator.validate(url))
				.isInstanceOf(UrlValidationException.class)
				.hasMessageContaining("not allowed")
				.extracting(ex -> ((UrlValidationException) ex).getReason())
				.isEqualTo(UrlValidationException.ErrorReason.SSRF_ATTEMPT);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"http://10.0.0.1",
			"https://10.255.255.254",
			"http://192.168.0.1",
			"http://192.168.100.5",
			"http://172.16.0.1",
			"http://172.31.255.255",
			"http://169.254.169.254",
			"http://0.0.0.0",
			"http://[fc00::1]",
			"http://[fdff::ffff]"
	})
	void rejectsPrivateAndLocalAddresses(String url) {
		assertThatThrownBy(() -> validator.validate(url))
				.isInstanceOf(UrlValidationException.class)
				.hasMessageContaining("not allowed")
				.extracting(ex -> ((UrlValidationException) ex).getReason())
				.isEqualTo(UrlValidationException.ErrorReason.SSRF_ATTEMPT);
	}
}
