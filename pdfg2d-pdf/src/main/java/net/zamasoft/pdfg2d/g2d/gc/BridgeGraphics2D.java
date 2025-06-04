package net.zamasoft.pdfg2d.g2d.gc;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Hashtable;
import java.util.Map;

import net.zamasoft.pdfg2d.g2d.image.RasterImageImpl;
import net.zamasoft.pdfg2d.g2d.util.G2DUtils;
import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.font.FontFamilyList;
import net.zamasoft.pdfg2d.gc.font.FontStyle.Style;
import net.zamasoft.pdfg2d.gc.font.FontStyle.Weight;
import net.zamasoft.pdfg2d.gc.paint.Color;
import net.zamasoft.pdfg2d.gc.paint.Paint;
import net.zamasoft.pdfg2d.gc.text.GlyphHandler;
import net.zamasoft.pdfg2d.gc.text.TextLayoutHandler;
import net.zamasoft.pdfg2d.gc.text.hyphenation.HyphenationBundle;
import net.zamasoft.pdfg2d.gc.text.layout.SimpleLayoutGlyphHandler;
import net.zamasoft.pdfg2d.gc.text.util.TextUtils;

/**
 * SakaeグラフィックコンテキストにJavaグラフィックコンテキストによりアクセスするためのクラスです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class BridgeGraphics2D extends Graphics2D implements Cloneable {
	protected GC gc;

	protected final GraphicsConfiguration config;

	protected static final AffineTransform DEFAULT_TRANSFORM = new AffineTransform();

	protected AffineTransform transform = new AffineTransform();

	protected Shape clip = null;

	protected Composite composite;

	protected java.awt.Paint paint = java.awt.Color.black;

	protected java.awt.Color foreground = java.awt.Color.BLACK;

	protected java.awt.Color background = java.awt.Color.WHITE;

	protected Stroke stroke = new BasicStroke();

	protected RenderingHints hints = new RenderingHints(null);

	protected Font font = new Font("serif", Font.PLAIN, 12);

	public BridgeGraphics2D(GC gc, GraphicsConfiguration config) {
		this.gc = gc;
		this.config = config;
	}

	public BridgeGraphics2D(GC gc) {
		this(gc, null);
	}

	public GC getGC() {
		return this.gc;
	}

	private void restoreState() {
		this.gc.resetState();
		Color gcolor = G2DUtils.fromAwtColor(this.foreground);
		this.gc.setStrokePaint(gcolor);
		this.gc.setFillPaint(gcolor);
		if (this.stroke instanceof BasicStroke) {
			BasicStroke bs = (BasicStroke) this.stroke;
			this.gc.setLineWidth(bs.getLineWidth());
			float[] da = bs.getDashArray();
			if (da != null) {
				double[] dda = new double[da.length];
				for (int i = 0; i < da.length; ++i) {
					dda[i] = da[i];
				}
				this.gc.setLinePattern(dda);
			} else {
				this.gc.setLinePattern(null);
			}
			this.gc.setLineCap(G2DUtils.decodeLineCap((short) bs.getEndCap()));
			this.gc.setLineJoin(G2DUtils.decodeLineJoin((short) bs.getLineJoin()));
		} else {
			throw new IllegalStateException();
		}
		if (this.transform != null) {
			this.gc.transform(this.transform);
		}
		if (this.clip != null) {
			Shape s = this.clip;
			if (this.transform != null) {
				try {
					s = this.transform.createInverse().createTransformedShape(s);
				} catch (NoninvertibleTransformException e) {
					s = null;
				}
			}
			this.gc.clip(s);
		}
	}

	public GraphicsConfiguration getDeviceConfiguration() {
		return this.config;
	}

	public void setRenderingHint(Key key, Object value) {
		this.hints.put(key, value);
	}

	public Object getRenderingHint(Key key) {
		return this.hints.get(key);
	}

	@SuppressWarnings("unchecked")
	public void setRenderingHints(Map<?, ?> hints) {
		this.hints = new RenderingHints((Map<RenderingHints.Key, ?>) hints);
	}

	public void addRenderingHints(Map<?, ?> hints) {
		this.hints.putAll(hints);
	}

	public RenderingHints getRenderingHints() {
		return this.hints;
	}

	public void transform(AffineTransform at) {
		this.transform.concatenate(at);
		this.gc.transform(at);
	}

	public void setTransform(AffineTransform at) {
		this.transform = new AffineTransform(at);
		this.restoreState();
	}

	public AffineTransform getTransform() {
		return new AffineTransform(this.transform);
	}

	public void setPaint(java.awt.Paint paint) {
		if (paint == null) {
			return;
		}
		this.paint = paint;
		if (paint instanceof java.awt.Color) {
			this.foreground = (java.awt.Color) paint;
		}
		Paint spaint = G2DUtils.fromAwtPaint(paint);
		if (spaint != null) {
			this.gc.setStrokePaint(spaint);
			this.gc.setFillPaint(spaint);
		}
	}

	public java.awt.Paint getPaint() {
		return this.paint;
	}

	public void setComposite(Composite composite) {
		this.composite = composite;
		if (composite instanceof AlphaComposite) {
			float alpha = ((AlphaComposite) composite).getAlpha();
			this.gc.setFillAlpha(alpha);
			this.gc.setStrokeAlpha(alpha);
		}
	}

	public Composite getComposite() {
		return this.composite;
	}

	public void setBackground(java.awt.Color background) {
		this.background = background;
	}

	public java.awt.Color getBackground() {
		return this.background;
	}

	public void setStroke(Stroke stroke) {
		this.stroke = stroke;
		if (stroke instanceof BasicStroke) {
			BasicStroke bs = (BasicStroke) stroke;
			this.gc.setLineWidth(bs.getLineWidth());
			float[] da = bs.getDashArray();
			if (da != null) {
				double[] dda = new double[da.length];
				for (int i = 0; i < da.length; ++i) {
					dda[i] = da[i];
				}
				this.gc.setLinePattern(dda);
			} else {
				this.gc.setLinePattern(null);
			}
			this.gc.setLineCap(G2DUtils.decodeLineCap((short) bs.getEndCap()));
			this.gc.setLineJoin(G2DUtils.decodeLineJoin((short) bs.getLineJoin()));
		}
	}

	public Stroke getStroke() {
		return this.stroke;
	}

	public java.awt.Color getColor() {
		return this.foreground;
	}

	public void setColor(java.awt.Color color) {
		this.setPaint(color);
	}

	public void clip(Shape clip) {
		Shape s = clip;
		if (s != null) {
			s = this.transform.createTransformedShape(s);
		}
		if (this.clip != null) {
			Area newClip = new Area(this.clip);
			newClip.intersect(new Area(s));
			this.clip = new GeneralPath(newClip);
		} else {
			this.clip = s;
		}

		this.gc.clip(clip);
	}

	public void clipRect(int x, int y, int width, int height) {
		this.clip(new Rectangle(x, y, width, height));
	}

	public void setClip(Shape clip) {
		if (clip != null) {
			this.clip = this.transform.createTransformedShape(clip);
		} else {
			this.clip = null;
		}
		this.restoreState();
	}

	public void setClip(int x, int y, int width, int height) {
		this.setClip(new Rectangle(x, y, width, height));
	}

	public Shape getClip() {
		try {
			return transform.createInverse().createTransformedShape(this.clip);
		} catch (NoninvertibleTransformException e) {
			return null;
		}
	}

	public void draw(Shape shape) {
		Stroke stroke = this.getStroke();
		if (stroke instanceof BasicStroke) {
			this.gc.draw(shape);
		} else {
			this.gc.fill(stroke.createStrokedShape(shape));
		}
	}

	public void fill(Shape shape) {
		this.gc.fill(shape);
	}

	public FontRenderContext getFontRenderContext() {
		Object antialiasingHint = hints.get(RenderingHints.KEY_TEXT_ANTIALIASING);
		boolean isAntialiased = true;
		if (antialiasingHint != RenderingHints.VALUE_TEXT_ANTIALIAS_ON
				&& antialiasingHint != RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT) {
			if (antialiasingHint != RenderingHints.VALUE_TEXT_ANTIALIAS_OFF) {
				antialiasingHint = hints.get(RenderingHints.KEY_ANTIALIASING);
				if (antialiasingHint != RenderingHints.VALUE_ANTIALIAS_ON
						&& antialiasingHint != RenderingHints.VALUE_ANTIALIAS_DEFAULT) {
					if (antialiasingHint == RenderingHints.VALUE_ANTIALIAS_OFF) {
						isAntialiased = false;
					}
				}
			} else {
				isAntialiased = false;
			}

		}

		boolean useFractionalMetrics = true;
		if (hints.get(RenderingHints.KEY_FRACTIONALMETRICS) == RenderingHints.VALUE_FRACTIONALMETRICS_OFF) {
			useFractionalMetrics = false;
		}

		FontRenderContext frc = new FontRenderContext(DEFAULT_TRANSFORM, isAntialiased, useFractionalMetrics);
		return frc;
	}

	public Font getFont() {
		return this.font;
	}

	public void setFont(Font font) {
		this.font = font;

	}

	class MyFontMetrics extends FontMetrics {
		private static final long serialVersionUID = 1L;

		MyFontMetrics(Font font) {
			super(font);
		}

		public int stringWidth(String text) {
			SimpleLayoutGlyphHandler lgh = new SimpleLayoutGlyphHandler();
			drawString(lgh, new AttributedString(text, this.getFont().getAttributes()).getIterator());
			return (int) lgh.getAdvance();
		}
	}

	public FontMetrics getFontMetrics(Font font) {
		return new MyFontMetrics(font);
	}

	public void drawString(String text, int x, int y) {
		this.drawString(text, (float) x, (float) y);
	}

	public void drawString(String text, float x, float y) {
		this.drawString(new AttributedString(text, this.getFont().getAttributes()).getIterator(), x, y);
	}

	public void drawString(AttributedCharacterIterator aci, int x, int y) {
		this.drawString(aci, (float) x, (float) y);
	}

	private void drawString(GlyphHandler gh, AttributedCharacterIterator aci) {
		try (TextLayoutHandler tlf = new TextLayoutHandler(this.gc, HyphenationBundle.getHyphenation("ja"), gh)) {
			Map<TextAttribute, ?> atts = this.font.getAttributes();
			tlf.setFontFamilies(FontFamilyList.create(this.font.getFamily()));
			int style = this.font.getStyle();
			tlf.setFontWeight(TextUtils.toFontWeight((Float) atts.get(TextAttribute.WEIGHT),
					(style & Font.BOLD) != 0 ? Weight.W_600 : Weight.W_400));
			tlf.setFontSize(this.font.getSize2D());
			tlf.setFontStyle(TextUtils.toFontStyle((Float) atts.get(TextAttribute.POSTURE),
					(style & Font.ITALIC) != 0 ? Style.ITALIC : Style.NORMAL));
			tlf.characters(aci);
		}
	}

	public void drawString(AttributedCharacterIterator aci, float x, float y) {
		this.gc.begin();
		this.gc.transform(AffineTransform.getTranslateInstance(x, y));

		SimpleLayoutGlyphHandler lgh = new SimpleLayoutGlyphHandler();
		lgh.setGC(this.gc);
		this.drawString(lgh, aci);

		this.gc.end();
	}

	public void drawGlyphVector(GlyphVector gv, float x, float y) {
		Shape glyphOutline = gv.getOutline(x, y);
		this.gc.fill(glyphOutline);
	}

	protected void drawBufferedImage(BufferedImage image, AffineTransform at) {
		this.gc.begin();
		this.gc.transform(at);
		this.gc.drawImage(new RasterImageImpl(image));
		this.gc.end();
	}

	@SuppressWarnings("rawtypes")
	public void drawRenderedImage(RenderedImage image, AffineTransform at) {
		BufferedImage bimage;
		if (image instanceof BufferedImage) {
			bimage = (BufferedImage) image;
		} else {
			Hashtable<?, ?> props = new Hashtable();
			bimage = new BufferedImage(image.getColorModel(), image.copyData(null), true, props);
		}
		this.drawBufferedImage(bimage, at);
	}

	public void drawRenderableImage(RenderableImage image, AffineTransform at) {
		this.drawRenderedImage(image.createDefaultRendering(), at);
	}

	public void drawImage(BufferedImage image, BufferedImageOp op, int x, int y) {
		AffineTransform at = AffineTransform.getTranslateInstance(x, y);
		if (op != null) {
			image = op.createCompatibleDestImage(image, image.getColorModel());
		}
		this.drawRenderedImage(image, at);
	}

	public boolean drawImage(Image image, AffineTransform at, ImageObserver obs) {
		if (image instanceof RenderedImage) {
			this.drawRenderedImage((RenderedImage) image, at);
		} else if (image instanceof RenderableImage) {
			this.drawRenderableImage((RenderableImage) image, at);
		} else {
			BufferedImage bimage = new BufferedImage(image.getWidth(obs), image.getHeight(obs),
					BufferedImage.TYPE_INT_ARGB);
			bimage.getGraphics().drawImage(image, 0, 0, null);
			this.drawBufferedImage(bimage, at);
		}
		return true;
	}

	public boolean drawImage(Image image, int x, int y, ImageObserver obs) {
		return this.drawImage(image, x, y, image.getWidth(null), image.getHeight(null), obs);
	}

	public boolean drawImage(Image image, int x, int y, int width, int height, ImageObserver obs) {
		AffineTransform at = new AffineTransform((double) width / (double) image.getWidth(null), 0, 0,
				(double) height / (double) image.getHeight(null), x, y);
		return this.drawImage(image, at, obs);
	}

	public boolean drawImage(Image image, int x, int y, java.awt.Color color, ImageObserver obs) {
		return this.drawImage(image, x, y, image.getWidth(null), image.getHeight(null), obs);
	}

	public boolean drawImage(Image image, int x, int y, int width, int height, java.awt.Color color,
			ImageObserver obs) {
		java.awt.Paint paint = this.getPaint();
		this.setPaint(color);
		this.fillRect(x, y, width, height);
		this.setPaint(paint);
		return this.drawImage(image, x, y, width, height, obs);
	}

	public boolean drawImage(Image image, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
			ImageObserver obs) {
		int w = dx2 - dx1, h = dy2 - dy1;
		BufferedImage bimage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		bimage.getGraphics().drawImage(image, 0, 0, w, h, sx1, sy1, sx2, sy2, null);
		return this.drawImage(bimage, dx1, dy1, obs);
	}

	public boolean drawImage(Image image, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
			java.awt.Color color, ImageObserver obs) {
		if (color != null) {
			java.awt.Paint paint = this.getPaint();
			this.setPaint(color);
			this.fillRect(dx1, dy1, dx2 - dx1, dy2 - dy1);
			this.setPaint(paint);
		}
		return this.drawImage(image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, obs);
	}

	public Graphics create() {
		try {
			BridgeGraphics2D g = (BridgeGraphics2D) this.clone();
			return g;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public void setPaintMode() {
		this.setComposite(null);
	}

	public void setXORMode(java.awt.Color color) {
		this.setComposite(AlphaComposite.Xor);
	}

	public Rectangle getClipBounds() {
		Shape clip = this.getClip();
		if (clip == null) {
			return null;
		} else {
			return clip.getBounds();
		}
	}

	public boolean hit(Rectangle rect, Shape shape, boolean onStroke) {
		// TODO
		throw new UnsupportedOperationException();
	}

	public void translate(int x, int y) {
		this.translate((double) x, (double) y);
	}

	public void translate(double x, double y) {
		this.transform(AffineTransform.getTranslateInstance(x, y));
	}

	public void rotate(double theta) {
		this.transform(AffineTransform.getRotateInstance(theta));
	}

	public void rotate(double theta, double x, double y) {
		this.transform(AffineTransform.getRotateInstance(theta, x, y));
	}

	public void scale(double sx, double sy) {
		this.transform(AffineTransform.getScaleInstance(sx, sy));
	}

	public void shear(double shx, double shy) {
		this.transform(AffineTransform.getShearInstance(shx, shy));
	}

	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		throw new UnsupportedOperationException();
	}

	public void drawLine(int x1, int y1, int x2, int y2) {
		this.gc.fill(new Line2D.Float(x1, y1, x2, y2));
	}

	public void drawRect(int x, int y, int width, int height) {
		this.draw(new Rectangle(x, y, width, height));
	}

	public void fillRect(int x, int y, int width, int height) {
		this.gc.fill(new Rectangle(x, y, width, height));
	}

	public void clearRect(int x, int y, int width, int height) {
		this.gc.setFillPaint(G2DUtils.fromAwtColor(this.getBackground()));
		this.gc.fill(new Rectangle(x, y, width, height));
		this.gc.setStrokePaint(G2DUtils.fromAwtColor(this.getColor()));

	}

	public void drawRoundRect(int x, int y, int width, int height, int rx, int ry) {
		this.draw(new RoundRectangle2D.Float(x, y, width, height, rx, ry));
	}

	public void fillRoundRect(int x, int y, int width, int height, int rx, int ry) {
		this.gc.fill(new RoundRectangle2D.Float(x, y, width, height, rx, ry));
	}

	public void drawOval(int x, int y, int width, int height) {
		this.draw(new Ellipse2D.Float(x, y, width, height));
	}

	public void fillOval(int x, int y, int width, int height) {
		this.gc.fill(new Ellipse2D.Float(x, y, width, height));
	}

	public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
		this.draw(new Arc2D.Float(x, y, width, height, startAngle, arcAngle, Arc2D.CHORD));
	}

	public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
		this.gc.fill(new Arc2D.Float(x, y, width, height, startAngle, arcAngle, Arc2D.PIE));
	}

	public void drawPolyline(int[] xpoints, int[] ypoints, int npoints) {
		if (npoints <= 0) {
			return;
		}
		GeneralPath path = new GeneralPath();
		path.moveTo(xpoints[0], ypoints[0]);
		for (int i = 0; i < npoints; ++i) {
			path.lineTo(xpoints[i], ypoints[i]);
		}
		this.draw(path);
	}

	public void drawPolygon(int[] xpoints, int[] ypoints, int npoints) {
		this.draw(new Polygon(xpoints, ypoints, npoints));
	}

	public void fillPolygon(int[] xpoints, int[] ypoints, int npoints) {
		this.gc.fill(new Polygon(xpoints, ypoints, npoints));
	}

	public void dispose() {
		if (this.gc != null) {
			this.gc = null;
		}
	}

	protected Object clone() throws CloneNotSupportedException {
		BridgeGraphics2D clone = (BridgeGraphics2D) super.clone();
		clone.transform = new AffineTransform(clone.transform);
		return clone;
	}
}
