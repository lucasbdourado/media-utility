# ADR-008: Define Public REST API Contract

## Status

Status: Accepted

Date: 2026-06-02

## Context

MVP-MEDIA-008 is blocked because the public REST wire contract for conversion, URL download, operation status, and result download is not confirmed by the available source documents or an accepted ADR.

The backend is a Java 21 Spring Boot modular monolith that serves a React frontend through REST APIs. Existing ADRs already require API handlers to remain thin, prohibit direct external process execution from API classes, and prohibit exposing raw filesystem paths, root-relative storage keys, or `ResultFileMetadata.internalPath` through public contracts.

## Decision

The MVP public REST API will use operation-centered endpoints with separate creation endpoints per operation type:

- `POST /api/operations/conversions`
- `POST /api/operations/downloads`
- `GET /api/operations/{operationId}`
- `GET /api/operations/{operationId}/result`

Conversion creation uses `multipart/form-data` with a single MP4 upload field named `file`.

URL download creation uses `application/json` with body:

```json
{ "url": "https://..." }
```

Creation requests do not include extra operation metadata. The backend derives `OperationType` from the endpoint.

Creation and status responses use a public operation representation:

```json
{
  "operationId": 123,
  "type": "CONVERSION",
  "status": "PENDING",
  "createdAt": "2026-06-02T12:00:00Z",
  "completedAt": null,
  "expiresAt": null,
  "result": null,
  "error": null,
  "links": {
    "status": "/api/operations/123"
  }
}
```

Completed operations may include public result metadata:

```json
{
  "fileName": "audio.mp3",
  "contentType": "audio/mpeg",
  "sizeBytes": 123456,
  "downloadUrl": "/api/operations/123/result"
}
```

Public responses must not expose `ResultFileMetadata.internalPath`, root-relative storage keys, absolute filesystem paths, or backend storage implementation details.

The public error response shape is:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed.",
  "details": [
    {
      "field": "file",
      "message": "MP4 file is required."
    }
  ]
}
```

The HTTP status mapping is:

- `201 Created`: operation accepted or created;
- `200 OK`: operation status found;
- `200 OK`: result download available;
- `400 Bad Request`: invalid payload, missing file, or invalid URL;
- `404 Not Found`: operation or result not found;
- `409 Conflict`: result is not available, operation failed, or result expired;
- `413 Payload Too Large`: upload exceeds configured limit;
- `415 Unsupported Media Type`: file or content type is not accepted;
- `500 Internal Server Error`: unexpected backend failure.

`GET /api/operations/{operationId}/result` returns the result file directly when available. It does not redirect and does not return a metadata handoff response.

## Considered Options

| Option | Summary | Trade-offs | Decision |
| --- | --- | --- | --- |
| Operation-centered endpoints with separate creation routes | Use separate conversion/download creation endpoints and shared status/result endpoints. | Keeps operation types distinguishable while sharing common status/result behavior. | Accepted |
| One generic operation creation endpoint | Use one `POST /api/operations` with a type field. | Reduces endpoint count, but makes request shape more complex because conversion is multipart and URL download is JSON. | Rejected |
| Numeric operation id | Expose the persisted operation identifier as `operationId`. | Simple and compatible with current operation persistence; less opaque than generated public tokens. | Accepted for MVP |
| Opaque public token | Generate a separate public identifier for status and result access. | Better decoupling, but adds scope not required by current MVP sources. | Deferred |
| Direct result download endpoint | Serve the file from the result endpoint when available. | Simple frontend integration and no storage URL exposure. | Accepted |
| Redirect result download | Redirect clients to another URL. | Useful for remote object storage, but not needed for current local temporary storage. | Rejected for MVP |
| Metadata-only result handoff | Return metadata and require another download path. | Adds another client step and risks exposing storage details. | Rejected |
| Standard JSON error object | Return code, message, and optional field details. | Small, testable, and works for validation and operational failures. | Accepted |

## Consequences

- API implementation must define DTOs and route contracts from this ADR.
- Conversion and URL download submissions remain distinguishable and align with `OperationType`.
- API handlers must stay thin and hand off orchestration to backend services.
- Public API DTOs must translate internal operation/result metadata into safe public fields.
- Public responses must never expose `ResultFileMetadata.internalPath`, root-relative storage keys, absolute paths, or storage implementation details.
- Result download implementation must stream or otherwise return the file directly from the result endpoint when available.
- Future tasks may introduce opaque public operation identifiers if abuse, privacy, or compatibility needs require them.

## Task Impact

- Related task file: `docs/tasks/mvp-media-utility/008-define-rest-api-contracts.md`
- Related architecture decision notes: `docs/architecture/task-decisions/mvp-media-utility/008-define-rest-api-contracts-architecture-decisions.md`
- Unlock effect: Resolves the REST API contract blocker for MVP-MEDIA-008 planning.
- Remaining blockers: None for planning MVP-MEDIA-008 after the task file and architecture decision notes are updated to reference this ADR. A task plan still needs to be created before implementation.

## Source References

- `docs/tasks/mvp-media-utility/008-define-rest-api-contracts.md`: blocked task scope, dependencies, acceptance criteria, and open questions.
- `docs/planning/project-planning.md`: confirms conversion, public URL download, immediate result download, and operation/result flow needs.
- `docs/adrs/001-use-java-and-spring-boot-modular-monolith.md`: confirms Spring Boot REST-capable backend.
- `docs/adrs/002-use-react-vite-typescript-served-by-spring-boot.md`: confirms React frontend communicates with backend through REST APIs.
- `docs/adrs/005-use-shared-operation-domain-model-for-metadata-persistence.md`: prohibits exposing raw filesystem paths through public API contracts.
- `docs/adrs/006-use-root-relative-keys-for-temporary-local-storage.md`: confirms root-relative storage keys remain internal.
- `docs/adrs/007-define-process-execution-contract-for-media-tools.md`: prohibits API handlers from executing external commands directly.
- User confirmation: current `resolve-architecture-blocker` session accepted ADR status `Accepted`, operation-centered REST endpoints, multipart `file` conversion upload, JSON `url` download request, numeric `operationId`, safe public result metadata, standard error response shape, HTTP status mapping, and direct result download endpoint behavior.
