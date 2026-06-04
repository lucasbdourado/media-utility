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
    expect(screen.getByRole("form", { name: /url download form/i })).toBeInTheDocument();

    fireEvent.click(conversionOption);

    expect(conversionOption).toHaveAttribute("aria-pressed", "true");
    expect(screen.getByRole("heading", { name: /mp4-to-mp3 workspace/i })).toBeInTheDocument();
    expect(screen.getByRole("form", { name: /mp4 upload form/i })).toBeInTheDocument();
  });

  it("shows required-url feedback when submitting without a URL", () => {
    render(<App />);

    fireEvent.click(screen.getByRole("button", { name: /public url download/i }));
    fireEvent.click(screen.getByRole("button", { name: /prepare download/i }));

    expect(screen.getByRole("alert")).toHaveTextContent(/enter a url before continuing/i);
  });

  it("shows invalid-url feedback when submitting an invalid URL format", () => {
    render(<App />);

    fireEvent.click(screen.getByRole("button", { name: /public url download/i }));

    const input = screen.getByRole("textbox", { name: /public url/i });
    fireEvent.change(input, { target: { value: "not-a-url" } });
    fireEvent.click(screen.getByRole("button", { name: /prepare download/i }));

    expect(screen.getByRole("alert")).toHaveTextContent(
      /enter a valid http or https url to continue/i,
    );
  });

  it("rejects non-HTTP URL protocols", () => {
    render(<App />);

    fireEvent.click(screen.getByRole("button", { name: /public url download/i }));

    const input = screen.getByRole("textbox", { name: /public url/i });
    fireEvent.change(input, { target: { value: "ftp://example.com/video.mp4" } });
    fireEvent.click(screen.getByRole("button", { name: /prepare download/i }));

    expect(screen.getByRole("alert")).toHaveTextContent(
      /enter a valid http or https url to continue/i,
    );
    expect(
      screen.queryByText(/this url is ready for the later download submission step/i),
    ).not.toBeInTheDocument();
  });

  it("shows responsibility notice and submit-ready state with a valid URL", () => {
    render(<App />);

    fireEvent.click(screen.getByRole("button", { name: /public url download/i }));

    expect(
      screen.getByText(
        /by submitting this url, you confirm that you have the right to download this media and agree to our terms of service/i,
      ),
    ).toBeInTheDocument();

    const input = screen.getByRole("textbox", { name: /public url/i });
    fireEvent.change(input, { target: { value: "https://example.com/video.mp4" } });
    fireEvent.click(screen.getByRole("button", { name: /prepare download/i }));

    expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    expect(
      screen.getByText(/this url is ready for the later download submission step/i),
    ).toBeInTheDocument();
  });

  it("accepts valid HTTP URLs for the download ready state", () => {
    render(<App />);

    fireEvent.click(screen.getByRole("button", { name: /public url download/i }));

    const input = screen.getByRole("textbox", { name: /public url/i });
    fireEvent.change(input, { target: { value: "http://example.com/video.mp4" } });
    fireEvent.click(screen.getByRole("button", { name: /prepare download/i }));

    expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    expect(
      screen.getByText(/this url is ready for the later download submission step/i),
    ).toBeInTheDocument();
  });

  it("resets URL feedback and ready state when input changes", () => {
    render(<App />);

    fireEvent.click(screen.getByRole("button", { name: /public url download/i }));

    const input = screen.getByRole("textbox", { name: /public url/i });

    // Trigger required feedback
    fireEvent.click(screen.getByRole("button", { name: /prepare download/i }));
    expect(screen.getByRole("alert")).toHaveTextContent(/enter a url before continuing/i);

    // Typing should clear feedback
    fireEvent.change(input, { target: { value: "h" } });
    expect(screen.queryByRole("alert")).not.toBeInTheDocument();

    // Trigger ready state
    fireEvent.change(input, { target: { value: "https://example.com/media" } });
    fireEvent.click(screen.getByRole("button", { name: /prepare download/i }));
    expect(
      screen.getByText(/this url is ready for the later download submission step/i),
    ).toBeInTheDocument();

    // Typing again should clear the ready message
    fireEvent.change(input, { target: { value: "https://example.com/other" } });
    expect(
      screen.queryByText(/this url is ready for the later download submission step/i),
    ).not.toBeInTheDocument();
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

  it("clears the selected file and ready message when the file input is emptied", () => {
    render(<App />);

    const input = screen.getByLabelText(/mp4 file/i);
    const file = new File(["video"], "clip.mp4", { type: "video/mp4" });

    fireEvent.change(input, { target: { files: [file] } });
    fireEvent.click(screen.getByRole("button", { name: /prepare conversion/i }));
    expect(
      screen.getByText(/this mp4 is ready for the later conversion submission step/i),
    ).toBeInTheDocument();

    fireEvent.change(input, { target: { files: [] } });

    expect(screen.queryByLabelText(/selected mp4 file/i)).not.toBeInTheDocument();
    expect(
      screen.queryByText(/this mp4 is ready for the later conversion submission step/i),
    ).not.toBeInTheDocument();
    expect(screen.queryByRole("alert")).not.toBeInTheDocument();
  });

  it("accepts files with the video/mp4 MIME type and displays the minimum file size", () => {
    render(<App />);

    const input = screen.getByLabelText(/mp4 file/i);
    const file = new File(["video"], "clip.bin", { type: "video/mp4" });

    fireEvent.change(input, { target: { files: [file] } });

    expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    expect(screen.getByLabelText(/selected mp4 file/i)).toHaveTextContent("clip.bin");
    expect(screen.getByText("0.1 MB")).toBeInTheDocument();
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
