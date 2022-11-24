package net.zamasoft.pdfg2d.g2d.util;

import java.awt.Canvas;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.MediaTracker;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.batik.ext.awt.LinearGradientPaint;
import org.apache.batik.ext.awt.MultipleGradientPaint;
import org.apache.batik.ext.awt.RadialGradientPaint;

import net.zamasoft.pdfg2d.g2d.gc.G2DGC;
import net.zamasoft.pdfg2d.g2d.image.RasterImageImpl;
import net.zamasoft.pdfg2d.g2d.image.png.PNGDecodeParam;
import net.zamasoft.pdfg2d.g2d.image.png.PNGImage;
import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.GC.LineCap;
import net.zamasoft.pdfg2d.gc.GC.LineJoin;
import net.zamasoft.pdfg2d.gc.font.FontFamily;
import net.zamasoft.pdfg2d.gc.font.FontStyle;
import net.zamasoft.pdfg2d.gc.font.util.FontUtils;
import net.zamasoft.pdfg2d.gc.image.Image;
import net.zamasoft.pdfg2d.gc.paint.Color;
import net.zamasoft.pdfg2d.gc.paint.LinearGradient;
import net.zamasoft.pdfg2d.gc.paint.Paint;
import net.zamasoft.pdfg2d.gc.paint.Pattern;
import net.zamasoft.pdfg2d.gc.paint.RGBAColor;
import net.zamasoft.pdfg2d.gc.paint.RGBColor;
import net.zamasoft.pdfg2d.gc.paint.RadialGradient;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public final class G2DUtils {
	private static final Logger LOGGER = Logger.getLogger(G2DUtils.class.getName());

	private G2DUtils() {
		// unused
	}

	public static Paint fromAwtPaint(java.awt.Paint paint) {
		if (paint instanceof java.awt.Color) {
			return fromAwtColor((java.awt.Color) paint);
		}
		if (paint instanceof GradientPaint) {
			GradientPaint gpaint = (GradientPaint) paint;
			if (gpaint.isCyclic()) {
				return null;
			}
			return new LinearGradient(gpaint.getPoint1().getX(), gpaint.getPoint1().getY(), gpaint.getPoint2().getX(),
					gpaint.getPoint2().getY(), new double[] { 0, 1 },
					new Color[] { fromAwtColor(gpaint.getColor1()), fromAwtColor(gpaint.getColor2()) },
					new AffineTransform());
		}
		if (paint instanceof RadialGradientPaint) {
			RadialGradientPaint gpaint = (RadialGradientPaint) paint;
			float[] fs = gpaint.getFractions();
			double[] fractions = new double[fs.length];
			for (int i = 0; i < fs.length; ++i) {
				fractions[i] = fs[i];
			}
			java.awt.Color[] cs = gpaint.getColors();
			Color[] colors = new Color[cs.length];
			for (int i = 0; i < cs.length; ++i) {
				colors[i] = fromAwtColor(cs[i]);
			}
			return new RadialGradient(gpaint.getCenterPoint().getX(), gpaint.getCenterPoint().getY(),
					gpaint.getRadius(), gpaint.getFocusPoint().getX(), gpaint.getFocusPoint().getY(), fractions, colors,
					gpaint.getTransform());
		}
		if (paint instanceof LinearGradientPaint) {
			LinearGradientPaint gpaint = (LinearGradientPaint) paint;
			if (gpaint.getCycleMethod() != LinearGradientPaint.NO_CYCLE) {
				return null;
			}
			float[] fs = gpaint.getFractions();
			double[] fractions = new double[fs.length];
			for (int i = 0; i < fs.length; ++i) {
				fractions[i] = fs[i];
			}
			java.awt.Color[] cs = gpaint.getColors();
			Color[] colors = new Color[cs.length];
			for (int i = 0; i < cs.length; ++i) {
				colors[i] = fromAwtColor(cs[i]);
			}
			return new LinearGradient(gpaint.getStartPoint().getX(), gpaint.getStartPoint().getY(),
					gpaint.getEndPoint().getX(), gpaint.getEndPoint().getY(), fractions, colors, gpaint.getTransform());
		}
		if (paint instanceof TexturePaint) {
			TexturePaint tpaint = (TexturePaint) paint;
			Rectangle2D r = tpaint.getAnchorRect();
			AffineTransform at = AffineTransform.getTranslateInstance(r.getX(), r.getY());
			BufferedImage image = tpaint.getImage();
			at.scale(r.getWidth() / image.getWidth(), r.getHeight() / image.getHeight());
			return new Pattern(new RasterImageImpl(image), at);
		}
		return null;
	}

	public static java.awt.Paint toAwtPaint(LinearGradient gradient) {
		double[] fs = gradient.getFractions();
		float[] fractions = new float[fs.length];
		for (int i = 0; i < fs.length; ++i) {
			fractions[i] = (float) fs[i];
		}
		Color[] cs = gradient.getColors();
		java.awt.Color[] colors = new java.awt.Color[cs.length];
		for (int i = 0; i < cs.length; ++i) {
			colors[i] = toAwtColor(cs[i]);
		}
		return new LinearGradientPaint(new Point2D.Double(gradient.getX1(), gradient.getY1()),
				new Point2D.Double(gradient.getX2(), gradient.getY2()), fractions, colors,
				MultipleGradientPaint.NO_CYCLE, MultipleGradientPaint.SRGB, gradient.getTransform());
	}

	public static java.awt.Paint toAwtPaint(RadialGradient gradient) {
		double[] fs = gradient.getFractions();
		float[] fractions = new float[fs.length];
		for (int i = 0; i < fs.length; ++i) {
			fractions[i] = (float) fs[i];
		}
		Color[] cs = gradient.getColors();
		java.awt.Color[] colors = new java.awt.Color[cs.length];
		for (int i = 0; i < cs.length; ++i) {
			colors[i] = toAwtColor(cs[i]);
		}
		return new RadialGradientPaint(new Point2D.Double(gradient.getCX(), gradient.getCY()),
				(float) gradient.getRadius(), new Point2D.Double(gradient.getFY(), gradient.getFX()), fractions, colors,
				MultipleGradientPaint.NO_CYCLE, MultipleGradientPaint.SRGB, gradient.getTransform());
	}

	public static Color fromAwtColor(java.awt.Color color) {
		float r = (short) color.getRed() / 255f;
		float g = (short) color.getGreen() / 255f;
		float b = (short) color.getBlue() / 255f;
		if (color.getAlpha() == 255) {
			return RGBColor.create(r, g, b);
		}
		float a = (short) color.getAlpha() / 255f;
		return RGBAColor.create(r, g, b, a);
	}

	public static java.awt.Color toAwtColor(Color color) {
		return new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}

	public static java.awt.Paint toAwtPaint(Pattern pattern, GC gc) {
		Image image = pattern.getImage();
		double width = image.getWidth();
		double height = image.getHeight();
		BufferedImage bimage;
		if (image instanceof RasterImageImpl) {
			bimage = (BufferedImage) ((RasterImageImpl) image).getImage();
		} else {
			bimage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D bg = (Graphics2D) bimage.getGraphics();
			image.drawTo(new G2DGC(bg, gc.getFontManager()));
		}
		return new TexturePaint(bimage, new Rectangle2D.Double(0, 0, width, height));
	}

	public static void drawImage(Graphics2D g, BufferedImage image, double x, double y, double w, double h) {
		g.drawImage(image, new AffineTransform(w / image.getWidth(), 0, 0, h / image.getHeight(), x, y), null);
	}

	public static final String toAwtFamilyName(FontFamily ffe) {
		switch (ffe.getGenericFamily()) {
		case CURSIVE:
			return "SansSerif";// AWT doesn't support logical font family
		// 'cursive'.
		case FANTASY:
			return "SansSerif";// AWT doesn't support logical font family
		// 'fantasy'.
		case MONOSPACE:
			return "Monospaced";
		case SANS_SERIF:
			return "SansSerif";
		case SERIF:
			return "Serif";

		default:
			return ffe.getName();
		}

	}

	public static final Font[] toFonts(FontStyle fontStyle) {
		Map<TextAttribute, Object> atts = new HashMap<TextAttribute, Object>();
		setFontAttributes(atts, fontStyle);
		Font[] fonts = new Font[fontStyle.getFamily().getLength()];
		for (int i = 0; i < fonts.length; ++i) {
			atts.put(TextAttribute.FAMILY, G2DUtils.toAwtFamilyName(fontStyle.getFamily().get(i)));
			fonts[i] = new Font(atts);
		}
		return fonts;
	}

	public static final void setFontAttributes(Map<TextAttribute, Object> atts, FontStyle fontStyle) {
		atts.put(TextAttribute.SIZE, Float.valueOf((float) fontStyle.getSize()));

		Float weight;
		switch (fontStyle.getWeight()) {
		case W_100:
			weight = TextAttribute.WEIGHT_EXTRA_LIGHT;
			break;
		case W_200:
			weight = TextAttribute.WEIGHT_LIGHT;
			break;
		case W_300:
			weight = TextAttribute.WEIGHT_DEMILIGHT;
			break;
		case W_400:
			weight = TextAttribute.WEIGHT_REGULAR;
			break;
		case W_500:
			weight = TextAttribute.WEIGHT_SEMIBOLD;
			break;
		case W_600:
			weight = TextAttribute.WEIGHT_DEMIBOLD;
			break;
		case W_700:
			weight = TextAttribute.WEIGHT_BOLD;
			break;
		case W_800:
			weight = TextAttribute.WEIGHT_EXTRABOLD;
			break;
		case W_900:
			weight = TextAttribute.WEIGHT_ULTRABOLD;
			break;
		default:
			throw new IllegalStateException();
		}
		atts.put(TextAttribute.WEIGHT, weight);

		Float posture;
		switch (fontStyle.getStyle()) {
		case NORMAL:
			posture = TextAttribute.POSTURE_REGULAR;
			break;
		case ITALIC:
		case OBLIQUE:
			posture = TextAttribute.POSTURE_OBLIQUE;
			break;
		default:
			throw new IllegalStateException();
		}
		atts.put(TextAttribute.POSTURE, posture);
	}

	private static Map<String, String> normNameToAWTName = null;

	private static void buildNormNameToAWTName() {
		if (normNameToAWTName == null) {
			normNameToAWTName = new HashMap<String, String>();
			Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
			for (int i = 0; i < fonts.length; ++i) {
				String name = fonts[i].getFontName();
				LOGGER.fine(name);
				normNameToAWTName.put(FontUtils.normalizeName(name), name);
			}
			normNameToAWTName = Collections.unmodifiableMap(normNameToAWTName);
		}
	}

	public static synchronized boolean isAvailable(String fontName) {
		buildNormNameToAWTName();
		return normNameToAWTName.containsKey(FontUtils.normalizeName(fontName));
	}

	public static synchronized String toAwtFontName(String fontName) {
		buildNormNameToAWTName();
		return (String) normNameToAWTName.get(FontUtils.normalizeName(fontName));
	}

	public static BufferedImage loadImage(ImageReader reader, ImageInputStream imageIn) throws IOException {
		try {
			String type = reader.getFormatName();
			BufferedImage buffer = null;
			if (type.equalsIgnoreCase("png")) {
				// 独自のPNGデコーダを使う
				try {
					RenderedImage rimage = new PNGImage(new ImageInputStreamWrapper(imageIn), new PNGDecodeParam());
					buffer = new BufferedImage(rimage.getWidth(), rimage.getHeight(), BufferedImage.TYPE_INT_ARGB);
					((Graphics2D) buffer.getGraphics()).drawRenderedImage(rimage, new AffineTransform());
				} catch (Throwable e) {
					imageIn.seek(0);
				}
			}
			if (buffer == null) {
				try {
					// ImageIOを使う

					// HACK JFIFが色化けするJavaのバグへの対策
					if ("JPEG".equalsIgnoreCase(reader.getFormatName())) {
						for (int i = 0; i < 100; ++i) {
							if (imageIn.read() == 0xFF) {
								if (imageIn.read() == 0xD8) {
									if (imageIn.read() == 0xFF) {
										if (imageIn.read() == 0xDB) {
											throw new Exception("読み込めないJPEGです");
										}
									}
									break;
								}
							}
						}
						imageIn.seek(0);
					}

					reader.setInput(imageIn);
					buffer = reader.read(0);
				} catch (Throwable e1) {
					LOGGER.log(Level.FINE, "loadImage", e1);

					// Toolkitを使う
					try {
						imageIn.seek(0);
						ByteArrayOutputStream out = new ByteArrayOutputStream();
						byte[] buff = new byte[8192];
						for (int len = imageIn.read(buff); len != -1; len = imageIn.read(buff)) {
							out.write(buff, 0, len);
						}
						java.awt.Image image = Toolkit.getDefaultToolkit().createImage(out.toByteArray());
						MediaTracker tracker = new MediaTracker(new Canvas());
						tracker.addImage(image, 0);
						tracker.waitForAll();
						buffer = new BufferedImage(image.getWidth(null), image.getHeight(null),
								BufferedImage.TYPE_INT_ARGB);
						buffer.getGraphics().drawImage(image, 0, 0, null);
					} catch (IOException ioe) {
						throw ioe;
					} catch (Throwable e2) {
						IOException ioe = new IOException(e2.getMessage());
						ioe.initCause(e2);
						throw ioe;
					}
				}
			}
			return buffer;
		} finally {
			reader.dispose();
		}
	}

	public static LineCap decodeLineCap(final short lineCap) {
		switch (lineCap) {
		case 0:
			return LineCap.BUTT;
		case 1:
			return LineCap.ROUND;
		case 2:
			return LineCap.SQUARE;
		default:
			throw new IllegalStateException();
		}
	}

	public static LineJoin decodeLineJoin(final short lineJoin) {
		switch (lineJoin) {
		case 0:
			return LineJoin.MITER;
		case 1:
			return LineJoin.ROUND;
		case 2:
			return LineJoin.BEVEL;
		default:
			throw new IllegalStateException();
		}
	}
}

class ImageInputStreamWrapper extends InputStream {
	private final ImageInputStream in;

	public ImageInputStreamWrapper(ImageInputStream in) throws IOException {
		this.in = in;
		this.in.seek(0);
	}

	public int read() throws IOException {
		return this.in.read();
	}

	public int read(byte[] buff, int off, int len) throws IOException {
		return this.in.read(buff, off, len);
	}

	public int read(byte[] buff) throws IOException {
		return this.in.read(buff);
	}
}