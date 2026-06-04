# Task Architecture Decision Notes: Create Docker Runtime Packaging

## Status

Status: Ready for Implementation

Last updated: 2026-06-03

Decision notes file: `docs/architecture/task-decisions/mvp-media-utility/021-create-docker-runtime-packaging-architecture-decisions.md`

Task plan file: `docs/task-plans/mvp-media-utility/021-create-docker-runtime-packaging-plan.md`

Task file: `docs/tasks/mvp-media-utility/021-create-docker-runtime-packaging.md`

## Purpose

Record task-specific architecture decisions, conflicts, and confirmed ADR candidates identified while preparing the task implementation plan.

This file is planning support only. It is not a final ADR and must not replace the ADR workflow.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| ADR-003 | `docs/adrs/003-use-maven-npm-coordinated-asset-packaging.md` | Decision, Consequences | Confirmed by source document | Dictates unified Maven build orchestrating the frontend compilation. |
| User decision | Current `plan-task` session | Docker build strategy | Confirmed by user | Selected Option A (Multi-stage build). |
| User decision | Current `plan-task` session | Base execution image | Confirmed by user | Selected Option A (Debian/Ubuntu-based Eclipse Temurin JRE base image). |
| User decision | Current `plan-task` session | yt-dlp install strategy | Confirmed by user | Selected Option A (Direct curl binary download of official release). |
| User decision | Current `plan-task` session | Runtime user security | Confirmed by user | Selected Option A (Non-root user execution). |
| User decision | Current `plan-task` session | Storage path location | Confirmed by user | Selected Option A (Default folder `/app/storage`). |

## Confirmed Architecture Decisions

List architecture decisions that are already confirmed by source documents, codebase evidence, or explicit user confirmation.

| Decision | Source | Impact on This Task | Notes |
| --- | --- | --- | --- |
| Multi-stage Docker Build | User Confirmation | Isolates build-time dependencies (Maven, JDK, Node.js, NPM) from runtime container. | Keeps runner image lean and secure. |
| eclipse-temurin:21-jre-noble base | User Confirmation | Supplies JRE 21 runtime running on stable Ubuntu Noble package baseline. | Ensures full support for external binary dependencies like python3 and ffmpeg. |
| Direct download for yt-dlp | User Confirmation | Installs the latest release from the official GitHub releases. | Highly recommended for yt-dlp to support video extraction. |
| Non-root user execution | User Confirmation | Runs the application process under user `appuser` (UID 10001). | Follows container security best practices. |
| Default Storage Root at `/app/storage` | User Confirmation | Directs temporary downloads and uploads to `/app/storage` inside the workspace. | Configures appropriate read/write folder rights for non-root user. |

## Pending Architecture Decisions

None. All task-relevant architecture decisions have been answered or explicitly deferred out of scope by the user.

## Architecture Conflicts

None.

## ADR Candidates

None. The packaging choices are specific to the task scope and build execution. ADR-003 remains the authoritative ADR for the build coordination.

## Implementation Impact

- **Dockerfile design:** The build stage installs curl/gnupg/node, copies source code, and triggers Maven. The execution stage configures apt packages (ffmpeg, python3, curl), installs yt-dlp, defines the user group structure, prepares `/app/storage`, and launches Java.
- **Security context:** Non-root execution means the app cannot execute operations requiring superuser privileges inside the container, which is fine since the application only needs to launch ffmpeg/yt-dlp processes and write to `/app/storage`.

## Questions for the User

None. All task-relevant architecture questions have been answered.

## Notes for the Implementing Agent

- Make sure to add `chown` permissions when creating `/app/storage` and copying the JAR file, so `appuser` can read the JAR and write to the storage path.
- Keep system update lists clean using `rm -rf /var/lib/apt/lists/*` to limit container image size.
