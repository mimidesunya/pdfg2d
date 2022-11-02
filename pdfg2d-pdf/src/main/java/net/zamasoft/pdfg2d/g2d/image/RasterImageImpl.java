package net.zamasoft.pdfg2d.g2d.image;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;

import net.zamasoft.pdfg2d.g2d.gc.G2DGC;
import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.GraphicsException;
import net.zamasoft.pdfg2d.gc.image.Image;
import net.zamasoft.pdfg2d.pdf.gc.PDFGC;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */

public class RasterImageImpl implements RasterImage, ImageObserver {
	private BufferedImage image;

	private final String altString;

	private int width = -1, height = -1;

	public RasterImageImpl(BufferedImage image, String altString) {
		if (image == null) {
			throw new NullPointerException();
		}
		this.image = image;
		this.altString = altString;
	}

	public RasterImageImpl(BufferedImage image) {
		this(image, null);
	}

	public BufferedImage getImage() {
		return this.image;
	}

	public synchronized double getWidth() {
		if (this.width == -1) {
			this.width = this.image.getWidth(this);
			while (this.width == -1) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {
					return this.width = 0;
				}
			}
		}
		return this.width;
	}

	public synchronized double getHeight() {
		if (this.height == -1) {
			this.height = this.image.getHeight(this);
			while (this.height == -1) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {
					return this.height = 0;
				}
			}
		}
		return this.height;
	}

	public String getAltString() {
		return this.altString;
	}

	public void drawTo(GC gc) throws GraphicsException {
		if (gc instanceof PDFGC) {
			try {
				Image image = ((PDFGC) gc).getPDFGraphicsOutput().getPdfWriter().addImage(this.image);
				gc.drawImage(image);
			} catch (IOException e) {
				throw new GraphicsException(e);
			}
		} else {
			java.awt.Image image = this.image;
			Graphics2D g2d = ((G2DGC) gc).getGraphics2D();
			AffineTransform at = g2d.getTransform();
			g2d.drawImage(image, null, null);
			if (image != this.image) {
				g2d.setTransform(at);
				image.flush();
			}
		}
	}

	public synchronized boolean imageUpdate(java.awt.Image img, int infoflags, int x, int y, int width, int height) {
		if ((infoflags & (ERROR | ABORT)) != 0) {
			this.width = this.height = 0;
		} else {
			if ((infoflags & WIDTH) != 0) {
				this.width = width;
			}
			if ((infoflags & HEIGHT) != 0) {
				this.height = height;
			}
		}
		this.notifyAll();
		return (this.width == -1 || this.height == -1);
	}
}