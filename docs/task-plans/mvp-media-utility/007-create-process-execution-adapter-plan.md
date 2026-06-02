# Task Implementation Plan: Create Process Execution Adapter

## Status

Status: Ready for Implementation

Last updated: 2026-06-02

Plan file: `docs/task-plans/mvp-media-utility/007-create-process-execution-adapter-plan.md`

Architecture decision notes file: `docs/architecture/task-decisions/mvp-media-utility/007-create-process-execution-adapter-architecture-decisions.md`

## Task Reference

Task ID: `MVP-MEDIA-007`

Task file: `docs/tasks/mvp-media-utility/007-create-process-execution-adapter.md`

Task status: `Blocked`

Task group or feature: `mvp-media-utility`

## Planning Mode Requirement

Plan mode verified: Yes

Notes:

- This plan was created in plan mode and updated through `resolve-architecture-blocker`.
- Implementation must not start during planning or blocker resolution.
- ADR-007 resolves the required process execution contract blocker.
- A future implementation request must use this saved plan, the saved architecture decision notes, and ADR-007 as source context.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project discovery | `docs/context/project-discover.md` | Project Scenario | Confirmed by source document | Required Harness discovery context exists, but it predates the current implementation codebase. |
| Project planning | `docs/planning/project-planning.md` | Media processing, technology dependencies, risks | Confirmed by source document | Confirms media processing depends on technology definition and must support conversion/download flows. |
| Task file | `docs/tasks/mvp-media-utility/007-create-process-execution-adapter.md` | Scope, Dependencies, Validation, Acceptance Criteria | Confirmed by source document | Defines process adapter scope and original open questions. |
| Task 002 | `docs/tasks/mvp-media-utility/002-create-backend-module-boundaries.md` | Context, Scope, Implementation Instructions | Confirmed by source document | Confirms request handlers must not call FFmpeg or yt-dlp directly and process execution must be isolated. |
| Task plan 002 | `docs/task-plans/mvp-media-utility/002-create-backend-module-boundaries-plan.md` | Confirmed Scope, Out of Scope, Notes for Implementing Agent | Confirmed by source document | Confirms `media.process` exists only as a boundary and process adapter behavior is a later task. |
| Task 002 architecture notes | `docs/architecture/task-decisions/mvp-media-utility/002-create-backend-module-boundaries-architecture-decisions.md` | Confirmed Architecture Decisions, Implementation Impact | Confirmed by source document | Confirms complete contracts and adapter behavior were intentionally left out of task 002. |
| ADR-001 | `docs/adrs/001-use-java-and-spring-boot-modular-monolith.md` | Decision, Consequences | Accepted | Confirms Java 21 Spring Boot modular monolith with internal module boundaries. |
| ADR-002 | `docs/adrs/002-use-react-vite-typescript-served-by-spring-boot.md` | Decision, Consequences | Accepted | Confirms REST remains the frontend/backend boundary, but does not define process execution behavior. |
| ADR-007 | `docs/adrs/007-define-process-execution-contract-for-media-tools.md` | Decision, Consequences, Task Impact | Accepted / Resolved by ADR | Defines the process execution contract and resolves the task 007 blocker. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/media/process/` | Process execution boundary | Detected in codebase | `ProcessExecutionBoundary` and package documentation exist, but no process execution adapter behavior exists yet. |
| Tech Spec | `docs/specs/tech-spec.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |
| Technology definition | `docs/architecture/technology-definition.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |
| User decision | Current `resolve-architecture-blocker` session | Process execution contract | Confirmed by user | Use executable plus argument list, configurable timeout, bounded separate output capture, failed-result semantics, required working directory, inherited environment with overrides, and final-only output. |

## Context Summary

The backend is a Java 21 Spring Boot modular monolith with an existing `media.process` boundary. Later FFmpeg conversion and yt-dlp download adapters need shared process execution behavior.

Task 007 creates the shared, tool-agnostic process execution contract and local JVM adapter under `media.process`. ADR-007 resolves the original blocker by accepting a safe executable-plus-arguments contract with bounded output capture, configurable timeout, explicit working directory, and failed-result semantics.

## Task Goal

Create a shared backend process execution contract and local JVM adapter for external media-tool commands, without implementing FFmpeg-specific or yt-dlp-specific behavior.

## Confirmed Scope

- Keep process execution behavior under the existing `media.process` package.
- Define a request model using executable plus argument list.
- Require a working directory per execution request.
- Support optional timeout override per request.
- Support optional environment variable overrides per request.
- Use a configurable default timeout property: `media-utility.process.default-timeout`.
- Use an initial default timeout of 5 minutes.
- Capture stdout and stderr separately.
- Limit each captured stream with a configurable capture limit.
- Use an initial default capture limit of 64 KiB per stream.
- Return result objects for success, non-zero exit, timeout, and interruption.
- Include at least exit code, stdout, stderr, timed-out flag, and duration in results.
- Attempt to terminate timed-out or interrupted processes and force termination if needed.
- Add focused tests for success, non-zero exit, output capture, timeout behavior, and invalid inputs.

## Out of Scope

- Do not implement FFmpeg conversion behavior.
- Do not implement yt-dlp download behavior.
- Do not implement REST endpoints or public API DTOs.
- Do not implement operation orchestration.
- Do not implement temporary storage behavior.
- Do not implement progress streaming callbacks in task 007.
- Do not use shell command strings.
- Do not add tool-specific command construction for FFmpeg or yt-dlp.

## Requirements Covered

| Requirement | Source | How This Task Covers It | Status |
| --- | --- | --- | --- |
| Keep external process execution isolated | Task 002, task plan 002, current codebase | Implements shared process execution behavior under `media.process`. | Ready |
| Support later FFmpeg and yt-dlp adapters | Task 007, package documentation, ADR-007 | Provides a shared tool-agnostic executor for later media adapters. | Ready |
| Avoid direct process execution in API handlers | Task 002, task 007, ADR-007 | Keeps process launching behind the `media.process` adapter. | Ready |
| Avoid unsafe shell-string execution | Task 007, ADR-007 | Uses executable plus argument list, not command strings. | Ready |
| Prevent hanging processes | Task 007, ADR-007 | Uses configurable default timeout with per-call override and process termination. | Ready |
| Prevent unbounded output memory use | Task 007, ADR-007 | Captures stdout/stderr separately with configurable limits. | Ready |

## Technical Specification Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Not available | Missing | Task cannot rely on Tech Spec content because `docs/specs/tech-spec.md` is empty. | Source limitation remains documented. ADR-007 resolves task 007 contract details. |

Coverage assessment:

- Justifying Tech Spec section: unavailable because the Tech Spec file is empty.
- Tech Spec sections implemented by this task: none directly from Tech Spec content.
- Gaps between task and Tech Spec: process request model, timeout, output capture, failure semantics, environment handling, working directory, and termination behavior were not documented there.
- Dependencies not specified by the Tech Spec: resolved by ADR-007 and user decisions in the unblock session.

## Architecture and ADR Considerations

| Decision or ADR | Source | Impact on This Task | Status |
| --- | --- | --- | --- |
| Java 21 Spring Boot modular monolith | ADR-001 | Process adapter lives inside the single backend app under the existing internal boundary. | Accepted |
| REST frontend/backend boundary | ADR-002 | Process execution remains backend-internal and is not a public REST contract. | Accepted |
| `media.process` boundary | Task 002, task plan 002, current codebase | Existing package boundary is the correct location for process execution behavior. | Confirmed |
| Process execution contract for media tools | ADR-007 | Defines request/result shape, timeout, output capture, failure semantics, working directory, environment, and termination behavior. | Resolved by ADR |
| No shell-string execution | ADR-007 | Implementation must use executable plus argument list. | Resolved by ADR |
| Final-only output collection | ADR-007 | Task 007 does not implement streaming callbacks. | Resolved by ADR |

ADR candidates or architecture decisions needed:

- None. ADR-007 resolves the required process execution contract blocker.
- No architecture blocker remains for task 007.

Architecture decision notes:

- Saved separately: Yes
- Path: `docs/architecture/task-decisions/mvp-media-utility/007-create-process-execution-adapter-architecture-decisions.md`
- Notes file status: Updated

## Confirmed Decisions

- The selected task is `MVP-MEDIA-007`.
- The existing process boundary package is `src/main/java/com/lucasdourado/mediautility/media/process/`.
- FFmpeg and yt-dlp behavior belong to later dedicated tasks.
- API handlers must not execute external processes directly.
- The process request model is executable plus argument list.
- Shell command strings are rejected.
- Each request includes working directory, optional timeout override, and optional environment overrides.
- Default timeout uses `media-utility.process.default-timeout`.
- Initial default timeout is 5 minutes.
- Stdout and stderr are captured separately.
- Output capture limit is configurable.
- Initial default capture limit is 64 KiB per stream.
- Non-zero exit code returns a failed result.
- Timeout or interruption returns a failed result after attempting process termination.
- Output streaming is deferred out of task 007.
- ADR-007 accepts the process execution contract and resolves the task 007 architecture blocker.

## Pending Decisions

None. All task-relevant decisions have been answered, resolved by ADR-007, or explicitly deferred out of this task.

## Questions for the User

None. All task-relevant questions have been answered.

## Proposed Implementation Approach

1. Re-read the task file, this plan, architecture decision notes, ADR-007, and current `media.process` code.
2. Define small process request and result types under `media.process`.
3. Define a process execution service contract under `media.process`.
4. Implement a local JVM process executor using Java process APIs without shell command strings.
5. Add configuration for `media-utility.process.default-timeout` with a 5 minute default.
6. Add configuration for per-stream output capture limit with a 64 KiB default.
7. Enforce required working directory and reject invalid requests before launching a process.
8. Capture stdout and stderr separately up to the configured limit.
9. Return result objects for successful exit, non-zero exit, timeout, and interruption.
10. On timeout or interruption, attempt process termination and force termination if needed.
11. Add focused unit tests using harmless local commands available in the test environment or test doubles where platform commands are unreliable.
12. Perform a scope review confirming no FFmpeg, yt-dlp, REST endpoint, operation orchestration, or storage behavior was added.

## Files and Areas Expected to Change

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasdourado/mediautility/media/process/` | Create / Modify | Task 007, ADR-007 | Process request/result types, executor contract, local JVM adapter, exception type if needed, and package documentation update. |
| `src/main/resources/application.properties` | Modify | ADR-007 | Add process timeout and output capture configuration defaults. |
| `src/test/java/com/lucasdourado/mediautility/media/process/` | Create | Task 007, ADR-007 | Focused unit tests for execution behavior. |

## Step-by-Step Implementation Plan

1. Verify working tree state before editing and preserve unrelated changes.
2. Inspect the existing `media.process` package.
3. Add request/result value types for executable, arguments, working directory, timeout override, environment overrides, exit code, stdout, stderr, timed-out flag, and duration.
4. Add a process executor interface extending or aligned with `ProcessExecutionBoundary`.
5. Add configuration properties for:
   - `media-utility.process.default-timeout=5m`
   - a per-stream output capture limit defaulting to 64 KiB.
6. Implement the local JVM process executor:
   - build the process from executable plus argument list;
   - set working directory from the request;
   - inherit environment and apply overrides;
   - read stdout and stderr separately with limits;
   - wait with timeout;
   - terminate and force terminate timed-out/interrupted processes;
   - return result objects rather than throwing for ordinary process failures.
7. Throw or reject before execution only for invalid requests or setup errors that prevent launching safely.
8. Add tests for success, non-zero exit, stdout/stderr capture, output limits, timeout termination, required working directory, and invalid request rejection.
9. Run the relevant Maven test command available in the repository environment.
10. Review the diff and scope-search for out-of-scope FFmpeg, yt-dlp, REST endpoint, operation orchestration, and storage behavior.

## Validation Strategy

- Run the repository's established Maven test command.
- Verify backend compilation.
- Verify focused process tests pass.
- Verify process execution does not use shell command strings.
- Verify timeout and output limits are covered by tests.
- Verify no FFmpeg adapter, yt-dlp adapter, REST endpoint, operation orchestration, or storage behavior was added.

## Tests to Add or Update

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| Successful process execution | Unit | Verify the adapter can run an allowed command and capture result data. | Use platform-stable commands or test doubles if needed. |
| Non-zero exit handling | Unit | Verify non-zero exit returns failed result with exit code and output. | Do not throw for ordinary non-zero exit. |
| Output capture | Unit | Verify stdout/stderr are separate and bounded. | Cover the 64 KiB default or configured test limit. |
| Timeout handling | Unit | Verify timeout returns failed timed-out result and terminates process. | Keep test duration short with overridden timeout. |
| Invalid command input | Unit | Verify unsafe or malformed process requests are rejected before launch. | Include missing executable and missing working directory. |
| Configuration defaults | Unit / slice | Verify default timeout and capture limit load as expected. | Use focused configuration test if local pattern supports it. |
| Scope review | Manual/static | Verify no out-of-scope media adapter or API behavior was added. | Required after implementation. |

## Acceptance Criteria

- [x] Required process execution contract architecture decision or ADR is accepted and saved.
- [x] Blocked task plan is updated after the architecture decision is accepted.
- [ ] A process execution contract exists under the `media.process` package.
- [ ] A local JVM process execution adapter exists under the `media.process` package.
- [ ] Process execution results expose only the fields confirmed by ADR-007.
- [ ] Timeout behavior follows ADR-007.
- [ ] Command output capture follows ADR-007.
- [ ] Failure handling follows ADR-007.
- [ ] API handlers do not execute external processes directly.
- [ ] No FFmpeg conversion adapter or yt-dlp download adapter is implemented in this task.
- [ ] Focused process execution tests cover success, failure, output capture, timeout, and invalid inputs.

## Risks and Edge Cases

- Process tests can be platform-sensitive on Windows; prefer stable commands or isolate process-launching behavior carefully.
- Incorrect stream handling can deadlock if stdout/stderr are not consumed while the process runs.
- Output truncation must not allocate unbounded memory before limiting content.
- Timeout termination must avoid leaving orphan processes.
- Environment inheritance may expose runtime differences; adapters should use overrides only when needed.
- Future FFmpeg and yt-dlp adapters may need richer telemetry or streaming; ADR-007 defers that out of task 007.

## Rollback or Recovery Notes

Rollback would remove process request/result types, executor contract, local JVM executor, process configuration defaults, and focused process tests while preserving the existing `media.process` boundary and ADR-007.

If implementation discovers that final-only output capture cannot satisfy later adapter needs, record a follow-up architecture issue instead of silently adding streaming behavior.

## Documentation Updates

- Do not update PRD, project planning, technology definition, Tech Spec, final ADRs, task files, or unrelated documents during task implementation.
- Package documentation under `media.process` may be updated to reflect implemented process execution behavior.
- The task execution report should document implemented contract shape, configuration properties, validation evidence, and any deviations.

## Implementation Readiness Checklist

- [x] Task scope is clear.
- [x] Source documents were reviewed.
- [x] Tech Spec coverage was verified.
- [x] Architecture decisions were checked.
- [x] ADR candidates were confirmed, rejected, required-before-implementation, or explicitly deferred by the user.
- [x] Pending decisions are resolved or explicitly deferred out of this task by the user.
- [x] User questions were answered.
- [x] Expected files or areas are identified.
- [x] Acceptance criteria are defined.
- [x] Test strategy is defined.
- [x] Risks and edge cases are documented.

## Notes for the Implementing Agent

- ADR-007 is the binding source for task 007 implementation.
- Do not use shell command strings.
- Keep process execution shared and tool-agnostic.
- Keep FFmpeg-specific behavior for MVP-MEDIA-013.
- Keep yt-dlp-specific behavior for MVP-MEDIA-016.
- Do not add streaming output support in task 007.
