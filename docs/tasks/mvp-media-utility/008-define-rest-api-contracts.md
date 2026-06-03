# Task: Define REST API Contracts

## Status

Status: Ready for Planning

Last updated: 2026-06-02

## Task ID

ID: MVP-MEDIA-008

Order: 008

Task file: `docs/tasks/mvp-media-utility/008-define-rest-api-contracts.md`

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project discovery | `docs/context/project-discover.md` | Project Scenario | Confirmed by source document | Required Harness context exists, but it predates the current implementation codebase. |
| Project planning | `docs/planning/project-planning.md` | Functional Scope, Task Breakdown, Result Delivery and Temporary Files | Confirmed by source document | Confirms single operation selector flow, MP4-to-MP3 conversion, URL/public download, immediate result download, cleanup, and success/failure tracking. |
| Task 002 | `docs/tasks/mvp-media-utility/002-create-backend-module-boundaries.md` | Scope, Out of Scope, Implementation Instructions | Confirmed by source document | Confirms API/controllers must stay thin and must not call FFmpeg or yt-dlp directly. |
| Task plan 002 | `docs/task-plans/mvp-media-utility/002-create-backend-module-boundaries-plan.md` | Out of Scope, Confirmed Decisions, Notes for the Implementing Agent | Confirmed by source document | Confirms REST DTOs, routes, and complete contracts were intentionally deferred to later tasks. |
| Task 004 execution report | `docs/task-executions/mvp-media-utility/004-create-operation-domain-model-execution.md` | Implemented Changes | Confirmed by source document | Confirms shared `Operation`, `OperationType`, `OperationStatus`, and `ResultFileMetadata` exist. |
| Task 005 execution report | `docs/task-executions/mvp-media-utility/005-create-operation-events-model-execution.md` | Execution Summary, Out-of-scope behavior | Confirmed by source document | Confirms `OperationEvent` model exists and event emission remains later work. |
| Task 006 execution report | `docs/task-executions/mvp-media-utility/006-create-temporary-storage-service-execution.md` | Implemented Changes, Decisions Used | Confirmed by source document | Confirms storage uses root-relative internal keys and public APIs must not expose raw filesystem paths. |
| Task 007 execution report | `docs/task-executions/mvp-media-utility/007-create-process-execution-adapter-execution.md` | Execution Summary, Decisions Used | Confirmed by source document | Confirms process execution exists under `media.process` and API handlers must not execute processes directly. |
| ADR-001 | `docs/adrs/001-use-java-and-spring-boot-modular-monolith.md` | Decision, Consequences | Accepted | Confirms Java 21 Spring Boot modular monolith exposing REST APIs. |
| ADR-002 | `docs/adrs/002-use-react-vite-typescript-served-by-spring-boot.md` | Decision, Consequences | Accepted | Confirms frontend/backend communication through REST APIs in the same deployable monolith. |
| ADR-005 | `docs/adrs/005-use-shared-operation-domain-model-for-metadata-persistence.md` | Decision, Consequences | Accepted | Confirms shared operation metadata and prohibits exposing raw filesystem paths through public API contracts. |
| ADR-006 | `docs/adrs/006-use-root-relative-keys-for-temporary-local-storage.md` | Decision, Consequences | Accepted | Confirms `ResultFileMetadata.internalPath` stores root-relative storage keys and public APIs must not expose absolute paths. |
| ADR-007 | `docs/adrs/007-define-process-execution-contract-for-media-tools.md` | Decision, Consequences | Accepted | Confirms process execution must use the backend process adapter, not direct API handler execution. |
| ADR-008 | `docs/adrs/008-define-public-rest-api-contract.md` | Decision, Consequences, Task Impact | Accepted / Resolved by ADR | Confirms endpoint paths, request shapes, public response metadata, error schema, HTTP status mapping, and direct result download behavior. |
| PRD | `docs/product/prd.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |
| Tech Spec | `docs/specs/tech-spec.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |
| Technology definition | `docs/architecture/technology-definition.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |

## Context

The MVP now has backend package boundaries, operation metadata, operation events, temporary local storage, and process execution foundations. The `api` package exists only as a REST/API boundary marker. Complete REST routes, public DTOs, request validation shape, response metadata, and error contracts were intentionally deferred from earlier tasks.

This task should define the public backend REST contract used by the React frontend for media conversion, URL download, operation/result status, and result download handoff. ADR-008 resolves the original REST wire contract blocker and makes this task ready for implementation planning.

## Goal

Define the public backend REST contract for conversion, URL download, operation/result status, and result download handoff without implementing media processing or endpoint behavior.

## Scope

- Define REST endpoint contract decisions for MP4 conversion submission, URL download submission, operation/result status, and result download handoff.
- Define request and response DTO shapes only after the REST contract is confirmed.
- Define error response shape and HTTP status mapping only after the REST contract is confirmed.
- Ensure public responses never expose `ResultFileMetadata.internalPath`, root-relative storage keys, or absolute filesystem paths.
- Keep API handlers thin and oriented toward validation and orchestration handoff.
- Add contract-level tests only after the REST contract is confirmed.

## Out of Scope

- Do not implement controller behavior before the REST contract is confirmed.
- Do not implement FFmpeg conversion behavior.
- Do not implement yt-dlp download behavior.
- Do not execute external processes directly from API classes.
- Do not modify storage implementation behavior.
- Do not implement cleanup jobs or retention behavior.
- Do not emit operation events.
- Do not implement frontend integration.
- Do not change operation/domain persistence models unless a later approved plan explicitly requires it.

## Implementation Instructions

- Do not implement this task until the REST API contract is confirmed in an architecture decision or task implementation plan.
- Use the existing `src/main/java/com/lucasdourado/mediautility/api/` package for public REST boundary types after the blocker is resolved.
- Keep media processing, process execution, storage resolution, cleanup, persistence details, and event emission outside API classes.
- Public contracts must use safe operation/result metadata and must not expose internal storage paths or keys.
- Preserve the existing Spring Boot modular monolith and React-through-REST architecture.
- Document the confirmed endpoint paths, request DTOs, response DTOs, error schema, and HTTP status mapping before implementation.

## Expected Files

| Path | Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasdourado/mediautility/api/` | Create / Modify | ADR-002, task 002, current codebase | REST DTOs, controller contracts, exception/error contract classes, or controller skeletons after decisions are confirmed. |
| `src/test/java/com/lucasdourado/mediautility/api/` | Create | Validation need | Focused REST contract tests after decisions are confirmed. |
| `docs/architecture/task-decisions/mvp-media-utility/008-define-rest-api-contracts-architecture-decisions.md` | Create / Update | Required blocker resolution | Should record REST API contract decisions before implementation. |
| `docs/adrs/` | Create if required | Architecture blocker | A future ADR may be required if the REST wire contract is treated as a long-lived architecture decision. |

## Dependencies

| Dependency | Type | Status | Notes |
| --- | --- | --- | --- |
| MVP-MEDIA-001 | Previous task | Completed | Spring Boot and React monolith foundation exists. |
| MVP-MEDIA-002 | Previous task | Completed | API boundary package exists and complete contracts were deferred to later tasks. |
| MVP-MEDIA-004 | Previous task | Completed | Shared operation metadata model exists. |
| MVP-MEDIA-005 | Previous task | Completed | Operation event model exists, but event emission remains out of scope. |
| MVP-MEDIA-006 | Previous task | Completed with Follow-ups | Temporary storage service exists with root-relative internal keys. |
| MVP-MEDIA-007 | Previous task | Completed with Follow-ups | Process execution adapter exists and must remain outside API handlers. |
| REST API contract decision | Architecture decision | Resolved by ADR | ADR-008 confirms endpoint paths, DTO fields, multipart field names, result public identifier, HTTP status mapping, error schema, and direct result download behavior. |
| Tech Spec API details | Documentation | Missing | `docs/specs/tech-spec.md` is empty in the current workspace. |
| PRD API behavior details | Documentation | Missing | `docs/product/prd.md` is empty in the current workspace. |
| Technology definition API details | Documentation | Missing | `docs/architecture/technology-definition.md` is empty in the current workspace. |

## Validation

- Backend compiles after implementation.
- API contract tests verify confirmed DTO serialization and deserialization behavior.
- MockMvc or equivalent Spring MVC tests verify confirmed routes, HTTP methods, request shapes, response shapes, and status codes.
- Tests verify public result/status responses do not expose raw filesystem paths, root-relative internal storage keys, or `ResultFileMetadata.internalPath`.
- Scope review verifies no media processing, direct process execution, storage implementation behavior, cleanup scheduling, or event emission was added to API contract work.

## Acceptance Criteria

- [x] A confirmed REST API contract decision exists before implementation.
- [ ] Public API request and response DTOs are defined only from confirmed contract decisions.
- [ ] Conversion and URL download contracts are distinguishable and align with `OperationType`.
- [ ] Public result/status responses expose safe operation/result metadata only.
- [ ] Error responses and HTTP statuses follow the confirmed contract.
- [ ] Public contracts do not expose absolute filesystem paths, root-relative storage keys, or `ResultFileMetadata.internalPath`.
- [ ] API package remains free of FFmpeg, yt-dlp, `ProcessBuilder`, storage path resolution, cleanup scheduling, and event emission behavior.
- [ ] Focused REST contract tests cover the confirmed contract shape.

## Risks

- Defining endpoint paths or DTO fields without a confirmed contract can create unstable frontend/backend coupling.
- Exposing internal operation IDs or storage keys without a confirmed public identifier strategy could create security and compatibility issues.
- Accidentally exposing `ResultFileMetadata.internalPath` would leak backend storage implementation details.
- Implementing controllers before the contract is confirmed could mix API design with orchestration and media processing decisions.
- Empty Tech Spec, PRD, and technology-definition files reduce confidence until the REST contract blocker is resolved.

## Open Questions

None for implementation planning. ADR-008 resolves the task-relevant REST API contract questions.

## Notes for the Implementing Agent

- Do not execute this task directly before `plan-task` creates an implementation plan.
- Treat ADR-008 as binding for endpoint paths, request DTOs, response DTOs, error schema, HTTP status mapping, and direct result download behavior.
- Treat ADR-006 as binding: raw filesystem paths and root-relative storage keys remain internal.
- Treat ADR-007 as binding: API handlers must not execute external commands.
- Keep actual operation handling for later endpoint implementation tasks.
