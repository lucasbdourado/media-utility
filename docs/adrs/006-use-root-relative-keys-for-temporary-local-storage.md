# ADR-006: Use Root-Relative Keys for Temporary Local Storage

## Status

Status: Accepted

Date: 2026-06-02

## Context

MVP-MEDIA-006 is blocked because the temporary storage service will define a long-lived contract used by conversion, URL download, result download, and cleanup tasks. The current operation model already includes `ResultFileMetadata.internalPath`, and ADR-005 allows internal server-side file location data while prohibiting raw filesystem paths from being exposed through public API contracts.

The task file requires local filesystem storage under a configured storage root, prevention of outside-root access, metadata compatibility with `ResultFileMetadata`, and no database BLOB storage. The Tech Spec, PRD, and technology definition files are empty in the current workspace, so the task-specific storage contract must be accepted explicitly before implementation.

During the unblock session, the user confirmed that operation result metadata should persist only root-relative storage keys, while absolute filesystem paths should be resolved exclusively inside the backend storage service or infrastructure code.

## Decision

The MVP temporary storage contract will use local filesystem storage under a configured storage root and persist root-relative storage keys in operation result metadata.

`ResultFileMetadata.internalPath` must store a key relative to the configured storage root, not an absolute filesystem path. Absolute filesystem paths may be resolved only inside backend storage service or infrastructure code.

The storage root must be configured through Spring property `media-utility.storage.root`, backed by environment variable `MEDIA_UTILITY_STORAGE_ROOT`.

Public API contracts must not expose absolute local filesystem paths. If a future storage backend such as S3 or another remote store becomes necessary, this decision may evolve toward opaque storage IDs, but that is outside the scope of MVP-MEDIA-006.

## Considered Options

| Option | Summary | Trade-offs | Decision |
| --- | --- | --- | --- |
| Root-relative storage keys | Persist keys relative to the configured local storage root and resolve absolute paths only inside storage code. | Keeps persisted metadata portable across deployments, avoids public exposure of local paths, and remains simple for local filesystem storage. Requires careful path normalization and outside-root enforcement. | Accepted |
| Absolute filesystem paths | Persist the full server filesystem path in operation metadata. | Simpler to resolve, but exposes deployment-specific infrastructure details in persistent metadata and makes the contract depend on the local environment. | Rejected |
| Opaque storage IDs | Persist non-path identifiers and resolve them through a separate storage mapping or lookup layer. | Better abstraction for future local/S3/multi-backend storage, but requires an additional mapping/lookup design outside task 006. | Deferred for future evolution |

## Consequences

- Task 006 can implement a local filesystem storage service without storing media bytes in MySQL.
- Operation result metadata must persist root-relative keys, not absolute paths.
- Storage code must normalize and validate keys so writes, reads, resolves, and deletes cannot escape the configured root.
- Storage code must reject or prevent path traversal and outside-root access.
- Missing-file delete behavior should be idempotent success for cleanup and retry compatibility.
- Task 006 must not hardcode a retention window; retention remains owned by later cleanup planning and implementation.
- Future public result-download contracts must translate operation/result state into safe public responses without exposing absolute local filesystem paths.
- Future remote storage support may require revisiting this ADR and moving from root-relative keys to opaque storage identifiers.

## Task Impact

- Related task plan: `docs/task-plans/mvp-media-utility/006-create-temporary-storage-service-plan.md`
- Related architecture decision notes: `docs/architecture/task-decisions/mvp-media-utility/006-create-temporary-storage-service-architecture-decisions.md`
- Unlock effect: Resolves the required root-relative temporary storage contract blocker for MVP-MEDIA-006.
- Remaining blockers: None for MVP-MEDIA-006 after the task plan and architecture decision notes are updated to reference this ADR.

## Source References

- `docs/tasks/mvp-media-utility/006-create-temporary-storage-service.md`: Scope, implementation instructions, validation, acceptance criteria, and storage-contract open questions.
- `docs/task-plans/mvp-media-utility/006-create-temporary-storage-service-plan.md`: Blocked plan and pending ADR prerequisite.
- `docs/architecture/task-decisions/mvp-media-utility/006-create-temporary-storage-service-architecture-decisions.md`: Blocked architecture decision notes and ADR candidate.
- `docs/adrs/005-use-shared-operation-domain-model-for-metadata-persistence.md`: Accepted rule that raw filesystem paths must not be exposed through public API contracts.
- Current codebase: `src/main/java/com/lucasdourado/mediautility/operations/ResultFileMetadata.java` and `src/main/resources/application.properties`.
- User confirmation: current `resolve-architecture-blocker` session accepted root-relative keys, rejected absolute paths, deferred opaque IDs to future storage evolution, confirmed security/compatibility constraints, and chose `Ready for Implementation` after saving the unblock artifacts.
