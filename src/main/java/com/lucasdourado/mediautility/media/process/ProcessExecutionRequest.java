package com.lucasdourado.mediautility.media.process;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record ProcessExecutionRequest(
		String executable,
		List<String> arguments,
		Path workingDirectory,
		Duration timeout,
		Map<String, String> environmentOverrides) {

	public ProcessExecutionRequest {
		if (executable == null || executable.isBlank()) {
			throw new ProcessExecutionException("Process executable must not be blank.");
		}
		arguments = List.copyOf(arguments == null ? List.of() : arguments);
		if (arguments.stream().anyMatch(argument -> argument == null)) {
			throw new ProcessExecutionException("Process arguments must not contain null values.");
		}
		if (workingDirectory == null) {
			throw new ProcessExecutionException("Process working directory is required.");
		}
		if (!Files.isDirectory(workingDirectory)) {
			throw new ProcessExecutionException("Process working directory must be an existing directory.");
		}
		if (timeout != null && (timeout.isZero() || timeout.isNegative())) {
			throw new ProcessExecutionException("Process timeout must be positive.");
		}
		Map<String, String> safeEnvironmentOverrides = new LinkedHashMap<>();
		if (environmentOverrides != null) {
			for (Map.Entry<String, String> entry : environmentOverrides.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				if (key == null || key.isBlank()) {
					throw new ProcessExecutionException("Process environment override names must not be blank.");
				}
				if (value == null) {
					throw new ProcessExecutionException("Process environment override values must not be null.");
				}
				safeEnvironmentOverrides.put(key, value);
			}
		}
		environmentOverrides = Map.copyOf(safeEnvironmentOverrides);
		if (environmentOverrides.keySet().stream().anyMatch(key -> key == null || key.isBlank())) {
			throw new ProcessExecutionException("Process environment override names must not be blank.");
		}
	}

	public static ProcessExecutionRequest of(String executable, List<String> arguments, Path workingDirectory) {
		return new ProcessExecutionRequest(executable, arguments, workingDirectory, null, Map.of());
	}
}
