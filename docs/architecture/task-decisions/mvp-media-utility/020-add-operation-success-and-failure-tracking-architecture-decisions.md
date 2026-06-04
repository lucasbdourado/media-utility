# Task Architecture Decision Notes: Add Operation Success and Failure Tracking

## Status

Status: Ready for Implementation

Last updated: 2026-06-03

Decision notes file: `docs/architecture/task-decisions/mvp-media-utility/020-add-operation-success-and-failure-tracking-architecture-decisions.md`

Task plan file: `docs/task-plans/mvp-media-utility/020-add-operation-success-and-failure-tracking-plan.md`

Task file: `docs/tasks/mvp-media-utility/020-add-operation-success-and-failure-tracking.md`

## Purpose

Record task-specific architecture decisions, conflicts, and confirmed ADR candidates identified while preparing the task implementation plan.

This file is planning support only. It is not a final ADR and must not replace the ADR workflow.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project planning | `docs/planning/project-planning.md` | Task Group: Metrics and Observability, Tasks: Track successful operations, Track failed operations | Confirmed by source document | Defines goals and validation expectations for tracking successful and failed operations. |
| Task 005 decision notes | `docs/architecture/task-decisions/mvp-media-utility/005-create-operation-events-model-architecture-decisions.md` | Confirmed Architecture Decisions | Confirmed by task context | Establishes taxonomy (`STARTED`, `COMPLETED`, `FAILED`) and relation with type snapshot. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/operations/OperationEvent.java` | Entire class | Detected in codebase | Declares factories for started, completed, and failed events. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/persistence/OperationEventRepository.java` | Interface contract | Detected in codebase | Persistent interface for operation event storage. |
| User decision | Current `plan-task` session | Event timing decision | Confirmed by user | User chose Option A: Save STARTED event in OperationService immediately upon creation. |

## Confirmed Architecture Decisions

| Decision | Source | Impact on This Task | Notes |
| --- | --- | --- | --- |
| Save STARTED event synchronously during operation instantiation | User decision | Persisted inside `OperationService` immediately before returning the pending response. | Ensures all initial user requests are captured, even if background scheduling or processing experiences delay or failure. |
| Decouple events from background executor exceptions | Tech Spec / Good practice | Background executor catch-blocks must save the `FAILED` event before finally block executes. | Guaranteed failure capture, preventing silent crashes without metrics update. |
| Shared database persistence mapping | ADR-005 | Events and Operations share the JPA persistence baseline. | Repository-based persistence is synchronous and transactionally direct. |

## Pending Architecture Decisions

None. All task-relevant architecture decisions have been answered or explicitly deferred out of scope by the user.

## Architecture Conflicts

None.

## ADR Candidates

None.

## Implementation Impact

- Event repository `OperationEventRepository` needs to be added as a dependency to class constructors in the `com.lucasdourado.mediautility.api` package.
- Unit testing mocks must include repository mappings for `OperationEventRepository` to prevent `NullPointerException` during mock runs.

## Questions for the User

None. All task-relevant architecture questions have been answered.

## Notes for the Implementing Agent

- When creating events, use the static factory methods defined in `OperationEvent`: `OperationEvent.started`, `OperationEvent.completed`, and `OperationEvent.failed`.
- The timestamp used for `OperationEvent.occurredAt` must exactly match the timestamp of the operation state update (`createdAt` or `completedAt`).
