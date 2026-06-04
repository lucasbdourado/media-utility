# Task Implementation Plan: Integrate Frontend with Operation APIs

## Status

Status: Ready for Implementation

Last updated: 2026-06-04

Plan file: `docs/task-plans/mvp-media-utility/025-integrate-frontend-with-operation-apis-plan.md`

Architecture decision notes file: Not generated

## Task Reference

Task ID: `MVP-MEDIA-025`

Task file: `docs/tasks/mvp-media-utility/025-integrate-frontend-with-operation-apis.md`

Task status: `Depends on Previous Task`

Task group or feature: `mvp-media-utility`

## Planning Mode Requirement

Plan mode verified: Yes

Notes:

- This plan was created in plan mode.
- Implementation must not start during `plan-task`.
- A future implementation request must use this saved plan as source context.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/tasks/mvp-media-utility/025-integrate-frontend-with-operation-apis.md` | Goal, Scope, Dependencies, Validation, Acceptance Criteria | Confirmed by source document | Defines frontend integration with operation APIs and excludes Playwright E2E tests. |
| Project discovery | `docs/context/project-discover.md` | Project Scenario | Confirmed by source document | Required Harness discovery document exists, but predates the current implementation codebase. |
| Project planning | `docs/planning/project-planning.md` | Web Experience, Media Conversion, Media Download, Result Delivery | Confirmed by source document | Confirms the single operation selector flow, URL download, MP4 conversion, immediate result download, and responsibility notice. |
| Tech Spec | `docs/specs/tech-spec.md` | Not available | Missing / documented limitation | File exists but is empty in this workspace. User confirmed the plan may proceed using ADRs, task artifacts, execution reports, and code evidence. |
| Technology definition | `docs/architecture/technology-definition.md` | Not available | Missing / documented limitation | File exists but is empty in this workspace. User confirmed this is not blocking for this task. |
| ADR-002 | `docs/adrs/002-use-react-vite-typescript-served-by-spring-boot.md` | Decision, Consequences | Accepted | Confirms React, Vite, TypeScript, and REST frontend/backend boundary. |
| ADR-003 | `docs/adrs/003-use-maven-npm-coordinated-asset-packaging.md` | Decision, Consequences | Accepted | Confirms npm scripts remain the frontend source of truth. |
| ADR-008 | `docs/adrs/008-define-public-rest-api-contract.md` | Decision, Consequences | Accepted | Defines operation creation, status, result download, public DTO, and public error shapes. |
| Backend API code | `src/main/java/com/lucasdourado/mediautility/api/OperationApiController.java`, `OperationService.java`, DTO records | Detected endpoint and DTO behavior | Detected in codebase | Confirms endpoint paths, request formats, status values, error responses, and result metadata. |
| Frontend code | `frontend/src/App.tsx`, `frontend/src/App.test.tsx`, `frontend/package.json`, `frontend/vite.config.ts` | Existing UI and test setup | Detected in codebase | Current UI validates locally and shows prepare messages, but does not submit to backend APIs or render backend-driven results. |
| Task 014 execution | `docs/task-executions/mvp-media-utility/014-implement-conversion-operation-endpoint-execution.md` | Execution Summary | Confirmed by source document | Backend conversion endpoint exists. |
| Task 017 execution | `docs/task-executions/mvp-media-utility/017-implement-url-download-endpoint-execution.md` | Execution Summary | Confirmed by source document | Backend URL download endpoint exists. |
| Task 018 execution | `docs/task-executions/mvp-media-utility/018-implement-result-download-endpoint-execution.md` | Execution Summary | Confirmed by source document | Backend result download endpoint exists. |
| Task 024 execution | `docs/task-executions/mvp-media-utility/024-add-frontend-test-coverage-execution.md` | Execution Summary, Scope | Confirmed by source document | Confirms current frontend tests cover local UI behavior and do not include API integration. |
| User decision | Current `plan-task` session | Status refresh behavior | Confirmed by user | Use bounded automatic polling after operation creation until terminal status or max attempts. |
| User decision | Current `plan-task` session | Result download behavior | Confirmed by user | Render an anchor/link using the backend-provided result download URL; do not fetch blobs in JavaScript. |
| User decision | Current `plan-task` session | Empty spec handling | Confirmed by user | Proceed with accepted ADRs and code evidence despite empty Tech Spec and technology-definition files. |

## Context Summary

The backend operation APIs are implemented and aligned with ADR-008, but the React UI still stops at local preparation states. This task connects the existing single-page operation selector UI to backend operation creation, status, error, and result metadata so the UI can exercise both MP4 conversion and URL download flows. MVP-MEDIA-026 remains dependent on this task because Playwright E2E critical flows require an integrated UI.

## Task Goal

Connect the React frontend to the accepted backend operation APIs for MP4 conversion and URL download, render backend-driven lifecycle states, display backend errors, and expose public result download links for completed operations.

## Confirmed Scope

- Submit MP4 conversion requests to `POST /api/operations/conversions` as `multipart/form-data` with field name `file`.
- Submit URL download requests to `POST /api/operations/downloads` as JSON `{ "url": "https://..." }`.
- Read operation status from the response `links.status` URL or `/api/operations/{operationId}`.
- Use bounded automatic polling after successful creation until `COMPLETED`, `FAILED`, or the max attempt count is reached.
- Render backend-driven `PENDING`, `PROCESSING`, `COMPLETED`, and `FAILED` states for both operation types.
- Render public error messages from backend error responses and failed operation status responses.
- Render a normal anchor/link to `result.downloadUrl` when a completed status response includes result metadata.
- Preserve useful client-side validation for immediate feedback, while still submitting valid inputs to backend validation.
- Add or update deterministic frontend tests with mocked `fetch`.

## Out of Scope

- Do not add Playwright E2E setup or specs.
- Do not change the accepted public backend API contract from ADR-008.
- Do not test real external media providers or actual YouTube endpoints.
- Do not introduce a new ADR.
- Do not implement JavaScript blob downloads for result files.
- Do not add backend feature work unless implementation discovers a frontend-blocking contract bug; in that case stop and report the mismatch rather than changing the public contract inside this task.

## Requirements Covered

| Requirement | Source | How This Task Covers It | Status |
| --- | --- | --- | --- |
| Single operation selector flow | Project planning, existing frontend | Keeps the existing selector and wires each selected form to its backend operation. | Confirmed |
| MP4-to-MP3 conversion operation | Task file, ADR-008 | Uploads selected MP4 to `/api/operations/conversions`. | Confirmed |
| Public URL download operation | Task file, ADR-008 | Submits URL JSON to `/api/operations/downloads`. | Confirmed |
| Immediate result download | Project planning, ADR-008 | Displays `result.downloadUrl` as a user-clickable download link after completion. | Confirmed |
| Backend validation/error visibility | Task file, ADR-008, API code | Parses public error responses and failed operation errors for display. | Confirmed |
| Frontend API behavior tests | Task file, task 024 execution | Extends current Vitest/RTL tests with mocked HTTP scenarios. | Confirmed |

## Technical Specification Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Not available | Missing | None directly from Tech Spec content. | `docs/specs/tech-spec.md` exists but is empty. |
| Accepted ADR-008 REST contract | Full via ADR substitute | Endpoint paths, request formats, status/result/error DTOs. | User confirmed proceeding with ADRs and code evidence. |
| Existing frontend flow evidence | Full via codebase | Current selector/form behavior to preserve while integrating APIs. | Source is code, not Tech Spec. |

Coverage assessment:

- Justifying Tech Spec section: unavailable because the Tech Spec file is empty.
- Tech Spec sections implemented by this task: none directly.
- Gaps between task and Tech Spec: Tech Spec traceability is missing in the workspace.
- Dependencies not specified by the Tech Spec: status refresh behavior and result download behavior were confirmed by user in this planning session.
- Source limitation handling: user confirmed this task can be planned as ready using accepted ADRs, task files, execution reports, and code evidence.

## Architecture and ADR Considerations

| Decision or ADR | Source | Impact on This Task | Status |
| --- | --- | --- | --- |
| React/Vite/TypeScript served by Spring Boot | ADR-002 | Implement in the existing React frontend and keep REST as the frontend/backend boundary. | Accepted |
| Maven/npm coordinated asset packaging | ADR-003 | Keep npm frontend scripts as the validation source; do not alter packaging unless required by existing build behavior. | Accepted |
| Public REST API contract | ADR-008 | Use the accepted endpoints, DTO shapes, HTTP error model, and direct result endpoint. | Accepted |
| Safe public result metadata | ADR-008, ADR-005, ADR-006 | UI must use public `downloadUrl` only and must not expect or expose internal paths/storage keys. | Accepted |
| Bounded polling | User decision | Implement automatic status refresh with a fixed interval and max attempts to avoid runaway requests. | Confirmed by user |
| Anchor result download | User decision | Render a normal link to backend `result.downloadUrl`; no blob-fetch client download implementation. | Confirmed by user |

ADR candidates or architecture decisions needed:

- None. The task uses accepted ADRs and user-confirmed task-level behavior decisions.
- No architecture decision notes file will be generated.

Architecture decision notes:

- Saved separately: No
- Path: Not generated
- Notes file status: Not applicable

## Confirmed Decisions

- Use the existing `frontend/` React/Vite/TypeScript application.
- Keep the single operation selector UI; integrate API behavior into the selected operation forms.
- Create a small frontend API helper/types module if it keeps `App.tsx` testable and avoids embedding wire parsing in component event handlers.
- Use `window.fetch`/`fetch` with deterministic mocks in tests; do not add a client HTTP library.
- Use `FormData` with field `file` for conversion creation.
- Use JSON request body `{ url }` and `Content-Type: application/json` for URL download creation.
- Use bounded automatic polling after a successful creation response.
- Stop polling once status is `COMPLETED` or `FAILED`, or after the fixed max attempt count.
- If polling exhausts before terminal status, show a nonterminal message that the operation is still processing and avoid further automatic requests.
- For public error responses, display field detail messages when present, otherwise display the top-level message.
- For failed operation status responses, display `error.message` when present.
- For completed operations, render an anchor to `result.downloadUrl` and display available public metadata such as file name and size.
- Keep existing client-side URL and MP4 validation as immediate feedback, but backend validation remains authoritative.
- Do not add Playwright E2E tests in this task.

## Pending Decisions

None. All task-relevant decisions have been answered or explicitly deferred out of scope by the user.

## Questions for the User

None. All task-relevant questions have been answered.

## Proposed Implementation Approach

1. Add typed frontend API helpers for the operation contract, error parsing, creation requests, status requests, and public result metadata.
2. Replace the current local "prepare" submit behavior with async API submission for both operation forms.
3. Track per-flow operation state in React: idle/input error, submitting, polling/processing, completed, failed, and request error.
4. Implement bounded polling with cleanup on operation switch, new submission, component unmount, terminal status, or max attempts.
5. Render backend-driven status, error, and result surfaces in the existing UI layout.
6. Extend frontend tests to mock `fetch`, fake timers where needed, and assert request payloads, status transitions, error messages, and result link rendering.

## Files and Areas Expected to Change

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| `frontend/src/App.tsx` | Modify | Task file, current code | Wire forms to API helper and render backend-driven operation states. |
| `frontend/src/App.test.tsx` | Modify | Task file, current tests | Add mocked API behavior tests; update obsolete "prepare" assertions. |
| `frontend/src/operationApi.ts` or equivalent | Create | Confirmed implementation approach | Define API types, request helpers, status fetch, and error parsing. |
| `frontend/src/App.css` | Modify if needed | Existing UI | Adjust state/result/error presentation only as needed. |
| `frontend/package.json` | Inspect / likely preserve | ADR-003 | No new dependency expected. |
| Backend API files | Inspect only | ADR-008, code evidence | Do not change unless a blocking mismatch is discovered and reported. |

## Step-by-Step Implementation Plan

1. Inspect the current frontend component and tests before editing to account for any user changes.
2. Create a frontend operation API module with TypeScript types matching ADR-008 and current backend DTOs: operation response, result metadata, error response, operation type, and operation status.
3. Implement `createConversion(file)`, `createDownload(url)`, `getOperation(statusUrlOrId)`, and public error parsing helpers using native `fetch`.
4. Update conversion submit handling to validate selected MP4 locally, send `FormData` with field `file`, store the returned operation, and start bounded polling.
5. Update URL submit handling to validate the HTTP/HTTPS URL locally, send JSON `{ url }`, store the returned operation, and start bounded polling.
6. Implement polling in React using a fixed interval and max attempts; clear timers on terminal status, operation switch, resubmission, and unmount.
7. Render operation status from backend responses, including submitting/pending/processing labels, completed result metadata, failed operation errors, request errors, and the max-attempts still-processing state.
8. Render the result download as an anchor whose `href` is `operation.result.downloadUrl`; use the backend file name when present and keep it accessible.
9. Update tests to mock successful conversion and download flows, polling transitions, backend validation errors, failed operation status, result link rendering, and preservation of local validation.
10. Remove or update tests that assert the old "ready for later submission" copy so tests describe integrated behavior.
11. Run frontend validation commands from `frontend/`: `npm run test`, `npm run typecheck`, and `npm run build`.
12. Review the diff to confirm no Playwright setup, backend contract changes, package dependency additions, or unrelated source changes were introduced.

## Validation Strategy

- Use Vitest and React Testing Library with controlled `fetch` mocks for all frontend API behavior.
- Use fake timers or controlled async utilities for bounded polling tests.
- Verify request payloads and headers for both operation creation endpoints.
- Verify UI behavior for `PENDING`, `PROCESSING`, `COMPLETED`, and `FAILED` status responses.
- Verify public backend error parsing for validation/detail errors and generic request failures.
- Verify completed operations render a normal link to the backend result endpoint.
- Run `npm run test`, `npm run typecheck`, and `npm run build` from `frontend/`.
- Do not run or add Playwright E2E tests in this task.

## Tests to Add or Update

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| Conversion creation request | Frontend integration/unit | Assert MP4 submit calls `POST /api/operations/conversions` with `FormData` field `file`. | Mock `fetch`. |
| URL download creation request | Frontend integration/unit | Assert valid URL submit calls `POST /api/operations/downloads` with JSON `{ url }`. | Mock `fetch`. |
| Status polling transitions | Frontend integration/unit | Assert accepted operation progresses through backend statuses and stops at terminal state. | Use bounded fake timers or controlled promises. |
| Completed result rendering | Frontend integration/unit | Assert completed response renders file metadata and anchor to `result.downloadUrl`. | Anchor href should be backend-provided. |
| Backend validation/error rendering | Frontend integration/unit | Assert public error response details/messages surface in the relevant form. | Cover field details and top-level message fallback. |
| Failed operation rendering | Frontend integration/unit | Assert `FAILED` status with `error.message` is visible and polling stops. | Cover both operation types if practical. |
| Local validation preservation | Frontend integration/unit | Assert empty/invalid local inputs still prevent API calls. | Update existing tests. |
| Max polling attempts | Frontend integration/unit | Assert polling stops after max attempts and shows still-processing guidance. | Prevent runaway request regression. |

## Acceptance Criteria

- [ ] MP4 conversion UI submits to `POST /api/operations/conversions` with multipart field `file`.
- [ ] URL download UI submits to `POST /api/operations/downloads` with JSON body `{ url }`.
- [ ] UI reads and renders operation status from `GET /api/operations/{operationId}` or the response `links.status`.
- [ ] UI uses bounded automatic polling and does not create runaway requests.
- [ ] UI exposes a public result download link using backend `result.downloadUrl` after success.
- [ ] Backend-driven `PENDING`, `PROCESSING`, `COMPLETED`, and `FAILED` states are visible to the user.
- [ ] Backend validation, request errors, and failed operation errors surface in the relevant UI flow.
- [ ] Existing useful client-side validation remains in place and prevents unnecessary API calls.
- [ ] Frontend tests cover integrated API behavior with controlled mocks.
- [ ] `npm run test`, `npm run typecheck`, and `npm run build` pass from `frontend/`.
- [ ] No Playwright E2E tests or setup are added in this task.
- [ ] No accepted backend public API contract changes are introduced.

## Risks and Edge Cases

- Polling can become flaky in tests or excessive in runtime if timers are not bounded and cleaned up.
- Fetch mocks must be reset between tests to avoid cross-test contamination.
- Backend error responses may contain field `details`, top-level `message`, or network-level failures; rendering should handle each gracefully.
- Switching operations or resubmitting while polling must not let stale responses overwrite the current UI state.
- Completed status may include result metadata; non-completed statuses should not assume `result` is present.
- The backend result endpoint returns a file directly, so the frontend should use a link rather than expecting JSON from `GET /result`.
- The empty Tech Spec and technology-definition files reduce traceability; accepted ADRs and code evidence are the binding sources for this task.

## Rollback or Recovery Notes

Frontend rollback should be limited to reverting changes in the React component, API helper module, CSS adjustments, and frontend tests. Because this task should not change backend contracts or persistence, recovery should not require database or backend migration rollback.

## Documentation Updates

- No PRD, Tech Spec, technology-definition, final ADR, or task file updates are expected.
- A later `execute-task` run should create the task execution report after implementation and validation.

## Implementation Readiness Checklist

- [x] Task scope is clear.
- [x] Source documents were reviewed.
- [x] Tech Spec coverage was verified and the empty-file limitation was explicitly accepted by the user.
- [x] Architecture decisions were checked.
- [x] ADR candidates were confirmed, rejected, required-before-implementation, or explicitly deferred by the user.
- [x] Pending decisions are resolved or explicitly deferred out of this task by the user.
- [x] User questions were answered.
- [x] Expected files or areas are identified.
- [x] Acceptance criteria are defined.
- [x] Test strategy is defined.
- [x] Risks and edge cases are documented.

## Notes for the Implementing Agent

- Treat ADR-008 and current backend DTOs as binding for endpoint paths, request formats, status values, result metadata, and public errors.
- Use native `fetch`; do not add a frontend HTTP client dependency unless a new approved plan says otherwise.
- Keep polling bounded and clean up timers on unmount, operation switch, terminal status, and resubmission.
- Do not call `GET /api/operations/{operationId}/result` expecting JSON; it is a direct file response. Link to `result.downloadUrl` instead.
- Do not add Playwright in this task. MVP-MEDIA-026 handles critical E2E tests after this integration exists.
