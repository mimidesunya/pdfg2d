package net.zamasoft.pdfg2d.pdf;

import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.OutputStream;

import net.zamasoft.pdfg2d.gc.paint.CMYKColor;
import net.zamasoft.pdfg2d.gc.paint.Color;
import net.zamasoft.pdfg2d.gc.paint.Color.Type;
import net.zamasoft.pdfg2d.gc.paint.RGBColor;
import net.zamasoft.pdfg2d.util.ColorUtils;

/**
 * Base output class for PDF graphics operations like path drawing, color
 * setting,
 * and transformations.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public abstract class PDFGraphicsOutput extends PDFOutput {
	protected final double width, height;

	protected final PDFWriter pdfWriter;

	public PDFGraphicsOutput(final PDFWriter pdfWriter, final OutputStream out, final double width,
			final double height) throws IOException {
		super(out, pdfWriter.getParams().getPlatformEncoding());
		final var params = pdfWriter.getParams();
		this.setPrecision(params.getPrecision());
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
	 * Writes coordinates relative to the bottom-left origin of PDF.
	 * 
	 * @param x X coordinate (top-left origin)
	 * @param y Y coordinate (top-left origin)
	 * @throws IOException If an I/O error occurs
	 */
	public void writePosition(final double x, final double y) throws IOException {
		this.writeReal(x);
		this.writeReal(this.height - y);
	}

	/**
	 * Writes a rectangle specified by two points.
	 * 
	 * @param x1 X1 coordinate
	 * @param y1 Y1 coordinate
	 * @param x2 X2 coordinate
	 * @param y2 Y2 coordinate
	 * @throws IOException If an I/O error occurs
	 */
	public void writeRectangle(final double x1, final double y1, final double x2, final double y2) throws IOException {
		this.startArray();
		this.writePosition(x1, y2);
		this.writePosition(x2, y1);
		this.endArray();
	}

	/**
	 * Writes a rectangle in 're' (rectangle) operator format: x, y, width, height.
	 * Note: PDF 're' operator uses bottom-left origin.
	 * 
	 * @param x      X coordinate (top-left origin)
	 * @param y      Y coordinate (top-left origin)
	 * @param width  Rectangle width
	 * @param height Rectangle height
	 * @throws IOException If an I/O error occurs
	 */
	public void writeRect(final double x, final double y, final double width, final double height) throws IOException {
		this.writePosition(x, y + height);
		this.writeReal(width);
		this.writeReal(height);
	}

	/**
	 * Writes an affine transformation matrix in PDF 'cm' operator format.
	 * 
	 * @param at The affine transform to write
	 * @throws IOException If an I/O error occurs
	 */
	public void writeTransform(final AffineTransform at) throws IOException {
		// Convert top-left origin to bottom-left origin
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
	 * Writes the fill color (non-stroking color).
	 * 
	 * @param color The color to set
	 * @throws IOException If an I/O error occurs
	 */
	public void writeFillColor(final Color color) throws IOException {
		final var params = this.pdfWriter.getParams();
		final var processedColor = switch (params.getColorMode()) {
			case GRAY -> (color.getColorType() != Type.GRAY) ? ColorUtils.toGray(color) : color;
			case CMYK -> (color.getColorType() != Type.CMYK) ? ColorUtils.toCMYK(color) : color;
			default -> color;
		};

		switch (processedColor.getColorType()) {
			case GRAY -> {
				this.writeReal(processedColor.getComponent(0));
				this.writeOperator("g");
			}
			case RGB, RGBA -> {
				this.writeReal(processedColor.getComponent(RGBColor.R));
				this.writeReal(processedColor.getComponent(RGBColor.G));
				this.writeReal(processedColor.getComponent(RGBColor.B));
				this.writeOperator("rg");
			}
			case CMYK -> {
				this.writeReal(processedColor.getComponent(CMYKColor.C));
				this.writeReal(processedColor.getComponent(CMYKColor.M));
				this.writeReal(processedColor.getComponent(CMYKColor.Y));
				this.writeReal(processedColor.getComponent(CMYKColor.K));
				this.writeOperator("k");
			}
			default -> throw new IllegalStateException("Unsupported color type: " + processedColor.getColorType());
		}
	}

	/**
	 * Writes the stroke color.
	 * 
	 * @param color The color to set
	 * @throws IOException If an I/O error occurs
	 */
	public void writeStrokeColor(final Color color) throws IOException {
		final var params = this.pdfWriter.getParams();
		final var processedColor = switch (params.getColorMode()) {
			case GRAY -> (color.getColorType() != Type.GRAY) ? ColorUtils.toGray(color) : color;
			case CMYK -> (color.getColorType() != Type.CMYK) ? ColorUtils.toCMYK(color) : color;
			default -> color;
		};

		switch (processedColor.getColorType()) {
			case GRAY -> {
				this.writeReal(processedColor.getComponent(0));
				this.writeOperator("G");
			}
			case RGB, RGBA -> {
				this.writeReal(processedColor.getComponent(RGBColor.R));
				this.writeReal(processedColor.getComponent(RGBColor.G));
				this.writeReal(processedColor.getComponent(RGBColor.B));
				this.writeOperator("RG");
			}
			case CMYK -> {
				this.writeReal(processedColor.getComponent(CMYKColor.C));
				this.writeReal(processedColor.getComponent(CMYKColor.M));
				this.writeReal(processedColor.getComponent(CMYKColor.Y));
				this.writeReal(processedColor.getComponent(CMYKColor.K));
				this.writeOperator("K");
			}
			default -> throw new IllegalStateException("Unsupported color type: " + processedColor.getColorType());
		}
	}
}
