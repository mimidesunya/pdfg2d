package net.zamasoft.pdfg2d.io.impl;

import java.io.IOException;
import java.io.OutputStream;

import net.zamasoft.pdfg2d.io.SequentialStream;

/**
 * ストリームに対して結果を構築する RandomBuilder です。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class OutputFragmentedStream extends AbstractTempFileStream implements SequentialStream {
	protected final OutputStream out;

	public OutputFragmentedStream(OutputStream out, int fragmentBufferSize, int totalBufferSize, int threshold) {
		super(fragmentBufferSize, totalBufferSize, threshold);
		this.out = out;
	}

	public OutputFragmentedStream(OutputStream out) {
		super();
		this.out = out;
	}

	public void write(byte[] b, int off, int len) throws IOException {
		this.out.write(b, off, len);
	}

	public void close() throws IOException {
		try {
			this.finish(this.out);
			this.out.close();
		} finally {
			super.close();
		}
	}
}