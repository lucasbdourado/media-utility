import { useState } from "react";
import "./App.css";

const OPERATIONS = {
  conversion: {
    label: "MP4-to-MP3 conversion",
    eyebrow: "Conversion",
    title: "MP4-to-MP3 workspace",
    description:
      "A dedicated area for the future MP4 upload flow and conversion progress.",
    placeholder: "MP4 upload form placeholder",
    nextTask: "Task 010 will attach file selection and upload state here.",
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

const SHARED_STATE_SURFACES = [
  "Idle",
  "Loading",
  "Success",
  "Error",
  "Result ready",
] as const;

export function App() {
  const [selectedOperation, setSelectedOperation] = useState<OperationKey>("conversion");
  const activeOperation = OPERATIONS[selectedOperation];

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
          <div className="placeholder-surface" aria-label={activeOperation.placeholder}>
            <span>{activeOperation.placeholder}</span>
            <p>{activeOperation.nextTask}</p>
          </div>
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
