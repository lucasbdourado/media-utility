# ADR-001: Use Java and Spring Boot Modular Monolith

## Status

Status: Accepted

Date: 2026-06-01

## Context

The task `MVP-MEDIA-001` is blocked until a formal ADR exists for the foundational backend and deployment architecture.

The technology definition confirms Java, Spring Boot, and modular monolith as user-confirmed choices. The Tech Spec proposes one Spring Boot application serving the frontend and exposing REST APIs. The task plan requires a formal ADR before implementation.

## Decision

The MVP will be built as one deployable Java 21 Spring Boot 4.0.x modular monolith, with internal module boundaries and no split frontend/backend or microservice deployment for the MVP.

## Considered Options

| Option | Summary | Trade-offs | Decision |
| --- | --- | --- | --- |
| Java and Spring Boot modular monolith | One deployable backend application with internal module boundaries. | Simpler MVP deployment and clear internal boundaries; future scaling may require revisiting architecture. | Accepted |
| Microservices | Split backend responsibilities into separately deployed services. | Adds operational complexity before MVP validation. | Rejected |
| Separate frontend/backend deployment | Deploy frontend and backend independently. | Adds deployment coordination and hosting complexity for the MVP. | Rejected |
| Server-rendered Spring MVC/Thymeleaf | Render UI from Spring Boot server templates. | Conflicts with confirmed React frontend direction. | Rejected |

## Consequences

- The MVP starts as a single deployable app.
- Backend code should preserve internal modular boundaries.
- React will be served as static assets later by the Spring Boot runtime.
- MVP deployment is simpler than a distributed architecture.
- Future scaling may require revisiting the architecture.

## Task Impact

- Related task plan: `docs/task-plans/mvp-media-utility/001-scaffold-spring-boot-react-monolith-plan.md`
- Related architecture decision notes: `docs/architecture/task-decisions/mvp-media-utility/001-scaffold-spring-boot-react-monolith-architecture-decisions.md`
- Unlock effect: Resolves the `Java/Spring Boot modular monolith` blocker for `MVP-MEDIA-001`.

## Source References

- `docs/architecture/technology-definition.md`: Confirmed Technology Decisions and ADR Candidates.
- `docs/specs/tech-spec.md`: Proposed Technical Solution and Architecture Overview.
- `docs/task-plans/mvp-media-utility/001-scaffold-spring-boot-react-monolith-plan.md`: Architecture and ADR Considerations.
- User confirmation: current `resolve-architecture-blocker` session.
