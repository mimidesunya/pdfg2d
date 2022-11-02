package net.zamasoft.pdfg2d.g2d.image;

import java.awt.image.BufferedImage;

import net.zamasoft.pdfg2d.gc.image.Image;

public interface RasterImage extends Image {
	public BufferedImage getImage();
}
