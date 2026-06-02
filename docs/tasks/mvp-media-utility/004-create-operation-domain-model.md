# Task: Create Operation Domain Model

## Status

Status: Depends on Previous Task

Last updated: 2026-06-01

## Task ID

ID: MVP-MEDIA-004

Order: 004

Task file: `docs/tasks/mvp-media-utility/004-create-operation-domain-model.md`

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Tech Spec | `docs/specs/tech-spec.md` | Data Model | Confirmed by source document | Defines Operation and result file metadata. |
| PRD | `docs/product/prd.md` | Functional Requirements | Confirmed by source document | Requires completed and failed operation tracking. |
| Technology definition | `docs/architecture/technology-definition.md` | Confirmed Technology Decisions | Confirmed by source document | MySQL/JPA and 1-hour retention are confirmed. |

## Context

Both conversion and URL download flows need operation metadata, status, result metadata, expiration, and failure details.

## Goal

Create the core operation and result metadata model.

## Scope

- Create an operation type model for conversion and URL download.
- Create operation statuses for lifecycle tracking.
- Create persistent operation metadata with timestamps and failure reason.
- Create result file metadata without exposing raw paths to clients.
- Add JPA repository boundaries as needed.

## Out of Scope

- Do not implement process execution.
- Do not implement endpoint behavior.
- Do not add operation events unless executing the dedicated event task.

## Implementation Instructions

- Store `expiresAt` for successful result availability.
- Keep internal file paths server-side only.
- Model failed operations separately from completed ones.
- Avoid storing unnecessary user media data.

## Expected Files

| Path | Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/.../operations/` | Create / Modify | Tech Spec | Domain types and lifecycle model. |
| `src/main/java/.../persistence/` | Create / Modify | Tech Spec | JPA entities/repositories if using separate persistence types. |

## Dependencies

| Dependency | Type | Status | Notes |
| --- | --- | --- | --- |
| MVP-MEDIA-003 | Previous task | Pending | Requires persistence baseline. |

## Validation

- Backend compiles.
- Repository or persistence tests verify model mapping when test infrastructure exists.

## Acceptance Criteria

- [ ] Operation type and status are represented.
- [ ] Operation metadata includes creation, completion, expiration, and failure fields.
- [ ] Result metadata does not expose filesystem paths through public contracts.

## Risks

- Entity design may need adjustment after API contracts are implemented.

## Open Questions

- Whether anonymous result downloads require unguessable result tokens is called out in the Tech Spec and should be considered during API/result endpoint tasks.

## Notes for the Implementing Agent

- Keep domain concepts reusable by both media flows.
