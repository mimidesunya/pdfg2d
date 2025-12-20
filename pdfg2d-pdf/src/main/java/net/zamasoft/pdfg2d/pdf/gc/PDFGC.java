package net.zamasoft.pdfg2d.pdf.gc;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.zamasoft.pdfg2d.font.BBox;
import net.zamasoft.pdfg2d.font.DrawableFont;
import net.zamasoft.pdfg2d.font.Font;
import net.zamasoft.pdfg2d.font.FontMetricsImpl;
import net.zamasoft.pdfg2d.font.FontSource;
import net.zamasoft.pdfg2d.font.ImageFont;
import net.zamasoft.pdfg2d.font.ShapedFont;
import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.GraphicsException;
import net.zamasoft.pdfg2d.gc.font.FontManager;
import net.zamasoft.pdfg2d.gc.font.FontPolicyList;
import net.zamasoft.pdfg2d.gc.font.FontStyle;
import net.zamasoft.pdfg2d.gc.font.FontStyle.Direction;
import net.zamasoft.pdfg2d.gc.font.FontStyle.Style;
import net.zamasoft.pdfg2d.gc.font.FontStyle.Weight;
import net.zamasoft.pdfg2d.gc.font.util.FontUtils;
import net.zamasoft.pdfg2d.gc.image.GroupImageGC;
import net.zamasoft.pdfg2d.gc.image.Image;
import net.zamasoft.pdfg2d.gc.paint.CMYKColor;
import net.zamasoft.pdfg2d.gc.paint.Color;
import net.zamasoft.pdfg2d.gc.paint.GrayColor;
import net.zamasoft.pdfg2d.gc.paint.LinearGradient;
import net.zamasoft.pdfg2d.gc.paint.Paint;
import net.zamasoft.pdfg2d.gc.paint.Pattern;
import net.zamasoft.pdfg2d.gc.paint.RadialGradient;
import net.zamasoft.pdfg2d.gc.paint.RGBAColor;
import net.zamasoft.pdfg2d.gc.text.Text;
import net.zamasoft.pdfg2d.pdf.PDFGraphicsOutput;
import net.zamasoft.pdfg2d.pdf.PDFNamedGraphicsOutput;
import net.zamasoft.pdfg2d.pdf.PDFNamedOutput;
import net.zamasoft.pdfg2d.pdf.PDFOutput;
import net.zamasoft.pdfg2d.pdf.PDFWriter;
import net.zamasoft.pdfg2d.pdf.font.PDFFont;
import net.zamasoft.pdfg2d.pdf.font.PDFFontSource;
import net.zamasoft.pdfg2d.pdf.font.PDFFontSource.Type;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;
import net.zamasoft.pdfg2d.util.ColorUtils;

/* PDF Operator Reference
 * 
 * w	line width
 * J	line cap
 * j	line join
 * M	miter limit
 * d	line dash pattern
 * ri	rendering intents
 * i	flatness tolerance
 * gs	special graphics state
 * 
 * q	save graphics state
 * Q	restore	graphics state
 * cm	current transformation matrics
 * 
 * m	moveTo
 * l	lineTo
 * c	curveTo (1,2,3)
 * v	curveTo (2,3)
 * y	curveTo (1,3)
 * h	closePath
 * re	rectangle
 * 
 * S	stroke
 * s	[h S]
 * f	fill Bonzero Winding Number Rule
 * F	[f]
 * f*	fill Even-Odd Rule
 * B	[f S]
 * B*	[f* S]
 * b	[h B]
 * b*	[h B*]
 * n	nop
 * 
 * W	clip Bonzero Winding Number Rule
 * W*	clip Even-Odd Rule
 * 
 * BT
 * ET
 * 
 * Tc
 * Tw
 * Tz
 * TL
 * Tf
 * Tr
 * Ts
 * 
 * Td
 * TD
 * Tm
 * T*
 * 
 * Tj
 * TJ
 * '
 * "
 * 
 * d0
 * d1
 * 
 * CS	stroke color space
 * cs	nonstroke color space
 * SC	stroke color
 * SCN
 * sc
 * scn
 * G
 * g
 * RG
 * rg
 * K
 * k
 * 
 * sh
 * 
 * BI
 * ID
 * EI
 * 
 * Do	draw object
 * 
 * MP
 * DP
 * BMC
 * BDC
 * EMC
 * 
 * BX
 * EX
 * 
 * sh	shading pattern
 */

/**
 * PDF Graphics Context.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class PDFGC implements GC, Closeable {
	private static final Logger LOG = Logger.getLogger(PDFGC.class.getName());

	private static final boolean DEBUG = false;

	protected final PDFGraphicsOutput out;

	/**
	 * Object for saving the configured graphics state.
	 * 
	 * @author MIYABE Tatsuhiko
	 * @since 1.0
	 */
	static class GraphicsState {
		public XGraphicsState gstate = null;

		public final double lineWidth;

		public final LineCap lineCap;

		public final LineJoin lineJoin;

		public final double[] linePattern;

		public final Paint strokePaint;

		public final Paint fillPaint;

		public final TextMode textMode;

		public final float strokeAlpha;

		public final float fillAlpha;

		public final byte strokeOverprint;

		public final byte fillOverprint;

		public final AffineTransform actualTransform;

		public GraphicsState(PDFGC gc) {
			this.lineWidth = gc.lineWidth;
			this.lineCap = gc.lineCap;
			this.lineJoin = gc.lineJoin;
			this.linePattern = gc.linePattern;
			this.strokePaint = gc.strokePaint;
			this.fillPaint = gc.fillPaint;
			this.textMode = gc.textMode;
			this.strokeAlpha = gc.strokeAlpha;
			this.fillAlpha = gc.fillAlpha;
			this.strokeOverprint = gc.strokeOverprint;
			this.fillOverprint = gc.fillOverprint;
			this.actualTransform = gc.actualTransform;
			if (this.actualTransform != null) {
				gc.actualTransform = new AffineTransform(this.actualTransform);
			}
		}

		public void restore(PDFGC gc) {
			gc.lineWidth = this.lineWidth;
			gc.lineCap = this.lineCap;
			gc.lineJoin = this.lineJoin;
			gc.linePattern = this.linePattern;
			gc.strokePaint = this.strokePaint;
			gc.fillPaint = this.fillPaint;
			gc.textMode = this.textMode;
			gc.strokeAlpha = this.strokeAlpha;
			gc.fillAlpha = this.fillAlpha;
			gc.strokeOverprint = this.strokeOverprint;
			gc.fillOverprint = this.fillOverprint;
			gc.actualTransform = this.actualTransform;
		}
	}

	/**
	 * Object for saving the current PDF graphics state.
	 * 
	 * @author MIYABE Tatsuhiko
	 * @since 1.0
	 */
	static class XGraphicsState {
		public final double lineWidth;

		public final LineCap lineCap;

		public final LineJoin lineJoin;

		public final double[] linePattern;

		public final Paint strokePaint;

		public final Paint fillPaint;

		public final float fillAlpha;

		public final float strokeAlpha;

		public final byte fillOverprint;

		public final byte strokeOverprint;

		public final double letterSpacing;

		public final TextMode textMode;

		public XGraphicsState(PDFGC gc) {
			this.lineWidth = gc.xlineWidth;
			this.lineCap = gc.xlineCap;
			this.lineJoin = gc.xlineJoin;
			this.linePattern = gc.xlinePattern;
			this.strokePaint = gc.xstrokePaint;
			this.fillPaint = gc.xfillPaint;
			this.letterSpacing = gc.xletterSpacing;
			this.textMode = gc.xtextMode;
			this.fillAlpha = gc.xfillAlpha;
			this.strokeAlpha = gc.xstrokeAlpha;
			this.fillOverprint = gc.xfillOverprint;
			this.strokeOverprint = gc.xstrokeOverprint;
		}

		public void restore(PDFGC gc) {
			gc.xlineWidth = this.lineWidth;
			gc.xlineCap = this.lineCap;
			gc.xlineJoin = this.lineJoin;
			gc.xlinePattern = this.linePattern;
			gc.xstrokePaint = this.strokePaint;
			gc.xfillPaint = this.fillPaint;
			gc.xletterSpacing = this.letterSpacing;
			gc.xtextMode = this.textMode;
			gc.xfillAlpha = this.fillAlpha;
			gc.xstrokeAlpha = this.strokeAlpha;
			gc.xfillOverprint = this.fillOverprint;
			gc.xstrokeOverprint = this.strokeOverprint;
		}
	}

	private List<GraphicsState> stack = new ArrayList<GraphicsState>();

	private AffineTransform transform = null;

	private AffineTransform actualTransform = null;

	private Shape clip = null;

	/** Line cap style. */
	private LineCap lineCap = LineCap.SQUARE;

	/** Current PDF line cap style. */
	private LineCap xlineCap = LineCap.SQUARE;

	/** Line join style. */
	private LineJoin lineJoin = LineJoin.MITER;

	/** Current PDF line join style. */
	private LineJoin xlineJoin = LineJoin.MITER;

	/** Line width. */
	private double lineWidth = 1;

	/** Current PDF line width. */
	private double xlineWidth = 1;

	/** Line dash pattern. */
	private double[] linePattern = STROKE_SOLID;

	/** Current PDF line dash pattern. */
	private double[] xlinePattern = STROKE_SOLID;

	/** Stroke paint. */
	private Paint strokePaint = GrayColor.BLACK;

	/** Current PDF stroke paint. */
	private Paint xstrokePaint = GrayColor.BLACK;

	/** Fill paint. */
	private Paint fillPaint = GrayColor.BLACK;

	/** Current PDF fill paint. */
	private Paint xfillPaint = GrayColor.BLACK;

	private double xletterSpacing = 0;

	/** Text rendering mode. */
	private TextMode textMode = TextMode.FILL;

	/** Current PDF text rendering mode. */
	private TextMode xtextMode = TextMode.FILL;

	/** Stroke opacity. */
	public float strokeAlpha = 1;

	/** Current PDF stroke opacity. */
	public float xstrokeAlpha = 1;

	/** Fill opacity. */
	public float fillAlpha = 1;

	/** Current PDF fill opacity. */
	public float xfillAlpha = 1;

	/** Stroke overprint mode. */
	public byte strokeOverprint = 0;

	/** Current PDF stroke overprint mode. */
	public byte xstrokeOverprint = 0;

	/** Fill overprint mode. */
	public byte fillOverprint = 0;

	/** Current PDF fill overprint mode. */
	public byte xfillOverprint = 0;

	private static class PatternKey {
		final double pageWidth;

		final double pageHeight;

		final Image image;

		final AffineTransform at;

		PatternKey(double pageWidth, double pageHeight, Image image, AffineTransform at) {
			this.pageWidth = pageWidth;
			this.pageHeight = pageHeight;
			this.image = image;
			this.at = at;
		}

		public boolean equals(Object o) {
			if (o instanceof PatternKey) {
				PatternKey key = (PatternKey) o;
				return key.pageWidth == this.pageWidth && key.pageHeight == this.pageHeight
						&& key.image.equals(this.image) && key.at.equals(this.at);
			}
			return false;
		}

		public int hashCode() {
			int hash = 1;
			long a = Double.doubleToLongBits(this.pageWidth);
			long b = Double.doubleToLongBits(this.pageHeight);
			hash = hash * 31 + (int) (a ^ (a >>> 32));
			hash = hash * 31 + (int) (b ^ (b >>> 32));
			hash = hash * 31 + this.image.hashCode();
			if (this.at != null) {
				hash = hash * 31 + this.at.hashCode();
			}
			return hash;
		}
	}

	private final Map<PatternKey, Object> patterns;

	private int qDepth = 0;

	private final PDFParams.Version pdfVersion;

	private PDFGC(PDFGraphicsOutput out, Map<PatternKey, Object> patterns) {
		this.out = out;
		this.patterns = patterns;
		this.pdfVersion = this.out.getPdfWriter().getParams().getVersion();
		this.stack.add(new GraphicsState(this));
	}

	public PDFGC(PDFGraphicsOutput out) {
		this(out, new HashMap<PatternKey, Object>());
	}

	public FontManager getFontManager() {
		return this.getPdfWriter().getFontManager();
	}

	public PDFGraphicsOutput getPDFGraphicsOutput() {
		return this.out;
	}

	public PDFWriter getPdfWriter() {
		return this.out.getPdfWriter();
	}

	public void begin() throws GraphicsException {
		if (DEBUG) {
			System.err.println("begin");
		}
		try {
			this.applyTransform();
			this.applyClip();
		} catch (IOException e) {
			throw new GraphicsException(e);
		}
		this.stack.add(new GraphicsState(this));
	}

	public void end() throws GraphicsException {
		if (DEBUG) {
			System.err.println("end");
		}
		try {
			this.grestore();
		} catch (IOException e) {
			throw new GraphicsException(e);
		}
		GraphicsState state = (GraphicsState) this.stack.remove(this.stack.size() - 1);
		state.restore(this);
		if (this.stack.isEmpty()) {
			this.transform = null;
		}
		this.clip = null;
	}

	public void resetState() throws GraphicsException {
		if (DEBUG) {
			System.err.println("reset");
		}
		try {
			this.grestore();
		} catch (IOException e) {
			throw new GraphicsException(e);
		}
		GraphicsState state = (GraphicsState) this.stack.get(this.stack.size() - 1);
		state.restore(this);
		this.transform = null;
		this.clip = null;
	}

	public void setLineWidth(double lineWidth) {
		if (DEBUG) {
			System.err.println("setLineWidth: " + lineWidth);
		}
		this.lineWidth = lineWidth;
	}

	public double getLineWidth() {
		return this.lineWidth;
	}

	public void setLinePattern(double[] linePattern) {
		if (DEBUG) {
			System.err.println("setLinePattern: " + linePattern);
		}
		if (linePattern != null && linePattern.length > 0) {
			this.linePattern = linePattern;
		} else {
			this.linePattern = STROKE_SOLID;
		}
	}

	public double[] getLinePattern() {
		return this.linePattern;
	}

	public void setLineJoin(LineJoin lineJoin) {
		if (DEBUG) {
			System.err.println("setLineJoin: " + lineJoin);
		}
		this.lineJoin = lineJoin;
	}

	public LineJoin getLineJoin() {
		return this.lineJoin;
	}

	public void setLineCap(LineCap lineCap) {
		if (DEBUG) {
			System.err.println("setLineCap: " + lineCap);
		}
		this.lineCap = lineCap;
	}

	public LineCap getLineCap() {
		return this.lineCap;
	}

	public void setStrokePaint(Paint paint) throws GraphicsException {
		if (DEBUG) {
			System.err.println("setStrokePaint: " + paint);
		}
		this.setPaint(paint, false);
	}

	public Paint getStrokePaint() {
		return this.strokePaint;
	}

	public void setFillPaint(Paint paint) throws GraphicsException {
		if (DEBUG) {
			System.err.println("setFillPaint: " + paint);
		}
		this.setPaint(paint, true);
	}

	public Paint getFillPaint() {
		return this.fillPaint;
	}

	protected void setPaint(Paint paint, boolean fill) throws GraphicsException {
		if (fill) {
			this.fillPaint = paint;
			this.fillAlpha = 1;
			this.fillOverprint = CMYKColor.OVERPRINT_NONE;
		} else {
			this.strokePaint = paint;
			this.strokeAlpha = 1;
			this.strokeOverprint = CMYKColor.OVERPRINT_NONE;
		}

		switch (paint) {
			case RGBAColor rgba -> {
				if (fill) {
					this.fillAlpha = rgba.getAlpha();
				} else {
					this.strokeAlpha = rgba.getAlpha();
				}
			}
			case CMYKColor cmyk -> {
				if (fill) {
					this.fillOverprint = cmyk.getOverprint();
				} else {
					this.strokeOverprint = cmyk.getOverprint();
				}
			}
			case Color color -> {
				// Other color types (RGB, Gray) - defaults already set
			}
			case Paint other -> {
				// Pattern, gradients - defaults already set
			}
		}
	}

	public float getStrokeAlpha() {
		return this.strokeAlpha;
	}

	public void setStrokeAlpha(float strokeAlpha) {
		this.strokeAlpha = strokeAlpha;
	}

	public float getFillAlpha() {
		return this.fillAlpha;
	}

	public void setFillAlpha(float fillAlpha) {
		this.fillAlpha = fillAlpha;
	}

	public void setTextMode(TextMode textMode) {
		if (DEBUG) {
			System.err.println("setTextMode: " + textMode);
		}
		this.textMode = textMode;
	}

	public TextMode getTextMode() {
		return this.textMode;
	}

	public void transform(AffineTransform at) throws GraphicsException {
		if (DEBUG) {
			System.err.println("transform: " + at);
		}
		if (at == null || at.isIdentity()) {
			return;
		}
		// assert at.getScaleX() != 0;
		// assert at.getScaleY() != 0;
		assert !Double.isNaN(at.getTranslateX());
		assert !Double.isNaN(at.getTranslateY());
		assert !Double.isNaN(at.getScaleX());
		assert !Double.isNaN(at.getScaleY());
		assert !Double.isNaN(at.getShearX());
		assert !Double.isNaN(at.getShearY());
		try {
			this.applyClip();
		} catch (IOException e) {
			throw new GraphicsException(e);
		}
		if (this.transform == null) {
			this.transform = new AffineTransform(at);
		} else {
			this.transform.concatenate(at);
		}
		if (this.actualTransform == null) {
			this.actualTransform = new AffineTransform(at);
		} else {
			this.actualTransform.concatenate(at);
		}
	}

	public AffineTransform getTransform() {
		return this.actualTransform == null ? null : new AffineTransform(this.actualTransform);
	}

	public void clip(Shape clip) throws GraphicsException {
		if (DEBUG) {
			System.err.println("clip: " + (clip == null ? clip : clip.getBounds2D()));
		}
		try {
			this.applyTransform();
			this.applyClip();
		} catch (IOException e) {
			throw new GraphicsException(e);
		}
		this.clip = clip;
	}

	public void fill(Shape shape) throws GraphicsException {
		if (DEBUG) {
			System.err.println("fill: " + shape.getBounds2D());
		}
		try {
			this.applyStates();
			int winding;
			if (shape instanceof Rectangle2D) {
				Rectangle2D r = (Rectangle2D) shape;
				if (this.out.equals(r.getWidth(), 0.0) || this.out.equals(r.getHeight(), 0.0)) {
					return;
				}
				winding = PathIterator.WIND_NON_ZERO;
				this.plotRect(r);
			} else {
				PathIterator i = shape.getPathIterator(null);
				winding = i.getWindingRule();
				this.plot(i);
			}

			switch (winding) {
				case PathIterator.WIND_NON_ZERO:
					this.out.writeOperator("f");
					break;
				case PathIterator.WIND_EVEN_ODD:
					this.out.writeOperator("f*");
					break;
				default:
					throw new IllegalStateException();
			}
		} catch (IOException e) {
			throw new GraphicsException(e);
		}
	}

	public void draw(Shape shape) throws GraphicsException {
		if (DEBUG) {
			System.err.println("draw: " + shape.getBounds2D());
		}
		try {
			this.applyStates();
			boolean close;
			if (shape instanceof Rectangle2D) {
				Rectangle2D r = (Rectangle2D) shape;
				close = false;
				this.plotRect(r);
			} else {
				PathIterator i = shape.getPathIterator(null);
				close = this.plot(i);
			}

			this.out.writeOperator(close ? "s" : "S");
		} catch (IOException e) {
			throw new GraphicsException(e);
		}
	}

	public void fillDraw(Shape shape) throws GraphicsException {
		if (DEBUG) {
			System.err.println("fillDraw: " + shape.getBounds2D());
		}
		try {
			this.applyStates();
			int winding;
			boolean close;
			if (shape instanceof Rectangle2D) {
				Rectangle2D r = (Rectangle2D) shape;
				winding = PathIterator.WIND_NON_ZERO;
				close = false;
				this.plotRect(r);
			} else {
				PathIterator i = shape.getPathIterator(null);
				winding = i.getWindingRule();
				close = this.plot(i);
			}

			switch (winding) {
				case PathIterator.WIND_NON_ZERO:
					this.out.writeOperator(close ? "b" : "B");
					break;
				case PathIterator.WIND_EVEN_ODD:
					this.out.writeOperator(close ? "b*" : "B*");
					break;
				default:
					throw new IllegalStateException();
			}
		} catch (IOException e) {
			throw new GraphicsException(e);
		}
	}

	public void drawImage(Image image) throws GraphicsException {
		if (DEBUG) {
			System.err.println("drawImage: " + image);
		}
		try {
			this.applyStates();
		} catch (IOException e) {
			throw new GraphicsException(e);
		}
		image.drawTo(this);
	}

	public void drawPDFImage(String name, double width, double height) throws GraphicsException {
		try {
			this.applyStates();
			this.begin();

			this.gsave();
			this.out.writeReal(width);
			this.out.writeReal(0);
			this.out.writeReal(0);
			this.out.writeReal(height);
			this.out.writePosition(0, height);
			this.out.writeOperator("cm");

			this.out.useResource("XObject", name);
			this.out.writeName(name);
			this.out.writeOperator("Do");

			this.end();
		} catch (IOException e) {
			throw new GraphicsException(e);
		}
	}

	public void drawText(Text text, double x, double y) throws GraphicsException {
		if (DEBUG) {
			System.err.println("drawText: " + text);
		}
		if (text.getGlyphCount() <= 0) {
			return;
		}

		Font font = ((FontMetricsImpl) text.getFontMetrics()).getFont();
		FontPolicyList fpl = text.getFontStyle().getPolicy();
		boolean outline = false;
		LOOP: for (int i = 0; i < fpl.getLength(); ++i) {
			switch (fpl.get(i)) {
				case EMBEDDED:
				case CID_IDENTITY:
					break LOOP;
				case OUTLINES:
					outline = true;
					break LOOP;
				default:
					break;
			}
		}
		if (outline || font instanceof ImageFont) {
			if (font instanceof DrawableFont) {
				if (font instanceof ShapedFont) {
					int glyphCount = text.getGlyphCount();
					int[] glyphIds = text.getGlyphIds();
					boolean hasShape = false;
					for (int i = 0; i < glyphCount; ++i) {
						int gid = glyphIds[i];
						Shape shape = ((ShapedFont) font).getShapeByGID(gid);
						if (shape != null && !shape.getPathIterator(null).isDone()) {
							hasShape = true;
							break;
						}
					}
					if (!hasShape) {
						// No characters to draw
						return;
					}
				}
				this.begin();
				this.transform(AffineTransform.getTranslateInstance(x, y));
				FontUtils.drawText(this, (DrawableFont) font, text);
				this.end();
				return;
			}
		}

		assert text.getCharCount() > 0;
		try {
			this.applyStates();
			if (this.textMode != this.xtextMode) {
				this.xtextMode = this.textMode;
				this.out.writeInt(this.textMode.code);
				this.out.writeOperator("Tr");
			}

			FontMetricsImpl fm = (FontMetricsImpl) text.getFontMetrics();
			PDFFontSource source = (PDFFontSource) fm.getFontSource();
			if (this.pdfVersion.v == PDFParams.Version.V_PDFA1B.v
					|| this.pdfVersion.v == PDFParams.Version.V_PDFX1A.v) {
				Type type = source.getType();
				if (type != Type.EMBEDDED && type != Type.MISSING) {
					throw new IllegalStateException("Only embedded fonts can be used in PDF/A-1 or PDF/X-1a.");
				}
			}
			FontStyle fontStyle = text.getFontStyle();

			if (LOG.isLoggable(Level.FINE)) {
				LOG.fine("drawText: fontSource=" + source + " text=" + text);
			}

			boolean localContext = false;
			double size = fontStyle.getSize();
			double enlargement;
			Weight weight = fontStyle.getWeight();
			if (this.textMode == TextMode.FILL && weight.w >= 500 && source.getWeight().w < 500) {
				// Simulate bold manually
				switch (weight) {
					case W_500:
						enlargement = size / 28.0;
						break;
					case W_600:
						enlargement = size / 24.0;
						break;
					case W_700:
						enlargement = size / 20.0;
						break;
					case W_800:
						enlargement = size / 16.0;
						break;
					case W_900:
						enlargement = size / 12.0;
						break;
					default:
						throw new IllegalStateException();
				}
				if (enlargement > 0 && this.fillPaint.getPaintType() == Paint.Type.COLOR && this.fillAlpha == 1) {
					this.q();
					localContext = true;
					this.out.writeReal(enlargement);
					this.out.writeOperator("w");
					this.out.writeInt(TextMode.FILL_STROKE.code);
					this.out.writeOperator("Tr");
					if (!this.fillPaint.equals(this.strokePaint)) {
						if (this.xstrokePaint != null && this.xstrokePaint.getPaintType() != Paint.Type.COLOR) {
							this.out.writeName("DeviceRGB");
							this.out.writeOperator("CS");
						}
						this.out.writeStrokeColor((Color) this.fillPaint);
					}
				}
			} else {
				enlargement = 0;
			}

			// Text direction
			Direction direction = fontStyle.getDirection();
			AffineTransform rotate = null;
			double center = 0;
			boolean verticalFont = false;
			switch (direction) {
				case LTR:
				case RTL:// TODO RTL
					// Horizontal
					break;
				case TB:
					// Vertical
					if (source.getDirection() == direction) {
						// Vertical typesetting
						verticalFont = true;
					} else {
						// 90-degree rotated horizontal
						if (!localContext) {
							this.q();
							localContext = true;
						}
						rotate = AffineTransform.getRotateInstance(Math.PI / 2, x, y);
						this.out.writeTransform(rotate);
						this.out.writeOperator("cm");
						BBox bbox = source.getBBox();
						center = ((bbox.lly() + bbox.ury()) * size / FontSource.DEFAULT_UNITS_PER_EM) / 2.0;
						y += center;
					}
					break;
				default:
					throw new IllegalStateException();
			}

			// Begin text
			this.out.writeOperator("BT");

			// Italic
			Style style = fontStyle.getStyle();
			if (style != Style.NORMAL && !source.isItalic()) {
				// Simulate italic manually
				if (verticalFont) {
					// Vertical italic
					this.out.writeReal(1);
					this.out.writeReal(-0.25);
					this.out.writeReal(0);
					this.out.writeReal(1);
					this.out.writePosition(x, y);
					this.out.writeOperator("Tm");
				} else {
					// Horizontal italic
					this.out.writeReal(1);
					this.out.writeReal(0);
					this.out.writeReal(0.25);
					this.out.writeReal(1);
					this.out.writePosition(x, y);
					this.out.writeOperator("Tm");
				}
			} else {
				this.out.writePosition(x, y);
				this.out.writeOperator("Td");
			}

			// Font name and size
			String name = ((PDFFont) font).getName();
			this.out.useResource("Font", name);
			this.out.writeName(name);
			this.out.writeReal(size);
			this.out.writeOperator("Tf");

			// Letter spacing
			double letterSpacing = text.getLetterSpacing();
			// Use negative value for vertical writing (PDF 1.3 spec 8.7.1.1)
			if (verticalFont) {
				letterSpacing = -letterSpacing;
			}
			if (!this.out.equals(letterSpacing, this.xletterSpacing)) {
				this.out.writeReal(letterSpacing);
				this.out.writeOperator("Tc");
				if (!localContext) {
					this.xletterSpacing = letterSpacing;
				}
			}

			// Draw
			font.drawTo(this, text);

			// End text
			this.out.writeOperator("ET");

			if (enlargement > 0 && this.fillPaint.getPaintType() == Paint.Type.COLOR && this.fillAlpha == 1) {
				// End bold simulation
				this.out.writeInt(TextMode.FILL.code);
				this.out.writeOperator("Tr");
				if (!this.fillPaint.equals(this.strokePaint)) {
					if (this.xfillPaint != null && this.xfillPaint.getPaintType() != Paint.Type.COLOR) {
						this.out.writeName("DeviceRGB");
						this.out.writeOperator("CS");
					}
					this.out.writeStrokeColor((Color) this.strokePaint);
				}
			}

			if (localContext) {
				this.Q();
			}
		} catch (IOException e) {
			throw new GraphicsException(e);
		}
	}

	private static class PdfGroupImageGC extends PDFGC implements GroupImageGC {
		PdfGroupImageGC(PDFGroupImage image) {
			super(image);
		}

		public Image finish() throws GraphicsException {
			PDFGroupImage image = (PDFGroupImage) this.out;
			try {
				image.close();
			} catch (IOException e) {
				throw new GraphicsException(e);
			}
			return image;
		}
	}

	public GroupImageGC createGroupImage(double width, double height) {
		try {
			final PDFGroupImage image = this.getPdfWriter().createGroupImage(width, height);
			final GroupImageGC gc = new PdfGroupImageGC(image);
			return gc;
		} catch (IOException e) {
			throw new GraphicsException(e);
		}
	}

	protected void applyTransform() throws IOException {
		if (this.transform != null) {
			this.gsave();
			this.out.writeTransform(this.transform);
			this.out.writeOperator("cm");
			this.transform = null;
		}
	}

	protected void applyClip() throws IOException {
		if (this.clip != null) {
			this.gsave();
			int winding;
			if (this.clip instanceof Rectangle2D) {
				Rectangle2D r = (Rectangle2D) this.clip;
				winding = PathIterator.WIND_NON_ZERO;
				this.out.writeRect((double) r.getX(), (double) r.getY(), (double) r.getWidth(), (double) r.getHeight());
				this.out.writeOperator("re");
			} else {
				PathIterator i = this.clip.getPathIterator(null);
				winding = i.getWindingRule();
				this.plot(i);
			}

			switch (winding) {
				case PathIterator.WIND_NON_ZERO:
					this.out.writeOperator("W");
					break;
				case PathIterator.WIND_EVEN_ODD:
					this.out.writeOperator("W*");
					break;
				default:
					throw new IllegalStateException();
			}
			this.out.writeOperator("n");
			this.clip = null;
		}
	}

	private String getPaintName(Paint paint) throws GraphicsException {
		switch (paint.getPaintType()) {
			case PATTERN: {
				String name;
				Pattern pattern = (Pattern) paint;
				Image image = pattern.getImage();
				AffineTransform at = this.getTransform();
				if (at == null) {
					at = pattern.getTransform();
				} else if (pattern.getTransform() != null) {
					at.concatenate(pattern.getTransform());
				}

				PDFGraphicsOutput pout = (PDFGraphicsOutput) this.out;
				PatternKey key = new PatternKey(pout.getWidth(), pout.getHeight(), image, at);

				name = (String) this.patterns.get(key);
				if (name == null) {
					double width = image.getWidth();
					double height = image.getHeight();
					try (PDFNamedGraphicsOutput tout = pout.getPdfWriter().createTilingPattern(width, height,
							pout.getHeight(), at)) {
						PDFGC pgc = new PDFGC(tout, this.patterns);
						image.drawTo(pgc);
						name = tout.getName();
					} catch (IOException e) {
						new GraphicsException(e);
					}
					this.patterns.put(key, name);
				}
				return name;
			}
			case LINEAR_GRADIENT: {
				// PDF Axial(Type 2) Shading
				if (this.pdfVersion.v < PDFParams.Version.V_1_3.v) {
					return null;
				}
				LinearGradient gradient = (LinearGradient) paint;

				Color[] colors = gradient.colors();
				double[] fractions = gradient.fractions();

				AffineTransform at = this.getTransform();
				if (at == null) {
					at = gradient.transform();
				} else if (gradient.transform() != null) {
					at.concatenate(gradient.transform());
				}

				PDFGraphicsOutput pout = (PDFGraphicsOutput) this.out;
				try (PDFNamedOutput sout = pout.getPdfWriter().createShadingPattern(pout.getHeight(), at)) {
					sout.writeName("ShadingType");
					sout.writeInt(2);
					sout.lineBreak();

					sout.writeName("Coords");
					sout.startArray();
					sout.writeReal(gradient.x1());
					sout.writeReal(gradient.y1());
					sout.writeReal(gradient.x2());
					sout.writeReal(gradient.y2());
					sout.endArray();
					sout.lineBreak();
					this.shadingFunction(sout, colors, fractions);

					return sout.getName();
				} catch (IOException e) {
					throw new GraphicsException(e);
				}
			}
			case RADIAL_GRADIENT: {
				// PDF Radial(Type 3) Shading
				if (this.pdfVersion.v < PDFParams.Version.V_1_3.v) {
					return null;
				}
				RadialGradient gp = (RadialGradient) paint;

				Color[] colors = gp.colors();
				double[] fractions = gp.fractions();
				double radius = gp.radius();

				AffineTransform at = this.getTransform();
				if (at == null) {
					at = gp.transform();
				} else if (gp.transform() != null) {
					at.concatenate(gp.transform());
				}

				double dx = gp.fx() - gp.cx();
				double dy = gp.fy() - gp.cy();
				double d = Math.sqrt(dx * dx + dy * dy);
				if (d > radius) {
					double scale = (radius * .9999) / d;
					dx = dx * scale;
					dy = dy * scale;
				}

				PDFGraphicsOutput pout = (PDFGraphicsOutput) this.out;
				try (PDFNamedOutput sout = pout.getPdfWriter().createShadingPattern(pout.getHeight(), at)) {
					sout.writeName("ShadingType");
					sout.writeInt(3);
					sout.lineBreak();

					sout.writeName("Coords");
					sout.startArray();
					sout.writeReal(gp.cx() + dx);
					sout.writeReal(gp.cy() + dy);
					sout.writeReal(0);
					sout.writeReal(gp.cx());
					sout.writeReal(gp.cy());
					sout.writeReal(radius);
					sout.endArray();
					sout.lineBreak();

					this.shadingFunction(sout, colors, fractions);
					return sout.getName();
				} catch (IOException e) {
					throw new GraphicsException(e);
				}
			}

			default:
				throw new IllegalStateException();
		}
	}

	protected void shadingFunction(PDFOutput sout, Color[] colors, double[] fractions) throws IOException {
		// TODO Alpha gradient
		sout.writeName("ColorSpace");
		Color.Type colorType;
		if (this.getPdfWriter().getParams().getColorMode() == PDFParams.ColorMode.GRAY) {
			colorType = Color.Type.GRAY;
		} else if (this.getPdfWriter().getParams().getColorMode() == PDFParams.ColorMode.CMYK) {
			colorType = Color.Type.CMYK;
		} else {
			colorType = colors[0].getColorType();
			for (int i = 1; i < colors.length; ++i) {
				if (colorType != colors[i].getColorType()) {
					colorType = Color.Type.RGB;
				}
			}
			if (colorType == Color.Type.RGBA) {
				colorType = Color.Type.RGB;
			}
		}
		switch (colorType) {
			case GRAY:
				sout.writeName("DeviceGray");
				break;
			case RGB:
				sout.writeName("DeviceRGB");
				break;
			case CMYK:
				sout.writeName("DeviceCMYK");
				break;
			default:
				throw new IllegalStateException();
		}
		sout.lineBreak();

		sout.writeName("Extend");
		sout.startArray();
		sout.writeBoolean(true);
		sout.writeBoolean(true);
		sout.endArray();
		sout.lineBreak();

		sout.writeName("Function");
		sout.startHash();
		if (colors.length <= 2
				&& (fractions == null || fractions.length == 0 || (fractions.length == 1 && fractions[0] == 0)
						|| (fractions.length == 2 && fractions[0] == 0 && fractions[1] == 1))) {
			// Simple case
			Color c0 = colors[0];
			Color c1 = colors[1];

			sout.writeName("FunctionType");
			sout.writeInt(2);
			sout.lineBreak();

			sout.writeName("Domain");
			sout.startArray();
			sout.writeReal(0.0);
			sout.writeReal(1.0);
			sout.endArray();
			sout.lineBreak();

			sout.writeName("N");
			sout.writeReal(1.0);
			sout.lineBreak();

			sout.writeName("C0");
			sout.startArray();
			writeColor(sout, colorType, c0);
			sout.endArray();
			sout.lineBreak();

			sout.writeName("C1");
			sout.startArray();
			writeColor(sout, colorType, c1);
			sout.endArray();
			sout.lineBreak();
		} else {
			// Complex case
			int segments = fractions.length - 1;
			if (fractions[0] != 0) {
				++segments;
			}
			if (fractions[fractions.length - 1] != 1) {
				++segments;
			}

			sout.writeName("FunctionType");
			sout.writeInt(3);
			sout.lineBreak();

			sout.writeName("Domain");
			sout.startArray();
			sout.writeReal(0.0);
			sout.writeReal(1.0);
			sout.endArray();
			sout.lineBreak();

			sout.writeName("Encode");
			sout.startArray();
			for (int i = 0; i < segments; ++i) {
				sout.writeReal(0.0);
				sout.writeReal(1.0);
			}
			sout.endArray();
			sout.lineBreak();

			sout.writeName("Bounds");
			sout.startArray();
			if (fractions[0] != 0) {
				sout.writeReal(fractions[0]);
			}
			for (int i = 1; i < fractions.length - 1; ++i) {
				sout.writeReal(fractions[i]);
			}
			if (fractions[fractions.length - 1] != 1) {
				sout.writeReal(fractions[fractions.length - 1]);
			}
			sout.endArray();
			sout.lineBreak();

			sout.writeName("Functions");
			sout.startArray();
			for (int i = -1; i < fractions.length; ++i) {
				Color c0, c1;
				if (i == -1) {
					if (fractions[0] != 0) {
						c0 = colors[0];
						c1 = colors[0];
					} else {
						continue;
					}
				} else if (i == fractions.length - 1) {
					if (fractions[i] != 1) {
						c0 = colors[i];
						c1 = colors[i];
					} else {
						break;
					}
				} else {
					c0 = colors[i];
					c1 = colors[i + 1];
				}

				sout.startHash();
				sout.writeName("FunctionType");
				sout.writeInt(2);
				sout.lineBreak();

				sout.writeName("Domain");
				sout.startArray();
				sout.writeReal(0.0);
				sout.writeReal(1.0);
				sout.endArray();
				sout.lineBreak();

				sout.writeName("N");
				sout.writeReal(1.0);
				sout.lineBreak();

				sout.writeName("C0");
				sout.startArray();
				writeColor(sout, colorType, c0);
				sout.endArray();
				sout.lineBreak();

				sout.writeName("C1");
				sout.startArray();
				writeColor(sout, colorType, c1);
				sout.endArray();
				sout.lineBreak();
				sout.endHash();
			}
			sout.endArray();
			sout.lineBreak();
		}
		sout.endHash();
		sout.lineBreak();
	}

	private static void writeColor(PDFOutput sout, Color.Type colorType, Color color) throws IOException {
		switch (colorType) {
			case GRAY:
				if (color.getColorType() == Color.Type.GRAY) {
					sout.writeReal(((GrayColor) color).getComponent(0));
					break;
				}
				sout.writeReal(ColorUtils.toGray(color.getRed(), color.getGreen(), color.getBlue()));
				break;
			case RGB:
				sout.writeReal(color.getRed());
				sout.writeReal(color.getGreen());
				sout.writeReal(color.getBlue());
				break;
			case CMYK:
				CMYKColor cmyk = ColorUtils.toCMYK(color);
				sout.writeReal(cmyk.getComponent(CMYKColor.C));
				sout.writeReal(cmyk.getComponent(CMYKColor.M));
				sout.writeReal(cmyk.getComponent(CMYKColor.Y));
				sout.writeReal(cmyk.getComponent(CMYKColor.K));
				break;
			default:
				throw new IllegalStateException();
		}
	}

	protected void applyStates() throws IOException {
		// Transform
		this.applyTransform();
		this.applyClip();

		// Stroke
		if (this.lineWidth != this.xlineWidth) {
			this.xlineWidth = this.lineWidth;
			this.out.writeReal(this.lineWidth);
			this.out.writeOperator("w");
		}
		if (this.lineCap != this.xlineCap) {
			this.xlineCap = this.lineCap;
			this.out.writeInt(this.lineCap.code);
			this.out.writeOperator("J");
		}
		if (this.lineJoin != this.xlineJoin) {
			this.xlineJoin = this.lineJoin;
			this.out.writeInt(this.lineJoin.j);
			this.out.writeOperator("j");
		}
		if (!Arrays.equals(this.linePattern, this.xlinePattern)) {
			this.xlinePattern = this.linePattern;
			this.out.startArray();
			if (this.linePattern != null) {
				for (int i = 0; i < this.linePattern.length; ++i) {
					this.out.writeReal(this.linePattern[i]);
				}
			}
			this.out.endArray();
			this.out.writeInt(0);
			this.out.writeOperator("d");
		}

		// Color
		if (this.strokePaint != null && !this.strokePaint.equals(this.xstrokePaint)) {
			switch (this.strokePaint.getPaintType()) {
				case COLOR:
					if (this.xstrokePaint != null && this.xstrokePaint.getPaintType() != Paint.Type.COLOR) {
						this.out.writeName("DeviceRGB");
						this.out.writeOperator("CS");
					}
					this.out.writeStrokeColor((Color) this.strokePaint);
					break;
				case PATTERN:
				case LINEAR_GRADIENT:
				case RADIAL_GRADIENT:
					String name = this.getPaintName(this.strokePaint);
					if (name != null) {
						this.out.writeName("Pattern");
						this.out.writeOperator("CS");
						this.out.useResource("Pattern", name);
						this.out.writeName(name);
						this.out.writeOperator("SCN");
					}
					break;
				default:
					throw new IllegalStateException();
			}
			this.xstrokePaint = this.strokePaint;
		}
		if (this.fillPaint != null && !this.fillPaint.equals(this.xfillPaint)) {
			switch (this.fillPaint.getPaintType()) {
				case COLOR:
					if (this.xfillPaint != null && this.xfillPaint.getPaintType() != Paint.Type.COLOR) {
						this.out.writeName("DeviceRGB");
						this.out.writeOperator("cs");
					}
					this.out.writeFillColor((Color) this.fillPaint);
					break;
				case PATTERN:
				case LINEAR_GRADIENT:
				case RADIAL_GRADIENT:
					String name = this.getPaintName(this.fillPaint);
					if (name != null) {
						this.out.writeName("Pattern");
						this.out.writeOperator("cs");
						this.out.useResource("Pattern", name);
						this.out.writeName(name);
						this.out.writeOperator("scn");
					}
					break;
				default:
					throw new IllegalStateException();
			}
			this.xfillPaint = this.fillPaint;
		}

		// Opacity
		boolean supportAlpha = this.pdfVersion.v >= PDFParams.Version.V_1_4.v
				&& this.pdfVersion.v != PDFParams.Version.V_PDFA1B.v
				&& this.pdfVersion.v != PDFParams.Version.V_PDFX1A.v;
		// When transparency is supported
		if ((supportAlpha && (this.strokeAlpha != this.xstrokeAlpha || this.fillAlpha != this.xfillAlpha))
				|| (this.strokeOverprint != this.xstrokeOverprint || this.fillOverprint != this.xfillOverprint)) {
			this.xstrokeAlpha = this.strokeAlpha;
			this.xfillAlpha = this.fillAlpha;
			this.xstrokeOverprint = this.strokeOverprint;
			this.xfillOverprint = this.fillOverprint;
			@SuppressWarnings("unchecked")
			Map<String, String> gs = (Map<String, String>) this.out.getPdfWriter().getAttribute("sfGs");
			if (gs == null) {
				gs = new HashMap<String, String>();
				this.out.getPdfWriter().putAttribute("sfGs", gs);
			}
			String key = (supportAlpha ? (this.strokeAlpha + "/" + this.fillAlpha) : "") + "/" + this.fillOverprint
					+ "/" + this.strokeOverprint;
			String name = (String) gs.get(key);
			try {
				if (name == null) {
					try (PDFNamedOutput gsOut = this.out.getPdfWriter().createSpecialGraphicsState()) {
						if (supportAlpha) {
							gsOut.writeName("CA");
							gsOut.writeReal(this.strokeAlpha);
							gsOut.writeName("ca");
							gsOut.writeReal(this.fillAlpha);
						}
						if (this.strokeOverprint != CMYKColor.OVERPRINT_NONE) {
							gsOut.writeName("OP");
							gsOut.writeBoolean(true);
							if (this.strokeOverprint == CMYKColor.OVERPRINT_ILLUSTRATOR) {
								gsOut.writeName("OPM");
								gsOut.writeInt(1);
							}
						}
						if (this.fillOverprint != CMYKColor.OVERPRINT_NONE) {
							gsOut.writeName("op");
							gsOut.writeBoolean(true);
							if (this.fillOverprint == CMYKColor.OVERPRINT_ILLUSTRATOR) {
								gsOut.writeName("opm");
								gsOut.writeInt(1);
							}
						}
						name = gsOut.getName();
						gs.put(key, name);
					}
				}
				this.out.useResource("ExtGState", name);
				this.out.writeName(name);
				this.out.writeOperator("gs");
			} catch (IOException e) {
				throw new GraphicsException(e);
			}
		}
	}

	/**
	 * If the current graphics state is applied for the first time,
	 * outputs the graphics context start instruction (q) and saves the current
	 * graphics state.
	 * 
	 * @throws IOException if an I/O error occurs
	 */
	private void gsave() throws IOException {
		if (this.stack.isEmpty()) {
			return;
		}
		GraphicsState state = (GraphicsState) this.stack.get(this.stack.size() - 1);
		if (state.gstate == null) {
			this.q();
			state.gstate = new XGraphicsState(this);
		}
	}

	/**
	 * If a previous graphics state is saved,
	 * outputs the graphics context end instruction (Q) and restores the graphics
	 * state.
	 * 
	 * @throws IOException if an I/O error occurs
	 */
	private void grestore() throws IOException {
		GraphicsState state = (GraphicsState) this.stack.get(this.stack.size() - 1);
		if (state.gstate != null) {
			this.Q();
			state.gstate.restore(this);
			state.gstate = null;
		}
	}

	private void q() throws IOException {
		++this.qDepth;
		if (this.pdfVersion.v == PDFParams.Version.V_PDFA1B.v) {
			if (this.qDepth > 28) {
				throw new IllegalStateException("PDF/A-1 cannot nest graphic states more than 28 levels.");
			}
		}
		this.out.writeOperator("q");
	}

	private void Q() throws IOException {
		--this.qDepth;
		this.out.writeOperator("Q");
	}

	protected void plotRect(Rectangle2D r) throws IOException {
		this.out.writeRect((double) r.getX(), (double) r.getY(), (double) r.getWidth(), (double) r.getHeight());
		this.out.writeOperator("re");
	}

	protected boolean plot(PathIterator i) throws IOException {
		double[] cord = new double[6];
		double sx = 0, sy = 0;
		while (!i.isDone()) {
			int type = i.currentSegment(cord);
			switch (type) {
				case PathIterator.SEG_MOVETO:
					this.out.writePosition(sx = cord[0], sy = cord[1]);
					this.out.writeOperator("m");
					break;
				case PathIterator.SEG_LINETO:
					this.out.writePosition(sx = cord[0], sy = cord[1]);
					this.out.writeOperator("l");
					break;
				case PathIterator.SEG_QUADTO:
					double cx = cord[0];
					double cy = cord[1];
					double ex = cord[2];
					double ey = cord[3];
					double x1 = (sx + 2.0 * cx) / 3.0;
					double y1 = (sy + 2.0 * cy) / 3.0;
					double x2 = (ex + 2.0 * cx) / 3.0;
					double y2 = (ey + 2.0 * cy) / 3.0;
					this.out.writePosition(x1, y1);
					this.out.writePosition(x2, y2);
					this.out.writePosition(sx = ex, sy = ey);
					this.out.writeOperator("c");
					break;
				case PathIterator.SEG_CUBICTO:
					this.out.writePosition(cord[0], cord[1]);
					this.out.writePosition(cord[2], cord[3]);
					this.out.writePosition(sx = cord[4], sy = cord[5]);
					this.out.writeOperator("c");
					break;
				case PathIterator.SEG_CLOSE:
					i.next();
					if (i.isDone()) {
						return true;
					}
					this.out.writeOperator("h");
					continue;
			}
			i.next();
		}
		return false;
	}

	public void close() throws IOException {
		this.out.close();
	}
}