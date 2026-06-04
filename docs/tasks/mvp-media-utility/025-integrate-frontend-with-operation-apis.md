# Task: Integrate Frontend with Operation APIs

## Status

Status: Depends on Previous Task

Last updated: 2026-06-04

## Task ID

ID: MVP-MEDIA-025

Order: 025

Task file: `docs/tasks/mvp-media-utility/025-integrate-frontend-with-operation-apis.md`

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| ADR-002 | `docs/adrs/002-use-react-vite-typescript-served-by-spring-boot.md` | Decision, Consequences | Accepted | Confirms React/Vite frontend and REST frontend/backend boundary. |
| ADR-008 | `docs/adrs/008-define-public-rest-api-contract.md` | Decision, Consequences | Accepted | Confirms public operation API endpoints used by the frontend. |
| Task 014 execution | `docs/task-executions/mvp-media-utility/014-implement-conversion-operation-endpoint-execution.md` | Execution Summary | Confirmed by source document | Backend conversion endpoint exists. |
| Task 017 execution | `docs/task-executions/mvp-media-utility/017-implement-url-download-endpoint-execution.md` | Execution Summary | Confirmed by source document | Backend URL download endpoint exists. |
| Task 018 execution | `docs/task-executions/mvp-media-utility/018-implement-result-download-endpoint-execution.md` | Execution Summary | Confirmed by source document | Backend result download endpoint exists. |
| Blocked task plan | `docs/task-plans/mvp-media-utility/026-add-critical-e2e-flow-tests-plan.md` | Context Summary, Pending Decisions | Confirmed by user | Critical E2E tests are blocked until the UI integrates with backend operation APIs. |

## Context

The backend exposes the accepted operation API contract, but the current frontend flow does not submit MP4 conversion or URL download operations to the backend. It also does not render backend-driven processing, error, success, status, or public result download states. Critical Playwright E2E coverage must wait until the UI can exercise these backend flows through the application surface.

## Goal

Connect the React UI to the backend operation APIs for MP4 conversion and URL download flows, rendering backend-driven operation states and public result download links.

## Scope

- Submit MP4 conversion requests from the frontend to `POST /api/operations/conversions`.
- Submit URL download requests from the frontend to `POST /api/operations/downloads`.
- Read operation status from `GET /api/operations/{operationId}`.
- Expose the public result link from `GET /api/operations/{operationId}/result` when an operation succeeds.
- Render backend-driven states for pending, processing, success, and failure outcomes.
- Render backend validation or operation errors in the relevant UI flow.
- Preserve existing client-side validation where it still improves immediate feedback without replacing backend validation.
- Add or update frontend tests for API submission, status rendering, error rendering, and result link rendering using controlled mocks.

## Out of Scope

- Do not add Playwright E2E tests in this task.
- Do not change the public backend API contract accepted by ADR-008.
- Do not test real external media providers or actual YouTube endpoints.
- Do not introduce a new architecture decision record.

## Implementation Instructions

- Keep API paths aligned with ADR-008:
  - `POST /api/operations/conversions`
  - `POST /api/operations/downloads`
  - `GET /api/operations/{operationId}`
  - `GET /api/operations/{operationId}/result`
- Use the existing frontend structure unless the implementation reveals a local pattern that warrants small component or service extraction.
- Model operation status and result rendering from backend responses rather than hard-coded local success messages.
- Keep polling, refresh, or status lookup behavior bounded so the UI does not create runaway requests.
- Keep tests deterministic by mocking HTTP responses or using existing test doubles.

## Expected Files

| Path | Action | Source | Notes |
| --- | --- | --- | --- |
| `frontend/src/App.tsx` or frontend modules | Modify | Frontend API integration | Wire UI flows to backend operation APIs. |
| `frontend/src/App.test.tsx` or frontend tests | Modify | Frontend test coverage | Cover API submission, status, errors, and result link rendering. |
| Frontend API helper module, if needed | Create | Local implementation pattern | Only create if it keeps UI code clearer and testable. |

## Dependencies

| Dependency | Type | Status | Notes |
| --- | --- | --- | --- |
| MVP-MEDIA-014 | Previous task | Completed | Backend conversion endpoint exists. |
| MVP-MEDIA-017 | Previous task | Completed | Backend URL download endpoint exists. |
| MVP-MEDIA-018 | Previous task | Completed | Backend result download endpoint exists. |
| ADR-008 | Architecture decision | Accepted | Defines the public API contract this task must use. |

## Validation

- Run the existing frontend test command after implementation.
- Verify tests cover successful submission and backend-driven rendering for both operation types.
- Verify backend errors or validation failures surface in the UI.
- Verify no Playwright E2E test setup or specs are added by this task.

## Acceptance Criteria

- [ ] MP4 conversion UI submits to `POST /api/operations/conversions`.
- [ ] URL download UI submits to `POST /api/operations/downloads`.
- [ ] UI reads and renders operation status from `GET /api/operations/{operationId}`.
- [ ] UI exposes a public result download link using `GET /api/operations/{operationId}/result` after success.
- [ ] Backend-driven pending, processing, success, failure, and error states are visible to the user.
- [ ] Frontend tests cover the integrated API behavior with controlled mocks.
- [ ] No Playwright E2E tests are added in this task.

## Risks

- Polling or repeated status checks can become flaky or excessive if not bounded.
- Backend response shapes must be verified from existing API code before implementing frontend assumptions.
- UI tests should avoid relying on real media processing or network availability.

## Open Questions

None. This is a task sequencing and scope refinement, not a new architecture decision.

## Notes for the Implementing Agent

- Complete this task before planning or executing MVP-MEDIA-026.
- Treat ADR-008 as the binding API contract.
