# ADR-005: Use Shared Operation Domain Model for Metadata Persistence

## Status

Status: Accepted

Date: 2026-06-02

## Context

MVP-MEDIA-004 is blocked because the operation domain model will define long-lived persistent metadata used by both MP4 conversion and URL download flows. The blocked task plan also recorded that `docs/specs/tech-spec.md`, `docs/product/prd.md`, `docs/architecture/technology-definition.md`, and `docs/adrs/004-use-mysql-with-jpa-for-operation-metadata-persistence.md` are empty in the current workspace.

The current codebase already has a Java 21 Spring Boot modular monolith, backend package boundaries, Spring Data JPA, MySQL connector, and environment-driven datasource configuration. The task file confirms that the operation model must represent operation type, lifecycle status, timestamps, expiration, failure details, and result metadata while keeping raw filesystem paths server-side only.

During the unblock session, the user chose to resolve this blocker with a new accepted ADR for the operation metadata model. The empty source documents remain a documented limitation, but they are no longer blocking for this task because the user explicitly confirmed the decision below.

## Decision

The MVP will use one shared persistent `Operation` domain model for conversion and URL download operation metadata, backed by JPA/MySQL.

This shared model is accepted only if `Operation` is treated as a real common domain concept for the task: it must represent shared lifecycle, status, timestamps, result metadata, expiration, and failure metadata across both flows. The decision does not authorize a generic or anemic entity created only for technical persistence convenience.

If MP4 conversion and URL download later prove to have materially different rules, invariants, states, or lifecycles, that divergence must be recorded and re-evaluated in a future task or architecture decision.

## Considered Options

| Option | Summary | Trade-offs | Decision |
| --- | --- | --- | --- |
| Shared persistent `Operation` model | One common domain model represents lifecycle and metadata shared by conversion and URL download operations. | Keeps task 004 focused and gives later API, storage, cleanup, and observability tasks one common operation reference. It must preserve real domain invariants and avoid becoming a generic technical record. | Accepted |
| Separate persistent models per flow | Model conversion and URL download independently, sharing only small common concepts. | Better if each flow develops distinct rules, invariants, states, or lifecycle semantics, but premature for task 004 based on the current confirmed task scope. | Rejected for now |
| Domain-only model without JPA persistence | Create domain types now and defer JPA entities/repositories to a later task. | More aligned with strict Clean Architecture layering, but conflicts with task 004's confirmed scope to create persistent operation metadata and repository boundaries as needed. | Rejected for now |

## Consequences

- Task 004 may create a shared operation model and persistence mapping for common operation metadata.
- The implementation must preserve `Operation` as a domain concept with shared lifecycle semantics, not a generic database row.
- Operation type and status should be represented explicitly for conversion and URL download lifecycle tracking.
- Result metadata may include internal server-side file location data, but raw filesystem paths must not be exposed through public API contracts.
- Operation events remain out of scope for task 004 and belong to the dedicated event model task.
- If future tasks identify divergent flow-specific lifecycle rules, the shared model decision must be revisited.

## Task Impact

- Related task plan: `docs/task-plans/mvp-media-utility/004-create-operation-domain-model-plan.md`
- Related architecture decision notes: `docs/architecture/task-decisions/mvp-media-utility/004-create-operation-domain-model-architecture-decisions.md`
- Unlock effect: Resolves the task 004 architecture blocker for the persistent operation metadata model.
- Remaining blockers: None for MVP-MEDIA-004 after the task plan and architecture decision notes are updated to reference this ADR.

## Source References

- `docs/tasks/mvp-media-utility/004-create-operation-domain-model.md`: Scope, implementation instructions, validation, and acceptance criteria.
- `docs/task-plans/mvp-media-utility/004-create-operation-domain-model-plan.md`: Blocked plan and pending architecture decisions.
- `docs/architecture/task-decisions/mvp-media-utility/004-create-operation-domain-model-architecture-decisions.md`: Blocked architecture decision notes.
- `docs/adrs/001-use-java-and-spring-boot-modular-monolith.md`: Accepted monolith architecture.
- Current codebase: Spring Data JPA and MySQL configuration in `pom.xml` and `src/main/resources/application.properties`.
- User confirmation: current `resolve-architecture-blocker` session accepted a shared persistent `Operation` model with the domain-concept limitation and chose `Accepted + Ready`.
