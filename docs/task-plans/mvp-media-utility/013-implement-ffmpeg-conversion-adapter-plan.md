# Task Implementation Plan: Implement FFmpeg Conversion Adapter

## Status

Status: Completed

Last updated: 2026-06-03

Plan file: `docs/task-plans/mvp-media-utility/013-implement-ffmpeg-conversion-adapter-plan.md`

Architecture decision notes file: Not generated

## Task Reference

Task ID: MVP-MEDIA-013

Task file: `docs/tasks/mvp-media-utility/013-implement-ffmpeg-conversion-adapter.md`

Task status: Ready

Task group or feature: mvp-media-utility

## Planning Mode Requirement

Plan mode verified: Yes

Notes:
- This plan is created in plan mode.
- Implementation must not start during `plan-task`.
- A future implementation request must use this saved plan as source context.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project planning | `docs/planning/project-planning.md` | Task Group: Media Conversion, Task: Implement MP4-to-MP3 conversion adapter | Confirmed by source document | Confirms the goal of producing MP3 output from valid MP4 input using the selected media processing technology. |
| ADR-007 | `docs/adrs/007-define-process-execution-contract-for-media-tools.md` | Decision, Consequences, Task Impact | Accepted | Confirms the process executor contract and that FFmpeg adapter must use this executor. |
| ADR-005 | `docs/adrs/005-use-shared-operation-domain-model-for-metadata-persistence.md` | Decision, Consequences | Accepted | Confirms result metadata can store internal server-side paths but raw paths must not be exposed to the REST API. |
| ADR-006 | `docs/adrs/006-use-root-relative-keys-for-temporary-local-storage.md` | Decision, Consequences | Accepted | Confirms that storage keys are root-relative and resolved through the storage service. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/media/process/ProcessExecutor.java` | Entire contract | Detected in codebase | Confirms a Spring-managed ProcessExecutor exists and is implemented by LocalJvmProcessExecutor. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/storage/TemporaryStorageService.java` | Entire contract | Detected in codebase | Confirms how local file paths are resolved. |
| User decision | Conversation history (A1, A2, A3) | Current planning session | Confirmed by user | Confirmed process timeout inheritance, VBR `-q:a 2` setting, and pre-execution source path existence/size validation in converter. |

## Context Summary

The conversion operation accepts validated MP4 uploads and converts them to MP3. In Task 007, a safe, tool-agnostic JVM process execution adapter (`LocalJvmProcessExecutor`) was built under `media.process`. In Task 012, the backend validation layer (`Mp4UploadValidator`) was completed.

This task implements the core media conversion service contract (`Mp4ToMp3Converter`) and its adapter (`FfmpegMp4ToMp3Converter`). The adapter is responsible for building and executing the FFmpeg command line arguments using the shared `ProcessExecutor` and managing process outcomes.

## Task Goal

Create a Spring-managed MP4-to-MP3 conversion adapter that uses `ProcessExecutor` to run FFmpeg and maps outcomes to domain exceptions.

## Confirmed Scope

- Define `Mp4ToMp3Converter` interface extending `ConversionBoundary`.
- Define `ConversionException` runtime exception.
- Implement `FfmpegMp4ToMp3Converter` implementing `Mp4ToMp3Converter`.
- Inject `ProcessExecutor` into `FfmpegMp4ToMp3Converter` via constructor.
- Support configuring the `ffmpeg` executable path via Spring property `media-utility.ffmpeg.path` (defaulting to `"ffmpeg"`).
- Validate that the input source file exists and is not empty before starting execution.
- Invoke the process executor with command line: `[ffmpeg, -y, -i, <source-path>, -vn, -acodec, libmp3lame, -q:a, 2, <target-path>]`.
- Use the parent directory of `<target-path>` as the process's working directory.
- Verify `ProcessExecutionResult`:
  - If process timed out, throw `ConversionException` with a timeout message.
  - If process returned non-zero exit code, throw `ConversionException` containing the exit code and stderr.
  - If process returned 0, verify that `<target-path>` exists and is not empty. If not, throw `ConversionException`.
- Write unit tests in `FfmpegMp4ToMp3ConverterTest` mocking `ProcessExecutor` to cover success and all failure modes.
- Write a conditional integration test that executes real conversion if `ffmpeg` is available on the local OS path.

## Out of Scope

- Do not implement URL download adapter or yt-dlp tasks (belongs to Task 016).
- Do not implement REST controllers, DTOs, or conversion endpoint handlers (belongs to Task 014).
- Do not update database entities or write JPA persistence code (belongs to Task 014).
- Do not add scheduler or cleanup logic (belongs to Task 019).

## Requirements Covered

| Requirement | Source | How This Task Covers It | Status |
| --- | --- | --- | --- |
| MP4-to-MP3 conversion adapter | `docs/planning/project-planning.md` | Concrete FFmpeg conversion adapter implemented and tested. | Confirmed |
| Run media tools via ProcessExecutor | ADR-007 | Converter uses ProcessExecutor to execute FFmpeg instead of direct ProcessBuilder. | Confirmed |
| Safe path resolution | ADR-006 | Converter works entirely with local Path inputs, leaving storage key resolution to caller. | Confirmed |

## Technical Specification Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Media Conversion | Full | MP4 to MP3 conversion interface and FFmpeg adapter. | None |

Coverage assessment:
- Justifying Tech Spec section: Media Conversion
- Tech Spec sections implemented by this task: Media Conversion
- Gaps between task and Tech Spec: None (planning document and codebase establish direct boundaries)
- Dependencies not specified by the Tech Spec: None

## Architecture and ADR Considerations

| Decision or ADR | Source | Impact on This Task | Status |
| --- | --- | --- | --- |
| ADR-007 (Process Contract) | `docs/adrs/007-define-process-execution-contract-for-media-tools.md` | Prohibits direct command construction outside of process package; requires using ProcessExecutor. | Confirmed |
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
- Use VBR encoding setting `-q:a 2` for high quality MP3 conversion.
- Perform source file existence and size > 0 validation inside `FfmpegMp4ToMp3Converter` before starting execution.
- Check target file existence and size > 0 after execution before returning successfully.

## Pending Decisions

None. All task-relevant decisions have been answered or explicitly deferred out of scope by the user.

## Questions for the User

None. All task-relevant questions have been answered.

## Proposed Implementation Approach

1. **Define Contract**: Create `Mp4ToMp3Converter` extending `ConversionBoundary` with the method `void convert(Path source, Path target)`.
2. **Define Exception**: Create `ConversionException` inheriting from `RuntimeException` to wrap all conversion/process failures.
3. **Implement Adapter**:
   - Create `FfmpegMp4ToMp3Converter` annotated with `@Service`.
   - Inject `ProcessExecutor` in the constructor.
   - Inject `@Value("${media-utility.ffmpeg.path:ffmpeg}")` String executable path.
   - Implement `convert(Path source, Path target)`:
     - Assert that `source` is not null, exists, and has `Files.size(source) > 0`.
     - Construct a `ProcessExecutionRequest` using the configured executable path and arguments: `List.of("-y", "-i", source.toAbsolutePath().toString(), "-vn", "-acodec", "libmp3lame", "-q:a", "2", target.toAbsolutePath().toString())`.
     - Pass the parent directory `target.getParent()` as the working directory.
     - Call `ProcessExecutor.execute(request)`.
     - Evaluate `ProcessExecutionResult`:
       - If timedOut, throw `ConversionException` with timeout message.
       - If exitCode is not 0, throw `ConversionException` with exit code and stderr.
       - If exitCode is 0, verify that `target` exists and `Files.size(target) > 0`. If not, throw `ConversionException` with output missing/empty message.
       - If any `ProcessExecutionException` or `IOException` is thrown, catch and wrap in `ConversionException`.
4. **Configure Properties**: Add `media-utility.ffmpeg.path=ffmpeg` in `application.properties`.
5. **Write Tests**:
   - Unit tests mocking `ProcessExecutor` to verify all logical paths and exceptions.
   - Integration tests executing real conversion only if `ffmpeg` is available on the local path.

## Files and Areas Expected to Change

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasdourado/mediautility/media/conversion/Mp4ToMp3Converter.java` | Create | Task specification | Interface |
| `src/main/java/com/lucasdourado/mediautility/media/conversion/ConversionException.java` | Create | Task specification | Domain Exception |
| `src/main/java/com/lucasdourado/mediautility/media/conversion/FfmpegMp4ToMp3Converter.java` | Create | Task specification | Concrete Service |
| `src/test/java/com/lucasdourado/mediautility/media/conversion/FfmpegMp4ToMp3ConverterTest.java` | Create | Task validation | Unit/Integration Tests |
| `src/main/resources/application.properties` | Modify | Configuration | Add default ffmpeg path |

## Step-by-Step Implementation Plan

1. **Create Interface**: Implement `Mp4ToMp3Converter` interface under `com.lucasdourado.mediautility.media.conversion`.
2. **Create Exception**: Implement `ConversionException` runtime exception under `com.lucasdourado.mediautility.media.conversion`.
3. **Implement Adapter**: Implement `FfmpegMp4ToMp3Converter` under `com.lucasdourado.mediautility.media.conversion`. Ensure it is a Spring `@Service` and implements `Mp4ToMp3Converter`.
4. **Implement Unit Tests**: Create `FfmpegMp4ToMp3ConverterTest` and mock `ProcessExecutor`. Verify all mock outcomes (success, non-zero exit, timeout, IO failures) result in correct method execution or exceptions. Ensure that on mock success, the mock writes dummy bytes to the target path so the post-execution check succeeds.
5. **Implement Integration Tests**: Add a conditional test in the test suite that checks if `ffmpeg` is present, writes a mock MP4 file, runs `convert` and asserts that a valid MP3 file is generated.
6. **Configure Properties**: Verify properties are set correctly in `application.properties`.
7. **Run Maven build**: Execute `./mvnw test -Dtest=!MediaUtilityApplicationTests` to verify all tests pass.

## Validation Strategy

- Run unit test suite `FfmpegMp4ToMp3ConverterTest`.
- Ensure mock tests execute completely without needing a local installation of FFmpeg.
- Ensure integration test compiles and runs conditionally based on local FFmpeg availability.
- Verify no other existing package test is broken.

## Tests to Add or Update

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| `FfmpegMp4ToMp3ConverterTest` | Unit | Validate adapter logic under all ProcessExecutor result configurations. | Mocks ProcessExecutor. |
| `FfmpegMp4ToMp3ConverterTest` | Integration | Validate real end-to-end MP4 to MP3 conversion using FFmpeg binary when available on the local OS. | Conditional (disabled if ffmpeg not present). |

## Acceptance Criteria

- [ ] Interface `Mp4ToMp3Converter` exists in package `com.lucasdourado.mediautility.media.conversion`.
- [ ] Exception `ConversionException` exists in package `com.lucasdourado.mediautility.media.conversion`.
- [ ] Class `FfmpegMp4ToMp3Converter` exists and implements `Mp4ToMp3Converter`.
- [ ] The converter executes FFmpeg using the shared `ProcessExecutor` and arguments: `-y`, `-i`, `-vn`, `-acodec libmp3lame`, `-q:a 2`.
- [ ] The converter validates input presence and size > 0 before executing, throwing `ConversionException` if missing or empty.
- [ ] The converter validates output presence and size > 0 after execution, throwing `ConversionException` if missing or empty.
- [ ] The converter maps exit codes != 0, timeout, and thread interruption to `ConversionException`.
- [ ] FFmpeg path is configurable via Spring property `media-utility.ffmpeg.path`.
- [ ] Unit tests mock `ProcessExecutor` to test all success and failure flow logic without needing FFmpeg installed.
- [ ] Integration tests run a real conversion conditionally if `ffmpeg` is present on the system path.

## Risks and Edge Cases

- **Process Hangups**: Blocked by ProcessExecutor timeout.
- **Interruption**: Caught and wrapped, ensuring the OS process is cleaned up (handled by LocalJvmProcessExecutor's destroy block).
- **Paths with spaces**: Handled by passing raw String paths to ProcessBuilder through the arguments list instead of command line string concatenation.

## Rollback or Recovery Notes

- Delete `Mp4ToMp3Converter.java`, `ConversionException.java`, `FfmpegMp4ToMp3Converter.java`, and `FfmpegMp4ToMp3ConverterTest.java`.
- Remove property `media-utility.ffmpeg.path` from `application.properties`.

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
