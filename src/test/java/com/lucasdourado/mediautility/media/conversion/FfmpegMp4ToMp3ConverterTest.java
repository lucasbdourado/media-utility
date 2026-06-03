package com.lucasdourado.mediautility.media.conversion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
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

class FfmpegMp4ToMp3ConverterTest {

    @TempDir
    Path tempDir;

    private ProcessExecutor processExecutor;
    private FfmpegMp4ToMp3Converter converter;
    private Path sourceFile;
    private Path targetFile;

    @BeforeEach
    void setUp() throws IOException {
        processExecutor = mock(ProcessExecutor.class);
        converter = new FfmpegMp4ToMp3Converter(processExecutor, "ffmpeg");

        sourceFile = tempDir.resolve("input.mp4");
        Files.writeString(sourceFile, "mock content"); // size > 0

        targetFile = tempDir.resolve("output.mp3");
    }

    @Test
    void rejectsNullSource() {
        assertThatThrownBy(() -> converter.convert(null, targetFile))
            .isInstanceOf(ConversionException.class)
            .hasMessageContaining("Source path must not be null");
    }

    @Test
    void rejectsNullTarget() {
        assertThatThrownBy(() -> converter.convert(sourceFile, null))
            .isInstanceOf(ConversionException.class)
            .hasMessageContaining("Target path must not be null");
    }

    @Test
    void rejectsMissingSource() {
        Path missingFile = tempDir.resolve("missing.mp4");
        assertThatThrownBy(() -> converter.convert(missingFile, targetFile))
            .isInstanceOf(ConversionException.class)
            .hasMessageContaining("Source file does not exist");
    }

    @Test
    void rejectsEmptySource() throws IOException {
        Path emptyFile = tempDir.resolve("empty.mp4");
        Files.createFile(emptyFile); // size = 0
        assertThatThrownBy(() -> converter.convert(emptyFile, targetFile))
            .isInstanceOf(ConversionException.class)
            .hasMessageContaining("Source file is empty");
    }

    @Test
    void convertsSuccessfullyAndValidatesOutput() throws IOException {
        ProcessExecutionResult mockResult = new ProcessExecutionResult(
            0, "stdout", "stderr", false, Duration.ofSeconds(1)
        );

        when(processExecutor.execute(any(ProcessExecutionRequest.class))).thenAnswer(invocation -> {
            Files.writeString(targetFile, "mock audio output");
            return mockResult;
        });

        converter.convert(sourceFile, targetFile);

        ArgumentCaptor<ProcessExecutionRequest> captor = ArgumentCaptor.forClass(ProcessExecutionRequest.class);
        verify(processExecutor).execute(captor.capture());

        ProcessExecutionRequest request = captor.getValue();
        verifyRequest(request, sourceFile, targetFile);
    }

    @Test
    void throwsConversionExceptionWhenFfmpegExitsWithNonZero() {
        ProcessExecutionResult mockResult = new ProcessExecutionResult(
            1, "", "invalid format error", false, Duration.ofSeconds(1)
        );
        when(processExecutor.execute(any(ProcessExecutionRequest.class))).thenReturn(mockResult);

        assertThatThrownBy(() -> converter.convert(sourceFile, targetFile))
            .isInstanceOf(ConversionException.class)
            .hasMessageContaining("FFmpeg process exited with code 1")
            .hasMessageContaining("invalid format error");
    }

    @Test
    void throwsConversionExceptionWhenFfmpegTimesOut() {
        ProcessExecutionResult mockResult = new ProcessExecutionResult(
            null, "", "", true, Duration.ofSeconds(5)
        );
        when(processExecutor.execute(any(ProcessExecutionRequest.class))).thenReturn(mockResult);

        assertThatThrownBy(() -> converter.convert(sourceFile, targetFile))
            .isInstanceOf(ConversionException.class)
            .hasMessageContaining("FFmpeg process timed out during conversion");
    }

    @Test
    void throwsConversionExceptionWhenFfmpegFailsToStart() {
        when(processExecutor.execute(any(ProcessExecutionRequest.class)))
            .thenThrow(new ProcessExecutionException("Executable not found"));

        assertThatThrownBy(() -> converter.convert(sourceFile, targetFile))
            .isInstanceOf(ConversionException.class)
            .hasCauseInstanceOf(ProcessExecutionException.class);
    }

    @Test
    void throwsConversionExceptionWhenTargetFileNotCreated() {
        ProcessExecutionResult mockResult = new ProcessExecutionResult(
            0, "", "", false, Duration.ofSeconds(1)
        );
        when(processExecutor.execute(any(ProcessExecutionRequest.class))).thenReturn(mockResult);

        assertThatThrownBy(() -> converter.convert(sourceFile, targetFile))
            .isInstanceOf(ConversionException.class)
            .hasMessageContaining("Conversion completed successfully but target file does not exist");
    }

    @Test
    void throwsConversionExceptionWhenTargetFileIsEmpty() throws IOException {
        ProcessExecutionResult mockResult = new ProcessExecutionResult(
            0, "", "", false, Duration.ofSeconds(1)
        );

        when(processExecutor.execute(any(ProcessExecutionRequest.class))).thenAnswer(invocation -> {
            Files.createFile(targetFile); // empty file
            return mockResult;
        });

        assertThatThrownBy(() -> converter.convert(sourceFile, targetFile))
            .isInstanceOf(ConversionException.class)
            .hasMessageContaining("target file is empty (0 bytes)");
    }

    @Test
    void integrationTestRealFfmpegConversion() throws IOException {
        boolean ffmpegAvailable = false;
        LocalJvmProcessExecutor localExecutor = new LocalJvmProcessExecutor(Duration.ofSeconds(10), 1024);
        try {
            ProcessExecutionResult res = localExecutor.execute(new ProcessExecutionRequest(
                "ffmpeg", List.of("-version"), tempDir, null, Map.of()
            ));
            ffmpegAvailable = res.exitCode() != null && res.exitCode() == 0;
        } catch (Exception e) {
            // not available
        }

        if (!ffmpegAvailable) {
            System.out.println("FFmpeg not available on system path. Skipping integration test.");
            return;
        }

        FfmpegMp4ToMp3Converter realConverter = new FfmpegMp4ToMp3Converter(localExecutor, "ffmpeg");

        Path invalidSource = tempDir.resolve("invalid.mp4");
        Files.writeString(invalidSource, "not a real mp4 video");

        Path target = tempDir.resolve("output.mp3");

        assertThatThrownBy(() -> realConverter.convert(invalidSource, target))
            .isInstanceOf(ConversionException.class)
            .hasMessageContaining("FFmpeg process exited with code");
    }

    private void verifyRequest(ProcessExecutionRequest request, Path source, Path target) {
        assertThat(request.executable()).isEqualTo("ffmpeg");

        List<String> expectedArgs = List.of(
            "-y",
            "-i", source.toAbsolutePath().toString(),
            "-vn",
            "-acodec", "libmp3lame",
            "-q:a", "2",
            target.toAbsolutePath().toString()
        );
        assertThat(request.arguments()).containsExactlyElementsOf(expectedArgs);
        assertThat(request.workingDirectory()).isEqualTo(target.toAbsolutePath().getParent());
    }
}
