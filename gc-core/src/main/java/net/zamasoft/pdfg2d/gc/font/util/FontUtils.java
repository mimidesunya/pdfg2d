package net.zamasoft.pdfg2d.gc.font.util;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

import net.zamasoft.pdfg2d.font.BBox;
import net.zamasoft.pdfg2d.font.DrawableFont;
import net.zamasoft.pdfg2d.font.FontSource;
import net.zamasoft.pdfg2d.font.ImageFont;
import net.zamasoft.pdfg2d.font.ShapedFont;
import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.GraphicsException;
import net.zamasoft.pdfg2d.gc.font.FontMetrics;
import net.zamasoft.pdfg2d.gc.font.FontStyle;
import net.zamasoft.pdfg2d.gc.text.Text;

/**
 * テキスト描画・フォント関連のユーティリティです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: FontUtils.java 1631 2022-05-15 05:43:49Z miyabe $
 */
public final class FontUtils {
	private FontUtils() {
		// unused
	}

	/**
	 * フォント名を大文字に統一し、ハイフン、スペースを除去します。
	 * 
	 * @param fontName
	 * @return
	 */
	public static String normalizeName(String fontName) {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < fontName.length(); ++i) {
			char ch = fontName.charAt(i);
			if (Character.isWhitespace(ch) || ch == '-' || ch == '_') {
				continue;
			}
			buff.append(Character.toUpperCase(ch));
		}
		return buff.toString();
	}

	public static boolean equals(FontStyle a, FontStyle b) {
		return a.getFamily().equals(b.getFamily()) && a.getSize() == b.getSize() && a.getStyle() == b.getStyle()
				&& a.getWeight() == b.getWeight() && a.getDirection() == b.getDirection()
				&& a.getPolicy().equals(b.getPolicy());
	}

	public static int hashCode(FontStyle fontStyle) {
		int h = fontStyle.getFamily().hashCode();
		long a = Double.doubleToLongBits(fontStyle.getSize());
		h = 31 * h + (int) (a ^ (a >>> 32));
		h = 31 * h + fontStyle.getStyle();
		h = 31 * h + fontStyle.getWeight();
		h = 31 * h + fontStyle.getDirection();
		h = 31 * h + fontStyle.getPolicy().hashCode();
		return h;
	}

	/**
	 * テキストをパスに追加します。絵文字は除外されます。
	 * 
	 * @param path
	 * @param font
	 * @param text
	 */
	public static void addTextPath(final GeneralPath path, ShapedFont font, Text text, AffineTransform transform) {
		FontStyle fontStyle = text.getFontStyle();
		byte direction = fontStyle.getDirection();
		double fontSize = fontStyle.getSize();
		int glen = text.getGLen();
		int[] gids = text.getGIDs();
		double letterSpacing = text.getLetterSpacing();
		double[] xadvances = text.getXAdvances(false);
		FontMetrics fm = text.getFontMetrics();
		
		double s = fontSize / FontSource.DEFAULT_UNITS_PER_EM;
		AffineTransform at = AffineTransform.getScaleInstance(s, s);
		
		boolean verticalFont = direction == FontStyle.DIRECTION_TB && font.getFontSource().getDirection() == direction;
		AffineTransform oblique = null;
		short style = fontStyle.getStyle();
		if (style != FontStyle.FONT_STYLE_NORMAL && !font.getFontSource().isItalic()) {
			// 自前でイタリックを再現する
			if (verticalFont) {
				oblique = AffineTransform.getShearInstance(0, 0.25);
			} else {
				oblique = AffineTransform.getShearInstance(-0.25, 0);
			}
		}

		if (verticalFont) {
			// 縦書きモード
			// 縦書き対応フォント
			at.preConcatenate(AffineTransform.getTranslateInstance(-fontSize / 2.0, fontSize * 0.88));
			int pgid = 0;
			for (int i = 0; i < glen; ++i) {
				int gid = gids[i];
				if (i > 0) {
					double dy = fm.getAdvance(pgid) + letterSpacing;
					dy -= fm.getKerning(pgid, gid);
					if (xadvances != null) {
						dy += xadvances[i];
					}
					at.preConcatenate(AffineTransform.getTranslateInstance(0, dy));
				}
				pgid = gid;
				Shape shape = ((ShapedFont) font).getShapeByGID(gid);
				if (shape != null) {
					AffineTransform at2 = new AffineTransform(transform);
					double width = (fontSize - fm.getWidth(gid)) / 2.0;
					if (width != 0) {
						at2.translate(width, 0);
					}
					at2.concatenate(at);
					if (oblique != null) {
						shape = oblique.createTransformedShape(shape);
					}
					path.append(shape.getPathIterator(at2), false);
				}
			}
		} else {
			// 横書き
			int pgid = 0;
			for (int i = 0; i < glen; ++i) {
				final int gid = gids[i];
				if (i > 0) {
					double dx = fm.getAdvance(pgid) + letterSpacing;
					if (i > 0) {
						dx -= fm.getKerning(pgid, gid);
					}
					if (xadvances != null) {
						dx += xadvances[i];
					}
					at.preConcatenate(AffineTransform.getTranslateInstance(dx, 0));
				}
				AffineTransform at2 = new AffineTransform(transform);
				Shape shape = ((ShapedFont) font).getShapeByGID(gid);
				if (shape != null) {
					at2.concatenate(at);
					if (oblique != null) {
						shape = oblique.createTransformedShape(shape);
					}
					path.append(shape.getPathIterator(at2), false);
				}
				pgid = gid;
			}
		}
	}

	/**
	 * テキストのアウトラインを直接描画します。
	 * 
	 * @param gc
	 * @param font
	 * @param text
	 */
	public static void drawText(GC gc, DrawableFont font, Text text) throws GraphicsException {
		gc.begin();
		FontStyle fontStyle = text.getFontStyle();
		byte direction = fontStyle.getDirection();
		double fontSize = fontStyle.getSize();
		int glen = text.getGLen();
		int[] gids = text.getGIDs();
		double letterSpacing = text.getLetterSpacing();
		double[] xadvances = text.getXAdvances(false);
		FontMetrics fm = text.getFontMetrics();
		AffineTransform at;
		{
			double s = fontSize / FontSource.DEFAULT_UNITS_PER_EM;
			at = AffineTransform.getScaleInstance(s, s);
		}

		short textMode = gc.getTextMode();
		double enlargement;
		short weight = fontStyle.getWeight();
		double xlineWidth = 0;
		Object xstrokePaint = null;
		if (textMode == GC.TEXT_MODE_FILL && weight >= 500 && font.getFontSource().getWeight() < 500) {
			// 自前でBOLDを再現する
			switch (weight) {
			case 500:
				enlargement = fontSize / 28.0;
				break;
			case 600:
				enlargement = fontSize / 24.0;
				break;
			case 700:
				enlargement = fontSize / 20.0;
				break;
			case 800:
				enlargement = fontSize / 16.0;
				break;
			case 900:
				enlargement = fontSize / 12.0;
				break;
			default:
				throw new IllegalStateException();
			}
			if (enlargement > 0) {
				textMode = GC.TEXT_MODE_FILL_STROKE;
				xlineWidth = gc.getLineWidth();
				gc.setLineWidth(enlargement);
				gc.setStrokePaint(gc.getFillPaint());
				xstrokePaint = gc.getStrokePaint();
			}
		} else {
			enlargement = 0;
		}

		boolean verticalFont = direction == FontStyle.DIRECTION_TB && font.getFontSource().getDirection() == direction;
		AffineTransform oblique = null;
		short style = fontStyle.getStyle();
		if (style != FontStyle.FONT_STYLE_NORMAL && !font.getFontSource().isItalic()) {
			// 自前でイタリックを再現する
			if (verticalFont) {
				oblique = AffineTransform.getShearInstance(0, 0.25);
			} else {
				oblique = AffineTransform.getShearInstance(-0.25, 0);
			}
		}

		GeneralPath path = null;
		if (verticalFont) {
			// 縦書きモード
			// 縦書き対応フォント
			gc.transform(AffineTransform.getTranslateInstance(-fontSize / 2.0, fontSize * 0.88));
			int pgid = 0;
			for (int i = 0; i < glen; ++i) {
				AffineTransform at2 = at;
				int gid = gids[i];
				if (i > 0) {
					double dy = fm.getAdvance(pgid) + letterSpacing;
					dy -= fm.getKerning(pgid, gid);
					if (xadvances != null) {
						dy += xadvances[i];
					}
					at.preConcatenate(AffineTransform.getTranslateInstance(0, dy));
				}
				pgid = gid;
				if (font instanceof ShapedFont) {
					Shape shape = ((ShapedFont) font).getShapeByGID(gid);
					if (shape != null) {
						double width = (fontSize - fm.getWidth(gid)) / 2.0;
						if (width != 0) {
							at2 = AffineTransform.getTranslateInstance(width, 0);
							at2.concatenate(at);
						}
						if (oblique != null) {
							shape = oblique.createTransformedShape(shape);
						}
						if (path == null) {
							path = new GeneralPath();
						}
						path.append(shape.getPathIterator(at2), false);
					}
				} else {
					((ImageFont) font).drawGlyphForGid(gc, gid, at2);
				}
			}
		} else {
			if (direction == FontStyle.DIRECTION_TB) {
				// 横倒し
				gc.transform(AffineTransform.getRotateInstance(Math.PI / 2.0));
				BBox bbox = font.getFontSource().getBBox();
				double dy = ((bbox.lly + bbox.ury) * fontSize / FontSource.DEFAULT_UNITS_PER_EM) / 2.0;
				gc.transform(AffineTransform.getTranslateInstance(0, dy));
			}
			// 横書き
			int pgid = 0;
			for (int i = 0; i < glen; ++i) {
				final int gid = gids[i];
				if (i > 0) {
					double dx = fm.getAdvance(pgid) + letterSpacing;
					if (i > 0) {
						dx -= fm.getKerning(pgid, gid);
					}
					if (xadvances != null) {
						dx += xadvances[i];
					}
					at.preConcatenate(AffineTransform.getTranslateInstance(dx, 0));
				}
				if (font instanceof ShapedFont) {
					Shape shape = ((ShapedFont) font).getShapeByGID(gid);
					if (shape != null) {
						if (oblique != null) {
							shape = oblique.createTransformedShape(shape);
						}
						if (path == null) {
							path = new GeneralPath();
						}
						path.append(shape.getPathIterator(at), false);
					}
				} else {
					((ImageFont) font).drawGlyphForGid(gc, gid, at);
				}
				pgid = gid;
			}
		}
		if (path != null) {
			drawPath(gc, path, textMode);
		}

		if (enlargement > 0) {
			gc.setLineWidth(xlineWidth);
			gc.setStrokePaint(xstrokePaint);
		}
		gc.end();
	}

	private static void drawPath(GC gc, GeneralPath path, short textMode) {
		switch (textMode) {
		case GC.TEXT_MODE_FILL:
			gc.fill(path);
			break;
		case GC.TEXT_MODE_STROKE:
			gc.draw(path);
			break;
		case GC.TEXT_MODE_FILL_STROKE:
			gc.fillDraw(path);
			break;
		default:
			throw new IllegalStateException();
		}
	}
}