package net.zamasoft.pdfg2d.resolver.protocol.zip;

import java.io.File;
import net.zamasoft.pdfg2d.resolver.SourceValidity;

record ZIPFileSourceValidity(long timestamp, File file) implements SourceValidity {
	@Override
	public Validity getValid() {
		return checkValidity(this.file);
	}

	@Override
	public Validity getValid(SourceValidity validity) {
		if (validity instanceof ZIPFileSourceValidity other) {
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
