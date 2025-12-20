package net.zamasoft.pdfg2d.io.util;

import java.io.IOException;

import net.zamasoft.pdfg2d.io.FragmentedOutput;

/**
 * A wrapper that delegates all operations to another FragmentedOutput.
 * <p>
 * This class implements the Decorator pattern, allowing subclasses to
 * extend functionality (e.g., measuring size, tracking positions) while
 * delegating actual I/O to the wrapped instance.
 * </p>
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 * @see OutputMeasurer
 * @see PositionTrackingOutput
 */
public class FragmentedOutputWrapper implements FragmentedOutput {
	/** The wrapped FragmentedOutput instance. */
	protected final FragmentedOutput builder;

	/**
	 * Creates a new wrapper around the specified output.
	 * 
	 * @param builder the FragmentedOutput to wrap.
	 */
	public FragmentedOutputWrapper(final FragmentedOutput builder) {
		this.builder = builder;
	}

	/** {@inheritDoc} */
	@Override
	public void addFragment() throws IOException {
		this.builder.addFragment();
	}

	/** {@inheritDoc} */
	@Override
	public void insertFragmentBefore(final int anchorId) throws IOException {
		this.builder.insertFragmentBefore(anchorId);
	}

	/** {@inheritDoc} */
	@Override
	public void write(final int id, final byte[] b, final int off, final int len) throws IOException {
		this.builder.write(id, b, off, len);
	}

	/** {@inheritDoc} */
	@Override
	public void finishFragment(final int id) throws IOException {
		this.builder.finishFragment(id);
	}

	/** {@inheritDoc} */
	@Override
	public PositionInfo getPositionInfo() {
		return this.builder.getPositionInfo();
	}

	/** {@inheritDoc} */
	@Override
	public boolean supportsPositionInfo() {
		return this.builder.supportsPositionInfo();
	}

	/** {@inheritDoc} */
	@Override
	public void close() throws IOException {
		this.builder.close();
	}
}