package com.lucasdourado.mediautility.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import com.lucasdourado.mediautility.media.download.DownloadException;
import com.lucasdourado.mediautility.media.download.UrlDownloader;
import com.lucasdourado.mediautility.operations.Operation;
import com.lucasdourado.mediautility.operations.OperationEvent;
import com.lucasdourado.mediautility.operations.OperationEventType;
import com.lucasdourado.mediautility.operations.OperationStatus;
import com.lucasdourado.mediautility.operations.OperationType;
import com.lucasdourado.mediautility.operations.ResultFileMetadata;
import com.lucasdourado.mediautility.persistence.OperationRepository;
import com.lucasdourado.mediautility.persistence.OperationEventRepository;
import com.lucasdourado.mediautility.storage.TemporaryStorageService;

class BackgroundDownloadExecutorTest {

	private static final Instant NOW = Instant.parse("2026-06-03T12:00:00Z");

	private OperationRepository operationRepository;
	private OperationEventRepository operationEventRepository;
	private UrlDownloader urlDownloader;
	private TemporaryStorageService temporaryStorageService;
	private BackgroundDownloadExecutor executor;

	@BeforeEach
	void setUp() {
		operationRepository = mock(OperationRepository.class);
		operationEventRepository = mock(OperationEventRepository.class);
		urlDownloader = mock(UrlDownloader.class);
		temporaryStorageService = mock(TemporaryStorageService.class);
		executor = new BackgroundDownloadExecutor(
				operationRepository,
				operationEventRepository,
				urlDownloader,
				temporaryStorageService,
				Duration.ofHours(1));
	}

	@Test
	void executesSuccessfulDownload() throws Exception {
		URI url = new URI("https://example.com/videos/cats.mp4");
		Operation operation = Operation.create(OperationType.URL_DOWNLOAD, NOW);

		when(operationRepository.findById(1L)).thenReturn(Optional.of(operation));

		List<OperationStatus> savedStatuses = new ArrayList<>();
		when(operationRepository.save(any(Operation.class))).thenAnswer(invocation -> {
			Operation op = invocation.getArgument(0);
			savedStatuses.add(op.getStatus());
			return op;
		});

		ResultFileMetadata storedMetadata = new ResultFileMetadata("cats.mp4", "video/mp4", 1024L, "keys/cats.mp4");
		when(temporaryStorageService.storeResult(eq("1"), eq("cats.mp4"), eq("video/mp4"), any(InputStream.class)))
				.thenReturn(storedMetadata);

		executor.executeDownload(1L, url);

		assertThat(savedStatuses).containsExactly(OperationStatus.PROCESSING, OperationStatus.COMPLETED);
		assertThat(operation.getStatus()).isEqualTo(OperationStatus.COMPLETED);
		assertThat(operation.getResultFile()).isEqualTo(storedMetadata);
		assertThat(operation.getExpiresAt()).isAfter(NOW);

		ArgumentCaptor<Path> pathCaptor = ArgumentCaptor.forClass(Path.class);
		verify(urlDownloader).download(eq(url), pathCaptor.capture());

		// Verify COMPLETED event was saved
		ArgumentCaptor<OperationEvent> eventCaptor = ArgumentCaptor.forClass(OperationEvent.class);
		verify(operationEventRepository).save(eventCaptor.capture());
		OperationEvent capturedEvent = eventCaptor.getValue();
		assertThat(capturedEvent.getOperation()).isEqualTo(operation);
		assertThat(capturedEvent.getEventType()).isEqualTo(OperationEventType.COMPLETED);
		assertThat(capturedEvent.getOccurredAt()).isNotNull();

		Path tempFileUsed = pathCaptor.getValue();
		assertThat(Files.exists(tempFileUsed)).isFalse();
	}

	@Test
	void executesSuccessfulDownloadWithAlternativeFilenames() throws Exception {
		URI[] urls = {
				new URI("https://example.com/nature.MP4"),
				new URI("https://example.com/space%20walk.mp4"),
				new URI("https://example.com/folder/subfolder/file.mp4?query=123"),
				new URI("https://example.com/no-extension"),
				new URI("https://example.com/dir/"),
				new URI("https://example.com/unsafe/../../traversal.mp4")
		};

		String[] expectedFilenames = {
				"nature.mp4",
				"space_walk.mp4",
				"file.mp4",
				"no-extension.mp4",
				"download.mp4",
				"traversal.mp4"
		};

		for (int i = 0; i < urls.length; i++) {
			URI url = urls[i];
			String expectedName = expectedFilenames[i];

			Operation operation = Operation.create(OperationType.URL_DOWNLOAD, NOW);
			when(operationRepository.findById((long) i)).thenReturn(Optional.of(operation));
			when(operationRepository.save(any(Operation.class))).thenReturn(operation);

			ResultFileMetadata storedMetadata = new ResultFileMetadata(expectedName, "video/mp4", 1024L, "keys/" + expectedName);
			when(temporaryStorageService.storeResult(eq(String.valueOf(i)), eq(expectedName), eq("video/mp4"), any(InputStream.class)))
					.thenReturn(storedMetadata);

			executor.executeDownload((long) i, url);

			verify(temporaryStorageService).storeResult(eq(String.valueOf(i)), eq(expectedName), eq("video/mp4"), any(InputStream.class));

			// Verify COMPLETED event was saved for this operation
			ArgumentCaptor<OperationEvent> eventCaptor = ArgumentCaptor.forClass(OperationEvent.class);
			verify(operationEventRepository, times(i + 1)).save(eventCaptor.capture());
			OperationEvent capturedEvent = eventCaptor.getAllValues().get(i);
			assertThat(capturedEvent.getOperation()).isEqualTo(operation);
			assertThat(capturedEvent.getEventType()).isEqualTo(OperationEventType.COMPLETED);
		}
	}

	@Test
	void handlesDownloadFailure() throws Exception {
		URI url = new URI("https://example.com/invalid.mp4");
		Operation operation = Operation.create(OperationType.URL_DOWNLOAD, NOW);

		when(operationRepository.findById(1L)).thenReturn(Optional.of(operation));

		List<OperationStatus> savedStatuses = new ArrayList<>();
		when(operationRepository.save(any(Operation.class))).thenAnswer(invocation -> {
			Operation op = invocation.getArgument(0);
			savedStatuses.add(op.getStatus());
			return op;
		});

		doThrow(new DownloadException("yt-dlp exited with non-zero exit code"))
				.when(urlDownloader).download(eq(url), any(Path.class));

		executor.executeDownload(1L, url);

		assertThat(savedStatuses).containsExactly(OperationStatus.PROCESSING, OperationStatus.FAILED);
		assertThat(operation.getStatus()).isEqualTo(OperationStatus.FAILED);
		assertThat(operation.getFailureReason()).isEqualTo("yt-dlp exited with non-zero exit code");

		// Verify FAILED event was saved
		ArgumentCaptor<OperationEvent> eventCaptor = ArgumentCaptor.forClass(OperationEvent.class);
		verify(operationEventRepository).save(eventCaptor.capture());
		OperationEvent capturedEvent = eventCaptor.getValue();
		assertThat(capturedEvent.getOperation()).isEqualTo(operation);
		assertThat(capturedEvent.getEventType()).isEqualTo(OperationEventType.FAILED);
		assertThat(capturedEvent.getOccurredAt()).isNotNull();
		assertThat(capturedEvent.getFailureReason()).isEqualTo("yt-dlp exited with non-zero exit code");
	}
}
