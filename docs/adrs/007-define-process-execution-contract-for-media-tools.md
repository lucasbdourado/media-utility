# ADR-007: Define Process Execution Contract for Media Tools

## Status

Status: Accepted

Date: 2026-06-02

## Context

MVP-MEDIA-007 is blocked because the process execution adapter defines a shared backend contract that later FFmpeg conversion and yt-dlp download adapters will reuse. The existing backend is a Java 21 Spring Boot modular monolith with a `media.process` boundary created by MVP-MEDIA-002. That boundary intentionally contains no behavior yet.

The task file and blocked task plan require process execution to remain isolated from API handlers, operation orchestration, conversion adapters, and download adapters. The available Tech Spec and technology definition files are empty in the current workspace, so the process execution contract must be explicitly accepted before implementation.

The contract must avoid shell-string execution, prevent unbounded process runtime, avoid unbounded output capture, and provide predictable failure semantics for media-tool adapters.

## Decision

The MVP process execution adapter will expose a safe JVM process execution contract based on an executable plus a list of arguments, not a shell command string.

Each process request must include:

- executable name or path;
- argument list;
- working directory;
- optional timeout override;
- optional environment variable overrides.

The executor will use a configurable default timeout of 5 minutes through `media-utility.process.default-timeout`, while allowing callers to provide a per-call timeout override.

The executor will capture stdout and stderr separately. Each stream will have a configurable capture limit, with an initial default of 64 KiB per stream. Output capture is final-only in task 007; streaming callbacks are out of scope for the MVP process adapter contract.

Process execution will return a result object rather than throwing for ordinary process failures. The result must include at least:

- exit code;
- captured stdout;
- captured stderr;
- whether the process timed out;
- duration.

A non-zero exit code must produce a failed result. Timeout or interruption must attempt to terminate the process, force termination if needed, and return a failed result with timeout indicated.

Each request must provide a working directory. The executor must not default to the application process working directory.

The executor will inherit the application process environment and allow per-call environment overrides. Tool-specific environment policy remains the responsibility of later FFmpeg and yt-dlp adapter tasks when needed.

## Considered Options

| Option | Summary | Trade-offs | Decision |
| --- | --- | --- | --- |
| Executable plus argument list | Callers provide executable and arguments separately. | Reduces command injection and quoting risk while keeping the contract small. | Accepted |
| Rich command object from the start | Model a larger command abstraction. | More structure, but larger than task 007 needs. | Rejected for MVP |
| Shell command string | Callers provide a full command string. | Flexible, but increases injection and quoting risk. | Rejected |
| Configurable default timeout with override | Default timeout is configured and callers may override it. | Balances safe defaults with adapter-specific needs. | Accepted |
| Fixed timeout in code | One hardcoded timeout. | Simple, but less operationally adjustable. | Rejected |
| Caller-required timeout only | Every call supplies a timeout. | Explicit, but spreads default policy across adapters. | Rejected |
| Separate limited stdout/stderr capture | Capture stdout and stderr separately with limits. | Preserves diagnostics while bounding memory use. | Accepted |
| Unified limited output | Merge stdout and stderr. | Simpler, but loses diagnostic separation. | Rejected |
| No output capture | Do not capture output. | Lower memory, but poor failure diagnostics. | Rejected |
| Failed result for non-zero exit | Return process result with failure data. | Lets media adapters map tool failures explicitly. | Accepted |
| Exception for non-zero exit | Throw on process failure. | Simple, but treats expected tool failures as exceptional control flow. | Rejected |
| Final-only output collection | Return captured output only after process completion. | Smaller and easier to test for MVP. | Accepted |
| Streaming callbacks | Provide output callbacks during execution. | Useful for progress, but increases scope and complexity. | Deferred |

## Consequences

- API handlers and media-specific adapters must not execute shell command strings directly.
- The process adapter must use Java process execution with executable and argument list separation.
- Later FFmpeg and yt-dlp adapters can use one shared result model for success, failure, and timeout.
- The implementation must enforce timeout behavior and terminate timed-out processes.
- Output capture must be bounded to avoid unbounded memory use.
- Task 007 does not need to implement progress streaming.
- Tool-specific command construction and media semantics remain out of scope for task 007.
- Future tasks may revisit this ADR if streaming progress or richer process telemetry becomes necessary.

## Task Impact

- Related task plan: `docs/task-plans/mvp-media-utility/007-create-process-execution-adapter-plan.md`
- Related architecture decision notes: `docs/architecture/task-decisions/mvp-media-utility/007-create-process-execution-adapter-architecture-decisions.md`
- Unlock effect: Resolves the process execution contract blocker for MVP-MEDIA-007.
- Remaining blockers: None for MVP-MEDIA-007 after the task plan and architecture decision notes are updated to reference this ADR.

## Source References

- `docs/tasks/mvp-media-utility/007-create-process-execution-adapter.md`: blocked task scope, dependencies, open questions, and validation expectations.
- `docs/task-plans/mvp-media-utility/007-create-process-execution-adapter-plan.md`: blocked task implementation plan.
- `docs/architecture/task-decisions/mvp-media-utility/007-create-process-execution-adapter-architecture-decisions.md`: blocked task architecture notes.
- `docs/tasks/mvp-media-utility/002-create-backend-module-boundaries.md`: confirms process execution must be isolated from request handlers.
- `docs/task-plans/mvp-media-utility/002-create-backend-module-boundaries-plan.md`: confirms `media.process` exists as a later process adapter boundary.
- `docs/adrs/001-use-java-and-spring-boot-modular-monolith.md`: confirms Java 21 Spring Boot modular monolith.
- User confirmation: current `resolve-architecture-blocker` session accepted the safe executable-plus-args process contract, configurable timeout, bounded separate output capture, failed-result semantics, required working directory, inherited environment with overrides, final-only output capture, and `Ready for Implementation` unblock status.
