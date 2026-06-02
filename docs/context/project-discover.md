# Project Discovery

## Purpose

This document records the initial discovery context for the project so later Harness workflows can operate from explicit, user-confirmed information instead of assumptions.

The next Harness step is `project-structure`, which must use this discovery document as its input.

## Discovery Status

Status: Draft

Last updated: 2026-05-31

## Project Scenario

Scenario: Greenfield / No Codebase Available

Codebase status: Not yet created

Next workflow mode: Planning/conceptual analysis

Evidence basis:

- Codebase evidence: None. The user confirmed this is a greenfield project with no available codebase.
- User-provided context: Discovery interview conducted in Portuguese.
- Documents or planning sources: None provided.

## Discovery Interview

Record the user-provided answers gathered before generating this document.

- Project scenario confirmed by user: Greenfield.
- Project location provided by user: This repository.
- Codebase availability confirmed by user: No codebase available yet.
- Project purpose provided by user: Not provided.
- Existing documentation provided by user: None.
- Related repositories, systems, or integrations provided by user: None.
- Greenfield objective, scope, constraints, deliverables, and open decisions provided by user:
  - Initial scope: None.
  - Expected deliverables: None.
  - Constraints: N/A.
  - Mandatory technologies: N/A.
  - Prohibited technologies: N/A.
  - Open decisions: None.

## Project Location

- Local path: `C:\Users\lucas.dourado\IdeaProjects\harness-project`
- Remote repository URL: Not provided.
- Storage location: This repository.
- Availability: Local repository available for storing Harness context.

## Project Summary

Not provided.

## Business Context

Not provided.

## Available Sources

- User discovery interview.

No PRD, planning notes, product documentation, technical documentation, design docs, diagrams, tickets, or other documents were provided during discovery.

## Greenfield Context

Use this section only when the scenario is `Greenfield / No Codebase Available`.

- Project objective: Not provided.
- Business domain: Not provided.
- Initial scope: None.
- Expected deliverables: None.
- Known requirements: Not provided.
- Constraints: N/A.
- Mandatory technologies explicitly provided by the user: N/A.
- Prohibited technologies explicitly provided by the user: N/A.
- Decisions still open: None provided.
- Notes for planning/conceptual analysis: Later workflows must preserve the missing context as unknown and ask follow-up questions before producing concrete planning, technology, architecture, or implementation decisions.

## Repositories and Related Systems

No implementation repositories, related repositories, dependency repositories, microservices, integrations, or modules were provided during discovery.

## Main Repository

- Name: `harness-project`
- Location: `C:\Users\lucas.dourado\IdeaProjects\harness-project`
- Availability: Available locally as the storage location for Harness context.
- Notes: This repository is currently used to store the Harness project context. No implementation codebase was identified for the greenfield project.

## Related Repositories

| Repository | Location | Relationship | Availability | Notes |
| --- | --- | --- | --- | --- |
| None provided | N/A | N/A | N/A | The user confirmed there are no related repositories. |

## Microservices

| Service | Repository or Location | Responsibility | Availability | Notes |
| --- | --- | --- | --- | --- |
| None provided | N/A | N/A | N/A | The user confirmed there are no microservices to include in discovery. |

## Internal Libraries or Dependencies

| Name | Location | Relationship | Availability | Notes |
| --- | --- | --- | --- | --- |
| None provided | N/A | N/A | N/A | The user confirmed there are no dependency repositories or internal libraries to include in discovery. |

## External Integrations

| Integration | Purpose | Criticality | Notes |
| --- | --- | --- | --- |
| None provided | N/A | N/A | The user confirmed there are no integrations to include in discovery. |

## Known Technologies

| Technology | Area | Status | Source | Notes |
| --- | --- | --- | --- | --- |
| None provided | N/A | N/A | User discovery interview | No mandatory, prohibited, planned, or confirmed technologies were provided. |

## Analysis Scope

The next structural analysis should operate in planning/conceptual mode because the project is greenfield and no codebase exists yet.

The next step should include only:

- this discovery document;
- future documents explicitly added to the repository;
- user-provided context confirmed after this discovery.

## Out of Scope

- Codebase inspection for an implementation project, because no implementation codebase exists yet.
- Related repositories, dependency repositories, microservices, integrations, or modules, because none were provided.
- Technology decisions, architecture decisions, ADRs, planning commitments, or implementation work without additional confirmed context.

## Existing Documentation

| Document | Location | Type | Relevance | Notes |
| --- | --- | --- | --- | --- |
| None provided | N/A | N/A | N/A | The user confirmed there are no existing documents to consider. |

## Open Questions

- What is the project purpose?
- What business domain or product context should this project support?
- What problem should this project solve?
- What requirements, deliverables, or planning inputs should be created or collected before later Harness workflows?

## Inputs for the Next Step

`project-structure` must use this document as its primary input.

Because this is a greenfield project, `project-structure` must produce a conceptual/proposed structure and must not attempt to analyze nonexistent source files or implementation repositories.

## Notes for `project-structure`

- Operate in planning/conceptual mode.
- Do not infer project purpose, business domain, requirements, technologies, modules, repositories, or dependencies.
- Ask objective follow-up questions before proposing concrete structure if the missing context prevents useful output.
