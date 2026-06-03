package com.lucasdourado.mediautility.media.download;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import org.springframework.stereotype.Component;

/**
 * Validator component for download URLs to prevent invalid requests and SSRF.
 */
@Component
public class UrlDownloadValidator {

	/**
	 * Validates a URL string.
	 *
	 * @param urlString the URL string to validate
	 * @throws UrlValidationException if validation fails
	 */
	public void validate(String urlString) {
		if (urlString == null || urlString.isBlank()) {
			throw new UrlValidationException(UrlValidationException.ErrorReason.MISSING_OR_EMPTY, "URL is missing or empty.");
		}

		URI uri;
		try {
			uri = new URI(urlString);
		} catch (URISyntaxException e) {
			throw new UrlValidationException(UrlValidationException.ErrorReason.INVALID_SYNTAX, "URL must be an absolute http or https URL.", e);
		}

		validate(uri);
	}

	/**
	 * Validates a parsed URI.
	 *
	 * @param uri the URI to validate
	 * @throws UrlValidationException if validation fails
	 */
	public void validate(URI uri) {
		if (uri == null) {
			throw new UrlValidationException(UrlValidationException.ErrorReason.MISSING_OR_EMPTY, "URL is missing or empty.");
		}

		if (!uri.isAbsolute()) {
			throw new UrlValidationException(UrlValidationException.ErrorReason.INVALID_SYNTAX, "URL must be an absolute http or https URL.");
		}

		String scheme = uri.getScheme();
		if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
			throw new UrlValidationException(UrlValidationException.ErrorReason.INVALID_SCHEME, "URL must be an absolute http or https URL.");
		}

		String host = uri.getHost();
		if (host == null || host.isBlank()) {
			throw new UrlValidationException(UrlValidationException.ErrorReason.MISSING_HOST, "URL must have a valid host.");
		}

		validateHost(host);
	}

	private void validateHost(String host) {
		// Syntactic pre-check to avoid unnecessary DNS resolution for obvious local/private destinations
		if (isLocalOrPrivateSyntactically(host)) {
			throw new UrlValidationException(UrlValidationException.ErrorReason.SSRF_ATTEMPT, "URL is not allowed.");
		}

		// Perform DNS resolution checks to prevent DNS Rebinding SSRF
		try {
			InetAddress[] addresses = InetAddress.getAllByName(host);
			for (InetAddress address : addresses) {
				if (address.isLoopbackAddress()
						|| address.isSiteLocalAddress()
						|| address.isLinkLocalAddress()
						|| address.isMulticastAddress()
						|| address.isAnyLocalAddress()
						|| isPrivateAddress(address)) {
					throw new UrlValidationException(UrlValidationException.ErrorReason.SSRF_ATTEMPT, "URL is not allowed.");
				}
			}
		} catch (UnknownHostException e) {
			throw new UrlValidationException(UrlValidationException.ErrorReason.INVALID_SYNTAX, "URL host cannot be resolved.", e);
		}
	}

	private boolean isLocalOrPrivateSyntactically(String host) {
		String lowerHost = host.trim().toLowerCase();
		if ("localhost".equals(lowerHost) || "localhost.localdomain".equals(lowerHost)) {
			return true;
		}

		// IPv4 loopback, APIPA, or RFC 1918 private network syntactical prefixes
		if (lowerHost.startsWith("127.")) {
			return true;
		}
		if (lowerHost.startsWith("10.")) {
			return true;
		}
		if (lowerHost.startsWith("192.168.")) {
			return true;
		}
		if (lowerHost.startsWith("169.254.")) {
			return true;
		}
		if (lowerHost.startsWith("172.")) {
			String[] parts = lowerHost.split("\\.");
			if (parts.length >= 2) {
				try {
					int secondOctet = Integer.parseInt(parts[1]);
					if (secondOctet >= 16 && secondOctet <= 31) {
						return true;
					}
				} catch (NumberFormatException e) {
					// Not an IP address literal, ignore and let DNS check handle it
				}
			}
		}

		// IPv6 loopback literals
		if ("::1".equals(lowerHost) || "0:0:0:0:0:0:0:1".equals(lowerHost) || "[::1]".equals(lowerHost)) {
			return true;
		}

		return false;
	}

	private boolean isPrivateAddress(InetAddress address) {
		byte[] ip = address.getAddress();
		if (ip.length == 4) {
			int octet1 = ip[0] & 0xFF;
			int octet2 = ip[1] & 0xFF;
			
			// Double-check RFC 1918 and loopback/link-local boundaries
			if (octet1 == 10) {
				return true;
			}
			if (octet1 == 172 && (octet2 >= 16 && octet2 <= 31)) {
				return true;
			}
			if (octet1 == 192 && octet2 == 168) {
				return true;
			}
			if (octet1 == 169 && octet2 == 254) {
				return true;
			}
			if (octet1 == 127) {
				return true;
			}
		} else if (ip.length == 16) {
			// Unique Local Addresses (ULA): fc00::/7
			int firstByte = ip[0] & 0xFF;
			if ((firstByte & 0xFE) == 0xFC) {
				return true;
			}
		}
		return false;
	}
}
