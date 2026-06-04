# Task: Add Operation Success and Failure Tracking

## Status

Status: Ready

Last updated: 2026-06-03

## Task ID

ID: MVP-MEDIA-020

Order: 020

Task file: `docs/tasks/mvp-media-utility/020-add-operation-success-and-failure-tracking.md`

## Source Documents

List every document or explicit user decision that justifies this task.

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project planning | `docs/planning/project-planning.md` | Task Group: Metrics and Observability, Tasks: Track successful operations, Track failed operations | Confirmed by source document | Defines goals and validation expectations for tracking successful and failed operations. |
| Task 005 decision notes | `docs/architecture/task-decisions/mvp-media-utility/005-create-operation-events-model-architecture-decisions.md` | Confirmed Architecture Decisions | Confirmed by task context | Establishes taxonomy (`STARTED`, `COMPLETED`, `FAILED`) and relation with type snapshot. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/operations/OperationEvent.java` | Entire class | Detected in codebase | Declares factories for started, completed, and failed events. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/persistence/OperationEventRepository.java` | Interface contract | Detected in codebase | Persistent interface for operation event storage. |

## Context

The JPA entity `OperationEvent` and repository `OperationEventRepository` were introduced to capture operational metrics. These events record lifecycle transitions (`STARTED`, `COMPLETED`, `FAILED`) and snapshot the operation type to support reporting.
Currently, these events are not recorded during execution. We need to integrate the repository and emit events at key lifecycle transitions: when operations are created (started) and when asynchronous execution finishes (completed or failed).

## Goal

Integrate `OperationEventRepository` into the application workflow to save `STARTED`, `COMPLETED`, and `FAILED` events for all media conversion and download operations, verified with comprehensive unit and integration tests.

## Scope

- Modify `com.lucasdourado.mediautility.api.OperationService`:
  - Inject `OperationEventRepository`.
  - In `createConversion(MultipartFile)`: After successfully saving the new `Operation`, save a `STARTED` event:
    ```java
    operationEventRepository.save(OperationEvent.started(operation, createdAt));
    ```
  - In `createDownload(URI)`: After successfully saving the new `Operation`, save a `STARTED` event:
    ```java
    operationEventRepository.save(OperationEvent.started(operation, createdAt));
    ```
- Modify `com.lucasdourado.mediautility.api.BackgroundConversionExecutor`:
  - Inject `OperationEventRepository`.
  - On successful completion: Save a `COMPLETED` event using the `completedAt` timestamp:
    ```java
    operationEventRepository.save(OperationEvent.completed(operation, completedAt));
    ```
  - On failure (catch block): Save a `FAILED` event using the `completedAt` timestamp and the failure reason:
    ```java
    operationEventRepository.save(OperationEvent.failed(operation, completedAt, reason));
    ```
- Modify `com.lucasdourado.mediautility.api.BackgroundDownloadExecutor`:
  - Inject `OperationEventRepository`.
  - On successful completion: Save a `COMPLETED` event using the `completedAt` timestamp:
    ```java
    operationEventRepository.save(OperationEvent.completed(operation, completedAt));
    ```
  - On failure (catch block): Save a `FAILED` event using the `completedAt` timestamp and the failure reason:
    ```java
    operationEventRepository.save(OperationEvent.failed(operation, completedAt, reason));
    ```
- Update tests to verify event logging:
  - Update `OperationServiceTest.java` to verify that `STARTED` events are persisted for conversions and downloads.
  - Update `BackgroundConversionExecutor` tests (in `OperationServiceTest.java`) to verify `COMPLETED` and `FAILED` events are persisted.
  - Update `BackgroundDownloadExecutorTest.java` to verify `COMPLETED` and `FAILED` events are persisted.

## Out of Scope

- Exposing event logs through public REST API endpoints (not required by MVP).
- Generating periodic metrics reports or dashboards.

## Implementation Instructions

- Keep event persistence synchronous within the same transactions/methods where the parent `Operation` is saved.
- Ensure that if an operation execution fails, the `FAILED` event is saved correctly in the database.
- Use the exact same timestamps for `OperationEvent.occurredAt` as used for `Operation.createdAt` / `Operation.completedAt`.

## Expected Files

| Path | Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasdourado/mediautility/api/OperationService.java` | Modify | Planning, events model | Inject repository, save `STARTED` events. |
| `src/main/java/com/lucasdourado/mediautility/api/BackgroundConversionExecutor.java` | Modify | Planning, events model | Inject repository, save `COMPLETED`/`FAILED` events. |
| `src/main/java/com/lucasdourado/mediautility/api/BackgroundDownloadExecutor.java` | Modify | Planning, events model | Inject repository, save `COMPLETED`/`FAILED` events. |
| `src/test/java/com/lucasdourado/mediautility/api/OperationServiceTest.java` | Modify | Validation need | Verify events for creation and background conversion. |
| `src/test/java/com/lucasdourado/mediautility/api/BackgroundDownloadExecutorTest.java` | Modify | Validation need | Verify events for background download. |

## Dependencies

| Dependency | Type | Status | Notes |
| --- | --- | --- | --- |
| MVP-MEDIA-004 | Previous task | Completed | Operation domain metadata entity exists. |
| MVP-MEDIA-005 | Previous task | Completed | Operation event model and repository exist. |
| MVP-MEDIA-014 | Previous task | Completed | Conversion endpoint exists. |
| MVP-MEDIA-017 | Previous task | Completed | URL download endpoint exists. |

## Validation

- Compile successfully: `.\mvnw clean compile`
- Run unit/integration tests: `.\mvnw test`
- Verifications to write in tests:
  - Creation of a conversion or download persists a `STARTED` event with the correct operation details and timestamp.
  - Successful execution of conversion/download persists a `COMPLETED` event.
  - Failed execution of conversion/download persists a `FAILED` event containing the correct failure reason.

## Acceptance Criteria

- [ ] `OperationEventRepository` is integrated into `OperationService`, `BackgroundConversionExecutor`, and `BackgroundDownloadExecutor`.
- [ ] Every initiated operation generates a persistent `STARTED` event record.
- [ ] Every successful operation execution generates a persistent `COMPLETED` event record.
- [ ] Every failed operation execution generates a persistent `FAILED` event record containing the error message.
- [ ] Unit and integration tests verify the creation and attributes of all event types under success and failure scenarios.

## Risks

- **Transaction isolation/failures**: If the event repository save fails, it should not fail the main operation if possible, but since metrics are critical here, keeping them in the same transaction is acceptable and standard.

## Open Questions

None.

## Notes for the Implementing Agent

- Ensure to mock `OperationEventRepository` in existing test classes and verify the `save` invocations with appropriate arguments.
