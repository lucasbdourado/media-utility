# Task Implementation Plan: Add Backend Test Coverage

## Status

Status: Ready for Implementation

Last updated: 2026-06-03

Plan file: `docs/task-plans/mvp-media-utility/023-add-backend-test-coverage-plan.md`

Architecture decision notes file: Not generated

## Task Reference

Task ID: `MVP-MEDIA-023`

Task file: `docs/tasks/mvp-media-utility/023-add-backend-test-coverage.md`

Task status: `Depends on Previous Task`

Task group or feature: `mvp-media-utility`

## Planning Mode Requirement

Plan mode verified: Yes

Notes:

- This plan must be created in plan mode.
- Implementation must not start during `plan-task`.
- A future implementation request must use this saved plan as source context.

## Source Documents

| Source | Location or Reference | Relevant Section | Status | Notes |
| --- | --- | --- | --- | --- |
| Task file | `docs/tasks/mvp-media-utility/023-add-backend-test-coverage.md` | Full document | Confirmed by source document | Defines Jacoco setup, 80% target, scope, exclusions, acceptance criteria. |
| Project planning | `docs/planning/project-planning.md` | Milestone 5, Suggested Task Order | Confirmed by source document | Proposes metrics and verification steps. |
| Tech Spec | `docs/specs/tech-spec.md` | — | Missing | Tech Spec file is empty. Coverage not specified. Proceeding based on task file and user decisions. |
| Codebase analysis | `src/main/java/com/lucasdourado/mediautility/` | Full source tree | Detected in codebase | 49 source files across 8 packages. |
| Codebase analysis | `src/test/java/com/lucasdourado/mediautility/` | Full test tree | Detected in codebase | 14 test files covering api, cleanup, media, operations, storage. |
| `pom.xml` | `pom.xml` | `<build><plugins>` | Detected in codebase | No Jacoco plugin currently configured. Uses `spring-boot-starter-webmvc-test` for testing. |
| User decisions | Current plan-task session | Exclusions, enforcement, test scope | Confirmed by user | See Confirmed Decisions section. |

## Context Summary

The media-utility backend is a Spring Boot 4.0.6 application with Java 21. It has 49 source files and 14 test files. Tests use JUnit 5, Mockito, AssertJ, `@WebMvcTest`, and `@TempDir`. JPA auto-configuration is excluded in tests — all repositories are mocked. No code coverage tooling is currently configured in the Maven build. This task adds Jacoco for coverage reporting and identifies whether additional tests are needed to meet the 80% line/branch target.

## Task Goal

Configure the `jacoco-maven-plugin` in `pom.xml` for test coverage report generation (report-only mode, no build failure enforcement), with appropriate exclusions for non-business-logic classes. After generating the initial report, add tests only if clearly needed to reach the 80% coverage target for the included packages.

## Confirmed Scope

- Add `jacoco-maven-plugin` to the `<build><plugins>` section of `pom.xml`.
- Configure `prepare-agent` execution bound to the test phase.
- Configure `report` execution bound to the `verify` phase.
- Configure exclusion patterns to skip non-business-logic classes from coverage measurement.
- Run `mvnw clean verify` and generate the HTML coverage report at `target/site/jacoco/index.html`.
- Review the generated report to identify whether existing tests already meet 80% for included packages.
- Add tests only if clearly needed to reach 80% instruction/branch coverage for included packages.

## Out of Scope

- Configuring frontend Vitest coverage (handled by Task 024).
- Creating E2E UI flow tests (handled by Task 025).
- Enforcing the 80% threshold as a build-failure gate.
- Creating a dedicated `BackgroundConversionExecutorTest` (user deferred — existing `OperationServiceTest` coverage is sufficient).
- Creating a dedicated `OperationTrackerTest` (user deferred — indirect coverage through service/executor tests is sufficient).
- Adding edge-case and exception-handling tests proactively (only add tests if clearly needed for 80%).

## Requirements Covered

| Requirement | Source | How This Task Covers It | Status |
| --- | --- | --- | --- |
| Code coverage reports for backend | Project planning, Milestone 5 | Jacoco plugin generates HTML reports | Confirmed |
| 80% coverage target | Task file | Reports measure coverage; tests added only if needed to reach target | Confirmed |
| Automated coverage checks | Task file (Tech Spec reference) | Jacoco `report` goal runs during `verify` phase | Confirmed |

## Technical Specification Coverage

| Tech Spec Section | Coverage | Implemented by This Task | Gaps or Notes |
| --- | --- | --- | --- |
| Verification Plan, Automated Tests | Missing | This task addresses automated coverage checks | Tech Spec file is empty. Task proceeds based on task file and user decisions. |

Coverage assessment:

- Justifying Tech Spec section: Not available (Tech Spec is empty).
- Tech Spec sections implemented by this task: N/A.
- Gaps between task and Tech Spec: The Tech Spec is empty; no conflict exists. The task file provides sufficient scope definition.
- Dependencies not specified by the Tech Spec: None.

## Architecture and ADR Considerations

| Decision or ADR | Source | Impact on This Task | Status |
| --- | --- | --- | --- |
| No architecture decisions required | N/A | This is a build tooling and test improvement task | Not applicable |

ADR candidates or architecture decisions needed:

- None. This task adds build-time tooling only.

Architecture decision notes:

- Saved separately: No
- Path: Not generated
- Notes file status: Not applicable

## Confirmed Decisions

- **Jacoco coverage exclusions**: Exclude all non-business-logic classes: main class, JPA entities, enums, Spring Data interfaces, records, boundary marker interfaces, exception classes, DTOs, and package-info files. Confirmed by user.
- **Build enforcement**: Report-only mode. Do not fail the build if coverage is below 80%. Confirmed by user.
- **BackgroundConversionExecutorTest**: Not required. Existing `OperationServiceTest` coverage is sufficient. Confirmed by user.
- **OperationTrackerTest**: Not required. Indirect coverage through service/executor tests is sufficient. Confirmed by user.
- **Edge-case tests**: Do not add proactively. Add tests only if clearly needed to reach 80% coverage on included packages. Confirmed by user.
- **Jacoco plugin version**: Use the latest stable version ≥0.8.11 available at implementation time. Confirmed by user.

## Pending Decisions

None. All task-relevant decisions have been answered or explicitly deferred out of scope by the user.

## Questions for the User

None. All task-relevant questions have been answered.

## Proposed Implementation Approach

1. Add the `jacoco-maven-plugin` to `pom.xml` with `prepare-agent` and `report` executions.
2. Configure exclusion patterns covering all non-business-logic classes identified during planning.
3. Run `mvnw clean verify` to generate the initial coverage report.
4. Review the HTML report at `target/site/jacoco/index.html` to assess whether included packages already meet 80%.
5. If any included package is clearly below 80%, add targeted unit tests for the specific under-covered methods/branches.
6. Re-run `mvnw clean verify` and confirm the 80% target is met for included packages.

## Files and Areas Expected to Change

| Path or Area | Expected Action | Source | Notes |
| --- | --- | --- | --- |
| `pom.xml` | Modify | Task file | Add `jacoco-maven-plugin` under `<build><plugins>`. |
| `src/test/java/com/lucasdourado/mediautility/...` | Create / Modify (conditional) | Task file | Only if coverage report shows included packages below 80%. |
| `target/site/jacoco/index.html` | Generated output | Jacoco plugin | Verify the report is generated and readable. |

## Step-by-Step Implementation Plan

### Step 1: Add Jacoco Maven Plugin to `pom.xml`

Add the `jacoco-maven-plugin` to the `<build><plugins>` section of `pom.xml`.

Configuration:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>LATEST_STABLE_VERSION</version> <!-- ≥0.8.11, pick latest at implementation time -->
    <executions>
        <execution>
            <id>prepare-agent</id>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>verify</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <excludes>
            <!-- Main class -->
            <exclude>com/lucasdourado/mediautility/MediaUtilityApplication.class</exclude>
            <!-- JPA Entities -->
            <exclude>com/lucasdourado/mediautility/operations/Operation.class</exclude>
            <exclude>com/lucasdourado/mediautility/operations/OperationEvent.class</exclude>
            <exclude>com/lucasdourado/mediautility/operations/ResultFileMetadata.class</exclude>
            <!-- Enums -->
            <exclude>com/lucasdourado/mediautility/operations/OperationStatus.class</exclude>
            <exclude>com/lucasdourado/mediautility/operations/OperationType.class</exclude>
            <exclude>com/lucasdourado/mediautility/operations/OperationEventType.class</exclude>
            <exclude>com/lucasdourado/mediautility/api/PublicErrorCode.class</exclude>
            <!-- Spring Data interfaces -->
            <exclude>com/lucasdourado/mediautility/persistence/OperationRepository.class</exclude>
            <exclude>com/lucasdourado/mediautility/persistence/OperationEventRepository.class</exclude>
            <!-- Records / DTOs -->
            <exclude>com/lucasdourado/mediautility/api/OperationLinks.class</exclude>
            <exclude>com/lucasdourado/mediautility/api/PublicErrorDetail.class</exclude>
            <exclude>com/lucasdourado/mediautility/api/PublicErrorResponse.class</exclude>
            <exclude>com/lucasdourado/mediautility/api/PublicOperationResponse.class</exclude>
            <exclude>com/lucasdourado/mediautility/api/PublicResultMetadata.class</exclude>
            <exclude>com/lucasdourado/mediautility/api/UrlDownloadRequest.class</exclude>
            <exclude>com/lucasdourado/mediautility/media/process/ProcessExecutionRequest.class</exclude>
            <exclude>com/lucasdourado/mediautility/media/process/ProcessExecutionResult.class</exclude>
            <!-- Exception classes -->
            <exclude>com/lucasdourado/mediautility/api/ApiException.class</exclude>
            <exclude>com/lucasdourado/mediautility/media/conversion/ConversionException.class</exclude>
            <exclude>com/lucasdourado/mediautility/media/conversion/Mp4ValidationException.class</exclude>
            <exclude>com/lucasdourado/mediautility/media/download/DownloadException.class</exclude>
            <exclude>com/lucasdourado/mediautility/media/download/UrlValidationException.class</exclude>
            <exclude>com/lucasdourado/mediautility/media/process/ProcessExecutionException.class</exclude>
            <exclude>com/lucasdourado/mediautility/storage/StorageException.class</exclude>
            <!-- Boundary marker interfaces -->
            <exclude>com/lucasdourado/mediautility/api/ApiBoundary.class</exclude>
            <exclude>com/lucasdourado/mediautility/cleanup/CleanupBoundary.class</exclude>
            <exclude>com/lucasdourado/mediautility/observability/ObservabilityBoundary.class</exclude>
            <exclude>com/lucasdourado/mediautility/operations/OperationsBoundary.class</exclude>
            <exclude>com/lucasdourado/mediautility/persistence/PersistenceBoundary.class</exclude>
            <exclude>com/lucasdourado/mediautility/storage/StorageBoundary.class</exclude>
            <exclude>com/lucasdourado/mediautility/media/conversion/ConversionBoundary.class</exclude>
            <exclude>com/lucasdourado/mediautility/media/download/DownloadBoundary.class</exclude>
            <exclude>com/lucasdourado/mediautility/media/process/ProcessExecutionBoundary.class</exclude>
            <!-- Interfaces (contracts, no implementation logic) -->
            <exclude>com/lucasdourado/mediautility/api/OperationApiPort.class</exclude>
            <exclude>com/lucasdourado/mediautility/media/conversion/Mp4ToMp3Converter.class</exclude>
            <exclude>com/lucasdourado/mediautility/media/download/UrlDownloader.class</exclude>
            <exclude>com/lucasdourado/mediautility/media/process/ProcessExecutor.class</exclude>
            <exclude>com/lucasdourado/mediautility/storage/TemporaryStorageService.class</exclude>
        </excludes>
    </configuration>
</plugin>
```

The `<excludes>` in Jacoco's `<configuration>` at the plugin level apply to both instrumentation (`prepare-agent`) and reporting (`report`). This means excluded classes will not be instrumented, will not appear in reports, and will not count toward or against any coverage calculations.

### Step 2: Run Initial Coverage Report

Execute:

```bash
.\mvnw clean verify -DskipFrontendBuild=true
```

Or if the frontend build cannot be easily skipped:

```bash
.\mvnw clean verify
```

Verify:
- Build succeeds.
- Report is generated at `target/site/jacoco/index.html`.
- Open the report and check instruction/branch coverage percentages for the included packages.

### Step 3: Assess Coverage Gaps

Review the report for each included package/class:
- `api`: `OperationApiController`, `OperationService`, `BackgroundConversionExecutor`, `BackgroundDownloadExecutor`, `ApiExceptionHandler`
- `cleanup`: `TemporaryFileCleanupService`
- `media.conversion`: `FfmpegMp4ToMp3Converter`, `Mp4UploadValidator`
- `media.download`: `UrlDownloadValidator`, `YtDlpUrlDownloader`
- `media.process`: `LocalJvmProcessExecutor`
- `storage`: `LocalFilesystemTemporaryStorageService`

Identify any class or method where line/branch coverage is clearly below 80%.

### Step 4: Add Tests (Conditional)

Only if Step 3 reveals specific included classes below 80%:

- Write targeted unit tests for the under-covered methods.
- Follow existing test patterns: JUnit 5 + Mockito for service/component tests, `@WebMvcTest` for controller tests, `@TempDir` for file-system tests.
- Do not create new test classes unless existing tests cannot cover the gap.
- Focus on the specific branches or lines identified in the report.

### Step 5: Re-verify Coverage

If new tests were added:
- Re-run `.\mvnw clean verify`.
- Re-check the report at `target/site/jacoco/index.html`.
- Confirm 80% coverage is met for the included core packages.

## Validation Strategy

- Run `.\mvnw clean verify` (or `mvnw.cmd clean verify` on Windows).
- Verify the build succeeds without errors.
- Verify the Jacoco report is generated at `target/site/jacoco/index.html`.
- Open the HTML report and verify coverage percentages for included classes.
- Confirm no regression in existing test suite (all existing tests still pass).
- If tests were added, confirm they follow existing project test patterns.

## Tests to Add or Update

| Test or Area | Type | Purpose | Notes |
| --- | --- | --- | --- |
| Existing test suite | Unit / Integration | Verify no regressions | All existing tests must still pass. |
| Targeted coverage tests (conditional) | Unit | Close specific coverage gaps below 80% | Only if the initial report shows included packages below 80%. |

## Acceptance Criteria

- [x] Jacoco plugin is configured and executes during Maven build verification.
- [ ] Code coverage reports are successfully generated in HTML format at `target/site/jacoco/index.html`.
- [ ] Core business logic packages (services, validation, adapters, controllers, cleanup, storage) meet the target threshold of 80% code coverage for included classes.
- [ ] No regression is introduced in the existing test suite.

## Risks and Edge Cases

- **Frontend build dependency**: The Maven build includes frontend npm install/build steps via `exec-maven-plugin`. If the frontend build fails or is unavailable, it may block `mvnw clean verify`. Consider skipping the frontend phase during coverage analysis if a skip profile or property exists.
- **Over-mocking**: All JPA tests use mocked repositories. This means Jacoco will measure code paths through the service layer with mocks, which provides instruction coverage but not true integration validation. This is acceptable for this task — integration testing is a separate concern.
- **Jacoco + Spring Boot 4.x + Java 21 compatibility**: Ensure the selected Jacoco version supports Java 21 bytecode. Version 0.8.11+ is confirmed compatible.
- **Large exclusion list maintenance**: The explicit exclusion list is verbose but precise. If new classes are added in the future, they will be included in coverage by default (safe-by-default behavior).

## Rollback or Recovery Notes

- The only production file changed is `pom.xml`. Rollback is a single revert of the Jacoco plugin block.
- Any new test files are additive and do not modify existing source code.
- No database, configuration, or runtime changes are involved.

## Documentation Updates

- None required. The `pom.xml` change is self-documenting.

## Implementation Readiness Checklist

- [x] Task scope is clear.
- [x] Source documents were reviewed.
- [x] Tech Spec coverage was verified (Tech Spec is empty; task file provides sufficient scope).
- [x] Architecture decisions were checked (none required).
- [x] ADR candidates were confirmed, rejected, required-before-implementation, or explicitly deferred by the user (none identified).
- [x] Pending decisions are resolved or explicitly deferred out of this task by the user.
- [x] User questions were answered.
- [x] Expected files or areas are identified.
- [x] Acceptance criteria are defined.
- [x] Test strategy is defined.
- [x] Risks and edge cases are documented.

## Notes for the Implementing Agent

- **Do not add `jacoco:check` goal or coverage enforcement rules.** The user explicitly decided on report-only mode.
- **Do not create `BackgroundConversionExecutorTest` or `OperationTrackerTest`.** The user deferred these — existing indirect coverage is sufficient.
- **Do not proactively add edge-case tests.** Only add tests if the initial Jacoco report clearly shows included packages below 80%.
- **Jacoco version**: Use the latest stable version ≥0.8.11 at implementation time. Check Maven Central for the latest release.
- **Exclusion approach**: Exclusions are set at the plugin-level `<configuration>` block, which applies to both `prepare-agent` (instrumentation) and `report` generation. Excluded classes will not appear in the coverage report at all.
- **Frontend build**: If the frontend build blocks `mvnw clean verify`, check whether a profile or property exists to skip it. The existing `exec-maven-plugin` runs `npm install` and `npm run build` during `generate-resources`. You may need to skip this temporarily for coverage analysis.
- **Read the existing test files** before writing any new tests. Follow the established patterns: Mockito for service tests, `@WebMvcTest` for controller tests, `@TempDir` for filesystem tests, AssertJ for assertions.
- **The `spring-boot-starter-webmvc-test` dependency** already provides `@WebMvcTest`, `MockMvc`, and related test infrastructure. No additional test dependencies should be needed.
