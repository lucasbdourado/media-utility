package com.lucasdourado.mediautility.media.download;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import com.lucasdourado.mediautility.media.process.LocalJvmProcessExecutor;
import com.lucasdourado.mediautility.media.process.ProcessExecutionException;
import com.lucasdourado.mediautility.media.process.ProcessExecutionRequest;
import com.lucasdourado.mediautility.media.process.ProcessExecutionResult;
import com.lucasdourado.mediautility.media.process.ProcessExecutor;

class YtDlpUrlDownloaderTest {

	@TempDir
	Path tempDir;

	private ProcessExecutor processExecutor;
	private YtDlpUrlDownloader downloader;
	private URI videoUrl;
	private Path targetFile;

	@BeforeEach
	void setUp() {
		processExecutor = mock(ProcessExecutor.class);
		downloader = new YtDlpUrlDownloader(processExecutor, "yt-dlp");
		videoUrl = URI.create("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
		targetFile = tempDir.resolve("downloads").resolve("video.mp4");
	}

	@Test
	void rejectsNullUrl() {
		assertThatThrownBy(() -> downloader.download(null, targetFile))
				.isInstanceOf(DownloadException.class)
				.hasMessageContaining("URL must not be null");
	}

	@Test
	void rejectsNullTarget() {
		assertThatThrownBy(() -> downloader.download(videoUrl, null))
				.isInstanceOf(DownloadException.class)
				.hasMessageContaining("Target path must not be null");
	}

	@Test
	void downloadsSuccessfullyAndValidatesOutput() throws IOException {
		ProcessExecutionResult mockResult = new ProcessExecutionResult(
				0, "stdout", "stderr", false, Duration.ofSeconds(2)
		);

		when(processExecutor.execute(any(ProcessExecutionRequest.class))).thenAnswer(invocation -> {
			Path target = targetFile.toAbsolutePath();
			Files.createDirectories(target.getParent());
			Files.writeString(target, "dummy video bytes"); // non-empty output
			return mockResult;
		});

		downloader.download(videoUrl, targetFile);

		ArgumentCaptor<ProcessExecutionRequest> captor = ArgumentCaptor.forClass(ProcessExecutionRequest.class);
		verify(processExecutor).execute(captor.capture());

		ProcessExecutionRequest request = captor.getValue();
		verifyRequest(request, videoUrl, targetFile);
	}

	@Test
	void throwsDownloadExceptionWhenYtDlpExitsWithNonZero() {
		ProcessExecutionResult mockResult = new ProcessExecutionResult(
				1, "", "ERROR: unable to download video", false, Duration.ofSeconds(2)
		);
		when(processExecutor.execute(any(ProcessExecutionRequest.class))).thenReturn(mockResult);

		assertThatThrownBy(() -> downloader.download(videoUrl, targetFile))
				.isInstanceOf(DownloadException.class)
				.hasMessageContaining("yt-dlp process exited with code 1")
				.hasMessageContaining("ERROR: unable to download video");
	}

	@Test
	void throwsDownloadExceptionWhenYtDlpTimesOut() {
		ProcessExecutionResult mockResult = new ProcessExecutionResult(
				null, "", "", true, Duration.ofSeconds(5)
		);
		when(processExecutor.execute(any(ProcessExecutionRequest.class))).thenReturn(mockResult);

		assertThatThrownBy(() -> downloader.download(videoUrl, targetFile))
				.isInstanceOf(DownloadException.class)
				.hasMessageContaining("yt-dlp process timed out or was interrupted during download");
	}

	@Test
	void throwsDownloadExceptionWhenYtDlpFailsToStart() {
		when(processExecutor.execute(any(ProcessExecutionRequest.class)))
				.thenThrow(new ProcessExecutionException("Executable not found"));

		assertThatThrownBy(() -> downloader.download(videoUrl, targetFile))
				.isInstanceOf(DownloadException.class)
				.hasCauseInstanceOf(ProcessExecutionException.class);
	}

	@Test
	void throwsDownloadExceptionWhenTargetFileNotCreated() {
		ProcessExecutionResult mockResult = new ProcessExecutionResult(
				0, "", "", false, Duration.ofSeconds(2)
		);
		when(processExecutor.execute(any(ProcessExecutionRequest.class))).thenReturn(mockResult);

		assertThatThrownBy(() -> downloader.download(videoUrl, targetFile))
				.isInstanceOf(DownloadException.class)
				.hasMessageContaining("Download completed successfully but target file does not exist");
	}

	@Test
	void throwsDownloadExceptionWhenTargetFileIsEmpty() {
		ProcessExecutionResult mockResult = new ProcessExecutionResult(
				0, "", "", false, Duration.ofSeconds(2)
		);

		when(processExecutor.execute(any(ProcessExecutionRequest.class))).thenAnswer(invocation -> {
			Path target = targetFile.toAbsolutePath();
			Files.createDirectories(target.getParent());
			Files.createFile(target); // size = 0
			return mockResult;
		});

		assertThatThrownBy(() -> downloader.download(videoUrl, targetFile))
				.isInstanceOf(DownloadException.class)
				.hasMessageContaining("target file is empty (0 bytes)");
	}

	@Test
	void integrationTestRealYtDlpDownload() throws IOException {
		boolean ytdlpAvailable = false;
		LocalJvmProcessExecutor localExecutor = new LocalJvmProcessExecutor(Duration.ofSeconds(15), 1024);
		try {
			ProcessExecutionResult res = localExecutor.execute(new ProcessExecutionRequest(
					"yt-dlp", List.of("--version"), tempDir, null, Map.of()
			));
			ytdlpAvailable = res.exitCode() != null && res.exitCode() == 0;
		} catch (Exception e) {
			// not available
		}

		if (!ytdlpAvailable) {
			System.out.println("yt-dlp not available on system path. Skipping integration test.");
			return;
		}

		YtDlpUrlDownloader realDownloader = new YtDlpUrlDownloader(localExecutor, "yt-dlp");
		URI invalidUrl = URI.create("https://www.youtube.com/watch?v=invalid_id_12345");
		Path target = tempDir.resolve("output_integration.mp4");

		assertThatThrownBy(() -> realDownloader.download(invalidUrl, target))
				.isInstanceOf(DownloadException.class)
				.hasMessageContaining("yt-dlp process exited with code");
	}

	private void verifyRequest(ProcessExecutionRequest request, URI url, Path target) {
		assertThat(request.executable()).isEqualTo("yt-dlp");

		List<String> expectedArgs = List.of(
				"--no-playlist",
				"-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best",
				"-o", target.toAbsolutePath().toString(),
				url.toString()
		);
		assertThat(request.arguments()).containsExactlyElementsOf(expectedArgs);
		assertThat(request.workingDirectory()).isEqualTo(target.toAbsolutePath().getParent());
	}
}
