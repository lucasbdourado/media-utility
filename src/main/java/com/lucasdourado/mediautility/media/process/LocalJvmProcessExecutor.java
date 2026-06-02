package com.lucasdourado.mediautility.media.process;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LocalJvmProcessExecutor implements ProcessExecutor {

	private static final Duration FORCE_TERMINATION_WAIT = Duration.ofSeconds(2);

	private final Duration defaultTimeout;
	private final int outputCaptureLimitBytes;
	private final Clock clock;

	public LocalJvmProcessExecutor(
			@Value("${media-utility.process.default-timeout:5m}") Duration defaultTimeout,
			@Value("${media-utility.process.output-capture-limit-bytes:65536}") int outputCaptureLimitBytes) {
		this(defaultTimeout, outputCaptureLimitBytes, Clock.systemUTC());
	}

	LocalJvmProcessExecutor(Duration defaultTimeout, int outputCaptureLimitBytes, Clock clock) {
		if (defaultTimeout == null || defaultTimeout.isZero() || defaultTimeout.isNegative()) {
			throw new ProcessExecutionException("Default process timeout must be positive.");
		}
		if (outputCaptureLimitBytes < 0) {
			throw new ProcessExecutionException("Process output capture limit must not be negative.");
		}
		this.defaultTimeout = defaultTimeout;
		this.outputCaptureLimitBytes = outputCaptureLimitBytes;
		this.clock = Objects.requireNonNull(clock, "clock must not be null");
	}

	@Override
	public ProcessExecutionResult execute(ProcessExecutionRequest request) {
		Objects.requireNonNull(request, "request must not be null");

		Process process = startProcess(request);
		Instant startedAt = clock.instant();
		CompletableFuture<String> stdout = CompletableFuture.supplyAsync(
				() -> readLimited(process.getInputStream(), outputCaptureLimitBytes));
		CompletableFuture<String> stderr = CompletableFuture.supplyAsync(
				() -> readLimited(process.getErrorStream(), outputCaptureLimitBytes));

		try {
			Duration timeout = request.timeout() == null ? defaultTimeout : request.timeout();
			if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
				terminate(process);
				return result(null, stdout, stderr, true, startedAt);
			}
			return result(process.exitValue(), stdout, stderr, false, startedAt);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			terminate(process);
			return result(null, stdout, stderr, true, startedAt);
		}
	}

	private Process startProcess(ProcessExecutionRequest request) {
		List<String> command = new ArrayList<>();
		command.add(request.executable());
		command.addAll(request.arguments());

		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.directory(request.workingDirectory().toFile());
		processBuilder.environment().putAll(request.environmentOverrides());

		try {
			return processBuilder.start();
		}
		catch (IOException ex) {
			throw new ProcessExecutionException("Could not start external process.", ex);
		}
	}

	private static String readLimited(InputStream inputStream, int limitBytes) {
		try (inputStream) {
			ByteArrayOutputStream output = new ByteArrayOutputStream(Math.min(limitBytes, 8192));
			byte[] buffer = new byte[8192];
			int remaining = limitBytes;
			int read;
			while ((read = inputStream.read(buffer)) != -1) {
				if (remaining > 0) {
					int accepted = Math.min(read, remaining);
					output.write(buffer, 0, accepted);
					remaining -= accepted;
				}
			}
			return output.toString(StandardCharsets.UTF_8);
		}
		catch (IOException ex) {
			throw new ProcessExecutionException("Could not capture process output.", ex);
		}
	}

	private ProcessExecutionResult result(
			Integer exitCode,
			CompletableFuture<String> stdout,
			CompletableFuture<String> stderr,
			boolean timedOut,
			Instant startedAt) {
		return new ProcessExecutionResult(
				exitCode,
				await(stdout),
				await(stderr),
				timedOut,
				Duration.between(startedAt, clock.instant()));
	}

	private static String await(CompletableFuture<String> output) {
		try {
			return output.get();
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new ProcessExecutionException("Interrupted while collecting process output.", ex);
		}
		catch (ExecutionException ex) {
			if (ex.getCause() instanceof ProcessExecutionException processExecutionException) {
				throw processExecutionException;
			}
			throw new ProcessExecutionException("Could not collect process output.", ex);
		}
	}

	private static void terminate(Process process) {
		process.destroy();
		try {
			if (!process.waitFor(FORCE_TERMINATION_WAIT.toMillis(), TimeUnit.MILLISECONDS)) {
				process.destroyForcibly();
				process.waitFor(FORCE_TERMINATION_WAIT.toMillis(), TimeUnit.MILLISECONDS);
			}
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			process.destroyForcibly();
		}
	}
}
