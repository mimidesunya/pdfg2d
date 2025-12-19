package net.zamasoft.pdfg2d.io.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import net.zamasoft.pdfg2d.io.FragmentedOutput;

/**
 * An OutputStream that writes data to a specific fragment of a
 * FragmentedOutput.
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: RandomBuilderOutputStream.java 656 2011-09-03 15:42:28Z miyabe
 *          $
 */
public class FragmentOutputAdapter extends OutputStream {
	private final FragmentedOutput builder;

	private final int fragmentId;

	private final byte[] singleByteBuffer = new byte[1];

	/**
	 * Creates a new adapter for the specified fragment.
	 * 
	 * @param builder    target FragmentedOutput.
	 * @param fragmentId target fragment ID.
	 */
	public FragmentOutputAdapter(final FragmentedOutput builder, final int fragmentId) {
		this.builder = Objects.requireNonNull(builder);
		this.fragmentId = fragmentId;
	}

	@Override
	public void write(final int b) throws IOException {
		this.singleByteBuffer[0] = (byte) b;
		this.builder.write(this.fragmentId, this.singleByteBuffer, 0, 1);
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		this.builder.write(this.fragmentId, b, off, len);
	}

	@Override
	public void write(final byte[] b) throws IOException {
		this.builder.write(this.fragmentId, b, 0, b.length);
	}

	/**
	 * Closes the fragment by calling finishFragment on the builder.
	 */
	@Override
	public void close() throws IOException {
		this.builder.finishFragment(this.fragmentId);
	}
}