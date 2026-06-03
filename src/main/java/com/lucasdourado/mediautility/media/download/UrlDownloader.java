package com.lucasdourado.mediautility.media.download;

import java.net.URI;
import java.nio.file.Path;

/**
 * Interface for downloading media from a URL.
 */
public interface UrlDownloader extends DownloadBoundary {

	/**
	 * Downloads media from the specified URL to the target path.
	 *
	 * @param url    the URL to download media from
	 * @param target the target local path to save the media to
	 * @throws DownloadException if the download fails
	 */
	void download(URI url, Path target);
}
