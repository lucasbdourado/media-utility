# Task: Add Local Development Compose Setup

## Status

Status: Depends on Previous Task

Last updated: 2026-06-03

## Task ID

ID: MVP-MEDIA-022

Order: 022

Task file: `docs/tasks/mvp-media-utility/022-add-local-development-compose-setup.md`

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project planning | `docs/planning/project-planning.md` | Milestone 5, Suggested Task Order | Confirmed by source document | Proposes orchestrating local dependencies. |
| Task 021 | `docs/tasks/mvp-media-utility/021-create-docker-runtime-packaging.md` | Expected Files | Confirmed by task context | Depends on the Dockerfile defined in task 021. |

## Context

Running the Spring Boot monolith with its MySQL backend and media utilities (FFmpeg and yt-dlp) locally can be complex. Docker Compose simplifies setting up a reproducible development environment where database setup, application configuration, and local storage volume mappings are handled automatically.

## Goal

Create a local Docker Compose setup that coordinates the application container (which includes the built frontend assets) and the MySQL database container.

## Scope

- Create a `docker-compose.yml` file at the root of the project.
- Define a service for `mysql` (version 8.0 or compatible) with a healthy-check, database name, user, root password, and a named Docker volume for persistent storage.
- Define a service for `app` that builds from the root `Dockerfile` (or uses the packaged image) and connects to the MySQL container.
- Map the required application ports (e.g. host port 8080 to container port 8080).
- Expose environment variables to the app container to configure database URL, username, password, and storage directories.
- Define a local volume for temporary media uploads (`MEDIA_UTILITY_STORAGE_ROOT`) mapping to a host folder to easily verify file operations.

## Out of Scope

- Setting up production clustering, Docker Swarm, or Kubernetes manifests.
- Configuring remote CI/CD container registry integrations.

## Implementation Instructions

- Design the `docker-compose.yml` so that the `app` service waits for the `mysql` service to be healthy (using `depends_on` with `condition: service_healthy`).
- Place default configuration values or environment variables in a `.env.compose` template file for developer reference.
- Use environment variables (`spring.datasource.url`, etc.) rather than hardcoded credentials in `application.properties` or `application.yml` for connection settings.

## Expected Files

| Path | Action | Source | Notes |
| --- | --- | --- | --- |
| `docker-compose.yml` | Create | Dev Environment Setup | Docker Compose file. |
| `.env.compose` | Create | Dev Environment Setup | Example environment configuration. |

## Dependencies

| Dependency | Type | Status | Notes |
| --- | --- | --- | --- |
| MVP-MEDIA-021 | Previous task | Pending | Requires buildable Dockerfile. |

## Validation

- Execute `docker compose -f docker-compose.yml up --build -d`.
- Verify both `mysql` and `app` containers start successfully and communicate.
- Access the web interface at `http://localhost:8080` and execute a conversion or URL download flow.
- Check that files are written to/deleted from the mounted volume.

## Acceptance Criteria

- [ ] `docker-compose.yml` is present in the project root.
- [ ] Running `docker compose up` starts MySQL and the application Monolith.
- [ ] Monolith container successfully connects to MySQL and boots.
- [ ] MySQL storage persists across container recreation via named volume.
- [ ] App uses host-mounted volume for media storage, readable/writable on the host.

## Risks

- Host port 3306 or 8080 conflicts if local database or local app instances are running (recommend using variables in `.env` for host port mapping).

## Open Questions

None.

## Notes for the Implementing Agent

Ensure standard environment variable names match the configuration expected by Spring Boot (e.g., `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`).
