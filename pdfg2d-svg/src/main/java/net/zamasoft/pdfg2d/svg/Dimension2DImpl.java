package net.zamasoft.pdfg2d.svg;

import java.awt.geom.Dimension2D;

/**
 * Concrete implementation of {@link Dimension2D} for SVG rendering.
 * 
 * <p>
 * This class provides a simple mutable 2D dimension with double precision,
 * since {@link Dimension2D} itself is abstract.
 * 
 * @since 1.0
 */
public class Dimension2DImpl extends Dimension2D {

	/** The width of this dimension. */
	private double width;

	/** The height of this dimension. */
	private double height;

	/**
	 * Creates a new dimension with the specified width and height.
	 *
	 * @param width  the width
	 * @param height the height
	 */
	public Dimension2DImpl(final double width, final double height) {
		this.setSize(width, height);
	}

	/**
	 * Returns the height of this dimension.
	 *
	 * @return the height
	 */
	@Override
	public double getHeight() {
		return this.height;
	}

	/**
	 * Returns the width of this dimension.
	 *
	 * @return the width
	 */
	@Override
	public double getWidth() {
		return this.width;
	}

	/**
	 * Sets the size of this dimension.
	 *
	 * @param width  the new width
	 * @param height the new height
	 */
	@Override
	public void setSize(final double width, final double height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * Returns a string representation of this dimension.
	 *
	 * @return a string in the format "ClassName[width,height]"
	 */
	@Override
	public String toString() {
		return super.toString() + "[" + this.width + "," + this.height + "]";
	}
}
