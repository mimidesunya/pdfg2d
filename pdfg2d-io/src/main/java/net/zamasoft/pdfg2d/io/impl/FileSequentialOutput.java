package net.zamasoft.pdfg2d.io.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.zamasoft.pdfg2d.io.SequentialOutput;

/**
 * Sequential output that builds results to a file.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class FileSequentialOutput extends AbstractTempFileOutput implements SequentialOutput {
	protected final File file;

	protected OutputStream out = null;

	public FileSequentialOutput(final File file, final int fragmentBufferSize, final int totalBufferSize,
			final int threshold) {
		super(fragmentBufferSize, totalBufferSize, threshold);
		this.file = file;
	}

	public FileSequentialOutput(final File file) {
		super();
		this.file = file;
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		if (this.out == null) {
			this.out = new FileOutputStream(this.file);
		}
		this.out.write(b, off, len);
	}

	@Override
	public void close() throws IOException {
		try {
			if (this.out != null) {
				this.out.close();
				this.out = null;
				return;
			}
			try (final var os = new FileOutputStream(this.file)) {
				this.finish(os);
			}
		} finally {
			super.close();
		}
	}
}