package net.zamasoft.font.truetype;

import java.awt.geom.GeneralPath;
import java.lang.ref.SoftReference;

import net.zamasoft.font.Glyph;
import net.zamasoft.font.GlyphList;
import net.zamasoft.font.table.GlyfTable;
import net.zamasoft.font.table.HeadTable;
import net.zamasoft.font.table.MaxpTable;

public class TrueTypeGlyphList implements GlyphList {

	private final HeadTable head;

	private final GlyfTable glyf;

	private SoftReference<Glyph>[] glyphs;

	@SuppressWarnings("unchecked")
	public TrueTypeGlyphList(GlyfTable glyf, HeadTable head, MaxpTable maxp) {
		this.head = head;
		this.glyf = glyf;
		this.glyphs = new SoftReference[maxp.getNumGlyphs()];
	}

	public synchronized Glyph getGlyph(int ix) {
		if (ix >= this.glyphs.length) {
			return null;
		}
		Glyph glyph = this.glyphs[ix] == null ? null : this.glyphs[ix].get();
		if (glyph != null) {
			return glyph;
		}

		GlyfDescript gd = this.glyf.getDescription(ix);
		if (gd == null) {
			return null;
		}
		short upm = this.head.getUnitsPerEm();
		GeneralPath path = new GeneralPath();
		int count = 0, endIndex = 0;
		float[] x = new float[5], y = new float[5];
		boolean[] onCurve = new boolean[5];
		int scount = 0;
		float[] sx = new float[2], sy = new float[2];
		boolean[] sonCurve = new boolean[2];
		boolean first = true;

		for (int i = 0; i < gd.getPointCount(); i++) {
			if (count < 0) {
				count = 0;
			}

			x[count] = gd.getXCoordinate(i) * 1000f / upm;
			y[count] = -(gd.getYCoordinate(i) * 1000f / upm);
			onCurve[count] = (gd.getFlags(i) & GlyfDescript.onCurve) != 0;
			if (scount <= 1) {
				sx[scount] = x[count];
				sy[scount] = y[count];
				sonCurve[scount] = onCurve[count];
				++scount;
			}
			++count;

			// 曲線の終了
			boolean end = gd.getEndPtOfContours(endIndex) == i;
			if (end) {
				for (int j = 0; j < scount; ++j) {
					x[count + j] = sx[j];
					y[count + j] = sy[j];
					onCurve[count + j] = sonCurve[j];
				}
				scount = 0;
			}
			do {
				if (!end && count < 2) {
					break;
				}
				// 2点接続
				if (onCurve[0] && onCurve[1]) {
					if (first) {
						// 最初の移動
						path.moveTo(x[0], y[0]);
						first = false;
					}
					path.lineTo(x[1], y[1]);
				} else if (!onCurve[0] && !onCurve[1]) {
					if (first) {
						// 最初の移動
						path.moveTo(midValue(x[0], x[1]), midValue(y[0], y[1]));
						first = false;
					}
					else {
						path.quadTo(x[0], y[0], midValue(x[0], x[1]), midValue(y[0], y[1]));
					}
				} else if (!onCurve[0] && onCurve[1]) {
					if (first) {
						// 最初の移動
						path.moveTo(x[1], y[1]);
						first = false;
					} else {
						path.quadTo(x[0], y[0], x[1], y[1]);
					}
				} else {
					if (!end && count < 3) {
						break;
					}
					// 3点接続
					if (first) {
						// 最初の移動
						path.moveTo(x[0], y[0]);
						first = false;
					}
					if (onCurve[0] && !onCurve[1] && onCurve[2]) {
						path.quadTo(x[1], y[1], x[2], y[2]);
					} else if (onCurve[0] && !onCurve[1] && !onCurve[2]) {
						path.quadTo(x[1], y[1], midValue(x[1], x[2]), midValue(y[1], y[2]));
					} else {
						throw new IllegalStateException();
					}
					count -= 2;
					System.arraycopy(x, 2, x, 0, x.length - 2);
					System.arraycopy(y, 2, y, 0, y.length - 2);
					System.arraycopy(onCurve, 2, onCurve, 0, onCurve.length - 2);
					continue;
				}
				--count;
				System.arraycopy(x, 1, x, 0, x.length - 1);
				System.arraycopy(y, 1, y, 0, y.length - 1);
				System.arraycopy(onCurve, 1, onCurve, 0, onCurve.length - 1);
			} while (end && count > 0);
			if (end) {
				++endIndex;
				path.closePath();
				first = true;
			}
		}

		glyph = new Glyph(path, null);
		this.glyphs[ix] = new SoftReference<Glyph>(glyph);
		return glyph;
	}

	private static float midValue(float a, float b) {
		return a + (b - a) / 2.0f;
	}
}
