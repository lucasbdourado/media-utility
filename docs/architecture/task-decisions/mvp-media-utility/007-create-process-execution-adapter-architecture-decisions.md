# Task Architecture Decision Notes: Create Process Execution Adapter

## Status

Status: Ready for Implementation

Last updated: 2026-06-02

Decision notes file: `docs/architecture/task-decisions/mvp-media-utility/007-create-process-execution-adapter-architecture-decisions.md`

Task plan file: `docs/task-plans/mvp-media-utility/007-create-process-execution-adapter-plan.md`

Task file: `docs/tasks/mvp-media-utility/007-create-process-execution-adapter.md`

## Purpose

Record task-specific architecture decisions, conflicts, and confirmed ADR candidates identified while preparing the task implementation plan.

This file is planning support only. It is not a final ADR and must not replace the ADR workflow.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/tasks/mvp-media-utility/007-create-process-execution-adapter.md` | Status, Scope, Dependencies, Open Questions | Confirmed by source document | Defines the process adapter task and original process contract questions. |
| Project planning | `docs/planning/project-planning.md` | Media processing, technology dependencies, risks | Confirmed by source document | Confirms media processing is required and technology/process details must be resolved before implementation. |
| Task 002 | `docs/tasks/mvp-media-utility/002-create-backend-module-boundaries.md` | Context, Implementation Instructions | Confirmed by source document | Confirms external process execution must be isolated from request handlers. |
| Task plan 002 | `docs/task-plans/mvp-media-utility/002-create-backend-module-boundaries-plan.md` | Confirmed Scope, Out of Scope | Confirmed by source document | Confirms `media.process` was created as a boundary only. |
| Task 002 architecture notes | `docs/architecture/task-decisions/mvp-media-utility/002-create-backend-module-boundaries-architecture-decisions.md` | Confirmed Architecture Decisions, Implementation Impact | Confirmed by source document | Confirms complete contracts and adapter behavior were intentionally deferred from task 002. |
| ADR-001 | `docs/adrs/001-use-java-and-spring-boot-modular-monolith.md` | Decision, Consequences | Accepted | Confirms the adapter belongs inside the single Spring Boot backend. |
| ADR-002 | `docs/adrs/002-use-react-vite-typescript-served-by-spring-boot.md` | Decision, Consequences | Accepted | Confirms REST is the frontend/backend boundary; process execution remains backend-internal. |
| ADR-007 | `docs/adrs/007-define-process-execution-contract-for-media-tools.md` | Decision, Consequences, Task Impact | Accepted / Resolved by ADR | Resolves the task 007 process execution contract blocker. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/media/process/` | Process execution boundary | Detected in codebase | Existing files are boundary markers only. |
| Tech Spec | `docs/specs/tech-spec.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |
| Technology definition | `docs/architecture/technology-definition.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |
| User decision | Current `resolve-architecture-blocker` session | Process execution contract | Confirmed by user | Accepted executable-plus-args, configurable timeout, bounded separate output capture, failed-result semantics, required working directory, inherited environment with overrides, and final-only output capture. |

## Confirmed Architecture Decisions

| Decision | Source | Impact on This Task | Notes |
| --- | --- | --- | --- |
| Backend remains one modular monolith. | ADR-001 | Process execution adapter should be implemented inside the existing Spring Boot app, not as a separate service. | Accepted ADR. |
| Process execution belongs under `media.process`. | Task 002, task plan 002, current codebase | Future process execution contract and implementation should live under the existing boundary. | Confirmed boundary. |
| API handlers must not execute external processes directly. | Task 002, task 007 | Future implementation must keep process launching out of API classes. | Confirmed task constraint. |
| FFmpeg and yt-dlp behavior are not part of task 007. | Task 007 | Future implementation must provide shared process execution only, not media-tool-specific adapters. | Confirmed out-of-scope boundary. |
| Process requests use executable plus argument list. | ADR-007 | Implementation must not accept shell command strings. | Resolved by ADR. |
| Timeout uses configurable default plus per-call override. | ADR-007 | Implementation must add timeout configuration and request override support. | Resolved by ADR. |
| Stdout and stderr are captured separately with limits. | ADR-007 | Implementation must preserve stream separation and bound captured output. | Resolved by ADR. |
| Non-zero exit and timeout return failed results. | ADR-007 | Implementation must return result objects for ordinary process failures. | Resolved by ADR. |
| Working directory is required per request. | ADR-007 | Implementation must reject requests without working directory. | Resolved by ADR. |
| Environment is inherited with per-call overrides. | ADR-007 | Implementation must apply overrides after inheriting application environment. | Resolved by ADR. |
| Output streaming is deferred out of task 007. | ADR-007 | Implementation must not add streaming callbacks in this task. | Resolved by ADR. |

## Pending Architecture Decisions

None. All task-relevant architecture decisions have been answered, resolved by ADR-007, or explicitly deferred out of scope by the user.

## Architecture Conflicts

| Conflict | Sources | Impact | Resolution Status |
| --- | --- | --- | --- |
| Task 007 needs a concrete process execution adapter, but the Tech Spec and technology definition are empty. | Task 007 vs empty source documents | Implementation would require inventing interface shape, timeout, output, and failure behavior. | Resolved for task 007 by ADR-007. |
| Task 002 intentionally deferred complete contracts, while task 007 now needs one for process execution. | Task plan 002 and task 007 | The deferred process contract must now be explicitly resolved before implementation. | Resolved by ADR-007. |

## ADR Candidates

| Candidate | Trigger | Suggested ADR Scope | Blocking for This Task? |
| --- | --- | --- | --- |
| Process execution contract for media tools | Task 007 must define a shared contract used by FFmpeg and yt-dlp adapters. | Covered by ADR-007. | No; resolved by accepted ADR. |

## Implementation Impact

- Task 007 can proceed after this updated ready task plan is saved.
- ADR-007 binds implementation to executable plus argument list, not shell command strings.
- The process executor must be shared, backend-internal, and tool-agnostic.
- The implementation must include configurable timeout and output capture limits.
- Non-zero exit, timeout, and interruption should be represented as failed results rather than ordinary exceptions.
- Streaming output support remains out of scope for task 007.

## Questions for the User

None. All task-relevant architecture questions have been answered.

## Notes for the Implementing Agent

- This file is planning support only; it is not a final ADR.
- Use ADR-007 as the binding process execution contract source.
- Keep process execution shared and tool-agnostic; FFmpeg and yt-dlp adapters belong to later tasks.
- Do not add streaming output support unless a future ADR or task explicitly changes that decision.
