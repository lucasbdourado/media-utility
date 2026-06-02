# Task: Create Process Execution Adapter

## Status

Status: Blocked

Last updated: 2026-06-02

## Task ID

ID: MVP-MEDIA-007

Order: 007

Task file: `docs/tasks/mvp-media-utility/007-create-process-execution-adapter.md`

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project planning | `docs/planning/project-planning.md` | Media processing, technology dependencies, risks | Confirmed by source document | Confirms media processing depends on technology definition and must support conversion/download flows. |
| Task 002 | `docs/tasks/mvp-media-utility/002-create-backend-module-boundaries.md` | Context, Scope, Implementation Instructions | Confirmed by source document | Confirms request handlers must not call FFmpeg or yt-dlp directly and external process execution must be isolated. |
| Task plan 002 | `docs/task-plans/mvp-media-utility/002-create-backend-module-boundaries-plan.md` | Confirmed Scope, Out of Scope, Notes for Implementing Agent | Confirmed by source document | Confirms `media.process` exists only as a boundary and later process adapter behavior is out of task 002 scope. |
| ADR-001 | `docs/adrs/001-use-java-and-spring-boot-modular-monolith.md` | Decision, Consequences | Accepted | Confirms Java 21 Spring Boot modular monolith with internal module boundaries. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/media/process/` | Process execution boundary | Detected in codebase | `ProcessExecutionBoundary` and package documentation exist, but no process execution adapter behavior exists yet. |
| Tech Spec | `docs/specs/tech-spec.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |
| Technology definition | `docs/architecture/technology-definition.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |

## Context

The MVP will eventually use external media tools such as FFmpeg and yt-dlp, but request handlers and media-specific adapters must not execute external processes directly. The current backend already contains a `media.process` boundary for isolating process execution behavior.

This task should create the shared process execution adapter used by later FFmpeg conversion and yt-dlp download tasks. However, implementation is blocked because the process execution contract is not documented in the available Tech Spec or technology definition.

## Goal

Create a backend process execution adapter contract and implementation that safely runs external media-tool commands for later media adapters.

## Scope

- Define the process execution contract under the existing `media.process` package.
- Implement a local JVM process execution adapter only after the process contract is confirmed.
- Capture command exit status and output according to a confirmed contract.
- Provide timeout behavior according to a confirmed contract.
- Keep process execution isolated from API handlers, operation orchestration, conversion adapters, and download adapters.
- Add focused tests for successful execution, failed execution, captured output, timeout behavior, and invalid command inputs after the contract is confirmed.

## Out of Scope

- Do not implement FFmpeg conversion behavior.
- Do not implement yt-dlp download behavior.
- Do not implement REST endpoints or public API DTOs.
- Do not implement operation orchestration.
- Do not implement temporary storage behavior.
- Do not choose command timeout values without a confirmed architecture decision.
- Do not choose command environment, working-directory, output-size, or failure-mapping rules without a confirmed architecture decision.

## Implementation Instructions

- Do not implement this task until the process execution contract is confirmed in an architecture decision or task implementation plan.
- Keep all process execution behavior under `src/main/java/com/lucasdourado/mediautility/media/process/`.
- Preserve the `media.process` boundary so future FFmpeg and yt-dlp adapters can depend on it without duplicating process-launching logic.
- Do not let API handlers call Java `ProcessBuilder`, FFmpeg, yt-dlp, or shell commands directly.
- Do not use shell-string command concatenation for user-controlled input.
- Document any confirmed process failure modes in task planning before implementation.

## Expected Files

| Path | Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasdourado/mediautility/media/process/` | Create / Modify | Task 002, current codebase | Process execution contract, result type, exception type, and local adapter after decisions are confirmed. |
| `src/test/java/com/lucasdourado/mediautility/media/process/` | Create | Validation need | Focused tests for execution behavior after decisions are confirmed. |
| `docs/architecture/task-decisions/mvp-media-utility/007-create-process-execution-adapter-architecture-decisions.md` | Create / Update | Required blocker resolution | Should record process contract decisions before implementation. |

## Dependencies

| Dependency | Type | Status | Notes |
| --- | --- | --- | --- |
| MVP-MEDIA-001 | Previous task | Completed | Spring Boot monolith exists. |
| MVP-MEDIA-002 | Previous task | Completed | `media.process` boundary exists. |
| Process execution contract | Architecture decision | Blocked | Timeout, output capture, exit-code semantics, working directory, environment handling, and failure mapping are not confirmed. |
| Tech Spec process details | Documentation | Missing | `docs/specs/tech-spec.md` is empty in the current workspace. |
| Technology definition process details | Documentation | Missing | `docs/architecture/technology-definition.md` is empty in the current workspace. |

## Validation

- Backend compiles after implementation.
- Unit tests verify a successful process execution path.
- Unit tests verify non-zero exit status handling.
- Unit tests verify stdout/stderr capture according to the confirmed contract.
- Unit tests verify timeout behavior according to the confirmed contract.
- Unit tests verify invalid or unsafe command inputs are rejected.
- Scope review verifies no FFmpeg conversion, yt-dlp download, REST endpoint, operation orchestration, or storage behavior was added.

## Acceptance Criteria

- [ ] A process execution contract exists under the `media.process` package.
- [ ] A local JVM process execution adapter exists under the `media.process` package.
- [ ] Process execution results expose only the fields confirmed by the architecture decision.
- [ ] Timeout behavior follows the confirmed process contract.
- [ ] Command output capture follows the confirmed process contract.
- [ ] Failure handling follows the confirmed process contract.
- [ ] API handlers do not execute external processes directly.
- [ ] No FFmpeg conversion adapter or yt-dlp download adapter is implemented in this task.
- [ ] Focused process execution tests cover success, failure, output capture, timeout, and invalid inputs.

## Risks

- A process adapter without confirmed timeout and output-size rules can hang requests or consume unbounded memory.
- Shell-string execution could create command injection risk if later adapters pass user-controlled values.
- Incorrect failure mapping could make later conversion and download tasks hide operational errors.
- Implementing FFmpeg or yt-dlp behavior here would blur boundaries and duplicate future adapter work.
- Empty Tech Spec and technology-definition files reduce confidence until the architecture blocker is resolved.

## Open Questions

- What process execution request model should be used: executable plus argument list, command object, or another shape?
- What timeout should apply by default, and should callers override it?
- How should stdout and stderr be captured, limited, and exposed?
- Should non-zero exit codes throw an exception or return a failed result object?
- What working directory should external commands use?
- Which environment variables should be inherited, overridden, or blocked?
- Should the adapter support streaming output, or only collect final output?
- How should interrupted execution and process termination be handled?

## Notes for the Implementing Agent

- Do not execute this task directly while it is blocked.
- Run `resolve-architecture-blocker` for this task before `plan-task` or `execute-task`.
- Treat the current `ProcessExecutionBoundary` as a package marker only.
- Keep FFmpeg-specific behavior for MVP-MEDIA-013 and yt-dlp-specific behavior for MVP-MEDIA-016.
