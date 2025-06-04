package net.zamasoft.pdfg2d.gc;

import java.awt.Shape;
import java.awt.geom.AffineTransform;

import net.zamasoft.pdfg2d.gc.font.FontManager;
import net.zamasoft.pdfg2d.gc.image.GroupImageGC;
import net.zamasoft.pdfg2d.gc.image.Image;
import net.zamasoft.pdfg2d.gc.text.Text;

/**
 * グラフィックコンテキストです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface GC {
	public static enum LineJoin {
		MITER((short)0), ROUND((short)1), BEVEL((short)2);
		public final short j;
		LineJoin(final short j) {
			this.j = j;
		}
	}
	public static enum LineCap {
		BUTT((short)0), ROUND((short)1), SQUARE((short)2);
		public final short code;
		LineCap(final short c) {
			this.code = c;
		}
	}

	public static final double[] STROKE_SOLID = new double[0];
	public static enum TextMode {
		FILL((short)0), STROKE((short)1), FILL_STROKE((short)2);
		public final short code;
		TextMode(final short t) {
			this.code = t;
		}
	}
	public FontManager getFontManager();

	public void begin() throws GraphicsException;

	public void resetState() throws GraphicsException;

	public void end() throws GraphicsException;

	// FIXME 互換性のためPaintのところObjectに
	public void setStrokePaint(Object paint) throws GraphicsException;

	public Object getStrokePaint();

	// FIXME 互換性のためPaintのところObjectに
	public void setFillPaint(Object paint) throws GraphicsException;

	public Object getFillPaint();

	public float getStrokeAlpha();

	public void setStrokeAlpha(float strokeAlpha);

	public float getFillAlpha();

	public void setFillAlpha(float fillAlpha);

	public void setLineWidth(double width) throws GraphicsException;

	public double getLineWidth();

	public void setLinePattern(double[] pattern) throws GraphicsException;

	public double[] getLinePattern();

	public void setLineJoin(LineJoin style) throws GraphicsException;

	public LineJoin getLineJoin();

	public void setLineCap(LineCap style) throws GraphicsException;

	public LineCap getLineCap();

	public void setTextMode(TextMode textMode) throws GraphicsException;

	public TextMode getTextMode();

	public void transform(AffineTransform at) throws GraphicsException;

	public AffineTransform getTransform();

	public void clip(Shape shape) throws GraphicsException;

	public void draw(Shape shape) throws GraphicsException;

	public void fill(Shape shape) throws GraphicsException;

	public void fillDraw(Shape shape) throws GraphicsException;

	public void drawImage(Image image) throws GraphicsException;

	public void drawText(Text text, double x, double y) throws GraphicsException;

	public GroupImageGC createGroupImage(double width, double height) throws GraphicsException;
}