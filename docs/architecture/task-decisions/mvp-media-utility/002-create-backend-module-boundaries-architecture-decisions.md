# Task Architecture Decision Notes: Create Backend Module Boundaries

## Status

Status: Ready for Implementation

Last updated: 2026-06-02

Decision notes file: `docs/architecture/task-decisions/mvp-media-utility/002-create-backend-module-boundaries-architecture-decisions.md`

Task plan file: `docs/task-plans/mvp-media-utility/002-create-backend-module-boundaries-plan.md`

Task file: `docs/tasks/mvp-media-utility/002-create-backend-module-boundaries.md`

## Purpose

Record task-specific architecture decisions, conflicts, and confirmed ADR candidates identified while preparing the task implementation plan.

This file is planning support only. It is not a final ADR and must not replace the ADR workflow.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/tasks/mvp-media-utility/002-create-backend-module-boundaries.md` | Scope, Implementation Instructions | Confirmed by source document | Requires clear backend package/module boundaries. |
| Tech Spec | `docs/specs/tech-spec.md` | Modules and Responsibilities | Confirmed by source document | Defines backend responsibility areas. |
| ADR-001 | `docs/adrs/001-use-java-and-spring-boot-modular-monolith.md` | Decision, Consequences | Accepted | Confirms internal module boundaries inside one Spring Boot app. |
| Task execution report 001 | `docs/task-executions/mvp-media-utility/001-scaffold-spring-boot-react-monolith-execution.md` | Implemented Changes | Confirmed by source document | Confirms base package `com.lucasdourado.mediautility`. |
| User decision | Current `plan-task` session | Boundary implementation style | Confirmed by user | Use interfaces to materialize boundaries, but keep them method-free. |
| User decision | Current `plan-task` session | Scope conflict resolution | Confirmed by user | Keep complete contracts out of task 002. |
| User decision | Current `plan-task` session | Package layout | Confirmed by user | Use the direct Tech Spec package layout. |

## Confirmed Architecture Decisions

| Decision | Source | Impact on This Task | Notes |
| --- | --- | --- | --- |
| Backend remains one modular monolith. | ADR-001 | Use Java packages inside the Spring Boot app, not separate services or Maven modules. | No new deployment unit is created. |
| Base package is `com.lucasdourado.mediautility`. | Task 001 execution report and current codebase | All boundaries are created under this base package. | Long-lived package convention from scaffold. |
| Boundary implementation uses no-method interfaces. | User decision | Creates explicit reviewable boundaries without premature service contracts. | Interfaces must not be annotated as Spring beans. |
| Package layout follows the direct Tech Spec responsibility map. | User decision | Creates `api`, `operations`, `media.conversion`, `media.download`, `media.process`, `storage`, `persistence`, `cleanup`, and `observability`. | Keeps package naming aligned with existing documentation. |
| Complete contracts are out of scope for task 002. | User decision | Prevents task 002 from implementing method signatures, DTOs, domain models, repositories, adapters, or API contracts. | Dedicated later tasks own those details. |

## Pending Architecture Decisions

None. All task-relevant architecture decisions have been answered or explicitly deferred out of scope by the user.

## Architecture Conflicts

| Conflict | Sources | Impact | Resolution Status |
| --- | --- | --- | --- |
| User initially selected complete interfaces, but the task file limits scope to boundaries and later tasks own models/contracts/adapters. | User decision, task 002, tasks 004/006/007/008 | Complete contracts would over-expand task 002 and force premature API/domain decisions. | Resolved by user: limit task 002 to boundaries with method-free interfaces. |

## ADR Candidates

| Candidate | Trigger | Suggested ADR Scope | Blocking for This Task? |
| --- | --- | --- | --- |
| None | Existing ADR-001 already covers modular monolith and internal boundaries. | Not applicable | No |

## Implementation Impact

- The future implementation must create structure only.
- Boundary interfaces must be public and method-free.
- `package-info.java` files should document each package responsibility concisely.
- The implementation must not define complete service contracts, domain models, DTOs, repository interfaces, process adapter behavior, cleanup scheduling, or observability behavior.
- This task does not require a new formal ADR.

## Questions for the User

None. All task-relevant architecture questions have been answered.

## Notes for the Implementing Agent

- Use package boundaries as architectural signposts, not as behavior.
- Keep `api` isolated from media processing details.
- Keep external process concerns under `media.process` for the later process adapter task.
- Keep persistence as a boundary only; do not add JPA types in task 002.
- Preserve task 001 build and frontend packaging behavior.
