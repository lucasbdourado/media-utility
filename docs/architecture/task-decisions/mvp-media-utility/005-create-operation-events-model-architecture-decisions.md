# Task Architecture Decision Notes: Create Operation Events Model

## Status

Status: Ready for Implementation

Last updated: 2026-06-02

Decision notes file: `docs/architecture/task-decisions/mvp-media-utility/005-create-operation-events-model-architecture-decisions.md`

Task plan file: `docs/task-plans/mvp-media-utility/005-create-operation-events-model-plan.md`

Task file: `docs/tasks/mvp-media-utility/005-create-operation-events-model.md`

## Purpose

Record task-specific architecture decisions, conflicts, and confirmed ADR candidates identified while preparing the task implementation plan.

This file is planning support only. It is not a final ADR and must not replace the ADR workflow.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/tasks/mvp-media-utility/005-create-operation-events-model.md` | Not available | Documented limitation | File exists but is empty. |
| Project planning | `docs/planning/project-planning.md` | Metrics and Observability, task "Define operation event model" | Confirmed by source document | Defines the event model goal and fields at planning level. |
| ADR-005 | `docs/adrs/005-use-shared-operation-domain-model-for-metadata-persistence.md` | Consequences | Accepted | Confirms operation events belong to the dedicated event model task, not task 004. |
| Task 004 plan | `docs/task-plans/mvp-media-utility/004-create-operation-domain-model-plan.md` | Architecture and ADR Considerations | Confirmed by source document | Confirms operation events are separate from the operation domain model. |
| Task 004 execution report | `docs/task-executions/mvp-media-utility/004-create-operation-domain-model-execution.md` | Implemented Changes | Confirmed by source document | Confirms the current shared `Operation` model and repository baseline. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/operations/`, `src/main/java/com/lucasdourado/mediautility/persistence/` | Existing backend model | Detected in codebase | `Operation` is a JPA entity and `OperationRepository` uses Spring Data JPA. |
| User decision | Current `plan-task` session | Empty task handling | Confirmed by user | User chose to plan from the planning document and adjacent context. |
| User decision | Current `plan-task` session | Event model scope | Confirmed by user | Create a persistent JPA event model linked to `Operation`; no instrumentation yet. |
| User decision | Current `plan-task` session | Event taxonomy | Confirmed by user | Use `STARTED`, `COMPLETED`, and `FAILED`. |
| User decision | Current `plan-task` session | Event field shape | Confirmed by user | Use relation plus operation type snapshot. |

## Confirmed Architecture Decisions

| Decision | Source | Impact on This Task | Notes |
| --- | --- | --- | --- |
| Use a persistent JPA operation event model | User decision | Task 005 should create entity/repository-level persistence readiness. | This is model work only, not instrumentation. |
| Link operation events to existing `Operation` | User decision, current codebase | Event records should relate to the task 004 operation lifecycle model. | The event must not replace `Operation` as lifecycle authority. |
| Store operation type snapshot on each event | User decision | Enables reporting by operation type without relying only on joins or future lifecycle state. | Later emitters must keep snapshot consistent with the related operation. |
| Use `STARTED`, `COMPLETED`, and `FAILED` event taxonomy | User decision, project planning | Defines the first event model contract for later tracking tasks. | This intentionally differs from `OperationStatus.PENDING`/`PROCESSING`. |
| Keep event emission out of task 005 | User decision, project planning sequence | Later tracking tasks will write events after operation handlers exist. | Prevents premature integration behavior. |
| Avoid unnecessary user media data in events | Project planning | Event records should only store operational metadata. | Do not store URLs, media content, raw paths, or client-identifying data. |

## Pending Architecture Decisions

None. All task-relevant architecture decisions have been answered or explicitly accepted as source limitations by the user.

## Architecture Conflicts

| Conflict | Sources | Impact | Resolution Status |
| --- | --- | --- | --- |
| The selected task file, Tech Spec, PRD, and technology definition are empty, but planning needs event model scope. | Current filesystem vs Harness source expectations | Could block planning if treated as missing confirmed scope. | Resolved by user decision to plan from project planning and adjacent task context. |
| Existing `OperationStatus` uses `PENDING` and `PROCESSING`, while event taxonomy uses `STARTED`. | Current codebase vs user-confirmed event taxonomy | Implementer must not simply mirror `OperationStatus`. | Resolved by user decision to use `STARTED`, `COMPLETED`, `FAILED`. |
| Relation plus snapshot stores operation type twice. | User decision vs normalization concern | Later emitters must keep event snapshot consistent with related `Operation`. | Accepted as intentional for reporting readiness. |

## ADR Candidates

| Candidate | Trigger | Suggested ADR Scope | Blocking for This Task? |
| --- | --- | --- | --- |
| Operation event persistence model | Task creates persistent event model and relation to `Operation`. | Could be formalized later if event sourcing, audit guarantees, or analytics storage become broader architecture concerns. | No; user chose not to require an ADR before implementation. |

## Implementation Impact

- Task 005 can proceed without a formal ADR.
- The implementation should add a JPA event entity and repository boundary, not just enums/value objects.
- Events should reference `Operation` and store operation type snapshot, event type, timestamp, and optional failure reason.
- The event taxonomy is `STARTED`, `COMPLETED`, and `FAILED`.
- Event records are not the source of truth for operation lifecycle transitions.
- Runtime event emission and success/failure tracking are explicitly deferred to later tasks.

## Questions for the User

None. All task-relevant architecture questions have been answered.

## Notes for the Implementing Agent

- This file is planning support only; it is not a final ADR.
- Use the task plan as the implementation handoff.
- Preserve the existing task 004 `Operation` model and repository unless minimal relationship mapping requires a compatible adjustment.
- Do not add event emitters, handlers, listeners, services, endpoints, dashboards, or API contracts during task 005.
- If implementing the relation plus snapshot reveals a need for broader audit/event sourcing decisions, stop and record a follow-up architecture blocker instead of expanding scope.
