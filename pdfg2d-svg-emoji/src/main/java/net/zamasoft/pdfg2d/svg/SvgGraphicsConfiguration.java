package net.zamasoft.pdfg2d.svg;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.VolatileImage;

class SvgGraphicsConfiguration extends GraphicsConfiguration {
	private static BufferedImage BIWithAlpha = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

	private static BufferedImage BIWithOutAlpha = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

	public static final SvgGraphicsConfiguration SHARED_INSTANCE = new SvgGraphicsConfiguration();

	private SvgGraphicsConfiguration() {
		// internal
	}

	public BufferedImage createCompatibleImage(int width, int height, int transparency) {
		if (transparency == Transparency.OPAQUE)
			return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		else
			return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	public BufferedImage createCompatibleImage(int width, int height) {
		return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	public Rectangle getBounds() {
		return null;
	}

	public ColorModel getColorModel() {
		return BIWithAlpha.getColorModel();
	}

	public ColorModel getColorModel(int transparency) {
		if (transparency == Transparency.OPAQUE)
			return BIWithOutAlpha.getColorModel();
		else
			return BIWithAlpha.getColorModel();
	}

	public AffineTransform getDefaultTransform() {
		return new AffineTransform();
	}

	public AffineTransform getNormalizingTransform() {
		return new AffineTransform(2, 0, 0, 2, 0, 0);
	}

	public GraphicsDevice getDevice() {
		return new SvgGraphicsDevice(this);
	}

	public VolatileImage createCompatibleVolatileImage(int width, int height) {
		throw new UnsupportedOperationException();
	}

	public VolatileImage createCompatibleVolatileImage(int width, int height, int transparency) {
		throw new UnsupportedOperationException();
	}
}
