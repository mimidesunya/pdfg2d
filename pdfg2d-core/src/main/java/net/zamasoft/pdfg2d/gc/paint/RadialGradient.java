package net.zamasoft.pdfg2d.gc.paint;

import java.awt.geom.AffineTransform;

/**
 * Represents a radial gradient paint.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class RadialGradient implements Paint {
	protected final double cx, cy;
	protected final double radius;
	protected final double fx, fy;
	protected final Color[] colors;
	protected final double[] fractions;
	protected final AffineTransform transform;

	/**
	 * Creates a new RadialGradient.
	 * 
	 * @param cx        the X coordinate of the center point
	 * @param cy        the Y coordinate of the center point
	 * @param radius    the radius of the circle
	 * @param fx        the X coordinate of the focus point
	 * @param fy        the Y coordinate of the focus point
	 * @param fractions the fractions for color distribution
	 * @param colors    the colors to distribute
	 * @param transform the transform to apply
	 */
	public RadialGradient(final double cx, final double cy, final double radius, final double fx, final double fy,
			final double[] fractions,
			final Color[] colors, final AffineTransform transform) {
		this.cx = cx;
		this.cy = cy;
		this.radius = radius;
		this.fx = fx;
		this.fy = fy;
		this.colors = colors;
		this.fractions = fractions;
		this.transform = transform;
	}

	@Override
	public Paint.Type getPaintType() {
		return Paint.Type.RADIAL_GRADIENT;
	}

	/**
	 * Returns the X coordinate of the center point.
	 * 
	 * @return the X coordinate
	 */
	public double getCX() {
		return this.cx;
	}

	/**
	 * Returns the Y coordinate of the center point.
	 * 
	 * @return the Y coordinate
	 */
	public double getCY() {
		return this.cy;
	}

	/**
	 * Returns the radius of the circle.
	 * 
	 * @return the radius
	 */
	public double getRadius() {
		return this.radius;
	}

	/**
	 * Returns the X coordinate of the focus point.
	 * 
	 * @return the X coordinate
	 */
	public double getFX() {
		return this.fx;
	}

	/**
	 * Returns the Y coordinate of the focus point.
	 * 
	 * @return the Y coordinate
	 */
	public double getFY() {
		return this.fy;
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
		return new StringBuilder().append(super.toString()).append("[cx=").append(this.cx).append(",cy=")
				.append(this.cy)
				.append(",radius=").append(this.radius).append(",fx=").append(this.fx).append(",fy=").append(this.fy)
				.append(",fractions=").append(this.fractions).append(",colors=").append(this.colors)
				.append(",transform=").append(this.transform).append("]").toString();
	}
}
