package net.zamasoft.pdfg2d.gc.text;

/**
 * Represents an element in the text layout.
 */
public sealed interface Element permits Text, TextControl {

	/**
	 * Returns the advance width of the element.
	 * 
	 * @return the advance width
	 */
	public double getAdvance();
}
