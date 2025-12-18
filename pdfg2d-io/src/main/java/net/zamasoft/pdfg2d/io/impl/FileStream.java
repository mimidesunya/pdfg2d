package net.zamasoft.pdfg2d.io.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.zamasoft.pdfg2d.io.SequentialStream;

/**
 * ファイルに対して結果を構築する RandomBuilder です。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class FileStream extends AbstractTempFileStream implements SequentialStream {
	protected final File file;

	protected OutputStream out = null;

	public FileStream(File file, int fragmentBufferSize, int totalBufferSize, int threshold) {
		super(fragmentBufferSize, totalBufferSize, threshold);
		this.file = file;
	}

	public FileStream(File file) {
		super();
		this.file = file;
	}

	public void write(byte[] b, int off, int len) throws IOException {
		if (this.out == null) {
			this.out = new FileOutputStream(this.file);
		}
		this.out.write(b, off, len);
	}

	public void close() throws IOException {
		try {
			if (this.out != null) {
				this.out.close();
				this.out = null;
				return;
			}
			try (OutputStream out = new FileOutputStream(this.file)) {
				this.finish(out);
			}
		} finally {
			super.close();
		}
	}
}