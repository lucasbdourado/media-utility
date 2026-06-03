# Task: Build React Operation Selector Flow

## Status

Status: Depends on Previous Task

Last updated: 2026-06-03

## Task ID

ID: MVP-MEDIA-009

Order: 009

Task file: `docs/tasks/mvp-media-utility/009-build-react-operation-selector-flow.md`

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project planning | `docs/planning/project-planning.md` | Functional Scope, Web Experience, Task Breakdown, Execution Sequence | Confirmed by source document | Confirms a single MVP web flow with an operation selector for URL download or MP4-to-MP3 conversion. |
| ADR-002 | `docs/adrs/002-use-react-vite-typescript-served-by-spring-boot.md` | Decision, Consequences | Accepted | Confirms React, Vite, TypeScript, and REST as the frontend/backend boundary. |
| ADR-008 | `docs/adrs/008-define-public-rest-api-contract.md` | Decision, Consequences | Accepted | Confirms operation-centered API routes and operation type distinction for conversion and URL download. |
| Task 001 execution report | `docs/task-executions/mvp-media-utility/001-scaffold-spring-boot-react-monolith-execution.md` | Implemented Changes | Confirmed by source document | Confirms the current frontend scaffold exists under `frontend/` with `App.tsx`, `App.css`, and a basic render test. |
| Current codebase | `frontend/src/App.tsx` | React app shell | Detected in codebase | Current UI is scaffold-only and has no operation selector. |
| Current codebase | `frontend/src/App.test.tsx` | Frontend test | Detected in codebase | Current test only verifies the scaffold heading. |
| PRD | `docs/product/prd.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |
| Tech Spec | `docs/specs/tech-spec.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |
| Technology definition | `docs/architecture/technology-definition.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |

## Context

The MVP requires one anonymous web flow where a user chooses between MP4-to-MP3 conversion and public URL download. The React/Vite/TypeScript scaffold currently renders only a placeholder shell. ADR-008 defines the backend operation types and REST routes that later frontend tasks will use, but this task is limited to the selector and shared frontend state surfaces.

## Goal

Replace the scaffold-only React shell with a tested operation selector flow that lets users switch between conversion and URL download UI states.

## Scope

- Add React state for the selected media operation.
- Render two selectable operation choices:
  - MP4-to-MP3 conversion.
  - Public URL download.
- Keep the operation choices aligned with ADR-008 operation distinctions.
- Show operation-specific placeholder areas for later form tasks.
- Add shared frontend display states for loading, success, error, and result-download readiness as UI state surfaces only.
- Update frontend styling for the selector flow.
- Update focused frontend tests for default rendering and operation switching.

## Out of Scope

- Do not implement MP4 upload form behavior.
- Do not implement URL input form behavior.
- Do not submit requests to backend APIs.
- Do not call `POST /api/operations/conversions`, `POST /api/operations/downloads`, status, or result endpoints.
- Do not implement media conversion, URL download, result download, polling, cleanup, metrics, or event tracking.
- Do not create legal/responsibility wording beyond placeholders already justified by planning; final wording remains outside this task.
- Do not change backend source code.
- Do not update PRD, project planning, technology definition, Tech Spec, ADRs, or unrelated task files.

## Implementation Instructions

- Work in the existing React frontend under `frontend/src/`.
- Replace the scaffold shell in `App.tsx` with the MVP operation selector surface.
- Use TypeScript types or constants for the selected operation state instead of stringly-typed repeated literals.
- Default to one operation on initial render; the exact default can be chosen during implementation as long as tests document it.
- Ensure users can switch between conversion and URL download without page navigation.
- Keep placeholders explicit enough for later tasks 010 and 011 to attach their form states.
- Keep the UI anonymous and single-flow; do not introduce accounts, separate service pages, or routing.
- Keep network/API integration out of this task.
- Preserve React/Vite/TypeScript conventions already present in the scaffold.

## Expected Files

| Path | Action | Source | Notes |
| --- | --- | --- | --- |
| `frontend/src/App.tsx` | Modify | Current codebase, project planning, ADR-008 | Add selector state and operation-specific UI surfaces. |
| `frontend/src/App.css` | Modify | Current codebase | Style the selector flow and shared state surfaces. |
| `frontend/src/App.test.tsx` | Modify | Current codebase, validation need | Test render and operation switching behavior. |
| `frontend/src/` | Create if useful | Implementation discretion | Small component or type files may be added if they keep the selector focused and maintainable. |

## Dependencies

| Dependency | Type | Status | Notes |
| --- | --- | --- | --- |
| MVP-MEDIA-001 | Previous task | Completed with Follow-ups | React/Vite/TypeScript scaffold exists. |
| MVP-MEDIA-008 | Previous task | Ready for Implementation | ADR-008 confirms operation-centered contracts; this task should align with those operation distinctions. |
| MP4 upload form task | Later task | Pending | Detailed upload form behavior belongs to task 010. |
| URL download form task | Later task | Pending | Detailed URL form behavior belongs to task 011. |
| Legal/responsibility wording | Product/legal decision | Pending | Planning identifies this as required before public MVP, but final wording is not part of this selector task. |

## Validation

- Run the frontend test command from `frontend/package.json`.
- Run the frontend typecheck or build command if implementation changes TypeScript structure.
- Verify tests cover:
  - the selector renders;
  - the default selected operation renders its corresponding panel;
  - users can switch to the other operation;
  - shared loading/success/error/result state surfaces are represented without backend calls.
- Scope review verifies no backend code, API submission, upload behavior, URL submission behavior, media processing, polling, cleanup, metrics, or event tracking was added.

## Acceptance Criteria

- [ ] The React app renders a single MVP operation selector flow instead of the scaffold-only placeholder.
- [ ] Users can switch between MP4-to-MP3 conversion and public URL download selections.
- [ ] The selected operation state is represented in React and drives the visible operation panel.
- [ ] Operation-specific placeholders exist for later MP4 upload and URL form tasks.
- [ ] Shared loading, success, error, and result-download readiness UI state surfaces exist without performing real backend work.
- [ ] The UI remains a single anonymous flow with no separate service pages or account features.
- [ ] Frontend tests cover initial rendering and operation switching.
- [ ] No backend API calls, upload submission, URL submission, media processing, result download, cleanup, metrics, or event tracking behavior is implemented.

## Risks

- Adding real API calls in this task would couple the UI before the later form and endpoint implementation tasks are complete.
- Overbuilding the selector could blur task boundaries with upload and URL form tasks.
- Final legal/responsibility wording is not confirmed, so any public URL notice text should remain placeholder-level or deferred.
- Empty PRD, Tech Spec, and technology-definition files reduce source confidence; project planning and accepted ADRs are the binding sources for this task.

## Open Questions

- What final responsibility or acceptable-use wording should appear in the URL download flow before public launch?
- Should the default selected operation be conversion or URL download? This can be selected during implementation unless product confirms a preference.

## Notes for the Implementing Agent

- Do not execute this task directly before `plan-task` creates an implementation plan.
- Treat task 010 as the place for MP4 upload form states.
- Treat task 011 as the place for URL download form states.
- Treat ADR-008 as binding for operation distinctions and future API integration points.
- Keep this task focused on frontend selector behavior and state-driven UI only.
