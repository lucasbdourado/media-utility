# Task: Implement URL Download Endpoint

## Status

Status: Completed

Last updated: 2026-06-03

## Task ID

ID: MVP-MEDIA-017

Order: 017

Task file: `docs/tasks/mvp-media-utility/017-implement-url-download-endpoint.md`

## Source Documents

List every document or explicit user decision that justifies this task.

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project planning | `docs/planning/project-planning.md` | Task Group: Media Download, Task: Add download endpoint or handler | Confirmed by source document | Focuses on exposing the URL download operation to the web flow, validating the URL, calling the download service, and returning temporary result metadata. |
| ADR-008 | `docs/adrs/008-define-public-rest-api-contract.md` | Decision, Consequences | Accepted | Confirms API endpoint mapping for downloads, response structure, validation, and error shape. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/api/OperationApiController.java` | API controller mapping | Detected in codebase | Exposes `/api/operations/downloads` and delegates to `OperationApiPort`. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/api/OperationApiPort.java` | API Port interface | Detected in codebase | Declares the boundary method `createDownload(URI)`. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/media/download/UrlDownloadValidator.java` | Entire class | Detected in codebase | Performs URL security and structural validations. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/media/download/YtDlpUrlDownloader.java` | Entire class | Detected in codebase | Downloads media from a public URL using the shared ProcessExecutor. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/storage/TemporaryStorageService.java` | Interface contract | Detected in codebase | Manages temporary result files. |

## Context

The URL download operation endpoint exposes the public URL download flow to the web client. The REST controller `OperationApiController` is already defined and delegates to the `OperationApiPort` interface. The URL input validator (`UrlDownloadValidator`) and yt-dlp download adapter (`YtDlpUrlDownloader`) are fully implemented and verified.

This task implements the `OperationApiPort` interface (specifically `createDownload(URI)`) and a new component `BackgroundDownloadExecutor` to orchestrate: validating the URL, creating/saving the pending `Operation` in the database, calling the download service in a background thread, storing the downloaded file in temporary storage, updating/completing the operation status, and cleaning up the local temporary downloaded file.

## Goal

Implement a Spring-managed background executor `BackgroundDownloadExecutor` and orchestrate the URL download flow in `OperationService.createDownload(URI)` by persisting a `PENDING` operation, triggering the async task, and returning the pending metadata.

## Scope

- Create a Spring Component `com.lucasdourado.mediautility.api.BackgroundDownloadExecutor`.
- Inject `OperationRepository`, `UrlDownloader`, `TemporaryStorageService`, and the configured retention duration (`media-utility.storage.retention`, defaulting to `1h`).
- Define an `@Async` method `executeDownload(Long operationId, URI url)` in `BackgroundDownloadExecutor`:
  1. Retrieve the operation and transition status to `PROCESSING`.
  2. Create a temporary local file for the download target (e.g. `Files.createTempFile("media-utility-download-", ".mp4")`).
  3. Run `UrlDownloader.download(url, tempTarget)`.
  4. Extract and sanitize a friendly target filename from the URL path, ensuring it ends with `.mp4` (fallback to `download.mp4` if empty or invalid).
  5. Save the output file to temporary storage via `TemporaryStorageService.storeResult(...)` with content type `video/mp4`.
  6. Complete the operation in the database with the result metadata and calculated expiration time (`completedAt` plus configured retention duration).
  7. On any background error/exception, catch it and transition the operation status to `FAILED` with the exception message.
  8. In a `finally` block, delete local temporary target file using `Files.deleteIfExists` to prevent disk leaks.
- Implement `createDownload(URI url)` in `OperationService`:
  - Run validation using `urlDownloadValidator.validate(url)` (already present).
  - Create and persist a new `Operation` record with type `URL_DOWNLOAD` and status `PENDING` to get a database ID.
  - Submit a task to `BackgroundDownloadExecutor.executeDownload(...)` to run asynchronously in the background.
  - Return the immediate pending `PublicOperationResponse`.
- Write comprehensive unit tests in `OperationServiceTest.java` and `BackgroundDownloadExecutorTest.java` (or inside `OperationServiceTest.java` as a nested class) using Mockito to verify the orchestrations, validations, exception mapping, async flow, and file cleanups.

## Out of Scope

- Implementing the direct result streaming download handler (belongs to Task 018).
- Implementing temporary file cleanup scheduling job (belongs to Task 019).
- Adding metrics tracking or logging operation event lifecycle (belongs to Task 020).

## Implementation Instructions

- Keep all new classes under package `com.lucasdourado.mediautility.api`.
- Ensure `BackgroundDownloadExecutor` is annotated with `@Component` for injection and separate proxying.
- Inject the retention duration via Spring properties: `@Value("${media-utility.storage.retention:1h}") Duration retentionDuration`.
- Ensure all temporary files created on disk during download are cleaned up under `finally` blocks.
- Inject `BackgroundDownloadExecutor` into `OperationService` and replace the current throw statement in `createDownload`.

## Expected Files

| Path | Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasdourado/mediautility/api/BackgroundDownloadExecutor.java` | Create | ADR-008, planning | Asynchronous download orchestrator. |
| `src/main/java/com/lucasdourado/mediautility/api/OperationService.java` | Modify | ADR-008, planning | Implement `createDownload` and inject `BackgroundDownloadExecutor`. |
| `src/test/java/com/lucasdourado/mediautility/api/OperationServiceTest.java` | Modify | Validation need | Update orchestrator unit tests and add validation tests. |
| `src/test/java/com/lucasdourado/mediautility/api/BackgroundDownloadExecutorTest.java` | Create | Validation need | Unit tests verifying background async execution, storage saving, failure state transition, and cleanup. |

## Dependencies

| Dependency | Type | Status | Notes |
| --- | --- | --- | --- |
| MVP-MEDIA-004 | Previous task | Completed | Operation domain metadata entity exists. |
| MVP-MEDIA-006 | Previous task | Completed | Temporary storage service exists. |
| MVP-MEDIA-015 | Previous task | Completed | URL validation component exists. |
| MVP-MEDIA-016 | Previous task | Completed | URL download adapter exists. |

## Validation

- Compile successfully: `.\mvnw clean compile`
- Run unit/integration tests: `.\mvnw test`
- Verifications to write in tests:
  - `createDownload` correctly creates a `PENDING` database record, triggers the async method, and returns pending details.
  - `BackgroundDownloadExecutor` correctly handles status transitions `PENDING` -> `PROCESSING` -> `COMPLETED`/`FAILED`.
  - Storing in temporary storage uses the correct content type (`video/mp4`) and a parsed/sanitized filename.
  - The local temporary download target file is strictly cleaned up on success and failure.

## Acceptance Criteria

- [x] Class `BackgroundDownloadExecutor` exists in package `com.lucasdourado.mediautility.api` and is registered as a Spring bean.
- [x] Method `executeDownload` is marked `@Async` and handles download orchestrations on a background thread.
- [x] `OperationService.createDownload(URI)` executes URL validation, persists a `PENDING` operation of type `URL_DOWNLOAD`, triggers the async execution, and returns a pending response.
- [x] Expiration date is calculated and configured via property `media-utility.storage.retention`.
- [x] Sanity check filenames: parsed from URI path, guaranteed to end in `.mp4` (defaulting to `download.mp4`).
- [x] Completed downloads are saved via `TemporaryStorageService.storeResult` with correct content type `video/mp4`.
- [x] Failure transitions status to `FAILED` and stores the error message.
- [x] Local temporary download files are guaranteed to be cleaned up from disk in both success and failure cases.
- [x] Unit tests cover validation mapping, async thread submission, status transitions, storage saving, and temporary file cleanup.

## Risks

- **Disk space leaks**: Failing to clean up temporary downloaded files if the process fails or throws exceptions. Mitigation: Clean up strictly in `finally` block.
- **Unsafe filename extraction**: Extracting filename from URLs could result in directory traversal or invalid characters. Mitigation: Implement a strict parsing helper that strips path elements, queries, and defaults to `download.mp4`.

## Open Questions

None.

## Notes for the Implementing Agent

- When unit testing the background async execution, use a synchronous `SyncTaskExecutor` or Mockito argument capture in your unit tests to verify the background run behavior synchronously.
- Make sure to sanitize path separators in filename extraction.
