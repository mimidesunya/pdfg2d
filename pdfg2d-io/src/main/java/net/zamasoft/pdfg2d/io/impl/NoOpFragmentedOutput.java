package net.zamasoft.pdfg2d.io.impl;

import java.io.IOException;

import net.zamasoft.pdfg2d.io.FragmentedOutput;

/**
 * A no-operation implementation of {@link FragmentedOutput}.
 * <p>
 * This class implements the Null Object pattern, providing a safe
 * implementation that discards all data. Useful when output is
 * not needed but a FragmentedOutput instance is required.
 * </p>
 * <p>
 * This is a singleton class; use {@link #INSTANCE} to get the instance.
 * </p>
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public final class NoOpFragmentedOutput implements FragmentedOutput {
	/** Position info that always returns 0 for any fragment ID. */
	private static final PositionInfo NULL_INFO = id -> 0;

	/** Singleton instance. */
	public static final NoOpFragmentedOutput INSTANCE = new NoOpFragmentedOutput();

	/**
	 * Private constructor to enforce singleton pattern.
	 */
	private NoOpFragmentedOutput() {
		// Singleton - use INSTANCE
	}

	/**
	 * Returns a position info that always returns 0.
	 * 
	 * @return position info returning 0 for all fragments.
	 */
	@Override
	public PositionInfo getPositionInfo() {
		return NULL_INFO;
	}

	/**
	 * Always returns true as position info is trivially supported.
	 * 
	 * @return always true.
	 */
	@Override
	public boolean supportsPositionInfo() {
		return true;
	}

	/**
	 * No-op: does nothing.
	 */
	@Override
	public void addFragment() throws IOException {
		// No-op: discards all data
	}

	/**
	 * No-op: does nothing.
	 * 
	 * @param anchorId ignored.
	 */
	@Override
	public void insertFragmentBefore(final int anchorId) throws IOException {
		// No-op: discards all data
	}

	/**
	 * No-op: discards all data.
	 * 
	 * @param id  ignored.
	 * @param b   ignored.
	 * @param off ignored.
	 * @param len ignored.
	 */
	@Override
	public void write(final int id, final byte[] b, final int off, final int len) throws IOException {
		// No-op: discards all data
	}

	/**
	 * No-op: does nothing.
	 * 
	 * @param id ignored.
	 */
	@Override
	public void finishFragment(final int id) throws IOException {
		// No-op: discards all data
	}

	/**
	 * No-op: does nothing.
	 */
	@Override
	public void close() throws IOException {
		// No-op: nothing to close
	}
}