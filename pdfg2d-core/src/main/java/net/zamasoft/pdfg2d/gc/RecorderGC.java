package net.zamasoft.pdfg2d.gc;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

import net.zamasoft.pdfg2d.gc.font.FontManager;
import net.zamasoft.pdfg2d.gc.image.GroupImageGC;
import net.zamasoft.pdfg2d.gc.image.Image;
import net.zamasoft.pdfg2d.gc.text.Text;

enum Command {
	BEGIN, END, LINE_WIDTH, LINE_PATTERN, LINE_CAP, LINE_JOIN, TEXT_MODE, STROKE_PAINT, FILL_PAINT, STROKE_ALPHA,
	FILL_ALPHA, TRANSFORM, CLIP, RESET_STATE, DRAW_IMAGE, FILL, DRAW, FILL_DRAW, DRAW_TEXT
}

/**
 * A graphics context that records all graphics operations.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class RecorderGC extends NopGC {
	protected final List<Object> contents = new ArrayList<>();

	/**
	 * Creates a new RecorderGC.
	 * 
	 * @param fm the font manager
	 */
	public RecorderGC(final FontManager fm) {
		super(fm);
	}

	@Override
	public void begin() {
		super.begin();
		this.contents.add(Command.BEGIN);
	}

	@Override
	public void end() {
		super.end();
		this.contents.add(Command.END);
	}

	@Override
	public void setLineWidth(final double lineWidth) {
		super.setLineWidth(lineWidth);
		this.contents.add(Command.LINE_WIDTH);
		this.contents.add(Double.valueOf(lineWidth));
	}

	@Override
	public void setLinePattern(final double[] linePattern) {
		super.setLinePattern(linePattern);
		this.contents.add(Command.LINE_PATTERN);
		this.contents.add(linePattern);
	}

	@Override
	public void setLineJoin(final LineJoin lineJoin) {
		super.setLineJoin(lineJoin);
		this.contents.add(Command.LINE_JOIN);
		this.contents.add(lineJoin);
	}

	@Override
	public void setLineCap(final LineCap lineCap) {
		super.setLineCap(lineCap);
		this.contents.add(Command.LINE_CAP);
		this.contents.add(lineCap);
	}

	@Override
	public void setStrokePaint(final Object paint) throws GraphicsException {
		super.setStrokePaint(paint);
		this.contents.add(Command.STROKE_PAINT);
		this.contents.add(paint);
	}

	@Override
	public void setFillPaint(final Object paint) throws GraphicsException {
		super.setFillPaint(paint);
		this.contents.add(Command.FILL_PAINT);
		this.contents.add(paint);
	}

	@Override
	public void setStrokeAlpha(final float alpha) {
		super.setStrokeAlpha(alpha);
		this.contents.add(Command.STROKE_ALPHA);
		this.contents.add(alpha);
	}

	@Override
	public void setFillAlpha(final float alpha) {
		super.setFillAlpha(alpha);
		this.contents.add(Command.FILL_ALPHA);
		this.contents.add(alpha);
	}

	@Override
	public void setTextMode(final TextMode textMode) {
		super.setTextMode(textMode);
		this.contents.add(Command.TEXT_MODE);
		this.contents.add(textMode);
	}

	@Override
	public void transform(final AffineTransform at) {
		super.transform(at);
		this.contents.add(Command.TRANSFORM);
		this.contents.add(at);
	}

	@Override
	public void clip(final Shape clip) {
		super.clip(clip);
		this.contents.add(Command.CLIP);
		this.contents.add(clip);
	}

	@Override
	public void resetState() {
		super.resetState();
		this.contents.add(Command.RESET_STATE);
	}

	@Override
	public void drawImage(final Image image) throws GraphicsException {
		super.drawImage(image);
		this.contents.add(Command.DRAW_IMAGE);
		this.contents.add(image);
	}

	@Override
	public void fill(final Shape shape) {
		super.fill(shape);
		this.contents.add(Command.FILL);
		this.contents.add(shape);
	}

	@Override
	public void draw(final Shape shape) {
		super.draw(shape);
		this.contents.add(Command.DRAW);
		this.contents.add(shape);
	}

	@Override
	public void fillDraw(final Shape shape) {
		super.fillDraw(shape);
		this.contents.add(Command.FILL_DRAW);
		this.contents.add(shape);
	}

	@Override
	public void drawText(final Text text, final double x, final double y) throws GraphicsException {
		super.drawText(text, x, y);
		this.contents.add(Command.DRAW_TEXT);
		this.contents.add(text);
		this.contents.add(x);
		this.contents.add(y);
	}

	/**
	 * An image that records graphics operations.
	 */
	public static class RecorderImage extends NopImage {
		protected final Page page;

		/**
		 * Creates a new RecorderImage.
		 * 
		 * @param width  the width
		 * @param height the height
		 * @param page   the page containing recorded operations
		 */
		public RecorderImage(final double width, final double height, final Page page) {
			super(width, height);
			this.page = page;
		}

		@Override
		public void drawTo(final GC gc) throws GraphicsException {
			this.page.drawTo(gc);
		}
	}

	/**
	 * A group image graphics context that records operations.
	 */
	public static class RecorderGroupImageGC extends RecorderGC implements GroupImageGC {
		private final double width, height;

		/**
		 * Creates a new RecorderGroupImageGC.
		 * 
		 * @param fm     the font manager
		 * @param width  the width
		 * @param height the height
		 */
		public RecorderGroupImageGC(final FontManager fm, final double width, final double height) {
			super(fm);
			this.width = width;
			this.height = height;
		}

		@Override
		public Image finish() throws GraphicsException {
			final var page = this.getPage();
			return new RecorderImage(this.width, this.height, page);
		}
	}

	@Override
	public GroupImageGC createGroupImage(final double width, final double height) throws GraphicsException {
		return new RecorderGroupImageGC(this.getFontManager(), width, height);
	}

	/**
	 * Returns the page containing the recorded operations.
	 * 
	 * @return the recorded page
	 */
	public Page getPage() {
		return new Page(this.contents.toArray(new Object[0]));
	}

	/**
	 * Represents a page of recorded graphics operations.
	 */
	public static class Page {
		protected final Object[] data;

		/**
		 * Creates a new Page.
		 * 
		 * @param data the recorded data
		 */
		protected Page(final Object[] data) {
			this.data = data;
		}

		/**
		 * Replays the recorded operations to the given graphics context.
		 * 
		 * @param gc the graphics context
		 */
		public void drawTo(final GC gc) {
			for (int i = 0; i < this.data.length; ++i) {
				final var e = (Command) this.data[i];
				switch (e) {
					case BEGIN -> gc.begin();
					case END -> gc.end();
					case LINE_WIDTH -> {
						final var width = (Double) this.data[++i];
						gc.setLineWidth(width);
					}
					case LINE_PATTERN -> {
						final var pattern = (double[]) this.data[++i];
						gc.setLinePattern(pattern);
					}
					case LINE_CAP -> {
						final var lineCap = (LineCap) this.data[++i];
						gc.setLineCap(lineCap);
					}
					case LINE_JOIN -> {
						final var lineJoin = (LineJoin) this.data[++i];
						gc.setLineJoin(lineJoin);
					}
					case TEXT_MODE -> {
						final var textMode = (TextMode) this.data[++i];
						gc.setTextMode(textMode);
					}
					case STROKE_PAINT -> {
						final var paint = this.data[++i];
						gc.setStrokePaint(paint);
					}
					case FILL_PAINT -> {
						final var paint = this.data[++i];
						gc.setFillPaint(paint);
					}
					case STROKE_ALPHA -> {
						final var paint = (Float) this.data[++i];
						gc.setStrokeAlpha(paint);
					}
					case FILL_ALPHA -> {
						final var paint = (Float) this.data[++i];
						gc.setFillAlpha(paint);
					}
					case TRANSFORM -> {
						final var at = (AffineTransform) this.data[++i];
						gc.transform(at);
					}
					case CLIP -> {
						final var shape = (Shape) this.data[++i];
						gc.clip(shape);
					}
					case RESET_STATE -> gc.resetState();
					case DRAW_IMAGE -> {
						final var image = (Image) this.data[++i];
						gc.drawImage(image);
					}
					case FILL -> {
						final var shape = (Shape) this.data[++i];
						gc.fill(shape);
					}
					case DRAW -> {
						final var shape = (Shape) this.data[++i];
						gc.draw(shape);
					}
					case FILL_DRAW -> {
						final var shape = (Shape) this.data[++i];
						gc.fillDraw(shape);
					}
					case DRAW_TEXT -> {
						final var text = (Text) this.data[++i];
						final var x = (Double) this.data[++i];
						final var y = (Double) this.data[++i];
						gc.drawText(text, x.doubleValue(), y.doubleValue());
					}
					default -> throw new IllegalStateException(String.valueOf(e));
				}
			}
		}
	}
}