# Task Implementation Plan: Create Docker Runtime Packaging

## Status

Status: Completed

Last updated: 2026-06-03

Plan file: `docs/task-plans/mvp-media-utility/021-create-docker-runtime-packaging-plan.md`

Architecture decision notes file: `docs/architecture/task-decisions/mvp-media-utility/021-create-docker-runtime-packaging-architecture-decisions.md`

## Task Reference

Task ID: `MVP-MEDIA-021`

Task file: `docs/tasks/mvp-media-utility/021-create-docker-runtime-packaging.md`

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
| ADR-003 | `docs/adrs/003-use-maven-npm-coordinated-asset-packaging.md` | Decision, Consequences | Confirmed by source document | Establishes that Maven orchestrates both frontend and backend build steps. |
| Codebase configuration | `pom.xml` | Build plugins, dependencies | Detected in codebase | Confirms JDK 21 and the execution scripts for React frontend asset building. |
| Codebase configuration | `src/main/resources/application.properties` | All properties | Detected in codebase | Identifies runtime environment variables needed for configuration. |
| User decision | Current `plan-task` session | Docker build strategy | Confirmed by user | User chose Option A: Multi-stage Docker build. |
| User decision | Current `plan-task` session | Runtime base image | Confirmed by user | User chose Option A: Debian/Ubuntu-based Eclipse Temurin JRE base image. |
| User decision | Current `plan-task` session | yt-dlp install strategy | Confirmed by user | User chose Option A: Direct curl binary download of official release. |
| User decision | Current `plan-task` session | Runtime user security | Confirmed by user | User chose Option A: Execute under non-root user `appuser`. |
| User decision | Current `plan-task` session | Storage path location | Confirmed by user | User chose Option A: Default temporary storage path set to `/app/storage`. |

## Context Summary

The Media Utility application requires a standard runtime environment including the JRE 21 execution engine, the external media manipulator tool `ffmpeg`, and `python3` + `yt-dlp` for video downloading. Standardizing this stack via a multi-stage Dockerfile ensures portability and isolates system dependency configuration from local machine setup.

## Task Goal

Create a multi-stage `Dockerfile` and a `.dockerignore` file to build, package, and execute the Spring Boot and React monolithic application, alongside FFmpeg and yt-dlp under a secure non-root user.

## Confirmed Scope

- Create a `Dockerfile` using a multi-stage build layout (build phase using Maven + Node.js; run phase using `eclipse-temurin:21-jre-noble`).
- Configure the runtime container to install `ffmpeg` and `python3` via `apt-get`, and `yt-dlp` via direct official binary download.
- Set up a non-root Linux user (`appuser`) and group (`spring`) to execute the application process inside the container.
- Establish a workspace directory (`/app`) and a default storage root (`/app/storage`) owned by `appuser`.
- Declare exposure for port `8080` and document configuration environment variables.
- Create a `.dockerignore` file to exclude local test output, development tools, and IDE files from the Docker context.
- Update `README.md` with instructions on how to build and execute the application container.

## Out of Scope

- Setting up production environment hosting providers.
- Building CI/CD runner workflows.
- Creating docker-compose files (handled in subsequent tasks).

## Requirements Covered

| Requirement | Source | How This Task Covers It | Status |
| --- | --- | --- | --- |
| Docker packaging | Technology definition | Creates the Dockerfile and .dockerignore for packaging. | Confirmed |
| Java, FFmpeg, yt-dlp runtimes | Tech Spec | Installs the JRE, FFmpeg, and yt-dlp in the runtime image. | Confirmed |
| Secure execution | Standard practice | Uses a non-root user and specific directory permissions. | Confirmed |
| Env configuration | application.properties | Defines expected environment variables for the container. | Confirmed |

## Technical Specification Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Monolith Packaging | Full | Yes | Sets up multi-stage container build and environment configurations. |

Coverage assessment:
- Justifying Tech Spec section: N/A (Tech Spec is currently empty in the repo, but the requirements are derived from task details and ADR-003).
- Tech Spec sections implemented by this task: N/A.
- Gaps between task and Tech Spec: None.

## Architecture and ADR Considerations

| Decision or ADR | Source | Impact on This Task | Status |
| --- | --- | --- | --- |
| ADR-003 | `docs/adrs/003-use-maven-npm-coordinated-asset-packaging.md` | Requires the builder image to have both Maven/Java and Node/NPM to run the unified build. | Confirmed |

ADR candidates or architecture decisions needed:

- None.

Architecture decision notes:

- Saved separately: Yes
- Path: `docs/architecture/task-decisions/mvp-media-utility/021-create-docker-runtime-packaging-architecture-decisions.md`
- Notes file status: New

## Confirmed Decisions

- Use a multi-stage Docker build to keep the runtime image lean and secure.
- Use `eclipse-temurin:21-jre-noble` as the runtime base image for Debian compatibility.
- Download the official `yt-dlp` binary directly from GitHub releases during image assembly.
- Create and run the application as `appuser` (UID/GID 10001).
- Set the default container storage path to `/app/storage`.

## Pending Decisions

None. All task-relevant decisions have been answered or explicitly deferred out of scope by the user.

## Questions for the User

None. All task-relevant questions have been answered.

## Proposed Implementation Approach

### 1. Multi-Stage Dockerfile Layout

#### Build Stage (Builder):
Use `maven:3.9.9-eclipse-temurin-21` as the base image. To support ADR-003, we install Node.js 20 inside the builder:
```dockerfile
FROM maven:3.9.9-eclipse-temurin-21 AS builder

# Install Node.js 20 & NPM for React build orchestration
RUN apt-get update && apt-get install -y curl gnupg \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /build
COPY . .
RUN mvn clean package -DskipTests
```

#### Run Stage (Runner):
Use `eclipse-temurin:21-jre-noble` for JRE 21 on Ubuntu 24.04:
```dockerfile
FROM eclipse-temurin:21-jre-noble

# Install runtime packages
RUN apt-get update && apt-get install -y \
    ffmpeg \
    python3 \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Install yt-dlp official binary
RUN curl -L https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -o /usr/local/bin/yt-dlp \
    && chmod a+rx /usr/local/bin/yt-dlp

# Configure non-root user
RUN groupadd -g 10001 spring && \
    useradd -u 10001 -g spring -m -d /app -s /bin/bash appuser

WORKDIR /app
RUN mkdir -p /app/storage && chown -R appuser:spring /app

# Copy packaged JAR from builder
COPY --from=builder /build/target/media-utility-0.0.1-SNAPSHOT.jar app.jar
RUN chown appuser:spring app.jar

USER appuser

ENV PORT=8080
ENV MEDIA_UTILITY_STORAGE_ROOT=/app/storage

EXPOSE 8080
VOLUME ["/app/storage"]

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. .dockerignore Configuration
Exclude standard build outputs, IDE config, node modules, and local metadata to avoid bloating the build context:
```text
.git
.gitignore
.gitmodules
.idea
.agents
.codex
**/node_modules
**/dist
target/
*.log
```

## Files and Areas Expected to Change

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| `Dockerfile` | Create | Architecture Definition | Multi-stage build layout. |
| `.dockerignore` | Create | Standard practice | Filter build context. |
| `README.md` | Modify | Documentation | Add instructions for building and running via Docker. |

## Step-by-Step Implementation Plan

1. **Create `.dockerignore`:** Write the ignore rules in the project root folder.
2. **Create `Dockerfile`:** Write the multi-stage build instructions in the project root folder.
3. **Update `README.md`:** Append a new section "Running with Docker" detailing:
   - Build command: `docker build -t media-utility .`
   - Run command with environment variables and storage volume parameters.
4. **Local Verification:**
   - Run `docker build` to verify the compilation and packaging pipeline succeeds.
   - Run the container locally and query the version info of `ffmpeg` and `yt-dlp` to ensure they are found on the container's executable PATH.

## Validation Strategy

- Execute `docker build -t media-utility:test .` and verify the build finishes successfully.
- Verify user and folder permissions:
  - Run `docker run --rm --entrypoint id media-utility:test` -> verify UID/GID is `10001(appuser)/10001(spring)`.
- Verify external utility tool versions inside the built image:
  - Run `docker run --rm --entrypoint ffmpeg media-utility:test -version`
  - Run `docker run --rm --entrypoint yt-dlp media-utility:test --version`
- Run container with custom parameters to ensure Spring Boot starts up and reads standard configurations correctly.

## Tests to Add or Update

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| Dockerfile compilation | Manual/CI | Ensure container assembly succeeds. | Checked via CLI commands. |
| Dependency availability | Manual/CI | Confirm `ffmpeg` and `yt-dlp` execute successfully within the image. | Checked via container run. |

## Acceptance Criteria

- [ ] The `Dockerfile` implements a multi-stage build separating compile-time tools from the runtime container.
- [ ] The `.dockerignore` successfully excludes `node_modules`, `target`, and IDE configuration files from context.
- [ ] The runner container runs as the non-root user `appuser` (UID 10001).
- [ ] `ffmpeg` and `yt-dlp` are successfully installed and runnable in the final runtime PATH.
- [ ] The default storage directory `/app/storage` exists with write permissions for `appuser`.
- [ ] Secrets or database credentials are not hardcoded into the image.
- [ ] `README.md` includes explicit instructions for building and executing the Docker container.

## Risks and Edge Cases

- **Build Cache Bloat:** Multi-stage installs download packages on every run if files change. Using fine-grained copies of `pom.xml` and package files before adding source directories could optimize layer caching but adds complexity. Given this is a simple MVP, copying all source files at once is acceptable but we should emphasize keeping dependencies clean.
- **yt-dlp updates:** As yt-dlp downloads the latest executable during build time, image versions built at different times might use slightly different yt-dlp versions. This is an accepted risk to guarantee video extraction works out of the box.

## Rollback or Recovery Notes

- If container issues arise, developers can revert back to executing the standalone fat JAR on the host machine as documented in the baseline `README.md`.

## Documentation Updates

- Update `README.md` to document container assembly and local execution.

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

- Ensure NPM is installed properly in the builder stage to let Maven complete `exec-maven-plugin` execution tasks.
- Keep the `apt-get` cleanups (`rm -rf /var/lib/apt/lists/*`) present in all run layers to minimize output image size.
