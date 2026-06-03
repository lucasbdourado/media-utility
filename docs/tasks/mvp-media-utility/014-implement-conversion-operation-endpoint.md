# Task: Implement Conversion Operation Endpoint

## Status

Status: Completed

Last updated: 2026-06-03

## Task ID

ID: MVP-MEDIA-014

Order: 014

Task file: `docs/tasks/mvp-media-utility/014-implement-conversion-operation-endpoint.md`

## Source Documents

List every document or explicit user decision that justifies this task.

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project planning | `docs/planning/project-planning.md` | Task Group: Media Conversion, Task: Add conversion endpoint or handler | Confirmed by source document | Focuses on exposing the conversion operation to the web client, accepting MP4, calling conversion service, and returning result metadata. |
| ADR-008 | `docs/adrs/008-define-public-rest-api-contract.md` | Decision, Consequences | Accepted | Confirms API endpoint mapping, response structure, validation mapping, and error shape. |
| ADR-005 | `docs/adrs/005-use-shared-operation-domain-model-for-metadata-persistence.md` | Decision, Consequences | Accepted | Confirms persistence entity model `Operation` and encapsulation of internal storage paths. |
| ADR-006 | `docs/adrs/006-use-root-relative-keys-for-temporary-local-storage.md` | Decision, Consequences | Accepted | Confirms root-relative storage keys. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/api/OperationApiController.java` | API controller mapping | Detected in codebase | Exposes `/api/operations/conversions` and delegates to `OperationApiPort`. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/api/OperationApiPort.java` | API Port interface | Detected in codebase | Declares the boundary methods for API delegation. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/media/conversion/Mp4UploadValidator.java` | Entire class | Detected in codebase | Performs ftyp signature and size validations on MP4 uploads. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/media/conversion/Mp4ToMp3Converter.java` | Interface contract | Detected in codebase | Converts local MP4 to MP3. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/storage/TemporaryStorageService.java` | Interface contract | Detected in codebase | Manages temporary result files. |

## Context

The conversion operation endpoint exposes the MP4-to-MP3 flow to the web client. The REST controller `OperationApiController` is already defined and delegates to the `OperationApiPort` interface. The MP4 upload validator (`Mp4UploadValidator`) and FFmpeg conversion adapter (`FfmpegMp4ToMp3Converter`) are implemented and verified.

This task implements the `OperationApiPort` interface (specifically `createConversion(MultipartFile)` and `getOperation(Long)`) to orchestrate: validating the upload, creating/saving the pending `Operation` in the database, writing the uploaded file to disk, calling the conversion service in a background thread, storing the converted MP3 file in temporary storage, updating/completing the operation status, and cleaning up the local temporary source file.

## Goal

Implement a Spring-managed class implementing `OperationApiPort` (e.g. `OperationService`) that orchestrates the MP4-to-MP3 conversion flow, persists metadata via `OperationRepository`, and runs FFmpeg conversion asynchronously in a background thread.

## Scope

- Create a Spring Service/Component implementing `OperationApiPort` (e.g. `com.lucasdourado.mediautility.api.OperationService`).
- Inject `OperationRepository`, `Mp4UploadValidator`, `Mp4ToMp3Converter`, `TemporaryStorageService`, `TaskExecutor` (or Spring `AsyncTaskExecutor`), and the configured retention duration.
- Implement `createConversion(MultipartFile file)`:
  - Run upload validation using `Mp4UploadValidator.validate(file)`.
  - Catch `Mp4ValidationException` and map it to `ApiException` with correct HTTP Status and `PublicErrorResponse` (e.g., `SIZE_LIMIT_EXCEEDED` maps to `413 Payload Too Large` and `INVALID_HEADER` maps to `400 Bad Request` validation error).
  - Create and persist a new `Operation` record with type `CONVERSION` and status `PENDING` to get a database ID.
  - Copy the incoming `MultipartFile`'s input stream to a local temporary file (e.g. `Files.createTempFile("media-utility-upload-", ".mp4")`) synchronously on the request thread.
  - Submit a task to `TaskExecutor` to perform the background conversion orchestrating:
    1. Retrieve the operation and transition status to `PROCESSING`.
    2. Create a temporary local file for the conversion target (e.g. `Files.createTempFile("media-utility-convert-", ".mp3")`).
    3. Run `Mp4ToMp3Converter.convert(tempSource, tempTarget)`.
    4. Construct target filename (replace `.mp4` suffix from original filename with `.mp3`).
    5. Save the output MP3 file to temporary storage via `TemporaryStorageService.storeResult(...)`.
    6. Complete the operation in the database with the result metadata and calculated expiration time (`completedAt` plus configured retention duration).
    7. On any background error/exception, catch it and transition the operation status to `FAILED` with the exception message.
    8. In a `finally` block, delete local temporary source and target files using `Files.deleteIfExists` to prevent disk leaks.
  - Return the immediate pending `PublicOperationResponse`.
- Implement `getOperation(Long operationId)`:
  - Fetch the operation from `OperationRepository`. Throw `ApiException(404 Not Found)` if missing.
  - Map operation state (including result metadata or failure error responses) to `PublicOperationResponse` using safe representation fields (never expose internal paths or keys).
- Stub other `OperationApiPort` methods (`createDownload` and `getResult`) by throwing `UnsupportedOperationException` or `ApiException` (these are out of scope for this task and belong to Tasks 017 and 018).

## Out of Scope

- Do not implement URL/public download flow adapter (`yt-dlp`) or its controller handler (belongs to Task 017).
- Do not implement direct result streaming download handler (belongs to Task 018).
- Do not implement temporary file cleanup scheduler or background job (belongs to Task 019).
- Do not implement event logging or metrics tracking logic (belongs to Task 020).

## Implementation Instructions

- Keep all class implementations under package `com.lucasdourado.mediautility.api` (or a sub-package if preferred).
- Inject `TaskExecutor` (or Spring `AsyncTaskExecutor`) to execute background threads instead of using synchronous blocking or manual raw threads.
- Ensure that the retention duration is configurable via Spring properties (e.g. `media-utility.storage.retention`, defaulting to `24h` or `1h`).
- Make sure all temporary files created on disk during conversion (`tempSource`, `tempTarget`) are deleted under a `finally` block.
- Map `Mp4ValidationException` reasons to standard HTTP Status codes as required by ADR-008.

## Expected Files

| Path | Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasdourado/mediautility/api/OperationService.java` | Create | ADR-008, planning | Port implementation class. |
| `src/test/java/com/lucasdourado/mediautility/api/OperationServiceTest.java` | Create | Validation need | Unit tests verifying orchestration, async handling, exception mapping, and file cleanup. |
| `src/main/resources/application.properties` | Modify | Config requirement | Add `media-utility.storage.retention` configuration property. |

## Dependencies

| Dependency | Type | Status | Notes |
| --- | --- | --- | --- |
| MVP-MEDIA-004 | Previous task | Completed | Operation domain metadata entity exists. |
| MVP-MEDIA-006 | Previous task | Completed | Temporary storage service exists. |
| MVP-MEDIA-012 | Previous task | Completed | MP4 upload validator exists. |
| MVP-MEDIA-013 | Previous task | Completed | FFmpeg conversion adapter exists. |

## Validation

- Backend compiles successfully.
- Run `OperationServiceTest` to verify:
  - MP4 validation failure is mapped to correct HTTP exceptions.
  - Successful creation creates a database record, copies the upload, starts async task, and returns `PENDING`.
  - Async task successfully runs conversion, stores results, updates status to `COMPLETED`, and deletes temporary files.
  - Async task failure updates status to `FAILED`, stores error message, and deletes temporary files.
  - Expiration time is computed correctly based on configuration.
  - Local temp files are deleted in all cases.
- Run full codebase tests via `.\mvnw test`.

## Acceptance Criteria

- [ ] Class `OperationService` (or similar) implements `OperationApiPort` and is registered as a Spring bean.
- [ ] MP4 upload validation is executed, and its exceptions are mapped to the correct HTTP status codes/error response formats defined in ADR-008.
- [ ] A database record is created in `OperationRepository` with status `PENDING`.
- [ ] The heavy conversion process runs asynchronously in a background thread using `TaskExecutor`.
- [ ] Expiration date is calculated and configured via property `media-utility.storage.retention`.
- [ ] Completed conversions are stored in `TemporaryStorageService`, and the operation metadata is updated to `COMPLETED` with safe public result metadata.
- [ ] Failed conversions update the operation status to `FAILED` with the error reason.
- [ ] Local temporary uploaded file and target file are guaranteed to be cleaned up from disk in both success and failure cases.
- [ ] Unit tests cover validation mapping, async thread submission, operation state changes, and temporary file cleanup.

## Risks

- Disk space exhaustion: Failure to delete local temporary source/target files (`tempSource`, `tempTarget`) under `finally` will fill up disk space quickly.
- Asynchronous resource cleanup: If the background task execution fails before or during file streaming, we must ensure file streams are closed so they can be deleted.
- Database locks/stale states: If background updates take long, make sure proper transaction handling or repository save semantics are used.

## Open Questions

None.

## Notes for the Implementing Agent

- When unit testing the background async execution, use a synchronous `SyncTaskExecutor` or Mockito arguments capturing in your unit tests to verify the background run behavior synchronously without concurrency timing issues.
- Retrieve the original filename extension safely, and gracefully handle empty/null filename strings.
