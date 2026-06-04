# Task Architecture Decision Notes: Add Local Development Compose Setup

## Status

Status: Ready for Implementation

Last updated: 2026-06-03

Decision notes file: `docs/architecture/task-decisions/mvp-media-utility/022-add-local-development-compose-setup-architecture-decisions.md`

Task plan file: `docs/task-plans/mvp-media-utility/022-add-local-development-compose-setup-plan.md`

Task file: `docs/tasks/mvp-media-utility/022-add-local-development-compose-setup.md`

## Purpose

Record task-specific architecture decisions, conflicts, and confirmed ADR candidates identified while preparing the task implementation plan.

This file is planning support only. It is not a final ADR and must not replace the ADR workflow.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project Discovery | `docs/context/project-discover.md` | Known Technologies | Confirmed by source document | Defines Greenfield and repository constraints. |
| ADR-004 | `docs/adrs/004-use-mysql-with-jpa-for-operation-metadata-persistence.md` | Decisions | Confirmed by source document | Establishes MySQL persistence configuration constraints. |
| ADR-006 | `docs/adrs/006-use-root-relative-keys-for-temporary-local-storage.md` | Storage keys | Confirmed by source document | Defines filesystem local storage volume mounting rules. |

## Confirmed Architecture Decisions

List architecture decisions that are already confirmed by source documents, codebase evidence, or explicit user confirmation.

| Decision | Source | Impact on This Task | Notes |
| --- | --- | --- | --- |
| MySQL 8.0 Base Image | User confirmation | Instructs `docker-compose.yml` to pull `mysql:8.0`. | Ensures standard compatibility. |
| Host Port Configuration via Env | User confirmation | Prevents hardcoded port conflicts in docker-compose.yml. | Overridable host port bindings. |
| Local Mount `./storage` directory | User confirmation | Maps host directory `./storage` to container `/app/storage`. | Enables easy inspection of uploaded files. |
| Hibernate DDL-Auto update | User confirmation | Passes `MEDIA_UTILITY_JPA_DDL_AUTO=update` to container. | Automates initial database schema generation. |

## Pending Architecture Decisions

None. All task-relevant architecture decisions have been answered or explicitly deferred out of scope by the user.

## Architecture Conflicts

None.

## ADR Candidates

None.

## Implementation Impact

- **Spring Boot Environment Mappings:**
  The `app` container service in `docker-compose.yml` will map host-defined credentials directly to standard Spring Boot environment properties, resolving the connection parameters specified in `src/main/resources/application.properties` dynamically.
- **MySQL Boot Check:**
  The `app` service configuration will use `depends_on.condition: service_healthy` referencing the database service. The database container will expose a health check running `mysqladmin ping` to guarantee the Spring application doesn't boot prematurely and crash due to connection refusal.
- **Storage Directory Handling:**
  Because the application container operates under a non-root `appuser` (UID 10001), the host-mounted folder `./storage` must be accessible. The execution notes will explicitly detail the permission requirements on Linux host environments to prevent runtime storage failures.

## Questions for the User

None. All task-relevant architecture questions have been answered.

## Notes for the Implementing Agent

- The Docker Compose environment properties must align exactly with properties expected by Spring Boot (e.g. `MEDIA_UTILITY_DATASOURCE_URL`, `MEDIA_UTILITY_DATASOURCE_USERNAME`, `MEDIA_UTILITY_DATASOURCE_PASSWORD`).
- Document the Linux volume host permission mitigation command (`chown -R 10001:10001 ./storage`) clearly inside the `README.md` changes.
