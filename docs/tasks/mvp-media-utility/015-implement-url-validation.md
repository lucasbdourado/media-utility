# Task: Implement URL Validation

## Status

Status: Completed

Last updated: 2026-06-03

## Task ID

ID: MVP-MEDIA-015

Order: 015

Task file: `docs/tasks/mvp-media-utility/015-implement-url-validation.md`

## Source Documents

List every document or explicit user decision that justifies this task.

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project planning | `docs/planning/project-planning.md` | Task Group: Media Download, Task: Add URL input validation | Confirmed by source document | Focuses on validating public URL presence, format, and unsupported schemes, with unit tests. |
| ADR-008 | `docs/adrs/008-define-public-rest-api-contract.md` | Decision, Consequences | Accepted | Confirms API endpoint mapping, request/response structures, and HTTP status mapping for URL download failures. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/api/OperationApiController.java` | `validatePublicHttpUrl(String)` method | Detected in codebase | Performs initial basic check of absolute URL and http/https scheme. |

## Context

The URL download operation endpoint needs to validate user-provided URLs before passing them to the background download adapter. Currently, the `OperationApiController` performs a minimal structural check (absolute URI, scheme is http or https). 

To ensure safety, security (specifically Server-Side Request Forgery - SSRF prevention), and separation of concerns, we need a dedicated `UrlDownloadValidator` component in the download domain boundary (`com.lucasdourado.mediautility.media.download`). This component will validate the URL and throw a specific domain exception (`UrlValidationException`) on failure. `OperationService` will call this validator during the creation of a download operation and map its exceptions to standard HTTP error responses.

## Goal

Create a dedicated `UrlDownloadValidator` component and corresponding exception, integrate it with `OperationService`, and ensure that validation failures are mapped to the correct REST API response shapes with HTTP status `400 Bad Request`.

## Scope

- Create `UrlDownloadValidator` as a Spring `@Component` under package `com.lucasdourado.mediautility.media.download`.
- Implement validation in `UrlDownloadValidator`:
  - Enforce presence (not null or blank).
  - Syntactically validate URL as a valid absolute URI.
  - Enforce that the scheme is exactly `http` or `https` (case-insensitive).
  - Enforce that the host is present and is a valid hostname or IP address.
  - **SSRF Prevention**: Reject any host resolved or syntactically matching private IP address ranges (RFC 1918: `10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16`), loopback addresses (`127.0.0.1/8`, `localhost`, `::1`), link-local/APIPA (`169.254.0.0/16`), and multicast addresses.
- Create `UrlValidationException` under package `com.lucasdourado.mediautility.media.download`.
  - Include an enum `ErrorReason` containing: `MISSING_OR_EMPTY`, `INVALID_SYNTAX`, `INVALID_SCHEME`, `MISSING_HOST`, `SSRF_ATTEMPT`.
- Modify `OperationService` to inject `UrlDownloadValidator` and execute it in `createDownload(URI)`.
- Catch `UrlValidationException` in `OperationService` and map it to `ApiException(HttpStatus.BAD_REQUEST, PublicErrorResponse.validation(...))` using the field name `"url"`.
- Create unit tests for `UrlDownloadValidator` covering all validation rules (valid URLs, malformed URLs, unsupported schemes, localhost, private IP addresses, loopback addresses, etc.).
- Update `OperationServiceTest` to verify that `createDownload` correctly calls the validator, handles success, and maps the validation exception.

## Out of Scope

- Implementing the background download process with `yt-dlp` (belongs to Task 016).
- Creating/modifying the controller-level model `UrlDownloadRequest` or the API mapping itself (already done in ADR-008 / Task 008).

## Implementation Instructions

- Keep all validator logic inside the download domain package (`com.lucasdourado.mediautility.media.download`).
- When validating SSRF IP addresses, check both IPv4 and IPv6 formats. Use safe syntactic IP checks or `InetAddress` parsing without triggering remote DNS lookups where possible (to keep validation fast), or handle lookup failures gracefully if resolving hostnames.
- Standardize on `url` as the target field name in error details.
- Avoid exposing technical Java exception messages in the public response. Use descriptive messages (e.g. "URL must be an absolute http or https URL." or "URL is not allowed.").

## Expected Files

| Path | Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasdourado/mediautility/media/download/UrlDownloadValidator.java` | Create | planning | The URL validation logic component. |
| `src/main/java/com/lucasdourado/mediautility/media/download/UrlValidationException.java` | Create | planning | Domain exception indicating URL validation failure. |
| `src/test/java/com/lucasdourado/mediautility/media/download/UrlDownloadValidatorTest.java` | Create | validation | Unit tests for all URL validation edge cases. |
| `src/main/java/com/lucasdourado/mediautility/api/OperationService.java` | Modify | planning | Call the new validator and map its exceptions in `createDownload`. |
| `src/test/java/com/lucasdourado/mediautility/api/OperationServiceTest.java` | Modify | validation | Test validation integration in service layer. |

## Dependencies

| Dependency | Type | Status | Notes |
| --- | --- | --- | --- |
| MVP-MEDIA-008 | Previous task | Completed | REST API contracts and controller setup exist. |

## Validation

- Build the project and ensure it compiles successfully: `.\mvnw clean compile`
- Run the new unit tests for the validator: `.\mvnw test -Dtest=UrlDownloadValidatorTest`
- Run the modified service tests: `.\mvnw test -Dtest=OperationServiceTest`
- Run the full test suite to verify no regressions: `.\mvnw test`

## Acceptance Criteria

- [x] `UrlDownloadValidator` class created and annotated with `@Component`.
- [x] `UrlValidationException` class created with specific reasons.
- [x] Validation correctly rejects empty, blank, or null URL inputs.
- [x] Validation correctly rejects malformed, relative, or non-http/https URLs.
- [x] SSRF validation correctly rejects private IP addresses, loopback addresses (`127.0.0.1`, `localhost`, `::1`), link-local IPs, and multicast addresses.
- [x] `OperationService.createDownload` calls `UrlDownloadValidator.validate`.
- [x] `UrlValidationException` thrown by the validator is mapped in `OperationService` to an `ApiException` representing a `400 Bad Request` validation error.
- [x] Extensive test suite covers standard HTTP URLs (should pass) and invalid formats/SSRF attempts (should fail).
- [x] All tests compile and pass successfully.

## Risks

- SSRF Bypass: Malicious hostnames that resolve to local/private IP addresses (DNS rebinding or private DNS). During validation, the hostname could resolve to a public IP, but during download it could resolve to a local IP. 
  - *Mitigation*: The validator should do basic syntactic validation. The download engine should also enforce host restrictions, or DNS resolution should be pinned/checked if possible. For MVP, syntactic host checking and basic IP checks are required, and note the DNS rebinding risk in the design.
- Network Dependencies: If DNS resolution is used to validate hostnames, it might cause slow validation or fail if the service runs offline.

## Open Questions

- Should we resolve hostnames to IPs during validation to prevent SSRF DNS rebinding, or is a purely syntactic hostname/IP check sufficient for MVP?
  - *Recommendation*: A syntactic check of the input hostname/IP is standard for the first level. Deeper resolution checks can be implemented or handled at the HTTP client/download library level.

## Notes for the Implementing Agent

- Look at `Mp4UploadValidator` and `Mp4ValidationException` for inspiration on exception formatting, error mapping, and testing patterns.
- Make sure that `UrlValidationException` contains the `ErrorReason` for fine-grained logging and debugging.
