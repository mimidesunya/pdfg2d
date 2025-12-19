package net.zamasoft.pdfg2d.io.impl;

import java.io.IOException;

import net.zamasoft.pdfg2d.io.FragmentedOutput;

/**
 * Fragmented output implementation that does nothing.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public final class NoOpFragmentedOutput implements FragmentedOutput {
	private static final PositionInfo NULL_INFO = id -> 0;

	public static final NoOpFragmentedOutput INSTANCE = new NoOpFragmentedOutput();

	private NoOpFragmentedOutput() {
		// private
	}

	@Override
	public PositionInfo getPositionInfo() {
		return NULL_INFO;
	}

	@Override
	public boolean supportsPositionInfo() {
		return true;
	}

	@Override
	public void addFragment() throws IOException {
		// ignore
	}

	@Override
	public void insertFragmentBefore(final int anchorId) throws IOException {
		// ignore
	}

	@Override
	public void write(final int id, final byte[] b, final int off, final int len) throws IOException {
		// ignore
	}

	@Override
	public void finishFragment(final int id) throws IOException {
		// ignore
	}

	@Override
	public void close() throws IOException {
		// ignore
	}
}