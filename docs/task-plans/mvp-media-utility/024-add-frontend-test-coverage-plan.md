# Task Implementation Plan: Add Frontend Test Coverage

## Status

Status: Ready for Implementation

Last updated: 2026-06-04

Plan file: `docs/task-plans/mvp-media-utility/024-add-frontend-test-coverage-plan.md`

Architecture decision notes file: Not generated

## Task Reference

Task ID: `MVP-MEDIA-024`

Task file: `docs/tasks/mvp-media-utility/024-add-frontend-test-coverage.md`

Task status: `Depends on Previous Task`

Task group or feature: `mvp-media-utility`

## Planning Mode Requirement

Plan mode verified: Yes

Notes:

- This plan was created in plan mode.
- Implementation must not start during `plan-task`.
- A future implementation request must use this saved plan as source context.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/tasks/mvp-media-utility/024-add-frontend-test-coverage.md` | Goal, Scope, Out of Scope, Implementation Instructions, Validation, Acceptance Criteria, Open Questions | Confirmed by source document | Primary task definition for frontend coverage configuration and coverage-improving tests. |
| Project discovery | `docs/context/project-discover.md` | Project Scenario | Confirmed by source document | Required Harness discovery document exists, but it predates the current implementation codebase. |
| Project planning | `docs/planning/project-planning.md` | Milestone 5, End-to-End Validation, Suggested Task Order | Confirmed by source document | Confirms operational MVP validation phase and automated verification need. |
| ADR-002 | `docs/adrs/002-use-react-vite-typescript-served-by-spring-boot.md` | Decision, Consequences | Accepted | Confirms React, Vite, TypeScript, npm, and frontend/backend REST boundary. |
| ADR-003 | `docs/adrs/003-use-maven-npm-coordinated-asset-packaging.md` | Decision, Consequences | Accepted | Confirms npm scripts remain frontend source of truth and Maven coordinates frontend build/package flow. |
| Task 001 execution report | `docs/task-executions/mvp-media-utility/001-scaffold-spring-boot-react-monolith-execution.md` | Files Created, Tests Executed | Confirmed by source document | Confirms Vite, Vitest, React Testing Library, npm scripts, and `frontend/package-lock.json`. |
| Task 009 plan/execution | `docs/task-plans/mvp-media-utility/009-build-react-operation-selector-flow-plan.md`, `docs/task-executions/mvp-media-utility/009-build-react-operation-selector-flow-execution.md` | Frontend selector behavior | Confirmed by source document | Establishes operation selector behavior and shared state surface expectations. |
| Task 010 plan/execution | `docs/task-plans/mvp-media-utility/010-build-mp4-upload-form-states-plan.md`, `docs/task-executions/mvp-media-utility/010-build-mp4-upload-form-states-execution.md` | MP4 form states and tests | Confirmed by source document | Establishes MP4 file selection, invalid-file, required-file, and ready-state behavior. |
| Task 011 plan/execution | `docs/task-plans/mvp-media-utility/011-build-url-download-form-states-plan.md`, `docs/task-executions/mvp-media-utility/011-build-url-download-form-states-execution.md` | URL form states and tests | Confirmed by source document | Establishes URL validation, responsibility notice, and ready-state behavior. |
| Current codebase | `frontend/package.json` | Scripts and dependencies | Detected in codebase | Confirms Vitest 4.1.8, Vite 8.0.16, React Testing Library, and no coverage script/package yet. |
| Current codebase | `frontend/package-lock.json` | npm lockfile | Detected in codebase | Must be updated if a coverage package is installed. |
| Current codebase | `frontend/vite.config.ts` | Vitest configuration | Detected in codebase | Currently configures jsdom, globals, and setup file; no coverage section yet. |
| Current codebase | `frontend/src/App.tsx` | Frontend behavior | Detected in codebase | Current frontend behavior is concentrated in `App.tsx`, including selector, MP4 form, URL form, and helpers. |
| Current codebase | `frontend/src/App.test.tsx` | Existing component tests | Detected in codebase | Existing tests cover main happy/error states, but coverage task should add targeted tests for uncovered branches and helper-driven UI states. |
| Current codebase | `.gitignore` | Generated outputs | Detected in codebase | Ignores `frontend/dist` and `frontend/node_modules`, but not `frontend/coverage`. |
| PRD | `docs/product/prd.md` | Not available | Missing / documented limitation | File exists but is empty in the current workspace. |
| Tech Spec | `docs/specs/tech-spec.md` | Not available | Missing / documented limitation | File exists but is empty in the current workspace. |
| Technology definition | `docs/architecture/technology-definition.md` | Not available | Missing / documented limitation | File exists but is empty in the current workspace. |
| User decision | Current `plan-task` session | Source gap handling | Confirmed by user | Proceed using task file, accepted ADRs, prior frontend task artifacts, and current codebase while documenting empty files as limitations. |
| User decision | Current `plan-task` session | Coverage enforcement | Confirmed by user | Generate coverage reports and target 80% line/branch coverage, but do not configure coverage thresholds to hard-fail initially. |
| User decision | Current `plan-task` session | Dependency handling | Confirmed by user | Treat task 024 as ready because explicit frontend dependency `MVP-MEDIA-011` is satisfied; task 023 remains separate and out of scope. |

## Context Summary

The frontend is a Vite React TypeScript app with Vitest and React Testing Library already configured. The current UI behavior is implemented in `frontend/src/App.tsx` and tested in `frontend/src/App.test.tsx`. Task 024 adds frontend coverage tooling and expands focused component tests so coverage reports can identify gaps and the frontend can target at least 80% line and branch coverage.

## Task Goal

Configure Vitest frontend coverage reporting and add focused tests so `npm run test:coverage` generates an HTML coverage report for the frontend and the covered UI behavior targets at least 80% line and branch coverage without enforcing hard thresholds in the initial configuration.

## Confirmed Scope

- Install `@vitest/coverage-v8` as a frontend development dependency.
- Update `frontend/package.json` with a `test:coverage` script that runs Vitest with coverage.
- Update `frontend/package-lock.json` through npm dependency installation.
- Configure the Vitest `coverage` section in `frontend/vite.config.ts`.
- Use the V8 provider and generate reports including HTML output under `frontend/coverage`.
- Exclude standard non-product frontend files from coverage metrics, including `src/setupTests.ts`, declaration files, Vite environment declarations, entrypoint bootstrap where appropriate, build outputs, and test files.
- Do not configure hard coverage thresholds initially; use the report to validate the 80% line and branch target manually.
- Add or expand tests under `frontend/src/` to cover existing frontend behavior gaps, especially branch paths around form validation, selector changes, upload/download readiness, and utility/helper-driven UI behavior.
- Add `frontend/coverage/` to `.gitignore` so generated reports are not tracked.

## Out of Scope

- Do not configure backend coverage; task 023 owns backend coverage.
- Do not configure E2E tests; task 025 owns critical E2E flows.
- Do not implement new frontend product behavior, network requests, backend API calls, upload submission, URL submission, polling, result download, media processing, cleanup, metrics, or event tracking.
- Do not refactor the app into multiple components solely for coverage unless implementation discovers a small, clearly useful testability improvement.
- Do not change accepted ADRs, task files, PRD, project planning, technology-definition, or Tech Spec documents during implementation.

## Requirements Covered

| Requirement | Source | How This Task Covers It | Status |
| --- | --- | --- | --- |
| Frontend coverage reporting | Task file | Adds Vitest coverage provider, reports, and `test:coverage` script. | Confirmed |
| HTML coverage report under `frontend/coverage` | Task file | Configures HTML reporter and keeps generated output under the frontend coverage folder. | Confirmed |
| Additional UI component tests | Task file | Extends React Testing Library tests for form validation, selector changes, upload/download ready states, and branch coverage. | Confirmed |
| 80% line and branch target | Task file, user decision | Future implementation should use coverage report to reach at least 80% lines and branches, but without hard-failing thresholds initially. | Confirmed |
| Frontend-only task boundary | Task file, user decision | Keeps backend coverage and E2E setup out of scope. | Confirmed |
| npm scripts remain frontend source of truth | ADR-003 | Adds the coverage command to `frontend/package.json`. | Confirmed |

## Technical Specification Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Not available | Missing | None directly from Tech Spec content. | `docs/specs/tech-spec.md` exists but is empty. User confirmed proceeding with available task, ADR, prior frontend artifact, and codebase sources. |

Coverage assessment:

- Justifying Tech Spec section: unavailable because the Tech Spec file is empty.
- Tech Spec sections implemented by this task: none directly from Tech Spec content.
- Gaps between task and Tech Spec: exact frontend coverage exclusions, threshold enforcement behavior, and report-only policy are not specified in the Tech Spec.
- Dependencies not specified by the Tech Spec: resolved by task file, ADR-002, ADR-003, prior frontend task plans/executions, codebase evidence, and current user decisions.
- Source limitation handling: empty PRD, Tech Spec, and technology-definition files are documented limitations and do not block this task because the user confirmed proceeding with available binding sources.

## Architecture and ADR Considerations

| Decision or ADR | Source | Impact on This Task | Status |
| --- | --- | --- | --- |
| React/Vite/TypeScript frontend served by Spring Boot | ADR-002 | Coverage configuration belongs in the existing `frontend/` Vite/Vitest setup. | Accepted |
| npm scripts remain frontend source of truth | ADR-003 | Add `test:coverage` in `frontend/package.json`; future Maven/CI integration can invoke npm scripts rather than duplicating coverage config. | Accepted |
| Frontend/backend REST boundary | ADR-002, ADR-008 | Tests should not introduce or require real backend requests; any API behavior remains mocked or absent unless already implemented. | Accepted |
| Report-only coverage threshold policy | User decision | Do not configure hard failing `thresholds` in Vitest coverage for this task; document 80% as target verified from report. | Confirmed |
| Backend coverage separate | Task file, user decision | Task 023 is out of scope and does not block this frontend task. | Confirmed |
| Empty Tech Spec / PRD / technology-definition source gap | Source review, user decision | Proceed with documented limitations; no required ADR or architecture blocker for this test coverage task. | Resolved by user |

ADR candidates or architecture decisions needed:

- None. No new formal ADR is required.
- The report-only threshold behavior is a task-level validation policy decision, not an architecture decision.

Architecture decision notes:

- Saved separately: No
- Path: Not generated
- Notes file status: Not applicable

## Confirmed Decisions

- The selected task is `MVP-MEDIA-024`.
- The plan path is `docs/task-plans/mvp-media-utility/024-add-frontend-test-coverage-plan.md`.
- No architecture decision notes file is generated for this task.
- Empty PRD, Tech Spec, and technology-definition files are documented limitations, not blockers.
- Binding sources are task 024, project planning, ADR-002, ADR-003, prior frontend task plans/executions, codebase evidence, and current user decisions.
- Treat `MVP-MEDIA-011` as the relevant frontend dependency; it is completed and the task is ready despite task 023 being separate and not executed.
- Configure coverage reporting but do not hard-fail on thresholds initially.
- Still use the generated report to target at least 80% line and branch coverage.
- Generated coverage output should be ignored by git through `frontend/coverage/`.
- Keep this task frontend-only and testing/tooling-focused.

## Pending Decisions

None. All task-relevant decisions have been answered or explicitly deferred out of scope by the user.

## Questions for the User

None. All task-relevant questions have been answered.

## Proposed Implementation Approach

1. Re-read this plan, task 024, ADR-002, ADR-003, relevant prior frontend task artifacts, and current frontend test/config files.
2. Install `@vitest/coverage-v8` in `frontend/` as a dev dependency so `frontend/package.json` and `frontend/package-lock.json` are updated together.
3. Add `test:coverage` to `frontend/package.json` as `vitest run --coverage`.
4. Update `frontend/vite.config.ts` with a Vitest `coverage` block using the V8 provider and HTML report output under `coverage`.
5. Configure coverage exclusions for test files, setup files, declaration files, Vite environment declarations, build outputs, and bootstrap files that do not contain meaningful product logic.
6. Do not set `coverage.thresholds` in this task because the user chose report-only threshold handling.
7. Extend `frontend/src/App.test.tsx` or create focused colocated tests under `frontend/src/` to cover current UI branches without changing product behavior.
8. Prefer testing through accessible UI interactions with React Testing Library and existing `fireEvent`; do not add test dependencies unless implementation discovers a concrete blocker.
9. Run the normal frontend tests and coverage command.
10. Use the coverage summary/report to identify any remaining line/branch gaps and add focused tests for existing behavior until line and branch coverage reach at least 80% or any environment/tooling blocker is documented.
11. Run typecheck/build if config or TypeScript changes make them relevant.
12. Scope-review the diff to ensure no backend, E2E, API integration, or product behavior work was added.

## Files and Areas Expected to Change

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| `frontend/package.json` | Modify | Task file, ADR-003 | Add `@vitest/coverage-v8` dev dependency and `test:coverage` script. |
| `frontend/package-lock.json` | Modify | npm dependency management | Update lockfile through npm install. |
| `frontend/vite.config.ts` | Modify | Task file, current Vitest config | Add report-only coverage configuration with V8 provider and reporters. |
| `frontend/src/App.test.tsx` or colocated frontend tests | Modify / Create | Task file, current codebase | Add focused coverage for form validation branches and UI state paths. |
| `.gitignore` | Modify | Generated report hygiene | Add `frontend/coverage/` if not already ignored. |
| `frontend/coverage/` | Generated output only | Coverage validation | Generated by `npm run test:coverage`; should not be committed. |

## Step-by-Step Implementation Plan

1. Verify working tree state before editing and preserve unrelated user changes.
2. Inspect `frontend/package.json`, `frontend/package-lock.json`, `frontend/vite.config.ts`, `frontend/src/App.tsx`, and `frontend/src/App.test.tsx`.
3. From `frontend/`, install the coverage provider with npm:
   - `npm install --save-dev @vitest/coverage-v8`
4. Update `frontend/package.json` scripts:
   - keep existing `test`, `typecheck`, and `build`;
   - add `"test:coverage": "vitest run --coverage"`.
5. In `frontend/vite.config.ts`, add `test.coverage` configuration:
   - `provider: "v8"`;
   - reporters including `text`, `html`, and optionally `lcov`;
   - reports directory `coverage`;
   - include source files under `src`;
   - exclude tests, setup, declarations, `vite-env.d.ts`, `main.tsx`, generated/build output, and `node_modules`;
   - no hard `thresholds` field.
6. Update `.gitignore` to include `frontend/coverage/`.
7. Run the existing frontend test suite once to establish the current test status:
   - `npm run test` from `frontend/`.
8. Run coverage:
   - `npm run test:coverage` from `frontend/`.
9. Review the terminal coverage summary and, if needed, inspect the HTML report at `frontend/coverage/index.html` locally to identify uncovered lines/branches.
10. Add focused tests for existing behavior. Prioritize:
   - no-file submit branch for MP4 conversion;
   - invalid file branch and clearing of selected-file/ready state;
   - valid MP4 extension branch and `video/mp4` MIME branch if not both covered;
   - minimum file-size display branch for files under 0.1 MB;
   - URL empty, invalid protocol, malformed URL, valid HTTP, and valid HTTPS branches;
   - selector switching preserving expected accessible state;
   - ready messages clearing when URL input changes;
   - shared state surface rendering.
11. Keep tests behavior-focused. Do not import or test private helper functions directly unless implementation intentionally extracts them to a small tested utility as a local maintainability improvement.
12. Re-run `npm run test:coverage`.
13. If line or branch coverage is below 80%, add more focused tests for existing branches until both reach at least 80%, or document an environment/tool limitation in the future execution report.
14. Run `npm run typecheck` from `frontend/`.
15. Run `npm run build` from `frontend/` because `vite.config.ts` and package metadata changed.
16. Perform a final scope review:
   - no backend files changed except `.gitignore` if needed;
   - no E2E framework/config added;
   - no product behavior added or changed except test-only coverage if unavoidable and justified;
   - no API requests, media processing, polling, cleanup, metrics, or event tracking added.
17. Record in the future execution report the coverage summary, exact validation commands, and whether 80% lines and branches were achieved.

## Validation Strategy

- Run `npm run test` from `frontend/`.
- Run `npm run test:coverage` from `frontend/`.
- Verify the coverage command generates `frontend/coverage/index.html`.
- Verify the coverage summary shows at least 80% line coverage and at least 80% branch coverage.
- Because thresholds are report-only, manually inspect/report the percentages rather than relying on Vitest to fail the command.
- Run `npm run typecheck` from `frontend/`.
- Run `npm run build` from `frontend/`.
- Review generated files and git status to ensure `frontend/coverage/` is untracked/ignored and not committed.
- Static scope review should confirm no backend coverage, E2E setup, API integration, or product behavior work was added.

## Tests to Add or Update

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| Coverage script smoke validation | Tooling / Manual | Verify `npm run test:coverage` executes and generates HTML coverage output. | Not a committed test; validation command for execution. |
| MP4 no-file and invalid-file branches | Frontend component | Cover conversion form validation branches and ready-state inactivity. | Extend existing `App.test.tsx` where practical. |
| MP4 valid-file branches | Frontend component | Cover `.mp4` extension and/or `video/mp4` MIME acceptance and selected-file display. | Include small-file branch if uncovered by current tests. |
| URL validation branches | Frontend component | Cover empty URL, malformed URL, non-HTTP protocol, valid HTTP/HTTPS, ready-state, and reset behavior. | Existing tests cover several paths; add missing branch-focused cases. |
| Operation selector and shared state surfaces | Frontend component | Preserve selector accessibility and shared state rendering coverage. | Keep existing assertions or consolidate carefully. |
| Coverage output exclusion review | Manual/static | Verify excluded files do not skew coverage metrics. | Confirm setup/declaration/bootstrap files are excluded as configured. |

## Acceptance Criteria

- [ ] `@vitest/coverage-v8` is installed as a development dependency in `frontend/package.json`.
- [ ] `frontend/package-lock.json` is updated consistently with the installed coverage package.
- [ ] `frontend/package.json` includes `npm run test:coverage` via `vitest run --coverage`.
- [ ] `frontend/vite.config.ts` configures Vitest coverage with the V8 provider and HTML report generation under `frontend/coverage`.
- [ ] Coverage excludes non-product files such as test files, setup files, declarations, Vite environment declarations, bootstrap entrypoint, generated outputs, and dependencies.
- [ ] Coverage thresholds are not hard-failing in Vitest config for this initial task, per user decision.
- [ ] Frontend tests are expanded to cover existing UI component branches and utility-driven UI states around selector changes, MP4 form validation, URL form validation, upload/download readiness, and shared state surfaces.
- [ ] `npm run test` passes from `frontend/`.
- [ ] `npm run test:coverage` passes from `frontend/` and generates `frontend/coverage/index.html`.
- [ ] Coverage summary reaches at least 80% line coverage and at least 80% branch coverage, or any blocker is explicitly documented in the future execution report.
- [ ] `npm run typecheck` and `npm run build` pass from `frontend/`.
- [ ] `frontend/coverage/` is ignored and generated coverage files are not committed.
- [ ] No backend coverage, E2E setup, API integration, or product behavior work is added.

## Risks and Edge Cases

- Report-only thresholds mean coverage regressions will not fail automatically; the execution report must record the coverage percentages and the future CI/build policy can harden this later.
- Branch coverage can remain low if helper branches are only indirectly tested; implementation should add behavior-level tests for both accepted and rejected form paths before considering helper extraction.
- Excluding too much can inflate coverage; exclusions should be limited to setup, declarations, bootstrap, tests, dependencies, and generated output.
- Excluding too little can skew coverage with files that do not contain meaningful product logic, such as declarations or test setup.
- `@vitest/coverage-v8` must be compatible with the installed Vitest 4.1.8; npm should resolve a compatible version through the lockfile.
- Generated `frontend/coverage/` output can be large and should remain ignored.
- The current frontend is mostly in `App.tsx`; broad component refactors would expand task scope and should be avoided unless a small extraction clearly improves testability without changing behavior.

## Rollback or Recovery Notes

Rollback should be limited to reverting frontend coverage configuration, package metadata, test changes, and `.gitignore` additions from this task. Remove generated `frontend/coverage/` output if it exists locally; it should not be tracked.

## Documentation Updates

- Do not update PRD, project planning, technology definition, Tech Spec, final ADRs, task files, or unrelated documents during implementation.
- A future task execution report should document the coverage configuration, added tests, commands run, coverage percentages, generated report path, and confirmation that thresholds are report-only.

## Implementation Readiness Checklist

- [x] Task scope is clear.
- [x] Source documents were reviewed.
- [x] Tech Spec coverage was verified as unavailable and documented.
- [x] Architecture decisions were checked.
- [x] ADR candidates were confirmed, rejected, required-before-implementation, or explicitly deferred by the user.
- [x] Pending decisions are resolved or explicitly deferred out of this task by the user.
- [x] User questions were answered.
- [x] Expected files or areas are identified.
- [x] Acceptance criteria are defined.
- [x] Test strategy is defined.
- [x] Risks and edge cases are documented.

## Notes for the Implementing Agent

- Do not execute this task without using this saved plan as source context.
- Keep this task frontend-only.
- Treat task 023 backend coverage and task 025 E2E tests as out of scope.
- Use `@vitest/coverage-v8` with the existing Vitest setup.
- Do not add hard coverage thresholds in `vite.config.ts`; the user chose report-only enforcement for this task.
- Still use the coverage summary to reach and report at least 80% line and branch coverage.
- Prefer behavior-level React Testing Library tests over direct private-helper tests.
- Do not add API calls, backend behavior, media processing, polling, cleanup, metrics, or event tracking.
