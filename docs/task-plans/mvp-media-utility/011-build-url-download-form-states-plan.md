# Task Implementation Plan: Build URL Download Form States

## Status

Status: Ready for Implementation

Last updated: 2026-06-03

Plan file: `docs/task-plans/mvp-media-utility/011-build-url-download-form-states-plan.md`

Architecture decision notes file: Not generated

## Task Reference

Task ID: MVP-MEDIA-011

Task file: `docs/tasks/mvp-media-utility/011-build-url-download-form-states.md`

Task status: Depends on Previous Task (Note: MVP-MEDIA-009 and MVP-MEDIA-010 are completed)

Task group or feature: mvp-media-utility

## Planning Mode Requirement

Plan mode verified: Yes

Notes:

- This plan was created in plan mode.
- Implementation must not start during `plan-task`.
- A future implementation request must use this saved plan as source context.

## Source Documents

List every document, ADR, codebase evidence item, or explicit user decision used to prepare this plan.

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | [011-build-url-download-form-states.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/011-build-url-download-form-states.md) | Scope, Instructions, Acceptance Criteria | Confirmed by source document | Primary task definition. |
| Project planning | [project-planning.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/planning/project-planning.md) | Web Experience, Legal and UX Messaging | Confirmed by source document | Confirms requirements and sequence. |
| ADR-002 | [002-use-react-vite-typescript-served-by-spring-boot.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/adrs/002-use-react-vite-typescript-served-by-spring-boot.md) | Decision, Consequences | Accepted | Confirms React, Vite, and TypeScript. |
| ADR-008 | [008-define-public-rest-api-contract.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/adrs/008-define-public-rest-api-contract.md) | Request payloads | Accepted | Confirms endpoint and payload schema. |
| Codebase | [App.tsx](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/frontend/src/App.tsx) | Component layout | Detected in codebase | Main application entry point. |
| Codebase | [App.css](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/frontend/src/App.css) | Layout styles | Detected in codebase | Styling rules for forms and panels. |
| Codebase | [App.test.tsx](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/frontend/src/App.test.tsx) | App component tests | Detected in codebase | Test suite for layout and selector. |
| User Decision | Plan-task Q&A Session | Q1, Q2, Q3 responses | Confirmed by user | Confirmed wording, validation method, and styling reuse. |

## Context Summary

The MVP web experience allows users to choose between converting an MP4 file or downloading media from a public URL. The operation selector flow (Task 009) and the MP4 upload form (Task 010) have been successfully built and styled. This task replaces the static placeholder in the public URL download workspace with a functional URL input form, displaying client-side validation states (required and format verification) and a responsibility notice, preparing the UI for future backend API integration.

## Task Goal

Add a tested frontend public URL download form state with input validation and a responsibility notice to the URL download workspace without performing real backend download or network requests.

## Confirmed Scope

- Replace the placeholder `<div className="placeholder-surface">` inside the URL download pane in `App.tsx` with a `<form>` containing a text input, responsibility notice text, and a submit button.
- Implement React state variables for managing URL input (`urlInput`), validation feedback (`urlFeedback`), and readiness message (`urlReadyMessage`).
- Validate the input on submission:
  - If the input is empty or contains only whitespace, show required-field feedback: `"Enter a URL before continuing."`.
  - If the input is not a well-formed URL starting with `http://` or `https://` (validated using browser's built-in `URL` constructor), show format validation feedback: `"Enter a valid HTTP or HTTPS URL to continue."`.
  - If the URL is valid, clear feedback and set the ready message: `"This URL is ready for the later download submission step."`.
- Display a legal responsibility notice: *"By submitting this URL, you confirm that you have the right to download this media and agree to our Terms of Service."*
- Ensure the form layout and elements reuse existing CSS styling classes in `App.css` (e.g. `.upload-form`, `.file-control`, `.form-feedback`, `.ready-surface`, `.submit-button`).
- Add a custom CSS rule for `.responsibility-notice` in `App.css` to fit nicely and cohesively with the rest of the form.
- Update `App.test.tsx` to assert:
  - Switching to URL download renders the form.
  - Submitting an empty form triggers required-field feedback.
  - Submitting an invalid URL format triggers format validation feedback.
  - Submitting a valid URL shows the submit-ready state surface.
  - Editing the input after feedback clears the messages.

## Out of Scope

- Implementing backend URL download endpoints or controllers.
- Making actual network calls or `fetch` requests to `POST /api/operations/downloads`.
- Integrating `yt-dlp` or performing media extraction.
- Implementing progress tracking, downloading status pages, or file saving.

## Requirements Covered

| Requirement | Source | How This Task Covers It | Status |
| --- | --- | --- | --- |
| URL form interface | MVP-MEDIA-011 | Replaces workspace placeholder with URL input form. | Confirmed |
| URL state tracking | MVP-MEDIA-011 | Introduces React state for text value, error, and ready state. | Confirmed |
| Client-side validation | MVP-MEDIA-011 | Validates URL presence and well-formedness (HTTP/HTTPS). | Confirmed |
| Responsibility notice | FR-007 / MVP-MEDIA-011 | Renders the legal notice copy before form submission. | Confirmed |
| Form unit testing | MVP-MEDIA-011 | Adds tests verifying validation feedback, switching, and ready states. | Confirmed |

## Technical Specification Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| N/A | Missing | N/A | `tech-spec.md` is empty in current workspace. |

Coverage assessment:

- Justifying Tech Spec section: N/A (Using task scope and planning documents)
- Tech Spec sections implemented by this task: N/A
- Gaps between task and Tech Spec: N/A
- Dependencies not specified by the Tech Spec: N/A

## Architecture and ADR Considerations

| Decision or ADR | Source | Impact on This Task | Status |
| --- | --- | --- | --- |
| ADR-002 | `docs/adrs/002-use-react-vite-typescript-served-by-spring-boot.md` | React, Vite, and TypeScript structure constraints. | Confirmed |
| ADR-008 | `docs/adrs/008-define-public-rest-api-contract.md` | Provides endpoint boundary contract context. | Confirmed |

ADR candidates or architecture decisions needed:

- None.

Architecture decision notes:

- Saved separately: No
- Path: N/A
- Notes file status: Not applicable

## Confirmed Decisions

- **Notice copy**: *"By submitting this URL, you confirm that you have the right to download this media and agree to our Terms of Service."*
- **Validation**: Basic HTTP/HTTPS checks using the browser's built-in `URL` constructor.
- **Styling**: Reuse existing CSS class names (`.upload-form`, `.file-control`, `.form-feedback`, `.ready-surface`, `.submit-button`) in `App.css` to keep look and feel cohesive.
- **Wording of feedback**:
  - Required error: `"Enter a URL before continuing."`
  - Format error: `"Enter a valid HTTP or HTTPS URL to continue."`
  - Success readiness: `"This URL is ready for the later download submission step."`

## Pending Decisions

None. All task-relevant decisions have been answered or explicitly deferred out of scope by the user.

## Questions for the User

None. All task-relevant questions have been answered.

## Proposed Implementation Approach

1. **State Definition**: In `App.tsx`, introduce three states:
   - `urlInput` (initialized to `""`)
   - `urlFeedback` (initialized to `null`)
   - `urlReadyMessage` (initialized to `null`)
2. **Input Handler**: Create a `handleUrlChange` handler that updates `urlInput` and resets `urlFeedback` and `urlReadyMessage` to ensure the user does not see stale feedback after they start typing again.
3. **Validation logic**: Write a helper function `isValidUrl` inside `App.tsx` that tries to construct a new `URL(input)` and checks if `url.protocol` is `http:` or `https:`.
4. **Form submit handler**: Create `handleUrlSubmit` to handle form submission:
   - Prevent default event.
   - If `urlInput.trim()` is empty, set `urlFeedback` to `"Enter a URL before continuing."` and reset ready state.
   - If not empty, call `isValidUrl(urlInput)`. If invalid, set `urlFeedback` to `"Enter a valid HTTP or HTTPS URL to continue."` and reset ready state.
   - If valid, clear `urlFeedback` and set `urlReadyMessage` to `"This URL is ready for the later download submission step."`.
5. **Form rendering**: Replace the placeholder `<div className="placeholder-surface" ...>` with the `<form>` element containing:
   - Input block styled with `.file-control` (or modified to allow `.url-control` with same styling).
   - Form feedback paragraph styled with `.form-feedback`.
   - Responsibility notice paragraph styled with `.responsibility-notice`.
   - Expiration / readiness status container styled with `.ready-surface`.
   - Submit button styled with `.submit-button` containing text `"Prepare download"`.
6. **Styling refinements**: Update `App.css` to apply consistent styles. Add styles for `.responsibility-notice`.
7. **Test suite updates**: Update `App.test.tsx` to modify the selector switching test (verifying the form exists instead of the placeholder) and add tests for URL validation cases.

## Files and Areas Expected to Change

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| [App.tsx](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/frontend/src/App.tsx) | Modify | Current codebase | Replace placeholder with URL form, add state and handlers. |
| [App.css](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/frontend/src/App.css) | Modify | Current codebase | Style the responsibility notice and reuse form controls. |
| [App.test.tsx](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/frontend/src/App.test.tsx) | Modify | Current codebase | Update operation switcher test and add validation/ready state assertions. |

## Step-by-Step Implementation Plan

1. **Modify `App.tsx`**:
   - Define state hooks `urlInput`, `urlFeedback`, `urlReadyMessage`.
   - Write helper function `isValidUrl(input: string): boolean`.
   - Write handlers `handleUrlChange(event: React.ChangeEvent<HTMLInputElement>)` and `handleUrlSubmit(event: React.FormEvent<HTMLFormElement>)`.
   - Replace placeholder UI in the download panel with the `<form>` markup structure.
2. **Modify `App.css`**:
   - Add `.url-control` to style definitions matching `.file-control`.
   - Add styling rules for `.responsibility-notice` with a left border color `#2f6f68` and size `0.85rem` to match the theme.
3. **Modify `App.test.tsx`**:
   - Update `lets users switch to URL download and back without navigation` test to check for `screen.getByRole("form", { name: /url download form/i })` instead of `/url form placeholder/i`.
   - Add test `shows required-url feedback when submitting without a URL`.
   - Add test `shows invalid-url feedback when submitting an invalid URL format`.
   - Add test `shows responsibility notice and submit-ready state with a valid URL`.
   - Add test `resets URL feedback and ready state when input changes`.
4. **Validation**:
   - Run tests using `npm run test` inside `frontend/` directory.
   - Run TypeScript type checks using `npm run typecheck` inside `frontend/`.
   - Build frontend using `npm run build` to verify compiling.

## Validation Strategy

- Run unit and component tests with Jest (`npm run test`).
- Enforce strict typing compilation checks (`npm run typecheck`).
- Verify production bundle build successfully (`npm run build`).

## Tests to Add or Update

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| Operation selector switch | UI / Integration | Verifies switching to URL workspace renders the form. | Updates existing test in `App.test.tsx`. |
| Empty URL submission | UI / Integration | Verifies required error message displays. | New test. |
| Invalid URL format | UI / Integration | Verifies format error message displays for malformed URLs. | New test. |
| Valid URL submission | UI / Integration | Verifies ready surface message displays. | New test. |
| Input change reset | UI / Integration | Verifies error and ready messages disappear when typing begins. | New test. |

## Acceptance Criteria

- [ ] The public URL download workspace renders a URL input form instead of the placeholder.
- [ ] The form contains a text input for the URL, a responsibility notice, and a submit button.
- [ ] React state tracks the URL input, validation feedback, and readiness state.
- [ ] Submitting with an empty input shows required-field feedback: `"Enter a URL before continuing."`.
- [ ] Submitting an invalid URL format shows invalid-URL feedback: `"Enter a valid HTTP or HTTPS URL to continue."`.
- [ ] The responsibility notice copy matches exactly: *"By submitting this URL, you confirm that you have the right to download this media and agree to our Terms of Service."*
- [ ] Submitting a valid HTTP/HTTPS URL displays a submit-ready state surface: `"This URL is ready for the later download submission step."`.
- [ ] The existing operation selector still functions correctly.
- [ ] Frontend tests cover required, invalid, valid, and operation-switching behavior for the URL download form.
- [ ] No backend API calls, download processing, polling, result download, cleanup, metrics, or event tracking are implemented.

## Risks and Edge Cases

- **Validation Robustness**: Client-side validation uses `new URL()`. Edge cases like `http://localhost` or plain IP addresses are accepted by `new URL()`, which is appropriate for basic well-formedness.
- **Accidental Integration**: The developer might be tempted to add fetch logic to the endpoint `POST /api/operations/downloads` defined in ADR-008. The plan explicitly forbids this.

## Rollback or Recovery Notes

- If changes fail code validation, git rollback `frontend/src/App.tsx`, `App.css`, and `App.test.tsx` to revert to previous clean state.

## Documentation Updates

- None.

## Implementation Readiness Checklist

- [x] Task scope is clear.
- [x] Source documents were reviewed.
- [x] Tech Spec coverage was verified.
- [x] Architecture decisions were checked.
- [x] ADR candidates were confirmed, rejected, required-before-implementation, or explicitly deferred by the user.
- [x] Pending decisions are resolved or explicitly deferred out of this task by the user.
- [x] User questions were answered, or the plan is explicitly marked `Blocked`.
- [x] Expected files or areas are identified.
- [x] Acceptance criteria are defined.
- [x] Test strategy is defined.
- [x] Risks and edge cases are documented.

## Notes for the Implementing Agent

- Ensure HTML5 accessibility is preserved: use `htmlFor`, labels, and proper ARIA roles (`role="alert"`, `aria-live="polite"`).
- Make sure to clear state on changing input values so the user does not see outdated errors or ready status.
- Do not implement any network request calls to backend controllers.
