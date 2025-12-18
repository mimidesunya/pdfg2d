package net.zamasoft.pdfg2d.resolver.protocol.file;

import java.io.File;
import net.zamasoft.pdfg2d.resolver.SourceValidity;

record FileSourceValidity(long timestamp, File file) implements SourceValidity {
	@Override
	public Validity getValid() {
		return checkValidity(this.file);
	}

	@Override
	public Validity getValid(SourceValidity validity) {
		if (validity instanceof FileSourceValidity other) {
			return checkValidity(other.file);
		}
		return Validity.UNKNOWN;
	}

	private Validity checkValidity(File f) {
		if (f.lastModified() != this.timestamp) {
			return Validity.INVALID;
		}
		return Validity.VALID;
	}
}

