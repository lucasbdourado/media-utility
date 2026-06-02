# Task: Configure MySQL JPA Persistence Baseline

## Status

Status: Depends on Previous Task

Last updated: 2026-06-01

## Task ID

ID: MVP-MEDIA-003

Order: 003

Task file: `docs/tasks/mvp-media-utility/003-configure-mysql-jpa-persistence-baseline.md`

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Technology definition | `docs/architecture/technology-definition.md` | Confirmed Technology Decisions | Confirmed by source document | MySQL and JPA are confirmed. |
| Tech Spec | `docs/specs/tech-spec.md` | Data Model, Integrations | Confirmed by source document | MySQL stores operation metadata and events. |

## Context

Operation metadata must be stored in MySQL using JPA. Media files must remain temporary local files, not database blobs.

## Goal

Configure the backend persistence baseline for MySQL and JPA.

## Scope

- Add required Spring Data JPA and MySQL dependencies.
- Configure environment-driven datasource settings.
- Add baseline persistence configuration.
- Prepare repository package scanning if needed.

## Out of Scope

- Do not implement operation entities in this task.
- Do not add Docker Compose unless executing the dedicated local development task.
- Do not store media file bytes in MySQL.

## Implementation Instructions

- Use Spring Data JPA.
- Keep database connection settings configurable.
- Avoid committing secrets.
- Keep temporary media files outside the database.

## Expected Files

| Path | Action | Source | Notes |
| --- | --- | --- | --- |
| `pom.xml` | Modify | Technology definition | Add persistence dependencies. |
| `src/main/resources/application*.yml` or `.properties` | Create / Modify | Tech Spec | Add datasource/JPA config. |

## Dependencies

| Dependency | Type | Status | Notes |
| --- | --- | --- | --- |
| MVP-MEDIA-001 | Previous task | Pending | Requires Spring Boot scaffold. |
| MVP-MEDIA-002 | Previous task | Pending | Uses persistence package boundary. |

## Validation

- Application starts with database configuration supplied.
- Build succeeds without requiring hardcoded credentials.

## Acceptance Criteria

- [ ] JPA and MySQL dependencies are present.
- [ ] Datasource configuration is environment-driven.
- [ ] No media file content is stored in the database.

## Risks

- Local startup may require a MySQL instance before Docker Compose is confirmed.

## Open Questions

- Local database orchestration is pending Docker Compose confirmation.

## Notes for the Implementing Agent

- Keep config compatible with later Docker Compose or hosted MySQL settings.
