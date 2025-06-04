package jp.cssj.resolver.zip;

import java.io.File;

import jp.cssj.resolver.SourceValidity;

class ZipFileSourceValidity implements SourceValidity {
	private static final long serialVersionUID = 0L;

	private final long timestamp;

	private final File file;

	public ZipFileSourceValidity(long timestamp, File file) {
		this.timestamp = timestamp;
		this.file = file;
	}

	public Validity getValid() {
		return this.getValid(this.file);
	}

	public Validity getValid(SourceValidity validity) {
		return this.getValid(((ZipFileSourceValidity) validity).file);
	}

	private Validity getValid(File file) {
		if (file.lastModified() != this.timestamp) {
			return Validity.INVALID;
		}
		return Validity.VALID;
	}

}
