package net.zamasoft.pdfg2d.pdf;

import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.OutputStream;

import net.zamasoft.pdfg2d.gc.paint.CMYKColor;
import net.zamasoft.pdfg2d.gc.paint.Color;
import net.zamasoft.pdfg2d.gc.paint.Color.Type;
import net.zamasoft.pdfg2d.gc.paint.RGBColor;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;
import net.zamasoft.pdfg2d.util.ColorUtils;

/**
 * Output for PDF graphics operations.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public abstract class PDFGraphicsOutput extends PDFOutput {
	protected final double width, height;

	protected final PDFWriter pdfWriter;

	public PDFGraphicsOutput(final PDFWriter pdfWriter, final OutputStream out, final double width, final double height)
			throws IOException {
		super(out, pdfWriter.getParams().getPlatformEncoding());
		this.width = width;
		this.height = height;
		this.pdfWriter = pdfWriter;
	}

	/**
	 * Returns the creating PDFWriter.
	 * 
	 * @return the PDFWriter
	 */
	public PDFWriter getPdfWriter() {
		return this.pdfWriter;
	}

	public double getWidth() {
		return width;
	}

	public double getHeight() {
		return height;
	}

	public abstract void useResource(String type, String name) throws IOException;

	/**
	 * Writes coordinates.
	 * 
	 * @param x x coordinate
	 * @param y y coordinate
	 * @throws IOException in case of I/O error
	 */
	public void writePosition(final double x, final double y) throws IOException {
		this.writeReal(x);
		this.writeReal(this.height - y);
	}

	/**
	 * Writes a Rectangle type.
	 * 
	 * @param x1 x1 coordinate
	 * @param y1 y1 coordinate
	 * @param x2 x2 coordinate
	 * @param y2 y2 coordinate
	 * @throws IOException in case of I/O error
	 */
	public void writeRectangle(final double x1, final double y1, final double x2, final double y2) throws IOException {
		this.startArray();
		this.writePosition(x1, y2);
		this.writePosition(x2, y1);
		this.endArray();
	}

	/**
	 * Writes a rectangle in x, y, width, height format.
	 * <p>
	 * <strong>Unlike Rectangle type, this is not enclosed in an array.</strong>
	 * </p>
	 * 
	 * @param x      x coordinate
	 * @param y      y coordinate
	 * @param width  width
	 * @param height height
	 * @throws IOException in case of I/O error
	 */
	public void writeRect(final double x, final double y, final double width, final double height) throws IOException {
		this.writePosition(x, y + height);
		this.writeReal(width);
		this.writeReal(height);
	}

	/**
	 * Writes an affine transform matrix.
	 * 
	 * @param at the transform
	 * @throws IOException in case of I/O error
	 */
	public void writeTransform(final AffineTransform at) throws IOException {
		final var tpdf = new AffineTransform(1, 0, 0, -1, 0, this.height);
		final var iat = new AffineTransform(at);
		iat.preConcatenate(tpdf);
		iat.concatenate(tpdf);
		this.writeReal(iat.getScaleX());
		this.writeReal(iat.getShearY());
		this.writeReal(iat.getShearX());
		this.writeReal(iat.getScaleY());
		this.writeReal(iat.getTranslateX());
		this.writeReal(iat.getTranslateY());
	}

	/**
	 * Writes fill color.
	 * 
	 * @param color the color
	 * @throws IOException in case of I/O error
	 */
	public void writeFillColor(Color color) throws IOException {
		if (this.getPdfWriter().getParams().getColorMode() == PDFParams.ColorMode.GRAY) {
			if (color.getColorType() != Type.GRAY) {
				color = ColorUtils.toGray(color);
			}
		} else if (this.getPdfWriter().getParams().getColorMode() == PDFParams.ColorMode.CMYK) {
			if (color.getColorType() != Type.CMYK) {
				color = ColorUtils.toCMYK(color);
			}
		}
		switch (color.getColorType()) {
			case GRAY:
				this.writeReal(color.getComponent(0));
				this.writeOperator("g");
				break;
			case RGB:
			case RGBA:
				this.writeReal(color.getComponent(RGBColor.R));
				this.writeReal(color.getComponent(RGBColor.G));
				this.writeReal(color.getComponent(RGBColor.B));
				this.writeOperator("rg");
				break;
			case CMYK:
				final float c = color.getComponent(CMYKColor.C);
				final float m = color.getComponent(CMYKColor.M);
				final float y = color.getComponent(CMYKColor.Y);
				final float k = color.getComponent(CMYKColor.K);
				this.writeReal(c);
				this.writeReal(m);
				this.writeReal(y);
				this.writeReal(k);
				this.writeOperator("k");
				break;
			default:
				throw new IllegalStateException();
		}
	}

	/**
	 * Writes stroke color.
	 * 
	 * @param color the color
	 * @throws IOException in case of I/O error
	 */
	public void writeStrokeColor(Color color) throws IOException {
		if (this.getPdfWriter().getParams().getColorMode() == PDFParams.ColorMode.GRAY) {
			// Gray color mode
			if (color.getColorType() != Type.GRAY) {
				color = ColorUtils.toGray(color);
			}
		} else if (this.getPdfWriter().getParams().getColorMode() == PDFParams.ColorMode.CMYK) {
			// CMYK color mode
			if (color.getColorType() != Type.CMYK) {
				color = ColorUtils.toCMYK(color);
			}
		}
		switch (color.getColorType()) {
			case GRAY:
				this.writeReal(color.getComponent(0));
				this.writeOperator("G");
				break;
			case RGB:
			case RGBA:
				this.writeReal(color.getComponent(RGBColor.R));
				this.writeReal(color.getComponent(RGBColor.G));
				this.writeReal(color.getComponent(RGBColor.B));
				this.writeOperator("RG");
				break;
			case CMYK:
				final float c = color.getComponent(CMYKColor.C);
				final float m = color.getComponent(CMYKColor.M);
				final float y = color.getComponent(CMYKColor.Y);
				final float k = color.getComponent(CMYKColor.K);
				this.writeReal(c);
				this.writeReal(m);
				this.writeReal(y);
				this.writeReal(k);
				this.writeOperator("K");
				break;
			default:
				throw new IllegalStateException();
		}
	}
}
