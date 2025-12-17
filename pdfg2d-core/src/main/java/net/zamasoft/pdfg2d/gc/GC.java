package net.zamasoft.pdfg2d.gc;

import java.awt.Shape;
import java.awt.geom.AffineTransform;

import net.zamasoft.pdfg2d.gc.font.FontManager;
import net.zamasoft.pdfg2d.gc.image.GroupImageGC;
import net.zamasoft.pdfg2d.gc.image.Image;
import net.zamasoft.pdfg2d.gc.paint.Paint;
import net.zamasoft.pdfg2d.gc.text.Text;

/**
 * Represents a graphics context.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface GC {
	/**
	 * Represents the line join style.
	 */
	public enum LineJoin {
		MITER((short) 0), ROUND((short) 1), BEVEL((short) 2);

		public final short j;

		LineJoin(final short j) {
			this.j = j;
		}
	}

	/**
	 * Represents the line cap style.
	 */
	public enum LineCap {
		BUTT((short) 0), ROUND((short) 1), SQUARE((short) 2);

		public final short code;

		LineCap(final short c) {
			this.code = c;
		}
	}

	public static final double[] STROKE_SOLID = new double[0];

	/**
	 * Represents the text rendering mode.
	 */
	public enum TextMode {
		FILL((short) 0), STROKE((short) 1), FILL_STROKE((short) 2);

		public final short code;

		TextMode(final short t) {
			this.code = t;
		}
	}

	/**
	 * Returns the font manager.
	 * 
	 * @return the font manager
	 */
	public FontManager getFontManager();

	/**
	 * Begins a new graphics state.
	 * 
	 * @throws GraphicsException if a graphics error occurs
	 */
	public void begin() throws GraphicsException;

	/**
	 * Resets the current graphics state to the initial state.
	 * 
	 * @throws GraphicsException if a graphics error occurs
	 */
	public void resetState() throws GraphicsException;

	/**
	 * Ends the current graphics state.
	 * 
	 * @throws GraphicsException if a graphics error occurs
	 */
	public void end() throws GraphicsException;

	/**
	 * Sets the stroke paint.
	 * 
	 * @param paint the paint object
	 * @throws GraphicsException if a graphics error occurs
	 */
	public void setStrokePaint(final Paint paint) throws GraphicsException;

	/**
	 * Returns the stroke paint.
	 * 
	 * @return the stroke paint object
	 */
	public Paint getStrokePaint();

	/**
	 * Sets the fill paint.
	 * 
	 * @param paint the paint object
	 * @throws GraphicsException if a graphics error occurs
	 */
	public void setFillPaint(final Paint paint) throws GraphicsException;

	/**
	 * Returns the fill paint.
	 * 
	 * @return the fill paint object
	 */
	public Paint getFillPaint();

	/**
	 * Returns the stroke alpha.
	 * 
	 * @return the stroke alpha
	 */
	public float getStrokeAlpha();

	/**
	 * Sets the stroke alpha.
	 * 
	 * @param strokeAlpha the stroke alpha
	 */
	public void setStrokeAlpha(final float strokeAlpha);

	/**
	 * Returns the fill alpha.
	 * 
	 * @return the fill alpha
	 */
	public float getFillAlpha();

	/**
	 * Sets the fill alpha.
	 * 
	 * @param fillAlpha the fill alpha
	 */
	public void setFillAlpha(final float fillAlpha);

	/**
	 * Sets the line width.
	 * 
	 * @param width the line width
	 * @throws GraphicsException if a graphics error occurs
	 */
	public void setLineWidth(final double width) throws GraphicsException;

	/**
	 * Returns the line width.
	 * 
	 * @return the line width
	 */
	public double getLineWidth();

	/**
	 * Sets the line pattern.
	 * 
	 * @param pattern the line pattern
	 * @throws GraphicsException if a graphics error occurs
	 */
	public void setLinePattern(final double[] pattern) throws GraphicsException;

	/**
	 * Returns the line pattern.
	 * 
	 * @return the line pattern
	 */
	public double[] getLinePattern();

	/**
	 * Sets the line join style.
	 * 
	 * @param style the line join style
	 * @throws GraphicsException if a graphics error occurs
	 */
	public void setLineJoin(final LineJoin style) throws GraphicsException;

	/**
	 * Returns the line join style.
	 * 
	 * @return the line join style
	 */
	public LineJoin getLineJoin();

	/**
	 * Sets the line cap style.
	 * 
	 * @param style the line cap style
	 * @throws GraphicsException if a graphics error occurs
	 */
	public void setLineCap(final LineCap style) throws GraphicsException;

	/**
	 * Returns the line cap style.
	 * 
	 * @return the line cap style
	 */
	public LineCap getLineCap();

	/**
	 * Sets the text rendering mode.
	 * 
	 * @param textMode the text rendering mode
	 * @throws GraphicsException if a graphics error occurs
	 */
	public void setTextMode(final TextMode textMode) throws GraphicsException;

	/**
	 * Returns the text rendering mode.
	 * 
	 * @return the text rendering mode
	 */
	public TextMode getTextMode();

	/**
	 * Concatenates the current transform with the given transform.
	 * 
	 * @param at the transform to concatenate
	 * @throws GraphicsException if a graphics error occurs
	 */
	public void transform(final AffineTransform at) throws GraphicsException;

	/**
	 * Returns the current transform.
	 * 
	 * @return the current transform
	 */
	public AffineTransform getTransform();

	/**
	 * Intersects the current clip with the given shape.
	 * 
	 * @param shape the clip shape
	 * @throws GraphicsException if a graphics error occurs
	 */
	public void clip(final Shape shape) throws GraphicsException;

	/**
	 * Draws the outline of the given shape.
	 * 
	 * @param shape the shape to draw
	 * @throws GraphicsException if a graphics error occurs
	 */
	public void draw(final Shape shape) throws GraphicsException;

	/**
	 * Fills the interior of the given shape.
	 * 
	 * @param shape the shape to fill
	 * @throws GraphicsException if a graphics error occurs
	 */
	public void fill(final Shape shape) throws GraphicsException;

	/**
	 * Fills and then draws the outline of the given shape.
	 * 
	 * @param shape the shape to fill and draw
	 * @throws GraphicsException if a graphics error occurs
	 */
	public void fillDraw(final Shape shape) throws GraphicsException;

	/**
	 * Draws an image.
	 * 
	 * @param image the image to draw
	 * @throws GraphicsException if a graphics error occurs
	 */
	public void drawImage(final Image image) throws GraphicsException;

	/**
	 * Draws text at the specified location.
	 * 
	 * @param text the text to draw
	 * @param x    the x-coordinate
	 * @param y    the y-coordinate
	 * @throws GraphicsException if a graphics error occurs
	 */
	public void drawText(final Text text, final double x, final double y) throws GraphicsException;

	/**
	 * Creates a new group image graphics context.
	 * 
	 * @param width  the width of the group image
	 * @param height the height of the group image
	 * @return the group image graphics context
	 * @throws GraphicsException if a graphics error occurs
	 */
	public GroupImageGC createGroupImage(final double width, final double height) throws GraphicsException;
}