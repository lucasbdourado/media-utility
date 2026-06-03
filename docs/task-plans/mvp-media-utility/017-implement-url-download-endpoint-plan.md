# Task Implementation Plan: Implement URL Download Endpoint

## Status

Status: Ready for Implementation

Last updated: 2026-06-03

Plan file: `docs/task-plans/mvp-media-utility/017-implement-url-download-endpoint-plan.md`

Architecture decision notes file: Not generated

## Task Reference

Task ID: `MVP-MEDIA-017`

Task file: `docs/tasks/mvp-media-utility/017-implement-url-download-endpoint.md`

Task status: Ready

Task group or feature: mvp-media-utility

## Planning Mode Requirement

Plan mode verified: Yes

Notes:
- This plan was created in plan mode.
- Implementation must not start during the execution of `plan-task`.
- A future implementation request must use this saved plan as source context.

## Source Documents

List every document, ADR, codebase evidence item, or explicit user decision used to prepare this plan.

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project planning | `docs/planning/project-planning.md` | Task Group: Media Download, Task: Add download endpoint or handler | Confirmed by source document | Focuses on exposing URL download operation. |
| ADR-008 | `docs/adrs/008-define-public-rest-api-contract.md` | Decision, Consequences | Accepted | Confirms API endpoints, request JSON format, and HTTP status mappings. |
| ADR-005 | `docs/adrs/005-use-shared-operation-domain-model-for-metadata-persistence.md` | Decision, Consequences | Accepted | Prohibits exposing raw internal filesystem paths in the REST API. |
| ADR-006 | `docs/adrs/006-use-root-relative-keys-for-temporary-local-storage.md` | Decision, Consequences | Accepted | Requires root-relative keys for temporary storage. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/api/OperationApiController.java` | API controller mapping | Detected in codebase | Exposes `/api/operations/downloads` endpoint. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/api/OperationApiPort.java` | API Port interface | Detected in codebase | Declares `createDownload(URI)`. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/media/download/UrlDownloadValidator.java` | Entire class | Detected in codebase | Performs URL validation. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/media/download/YtDlpUrlDownloader.java` | Entire class | Detected in codebase | Downloader adapter implementing `UrlDownloader`. |
| User Decision | Explicit user decision during planning | Filename Extraction Strategy | Confirmed by user | Agreed to extract the filename from the URL path, sanitize it, ensure it ends with `.mp4`, and fallback to `download.mp4`. |

## Context Summary

The URL download operation endpoint exposes the public URL download flow to the web client. The REST controller `OperationApiController` is already set up and defines the `POST /api/operations/downloads` route which delegates to the `OperationApiPort.createDownload(URI)` interface method. The URL input validator (`UrlDownloadValidator`) and yt-dlp download adapter (`YtDlpUrlDownloader`) are fully implemented and verified.

This task implements the orchestrator service method `OperationService.createDownload(URI)` and a background task runner component `BackgroundDownloadExecutor` to handle asynchronously downloading media files using `YtDlpUrlDownloader` in a background thread, storing results via `TemporaryStorageService`, and persisting status transitions using `OperationRepository`.

## Task Goal

Implement a Spring-managed background executor `BackgroundDownloadExecutor` and orchestrate the URL download flow in `OperationService.createDownload(URI)` by persisting a `PENDING` operation, triggering the async task, and returning the pending metadata.

## Confirmed Scope

- Create a Spring-managed `BackgroundDownloadExecutor` bean.
- Implement an `@Async` method `executeDownload(Long operationId, URI url)` in `BackgroundDownloadExecutor`:
  - Find the operation entity in `OperationRepository`.
  - Mark status as `PROCESSING` and save.
  - Create a temporary local file (e.g. `Files.createTempFile("media-utility-download-", ".mp4")`).
  - Run the `UrlDownloader.download` adapter.
  - Extract and sanitize the target filename from the URL path. Ensure it ends with `.mp4`. Fallback to `download.mp4`.
  - Store the target file in `TemporaryStorageService` with content type `video/mp4`.
  - Mark operation as `COMPLETED` and persist completion/expiration timestamps.
  - Fail gracefully on exception: transition status to `FAILED` and record the error reason.
  - Clean up the temporary local file on disk using a `finally` block.
- Update `OperationService.createDownload(URI)`:
  - Perform URL validation using `urlDownloadValidator.validate(url)` (already present).
  - Save a new `Operation` record of type `URL_DOWNLOAD` and status `PENDING`.
  - Call the background downloader using `backgroundDownloadExecutor.executeDownload(operation.getId(), url)`.
  - Return a pending `PublicOperationResponse`.
- Write comprehensive unit tests in `OperationServiceTest.java` and `BackgroundDownloadExecutorTest.java`.

## Out of Scope

- Implementing the result file download stream handler (belongs to Task 018).
- Implementing temporary file cleanup scheduling job (belongs to Task 019).
- Adding metrics tracking or logging operation event lifecycle (belongs to Task 020).

## Requirements Covered

| Requirement | Source | How This Task Covers It | Status |
| --- | --- | --- | --- |
| YouTube download operation (FR-002) | `docs/product/prd.md` | Orchestrates URL download asynchronously using `YtDlpUrlDownloader`. | Confirmed |
| Immediate completed file download (FR-005) | `docs/product/prd.md` | Saves result file to temporary storage and completes the metadata so it is ready for download. | Confirmed |

## Technical Specification Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| N/A | Partial | `OperationService` URL Download Flow | `tech-spec.md` is empty, but implementation is fully justified by the planning tasks and existing codebase conversion flow structure. |

Coverage assessment:
- Justifying Tech Spec section: N/A
- Tech Spec sections implemented by this task: N/A (Flow implementation)
- Gaps between task and Tech Spec: Gaps resolved by matching the existing conversion architecture.
- Dependencies not specified by the Tech Spec: None.

## Architecture and ADR Considerations

| Decision or ADR | Source | Impact on This Task | Status |
| --- | --- | --- | --- |
| ADR-008: Define REST API Contract | `docs/adrs/008-define-public-rest-api-contract.md` | Confirms POST `/api/operations/downloads` JSON contract and fields. | Confirmed |
| ADR-005: Use Shared Operation Domain Model | `docs/adrs/005-use-shared-operation-domain-model-for-metadata-persistence.md` | Dictates that metadata is persisted in `Operation` table. | Confirmed |
| ADR-006: Use Root-Relative Keys | `docs/adrs/006-use-root-relative-keys-for-temporary-local-storage.md` | Temporary storage files are saved with root-relative keys. | Confirmed |

ADR candidates or architecture decisions needed:
- None.

Architecture decision notes:
- Saved separately: No
- Path: N/A
- Notes file status: Not applicable

## Confirmed Decisions

- **Asynchronous Execution Pattern**: The background download process uses Spring's `@Async` annotation on a separate bean `BackgroundDownloadExecutor` to avoid self-invocation proxy limitations.
- **URL Filename Extraction**: Filename is extracted from the last path segment of the URL (e.g. `nature.mp4` from `https://example.com/nature.mp4`), sanitized to strip path traversals (`/` or `\`), and verified to end in `.mp4`. If the path is empty, invalid, or doesn't end with a filename, fallback to `download.mp4`.
- **Result Content Type**: Stored results are saved with content type `video/mp4`.

## Pending Decisions

None. All task-relevant decisions have been answered or explicitly deferred out of scope by the user.

## Questions for the User

None. All task-relevant questions have been answered.

## Proposed Implementation Approach

1. **Create BackgroundDownloadExecutor**:
   - Standard `@Component` bean in `com.lucasdourado.mediautility.api` package.
   - Inject `OperationRepository`, `UrlDownloader` (injects `YtDlpUrlDownloader`), `TemporaryStorageService`, and the retention duration from `@Value("${media-utility.storage.retention:1h}")`.
   - Implement `executeDownload(Long operationId, URI url)` annotated with `@Async` and `@Transactional` (if database writes require active transactions).
2. **Implement Filename Extraction**:
   - In `BackgroundDownloadExecutor`, write a helper method `getFilenameFromUrl(URI url)` to extract the last path segment.
   - Strip query parameters, decode any URL-encoded parts if needed, and sanitize out special characters or separators.
   - Check if it ends with `.mp4`. If not, strip the extension (if any) and append `.mp4`. Fallback to `download.mp4` on empty or invalid segments.
3. **Integrate into OperationService**:
   - Inject `BackgroundDownloadExecutor` in the constructor.
   - Update `createDownload(URI url)` to:
     - Keep the validation layer (`urlDownloadValidator.validate(url)`).
     - Instantiate a new `Operation` of type `URL_DOWNLOAD`.
     - Save the operation in the database.
     - Call `backgroundDownloadExecutor.executeDownload(...)` with the operation ID and URL.
     - Return `PublicOperationResponse.pending(id, type, createdAt)`.

## Files and Areas Expected to Change

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasdourado/mediautility/api/BackgroundDownloadExecutor.java` | Create | Task definition | Background async download orchestrator. |
| `src/main/java/com/lucasdourado/mediautility/api/OperationService.java` | Modify | Task definition | Inject executor and implement `createDownload`. |
| `src/test/java/com/lucasdourado/mediautility/api/OperationServiceTest.java` | Modify | Validation need | Update orchestrator unit tests to verify `createDownload` behaves correctly. |
| `src/test/java/com/lucasdourado/mediautility/api/BackgroundDownloadExecutorTest.java` | Create | Validation need | Write unit tests for background execution, file storing, transitions, and cleanup. |

## Step-by-Step Implementation Plan

1. **Step 1**: Create `BackgroundDownloadExecutor.java` in the api package. Introduce the dependencies, private helper method `getFilenameFromUrl(URI)`, and the `@Async` execution skeleton.
2. **Step 2**: Implement the core logic of `@Async executeDownload(...)` method. Include the steps to fetch, mark processing, create temp file, call downloader, store result in `TemporaryStorageService`, complete operation, fail operation on exceptions, and clean up temp files in `finally`.
3. **Step 3**: Modify `OperationService.java` to inject `BackgroundDownloadExecutor` and wire up the `createDownload(URI)` method to persist the operation and trigger the background download.
4. **Step 4**: Create `BackgroundDownloadExecutorTest.java` to unit test the background executor. Mock `OperationRepository`, `UrlDownloader`, and `TemporaryStorageService` to verify transitions, filename parsing, and file deletions on success and failure.
5. **Step 5**: Update `OperationServiceTest.java` to verify that `createDownload` validates the URL, saves a `PENDING` operation, triggers the async call, and returns the correct response.
6. **Step 6**: Run compile and tests: `.\mvnw clean test` to ensure there are no compilation errors or broken tests.

## Validation Strategy

- The build must compile successfully: `.\mvnw clean compile`.
- Run all unit tests to ensure no regressions: `.\mvnw test`.
- Verify the following in `BackgroundDownloadExecutorTest`:
  - A successful download saves the file in temporary storage and completes the operation with a calculated expiration time.
  - A failed download records the failure reason and sets status to `FAILED`.
  - The local temporary download target file is cleaned up in both success and failure states.
  - Parsing the filename correctly handles URLs with valid files, parameters, and paths without filenames (falling back to `download.mp4`).

## Tests to Add or Update

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| `OperationServiceTest.CreateDownloadTests` | Unit | Verify `createDownload` correctly validates, saves `PENDING`, and triggers the async background download. | Update existing test class. |
| `BackgroundDownloadExecutorTest` | Unit | Verify the async task transitions states, stores the downloaded media, sets correct metadata, and guarantees cleanup. | New test class. |

## Acceptance Criteria

- [ ] Class `BackgroundDownloadExecutor` is defined in package `com.lucasdourado.mediautility.api` and registered as a Spring bean.
- [ ] Method `executeDownload` is marked `@Async` and handles download orchestrations on a background thread.
- [ ] `OperationService.createDownload(URI)` executes URL validation, persists a `PENDING` operation of type `URL_DOWNLOAD`, triggers the async execution, and returns a pending response.
- [ ] Expiration date is calculated and configured via property `media-utility.storage.retention`.
- [ ] Stored result filenames are parsed from the URI path, guaranteed to end in `.mp4` (defaulting to `download.mp4`).
- [ ] Stored results are saved via `TemporaryStorageService.storeResult` with correct content type `video/mp4`.
- [ ] Failure transitions status to `FAILED` and stores the error message.
- [ ] Local temporary download files are guaranteed to be cleaned up from disk in both success and failure cases.
- [ ] Unit tests cover validation mapping, async thread submission, status transitions, storage saving, and temporary file cleanup.

## Risks and Edge Cases

- **Disk Space Leakage**: Heavy media files downloaded from public URLs could exhaust disk space if not deleted after processing. *Mitigation*: Ensure the local temporary target file is deleted in a `finally` block in `executeDownload` under all execution outcomes.
- **Unsafe filename extraction**: Extracting the filename directly from user-provided URLs might allow Directory Traversal attacks (e.g. `../../filename`). *Mitigation*: Use `URI.getPath()` and parse only the last segment by finding the last slash. Sanitize out path traversal characters.
- **Concurrency issues**: The database status could be updated by a cleanup scheduler or other threads during processing. *Mitigation*: Rely on Spring Jpa transaction manager and repository saves to handle state updates.

## Rollback or Recovery Notes

- If the implementation fails or requires rollback, restore `OperationService.java` to throw `UnsupportedOperationException`, revert the test changes, and remove `BackgroundDownloadExecutor.java`.

## Documentation Updates

- None required for this task.

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

- When unit testing the background async execution, use a synchronous task executor or capture arguments to verify the background run behavior synchronously without concurrency timing issues.
- Be careful with extracting path segments from URIs; decode potential spaces/characters safely.
