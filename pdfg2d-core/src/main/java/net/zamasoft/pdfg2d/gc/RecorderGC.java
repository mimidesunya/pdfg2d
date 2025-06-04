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

public class RecorderGC extends NopGC {
	protected final List<Object> contents = new ArrayList<Object>();

	public RecorderGC(final FontManager fm) {
		super(fm);
	}

	public void begin() {
		super.begin();
		this.contents.add(Command.BEGIN);
	}

	public void end() {
		super.end();
		this.contents.add(Command.END);
	}

	public void setLineWidth(double lineWidth) {
		super.setLineWidth(lineWidth);
		this.contents.add(Command.LINE_WIDTH);
		this.contents.add(Double.valueOf(lineWidth));
	}

	public void setLinePattern(double[] linePattern) {
		super.setLinePattern(linePattern);
		this.contents.add(Command.LINE_PATTERN);
		this.contents.add(linePattern);
	}

	public void setLineJoin(LineJoin lineJoin) {
		super.setLineJoin(lineJoin);
		this.contents.add(Command.LINE_JOIN);
		this.contents.add(lineJoin);
	}

	public void setLineCap(LineCap lineCap) {
		super.setLineCap(lineCap);
		this.contents.add(Command.LINE_CAP);
		this.contents.add(lineCap);
	}

	public void setStrokePaint(Object paint) throws GraphicsException {
		super.setStrokePaint(paint);
		this.contents.add(Command.STROKE_PAINT);
		this.contents.add(paint);
	}

	public void setFillPaint(Object paint) throws GraphicsException {
		super.setFillPaint(paint);
		this.contents.add(Command.FILL_PAINT);
		this.contents.add(paint);
	}

	public void setStrokeAlpha(float alpha) {
		super.setStrokeAlpha(alpha);
		this.contents.add(Command.STROKE_ALPHA);
		this.contents.add(alpha);
	}

	public void setFillAlpha(float alpha) {
		super.setFillAlpha(alpha);
		this.contents.add(Command.FILL_ALPHA);
		this.contents.add(alpha);
	}

	public void setTextMode(TextMode textMode) {
		super.setTextMode(textMode);
		this.contents.add(Command.TEXT_MODE);
		this.contents.add(textMode);
	}

	public void transform(AffineTransform at) {
		super.transform(at);
		this.contents.add(Command.TRANSFORM);
		this.contents.add(at);
	}

	public void clip(Shape clip) {
		super.clip(clip);
		this.contents.add(Command.CLIP);
		this.contents.add(clip);
	}

	public void resetState() {
		super.resetState();
		this.contents.add(Command.RESET_STATE);
	}

	public void drawImage(Image image) throws GraphicsException {
		super.drawImage(image);
		this.contents.add(Command.DRAW_IMAGE);
		this.contents.add(image);
	}

	public void fill(Shape shape) {
		super.fill(shape);
		this.contents.add(Command.FILL);
		this.contents.add(shape);
	}

	public void draw(Shape shape) {
		super.draw(shape);
		this.contents.add(Command.DRAW);
		this.contents.add(shape);
	}

	public void fillDraw(Shape shape) {
		super.fillDraw(shape);
		this.contents.add(Command.FILL_DRAW);
		this.contents.add(shape);
	}

	public void drawText(final Text text, final double x, final double y) throws GraphicsException {
		super.drawText(text, x, y);
		this.contents.add(Command.DRAW_TEXT);
		this.contents.add(text);
		this.contents.add(x);
		this.contents.add(y);
	}

	public static class RecorderImage extends NopImage {
		protected final Page page;

		public RecorderImage(final double width, final double height, final Page page) {
			super(width, height);
			this.page = page;
		}

		public void drawTo(final GC gc) throws GraphicsException {
			this.page.drawTo(gc);
		}
	}

	public static class RecorderGroupImageGC extends RecorderGC implements GroupImageGC {
		private final double width, height;

		public RecorderGroupImageGC(final FontManager fm, final double width, final double height) {
			super(fm);
			this.width = width;
			this.height = height;
		}

		public Image finish() throws GraphicsException {
			final Page page = this.getPage();
			return new RecorderImage(this.width, this.height, page);
		}
	}

	public GroupImageGC createGroupImage(final double width, final double height) throws GraphicsException {
		return new RecorderGroupImageGC(this.getFontManager(), width, height);
	}

	public Page getPage() {
		return new Page(this.contents.toArray(new Object[this.contents.size()]));
	}

	public static class Page {
		protected Object[] data;

		protected Page(final Object[] data) {
			this.data = data;
		}

		public void drawTo(final GC gc) {
			for (int i = 0; i < this.data.length; ++i) {
				Command e = (Command) this.data[i];
				switch (e) {
				case BEGIN:
					gc.begin();
					break;
				case END:
					gc.end();
					break;
				case LINE_WIDTH: {
					Double width = (Double) this.data[++i];
					gc.setLineWidth(width);
				}
					break;
				case LINE_PATTERN: {
					double[] pattern = (double[]) this.data[++i];
					gc.setLinePattern(pattern);
				}
					break;
				case LINE_CAP: {
					LineCap lineCap = (LineCap) this.data[++i];
					gc.setLineCap(lineCap);
				}
					break;
				case LINE_JOIN: {
					LineJoin lineJoin = (LineJoin) this.data[++i];
					gc.setLineJoin(lineJoin);
				}
					break;
				case TEXT_MODE: {
					TextMode textMode = (TextMode) this.data[++i];
					gc.setTextMode(textMode);
				}
					break;
				case STROKE_PAINT: {
					Object paint = this.data[++i];
					gc.setStrokePaint(paint);
				}
					break;
				case FILL_PAINT: {
					Object paint = this.data[++i];
					gc.setFillPaint(paint);
				}
					break;
				case STROKE_ALPHA: {
					float paint = (Float) this.data[++i];
					gc.setStrokeAlpha(paint);
				}
					break;
				case FILL_ALPHA: {
					float paint = (Float) this.data[++i];
					gc.setFillAlpha(paint);
				}
					break;
				case TRANSFORM: {
					AffineTransform at = (AffineTransform) this.data[++i];
					gc.transform(at);
				}
					break;
				case CLIP: {
					Shape shape = (Shape) this.data[++i];
					gc.clip(shape);
				}
					break;
				case RESET_STATE:
					gc.resetState();
					break;
				case DRAW_IMAGE: {
					Image image = (Image) this.data[++i];
					gc.drawImage(image);
				}
					break;
				case FILL: {
					Shape shape = (Shape) this.data[++i];
					gc.fill(shape);
				}
					break;
				case DRAW: {
					Shape shape = (Shape) this.data[++i];
					gc.draw(shape);
				}
					break;
				case FILL_DRAW: {
					Shape shape = (Shape) this.data[++i];
					gc.fillDraw(shape);
				}
					break;
				case DRAW_TEXT: {
					Text text = (Text) this.data[++i];
					Double x = (Double) this.data[++i];
					Double y = (Double) this.data[++i];
					gc.drawText(text, x.doubleValue(), y.doubleValue());
				}
					break;
				default:
					throw new IllegalStateException(String.valueOf(e));
				}
			}
		}
	}
}