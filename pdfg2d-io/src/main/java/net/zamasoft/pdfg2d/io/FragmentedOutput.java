package net.zamasoft.pdfg2d.io;

import java.io.Closeable;
import java.io.IOException;

/**
 * <p>
 * Interface for building fragmented data.
 * </p>
 * 
 * <p>
 * A new fragment is added using addFragment or insertFragmentBefore.
 * Fragments are assigned IDs 0, 1, 2, 3... in the order they are created.
 * The ID can be used to identify fragment when inserting a fragment or adding
 * data to a fragment.
 * </p>
 * 
 * <p>
 * If an instance implements the SequentialOutput interface, it indicates that
 * the data is
 * sequential by nature and there is no need to use insertFragmentBefore.
 * In this case, data can be built efficiently by calling methods of the
 * SequentialOutput interface without calling addFragment.
 * </p>
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface FragmentedOutput extends Closeable {
	/**
	 * Position information for fragments.
	 */
	@FunctionalInterface
	interface PositionInfo {
		/**
		 * <p>
		 * Returns the start position of the fragment.
		 * </p>
		 * <p>
		 * This is the value at the time getPositionInfo was called,
		 * and subsequent operations on the builder do not affect it.
		 * </p>
		 * 
		 * @param id fragment ID.
		 * @return start position of the fragment.
		 */
		long getPosition(int id);
	}

	/**
	 * Adds a fragment to the end.
	 * 
	 * @throws IOException if an I/O error occurs.
	 */
	void addFragment() throws IOException;

	/**
	 * Inserts a fragment immediately before the specified fragment.
	 * 
	 * @param anchorId anchor fragment ID.
	 * @throws IOException if an I/O error occurs.
	 */
	void insertFragmentBefore(int anchorId) throws IOException;

	/**
	 * Adds data to a fragment.
	 * 
	 * @param id  fragment ID.
	 * @param b   byte array.
	 * @param off start position in the byte array.
	 * @param len length of data in the byte array.
	 * @throws IOException if an I/O error occurs.
	 */
	void write(int id, byte[] b, int off, int len) throws IOException;

	/**
	 * Returns whether position information is supported.
	 * 
	 * @return true if position information is supported.
	 */
	boolean supportsPositionInfo();

	/**
	 * Returns an object to get the start position of each fragment being built.
	 * 
	 * @return position information.
	 * @throws UnsupportedOperationException if position information is not
	 *                                       supported.
	 */
	PositionInfo getPositionInfo() throws UnsupportedOperationException;

	/**
	 * Finishes writing to a fragment. This call is not mandatory,
	 * but it may optimize data construction.
	 * 
	 * @param id fragment ID.
	 * @throws IOException if an I/O error occurs.
	 */
	void finishFragment(int id) throws IOException;

	/**
	 * Completes data construction.
	 * 
	 * @throws IOException if an I/O error occurs.
	 */
	@Override
	void close() throws IOException;
}