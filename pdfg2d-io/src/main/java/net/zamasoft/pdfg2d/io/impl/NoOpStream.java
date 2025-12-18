package net.zamasoft.pdfg2d.io.impl;

import java.io.IOException;

import net.zamasoft.pdfg2d.io.FragmentedStream;

/**
 * 何も生成しないRandomBuilderです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class NoOpStream implements FragmentedStream {
	private static final PositionInfo NULL_INFO = id -> 0;

	public static final NoOpStream SHARED_INSTANCE = new NoOpStream();

	private NoOpStream() {
		// private
	}

	public PositionInfo getPositionInfo() {
		return NULL_INFO;
	}

	public boolean supportsPositionInfo() {
		return true;
	}

	public void addFragment() throws IOException {
		// ignore
	}

	public void insertFragmentBefore(int anchorId) throws IOException {
		// ignore
	}

	public void write(int id, byte[] b, int off, int len) throws IOException {
		// ignore
	}

	public void finishFragment(int id) throws IOException {
		// ignore
	}

	public void close() throws IOException {
		// ignore
	}
}