package com.lucasdourado.mediautility.media.conversion;

import java.io.IOException;
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
 * Concrete implementation of Mp4ToMp3Converter that uses FFmpeg via the shared ProcessExecutor.
 */
@Service
public class FfmpegMp4ToMp3Converter implements Mp4ToMp3Converter {

    private final ProcessExecutor processExecutor;
    private final String ffmpegPath;

    public FfmpegMp4ToMp3Converter(
            ProcessExecutor processExecutor,
            @Value("${media-utility.ffmpeg.path:ffmpeg}") String ffmpegPath) {
        this.processExecutor = processExecutor;
        this.ffmpegPath = ffmpegPath;
    }

    @Override
    public void convert(Path source, Path target) {
        if (source == null) {
            throw new ConversionException("Source path must not be null.");
        }
        if (target == null) {
            throw new ConversionException("Target path must not be null.");
        }

        // Pre-execution validation
        if (!Files.exists(source)) {
            throw new ConversionException("Source file does not exist: " + source);
        }
        if (!Files.isRegularFile(source)) {
            throw new ConversionException("Source path is not a regular file: " + source);
        }
        try {
            if (Files.size(source) == 0) {
                throw new ConversionException("Source file is empty (0 bytes): " + source);
            }
        } catch (IOException e) {
            throw new ConversionException("Failed to read source file size: " + source, e);
        }

        Path absoluteTarget = target.toAbsolutePath();
        Path workingDirectory = absoluteTarget.getParent();
        if (workingDirectory == null) {
            throw new ConversionException("Target path must have a parent directory: " + target);
        }

        try {
            Files.createDirectories(workingDirectory);
        } catch (IOException e) {
            throw new ConversionException("Failed to create working directory for target file: " + workingDirectory, e);
        }

        List<String> arguments = List.of(
                "-y",
                "-i", source.toAbsolutePath().toString(),
                "-vn",
                "-acodec", "libmp3lame",
                "-q:a", "2",
                absoluteTarget.toString()
        );

        ProcessExecutionRequest request = new ProcessExecutionRequest(
                ffmpegPath,
                arguments,
                workingDirectory,
                null, // Inherit default process timeout from ProcessExecutor
                Map.of()
        );

        ProcessExecutionResult result;
        try {
            result = processExecutor.execute(request);
        } catch (ProcessExecutionException | IllegalArgumentException e) {
            throw new ConversionException("Process execution failed to start or configuration was invalid.", e);
        }

        if (result.timedOut()) {
            throw new ConversionException("FFmpeg process timed out during conversion.");
        }

        if (result.exitCode() == null || result.exitCode() != 0) {
            int code = result.exitCode() != null ? result.exitCode() : -1;
            throw new ConversionException("FFmpeg process exited with code " + code + ". Stderr: " + result.stderr());
        }

        // Post-execution validation
        if (!Files.exists(absoluteTarget)) {
            throw new ConversionException("Conversion completed successfully but target file does not exist: " + absoluteTarget);
        }
        if (!Files.isRegularFile(absoluteTarget)) {
            throw new ConversionException("Conversion completed successfully but target path is not a regular file: " + absoluteTarget);
        }
        try {
            if (Files.size(absoluteTarget) == 0) {
                throw new ConversionException("Conversion completed successfully but target file is empty (0 bytes): " + absoluteTarget);
            }
        } catch (IOException e) {
            throw new ConversionException("Failed to read target file size: " + absoluteTarget, e);
        }
    }
}
