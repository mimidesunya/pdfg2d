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
 * @version $Id: GC.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public interface GC {
	public static final short LINE_JOIN_MITER = 0;

	public static final short LINE_JOIN_ROUND = 1;

	public static final short LINE_JOIN_BEVEL = 2;

	public static final short LINE_CAP_BUTT = 0;

	public static final short LINE_CAP_ROUND = 1;

	public static final short LINE_CAP_SQUARE = 2;

	public static final double[] STROKE_SOLID = new double[0];

	public static final short TEXT_MODE_FILL = 0;

	public static final short TEXT_MODE_STROKE = 1;

	public static final short TEXT_MODE_FILL_STROKE = 2;

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

	public void setLineJoin(short style) throws GraphicsException;

	public short getLineJoin();

	public void setLineCap(short style) throws GraphicsException;

	public short getLineCap();

	public void setTextMode(short textMode) throws GraphicsException;

	public short getTextMode();

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