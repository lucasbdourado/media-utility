package com.lucasdourado.mediautility.media.download;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.lucasdourado.mediautility.media.process.ProcessExecutionException;
import com.lucasdourado.mediautility.media.process.ProcessExecutionRequest;
import com.lucasdourado.mediautility.media.process.ProcessExecutionResult;
import com.lucasdourado.mediautility.media.process.ProcessExecutor;

/**
 * Concrete implementation of UrlDownloader that uses yt-dlp via the shared ProcessExecutor.
 */
@Service
public class YtDlpUrlDownloader implements UrlDownloader {

	private final ProcessExecutor processExecutor;
	private final String ytdlpPath;

	public YtDlpUrlDownloader(
			ProcessExecutor processExecutor,
			@Value("${media-utility.ytdlp.path:yt-dlp}") String ytdlpPath) {
		this.processExecutor = processExecutor;
		this.ytdlpPath = ytdlpPath;
	}

	@Override
	public void download(URI url, Path target) {
		if (url == null) {
			throw new DownloadException("URL must not be null.");
		}
		if (target == null) {
			throw new DownloadException("Target path must not be null.");
		}

		Path absoluteTarget = target.toAbsolutePath();
		Path workingDirectory = absoluteTarget.getParent();
		if (workingDirectory == null) {
			throw new DownloadException("Target path must have a parent directory: " + target);
		}

		try {
			Files.createDirectories(workingDirectory);
		} catch (IOException e) {
			throw new DownloadException("Failed to create working directory for target file: " + workingDirectory, e);
		}

		List<String> arguments = List.of(
				"--no-playlist",
				"-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best",
				"-o", absoluteTarget.toString(),
				url.toString()
		);

		ProcessExecutionRequest request = new ProcessExecutionRequest(
				ytdlpPath,
				arguments,
				workingDirectory,
				null, // Inherit default process timeout from ProcessExecutor
				Map.of()
		);

		ProcessExecutionResult result;
		try {
			result = processExecutor.execute(request);
		} catch (ProcessExecutionException | IllegalArgumentException e) {
			throw new DownloadException("Process execution failed to start or configuration was invalid.", e);
		}

		if (result.timedOut() || Thread.currentThread().isInterrupted()) {
			throw new DownloadException("yt-dlp process timed out or was interrupted during download.");
		}

		if (result.exitCode() == null || result.exitCode() != 0) {
			int code = result.exitCode() != null ? result.exitCode() : -1;
			throw new DownloadException("yt-dlp process exited with code " + code + ". Stderr: " + result.stderr());
		}

		// Post-execution validation
		if (!Files.exists(absoluteTarget)) {
			throw new DownloadException("Download completed successfully but target file does not exist: " + absoluteTarget);
		}
		if (!Files.isRegularFile(absoluteTarget)) {
			throw new DownloadException("Download completed successfully but target path is not a regular file: " + absoluteTarget);
		}
		try {
			if (Files.size(absoluteTarget) == 0) {
				throw new DownloadException("Download completed successfully but target file is empty (0 bytes): " + absoluteTarget);
			}
		} catch (IOException e) {
			throw new DownloadException("Failed to read target file size: " + absoluteTarget, e);
		}
	}
}
