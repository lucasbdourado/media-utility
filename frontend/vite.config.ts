import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    cors: true,
  },
  preview: {
    cors: true,
  },
  test: {
    globals: true,
    environment: "jsdom",
    setupFiles: "./src/setupTests.ts",
    coverage: {
      provider: "v8",
      reportsDirectory: "coverage",
      reporter: ["text", "html", "lcov"],
      include: ["src/**/*.{ts,tsx}"],
      exclude: [
        "src/**/*.test.{ts,tsx}",
        "src/**/*.spec.{ts,tsx}",
        "src/setupTests.ts",
        "src/vite-env.d.ts",
        "src/main.tsx",
        "src/**/*.d.ts",
        "dist/**",
        "coverage/**",
        "node_modules/**"
      ]
    }
  }
});
