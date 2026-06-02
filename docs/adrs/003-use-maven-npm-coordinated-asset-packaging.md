# ADR-003: Use Maven npm Coordinated Asset Packaging

## Status

Status: Accepted

Date: 2026-06-01

## Context

The task `MVP-MEDIA-001` is blocked until a formal ADR exists for the build and packaging strategy that coordinates the Spring Boot backend and Vite frontend.

The task plan requires Maven to orchestrate the frontend npm workflow and package built frontend assets into the Spring Boot runtime.

## Decision

The backend Maven build will orchestrate the frontend npm install/build workflow and copy `frontend/dist` into Spring Boot static assets for packaged runtime, while preserving independent frontend and backend local development workflows.

## Considered Options

| Option | Summary | Trade-offs | Decision |
| --- | --- | --- | --- |
| Maven/npm coordinated asset packaging | Maven packages the backend and coordinates frontend asset generation for runtime packaging. | Creates one runnable backend artifact with current frontend assets; CI needs Java/Maven and Node/npm. | Accepted |
| Fully separate frontend deployment | Frontend and backend are built and deployed independently. | More deployment flexibility, but conflicts with the MVP monolith packaging direction. | Rejected |
| Manual prebuilt frontend assets | Developer manually builds frontend assets before backend packaging. | Simple tooling, but error-prone and likely to cause packaging drift. | Rejected |
| Maven-managed Node installation plugin | Maven downloads/manages Node/npm through a plugin. | More reproducible in some environments, but adds plugin complexity before needed. | Rejected |

## Consequences

- Maven package must be able to produce a runnable backend artifact with current frontend assets.
- npm scripts remain the frontend source of truth.
- Build wiring must be documented.
- CI later needs both Java/Maven and Node/npm available.

## Task Impact

- Related task plan: `docs/task-plans/mvp-media-utility/001-scaffold-spring-boot-react-monolith-plan.md`
- Related architecture decision notes: `docs/architecture/task-decisions/mvp-media-utility/001-scaffold-spring-boot-react-monolith-architecture-decisions.md`
- Unlock effect: Resolves the `Maven/npm coordinated asset packaging` blocker for `MVP-MEDIA-001`.

## Source References

- `docs/architecture/technology-definition.md`: Confirmed Technology Decisions.
- `docs/specs/tech-spec.md`: Proposed Technical Solution and Testing Strategy.
- `docs/task-plans/mvp-media-utility/001-scaffold-spring-boot-react-monolith-plan.md`: confirmed asset integration decision.
- User confirmation: current `resolve-architecture-blocker` session.
