package net.zamasoft.pdfg2d.gc.text;

/**
 * Represents an element in the text layout.
 */
public interface Element {
	/**
	 * Enumeration of element types.
	 */
	public enum Type {
		/** Text element. */
		TEXT,
		/** Quad element (e.g., whitespace, tabs, newlines). */
		QUAD
	}

	/**
	 * Returns the type of the element.
	 * 
	 * @return the element type
	 */
	public Type getElementType();

	/**
	 * Returns the advance width of the element.
	 * 
	 * @return the advance width
	 */
	public double getAdvance();
}
