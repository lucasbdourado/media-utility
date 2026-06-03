# Task Implementation Plan: Build React Operation Selector Flow

## Status

Status: Ready for Implementation

Last updated: 2026-06-03

Plan file: `docs/task-plans/mvp-media-utility/009-build-react-operation-selector-flow-plan.md`

Architecture decision notes file: `docs/architecture/task-decisions/mvp-media-utility/009-build-react-operation-selector-flow-architecture-decisions.md`

## Task Reference

Task ID: `MVP-MEDIA-009`

Task file: `docs/tasks/mvp-media-utility/009-build-react-operation-selector-flow.md`

Task status: `Depends on Previous Task`

Task group or feature: `mvp-media-utility`

## Planning Mode Requirement

Plan mode verified: Yes

Notes:

- This plan was created in plan mode.
- Implementation must not start during `plan-task`.
- A future implementation request must use this saved plan and the saved architecture decision notes as source context.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/tasks/mvp-media-utility/009-build-react-operation-selector-flow.md` | Goal, Scope, Out of Scope, Dependencies, Validation, Acceptance Criteria | Confirmed by source document | Defines the frontend selector scope and prohibits backend/API integration in this task. |
| Project discovery | `docs/context/project-discover.md` | Project Scenario | Confirmed by source document | Required Harness discovery document exists, but it predates the current implementation codebase. |
| Project planning | `docs/planning/project-planning.md` | Goals, Functional Scope, Web Experience, Task Breakdown, Suggested Task Order | Confirmed by source document | Confirms one anonymous MVP web flow, operation selector, no accounts, no separate service pages, and conversion before URL download validation preference. |
| ADR-002 | `docs/adrs/002-use-react-vite-typescript-served-by-spring-boot.md` | Decision, Consequences | Accepted | Confirms React, Vite, TypeScript, npm, and REST frontend/backend boundary. |
| ADR-008 | `docs/adrs/008-define-public-rest-api-contract.md` | Decision, Consequences | Accepted | Confirms operation-centered API routes and operation distinctions for conversion and URL download. |
| Task 001 execution report | `docs/task-executions/mvp-media-utility/001-scaffold-spring-boot-react-monolith-execution.md` | Implemented Changes, Tests Executed | Confirmed by source document | Confirms the current React scaffold under `frontend/`, Vite, Vitest, Testing Library, and frontend scripts. |
| Task 008 execution report | `docs/task-executions/mvp-media-utility/008-define-rest-api-contracts-execution.md` | Execution Summary, Implemented Changes, Decisions Used | Confirmed by source document | Confirms the public REST contract has been implemented as contract surface, but task 009 must not call it. |
| Current codebase | `frontend/src/App.tsx` | React app shell | Detected in codebase | Current UI is scaffold-only and must be replaced by selector flow. |
| Current codebase | `frontend/src/App.css` | Frontend styling | Detected in codebase | Existing shell styling can be replaced or adapted for the selector flow. |
| Current codebase | `frontend/src/App.test.tsx` | Frontend test | Detected in codebase | Current test only verifies scaffold heading and must be updated. |
| Current codebase | `frontend/package.json` | Frontend dependencies and scripts | Detected in codebase | Confirms `npm run test`, `npm run typecheck`, and `npm run build`; no `@testing-library/user-event` dependency exists. |
| PRD | `docs/product/prd.md` | Not available | Missing / documented limitation | File exists but is empty in the current workspace. |
| Tech Spec | `docs/specs/tech-spec.md` | Not available | Missing / documented limitation | File exists but is empty in the current workspace. |
| Technology definition | `docs/architecture/technology-definition.md` | Not available | Missing / documented limitation | File exists but is empty in the current workspace. |
| User decision | Current `plan-task` session | Source gap handling | Confirmed by user | Proceed using task 009, project planning, ADR-002, ADR-008, task 008 execution, and codebase evidence as binding sources; document empty files as limitations. |
| User decision | Current `plan-task` session | Default selected operation | Confirmed by user | Default the selector to MP4-to-MP3 conversion. |
| User decision | Current `plan-task` session | Shared state surface style | Confirmed by user | Render a static non-interactive status/result panel for idle/loading/success/error/result readiness placeholders. |

## Context Summary

The project now has a React/Vite/TypeScript frontend scaffold and a backend REST contract surface. The MVP needs a single anonymous frontend flow where users choose between MP4-to-MP3 conversion and public URL download. This task replaces the scaffold-only UI with a selector flow and non-interactive shared state placeholders, while leaving actual upload, URL input, API submission, result download, and backend behavior to later tasks.

## Task Goal

Build a tested React operation selector flow that defaults to MP4-to-MP3 conversion, lets users switch to public URL download, and displays static shared operation state placeholders without making backend calls.

## Confirmed Scope

- Replace the scaffold-only app shell in `frontend/src/App.tsx`.
- Add typed React state for selected operation.
- Use two operation values aligned with ADR-008: conversion and URL download.
- Default the selected operation to MP4-to-MP3 conversion.
- Render accessible selector controls for MP4-to-MP3 conversion and public URL download.
- Render operation-specific placeholder panels for later tasks 010 and 011.
- Render a static non-interactive shared status/result panel covering idle/loading/success/error/result-download readiness placeholders.
- Update `frontend/src/App.css` for the selector flow.
- Update `frontend/src/App.test.tsx` to cover initial render, default conversion panel, switching to URL download, and static state placeholders.

## Out of Scope

- Do not implement MP4 upload form behavior.
- Do not implement URL input form behavior.
- Do not submit requests to backend APIs.
- Do not call `POST /api/operations/conversions`, `POST /api/operations/downloads`, operation status, or result endpoints.
- Do not implement media conversion, URL download, result download, polling, cleanup, metrics, operation events, or operation tracking.
- Do not add routing or separate service pages.
- Do not introduce user accounts or saved history.
- Do not add new frontend dependencies unless an implementation blocker is discovered and documented.
- Do not change backend source code.
- Do not update PRD, project planning, technology definition, Tech Spec, final ADRs, task files, or unrelated documents during implementation.

## Requirements Covered

| Requirement | Source | How This Task Covers It | Status |
| --- | --- | --- | --- |
| Single MVP flow with operation selector | Project planning, task file | Replaces the scaffold shell with one selector-driven React flow. | Confirmed |
| MP4-to-MP3 conversion operation visible | Project planning, ADR-008, task file, user decision | Adds conversion option and defaults to its placeholder panel. | Confirmed |
| Public URL download operation visible | Project planning, ADR-008, task file | Adds URL download option and placeholder panel. | Confirmed |
| Anonymous MVP use | Project planning, task file | Adds no accounts, auth, history, or user-specific state. | Confirmed |
| No separate service pages in MVP | Project planning, task file | Keeps operation switching inside one React screen with no routing. | Confirmed |
| Shared loading/success/error/result UI state surfaces | Task file, user decision | Adds a static shared status/result panel for later integration tasks. | Confirmed |
| Frontend tests cover selector behavior | Task file | Updates Vitest/Testing Library tests for render and switching behavior. | Confirmed |

## Technical Specification Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Not available | Missing | None directly from Tech Spec content. | `docs/specs/tech-spec.md` exists but is empty. User confirmed proceeding with ADRs, task file, planning, task 008 execution, and codebase evidence as binding sources. |

Coverage assessment:

- Justifying Tech Spec section: unavailable because the Tech Spec file is empty.
- Tech Spec sections implemented by this task: none directly from Tech Spec content.
- Gaps between task and Tech Spec: UI component composition, default operation, state placeholder style, and frontend validation expectations are not documented in the Tech Spec.
- Dependencies not specified by the Tech Spec: resolved by ADR-002, ADR-008, task 009, task 008 execution, codebase evidence, and current user decisions.
- Source limitation handling: the empty Tech Spec, PRD, and technology-definition files are documented limitations and do not block task 009 planning because the user confirmed proceeding with the available binding sources.

## Architecture and ADR Considerations

| Decision or ADR | Source | Impact on This Task | Status |
| --- | --- | --- | --- |
| React/Vite/TypeScript frontend served by Spring Boot | ADR-002, task 001 execution | Implement in the existing `frontend/` app and preserve TypeScript/Vite conventions. | Accepted |
| REST is the frontend/backend boundary | ADR-002, ADR-008 | Selector labels and future integration placeholders should align with REST operation concepts, but this task makes no API calls. | Accepted |
| Operation-centered public REST contract | ADR-008, task 008 execution | UI operation choices should align with conversion and URL download operation distinctions. | Accepted |
| Conversion should be validated before URL download | Project planning | Default selected operation is conversion. | Confirmed by user |
| Static state surface for shared operation states | User decision | Implement non-interactive placeholders instead of fake state transition controls. | Confirmed |
| Empty Tech Spec / PRD / technology-definition source gap | Source review, user decision | Proceed with documented limitations; no required ADR or architecture blocker for this frontend selector task. | Resolved by user |

ADR candidates or architecture decisions needed:

- None. No new formal ADR is required for this task.
- The selector default and static shared state surface are task-level planning decisions, not ADR candidates.

Architecture decision notes:

- Saved separately: Yes
- Path: `docs/architecture/task-decisions/mvp-media-utility/009-build-react-operation-selector-flow-architecture-decisions.md`
- Notes file status: New

## Confirmed Decisions

- The selected task is `MVP-MEDIA-009`.
- The plan path is `docs/task-plans/mvp-media-utility/009-build-react-operation-selector-flow-plan.md`.
- The architecture decision notes path is `docs/architecture/task-decisions/mvp-media-utility/009-build-react-operation-selector-flow-architecture-decisions.md`.
- Empty PRD, Tech Spec, and technology-definition files are documented limitations, not blockers for this task.
- Binding sources for this task are task 009, project planning, ADR-002, ADR-008, task 001 execution, task 008 execution, codebase evidence, and current user decisions.
- Default selected operation is MP4-to-MP3 conversion.
- Shared loading/success/error/result-download readiness surfaces should be static and non-interactive.
- No backend API calls, request submission, upload handling, URL handling, polling, or result download behavior should be implemented in this task.
- Do not add `@testing-library/user-event`; use existing Testing Library utilities such as `fireEvent` unless a future implementation discovers a concrete blocker.

## Pending Decisions

None. All task-relevant decisions have been answered or explicitly deferred out of scope by the user.

## Questions for the User

None. All task-relevant questions have been answered.

## Proposed Implementation Approach

1. Re-read task 009, this plan, the architecture decision notes, ADR-002, ADR-008, and current `frontend/src` files.
2. Replace the current scaffold shell in `App.tsx` with a single selector-driven React screen.
3. Define a small TypeScript union or constant map for the two operations, using internal frontend keys such as `conversion` and `download` while displaying user-facing labels.
4. Initialize selected operation state to conversion.
5. Render an accessible two-option selector as a segmented control or button group with `aria-pressed` or tab-like semantics.
6. Render a conversion placeholder panel when conversion is selected and a URL download placeholder panel when download is selected.
7. Render one static shared status/result panel that represents idle, loading, success, error, and result-download readiness placeholders without controls or fake transitions.
8. Update CSS for responsive, stable layout using the existing app-level styling approach.
9. Update tests to cover default render, conversion-first behavior, switching to URL download, selector state indication, and static shared status placeholders.
10. Scope-review the diff to verify that no backend/API integration or out-of-scope behavior was added.

## Files and Areas Expected to Change

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| `frontend/src/App.tsx` | Modify | Task file, current codebase, user decisions | Replace scaffold shell with selector state, operation panels, and static shared state panel. |
| `frontend/src/App.css` | Modify | Task file, current codebase | Replace or adapt scaffold styles for the selector flow. |
| `frontend/src/App.test.tsx` | Modify | Task file, current codebase | Replace scaffold test with selector and switching coverage. |
| `frontend/src/` | Create only if useful | Task file | Small component/type files may be created if they reduce complexity; keep scope limited to selector UI. |

## Step-by-Step Implementation Plan

1. Verify working tree state before editing and preserve unrelated user changes.
2. Inspect `frontend/src/App.tsx`, `frontend/src/App.css`, and `frontend/src/App.test.tsx`.
3. In `App.tsx`, define the operation type and operation metadata for:
   - MP4-to-MP3 conversion;
   - Public URL download.
4. Add `useState` for selected operation, initialized to conversion.
5. Replace the scaffold section with:
   - page heading for Media Utility;
   - short single-flow summary;
   - selector controls for the two operations;
   - selected operation detail panel;
   - static shared status/result panel.
6. Keep operation-specific panels as placeholders:
   - conversion panel should reserve space for the future MP4 upload form from task 010;
   - URL download panel should reserve space for the future URL form from task 011;
   - neither panel should contain working inputs, submit buttons, API calls, or validation behavior.
7. Render the static shared status/result panel with placeholder labels for idle, loading, success, error, and result ready/download state. Do not add fake transition controls.
8. Update CSS to provide a responsive single-screen app layout, accessible button states, and stable panel dimensions. Preserve 8px-or-less card radius and avoid nested card styling.
9. Update `App.test.tsx` with Testing Library tests:
   - app heading and selector render;
   - conversion is selected by default;
   - clicking URL download shows the URL placeholder and updates selector state;
   - clicking conversion returns to the conversion placeholder;
   - static shared state placeholders are visible;
   - no backend endpoint text or behavior is asserted as active submission.
10. Use existing `fireEvent` from `@testing-library/react` for click tests because `@testing-library/user-event` is not currently installed.
11. Run frontend validation commands during future implementation:
   - `npm run test`;
   - `npm run typecheck`;
   - `npm run build` if CSS/structure changes are significant.
12. Perform a final scope review confirming no backend changes, API calls, upload handling, URL submission, polling, result download behavior, cleanup, metrics, or event tracking were added.

## Validation Strategy

- Run `npm run test` from `frontend/`.
- Run `npm run typecheck` from `frontend/`.
- Run `npm run build` from `frontend/` if implementation modifies enough UI structure to warrant full frontend build validation.
- Review the diff to verify all changes stay in frontend selector files or small frontend-only helper files.
- Verify task boundaries manually: no backend code, no API calls, no form submission, no upload handling, no URL validation, no result download, no polling, no metrics, and no event tracking.

## Tests to Add or Update

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| Initial selector render | Frontend unit/component | Verify heading, selector controls, and default conversion panel render. | Update `frontend/src/App.test.tsx`. |
| Operation switching | Frontend unit/component | Verify users can switch from conversion to URL download and back. | Use `fireEvent` from existing Testing Library dependency. |
| Selected state semantics | Frontend unit/component | Verify selected control exposes selected state, such as `aria-pressed="true"` if buttons are used. | Keeps selector accessible and testable. |
| Static shared state panel | Frontend unit/component | Verify idle/loading/success/error/result-ready placeholder labels render. | No fake state transition controls. |
| Scope review | Manual/static | Verify no API calls, backend changes, upload behavior, URL submission, polling, cleanup, metrics, or event tracking. | Required before task completion. |

## Acceptance Criteria

- [ ] React app renders a single MVP operation selector flow instead of the scaffold-only placeholder.
- [ ] MP4-to-MP3 conversion is selected by default.
- [ ] Users can switch between MP4-to-MP3 conversion and public URL download selections.
- [ ] Selected operation state is represented in React and drives the visible operation panel.
- [ ] Operation-specific placeholders exist for later MP4 upload and URL form tasks.
- [ ] A static shared status/result panel represents idle, loading, success, error, and result-download readiness without performing backend work.
- [ ] UI remains a single anonymous flow with no separate service pages, routing, accounts, or saved history.
- [ ] Frontend tests cover initial rendering, default conversion selection, operation switching, selected-state semantics, and shared state placeholders.
- [ ] No backend API calls, upload submission, URL submission, media processing, result download, cleanup, metrics, or event tracking behavior is implemented.
- [ ] Frontend validation commands pass or any environment blocker is documented in the execution report.

## Risks and Edge Cases

- Empty Tech Spec, PRD, and technology-definition files reduce formal source coverage; this is mitigated by user-confirmed reliance on task 009, project planning, ADRs, task 008 execution, and codebase evidence.
- Adding interactive fake status controls could blur scope with later form/submission tasks; this plan requires a static status panel.
- Adding inputs or submit buttons could accidentally implement task 010 or task 011 behavior; this task should use placeholders only.
- Adding new test dependencies would expand scope; use existing Testing Library utilities unless implementation hits a documented blocker.
- UI copy must avoid final legal/responsibility wording because that remains a later product/legal decision.

## Rollback or Recovery Notes

Rollback should be limited to reverting the frontend files changed for this selector task. Since this task must not alter backend APIs or persistent data, recovery should not require database, API, or migration rollback.

## Documentation Updates

- Do not update PRD, project planning, technology definition, Tech Spec, final ADRs, task files, or unrelated documents during implementation.
- A future task execution report should document changed frontend files, tests run, selected default operation, and confirmation that out-of-scope backend/API behavior was not added.

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

- Do not execute this task without using this saved plan as source context.
- Keep task 009 frontend-only.
- Treat task 010 as the place for MP4 upload form state and task 011 as the place for URL download form state.
- Treat ADR-008 as future API integration context only; do not call those endpoints in this task.
- Use conversion as the default selected operation.
- Use a static status/result panel for shared operation states.
