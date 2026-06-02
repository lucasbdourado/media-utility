# Task Implementation Plan: Create Operation Domain Model

## Status

Status: Ready for Implementation

Last updated: 2026-06-02

Plan file: `docs/task-plans/mvp-media-utility/004-create-operation-domain-model-plan.md`

Architecture decision notes file: `docs/architecture/task-decisions/mvp-media-utility/004-create-operation-domain-model-architecture-decisions.md`

## Task Reference

Task ID: `MVP-MEDIA-004`

Task file: `docs/tasks/mvp-media-utility/004-create-operation-domain-model.md`

Task status: `Depends on Previous Task`

Task group or feature: `mvp-media-utility`

## Planning Mode Requirement

Plan mode verified: Yes

Notes:

- This plan was updated through `resolve-architecture-blocker` in plan mode.
- Implementation must not start during planning or blocker resolution.
- A future implementation request must use this saved plan, the saved architecture decision notes, and ADR-005 as source context.
- ADR-005 resolves the architecture blocker for the shared persistent operation metadata model.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project discovery | `docs/context/project-discover.md` | Project Scenario | Confirmed by source document | Required Harness discovery context exists, but it predates the current implementation codebase. |
| Task file | `docs/tasks/mvp-media-utility/004-create-operation-domain-model.md` | Scope, Out of Scope, Dependencies, Validation, Acceptance Criteria | Confirmed by source document | Defines the operation domain model task scope. |
| Tech Spec | `docs/specs/tech-spec.md` | Data Model | Documented limitation | File exists but is empty in the current workspace; user explicitly accepted this as non-blocking for task 004 after ADR-005. |
| PRD | `docs/product/prd.md` | Functional Requirements | Documented limitation | File exists but is empty in the current workspace; user explicitly accepted this as non-blocking for task 004 after ADR-005. |
| Technology definition | `docs/architecture/technology-definition.md` | Confirmed Technology Decisions | Documented limitation | File exists but is empty in the current workspace; user explicitly accepted this as non-blocking for task 004 after ADR-005. |
| ADR-001 | `docs/adrs/001-use-java-and-spring-boot-modular-monolith.md` | Decision | Accepted | Confirms Java 21 Spring Boot modular monolith. |
| ADR-002 | `docs/adrs/002-use-react-vite-typescript-served-by-spring-boot.md` | Decision | Accepted | Confirms React/Vite/TypeScript served by Spring Boot; relevant only as surrounding architecture. |
| ADR-003 | `docs/adrs/003-use-maven-npm-coordinated-asset-packaging.md` | Decision | Accepted | Confirms Maven/npm coordinated packaging; relevant only as build constraint. |
| ADR-004 | `docs/adrs/004-use-mysql-with-jpa-for-operation-metadata-persistence.md` | Decision | Documented limitation | File exists but is empty; task 004 is unblocked by ADR-005 rather than ADR-004. |
| ADR-005 | `docs/adrs/005-use-shared-operation-domain-model-for-metadata-persistence.md` | Decision, Consequences, Task Impact | Accepted / Resolved by ADR | Accepts a shared persistent `Operation` domain model for task 004, with domain-concept limitations. |
| Task plan 003 | `docs/task-plans/mvp-media-utility/003-configure-mysql-jpa-persistence-baseline-plan.md` | Source Documents, Context Summary, Confirmed Decisions | Secondary evidence only | Describes JPA/MySQL baseline decisions and ADR-004, but the referenced source/ADR files are empty now. |
| Current codebase | `pom.xml`, `src/main/resources/application.properties`, backend package tree | Detected dependencies/configuration | Detected in codebase | Spring Data JPA, MySQL connector, datasource properties, and package boundaries are present. |
| User decision | Current `resolve-architecture-blocker` session | Source handling and ADR acceptance | Confirmed by user | User accepted ADR-005 and chose to make the empty sources documented limitations rather than blockers for task 004. |

## Context Summary

Task 004 creates the core operation and result metadata model used by conversion and URL download flows. The task file says this model must include operation type, lifecycle status, persistent metadata timestamps, failure details, result metadata, `expiresAt`, and server-side-only raw file paths.

The current codebase already has a Spring Boot Java 21 scaffold, backend package boundaries, Spring Data JPA, MySQL driver, and environment-driven datasource properties. The referenced Tech Spec, PRD, technology definition, and ADR-004 are empty in the workspace, which remains a documented source limitation.

ADR-005 resolves the task-specific architecture blocker by accepting a shared persistent `Operation` domain model for task 004. The shared model is valid only if `Operation` remains a real common domain concept with shared lifecycle semantics across conversion and URL download operations.

## Task Goal

Create the shared operation domain model and result metadata model for persistent operation lifecycle tracking.

## Confirmed Scope

- Create an operation type model for conversion and URL download.
- Create operation statuses for lifecycle tracking.
- Create persistent operation metadata with creation, completion, expiration, and failure fields.
- Create result file metadata without exposing raw paths to clients.
- Add JPA repository boundaries as needed.
- Preserve `Operation` as a domain concept with shared lifecycle semantics, not as an anemic technical record.

## Out of Scope

- Do not implement process execution.
- Do not implement endpoint behavior.
- Do not add operation events; task 005 owns the dedicated event model.
- Do not implement media conversion, URL downloading, temporary storage service behavior, cleanup jobs, API contracts, frontend UI, or result download endpoint behavior.
- Do not update PRD, Tech Spec, technology definition, final ADR files, or task files during implementation.

## Requirements Covered

| Requirement | Source | How This Task Covers It | Status |
| --- | --- | --- | --- |
| Track completed and failed operations | Task file, ADR-005 | Shared `Operation` model supports lifecycle tracking for conversion and URL download. | Ready |
| Store result availability expiration | Task file, ADR-005 | Operation/result metadata includes `expiresAt` for successful result availability. | Ready |
| Keep raw file paths server-side only | Task file, ADR-005 | Result metadata may hold internal file location data, while public contracts must not expose raw paths. | Confirmed |
| Persist operation metadata through JPA/MySQL | Task file, detected codebase, ADR-005 | Task may create shared persistent operation metadata backed by JPA/MySQL. | Ready |

## Technical Specification Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Data Model | Unavailable | Task proceeds from task file and ADR-005. | `docs/specs/tech-spec.md` is empty; accepted as non-blocking by user. |
| Integrations / Persistence | Unavailable | Task proceeds from detected JPA/MySQL baseline and ADR-005. | Tech Spec content is unavailable. |
| Implementation Notes | Unavailable | Task proceeds from task file and ADR-005. | No binding source text is available. |

Coverage assessment:

- Justifying Tech Spec section: unavailable because the Tech Spec file is empty.
- Tech Spec sections implemented by this task: operation/result metadata model is implemented from task file and ADR-005.
- Gaps between task and Tech Spec: unknown until the Tech Spec is restored or replaced.
- Dependencies not specified by the Tech Spec: exact model details must stay within task file and ADR-005 constraints.
- Source limitation handling: user explicitly accepted the empty source documents as non-blocking for task 004 after ADR-005.

## Architecture and ADR Considerations

| Decision or ADR | Source | Impact on This Task | Status |
| --- | --- | --- | --- |
| Java 21 Spring Boot modular monolith | ADR-001 | Domain and persistence model should live inside the existing monolith packages. | Accepted |
| Maven/npm coordinated asset packaging | ADR-003 | Backend model changes must preserve Maven build wiring. | Accepted |
| Shared persistent `Operation` model | ADR-005 | Allows task 004 to implement one common operation metadata model for conversion and URL download. | Resolved by ADR |
| Public contract must not expose raw filesystem paths | Task file, ADR-005 | Requires separation between internal result file metadata and future API DTOs/contracts. | Confirmed |
| Operation events are separate from operation domain model | Task file, task 005, ADR-005 | Prevents implementing event model in task 004. | Confirmed |
| Empty Tech Spec/PRD/technology-definition/ADR-004 | User decision, ADR-005 | Remain documented limitations, not blockers for this task. | Resolved by user decision |

ADR candidates or architecture decisions needed:

- None. ADR-005 resolves the task-specific persistent operation metadata model decision.
- No architecture blocker remains for task 004.

Architecture decision notes:

- Saved separately: Yes
- Path: `docs/architecture/task-decisions/mvp-media-utility/004-create-operation-domain-model-architecture-decisions.md`
- Notes file status: Updated

## Confirmed Decisions

- Task 004 should create operation and result metadata models, not endpoint behavior or media processing.
- Operation types must cover conversion and URL download.
- Operation lifecycle status must be represented.
- Operation metadata must include creation, completion, expiration, and failure fields.
- Successful result availability must store `expiresAt`.
- Internal file paths must remain server-side only and must not be exposed through public contracts.
- Operation events are out of scope for task 004.
- The current codebase has Spring Data JPA and MySQL connector dependencies plus datasource configuration.
- ADR-005 accepts one shared persistent `Operation` domain model for this task.
- The shared model is valid only if `Operation` represents real shared lifecycle/domain semantics across both flows.
- If conversion and URL download later need materially different rules, invariants, states, or lifecycles, the shared model decision must be revisited.
- The empty Tech Spec, PRD, technology definition, and ADR-004 files are documented limitations but no longer block this task.

## Pending Decisions

None. All task-relevant architecture blockers are resolved by ADR-005 or explicitly accepted as non-blocking limitations by the user.

## Questions for the User

None. All task-relevant unblock questions were answered during the `resolve-architecture-blocker` session.

## Proposed Implementation Approach

Implementation should proceed from the task file, current codebase, and ADR-005:

1. Re-read the task file, this plan, the architecture decision notes, ADR-001, ADR-003, ADR-005, and current codebase.
2. Inspect the existing `operations` and `persistence` packages created by earlier tasks.
3. Create operation type and status representations that cover conversion and URL download lifecycle tracking.
4. Create a shared `Operation` domain model only around lifecycle and metadata that are genuinely common to both operation flows.
5. Create result metadata that supports successful result availability and keeps raw file paths internal.
6. Add JPA persistence mapping and repository boundaries as needed for operation metadata.
7. Keep operation events, endpoint behavior, process execution, storage service behavior, cleanup, and API contracts out of scope.
8. Add focused tests or mapping validation appropriate for the current test infrastructure.

## Files and Areas Expected to Change

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasdourado/mediautility/operations/` | Create / Modify | Task file, ADR-005 | Domain types, lifecycle status, operation model, and result metadata concepts. |
| `src/main/java/com/lucasdourado/mediautility/persistence/` | Create / Modify | Task file, ADR-005, detected JPA baseline | JPA entity/repository boundaries or mappings as needed for operation metadata. |
| `src/test/java/com/lucasdourado/mediautility/` | Create / Modify | Task validation | Domain tests and persistence mapping/repository tests where infrastructure supports them. |

## Step-by-Step Implementation Plan

1. Verify ADR-005 exists and is accepted before changing application source files.
2. Inspect `operations` and `persistence` packages to preserve existing boundary conventions.
3. Add operation type and operation status representations for conversion and URL download lifecycle tracking.
4. Add the shared operation domain model with creation, completion, expiration, result, and failure metadata fields scoped to common lifecycle semantics.
5. Add result file metadata that can retain internal server-side location data without defining public API exposure.
6. Add JPA entity/repository boundaries or persistence mapping needed to store operation metadata through the existing JPA/MySQL baseline.
7. Confirm task 005 event model remains untouched and no endpoint/process/storage/cleanup behavior was added.
8. Add or update tests for operation lifecycle metadata and persistence mapping/repository behavior where supported.
9. Run `.\mvnw.cmd test` from the repository root.

## Validation Strategy

- Run `.\mvnw.cmd test`.
- Verify operation type and status are represented.
- Verify operation metadata includes creation, completion, expiration, and failure fields.
- Verify result metadata keeps raw filesystem paths internal and does not introduce public API exposure.
- Verify JPA mapping/repository boundaries compile and are test-covered where infrastructure supports it.
- Verify no operation events, endpoint behavior, process execution, storage service behavior, cleanup job, frontend UI, or API contracts were added.

## Tests to Add or Update

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| Operation domain model tests | Unit | Verify operation type/status and lifecycle metadata behavior. | Keep focused on task 004 domain scope. |
| Result metadata tests | Unit | Verify result metadata can carry expiration and internal file location without public-path exposure. | Public API contracts remain for later tasks. |
| JPA mapping/repository tests | Integration / persistence | Verify persistent metadata mapping when current test infrastructure supports it. | If no database test infrastructure exists, document deferred runtime persistence validation in the execution report. |
| Maven test run | Build validation | Verify backend compiles and existing tests still pass. | Future implementation should run `.\mvnw.cmd test`. |
| Scope review | Manual | Confirm operation events and behavior outside task 004 were not implemented. | Required because adjacent tasks own those areas. |

## Acceptance Criteria

- [x] Architecture blocker for the shared operation metadata model is resolved by ADR-005.
- [ ] Operation type and status are represented after implementation.
- [ ] Operation metadata includes creation, completion, expiration, and failure fields after implementation.
- [ ] Result metadata does not expose filesystem paths through public contracts after implementation.
- [ ] Implementation does not add process execution, endpoint behavior, or operation events.

## Risks and Edge Cases

- Empty source documents remain a documentation risk for later workflows.
- A shared model can become too generic if it does not preserve real common domain lifecycle semantics.
- A JPA entity shape chosen too early may constrain API contracts, cleanup, and event tracking tasks.
- Raw path handling must be carefully separated from public API models once API contracts are planned.
- If conversion and URL download later need distinct invariants or lifecycle states, ADR-005 must be revisited.

## Rollback or Recovery Notes

After implementation, rollback would remove the operation domain model, related persistence mappings/repository boundaries, and task-specific tests while preserving the existing scaffold, package boundaries, and JPA baseline.

## Documentation Updates

- Do not update PRD, Tech Spec, technology definition, final ADR files, or task files during task 004 implementation.
- The future task execution report should document implemented model fields, persistence boundaries, validation evidence, and any deferred persistence runtime validation.
- If implementation discovers materially divergent conversion/download lifecycle rules, record that as a follow-up architecture issue rather than expanding task 004 silently.

## Implementation Readiness Checklist

- [x] Task scope is clear from the task file.
- [x] Source documents were reviewed.
- [x] Tech Spec limitation was handled through explicit user decision and ADR-005.
- [x] Architecture decisions were checked.
- [x] ADR candidates were confirmed, rejected, required-before-implementation, or explicitly deferred by the user.
- [x] Pending decisions are resolved or explicitly accepted as non-blocking limitations by the user.
- [x] User questions were answered.
- [x] Expected files or areas are identified.
- [x] Acceptance criteria are defined.
- [x] Test strategy is defined.
- [x] Risks and edge cases are documented.

## Notes for the Implementing Agent

- ADR-005 is the binding source for task 004's shared persistent operation model decision.
- Do not treat `Operation` as a generic technical database row; preserve real common domain lifecycle semantics.
- Do not implement conversion-specific or download-specific behavior unless it is genuinely shared metadata required by the task.
- Do not expose raw filesystem paths through public API contracts.
- Do not add operation events; task 005 owns that model.
- Do not implement endpoint behavior, process execution, storage service behavior, cleanup, frontend UI, or API contracts.
