# Task Architecture Decision Notes: Build MP4 Upload Form States

## Status

Status: Ready for Implementation

Last updated: 2026-06-03

Decision notes file: `docs/architecture/task-decisions/mvp-media-utility/010-build-mp4-upload-form-states-architecture-decisions.md`

Task plan file: `docs/task-plans/mvp-media-utility/010-build-mp4-upload-form-states-plan.md`

Task file: `docs/tasks/mvp-media-utility/010-build-mp4-upload-form-states.md`

## Purpose

Record task-specific architecture decisions, conflicts, and confirmed ADR candidates identified while preparing the task implementation plan.

This file is planning support only. It is not a final ADR and must not replace the ADR workflow.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/tasks/mvp-media-utility/010-build-mp4-upload-form-states.md` | Scope, Implementation Instructions, Risks, Open Questions | Confirmed by source document | Defines frontend-only MP4 upload form state and prohibits backend/API implementation. |
| Project planning | `docs/planning/project-planning.md` | Web Experience, Media Conversion, Task Breakdown, Suggested Task Order | Confirmed by source document | Confirms MP4 upload form state belongs to web experience before backend conversion implementation. |
| ADR-002 | `docs/adrs/002-use-react-vite-typescript-served-by-spring-boot.md` | Decision, Consequences | Accepted | Confirms React, Vite, TypeScript, frontend directory, and REST boundary. |
| ADR-008 | `docs/adrs/008-define-public-rest-api-contract.md` | Decision, Consequences | Accepted | Confirms future conversion endpoint, multipart field name `file`, and backend validation error shape. |
| Task 009 plan | `docs/task-plans/mvp-media-utility/009-build-react-operation-selector-flow-plan.md` | Architecture and ADR Considerations, Notes for the Implementing Agent | Confirmed by source document | Confirms task 010 owns MP4 upload form behavior and task 009 remains frontend-only. |
| Task 009 architecture notes | `docs/architecture/task-decisions/mvp-media-utility/009-build-react-operation-selector-flow-architecture-decisions.md` | Confirmed Architecture Decisions | Confirmed by source document | Confirms selector flow architecture and no backend integration precedent. |
| Task 009 execution report | `docs/task-executions/mvp-media-utility/009-build-react-operation-selector-flow-execution.md` | Implemented Changes, Decisions Used | Confirmed by source document | Confirms current code includes a conversion placeholder for task 010. |
| Current codebase | `frontend/src/App.tsx`, `frontend/src/App.css`, `frontend/src/App.test.tsx`, `frontend/package.json` | Frontend app and tests | Detected in codebase | Confirms current React implementation and available frontend scripts/dependencies. |
| PRD | `docs/product/prd.md` | Not available | Missing / documented limitation | File exists but is empty. |
| Tech Spec | `docs/specs/tech-spec.md` | Not available | Missing / documented limitation | File exists but is empty. |
| Technology definition | `docs/architecture/technology-definition.md` | Not available | Missing / documented limitation | File exists but is empty. |
| User decisions | Current `plan-task` session | Source gap and frontend validation behavior | Confirmed by user | Confirms source-gap handling, invalid copy, MP4 UX rule, size display, invalid-file rejection, and valid submit-ready behavior. |

## Confirmed Architecture Decisions

| Decision | Source | Impact on This Task | Notes |
| --- | --- | --- | --- |
| Frontend is React with Vite and TypeScript. | ADR-002 | Implement upload form state in the existing `frontend/` React app. | Accepted ADR constraint. |
| Frontend/backend communication is through REST. | ADR-002, ADR-008 | This task must avoid backend calls while preserving future REST boundary context. | Accepted ADR constraint. |
| Future conversion creation endpoint is `POST /api/operations/conversions` with multipart field `file`. | ADR-008 | Use only as future naming/boundary context; do not create requests in task 010. | Accepted ADR constraint. |
| MVP uses one anonymous flow with no separate service pages. | Project planning, task 009 artifacts | Upload form should attach to the existing conversion workspace without routing/account changes. | Confirmed planning constraint. |
| Client-side MP4 validation is UX-only. | Task file, user decision | Implement local feedback for `.mp4` extension or `video/mp4` MIME, but leave authoritative validation to later backend tasks. | Confirmed task-level decision. |
| Invalid non-MP4 selection is rejected locally. | User decision | Clear selected file state, show feedback, and keep submit-ready state inactive. | Confirmed task-level decision. |
| Valid form submit remains frontend-only. | User decision | Prevent default submission and show readiness without API calls or multipart creation. | Confirmed task-level decision. |
| Empty Tech Spec, PRD, and technology-definition files do not block task 010. | User decision | Plan is ready using task, planning, ADR, prior task, and codebase evidence as binding sources. | Resolved source gap. |

## Pending Architecture Decisions

None. All task-relevant architecture decisions have been answered or explicitly deferred out of scope by the user.

## Architecture Conflicts

| Conflict | Sources | Impact | Resolution Status |
| --- | --- | --- | --- |
| Tech Spec, PRD, and technology-definition files are empty, but task 010 needs concrete frontend planning. | Empty source files vs task planning requirements | Could block a ready implementation plan if treated as required source coverage. | Resolved by user decision to proceed with task, project planning, accepted ADRs, task 009 artifacts, and codebase evidence. |
| ADR-008 defines a future conversion endpoint, but task 010 must not implement API integration. | ADR-008, task 010 out-of-scope, task 009 no-API precedent | Implementer could accidentally create `FormData` or call the endpoint too early. | Resolved by explicit frontend-only scope and no API-call acceptance criteria. |
| MP4 validation is needed in the frontend, but authoritative validation belongs to later backend tasks. | Task 010 validation scope, tasks 012/014 dependencies | Implementer could overstate client validation as security validation. | Resolved by requiring UX-only validation language and preserving backend validation as later work. |

## ADR Candidates

| Candidate | Trigger | Suggested ADR Scope | Blocking for This Task? |
| --- | --- | --- | --- |
| None | No new architecture-level decision is introduced by this frontend form-state task. | Not applicable. | No |

## Implementation Impact

- Task 010 can proceed without a new formal ADR.
- Implementation must stay in the React frontend unless a small frontend-only component/type file is useful.
- The upload form must attach to the existing conversion workspace and preserve task 009 selector behavior.
- ADR-008 should guide future endpoint and field naming context only; task 010 must not call the API or create multipart requests.
- The MP4 check must be implemented as UX-level local validation using `.mp4` extension or `video/mp4` MIME type.
- Invalid files must be rejected locally by clearing selected file state and showing feedback.
- Valid submissions must prevent default browser behavior and show a readiness state only.
- Empty Tech Spec, PRD, and technology-definition files remain documented limitations but are not blockers for this task.

## Questions for the User

None. All task-relevant architecture questions have been answered.

## Notes for the Implementing Agent

- This file is planning support only; it is not a final ADR.
- Use ADR-002 for frontend stack constraints and ADR-008 for future API boundary context.
- Do not treat the existing API contract as permission to call backend endpoints in task 010.
- Do not create `FormData`, multipart payloads, polling, result download behavior, metrics, or event tracking.
- Keep backend validation and conversion submission for later tasks.
- Preserve the single anonymous MVP flow and current operation selector behavior.
