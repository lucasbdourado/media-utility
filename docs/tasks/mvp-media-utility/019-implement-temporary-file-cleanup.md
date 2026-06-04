# Task: Implement Temporary File Cleanup

## Status

Status: Completed

Last updated: 2026-06-03

## Task ID

ID: MVP-MEDIA-019

Order: 019

Task file: `docs/tasks/mvp-media-utility/019-implement-temporary-file-cleanup.md`

## Source Documents

List every document or explicit user decision that justifies this task.

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project planning | `docs/planning/project-planning.md` | Task Group: Result Delivery and Temporary Files, Task: Implement temporary file cleanup job | Confirmed by source document | Details the need for deleting expired temporary files and associated metadata. |
| ADR-006 | `docs/adrs/006-use-root-relative-keys-for-temporary-local-storage.md` | Decision, Consequences | Accepted | Confirms that storage keys are root-relative and that missing-file deletion is idempotent. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/storage/TemporaryStorageService.java` | Interface contract | Detected in codebase | Exposes `delete(String internalPath)` for file deletion. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/operations/Operation.java` | Fields and complete method | Detected in codebase | Declares `expiresAt` and `resultFile` fields. |

## Context

The application creates temporary media files on the local filesystem during conversion or download operations. These files are tracked via the `Operation.resultFile` embedded metadata field, and an expiration timestamp is stored in `Operation.expiresAt`. 

To prevent the server's storage root from filling up and to respect file lifecycle requirements, we need to periodically scan the database for completed operations that have expired, delete their physical files on disk, and clear their associated metadata in the database.

## Goal

Implement a scheduled background service `TemporaryFileCleanupService` that runs periodically to delete physical files on disk for expired completed operations and clear their associated result file metadata in the database, verified with comprehensive unit tests.

## Scope

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

## Implementation Instructions

- Inject `Clock` rather than using `Instant.now()` directly to ensure testability.
- Rely on the idempotent behavior of `TemporaryStorageService.delete()` (which succeeds even if the file was already deleted from disk).
- Use `logger.error` or `logger.warn` to log individual file deletion exceptions without rethrowing them.

## Expected Files

| Path | Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasdourado/mediautility/operations/Operation.java` | Modify | Domain model | Add domain method to clear metadata. |
| `src/main/java/com/lucasdourado/mediautility/persistence/OperationRepository.java` | Modify | Persistence layer | Add query finder method. |
| `src/main/java/com/lucasdourado/mediautility/MediaUtilityApplication.java` | Modify | Boot configuration | Add `@EnableScheduling`. |
| `src/main/java/com/lucasdourado/mediautility/cleanup/TemporaryFileCleanupService.java` | Create | Cleanup logic | Background scheduled cleanup runner. |
| `src/test/java/com/lucasdourado/mediautility/cleanup/TemporaryFileCleanupServiceTest.java` | Create | Testing | Unit tests verifying the scheduler behavior and resilience. |

## Dependencies

| Dependency | Type | Status | Notes |
| --- | --- | --- | --- |
| MVP-MEDIA-004 | Previous task | Completed | Operation domain metadata entity exists. |
| MVP-MEDIA-006 | Previous task | Completed | Temporary storage service exists. |
| MVP-MEDIA-018 | Previous task | Completed | Result download endpoint exists. |

## Validation

- Compile successfully: `.\mvnw clean compile`
- Run unit/integration tests: `.\mvnw test`
- Verifications to write in tests:
  - Expired completed operations are correctly deleted from disk and metadata is cleared.
  - Non-expired completed operations or operations with other statuses are ignored.
  - File deletion failures do not halt the entire cleanup process.

## Acceptance Criteria

- [x] `@EnableScheduling` is active in the Spring Boot application context.
- [x] `TemporaryFileCleanupService` runs periodically using `@Scheduled` configured via property `${media-utility.cleanup.fixed-delay:60000}`.
- [x] The cleanup process deletes expired files from disk and sets their metadata `resultFile` to null in the database.
- [x] Handled exceptions ensure that a failed file deletion doesn't block the rest of the batch.
- [x] Unit tests cover success paths, no-op cases, non-expired operations, and storage service failures.

## Risks

- **Database Locks**: Deleting files and saving operations one by one could cause locks if the database has huge batches of expired files. Mitigation: Since the scheduler runs frequently, the size of each batch should remain small.

## Open Questions

None.

## Notes for the Implementing Agent

- Use `@TempDir` in unit tests if physical file deletion checks are needed, or verify calls to `TemporaryStorageService.delete(...)` via mock assertions.
