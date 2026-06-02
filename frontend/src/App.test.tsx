import { render, screen } from "@testing-library/react";
import { App } from "./App";

describe("App", () => {
  it("renders the scaffold shell", () => {
    render(<App />);

    expect(screen.getByRole("heading", { name: /media utility/i })).toBeInTheDocument();
  });
});
