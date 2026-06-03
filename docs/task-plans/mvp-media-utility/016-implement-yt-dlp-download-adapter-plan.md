# Task Implementation Plan: Implement yt-dlp Download Adapter

## Status

Status: Completed

Last updated: 2026-06-03

Plan file: `docs/task-plans/mvp-media-utility/016-implement-yt-dlp-download-adapter-plan.md`

Architecture decision notes file: Not generated

## Task Reference

Task ID: MVP-MEDIA-016

Task file: `docs/tasks/mvp-media-utility/016-implement-yt-dlp-download-adapter.md`

Task status: Ready

Task group or feature: mvp-media-utility

## Planning Mode Requirement

Plan mode verified: Yes

Notes:

- This plan is created in plan mode.
- Implementation must not start during `plan-task`.
- A future implementation request must use this saved plan and any saved architecture decision notes as source context.

## Source Documents

List every document, ADR, codebase evidence item, or explicit user decision used to prepare this plan.

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/tasks/mvp-media-utility/016-implement-yt-dlp-download-adapter.md` | Scope, Implementation Instructions, Acceptance Criteria | Confirmed by source document | Defines the download adapter task scope and requirements. |
| Project planning | `docs/planning/project-planning.md` | Task Group: Media Download, Task: Implement URL download adapter | Confirmed by source document | Confirms the goal of producing downloadable media from a user-provided URL using the selected technology. |
| ADR-007 | `docs/adrs/007-define-process-execution-contract-for-media-tools.md` | Decision, Consequences, Task Impact | Accepted | Confirms the process executor contract and that yt-dlp adapter must use this executor. |
| ADR-006 | `docs/adrs/006-use-root-relative-keys-for-temporary-local-storage.md` | Decision, Consequences | Accepted | Confirms that storage keys are root-relative and resolved through the storage service. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/media/process/ProcessExecutor.java` | Entire contract | Detected in codebase | Confirms a Spring-managed ProcessExecutor exists and is implemented by LocalJvmProcessExecutor. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/media/download/UrlDownloadValidator.java` | Entire contract | Detected in codebase | Confirms URL validator and download package package-info. |
| User decision | Current planning session | yt-dlp parameters and target format | Confirmed by user | Confirmed download target format is MP4, command arguments are `--no-playlist`, `-f bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best`, `-o <target>`, and inherits default timeout of 5m. |

## Context Summary

The public URL download operation accepts validated public URLs (e.g. YouTube) and downloads the target media. In Task 007, a safe, tool-agnostic JVM process execution adapter (`LocalJvmProcessExecutor`) was built under `media.process`. In Task 015, the URL validation layer (`UrlDownloadValidator`) was completed.

This task implements the core media download service contract (`UrlDownloader` interface) and its adapter (`YtDlpUrlDownloader`). The adapter is responsible for building and executing the `yt-dlp` command line arguments using the shared `ProcessExecutor` and managing process outcomes.

## Task Goal

Create a Spring-managed URL download adapter that uses `ProcessExecutor` to run `yt-dlp` and maps outcomes to domain exceptions.

## Confirmed Scope

- Define `UrlDownloader` interface extending `DownloadBoundary`.
- Define `DownloadException` runtime exception.
- Implement `YtDlpUrlDownloader` implementing `UrlDownloader`.
- Inject `ProcessExecutor` into `YtDlpUrlDownloader` via constructor.
- Support configuring the `yt-dlp` executable path via Spring property `media-utility.ytdlp.path` (defaulting to `"yt-dlp"`).
- Validate that the input URL and target path are not null before starting execution.
- Invoke the process executor with command line: `[<yt-dlp-path>, --no-playlist, -f, bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best, -o, <target-path>, <url>]`.
- Use the parent directory of `<target-path>` as the process's working directory.
- Verify `ProcessExecutionResult`:
  - If process timed out, throw `DownloadException` with a timeout message.
  - If process returned non-zero exit code, throw `DownloadException` containing the exit code and stderr.
  - If process returned 0, verify that `<target-path>` exists and is not empty. If not, throw `DownloadException`.
- Write unit tests in `YtDlpUrlDownloaderTest` mocking `ProcessExecutor` to cover success and all failure modes.
- Write a conditional integration test that executes real download only if `yt-dlp` is available on the local OS path.

## Out of Scope

- Implementing URL download endpoint or background executor (belongs to Task 017).
- Modifying `OperationService.createDownload` to call background download executor (belongs to Task 017).
- Creating/modifying controller REST DTOs (belongs to Task 008 / Task 017).
- Temporary file cleanup (belongs to Task 019).

## Requirements Covered

| Requirement | Source | How This Task Covers It | Status |
| --- | --- | --- | --- |
| URL download adapter | `docs/planning/project-planning.md` | Concrete yt-dlp download adapter implemented and tested. | Confirmed |
| Run media tools via ProcessExecutor | ADR-007 | Downloader uses ProcessExecutor to execute yt-dlp instead of direct ProcessBuilder. | Confirmed |
| Safe path resolution | ADR-006 | Downloader works entirely with local Path and URI inputs, leaving storage key resolution to caller. | Confirmed |

## Technical Specification Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Media Download | Full | URL download interface and yt-dlp adapter. | None |

Coverage assessment:

- Justifying Tech Spec section: Media Download
- Tech Spec sections implemented by this task: Media Download
- Gaps between task and Tech Spec: None (planning document and codebase establish direct boundaries)
- Dependencies not specified by the Tech Spec: None

## Architecture and ADR Considerations

| Decision or ADR | Source | Impact on This Task | Status |
| --- | --- | --- | --- |
| ADR-007 (Process Contract) | `docs/adrs/007-define-process-execution-contract-for-media-tools.md` | Prohibits direct command execution outside of process package; requires using ProcessExecutor. | Confirmed |
| ADR-005 (Internal Paths) | `docs/adrs/005-use-shared-operation-domain-model-for-metadata-persistence.md` | Exposes local paths only internally; REST layer must not see them. | Confirmed |
| ADR-006 (Root-Relative Keys) | `docs/adrs/006-use-root-relative-keys-for-temporary-local-storage.md` | Exposes local paths through Storage Service only, which this task interacts with at orchestration boundary. | Confirmed |

ADR candidates or architecture decisions needed:

- None

Architecture decision notes:

- Saved separately: No
- Path: Not generated
- Notes file status: Not applicable

## Confirmed Decisions

- Inherit default process timeout from `ProcessExecutor` (5 minutes, configurable via `media-utility.process.default-timeout`).
- Download target format is MP4, with yt-dlp arguments `--no-playlist`, `-f bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best`, `-o <target>`, and the URL.
- Perform source URI and target path null validation inside `YtDlpUrlDownloader` before starting execution.
- Check target file existence and size > 0 after execution before returning successfully.

## Pending Decisions

None. All task-relevant decisions have been answered or explicitly deferred out of scope by the user.

## Questions for the User

None. All task-relevant questions have been answered.

## Proposed Implementation Approach

1. **Define Contract**: Create `UrlDownloader` interface extending `DownloadBoundary` with the method `void download(URI url, Path target)`.
2. **Define Exception**: Create `DownloadException` inheriting from `RuntimeException` to wrap all download/process failures.
3. **Implement Adapter**:
   - Create `YtDlpUrlDownloader` annotated with `@Service`.
   - Inject `ProcessExecutor` in the constructor.
   - Inject `@Value("${media-utility.ytdlp.path:yt-dlp}")` String executable path.
   - Implement `download(URI url, Path target)`:
     - Assert that `url` is not null, and `target` is not null.
     - Construct a `ProcessExecutionRequest` using the configured executable path and arguments: `List.of("--no-playlist", "-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best", "-o", target.toAbsolutePath().toString(), url.toString())`.
     - Pass the parent directory `target.getParent()` as the working directory.
     - Call `ProcessExecutor.execute(request)`.
     - Evaluate `ProcessExecutionResult`:
       - If timedOut, throw `DownloadException` with timeout message.
       - If exitCode is not 0, throw `DownloadException` with exit code and stderr.
       - If exitCode is 0, verify that `target` exists and `Files.size(target) > 0`. If not, throw `DownloadException` with output missing/empty message.
       - If any `ProcessExecutionException` or `IOException` is thrown, catch and wrap in `DownloadException`.
4. **Configure Properties**: Add `media-utility.ytdlp.path=yt-dlp` in `application.properties`.
5. **Write Tests**:
   - Unit tests mocking `ProcessExecutor` to verify all logical paths and exceptions.
   - Integration tests executing real download only if `yt-dlp` is available on the local path.

## Files and Areas Expected to Change

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasdourado/mediautility/media/download/UrlDownloader.java` | Create | Task specification | Interface |
| `src/main/java/com/lucasdourado/mediautility/media/download/DownloadException.java` | Create | Task specification | Domain Exception |
| `src/main/java/com/lucasdourado/mediautility/media/download/YtDlpUrlDownloader.java` | Create | Task specification | Concrete Service |
| `src/test/java/com/lucasdourado/mediautility/media/download/YtDlpUrlDownloaderTest.java` | Create | Task validation | Unit/Integration Tests |
| `src/main/resources/application.properties` | Modify | Configuration | Add default yt-dlp path |

## Step-by-Step Implementation Plan

1. **Create Interface**: Implement `UrlDownloader` interface under `com.lucasdourado.mediautility.media.download`.
2. **Create Exception**: Implement `DownloadException` runtime exception under `com.lucasdourado.mediautility.media.download`.
3. **Implement Adapter**: Implement `YtDlpUrlDownloader` under `com.lucasdourado.mediautility.media.download`. Ensure it is a Spring `@Service` and implements `UrlDownloader`.
4. **Implement Unit Tests**: Create `YtDlpUrlDownloaderTest` and mock `ProcessExecutor`. Verify all mock outcomes (success, non-zero exit, timeout, IO failures) result in correct method execution or exceptions. Ensure that on mock success, the mock writes dummy bytes to the target path so the post-execution check succeeds.
5. **Implement Integration Tests**: Add a conditional test in the test suite that checks if `yt-dlp` is present, writes a temp file target, runs `download` with a sample video URL, and asserts that a valid MP4 file is generated.
6. **Configure Properties**: Verify properties are set correctly in `application.properties`.
7. **Run Maven build**: Execute `./mvnw test -Dtest=!MediaUtilityApplicationTests` to verify all tests pass.

## Validation Strategy

- Run unit test suite `YtDlpUrlDownloaderTest`.
- Ensure mock tests execute completely without needing a local installation of yt-dlp.
- Ensure integration test compiles and runs conditionally based on local yt-dlp availability.
- Verify no other existing package test is broken.

## Tests to Add or Update

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| `YtDlpUrlDownloaderTest` | Unit | Validate adapter logic under all ProcessExecutor result configurations. | Mocks ProcessExecutor. |
| `YtDlpUrlDownloaderTest` | Integration | Validate real end-to-end media download using yt-dlp binary when available on the local OS. | Conditional (disabled if yt-dlp not present). |

## Acceptance Criteria

- [ ] Interface `UrlDownloader` exists in package `com.lucasdourado.mediautility.media.download`.
- [ ] Exception `DownloadException` exists in package `com.lucasdourado.mediautility.media.download`.
- [ ] Class `YtDlpUrlDownloader` exists and implements `UrlDownloader`.
- [ ] The downloader executes `yt-dlp` using the shared `ProcessExecutor` and arguments: `--no-playlist`, `-f bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best`, `-o <target>`, `<url>`.
- [ ] The downloader validates input presence before executing, throwing `DownloadException` if missing.
- [ ] The downloader validates output presence and size > 0 after execution, throwing `DownloadException` if missing or empty.
- [ ] The downloader maps exit codes != 0, timeout, and thread interruption to `DownloadException`.
- [ ] `yt-dlp` path is configurable via Spring property `media-utility.ytdlp.path`.
- [ ] Unit tests mock `ProcessExecutor` to test all success and failure flow logic without needing `yt-dlp` installed.
- [ ] Integration tests run a real download conditionally if `yt-dlp` is present on the system path.

## Risks and Edge Cases

- **Process Hangups**: Blocked by ProcessExecutor timeout.
- **Interruption**: Caught and wrapped, ensuring the OS process is cleaned up (handled by LocalJvmProcessExecutor's destroy block).
- **Paths with spaces**: Handled by passing raw String paths to ProcessBuilder through the arguments list instead of command line string concatenation.

## Rollback or Recovery Notes

- Delete `UrlDownloader.java`, `DownloadException.java`, `YtDlpUrlDownloader.java`, and `YtDlpUrlDownloaderTest.java`.
- Remove property `media-utility.ytdlp.path` from `application.properties`.

## Documentation Updates

- None required (Harness tasks list is updated by task status/executions).

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

- When implementing Mockito tests, write a lenient answer mock for `ProcessExecutor.execute` that writes dummy bytes to the target Path so that the adapter's post-execution validation passes during success scenarios.
- Do not add REST endpoints or change persistence logic.
