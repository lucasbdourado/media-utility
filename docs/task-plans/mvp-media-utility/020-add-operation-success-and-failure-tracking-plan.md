# Task Implementation Plan: Add Operation Success and Failure Tracking

## Status

Status: Ready for Implementation

Last updated: 2026-06-03

Plan file: `docs/task-plans/mvp-media-utility/020-add-operation-success-and-failure-tracking-plan.md`

Architecture decision notes file: `docs/architecture/task-decisions/mvp-media-utility/020-add-operation-success-and-failure-tracking-architecture-decisions.md`

## Task Reference

Task ID: `MVP-MEDIA-020`

Task file: `docs/tasks/mvp-media-utility/020-add-operation-success-and-failure-tracking.md`

Task status: `Ready`

Task group or feature: `Metrics and Observability`

## Planning Mode Requirement

Plan mode verified: Yes

Notes:

- This plan was created in plan mode.
- Implementation must not start during `plan-task`.
- A future implementation request must use this saved plan and any saved architecture decision notes as source context.

## Source Documents

List every document, ADR, codebase evidence item, or explicit user decision used to prepare this plan.

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project planning | `docs/planning/project-planning.md` | Task Group: Metrics and Observability, Tasks: Track successful operations, Track failed operations | Confirmed by source document | Defines goals and validation expectations for tracking successful and failed operations. |
| Task 005 decision notes | `docs/architecture/task-decisions/mvp-media-utility/005-create-operation-events-model-architecture-decisions.md` | Confirmed Architecture Decisions | Confirmed by task context | Establishes taxonomy (`STARTED`, `COMPLETED`, `FAILED`) and relation with type snapshot. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/operations/OperationEvent.java` | Entire class | Detected in codebase | Declares factories for started, completed, and failed events. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/persistence/OperationEventRepository.java` | Interface contract | Detected in codebase | Persistent interface for operation event storage. |
| User decision | Current `plan-task` session | Event timing decision | Confirmed by user | User chose Option A: Save STARTED event in OperationService immediately upon creation. |

## Context Summary

The JPA entity `OperationEvent` and repository `OperationEventRepository` were introduced to capture operational metrics. These events record lifecycle transitions (`STARTED`, `COMPLETED`, `FAILED`) and snapshot the operation type to support reporting.
Currently, these events are not recorded during execution. We need to integrate the repository and emit events at key lifecycle transitions: when operations are created (started) and when asynchronous execution finishes (completed or failed).

## Task Goal

Integrate `OperationEventRepository` into the application workflow to save `STARTED`, `COMPLETED`, and `FAILED` events for all media conversion and download operations, verified with comprehensive unit and integration tests.

## Confirmed Scope

- Inject `OperationEventRepository` into `OperationService`, `BackgroundConversionExecutor`, and `BackgroundDownloadExecutor`.
- Save `STARTED` events in `OperationService` when operations are created (both conversion and URL download flows).
- Save `COMPLETED` events when background conversion or download finishes successfully.
- Save `FAILED` events when background conversion or download fails, capturing the exception message in the `failureReason` field.
- Update `OperationServiceTest.java` and `BackgroundDownloadExecutorTest.java` to verify all event saving logic.

## Out of Scope

- Exposing event logs through public REST API endpoints (not required by MVP).
- Generating periodic metrics reports or dashboards.

## Requirements Covered

| Requirement | Source | How This Task Covers It | Status |
| --- | --- | --- | --- |
| Success Metrics | PRD | Persists completed events for successful operations. | Confirmed |
| Reliability tracking | Project Planning | Persists failed events containing details of execution failures. | Confirmed |

## Technical Specification Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Metrics/Observability | Full | Yes | Implements operation success/failure persistence. |

Coverage assessment:

- Justifying Tech Spec section: N/A (Greenfield/Tech Spec is currently empty, but planning context is used)
- Tech Spec sections implemented by this task: N/A
- Gaps between task and Tech Spec: Tech Spec is empty; implemented using project-planning.md context and user decisions.
- Dependencies not specified by the Tech Spec: None.

## Architecture and ADR Considerations

| Decision or ADR | Source | Impact on This Task | Status |
| --- | --- | --- | --- |
| ADR-005 | `docs/adrs/005-use-shared-operation-domain-model-for-metadata-persistence.md` | Focuses task scope purely on events, keeping it decoupled from domain entities. | Confirmed |
| Event taxonomy and schema | `005-create-operation-events-model-architecture-decisions.md` | Establishes the use of `STARTED`, `COMPLETED`, `FAILED` events. | Confirmed |

ADR candidates or architecture decisions needed:

- None.

Architecture decision notes:

- Saved separately: Yes
- Path: `docs/architecture/task-decisions/mvp-media-utility/020-add-operation-success-and-failure-tracking-architecture-decisions.md`
- Notes file status: New

## Confirmed Decisions

- Persist the `STARTED` event synchronously inside the `OperationService` during the creation step (Option A).
- Use the exact same timestamps for `OperationEvent.occurredAt` as used for `Operation.createdAt` / `Operation.completedAt`.
- Save `FAILED` events inside background task catch blocks to ensure errors are captured even on uncaught exceptions.

## Pending Decisions

None. All task-relevant decisions have been answered or explicitly deferred out of scope by the user.

## Questions for the User

None. All task-relevant questions have been answered.

## Proposed Implementation Approach

1. In `OperationService`:
   - Inject `OperationEventRepository`.
   - Call `operationEventRepository.save(OperationEvent.started(operation, createdAt))` immediately after saving a new operation in both `createConversion` and `createDownload`.
2. In `BackgroundConversionExecutor`:
   - Inject `OperationEventRepository`.
   - In the `try` block, after calling `operationRepository.save(operation)` for successful completion, call `operationEventRepository.save(OperationEvent.completed(operation, completedAt))`.
   - In the `catch` block, after saving the failed operation, call `operationEventRepository.save(OperationEvent.failed(operation, completedAt, reason))`.
3. In `BackgroundDownloadExecutor`:
   - Inject `OperationEventRepository`.
   - In the `try` block, after calling `operationRepository.save(operation)` for successful completion, call `operationEventRepository.save(OperationEvent.completed(operation, completedAt))`.
   - In the `catch` block, after saving the failed operation, call `operationEventRepository.save(OperationEvent.failed(operation, completedAt, reason))`.
4. In test classes:
   - Mock `OperationEventRepository` and verify event persistence assertions.

## Files and Areas Expected to Change

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasdourado/mediautility/api/OperationService.java` | Modify | Planning | Inject repository, save `STARTED` events. |
| `src/main/java/com/lucasdourado/mediautility/api/BackgroundConversionExecutor.java` | Modify | Planning | Inject repository, save `COMPLETED`/`FAILED` events. |
| `src/main/java/com/lucasdourado/mediautility/api/BackgroundDownloadExecutor.java` | Modify | Planning | Inject repository, save `COMPLETED`/`FAILED` events. |
| `src/test/java/com/lucasdourado/mediautility/api/OperationServiceTest.java` | Modify | Validation need | Verify events for creation and background conversion. |
| `src/test/java/com/lucasdourado/mediautility/api/BackgroundDownloadExecutorTest.java` | Modify | Validation need | Verify events for background download. |

## Step-by-Step Implementation Plan

1. **Inject Repositories:** Modify constructors and fields of `OperationService`, `BackgroundConversionExecutor`, and `BackgroundDownloadExecutor` to accept `OperationEventRepository`.
2. **Implement STARTED Event Emission:** Edit `OperationService`'s `createConversion` and `createDownload` methods to save `OperationEvent.started(operation, createdAt)`.
3. **Implement COMPLETED/FAILED Event Emission (Conversion):** Edit `BackgroundConversionExecutor.executeConversion` to persist `completed` or `failed` event matching the operation's completion timestamp and failure details.
4. **Implement COMPLETED/FAILED Event Emission (Download):** Edit `BackgroundDownloadExecutor.executeDownload` to persist `completed` or `failed` event matching the operation's completion timestamp and failure details.
5. **Update Unit Tests:** Mock `OperationEventRepository` in `OperationServiceTest.java` and `BackgroundDownloadExecutorTest.java`. Add verification statements ensuring that the expected type and number of events are persisted with matching timestamps and failure reasons.

## Validation Strategy

- Run `.\mvnw test` to verify unit and integration tests compile and execute cleanly.
- Confirm database updates: `STARTED` event saved at creation, and either `COMPLETED` or `FAILED` saved at the end of the async thread run.

## Tests to Add or Update

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| `OperationServiceTest.CreateConversionTests` | Unit | Assert `STARTED` event is saved on conversion creation. | Verify repository mock call. |
| `OperationServiceTest.CreateDownloadTests` | Unit | Assert `STARTED` event is saved on download creation. | Verify repository mock call. |
| `OperationServiceTest.BackgroundConversionExecutorTests` | Unit | Assert `COMPLETED` and `FAILED` events are saved on execution outcomes. | Verify repository mock call. |
| `BackgroundDownloadExecutorTest` | Unit | Assert `COMPLETED` and `FAILED` events are saved on execution outcomes. | Verify repository mock call. |

## Acceptance Criteria

- [ ] `OperationEventRepository` is integrated into `OperationService`, `BackgroundConversionExecutor`, and `BackgroundDownloadExecutor`.
- [ ] Every initiated operation generates a persistent `STARTED` event record.
- [ ] Every successful operation execution generates a persistent `COMPLETED` event record.
- [ ] Every failed operation execution generates a persistent `FAILED` event record containing the error message.
- [ ] Unit and integration tests verify the creation and attributes of all event types under success and failure scenarios.

## Risks and Edge Cases

- **Asynchronous Execution Failures:** If the database connection drops during background processing, updating the operation status and saving the event might fail. This is mitigated by proper exception handling and the transactional context.

## Rollback or Recovery Notes

- Since this task adds event persistence logs but does not alter domain metadata schemas, rollback only requires removing the event repository dependencies and method calls.

## Documentation Updates

- None required.

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

- Ensure to inject and mock the `OperationEventRepository` in all affected constructors.
- Keep the `failureReason` in `FAILED` events aligned with `operation.getFailureReason()`.
