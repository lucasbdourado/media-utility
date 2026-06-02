# Task Architecture Decision Notes: Create Operation Domain Model

## Status

Status: Ready for Implementation

Last updated: 2026-06-02

Decision notes file: `docs/architecture/task-decisions/mvp-media-utility/004-create-operation-domain-model-architecture-decisions.md`

Task plan file: `docs/task-plans/mvp-media-utility/004-create-operation-domain-model-plan.md`

Task file: `docs/tasks/mvp-media-utility/004-create-operation-domain-model.md`

## Purpose

Record task-specific architecture decisions, conflicts, and confirmed ADR candidates identified while preparing the task implementation plan.

This file is planning support only. It is not a final ADR and must not replace the ADR workflow.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/tasks/mvp-media-utility/004-create-operation-domain-model.md` | Scope and implementation instructions | Confirmed by source document | Defines intended operation/result metadata model scope. |
| Tech Spec | `docs/specs/tech-spec.md` | Data Model | Documented limitation | File exists but is empty; user accepted this as non-blocking for task 004 after ADR-005. |
| PRD | `docs/product/prd.md` | Functional Requirements | Documented limitation | File exists but is empty; user accepted this as non-blocking for task 004 after ADR-005. |
| Technology definition | `docs/architecture/technology-definition.md` | Confirmed Technology Decisions | Documented limitation | File exists but is empty; user accepted this as non-blocking for task 004 after ADR-005. |
| ADR-001 | `docs/adrs/001-use-java-and-spring-boot-modular-monolith.md` | Decision | Accepted | Confirms monolith architecture. |
| ADR-003 | `docs/adrs/003-use-maven-npm-coordinated-asset-packaging.md` | Decision | Accepted | Confirms build packaging constraints. |
| ADR-004 | `docs/adrs/004-use-mysql-with-jpa-for-operation-metadata-persistence.md` | Decision | Documented limitation | File exists but is empty; task 004 is unblocked by ADR-005 instead. |
| ADR-005 | `docs/adrs/005-use-shared-operation-domain-model-for-metadata-persistence.md` | Decision, Consequences, Task Impact | Accepted / Resolved by ADR | Resolves task 004's shared persistent operation metadata model blocker. |
| Current codebase | `pom.xml`, `src/main/resources/application.properties`, backend packages | Detected state | Detected in codebase | JPA/MySQL baseline appears present. |
| User decision | Current `resolve-architecture-blocker` session | Source handling, ADR status, decision, alternatives, consequences | Confirmed by user | User accepted ADR-005 and chose `Ready for Implementation` after saving the unblock artifacts. |

## Confirmed Architecture Decisions

| Decision | Source | Impact on This Task | Notes |
| --- | --- | --- | --- |
| Use Java 21 Spring Boot modular monolith | ADR-001 | Operation model should live inside the existing monolith. | Accepted. |
| Keep Maven/npm coordinated packaging | ADR-003 | Backend domain/persistence changes must preserve existing build wiring. | Accepted. |
| Use one shared persistent `Operation` domain model | ADR-005 | Task 004 can implement common operation metadata for conversion and URL download. | Accepted only when `Operation` remains a real shared domain concept. |
| Keep raw filesystem paths server-side only | Task file, ADR-005 | Future model/API separation must avoid exposing internal paths through public contracts. | Confirmed. |
| Do not add operation events in task 004 | Task file, ADR-005 | Event model remains owned by task 005. | Confirmed. |
| Re-evaluate if operation flows diverge | ADR-005 | Future tasks must revisit the shared model if conversion and download need materially different lifecycle rules. | Confirmed. |

## Pending Architecture Decisions

None. All task-relevant architecture decisions are resolved by ADR-005 or explicitly accepted as non-blocking limitations by the user.

## Architecture Conflicts

| Conflict | Sources | Impact | Resolution Status |
| --- | --- | --- | --- |
| Task file cites Tech Spec, PRD, technology definition, and ADR-004, but those files are empty in the current workspace. | Task file vs current filesystem state | Earlier blocked task 004 planning. | Resolved for task 004 by user decision and ADR-005; remains a documented source limitation. |
| Codebase contains JPA/MySQL configuration, but ADR-004 content is unavailable. | Current codebase vs empty ADR-004 | Earlier persistence architecture verification gap. | Non-blocking for task 004 because ADR-005 is the resolving ADR for operation metadata model scope. |

## ADR Candidates

| Candidate | Trigger | Suggested ADR Scope | Blocking for This Task? |
| --- | --- | --- | --- |
| Operation metadata domain and persistence model | Task 004 defines long-lived persistent operation/result metadata | Resolved by ADR-005: shared persistent `Operation` model, domain-concept limitation, alternatives, and consequences. | No |

## Implementation Impact

- Task 004 can proceed after saving ADR-005 and this updated handoff.
- The implementation must preserve `Operation` as a real common domain concept with shared lifecycle semantics.
- The implementation may create JPA/MySQL-backed operation metadata mapping and repository boundaries as needed.
- Raw filesystem paths must remain server-side and must not be exposed through public API contracts.
- Operation events remain out of scope for task 004.
- If future work discovers divergent conversion/download rules, invariants, states, or lifecycles, the shared model decision must be revisited.

## Questions for the User

None. All task-relevant architecture questions were answered during the `resolve-architecture-blocker` session.

## Notes for the Implementing Agent

- ADR-005 is the binding architecture source for task 004.
- This file is planning support only; use ADR-005 for the formal decision.
- Do not implement task 004 unless the task plan remains `Ready for Implementation`.
- Re-check the empty source documents before implementation, but treat them as documented limitations rather than blockers for this task.
