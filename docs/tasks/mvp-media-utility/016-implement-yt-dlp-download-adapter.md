# Task: Implement yt-dlp Download Adapter

## Status

Status: Completed

Last updated: 2026-06-03

## Task ID

ID: MVP-MEDIA-016

Order: 016

Task file: `docs/tasks/mvp-media-utility/016-implement-yt-dlp-download-adapter.md`

## Source Documents

List every document or explicit user decision that justifies this task.

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project planning | `docs/planning/project-planning.md` | Task Group: Media Download, Task: Implement URL download adapter | Confirmed by source document | Confirms the goal of producing downloadable media from a user-provided URL using the selected technology. |
| ADR-007 | `docs/adrs/007-define-process-execution-contract-for-media-tools.md` | Decision, Consequences, Task Impact | Accepted | Confirms the process executor contract and that yt-dlp adapter must use this executor. |
| ADR-006 | `docs/adrs/006-use-root-relative-keys-for-temporary-local-storage.md` | Decision, Consequences | Accepted | Confirms that storage keys are root-relative and resolved through the storage service. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/media/process/ProcessExecutor.java` | Entire contract | Detected in codebase | Confirms a Spring-managed ProcessExecutor exists and is implemented by LocalJvmProcessExecutor. |

## Context

The public URL download operation accepts validated public URLs (e.g. YouTube) and downloads the target media. In Task 007, a safe, tool-agnostic JVM process execution adapter (`LocalJvmProcessExecutor`) was built under `media.process`. In Task 015, the URL validation layer (`UrlDownloadValidator`) was completed.

This task implements the core media download service contract (`UrlDownloader` interface) and its adapter (`YtDlpUrlDownloader`). The adapter is responsible for building and executing the `yt-dlp` command line arguments using the shared `ProcessExecutor` and managing process outcomes.

## Goal

Create a Spring-managed URL download adapter that uses `ProcessExecutor` to run `yt-dlp` and maps outcomes to domain exceptions.

## Scope

- Create a `UrlDownloader` interface inside `com.lucasdourado.mediautility.media.download` extending `DownloadBoundary`.
- Create a `DownloadException` runtime exception inside the same package.
- Create a `YtDlpUrlDownloader` component implementing `UrlDownloader`.
- Read configured `yt-dlp` executable path (e.g. `media-utility.ytdlp.path`, defaulting to `yt-dlp`) from Spring environment.
- Validate that the input URL and target path are not null before initiating process execution.
- Construct and execute the `yt-dlp` process with optimized, non-interactive parameters:
  - `--no-playlist` (only download a single video if URL points to a playlist/mix)
  - `-f` `bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best` (request MP4 format or best available merging to MP4)
  - `-o` `<target>` (specify absolute target output path)
  - `<url>` (the target URL to download)
- Run `yt-dlp` with the target output parent directory as the working directory.
- Verify execution result:
  - If the process times out or is interrupted, throw a `DownloadException`.
  - If the exit status is non-zero, throw a `DownloadException` containing the stderr output.
  - If the output target file does not exist or is empty (0 bytes) after a zero exit code, throw a `DownloadException`.
- Write comprehensive unit tests in `YtDlpUrlDownloaderTest` using Mockito to mock `ProcessExecutor`.
- Write a conditional integration test that runs real download conditionally if `yt-dlp` is available on the path of the running system.

## Out of Scope

- Implementing URL download endpoint or background executor (belongs to Task 017).
- Modifying `OperationService.createDownload` to call background download executor (belongs to Task 017).
- Creating/modifying controller REST models (belongs to Task 008 / Task 017).
- Temporary file cleanup (belongs to Task 019).

## Implementation Instructions

- Keep all new classes under `src/main/java/com/lucasdourado/mediautility/media/download/`.
- Ensure `YtDlpUrlDownloader` is annotated with `@Service` or `@Component` for injection.
- Call `ProcessExecutor.execute(ProcessExecutionRequest)` to run the command.
- Set the `workingDirectory` of `ProcessExecutionRequest` to the parent directory of the target file path.
- Throw `DownloadException` with diagnostic info on failure.
- Add Spring property `media-utility.ytdlp.path=yt-dlp` to `application.properties`.

## Expected Files

List expected files to create or modify when known.

| Path | Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasdourado/mediautility/media/download/UrlDownloader.java` | Create | Product planning | Service interface. |
| `src/main/java/com/lucasdourado/mediautility/media/download/DownloadException.java` | Create | Blocked error handling | Domain-specific download exception. |
| `src/main/java/com/lucasdourado/mediautility/media/download/YtDlpUrlDownloader.java` | Create | ADR-007, planning | Concrete download adapter. |
| `src/test/java/com/lucasdourado/mediautility/media/download/YtDlpUrlDownloaderTest.java` | Create | Validation strategy | Comprehensive unit and integration tests. |
| `src/main/resources/application.properties` | Modify | Config requirement | Add `media-utility.ytdlp.path` property. |

## Dependencies

List task dependencies, external dependencies, and pending decisions.

| Dependency | Type | Status | Notes |
| --- | --- | --- | --- |
| MVP-MEDIA-007 | Previous task | Ready | Process execution adapter and contracts exist. |
| MVP-MEDIA-015 | Previous task | Ready | URL validation component exists. |

## Validation

Describe how the implementing agent must verify the task.

- Backend compiles successfully: `.\mvnw clean compile`
- Run `YtDlpUrlDownloaderTest` to verify:
  - Command line arguments are constructed exactly as specified.
  - Successful execution returns normally and writes output.
  - Target file absence or exit code != 0 throws `DownloadException`.
  - Process timeout throws `DownloadException`.
  - Pre-execution validation rejects missing input URL or target path.
- Verify the build passes via `.\mvnw test`.

## Acceptance Criteria

List objective criteria that prove this task is complete.

- [ ] Interface `UrlDownloader` exists in package `com.lucasdourado.mediautility.media.download`.
- [ ] Exception `DownloadException` exists in package `com.lucasdourado.mediautility.media.download`.
- [ ] Class `YtDlpUrlDownloader` exists and implements `UrlDownloader`.
- [ ] The downloader executes `yt-dlp` using the shared `ProcessExecutor` and optimized arguments: `--no-playlist`, `-f bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best`, `-o <target>`, `<url>`.
- [ ] The downloader validates input presence before executing, throwing `DownloadException` if missing.
- [ ] The downloader validates output presence and size > 0 after execution, throwing `DownloadException` if missing or empty.
- [ ] The downloader maps exit codes != 0, timeout, and thread interruption to `DownloadException`.
- [ ] `yt-dlp` path is configurable via Spring property `media-utility.ytdlp.path`.
- [ ] Unit tests mock `ProcessExecutor` to test all success and failure flow logic without needing `yt-dlp` installed.
- [ ] Integration tests run a real download conditionally if `yt-dlp` is present on the system path.

## Risks

List known risks, constraints, or regression areas.

- Executable path errors: If `yt-dlp` is not on the path and not overridden, runtime execution will fail. Handled by raising a clear `DownloadException`.
- IO stream or CPU exhaustion: Blocked by using the default execution timeout (5m) and letting the OS manage file handles via Java's ProcessBuilder.
- Interrupted or timed-out downloads might leave orphaned files: Handled by throwing an exception, allowing the higher-level orchestrator or cleanup job to purge temporary files.

## Open Questions

List missing or unconfirmed details. Do not turn these into implementation instructions.

- None.

## Notes for the Implementing Agent

Add concise handoff notes, source-reading reminders, or sequencing constraints.

- For unit testing target file validation without running `yt-dlp`, implement an answer stub for `ProcessExecutor.execute` that writes a dummy string/byte array to the target path so that the post-execution file-existence check passes.
