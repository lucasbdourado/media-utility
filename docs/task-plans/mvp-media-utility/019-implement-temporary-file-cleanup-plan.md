# Task Implementation Plan: Implement Temporary File Cleanup

## Status

Status: Ready for Implementation

Last updated: 2026-06-03

Plan file: `docs/task-plans/mvp-media-utility/019-implement-temporary-file-cleanup-plan.md`

Architecture decision notes file: Not generated

## Task Reference

Task ID: `MVP-MEDIA-019`

Task file: `docs/tasks/mvp-media-utility/019-implement-temporary-file-cleanup.md`

Task status: `Ready`

Task group or feature: `mvp-media-utility`

## Planning Mode Requirement

Plan mode verified: Yes

Notes:

- This plan must be created in plan mode.
- Implementation must not start during `plan-task`.
- A future implementation request must use this saved plan as source context.

## Source Documents

List every document, ADR, codebase evidence item, or explicit user decision used to prepare this plan.

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project planning | `docs/planning/project-planning.md` | Task Group: Result Delivery and Temporary Files, Task: Implement temporary file cleanup job | Confirmed by source document | Details the need for deleting expired temporary files and associated metadata. |
| ADR-006 | `docs/adrs/006-use-root-relative-keys-for-temporary-local-storage.md` | Decision, Consequences | Accepted | Confirms that storage keys are root-relative and that missing-file deletion is idempotent. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/storage/TemporaryStorageService.java` | Interface contract | Detected in codebase | Exposes `delete(String internalPath)` for file deletion. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/operations/Operation.java` | Fields and complete method | Detected in codebase | Declares `expiresAt` and `resultFile` fields. |
| User Decision | Current `plan-task` session | Database Cleanup Strategy | Confirmed by user | Decided that `resultFile` will be set to `null` to clear metadata while keeping the operation row for metrics. |
| User Decision | Current `plan-task` session | Scheduler Configuration | Confirmed by user | Decided to configure a fixed delay scheduled execution: `@Scheduled(fixedDelayString = "${media-utility.cleanup.fixed-delay:60000}")`. |

## Context Summary

The application creates temporary media files on the local filesystem during conversion or download operations. These files are tracked via the `Operation.resultFile` embedded metadata field, and an expiration timestamp is stored in `Operation.expiresAt`. 

To prevent the server's storage root from filling up and to respect file lifecycle requirements, we need to periodically scan the database for completed operations that have expired, delete their physical files on disk, and clear their associated metadata in the database.

## Task Goal

Implement a scheduled background service `TemporaryFileCleanupService` that runs periodically to delete physical files on disk for expired completed operations and clear their associated result file metadata in the database, verified with comprehensive unit tests.

## Confirmed Scope

- Modify `com.lucasdourado.mediautility.operations.Operation` to add a public domain method to clear the result metadata:
  ```java
  public void clearResultFile() {
      this.resultFile = null;
  }
  ```
- Modify `com.lucasdourado.mediautility.persistence.OperationRepository` to declare a Spring Data JPA finder method to query expired completed operations that still have file metadata:
  ```java
  List<Operation> findByStatusAndExpiresAtBeforeAndResultFileIsNotNull(
          OperationStatus status,
          Instant time);
  ```
- Modify `com.lucasdourado.mediautility.MediaUtilityApplication` to include the `@EnableScheduling` annotation.
- Create `com.lucasdourado.mediautility.cleanup.TemporaryFileCleanupService` as a Spring-managed bean (`@Component` or `@Service`):
  - Inject `OperationRepository`, `TemporaryStorageService`, and `Clock`.
  - Implement a method `cleanupExpiredFiles()` scheduled to run periodically using `@Scheduled(fixedDelayString = "${media-utility.cleanup.fixed-delay:60000}")`.
  - In the scheduled method:
    1. Query all completed operations whose `expiresAt` is in the past (`expiresAt < clock.instant()`) and `resultFile` is not null.
    2. For each expired operation, log the cleanup attempt, call `temporaryStorageService.delete(...)` with the key from `resultFile.getInternalPath()`, call `operation.clearResultFile()`, and save the updated operation.
    3. Use a try-catch block inside the loop to ensure that an error deleting a specific file does not abort the entire cleanup cycle or crash the scheduler.
- Create `com.lucasdourado.mediautility.cleanup.TemporaryFileCleanupServiceTest` to verify cleanup logic:
  - Mock database retrieval of expired completed operations.
  - Verify that matching physical files are deleted and database metadata is cleared.
  - Verify that non-expired operations, non-completed operations, or operations without result file metadata are ignored.
  - Verify resilience when `TemporaryStorageService.delete()` throws an exception (other files are still processed).

## Out of Scope

- Adding success and failure tracking metrics (belongs to Task 020).
- Modifying REST API endpoints or UI flow states.

## Requirements Covered

| Requirement | Source | How This Task Covers It | Status |
| --- | --- | --- | --- |
| Automatic file removal | PRD FR-006 | Periodically purges physical files on disk and clears metadata | Confirmed |
| Idempotent storage cleanup | ADR-006 | Leverages `TemporaryStorageService.delete` which normalizes paths and handles missing files gracefully | Confirmed |

## Technical Specification Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Modules and Responsibilities | Partial | Cleans up storage files and clears domain metadata | Tech Spec document is empty, but project planning, ADRs, and codebase provide complete details. |

Coverage assessment:

- Justifying Tech Spec section: N/A
- Tech Spec sections implemented by this task: N/A
- Gaps between task and Tech Spec: The `tech-spec.md` is empty, but project-planning and codebase provide the necessary details.
- Dependencies not specified by the Tech Spec: None.

## Architecture and ADR Considerations

| Decision or ADR | Source | Impact on This Task | Status |
| --- | --- | --- | --- |
| ADR-006 | `docs/adrs/006-use-root-relative-keys-for-temporary-local-storage.md` | Storage keys resolution and idempotent deletion | Confirmed |
| ADR-005 | `docs/adrs/005-use-shared-operation-domain-model-for-metadata-persistence.md` | Operation model schema and constraints | Confirmed |

ADR candidates or architecture decisions needed:

- None

Architecture decision notes:

- Saved separately: No
- Path: `Not generated`
- Notes file status: Not applicable

## Confirmed Decisions

- Expired completed files are deleted on disk and their associated metadata is cleared in the database.
- The `Operation` row is preserved for metrics history (the foreign keys from `OperationEvent` are kept).
- Setting `resultFile = null` in `Operation` is the mechanism used to clear metadata and exclude operations from future scheduler sweeps.
- `@EnableScheduling` enables scheduler processing.
- Configurable delay is set to 60000ms by default.
- Exception handling inside the scheduler loop prevents single-file errors from crashing the cleanup process.

## Pending Decisions

None. All task-relevant decisions have been answered or explicitly deferred out of scope by the user.

## Questions for the User

None. All task-relevant questions have been answered.

## Proposed Implementation Approach

1. **Domain and Persistence Updates**:
   * Add a `clearResultFile()` method to `Operation.java` to set the `resultFile` embedded field to `null`.
   * Declare the finder method in `OperationRepository.java` to search for expired operations with non-null result metadata.
2. **Scheduling Setup**:
   * Add `@EnableScheduling` to `MediaUtilityApplication.java`.
3. **Cleanup Service Implementation**:
   * Implement `TemporaryFileCleanupService` under `com.lucasdourado.mediautility.cleanup`.
   * Create the scheduled method iterating over the operations list, deleting the files, setting metadata to null, and saving.
4. **Validation**:
   * Implement `TemporaryFileCleanupServiceTest.java` verifying all logic using mock classes and Clock control.

## Files and Areas Expected to Change

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasdourado/mediautility/operations/Operation.java` | Modify | Domain model | Add domain method to clear metadata. |
| `src/main/java/com/lucasdourado/mediautility/persistence/OperationRepository.java` | Modify | Persistence layer | Add query finder method. |
| `src/main/java/com/lucasdourado/mediautility/MediaUtilityApplication.java` | Modify | Boot configuration | Add `@EnableScheduling`. |
| `src/main/java/com/lucasdourado/mediautility/cleanup/TemporaryFileCleanupService.java` | Create | Cleanup logic | Background scheduled cleanup runner. |
| `src/test/java/com/lucasdourado/mediautility/cleanup/TemporaryFileCleanupServiceTest.java` | Create | Testing | Unit tests verifying scheduler behavior. |

## Step-by-Step Implementation Plan

1. **Domain Model Change**:
   - Open [Operation.java](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/src/main/java/com/lucasdourado/mediautility/operations/Operation.java).
   - Add the public method `clearResultFile()` setting `this.resultFile = null`.
2. **Repository Integration**:
   - Open [OperationRepository.java](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/src/main/java/com/lucasdourado/mediautility/persistence/OperationRepository.java).
   - Add the finder method declaration `findByStatusAndExpiresAtBeforeAndResultFileIsNotNull(...)`.
3. **Enable Scheduling**:
   - Open [MediaUtilityApplication.java](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/src/main/java/com/lucasdourado/mediautility/MediaUtilityApplication.java).
   - Add `@org.springframework.scheduling.annotation.EnableScheduling` to the class declaration annotations.
4. **Implement Scheduler**:
   - Create `com/lucasdourado/mediautility/cleanup/TemporaryFileCleanupService.java`.
   - Implement the scheduled loop with Clock-injected `Instant` evaluation, robust individual file deletion wrapping, and database updates.
5. **Implement Unit Tests**:
   - Create `src/test/java/com/lucasdourado/mediautility/cleanup/TemporaryFileCleanupServiceTest.java`.
   - Cover matching query invocations, deletion calls, database entity saves, no-op cases, and robust logging on file exception throw.
6. **Execution Verification**:
   - Run compilation: `.\mvnw clean compile`
   - Run test suite: `.\mvnw test`

## Validation Strategy

Verify using comprehensive unit tests in `TemporaryFileCleanupServiceTest.java` that:
- Deletion is successfully run on expired completed operations.
- Non-expired, pending, or failed operations are correctly skipped.
- Exception on disk deletion for one operation does not halt the iteration or scheduler thread.

## Tests to Add or Update

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| `TemporaryFileCleanupServiceTest` | Unit | Verifies retrieval, disk removal, metadata updates, and error boundary resilience | Utilizes mock repository/storage components and fixed Clock control |

## Acceptance Criteria

- [ ] `@EnableScheduling` is active in the Spring Boot application context.
- [ ] `TemporaryFileCleanupService` runs periodically using `@Scheduled` configured via property `${media-utility.cleanup.fixed-delay:60000}`.
- [ ] The cleanup process deletes expired files from disk and sets their metadata `resultFile` to null in the database.
- [ ] Handled exceptions ensure that a failed file deletion doesn't block the rest of the batch.
- [ ] Unit tests cover success paths, no-op cases, non-expired operations, and storage service failures.

## Risks and Edge Cases

- **Concurrent sweeps/locks**: Overlapping execution runs if a delay is short and the deletion batch is very large. Mitigation: Configured with `fixedDelayString` rather than `fixedRateString` to guarantee that the next run starts only after the current one completes.
- **Failures updating DB**: If the file is deleted but the database fails to save, the scheduler will try to delete it again on the next run. This is safe because `Files.deleteIfExists(...)` handles missing files idempotently.

## Rollback or Recovery Notes

- Git checkout: Revert modifications to `Operation.java`, `OperationRepository.java`, and `MediaUtilityApplication.java`. Delete new service and test files.

## Documentation Updates

- Update task file `019-implement-temporary-file-cleanup.md` status to `Completed` once implementation task execution completes.

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

- Inject `Clock` to ensure mock time can be set in the service test.
- Use `logger.error` or `logger.warn` to log individual file deletion exceptions without rethrowing them.
