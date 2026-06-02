package com.lucasdourado.mediautility.media.process;

import java.time.Duration;

public record ProcessExecutionResult(
		Integer exitCode,
		String stdout,
		String stderr,
		boolean timedOut,
		Duration duration) {

	public ProcessExecutionResult {
		if (stdout == null) {
			stdout = "";
		}
		if (stderr == null) {
			stderr = "";
		}
		if (duration == null) {
			throw new ProcessExecutionException("Process execution duration is required.");
		}
	}
}
