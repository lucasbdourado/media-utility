package com.lucasdourado.mediautility.media.process;

public interface ProcessExecutor extends ProcessExecutionBoundary {

	ProcessExecutionResult execute(ProcessExecutionRequest request);
}
