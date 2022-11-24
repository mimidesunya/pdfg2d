package net.zamasoft.pdfg2d.gc;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import net.zamasoft.pdfg2d.gc.font.FontManager;
import net.zamasoft.pdfg2d.gc.image.GroupImageGC;
import net.zamasoft.pdfg2d.gc.image.Image;
import net.zamasoft.pdfg2d.gc.text.Text;

public class NopGC implements GC {
	protected static class GraphicsState {
		public final AffineTransform transform;

		public final double lineWidth;

		public final double[] linePattern;

		public final LineJoin lineJoin;

		public final LineCap lineCap;

		public final Object strokePaintObject;

		public final Object fillPaintObject;

		public final float fillAlpha, strokeAlpha;

		public final TextMode textMode;

		public GraphicsState(final NopGC gc) {
			this.transform = new AffineTransform(gc.transform);
			this.strokePaintObject = gc.strokePaintObject;
			this.fillPaintObject = gc.fillPaintObject;
			this.lineWidth = gc.lineWidth;
			this.linePattern = gc.linePattern;
			this.lineJoin = gc.lineJoin;
			this.lineCap = gc.lineCap;
			this.strokeAlpha = gc.strokeAlpha;
			this.fillAlpha = gc.fillAlpha;
			this.textMode = gc.textMode;
		}

		public void restore(final NopGC gc) {
			gc.transform = this.transform;
			gc.lineWidth = this.lineWidth;
			gc.linePattern = this.linePattern;
			gc.lineJoin = this.lineJoin;
			gc.lineCap = this.lineCap;
			gc.fillPaintObject = this.fillPaintObject;
			gc.strokePaintObject = this.strokePaintObject;
			gc.fillAlpha = this.fillAlpha;
			gc.strokeAlpha = this.strokeAlpha;
			gc.textMode = this.textMode;
		}
	}

	protected AffineTransform transform = new AffineTransform();

	protected double lineWidth = 1;

	protected double[] linePattern = GC.STROKE_SOLID;

	protected LineJoin lineJoin = LineJoin.MITER;

	protected LineCap lineCap = GC.LineCap.BUTT;

	protected Object strokePaintObject;

	protected Object fillPaintObject;

	protected float fillAlpha = 1, strokeAlpha = 1;

	protected TextMode textMode = TextMode.FILL;

	protected ArrayList<GraphicsState> stack = new ArrayList<GraphicsState>();

	protected final FontManager fm;

	public NopGC(final FontManager fm) {
		this.fm = fm;
	}

	public FontManager getFontManager() {
		return this.fm;
	}

	public void begin() {
		this.stack.add(new GraphicsState(this));
	}

	public void end() {
		final GraphicsState state = (GraphicsState) this.stack.remove(this.stack.size() - 1);
		state.restore(this);
	}

	public void setLineWidth(double lineWidth) {
		this.lineWidth = lineWidth;
	}

	public double getLineWidth() {
		return this.lineWidth;
	}

	public void setLinePattern(double[] linePattern) {
		this.linePattern = linePattern;
	}

	public double[] getLinePattern() {
		return this.linePattern;
	}

	public void setLineJoin(LineJoin lineJoin) {
		this.lineJoin = lineJoin;
	}

	public LineJoin getLineJoin() {
		return this.lineJoin;
	}

	public void setLineCap(LineCap lineCap) {
		this.lineCap = lineCap;
	}

	public LineCap getLineCap() {
		return this.lineCap;
	}

	public void setStrokePaint(Object paint) throws GraphicsException {
		this.strokePaintObject = paint;
	}

	public Object getStrokePaint() {
		return this.strokePaintObject;
	}

	public void setFillPaint(Object paint) throws GraphicsException {
		this.fillPaintObject = paint;
	}

	public Object getFillPaint() {
		return this.fillPaintObject;
	}

	public void setStrokeAlpha(float alpha) {
		this.strokeAlpha = alpha;
	}

	public float getStrokeAlpha() {
		return this.strokeAlpha;
	}

	public void setFillAlpha(float alpha) {
		this.fillAlpha = alpha;
	}

	public float getFillAlpha() {
		return this.fillAlpha;
	}

	public void setTextMode(TextMode textMode) {
		this.textMode = textMode;
	}

	public TextMode getTextMode() {
		return this.textMode;
	}

	public void transform(AffineTransform at) {
		this.transform.concatenate(at);
	}

	public AffineTransform getTransform() {
		return this.transform;
	}

	public void clip(Shape clip) {
		// ignore
	}

	public void resetState() {
		final GraphicsState state = (GraphicsState) this.stack.get(this.stack.size() - 1);
		state.restore(this);
	}

	public void drawImage(Image image) throws GraphicsException {
		// ignore
	}

	public void fill(Shape shape) {
		// ignore
	}

	public void draw(Shape shape) {
		// ignore
	}

	public void fillDraw(Shape shape) {
		// ignore
	}

	public void drawText(final Text text, final double x, final double y) throws GraphicsException {
		// ignore
	}

	public static class NopImage implements Image {
		protected final double width, height;

		public NopImage(final double width, final double height) {
			this.width = width;
			this.height = height;
		}

		public double getWidth() {
			return this.width;
		}

		public double getHeight() {
			return this.height;
		}

		public void drawTo(final GC gc) throws GraphicsException {
			// ignore
		}

		public String getAltString() {
			return "";
		}
	}

	public static class NopGroupImageGC extends NopGC implements GroupImageGC {
		private final double width, height;

		public NopGroupImageGC(final FontManager fm, final double width, final double height) {
			super(fm);
			this.width = width;
			this.height = height;
		}

		public Image finish() throws GraphicsException {
			return new NopImage(this.width, this.height);
		}
	}

	public GroupImageGC createGroupImage(final double width, final double height) throws GraphicsException {
		return new NopGroupImageGC(this.getFontManager(), width, height);
	}
}
