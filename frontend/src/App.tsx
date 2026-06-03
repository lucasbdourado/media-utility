import { useState } from "react";
import "./App.css";

const REQUIRED_FILE_MESSAGE = "Choose an MP4 file before continuing.";
const INVALID_FILE_MESSAGE = "Select an MP4 file to continue.";
const READY_FILE_MESSAGE = "This MP4 is ready for the later conversion submission step.";

const OPERATIONS = {
  conversion: {
    label: "MP4-to-MP3 conversion",
    eyebrow: "Conversion",
    title: "MP4-to-MP3 workspace",
    description:
      "Select an MP4 file and confirm it is ready for the later conversion request.",
  },
  download: {
    label: "Public URL download",
    eyebrow: "URL download",
    title: "Public URL workspace",
    description:
      "A dedicated area for the future public URL entry flow and download progress.",
    placeholder: "URL form placeholder",
    nextTask: "Task 011 will attach URL entry and request state here.",
  },
} as const;

type OperationKey = keyof typeof OPERATIONS;

function isMp4File(file: File) {
  return file.type === "video/mp4" || file.name.toLowerCase().endsWith(".mp4");
}

function formatFileSize(bytes: number) {
  const megabytes = bytes / 1024 / 1024;

  if (megabytes < 0.1) {
    return "0.1 MB";
  }

  return `${megabytes.toFixed(1)} MB`;
}

const SHARED_STATE_SURFACES = [
  "Idle",
  "Loading",
  "Success",
  "Error",
  "Result ready",
] as const;

export function App() {
  const [selectedOperation, setSelectedOperation] = useState<OperationKey>("conversion");
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [fileFeedback, setFileFeedback] = useState<string | null>(null);
  const [readyMessage, setReadyMessage] = useState<string | null>(null);
  const activeOperation = OPERATIONS[selectedOperation];

  function handleFileChange(event: React.ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0] ?? null;
    setReadyMessage(null);

    if (!file) {
      setSelectedFile(null);
      setFileFeedback(null);
      return;
    }

    if (!isMp4File(file)) {
      event.target.value = "";
      setSelectedFile(null);
      setFileFeedback(INVALID_FILE_MESSAGE);
      return;
    }

    setSelectedFile(file);
    setFileFeedback(null);
  }

  function handleUploadSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!selectedFile) {
      setReadyMessage(null);
      setFileFeedback(REQUIRED_FILE_MESSAGE);
      return;
    }

    setFileFeedback(null);
    setReadyMessage(READY_FILE_MESSAGE);
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
          const isSelected = selectedOperation === operationKey;

          return (
            <button
              aria-label={operation.label}
              aria-pressed={isSelected}
              className="operation-option"
              key={operationKey}
              onClick={() => setSelectedOperation(operationKey as OperationKey)}
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
                {readyMessage ?? "Select a valid MP4 file to prepare the conversion request."}
              </div>

              <button className="submit-button" type="submit">
                Prepare conversion
              </button>
            </form>
          ) : (
            <div className="placeholder-surface" aria-label={OPERATIONS.download.placeholder}>
              <span>{OPERATIONS.download.placeholder}</span>
              <p>{OPERATIONS.download.nextTask}</p>
            </div>
          )}
        </article>

        <aside className="status-panel" aria-labelledby="state-surfaces-title">
          <p className="eyebrow">Shared state</p>
          <h2 id="state-surfaces-title">Operation state surfaces</h2>
          <ul>
            {SHARED_STATE_SURFACES.map((stateSurface) => (
              <li key={stateSurface}>
                <span aria-hidden="true" />
                {stateSurface}
              </li>
            ))}
          </ul>
        </aside>
      </section>
    </main>
  );
}
