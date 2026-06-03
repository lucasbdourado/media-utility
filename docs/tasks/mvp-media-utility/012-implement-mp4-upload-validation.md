# Task: Implement MP4 Upload Validation

## Status

Status: Ready

Last updated: 2026-06-03

## Task ID

ID: MVP-MEDIA-012

Order: 012

Task file: `docs/tasks/mvp-media-utility/012-implement-mp4-upload-validation.md`

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project planning | `docs/planning/project-planning.md` | Task Group: Media Conversion, Task: Add MP4 upload validation | Confirmed by source document | Confirms the goal of rejecting invalid conversion inputs with file type, size, and basic constraints checks. |
| ADR-008 | `docs/adrs/008-define-public-rest-api-contract.md` | Decision, HTTP status mapping, Consequences | Accepted | Confirms API error responses for validation error (400) and unsupported media type (415). |
| Task 008 execution report | `docs/task-executions/mvp-media-utility/008-define-rest-api-contracts-execution.md` | Execution Summary, Validation | Confirmed by source document | Notes that task 008 deferred byte inspection validation to task 012. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/api/OperationApiController.java` | `validateMp4File` method | Detected in codebase | Existing controller-level check for presence, content type, and filename extension. |

## Context

The conversion operation accepts user-uploaded MP4 files to be converted into MP3 format. In Task 008, the API controller was implemented with basic checks for file presence, extension, and content type. However, to protect the server from processing corrupted, disguised, or excessively large files (which could crash FFmpeg or cause resource exhaustion), a deeper validation layer is required. 

This task implements the backend MP4 validation component, which includes validating file presence, size limits, file extension, MIME type, and inspecting the file's header (magic bytes) for a valid MP4 signature.

## Goal

Create a robust backend validator component for MP4 file uploads that verifies file presence, filename extension, content type, max size constraints, and verifies magic bytes/header signature.

## Scope

- Create an `Mp4UploadValidator` component inside the `com.lucasdourado.mediautility.media.conversion` package.
- Implement size limits validation, reading the maximum allowed size (e.g., `media-utility.upload.max-size-bytes`, defaulting to 50MB) from Spring environment/properties.
- Implement magic bytes inspection: read the first 8 bytes of the input stream and assert that the file contains the "ftyp" box (specifically, bytes 4-7 must contain "ftyp").
- Return clear validation failures or throw a custom validation exception that can be mapped to appropriate HTTP statuses by the REST layer.
- Write extensive unit tests covering valid MP4 files (including mock/fixture MP4 bytes), files exceeding the size limit, files with wrong magic bytes, files with wrong extensions, and empty files.

## Out of Scope

- Do not implement FFmpeg process execution or conversion logic (belongs to Task 013).
- Do not implement URL download validation (belongs to Task 015).
- Do not implement the conversion orchestration endpoint/service (belongs to Task 014).
- Do not add database persistence code or change existing operation entity models.

## Implementation Instructions

- Keep the validator component under `src/main/java/com/lucasdourado/mediautility/media/conversion/`.
- The validator should accept a `MultipartFile` or an `InputStream` along with filename and content-type metadata.
- Expose the maximum file size configuration using `@Value("${media-utility.upload.max-size-bytes:52428800}")` (50MB as default).
- For magic bytes validation, look at the byte array layout of MP4 files. The file signature requires that the "ftyp" brand indicator is present starting at offset 4:
  - Byte 4: 'f' (0x66)
  - Byte 5: 't' (0x74)
  - Byte 6: 'y' (0x79)
  - Byte 7: 'p' (0x70)
- Throw a specific domain-level or API-compatible exception on validation failure so that it can be cleanly mapped to a 400 Bad Request or 415 Unsupported Media Type.
- Ensure the validator is a Spring managed component (e.g., `@Component`) so it can be injected in future orchestration components.

## Expected Files

| Path | Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasdourado/mediautility/media/conversion/Mp4UploadValidator.java` | Create | Project planning, ADR-008 | Main validation component. |
| `src/test/java/com/lucasdourado/mediautility/media/conversion/Mp4UploadValidatorTest.java` | Create | Validation strategy | Comprehensive unit tests for validation rules. |
| `src/main/resources/application.properties` | Modify | Config requirement | Add `media-utility.upload.max-size-bytes` default configuration if not present. |

## Dependencies

| Dependency | Type | Status | Notes |
| --- | --- | --- | --- |
| MVP-MEDIA-002 | Previous task | Completed | Monolith package boundaries and `media.conversion` package exist. |
| MVP-MEDIA-008 | Previous task | Completed | API contract and controllers defined. |
| MVP-MEDIA-010 | Previous task | Completed | Frontend upload form states built. |

## Validation

- Backend compiles successfully.
- Run `Mp4UploadValidatorTest` to verify:
  - Valid MP4 files (having "ftyp" magic bytes, correct content-type, correct extension, and size under limit) pass validation.
  - Empty files or null inputs are rejected.
  - Non-MP4 files (missing "ftyp" bytes, even if extension/content-type are renamed) are rejected.
  - Files exceeding the configured size limit are rejected.
  - Files with wrong extension or content-type are rejected.
- Scoped test suite passes via `.\mvnw test` or targeted Maven test command.

## Acceptance Criteria

- [ ] A Spring-managed `Mp4UploadValidator` component exists in the `media.conversion` package.
- [ ] The validator checks for file presence, size limits, content-type (`video/mp4`), and file extension (`.mp4`).
- [ ] The validator inspects magic bytes at the beginning of the file stream to assert the presence of the "ftyp" signature.
- [ ] The maximum upload size limit is configurable via Spring properties with a sensible default (e.g., 50MB).
- [ ] Appropriate exceptions are thrown on validation failures.
- [ ] A comprehensive suite of unit tests covers all validation rules, including byte-level checks.
- [ ] No conversion logic, database writes, or REST mapping changes are included.

## Risks

- File validation reads input stream bytes, which might consume the stream. Ensure that reading the header doesn't prevent subsequent media processing (or use mark/reset if input streams are reused, though MultipartFile allows multiple calls to `getInputStream()`).
- High memory usage if the entire file is read into memory for byte checks. Perform header checks by reading only the first few bytes (e.g., first 8 or 12 bytes), not the whole file.

## Open Questions

- Should the validator throw Spring's built-in exceptions or custom domain/API exceptions? A custom validator exception that maps to `ApiException` (with correct HttpStatus and PublicErrorResponse) is recommended to align with the REST layer.

## Notes for the Implementing Agent

- Implement magic bytes check by reading only the first 8-12 bytes from the file's input stream to minimize resource consumption and avoid reading the entire payload into memory.
- Use `MockMultipartFile` in unit tests to simulate various upload scenarios and payloads.
