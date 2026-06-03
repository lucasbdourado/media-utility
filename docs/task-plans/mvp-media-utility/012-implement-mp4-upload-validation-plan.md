# Task Implementation Plan: Implement MP4 Upload Validation

## Status

Status: Ready for Implementation

Last updated: 2026-06-03

Plan file: `docs/task-plans/mvp-media-utility/012-implement-mp4-upload-validation-plan.md`

Architecture decision notes file: Not generated

## Task Reference

Task ID: MVP-MEDIA-012

Task file: `docs/tasks/mvp-media-utility/012-implement-mp4-upload-validation.md`

Task status: Ready

Task group or feature: mvp-media-utility

## Planning Mode Requirement

Plan mode verified: Yes

Notes:

- This plan was created in plan mode.
- Implementation must not start during `plan-task`.
- A future implementation request must use this saved plan as source context.

## Source Documents

List every document, ADR, codebase evidence item, or explicit user decision used to prepare this plan.

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | [012-implement-mp4-upload-validation.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/012-implement-mp4-upload-validation.md) | Scope, Instructions, Acceptance Criteria | Confirmed by source document | Defines the validator requirements. |
| Project planning | [project-planning.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/planning/project-planning.md) | Task Group: Media Conversion | Confirmed by source document | Confirms the goal of rejecting invalid conversion inputs. |
| ADR-008 | [008-define-public-rest-api-contract.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/adrs/008-define-public-rest-api-contract.md) | HTTP status mapping | Accepted | Confirms API responses for validation error (400) and unsupported media type (415). |
| Codebase | [OperationApiController.java](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/src/main/java/com/lucasdourado/mediautility/api/OperationApiController.java) | `validateMp4File` method | Detected in codebase | Existing controller-level checks for extension/content-type. |
| Codebase | [ConversionBoundary.java](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/src/main/java/com/lucasdourado/mediautility/media/conversion/ConversionBoundary.java) | Package boundary | Detected in codebase | Package boundary marker. |

## Context Summary

The media utility accepts user-uploaded MP4 files for conversion into MP3 format. The API controller currently includes basic checks for file presence, extension, and content type. To protect the system from resource exhaustion or processing corrupted and disguised files, a robust validation component is required. This task implements the backend MP4 validation component, verifying file metadata, enforcing size limits, and inspecting file headers (magic bytes) for the MP4 signature.

## Task Goal

Create a Spring-managed `Mp4UploadValidator` component and corresponding unit tests to validate uploaded MP4 files against presence, size limits, correct metadata, and the presence of the "ftyp" magic bytes.

## Confirmed Scope

- Create a Spring-managed `@Component` named `Mp4UploadValidator` under `com.lucasdourado.mediautility.media.conversion`.
- Expose maximum allowed file size config via `@Value("${media-utility.upload.max-size-bytes:52428800}")` (default 50MB).
- Create a custom domain runtime exception `Mp4ValidationException` under `com.lucasdourado.mediautility.media.conversion` that defines an `ErrorReason` enum containing:
  - `MISSING_OR_EMPTY`
  - `INVALID_EXTENSION`
  - `INVALID_CONTENT_TYPE`
  - `SIZE_LIMIT_EXCEEDED`
  - `INVALID_HEADER`
- Implement validation methods accepting:
  - `MultipartFile`
  - `InputStream`, `String filename`, `String contentType`, `long sizeBytes` (for general use).
- Implement magic bytes inspection: read up to the first 8 bytes of the file stream to assert the presence of `ftyp` (specifically, bytes 4-7 must contain the characters "ftyp", corresponding to bytes `0x66`, `0x74`, `0x79`, `0x70`).
- Ensure stream reading is efficient and non-destructive: use `readNBytes(8)` to minimize memory consumption, and apply `mark(8)`/`reset()` if the input stream supports marking, avoiding consuming stream contents for subsequent steps.
- Add the configuration key `media-utility.upload.max-size-bytes=52428800` to `src/main/resources/application.properties`.
- Create a comprehensive unit test suite in `Mp4UploadValidatorTest` using JUnit 5 and `MockMultipartFile` to cover all validation flows.

## Out of Scope

- Do not implement FFmpeg process execution or conversion logic.
- Do not implement URL download validation.
- Do not implement/change the conversion orchestration endpoints or services (leaving controller integration for future orchestration tasks).
- Do not add database persistence or change existing operation models.

## Requirements Covered

| Requirement | Source | How This Task Covers It | Status |
| --- | --- | --- | --- |
| Spring-managed validator | MVP-MEDIA-012 | Implement `Mp4UploadValidator` as a `@Component` in `media.conversion`. | Confirmed |
| Metadata validation | MVP-MEDIA-012 | Validator asserts non-empty, `.mp4` extension, and `video/mp4` type. | Confirmed |
| Configurable size limits | MVP-MEDIA-012 | Read size limit from properties with `@Value` and default to 50MB. | Confirmed |
| Magic bytes check | MVP-MEDIA-012 | Verify first 8 bytes contain the "ftyp" signature. | Confirmed |
| Domain exception | MVP-MEDIA-012 | Throw `Mp4ValidationException` with detailed semantic reason. | Confirmed |
| Robust test suite | MVP-MEDIA-012 | Add `Mp4UploadValidatorTest` covering edge cases and byte-level check. | Confirmed |

## Technical Specification Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| N/A | Missing | N/A | `tech-spec.md` is empty in the current workspace. |

Coverage assessment:

- Justifying Tech Spec section: N/A
- Tech Spec sections implemented by this task: N/A
- Gaps between task and Tech Spec: N/A
- Dependencies not specified by the Tech Spec: Confirmed by task instructions and codebase layout.

## Architecture and ADR Considerations

| Decision or ADR | Source | Impact on This Task | Status |
| --- | --- | --- | --- |
| Java 21 Spring Boot Monolith | ADR-001 | Validator will be built as a standard Spring `@Component`. | Confirmed |
| Package Boundaries | Task 002 | Validator resides inside `com.lucasdourado.mediautility.media.conversion`. | Confirmed |
| REST API status mapping | ADR-008 | Custom domain exception reasons will support mapping to 400 or 415. | Confirmed |

ADR candidates or architecture decisions needed:

- None.

Architecture decision notes:

- Saved separately: No
- Path: N/A
- Notes file status: Not applicable

## Confirmed Decisions

- **Validator package**: `com.lucasdourado.mediautility.media.conversion`.
- **Exception handling**: Use custom domain-level `Mp4ValidationException` containing `ErrorReason` enum instead of HTTP/API-specific `ApiException` to maintain modularity.
- **Config property**: `media-utility.upload.max-size-bytes`, default 50MB (52,428,800 bytes) in `application.properties`.
- **Magic bytes offset**: Assert bytes 4-7 equal to `[0x66, 0x74, 0x79, 0x70]`.

## Pending Decisions

None. All task-relevant decisions have been answered or explicitly deferred out of scope by the user.

## Questions for the User

None. All task-relevant questions have been answered.

## Proposed Implementation Approach

1. **Create Exception Class**:
   Define `Mp4ValidationException` as a runtime exception wrapping an `ErrorReason` enum to communicate the specific constraint violation.
2. **Implement Validator**:
   Create `Mp4UploadValidator` inside `com.lucasdourado.mediautility.media.conversion`. Inject configuration values using `@Value`.
3. **Stream Handling & Magic Bytes**:
   - Check metadata (presence, filename ending, contentType).
   - Check size limit against `sizeBytes`.
   - Read 8 bytes from `InputStream` using `readNBytes(8)`. If `markSupported()` is true, use `mark`/`reset` to preserve the stream state.
   - Assert `ftyp` signature in bytes 4-7.
4. **Configuration**:
   Add `media-utility.upload.max-size-bytes` config entry to `application.properties`.
5. **Unit Tests**:
   - Write JUnit 5 unit tests in `Mp4UploadValidatorTest`.
   - Test a valid MP4 fixture byte array (e.g. 8 bytes: `[0x00, 0x00, 0x00, 0x18, 0x66, 0x74, 0x79, 0x70]`).
   - Test size limits.
   - Test missing `ftyp` header bytes.
   - Test wrong extension and content-type.
   - Test empty streams.

## Files and Areas Expected to Change

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasdourado/mediautility/media/conversion/Mp4ValidationException.java` | Create | Task instructions | New domain exception. |
| `src/main/java/com/lucasdourado/mediautility/media/conversion/Mp4UploadValidator.java` | Create | Task instructions | Validator component. |
| `src/main/resources/application.properties` | Modify | Task instructions | Configure upload size property. |
| `src/test/java/com/lucasdourado/mediautility/media/conversion/Mp4UploadValidatorTest.java` | Create | Validation strategy | Comprehensive unit tests. |

## Step-by-Step Implementation Plan

1. Create `Mp4ValidationException.java` under `src/main/java/com/lucasdourado/mediautility/media/conversion/`.
2. Create `Mp4UploadValidator.java` under `src/main/java/com/lucasdourado/mediautility/media/conversion/`.
3. Add configuration to `src/main/resources/application.properties`.
4. Create parent directories and create `Mp4UploadValidatorTest.java` under `src/test/java/com/lucasdourado/mediautility/media/conversion/`.
5. Run `./mvnw test` to verify the build compiles and the new unit tests pass successfully.

## Validation Strategy

- Backend compiles and builds successfully using `./mvnw test`.
- All `Mp4UploadValidatorTest` test cases pass successfully.

## Tests to Add or Update

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| `Mp4UploadValidatorTest` | Unit | Verify valid files pass validation | Use standard MockMultipartFile and valid magic bytes. |
| `Mp4UploadValidatorTest` | Unit | Verify size limit enforcement | Assert `SIZE_LIMIT_EXCEEDED` is thrown. |
| `Mp4UploadValidatorTest` | Unit | Verify magic bytes header checks | Assert `INVALID_HEADER` is thrown for wrong signatures. |
| `Mp4UploadValidatorTest` | Unit | Verify empty / null files | Assert `MISSING_OR_EMPTY` is thrown. |
| `Mp4UploadValidatorTest` | Unit | Verify wrong extension / content-type | Assert `INVALID_EXTENSION` / `INVALID_CONTENT_TYPE`. |

## Acceptance Criteria

- [ ] A Spring-managed `Mp4UploadValidator` component exists in the `media.conversion` package.
- [ ] The validator checks for file presence, size limits, content-type (`video/mp4`), and file extension (`.mp4`).
- [ ] The validator inspects magic bytes at the beginning of the file stream to assert the presence of the "ftyp" signature.
- [ ] The maximum upload size limit is configurable via Spring properties with a sensible default (e.g., 50MB).
- [ ] Appropriate exceptions are thrown on validation failures.
- [ ] A comprehensive suite of unit tests covers all validation rules, including byte-level checks.
- [ ] No conversion logic, database writes, or REST mapping changes are included.

## Risks and Edge Cases

- **Memory overhead**: Avoid reading the entire input stream into memory. Only the first 8 bytes must be read.
- **Destructive stream consumption**: If the stream does not support `mark`/`reset`, subsequent reads will fail. The implementing agent should explicitly note this behavior or document how the caller should pass a fresh stream or use mark/reset where supported.
- **Property parsing**: Ensure that `@Value` configuration behaves correctly if the property is missing from environment.

## Rollback or Recovery Notes

- Delete `Mp4UploadValidator.java`, `Mp4ValidationException.java`, `Mp4UploadValidatorTest.java` and revert changes in `application.properties`.

## Documentation Updates

- None.

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

- Read bytes 4-7 using `InputStream.readNBytes(8)` to safely match the exact "ftyp" marker.
- Make sure to write a clean error-reason mapping pattern that makes it straightforward to integrate in the API controller later.
