package net.zamasoft.pdfg2d.pdf.font.util;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.zamasoft.pdfg2d.font.BBox;
import net.zamasoft.pdfg2d.font.FontSource;
import net.zamasoft.pdfg2d.g2d.util.G2DUtils;
import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.GraphicsException;
import net.zamasoft.pdfg2d.gc.font.FontMetrics;
import net.zamasoft.pdfg2d.gc.font.FontStyle;
import net.zamasoft.pdfg2d.gc.text.Text;
import net.zamasoft.pdfg2d.pdf.PDFGraphicsOutput;

/**
 * フォント関連のユーティリティです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class PDFFontUtils {
	private static final Logger LOG = Logger.getLogger(PDFFontUtils.class.getName());
	private static final FontRenderContext FRC = new FontRenderContext(null, true, true);

	private static final boolean JDK1_5;

	static {
		JDK1_5 = "1.5".equals(System.getProperty("java.specification.version"));
	}

	private PDFFontUtils() {
		// unused
	}

	/**
	 * 代替のAWTフォントを返します。
	 * 
	 * @param source
	 * @return
	 */
	public static Font toAwtFont(FontSource source) {
		Map<TextAttribute, String> atts = new HashMap<TextAttribute, String>();
		String fontName = source.getFontName();
		String awtName = null;
		if (!G2DUtils.isAvailable(fontName)) {
			String[] aliases = source.getAliases();
			for (int i = 0; i < aliases.length; ++i) {
				String alias = aliases[i];
				if (G2DUtils.isAvailable(alias)) {
					awtName = G2DUtils.toAwtFontName(alias);
					break;
				}
			}
			if (awtName == null) {
				awtName = fontName;
			}
		} else {
			awtName = G2DUtils.toAwtFontName(fontName);
		}
		atts.put(TextAttribute.FAMILY, awtName);
		return new Font(atts);
	}

	/**
	 * PDFグラフィックコンテキストにCIDテキストを描画します。
	 * 
	 * @param out
	 * @param text
	 * @throws IOException
	 */
	public static void drawCIDTo(PDFGraphicsOutput out, Text text, boolean verticalFont) throws IOException {
		int[] gids = text.getGIDs();
		int glen = text.getGLen();
		double[] xadvances = text.getXAdvances(false);
		FontMetrics fm = text.getFontMetrics();
		out.startArray();
		int len = 0;
		int off = 0;
		double size = fm.getFontSize();
		for (int i = 0; i < glen; ++i) {
			double xadvance = xadvances == null ? 0 : xadvances[i];
			if (i > 0) {
				xadvance -= fm.getKerning(gids[i - 1], gids[i]);
			}
			if (xadvance != 0) {
				if (verticalFont) {
					xadvance = -xadvance;
				}
				if (len > 0) {
					out.writeBytes16(gids, off, len);
					off += len;
					len = 0;
				}
				double kerning = -xadvance * FontSource.DEFAULT_UNITS_PER_EM / size;
				out.writeReal(kerning);
			}
			++len;
		}
		if (len > 0) {
			out.writeBytes16(gids, off, len);
		}
		out.endArray();
		out.writeOperator("TJ");
	}

	/**
	 * AWTフォントを描画します。
	 * 
	 * @param gc
	 * @param fontSource
	 * @param awtFont
	 * @param text
	 */
	public static void drawAwtFont(GC gc, FontSource fontSource, Font awtFont, Text text) throws GraphicsException {
		byte direction = text.getFontStyle().getDirection();
		double fontSize = text.getFontStyle().getSize();
		Map<TextAttribute, Object> atts = new HashMap<TextAttribute, Object>();
		G2DUtils.setFontAttributes(atts, text.getFontStyle());
		awtFont = awtFont.deriveFont(atts);
		int glen = text.getGLen();
		int[] gids = text.getGIDs();
		byte[] clens = text.getCLens();
		char[] chars = text.getChars();
		double letterSpacing = text.getLetterSpacing();
		double[] xadvances = text.getXAdvances(false);
		FontMetrics fm = text.getFontMetrics();
		short textMode = gc.getTextMode();

		if (direction == FontStyle.DIRECTION_TB) {
			// 横倒し
			gc.transform(AffineTransform.getRotateInstance(Math.PI / 2.0));
			BBox bbox = fontSource.getBBox();
			gc.transform(AffineTransform.getTranslateInstance(0,
					((bbox.lly + bbox.ury) * fontSize / FontSource.DEFAULT_UNITS_PER_EM) / 2f));
		}
		// 横書き
		int pgid = 0;
		for (int i = 0, k = 0; i < glen; ++i) {
			int gid = gids[i];
			byte gclen = clens[i];
			try {
				GlyphVector gv;
				if (JDK1_5) {
					// JDK1.5のバグへの対応 #6266084
					gv = awtFont.layoutGlyphVector(FRC, chars, k, gclen, Font.LAYOUT_LEFT_TO_RIGHT);
				} else {
					gv = awtFont.layoutGlyphVector(FRC, chars, k, k + gclen, Font.LAYOUT_LEFT_TO_RIGHT);
				}
				Shape s = gv.getOutline();
				switch (textMode) {
				case GC.TEXT_MODE_FILL:
					gc.fill(s);
					break;
				case GC.TEXT_MODE_STROKE:
					gc.draw(s);
					break;
				case GC.TEXT_MODE_FILL_STROKE:
					gc.fill(s);
					gc.draw(s);
					break;
				default:
					throw new IllegalStateException();
				}
			} catch (Exception e) {
				LOG.log(Level.WARNING, new String(chars) + "/k=" + k + "/gclen=" + gclen + "/clen=" + text.getCLen(),
						e);
			}
			double dx = fm.getAdvance(gid) + letterSpacing;
			if (i > 0) {
				dx -= fm.getKerning(pgid, gid);
			}
			if (xadvances != null) {
				dx += xadvances[i];
			}
			gc.transform(AffineTransform.getTranslateInstance(dx, 0));
			k += gclen;
			pgid = gid;
		}
	}
}
