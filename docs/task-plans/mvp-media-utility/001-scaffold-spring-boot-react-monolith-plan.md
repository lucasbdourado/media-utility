# Task Implementation Plan: Scaffold Spring Boot React Monolith

## Status

Status: Ready for Implementation

Last updated: 2026-06-01

Plan file: `docs/task-plans/mvp-media-utility/001-scaffold-spring-boot-react-monolith-plan.md`

Architecture decision notes file: `docs/architecture/task-decisions/mvp-media-utility/001-scaffold-spring-boot-react-monolith-architecture-decisions.md`

## Task Reference

Task ID: `MVP-MEDIA-001`

Task file: `docs/tasks/mvp-media-utility/001-scaffold-spring-boot-react-monolith.md`

Task status: `Ready`

Task group or feature: `mvp-media-utility`

## Planning Mode Requirement

Plan mode verified: Yes

Notes:

- This plan was prepared in plan mode.
- Implementation must not start during `plan-task`.
- A future implementation request must use this saved plan and the architecture decision notes as source context.
- Formal ADRs required by the previous blocker have been accepted and saved.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/tasks/mvp-media-utility/001-scaffold-spring-boot-react-monolith.md` | Scope, Out of Scope, Validation, Acceptance Criteria | Confirmed by source document | Defines this as minimal backend/frontend scaffold only. |
| Existing task plan | `docs/task-plans/mvp-media-utility/001-scaffold-spring-boot-react-monolith-plan.md` | Pending Decisions | Existing generated file | Replaced by this revised blocked plan after user confirmation. |
| Project discovery | `docs/context/project-discover.md` | Project Scenario, Project Location | Confirmed by source document | Confirms greenfield project with no implementation codebase. |
| PRD | `docs/product/prd.md` | Scope, Functional Requirements | Confirmed product input | Defines anonymous single-flow MVP. |
| Project planning | `docs/planning/project-planning.md` | Project Foundation, Short Implementation Tasks | Confirmed planning input | Places base project structure after technology definition. |
| Technology definition | `docs/architecture/technology-definition.md` | Confirmed Technology Decisions, ADR Candidates | In Review with confirmed user choices | Confirms Java, Spring Boot, modular monolith, Vite React TypeScript, Maven, npm, Docker, MySQL/JPA, FFmpeg, yt-dlp, local disk, and 1-hour retention. |
| Tech Spec | `docs/specs/tech-spec.md` | Proposed Technical Solution, ADR Candidates, Testing Strategy | Draft technical source | Covers monolith shape and frontend/backend integration. |
| User decision | Current `plan-task` session | Version policy | Confirmed by user | Use Java 21, Spring Boot 4.0.x, and Node 24 LTS. |
| User decision | Current `plan-task` session | Project naming | Confirmed by user | Use package `com.lucasdourado.mediautility` and artifact `media-utility`. |
| User decision | Current `plan-task` session | Asset integration | Confirmed by user | Maven orchestrates `npm install/build` and copies `frontend/dist` into Spring Boot static assets during packaging. |
| User decision | Current `plan-task` session | Scaffold tests | Confirmed by user | Include Spring context test, frontend build/typecheck, and a basic React Vitest test. |
| User decision | Current `plan-task` session | ADR handling | Confirmed by user | Formal ADRs are required before implementing this scaffold task. |
| ADR-001 | `docs/adrs/001-use-java-and-spring-boot-modular-monolith.md` | Decision | Resolved by ADR | Accepts Java 21 Spring Boot 4.0.x modular monolith for the MVP. |
| ADR-002 | `docs/adrs/002-use-react-vite-typescript-served-by-spring-boot.md` | Decision | Resolved by ADR | Accepts React, Vite, and TypeScript served by Spring Boot static assets. |
| ADR-003 | `docs/adrs/003-use-maven-npm-coordinated-asset-packaging.md` | Decision | Resolved by ADR | Accepts Maven-orchestrated npm build and frontend asset packaging. |
| Official version check | Spring, Node.js, Oracle documentation | Current supported versions | External verification | Used only to support the user-confirmed version decision. |
| ADRs | `docs/adrs/` | ADR-001, ADR-002, ADR-003 | Resolved by ADR | Foundational architecture/build blockers for this task are resolved. |
| Project structure | `docs/context/project-structure.md` | Not available | Missing | No implementation codebase exists yet. |
| Project analysis | `docs/context/project-analysis.md` | Not available | Missing | No implementation codebase exists yet. |

## Context Summary

The project is greenfield and has no application codebase. The MVP is an anonymous internet-hosted media utility with one React-based user flow and Spring Boot backend APIs. This task establishes only the initial runnable monolith scaffold. Later tasks will implement media operations, persistence, cleanup, metrics, Docker packaging, and full validation.

## Task Goal

Create a minimal runnable Spring Boot Maven backend and Vite React TypeScript frontend scaffold for one deployable modular monolith, with backend packaging able to serve built frontend static assets.

## Confirmed Scope

- Create the Spring Boot Maven project baseline at repository root.
- Create the Vite React TypeScript frontend baseline under `frontend/`.
- Use Java 21, Spring Boot 4.0.x, Node 24 LTS, Maven, npm, React, Vite, and TypeScript.
- Use Maven artifact `media-utility` and Java base package `com.lucasdourado.mediautility`.
- Add Maven/npm build wiring so Maven can run the frontend build and package built frontend assets into Spring Boot static assets.
- Add minimal Spring Boot startup behavior.
- Add a Spring Boot context test, frontend build/typecheck validation, and one basic React Vitest test.
- Keep the scaffold minimal and avoid MVP feature behavior.

## Out of Scope

- Media conversion and URL download behavior.
- FFmpeg and yt-dlp adapters.
- MySQL schema, JPA entities, migrations, or operation metadata.
- Temporary file lifecycle, cleanup jobs, metrics, or observability implementation.
- Docker image, Docker Compose, CI/CD, deployment provider configuration.
- Responsibility notice copy, legal terms, user accounts, service pages, or production rate limits.
- Creating formal ADRs inside this implementation task.

## Requirements Covered

| Requirement | Source | How This Task Covers It | Status |
| --- | --- | --- | --- |
| Establish base project structure | Project planning, task file | Creates the runnable empty application foundation. | Confirmed |
| Single deployable monolith | Technology definition, Tech Spec | Backend and frontend are coordinated in one Spring Boot-served application. | Confirmed |
| React frontend, not server-rendered UI | Technology definition | Uses Vite React TypeScript frontend baseline. | Confirmed |
| REST-capable backend foundation | Technology definition, Tech Spec | Creates Spring Boot baseline suitable for later REST APIs. | Partial |
| MVP user flow requirements | PRD | This task only prepares the shell for later flow implementation. | Partial |

## Technical Specification Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Proposed Technical Solution | Partial | Establishes Spring Boot monolith serving a Vite React frontend. | Does not implement media APIs or storage. |
| Architecture Overview | Partial | Creates runtime foundation for browser loading React from Spring Boot. | Operation processing flow remains later work. |
| Modules and Responsibilities | Partial | Starts initial backend/frontend boundaries only. | Module internals can remain minimal until feature tasks. |
| API Design | Not applicable | No media API contract must be implemented in this scaffold. | A minimal page/static serving check is enough. |
| Testing Strategy | Partial | Adds scaffold-level backend and frontend validation. | Full media, DB, integration, and E2E tests remain later work. |
| ADR Candidates | Relevant | Foundational choices are recognized as ADR candidates. | User requires formal ADRs before implementation. |

Coverage assessment:

- Justifying Tech Spec section: `Proposed Technical Solution`.
- Tech Spec sections implemented by this task: monolith runtime shape, frontend/backend scaffold, build foundation.
- Gaps between task and Tech Spec: detailed APIs, data model, media processing, persistence, cleanup, Docker, and observability are intentionally out of scope.
- Dependencies not specified by the Tech Spec: formal ADRs for foundational scaffold decisions have been accepted and saved.

## Architecture and ADR Considerations

| Decision or ADR | Source | Impact on This Task | Status |
| --- | --- | --- | --- |
| Use Java and Spring Boot modular monolith | Technology definition, Tech Spec, user decision, ADR-001 | Determines backend scaffold and deployable shape. | Resolved by ADR |
| Use React with Vite and TypeScript served by Spring Boot | Technology definition, Tech Spec, user decision, ADR-002 | Determines frontend scaffold and asset integration. | Resolved by ADR |
| Use Maven and npm coordinated by Maven packaging | Technology definition, task file, user decision, ADR-003 | Determines build commands and project layout. | Resolved by ADR |
| Use Java 21, Spring Boot 4.0.x, Node 24 LTS | User decision with official version check | Determines generated project configuration. | Confirmed |
| Use package `com.lucasdourado.mediautility` and artifact `media-utility` | User decision | Establishes long-term naming conventions. | Confirmed |
| Use MySQL/JPA, FFmpeg, yt-dlp, local disk, Docker | Technology definition | Do not implement in this task; avoid scaffold choices that conflict with later additions. | Confirmed but out of scope |
| Spring Scheduling, Actuator/Micrometer, Docker Compose | Technology definition, Tech Spec | Affects later validation/local dev; scaffold must not implement these unconfirmed recommendations. | Pending outside this task |
| Final ADRs | ADR-001, ADR-002, ADR-003 | Required formal ADRs now exist for this task. | Resolved by ADR |

ADR candidates or architecture decisions needed:

- Resolved by ADR: Java/Spring Boot modular monolith (`docs/adrs/001-use-java-and-spring-boot-modular-monolith.md`).
- Resolved by ADR: React + Vite + TypeScript served by Spring Boot (`docs/adrs/002-use-react-vite-typescript-served-by-spring-boot.md`).
- Resolved by ADR: Maven/npm coordinated build and frontend asset packaging (`docs/adrs/003-use-maven-npm-coordinated-asset-packaging.md`).

Architecture decision notes:

- Saved separately: Yes
- Path: `docs/architecture/task-decisions/mvp-media-utility/001-scaffold-spring-boot-react-monolith-architecture-decisions.md`
- Notes file status: New

## Confirmed Decisions

- Project is greenfield with no implementation codebase.
- Backend language/framework is Java with Spring Boot.
- Architecture style is a modular monolith.
- Frontend is Vite + React + TypeScript.
- Backend build uses Maven.
- Frontend package management/build uses npm.
- Java version is 21.
- Spring Boot line is 4.0.x.
- Node line is 24 LTS.
- Maven artifact is `media-utility`.
- Java base package is `com.lucasdourado.mediautility`.
- Frontend is served as built static assets by the Spring Boot application.
- Maven must orchestrate `npm install/build` and copy `frontend/dist` into Spring Boot static assets during packaging.
- Scaffold validation includes Spring context test, frontend build/typecheck, and a basic React Vitest test.
- Formal ADRs are required before this scaffold implementation starts.
- Required formal ADRs have been accepted and saved.
- This task is scaffold-only and must not implement media feature behavior.

## Pending Decisions

None. All task-relevant decisions have been answered by source documents or explicit user decisions.

Blocking prerequisite:

- None. The formal ADRs for the three foundational architecture/build decisions have been created and accepted.

## Questions for the User

None. All task-relevant questions have been answered.

## Proposed Implementation Approach

1. Verify the accepted ADRs remain present before implementation starts.
2. Scaffold the Spring Boot Maven backend as the root application using Java 21, Spring Boot 4.0.x, artifact `media-utility`, and package `com.lucasdourado.mediautility`.
3. Scaffold the Vite React TypeScript frontend under `frontend/` using Node 24 LTS and npm.
4. Configure Maven so package/build execution runs the frontend npm workflow and copies `frontend/dist` into the Spring Boot static assets location used by packaged runtime.
5. Add only minimal backend startup behavior and a minimal React app shell.
6. Add scaffold-level validation: Spring context test, frontend build/typecheck, and one basic Vitest React test.
7. Document minimal local build/start commands only if needed to make the scaffold usable.

## Files and Areas Expected to Change

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| `pom.xml` | Create | Task file, technology definition, user decisions | Maven backend build with Java 21, Spring Boot 4.0.x, artifact `media-utility`, and frontend build orchestration. |
| `src/main/...` | Create | Task file, Tech Spec | Spring Boot application source under `com.lucasdourado.mediautility`. |
| `src/test/...` | Create | User decision | Minimal Spring Boot context startup test. |
| `frontend/` | Create | Task file, technology definition | Vite React TypeScript app baseline. |
| `frontend/package.json` | Create | Task file, technology definition, user decision | npm scripts for dev, build/typecheck, and Vitest. |
| `frontend/src/...` | Create | Technology definition | Minimal React app shell and basic test. |
| `README.md` or equivalent project docs | Create or update if needed | Validation needs | Record minimal local build/start commands only if useful. |

## Step-by-Step Implementation Plan

1. Verify ADR-001, ADR-002, and ADR-003 exist before changing application source files.
2. Inspect repository root to confirm there is no existing application scaffold and preserve existing Harness documentation.
3. Create the Maven Spring Boot project baseline at repository root with Java 21, Spring Boot 4.0.x, artifact `media-utility`, and package `com.lucasdourado.mediautility`.
4. Add the Spring Boot main application class.
5. Add a minimal Spring Boot context startup test.
6. Create the Vite React TypeScript frontend under `frontend/`.
7. Keep the frontend UI minimal: render a placeholder application shell only, without media operation forms or feature logic.
8. Add npm scripts for local frontend development, production build/typecheck, and Vitest.
9. Add one basic React test that verifies the placeholder app shell renders.
10. Wire Maven package/build behavior to run `npm install/build` for `frontend/` and copy `frontend/dist` into Spring Boot static assets for packaged runtime.
11. Verify backend and frontend builds independently.
12. Verify the packaged Spring Boot app can serve the built frontend assets.
13. Keep generated files focused on scaffold conventions; do not add media APIs, database schema, Docker, cleanup, metrics, or external process adapters.

## Validation Strategy

- Confirm ADR-001, ADR-002, and ADR-003 exist before implementation starts.
- Run the backend build with Maven.
- Run frontend npm install/build/typecheck workflow.
- Run the basic frontend Vitest test.
- Run the Spring Boot context startup test.
- Start the Spring Boot application locally.
- Verify the application serves the built frontend page from the packaged Spring Boot runtime.
- Do not require MySQL, FFmpeg, yt-dlp, Docker, or external network media sources for this task.

## Tests to Add or Update

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| Spring Boot context startup | Integration-lite | Verify backend scaffold starts. | Required for scaffold. |
| Frontend build/typecheck | Build validation | Verify Vite/TypeScript build succeeds. | Required for scaffold. |
| Basic React render test | Unit/component | Verify placeholder app shell renders. | Use Vitest. |
| Packaged static asset serving | Manual or integration-lite | Verify Spring Boot can serve built frontend assets. | Required validation; automation optional if simple. |
| Feature tests | Not applicable | Media behavior is out of scope. | Do not add conversion/download tests here. |

## Acceptance Criteria

- [x] Required formal ADRs exist for monolith architecture, React/Vite served by Spring Boot, and Maven/npm asset packaging.
- [ ] Backend project builds with Maven.
- [ ] Frontend project builds with npm.
- [ ] Basic frontend Vitest test passes.
- [ ] Spring Boot context startup test passes.
- [ ] Application can start locally.
- [ ] Built frontend can be packaged or served by Spring Boot.
- [ ] No media conversion, URL download, persistence, cleanup, metrics, Docker, or deployment behavior is implemented in this task.

## Risks and Edge Cases

- Spring Boot 4.0.x may require plugin/dependency choices that differ from older Boot 3 examples.
- Frontend asset integration can become fragile if Maven and npm scripts are not clearly separated.
- Greenfield package/directory names become long-lived conventions.
- Adding too many dependencies during scaffold can accidentally commit future architecture choices before they are needed.
- Depending on external network generators during implementation may fail in restricted environments; generated scaffold can be created manually if needed.

## Rollback or Recovery Notes

- Scaffold changes should be revertible as one project-foundation change set.
- Keep generated dependency and lock files scoped to the Maven/npm scaffold.
- Avoid altering Harness planning documents during implementation unless a future task explicitly requests documentation updates.

## Documentation Updates

- Add or update minimal README instructions only for build/start commands if not already present.
- Do not update PRD, planning, technology definition, Tech Spec, final ADRs, or task files during this implementation task.

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

- Treat this as a scaffold task only.
- Verify the required formal ADRs remain present before implementation starts.
- Preserve existing Harness documentation and task/planning files.
- Do not implement the operation selector, upload form, URL form, REST media APIs, database persistence, cleanup jobs, metrics, Docker packaging, or external process adapters.
- Prefer minimal default generated structure over premature custom abstractions.
- If implementation tooling requires network access for dependencies or generators, request approval rather than replacing confirmed technologies.
