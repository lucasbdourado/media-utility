import { fireEvent, render, screen } from "@testing-library/react";
import { App } from "./App";

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
    expect(screen.getByLabelText(/url form placeholder/i)).toBeInTheDocument();

    fireEvent.click(conversionOption);

    expect(conversionOption).toHaveAttribute("aria-pressed", "true");
    expect(screen.getByRole("heading", { name: /mp4-to-mp3 workspace/i })).toBeInTheDocument();
    expect(screen.getByRole("form", { name: /mp4 upload form/i })).toBeInTheDocument();
  });

  it("shows required-file feedback when submitting without a selected file", () => {
    render(<App />);

    fireEvent.click(screen.getByRole("button", { name: /prepare conversion/i }));

    expect(screen.getByRole("alert")).toHaveTextContent(/choose an mp4 file before continuing/i);
    expect(screen.queryByLabelText(/selected mp4 file/i)).not.toBeInTheDocument();
  });

  it("rejects a non-MP4 file and keeps the ready state inactive", () => {
    render(<App />);

    const input = screen.getByLabelText(/mp4 file/i);
    const file = new File(["not media"], "notes.txt", { type: "text/plain" });

    fireEvent.change(input, { target: { files: [file] } });

    expect(screen.getByRole("alert")).toHaveTextContent(/select an mp4 file to continue/i);
    expect(screen.queryByText("notes.txt")).not.toBeInTheDocument();
    expect(screen.queryByText(/ready for the later conversion submission step/i)).not.toBeInTheDocument();
  });

  it("shows selected MP4 file information and can reach submit-ready state", () => {
    render(<App />);

    const input = screen.getByLabelText(/mp4 file/i);
    const file = new File([new Uint8Array(1_572_864)], "sample.MP4", {
      type: "application/octet-stream",
    });

    fireEvent.change(input, { target: { files: [file] } });

    expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    expect(screen.getByLabelText(/selected mp4 file/i)).toHaveTextContent("sample.MP4");
    expect(screen.getByText("1.5 MB")).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: /prepare conversion/i }));

    expect(
      screen.getByText(/this mp4 is ready for the later conversion submission step/i),
    ).toBeInTheDocument();
  });

  it("renders static shared operation state surfaces", () => {
    render(<App />);

    expect(screen.getByRole("heading", { name: /operation state surfaces/i })).toBeInTheDocument();
    expect(screen.getByText("Idle")).toBeInTheDocument();
    expect(screen.getByText("Loading")).toBeInTheDocument();
    expect(screen.getByText("Success")).toBeInTheDocument();
    expect(screen.getByText("Error")).toBeInTheDocument();
    expect(screen.getByText("Result ready")).toBeInTheDocument();
  });
});
