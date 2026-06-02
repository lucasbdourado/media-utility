# Task Implementation Plan: Create Temporary Storage Service

## Status

Status: Ready for Implementation

Last updated: 2026-06-02

Plan file: `docs/task-plans/mvp-media-utility/006-create-temporary-storage-service-plan.md`

Architecture decision notes file: `docs/architecture/task-decisions/mvp-media-utility/006-create-temporary-storage-service-architecture-decisions.md`

## Task Reference

Task ID: `MVP-MEDIA-006`

Task file: `docs/tasks/mvp-media-utility/006-create-temporary-storage-service.md`

Task status: `Needs Clarification`

Task group or feature: `mvp-media-utility`

## Planning Mode Requirement

Plan mode verified: Yes

Notes:

- This plan was created in plan mode and updated through `resolve-architecture-blocker`.
- Implementation must not start during planning or blocker resolution.
- ADR-006 resolves the required temporary storage contract blocker.
- A future implementation request must use this saved plan, the saved architecture decision notes, and ADR-006 as source context.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project discovery | `docs/context/project-discover.md` | Project Scenario | Confirmed by source document | Required Harness discovery context exists, but it predates the current implementation codebase. |
| Task file | `docs/tasks/mvp-media-utility/006-create-temporary-storage-service.md` | Scope, Out of Scope, Dependencies, Validation, Acceptance Criteria | Confirmed by source document | Defines temporary local storage service scope and open questions. |
| Project planning | `docs/planning/project-planning.md` | Result Delivery and Temporary File Lifecycle | Confirmed by source document | Requires completed files to be downloadable immediately and removed later. |
| Task plan 002 | `docs/task-plans/mvp-media-utility/002-create-backend-module-boundaries-plan.md` | Confirmed Scope | Confirmed by source document | Confirms `storage` package boundary exists. |
| Task plan 003 | `docs/task-plans/mvp-media-utility/003-configure-mysql-jpa-persistence-baseline-plan.md` | Confirmed Scope, Out of Scope | Confirmed by source document | Confirms media bytes remain outside MySQL. |
| Task plan 004 | `docs/task-plans/mvp-media-utility/004-create-operation-domain-model-plan.md` | Result metadata and ADR considerations | Confirmed by source document | Confirms operation result metadata includes internal server-side file location data. |
| ADR-005 | `docs/adrs/005-use-shared-operation-domain-model-for-metadata-persistence.md` | Consequences | Accepted | Raw filesystem paths must not be exposed through public API contracts. |
| ADR-006 | `docs/adrs/006-use-root-relative-keys-for-temporary-local-storage.md` | Decision, Consequences, Task Impact | Accepted / Resolved by ADR | Accepts root-relative keys for temporary local storage and resolves the task 006 storage contract blocker. |
| Task 004 execution report | `docs/task-executions/mvp-media-utility/004-create-operation-domain-model-execution.md` | Implemented Changes | Confirmed by source document | Confirms `ResultFileMetadata` stores file name, content type, size, and internal path. |
| Task 005 execution report | `docs/task-executions/mvp-media-utility/005-create-operation-events-model-execution.md` | Execution Summary | Confirmed by source document | Confirms no storage behavior was added in task 005. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/storage/` | Storage boundary | Detected in codebase | `StorageBoundary` and package documentation exist, but no storage service behavior exists yet. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/operations/ResultFileMetadata.java` | Result file metadata | Detected in codebase | Existing metadata has `internalPath`. |
| Current codebase | `src/main/resources/application.properties` | Configuration naming pattern | Detected in codebase | Existing environment variables use `MEDIA_UTILITY_*` names. |
| Tech Spec | `docs/specs/tech-spec.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |
| PRD | `docs/product/prd.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |
| Technology definition | `docs/architecture/technology-definition.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |
| User decision | Current `plan-task` session | Internal location shape | Confirmed by user | Use a root-relative key for `ResultFileMetadata.internalPath`. |
| User decision | Current `plan-task` session | Storage root configuration naming | Confirmed by user | Use Spring property `media-utility.storage.root` and env var `MEDIA_UTILITY_STORAGE_ROOT`. |
| User decision | Current `plan-task` session | Missing-file delete behavior | Confirmed by user | Treat deletion of an already-missing file as idempotent success. |
| User decision | Current `resolve-architecture-blocker` session | ADR acceptance and unblock status | Confirmed by user | Accept ADR-006 and mark task 006 ready after saving all unblock artifacts. |

## Context Summary

The MVP stores generated or downloaded media files as temporary local files while persisting operation metadata in MySQL/JPA. The current codebase already has backend package boundaries, a JPA operation model, and `ResultFileMetadata.internalPath` for server-side result file location data.

Task 006 creates the storage service contract and local filesystem implementation needed by later conversion, URL download, result download, and cleanup tasks. ADR-006 resolves the root-relative temporary storage contract blocker by accepting local filesystem storage under a configured root and persisted root-relative storage keys.

## Task Goal

Create a temporary local storage service that writes, resolves, and deletes server-side media result files under a configured storage root without exposing raw filesystem paths through public API contracts.

## Confirmed Scope

- Create a storage service contract inside the existing `storage` package.
- Create a local filesystem implementation for temporary media files.
- Store files under a configured storage root.
- Use root-relative keys in `ResultFileMetadata.internalPath`.
- Resolve root-relative keys to absolute filesystem paths only inside backend storage code.
- Delete temporary files by root-relative key.
- Treat missing-file deletion as idempotent success.
- Keep raw filesystem paths server-side only.
- Add storage tests with temporary directories.

## Out of Scope

- Do not implement REST endpoints or public download URLs.
- Do not expose raw filesystem paths through API DTOs.
- Do not implement FFmpeg or yt-dlp process execution.
- Do not implement conversion or URL download business flows.
- Do not implement scheduled cleanup or expired-file scanning.
- Do not choose or hardcode a retention window in this task.
- Do not store media bytes in MySQL.
- Do not change operation event emission or analytics behavior.
- Do not update PRD, project planning, technology definition, Tech Spec, final ADRs, task files, or unrelated documents during task implementation.

## Requirements Covered

| Requirement | Source | How This Task Covers It | Status |
| --- | --- | --- | --- |
| Completed files are downloadable immediately and removed later | Project planning | Storage service provides the backend filesystem foundation for later download and cleanup tasks. | Ready |
| Media bytes stay outside MySQL | Task 003, task plan 003 | Storage service writes media bytes to local disk, not database BLOBs. | Confirmed |
| Result metadata can include internal server-side location | Task plan 004, ADR-005, ADR-006, current codebase | Storage result metadata uses `ResultFileMetadata.internalPath` for a root-relative key. | Resolved by ADR |
| Raw filesystem paths are not public API contracts | ADR-005, ADR-006, task file | Storage contract keeps absolute paths internal and persists only root-relative keys. | Resolved by ADR |
| Storage package owns temporary storage behavior | Task plan 002, current codebase | Implementation will stay under the existing `storage` boundary. | Confirmed |

## Technical Specification Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Not available | Missing | Task cannot rely on Tech Spec details because `docs/specs/tech-spec.md` is empty. | Source limitation remains documented. ADR-006 resolves the storage contract gap for task 006. |

Coverage assessment:

- Justifying Tech Spec section: unavailable because the Tech Spec file is empty.
- Tech Spec sections implemented by this task: none directly from Tech Spec content.
- Gaps between task and Tech Spec: storage service contract, local filesystem behavior, storage root configuration, and internal path semantics are not documented in Tech Spec.
- Dependencies not specified by the Tech Spec: root-relative storage-key contract, storage root config names, and delete idempotency are resolved by user decisions and ADR-006.
- Source limitation handling: the empty Tech Spec remains a documentation limitation but no longer blocks task 006 because ADR-006 accepts the storage contract.

## Architecture and ADR Considerations

| Decision or ADR | Source | Impact on This Task | Status |
| --- | --- | --- | --- |
| Java 21 Spring Boot modular monolith | ADR-001 | Storage service belongs inside the existing Spring Boot app and package boundaries. | Accepted |
| Maven/npm coordinated asset packaging | ADR-003 | Storage changes must preserve existing Maven/npm packaging. | Accepted |
| Shared operation metadata model | ADR-005 | Result metadata may include internal file location data, but raw paths must not be public API contracts. | Accepted |
| Root-relative temporary local storage contract | ADR-006 | `ResultFileMetadata.internalPath` stores a root-relative key; absolute paths are resolved only inside storage code. | Resolved by ADR |
| Local temporary files outside MySQL | Task 003, task plan 003, ADR-006 | Media bytes must be stored on disk, while metadata remains in MySQL/JPA. | Confirmed |
| Storage root config naming | User decision, ADR-006 | Use `media-utility.storage.root` backed by `MEDIA_UTILITY_STORAGE_ROOT`. | Confirmed |
| Missing-file delete idempotency | User decision, ADR-006 | Delete should succeed when the file is already missing. | Confirmed |
| Retention window | Project planning, task file, ADR-006 | Must not be hardcoded here; cleanup task owns retention policy. | Deferred out of this task |

ADR candidates or architecture decisions needed:

- None. ADR-006 resolves the required storage contract ADR blocker.
- No architecture blocker remains for task 006.

Architecture decision notes:

- Saved separately: Yes
- Path: `docs/architecture/task-decisions/mvp-media-utility/006-create-temporary-storage-service-architecture-decisions.md`
- Notes file status: Updated

## Confirmed Decisions

- The task target is `docs/tasks/mvp-media-utility/006-create-temporary-storage-service.md`.
- The storage package boundary already exists.
- The current operation model has `ResultFileMetadata.internalPath`.
- Media bytes must remain outside MySQL.
- No public API contract should expose raw filesystem paths.
- The persisted internal location should be a root-relative key.
- The storage root configuration should use `media-utility.storage.root` and `MEDIA_UTILITY_STORAGE_ROOT`.
- Deleting an already-missing file should be idempotent success.
- Retention duration is out of scope for this task.
- ADR-006 accepts the root-relative temporary storage contract and resolves the task 006 architecture blocker.

## Pending Decisions

None. All task-relevant decisions have been answered, resolved by ADR-006, or explicitly deferred out of this task.

## Questions for the User

None. All task-relevant questions have been answered.

## Proposed Implementation Approach

1. Re-read the task file, this plan, the architecture decision notes, ADR-005, ADR-006, and current storage/operation code.
2. Inspect the existing `storage` package and preserve package-boundary conventions.
3. Define a storage service contract in `storage` for storing or reserving operation result files, resolving root-relative keys for backend use, and deleting stored files.
4. Implement local filesystem storage under `media-utility.storage.root` / `MEDIA_UTILITY_STORAGE_ROOT`.
5. Persist or return `ResultFileMetadata` compatible metadata where `internalPath` is a root-relative key.
6. Normalize and validate all keys and paths so storage operations cannot escape the configured root.
7. Make missing-file deletion idempotent success while still rejecting invalid or outside-root keys.
8. Keep retention windows, public download URLs, REST endpoints, process execution, conversion/download flows, cleanup scheduling, and database BLOB storage out of scope.

## Files and Areas Expected to Change

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasdourado/mediautility/storage/` | Create / Modify | Task file, ADR-006 | Storage service contract, local implementation, and supporting value types if needed. |
| `src/main/resources/application.properties` | Modify | ADR-006 | Add `media-utility.storage.root=${MEDIA_UTILITY_STORAGE_ROOT}` or equivalent Spring placeholder style. |
| `src/test/java/com/lucasdourado/mediautility/storage/` | Create | Task validation, ADR-006 | Unit tests with temporary directories for root enforcement, metadata, resolve, and delete behavior. |
| `src/main/java/com/lucasdourado/mediautility/operations/ResultFileMetadata.java` | Inspect / Possibly modify | Current codebase, ADR-005, ADR-006 | Modify only if needed for metadata compatibility; preserve server-side-only internal location semantics. |

## Step-by-Step Implementation Plan

1. Verify ADR-006 exists and is accepted before source edits.
2. Inspect current `storage`, `operations`, and `application.properties` files.
3. Add storage root configuration using `media-utility.storage.root` backed by `MEDIA_UTILITY_STORAGE_ROOT`.
4. Create the storage service contract and any minimal value types needed for result storage, resolution, and delete operations.
5. Implement a local filesystem storage service that creates parent directories and stores files under the configured root.
6. Generate or reserve operation-scoped root-relative keys for result files without using absolute paths in persisted metadata.
7. Resolve root-relative keys to absolute filesystem paths only inside storage code after normalization and root containment checks.
8. Implement idempotent delete for missing files while rejecting invalid keys and outside-root attempts.
9. Return or populate `ResultFileMetadata` with file name, content type, size, and root-relative internal key.
10. Add focused unit tests with temporary directories for root enforcement, traversal rejection, metadata creation, resolve, and delete behavior.
11. Run the repository's established Maven test command and perform scope review.

## Validation Strategy

- Run Maven tests using the repository's established Maven validation path.
- Verify files are stored only under the configured root.
- Verify path traversal and outside-root keys are rejected or prevented for store, resolve, and delete paths.
- Verify `ResultFileMetadata.internalPath` contains a root-relative key, not an absolute path.
- Verify delete removes existing files and treats already-missing files as success.
- Verify no REST endpoints, public download URLs, process adapters, conversion/download flows, cleanup scheduler, retention hardcoding, or database BLOB storage are added.

## Tests to Add or Update

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| Storage root enforcement | Unit | Verify writes and resolves remain under the configured root. | Use temporary directories. |
| Path traversal rejection | Unit | Verify traversal or outside-root keys cannot escape the storage root. | Cover store, resolve, and delete inputs. |
| Result metadata creation | Unit | Verify file name, content type, size, and root-relative internal key are produced. | Compatible with `ResultFileMetadata`. |
| Delete behavior | Unit | Verify deleting an existing file removes it and deleting a missing file succeeds idempotently. | Confirm outside-root keys are still rejected. |
| Maven validation | Build validation | Verify backend compiles and tests pass. | Use the repository's established Maven command. |
| Scope review | Manual | Verify no endpoint, process adapter, cleanup scheduler, public API DTO, retention hardcoding, or DB BLOB behavior was added. | Required after implementation. |

## Acceptance Criteria

- [x] Required formal ADR for the root-relative temporary storage contract is accepted.
- [x] Blocked task plan is updated through `resolve-architecture-blocker` after ADR acceptance.
- [ ] A temporary storage service contract exists in the `storage` package.
- [ ] A local filesystem implementation stores temporary media files under a configured root.
- [ ] Storage operations prevent writes, reads, resolves, and deletes outside the configured root.
- [ ] Stored file metadata is compatible with `ResultFileMetadata`.
- [ ] `ResultFileMetadata.internalPath` stores a root-relative key, not an absolute path.
- [ ] Raw filesystem paths remain server-side only and are not exposed through public API contracts.
- [ ] Delete behavior exists and missing-file deletion is idempotent success.
- [ ] No media file bytes are stored in MySQL.
- [ ] No REST endpoint, FFmpeg/yt-dlp adapter, conversion/download flow, scheduled cleanup job, or retention hardcoding is implemented.

## Risks and Edge Cases

- Root-relative keys must not allow traversal, symlinks, or normalization edge cases to escape the storage root.
- Persisting absolute paths would expose deployment details in metadata and is rejected by ADR-006.
- Retention duration remains out of scope and must not be invented in this task.
- Later result-download and cleanup tasks depend on the storage contract and may require compatible method shapes.
- Empty Tech Spec, PRD, and technology-definition files remain documentation risks.
- Existing `docs/tasks/mvp-media-utility/006-create-temporary-storage-service.md` is modified in the working tree and should be treated as user-owned source context.

## Rollback or Recovery Notes

After implementation, rollback would remove the storage service contract, local filesystem implementation, storage configuration, supporting storage value types, and storage tests while preserving ADR-006, the operation model, persistence baseline, and package boundaries.

If implementation discovers that root-relative keys cannot satisfy later result-download or cleanup needs, record a follow-up architecture issue instead of silently changing the storage contract.

## Documentation Updates

- Do not update PRD, project planning, technology definition, Tech Spec, final ADR files, task files, or unrelated documents during task implementation.
- ADR-006 is already accepted and should be treated as binding source context.
- The future task execution report should document implemented storage contract shape, config key, validation evidence, and any deviations.

## Implementation Readiness Checklist

- [x] Task scope is clear.
- [x] Source documents were reviewed.
- [x] Tech Spec coverage was verified as unavailable and documented.
- [x] Architecture decisions were checked.
- [x] ADR candidates were confirmed, rejected, required-before-implementation, or explicitly deferred by the user.
- [x] Pending decisions are resolved or explicitly deferred out of this task by the user.
- [x] User questions were answered, or the plan is explicitly marked ready.
- [x] Expected files or areas are identified.
- [x] Acceptance criteria are defined.
- [x] Test strategy is defined.
- [x] Risks and edge cases are documented.

## Notes for the Implementing Agent

- ADR-006 is the binding source for temporary storage key semantics.
- Use root-relative keys in persisted metadata and resolve absolute paths only inside storage code.
- Reject or prevent traversal and outside-root access.
- Keep delete idempotent for missing files.
- Do not implement retention, result download endpoints, media processing, cleanup scheduling, or public API contracts in task 006.
