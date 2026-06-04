# Task Implementation Plan: Add Critical E2E Flow Tests

## Status

Status: Blocked

Last updated: 2026-06-04

Plan file: `docs/task-plans/mvp-media-utility/026-add-critical-e2e-flow-tests-plan.md`

Architecture decision notes file: Not generated

## Task Reference

Task ID: `MVP-MEDIA-026`

Task file: `docs/tasks/mvp-media-utility/026-add-critical-e2e-flow-tests.md`

Task status: `Depends on Previous Task`

Task group or feature: `mvp-media-utility`

## Planning Mode Requirement

Plan mode verified: Yes

Notes:

- This plan was created in plan mode.
- Implementation must not start during `plan-task`.
- This is a non-executable blocked planning snapshot.
- A future implementation request must not execute MVP-MEDIA-026 from this plan until MVP-MEDIA-025 is completed and this plan is updated to `Ready for Implementation`.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/tasks/mvp-media-utility/026-add-critical-e2e-flow-tests.md` | Goal, Scope, Dependencies, Validation, Acceptance Criteria, Open Questions | Confirmed by source document | Defines Playwright E2E tests for MP4 conversion, URL download, validation failures, and local app execution. |
| Prerequisite task file | `docs/tasks/mvp-media-utility/025-integrate-frontend-with-operation-apis.md` | Goal, Scope, Dependencies, Acceptance Criteria | Confirmed by user | Defines the frontend/backend operation API integration that must be completed before E2E tests are ready. |
| Project discovery | `docs/context/project-discover.md` | Project Scenario | Confirmed by source document | Required Harness discovery document exists, but it predates the current implementation codebase. |
| Project planning | `docs/planning/project-planning.md` | Milestone 5, End-to-End Validation, Suggested Task Order | Confirmed by source document | Confirms E2E validation after conversion, download, result delivery, cleanup, metrics, and frontend test coverage tasks. |
| ADR-001 | `docs/adrs/001-use-java-and-spring-boot-modular-monolith.md` | Decision, Consequences | Accepted | Confirms one Java 21 Spring Boot modular monolith. |
| ADR-002 | `docs/adrs/002-use-react-vite-typescript-served-by-spring-boot.md` | Decision, Consequences | Accepted | Confirms React, Vite, TypeScript frontend served by Spring Boot and REST frontend/backend boundary. |
| ADR-003 | `docs/adrs/003-use-maven-npm-coordinated-asset-packaging.md` | Decision, Consequences | Accepted | Confirms npm scripts remain frontend source of truth and Maven packages frontend assets into Spring Boot. |
| ADR-008 | `docs/adrs/008-define-public-rest-api-contract.md` | Decision, Consequences | Accepted | Confirms REST endpoints for conversions, URL downloads, operation status, and result download. |
| Task 014 execution | `docs/task-executions/mvp-media-utility/014-implement-conversion-operation-endpoint-execution.md` | Execution Summary, Acceptance Criteria | Confirmed by source document | Confirms backend conversion endpoint/orchestration exists. |
| Task 017 execution | `docs/task-executions/mvp-media-utility/017-implement-url-download-endpoint-execution.md` | Execution Summary, Acceptance Criteria | Confirmed by source document | Confirms backend URL download endpoint/orchestration exists. |
| Task 018 execution | `docs/task-executions/mvp-media-utility/018-implement-result-download-endpoint-execution.md` | Execution Summary, Acceptance Criteria | Confirmed by source document | Confirms backend result download endpoint exists. |
| Task 022 execution | `docs/task-executions/mvp-media-utility/022-add-local-development-compose-setup-execution.md` | Execution Summary, Risks and Follow-ups | Confirmed by source document | Confirms Docker Compose structure exists, but full runtime validation was not executed in that task. |
| Task 024 execution | `docs/task-executions/mvp-media-utility/024-add-frontend-test-coverage-execution.md` | Execution Summary, Scope | Confirmed by source document | Confirms current frontend tests cover local UI behavior and did not add API integration or E2E setup. |
| Current codebase | `frontend/package.json`, `frontend/vite.config.ts`, `frontend/src/App.tsx`, `frontend/src/App.test.tsx` | Frontend scripts, config, UI behavior, tests | Detected in codebase | Confirms frontend currently validates inputs locally and shows ready messages, but does not submit to backend APIs, poll status, or render result download links. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/api/OperationApiController.java`, `OperationService.java` | REST endpoints and service orchestration | Detected in codebase | Confirms backend endpoint surfaces exist for the flows E2E tests should eventually exercise. |
| PRD | `docs/product/prd.md` | Not available | Missing / documented limitation | File exists but is empty in the current workspace. |
| Tech Spec | `docs/specs/tech-spec.md` | Not available | Missing / documented limitation | File exists but is empty in the current workspace despite being referenced by the task. |
| Technology definition | `docs/architecture/technology-definition.md` | Not available | Missing / documented limitation | File exists but is empty in the current workspace. |
| User decision | Current unblock session | Frontend integration blocker handling | Confirmed by user | User chose to split the work into sequential tasks: MVP-MEDIA-025 for frontend/backend integration, then MVP-MEDIA-026 for Playwright E2E tests. |

## Context Summary

MVP-MEDIA-026 is intended to add Playwright tests for critical end-to-end media flows against a running application. The backend already exposes conversion, URL download, status, and result endpoints, and local Docker Compose exists. However, the current React UI only validates local form inputs and displays preparation messages. It does not submit conversion or URL download requests, poll operation status, show processing/result states from the backend, or expose a download link returned by the API.

Because the task's acceptance criteria require full UI-to-backend E2E flows, the selected task is not ready for implementation as a Playwright-only task. MVP-MEDIA-025 now exists as the required prerequisite integration task.

## Task Goal

After MVP-MEDIA-025 is completed, configure Playwright and add E2E tests that verify the critical MP4-to-MP3 conversion flow, URL download flow, validation failures, and result download behavior against a running local application.

## Confirmed Scope

For the blocked snapshot only:

- Record that MVP-MEDIA-026 is not ready for implementation.
- Preserve the intended E2E scope from the task file.
- Identify the missing prerequisite: frontend integration with backend operation APIs.
- Require a preceding refined task or task split before Playwright E2E implementation.
- Do not provide executable Playwright implementation steps until the prerequisite is complete.

Intended scope after unblock:

- Configure Playwright in the frontend workspace unless a later ready plan confirms otherwise.
- Add E2E tests for MP4 conversion happy path, URL download happy path, and validation failure flows.
- Run tests against a live local app instance.
- Avoid external provider dependencies by using controlled local fixtures, mocks, or test-mode adapters.

## Out of Scope

- Do not implement Playwright setup from this blocked plan.
- Do not implement frontend API integration from this task plan; that work belongs to MVP-MEDIA-025.
- Do not add source-code changes, application behavior, package dependencies, or tests during `plan-task`.
- Do not test real external media providers or actual YouTube endpoints.
- Do not create or modify PRDs, Tech Specs, final ADRs, technology-definition documents, task files, or application source code during planning.
- Do not mark this task `Ready for Implementation` until MVP-MEDIA-025 is completed.

## Requirements Covered

| Requirement | Source | How This Plan Covers It | Status |
| --- | --- | --- | --- |
| Critical E2E MP4 conversion flow | Task file | Preserved as intended future scope after unblock. | Blocked |
| Critical E2E URL download flow | Task file | Preserved as intended future scope after unblock. | Blocked |
| Validation failure flows | Task file | Preserved as intended future scope after unblock. | Blocked |
| Run Playwright against local app | Task file, task 022 execution | Requires a complete UI flow and runtime validation before executable E2E steps are safe. | Blocked |
| Avoid external media providers | Task file | Future ready plan must use controlled fixtures, mocks, or test-mode adapters. | Confirmed |
| Single frontend/backend REST boundary | ADR-002, ADR-008 | Future E2E tests should exercise the React UI through backend REST APIs. | Confirmed |
| Frontend API integration prerequisite | Current codebase, user decision | Identified as missing and blocking for E2E critical flow tests. | Confirmed by user |

## Technical Specification Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Not available | Missing | None from Tech Spec content. | `docs/specs/tech-spec.md` exists but is empty. |
| Task file reference to Verification Plan / Manual Verification | Partial via task reference only | Intended E2E validation is described by the task file, not by available Tech Spec content. | Cannot verify against Tech Spec content. |

Coverage assessment:

- Justifying Tech Spec section: unavailable because the Tech Spec file is empty.
- Tech Spec sections implemented by this task: none in this blocked plan.
- Gaps between task and Tech Spec: the task references E2E verification, but the referenced Tech Spec content is unavailable in the workspace.
- Dependencies not specified by the Tech Spec: frontend API integration is missing in the current codebase and must be handled before this E2E task.
- Source limitation handling: empty PRD, Tech Spec, and technology-definition files are documented limitations. The immediate blocker is the current frontend/backend integration gap, confirmed by user decision.

## Architecture and ADR Considerations

| Decision or ADR | Source | Impact on This Task | Status |
| --- | --- | --- | --- |
| Spring Boot modular monolith | ADR-001 | E2E should target one local deployed app when unblocked. | Accepted |
| React/Vite/TypeScript served by Spring Boot | ADR-002 | Playwright configuration should live with the frontend unless a later ready plan confirms another layout. | Accepted |
| Maven/npm coordinated asset packaging | ADR-003 | Future implementation must preserve npm frontend scripts and packaged app behavior. | Accepted |
| Public REST API contract | ADR-008 | UI integration and E2E tests must use `/api/operations/conversions`, `/api/operations/downloads`, `/api/operations/{operationId}`, and `/api/operations/{operationId}/result`. | Accepted |
| No public exposure of internal storage paths | ADR-005, ADR-006, ADR-008 | E2E assertions must verify public result/download behavior without expecting internal filesystem paths or storage keys. | Accepted |
| Frontend integration blocker | Current codebase, user decision | Implementation of Playwright critical flows is blocked until MVP-MEDIA-025 integrates the UI with backend APIs and renders backend-driven progress/result states. | Confirmed by user |

ADR candidates or architecture decisions needed:

- None for this blocked snapshot.
- The missing prerequisite is a task sequencing/scope problem, not a required formal ADR.

Architecture decision notes:

- Saved separately: No
- Path: Not generated
- Notes file status: Not applicable

## Confirmed Decisions

- The selected task is `MVP-MEDIA-026`.
- The plan path is `docs/task-plans/mvp-media-utility/026-add-critical-e2e-flow-tests-plan.md`.
- The accepted unblock/refinement decision is to split frontend/backend integration and E2E tests into sequential tasks.
- The selected alternative is: MVP-MEDIA-025 integrates the frontend with operation APIs, then MVP-MEDIA-026 adds critical Playwright E2E flow tests.
- The rejected alternative is: execute E2E tests before the UI is integrated with backend operation APIs.
- The effect is: MVP-MEDIA-026 remains blocked/dependent until MVP-MEDIA-025 is implemented.
- No architecture decision notes file is generated for this task.
- Empty PRD, Tech Spec, and technology-definition files are documented limitations.
- Backend conversion, URL download, status, and result endpoints exist.
- Docker Compose configuration exists, but earlier full runtime validation was documented as a follow-up.
- The current frontend does not integrate with backend operation APIs.
- User chose to block/refine the E2E task instead of expanding it to implement frontend integration or testing only current local UI behavior.
- MVP-MEDIA-026 must not be executed until MVP-MEDIA-025 is completed and this plan is updated.

## Pending Decisions

| Decision Needed | Why It Matters | Blocking? | Owner or Next Step |
| --- | --- | --- | --- |
| Complete MVP-MEDIA-025 frontend API integration | Critical E2E tests require a UI that submits operations, observes backend status, and exposes result downloads. | Yes | Execute MVP-MEDIA-025 before replanning MVP-MEDIA-026. |
| Decide the E2E controlled media strategy after integration exists | Future Playwright tests need stable fixtures or test-mode behavior to avoid real external providers and slow media processing. | Yes for ready E2E planning | Resolve during the future ready planning pass after the UI integration task exists. |
| Confirm local runtime startup strategy for E2E | Future Playwright config must know whether tests start the app themselves, depend on `docker compose up`, or use another local command. | Yes for ready E2E planning | Resolve during the future ready planning pass after task sequencing is fixed. |

## Questions for the User

None for this blocked snapshot. The user confirmed that the E2E task should remain blocked/dependent because the current frontend lacks backend integration required for critical E2E flows.

## Proposed Implementation Approach

This blocked plan must not be used for implementation.

Prerequisite approach before MVP-MEDIA-026 can be replanned:

1. Execute MVP-MEDIA-025, which connects the React UI to the accepted REST API contract.
2. MVP-MEDIA-025 should implement conversion submission, URL download submission, backend validation/error rendering, operation status polling or equivalent status refresh behavior, processing/success/error states, and public result download link rendering.
3. Complete and verify MVP-MEDIA-025 with frontend tests and relevant backend/API integration expectations.
4. Re-run `plan-task` for MVP-MEDIA-026 after the integrated UI exists.
5. Only then produce a `Ready for Implementation` Playwright plan with executable steps, fixture strategy, runtime startup strategy, and acceptance criteria.

## Files and Areas Expected to Change

For this blocked planning snapshot:

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| `docs/task-plans/mvp-media-utility/026-add-critical-e2e-flow-tests-plan.md` | Update | unblock workflow | Preserve this blocked snapshot with the refined task sequencing decision. |

Expected areas for the prerequisite task, not for MVP-MEDIA-026 from this plan:

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| `frontend/src/App.tsx` or future frontend modules | Modify | MVP-MEDIA-025 | Add backend API submission/status/result behavior in the prerequisite task. |
| `frontend/src/App.test.tsx` or frontend tests | Modify | MVP-MEDIA-025 | Test integrated frontend behavior in the prerequisite task. |
| Backend/API code | Inspect / possibly no change | ADR-008, current endpoints | Existing endpoints appear present; prerequisite task should inspect whether frontend integration needs contract adjustments before changing backend. |

Future MVP-MEDIA-026 areas after unblock:

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| `frontend/playwright.config.ts` | Create | Task file | Exact config deferred until unblock. |
| `frontend/e2e/` | Create | Task file | E2E specs and fixtures deferred until unblock. |
| `frontend/package.json` and `frontend/package-lock.json` | Modify | Task file | Add Playwright dependency/scripts after unblock. |

## Step-by-Step Implementation Plan

This task is blocked. Do not execute Playwright implementation steps from this plan.

Unblock sequence:

1. Treat MVP-MEDIA-026 as not ready for implementation.
2. Treat MVP-MEDIA-025 as the preceding task for frontend/backend API integration.
3. Plan MVP-MEDIA-025 with `plan-task`.
4. Execute MVP-MEDIA-025 only through the normal task planning and execution workflow.
5. Verify the UI can submit MP4 conversion and URL download operations to the backend, display backend-driven processing/error/success states, and expose a result download link.
6. Re-run `plan-task docs/tasks/mvp-media-utility/026-add-critical-e2e-flow-tests.md`.
7. During the future ready planning pass, decide the Playwright installation location, browser matrix, controlled media fixture/test-mode strategy, local runtime startup strategy, timeout policy, and download assertion behavior.
8. Save a replacement `Ready for Implementation` plan only when no task-relevant decisions remain pending.

## Validation Strategy

For this blocked snapshot:

- Validate by source review only: the plan records the discovered blocker and user decision.
- Do not run tests as part of `plan-task`.
- Do not run Playwright, build, package installation, Docker Compose startup, or application source changes from this plan.

Required validation after the prerequisite integration task:

- Frontend tests should verify API submission behavior, validation/error rendering, status/result rendering, and link display using controlled mocks or test doubles.
- Backend tests should already cover the endpoint behavior; inspect whether additional integration coverage is needed.
- A future MVP-MEDIA-026 ready plan should define Playwright validation against a running local app and controlled media fixtures.
- Future E2E validation should verify download responses without depending on external media provider availability.

## Tests to Add or Update

For this blocked snapshot:

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| None | Not applicable | No implementation is allowed from this blocked plan. | This plan only records the blocker. |

For the prerequisite frontend integration task:

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| Conversion API submission UI test | Frontend component/integration | Verify selected MP4 submits to `/api/operations/conversions` and renders backend-driven states. | Separate prerequisite task. |
| URL download API submission UI test | Frontend component/integration | Verify valid URL submits to `/api/operations/downloads` and renders notice, status, and result behavior. | Separate prerequisite task. |
| API validation/error rendering tests | Frontend component/integration | Verify backend validation and conflict errors surface correctly. | Separate prerequisite task. |

For future MVP-MEDIA-026 after unblock:

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| MP4 conversion critical flow | E2E | Validate upload, processing/status, completion, and result download through the UI. | Deferred until ready plan. |
| URL download critical flow | E2E | Validate URL submission, responsibility notice, processing/status, completion, and result download through the UI. | Deferred until ready plan. |
| Validation failure flows | E2E | Validate empty fields, invalid file, and invalid URL behavior through the UI. | Deferred until ready plan. |

## Acceptance Criteria

For this blocked planning artifact:

- [x] The selected task and target plan path are identified.
- [x] Source documents and codebase evidence were reviewed.
- [x] The frontend/backend integration gap is documented.
- [x] The user confirmed that the task should be blocked/refined.
- [x] No executable Playwright implementation steps are provided.
- [x] The required unblock path is documented.

Original MVP-MEDIA-026 acceptance criteria remain blocked:

- [ ] Playwright E2E testing framework is configured.
- [ ] Tests verify the MP4-to-MP3 conversion happy path, including file download check.
- [ ] Tests verify the URL download happy path, including file download check.
- [ ] Tests verify validation message triggers on incorrect inputs.
- [ ] E2E tests run successfully against a running local app instance.

## Risks and Edge Cases

- Implementing Playwright now would either test only local UI preparation behavior or require broad frontend integration work inside an E2E task, both of which would make acceptance misleading.
- Current backend flows run asynchronously and rely on external tools; future E2E tests need controlled fixtures or test-mode behavior to avoid slow or flaky tests.
- URL download E2E tests must not depend on real YouTube/public provider behavior.
- Docker Compose was structurally validated in task 022, but full runtime startup was previously left as a follow-up; future E2E planning must account for runtime availability.
- The empty Tech Spec, PRD, and technology-definition files reduce traceability; accepted ADRs, task artifacts, execution reports, and codebase evidence currently provide the usable binding context.

## Rollback or Recovery Notes

This blocked plan records the approved task sequencing refinement. To recover from an incorrect blocked snapshot, replace this plan with a future `Ready for Implementation` plan after MVP-MEDIA-025 is completed and MVP-MEDIA-026 is replanned.

## Documentation Updates

- Do not update PRD, project planning, technology definition, Tech Spec, final ADRs, task files, or unrelated documents from this plan.
- Recommended next workflow: plan and execute MVP-MEDIA-025 before returning to MVP-MEDIA-026.

## Implementation Readiness Checklist

- [x] Task scope is clear.
- [x] Source documents were reviewed.
- [x] Tech Spec coverage was verified as unavailable and documented.
- [x] Architecture decisions were checked.
- [x] ADR candidates were confirmed, rejected, required-before-implementation, or explicitly deferred by the user.
- [ ] Pending decisions are resolved or explicitly deferred out of this task by the user.
- [x] User questions were answered for the blocked/refine decision.
- [x] Expected files or areas are identified.
- [x] Acceptance criteria are defined.
- [x] Test strategy is defined for the blocked state and deferred future state.
- [x] Risks and edge cases are documented.

## Notes for the Implementing Agent

- Do not execute MVP-MEDIA-026 from this plan.
- This plan is intentionally blocked because the current frontend does not submit operations to backend APIs, poll operation status, or render backend result download links.
- The next required workflow is to plan and execute MVP-MEDIA-025 before returning to MVP-MEDIA-026.
- After MVP-MEDIA-025 is complete, re-run `plan-task` for MVP-MEDIA-026 and replace this blocked snapshot with a ready implementation plan.
- Do not interpret this blocked plan as permission to install Playwright, edit frontend source, run Docker Compose, or implement E2E tests.
