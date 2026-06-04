# Task: Document MVP Runtime and Operational Notes

## Status

Status: Depends on Previous Task

Last updated: 2026-06-01

## Task ID

ID: MVP-MEDIA-027

Order: 027

Task file: `docs/tasks/mvp-media-utility/027-document-mvp-runtime-and-operational-notes.md`

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Tech Spec | `docs/specs/tech-spec.md` | Rollout Strategy, Risks and Trade-offs, Implementation Notes | Confirmed by source document | Runtime must document binaries, cleanup, local disk, and operational risks. |
| Technology definition | `docs/architecture/technology-definition.md` | Constraints, Risks | Confirmed by source document | Docker, local disk, FFmpeg, yt-dlp, MySQL, and 1-hour retention are confirmed. |

## Context

Future implementation and deployment need concise documentation for runtime configuration, required binaries, temporary storage, cleanup behavior, and known MVP constraints.

## Goal

Document MVP runtime setup and operational constraints.

## Scope

- Document required environment variables.
- Document MySQL configuration.
- Document temporary storage location and 1-hour retention.
- Document FFmpeg and yt-dlp runtime requirements.
- Document Docker usage after packaging exists.
- Document known MVP operational risks and deferred decisions.

## Out of Scope

- Do not choose hosting provider.
- Do not create ADRs.
- Do not document features that were not implemented.

## Implementation Instructions

- Base documentation on implemented behavior and confirmed source documents.
- Clearly label unresolved hosting, rate limiting, and policy questions.
- Keep documentation in English.

## Expected Files

| Path | Action | Source | Notes |
| --- | --- | --- | --- |
| `README.md` | Create / Modify | Tech Spec | If repository convention uses root README. |
| `docs/` runtime document | Create / Modify | Tech Spec | Use existing documentation pattern if present. |

## Dependencies

| Dependency | Type | Status | Notes |
| --- | --- | --- | --- |
| MVP implementation tasks | Previous task | Pending | Documentation should reflect implemented behavior. |
| MVP-MEDIA-021 | Previous task | Pending | Docker packaging notes. |
| MVP-MEDIA-019 | Previous task | Pending | Cleanup notes if implemented. |

## Validation

- Follow documented setup steps in a clean local environment where feasible.
- Verify documentation does not describe unimplemented behavior as complete.

## Acceptance Criteria

- [ ] Runtime configuration is documented.
- [ ] FFmpeg and yt-dlp requirements are documented.
- [ ] Temporary storage and 1-hour retention are documented.
- [ ] Open operational questions are clearly labeled.

## Risks

- Documentation can become misleading if it describes planned behavior as implemented.

## Open Questions

- Which hosting provider or runtime environment will be used?
- Should minimal rate limiting be added before public launch?

## Notes for the Implementing Agent

- Keep this as operational handoff documentation, not an architecture decision record.
