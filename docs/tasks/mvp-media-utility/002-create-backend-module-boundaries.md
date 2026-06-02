# Task: Create Backend Module Boundaries

## Status

Status: Depends on Previous Task

Last updated: 2026-06-01

## Task ID

ID: MVP-MEDIA-002

Order: 002

Task file: `docs/tasks/mvp-media-utility/002-create-backend-module-boundaries.md`

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Tech Spec | `docs/specs/tech-spec.md` | Modules and Responsibilities | Confirmed by source document | Defines API, operation, media, storage, persistence, cleanup, and observability responsibilities. |
| Technology definition | `docs/architecture/technology-definition.md` | Confirmed Technology Decisions | Confirmed by source document | Modular monolith is confirmed. |

## Context

The backend should stay a modular monolith with clear internal boundaries. Request handlers must not call FFmpeg or yt-dlp directly.

## Goal

Create initial backend package or module boundaries for the MVP responsibilities.

## Scope

- Establish backend structure for API, operations, conversion, URL download, process execution, temporary storage, persistence, cleanup, and observability.
- Add placeholder classes or package markers only where useful.
- Document boundary expectations in code structure or concise comments.

## Out of Scope

- Do not implement business logic.
- Do not create database schema beyond what later persistence tasks require.
- Do not implement adapters or controllers.

## Implementation Instructions

- Keep API/controllers thin.
- Keep media processing behind service interfaces.
- Keep external process execution isolated from request handling.
- Keep storage concerns separate from operation orchestration.

## Expected Files

| Path | Action | Source | Notes |
| --- | --- | --- | --- |
| `src/main/java/.../api/` | Create | Tech Spec | REST boundary. |
| `src/main/java/.../operations/` | Create | Tech Spec | Operation lifecycle. |
| `src/main/java/.../media/` | Create | Tech Spec | Conversion and download abstractions. |
| `src/main/java/.../storage/` | Create | Tech Spec | Temporary local file storage. |
| `src/main/java/.../persistence/` | Create | Tech Spec | JPA repositories and entities. |

## Dependencies

| Dependency | Type | Status | Notes |
| --- | --- | --- | --- |
| MVP-MEDIA-001 | Previous task | Pending | Requires scaffolded backend. |

## Validation

- Backend still compiles.
- Package boundaries are visible and aligned with the Tech Spec responsibilities.

## Acceptance Criteria

- [ ] Backend contains clear package/module boundaries for MVP areas.
- [ ] No media processing logic exists in API packages.
- [ ] Application still builds.

## Risks

- Over-structuring before implementation can add unnecessary complexity.

## Open Questions

- Exact Java base package name must be chosen during scaffold implementation if not already present.

## Notes for the Implementing Agent

- Use the smallest structure that keeps boundaries clear.
