package com.lucasdourado.mediautility.media.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalJvmProcessExecutorTest {

	@TempDir
	Path workingDirectory;

	@Test
	void executesSuccessfulProcessAndCapturesOutput() {
		LocalJvmProcessExecutor executor = newExecutor(Duration.ofSeconds(5), 1024);

		ProcessExecutionResult result = executor.execute(command("success"));

		assertEquals(0, result.exitCode());
		assertEquals("ok", result.stdout());
		assertEquals("diagnostic", result.stderr());
		assertFalse(result.timedOut());
		assertNotNull(result.duration());
	}

	@Test
	void returnsResultForNonZeroExit() {
		LocalJvmProcessExecutor executor = newExecutor(Duration.ofSeconds(5), 1024);

		ProcessExecutionResult result = executor.execute(command("failure"));

		assertEquals(7, result.exitCode());
		assertEquals("failed", result.stderr());
		assertFalse(result.timedOut());
	}

	@Test
	void capturesStdoutAndStderrSeparatelyWithinLimit() {
		LocalJvmProcessExecutor executor = newExecutor(Duration.ofSeconds(5), 4);

		ProcessExecutionResult result = executor.execute(command("large-output"));

		assertEquals(0, result.exitCode());
		assertEquals("abcd", result.stdout());
		assertEquals("wxyz", result.stderr());
		assertFalse(result.timedOut());
	}

	@Test
	void returnsTimedOutResultAndTerminatesProcess() {
		LocalJvmProcessExecutor executor = newExecutor(Duration.ofMillis(100), 1024);

		ProcessExecutionResult result = executor.execute(command("sleep"));

		assertNull(result.exitCode());
		assertTrue(result.timedOut());
		assertTrue(result.duration().compareTo(Duration.ofSeconds(5)) < 0);
	}

	@Test
	void rejectsInvalidInputsBeforeLaunchingProcess() throws Exception {
		Path missingDirectory = workingDirectory.resolve("missing");

		assertThrows(ProcessExecutionException.class, () -> ProcessExecutionRequest.of("", List.of(), workingDirectory));
		assertThrows(ProcessExecutionException.class, () -> ProcessExecutionRequest.of(javaExecutable(), List.of(), null));
		assertThrows(ProcessExecutionException.class, () -> ProcessExecutionRequest.of(javaExecutable(), List.of(), missingDirectory));
		assertThrows(ProcessExecutionException.class, () -> new ProcessExecutionRequest(
				javaExecutable(),
				List.of(),
				workingDirectory,
				Duration.ZERO,
				Map.of()));
		assertThrows(ProcessExecutionException.class, () -> new ProcessExecutionRequest(
				javaExecutable(),
				List.of(),
				workingDirectory,
				Duration.ofSeconds(1),
				Map.of("", "value")));
		assertFalse(Files.exists(missingDirectory));
	}

	private LocalJvmProcessExecutor newExecutor(Duration timeout, int outputCaptureLimitBytes) {
		return new LocalJvmProcessExecutor(timeout, outputCaptureLimitBytes, Clock.systemUTC());
	}

	private ProcessExecutionRequest command(String mode) {
		return ProcessExecutionRequest.of(
				javaExecutable(),
				List.of("-cp", System.getProperty("java.class.path"), TestCommand.class.getName(), mode),
				workingDirectory);
	}

	private static String javaExecutable() {
		String executable = System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java";
		return Path.of(System.getProperty("java.home"), "bin", executable).toString();
	}

	public static final class TestCommand {

		private TestCommand() {
		}

		public static void main(String[] args) throws Exception {
			switch (args[0]) {
				case "success" -> {
					System.out.print("ok");
					System.err.print("diagnostic");
				}
				case "failure" -> {
					System.err.print("failed");
					System.exit(7);
				}
				case "large-output" -> {
					System.out.print("abcdefghij");
					System.err.print("wxyz1234");
				}
				case "sleep" -> Thread.sleep(5_000L);
				default -> throw new IllegalArgumentException("Unknown test command mode.");
			}
		}
	}
}
