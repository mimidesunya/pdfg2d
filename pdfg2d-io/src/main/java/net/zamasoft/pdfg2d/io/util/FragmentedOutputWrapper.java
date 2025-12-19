package net.zamasoft.pdfg2d.io.util;

import java.io.IOException;

import net.zamasoft.pdfg2d.io.FragmentedOutput;

/**
 * Wrapper for FragmentedOutput.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class FragmentedOutputWrapper implements FragmentedOutput {
	protected final FragmentedOutput builder;

	public FragmentedOutputWrapper(final FragmentedOutput builder) {
		this.builder = builder;
	}

	@Override
	public void addFragment() throws IOException {
		this.builder.addFragment();
	}

	@Override
	public void insertFragmentBefore(final int anchorId) throws IOException {
		this.builder.insertFragmentBefore(anchorId);
	}

	@Override
	public void write(final int id, final byte[] b, final int off, final int len) throws IOException {
		this.builder.write(id, b, off, len);
	}

	@Override
	public void finishFragment(final int id) throws IOException {
		this.builder.finishFragment(id);
	}

	@Override
	public PositionInfo getPositionInfo() {
		return this.builder.getPositionInfo();
	}

	@Override
	public boolean supportsPositionInfo() {
		return this.builder.supportsPositionInfo();
	}

	@Override
	public void close() throws IOException {
		this.builder.close();
	}
}