# Task Breakdown: MVP Media Utility

## Status

Status: Confirmed

Last updated: 2026-06-03

Target task directory: `docs/tasks/mvp-media-utility/`

## Source Documents

| Source | Location or Reference | Type | Status | Notes |
| --- | --- | --- | --- | --- |
| Project planning | `docs/planning/project-planning.md` | Planning | Confirmed | Defines milestones and suggested task order. |
| Tech Spec | `docs/specs/tech-spec.md` | Tech Spec | Confirmed | Target technical architecture and verification plan. |
| Technology definition | `docs/architecture/technology-definition.md` | Tech Definition | Confirmed | Establishes the stack and environment choices. |

## Summary

This task breakdown covers the full implementation of the MVP for the Media Utility application, from initial project scaffolding to core features (media conversion, URL download, temporary storage, event tracking, and cleanup) and final packaging/verification.

## Task Strategy

The implementation is broken down into small, sequential increments:
1. **Foundation (001-003)**: Monolith scaffolding, boundary creation, database integration.
2. **Domain & Infrastructure (004-007)**: Domain entities, event models, storage service, process executor.
3. **API & UI (008-011)**: REST API contracts, UI form states, and views.
4. **Core Features (012-019)**: MP4 upload/convert flow, URL download flow, download delivery, and file cleanup.
5. **Operational Features (020-022)**: Metric tracking, Docker packaging, Docker Compose environment.
6. **Testing & Handoff (023-027)**: Backend/frontend test coverage, frontend/backend operation integration, critical E2E flows, runtime documentation.

## Task List

| Order | Task File | Goal | Dependencies | Status |
| --- | --- | --- | --- | --- |
| 001 | [001-scaffold-spring-boot-react-monolith.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/001-scaffold-spring-boot-react-monolith.md) | Scaffold initial monolithic structure | None | Ready |
| 002 | [002-create-backend-module-boundaries.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/002-create-backend-module-boundaries.md) | Enforce Java module packaging | 001 | Ready |
| 003 | [003-configure-mysql-jpa-persistence-baseline.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/003-configure-mysql-jpa-persistence-baseline.md) | Set up database connections and JPA | 002 | Ready |
| 004 | [004-create-operation-domain-model.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/004-create-operation-domain-model.md) | Create `Operation` entity | 003 | Ready |
| 005 | [005-create-operation-events-model.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/005-create-operation-events-model.md) | Create `OperationEvent` entity | 004 | Ready |
| 006 | [006-create-temporary-storage-service.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/006-create-temporary-storage-service.md) | Implement local disk temporary storage | 001 | Ready |
| 007 | [007-create-process-execution-adapter.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/007-create-process-execution-adapter.md) | Implement external CLI process runner | 001 | Ready |
| 008 | [008-define-rest-api-contracts.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/008-define-rest-api-contracts.md) | Define REST HTTP endpoints structure | 004 | Ready |
| 009 | [009-build-react-operation-selector-flow.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/009-build-react-operation-selector-flow.md) | Build React main operation flow selector | 001 | Ready |
| 010 | [010-build-mp4-upload-form-states.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/010-build-mp4-upload-form-states.md) | Build upload form states and errors UI | 009 | Ready |
| 011 | [011-build-url-download-form-states.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/011-build-url-download-form-states.md) | Build URL submission states UI | 009 | Ready |
| 012 | [012-implement-mp4-upload-validation.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/012-implement-mp4-upload-validation.md) | Validate file size and mime types | 008 | Ready |
| 013 | [013-implement-ffmpeg-conversion-adapter.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/013-implement-ffmpeg-conversion-adapter.md) | Core FFmpeg conversion adapter | 007 | Ready |
| 014 | [014-implement-conversion-operation-endpoint.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/014-implement-conversion-operation-endpoint.md) | Integrate MP4 conversion end to end | 006, 012, 013 | Ready |
| 015 | [015-implement-url-validation.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/015-implement-url-validation.md) | Validate URL formats and schemas | 008 | Ready |
| 016 | [016-implement-yt-dlp-download-adapter.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/016-implement-yt-dlp-download-adapter.md) | Core yt-dlp download adapter | 007 | Ready |
| 017 | [017-implement-url-download-endpoint.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/017-implement-url-download-endpoint.md) | Integrate URL download end to end | 006, 015, 016 | Ready |
| 018 | [018-implement-result-download-endpoint.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/018-implement-result-download-endpoint.md) | Enable download of generated media | 006, 008 | Ready |
| 019 | [019-implement-temporary-file-cleanup.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/019-implement-temporary-file-cleanup.md) | Scheduler to purge old temporary files | 006 | Ready |
| 020 | [020-add-operation-success-and-failure-tracking.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/020-add-operation-success-and-failure-tracking.md) | Record execution metrics in database | 005, 014, 017 | Ready |
| 021 | [021-create-docker-runtime-packaging.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/021-create-docker-runtime-packaging.md) | Write build and runtime Dockerfile | 001, 013, 016 | Depends on Previous Task |
| 022 | [022-add-local-development-compose-setup.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/022-add-local-development-compose-setup.md) | Orchestrate app & database locally | 021 | Depends on Previous Task |
| 023 | [023-add-backend-test-coverage.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/023-add-backend-test-coverage.md) | Integrate Jacoco and achieve 80% coverage | 020 | Depends on Previous Task |
| 024 | [024-add-frontend-test-coverage.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/024-add-frontend-test-coverage.md) | Enable Vitest coverage and achieve 80% coverage | 011 | Depends on Previous Task |
| 025 | [025-integrate-frontend-with-operation-apis.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/025-integrate-frontend-with-operation-apis.md) | Integrate frontend flows with backend operation APIs | 014, 017, 018 | Depends on Previous Task |
| 026 | [026-add-critical-e2e-flow-tests.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/026-add-critical-e2e-flow-tests.md) | Implement Playwright critical flow tests | 025, 022, 014, 017 | Blocked until 025 is completed |
| 027 | [027-document-mvp-runtime-and-operational-notes.md](file:///c:/Users/lucas.dourado/IdeaProjects/media-utility/docs/tasks/mvp-media-utility/027-document-mvp-runtime-and-operational-notes.md) | Document configurations, environments and limits | All tasks | Depends on Previous Task |

## Execution Order

The tasks should be executed sequentially matching their numerical order (001 -> 027). This ensures that structural layout and baseline requirements are met before integrating media dependencies, Docker configs, frontend/backend operation flows, testing pipelines, and operational notes.

## Blocked Tasks

MVP-MEDIA-026 is blocked until MVP-MEDIA-025 is implemented. Critical Playwright E2E tests require the UI to submit operations to backend APIs, render backend-driven operation states, and expose public result download links.

## Open Questions

None.

## Notes for Implementation

- Read the referenced source documents before executing each task.
- Ensure all tests run and pass before concluding any execution step.
