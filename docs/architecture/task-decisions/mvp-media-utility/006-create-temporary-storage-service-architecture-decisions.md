# Task Architecture Decision Notes: Create Temporary Storage Service

## Status

Status: Ready for Implementation

Last updated: 2026-06-02

Decision notes file: `docs/architecture/task-decisions/mvp-media-utility/006-create-temporary-storage-service-architecture-decisions.md`

Task plan file: `docs/task-plans/mvp-media-utility/006-create-temporary-storage-service-plan.md`

Task file: `docs/tasks/mvp-media-utility/006-create-temporary-storage-service.md`

## Purpose

Record task-specific architecture decisions, conflicts, and confirmed ADR candidates identified while preparing the task implementation plan.

This file is planning support only. It is not a final ADR and must not replace the ADR workflow.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/tasks/mvp-media-utility/006-create-temporary-storage-service.md` | Scope, Implementation Instructions, Open Questions | Confirmed by source document | Defines temporary local storage service needs and storage-contract questions. |
| Project planning | `docs/planning/project-planning.md` | Result Delivery and Temporary File Lifecycle | Confirmed by source document | Requires immediate result downloads and later temporary cleanup. |
| Task plan 002 | `docs/task-plans/mvp-media-utility/002-create-backend-module-boundaries-plan.md` | Package layout | Confirmed by source document | Confirms `storage` package boundary. |
| Task plan 003 | `docs/task-plans/mvp-media-utility/003-configure-mysql-jpa-persistence-baseline-plan.md` | Out of Scope | Confirmed by source document | Confirms media bytes are not stored in MySQL. |
| Task plan 004 | `docs/task-plans/mvp-media-utility/004-create-operation-domain-model-plan.md` | Architecture and ADR Considerations | Confirmed by source document | Confirms internal file location data is server-side metadata. |
| ADR-005 | `docs/adrs/005-use-shared-operation-domain-model-for-metadata-persistence.md` | Consequences | Accepted | Allows internal file location data but prohibits exposing raw paths through public API contracts. |
| ADR-006 | `docs/adrs/006-use-root-relative-keys-for-temporary-local-storage.md` | Decision, Consequences, Task Impact | Accepted / Resolved by ADR | Resolves the root-relative temporary storage contract blocker. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/storage/` | Storage boundary | Detected in codebase | Storage boundary exists without behavior. |
| Current codebase | `src/main/java/com/lucasdourado/mediautility/operations/ResultFileMetadata.java` | Result file metadata | Detected in codebase | `internalPath` exists as a server-side metadata field. |
| Current codebase | `src/main/resources/application.properties` | Env config style | Detected in codebase | Existing env variables use `MEDIA_UTILITY_*`. |
| User decision | Current `plan-task` session | Internal location shape | Confirmed by user | Use root-relative keys for persisted internal storage locations. |
| User decision | Current `plan-task` session | Storage root configuration naming | Confirmed by user | Use `media-utility.storage.root` and `MEDIA_UTILITY_STORAGE_ROOT`. |
| User decision | Current `plan-task` session | Missing-file delete behavior | Confirmed by user | Missing-file deletion is idempotent success. |
| User decision | Current `resolve-architecture-blocker` session | ADR acceptance and unblock status | Confirmed by user | Accept ADR-006 and mark task 006 ready after saving all unblock artifacts. |

## Confirmed Architecture Decisions

| Decision | Source | Impact on This Task | Notes |
| --- | --- | --- | --- |
| Temporary media bytes stay on local disk, outside MySQL | Task file, task plan 003 | Storage implementation must write files to filesystem storage and keep database persistence metadata-only. | Confirmed by source documents. |
| Raw filesystem paths must not be exposed through public API contracts | ADR-005, ADR-006, task file | Storage must not force future API DTOs to expose absolute paths. | Accepted ADR constraint. |
| Persist root-relative storage keys instead of absolute paths | ADR-006, user decision | `ResultFileMetadata.internalPath` should store a root-relative key. | Resolved by ADR-006. |
| Resolve absolute filesystem paths only inside backend storage code | ADR-006, user decision | Keeps deployment-specific paths server-side and local to storage behavior. | Resolved by ADR-006. |
| Use namespaced storage root config | ADR-006, user decision, current config style | Storage root property should be `media-utility.storage.root` backed by `MEDIA_UTILITY_STORAGE_ROOT`. | Confirmed by user and ADR. |
| Missing-file delete is idempotent success | ADR-006, user decision | Supports later cleanup and retry behavior without failing on already-removed files. | Confirmed by user and ADR. |
| Retention duration is not part of task 006 | Task file, project planning, ADR-006 | Storage service must not hardcode cleanup timing. | Cleanup policy remains for later task 019. |

## Pending Architecture Decisions

None. All task-relevant architecture decisions have been answered or resolved by ADR-006.

## Architecture Conflicts

| Conflict | Sources | Impact | Resolution Status |
| --- | --- | --- | --- |
| Tech Spec, PRD, and technology definition files are empty, but the storage task needs a long-lived storage contract. | Empty source files vs task 006 and current codebase | Source documents do not formally define storage-key semantics. | Resolved for task 006 by ADR-006 and documented as a source limitation. |
| Task file says internal server-side paths or identifiers, while user chose root-relative keys. | Task file open wording vs user decision | Root-relative key is the selected contract. | Resolved by ADR-006. |
| Retention policy remains open, but storage service needs delete behavior. | Project planning, task file, ADR-006 | Storage can define delete semantics without choosing cleanup timing. | Resolved for task 006; retention deferred to cleanup task. |

## ADR Candidates

| Candidate | Trigger | Suggested ADR Scope | Blocking for This Task? |
| --- | --- | --- | --- |
| Root-relative temporary storage contract | Task 006 defines storage used by conversion, URL download, result download, and cleanup tasks. | Covered by ADR-006. | No; resolved by accepted ADR. |

## Implementation Impact

- Task 006 can proceed after the updated ready task plan is saved.
- ADR-006 binds the implementation to local filesystem storage under `MEDIA_UTILITY_STORAGE_ROOT` with root-relative persisted keys.
- Absolute paths must remain internal to storage code and must not appear in public API contracts.
- Storage operations must enforce root containment and traversal protection.
- Missing-file delete must be idempotent success.
- Retention hardcoding remains out of scope.

## Questions for the User

None. All task-relevant architecture questions have been answered.

## Notes for the Implementing Agent

- This file is planning support only; it is not a final ADR.
- Use ADR-006 as the binding storage-contract source.
- Execute from the updated task plan, not from the prior blocked snapshot.
- Do not infer absolute-path, opaque-ID, retention, or public-download behavior beyond ADR-006 and the ready plan.
