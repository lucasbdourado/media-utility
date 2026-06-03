# Task Implementation Plan: Implement URL Validation

## Status

Status: Completed

Last updated: 2026-06-03

Plan file: `docs/task-plans/mvp-media-utility/015-implement-url-validation-plan.md`

Architecture decision notes file: `docs/architecture/task-decisions/mvp-media-utility/015-implement-url-validation-architecture-decisions.md`

## Task Reference

Task ID: MVP-MEDIA-015

Task file: `docs/tasks/mvp-media-utility/015-implement-url-validation.md`

Task status: Ready

Task group or feature: mvp-media-utility

## Planning Mode Requirement

Plan mode verified: Yes

Notes:

- This plan was created in plan mode.
- Implementation must not start during `plan-task`.
- A future implementation request must use this saved plan and any saved architecture decision notes as source context.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project planning | `docs/planning/project-planning.md` | Task Group: Media Download, Task: Add URL input validation | Confirmed by source document | Focuses on validating public URL presence, format, and unsupported schemes, with unit tests. |
| ADR-008 | `docs/adrs/008-define-public-rest-api-contract.md` | Decision, Consequences | Accepted | Confirms API endpoint mapping, request/response structures, and HTTP status mapping for URL download failures. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/api/OperationApiController.java` | `validatePublicHttpUrl(String)` method | Detected in codebase | Performs initial basic check of absolute URL and http/https scheme. |
| User Decision | Current planning session | URL Domain and host validation security level | Resolved by user | User confirmed the validator must resolve the hostnames to check their resolved IPs to prevent DNS rebinding SSRF. |

## Context Summary

The URL download operation endpoint needs to validate user-provided URLs before passing them to the background download adapter. Currently, the `OperationApiController` performs a minimal structural check (absolute URI, scheme is http or https). 

To ensure safety, security (specifically Server-Side Request Forgery - SSRF prevention), and separation of concerns, we need a dedicated `UrlDownloadValidator` component in the download domain boundary (`com.lucasdourado.mediautility.media.download`). This component will validate the URL and throw a specific domain exception (`UrlValidationException`) on failure. `OperationService` will call this validator during the creation of a download operation and map its exceptions to standard HTTP error responses.

## Task Goal

Create a dedicated `UrlDownloadValidator` component and corresponding exception, integrate it with `OperationService`, and ensure that validation failures are mapped to the correct REST API response shapes with HTTP status `400 Bad Request`.

## Confirmed Scope

- Create `UrlDownloadValidator` as a Spring `@Component` under package `com.lucasdourado.mediautility.media.download`.
- Implement validation in `UrlDownloadValidator`:
  - Enforce presence (not null or blank).
  - Syntactically validate URL as a valid absolute URI.
  - Enforce that the scheme is exactly `http` or `https` (case-insensitive).
  - Enforce that the host is present and is a valid hostname or IP address.
  - **SSRF Prevention**: Reject any host resolved or syntactically matching private IP address ranges (RFC 1918: `10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16`), loopback addresses (`127.0.0.1/8`, `localhost`, `::1`), link-local/APIPA (`169.254.0.0/16`), and multicast addresses. This must be checked syntactically on the input, and also by resolving the hostname via DNS (e.g., using `InetAddress.getAllByName(host)`) to prevent DNS rebinding SSRF.
- Create `UrlValidationException` under package `com.lucasdourado.mediautility.media.download`.
  - Include an enum `ErrorReason` containing: `MISSING_OR_EMPTY`, `INVALID_SYNTAX`, `INVALID_SCHEME`, `MISSING_HOST`, `SSRF_ATTEMPT`.
- Modify `OperationService` to inject `UrlDownloadValidator` and execute it in `createDownload(URI)`.
- Catch `UrlValidationException` in `OperationService` and map it to `ApiException(HttpStatus.BAD_REQUEST, PublicErrorResponse.validation(...))` using the field name `"url"`.
- Create unit tests for `UrlDownloadValidator` covering all validation rules (valid URLs, malformed URLs, unsupported schemes, localhost, private IP addresses, loopback addresses, etc.).
- Update `OperationServiceTest` to verify that `createDownload` correctly calls the validator, handles success, and maps the validation exception.

## Out of Scope

- Implementing the background download process with `yt-dlp` (belongs to Task 016).
- Creating/modifying the controller-level model `UrlDownloadRequest` or the API mapping itself (already done in ADR-008 / Task 008).

## Requirements Covered

| Requirement | Source | How This Task Covers It | Status |
| --- | --- | --- | --- |
| YouTube download operation | PRD (FR-002), Planning | Validates download URL input before processing downloads. | Confirmed |
| URL input validation | Planning (Task 15) | Creates a domain validator component that handles syntactic, scheme, and SSRF checks. | Confirmed |
| HTTP Status 400 Bad Request on invalid URL | ADR-008 | Maps validation failures to REST bad request responses with validation details. | Confirmed |

## Technical Specification Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| TBD | Not applicable | TBD | The `tech-spec.md` file is currently empty, but ADR-008 and `project-planning.md` provide full justification and design details for URL validation. |

Coverage assessment:

- Justifying Tech Spec section: N/A
- Tech Spec sections implemented by this task: N/A
- Gaps between task and Tech Spec: N/A
- Dependencies not specified by the Tech Spec: N/A

## Architecture and ADR Considerations

| Decision or ADR | Source | Impact on This Task | Status |
| --- | --- | --- | --- |
| ADR-008: Define REST Contract | `docs/adrs/008-define-public-rest-api-contract.md` | Establishes validation error message format and HTTP status mapping (400 Bad Request). | Confirmed |
| Domain Boundary separation | Project Architecture | Placing the domain validation inside `media.download` package instead of the API controller layer. | Confirmed |

ADR candidates or architecture decisions needed:

- None.

Architecture decision notes:

- Saved separately: Yes
- Path: `docs/architecture/task-decisions/mvp-media-utility/015-implement-url-validation-architecture-decisions.md`
- Notes file status: New

## Confirmed Decisions

- `UrlDownloadValidator` will be a Spring-managed bean (`@Component`).
- The validator will perform both syntactic check and DNS resolution to ensure resolved IPs do not point to loopback, private ranges, link-local, or multicast addresses.
- The thrown exception is `UrlValidationException` containing `ErrorReason` enum.
- `OperationService` maps `UrlValidationException` to `ApiException` resulting in a `400 Bad Request` with field validation shape.

## Pending Decisions

None. All task-relevant decisions have been answered or explicitly deferred out of scope by the user.

## Questions for the User

None. All task-relevant questions have been answered.

## Proposed Implementation Approach

1. **Exception definition**: Create `UrlValidationException` with an `ErrorReason` enum to classify validation errors.
2. **Validator creation**: Implement `UrlDownloadValidator` as a `@Component`. Use standard java networking APIs (`InetAddress`) to validate the host. Iterate over all resolved addresses and assert they are not:
   - `isLoopbackAddress()`
   - `isSiteLocalAddress()`
   - `isLinkLocalAddress()`
   - `isMulticastAddress()`
   Also check private address ranges explicitly if site-local check is not sufficient for some IPv4/IPv6 private ranges.
3. **Integration**: Modify `OperationService` to call `UrlDownloadValidator.validate(URI)` inside `createDownload(URI)`. Handle the exception and map it to `ApiException` mapping to validation field `"url"`.
4. **Validation update**: Ensure the controller's existing helper `validatePublicHttpUrl` delegator works seamlessly, or simplify it to let the controller do basic parsing and delegate deep validation to the service.

## Files and Areas Expected to Change

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasdourado/mediautility/media/download/UrlDownloadValidator.java` | Create | Planning | Domain validator logic component. |
| `src/main/java/com/lucasdourado/mediautility/media/download/UrlValidationException.java` | Create | Planning | Exception thrown by the validator. |
| `src/test/java/com/lucasdourado/mediautility/media/download/UrlDownloadValidatorTest.java` | Create | Planning | Tests checking valid/invalid URLs and SSRF. |
| `src/main/java/com/lucasdourado/mediautility/api/OperationService.java` | Modify | Planning | Inject and invoke the validator, map its exceptions. |
| `src/test/java/com/lucasdourado/mediautility/api/OperationServiceTest.java` | Modify | Planning | Verify validator invocation and mapping. |

## Step-by-Step Implementation Plan

1. **Create Exception**:
   - Define `com.lucasdourado.mediautility.media.download.UrlValidationException` extending `RuntimeException`.
   - Add `ErrorReason` enum matching scope reasons.
2. **Create Validator**:
   - Define `com.lucasdourado.mediautility.media.download.UrlDownloadValidator`.
   - Implement `validate(String urlString)` and `validate(URI uri)`.
   - Parse input to `URI`, ensure absolute status, scheme (`http` / `https`), and non-null host.
   - Resolve host addresses via `InetAddress.getAllByName(host)`.
   - Validate resolved IP addresses against loopback, private networks, site-local, link-local, multicast.
   - Guard against `UnknownHostException` by throwing `UrlValidationException(INVALID_SYNTAX, ...)`.
3. **Add Validator Unit Tests**:
   - Write tests in `UrlDownloadValidatorTest` verifying various inputs:
     - Valid public URLs (e.g. `https://www.youtube.com/watch?v=123`, `http://example.com`)
     - Malformed URLs (invalid syntax)
     - Non-HTTP/HTTPS schemes (e.g., `ftp://...`, `file://...`)
     - Missing host (e.g. `https:///path`)
     - Loopback hostnames and IPs (`localhost`, `127.0.0.1`, `::1`)
     - Private network IPs (`192.168.1.1`, `10.0.0.1`, `172.16.0.1`)
     - Link-local and multicast IPs
4. **Modify Service Layer**:
   - Inject `UrlDownloadValidator` into `OperationService`.
   - Call `mp4UploadValidator` inside `createConversion` and `urlDownloadValidator` inside `createDownload`.
   - Implement `mapValidationException(UrlValidationException)` returning `ApiException(HttpStatus.BAD_REQUEST, ...)`.
5. **Update Service Unit Tests**:
   - Add tests to `OperationServiceTest` checking validator interaction and exception translation.

## Validation Strategy

- Run `mvn clean compile` to ensure compilation.
- Run `mvn test` to run all unit/integration tests and verify no regressions.

## Tests to Add or Update

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| `UrlDownloadValidatorTest` | Unit | Verifies all URL validation rules and SSRF blocks. | New file. |
| `OperationServiceTest` | Unit | Verifies `createDownload` validation mapping and integration. | Updates existing file. |

## Acceptance Criteria

- [ ] `UrlDownloadValidator` class created and annotated with `@Component`.
- [ ] `UrlValidationException` class created with specific reasons.
- [ ] Validation correctly rejects empty, blank, or null URL inputs.
- [ ] Validation correctly rejects malformed, relative, or non-http/https URLs.
- [ ] SSRF validation correctly rejects private IP addresses, loopback addresses (`127.0.0.1`, `localhost`, `::1`), link-local IPs, and multicast addresses, both syntactically and via DNS resolution.
- [ ] `OperationService.createDownload` calls `UrlDownloadValidator.validate`.
- [ ] `UrlValidationException` thrown by the validator is mapped in `OperationService` to an `ApiException` representing a `400 Bad Request` validation error.
- [ ] Extensive test suite covers standard HTTP URLs (should pass) and invalid formats/SSRF attempts (should fail).
- [ ] All tests compile and pass successfully.

## Risks and Edge Cases

- **Slow/Blocked DNS Resolution**: Resolving DNS names can block the validation thread if network/DNS is slow, or fail in offline environments during testing.
  - *Mitigation*: Unit tests in `UrlDownloadValidatorTest` should use hostnames that are either syntactically resolvable (like `localhost` or direct IP literals) or use mocking if feasible, or handle resolution failures gracefully.
- **DNS Rebinding Bypass**: An attacker registers a domain resolving to a public IP on first request, but resolving to a local IP on subsequent requests.
  - *Mitigation*: The validator checks IP resolution, but the actual download engine (`yt-dlp`) must also enforce similar socket protections.

## Rollback or Recovery Notes

- If issues occur, changes in `OperationService` can be easily reverted by reverting to the Git baseline.

## Documentation Updates

- Update the task status in `docs/tasks/mvp-media-utility/015-implement-url-validation.md` from `Ready` to `In Progress` or `Completed` during implementation.

## Implementation Readiness Checklist

- [x] Task scope is clear.
- [x] Source documents were reviewed.
- [x] Tech Spec coverage was verified.
- [x] Architecture decisions were checked.
- [x] ADR candidates were confirmed, rejected, required-before-implementation, or explicitly deferred by the user.
- [x] Pending decisions are resolved or explicitly deferred out of this task by the user.
- [x] User questions were answered, or the plan is explicitly marked `Blocked`.
- [x] Expected files or areas are identified.
- [x] Acceptance criteria are defined.
- [x] Test strategy is defined.
- [x] Risks and edge cases are documented.

## Notes for the Implementing Agent

- Re-use the coding pattern of `Mp4UploadValidator` and `Mp4ValidationException`.
- Handle `UnknownHostException` from `InetAddress.getAllByName()` gracefully as a syntax validation failure (e.g. invalid host/unresolvable host).
