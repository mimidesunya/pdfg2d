package net.zamasoft.pdfg2d.io.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import net.zamasoft.pdfg2d.io.SequentialOutput;

/**
 * An OutputStream that wraps a SequentialOutput.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class SequentialOutputAdapter extends OutputStream {
	private final SequentialOutput out;

	public SequentialOutputAdapter(final SequentialOutput out) {
		this.out = Objects.requireNonNull(out);
	}

	@Override
	public void write(final int b) throws IOException {
		final byte[] buff = { (byte) b };
		this.out.write(buff, 0, 1);
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		this.out.write(b, off, len);
	}

	@Override
	public void write(final byte[] b) throws IOException {
		this.out.write(b, 0, b.length);
	}

	@Override
	public void close() throws IOException {
		this.out.close();
	}
}