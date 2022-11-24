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

/* PDF命令早見表
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
 * PDFグラフィックコンテキストです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class PDFGC implements GC, Closeable {
	private static final Logger LOG = Logger.getLogger(PDFGC.class.getName());

	private static final boolean DEBUG = false;

	protected final PDFGraphicsOutput out;

	/**
	 * 設定されたグラフィック状態を保存するためのオブジェクト。
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
	 * PDFのカレントのグラフィック状態を保存するためのオブジェクト。
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

	/**
	 * 使用する線の末端の形状。
	 */
	private LineCap lineCap = LineCap.SQUARE;

	/**
	 * PDFのカレントの線の末端の形状。
	 */
	private LineCap xlineCap = LineCap.SQUARE;

	/**
	 * 使用する線の接続部分の形状。
	 */
	private LineJoin lineJoin = LineJoin.MITER;

	/**
	 * PDFのカレントの線の接続部分の形状。
	 */
	private LineJoin xlineJoin = LineJoin.MITER;

	/**
	 * 使用する線の幅。
	 */
	private double lineWidth = 1;

	/**
	 * PDFのカレントの線の幅。
	 */
	private double xlineWidth = 1;

	/**
	 * 使用する線のパターン。
	 */
	private double[] linePattern = STROKE_SOLID;

	/**
	 * PDFのカレントの線のパターン。
	 */
	private double[] xlinePattern = STROKE_SOLID;

	/**
	 * 使用する線の色。
	 */
	private Paint strokePaint = GrayColor.BLACK;

	/**
	 * PDFのカレントの線の色。
	 */
	private Paint xstrokePaint = GrayColor.BLACK;

	/**
	 * 使用する塗りつぶし色。
	 */
	private Paint fillPaint = GrayColor.BLACK;

	/**
	 * PDFのカレントの塗りつぶし色。
	 */
	private Paint xfillPaint = GrayColor.BLACK;

	private double xletterSpacing = 0;

	/**
	 * テキストの描画方法。
	 */
	private TextMode textMode = TextMode.FILL;

	/**
	 * PDFのカレントのテキストの描画方法。
	 */
	private TextMode xtextMode = TextMode.FILL;

	/**
	 * 線の不透明度。
	 */
	public float strokeAlpha = 1;

	/**
	 * PDFのカレントの線の不透明度。
	 */
	public float xstrokeAlpha = 1;

	/**
	 * 塗りの不透明度。
	 */
	public float fillAlpha = 1;

	/**
	 * PDFのカレントの塗りの不透明度。
	 */
	public float xfillAlpha = 1;

	/**
	 * 線のオーバープリントモード。
	 */
	public byte strokeOverprint = 0;

	/**
	 * PDFのカレントの線のオーバープリントモード。
	 */
	public byte xstrokeOverprint = 0;

	/**
	 * 塗りのオーバープリントモード。
	 */
	public byte fillOverprint = 0;

	/**
	 * PDFのカレントの塗りのオーバープリントモード。
	 */
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

	public void setStrokePaint(Object paint) throws GraphicsException {
		if (DEBUG) {
			System.err.println("setStrokePaint: " + paint);
		}
		this.setPaint(paint, false);
	}

	public Object getStrokePaint() {
		return this.strokePaint;
	}

	public void setFillPaint(Object paint) throws GraphicsException {
		if (DEBUG) {
			System.err.println("setFillPaint: " + paint);
		}
		this.setPaint(paint, true);
	}

	public Object getFillPaint() {
		return this.fillPaint;
	}

	protected void setPaint(Object paint, boolean fill) throws GraphicsException {
		Paint p = (Paint) paint;
		if (fill) {
			this.fillPaint = p;
			this.fillAlpha = 1;
			this.fillOverprint = CMYKColor.OVERPRINT_NONE;
		} else {
			this.strokePaint = p;
			this.strokeAlpha = 1;
			this.strokeOverprint = CMYKColor.OVERPRINT_NONE;
		}
		if (p.getPaintType() == Paint.COLOR) {
			Color color = (Color) p;
			switch (color.getColorType()) {
			case Color.RGBA:
				if (fill) {
					this.fillAlpha = color.getAlpha();
				} else {
					this.strokeAlpha = color.getAlpha();
				}
				break;
			case Color.CMYK:
				if (fill) {
					this.fillOverprint = ((CMYKColor) color).getOverprint();
				} else {
					this.strokeOverprint = ((CMYKColor) color).getOverprint();
				}
				break;
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
		if (text.getGLen() <= 0) {
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
				this.begin();
				this.transform(AffineTransform.getTranslateInstance(x, y));
				FontUtils.drawText(this, (DrawableFont) font, text);
				this.end();
				return;
			}
		}

		assert text.getCLen() > 0;
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
					throw new IllegalStateException("PDF/A-1またはPDF/X-1aで埋め込みフォント以外は使用できません。");
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
				// 自前でBOLDを再現する
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
				if (enlargement > 0 && this.fillPaint.getPaintType() == Paint.COLOR && this.fillAlpha == 1) {
					this.q();
					localContext = true;
					this.out.writeReal(enlargement);
					this.out.writeOperator("w");
					this.out.writeInt(TextMode.FILL_STROKE.code);
					this.out.writeOperator("Tr");
					if (!this.fillPaint.equals(this.strokePaint)) {
						if (this.xstrokePaint != null && this.xstrokePaint.getPaintType() != Paint.COLOR) {
							this.out.writeName("DeviceRGB");
							this.out.writeOperator("CS");
						}
						this.out.writeStrokeColor((Color) this.fillPaint);
					}
				}
			} else {
				enlargement = 0;
			}

			// 描画方向
			Direction direction = fontStyle.getDirection();
			AffineTransform rotate = null;
			double center = 0;
			boolean verticalFont = false;
			switch (direction) {
			case LTR:
			case RTL:// TODO RTL
				// 横書き
				break;
			case TB:
				// 縦書き
				if (source.getDirection() == direction) {
					// 縦組み
					verticalFont = true;
				} else {
					// ９０度回転横組み
					if (!localContext) {
						this.q();
						localContext = true;
					}
					rotate = AffineTransform.getRotateInstance(Math.PI / 2, x, y);
					this.out.writeTransform(rotate);
					this.out.writeOperator("cm");
					BBox bbox = source.getBBox();
					center = ((bbox.lly + bbox.ury) * size / FontSource.DEFAULT_UNITS_PER_EM) / 2.0;
					y += center;
				}
				break;
			default:
				throw new IllegalStateException();
			}

			// テキスト開始
			this.out.writeOperator("BT");

			// イタリック
			Style style = fontStyle.getStyle();
			if (style != Style.NORMAL && !source.isItalic()) {
				// 自前でイタリックを再現する
				if (verticalFont) {
					// 縦書きイタリック
					this.out.writeReal(1);
					this.out.writeReal(-0.25);
					this.out.writeReal(0);
					this.out.writeReal(1);
					this.out.writePosition(x, y);
					this.out.writeOperator("Tm");
				} else {
					// 横書きイタリック
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

			// フォント名とサイズ
			String name = ((PDFFont) font).getName();
			this.out.useResource("Font", name);
			this.out.writeName(name);
			this.out.writeReal(size);
			this.out.writeOperator("Tf");

			// // 字間
			double letterSpacing = text.getLetterSpacing();
			// 縦書きでは負の値を使う(SPEC PDF1.3 8.7.1.1)
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

			// 描画
			font.drawTo(this, text);

			// テキスト終了
			this.out.writeOperator("ET");

			if (enlargement > 0 && this.fillPaint.getPaintType() == Paint.COLOR && this.fillAlpha == 1) {
				// Bold終了
				this.out.writeInt(TextMode.FILL.code);
				this.out.writeOperator("Tr");
				if (!this.fillPaint.equals(this.strokePaint)) {
					if (this.xfillPaint != null && this.xfillPaint.getPaintType() != Paint.COLOR) {
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
		case Paint.PATTERN: {
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
		case Paint.LINEAR_GRADIENT: {
			// PDF Axial(Type 2) Shading
			if (this.pdfVersion.v < PDFParams.Version.V_1_3.v) {
				return null;
			}
			LinearGradient gradient = (LinearGradient) paint;

			Color[] colors = gradient.getColors();
			double[] fractions = gradient.getFractions();

			AffineTransform at = this.getTransform();
			if (at == null) {
				at = gradient.getTransform();
			} else if (gradient.getTransform() != null) {
				at.concatenate(gradient.getTransform());
			}

			PDFGraphicsOutput pout = (PDFGraphicsOutput) this.out;
			try (PDFNamedOutput sout = pout.getPdfWriter().createShadingPattern(pout.getHeight(), at)) {
				sout.writeName("ShadingType");
				sout.writeInt(2);
				sout.lineBreak();

				sout.writeName("Coords");
				sout.startArray();
				sout.writeReal(gradient.getX1());
				sout.writeReal(gradient.getY1());
				sout.writeReal(gradient.getX2());
				sout.writeReal(gradient.getY2());
				sout.endArray();
				sout.lineBreak();
				this.shadingFunction(sout, colors, fractions);

				return sout.getName();
			} catch (IOException e) {
				throw new GraphicsException(e);
			}
		}
		case Paint.RADIAL_GRADIENT: {
			// PDF Radial(Type 3) Shading
			if (this.pdfVersion.v < PDFParams.Version.V_1_3.v) {
				return null;
			}
			RadialGradient gp = (RadialGradient) paint;

			Color[] colors = gp.getColors();
			double[] fractions = gp.getFractions();
			double radius = gp.getRadius();

			AffineTransform at = this.getTransform();
			if (at == null) {
				at = gp.getTransform();
			} else if (gp.getTransform() != null) {
				at.concatenate(gp.getTransform());
			}

			double dx = gp.getFX() - gp.getCX();
			double dy = gp.getFY() - gp.getCY();
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
				sout.writeReal(gp.getCX() + dx);
				sout.writeReal(gp.getCY() + dy);
				sout.writeReal(0);
				sout.writeReal(gp.getCX());
				sout.writeReal(gp.getCY());
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
		// TODO alphaグラデーション
		sout.writeName("ColorSpace");
		short colorType;
		if (this.getPdfWriter().getParams().getColorMode() == PDFParams.ColorMode.GRAY) {
			colorType = Color.GRAY;
		} else if (this.getPdfWriter().getParams().getColorMode() == PDFParams.ColorMode.CMYK) {
			colorType = Color.CMYK;
		} else {
			colorType = colors[0].getColorType();
			for (int i = 1; i < colors.length; ++i) {
				if (colorType != colors[i].getColorType()) {
					colorType = Color.RGB;
				}
			}
			if (colorType == Color.RGBA) {
				colorType = Color.RGB;
			}
		}
		switch (colorType) {
		case Color.GRAY:
			sout.writeName("DeviceGray");
			break;
		case Color.RGB:
			sout.writeName("DeviceRGB");
			break;
		case Color.CMYK:
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
			// 単純な場合
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
			// 複雑な場合
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

	private static void writeColor(PDFOutput sout, short colorType, Color color) throws IOException {
		switch (colorType) {
		case Color.GRAY:
			if (color.getColorType() == Color.GRAY) {
				sout.writeReal(((GrayColor) color).getComponent(0));
				break;
			}
			sout.writeReal(ColorUtils.toGray(color.getRed(), color.getGreen(), color.getBlue()));
			break;
		case Color.RGB:
			sout.writeReal(color.getRed());
			sout.writeReal(color.getGreen());
			sout.writeReal(color.getBlue());
			break;
		case Color.CMYK:
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
		// 変換
		this.applyTransform();
		this.applyClip();

		// ストローク
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

		// 色
		if (this.strokePaint != null && !this.strokePaint.equals(this.xstrokePaint)) {
			switch (this.strokePaint.getPaintType()) {
			case Paint.COLOR:
				if (this.xstrokePaint != null && this.xstrokePaint.getPaintType() != Paint.COLOR) {
					this.out.writeName("DeviceRGB");
					this.out.writeOperator("CS");
				}
				this.out.writeStrokeColor((Color) this.strokePaint);
				break;
			case Paint.PATTERN:
			case Paint.LINEAR_GRADIENT:
			case Paint.RADIAL_GRADIENT:
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
			case Paint.COLOR:
				if (this.xfillPaint != null && this.xfillPaint.getPaintType() != Paint.COLOR) {
					this.out.writeName("DeviceRGB");
					this.out.writeOperator("cs");
				}
				this.out.writeFillColor((Color) this.fillPaint);
				break;
			case Paint.PATTERN:
			case Paint.LINEAR_GRADIENT:
			case Paint.RADIAL_GRADIENT:
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

		// 不透明度
		boolean supportAlpha = this.pdfVersion.v >= PDFParams.Version.V_1_4.v
				&& this.pdfVersion.v != PDFParams.Version.V_PDFA1B.v
				&& this.pdfVersion.v != PDFParams.Version.V_PDFX1A.v;
		// 透明化処理がサポートされる場合。
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
	 * 現在のグラフィック状態が最初に適用された場合、 グラフィックコンテキスト開始命令(q)を出力し、現在のグラフィック状態を保存します。
	 * 
	 * @throws IOException
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
	 * 以前のグラフィック状態が保存されている場合、 グラフィックコンテキスト終了命令(Q)を出力し、現在のグラフィック状態を復帰します。
	 * 
	 * @throws IOException
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
				throw new IllegalStateException("PDF/A-1ではグラフィックステートを28以上入れ子にできません。");
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