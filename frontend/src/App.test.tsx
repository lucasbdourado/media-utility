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
    expect(screen.getByLabelText(/mp4 upload form placeholder/i)).toBeInTheDocument();
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
