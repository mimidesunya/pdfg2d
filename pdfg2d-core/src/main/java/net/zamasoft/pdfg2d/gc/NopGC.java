package net.zamasoft.pdfg2d.gc;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import net.zamasoft.pdfg2d.gc.font.FontManager;
import net.zamasoft.pdfg2d.gc.image.GroupImageGC;
import net.zamasoft.pdfg2d.gc.image.Image;
import net.zamasoft.pdfg2d.gc.text.Text;

/**
 * A dummy graphics context that does nothing.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class NopGC implements GC {
	/**
	 * Represents the graphics state.
	 */
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

	protected final ArrayList<GraphicsState> stack = new ArrayList<>();

	protected final FontManager fm;

	/**
	 * Creates a new NopGC.
	 * 
	 * @param fm the font manager
	 */
	public NopGC(final FontManager fm) {
		this.fm = fm;
	}

	@Override
	public FontManager getFontManager() {
		return this.fm;
	}

	@Override
	public void begin() {
		this.stack.add(new GraphicsState(this));
	}

	@Override
	public void end() {
		final var state = this.stack.remove(this.stack.size() - 1);
		state.restore(this);
	}

	@Override
	public void setLineWidth(final double lineWidth) {
		this.lineWidth = lineWidth;
	}

	@Override
	public double getLineWidth() {
		return this.lineWidth;
	}

	@Override
	public void setLinePattern(final double[] linePattern) {
		this.linePattern = linePattern;
	}

	@Override
	public double[] getLinePattern() {
		return this.linePattern;
	}

	@Override
	public void setLineJoin(final LineJoin lineJoin) {
		this.lineJoin = lineJoin;
	}

	@Override
	public LineJoin getLineJoin() {
		return this.lineJoin;
	}

	@Override
	public void setLineCap(final LineCap lineCap) {
		this.lineCap = lineCap;
	}

	@Override
	public LineCap getLineCap() {
		return this.lineCap;
	}

	@Override
	public void setStrokePaint(final Object paint) throws GraphicsException {
		this.strokePaintObject = paint;
	}

	@Override
	public Object getStrokePaint() {
		return this.strokePaintObject;
	}

	@Override
	public void setFillPaint(final Object paint) throws GraphicsException {
		this.fillPaintObject = paint;
	}

	@Override
	public Object getFillPaint() {
		return this.fillPaintObject;
	}

	@Override
	public void setStrokeAlpha(final float alpha) {
		this.strokeAlpha = alpha;
	}

	@Override
	public float getStrokeAlpha() {
		return this.strokeAlpha;
	}

	@Override
	public void setFillAlpha(final float alpha) {
		this.fillAlpha = alpha;
	}

	@Override
	public float getFillAlpha() {
		return this.fillAlpha;
	}

	@Override
	public void setTextMode(final TextMode textMode) {
		this.textMode = textMode;
	}

	@Override
	public TextMode getTextMode() {
		return this.textMode;
	}

	@Override
	public void transform(final AffineTransform at) {
		this.transform.concatenate(at);
	}

	@Override
	public AffineTransform getTransform() {
		return this.transform;
	}

	@Override
	public void clip(final Shape clip) {
		// ignore
	}

	@Override
	public void resetState() {
		final var state = this.stack.get(this.stack.size() - 1);
		state.restore(this);
	}

	@Override
	public void drawImage(final Image image) throws GraphicsException {
		// ignore
	}

	@Override
	public void fill(final Shape shape) {
		// ignore
	}

	@Override
	public void draw(final Shape shape) {
		// ignore
	}

	@Override
	public void fillDraw(final Shape shape) {
		// ignore
	}

	@Override
	public void drawText(final Text text, final double x, final double y) throws GraphicsException {
		// ignore
	}

	/**
	 * A dummy image that does nothing.
	 */
	public static class NopImage implements Image {
		protected final double width, height;

		/**
		 * Creates a new NopImage.
		 * 
		 * @param width  the width
		 * @param height the height
		 */
		public NopImage(final double width, final double height) {
			this.width = width;
			this.height = height;
		}

		@Override
		public double getWidth() {
			return this.width;
		}

		@Override
		public double getHeight() {
			return this.height;
		}

		@Override
		public void drawTo(final GC gc) throws GraphicsException {
			// ignore
		}

		@Override
		public String getAltString() {
			return "";
		}
	}

	/**
	 * A dummy group image graphics context that does nothing.
	 */
	public static class NopGroupImageGC extends NopGC implements GroupImageGC {
		private final double width, height;

		/**
		 * Creates a new NopGroupImageGC.
		 * 
		 * @param fm     the font manager
		 * @param width  the width
		 * @param height the height
		 */
		public NopGroupImageGC(final FontManager fm, final double width, final double height) {
			super(fm);
			this.width = width;
			this.height = height;
		}

		@Override
		public Image finish() throws GraphicsException {
			return new NopImage(this.width, this.height);
		}
	}

	@Override
	public GroupImageGC createGroupImage(final double width, final double height) throws GraphicsException {
		return new NopGroupImageGC(this.getFontManager(), width, height);
	}
}
