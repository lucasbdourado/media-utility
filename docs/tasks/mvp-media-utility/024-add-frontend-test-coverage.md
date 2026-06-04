# Task: Add Frontend Test Coverage

## Status

Status: Depends on Previous Task

Last updated: 2026-06-03

## Task ID

ID: MVP-MEDIA-024

Order: 024

Task file: `docs/tasks/mvp-media-utility/024-add-frontend-test-coverage.md`

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project planning | `docs/planning/project-planning.md` | Milestone 5, Suggested Task Order | Confirmed by source document | Proposes metrics and verification steps. |
| Tech Spec | `docs/specs/tech-spec.md` | Verification Plan, Automated Tests | Confirmed by source document | Recommends automated code coverage checks. |

## Context

The frontend project utilizes Vitest and React Testing Library to test React components. In order to measure quality and identify uncovered logic (such as form validations, loading states, and error alerts), we need to configure Vitest's coverage tool and write additional unit/component tests.

## Goal

Configure Vitest test coverage reporting in the frontend module and write tests to achieve a target coverage of 80% on UI components and utility functions.

## Scope

- Install necessary coverage packages (e.g. `@vitest/coverage-v8`) in the frontend project.
- Modify `frontend/vite.config.ts` (or `frontend/vitest.config.ts`) to configure the coverage provider, reports, and coverage thresholds.
- Add a script in `frontend/package.json` to run tests and collect coverage (e.g., `"test:coverage": "vitest run --coverage"`).
- Write or expand test files in `frontend/src/` to cover form interactions, selector changes, upload/download triggers, and mock API calls.
- Reach a target of at least 80% line and branch coverage on frontend codebase.

## Out of Scope

- Setting up backend coverage (handled by Task 023).
- Configuring E2E tests (handled by Task 025).

## Implementation Instructions

- Configure Vitest coverage to output HTML reports in the `frontend/coverage` folder.
- Exclude build scripts, TypeScript config definitions (`.d.ts`), or standard test setups (e.g., `setupTests.ts`) from the coverage results.
- Implement tests targeting boundary states (empty inputs, network timeouts, successful response styling).

## Expected Files

| Path | Action | Source | Notes |
| --- | --- | --- | --- |
| `frontend/package.json` | Modify | Frontend Dependencies | Add coverage packages and scripts. |
| `frontend/vite.config.ts` | Modify | Frontend Build Config | Configure Vitest coverage section. |
| `frontend/src/.../*.spec.tsx` or similar | Create / Modify | Frontend Tests | Write missing component tests. |

## Dependencies

| Dependency | Type | Status | Notes |
| --- | --- | --- | --- |
| MVP-MEDIA-011 | Previous task | Pending | Requires React forms and state flows to be fully built first. |

## Validation

- Navigate to `frontend/` directory and execute `npm run test:coverage` (or equivalent command).
- Verify that the coverage report builds without errors.
- Open `frontend/coverage/index.html` in a browser and check coverage results.

## Acceptance Criteria

- [ ] `@vitest/coverage-v8` is installed as a development dependency in the frontend module.
- [ ] Command `npm run test:coverage` generates a valid coverage report.
- [ ] UI components (OperationSelector, Mp4UploadForm, UrlDownloadForm, etc.) achieve the 80% coverage threshold.
- [ ] Test suite runs successfully without failures.

## Risks

- Flaky component tests if asynchronous elements (like timers or fake network calls) are not properly cleaned up or mocked. Use `vi.useFakeTimers()` or standard Jest-like mocks in Vitest.

## Open Questions

- Should we enforce a hard threshold that fails the build if frontend coverage drops below 80%?

## Notes for the Implementing Agent

Keep test dependencies up to date with Vitest v4.x as used in `frontend/package.json`.
