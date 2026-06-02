# Task Architecture Decision Notes: Scaffold Spring Boot React Monolith

## Status

Status: Ready for Implementation

Last updated: 2026-06-01

Decision notes file: `docs/architecture/task-decisions/mvp-media-utility/001-scaffold-spring-boot-react-monolith-architecture-decisions.md`

Task plan file: `docs/task-plans/mvp-media-utility/001-scaffold-spring-boot-react-monolith-plan.md`

Task file: `docs/tasks/mvp-media-utility/001-scaffold-spring-boot-react-monolith.md`

## Purpose

Record task-specific architecture decisions, conflicts, and confirmed ADR candidates identified while preparing the task implementation plan.

This file is planning support only. It is not a final ADR and must not replace the ADR workflow.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/tasks/mvp-media-utility/001-scaffold-spring-boot-react-monolith.md` | Scope | Confirmed by source document | Requires Spring Boot and React monolith scaffold. |
| Technology definition | `docs/architecture/technology-definition.md` | Confirmed Technology Decisions, ADR Candidates | In Review with confirmed user choices | Identifies foundational decisions and ADR candidates. |
| Tech Spec | `docs/specs/tech-spec.md` | Proposed Technical Solution, ADR Candidates | Draft technical source | Defines proposed monolith and frontend/backend integration. |
| User decision | Current `plan-task` session | ADR handling | Confirmed by user | Formal ADRs are required before implementing this scaffold task. |
| User decision | Current `plan-task` session | Version, naming, asset integration, tests | Confirmed by user | Confirms Java 21, Spring Boot 4.0.x, Node 24 LTS, package/artifact names, Maven-orchestrated frontend packaging, and scaffold test level. |
| ADR-001 | `docs/adrs/001-use-java-and-spring-boot-modular-monolith.md` | Decision | Resolved by ADR | Accepts Java and Spring Boot modular monolith. |
| ADR-002 | `docs/adrs/002-use-react-vite-typescript-served-by-spring-boot.md` | Decision | Resolved by ADR | Accepts React, Vite, and TypeScript served by Spring Boot. |
| ADR-003 | `docs/adrs/003-use-maven-npm-coordinated-asset-packaging.md` | Decision | Resolved by ADR | Accepts Maven/npm coordinated asset packaging. |

## Confirmed Architecture Decisions

| Decision | Source | Impact on This Task | Notes |
| --- | --- | --- | --- |
| Use Java and Spring Boot modular monolith | Technology definition, Tech Spec, ADR-001 | Backend scaffold must be one Spring Boot deployable. | Resolved by ADR. |
| Use React with Vite and TypeScript served by Spring Boot | Technology definition, Tech Spec, ADR-002 | Frontend scaffold must be Vite React TypeScript and served as static assets by the backend. | Resolved by ADR. |
| Use Maven and npm with Maven-orchestrated asset packaging | Technology definition, task file, user decision, ADR-003 | Build must coordinate frontend build and backend packaging. | Resolved by ADR. |
| Use Java 21, Spring Boot 4.0.x, and Node 24 LTS | User decision | Fixes scaffold runtime/tooling versions. | Does not itself require a separate ADR unless included in the monolith/build ADRs. |
| Use package `com.lucasdourado.mediautility` and artifact `media-utility` | User decision | Establishes initial naming convention. | Treat as implementation convention for this scaffold. |

## Pending Architecture Decisions

None. All task-relevant architecture decisions have been answered by source documents, explicit user decisions, and accepted ADRs.

Blocking prerequisite:

None. The required formal ADRs have been accepted and saved.

## Architecture Conflicts

| Conflict | Sources | Impact | Resolution Status |
| --- | --- | --- | --- |
| Formal ADRs were missing, but user required them before implementation | Current `plan-task` user decision vs previously missing ADR folders/files | Previously blocked implementation of this task. | Resolved by ADR-001, ADR-002, and ADR-003 |
| Technology definition and Tech Spec are not final-approved documents, but contain confirmed user choices | `technology-definition.md`, `tech-spec.md` | Implementation should rely only on confirmed decisions and the task plan until documents are finalized. | Resolved by explicit task-scoped user decisions |

## ADR Candidates

| Candidate | Trigger | Suggested ADR Scope | Blocking for This Task? |
| --- | --- | --- | --- |
| Use Java and Spring Boot modular monolith | Technology definition and Tech Spec identify it as foundational. | Backend runtime, modular monolith style, one deployable application, no split services for MVP. | Resolved by ADR-001 |
| Use React with Vite and TypeScript served by Spring Boot | Technology definition and Tech Spec identify it as foundational. | Frontend stack, static asset serving through Spring Boot, no Thymeleaf, no separate frontend deployment for MVP. | Resolved by ADR-002 |
| Use Maven/npm coordinated build strategy | User selected Maven-orchestrated npm build and asset copy. | Frontend/backend build coordination, static asset packaging, independent local frontend workflow. | Resolved by ADR-003 |

## Implementation Impact

- Future implementation may proceed using the accepted ADRs as binding architecture context.
- The scaffold should use Java 21, Spring Boot 4.0.x, Node 24 LTS, artifact `media-utility`, and package `com.lucasdourado.mediautility`.
- The backend and frontend should remain one deployable monolith, with the React build served by Spring Boot.
- Maven must orchestrate frontend packaging by running npm build workflow and copying `frontend/dist` into Spring Boot static assets.
- Media, persistence, cleanup, Docker, and observability architecture decisions remain outside this scaffold task.

## Questions for the User

None. All task-relevant architecture questions have been answered.

## Notes for the Implementing Agent

- This notes file is planning support only and is not a final ADR.
- Do not treat these notes as permission to implement.
- Check for ADR-001, ADR-002, and ADR-003 before starting source changes for `MVP-MEDIA-001`.
- Do not create final ADRs as part of the scaffold implementation unless a separate ADR workflow/task explicitly authorizes that work.
