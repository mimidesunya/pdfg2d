package net.zamasoft.pdfg2d.pdf.util.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FastBufferedOutputStream extends FilterOutputStream {
	protected final byte[] buf;
	protected int count = 0;

	public FastBufferedOutputStream(OutputStream out, byte[] buf) {
		super(out);
		this.buf = buf;
	}

	protected void writeAll() throws IOException {
		this.out.write(this.buf, 0, this.count);
		this.count = 0;
	}

	public void write(byte[] b, int off, int len) throws IOException {
		while (len > 0) {
			int a = this.buf.length - this.count;
			if (a <= len) {
				System.arraycopy(b, off, this.buf, this.count, a);
				this.count += a;
				off += a;
				len -= a;
				this.writeAll();
			} else {
				System.arraycopy(b, off, this.buf, this.count, len);
				this.count += len;
				break;
			}
		}
	}

	public void write(byte[] b) throws IOException {
		this.write(b, 0, b.length);
	}

	public void write(int b) throws IOException {
		this.buf[this.count++] = (byte) b;
		if (this.count >= this.buf.length) {
			this.writeAll();
		}
	}

	public void flush() throws IOException {
		if (this.count > 0) {
			this.writeAll();
		}
		this.out.flush();
	}
}
