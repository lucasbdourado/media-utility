# Task Architecture Decision Notes: Implement Conversion Operation Endpoint

## Status

Status: Ready for Implementation

Last updated: 2026-06-03

Decision notes file: `docs/architecture/task-decisions/mvp-media-utility/014-implement-conversion-operation-endpoint-architecture-decisions.md`

Task plan file: `docs/task-plans/mvp-media-utility/014-implement-conversion-operation-endpoint-plan.md`

Task file: `docs/tasks/mvp-media-utility/014-implement-conversion-operation-endpoint.md`

## Purpose

Record task-specific architecture decisions, conflicts, and confirmed ADR candidates identified while preparing the task implementation plan.

This file is planning support only. It is not a final ADR and must not replace the ADR workflow.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| ADR-008 | `docs/adrs/008-define-public-rest-api-contract.md` | Decision, Consequences | Accepted | Defines public endpoint mappings and wire schemas. |
| ADR-005 | `docs/adrs/005-use-shared-operation-domain-model-for-metadata-persistence.md` | Decision, Consequences | Accepted | Prohibits exposing internal storage paths to clients. |
| ADR-006 | `docs/adrs/006-use-root-relative-keys-for-temporary-local-storage.md` | Decision, Consequences | Accepted | Defines root-relative keys and storage locations. |
| ADR-007 | `docs/adrs/007-define-process-execution-contract-for-media-tools.md` | Decision, Consequences | Accepted | Restricts direct process execution from API layer. |
| User Decision | Current planning session | Concurrency, class location, properties | Confirmed by user | Confirms technical choices for Task 014. |

## Confirmed Architecture Decisions

List architecture decisions that are already confirmed by source documents, codebase evidence, or explicit user confirmation.

| Decision | Source | Impact on This Task | Notes |
| --- | --- | --- | --- |
| Implement `OperationApiPort` directly in `com.lucasdourado.mediautility.api` | User Decision, current codebase | The concrete implementation `OperationService` is created in the `api` package, keeping controller delegation simple. | Confirmed |
| Run background task using Spring's `@Async` annotation | User Decision | Adds `@EnableAsync` to `MediaUtilityApplication` and triggers conversion in a separate component. | Confirmed |
| Segregate async execution into a separate bean `BackgroundConversionExecutor` | Spring Architecture best practices | Resolves proxy self-invocation limitation. `OperationService` starts transaction and writes upload to disk synchronously, then calls executor's `@Async` method. | Confirmed |
| Define configurable retention duration | User Decision | Introduce Spring property `media-utility.storage.retention` with default value `1h`. | Confirmed |

## Pending Architecture Decisions

None. All task-relevant architecture decisions have been answered or explicitly deferred out of scope by the user.

## Architecture Conflicts

None.

## ADR Candidates

None.

## Implementation Impact

- **Concurrence & Threading**: The use of Spring `@Async` requires a thread pool executor. The default TaskExecutor configured by Spring Boot is used.
- **Proxy Limitations**: Self-invocation of `@Async` methods fails. Hence, `BackgroundConversionExecutor` is introduced to ensure that the method call goes through the Spring proxy.
- **File Lifecycle**: Temporary uploaded files and conversion targets must be closed and deleted in a `finally` block to prevent disk space exhaustion.

## Questions for the User

None. All task-relevant architecture questions have been answered.

## Notes for the Implementing Agent

- Ensure `@EnableAsync` is active.
- Verify Spring `@Async` method signature is `public void` so it runs asynchronously without expecting a return value.
- Inject `BackgroundConversionExecutor` into `OperationService`.
