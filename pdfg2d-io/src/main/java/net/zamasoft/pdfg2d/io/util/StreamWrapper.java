package net.zamasoft.pdfg2d.io.util;

import java.io.IOException;

import net.zamasoft.pdfg2d.io.FragmentedStream;

/**
 * RandomBuilder のラッパークラスです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class StreamWrapper implements FragmentedStream {
	protected final FragmentedStream builder;

	public StreamWrapper(FragmentedStream builder) {
		this.builder = builder;
	}

	public void addFragment() throws IOException {
		this.builder.addFragment();
	}

	public void insertFragmentBefore(int anchorId) throws IOException {
		this.builder.insertFragmentBefore(anchorId);
	}

	public void write(int id, byte[] b, int off, int len) throws IOException {
		this.builder.write(id, b, off, len);
	}

	public void finishFragment(int id) throws IOException {
		this.builder.finishFragment(id);
	}

	public void close() throws IOException {
		this.builder.close();
	}

	public PositionInfo getPositionInfo() {
		return this.builder.getPositionInfo();
	}

	public boolean supportsPositionInfo() {
		return this.builder.supportsPositionInfo();
	}
}