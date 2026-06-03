# Task: Build MP4 Upload Form States

## Status

Status: Depends on Previous Task

Last updated: 2026-06-03

## Task ID

ID: MVP-MEDIA-010

Order: 010

Task file: `docs/tasks/mvp-media-utility/010-build-mp4-upload-form-states.md`

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project planning | `docs/planning/project-planning.md` | Web Experience, Media Conversion, Task Breakdown, Execution Sequence | Confirmed by source document | Confirms the MP4 upload form state task, MP4-to-MP3 conversion path, UX-level MP4 restriction, and non-MP4 rejection or warning validation expectation. |
| ADR-002 | `docs/adrs/002-use-react-vite-typescript-served-by-spring-boot.md` | Decision, Consequences | Accepted | Confirms React, Vite, TypeScript, dedicated `frontend/` directory, and REST as the frontend/backend boundary. |
| ADR-008 | `docs/adrs/008-define-public-rest-api-contract.md` | Decision, HTTP status mapping, Consequences | Accepted | Confirms future conversion creation endpoint, multipart form-data, upload field name `file`, and standard validation error shape. |
| Task 009 | `docs/tasks/mvp-media-utility/009-build-react-operation-selector-flow.md` | Scope, Implementation Instructions, Acceptance Criteria | Confirmed by source document | Confirms task 010 attaches file selection and upload state to the conversion placeholder created by the operation selector task. |
| Current codebase | `frontend/src/App.tsx` | React app shell | Detected in codebase | Current conversion workspace contains the task 010 placeholder and shared state surfaces. |
| Current codebase | `frontend/src/App.test.tsx` | Frontend tests | Detected in codebase | Current tests cover the selector flow and should be extended for MP4 upload form state behavior. |
| Current codebase | `frontend/package.json` | Scripts | Detected in codebase | Defines `test`, `typecheck`, and `build` validation commands. |
| PRD | `docs/product/prd.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |
| Tech Spec | `docs/specs/tech-spec.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |
| Technology definition | `docs/architecture/technology-definition.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |

## Context

The MVP web flow lets anonymous users choose MP4-to-MP3 conversion or public URL download. Task 009 creates the operation selector and a conversion workspace placeholder. This task fills the conversion workspace with frontend MP4 upload form state only, so later tasks can add backend validation, conversion submission, processing, and result download behavior.

## Goal

Add a tested frontend MP4 upload form state to the conversion workspace without performing real backend upload or conversion work.

## Scope

- Replace the conversion placeholder with an MP4 upload form surface when the conversion operation is selected.
- Add React state for the selected file.
- Add required-file feedback when the user attempts to submit without selecting a file.
- Restrict the file picker to MP4 at the user experience level.
- Reject or warn when the selected file is not an MP4 file.
- Display selected MP4 file information that is safe for the user interface, such as file name and size.
- Add a submit-ready state surface for later conversion submission work.
- Update frontend styling for the MP4 upload form.
- Update focused frontend tests for required-file feedback, invalid file feedback, valid file selection, and operation selector compatibility.

## Out of Scope

- Do not implement backend upload validation.
- Do not submit requests to `POST /api/operations/conversions`.
- Do not create `multipart/form-data` requests.
- Do not call operation status or result endpoints.
- Do not implement FFmpeg conversion or process execution.
- Do not implement operation polling, result download, cleanup, metrics, or event tracking.
- Do not implement URL download form behavior.
- Do not change backend source code.
- Do not update PRD, project planning, technology definition, Tech Spec, ADRs, or unrelated task files.

## Implementation Instructions

- Work in the existing React frontend under `frontend/src/`.
- Attach the MP4 upload form to the conversion operation workspace introduced by task 009.
- Use the future public API contract from ADR-008 only as naming and boundary context:
  - the conversion endpoint will later use `POST /api/operations/conversions`;
  - the upload field will later be named `file`;
  - this task must not perform that request.
- Use a file input that indicates MP4 selection at the browser UI level.
- Keep validation client-side and state-based:
  - no selected file should produce visible required-file feedback;
  - non-MP4 input should be rejected or warned;
  - a valid MP4 selection should clear invalid-file feedback and make the form submit-ready.
- Keep any MIME type or extension checks as UX-level validation only. Backend validation remains a later task.
- Preserve the single anonymous flow and operation selector behavior from task 009.
- Preserve React/Vite/TypeScript conventions already present in the scaffold.

## Expected Files

| Path | Action | Source | Notes |
| --- | --- | --- | --- |
| `frontend/src/App.tsx` | Modify | Current codebase, task 009, project planning, ADR-008 | Add MP4 upload form state within the conversion workspace. |
| `frontend/src/App.css` | Modify | Current codebase | Style the upload form, feedback messages, and selected-file state. |
| `frontend/src/App.test.tsx` | Modify | Current codebase, validation need | Add focused tests for MP4 upload form states. |
| `frontend/src/` | Create if useful | Implementation discretion | Small component or type files may be added if they keep the form state focused and maintainable. |

## Dependencies

| Dependency | Type | Status | Notes |
| --- | --- | --- | --- |
| MVP-MEDIA-009 | Previous task | Completed | Operation selector and conversion workspace placeholder exist in the current frontend. |
| MVP-MEDIA-012 | Later task | Pending | Backend MP4 upload validation belongs to a later task and must not be implemented here. |
| MVP-MEDIA-013 | Later task | Pending | FFmpeg conversion adapter belongs to a later task and must not be implemented here. |
| MVP-MEDIA-014 | Later task | Pending | Conversion operation endpoint belongs to a later task and must not be implemented here. |
| URL download form task | Later task | Pending | URL form behavior belongs to task 011. |

## Validation

- Run `npm run test` from `frontend/`.
- Run `npm run typecheck` from `frontend/`.
- Run `npm run build` from `frontend/` if the implementation changes build-relevant TypeScript or CSS structure.
- Verify tests cover:
  - the conversion workspace renders the MP4 upload form;
  - submitting without a selected file shows required-file feedback;
  - selecting a non-MP4 file is rejected or warned;
  - selecting an MP4 file shows selected-file information and submit-ready state;
  - switching between conversion and URL download still works.
- Scope review verifies no backend code, API submission, multipart request creation, conversion processing, polling, result download, cleanup, metrics, or event tracking was added.

## Acceptance Criteria

- [ ] The conversion operation workspace renders an MP4 upload form instead of the task 010 placeholder.
- [ ] The file input indicates MP4 selection at the user experience level.
- [ ] React state tracks the selected file and validation feedback.
- [ ] Submitting with no selected file shows required-file feedback.
- [ ] Selecting a non-MP4 file is rejected or warned without backend interaction.
- [ ] Selecting an MP4 file displays selected-file information and a submit-ready state surface.
- [ ] The existing operation selector flow still lets users switch between conversion and URL download.
- [ ] Frontend tests cover required, invalid, valid, and operation-switching behavior.
- [ ] No backend API calls, multipart uploads, media conversion, polling, result download, cleanup, metrics, or event tracking behavior is implemented.

## Risks

- Client-side file checks are only a user experience aid and must not be treated as security or backend validation.
- Adding real API calls in this task would blur boundaries with conversion endpoint and backend validation tasks.
- Overbuilding upload flow states could blur boundaries with processing, result, and download tasks.
- Empty PRD, Tech Spec, and technology-definition files reduce source confidence; project planning and accepted ADRs are the binding sources for this task.

## Open Questions

- What exact user-facing wording should be used for invalid MP4 feedback before public launch?
- Should the frontend accept only `.mp4` by extension, `video/mp4` by MIME type, or both as UX-level checks? This can be selected during implementation unless product or backend validation documents confirm a stricter rule.
- Should selected-file size be shown in bytes, MB, or another display format? This can be selected during implementation as display-only UI behavior.

## Notes for the Implementing Agent

- Do not execute this task directly before `plan-task` creates an implementation plan.
- Treat ADR-008 as future API boundary context only; do not integrate with the API in this task.
- Treat backend validation and conversion submission as later work.
- Keep this task focused on frontend form state, validation feedback, selected-file display, and tests.
