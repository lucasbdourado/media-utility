# Task: Add Backend Test Coverage

## Status

Status: Depends on Previous Task

Last updated: 2026-06-03

## Task ID

ID: MVP-MEDIA-023

Order: 023

Task file: `docs/tasks/mvp-media-utility/023-add-backend-test-coverage.md`

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Project planning | `docs/planning/project-planning.md` | Milestone 5, Suggested Task Order | Confirmed by source document | Proposes metrics and verification steps. |
| Tech Spec | `docs/specs/tech-spec.md` | Verification Plan, Automated Tests | Confirmed by source document | Recommends automated code coverage checks. |

## Context

To ensure the reliability and stability of the backend services, we need to track and report code coverage. Integrating Jacoco into the Maven build allows us to visualize code coverage and ensure no critical business logic paths are left untested.

## Goal

Configure Jacoco test coverage reporting for the Spring Boot application and improve tests to meet the target code coverage threshold (80% lines and branches).

## Scope

- Configure `jacoco-maven-plugin` in `pom.xml` under build plugins.
- Set up plugin executions for `prepare-agent` and `report` bound to the test/verify phases.
- Analyze the generated coverage report for gaps in test coverage across services, controllers, and adapters.
- Implement additional unit and integration tests to cover untested execution paths, focusing on edge cases, exception handling, and input validation rules.
- Maintain a minimum target of 80% instruction/branch coverage for the core packages (under `com.lucasdourado.mediautility`).

## Out of Scope

- Configuring frontend Vitest coverage (handled by Task 024).
- Creating E2E UI flow tests (handled by Task 025).

## Implementation Instructions

- Do not include auto-generated code, JPA entities, or pure exception classes (e.g., classes under `.operations` or exceptions) in the coverage rules if they skew statistics without adding logic value.
- Add the plugin setup to the `<build><plugins>` section of `pom.xml`.
- Ensure tests still execute successfully locally and within containers.

## Expected Files

| Path | Action | Source | Notes |
| --- | --- | --- | --- |
| `pom.xml` | Modify | Project Build Configuration | Add Jacoco Maven plugin. |
| `src/test/java/com/lucasdourado/mediautility/...` | Create / Modify | Backend Test Coverage | Write additional unit/integration tests. |

## Dependencies

| Dependency | Type | Status | Notes |
| --- | --- | --- | --- |
| MVP-MEDIA-020 | Previous task | Pending | Requires operational metrics/tracking code to be fully integrated first. |

## Validation

- Run `./mvnw clean verify` (or `mvnw.cmd clean verify` on Windows).
- Verify that the report is successfully generated at `target/site/jacoco/index.html`.
- Open `index.html` and verify the coverage percentages for classes, methods, and lines.

## Acceptance Criteria

- [ ] Jacoco plugin is configured and executes during Maven build verification.
- [ ] Code coverage reports are successfully generated in HTML format.
- [ ] Core business logic packages (services, validation, utilities) meet the target threshold of 80% code coverage.
- [ ] No regression is introduced in existing test suite.

## Risks

- Over-mocking in integration tests could result in false-positive high coverage without verifying actual bean interactions. Use real configurations and `@SpringBootTest` / `@WebMvcTest` where appropriate.

## Open Questions

- What specific packages or classes should be excluded from coverage checks (e.g., entity files)?

## Notes for the Implementing Agent

Verify the exact version of `jacoco-maven-plugin` is compatible with Java 21 (recommend version `0.8.11` or higher).
