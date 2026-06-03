# Task Architecture Decision Notes: Build React Operation Selector Flow

## Status

Status: Ready for Implementation

Last updated: 2026-06-03

Decision notes file: `docs/architecture/task-decisions/mvp-media-utility/009-build-react-operation-selector-flow-architecture-decisions.md`

Task plan file: `docs/task-plans/mvp-media-utility/009-build-react-operation-selector-flow-plan.md`

Task file: `docs/tasks/mvp-media-utility/009-build-react-operation-selector-flow.md`

## Purpose

Record task-specific architecture decisions, conflicts, and confirmed ADR candidates identified while preparing the task implementation plan.

This file is planning support only. It is not a final ADR and must not replace the ADR workflow.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/tasks/mvp-media-utility/009-build-react-operation-selector-flow.md` | Scope, Dependencies, Open Questions | Confirmed by source document | Defines the selector task and frontend-only boundaries. |
| Project planning | `docs/planning/project-planning.md` | Functional Scope, Web Experience, Suggested Task Order | Confirmed by source document | Confirms single web flow, operation selector, anonymous access, and no separate service pages. |
| ADR-002 | `docs/adrs/002-use-react-vite-typescript-served-by-spring-boot.md` | Decision, Consequences | Accepted | Confirms React/Vite/TypeScript frontend and REST boundary. |
| ADR-008 | `docs/adrs/008-define-public-rest-api-contract.md` | Decision, Consequences | Accepted | Confirms conversion and URL download are distinct public operation concepts. |
| Task 001 execution report | `docs/task-executions/mvp-media-utility/001-scaffold-spring-boot-react-monolith-execution.md` | Implemented Changes | Confirmed by source document | Confirms frontend scaffold and test setup. |
| Task 008 execution report | `docs/task-executions/mvp-media-utility/008-define-rest-api-contracts-execution.md` | Execution Summary, Implemented Changes | Confirmed by source document | Confirms API contract surface exists, but task 009 remains frontend-only and must not integrate with it. |
| Current codebase | `frontend/src/App.tsx`, `frontend/src/App.css`, `frontend/src/App.test.tsx`, `frontend/package.json` | Frontend app and tests | Detected in codebase | Confirms scaffold-only UI, CSS, current render test, and existing test dependencies. |
| User decision | Current `plan-task` session | Source gap handling | Confirmed by user | Proceed despite empty Tech Spec, PRD, and technology-definition files using available binding sources. |
| User decision | Current `plan-task` session | Default selected operation | Confirmed by user | Default selector to MP4-to-MP3 conversion. |
| User decision | Current `plan-task` session | Shared state surface style | Confirmed by user | Use a static non-interactive status/result panel. |

## Confirmed Architecture Decisions

| Decision | Source | Impact on This Task | Notes |
| --- | --- | --- | --- |
| Frontend is React with Vite and TypeScript. | ADR-002, task 001 execution | Implement selector in the existing `frontend/` app. | Accepted ADR constraint. |
| Frontend/backend communication is through REST. | ADR-002, ADR-008 | This task may align UI concepts with REST operations but must not call APIs. | Accepted ADR constraint. |
| Conversion and URL download are distinct operation concepts. | ADR-008, project planning | Selector must present MP4-to-MP3 conversion and public URL download as separate options. | Accepted ADR constraint. |
| MVP uses one anonymous flow with no separate service pages. | Project planning, task file | Selector should switch panels in one screen without routing or account features. | Confirmed planning constraint. |
| Conversion is the default selected operation. | User decision | Initial React state and tests must expect conversion first. | Confirmed task-level decision. |
| Shared operation states are static placeholders in this task. | User decision | Implement a non-interactive status/result panel instead of fake state controls or API-driven behavior. | Confirmed task-level decision. |
| Empty Tech Spec, PRD, and technology-definition files do not block task 009. | User decision | Plan is ready using ADRs, planning, task file, executions, and codebase evidence as binding sources. | Resolved source gap. |

## Pending Architecture Decisions

None. All task-relevant architecture decisions have been answered or explicitly deferred out of scope by the user.

## Architecture Conflicts

| Conflict | Sources | Impact | Resolution Status |
| --- | --- | --- | --- |
| Tech Spec, PRD, and technology-definition files are empty, but task 009 needs concrete frontend planning. | Empty source files vs task planning requirements | Could block a ready implementation plan if treated as required source coverage. | Resolved by user decision to proceed with ADRs, project planning, task file, task executions, and codebase evidence. |
| Task 008 API contract exists, but task 009 must not implement API integration. | ADR-008, task 008 execution, task 009 out-of-scope | Implementer could accidentally wire API calls too early. | Resolved by explicit frontend-only scope and no API-call acceptance criterion. |
| Shared status/result states are required, but real operation submission is out of scope. | Task 009 acceptance criteria vs out-of-scope API behavior | Fake controls could blur task boundaries. | Resolved by user decision to use a static non-interactive status/result panel. |

## ADR Candidates

| Candidate | Trigger | Suggested ADR Scope | Blocking for This Task? |
| --- | --- | --- | --- |
| None | No new architecture-level decision is introduced by this selector task. | Not applicable. | No |

## Implementation Impact

- Task 009 can proceed without a new formal ADR.
- The implementation must stay inside the React frontend unless a small frontend-only helper file is useful.
- Conversion must be the initial selected operation.
- Selector UI must represent conversion and URL download as distinct operations aligned with ADR-008.
- The shared state surface must be static and non-interactive.
- The implementation must not call or mock backend APIs, create form submission behavior, or implement later task scopes.
- Empty Tech Spec, PRD, and technology-definition files remain documented limitations but are not blockers for this task.

## Questions for the User

None. All task-relevant architecture questions have been answered.

## Notes for the Implementing Agent

- This file is planning support only; it is not a final ADR.
- Use ADR-002 for frontend stack constraints and ADR-008 for operation naming/integration context.
- Do not treat the existing API contract as permission to call backend endpoints in task 009.
- Keep upload form behavior for task 010 and URL form behavior for task 011.
- Preserve the single anonymous MVP flow with no routing or account features.
