package net.zamasoft.pdfg2d.svg;

import java.awt.Graphics2D;

import org.apache.batik.gvt.GraphicsNode;

import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.GraphicsException;
import net.zamasoft.pdfg2d.gc.image.Image;

/**
 * SVG画像です。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class SVGImage implements Image {
	protected final GraphicsNode gvtRoot;

	protected final double width, height;

	public SVGImage(GraphicsNode gvtRoot, double width, double height) {
		this.gvtRoot = gvtRoot;
		this.width = width;
		this.height = height;
	}

	public GraphicsNode getNode() {
		return this.gvtRoot;
	}

	public double getWidth() {
		return this.width;
	}

	public double getHeight() {
		return this.height;
	}

	public void drawTo(GC gc) throws GraphicsException {
		 gc.begin();
		 Graphics2D g2d = new SVGBridgeGraphics2D(gc);
		 this.gvtRoot.paint(g2d);
		 g2d.dispose();
		 gc.end();
	}

	public String getAltString() {
		return null;
	}
}
