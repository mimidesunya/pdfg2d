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
 * @version $Id: SVGImage.java 1569 2018-07-09 05:10:42Z miyabe $
 */
public class SvgImage implements Image {
	protected final GraphicsNode gvtRoot;

	protected final double width, height;

	public SvgImage(GraphicsNode gvtRoot, double width, double height) {
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
		 Graphics2D g2d = new SvgBridgeGraphics2D(gc);
		 this.gvtRoot.paint(g2d);
		 g2d.dispose();
		 gc.end();
	}

	public String getAltString() {
		return null;
	}
}
