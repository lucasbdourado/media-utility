# Task: Build URL Download Form States

## Status

Status: Depends on Previous Task

Last updated: 2026-06-03

## Task ID

ID: MVP-MEDIA-011

Order: 011

Task file: `docs/tasks/mvp-media-utility/011-build-url-download-form-states.md`

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project planning | `docs/planning/project-planning.md` | Web Experience, Legal and UX Messaging, Task Breakdown, Execution Sequence | Confirmed by source document | Confirms the URL download form state task, legal responsibility notice addition, and client-side validation rules. |
| ADR-002 | `docs/adrs/002-use-react-vite-typescript-served-by-spring-boot.md` | Decision, Consequences | Accepted | Confirms React, Vite, TypeScript, dedicated `frontend/` directory, and REST as the frontend/backend boundary. |
| ADR-008 | `docs/adrs/008-define-public-rest-api-contract.md` | Decision, HTTP status mapping, Consequences | Accepted | Confirms future download creation endpoint, JSON request payload `{ "url": "https://..." }`, and standard validation error shape. |
| Task 009 | `docs/tasks/mvp-media-utility/009-build-react-operation-selector-flow.md` | Scope, Implementation Instructions, Acceptance Criteria | Confirmed by source document | Confirms task 011 replaces the URL download form placeholder in the workspace introduced by task 009. |
| Current codebase | `frontend/src/App.tsx` | React app shell | Detected in codebase | Contains the URL download placeholder, selector flow, and MP4 form states. |
| Current codebase | `frontend/src/App.test.tsx` | Frontend tests | Detected in codebase | Contains tests for selector flow and MP4 form, to be extended for URL download form states. |
| Current codebase | `frontend/package.json` | Scripts | Detected in codebase | Defines `test`, `typecheck`, and `build` validation commands. |
| PRD | `docs/product/prd.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |
| Tech Spec | `docs/specs/tech-spec.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |
| Technology definition | `docs/architecture/technology-definition.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |

## Context

The MVP web flow lets anonymous users choose MP4-to-MP3 conversion or public URL download. Task 009 creates the operation selector and a workspace placeholder. Task 010 implements the MP4 upload form. This task replaces the URL download placeholder with the public URL input form, validation states, and a responsibility notice, so later tasks can add backend validation, yt-dlp integration, processing, and result download behavior.

## Goal

Add a tested frontend public URL download form state with input validation and a responsibility notice to the URL download workspace without performing real backend download or network requests.

## Scope

- Replace the URL download placeholder in the React workspace with a public URL input form when selected.
- Add React state for the URL input.
- Add required-field feedback when the user attempts to submit without entering a URL.
- Validate URL format on the client side (verifying it is a well-formed URL starting with `http://` or `https://`).
- Display client-side validation feedback when an invalid URL is entered.
- Display a legal responsibility notice directly above or near the submit button in the URL download flow.
- Add a submit-ready state surface for later URL download submission work.
- Update frontend styling for the URL download form, validation feedback, and responsibility notice.
- Update focused frontend tests to cover required URL validation, invalid URL format validation, responsibility notice display, switching between operations, and submit-ready state display.

## Out of Scope

- Do not implement backend URL download validation.
- Do not submit requests to `POST /api/operations/downloads`.
- Do not perform real network requests or background downloads.
- Do not call operation status or result endpoints.
- Do not implement yt-dlp download adapter or process execution.
- Do not implement operation polling, result download, cleanup, metrics, or event tracking.
- Do not implement MP4 upload form behavior (handled in Task 010).
- Do not change backend source code.
- Do not update PRD, project planning, technology definition, Tech Spec, ADRs, or unrelated task files.

## Implementation Instructions

- Work in the existing React frontend under `frontend/src/`.
- Replace the URL placeholder in `App.tsx` with the URL download form.
- Use the future public API contract from ADR-008 only as naming and boundary context: the download endpoint will later use `POST /api/operations/downloads` with payload `{ "url": "https://..." }`. This task must not perform that request.
- Display a clear, readable responsibility notice directly above the submit button in the URL form. Use a draft copy such as: *"By submitting this URL, you confirm that you have the right to download this media and agree to our Terms of Service."*
- Keep validation client-side and state-based:
  - an empty input should produce required-field feedback;
  - an invalid URL format should produce validation feedback;
  - a valid HTTP/HTTPS URL selection should clear feedback and display a submit-ready state indicating that the URL is prepared for submission.
- Preserve the single anonymous flow and operation selector behavior.
- Preserve React/Vite/TypeScript conventions already present in the scaffold.

## Expected Files

| Path | Action | Source | Notes |
| --- | --- | --- | --- |
| `frontend/src/App.tsx` | Modify | Current codebase, task 009, project planning, ADR-008 | Add URL download form state, input validation, and responsibility notice. |
| `frontend/src/App.css` | Modify | Current codebase | Style the URL download form, responsibility notice, and feedback. |
| `frontend/src/App.test.tsx` | Modify | Current codebase, validation need | Add focused tests for URL download form states. |

## Dependencies

| Dependency | Type | Status | Notes |
| --- | --- | --- | --- |
| MVP-MEDIA-009 | Previous task | Completed | Operation selector and workspace layout exist in the frontend. |
| MVP-MEDIA-010 | Previous task | Completed | MP4 upload form and its CSS patterns exist in the frontend. |
| MVP-MEDIA-015 | Later task | Pending | Backend URL validation belongs to a later task and must not be implemented here. |
| MVP-MEDIA-016 | Later task | Pending | yt-dlp download adapter belongs to a later task and must not be implemented here. |
| MVP-MEDIA-017 | Later task | Pending | URL download endpoint belongs to a later task and must not be implemented here. |

## Validation

- Run `npm run test` from `frontend/`.
- Run `npm run typecheck` from `frontend/`.
- Run `npm run build` from `frontend/` if the implementation changes build-relevant TypeScript or CSS structure.
- Verify tests cover:
  - the URL download workspace renders the URL input form;
  - submitting without a URL shows required-field feedback;
  - submitting an invalid URL format shows validation feedback;
  - the responsibility notice is clearly visible;
  - submitting a valid HTTP/HTTPS URL shows submit-ready state;
  - switching between conversion and URL download still works.
- Scope review verifies no backend code, API submission, or external downloading was added.

## Acceptance Criteria

- [ ] The public URL download workspace renders a URL input form instead of the placeholder.
- [ ] The form contains a text input for the URL, a responsibility notice, and a submit button.
- [ ] React state tracks the URL input, validation feedback, and readiness state.
- [ ] Submitting with an empty input shows required-field feedback.
- [ ] Submitting an invalid URL (e.g. missing scheme, invalid format) shows invalid-URL feedback.
- [ ] The responsibility notice is clearly visible before submission.
- [ ] Submitting a valid HTTP/HTTPS URL displays a submit-ready state surface.
- [ ] The existing operation selector still functions correctly.
- [ ] Frontend tests cover required, invalid, valid, and operation-switching behavior for the URL download form.
- [ ] No backend API calls, download processing, polling, result download, cleanup, metrics, or event tracking is implemented.

## Risks

- Client-side validation is UX-only and must not be treated as backend validation or legal enforcement.
- Adding real API calls or background downloading in this task would blur boundaries with later integration tasks.
- Empty PRD, Tech Spec, and technology-definition files reduce source confidence; project planning and accepted ADRs are the binding sources for this task.

## Open Questions

- What exact wording should be used for the responsibility notice before public launch?
- Should the validation enforce YouTube-specific domain checking on the client side, or just generic HTTP/HTTPS checks? Basic HTTP/HTTPS checks are selected for now.

## Notes for the Implementing Agent

- Do not execute this task directly before `plan-task` creates an implementation plan.
- Treat ADR-008 as future API boundary context only.
- Keep this task focused on frontend form state, validation feedback, and tests.
