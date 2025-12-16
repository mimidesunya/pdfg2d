package net.zamasoft.pdfg2d.gc.image;

import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.GraphicsException;

/**
 * Represents an image.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface Image {
	/**
	 * Returns the image width.
	 * 
	 * @return the width
	 */
	public double getWidth();

	/**
	 * Returns the image height.
	 * 
	 * @return the height
	 */
	public double getHeight();

	/**
	 * Draws the image.
	 * 
	 * @param gc the graphics context
	 * @throws GraphicsException if a graphics error occurs
	 */
	public void drawTo(final GC gc) throws GraphicsException;

	/**
	 * Returns the alternative string for the image.
	 * 
	 * @return the alternative string
	 */
	public String getAltString();
}
