import { act, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { App } from "./App";

const fetchMock = vi.fn<typeof fetch>();
const API_BASE_URL = "http://localhost:8080";

function operationResponse(overrides: Record<string, unknown> = {}) {
  return {
    operationId: 123,
    type: "CONVERSION",
    status: "PENDING",
    createdAt: "2026-06-04T12:00:00Z",
    completedAt: null,
    expiresAt: null,
    result: null,
    error: null,
    links: {
      status: "/api/operations/123",
    },
    ...overrides,
  };
}

function jsonResponse(body: unknown, init: ResponseInit = {}) {
  return new Response(JSON.stringify(body), {
    status: 200,
    headers: {
      "Content-Type": "application/json",
    },
    ...init,
  });
}

function selectConversionFile(file = new File(["video"], "clip.mp4", { type: "video/mp4" })) {
  fireEvent.change(screen.getByLabelText(/mp4 file/i), { target: { files: [file] } });
  return file;
}

async function submitConversion() {
  await act(async () => {
    fireEvent.click(screen.getByRole("button", { name: /submit conversion/i }));
  });
  expect(fetchMock).toHaveBeenCalled();
}

beforeEach(() => {
  fetchMock.mockReset();
  vi.stubGlobal("fetch", fetchMock);
});

afterEach(() => {
  vi.useRealTimers();
  vi.unstubAllGlobals();
});

describe("App", () => {
  it("renders the operation selector flow with conversion selected by default", () => {
    render(<App />);

    expect(screen.getByRole("heading", { name: /media utility/i })).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: /mp4-to-mp3 conversion/i }),
    ).toHaveAttribute("aria-pressed", "true");
    expect(screen.getByRole("button", { name: /public url download/i })).toHaveAttribute(
      "aria-pressed",
      "false",
    );
    expect(screen.getByRole("heading", { name: /mp4-to-mp3 workspace/i })).toBeInTheDocument();
    expect(screen.getByRole("form", { name: /mp4 upload form/i })).toBeInTheDocument();
    expect(screen.getByLabelText(/mp4 file/i)).toHaveAttribute("accept", ".mp4,video/mp4");
    expect(screen.getByText(/no backend operation has been submitted yet/i)).toBeInTheDocument();
  });

  it("lets users switch to URL download and back without navigation", () => {
    render(<App />);

    const conversionOption = screen.getByRole("button", {
      name: /mp4-to-mp3 conversion/i,
    });
    const downloadOption = screen.getByRole("button", {
      name: /public url download/i,
    });

    fireEvent.click(downloadOption);

    expect(downloadOption).toHaveAttribute("aria-pressed", "true");
    expect(conversionOption).toHaveAttribute("aria-pressed", "false");
    expect(screen.getByRole("heading", { name: /public url workspace/i })).toBeInTheDocument();
    expect(screen.getByRole("form", { name: /url download form/i })).toBeInTheDocument();

    fireEvent.click(conversionOption);

    expect(conversionOption).toHaveAttribute("aria-pressed", "true");
    expect(screen.getByRole("heading", { name: /mp4-to-mp3 workspace/i })).toBeInTheDocument();
    expect(screen.getByRole("form", { name: /mp4 upload form/i })).toBeInTheDocument();
  });

  it("keeps local URL validation before calling the backend", () => {
    render(<App />);

    fireEvent.click(screen.getByRole("button", { name: /public url download/i }));
    fireEvent.click(screen.getByRole("button", { name: /submit download/i }));

    expect(screen.getByRole("alert")).toHaveTextContent(/enter a url before continuing/i);
    expect(fetchMock).not.toHaveBeenCalled();

    fireEvent.change(screen.getByRole("textbox", { name: /public url/i }), {
      target: { value: "ftp://example.com/video.mp4" },
    });
    fireEvent.click(screen.getByRole("button", { name: /submit download/i }));

    expect(screen.getByRole("alert")).toHaveTextContent(
      /enter a valid http or https url to continue/i,
    );
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("submits URL downloads to the backend as JSON", async () => {
    fetchMock.mockResolvedValueOnce(
      jsonResponse(operationResponse({ type: "URL_DOWNLOAD", operationId: 456, links: { status: "/api/operations/456" } }), {
        status: 201,
      }),
    );
    render(<App />);

    fireEvent.click(screen.getByRole("button", { name: /public url download/i }));
    fireEvent.change(screen.getByRole("textbox", { name: /public url/i }), {
      target: { value: "https://example.com/video.mp4" },
    });
    fireEvent.click(screen.getByRole("button", { name: /submit download/i }));

    await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(1));
    expect(fetchMock).toHaveBeenCalledWith(`${API_BASE_URL}/api/operations/downloads`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ url: "https://example.com/video.mp4" }),
    });
    expect(screen.getByText(/operation is pending/i)).toBeInTheDocument();
    expect(screen.getByText(/operation #456/i)).toBeInTheDocument();
  });

  it("submits MP4 conversion requests as multipart form data", async () => {
    fetchMock.mockResolvedValueOnce(jsonResponse(operationResponse(), { status: 201 }));
    render(<App />);

    const file = selectConversionFile();
    await submitConversion();

    expect(fetchMock).toHaveBeenCalledWith(
      `${API_BASE_URL}/api/operations/conversions`,
      expect.objectContaining({
        method: "POST",
        body: expect.any(FormData),
      }),
    );
    const [, requestInit] = fetchMock.mock.calls[0];
    expect((requestInit?.body as FormData).get("file")).toBe(file);
    expect(screen.getByText(/operation is pending/i)).toBeInTheDocument();
  });

  it("polls backend status and renders completed result metadata with a download link", async () => {
    vi.useFakeTimers();
    fetchMock
      .mockResolvedValueOnce(jsonResponse(operationResponse(), { status: 201 }))
      .mockResolvedValueOnce(
        jsonResponse(
          operationResponse({
            status: "COMPLETED",
            completedAt: "2026-06-04T12:01:00Z",
            result: {
              fileName: "clip.mp3",
              contentType: "audio/mpeg",
              sizeBytes: 1_572_864,
              downloadUrl: "/api/operations/123/result",
            },
          }),
        ),
      );
    render(<App />);

    selectConversionFile();
    await submitConversion();

    await act(async () => {
      await vi.advanceTimersByTimeAsync(500);
    });

    expect(fetchMock).toHaveBeenLastCalledWith(`${API_BASE_URL}/api/operations/123`);
    expect(screen.getByText(/operation completed/i)).toBeInTheDocument();
    expect(screen.getByText("clip.mp3")).toBeInTheDocument();
    expect(screen.getByText("1.5 MB")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /download result/i })).toHaveAttribute(
      "href",
      "/api/operations/123/result",
    );
  });

  it("renders backend validation details from creation errors", async () => {
    fetchMock.mockResolvedValueOnce(
      jsonResponse(
        {
          code: "VALIDATION_ERROR",
          message: "Request validation failed.",
          details: [{ field: "url", message: "URL must be an absolute http or https URL." }],
        },
        { status: 400 },
      ),
    );
    render(<App />);

    fireEvent.click(screen.getByRole("button", { name: /public url download/i }));
    fireEvent.change(screen.getByRole("textbox", { name: /public url/i }), {
      target: { value: "https://example.com/video.mp4" },
    });
    fireEvent.click(screen.getByRole("button", { name: /submit download/i }));

    expect(await screen.findByText(/url must be an absolute http or https url/i)).toBeInTheDocument();
  });

  it("renders failed operation errors and stops polling", async () => {
    vi.useFakeTimers();
    fetchMock
      .mockResolvedValueOnce(jsonResponse(operationResponse(), { status: 201 }))
      .mockResolvedValueOnce(
        jsonResponse(
          operationResponse({
            status: "FAILED",
            error: {
              code: "CONFLICT",
              message: "Media conversion failed.",
            },
          }),
        ),
      );
    render(<App />);

    selectConversionFile();
    await submitConversion();

    await act(async () => {
      await vi.advanceTimersByTimeAsync(500);
    });

    expect(screen.getByText(/operation failed/i)).toBeInTheDocument();
    expect(screen.getByText(/media conversion failed/i)).toBeInTheDocument();

    await act(async () => {
      await vi.advanceTimersByTimeAsync(2_000);
    });

    expect(fetchMock).toHaveBeenCalledTimes(2);
  });

  it("stops automatic polling after the bounded max attempts", async () => {
    vi.useFakeTimers();
    fetchMock.mockResolvedValueOnce(jsonResponse(operationResponse(), { status: 201 }));
    for (let attempt = 0; attempt < 6; attempt += 1) {
      fetchMock.mockResolvedValueOnce(jsonResponse(operationResponse({ status: "PROCESSING" })));
    }
    render(<App />);

    selectConversionFile();
    await submitConversion();

    await act(async () => {
      await vi.advanceTimersByTimeAsync(3_500);
    });

    expect(fetchMock).toHaveBeenCalledTimes(7);
    expect(
      screen.getByText(/operation is still processing. automatic status checks have stopped/i),
    ).toBeInTheDocument();
  });

  it("keeps local file validation before calling the backend", () => {
    render(<App />);

    fireEvent.click(screen.getByRole("button", { name: /submit conversion/i }));

    expect(screen.getByRole("alert")).toHaveTextContent(/choose an mp4 file before continuing/i);
    expect(fetchMock).not.toHaveBeenCalled();

    const file = new File(["not media"], "notes.txt", { type: "text/plain" });
    fireEvent.change(screen.getByLabelText(/mp4 file/i), { target: { files: [file] } });

    expect(screen.getByRole("alert")).toHaveTextContent(/select an mp4 file to continue/i);
    expect(screen.queryByText("notes.txt")).not.toBeInTheDocument();
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("shows selected MP4 file information before submission", () => {
    render(<App />);

    const file = new File([new Uint8Array(1_572_864)], "sample.MP4", {
      type: "application/octet-stream",
    });

    fireEvent.change(screen.getByLabelText(/mp4 file/i), { target: { files: [file] } });

    expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    expect(screen.getByLabelText(/selected mp4 file/i)).toHaveTextContent("sample.MP4");
    expect(screen.getByText("1.5 MB")).toBeInTheDocument();
  });
});
