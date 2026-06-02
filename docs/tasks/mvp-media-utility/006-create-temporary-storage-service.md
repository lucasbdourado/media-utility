# Task: Create Temporary Storage Service

## Status

Status: Needs Clarification

Last updated: 2026-06-02

## Task ID

ID: MVP-MEDIA-006

Order: 006

Task file: `docs/tasks/mvp-media-utility/006-create-temporary-storage-service.md`

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project planning | `docs/planning/project-planning.md` | Result Delivery and Temporary File Lifecycle | Confirmed by source document | Requires completed files to be downloadable immediately and removed later. |
| Task 003 | `docs/tasks/mvp-media-utility/003-configure-mysql-jpa-persistence-baseline.md` | Context, Scope, Acceptance Criteria | Confirmed by source document | Confirms media files remain temporary local files and must not be database blobs. |
| Task plan 002 | `docs/task-plans/mvp-media-utility/002-create-backend-module-boundaries-plan.md` | Package layout and boundaries | Confirmed by source document | Confirms `storage` package boundary exists for temporary storage. |
| Task plan 004 | `docs/task-plans/mvp-media-utility/004-create-operation-domain-model-plan.md` | Result metadata and out-of-scope behavior | Confirmed by source document | Confirms operation result metadata includes internal server-side file location data. |
| ADR-005 | `docs/adrs/005-use-shared-operation-domain-model-for-metadata-persistence.md` | Consequences | Accepted | Confirms result metadata may include internal file location data and raw paths must not be exposed through public API contracts. |
| Task 005 execution report | `docs/task-executions/mvp-media-utility/005-create-operation-events-model-execution.md` | Current codebase evidence | Confirmed by source document | Confirms task 005 is complete and no storage behavior was added. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/storage/` | Storage boundary | Detected in codebase | `StorageBoundary` and package documentation exist, but no storage service behavior exists yet. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/operations/ResultFileMetadata.java` | Result file metadata | Detected in codebase | Existing metadata stores file name, content type, size, and internal path. |
| Tech Spec | `docs/specs/tech-spec.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |
| PRD | `docs/product/prd.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |
| Technology definition | `docs/architecture/technology-definition.md` | Not available | Documented limitation | File exists but is empty in the current workspace. |

## Context

The MVP stores generated or downloaded media as temporary local files. Operation metadata is persisted in MySQL/JPA, but media bytes must remain outside the database. The `storage` package boundary already exists, and the operation model already has `ResultFileMetadata` with an internal path field.

This task creates the backend storage service abstraction and local filesystem implementation needed by later conversion, URL download, result download, and cleanup tasks.

## Goal

Create a temporary local storage service that writes, resolves, and deletes server-side media result files without exposing raw filesystem paths through public contracts.

## Scope

- Create a storage service contract inside the existing `storage` package.
- Create a local filesystem implementation for temporary media files.
- Ensure files are stored under a configured storage root.
- Provide behavior to create or reserve operation-scoped result paths.
- Provide behavior to resolve an internal path for server-side download or processing.
- Provide behavior to delete stored temporary files.
- Return or populate storage metadata compatible with the existing `ResultFileMetadata` model.
- Keep raw filesystem paths server-side only.

## Out of Scope

- Do not implement REST endpoints or public download URLs.
- Do not expose raw filesystem paths through API DTOs.
- Do not implement FFmpeg or yt-dlp process execution.
- Do not implement conversion or URL download business flows.
- Do not implement the scheduled cleanup job; task 019 owns cleanup scheduling and expired-file scanning.
- Do not store media bytes in MySQL.
- Do not change operation event emission or analytics behavior.
- Do not update PRD, project planning, technology definition, Tech Spec, ADRs, or unrelated task files.

## Implementation Instructions

- Keep storage behavior under `src/main/java/com/lucasdourado/mediautility/storage/`.
- Use local filesystem storage only.
- Keep persisted operation metadata in MySQL/JPA and temporary media bytes on disk.
- Use internal server-side paths or identifiers only inside backend code and persisted metadata.
- Prevent storage operations from writing outside the configured storage root.
- Create parent directories when storing result files.
- Support deleting a stored file when given its internal server-side location.
- Design the service so later tasks can use it from conversion, URL download, result download, and cleanup flows.
- Do not choose or hardcode the final public download contract in this task.

## Expected Files

| Path | Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/com/lucasdourado/mediautility/storage/` | Create / Modify | Task plan 002, current codebase | Storage service contract, local implementation, and supporting value types if needed. |
| `src/main/resources/application.properties` | Modify | Current codebase, open question | Add storage root configuration only after the property/env naming is confirmed during task planning. |
| `src/test/java/com/lucasdourado/mediautility/storage/` | Create | Validation | Unit tests for storage-root enforcement, metadata creation, resolve, and delete behavior. |
| `src/main/java/com/lucasdourado/mediautility/operations/ResultFileMetadata.java` | Inspect / Possibly modify | Current codebase, ADR-005 | Modify only if storage metadata requires a source-backed field adjustment. |

## Dependencies

| Dependency | Type | Status | Notes |
| --- | --- | --- | --- |
| MVP-MEDIA-002 | Previous task | Completed | Storage package boundary exists. |
| MVP-MEDIA-003 | Previous task | Completed | Persistence baseline exists; media bytes must stay outside MySQL. |
| MVP-MEDIA-004 | Previous task | Completed | Operation and result metadata model exists. |
| MVP-MEDIA-005 | Previous task | Completed | Operation event model exists; storage behavior remains unimplemented. |
| Storage root configuration naming | Technology decision | Pending | Exact property and environment variable names are not confirmed in source documents. |
| Temporary retention window | Product / operational decision | Pending | Project planning says retention policy is pending; cleanup task depends on it more directly than this storage-service task. |

## Validation

- Backend compiles.
- Storage service tests verify files are written under the configured root.
- Tests verify path traversal or outside-root writes are rejected or prevented.
- Tests verify stored result metadata includes file name, content type, size, and internal server-side location.
- Tests verify a stored file can be resolved for backend use without creating a public URL.
- Tests verify delete behavior removes the stored file and handles missing files predictably.
- Scope review verifies no endpoints, media processing adapters, scheduled cleanup job, public API contracts, or database BLOB storage were added.

## Acceptance Criteria

- [ ] A temporary storage service contract exists in the `storage` package.
- [ ] A local filesystem implementation stores temporary media files under a configured root.
- [ ] Storage operations prevent writes or reads outside the configured root.
- [ ] Stored file metadata is compatible with `ResultFileMetadata`.
- [ ] Raw filesystem paths remain server-side only and are not exposed through public API contracts.
- [ ] Delete behavior exists for stored temporary files.
- [ ] No media file bytes are stored in MySQL.
- [ ] No REST endpoint, FFmpeg/yt-dlp adapter, conversion/download flow, or scheduled cleanup job is implemented.

## Risks

- Exact storage root property/env naming is not confirmed by source documents.
- Retention duration is still pending and must not be invented in this task.
- Internal path handling must avoid path traversal and accidental deletion outside the storage root.
- Later result-download and cleanup tasks may require small service-contract adjustments.
- Empty Tech Spec, PRD, and technology-definition files remain documentation risks.

## Open Questions

- What exact configuration property and environment variable should define the temporary storage root?
- Should the internal stored location be an absolute path, a root-relative path, or an opaque generated storage key?
- Should missing-file delete behavior be idempotent success or a reported storage miss?
- What retention window should cleanup use? This is mainly blocking task 019, but storage should avoid hardcoding any retention duration.

## Notes for the Implementing Agent

- This task should be planned with `plan-task` before implementation because it has open storage-contract decisions.
- Keep filesystem paths internal to backend code and persistence metadata.
- Do not implement public download behavior; task 018 owns result download endpoint behavior.
- Do not implement cleanup scheduling; task 019 owns expired temporary file cleanup.
- Use focused filesystem tests with temporary directories instead of relying on a real deployed storage path.
