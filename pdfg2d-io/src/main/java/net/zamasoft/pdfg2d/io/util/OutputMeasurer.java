package net.zamasoft.pdfg2d.io.util;

import java.io.IOException;

import net.zamasoft.pdfg2d.io.FragmentedOutput;

/**
 * Measures the total size of the data being built.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class OutputMeasurer extends FragmentedOutputWrapper {
	protected long length;

	public OutputMeasurer(final FragmentedOutput builder) {
		super(builder);
	}

	@Override
	public void write(final int id, final byte[] b, final int off, final int len) throws IOException {
		super.write(id, b, off, len);
		this.length += len;
	}

	/**
	 * Returns the size of the content.
	 * 
	 * @return total bytes of data added.
	 */
	public long getLength() {
		return this.length;
	}
}
