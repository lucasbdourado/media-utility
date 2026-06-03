# Task Architecture Decision Notes: Implement URL Validation

## Status

Status: Ready for Implementation

Last updated: 2026-06-03

Decision notes file: `docs/architecture/task-decisions/mvp-media-utility/015-implement-url-validation-architecture-decisions.md`

Task plan file: `docs/task-plans/mvp-media-utility/015-implement-url-validation-plan.md`

Task file: `docs/tasks/mvp-media-utility/015-implement-url-validation.md`

## Purpose

Record task-specific architecture decisions, conflicts, and confirmed ADR candidates identified while preparing the task implementation plan.

This file is planning support only. It is not a final ADR and must not replace the ADR workflow.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| ADR-008 | `docs/adrs/008-define-public-rest-api-contract.md` | Decision, Consequences | Accepted | Establishes public endpoint mapping and status codes. |
| User Decision | Current planning session | DNS Resolution Security Level | Confirmed by user | Confirmed that hostname DNS resolution is required during validation to prevent DNS Rebinding SSRF. |

## Confirmed Architecture Decisions

List architecture decisions that are already confirmed by source documents, codebase evidence, or explicit user confirmation.

| Decision | Source | Impact on This Task | Notes |
| --- | --- | --- | --- |
| Create `UrlDownloadValidator` inside `com.lucasdourado.mediautility.media.download` package | Project Architecture | Separates URL validation logic into its respective domain boundary rather than putting it in the API controller. | Confirmed |
| DNS resolution hostname checks | User Decision | The validator resolves the hostnames via DNS (`InetAddress.getAllByName`) to check resolved IPs against private/local ranges, preventing DNS rebinding SSRF. | Confirmed |

## Pending Architecture Decisions

None. All task-relevant architecture decisions have been answered or explicitly deferred out of scope by the user.

## Architecture Conflicts

None.

## ADR Candidates

None.

## Implementation Impact

- **Security & SSRF**: DNS resolution checking adds a vital security layer for user-provided URLs.
- **Performance**: DNS resolution makes the request validation thread perform blocking network lookups. These lookups are fast under normal conditions but can timeout or block on slow DNS.
- **Offline testing**: Offline test runs might fail to resolve external domain names. The test suite should use IP literals and local domain names (like `localhost`) which resolve without public network connections.

## Questions for the User

None. All task-relevant architecture questions have been answered.

## Notes for the Implementing Agent

- Standardize validation error response to target the field name `"url"`.
- Use local hostnames/IPs for offline test safety (e.g. `127.0.0.1`, `localhost`).
