package com.lucasdourado.mediautility.media.conversion;

import java.nio.file.Path;

/**
 * Service boundary for MP4 to MP3 conversion operations.
 */
public interface Mp4ToMp3Converter extends ConversionBoundary {

    /**
     * Converts a local MP4 source file into an MP3 target file.
     *
     * @param source local path to the source MP4 file, must exist and not be empty
     * @param target local path where the output MP3 file should be written
     * @throws ConversionException if conversion fails, times out, or output is invalid
     */
    void convert(Path source, Path target);
}
