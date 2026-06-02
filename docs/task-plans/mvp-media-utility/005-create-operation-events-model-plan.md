# Task Implementation Plan: Create Operation Events Model

## Status

Status: Ready for Implementation

Last updated: 2026-06-02

Plan file: `docs/task-plans/mvp-media-utility/005-create-operation-events-model-plan.md`

Architecture decision notes file: `docs/architecture/task-decisions/mvp-media-utility/005-create-operation-events-model-architecture-decisions.md`

## Task Reference

Task ID: `MVP-MEDIA-005`

Task file: `docs/tasks/mvp-media-utility/005-create-operation-events-model.md`

Task status: `Documented limitation: task file is empty`

Task group or feature: `mvp-media-utility`

## Planning Mode Requirement

Plan mode verified: Yes

Notes:

- This plan was created in plan mode through the `plan-task` workflow.
- Implementation must not start during `plan-task`.
- A future implementation request must use this saved plan and the saved architecture decision notes as source context.
- The selected task file is empty in the current workspace; the user explicitly chose to plan from `docs/planning/project-planning.md` and adjacent task context.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project discovery | `docs/context/project-discover.md` | Project Scenario | Confirmed by source document | Required Harness discovery context exists. |
| Task file | `docs/tasks/mvp-media-utility/005-create-operation-events-model.md` | Not available | Documented limitation | File exists but is empty; user chose to plan from planning document and adjacent context. |
| Project planning | `docs/planning/project-planning.md` | Metrics and Observability, task "Define operation event model" | Confirmed by source document | Defines the task goal, description, expected output, dependency on technology definition, and validation. |
| Tech Spec | `docs/specs/tech-spec.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |
| PRD | `docs/product/prd.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |
| Technology definition | `docs/architecture/technology-definition.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |
| ADR-001 | `docs/adrs/001-use-java-and-spring-boot-modular-monolith.md` | Decision | Accepted | Confirms Java 21 Spring Boot modular monolith. |
| ADR-003 | `docs/adrs/003-use-maven-npm-coordinated-asset-packaging.md` | Decision | Accepted | Confirms Maven/npm coordinated packaging. |
| ADR-005 | `docs/adrs/005-use-shared-operation-domain-model-for-metadata-persistence.md` | Consequences | Accepted | States operation events are out of scope for task 004 and belong to the dedicated event model task. |
| Task 004 plan | `docs/task-plans/mvp-media-utility/004-create-operation-domain-model-plan.md` | Out of Scope, Architecture Considerations, Notes | Confirmed by source document | Confirms operation events are owned by task 005. |
| Task 004 execution report | `docs/task-executions/mvp-media-utility/004-create-operation-domain-model-execution.md` | Implemented Changes, Risks and Follow-ups | Confirmed by source document | Confirms the current `Operation` model and repository were implemented and no event model exists yet. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/operations/Operation.java`, `OperationType.java`, `OperationStatus.java`, `ResultFileMetadata.java`, `persistence/OperationRepository.java` | Existing backend model | Detected in codebase | Existing shared `Operation` JPA entity has type, status, timestamps, expiration, failure reason, and result metadata. |
| User decision | Current `plan-task` session | Empty task handling | Confirmed by user | User chose to plan from the planning document instead of saving a blocked snapshot or stopping for task-file restore. |
| User decision | Current `plan-task` session | Event model scope | Confirmed by user | Task 005 should create a persistent JPA event entity/repository linked to existing `Operation`; no instrumentation yet. |
| User decision | Current `plan-task` session | Event taxonomy | Confirmed by user | Event types are `STARTED`, `COMPLETED`, and `FAILED`. |
| User decision | Current `plan-task` session | Event fields | Confirmed by user | Store `Operation` relation, operation type snapshot, event type, timestamp, and optional failure reason. |

## Context Summary

Task 004 created the shared persistent `Operation` model for conversion and URL download lifecycle metadata. Task 005 defines the dedicated operation event model that later tracking tasks will use to record successful and failed operations.

The selected task file and several higher-level source documents are empty in the current workspace. The user explicitly chose to proceed from the project planning document and adjacent task context. The event model should be persistent through JPA/MySQL, linked to the existing `Operation`, and kept separate from event emission or endpoint behavior.

## Task Goal

Create the persistent operation event model used by later metrics instrumentation to represent started, completed, and failed media utility operations.

## Confirmed Scope

- Create an operation event type model with `STARTED`, `COMPLETED`, and `FAILED`.
- Create a JPA-backed operation event model linked to the existing `Operation`.
- Store an operation type snapshot on each event.
- Store the event timestamp.
- Store an optional failure reason for failed operation events.
- Add repository or persistence boundary needed to persist operation events.
- Add focused tests for event model fields, mapping, and relationship to `Operation`.

## Out of Scope

- Do not emit or record operation events from real conversion or URL download flows.
- Do not implement success tracking instrumentation; task 020 owns successful operation tracking.
- Do not implement failure tracking instrumentation; task 020 or later failure-tracking scope owns failed operation tracking.
- Do not implement endpoints, API DTOs, process execution, media conversion, URL downloading, temporary storage, cleanup jobs, frontend UI, or reporting dashboards.
- Do not change the existing `Operation` lifecycle behavior unless a minimal relationship mapping requires it.
- Do not update PRD, Tech Spec, technology definition, final ADR files, or task files during implementation.

## Requirements Covered

| Requirement | Source | How This Task Covers It | Status |
| --- | --- | --- | --- |
| Define what counts as completed, failed, and started operations | Project planning, user decision | Event type taxonomy is `STARTED`, `COMPLETED`, `FAILED`. | Confirmed |
| Specify event fields for operation type, status, timestamp, and failure reason | Project planning, user decision | Event model stores operation type snapshot, event type, timestamp, and optional failure reason. | Confirmed |
| Prepare tracking for successful and failed operations | Project planning | Provides the persistent model later instrumentation tasks can write to. | Partial; emission is out of scope |
| Avoid storing unnecessary user media data | Project planning, ADR-005 | Event model stores operation/event metadata only, not uploaded file contents, URLs, raw paths, or user media payloads. | Confirmed |

## Technical Specification Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Data Model / Metrics | Missing | Task proceeds from project planning, adjacent task context, codebase evidence, and user decisions. | `docs/specs/tech-spec.md` is empty. |
| Persistence | Missing | JPA persistence approach follows existing `Operation` model and repository baseline. | No restored Tech Spec text is available. |
| Validation | Missing | Tests are defined by this plan. | No Tech Spec validation section is available. |

Coverage assessment:

- Justifying Tech Spec section: unavailable because `docs/specs/tech-spec.md` is empty.
- Tech Spec sections implemented by this task: operation event model and persistence are implemented from planning context and user decisions.
- Gaps between task and Tech Spec: unknown until the Tech Spec is restored or recreated.
- Dependencies not specified by the Tech Spec: exact event taxonomy, persistence scope, and event field shape were confirmed by the user during this planning session.

## Architecture and ADR Considerations

| Decision or ADR | Source | Impact on This Task | Status |
| --- | --- | --- | --- |
| Java 21 Spring Boot modular monolith | ADR-001 | Event model should live inside the existing backend package structure. | Accepted |
| Maven/npm coordinated asset packaging | ADR-003 | Backend model changes must preserve Maven build wiring. | Accepted |
| Shared persistent `Operation` model | ADR-005, current codebase | Event model should relate to the existing `Operation` entity rather than duplicate operation lifecycle state. | Accepted |
| Operation events are separate from operation domain model | ADR-005, task 004 plan | Task 005 owns the event model and must keep event emission separate from task 004 lifecycle behavior. | Confirmed |
| Persistent JPA event model linked to `Operation` | User decision | Task 005 should create a persistence-ready event entity/repository, not only domain enums. | Resolved by user |
| Relation plus operation type snapshot | User decision | Each event should store a JPA relation to `Operation` and a snapshot of operation type for reporting. | Resolved by user |
| Empty task and source documents | Current filesystem, user decision | Proceed as a documented limitation, not a blocker, for task 005 planning. | Resolved by user |

ADR candidates or architecture decisions needed:

- None required before implementation.
- A task-specific architecture decision notes file should be saved because the task depends on explicit user decisions about event persistence scope, event taxonomy, and event field shape.

Architecture decision notes:

- Saved separately: Yes
- Path: `docs/architecture/task-decisions/mvp-media-utility/005-create-operation-events-model-architecture-decisions.md`
- Notes file status: New

## Confirmed Decisions

- The empty task 005 file is a documented limitation; planning proceeds from project planning and adjacent context.
- Task 005 should create a persistent JPA event model linked to the existing `Operation`.
- Task 005 should not implement event emission or tracking instrumentation.
- Event types are `STARTED`, `COMPLETED`, and `FAILED`.
- Each event stores an `Operation` relation, operation type snapshot, event type, timestamp, and optional failure reason.
- Event records must avoid storing unnecessary user media data.
- Current `Operation` model from task 004 is the existing lifecycle anchor for event records.
- No formal ADR is required before implementing this task.

## Pending Decisions

None. All task-relevant decisions have been answered or explicitly accepted as source limitations by the user.

## Questions for the User

None. All task-relevant questions have been answered.

## Proposed Implementation Approach

Implementation should create the event model as a small persistence-ready addition beside the existing operation model:

1. Re-read this plan, the architecture decision notes, project planning, ADR-005, task 004 execution report, and current operation model.
2. Inspect existing `operations` and `persistence` package conventions.
3. Add an event type enum for `STARTED`, `COMPLETED`, and `FAILED`.
4. Add a JPA-backed operation event model linked to `Operation`.
5. Include operation type snapshot, event type, event timestamp, and optional failure reason fields.
6. Add a repository or persistence boundary for operation event records.
7. Add focused tests for event construction, field retention, and JPA mapping.
8. Keep event emission, endpoint behavior, processing integrations, and reporting out of scope.

## Files and Areas Expected to Change

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasdourado/mediautility/operations/` | Create / Modify | User decisions, current codebase | Add operation event domain/persistence model types near existing operation concepts. |
| `src/main/java/com/lucasdourado/mediautility/persistence/` | Create / Modify | User decision, current codebase | Add repository boundary for event persistence if following existing repository package pattern. |
| `src/test/java/com/lucasdourado/mediautility/operations/` | Create / Modify | Validation strategy | Add event model tests and mapping tests. |
| `src/test/java/com/lucasdourado/mediautility/persistence/` | Inspect / Create if needed | Validation strategy | Use only if repository-focused tests fit existing conventions. |

## Step-by-Step Implementation Plan

1. Verify the task 005 plan and architecture decision notes are saved and marked `Ready for Implementation`.
2. Inspect existing operation model classes and persistence repository conventions.
3. Add `OperationEventType` or equivalent enum with `STARTED`, `COMPLETED`, and `FAILED`.
4. Add an `OperationEvent` JPA entity mapped to an operation events table.
5. Model the relationship from `OperationEvent` to `Operation` using an appropriate JPA association.
6. Add fields for operation type snapshot, event type, occurred timestamp, and optional failure reason.
7. Avoid adding URL, uploaded file names, raw filesystem paths, media payload data, or client-identifying information to the event model.
8. Add an event repository boundary following the existing `OperationRepository` pattern.
9. Add unit tests for event creation and field behavior.
10. Add reflection or mapping tests for JPA annotations, relationship, enum mappings, timestamp, and failure reason.
11. Run Maven test validation using the project's working Maven command; if `.\mvnw.cmd test` still fails before Maven startup, use the documented IntelliJ Maven command and record the wrapper limitation in the execution report.
12. Perform a scope review confirming no event emission, endpoints, process execution, media/storage behavior, frontend UI, or reporting was added.

## Validation Strategy

- Run backend test validation through Maven.
- Verify the event type enum contains `STARTED`, `COMPLETED`, and `FAILED`.
- Verify the event model stores an `Operation` relation, operation type snapshot, event type, timestamp, and optional failure reason.
- Verify failed events can carry a failure reason and started/completed events do not require one.
- Verify the JPA mapping is persistence-ready without requiring a live MySQL instance unless existing test infrastructure supports one.
- Verify no unnecessary user media data is stored in event records.
- Verify no event emission or tracking instrumentation was implemented in this task.

## Tests to Add or Update

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| Operation event model tests | Unit | Verify event creation and field retention for started, completed, and failed events. | Should cover optional failure reason behavior. |
| Operation event persistence mapping tests | Unit / Reflection | Verify entity/table mapping, operation relation, enum mappings, timestamp, and failure reason mapping. | Mirrors task 004 mapping-test style if no DB test infrastructure exists. |
| Event repository boundary compile check | Build validation | Verify repository boundary compiles with Spring Data JPA. | Repository behavior can remain untested against live DB if infrastructure is unavailable. |
| Scope review | Manual / Static search | Confirm no event emission, endpoint, process, media, storage, frontend, or reporting behavior was added. | Required because later tasks own instrumentation. |
| Maven test run | Build validation | Verify project compiles and existing tests still pass. | Use wrapper first; document fallback if wrapper remains broken. |

## Acceptance Criteria

- [ ] Event type model represents `STARTED`, `COMPLETED`, and `FAILED`.
- [ ] Persistent operation event model is linked to the existing `Operation`.
- [ ] Operation event metadata includes operation type snapshot, event type, timestamp, and optional failure reason.
- [ ] Operation event repository or persistence boundary exists.
- [ ] Event records do not store unnecessary user media data.
- [ ] Implementation does not add event emission, endpoint behavior, media processing, storage behavior, cleanup, frontend UI, or reporting.

## Risks and Edge Cases

- The selected task file and Tech Spec are empty, so future restored documentation may require plan revision.
- Event model must not duplicate or mutate the authoritative `Operation` lifecycle state.
- Storing both relation and operation type snapshot creates denormalized data; later emitters must keep the snapshot consistent with the related operation type.
- Adding event persistence before instrumentation means runtime write behavior remains for later tasks.
- Failure reason length and sanitization are not specified by source documents; implementation should keep the field bounded and avoid media/user payload data.

## Rollback or Recovery Notes

Rollback would remove the operation event enum/entity, event repository boundary, and task-specific tests while preserving the existing `Operation` model, `OperationRepository`, ADR-005, and task 004 artifacts.

## Documentation Updates

- Do not update PRD, Tech Spec, technology definition, final ADR files, or task files during task 005 implementation.
- The future execution report should document event model fields, persistence mapping, validation evidence, and any deferred runtime repository validation.

## Implementation Readiness Checklist

- [x] Task scope is clear enough from project planning, adjacent task context, codebase evidence, and user decisions.
- [x] Source documents were reviewed.
- [x] Tech Spec coverage was verified as unavailable and accepted as a documented limitation.
- [x] Architecture decisions were checked.
- [x] ADR candidates were confirmed, rejected, required-before-implementation, or explicitly deferred by the user.
- [x] Pending decisions are resolved or explicitly accepted as source limitations by the user.
- [x] User questions were answered.
- [x] Expected files or areas are identified.
- [x] Acceptance criteria are defined.
- [x] Test strategy is defined.
- [x] Risks and edge cases are documented.

## Notes for the Implementing Agent

- Treat the saved user decisions in this plan as binding for task 005.
- Keep operation events as a model/persistence concern only; later tasks own actual event emission.
- Link events to `Operation`, but do not let events become the authoritative lifecycle state.
- Use `STARTED`, `COMPLETED`, and `FAILED` for the event taxonomy even though the existing `OperationStatus` enum uses `PENDING`, `PROCESSING`, `COMPLETED`, and `FAILED`.
- Do not store URLs, raw file paths, uploaded filenames beyond existing result metadata, media content, or client-identifying data in operation events.
