package net.zamasoft.pdfg2d.gc.paint;

import java.awt.geom.AffineTransform;

/**
 * Represents a linear gradient paint.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class LinearGradient implements Paint {
	protected final double x1, y1;
	protected final double x2, y2;
	protected final Color[] colors;
	protected final double[] fractions;
	protected final AffineTransform transform;

	/**
	 * Creates a new LinearGradient.
	 * 
	 * @param x1        the X coordinate of the start point
	 * @param y1        the Y coordinate of the start point
	 * @param x2        the X coordinate of the end point
	 * @param y2        the Y coordinate of the end point
	 * @param fractions the fractions for color distribution
	 * @param colors    the colors to distribute
	 * @param transform the transform to apply
	 * @throws NullPointerException if colors, fractions, or transform is null
	 */
	public LinearGradient(final double x1, final double y1, final double x2, final double y2, final double[] fractions,
			final Color[] colors,
			final AffineTransform transform) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.colors = colors;
		this.fractions = fractions;
		this.transform = transform;
		if (colors == null) {
			throw new NullPointerException("Colors cannnot be null.");
		}
		if (fractions == null) {
			throw new NullPointerException("Fractions cannnot be null.");
		}
		if (transform == null) {
			throw new NullPointerException("Transform cannnot be null.");
		}
	}

	@Override
	public Paint.Type getPaintType() {
		return Paint.Type.LINEAR_GRADIENT;
	}

	/**
	 * Returns the X coordinate of the start point.
	 * 
	 * @return the X coordinate
	 */
	public double getX1() {
		return this.x1;
	}

	/**
	 * Returns the Y coordinate of the start point.
	 * 
	 * @return the Y coordinate
	 */
	public double getY1() {
		return this.y1;
	}

	/**
	 * Returns the X coordinate of the end point.
	 * 
	 * @return the X coordinate
	 */
	public double getX2() {
		return this.x2;
	}

	/**
	 * Returns the Y coordinate of the end point.
	 * 
	 * @return the Y coordinate
	 */
	public double getY2() {
		return this.y2;
	}

	/**
	 * Returns the colors of this gradient.
	 * 
	 * @return the colors
	 */
	public Color[] getColors() {
		return this.colors;
	}

	/**
	 * Returns the fractions of this gradient.
	 * 
	 * @return the fractions
	 */
	public double[] getFractions() {
		return this.fractions;
	}

	/**
	 * Returns the transform applied to this gradient.
	 * 
	 * @return the transform
	 */
	public AffineTransform getTransform() {
		return this.transform;
	}

	@Override
	public String toString() {
		return new StringBuilder().append(super.toString()).append("[x1=").append(this.x1).append(",y1=")
				.append(this.y1)
				.append(",x2=").append(this.x2).append(",y2=").append(this.y2).append(",fractions=")
				.append(this.fractions)
				.append(",colors=").append(this.colors).append(",transform=").append(this.transform).append("]")
				.toString();
	}
}
