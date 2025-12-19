package net.zamasoft.pdfg2d.io.impl;

import java.io.IOException;
import java.io.OutputStream;

import net.zamasoft.pdfg2d.io.SequentialOutput;

/**
 * Sequential output that builds results to an existing OutputStream.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class StreamSequentialOutput extends AbstractTempFileOutput implements SequentialOutput {
	protected final OutputStream out;

	public StreamSequentialOutput(final OutputStream out, final int fragmentBufferSize, final int totalBufferSize,
			final int threshold) {
		super(fragmentBufferSize, totalBufferSize, threshold);
		this.out = out;
	}

	public StreamSequentialOutput(final OutputStream out) {
		super();
		this.out = out;
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		this.out.write(b, off, len);
	}

	@Override
	public void close() throws IOException {
		try {
			this.finish(this.out);
			this.out.close();
		} finally {
			super.close();
		}
	}
}