# Task Implementation Plan: Implement Conversion Operation Endpoint

## Status

Status: Completed

Last updated: 2026-06-03

Plan file: `docs/task-plans/mvp-media-utility/014-implement-conversion-operation-endpoint-plan.md`

Architecture decision notes file: `docs/architecture/task-decisions/mvp-media-utility/014-implement-conversion-operation-endpoint-architecture-decisions.md`

## Task Reference

Task ID: `MVP-MEDIA-014`

Task file: `docs/tasks/mvp-media-utility/014-implement-conversion-operation-endpoint.md`

Task status: `Ready`

Task group or feature: `mvp-media-utility`

## Planning Mode Requirement

Plan mode verified: Yes

Notes:
- This plan was created in plan mode.
- Implementation will not start during `plan-task`.
- A future implementation request must use this saved plan and the saved architecture decision notes as source context.

## Source Documents

List every document, ADR, codebase evidence item, or explicit user decision used to prepare this plan.

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
| User decision | Current planning session | Concurrency, class location, properties | Confirmed by user | User chose class location, retention duration, and Spring `@Async` execution method. |

## Context Summary

The conversion operation endpoint exposes the MP4-to-MP3 flow to the web client. The REST controller `OperationApiController` is already defined and delegates to the `OperationApiPort` interface. The MP4 upload validator (`Mp4UploadValidator`) and FFmpeg conversion adapter (`FfmpegMp4ToMp3Converter`) are implemented and verified.

This task implements the `OperationApiPort` interface (specifically `createConversion(MultipartFile)` and `getOperation(Long)`) to orchestrate: validating the upload, creating/saving the pending `Operation` in the database, writing the uploaded file to disk, calling the conversion service in a background thread, storing the converted MP3 file in temporary storage, updating/completing the operation status, and cleaning up the local temporary source file.

## Task Goal

Implement a Spring-managed class implementing `OperationApiPort` (e.g. `OperationService`) that orchestrates the MP4-to-MP3 conversion flow, persists metadata via `OperationRepository`, and runs FFmpeg conversion asynchronously in a background thread.

## Confirmed Scope

- Modify `MediaUtilityApplication.java` to enable Spring asynchronous execution using `@EnableAsync`.
- Create a new Spring service implementation `OperationService` implementing `OperationApiPort` under `com.lucasdourado.mediautility.api`.
- Create a new Spring component `BackgroundConversionExecutor` containing the `@Async` execution method to prevent self-invocation proxy limitations.
- Inject `OperationRepository`, `Mp4UploadValidator`, `BackgroundConversionExecutor` and `TemporaryStorageService`.
- Implement `createConversion(MultipartFile file)` to perform:
  - Synchronous upload validation via `Mp4UploadValidator.validate(file)`.
  - Mapping of `Mp4ValidationException` to `ApiException` with correct HTTP Status and `PublicErrorResponse`.
  - Creating and saving `Operation` (type `CONVERSION`, status `PENDING`) to DB.
  - Writing multipart upload contents to a temporary source file on disk.
  - Calling the `@Async` method of `BackgroundConversionExecutor` with operation ID, temp source path, and original filename.
  - Returning immediate `PublicOperationResponse` in status `PENDING`.
- Implement background executor's `@Async` method to:
  - Transition the operation status to `PROCESSING` in the database.
  - Create a temporary target path for the conversion.
  - Call `Mp4ToMp3Converter.convert(tempSource, tempTarget)`.
  - Suffix-replace the original filename `.mp4` to `.mp3` (or default to `result.mp3`).
  - Read `tempTarget` and store it via `TemporaryStorageService.storeResult(...)`.
  - Transition the operation status to `COMPLETED` in the database with the result metadata and expiration time (`completedAt` plus configurable retention duration).
  - Transition to `FAILED` with failure details if any exception occurs.
  - Delete temporary source and target files on disk in a `finally` block.
- Implement `getOperation(Long operationId)`:
  - Fetch the operation from repository, or throw `ApiException(404)` if not found.
  - Map operation state (including result metadata or failure error responses) to `PublicOperationResponse` using safe representation fields.
- Stub other `OperationApiPort` methods (`createDownload` and `getResult`) by throwing `UnsupportedOperationException` or `ApiException` (handled in later tasks).
- Configure the retention duration via Spring property `media-utility.storage.retention` in `application.properties`, defaulting to `1h`.

## Out of Scope

- Implementing the URL/public download flow adapter (`yt-dlp`) or its controller handler (belongs to Task 017).
- Implementing direct result streaming download handler (belongs to Task 018).
- Implementing temporary file cleanup scheduler or background job (belongs to Task 019).
- Implementing event logging or metrics tracking logic (belongs to Task 020).

## Requirements Covered

Map product, planning, or task requirements to this implementation plan.

| Requirement | Source | How This Task Covers It | Status |
| --- | --- | --- | --- |
| MP4-to-MP3 conversion operation endpoint | PRD FR-003, project planning | Implements the endpoint logic to orchestrate MP4 file upload, validation, conversion, and result metadata return. | Confirmed |
| Return immediate pending response | ADR-008 | Returns a PENDING operation response immediately while launching the conversion in the background. | Confirmed |
| Hide internal storage paths | ADR-005, ADR-006 | Translates internal storage paths into safe public URLs (`/api/operations/{id}/result`). | Confirmed |

## Technical Specification Coverage

Explain how the Tech Spec covers this task.

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| API Endpoints | Partial | Implements `POST /api/operations/conversions` and `GET /api/operations/{id}`. | `docs/specs/tech-spec.md` is empty, but design is fully detailed by ADR-008. |

Coverage assessment:
- Justifying Tech Spec section: N/A (Tech Spec is empty).
- Tech Spec sections implemented by this task: N/A.
- Gaps between task and Tech Spec: The empty Tech Spec is bypassed by referencing binding architecture decisions in ADR-008, ADR-005, ADR-006, and ADR-007.
- Dependencies not specified by the Tech Spec: None.

## Architecture and ADR Considerations

List relevant architecture decisions, ADRs, technology definitions, and decision candidates.

| Decision or ADR | Source | Impact on This Task | Status |
| --- | --- | --- | --- |
| ADR-008 (Define REST API Contract) | `docs/adrs/008-define-public-rest-api-contract.md` | Binding: Defines endpoint routes, request shapes, response metadata fields, error shape, and HTTP status codes. | Confirmed |
| ADR-005 (Shared Operation Model) | `docs/adrs/005-use-shared-operation-domain-model-for-metadata-persistence.md` | Binding: Defines domain model fields and encapsulates internal filesystem paths. | Confirmed |
| ADR-006 (Temporary Local Storage) | `docs/adrs/006-use-root-relative-keys-for-temporary-local-storage.md` | Binding: Persists root-relative keys, resolves absolute paths only inside storage code. | Confirmed |
| ADR-007 (Process Execution) | `docs/adrs/007-define-process-execution-contract-for-media-tools.md` | Binding: Banned direct API execution of external commands; conversion must be done via the adapter implementing `Mp4ToMp3Converter`. | Confirmed |

ADR candidates or architecture decisions needed:
- None.

Architecture decision notes:
- Saved separately: Yes
- Path: `docs/architecture/task-decisions/mvp-media-utility/014-implement-conversion-operation-endpoint-architecture-decisions.md`
- Notes file status: New

## Confirmed Decisions

List decisions that are already confirmed by source documents, codebase evidence, or explicit user confirmation.

- Class `OperationService` is implemented in the `api` package.
- Background execution is driven by Spring `@Async` annotation. This requires adding `@EnableAsync` to `MediaUtilityApplication`.
- To prevent Spring `@Async` self-proxy invocation limitations, a separate helper class `BackgroundConversionExecutor` is created with the `@Async` execution method.
- The retention duration property `media-utility.storage.retention` is introduced with a default value of `1h`.
- Validation exceptions (`Mp4ValidationException`) are mapped to `ApiException` inside `OperationService`.

## Pending Decisions

None. All task-relevant decisions have been answered or explicitly deferred out of scope by the user.

## Questions for the User

None. All task-relevant questions have been answered.

## Proposed Implementation Approach

Describe how the future implementing agent should approach the task using only confirmed information.

1. **Enable Async**: Add `@EnableAsync` annotation to `MediaUtilityApplication`.
2. **Add Configuration**: Add `media-utility.storage.retention=1h` to `src/main/resources/application.properties`.
3. **Background Executor**: Create a class `BackgroundConversionExecutor` annotated with `@Component`. Define `public void executeConversion(Long operationId, Path sourcePath, String originalFilename)` annotated with `@Async`. Inside:
   - Mark operation as `PROCESSING`.
   - Create local temporary file `tempTarget`.
   - Call `Mp4ToMp3Converter.convert`.
   - Store results via `TemporaryStorageService.storeResult` using a `.mp3` target filename derived from `originalFilename`.
   - Complete operation with metadata and expiration date.
   - Fail operation with exception message if errors occur.
   - Clean up local temp files in `finally`.
4. **Operation Service**: Create `OperationService` implementing `OperationApiPort`. Inside:
   - Validate file using `Mp4UploadValidator`. Catch `Mp4ValidationException` and map to `ApiException` with correct HTTP Status (e.g. `SIZE_LIMIT_EXCEEDED` maps to `HttpStatus.PAYLOAD_TOO_LARGE`).
   - Create/save the `Operation` in `OperationRepository`.
   - Copy file stream to temporary source file synchronously.
   - Trigger background executor.
   - Return pending `PublicOperationResponse`.
5. **Operation Retrieval**: Implement `getOperation` to fetch from `OperationRepository` (or throw `ApiException(404)` if not found) and map to `PublicOperationResponse` using safe representations.

## Files and Areas Expected to Change

List expected files, modules, packages, docs, tests, or areas.

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasdourado/mediautility/MediaUtilityApplication.java` | Modify | User Decision | Add `@EnableAsync`. |
| `src/main/resources/application.properties` | Modify | User Decision | Add `media-utility.storage.retention`. |
| `src/main/java/com/lucasdourado/mediautility/api/BackgroundConversionExecutor.java` | Create | User Decision | Helper component for `@Async` background execution. |
| `src/main/java/com/lucasdourado/mediautility/api/OperationService.java` | Create | ADR-008, planning | Implementing `OperationApiPort`. |
| `src/test/java/com/lucasdourado/mediautility/api/OperationServiceTest.java` | Create | Validation | Unit tests verifying flow orchestration. |

## Step-by-Step Implementation Plan

Give the future implementing agent a focused sequence of implementation steps.

1. **Step 1: Configuration and Async Enablement**
   - Add `@EnableAsync` to `MediaUtilityApplication.java`.
   - Add `media-utility.storage.retention=1h` to `src/main/resources/application.properties`.
2. **Step 2: Implement Background Executor**
   - Create `BackgroundConversionExecutor.java`.
   - Annotate with `@Component` and write the `@Async` execution method using safe resource copying, database status updates, and local disk cleanup in a `finally` block.
3. **Step 3: Implement Operation Service**
   - Create `OperationService.java`.
   - Implement `createConversion(MultipartFile)` with validator integration and exception mapping.
   - Implement `getOperation(Long)` returning mapped database state.
   - Stub `createDownload` and `getResult` throwing `UnsupportedOperationException`.
4. **Step 4: Implement Unit Tests**
   - Create `OperationServiceTest.java`.
   - Verify validation errors map to HTTP statuses.
   - Mock all dependencies and capture the runnables or arguments to test successful and failed flow paths.
5. **Step 5: Verification**
   - Verify compilation and run all tests via `.\mvnw test`.

## Validation Strategy

Define how the future implementation should be verified.

- Run `OperationServiceTest` to ensure that all validation mappings, async triggers, and database state transitions behave correctly.
- Run `OperationApiControllerTest` to ensure integration between the REST controller and the newly implemented `OperationService` behaves correctly.
- Verify that disk space remains clean by checking that the temporary source and target files are deleted on the host machine after processing.

## Tests to Add or Update

List specific tests or test areas expected for this task.

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| `OperationServiceTest` | Unit | Verifies `OperationService` orchestrates validation, db entity creation, copies stream, calls background executor, and retrieve operation status. | New test class. |
| `BackgroundConversionExecutorTest` | Unit | Verifies that background conversion successfully sets `PROCESSING` -> `COMPLETED`, deletes files, and calculates expiresAt correctly. | Optional/combined in OperationServiceTest. |

## Acceptance Criteria

List objective criteria that prove the task is complete.

- [ ] `MediaUtilityApplication.java` has `@EnableAsync` annotation.
- [ ] `media-utility.storage.retention` is defined in `application.properties`.
- [ ] Class `OperationService` implements `OperationApiPort` and is registered as a Spring bean.
- [ ] `createConversion` validates the file using `Mp4UploadValidator`, mapping `Mp4ValidationException` to `ApiException`.
- [ ] An `Operation` record is created in the database in status `PENDING`.
- [ ] The heavy conversion task runs asynchronously using Spring's `@Async` annotation.
- [ ] Converted files are stored in `TemporaryStorageService`, and the operation metadata is updated to `COMPLETED` with proper public result metadata and expiration date.
- [ ] Failed conversions update the operation status to `FAILED` with the error reason.
- [ ] Local temporary uploaded file and target file are deleted from disk in all outcomes.
- [ ] `getOperation` returns mapped database state or throws `ApiException(HttpStatus.NOT_FOUND)` if not found.
- [ ] Unit tests cover validation mapping, async thread submission, operation status transitions, and file cleanups.

## Risks and Edge Cases

List known risks, constraints, regression areas, and edge cases.

- **Disk Space Leak**: If background tasks throw unexpected errors before deleting `tempSource` or `tempTarget`, files will remain on disk. Handled by a robust `finally` block enclosing both delete operations.
- **Spring Context Load Failure**: Adding `@EnableAsync` could conflict with tests that mock context threads. Handled by configuring proper test execution.
- **Transaction Context inside Async Thread**: Background thread cannot inherit request-thread transaction. Database writes must be committed immediately using repository saves.

## Rollback or Recovery Notes

Describe how the implementing agent should think about rollback, recovery, or safe reversal when relevant.

- Delete newly created classes `OperationService.java`, `BackgroundConversionExecutor.java`, and `OperationServiceTest.java`.
- Revert changes to `MediaUtilityApplication.java` and `application.properties`.

## Documentation Updates

None.

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

- Use a synchronous task executor helper or capture execution context in unit tests to test the async code path deterministically.
- Suffix-replace the extension safely (e.g. `my-file.mp4` -> `my-file.mp3`) and fallback to `result.mp3` if the original file name is null or blank.
