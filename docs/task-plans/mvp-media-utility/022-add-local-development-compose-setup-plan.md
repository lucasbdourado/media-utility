# Task Implementation Plan: Add Local Development Compose Setup

## Status

Status: Ready for Implementation

Last updated: 2026-06-03

Plan file: `docs/task-plans/mvp-media-utility/022-add-local-development-compose-setup-plan.md`

Architecture decision notes file: `docs/architecture/task-decisions/mvp-media-utility/022-add-local-development-compose-setup-architecture-decisions.md`

## Task Reference

Task ID: `MVP-MEDIA-022`

Task file: `docs/tasks/mvp-media-utility/022-add-local-development-compose-setup.md`

Task status: `Depends on Previous Task` (Prerequisites implemented)

Task group or feature: `Infrastructure and Packaging`

## Planning Mode Requirement

Plan mode verified: Yes

Notes:

- This plan was created in plan mode.
- Implementation must not start during `plan-task`.
- A future implementation request must use this saved plan and any saved architecture decision notes as source context.

## Source Documents

List every document, ADR, codebase evidence item, or explicit user decision used to prepare this plan.

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project Discovery | `docs/context/project-discover.md` | Known Technologies | Confirmed by source document | Establishes the initial project context. |
| Task 021 Plan | `docs/task-plans/mvp-media-utility/021-create-docker-runtime-packaging-plan.md` | Proposed Implementation | Confirmed by source document | Depends on the buildable Dockerfile and runner parameters. |
| Codebase configuration | `src/main/resources/application.properties` | All properties | Detected in codebase | Identifies runtime environment variables needed for configuration. |
| User decision | Current `plan-task` session | MySQL database version | Confirmed by user | MySQL 8.0 official container image. |
| User decision | Current `plan-task` session | Port override configuration | Confirmed by user | Use environment variables in `.env.compose` to map host ports. |
| User decision | Current `plan-task` session | Media storage mount | Confirmed by user | Mount relative host directory `./storage` to `/app/storage`. |
| User decision | Current `plan-task` session | Database DDL Auto | Confirmed by user | Set `MEDIA_UTILITY_JPA_DDL_AUTO=update` to initialize tables automatically. |

## Context Summary

The Media Utility monolith has been dockerized with JRE 21, FFmpeg, and yt-dlp runtime requirements. However, it requires a MySQL database backend to persist metadata events and run properly. Coordinating the monolith container and a MySQL database container via Docker Compose simplifies the developer onboarding experience and establishes a reproducible local environment.

## Task Goal

Create a root-level `docker-compose.yml` and a template `.env.compose` configuration file that starts a MySQL 8.0 container (with a named volume and health check) and the Spring Boot application container (which builds from the root `Dockerfile`), connecting them dynamically using environment variables.

## Confirmed Scope

- Create a root-level `docker-compose.yml`.
- Configure the `mysql` service using the `mysql:8.0` official image, using environment variables for the root password, database name, user, and user password.
- Set up a health check on the `mysql` service to verify it is accepting connections (using `mysqladmin ping`).
- Configure a named Docker volume (`mysql_data`) mounted to `/var/lib/mysql` inside the database container.
- Configure the `app` service to build from the root directory `.`, and configure it to depend on the `mysql` service being healthy (`condition: service_healthy`).
- Map host ports `APP_HOST_PORT` (default 8080) and `MYSQL_HOST_PORT` (default 3306) to their respective container ports.
- Expose environment variables to the `app` service for connecting to MySQL and referencing the storage root:
  - `MEDIA_UTILITY_DATASOURCE_URL=jdbc:mysql://mysql:3306/media_utility?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
  - `MEDIA_UTILITY_DATASOURCE_USERNAME`
  - `MEDIA_UTILITY_DATASOURCE_PASSWORD`
  - `MEDIA_UTILITY_JPA_DDL_AUTO=update`
  - `MEDIA_UTILITY_STORAGE_ROOT=/app/storage`
- Mount host relative directory `./storage` to container directory `/app/storage` under the `app` service.
- Create a `.env.compose` file with default variables:
  - `MYSQL_ROOT_PASSWORD=root_password_here`
  - `MYSQL_DATABASE=media_utility`
  - `MYSQL_USER=media_user`
  - `MYSQL_PASSWORD=media_password`
  - `APP_HOST_PORT=8080`
  - `MYSQL_HOST_PORT=3306`
  - `MEDIA_UTILITY_JPA_DDL_AUTO=update`
- Document docker compose commands in the `README.md` file.

## Out of Scope

- Production deployments (Kubernetes, AWS ECS, Docker Swarm).
- Setup of external mail servers, monitoring systems (Prometheus/Grafana), or separate cache layers (Redis) in Compose.

## Requirements Covered

| Requirement | Source | How This Task Covers It | Status |
| --- | --- | --- | --- |
| Local Dev Environment setup | Task 022 | Creates docker-compose.yml and .env.compose at root. | Confirmed |
| Database container coordination | Task 022 | Defines mysql service with health check and persistent volume. | Confirmed |
| Database connection environment variables | application.properties | Passes config vars dynamically to the app container. | Confirmed |
| Persistent Storage mount | Task 022 | Maps relative host directory `./storage` to `/app/storage`. | Confirmed |

## Technical Specification Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Monolith Local Setup | Full | Yes | Sets up local multi-container composition using Compose. |

Coverage assessment:
- Justifying Tech Spec section: N/A (Tech Spec is empty in the repo, but the requirements are derived from task details and existing configuration variables).
- Tech Spec sections implemented by this task: N/A.
- Gaps between task and Tech Spec: None.

## Architecture and ADR Considerations

| Decision or ADR | Source | Impact on This Task | Status |
| --- | --- | --- | --- |
| ADR-004 | `docs/adrs/004-use-mysql-with-jpa-for-operation-metadata-persistence.md` | Requires MySQL service setup in Compose matching connection settings. | Confirmed |
| ADR-006 | `docs/adrs/006-use-root-relative-keys-for-temporary-local-storage.md` | Dictates application storage structure mapping via container volume. | Confirmed |

ADR candidates or architecture decisions needed:
- None.

Architecture decision notes:
- Saved separately: Yes
- Path: `docs/architecture/task-decisions/mvp-media-utility/022-add-local-development-compose-setup-architecture-decisions.md`
- Notes file status: New

## Confirmed Decisions

- MySQL version `mysql:8.0` is used for the database container.
- Host port mappings are externalized into environment variables (`APP_HOST_PORT` and `MYSQL_HOST_PORT`) to prevent port conflicts on the host system.
- Host directory `./storage` is mounted as the application's media uploads root folder.
- `MEDIA_UTILITY_JPA_DDL_AUTO=update` is passed by default to ensure the application auto-generates tables in the fresh database container on startup.

## Pending Decisions

None. All task-relevant decisions have been answered or explicitly deferred out of scope by the user.

## Questions for the User

None. All task-relevant questions have been answered.

## Proposed Implementation Approach

1. **Compose Configuration Layout:**
   Draft a standard Compose specification version `3.8`. Structure two main services: `mysql` and `app`.
2. **Environment Template (`.env.compose`):**
   Expose standard default credentials and port numbers that are safe for local development, allowing overriding via a local `.env` file (which should be Git-ignored).
3. **App Service Build context:**
   Define `build.context: .` and `build.dockerfile: Dockerfile` to trigger container assembly from the local root workspace context when running docker compose up.

## Files and Areas Expected to Change

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| `docker-compose.yml` | Create | Dev Environment Setup | Multi-service orchestration configuration file. |
| `.env.compose` | Create | Dev Environment Setup | Environment template file. |
| `README.md` | Modify | Documentation | Document local compose commands and configuration lifecycle. |

## Step-by-Step Implementation Plan

1. **Create `.env.compose`:** Define default MySQL credentials (`MYSQL_DATABASE`, `MYSQL_USER`, etc.) and default host port values (`APP_HOST_PORT=8080`, `MYSQL_HOST_PORT=3306`).
2. **Create `docker-compose.yml`:** Write the service schemas including ports, environment mappings, persistent named volumes for MySQL, healthcheck for MySQL, and host mount `./storage:/app/storage` for the application container.
3. **Update `README.md`:** Add a dedicated "Docker Compose Setup" section detailing:
   - Command to copy `.env.compose` to `.env`
   - Command to spin up the container network: `docker compose up --build -d`
   - Log tracking: `docker compose logs -f app`
   - Cleanup/Teardown: `docker compose down -v`
4. **Local Verification:**
   - Execute the compose stack locally.
   - Verify Spring Boot successfully connects to MySQL and the database updates its schema.
   - Test end-to-end user actions via port `8080` to verify files get stored on `./storage`.

## Validation Strategy

- Copy `.env.compose` to `.env`.
- Spin up the composition: `docker compose up --build -d`
- Check health status: `docker compose ps` -> verify `mysql` and `app` services are `running` and healthy.
- Verify Spring database boot logs:
  - Run `docker compose logs app` -> verify JPA connection success and table auto-updating events.
- Test operational validation:
  - Navigate to `http://localhost:8080` (or override port) and trigger a conversion or URL download.
  - Verify that the processed file appears in the host directory `./storage/`.
- Verify database persistence:
  - Stop containers: `docker compose down`
  - Re-run containers: `docker compose up -d`
  - Query database or front-end metadata lists to verify old entries persist.

## Tests to Add or Update

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| Compose Startup | Manual | Validate services boot up and link correctly. | Validated using `docker compose up`. |
| Port Customization | Manual | Override port variables in `.env` and verify services map to new ports. | Validated via custom `.env`. |
| Host Volume Permissions | Manual | Ensure processed media files are writable to host. | Checked by verifying file existence inside `./storage`. |

## Acceptance Criteria

- [ ] `docker-compose.yml` is present in the project root.
- [ ] `.env.compose` is present in the project root and defines customizable parameters for credentials and host ports.
- [ ] Running `docker compose up` starts MySQL and the application Monolith.
- [ ] The app container waits for the MySQL container to pass its health check before starting.
- [ ] The application container successfully connects to MySQL and boots.
- [ ] MySQL storage persists across container recreation via named volume.
- [ ] App uses host-mounted volume `./storage` for media storage, readable/writable on the host.

## Risks and Edge Cases

- **Port Conflicts:** If ports `8080` or `3306` are occupied on the host system, container startup will fail. This is mitigated by configuring host ports through the `.env` file (`APP_HOST_PORT` and `MYSQL_HOST_PORT`).
- **Linux Permission Gaps:** On Linux hosts, host-mounted folders inherit ownership of the container process user (UID `10001(appuser)`). If the directory `./storage` does not have proper permissions on the host, the container process might throw `AccessDeniedException`.
  - *Mitigation:* We will document that developers running on Linux hosts might need to create the `./storage` folder manually and change its ownership via `sudo chown -R 10001:10001 ./storage` or make it group-writable.

## Rollback or Recovery Notes

- Tear down container network: `docker compose down -v` to delete container environments and start fresh.

## Documentation Updates

- Update `README.md` to append compose configuration instructions.

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

- Match standard Spring Boot environment variable overrides (e.g. `MEDIA_UTILITY_DATASOURCE_URL`, `MEDIA_UTILITY_DATASOURCE_USERNAME`, `MEDIA_UTILITY_DATASOURCE_PASSWORD`).
- Ensure the MySQL healthcheck uses standard `mysqladmin ping` with a secure hostname config so the `app` container starts only after the database is completely ready to accept connections.
