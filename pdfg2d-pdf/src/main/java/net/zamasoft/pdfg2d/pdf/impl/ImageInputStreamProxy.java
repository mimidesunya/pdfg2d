package net.zamasoft.pdfg2d.pdf.impl;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageInputStreamProxy extends InputStream {
    private final ImageInputStream imageInputStream;

    public ImageInputStreamProxy(ImageInputStream imageInputStream) {
        this.imageInputStream = imageInputStream;
    }

    @Override
    public int read() throws IOException {
        return imageInputStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return imageInputStream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return imageInputStream.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return imageInputStream.skipBytes(n);
    }

    @Override
    public int available() throws IOException {
        long available = imageInputStream.length() - imageInputStream.getStreamPosition();
        return available > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) available;
    }

    @Override
    public void close() throws IOException {
        imageInputStream.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        imageInputStream.mark();
    }

    @Override
    public synchronized void reset() throws IOException {
        imageInputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return true;
    }
}
