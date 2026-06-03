# Task Architecture Decision Notes: Define REST API Contracts

## Status

Status: Ready for Implementation

Last updated: 2026-06-02

Decision notes file: `docs/architecture/task-decisions/mvp-media-utility/008-define-rest-api-contracts-architecture-decisions.md`

Task plan file: `docs/task-plans/mvp-media-utility/008-define-rest-api-contracts-plan.md`

Task file: `docs/tasks/mvp-media-utility/008-define-rest-api-contracts.md`

## Purpose

Record task-specific architecture decisions, conflicts, and confirmed ADR candidates identified while preparing the task implementation plan.

This file is planning support only. It is not a final ADR and must not replace the ADR workflow.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/tasks/mvp-media-utility/008-define-rest-api-contracts.md` | Status, Scope, Dependencies, Open Questions | Confirmed by source document | Defines the REST contract task and confirms ADR-008 resolved the prior blocker. |
| Project planning | `docs/planning/project-planning.md` | Functional Scope, Result Delivery, Media Conversion, Media Download | Confirmed by source document | Confirms conversion, URL download, immediate result download, and operation flow needs. |
| ADR-001 | `docs/adrs/001-use-java-and-spring-boot-modular-monolith.md` | Decision, Consequences | Accepted | Confirms Spring Boot modular monolith. |
| ADR-002 | `docs/adrs/002-use-react-vite-typescript-served-by-spring-boot.md` | Decision, Consequences | Accepted | Confirms React frontend communicates with backend through REST APIs. |
| ADR-005 | `docs/adrs/005-use-shared-operation-domain-model-for-metadata-persistence.md` | Decision, Consequences | Accepted | Confirms shared operation metadata and prohibits exposing raw paths through public API contracts. |
| ADR-006 | `docs/adrs/006-use-root-relative-keys-for-temporary-local-storage.md` | Decision, Consequences, Task Impact | Accepted | Confirms root-relative storage keys remain internal. |
| ADR-007 | `docs/adrs/007-define-process-execution-contract-for-media-tools.md` | Decision, Consequences, Task Impact | Accepted | Confirms API handlers must not execute external commands directly. |
| ADR-008 | `docs/adrs/008-define-public-rest-api-contract.md` | Decision, Consequences, Task Impact | Accepted / Resolved by ADR | Resolves the public REST API contract blocker for MVP-MEDIA-008. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/api/` | API boundary | Detected in codebase | API package currently has only boundary marker files. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/operations/` | Operation model | Detected in codebase | Operation type/status/result metadata exist and can be mapped into public DTOs. |
| User decision | Current `plan-task` session | Contract surface | Confirmed by user | Implement DTOs plus thin controllers. |
| User decision | Current `plan-task` session | Controller behavior boundary | Confirmed by user | Controllers delegate to a future orchestration port mocked in tests; no concrete processing implementation is created. |
| User decision | Current `plan-task` session | Validation approach | Confirmed by user | Use Bean Validation and add `spring-boot-starter-validation` if needed. |
| User decision | Current `plan-task` session | MP4 validation | Confirmed by user | Require multipart field `file`, non-empty file, `video/mp4`, and `.mp4` filename. |
| User decision | Current `plan-task` session | URL validation | Confirmed by user | Require absolute public `http` or `https` URL. |
| User decision | Current `plan-task` session | Error code set | Confirmed by user | Use `VALIDATION_ERROR`, `UNSUPPORTED_MEDIA_TYPE`, `PAYLOAD_TOO_LARGE`, `NOT_FOUND`, `CONFLICT`, and `INTERNAL_ERROR`. |
| User decision | Current `plan-task` session | Failed operation error shape | Confirmed by user | Reuse the public error object shape for failed operation response `error`. |

## Confirmed Architecture Decisions

| Decision | Source | Impact on This Task | Notes |
| --- | --- | --- | --- |
| Backend exposes REST APIs from the Spring Boot monolith. | ADR-001, ADR-002 | REST contracts belong under the existing backend API boundary. | Accepted ADR constraint. |
| Conversion creation endpoint is `POST /api/operations/conversions`. | ADR-008 | Contract tests and DTOs must use this route. | Resolved by ADR. |
| URL download creation endpoint is `POST /api/operations/downloads`. | ADR-008 | Contract tests and DTOs must use this route. | Resolved by ADR. |
| Operation status endpoint is `GET /api/operations/{operationId}`. | ADR-008 | Status DTO must expose safe public operation metadata. | Resolved by ADR. |
| Result download endpoint is `GET /api/operations/{operationId}/result`. | ADR-008 | Result download is direct when available. | Resolved by ADR. |
| Conversion upload uses multipart field `file`. | ADR-008, user decision | Validation and serialization tests must use `file`. | Resolved by ADR and confirmed by user. |
| URL download request uses JSON body with `url`. | ADR-008, user decision | Validation and serialization tests must use `{ "url": "..." }`. | Resolved by ADR and confirmed by user. |
| Public operation responses use numeric `operationId`. | ADR-008 | DTOs may expose the operation identifier but not storage identifiers. | Resolved by ADR. |
| Public result metadata includes `fileName`, `contentType`, `sizeBytes`, and `downloadUrl`. | ADR-008 | Result DTO must translate internal metadata into safe public fields. | Resolved by ADR. |
| Public errors use `code`, `message`, and optional field `details`. | ADR-008, user decision | Error contract tests should verify this shape and the fixed code set. | Resolved by ADR and confirmed by user. |
| Failed operation response `error` reuses the public error object shape. | User decision | Operation status DTO can represent failed operations without inventing another error schema. | Confirmed by user. |
| Public API responses must not expose internal paths or storage keys. | ADR-005, ADR-006, ADR-008 | DTOs must not expose `ResultFileMetadata.internalPath`. | Accepted ADR constraint. |
| API handlers must not execute FFmpeg, yt-dlp, `ProcessBuilder`, or shell commands directly. | ADR-007, task file | API classes stay validation/delegation oriented. | Accepted ADR constraint. |
| Controllers are part of task 008 but must be thin and delegate. | User decision | Route surfaces can be implemented and tested without real processing behavior. | Confirmed by user. |
| Bean Validation is the selected request validation mechanism. | User decision | Implementation may add validation dependency and annotations. | Confirmed by user. |

## Pending Architecture Decisions

None. All task-relevant architecture decisions have been answered, resolved by ADR-008, or confirmed by the user during planning.

## Architecture Conflicts

| Conflict | Sources | Impact | Resolution Status |
| --- | --- | --- | --- |
| Task 008 needs exact endpoint paths, DTO shapes, error schema, and HTTP statuses, but Tech Spec, PRD, and technology-definition are empty. | Task 008 vs empty source documents | Implementation would require inventing REST contract details. | Resolved for task 008 by ADR-008 and current planning decisions. |
| Current operation metadata includes internal result storage data, but public status/result responses need safe metadata. | ADR-005, ADR-006, task 008 | Public DTOs could accidentally expose backend storage details. | Resolved by ADR-008 and explicit safe metadata constraints. |
| Controllers are needed for route contract tests, but real operation orchestration is out of scope. | Task file, user decision | A concrete processing service would exceed scope. | Resolved by using a thin controller plus delegation port with no concrete processing implementation in this task. |
| Bean Validation is selected but current `pom.xml` does not explicitly declare validation starter. | User decision, codebase evidence | Validation annotations may be unavailable without dependency support. | Resolved by allowing `spring-boot-starter-validation` addition if needed. |

## ADR Candidates

| Candidate | Trigger | Suggested ADR Scope | Blocking for This Task? |
| --- | --- | --- | --- |
| Public REST API contract | Task 008 requires stable wire contract before DTOs and tests can be implemented. | Covered by ADR-008. | No; resolved by accepted ADR. |

## Implementation Impact

- Task 008 can proceed to implementation after this task plan and updated architecture notes are saved.
- DTOs, controller routes, validation, and tests must follow ADR-008 and the user-confirmed planning decisions.
- Controllers must be thin and delegate to an API port/interface; this task must not create real processing/orchestration behavior.
- Bean Validation should be used for request validation; dependency changes are allowed only as needed to support that contract.
- Public API models must expose safe operation/result metadata only.
- `ResultFileMetadata.internalPath`, root-relative storage keys, absolute paths, and storage implementation details must remain internal.
- API handlers must not execute media tools, resolve storage paths directly, schedule cleanup, or emit operation events in this task.

## Questions for the User

None. All task-relevant architecture questions have been answered.

## Notes for the Implementing Agent

- This file is planning support only; it is not a final ADR.
- Use ADR-008 as the binding public REST API contract source.
- Use this task plan for task-specific decisions not fully spelled out in ADR-008.
- Keep actual operation orchestration, media processing, frontend integration, cleanup, and event emission out of task 008.
- Do not expose internal storage paths or storage keys in public DTOs.
