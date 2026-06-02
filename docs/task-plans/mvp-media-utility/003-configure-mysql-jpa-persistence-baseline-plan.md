# Task Implementation Plan: Configure MySQL JPA Persistence Baseline

## Status

Status: Ready for Implementation

Last updated: 2026-06-02

Plan file: `docs/task-plans/mvp-media-utility/003-configure-mysql-jpa-persistence-baseline-plan.md`

Architecture decision notes file: `docs/architecture/task-decisions/mvp-media-utility/003-configure-mysql-jpa-persistence-baseline-architecture-decisions.md`

## Task Reference

Task ID: `MVP-MEDIA-003`

Task file: `docs/tasks/mvp-media-utility/003-configure-mysql-jpa-persistence-baseline.md`

Task status: `Depends on Previous Task`

Task group or feature: `mvp-media-utility`

## Planning Mode Requirement

Plan mode verified: Yes

Notes:

- This plan was created in plan mode.
- Implementation must not start during `plan-task`.
- A future implementation request must use this saved plan and the saved architecture decision notes as source context.
- This task is ready for implementation because the required persistence ADR has been created and accepted.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project discovery | `docs/context/project-discover.md` | Project Scenario | Confirmed by source document | Required Harness discovery context exists. |
| Task file | `docs/tasks/mvp-media-utility/003-configure-mysql-jpa-persistence-baseline.md` | Scope, Out of Scope, Dependencies, Validation, Acceptance Criteria | Confirmed by source document | Defines MySQL/JPA baseline configuration task. |
| Tech Spec | `docs/specs/tech-spec.md` | Data Model, Integrations, Implementation Notes, ADR Candidates | Confirmed by source document | States MySQL stores operation metadata/events and media files must remain outside the database. |
| Technology definition | `docs/architecture/technology-definition.md` | Confirmed Technology Decisions, ADR Candidates | Confirmed by source document | Confirms MySQL and JPA as selected technologies, and lists MySQL with JPA as an ADR candidate. |
| ADR-001 | `docs/adrs/001-use-java-and-spring-boot-modular-monolith.md` | Decision, Consequences | Accepted | Confirms Java 21 Spring Boot modular monolith. |
| ADR-003 | `docs/adrs/003-use-maven-npm-coordinated-asset-packaging.md` | Decision, Consequences | Accepted | Confirms Maven/npm build wiring must be preserved. |
| ADR-004 | `docs/adrs/004-use-mysql-with-jpa-for-operation-metadata-persistence.md` | Decision, Consequences, Task Impact | Accepted | Resolves the required MySQL/JPA persistence ADR blocker for this task. |
| Task execution report 001 | `docs/task-executions/mvp-media-utility/001-scaffold-spring-boot-react-monolith-execution.md` | Implemented Changes | Confirmed by source document | Confirms Spring Boot scaffold, Maven Wrapper, and base package. |
| Task execution report 002 | `docs/task-executions/mvp-media-utility/002-create-backend-module-boundaries-execution.md` | Implemented Changes | Confirmed by source document | Confirms persistence package boundary exists. |
| Current codebase | `pom.xml`, `src/main/resources/application.properties`, `src/main/java/com/lucasdourado/mediautility/persistence/` | Backend scaffold and persistence boundary | Detected in codebase | Current project has WebMVC dependencies only, application properties, and a persistence marker package. |
| User decision | Current `plan-task` session | ADR treatment for MySQL + JPA | Confirmed by user | User requires a formal ADR before task 003 implementation can proceed. |

## Context Summary

The project has a Spring Boot 4.0.6 / Java 21 scaffold and the backend module boundaries from tasks 001 and 002. Task 003 is intended to configure the persistence baseline for MySQL and Spring Data JPA without creating entities, repositories, schemas, Docker Compose, or media storage behavior.

MySQL and JPA are confirmed in the technology definition, both the technology definition and Tech Spec list `Use MySQL with JPA` as an ADR candidate, and ADR-004 now formally accepts MySQL with Spring Data JPA for operation metadata persistence. The architecture blocker for this task is resolved.

## Task Goal

Prepare the backend to use MySQL through Spring Data JPA with environment-driven datasource configuration, using ADR-004 as the binding persistence decision.

## Confirmed Scope

- Add Spring Data JPA dependency.
- Add MySQL JDBC driver dependency.
- Configure datasource settings through environment-driven configuration.
- Add baseline JPA configuration only where needed.
- Preserve Maven/npm frontend packaging behavior.
- Keep persistence package boundary aligned with task 002.
- Do not implement persistence entities or repositories.

## Out of Scope

- Do not implement operation entities.
- Do not implement repository interfaces.
- Do not add Flyway, Liquibase, migrations, or schema files unless a later source document explicitly requires them.
- Do not add Docker Compose in this task.
- Do not create local MySQL orchestration.
- Do not store media bytes in MySQL.
- Do not implement operation metadata, result metadata, or operation events in this task.
- Do not implement cleanup, observability, API contracts, or business logic.

## Requirements Covered

| Requirement | Source | How This Task Covers It | Status |
| --- | --- | --- | --- |
| MySQL stores operation metadata and events | Tech Spec, ADR-004 | This task adds the baseline dependencies/configuration needed before metadata entities are implemented later. | Ready |
| Use JPA persistence access | Technology definition, ADR-004 | This task adds Spring Data JPA baseline support. | Ready |
| Keep media files outside database | Task file, Tech Spec, technology definition | This plan keeps media bytes out of scope and documents that only metadata belongs in MySQL later. | Confirmed |
| Preserve modular monolith structure | ADR-001, task 002 execution | This task should configure persistence inside the existing Spring Boot app and persistence package boundary. | Confirmed |

## Technical Specification Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Proposed Technical Solution | Partial | Adds the baseline for MySQL-backed operation metadata. | No actual metadata model is implemented. |
| Modules and Responsibilities | Partial | Supports the persistence layer responsibility. | No entities or repositories are created. |
| Data Model | Partial | Prepares for future Operation, Result file metadata, and Operation event persistence. | Data model implementation remains later. |
| Integrations | Partial | Adds MySQL/JPA integration baseline after ADR acceptance. | No runtime database orchestration is added. |
| Implementation Notes | Partial | Keeps metadata in MySQL and media outside the database. | No media or metadata behavior is implemented. |
| ADR Candidates | Full for planning | Identifies `Use MySQL with JPA` as resolved by ADR-004. | Required ADR accepted. |

Coverage assessment:

- Justifying Tech Spec section: `Integrations`, `Data Model`, `Implementation Notes`, and `ADR Candidates`.
- Tech Spec sections implemented by this task: persistence baseline configuration only.
- Gaps between task and Tech Spec: operation entities, result metadata, operation events, repositories, migrations, cleanup, observability, and runtime orchestration remain later tasks.
- Dependencies not specified by the Tech Spec: exact datasource property names and profiles can be planned after the ADR is accepted; implementation must avoid secrets and hardcoded credentials.

## Architecture and ADR Considerations

| Decision or ADR | Source | Impact on This Task | Status |
| --- | --- | --- | --- |
| Java 21 Spring Boot modular monolith | ADR-001 | Persistence baseline must be configured inside one Spring Boot app. | Accepted |
| Maven/npm coordinated asset packaging | ADR-003 | Persistence dependency/configuration changes must not disrupt existing frontend packaging. | Accepted |
| MySQL with JPA | Technology definition, Tech Spec, ADR-004, user decision | Foundational persistence decision for this task. | Resolved by ADR |
| No media bytes in database | Task file, Tech Spec, technology definition | Prevents BLOB/media content persistence in this task and later metadata design. | Confirmed |
| Docker Compose not in this task | Task file | Local database orchestration remains for a dedicated local development task. | Confirmed |

ADR candidates or architecture decisions needed:

- None. ADR-004 resolves the required persistence ADR blocker.
- No architecture blocker remains for this task.

Architecture decision notes:

- Saved separately: Yes
- Path: `docs/architecture/task-decisions/mvp-media-utility/003-configure-mysql-jpa-persistence-baseline-architecture-decisions.md`
- Notes file status: New

## Confirmed Decisions

- Task 001 scaffold exists and uses Java 21, Spring Boot 4.0.6, Maven Wrapper, and package `com.lucasdourado.mediautility`.
- Task 002 created the `persistence` package boundary.
- MySQL and JPA are selected technologies in the technology definition.
- Media files must remain local temporary files and must not be stored as database blobs.
- Docker Compose/local database orchestration is out of scope for task 003.
- No entities, repositories, migrations, schemas, operation metadata model, result metadata model, or operation event model should be implemented in task 003.
- ADR-004 formally accepts `Use MySQL with JPA for Operation Metadata Persistence` before task 003 implementation.

## Pending Decisions

None.

## Questions for the User

- None.

## Proposed Implementation Approach

Implementation should proceed from ADR-004 and remain limited to the persistence baseline:

1. Re-read ADR-004, the task file, Tech Spec, technology definition, and current codebase.
2. Add Spring Data JPA and MySQL driver dependencies to `pom.xml`.
3. Configure datasource and JPA settings through environment-driven Spring configuration.
4. Keep the application able to build without committed credentials.
5. Do not add entities, repositories, migrations, Docker Compose, or database schema in this task.
6. Validate that the application builds and can start when database settings are supplied.

## Files and Areas Expected to Change

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| `pom.xml` | Modify | Task file, technology definition, ADR-004 | Add Spring Data JPA and MySQL dependencies without disrupting existing Maven/npm build wiring. |
| `src/main/resources/application.properties` or profile/config files | Modify/Create | Task file, Tech Spec, ADR-004 | Add environment-driven datasource/JPA configuration without secrets. |
| `src/main/java/com/lucasdourado/mediautility/persistence/` | Inspect / Preserve | Task 002 execution | Existing persistence boundary should remain; no repositories/entities should be added in task 003. |
| `src/test/java/com/lucasdourado/mediautility/` | Inspect / Possibly adjust after ADR acceptance | Existing scaffold | Existing context test may require profile/test configuration if JPA auto-configuration requires datasource settings. |

## Step-by-Step Implementation Plan

1. Re-read ADR-004, this task plan, the task file, the Tech Spec, the technology definition, and current codebase state.
2. Inspect `pom.xml` to preserve existing Spring Boot and Maven/npm build wiring.
3. Add Spring Data JPA and MySQL JDBC driver dependencies.
4. Add environment-driven datasource and JPA baseline configuration without committing credentials.
5. Adjust test/profile configuration only if needed to keep the Spring Boot context test viable after JPA auto-configuration.
6. Confirm no entities, repositories, migrations, schemas, Docker Compose files, or media byte persistence were added.

## Validation Strategy

- Run `.\mvnw.cmd test`.
- Confirm Maven resolves Spring Data JPA and MySQL dependencies.
- Confirm the app does not require hardcoded database credentials.
- Confirm no entity, repository, schema, migration, Docker Compose, or media BLOB storage was added.
- If a real MySQL instance is available and configured, verify the Spring application can start with supplied environment variables.
- If no local MySQL orchestration exists, document that runtime DB startup validation is deferred to the dedicated local development or Docker Compose task.

## Tests to Add or Update

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| Existing Spring Boot context test | Integration-lite | Verify app context behavior after JPA dependency/configuration changes. | Exact test profile/config strategy must be defined after the ADR and unblock re-plan. |
| Maven dependency/build validation | Build validation | Verify JPA/MySQL dependencies resolve and compile. | Future implementation should run `.\mvnw.cmd test`. |
| Scope review | Manual | Ensure no entities, repositories, schema, Docker Compose, or media byte persistence were added. | Required because task 003 is a baseline-only task. |

## Acceptance Criteria

- [x] Formal `Use MySQL with JPA` ADR exists and is accepted before implementation.
- [ ] JPA and MySQL dependencies are present after implementation.
- [ ] Datasource configuration is environment-driven after implementation.
- [ ] Build succeeds without hardcoded credentials after implementation.
- [ ] No media file content is stored in the database.
- [ ] No entities, repositories, migrations, schemas, or Docker Compose files are added in this task.

## Risks and Edge Cases

- Adding JPA dependencies may cause the existing Spring context test to require datasource/test configuration.
- Local startup may require a MySQL instance before Docker Compose is available.
- Over-configuring JPA before entities exist could create unnecessary constraints.
- Hardcoded datasource credentials would violate the task requirements.
- Adding entities/repositories/migrations would exceed this task's scope.
- The persistence ADR may change the exact implementation approach.

## Rollback or Recovery Notes

After implementation, the expected rollback would remove the added persistence dependencies and datasource/JPA configuration while preserving the existing Spring Boot scaffold and persistence boundary.

## Documentation Updates

- Do not update PRD, project planning, technology definition, Tech Spec, final ADR files, or task files during task 003 implementation.
- The required formal MySQL/JPA ADR must be produced through the ADR/blocker workflow before implementation.
- The future task execution report should document dependency/configuration changes and validation evidence.

## Implementation Readiness Checklist

- [x] Task scope is clear.
- [x] Source documents were reviewed.
- [x] Tech Spec coverage was verified.
- [x] Architecture decisions were checked.
- [x] ADR candidates were confirmed, rejected, required-before-implementation, or explicitly deferred by the user.
- [x] Pending decisions are resolved or explicitly deferred out of this task by the user.
- [x] User questions were answered, or the plan is explicitly marked ready.
- [x] Expected files or areas are identified.
- [x] Acceptance criteria are defined.
- [x] Test strategy is defined.
- [x] Risks and edge cases are documented.

## Notes for the Implementing Agent

- No architecture blocker remains for task 003.
- ADR-004 is the binding source for MySQL/JPA persistence baseline decisions.
- Do not infer migration strategy beyond the baseline-only scope.
- Do not add entities, repositories, migrations, Docker Compose, or media storage behavior in task 003.
