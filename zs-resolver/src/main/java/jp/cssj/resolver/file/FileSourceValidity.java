package jp.cssj.resolver.file;

import java.io.File;

import jp.cssj.resolver.SourceValidity;

class FileSourceValidity implements SourceValidity {
	private static final long serialVersionUID = 0L;

	private final long timestamp;

	private final File file;

	public FileSourceValidity(long timestamp, File file) {
		this.timestamp = timestamp;
		this.file = file;
	}

	public int getValid() {
		return this.getValid(this.file);
	}

	public int getValid(SourceValidity validity) {
		return this.getValid(((FileSourceValidity) validity).file);
	}

	private int getValid(File file) {
		if (file.lastModified() != this.timestamp) {
			return INVALID;
		}
		return VALID;
	}

}
