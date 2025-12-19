package net.zamasoft.pdfg2d.pdf.impl;

import net.zamasoft.pdfg2d.io.FragmentedOutput.PositionInfo;
import net.zamasoft.pdfg2d.pdf.ObjectRef;

/**
 * Implementation of PDF object reference with position tracking.
 */
public final class ObjectRefImpl extends ObjectRef {
	private int position = -1;
	private int id;

	/**
	 * Creates a new object reference with the given object number.
	 * 
	 * @param objectNum the object number
	 */
	public ObjectRefImpl(final int objectNum) {
		super(objectNum, 0);
	}

	/**
	 * Sets the position of this object in the PDF output.
	 * 
	 * @param id       the fragment ID
	 * @param position the position within the fragment
	 * @throws IllegalStateException if the position has already been set
	 */
	public void setPosition(final int id, final int position) {
		if (this.position != -1) {
			throw new IllegalStateException("Cannot create object twice with the same reference.");
		}
		this.id = id;
		this.position = position;
	}

	/**
	 * Gets the absolute position in the output.
	 * 
	 * @param info the position info from the fragmented output
	 * @return the absolute position
	 */
	public long getPosition(final PositionInfo info) {
		return info.getPosition(this.id) + this.position;
	}

	@Override
	public String toString() {
		return "R " + this.objectNumber() + " " + this.generationNumber();
	}
}