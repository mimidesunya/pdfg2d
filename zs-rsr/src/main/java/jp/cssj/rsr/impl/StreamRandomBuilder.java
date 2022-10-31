package jp.cssj.rsr.impl;

import java.io.IOException;
import java.io.OutputStream;

import jp.cssj.rsr.Sequential;

/**
 * ストリームに対して結果を構築する RandomBuilder です。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class StreamRandomBuilder extends AbstractRandomAccessFileBuilder implements Sequential {
	protected final OutputStream out;

	public StreamRandomBuilder(OutputStream out, int fragmentBufferSize, int totalBufferSize, int threshold) {
		super(fragmentBufferSize, totalBufferSize, threshold);
		this.out = out;
	}

	public StreamRandomBuilder(OutputStream out) {
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