# Task Implementation Plan: Define REST API Contracts

## Status

Status: Ready for Implementation

Last updated: 2026-06-02

Plan file: `docs/task-plans/mvp-media-utility/008-define-rest-api-contracts-plan.md`

Architecture decision notes file: `docs/architecture/task-decisions/mvp-media-utility/008-define-rest-api-contracts-architecture-decisions.md`

## Task Reference

Task ID: `MVP-MEDIA-008`

Task file: `docs/tasks/mvp-media-utility/008-define-rest-api-contracts.md`

Task status: `Ready for Planning`

Task group or feature: `mvp-media-utility`

## Planning Mode Requirement

Plan mode verified: Yes

Notes:

- This plan was created in plan mode.
- Implementation must not start during `plan-task`.
- ADR-008 resolves the public REST API contract blocker.
- A future implementation request must use this saved plan, the saved architecture decision notes, and ADR-008 as source context.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project discovery | `docs/context/project-discover.md` | Project Scenario | Confirmed by source document | Required Harness discovery context exists, but it predates the current implementation codebase. |
| Project planning | `docs/planning/project-planning.md` | Functional Scope, Media Conversion, Media Download, Result Delivery | Confirmed by source document | Confirms single operation selector flow, MP4-to-MP3 conversion, public URL download, immediate result download, and success/failure tracking. |
| Task file | `docs/tasks/mvp-media-utility/008-define-rest-api-contracts.md` | Scope, Dependencies, Validation, Acceptance Criteria | Confirmed by source document | Defines the REST API contract task scope and readiness after ADR-008. |
| Task plan 002 | `docs/task-plans/mvp-media-utility/002-create-backend-module-boundaries-plan.md` | Out of Scope, Confirmed Decisions, Notes for Implementing Agent | Confirmed by source document | Confirms complete REST contracts were intentionally deferred to later tasks. |
| Task 004 execution report | `docs/task-executions/mvp-media-utility/004-create-operation-domain-model-execution.md` | Implemented Changes | Confirmed by source document | Confirms `Operation`, `OperationType`, `OperationStatus`, and `ResultFileMetadata` exist. |
| Task 006 execution report | `docs/task-executions/mvp-media-utility/006-create-temporary-storage-service-execution.md` | Implemented Changes, Decisions Used | Confirmed by source document | Confirms storage uses root-relative internal keys and public APIs must not expose raw filesystem paths. |
| Task 007 execution report | `docs/task-executions/mvp-media-utility/007-create-process-execution-adapter-execution.md` | Execution Summary, Decisions Used | Confirmed by source document | Confirms process execution exists under `media.process` and API handlers must not execute processes directly. |
| ADR-001 | `docs/adrs/001-use-java-and-spring-boot-modular-monolith.md` | Decision, Consequences | Accepted | Confirms Java 21 Spring Boot modular monolith. |
| ADR-002 | `docs/adrs/002-use-react-vite-typescript-served-by-spring-boot.md` | Decision, Consequences | Accepted | Confirms frontend/backend communication through REST APIs. |
| ADR-005 | `docs/adrs/005-use-shared-operation-domain-model-for-metadata-persistence.md` | Decision, Consequences | Accepted | Prohibits exposing raw filesystem paths through public API contracts. |
| ADR-006 | `docs/adrs/006-use-root-relative-keys-for-temporary-local-storage.md` | Decision, Consequences | Accepted | Confirms root-relative storage keys remain internal. |
| ADR-007 | `docs/adrs/007-define-process-execution-contract-for-media-tools.md` | Decision, Consequences | Accepted | Confirms API handlers must not execute external commands directly. |
| ADR-008 | `docs/adrs/008-define-public-rest-api-contract.md` | Decision, Consequences, Task Impact | Accepted / Resolved by ADR | Confirms endpoint paths, request shapes, response metadata, error schema, HTTP status mapping, and direct result download behavior. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/api/` | API boundary | Detected in codebase | `ApiBoundary` and package documentation exist; no REST DTOs or controllers exist yet. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/operations/` | Operation model | Detected in codebase | Operation domain model already has type, status, timestamps, failure reason, and result metadata. |
| Current codebase | `pom.xml` | Dependencies | Detected in codebase | Spring WebMVC and WebMVC test dependencies exist; Bean Validation starter is not currently declared. |
| Tech Spec | `docs/specs/tech-spec.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |
| Technology definition | `docs/architecture/technology-definition.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |
| User decision | Current `plan-task` session | Contract surface | Confirmed by user | Implement DTOs plus thin controllers. |
| User decision | Current `plan-task` session | Controller behavior boundary | Confirmed by user | Controllers delegate to a future orchestration port mocked in tests; no real processing implementation is created in this task. |
| User decision | Current `plan-task` session | Validation approach | Confirmed by user | Use Bean Validation and add `spring-boot-starter-validation` if needed. |
| User decision | Current `plan-task` session | MP4 validation | Confirmed by user | Contract requires multipart field `file`, content type `video/mp4`, and `.mp4` filename; no byte inspection in this task. |
| User decision | Current `plan-task` session | URL validation | Confirmed by user | Contract requires an absolute public `http` or `https` URL and rejects empty, malformed, or non-http schemes. |
| User decision | Current `plan-task` session | Error code set | Confirmed by user | Use `VALIDATION_ERROR`, `UNSUPPORTED_MEDIA_TYPE`, `PAYLOAD_TOO_LARGE`, `NOT_FOUND`, `CONFLICT`, and `INTERNAL_ERROR`. |
| User decision | Current `plan-task` session | Failed operation error shape | Confirmed by user | Reuse the public error object shape for the operation response `error` field. |

## Context Summary

The MVP backend has package boundaries, operation metadata, temporary storage, and process execution foundations. The `api` package is currently only a boundary marker.

Task 008 defines the public REST contract used by the React frontend for conversion, URL download, operation status, and result download. ADR-008 is accepted and provides the binding wire contract. This task should now add public API DTOs, thin controller route contracts, error contract types, and contract tests without implementing media processing or real operation orchestration.

## Task Goal

Define the public REST API contract for conversion submission, URL download submission, operation status, error responses, and direct result download by adding DTOs, controller route contracts, a delegation port, and focused contract tests.

## Confirmed Scope

- Create public request, response, result, links, and error DTOs under the existing API boundary.
- Create thin Spring WebMVC controllers for:
  - `POST /api/operations/conversions`
  - `POST /api/operations/downloads`
  - `GET /api/operations/{operationId}`
  - `GET /api/operations/{operationId}/result`
- Use `multipart/form-data` field `file` for conversion uploads.
- Use JSON body `{ "url": "https://..." }` for URL download creation.
- Use numeric `operationId` in public operation responses and routes.
- Use safe result metadata only: `fileName`, `contentType`, `sizeBytes`, and `downloadUrl`.
- Reuse public error object shape for HTTP errors and failed operation response `error`.
- Add a small fixed public error code set: `VALIDATION_ERROR`, `UNSUPPORTED_MEDIA_TYPE`, `PAYLOAD_TOO_LARGE`, `NOT_FOUND`, `CONFLICT`, and `INTERNAL_ERROR`.
- Add a controller delegation port/interface for future orchestration and mock it in contract tests.
- Add Bean Validation support and request validation where applicable.
- Validate conversion upload contract with required non-empty file, `video/mp4` content type, and `.mp4` filename.
- Validate URL download contract with required absolute `http` or `https` URL.
- Add focused REST contract tests with MockMvc or equivalent Spring WebMVC tests.

## Out of Scope

- Do not implement real media conversion behavior.
- Do not implement real public URL download behavior.
- Do not implement real operation orchestration.
- Do not implement a concrete delegation port service.
- Do not execute FFmpeg, yt-dlp, `ProcessBuilder`, or shell commands from API classes.
- Do not resolve storage paths or expose storage keys from API DTOs.
- Do not implement cleanup jobs, retention behavior, or event emission.
- Do not implement frontend integration.
- Do not change operation/domain persistence models unless a later approved plan explicitly requires it.
- Do not update PRD, project planning, technology definition, Tech Spec, final ADR files, task files, or unrelated documents during implementation.

## Requirements Covered

| Requirement | Source | How This Task Covers It | Status |
| --- | --- | --- | --- |
| Public REST contract exists before implementation | Task file, ADR-008 | Adds DTOs, route declarations, error contract, and tests from ADR-008. | Ready |
| Conversion and URL download contracts are distinguishable | Task file, ADR-008 | Uses separate creation endpoints and `OperationType`-aligned response type values. | Ready |
| Safe public result/status metadata | ADR-005, ADR-006, ADR-008 | Public DTOs expose only safe fields and not `internalPath` or storage keys. | Ready |
| Error responses and HTTP statuses follow confirmed contract | Task file, ADR-008, user decision | Adds fixed error code set and tests expected status mappings. | Ready |
| API handlers stay thin | Task 002, ADR-007, user decision | Controllers validate and delegate to a port; no processing implementation is created. | Ready |
| Focused REST contract tests cover the shape | Task file, user decision | Adds MockMvc/WebMVC tests for routes, request validation, response shape, status mapping, and forbidden internal fields. | Ready |

## Technical Specification Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Not available | Missing | Task cannot rely on Tech Spec content because `docs/specs/tech-spec.md` is empty. | Source limitation remains documented. ADR-008 and user decisions resolve the task-specific contract details. |

Coverage assessment:

- Justifying Tech Spec section: unavailable because the Tech Spec file is empty.
- Tech Spec sections implemented by this task: none directly from Tech Spec content.
- Gaps between task and Tech Spec: endpoint paths, request DTOs, response DTOs, validation, error schema, HTTP status mapping, and result download behavior are not documented there.
- Dependencies not specified by the Tech Spec: resolved by ADR-008 and current `plan-task` user decisions.
- Source limitation handling: the empty Tech Spec remains a documentation limitation but does not block task 008 because ADR-008 and user-confirmed planning decisions define the task contract.

## Architecture and ADR Considerations

| Decision or ADR | Source | Impact on This Task | Status |
| --- | --- | --- | --- |
| Java 21 Spring Boot modular monolith | ADR-001 | REST contracts live inside the existing backend application. | Accepted |
| React communicates with backend through REST | ADR-002 | API contracts serve the frontend/backend boundary. | Accepted |
| Raw filesystem paths must not be public | ADR-005 | Public DTOs must not expose internal storage data. | Accepted |
| Root-relative storage keys remain internal | ADR-006 | Public result DTOs must not expose `ResultFileMetadata.internalPath`. | Accepted |
| API handlers must not execute media tools directly | ADR-007 | Controllers must only validate and delegate. | Accepted |
| Public REST API contract | ADR-008 | Defines endpoint paths, payloads, response shape, error schema, status mapping, and direct result download. | Resolved by ADR |
| Controllers plus delegation port | User decision | Allows route contracts and MockMvc tests without implementing real orchestration. | Confirmed |
| Bean Validation | User decision | Adds validation annotations and dependency support if needed. | Confirmed |
| MP4 request validation | User decision | Requires `file`, `video/mp4`, and `.mp4` filename. | Confirmed |
| URL request validation | User decision | Requires absolute `http` or `https` URL. | Confirmed |
| Error code set | User decision | Keeps public error codes small and stable for MVP. | Confirmed |

ADR candidates or architecture decisions needed:

- None. ADR-008 resolves the required public REST contract blocker.
- No architecture blocker remains for task 008.

Architecture decision notes:

- Saved separately: Yes
- Path: `docs/architecture/task-decisions/mvp-media-utility/008-define-rest-api-contracts-architecture-decisions.md`
- Notes file status: Existing file overwritten with updated planning decisions

## Confirmed Decisions

- The selected task is `MVP-MEDIA-008`.
- The plan path is `docs/task-plans/mvp-media-utility/008-define-rest-api-contracts-plan.md`.
- The architecture decision notes path is `docs/architecture/task-decisions/mvp-media-utility/008-define-rest-api-contracts-architecture-decisions.md`.
- ADR-008 is accepted and binding.
- The implementation should create DTOs plus thin controllers.
- Controllers should delegate to a future orchestration port/interface and should not create a concrete processing implementation in this task.
- Bean Validation should be used; add `spring-boot-starter-validation` if the implementation needs it.
- Conversion upload validation requires multipart field `file`, non-empty upload, `video/mp4` content type, and `.mp4` filename.
- URL download validation requires a non-empty absolute `http` or `https` URL.
- Error codes are `VALIDATION_ERROR`, `UNSUPPORTED_MEDIA_TYPE`, `PAYLOAD_TOO_LARGE`, `NOT_FOUND`, `CONFLICT`, and `INTERNAL_ERROR`.
- Failed operation responses reuse the public error object shape in the `error` field.
- Public result/status responses must never expose `ResultFileMetadata.internalPath`, root-relative storage keys, absolute filesystem paths, or storage implementation details.
- Result download is direct from `GET /api/operations/{operationId}/result` when available.

## Pending Decisions

None. All task-relevant decisions have been answered, resolved by ADR-008, or explicitly kept out of this task.

## Questions for the User

None. All task-relevant questions have been answered.

## Proposed Implementation Approach

1. Re-read the task file, this plan, the architecture decision notes, ADR-008, and current `api`/`operations` code.
2. Add API DTOs for conversion request binding, URL download request, operation response, result metadata, links, and public errors.
3. Add a public error code enum or equivalent stable type with the confirmed small fixed set.
4. Add a controller delegation port/interface in the API boundary that represents future operation orchestration and result download handoff.
5. Add thin controllers for the four ADR-008 endpoints that validate requests and delegate to the port.
6. Add Bean Validation dependency/configuration if required by the current Spring Boot setup.
7. Add API exception/error handling that maps validation and port-reported outcomes to the ADR-008 HTTP status mapping.
8. Add focused WebMVC/MockMvc contract tests with mocked port behavior for happy paths, validation failures, status mappings, response serialization, and safe metadata.
9. Perform a scope review confirming no processing, storage path resolution, event emission, cleanup, frontend integration, or concrete orchestration implementation was added.

## Files and Areas Expected to Change

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasdourado/mediautility/api/` | Create / Modify | Task file, ADR-008, user decisions | Public DTOs, controllers, delegation port, validation helpers if needed, and error contract classes. |
| `src/test/java/com/lucasdourado/mediautility/api/` | Create | Task validation, ADR-008, user decisions | Focused REST contract tests using MockMvc/WebMVC with mocked delegation port. |
| `pom.xml` | Modify if needed | User decision, codebase evidence | Add `spring-boot-starter-validation` if Bean Validation annotations are not available through existing dependencies. |
| `src/main/resources/application.properties` | Inspect only by default | ADR-008 | Do not add runtime processing config in this task; multipart limits may remain existing/default unless implementation needs an explicit contract-test setting. |

## Step-by-Step Implementation Plan

1. Verify working tree state before editing and preserve unrelated user changes.
2. Inspect existing API, operation, storage, and process packages for naming and boundary conventions.
3. Add or confirm Bean Validation support:
   - add `spring-boot-starter-validation` to `pom.xml` if validation annotations are not already available;
   - use validation only for request contract checks, not business processing.
4. Define public DTOs in the API boundary:
   - URL download request with `url`;
   - operation response with `operationId`, `type`, `status`, `createdAt`, `completedAt`, `expiresAt`, `result`, `error`, and `links`;
   - public result metadata with `fileName`, `contentType`, `sizeBytes`, and `downloadUrl`;
   - links object with at least `status`;
   - public error object with `code`, `message`, and optional field `details`;
   - error detail with `field` and `message`.
5. Define the fixed public error code set: `VALIDATION_ERROR`, `UNSUPPORTED_MEDIA_TYPE`, `PAYLOAD_TOO_LARGE`, `NOT_FOUND`, `CONFLICT`, and `INTERNAL_ERROR`.
6. Define a small API delegation port/interface for future orchestration:
   - conversion creation accepts the uploaded file contract input and returns operation response data;
   - URL download creation accepts validated URL input and returns operation response data;
   - status lookup accepts `operationId` and returns operation response data;
   - result download accepts `operationId` and returns file/resource response data needed by the controller.
7. Implement thin controllers:
   - `POST /api/operations/conversions` consumes multipart form data and field `file`;
   - `POST /api/operations/downloads` consumes JSON and body `url`;
   - `GET /api/operations/{operationId}` returns operation status DTO;
   - `GET /api/operations/{operationId}/result` returns the result file directly when the port reports it available.
8. Keep controller logic limited to request binding, validation, status/header mapping, and delegation.
9. Add validation/error handling:
   - missing or empty `file` maps to `400 Bad Request` with `VALIDATION_ERROR`;
   - non-`video/mp4` content type or non-`.mp4` filename maps to `415 Unsupported Media Type`;
   - empty, malformed, or non-http/https URL maps to `400 Bad Request` with `VALIDATION_ERROR`;
   - operation/result not found maps to `404 Not Found`;
   - unavailable, failed, or expired result maps to `409 Conflict`;
   - oversized upload maps to `413 Payload Too Large`;
   - unexpected backend errors map to `500 Internal Server Error`.
10. Add contract tests:
    - verify endpoint paths, HTTP methods, content types, and response statuses;
    - verify JSON request/response serialization;
    - verify multipart field name `file`;
    - verify URL validation behavior;
    - verify MP4 content type and filename validation behavior;
    - verify public operation response shape for pending and completed operations;
    - verify failed operation response uses the public error object in `error`;
    - verify public result/status responses do not include `internalPath`, storage keys, absolute paths, or storage implementation details;
    - verify result download delegates and returns direct file response when available;
    - verify error code/status mappings.
11. Run the repository's established Maven validation command if executing this plan later; document any environment blockers in the execution report.
12. Scope-review the diff to ensure no media processing, concrete orchestration, storage path resolution, cleanup, event emission, or frontend integration was added.

## Validation Strategy

- Run backend Maven tests through the repository's established Maven path.
- Run focused API WebMVC/MockMvc tests.
- Verify backend compiles with Bean Validation dependency and API DTO/controller additions.
- Verify route contracts match ADR-008 exactly.
- Verify public JSON does not expose internal storage fields.
- Verify controllers delegate through the API port and do not execute process, storage, cleanup, persistence, or event behavior directly.
- Document any validation environment blockers in the future task execution report.

## Tests to Add or Update

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| Conversion creation route | Contract / WebMVC | Verify `POST /api/operations/conversions`, multipart `file`, `201 Created`, and operation response shape. | Mock the delegation port. |
| Conversion upload validation | Contract / WebMVC | Verify missing/empty file maps to `400`, unsupported media type or filename maps to `415`. | Use `video/mp4` plus `.mp4` for valid case. |
| URL download creation route | Contract / WebMVC | Verify `POST /api/operations/downloads`, JSON `url`, `201 Created`, and operation response shape. | Mock the delegation port. |
| URL validation | Contract / WebMVC | Verify empty, malformed, and non-http/https URLs map to `400`. | No real network calls. |
| Operation status route | Contract / WebMVC | Verify `GET /api/operations/{operationId}` returns safe operation response for pending/completed/failed examples. | Include failed operation `error` object. |
| Result download route | Contract / WebMVC | Verify `GET /api/operations/{operationId}/result` directly returns file content, content type, and filename headers when available. | Mock the delegation port result. |
| Error mapping | Contract / WebMVC | Verify `404`, `409`, `413`, `415`, and `500` use the public error shape and fixed codes. | Use controller advice or equivalent error layer. |
| Safe metadata serialization | Contract / Serialization | Verify public responses do not serialize `internalPath`, root-relative keys, absolute paths, or storage implementation details. | Include completed operation fixture backed by internal metadata. |
| Scope review | Manual/static | Verify no media processing, concrete orchestration, cleanup, event emission, frontend integration, or storage path resolution was added. | Required after implementation. |

## Acceptance Criteria

- [x] A confirmed REST API contract decision exists before implementation.
- [ ] Public API request and response DTOs are defined from ADR-008 and current user-confirmed planning decisions.
- [ ] Thin controllers expose the four ADR-008 routes.
- [ ] Controllers delegate to an API port/interface and do not implement real operation processing.
- [ ] Conversion creation uses multipart field `file`.
- [ ] URL download creation uses JSON body field `url`.
- [ ] Conversion and URL download contracts are distinguishable and align with `OperationType`.
- [ ] Public operation responses expose `operationId`, `type`, `status`, timestamps, optional public result metadata, optional public error, and links.
- [ ] Public result/status responses expose safe operation/result metadata only.
- [ ] Public contracts do not expose absolute filesystem paths, root-relative storage keys, or `ResultFileMetadata.internalPath`.
- [ ] Error responses and HTTP statuses follow ADR-008 and the confirmed fixed error code set.
- [ ] Failed operation response `error` uses the public error object shape.
- [ ] API package remains free of FFmpeg, yt-dlp, `ProcessBuilder`, storage path resolution, cleanup scheduling, event emission behavior, and concrete operation orchestration.
- [ ] Focused REST contract tests cover the confirmed contract shape, validation, status mappings, and safe metadata constraints.

## Risks and Edge Cases

- The Tech Spec and technology-definition files are empty, so ADR-008 and this plan are the binding implementation sources.
- A concrete delegation-port implementation would exceed this task and should be left for later operation orchestration tasks.
- Adding controllers without a bean strategy for the delegation port can break application context; the implementation should either provide test configuration/mocking and avoid requiring a real processing bean in this task, or define a deliberate no-processing boundary that does not perform orchestration.
- Multipart validation can be split between Spring multipart handling and controller validation; tests must cover the public outcome rather than internal exception details.
- `413 Payload Too Large` may be produced by Spring multipart infrastructure; if no explicit upload size limit exists yet, contract tests may cover the error handler shape without inventing product size limits.
- Public result download must not reveal internal storage keys in URLs, headers, logs asserted by tests, or JSON metadata.
- Numeric `operationId` is accepted for MVP; future opaque identifiers may require a later ADR if privacy or abuse requirements change.
- Existing full Maven validation may require datasource environment variables or a clean local build environment; future execution should document any environment blockers.

## Rollback or Recovery Notes

Rollback would remove API DTOs, thin controllers, API delegation port, validation/error contract classes, Bean Validation dependency if added solely for this task, and focused API contract tests, while preserving ADR-008 and existing backend/domain/storage/process boundaries.

If implementation discovers that the delegation-port approach cannot coexist with Spring context startup without a real orchestration bean, record that as a planning deviation and keep any workaround limited to contract exposure, not real processing.

## Documentation Updates

- Do not update PRD, project planning, technology definition, Tech Spec, final ADR files, task files, or unrelated documents during task implementation.
- Package documentation under `api` may be updated to reflect implemented public REST contract types.
- The future task execution report should document DTO names, controller routes, validation behavior, error mapping, tests run, and any environment validation blockers.

## Implementation Readiness Checklist

- [x] Task scope is clear.
- [x] Source documents were reviewed.
- [x] Tech Spec coverage was verified as unavailable and documented.
- [x] Architecture decisions were checked.
- [x] ADR candidates were confirmed, rejected, required-before-implementation, or explicitly deferred by the user.
- [x] Pending decisions are resolved or explicitly deferred out of this task by the user.
- [x] User questions were answered.
- [x] Expected files or areas are identified.
- [x] Acceptance criteria are defined.
- [x] Test strategy is defined.
- [x] Risks and edge cases are documented.

## Notes for the Implementing Agent

- ADR-008 is the binding public REST contract source.
- This task defines API contracts and controller route surfaces; it does not implement real conversion, download, operation orchestration, cleanup, event emission, storage resolution, or frontend integration.
- Keep controllers thin: validate, map HTTP contract details, and delegate.
- Mock or test-scope the delegation port in contract tests.
- Do not expose `ResultFileMetadata.internalPath`, root-relative storage keys, absolute filesystem paths, or backend storage implementation details.
- Use `OperationType.CONVERSION`, `OperationType.URL_DOWNLOAD`, and existing operation status values in public operation responses.
