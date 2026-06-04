# Task: Create Docker Runtime Packaging

## Status

Status: Completed

Last updated: 2026-06-03

## Task ID

ID: MVP-MEDIA-021

Order: 021

Task file: `docs/tasks/mvp-media-utility/021-create-docker-runtime-packaging.md`

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Technology definition | `docs/architecture/technology-definition.md` | Confirmed Technology Decisions | Confirmed by source document | Docker packaging is confirmed. |
| Tech Spec | `docs/specs/tech-spec.md` | Integrations, Rollout Strategy, Compatibility | Confirmed by source document | Image must include Java runtime, FFmpeg, and yt-dlp. |

## Context

The deployed runtime must include application code plus external media tools. Docker standardizes that runtime.

## Goal

Create Docker packaging for the MVP application.

## Scope

- Add Dockerfile for building/running the Spring Boot application.
- Include or install FFmpeg and yt-dlp in the runtime image.
- Ensure built frontend assets are included in the packaged app.
- Expose runtime configuration through environment variables.

## Out of Scope

- Do not choose hosting provider.
- Do not add CI/CD workflow unless separately planned.
- Do not add Docker Compose in this task.

## Implementation Instructions

- Package one deployable application.
- Verify FFmpeg and yt-dlp availability in the image.
- Avoid baking secrets into the image.

## Expected Files

| Path | Action | Source | Notes |
| --- | --- | --- | --- |
| `Dockerfile` | Create | Technology definition | Runtime packaging. |
| `.dockerignore` | Create | Standard practice | Keep build context clean. |
| `README.md` or docs path | Modify | Tech Spec | Runtime notes may be completed in task 026. |

## Dependencies

| Dependency | Type | Status | Notes |
| --- | --- | --- | --- |
| MVP-MEDIA-001 | Previous task | Pending | Requires buildable app. |
| MVP-MEDIA-013 | Previous task | Pending | FFmpeg runtime needed. |
| MVP-MEDIA-016 | Previous task | Pending | yt-dlp runtime needed. |

## Validation

- Build Docker image.
- Verify app starts in container with required env vars.
- Verify FFmpeg and yt-dlp commands are available in the image.

## Acceptance Criteria

- [ ] Docker image builds successfully.
- [ ] Runtime image includes Java app, frontend assets, FFmpeg, and yt-dlp.
- [ ] Secrets are not baked into image.

## Risks

- yt-dlp installation/update strategy can affect image reproducibility.

## Open Questions

- Hosting provider remains open and may affect runtime configuration.

## Notes for the Implementing Agent

- Keep image design compatible with a single-instance local disk MVP.
