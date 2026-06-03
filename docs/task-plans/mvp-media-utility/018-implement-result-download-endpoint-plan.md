# Task Implementation Plan: Implement Result Download Endpoint

## Status

Status: Ready for Implementation

Last updated: 2026-06-03

Plan file: `docs/task-plans/mvp-media-utility/018-implement-result-download-endpoint-plan.md`

Architecture decision notes file: Not generated

## Task Reference

Task ID: `MVP-MEDIA-018`

Task file: `docs/tasks/mvp-media-utility/018-implement-result-download-endpoint.md`

Task status: `Ready`

Task group or feature: `mvp-media-utility`

## Planning Mode Requirement

Plan mode verified: Yes

Notes:

- This plan must be created in plan mode.
- Implementation must not start during `plan-task`.
- A future implementation request must use this saved plan and any saved architecture decision notes as source context.

## Source Documents

List every document, ADR, codebase evidence item, or explicit user decision used to prepare this plan.

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project planning | `docs/planning/project-planning.md` | Task Group: Result Delivery and Temporary Files, Task: Implement result download handler | Confirmed by source document | Focuses on exposing result download handler and retrieving result file. |
| ADR-008 | `docs/adrs/008-define-public-rest-api-contract.md` | Decision, Consequences | Accepted | Confirms API endpoint GET /api/operations/{operationId}/result, direct file stream return, status mapping (200, 404, 409). |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/api/OperationApiController.java` | API controller mapping | Detected in codebase | Exposes `/api/operations/{operationId}/result` and delegates to `OperationApiPort.getResult`. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/api/OperationService.java` | Method stub | Detected in codebase | Declares getResult which throws UnsupportedOperationException. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/storage/TemporaryStorageService.java` | Interface contract | Detected in codebase | Manages temporary result files and resolves paths. |

## Context Summary

The direct streaming download endpoint `/api/operations/{operationId}/result` is already mapped in `OperationApiController`. It delegates to the `OperationApiPort` interface method `getResult(Long)`.
Currently, `OperationService.getResult` throws `UnsupportedOperationException`. We need to implement it to fetch the operation metadata, perform validation checks (status, expiration, file presence), retrieve the file path from local temporary storage, and return a `ResultDownload` representation containing a Spring `Resource`.

## Task Goal

Implement the `getResult(Long operationId)` method in `OperationService` to enable downloading completed media results directly, validating expiration and file availability, and returning a resource record, verified with unit tests in `OperationServiceTest.java`.

## Confirmed Scope

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
- Write comprehensive unit tests in `OperationServiceTest.java` (using a nested class `GetResultTests` or equivalent) to verify all success and error execution paths.

## Out of Scope

- Implementing temporary file cleanup job execution (belongs to Task 019).
- Adding metrics tracking or logging operation event lifecycle (belongs to Task 020).

## Requirements Covered

Map product, planning, or task requirements to this implementation plan.

| Requirement | Source | How This Task Covers It | Status |
| --- | --- | --- | --- |
| Immediate completed file download | PRD FR-005 | Exposes direct binary download endpoint for operation results | Confirmed |
| Safe REST Contract mapping | ADR-008 | Direct endpoint file streaming without leaking internal directories | Confirmed |

## Technical Specification Coverage

Explain how the Tech Spec covers this task.

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Modules and Responsibilities | Partial | Direct streaming from local temporary storage | Tech Spec document is empty, but ADR-008 and project-planning.md fully specify the details. |

Coverage assessment:

- Justifying Tech Spec section: N/A (relying on ADR-008)
- Tech Spec sections implemented by this task: N/A
- Gaps between task and Tech Spec: The `tech-spec.md` is empty, but ADR-008 provides the necessary technical contract definitions.
- Dependencies not specified by the Tech Spec: None.

## Architecture and ADR Considerations

List relevant architecture decisions, ADRs, technology definitions, and decision candidates.

| Decision or ADR | Source | Impact on This Task | Status |
| --- | --- | --- | --- |
| ADR-008 | `docs/adrs/008-define-public-rest-api-contract.md` | API Endpoint, response mapping, status mapping, error response mapping | Confirmed |
| ADR-005 | `docs/adrs/005-use-shared-operation-domain-model-for-metadata-persistence.md` | Operation model contains embedding of ResultFileMetadata | Confirmed |
| ADR-006 | `docs/adrs/006-use-root-relative-keys-for-temporary-local-storage.md` | Temporary local storage keys resolution | Confirmed |

ADR candidates or architecture decisions needed:

- None

Architecture decision notes:

- Saved separately: No
- Path: `Not generated`
- Notes file status: Not applicable

## Confirmed Decisions

List decisions that are already confirmed by source documents, codebase evidence, or explicit user confirmation.

- Target endpoint is `GET /api/operations/{operationId}/result`.
- Download is streamed directly to client without redirects.
- Standard Spring Boot `FileSystemResource` wrapped in `ResponseEntity<?>` is already used in `OperationApiController`.
- Expired results and unavailable results return `409 Conflict`.
- Missing results or missing database records return `404 Not Found`.

## Pending Decisions

None. All task-relevant decisions have been answered or explicitly deferred out of scope by the user.

## Questions for the User

None. All task-relevant questions have been answered.

## Proposed Implementation Approach

Describe how the future implementing agent should approach the task using only confirmed information.

1. Implement the `getResult` method in `OperationService` using the standard logic:
   - Query operation from `OperationRepository`.
   - Validate status, metadata, expiration, and filesystem existence of the file.
   - Parse media type with `MediaType.parseMediaType` falling back to `MediaType.APPLICATION_OCTET_STREAM` on exception.
   - Return a `ResultDownload` instance wrapping `FileSystemResource`.
2. Add a static inner/nested class in `OperationServiceTest.java` named `GetResultTests`.
3. Inside `GetResultTests`, write unit tests using Mockito mock definitions to simulate different database/filesystem scenarios:
   - Success path: valid completed operation returns correct resource, name, and parsed MediaType.
   - Missing operation path: throws 404 ApiException.
   - Incomplete operation path (status = PENDING/PROCESSING/FAILED): throws 409 ApiException.
   - Expired operation path: throws 409 ApiException.
   - Missing result file metadata: throws 404 ApiException.
   - Missing file on disk path (path resolved, but `Files.exists` returns false): throws 404 ApiException.
   - Fallback content type: invalid content type string parsed successfully as `application/octet-stream`.

## Files and Areas Expected to Change

List expected files, modules, packages, docs, tests, or areas. Use `TBD` when the exact path must be discovered during implementation.

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasdourado/mediautility/api/OperationService.java` | Modify | Planning | Implement the `getResult` method. |
| `src/test/java/com/lucasdourado/mediautility/api/OperationServiceTest.java` | Modify | Planning | Add unit tests for the `getResult` method. |

## Step-by-Step Implementation Plan

Give the future implementing agent a focused sequence of implementation steps.

1. **Service Implementation**:
   - Open [OperationService.java](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/src/main/java/com/lucasdourado/mediautility/api/OperationService.java).
   - In `getResult(Long operationId)`, query the repository.
   - Implement the verification steps (status is COMPLETED, resultFile not null, not expired, file exists on disk).
   - Retrieve file path using `temporaryStorageService.resolve(...)` and check `Files.exists(path)`.
   - Wrap in `FileSystemResource`.
   - Implement parsing logic with fallback.
   - Return a `ResultDownload` record.
2. **Unit Test Implementation**:
   - Open [OperationServiceTest.java](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/src/test/java/com/lucasdourado/mediautility/api/OperationServiceTest.java).
   - Define a nested class `@Nested class GetResultTests` at the bottom of the test class.
   - Inject/mock dependencies (TemporaryStorageService, OperationRepository, Clock).
   - Write tests mapping all success/error branches.
3. **Verify compilation and execution**:
   - Run `.\mvnw clean compile` and `.\mvnw test` to ensure code works properly and all 117+ tests pass.

## Validation Strategy

Define how the future implementation should be verified.

- Automated unit tests in `OperationServiceTest.java` covering all validations.
- Run `.\mvnw test` to run all unit tests in the project.

## Tests to Add or Update

List specific tests or test areas expected for this task.

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| `OperationServiceTest.GetResultTests` | Unit | Verifies all business validations, exception mappings, and resource wrapping for result download retrieval | Test classes to mock disk and DB files |

## Acceptance Criteria

List objective criteria that prove the task is complete.

- [ ] `OperationService.getResult` is fully implemented and mapped.
- [ ] Returns `404 Not Found` when operation does not exist, has no result metadata, or the file is missing from the local storage root.
- [ ] Returns `409 Conflict` when the operation is not completed or the file expiration timestamp is in the past.
- [ ] Direct file response includes the correct file resource, contentType, size, and Content-Disposition headers.
- [ ] Invalid content types degrade gracefully to `application/octet-stream`.
- [ ] Unit tests cover all validation checks, exception mappings, and resource wrapping.

## Risks and Edge Cases

List known risks, constraints, regression areas, and edge cases.

- **Conconcurrent Deletions**: If the file is cleaned up right after checking database status but before streaming begins, it could cause issues. Validation checks `Files.exists` immediately before resource wrapping to minimize this window.
- **Malformed Content Type**: DB record contains malformed mimetype (e.g. from raw metadata). Mitigation: Catch parsing exception and fallback to `application/octet-stream`.

## Rollback or Recovery Notes

Describe how the implementing agent should think about rollback, recovery, or safe reversal when relevant.

- Git rollback: Revert changes in `OperationService.java` and `OperationServiceTest.java`. No schema or configuration changes to revert.

## Documentation Updates

List documentation that should be created or updated by the future implementation, if any.

- Update task file `018-implement-result-download-endpoint.md` status to `Completed`.

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

- Mock `TemporaryStorageService.resolve()` to return a valid or mock filesystem path during unit tests. Use `@TempDir` if you want to test physical disk file checks.
