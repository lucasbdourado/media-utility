import { useEffect, useRef, useState } from "react";
import "./App.css";
import {
  createConversion,
  createDownload,
  getOperation,
  getPublicErrorMessage,
  type PublicOperationResponse,
} from "./operationApi";

const REQUIRED_FILE_MESSAGE = "Choose an MP4 file before continuing.";
const INVALID_FILE_MESSAGE = "Select an MP4 file to continue.";

const REQUIRED_URL_MESSAGE = "Enter a URL before continuing.";
const INVALID_URL_MESSAGE = "Enter a valid HTTP or HTTPS URL to continue.";
const RESPONSIBILITY_NOTICE =
  "By submitting this URL, you confirm that you have the right to download this media and agree to our Terms of Service.";

const POLL_INTERVAL_MS = 500;
const MAX_POLL_ATTEMPTS = 6;

const OPERATIONS = {
  conversion: {
    label: "MP4-to-MP3 conversion",
    eyebrow: "Conversion",
    title: "MP4-to-MP3 workspace",
    description: "Select an MP4 file and submit it for backend MP3 conversion.",
  },
  download: {
    label: "Public URL download",
    eyebrow: "URL download",
    title: "Public URL workspace",
    description: "Enter a public URL and submit it for backend media download.",
  },
} as const;

type OperationKey = keyof typeof OPERATIONS;

type FlowState = {
  operation: PublicOperationResponse | null;
  requestError: string | null;
  isSubmitting: boolean;
  isPolling: boolean;
  pollExhausted: boolean;
};

function initialFlowState(): FlowState {
  return {
    operation: null,
    requestError: null,
    isSubmitting: false,
    isPolling: false,
    pollExhausted: false,
  };
}

function isMp4File(file: File) {
  return file.type === "video/mp4" || file.name.toLowerCase().endsWith(".mp4");
}

function isValidUrl(input: string): boolean {
  try {
    const url = new URL(input);
    return url.protocol === "http:" || url.protocol === "https:";
  } catch {
    return false;
  }
}

function formatFileSize(bytes: number) {
  const megabytes = bytes / 1024 / 1024;

  if (megabytes < 0.1) {
    return "0.1 MB";
  }

  return `${megabytes.toFixed(1)} MB`;
}

function describeStatus(operation: PublicOperationResponse | null, isSubmitting: boolean) {
  if (isSubmitting) {
    return "Submitting operation to the backend.";
  }

  if (!operation) {
    return "No backend operation has been submitted yet.";
  }

  switch (operation.status) {
    case "PENDING":
      return "Operation is pending.";
    case "PROCESSING":
      return "Operation is processing.";
    case "COMPLETED":
      return "Operation completed.";
    case "FAILED":
      return "Operation failed.";
    default:
      return "Operation status is unavailable.";
  }
}

function operationErrorMessage(operation: PublicOperationResponse | null) {
  if (operation?.status !== "FAILED") {
    return null;
  }

  return operation.error?.details?.map((detail) => detail.message).filter(Boolean).join(" ")
    || operation.error?.message
    || "Operation failed.";
}

function OperationStatusPanel({
  flowState,
}: {
  flowState: FlowState;
}) {
  const { operation, requestError, isSubmitting, isPolling, pollExhausted } = flowState;
  const failedMessage = operationErrorMessage(operation);
  const result = operation?.status === "COMPLETED" ? operation.result : null;

  return (
    <aside className="status-panel" aria-labelledby="state-surfaces-title">
      <p className="eyebrow">Backend state</p>
      <h2 id="state-surfaces-title">Operation status</h2>
      <div className="status-summary" aria-live="polite">
        <span className="status-label">{operation?.status ?? (isSubmitting ? "SUBMITTING" : "IDLE")}</span>
        <p>{describeStatus(operation, isSubmitting)}</p>
        {operation ? <p>Operation #{operation.operationId}</p> : null}
        {isPolling ? <p>Checking backend status.</p> : null}
        {pollExhausted ? (
          <p role="status">Operation is still processing. Automatic status checks have stopped.</p>
        ) : null}
      </div>

      {requestError ? (
        <p className="form-feedback" role="alert">
          {requestError}
        </p>
      ) : null}

      {failedMessage ? (
        <p className="form-feedback" role="alert">
          {failedMessage}
        </p>
      ) : null}

      {result ? (
        <div className="result-surface">
          <span>Result ready</span>
          {result.fileName ? <strong>{result.fileName}</strong> : null}
          {typeof result.sizeBytes === "number" ? <p>{formatFileSize(result.sizeBytes)}</p> : null}
          <a href={result.downloadUrl}>Download result</a>
        </div>
      ) : null}
    </aside>
  );
}

export function App() {
  const [selectedOperation, setSelectedOperation] = useState<OperationKey>("conversion");
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [fileFeedback, setFileFeedback] = useState<string | null>(null);
  const [urlInput, setUrlInput] = useState("");
  const [urlFeedback, setUrlFeedback] = useState<string | null>(null);
  const [flowState, setFlowState] = useState<FlowState>(() => initialFlowState());
  const activeOperation = OPERATIONS[selectedOperation];
  const pollTimeoutRef = useRef<number | null>(null);
  const pollRunRef = useRef(0);

  function clearPolling() {
    if (pollTimeoutRef.current !== null) {
      window.clearTimeout(pollTimeoutRef.current);
      pollTimeoutRef.current = null;
    }
  }

  useEffect(() => clearPolling, []);

  function resetOperationState() {
    clearPolling();
    pollRunRef.current += 1;
    setFlowState(initialFlowState());
  }

  function updateOperation(operation: PublicOperationResponse) {
    setFlowState((current) => ({
      ...current,
      operation,
      requestError: null,
      isSubmitting: false,
      isPolling: operation.status === "PENDING" || operation.status === "PROCESSING",
      pollExhausted: false,
    }));
  }

  function scheduleStatusPolling(operation: PublicOperationResponse) {
    clearPolling();
    const statusUrl = operation.links.status;
    const runId = ++pollRunRef.current;

    if (operation.status === "COMPLETED" || operation.status === "FAILED") {
      setFlowState((current) => ({ ...current, isPolling: false }));
      return;
    }

    function poll(attempt: number) {
      if (attempt > MAX_POLL_ATTEMPTS) {
        setFlowState((current) => ({ ...current, isPolling: false, pollExhausted: true }));
        return;
      }

      pollTimeoutRef.current = window.setTimeout(async () => {
        try {
          const latestOperation = await getOperation(statusUrl);

          if (pollRunRef.current !== runId) {
            return;
          }

          const isTerminal =
            latestOperation.status === "COMPLETED" || latestOperation.status === "FAILED";
          setFlowState((current) => ({
            ...current,
            operation: latestOperation,
            requestError: null,
            isPolling: !isTerminal,
            pollExhausted: false,
          }));

          if (!isTerminal) {
            poll(attempt + 1);
          }
        } catch (error) {
          if (pollRunRef.current !== runId) {
            return;
          }

          setFlowState((current) => ({
            ...current,
            requestError: getPublicErrorMessage(error),
            isPolling: false,
          }));
        }
      }, POLL_INTERVAL_MS);
    }

    poll(1);
  }

  function handleOperationCreated(operation: PublicOperationResponse) {
    updateOperation(operation);
    scheduleStatusPolling(operation);
  }

  function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0] ?? null;
    setFileFeedback(null);
    resetOperationState();

    if (!file) {
      setSelectedFile(null);
      return;
    }

    if (!isMp4File(file)) {
      event.target.value = "";
      setSelectedFile(null);
      setFileFeedback(INVALID_FILE_MESSAGE);
      return;
    }

    setSelectedFile(file);
  }

  async function handleUploadSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!selectedFile) {
      setFileFeedback(REQUIRED_FILE_MESSAGE);
      resetOperationState();
      return;
    }

    clearPolling();
    pollRunRef.current += 1;
    setFileFeedback(null);
    setFlowState({ ...initialFlowState(), isSubmitting: true });

    try {
      handleOperationCreated(await createConversion(selectedFile));
    } catch (error) {
      setFlowState({
        ...initialFlowState(),
        requestError: getPublicErrorMessage(error),
      });
    }
  }

  function handleUrlChange(event: React.ChangeEvent<HTMLInputElement>) {
    setUrlInput(event.target.value);
    setUrlFeedback(null);
    resetOperationState();
  }

  async function handleUrlSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!urlInput.trim()) {
      setUrlFeedback(REQUIRED_URL_MESSAGE);
      resetOperationState();
      return;
    }

    if (!isValidUrl(urlInput)) {
      setUrlFeedback(INVALID_URL_MESSAGE);
      resetOperationState();
      return;
    }

    clearPolling();
    pollRunRef.current += 1;
    setUrlFeedback(null);
    setFlowState({ ...initialFlowState(), isSubmitting: true });

    try {
      handleOperationCreated(await createDownload(urlInput.trim()));
    } catch (error) {
      setFlowState({
        ...initialFlowState(),
        requestError: getPublicErrorMessage(error),
      });
    }
  }

  function handleOperationSelection(operationKey: OperationKey) {
    setSelectedOperation(operationKey);
    resetOperationState();
  }

  return (
    <main className="app-shell">
      <section className="flow-header" aria-labelledby="app-title">
        <p className="eyebrow">MVP flow</p>
        <h1 id="app-title">Media Utility</h1>
        <p className="summary">Choose one media operation and stay in a single anonymous flow.</p>
      </section>

      <section className="operation-selector" aria-label="Media operation selector">
        {Object.entries(OPERATIONS).map(([operationKey, operation]) => {
          const typedOperationKey = operationKey as OperationKey;
          const isSelected = selectedOperation === typedOperationKey;

          return (
            <button
              aria-label={operation.label}
              aria-pressed={isSelected}
              className="operation-option"
              key={operationKey}
              onClick={() => handleOperationSelection(typedOperationKey)}
              type="button"
            >
              <span>{operation.eyebrow}</span>
              <strong>{operation.label}</strong>
            </button>
          );
        })}
      </section>

      <section className="workspace-grid" aria-label="Selected operation workspace">
        <article className="operation-panel" aria-labelledby="operation-title">
          <p className="eyebrow">{activeOperation.eyebrow}</p>
          <h2 id="operation-title">{activeOperation.title}</h2>
          <p>{activeOperation.description}</p>
          {selectedOperation === "conversion" ? (
            <form
              aria-label="MP4 upload form"
              className="upload-form"
              onSubmit={handleUploadSubmit}
            >
              <label className="file-control" htmlFor="conversion-file">
                <span>MP4 file</span>
                <input
                  accept=".mp4,video/mp4"
                  id="conversion-file"
                  name="file"
                  onChange={handleFileChange}
                  type="file"
                />
              </label>

              {fileFeedback ? (
                <p className="form-feedback" role="alert">
                  {fileFeedback}
                </p>
              ) : null}

              {selectedFile ? (
                <div className="selected-file" aria-label="Selected MP4 file">
                  <span>Selected file</span>
                  <strong>{selectedFile.name}</strong>
                  <p>{formatFileSize(selectedFile.size)}</p>
                </div>
              ) : null}

              <div className="ready-surface" aria-live="polite">
                Select a valid MP4 file to submit the conversion request.
              </div>

              <button className="submit-button" disabled={flowState.isSubmitting} type="submit">
                Submit conversion
              </button>
            </form>
          ) : (
            <form
              aria-label="URL download form"
              className="upload-form"
              onSubmit={handleUrlSubmit}
            >
              <label className="file-control" htmlFor="download-url">
                <span>Public URL</span>
                <input
                  id="download-url"
                  name="url"
                  onChange={handleUrlChange}
                  placeholder="https://example.com/media"
                  type="text"
                  value={urlInput}
                />
              </label>

              {urlFeedback ? (
                <p className="form-feedback" role="alert">
                  {urlFeedback}
                </p>
              ) : null}

              <p className="responsibility-notice">{RESPONSIBILITY_NOTICE}</p>

              <div className="ready-surface" aria-live="polite">
                Enter a valid public URL to submit the download request.
              </div>

              <button className="submit-button" disabled={flowState.isSubmitting} type="submit">
                Submit download
              </button>
            </form>
          )}
        </article>

        <OperationStatusPanel flowState={flowState} />
      </section>
    </main>
  );
}
