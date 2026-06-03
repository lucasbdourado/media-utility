# Task: Implement Result Download Endpoint

## Status

Status: Completed

Last updated: 2026-06-03

## Task ID

ID: MVP-MEDIA-018

Order: 018

Task file: `docs/tasks/mvp-media-utility/018-implement-result-download-endpoint.md`

## Source Documents

List every document or explicit user decision that justifies this task.

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project planning | `docs/planning/project-planning.md` | Task Group: Result Delivery and Temporary Files, Task: Implement result download handler | Confirmed by source document | Focuses on exposing result download handler and retrieving result file. |
| ADR-008 | `docs/adrs/008-define-public-rest-api-contract.md` | Decision, Consequences | Accepted | Confirms API endpoint GET /api/operations/{operationId}/result, direct file stream return, status mapping (200, 404, 409). |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/api/OperationApiController.java` | API controller mapping | Detected in codebase | Exposes `/api/operations/{operationId}/result` and delegates to `OperationApiPort.getResult`. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/api/OperationService.java` | Method stub | Detected in codebase | Declares getResult which throws UnsupportedOperationException. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/storage/TemporaryStorageService.java` | Interface contract | Detected in codebase | Manages temporary result files and resolves paths. |

## Context

The direct streaming download endpoint `/api/operations/{operationId}/result` is already mapped in `OperationApiController`. It delegates to the `OperationApiPort` interface method `getResult(Long)`.
Currently, `OperationService.getResult` throws `UnsupportedOperationException`. We need to implement it to fetch the operation metadata, perform validation checks (status, expiration, file presence), retrieve the file path from local temporary storage, and return a `ResultDownload` representation containing a Spring `Resource`.

## Goal

Implement the `getResult(Long operationId)` method in `OperationService` to enable downloading completed media results directly, validating expiration and file availability, and returning a resource record, verified with unit tests in `OperationServiceTest.java`.

## Scope

- Implement `getResult(Long operationId)` in `com.lucasdourado.mediautility.api.OperationService`:
  1. Retrieve the operation from `OperationRepository`. If not present, throw `ApiException(HttpStatus.NOT_FOUND)` with code `NOT_FOUND` and message "Operation not found: <operationId>".
  2. Verify operation status. If it is not `COMPLETED`, throw `ApiException(HttpStatus.CONFLICT)` with code `CONFLICT` and message "Result file is not available because operation status is <status>".
  3. Verify result file metadata is present in the operation. If null, throw `ApiException(HttpStatus.NOT_FOUND)` with code `NOT_FOUND` and message "Operation has no result file metadata: <operationId>".
  4. Verify if the result is expired. If `expiresAt` is present and in the past relative to `clock.instant()`, throw `ApiException(HttpStatus.CONFLICT)` with code `CONFLICT` and message "Result file has expired.".
  5. Resolve the absolute filesystem path via `TemporaryStorageService.resolve(resultFile.getInternalPath())`.
  6. Check if the resolved file exists on disk. If not, throw `ApiException(HttpStatus.NOT_FOUND)` with code `NOT_FOUND` and message "Result file does not exist on disk: <fileName>".
  7. Map the file to a Spring `FileSystemResource`.
  8. Parse the contentType from string to Spring `MediaType`. Fallback to `MediaType.APPLICATION_OCTET_STREAM` on empty or invalid media types.
  9. Return a new `ResultDownload` record containing: the `FileSystemResource`, original `fileName`, `MediaType`, and `sizeBytes`.
- Write comprehensive unit tests in `OperationServiceTest.java` (using a nested class `GetResultTests` or equivalent) to verify:
  - Successful result download metadata mapping.
  - Operation not found (404).
  - Operation not completed (409).
  - Expired result (409).
  - Missing result metadata (404).
  - File missing on disk (404).
  - Content type fallback when media type is invalid.

## Out of Scope

- Implementing temporary file cleanup job execution (belongs to Task 019).
- Adding metrics tracking or logging operation event lifecycle (belongs to Task 020).

## Implementation Instructions

- Inject `Clock` and `TemporaryStorageService` in `OperationService`.
- Wrap the path in a `FileSystemResource` so the API controller can stream it directly.
- Safely catch `InvalidMediaTypeException` or `IllegalArgumentException` during media type parsing and fallback to `APPLICATION_OCTET_STREAM`.
- Do not expose any internal path to API responses.

## Expected Files

| Path | Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasdourado/mediautility/api/OperationService.java` | Modify | ADR-008, planning | Implement `getResult` mapping. |
| `src/test/java/com/lucasdourado/mediautility/api/OperationServiceTest.java` | Modify | Validation need | Unit tests verifying getResult success and error scenarios. |

## Dependencies

| Dependency | Type | Status | Notes |
| --- | --- | --- | --- |
| MVP-MEDIA-004 | Previous task | Completed | Operation domain metadata entity exists. |
| MVP-MEDIA-006 | Previous task | Completed | Temporary storage service exists. |
| MVP-MEDIA-014 | Previous task | Completed | Conversion endpoint exists. |
| MVP-MEDIA-017 | Previous task | Completed | URL download endpoint exists. |

## Validation

- Compile successfully: `.\mvnw clean compile`
- Run unit/integration tests: `.\mvnw test`
- Verifications to write in tests:
  - Retrieving a valid completed operation returns correct filename, size, parsed media type, and FileSystemResource.
  - Non-existent operation ID throws 404 ApiException.
  - Pending, Processing, or Failed operation status throws 409 ApiException.
  - Expired operations throw 409 ApiException.
  - Missing file on disk throws 404 ApiException.

## Acceptance Criteria

- [x] `OperationService.getResult` is fully implemented and mapped.
- [x] Returns `404 Not Found` when operation does not exist, has no result metadata, or the file is missing from the local storage root.
- [x] Returns `409 Conflict` when the operation is not completed or the file expiration timestamp is in the past.
- [x] Direct file response includes the correct file resource, contentType, size, and Content-Disposition headers.
- [x] Invalid content types degrade gracefully to `application/octet-stream`.
- [x] Unit tests cover all validation checks, exception mappings, and resource wrapping.

## Risks

- **Race conditions with cleanup**: A client might request a file exactly as the scheduler deletes it. Mitigation: Validate file exists on disk at request time and throw 404 if missing.

## Open Questions

None.

## Notes for the Implementing Agent

- Ensure to mock `TemporaryStorageService.resolve()` to return a valid or mock filesystem path during unit tests.
