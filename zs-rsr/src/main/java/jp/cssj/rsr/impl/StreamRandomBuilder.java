package jp.cssj.rsr.impl;

import java.io.IOException;
import java.io.OutputStream;

import jp.cssj.rsr.Sequential;

/**
 * ストリームに対して結果を構築する RandomBuilder です。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: StreamRandomBuilder.java 1565 2018-07-04 11:51:25Z miyabe $
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

	public void finish() throws IOException {
		this.finish(this.out);
	}

	public void dispose() {
		super.dispose();
		try {
			this.out.close();
		} catch (IOException e) {
			// ignore
		}
	}
}