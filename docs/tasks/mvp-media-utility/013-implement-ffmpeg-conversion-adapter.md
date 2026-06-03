# Task: Implement FFmpeg Conversion Adapter

## Status

Status: Completed

Last updated: 2026-06-03

## Task ID

ID: MVP-MEDIA-013

Order: 013

Task file: `docs/tasks/mvp-media-utility/013-implement-ffmpeg-conversion-adapter.md`

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project planning | `docs/planning/project-planning.md` | Task Group: Media Conversion, Task: Implement MP4-to-MP3 conversion adapter | Confirmed by source document | Confirms the goal of producing MP3 output from valid MP4 input using the selected media processing technology. |
| ADR-007 | `docs/adrs/007-define-process-execution-contract-for-media-tools.md` | Decision, Consequences, Task Impact | Accepted | Confirms the process executor contract and that FFmpeg adapter must use this executor. |
| ADR-005 | `docs/adrs/005-use-shared-operation-domain-model-for-metadata-persistence.md` | Decision, Consequences | Accepted | Confirms result metadata can store internal server-side paths but raw paths must not be exposed to the REST API. |
| ADR-006 | `docs/adrs/006-use-root-relative-keys-for-temporary-local-storage.md` | Decision, Consequences | Accepted | Confirms that storage keys are root-relative and resolved through the storage service. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/media/process/ProcessExecutor.java` | Entire contract | Detected in codebase | Confirms a Spring-managed ProcessExecutor exists and is implemented by LocalJvmProcessExecutor. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/storage/TemporaryStorageService.java` | Entire contract | Detected in codebase | Confirms how local file paths are resolved. |

## Context

The conversion operation accepts validated MP4 uploads and converts them to MP3. In Task 007, a safe, tool-agnostic JVM process execution adapter (`LocalJvmProcessExecutor`) was built under `media.process`. In Task 012, the backend validation layer (`Mp4UploadValidator`) was completed.

This task implements the core media conversion service contract (`Mp4ToMp3Converter`) and its adapter (`FfmpegMp4ToMp3Converter`). The adapter is responsible for building and executing the FFmpeg command line arguments using the shared `ProcessExecutor` and managing process outcomes.

## Goal

Create a Spring-managed MP4-to-MP3 conversion adapter that uses `ProcessExecutor` to run FFmpeg and maps outcomes to domain exceptions.

## Scope

- Create a `Mp4ToMp3Converter` interface inside `com.lucasdourado.mediautility.media.conversion` extending `ConversionBoundary`.
- Create a `ConversionException` runtime exception inside the same package.
- Create an `FfmpegMp4ToMp3Converter` component implementing `Mp4ToMp3Converter`.
- Read configured FFmpeg executable path (e.g. `media-utility.ffmpeg.path`, defaulting to `ffmpeg`) from Spring environment.
- Validate that the input source file exists on disk before initiating process execution.
- Construct and execute the FFmpeg process with optimized, non-interactive parameters:
  - `-y` (overwrite output file)
  - `-i <source>` (input file)
  - `-vn` (disable video stream)
  - `-acodec libmp3lame` (lame MP3 encoder)
  - `-q:a 2` (VBR target for high-quality audio output)
  - `<target>` (output file)
- Run FFmpeg with the target output parent directory as the working directory.
- Verify execution result:
  - If the process times out or is interrupted, throw a `ConversionException`.
  - If the exit status is non-zero, throw a `ConversionException` containing the stderr output.
  - If the output target file does not exist or is empty (0 bytes) after a zero exit code, throw a `ConversionException`.
- Write comprehensive unit tests in `FfmpegMp4ToMp3ConverterTest` using Mockito to mock `ProcessExecutor`.
- Write a conditional integration test that runs real conversion if `ffmpeg` is available on the path of the running system.

## Out of Scope

- Do not implement URL download adapter or yt-dlp tasks (belongs to Task 016).
- Do not implement REST controllers, DTOs, or conversion endpoint handlers (belongs to Task 014).
- Do not update database entities or write JPA persistence code (belongs to Task 014).
- Do not add scheduler or cleanup logic (belongs to Task 019).

## Implementation Instructions

- Keep all new classes under `src/main/java/com/lucasdourado/mediautility/media/conversion/`.
- Ensure `FfmpegMp4ToMp3Converter` is annotated with `@Service` or `@Component` for injection.
- Call `ProcessExecutor.execute(ProcessExecutionRequest)` to run the command.
- Set the `workingDirectory` of `ProcessExecutionRequest` to the parent directory of the target file path.
- Throw `ConversionException` with diagnostic info on failure.

## Expected Files

| Path | Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasdourado/mediautility/media/conversion/Mp4ToMp3Converter.java` | Create | Product planning | Service interface. |
| `src/main/java/com/lucasdourado/mediautility/media/conversion/ConversionException.java` | Create | Blocked error handling | Domain-specific conversion exception. |
| `src/main/java/com/lucasdourado/mediautility/media/conversion/FfmpegMp4ToMp3Converter.java` | Create | ADR-007, planning | Concrete conversion adapter. |
| `src/test/java/com/lucasdourado/mediautility/media/conversion/FfmpegMp4ToMp3ConverterTest.java` | Create | Validation strategy | Comprehensive unit and integration tests. |
| `src/main/resources/application.properties` | Modify | Config requirement | Add `media-utility.ffmpeg.path` property. |

## Dependencies

| Dependency | Type | Status | Notes |
| --- | --- | --- | --- |
| MVP-MEDIA-007 | Previous task | Completed | Process execution adapter and contracts exist. |
| MVP-MEDIA-012 | Previous task | Completed | MP4 upload validator and package exists. |

## Validation

- Backend compiles successfully.
- Run `FfmpegMp4ToMp3ConverterTest` to verify:
  - Command line arguments are constructed exactly as specified.
  - Successful execution returns normally and writes output.
  - Target file absence or exit code != 0 throws `ConversionException`.
  - Process timeout throws `ConversionException`.
  - Pre-execution validation rejects missing input source file.
- Verify the build passes via `.\mvnw test` (excluding context-load test if datasource env vars are missing).

## Acceptance Criteria

- [x] Interface `Mp4ToMp3Converter` exists in package `com.lucasdourado.mediautility.media.conversion`.
- [x] Exception `ConversionException` exists in package `com.lucasdourado.mediautility.media.conversion`.
- [x] Class `FfmpegMp4ToMp3Converter` exists and implements `Mp4ToMp3Converter`.
- [x] The converter executes FFmpeg using the shared `ProcessExecutor` and optimized arguments: `-y`, `-i`, `-vn`, `-acodec libmp3lame`, `-q:a 2`.
- [x] The converter validates input presence before executing, throwing `ConversionException` if missing.
- [x] The converter validates output presence and size > 0 after execution, throwing `ConversionException` if missing or empty.
- [x] The converter maps exit codes != 0, timeout, and thread interruption to `ConversionException`.
- [x] FFmpeg path is configurable via Spring property `media-utility.ffmpeg.path`.
- [x] Unit tests mock `ProcessExecutor` to test all success and failure flow logic without needing FFmpeg installed.
- [x] Integration tests run a real conversion conditionally if `ffmpeg` is present on the system path.

## Risks

- Executable path errors: If `ffmpeg` is not on the path and not overridden, runtime execution will fail. Handled by raising a clear `ConversionException`.
- IO stream or CPU exhaustion: Blocked by using the default execution timeout (5m) and letting the OS manage file handles via Java's ProcessBuilder.
- Interrupted or timed-out conversions might leave orphaned files: Handled by throwing an exception, allowing the higher-level orchestrator or cleanup job to purge temporary files.

## Open Questions

None.

## Notes for the Implementing Agent

- For unit testing target file validation without running FFmpeg, implement an answer stub for `ProcessExecutor.execute` that writes a dummy string/byte array to the target path so that the post-execution file-existence check passes.
