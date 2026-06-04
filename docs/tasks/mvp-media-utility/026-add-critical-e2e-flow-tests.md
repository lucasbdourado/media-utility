# Task: Add Critical E2E Flow Tests

## Status

Status: Depends on Previous Task

Last updated: 2026-06-04

## Task ID

ID: MVP-MEDIA-026

Order: 026

Task file: `docs/tasks/mvp-media-utility/026-add-critical-e2e-flow-tests.md`

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project planning | `docs/planning/project-planning.md` | Milestone 5, Suggested Task Order | Confirmed by source document | Outlines end-to-end happy path validation. |
| Tech Spec | `docs/specs/tech-spec.md` | Verification Plan, Manual Verification | Confirmed by source document | Recommends validation of URL download and MP4 conversion flows. |

## Context

While unit and integration tests verify individual layers and components in isolation, we need end-to-end (E2E) tests to ensure the frontend and backend interact correctly. Running Playwright against a live containerized application validates the full user journey: from selecting an operation, submitting valid input, watching the progress, and downloading the resulting file.

## Goal

Configure Playwright and write E2E tests for the critical media conversion and URL download user flows against a running instance of the application after the frontend has been integrated with the backend operation APIs.

## Scope

- Install `@playwright/test` in the frontend directory (or project root as appropriate).
- Create a `playwright.config.ts` configuration file specifying test directories, browser targets (e.g. Chromium, Firefox), and target base URL (e.g. `http://localhost:8080`).
- Create E2E test files verifying:
  - **MP4 conversion flow**: Selecting MP4-to-MP3 option, uploading a mock MP4 file, verifying processing/loading state, and asserting the downloadable MP3 link is generated and reachable.
  - **URL download flow**: Selecting URL download option, inserting a valid URL (mocked or fast test source), confirming responsibility notice display, submitting, verifying progress, and asserting download success.
  - **Validation failure flows**: Empty fields, uploading invalid file extensions, or invalid URL inputs.
- Add run scripts in `package.json` (e.g. `"test:e2e": "playwright test"`).

## Out of Scope

- Integrating the frontend with backend operation APIs; this is handled by MVP-MEDIA-025.
- Testing external media provider APIs or actual YouTube endpoints (these should be simulated or mocked within the download adapter mock/test mode).

## Implementation Instructions

- Locate tests under a dedicated folder `frontend/e2e/` or `e2e/` according to standard React project structure.
- Use Playwright fixtures or mock endpoints if needed to bypass long media processing durations.
- Write tests in TypeScript to maintain consistency with the frontend codebase.

## Expected Files

| Path | Action | Source | Notes |
| --- | --- | --- | --- |
| `frontend/playwright.config.ts` | Create | E2E Testing Framework | Playwright E2E configuration file. |
| `frontend/e2e/media-conversion.spec.ts` | Create | E2E Critical Path | Test for MP4 conversion flow. |
| `frontend/e2e/url-download.spec.ts` | Create | E2E Critical Path | Test for URL download flow. |
| `frontend/package.json` | Modify | E2E Testing Framework | Add script to run Playwright. |

## Dependencies

| Dependency | Type | Status | Notes |
| --- | --- | --- | --- |
| MVP-MEDIA-025 | Previous task | Blocking | Requires the UI to submit operations to backend APIs, render backend-driven states, and expose result links. |
| MVP-MEDIA-022 | Previous task | Pending | Requires the application to be runnable containerized or locally with Docker Compose. |
| MVP-MEDIA-014 | Previous task | Pending | Requires conversion endpoint to be ready. |
| MVP-MEDIA-017 | Previous task | Pending | Requires URL download endpoint to be ready. |

## Validation

- Start the application (e.g., `docker compose up -d`).
- Run `npm run test:e2e` inside the `frontend` folder (or equivalent script from project root).
- Verify all E2E test cases pass successfully.

## Acceptance Criteria

- [ ] Playwright E2E testing framework is configured.
- [ ] Tests verify the MP4-to-MP3 conversion happy path (including file download check).
- [ ] Tests verify the URL download happy path (including file download check).
- [ ] Tests verify validation message triggers on incorrect inputs.
- [ ] E2E tests run successfully against a running local app instance.

## Risks

- Flakiness in E2E tests due to variable network/processing speed. Configure generous timeouts for file conversion mock execution.
- External dependencies (like YouTube download adapter) must use local fixtures or mock endpoints to prevent test failures on network hiccups.

## Open Questions

- Should Playwright be installed in the root folder or within `frontend/`? (Recommended: `frontend/` since it tests the React app).
- What controlled fixture, mock, or test-mode strategy should be used for stable media operation execution after MVP-MEDIA-025 is complete?
- Should E2E execution start the local runtime itself or require a pre-running Docker Compose/application instance?

## Notes for the Implementing Agent

- Do not execute or plan this task as ready until MVP-MEDIA-025 is completed.
- Ensure mock files (like a small sample MP4) are checked into the test resources folder (e.g. `frontend/e2e/fixtures/`) to facilitate automated testing.
