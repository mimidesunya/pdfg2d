package net.zamasoft.pdfg2d.gc.paint;

import java.awt.geom.AffineTransform;

import net.zamasoft.pdfg2d.gc.image.Image;

/**
 * Represents a pattern paint using an image.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class Pattern implements Paint {
	protected final Image image;

	protected final AffineTransform at;

	/**
	 * Creates a new Pattern.
	 * 
	 * @param image the image to be used as pattern
	 * @param at    the affine transform to apply
	 */
	public Pattern(final Image image, final AffineTransform at) {
		assert at == null || at.getScaleX() != 0;
		assert at == null || at.getScaleY() != 0;
		this.image = image;
		this.at = at;
	}

	@Override
	public Paint.Type getPaintType() {
		return Paint.Type.PATTERN;
	}

	/**
	 * Returns the affine transform applied to this pattern.
	 * 
	 * @return the affine transform
	 */
	public AffineTransform getTransform() {
		return this.at;
	}

	/**
	 * Returns the image used as pattern.
	 * 
	 * @return the image
	 */
	public Image getImage() {
		return this.image;
	}

	@Override
	public String toString() {
		return super.toString() + "/image=" + this.image + ",at=" + this.at;
	}
}
