# ADR-002: Use React Vite TypeScript Served by Spring Boot

## Status

Status: Accepted

Date: 2026-06-01

## Context

The task `MVP-MEDIA-001` is blocked until a formal ADR exists for the frontend architecture and how it fits into the Spring Boot monolith.

The technology definition confirms React, Vite, TypeScript, npm, REST, and Spring Boot static asset serving as user-confirmed choices. The Tech Spec defines a React frontend loaded from the Spring Boot application.

## Decision

The MVP frontend will use React with Vite and TypeScript, built as static assets and served by the Spring Boot application, with frontend/backend communication through REST APIs in the same deployable monolith.

## Considered Options

| Option | Summary | Trade-offs | Decision |
| --- | --- | --- | --- |
| React with Vite and TypeScript served by Spring Boot | Build frontend assets and serve them from the Spring Boot app. | Keeps one deployable monolith while preserving a modern frontend toolchain. | Accepted |
| Thymeleaf/server-rendered UI | Render UI from Spring Boot templates. | Simpler backend-only rendering, but conflicts with confirmed React direction. | Rejected |
| Separate frontend deployment | Deploy the frontend independently from the backend. | More flexible hosting, but adds MVP deployment complexity. | Rejected |
| Plain JavaScript without TypeScript | Use React without TypeScript. | Less setup, but weaker type safety and maintainability. | Rejected |

## Consequences

- Frontend code should live under a dedicated directory such as `frontend/`.
- Spring Boot serves built static assets.
- REST remains the frontend/backend boundary.
- Frontend and backend can still have independent local development workflows.

## Task Impact

- Related task plan: `docs/task-plans/mvp-media-utility/001-scaffold-spring-boot-react-monolith-plan.md`
- Related architecture decision notes: `docs/architecture/task-decisions/mvp-media-utility/001-scaffold-spring-boot-react-monolith-architecture-decisions.md`
- Unlock effect: Resolves the `React + Vite + TypeScript served by Spring Boot` blocker for `MVP-MEDIA-001`.

## Source References

- `docs/architecture/technology-definition.md`: Confirmed Technology Decisions and ADR Candidates.
- `docs/specs/tech-spec.md`: Proposed Technical Solution, Architecture Overview, and UI/UX Technical Notes.
- `docs/task-plans/mvp-media-utility/001-scaffold-spring-boot-react-monolith-plan.md`: Architecture and ADR Considerations.
- User confirmation: current `resolve-architecture-blocker` session.
