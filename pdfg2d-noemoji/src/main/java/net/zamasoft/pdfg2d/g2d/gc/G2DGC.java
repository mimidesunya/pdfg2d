package net.zamasoft.pdfg2d.g2d.gc;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import net.zamasoft.pdfg2d.font.Font;
import net.zamasoft.pdfg2d.font.FontMetricsImpl;
import net.zamasoft.pdfg2d.g2d.image.RasterImageImpl;
import net.zamasoft.pdfg2d.g2d.util.G2DUtils;
import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.GraphicsException;
import net.zamasoft.pdfg2d.gc.font.FontManager;
import net.zamasoft.pdfg2d.gc.image.GroupImageGC;
import net.zamasoft.pdfg2d.gc.image.Image;
import net.zamasoft.pdfg2d.gc.image.util.TransformedImage;
import net.zamasoft.pdfg2d.gc.paint.Color;
import net.zamasoft.pdfg2d.gc.paint.LinearGradient;
import net.zamasoft.pdfg2d.gc.paint.Paint;
import net.zamasoft.pdfg2d.gc.paint.Pattern;
import net.zamasoft.pdfg2d.gc.paint.RadialGradient;
import net.zamasoft.pdfg2d.gc.text.Text;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */

public class G2DGC implements GC {
	protected static class GraphicsState {
		public final Shape clip;

		public final AffineTransform transform;

		public final Stroke stroke;

		public final Object strokePaintObject;

		public final Object fillPaintObject;

		public final java.awt.Color strokeColor;

		public final java.awt.Paint fillPaint;

		public final AffineTransform fillAt, strokeAt;

		public final float fillAlpha;

		public final short textMode;

		public final Composite composite;

		public GraphicsState(G2DGC gc) {
			Graphics2D g = gc.g;
			this.transform = g.getTransform();
			this.clip = g.getClip();
			this.strokePaintObject = gc.strokePaintObject;
			this.fillPaintObject = gc.fillPaintObject;
			this.stroke = g.getStroke();
			this.strokeColor = g.getColor();
			this.composite = g.getComposite();
			this.fillPaint = gc.fillPaint;
			this.fillAt = gc.fillAt;
			this.fillAlpha = gc.fillAlpha;
			this.strokeAt = gc.strokeAt;
			this.textMode = gc.textMode;
		}

		public void restore(G2DGC gc) {
			Graphics2D g = gc.g;
			g.setTransform(this.transform);
			g.setClip(this.clip);
			g.setStroke(this.stroke);
			g.setColor(this.strokeColor);
			g.setComposite(this.composite);
			gc.fillPaintObject = this.fillPaintObject;
			gc.strokePaintObject = this.strokePaintObject;
			gc.fillPaint = this.fillPaint;
			gc.fillAt = this.fillAt;
			gc.fillAlpha = this.fillAlpha;
			gc.strokeAt = this.strokeAt;
			gc.textMode = this.textMode;
		}
	}

	protected final Graphics2D g;

	protected boolean drewAnything = false;

	protected Object strokePaintObject;

	protected Object fillPaintObject;

	protected java.awt.Paint fillPaint;

	protected AffineTransform fillAt, strokeAt;

	protected float fillAlpha = 1;

	protected short textMode = TEXT_MODE_FILL;

	protected ArrayList<GraphicsState> stack = new ArrayList<GraphicsState>();

	protected final FontManager fm;

	public G2DGC(Graphics2D g, FontManager fm) {
		this.g = g;
		this.g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
		this.fillPaint = this.g.getPaint();
		this.fm = fm;
	}

	public FontManager getFontManager() {
		return this.fm;
	}

	public Graphics2D getGraphics2D() {
		return this.g;
	}

	public boolean drewAnything() {
		return this.drewAnything;
	}

	public void begin() {
		if (this.stack.isEmpty()) {
			this.drewAnything = false;
		}
		this.stack.add(new GraphicsState(this));
	}

	public void end() {
		GraphicsState state = (GraphicsState) this.stack.remove(this.stack.size() - 1);
		state.restore(this);
	}

	public void setLineWidth(double width) {
		BasicStroke stroke = (BasicStroke) this.g.getStroke();
		float fwidth = (float) width;
		this.g.setStroke(new BasicStroke(fwidth, stroke.getEndCap(), stroke.getLineJoin(), stroke.getMiterLimit(),
				stroke.getDashArray(), stroke.getDashPhase()));
	}

	public double getLineWidth() {
		return ((BasicStroke) this.g.getStroke()).getLineWidth();
	}

	public void setLinePattern(double[] pattern) {
		BasicStroke stroke = (BasicStroke) this.g.getStroke();
		float[] fpattern;
		if (pattern != null && pattern.length > 0) {
			fpattern = new float[pattern.length];
			for (int i = 0; i < pattern.length; ++i) {
				fpattern[i] = (float) pattern[i];
			}
		} else {
			fpattern = null;
		}
		this.g.setStroke(new BasicStroke(stroke.getLineWidth(), stroke.getEndCap(), stroke.getLineJoin(),
				stroke.getMiterLimit(), fpattern, stroke.getDashPhase()));
	}

	public double[] getLinePattern() {
		float[] da = ((BasicStroke) this.g.getStroke()).getDashArray();
		double[] pattern = new double[da.length];
		for (int i = 0; i < da.length; ++i) {
			pattern[i] = da[i];
		}
		return pattern;
	}

	public void setLineJoin(short style) {
		BasicStroke stroke = (BasicStroke) this.g.getStroke();
		this.g.setStroke(new BasicStroke(stroke.getLineWidth(), stroke.getEndCap(), style, stroke.getMiterLimit(),
				stroke.getDashArray(), stroke.getDashPhase()));
	}

	public short getLineJoin() {
		return (short) ((BasicStroke) this.g.getStroke()).getLineJoin();
	}

	public void setLineCap(short style) {
		BasicStroke stroke = (BasicStroke) this.g.getStroke();
		this.g.setStroke(new BasicStroke(stroke.getLineWidth(), style, stroke.getLineJoin(), stroke.getMiterLimit(),
				stroke.getDashArray(), stroke.getDashPhase()));
	}

	public short getLineCap() {
		return (short) ((BasicStroke) this.g.getStroke()).getEndCap();
	}

	protected void setPaint(Object _paint, boolean fill) throws GraphicsException {
		java.awt.Paint awtPaint;
		AffineTransform at;
		Paint paint = (net.zamasoft.pdfg2d.gc.paint.Paint) _paint;

		switch (paint.getPaintType()) {
		case Paint.COLOR:
			awtPaint = G2DUtils.toAwtColor((Color) paint);
			at = null;
			break;

		case Paint.PATTERN:
			Pattern pattern = (Pattern) paint;
			awtPaint = G2DUtils.toAwtPaint(pattern, this);
			at = pattern.getTransform();
			break;

		case Paint.LINEAR_GRADIENT:
			awtPaint = G2DUtils.toAwtPaint((LinearGradient) paint);
			at = null;
			break;
		case Paint.RADIAL_GRADIENT:
			awtPaint = G2DUtils.toAwtPaint((RadialGradient) paint);
			at = null;
			break;
		default:
			throw new IllegalStateException();
		}
		if (fill) {
			this.fillPaintObject = _paint;
			this.fillPaint = awtPaint;
			this.fillAt = at;
		} else {
			this.g.setPaint(awtPaint);
			this.strokePaintObject = _paint;
			this.strokeAt = at;
		}
	}

	public void setStrokePaint(Object paint) throws GraphicsException {
		this.setPaint(paint, false);
	}

	public Object getStrokePaint() {
		return this.strokePaintObject;
	}

	public void setFillPaint(Object paint) throws GraphicsException {
		this.setPaint(paint, true);
	}

	public Object getFillPaint() {
		return this.fillPaintObject;
	}

	public void setStrokeAlpha(float alpha) {
		if (alpha == 1) {
			this.g.setPaintMode();
			return;
		}
		AlphaComposite comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
		this.g.setComposite(comp);
	}

	public float getStrokeAlpha() {
		return ((AlphaComposite) this.g.getComposite()).getAlpha();
	}

	public void setFillAlpha(float alpha) {
		this.fillAlpha = alpha;
	}

	public float getFillAlpha() {
		return this.fillAlpha;
	}

	public void setTextMode(short textMode) {
		this.textMode = textMode;
	}

	public short getTextMode() {
		return this.textMode;
	}

	public void transform(AffineTransform at) {
		this.g.transform(at);
	}

	public AffineTransform getTransform() {
		return this.g.getTransform();
	}

	public void clip(Shape shape) {
		this.g.clip(shape);
	}

	public void resetState() {
		GraphicsState state = (GraphicsState) this.stack.get(this.stack.size() - 1);
		state.restore(this);
	}

	public void drawImage(Image image) throws GraphicsException {
		this.drewAnything = true;

		Composite composite = this.g.getComposite();
		if (this.fillAlpha == 1) {
			this.g.setPaintMode();
		} else {
			this.g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.fillAlpha));
		}
		image.drawTo(this);
		this.g.setComposite(composite);
	}

	public void fill(Shape shape) {
		this.drewAnything = true;
		java.awt.Paint paint = this.g.getPaint();
		this.g.setPaint(this.fillPaint);

		Composite composite = this.g.getComposite();
		if (this.fillAlpha == 1) {
			this.g.setPaintMode();
		} else {
			this.g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.fillAlpha));
		}

		if (this.fillAt != null) {
			AffineTransform saveAt = g.getTransform();
			this.g.transform(this.fillAt);
			try {
				shape = this.fillAt.createInverse().createTransformedShape(shape);
			} catch (NoninvertibleTransformException e) {
				throw new RuntimeException(e);
			}
			this.g.fill(shape);
			this.g.setTransform(saveAt);
		} else {
			this.g.fill(shape);
		}

		this.g.setPaint(paint);
		this.g.setComposite(composite);
	}

	public void draw(Shape shape) {
		this.drewAnything = true;
		if (this.strokeAt != null) {
			AffineTransform saveAt = g.getTransform();
			this.g.transform(this.strokeAt);
			try {
				shape = this.strokeAt.createInverse().createTransformedShape(shape);
			} catch (NoninvertibleTransformException e) {
				throw new RuntimeException(e);
			}
			this.g.draw(shape);
			this.g.setTransform(saveAt);
		} else {
			this.g.draw(shape);
		}
	}

	public void fillDraw(Shape shape) {
		this.fill(shape);
		this.draw(shape);
	}

	public void drawText(Text text, double x, double y) throws GraphicsException {
		this.drewAnything = true;

		this.begin();
		this.transform(AffineTransform.getTranslateInstance(x, y));
		Font font = ((FontMetricsImpl) text.getFontMetrics()).getFont();
		try {
			font.drawTo(this, text);
		} catch (IOException e) {
			throw new GraphicsException(e);
		}
		this.end();
	}

	private static class G2dGroupImageGC extends G2DGC implements GroupImageGC {
		final BufferedImage image;
		final AffineTransform at;

		G2dGroupImageGC(Graphics2D g2d, FontManager fm, BufferedImage image, AffineTransform at) {
			super(g2d, fm);
			this.image = image;
			try {
				at = at == null ? null : at.createInverse();
			} catch (NoninvertibleTransformException e) {
				at = null;
			}
			this.at = at;
		}

		public Image finish() throws GraphicsException {
			Image im = new RasterImageImpl(this.image);
			if (this.at != null && !this.at.isIdentity()) {
				im = new TransformedImage(im, this.at);
			}
			return im;
		}
	}

	@SuppressWarnings("unchecked")
	public GroupImageGC createGroupImage(double width, double height) throws GraphicsException {
		final Point2D size = new Point2D.Double(width, height);
		final AffineTransform at = this.g.getTransform();
		if (at != null) {
			at.transform(size, size);
		}

		final int w = (int) size.getX();
		final int h = (int) size.getY();
		final BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2d = (Graphics2D) image.getGraphics();
		g2d.setRenderingHints(this.g.getRenderingHints());
		final G2dGroupImageGC gc = new G2dGroupImageGC(g2d, this.getFontManager(), image, at);
		final GraphicsState state = new GraphicsState(this);
		state.restore(gc);
		gc.stack = (ArrayList<GraphicsState>) this.stack.clone();
		return gc;
	}
}