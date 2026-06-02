# Task Implementation Plan: Create Backend Module Boundaries

## Status

Status: Ready for Implementation

Last updated: 2026-06-02

Plan file: `docs/task-plans/mvp-media-utility/002-create-backend-module-boundaries-plan.md`

Architecture decision notes file: `docs/architecture/task-decisions/mvp-media-utility/002-create-backend-module-boundaries-architecture-decisions.md`

## Task Reference

Task ID: `MVP-MEDIA-002`

Task file: `docs/tasks/mvp-media-utility/002-create-backend-module-boundaries.md`

Task status: `Depends on Previous Task`

Task group or feature: `mvp-media-utility`

## Planning Mode Requirement

Plan mode verified: Yes

Notes:

- This plan was created in plan mode.
- Implementation must not start during `plan-task`.
- A future implementation request must use this saved plan and the saved architecture decision notes as source context.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/tasks/mvp-media-utility/002-create-backend-module-boundaries.md` | Scope, Out of Scope, Acceptance Criteria | Confirmed by source document | Defines package/module boundary task only. |
| Task execution report 001 | `docs/task-executions/mvp-media-utility/001-scaffold-spring-boot-react-monolith-execution.md` | Execution Summary, Implemented Changes | Confirmed by source document | Confirms Spring Boot scaffold exists and uses package `com.lucasdourado.mediautility`. |
| Tech Spec | `docs/specs/tech-spec.md` | Proposed Technical Solution, Modules and Responsibilities, Implementation Notes | Confirmed by source document | Defines API, operation, media, storage, persistence, cleanup, and observability responsibilities. |
| Technology definition | `docs/architecture/technology-definition.md` | Confirmed Technology Decisions | Confirmed by source document | Confirms Java, Spring Boot, REST, modular monolith, MySQL/JPA, FFmpeg, yt-dlp, local disk, and Docker. |
| ADR-001 | `docs/adrs/001-use-java-and-spring-boot-modular-monolith.md` | Decision, Consequences | Accepted | Confirms Java 21 Spring Boot 4.0.x modular monolith and internal module boundaries. |
| ADR-003 | `docs/adrs/003-use-maven-npm-coordinated-asset-packaging.md` | Decision, Consequences | Accepted | Confirms one backend artifact and existing Maven/npm packaging direction. |
| Current codebase | `pom.xml`, `src/main/java/com/lucasdourado/mediautility/MediaUtilityApplication.java` | Backend scaffold | Detected in codebase | Confirms Spring Boot 4.0.6, Java 21, and base package. |
| User decision | Current `plan-task` session | Boundary implementation style | Confirmed by user | Use interfaces to materialize boundaries, but limit them to no methods. |
| User decision | Current `plan-task` session | Scope conflict resolution | Confirmed by user | Keep complete contracts out of task 002; leave them for dedicated later tasks. |
| User decision | Current `plan-task` session | Package layout | Confirmed by user | Use the direct Tech Spec layout. |

## Context Summary

The project now has the Spring Boot scaffold from task 001. Task 002 creates backend module boundaries inside the modular monolith so later tasks have clear places for API handling, operation orchestration, media service boundaries, process execution, storage, persistence, cleanup, and observability.

This task must remain structural. It must not implement media behavior, controllers, adapters, persistence entities, REST contracts, DTOs, or business logic.

## Task Goal

Create explicit Java package boundaries under `com.lucasdourado.mediautility` using no-method boundary interfaces and concise package documentation aligned with the Tech Spec responsibilities.

## Confirmed Scope

- Create backend package boundaries under `src/main/java/com/lucasdourado/mediautility`.
- Use this package layout:
  - `api`
  - `operations`
  - `media.conversion`
  - `media.download`
  - `media.process`
  - `storage`
  - `persistence`
  - `cleanup`
  - `observability`
- Add one no-method boundary interface per package.
- Add concise `package-info.java` documentation for each package.
- Keep the existing Spring Boot application entrypoint unchanged unless package imports require no-op cleanup.
- Preserve frontend and Maven/npm packaging behavior from task 001.

## Out of Scope

- Do not implement controllers, endpoints, REST DTOs, request/response contracts, or API routes.
- Do not implement operation domain model, JPA entities, repositories, migrations, or database configuration.
- Do not implement FFmpeg, yt-dlp, process execution, storage, cleanup, or observability behavior.
- Do not define service method signatures, command/result DTOs, timeout values, validation rules, or error contracts.
- Do not add dependencies except if compilation requires none, which is expected.
- Do not update PRD, project planning, technology definition, Tech Spec, ADRs, task files, or previous execution reports.

## Requirements Covered

| Requirement | Source | How This Task Covers It | Status |
| --- | --- | --- | --- |
| Establish backend module boundaries | Task file, ADR-001, Tech Spec | Creates explicit Java package boundaries for MVP backend areas. | Confirmed |
| Keep request handlers separate from media processing | Task file, Tech Spec | Creates separate `api`, `media.*`, and `operations` boundaries. | Confirmed |
| Keep external process execution isolated | Task file, Tech Spec | Creates `media.process` boundary for later process adapter task. | Confirmed |
| Prepare persistence boundary | Task file, Tech Spec | Creates `persistence` package without entities or repositories. | Confirmed |
| Prepare cleanup and observability boundaries | Tech Spec | Creates `cleanup` and `observability` packages without implementation. | Confirmed |

## Technical Specification Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Proposed Technical Solution | Partial | Establishes internal backend boundaries for the modular monolith. | Does not implement media APIs or services. |
| Modules and Responsibilities | Partial | Maps listed backend responsibilities to Java packages. | Frontend app remains outside this backend task. |
| Architecture Overview | Partial | Supports the intended runtime separation between browser, API, operations, media, storage, persistence, cleanup, and observability. | No runtime flow behavior is implemented. |
| API Design | Not applicable | No API contracts are implemented in task 002. | Dedicated REST contract task remains later. |
| Data Model | Not applicable | No operation, result, event, or JPA model is implemented. | Dedicated model tasks remain later. |
| Integrations | Partial | Creates boundaries for FFmpeg/yt-dlp process isolation. | No binary integration is implemented. |
| Testing Strategy | Partial | Future implementation should verify compilation and context startup only. | No business tests are needed for no-method boundaries. |

Coverage assessment:

- Justifying Tech Spec section: `Modules and Responsibilities`.
- Tech Spec sections implemented by this task: backend package/module boundary mapping.
- Gaps between task and Tech Spec: all behavior, APIs, data model, integrations, cleanup, and observability implementation remain for later tasks.
- Dependencies not specified by the Tech Spec: user-confirmed decision to use no-method boundary interfaces plus `package-info.java` documentation.

## Architecture and ADR Considerations

| Decision or ADR | Source | Impact on This Task | Status |
| --- | --- | --- | --- |
| Use Java 21 Spring Boot 4.0.x modular monolith | ADR-001 | Boundaries are Java packages inside one Spring Boot app, not separate modules or services. | Resolved by ADR |
| Keep React served by Spring Boot | ADR-003 and task 001 execution | Backend boundary changes must not disrupt Maven/npm packaging or static asset serving. | Confirmed |
| Use direct Tech Spec package layout | User decision in this planning session | Determines exact package names for implementation. | Confirmed by user |
| Use no-method boundary interfaces | User decision in this planning session | Materializes boundaries without forcing premature domain contracts. | Confirmed by user |
| Keep complete contracts out of task 002 | User decision in this planning session | Prevents task 002 from overlapping dedicated API/model/adapter tasks. | Confirmed by user |

ADR candidates or architecture decisions needed:

- No new formal ADR is required before implementation.
- The package layout and boundary-interface approach are task-specific architecture decisions and should be recorded in the companion architecture decision notes file.
- Existing ADR-001 already covers the modular monolith decision.

Architecture decision notes:

- Saved separately: Yes
- Path: `docs/architecture/task-decisions/mvp-media-utility/002-create-backend-module-boundaries-architecture-decisions.md`
- Notes file status: New

## Confirmed Decisions

- Task 001 has created the Spring Boot backend scaffold.
- Base package is `com.lucasdourado.mediautility`.
- The backend remains one Spring Boot modular monolith.
- Task 002 will use package boundaries, not Maven submodules or separate services.
- Task 002 will create no-method boundary interfaces.
- Task 002 will add concise `package-info.java` documentation.
- Task 002 will use the direct Tech Spec package layout:
  - `api`
  - `operations`
  - `media.conversion`
  - `media.download`
  - `media.process`
  - `storage`
  - `persistence`
  - `cleanup`
  - `observability`
- Complete service contracts, DTOs, domain models, API contracts, adapters, repository types, and method signatures are explicitly out of scope for task 002.

## Pending Decisions

None. All task-relevant decisions have been answered or explicitly deferred out of scope by the user.

## Questions for the User

None. All task-relevant questions have been answered.

## Proposed Implementation Approach

1. Inspect the current `src/main/java/com/lucasdourado/mediautility` tree and preserve the existing `MediaUtilityApplication` entrypoint.
2. Create the selected package layout under the base package.
3. In each package, add one no-method public interface whose name describes the boundary:
   - `api.ApiBoundary`
   - `operations.OperationsBoundary`
   - `media.conversion.ConversionBoundary`
   - `media.download.DownloadBoundary`
   - `media.process.ProcessExecutionBoundary`
   - `storage.StorageBoundary`
   - `persistence.PersistenceBoundary`
   - `cleanup.CleanupBoundary`
   - `observability.ObservabilityBoundary`
4. Add a concise `package-info.java` in each package describing the responsibility and major out-of-scope items for that boundary.
5. Do not annotate these interfaces as Spring beans and do not add methods.
6. Run backend validation to confirm the application still compiles and the Spring context test still passes.

## Files and Areas Expected to Change

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasdourado/mediautility/api/` | Create | Task file, Tech Spec, user decision | REST/API boundary only; no controllers or DTOs. |
| `src/main/java/com/lucasdourado/mediautility/operations/` | Create | Task file, Tech Spec, user decision | Operation lifecycle boundary only; no domain model. |
| `src/main/java/com/lucasdourado/mediautility/media/conversion/` | Create | Tech Spec, user decision | Conversion boundary only; no FFmpeg commands. |
| `src/main/java/com/lucasdourado/mediautility/media/download/` | Create | Tech Spec, user decision | URL download boundary only; no yt-dlp commands. |
| `src/main/java/com/lucasdourado/mediautility/media/process/` | Create | Task file, Tech Spec, user decision | Process execution boundary only; no adapter implementation. |
| `src/main/java/com/lucasdourado/mediautility/storage/` | Create | Task file, Tech Spec, user decision | Temporary storage boundary only; no file I/O. |
| `src/main/java/com/lucasdourado/mediautility/persistence/` | Create | Task file, Tech Spec, user decision | Persistence boundary only; no JPA entities or repositories. |
| `src/main/java/com/lucasdourado/mediautility/cleanup/` | Create | Tech Spec, user decision | Cleanup boundary only; no scheduler/job. |
| `src/main/java/com/lucasdourado/mediautility/observability/` | Create | Tech Spec, user decision | Observability boundary only; no metrics implementation. |
| `src/test/java/com/lucasdourado/mediautility/MediaUtilityApplicationTests.java` | Inspect / preserve | Task 001 execution | Existing context test should remain enough for this structural task. |

## Step-by-Step Implementation Plan

1. Verify the working tree state before editing and preserve unrelated changes.
2. Confirm the base package remains `com.lucasdourado.mediautility`.
3. Create the nine package directories listed in this plan.
4. Add one no-method boundary interface per package using the exact names listed in the Proposed Implementation Approach.
5. Add one `package-info.java` per package with concise package documentation:
   - State what the boundary owns.
   - State that behavior/contracts are implemented by later dedicated tasks.
   - For `api`, explicitly note that media processing is not owned by API classes.
   - For `media.process`, explicitly note that FFmpeg/yt-dlp process execution will be isolated here later.
   - For `persistence`, explicitly note that media bytes must not be stored in the database.
6. Do not create controllers, services with methods, DTOs, entities, repositories, configs, scheduled jobs, metrics, or adapters.
7. Run `.\mvnw.cmd test` from the repository root.
8. Review the diff to verify only boundary/package documentation files were added.

## Validation Strategy

- Run `.\mvnw.cmd test` to compile the backend and run the existing Spring Boot context test.
- Verify created packages are under `com.lucasdourado.mediautility`.
- Verify no media processing logic, REST controller, persistence entity, repository, adapter implementation, scheduler, or metrics implementation was added.
- Verify the frontend/Maven packaging configuration is not changed.

## Tests to Add or Update

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| Existing Spring Boot context test | Integration-lite | Verify application still starts after package additions. | Reuse existing `MediaUtilityApplicationTests`; no new business test is needed. |
| Maven compile via `.\mvnw.cmd test` | Build validation | Verify new Java package files compile. | Required validation. |
| Scope review | Manual | Confirm boundaries only and no out-of-scope logic. | Required because package additions are structural. |

## Acceptance Criteria

- [ ] Backend contains clear package/module boundaries for MVP areas using the confirmed package layout.
- [ ] Each boundary package contains a no-method public boundary interface.
- [ ] Each boundary package contains concise package documentation.
- [ ] No media processing logic exists in API packages.
- [ ] No controllers, adapters, entities, repositories, DTOs, schedulers, metrics, or business logic are implemented.
- [ ] Application still builds and the existing Spring Boot context test passes.

## Risks and Edge Cases

- Creating too many placeholder types can make the project feel heavier than needed; this is limited by using one no-method interface per boundary.
- Method signatures in boundary interfaces would force premature domain/API decisions; they are explicitly prohibited in this task.
- Package names become long-lived conventions; use the exact layout confirmed in this plan.
- Empty directories are not tracked by Git; interfaces and `package-info.java` files make the boundaries explicit and reviewable.

## Rollback or Recovery Notes

- The task should be reversible by deleting the newly created boundary package files.
- No schema, runtime configuration, dependency, or behavior migration is expected.

## Documentation Updates

- Do not update product, planning, technology definition, Tech Spec, ADR, task, or README documents during implementation.
- Code-level package documentation via `package-info.java` is required and is part of the implementation scope.

## Implementation Readiness Checklist

- [x] Task scope is clear.
- [x] Source documents were reviewed.
- [x] Tech Spec coverage was verified.
- [x] Architecture decisions were checked.
- [x] ADR candidates were confirmed, rejected, required-before-implementation, or explicitly deferred by the user.
- [x] Pending decisions are resolved or explicitly deferred out of this task by the user.
- [x] User questions were answered.
- [x] Expected files or areas are identified.
- [x] Acceptance criteria are defined.
- [x] Test strategy is defined.
- [x] Risks and edge cases are documented.

## Notes for the Implementing Agent

- Treat this as a structure-only task.
- Do not implement complete contracts even though boundary interfaces are being created.
- Keep the interfaces method-free.
- Leave DTOs, domain models, API contracts, persistence types, process execution, storage behavior, cleanup behavior, and observability behavior to their dedicated tasks.
