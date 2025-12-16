package net.zamasoft.pdfg2d.svg;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.VolatileImage;

/**
 * Graphics configuration for SVG rendering operations.
 * 
 * <p>
 * This class provides a minimal {@link GraphicsConfiguration} implementation
 * suitable for rendering SVG content. It supports creating compatible buffered
 * images with or without alpha channel.
 * 
 * <p>
 * A shared singleton instance is available via {@link #SHARED_INSTANCE}.
 * 
 * @since 1.0
 */
class SVGGraphicsConfiguration extends GraphicsConfiguration {

	/** Buffered image template with alpha channel for color model queries. */
	private static final BufferedImage BI_WITH_ALPHA = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

	/** Buffered image template without alpha channel for color model queries. */
	private static final BufferedImage BI_WITHOUT_ALPHA = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

	/** Shared singleton instance. */
	public static final SVGGraphicsConfiguration SHARED_INSTANCE = new SVGGraphicsConfiguration();

	/**
	 * Private constructor for singleton pattern.
	 */
	private SVGGraphicsConfiguration() {
		// Singleton - use SHARED_INSTANCE
	}

	/**
	 * Creates a compatible buffered image with the specified transparency.
	 *
	 * @param width        the image width
	 * @param height       the image height
	 * @param transparency the transparency type (from {@link Transparency})
	 * @return a new buffered image
	 */
	@Override
	public BufferedImage createCompatibleImage(final int width, final int height,
			final int transparency) {
		if (transparency == Transparency.OPAQUE) {
			return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		}
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	/**
	 * Creates a compatible buffered image with alpha channel.
	 *
	 * @param width  the image width
	 * @param height the image height
	 * @return a new ARGB buffered image
	 */
	@Override
	public BufferedImage createCompatibleImage(final int width, final int height) {
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	/**
	 * Returns the bounds of this configuration.
	 *
	 * @return always null (not applicable for SVG rendering)
	 */
	@Override
	public Rectangle getBounds() {
		return null;
	}

	/**
	 * Returns the color model with alpha channel.
	 *
	 * @return the ARGB color model
	 */
	@Override
	public ColorModel getColorModel() {
		return BI_WITH_ALPHA.getColorModel();
	}

	/**
	 * Returns the color model for the specified transparency.
	 *
	 * @param transparency the transparency type
	 * @return the appropriate color model
	 */
	@Override
	public ColorModel getColorModel(final int transparency) {
		if (transparency == Transparency.OPAQUE) {
			return BI_WITHOUT_ALPHA.getColorModel();
		}
		return BI_WITH_ALPHA.getColorModel();
	}

	/**
	 * Returns the default transform (identity).
	 *
	 * @return an identity transform
	 */
	@Override
	public AffineTransform getDefaultTransform() {
		return new AffineTransform();
	}

	/**
	 * Returns the normalizing transform (2x scale for high-DPI).
	 *
	 * @return a 2x scaling transform
	 */
	@Override
	public AffineTransform getNormalizingTransform() {
		return new AffineTransform(2, 0, 0, 2, 0, 0);
	}

	/**
	 * Returns the graphics device for this configuration.
	 *
	 * @return a new SVG graphics device
	 */
	@Override
	public GraphicsDevice getDevice() {
		return new SVGGraphicsDevice(this);
	}

	/**
	 * Volatile images are not supported.
	 *
	 * @param width  unused
	 * @param height unused
	 * @return never returns
	 * @throws UnsupportedOperationException always
	 */
	@Override
	public VolatileImage createCompatibleVolatileImage(final int width, final int height) {
		throw new UnsupportedOperationException("Volatile images not supported for SVG rendering");
	}

	/**
	 * Volatile images are not supported.
	 *
	 * @param width        unused
	 * @param height       unused
	 * @param transparency unused
	 * @return never returns
	 * @throws UnsupportedOperationException always
	 */
	@Override
	public VolatileImage createCompatibleVolatileImage(final int width, final int height,
			final int transparency) {
		throw new UnsupportedOperationException("Volatile images not supported for SVG rendering");
	}
}
