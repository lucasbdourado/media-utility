package com.lucasdourado.mediautility.operations;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Server-side metadata for a completed operation result.
 */
@Embeddable
public class ResultFileMetadata {

	@Column(name = "result_file_name")
	private String fileName;

	@Column(name = "result_content_type")
	private String contentType;

	@Column(name = "result_size_bytes")
	private Long sizeBytes;

	@Column(name = "result_internal_path")
	private String internalPath;

	protected ResultFileMetadata() {
	}

	public ResultFileMetadata(String fileName, String contentType, Long sizeBytes, String internalPath) {
		this.fileName = fileName;
		this.contentType = contentType;
		this.sizeBytes = sizeBytes;
		this.internalPath = internalPath;
	}

	public String getFileName() {
		return fileName;
	}

	public String getContentType() {
		return contentType;
	}

	public Long getSizeBytes() {
		return sizeBytes;
	}

	public String getInternalPath() {
		return internalPath;
	}
}
