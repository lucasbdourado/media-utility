# Task Implementation Plan: Build MP4 Upload Form States

## Status

Status: Ready for Implementation

Last updated: 2026-06-03

Plan file: `docs/task-plans/mvp-media-utility/010-build-mp4-upload-form-states-plan.md`

Architecture decision notes file: `docs/architecture/task-decisions/mvp-media-utility/010-build-mp4-upload-form-states-architecture-decisions.md`

## Task Reference

Task ID: `MVP-MEDIA-010`

Task file: `docs/tasks/mvp-media-utility/010-build-mp4-upload-form-states.md`

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
| Task file | `docs/tasks/mvp-media-utility/010-build-mp4-upload-form-states.md` | Goal, Scope, Out of Scope, Dependencies, Validation, Acceptance Criteria, Open Questions | Confirmed by source document | Defines the frontend-only MP4 upload form state task. |
| Project discovery | `docs/context/project-discover.md` | Project Scenario | Confirmed by source document | Required Harness discovery document exists, but it predates the current implementation codebase. |
| Project planning | `docs/planning/project-planning.md` | Functional Scope, Web Experience, Media Conversion, Task Breakdown, Suggested Task Order | Confirmed by source document | Confirms single anonymous MVP flow, MP4-to-MP3 conversion, operation selector, and non-MP4 rejection or warning expectation. |
| ADR-002 | `docs/adrs/002-use-react-vite-typescript-served-by-spring-boot.md` | Decision, Consequences | Accepted | Confirms React, Vite, TypeScript, frontend directory, and REST frontend/backend boundary. |
| ADR-008 | `docs/adrs/008-define-public-rest-api-contract.md` | Decision, HTTP status mapping, Consequences | Accepted | Confirms future conversion endpoint, multipart upload field name `file`, and validation error shape for later backend integration. |
| Task 009 plan | `docs/task-plans/mvp-media-utility/009-build-react-operation-selector-flow-plan.md` | Confirmed Scope, Architecture and ADR Considerations, Step-by-Step Implementation Plan | Confirmed by source document | Confirms conversion is default, task 010 owns MP4 upload form behavior, and task 009 did not add API/form behavior. |
| Task 009 architecture notes | `docs/architecture/task-decisions/mvp-media-utility/009-build-react-operation-selector-flow-architecture-decisions.md` | Confirmed Architecture Decisions, Implementation Impact | Confirmed by source document | Confirms selector architecture, single anonymous flow, and static shared operation state surfaces. |
| Task 009 execution report | `docs/task-executions/mvp-media-utility/009-build-react-operation-selector-flow-execution.md` | Execution Summary, Implemented Changes, Decisions Used | Confirmed by source document | Confirms current frontend selector implementation and placeholders for task 010 and task 011. |
| Current codebase | `frontend/src/App.tsx` | React app shell | Detected in codebase | Contains `conversion` and `download` operation metadata, selected operation state, conversion placeholder, URL placeholder, and static status panel. |
| Current codebase | `frontend/src/App.css` | Frontend styling | Detected in codebase | Contains selector, workspace, placeholder, and status panel styles that task 010 should extend or adapt. |
| Current codebase | `frontend/src/App.test.tsx` | Frontend tests | Detected in codebase | Covers task 009 selector behavior and should be extended for MP4 upload form states. |
| Current codebase | `frontend/package.json` | Scripts and dependencies | Detected in codebase | Confirms `test`, `typecheck`, and `build` scripts; no `@testing-library/user-event` dependency exists. |
| PRD | `docs/product/prd.md` | Not available | Missing / documented limitation | File exists but is empty in the current workspace. |
| Tech Spec | `docs/specs/tech-spec.md` | Not available | Missing / documented limitation | File exists but is empty in the current workspace. |
| Technology definition | `docs/architecture/technology-definition.md` | Not available | Missing / documented limitation | File exists but is empty in the current workspace. |
| User decision | Current `plan-task` session | Source gap handling | Confirmed by user | Proceed using the task file, project planning, accepted ADRs, task 009 artifacts, and current codebase evidence as binding sources; document empty files as limitations. |
| User decision | Current `plan-task` session | Invalid MP4 feedback wording | Confirmed by user | Use concise neutral copy such as `Select an MP4 file to continue.` |
| User decision | Current `plan-task` session | MP4 UX acceptance rule | Confirmed by user | Accept files with `.mp4` extension or `video/mp4` MIME type as UX-level validation only. |
| User decision | Current `plan-task` session | Selected file size display | Confirmed by user | Display selected MP4 file size as a rounded MB value. |
| User decision | Current `plan-task` session | Invalid file state handling | Confirmed by user | Reject invalid files by clearing selected file state, showing feedback, and keeping submit-ready state inactive. |
| User decision | Current `plan-task` session | Valid submit behavior | Confirmed by user | Prevent default submission, make no API calls, and show a submit-ready message for later conversion submission work. |

## Context Summary

The React app currently renders the task 009 operation selector flow. MP4-to-MP3 conversion is selected by default and shows an `MP4 upload form placeholder`. Task 010 replaces that conversion placeholder with frontend-only MP4 upload form state. This task must keep the app in the single anonymous flow and must not call backend APIs, create multipart requests, or implement conversion processing.

## Task Goal

Add a tested MP4 upload form state to the conversion workspace so users can select an MP4 file, receive required and invalid-file feedback, see selected-file information, and reach a frontend-only submit-ready state without backend interaction.

## Confirmed Scope

- Replace the conversion operation placeholder with an MP4 upload form surface.
- Preserve the existing operation selector flow and URL download placeholder behavior.
- Add React state for selected file, validation feedback, and submit-ready feedback.
- Add a file input using browser-level MP4 hints.
- Use `accept=".mp4,video/mp4"` or an equivalent MP4 browser hint.
- On empty form submit, prevent default and show required-file feedback.
- On non-MP4 selection, reject the file by clearing selected file state, show `Select an MP4 file to continue.`, and keep submit-ready state inactive.
- On valid MP4 selection, clear invalid/required feedback, show selected file name and rounded MB size, and show a submit-ready surface.
- On valid form submit, prevent default, make no API calls, and show or maintain a submit-ready message for later conversion submission work.
- Update frontend styling for the upload form, validation feedback, selected-file state, and submit-ready surface.
- Update focused frontend tests for required-file feedback, invalid file rejection, valid file selection, submit-ready behavior, and operation selector compatibility.

## Out of Scope

- Do not implement backend upload validation.
- Do not submit requests to `POST /api/operations/conversions`.
- Do not create `multipart/form-data` requests or `FormData`.
- Do not call operation status or result endpoints.
- Do not implement FFmpeg conversion, process execution, polling, result download, cleanup, metrics, or event tracking.
- Do not implement URL download form behavior.
- Do not change backend source code.
- Do not add routing, accounts, saved history, or separate service pages.
- Do not update PRD, project planning, technology definition, Tech Spec, final ADRs, task files, or unrelated documents during implementation.

## Requirements Covered

| Requirement | Source | How This Task Covers It | Status |
| --- | --- | --- | --- |
| Single MVP flow with operation selector | Project planning, task 009 artifacts, current codebase | Keeps task 009 selector and attaches upload form only to conversion workspace. | Confirmed |
| MP4 upload to MP3 conversion operation | Project planning, task file | Adds frontend MP4 file selection state for the conversion path. | Confirmed |
| MP4 selection at UX level | Task file, user decision | Uses MP4 browser hint and `.mp4` extension or `video/mp4` MIME UX validation. | Confirmed |
| Required file feedback | Task file | Submitting without selected file shows visible required-file feedback. | Confirmed |
| Non-MP4 rejection or warning | Task file, user decision | Rejects invalid file selection, clears selected state, and shows concise warning copy. | Confirmed |
| Selected file information | Task file, user decision | Displays file name and rounded MB file size for valid MP4 selections. | Confirmed |
| Submit-ready state surface | Task file, user decision | Shows frontend-only readiness message without API submission. | Confirmed |
| Frontend tests cover upload states | Task file | Extends `App.test.tsx` for required, invalid, valid, ready, and selector-switching behavior. | Confirmed |

## Technical Specification Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Not available | Missing | None directly from Tech Spec content. | `docs/specs/tech-spec.md` exists but is empty. User confirmed proceeding with task, planning, ADR, prior task, and codebase sources. |

Coverage assessment:

- Justifying Tech Spec section: unavailable because the Tech Spec file is empty.
- Tech Spec sections implemented by this task: none directly from Tech Spec content.
- Gaps between task and Tech Spec: frontend upload-form composition, exact validation copy, MP4 UX check rule, invalid-file handling, selected-file size display, and submit-ready behavior are not documented in the Tech Spec.
- Dependencies not specified by the Tech Spec: resolved by task 010, project planning, ADR-002, ADR-008, task 009 artifacts, current codebase evidence, and current user decisions.
- Source limitation handling: the empty Tech Spec, PRD, and technology-definition files are documented limitations and do not block this frontend-only task because the user confirmed proceeding with available binding sources.

## Architecture and ADR Considerations

| Decision or ADR | Source | Impact on This Task | Status |
| --- | --- | --- | --- |
| React/Vite/TypeScript frontend served by Spring Boot | ADR-002 | Implement in the existing `frontend/` React app using TypeScript conventions. | Accepted |
| REST frontend/backend boundary | ADR-002, ADR-008 | This task may align names with future REST behavior but must not call APIs. | Accepted |
| Future conversion upload endpoint and field | ADR-008 | Treat `POST /api/operations/conversions` and upload field `file` as naming/boundary context only. | Accepted |
| Public validation error shape | ADR-008 | Do not implement backend errors; frontend feedback may use similar field concept but remains local UI state. | Accepted |
| Current operation selector flow | Task 009 plan, architecture notes, execution report, codebase | Attach MP4 upload form to the existing conversion workspace and preserve selector switching. | Confirmed |
| Client-side MP4 validation is UX-only | Task file, user decision | Implement as local feedback only and do not treat it as security or backend validation. | Confirmed |
| Empty Tech Spec / PRD / technology-definition source gap | Source review, user decision | Proceed with documented limitations; no required ADR or architecture blocker for this frontend form-state task. | Resolved by user |

ADR candidates or architecture decisions needed:

- None. No new formal ADR is required for this task.
- The MP4 UX validation rule, invalid-file handling, size display, and submit-ready behavior are task-level UI decisions, not ADR candidates.

Architecture decision notes:

- Saved separately: Yes
- Path: `docs/architecture/task-decisions/mvp-media-utility/010-build-mp4-upload-form-states-architecture-decisions.md`
- Notes file status: New

## Confirmed Decisions

- The selected task is `MVP-MEDIA-010`.
- The plan path is `docs/task-plans/mvp-media-utility/010-build-mp4-upload-form-states-plan.md`.
- The architecture decision notes path is `docs/architecture/task-decisions/mvp-media-utility/010-build-mp4-upload-form-states-architecture-decisions.md`.
- Empty PRD, Tech Spec, and technology-definition files are documented limitations, not blockers for this task.
- Binding sources are task 010, project planning, ADR-002, ADR-008, task 009 plan/architecture notes/execution report, current codebase evidence, and current user decisions.
- Use concise invalid-file feedback copy: `Select an MP4 file to continue.`
- Use `.mp4` extension or `video/mp4` MIME type as UX-only MP4 validation.
- Reject invalid files by clearing selected file state, showing feedback, and keeping submit-ready state inactive.
- Display selected valid MP4 file name and rounded MB size.
- On valid submit, prevent default, make no API calls, and show a submit-ready message for later conversion submission work.
- Do not add `@testing-library/user-event`; use existing Testing Library utilities such as `fireEvent` unless a future implementation discovers and documents a concrete blocker.

## Pending Decisions

None. All task-relevant decisions have been answered or explicitly deferred out of scope by the user.

## Questions for the User

None. All task-relevant questions have been answered.

## Proposed Implementation Approach

1. Re-read task 010, this plan, the architecture decision notes, ADR-002, ADR-008, task 009 execution report, and current `frontend/src` files.
2. Preserve the task 009 operation selector metadata and switching behavior.
3. Replace only the conversion placeholder surface with an MP4 upload form component or inline form state.
4. Keep the URL download workspace as a placeholder for task 011.
5. Track selected file, validation message, and submit-ready message in React state.
6. Use a file input with MP4 browser hints and a stable accessible label.
7. Validate selected file client-side using `.mp4` file extension or `video/mp4` MIME type.
8. Keep validation strictly UX-level and local to frontend state.
9. Prevent default form submission for every submit path.
10. Show required feedback when submitting without a selected file.
11. Reject invalid files by clearing selected state and showing the confirmed feedback copy.
12. Show selected MP4 file name, rounded MB size, and a submit-ready state surface for valid selections.
13. On valid submit, show or maintain a message indicating the file is ready for later conversion submission, without creating requests, `FormData`, or API calls.
14. Update CSS for the upload form, feedback, selected-file summary, and ready state while keeping the app responsive and consistent with existing task 009 styling.
15. Extend tests to cover upload form states and verify operation selector compatibility.
16. Scope-review the diff to verify no backend/API integration or out-of-scope behavior was added.

## Files and Areas Expected to Change

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| `frontend/src/App.tsx` | Modify | Task file, task 009 codebase, user decisions | Add MP4 upload form state within the conversion workspace. |
| `frontend/src/App.css` | Modify | Task file, current codebase | Style upload form, feedback messages, selected-file state, and submit-ready state. |
| `frontend/src/App.test.tsx` | Modify | Task file, validation need | Add focused tests for required, invalid, valid, ready, and operation-switching behavior. |
| `frontend/src/` | Create only if useful | Task file | A small component or type file may be created if it keeps upload form state focused and maintainable. |

## Step-by-Step Implementation Plan

1. Verify working tree state before editing and preserve unrelated user changes.
2. Inspect `frontend/src/App.tsx`, `frontend/src/App.css`, and `frontend/src/App.test.tsx`.
3. In `App.tsx`, add upload-form state:
   - selected file state, initially `null`;
   - validation feedback state, initially absent;
   - submit-ready message or flag, initially inactive.
4. Add small helper logic for MP4 UX validation:
   - accept a file when `file.name` ends with `.mp4` case-insensitively or `file.type === "video/mp4"`;
   - treat this helper as frontend UX validation only.
5. Add small helper logic for selected file size:
   - convert bytes to MB;
   - display a rounded human-readable MB value such as `12.4 MB`.
6. Replace the conversion placeholder surface with a form:
   - accessible file input labelled for MP4 upload;
   - `accept=".mp4,video/mp4"` or equivalent;
   - selected-file summary area;
   - validation feedback area;
   - submit-ready state surface;
   - submit button for the frontend-only form state.
7. On file input change:
   - clear prior submit-ready feedback;
   - if no file is present, clear selected file and leave validation feedback inactive until submit;
   - if the file is not accepted by the MP4 UX helper, clear selected file and show `Select an MP4 file to continue.`;
   - if the file is valid, store it, clear validation feedback, and show selected file name and rounded MB size.
8. On form submit:
   - call `event.preventDefault()`;
   - if no selected file exists, show required-file feedback and keep submit-ready state inactive;
   - if selected file exists, clear validation feedback and show a submit-ready message for later conversion submission.
9. Preserve the operation selector controls, `aria-pressed` behavior, conversion default selection, URL download placeholder, and static shared operation state surfaces from task 009.
10. Decide state reset on operation switching as follows:
   - switching to URL download does not implement URL behavior;
   - when returning to conversion, preserving or resetting upload state is acceptable only if tests and UI are consistent, but prefer preserving local conversion form state because no source requires clearing it.
11. Update CSS by extending existing task 009 styles:
   - keep a stable responsive workspace layout;
   - style file input, validation feedback, selected-file summary, and ready state;
   - preserve readable contrast and existing app visual direction;
   - avoid nested cards and keep bordered surfaces at 8px border radius or less.
12. Update `App.test.tsx` using existing Testing Library APIs:
   - render conversion workspace MP4 upload form by default;
   - submit without a selected file and assert required feedback;
   - select a non-MP4 `File` and assert selected file info is absent, warning appears, and ready state is inactive;
   - select a valid `.mp4` or `video/mp4` `File` and assert file name, rounded MB size, and no invalid feedback;
   - submit valid selection and assert submit-ready message appears with no API calls;
   - switch to URL download and back to verify selector compatibility and conversion form accessibility.
13. Run frontend validation during future implementation:
   - `npm run test`;
   - `npm run typecheck`;
   - `npm run build` if implementation changes build-relevant TypeScript or CSS structure.
14. Perform final scope review:
   - no backend files changed;
   - no `fetch`, `axios`, `XMLHttpRequest`, `/api/operations`, `FormData`, multipart creation, polling, result download, cleanup, metrics, or tracking added.

## Validation Strategy

- Run `npm run test` from `frontend/`.
- Run `npm run typecheck` from `frontend/`.
- Run `npm run build` from `frontend/` if implementation modifies build-relevant TypeScript or CSS structure.
- Review the diff to verify all source implementation changes stay in frontend files.
- Static scope review should verify no backend API calls, no multipart request creation, no conversion processing, no polling, no result download, no cleanup, no metrics, and no event tracking.
- Optional browser verification may be used after implementation if the local frontend is running and a browser surface is available.

## Tests to Add or Update

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| Conversion workspace upload form render | Frontend unit/component | Verify conversion workspace renders MP4 upload form instead of placeholder. | Update `frontend/src/App.test.tsx`. |
| Required-file submit feedback | Frontend unit/component | Verify submitting without selected file shows required feedback. | Use `fireEvent.submit` or click the form submit button. |
| Invalid file rejection | Frontend unit/component | Verify non-MP4 selection is rejected, feedback appears, selected-file info is absent, and ready state is inactive. | Use a `File` such as `notes.txt` with `text/plain`. |
| Valid MP4 selection | Frontend unit/component | Verify valid MP4 selection clears invalid feedback and displays file name and rounded MB size. | Use `.mp4` extension or `video/mp4` MIME in test file. |
| Valid submit-ready state | Frontend unit/component | Verify valid submit prevents default behavior and shows ready-for-later-submission feedback without API calls. | Do not mock backend success. |
| Operation selector compatibility | Frontend unit/component | Verify users can still switch to URL download and back after upload form changes. | Preserve task 009 behavior coverage. |
| Scope review | Manual/static | Verify no backend/API/multipart/conversion/polling/result/cleanup/metrics/tracking behavior was added. | Required before task completion. |

## Acceptance Criteria

- [ ] The conversion operation workspace renders an MP4 upload form instead of the task 010 placeholder.
- [ ] The file input indicates MP4 selection at the user experience level.
- [ ] React state tracks the selected file, validation feedback, and submit-ready feedback.
- [ ] Submitting with no selected file shows required-file feedback.
- [ ] Selecting a non-MP4 file is rejected by clearing selected file state and showing `Select an MP4 file to continue.` without backend interaction.
- [ ] Selecting an MP4 file displays selected-file name and rounded MB size.
- [ ] A valid selected MP4 can reach a frontend-only submit-ready state without API calls.
- [ ] The existing operation selector flow still lets users switch between conversion and URL download.
- [ ] Frontend tests cover required, invalid, valid, ready, and operation-switching behavior.
- [ ] No backend API calls, multipart uploads, media conversion, polling, result download, cleanup, metrics, or event tracking behavior is implemented.

## Risks and Edge Cases

- Client-side file checks are only a UX aid and must not be treated as security or backend validation.
- Browser-provided MIME types can be missing or inconsistent; this plan accepts either `.mp4` extension or `video/mp4` MIME type for UX-level validation.
- A file named with `.mp4` could still be invalid media; backend validation remains a later task.
- Adding real API calls or `FormData` would blur boundaries with backend validation and conversion endpoint tasks.
- Overbuilding processing, result, or download states would blur boundaries with later tasks.
- Empty PRD, Tech Spec, and technology-definition files reduce formal source coverage; this is mitigated by the user-confirmed source gap decision and accepted ADRs.

## Rollback or Recovery Notes

Rollback should be limited to reverting the frontend files changed for this task. Since task 010 must not alter backend APIs, persistent data, or media files, recovery should not require database, API, migration, or storage rollback.

## Documentation Updates

- Do not update PRD, project planning, technology definition, Tech Spec, final ADRs, task files, or unrelated documents during implementation.
- A future task execution report should document changed frontend files, tests run, user decisions used for MP4 validation behavior, and confirmation that out-of-scope backend/API behavior was not added.

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

- Do not execute this task without using this saved plan and architecture decision notes as source context.
- Keep task 010 frontend-only.
- Attach upload form state to the existing conversion workspace introduced by task 009.
- Keep URL download behavior for task 011.
- Treat ADR-008 as future API boundary context only; do not call `POST /api/operations/conversions`.
- Do not create `FormData` or multipart requests in this task.
- Treat `.mp4` extension or `video/mp4` MIME checks as UX-level only.
- Preserve the single anonymous MVP flow and operation selector switching behavior.
