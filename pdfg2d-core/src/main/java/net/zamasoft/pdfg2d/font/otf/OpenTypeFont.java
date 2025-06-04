package net.zamasoft.pdfg2d.font.otf;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import net.zamasoft.font.Glyph;
import net.zamasoft.font.table.Feature;
import net.zamasoft.font.table.FeatureList;
import net.zamasoft.font.table.FeatureTags;
import net.zamasoft.font.table.GsubTable;
import net.zamasoft.font.table.LangSys;
import net.zamasoft.font.table.Lookup;
import net.zamasoft.font.table.LookupList;
import net.zamasoft.font.table.Script;
import net.zamasoft.font.table.ScriptList;
import net.zamasoft.font.table.ScriptTags;
import net.zamasoft.font.table.SingleSubst;
import net.zamasoft.font.table.Table;
import net.zamasoft.font.table.XmtxTable;
import net.zamasoft.pdfg2d.font.FontSource;
import net.zamasoft.pdfg2d.font.ShapedFont;
import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.GraphicsException;
import net.zamasoft.pdfg2d.gc.font.FontStyle.Direction;
import net.zamasoft.pdfg2d.gc.font.util.FontUtils;
import net.zamasoft.pdfg2d.gc.text.Text;
import net.zamasoft.pdfg2d.gc.text.hyphenation.impl.BitSetCharacterSet;
import net.zamasoft.pdfg2d.gc.text.hyphenation.impl.CharacterSet;

public abstract class OpenTypeFont implements ShapedFont {
	private static final long serialVersionUID = 2L;

	protected static final int DEFAULT_VERTICAL_ORIGIN = 880;

	protected static final boolean ADJUST_VERTICAL = false;

	protected final OpenTypeFontSource source;

	protected final SingleSubst vSubst;

	protected final XmtxTable vmtx, hmtx;

	protected OpenTypeFont(OpenTypeFontSource source) {
		this.source = source;
		net.zamasoft.font.OpenTypeFont ttfFont = source.getOpenTypeFont();
		this.hmtx = (XmtxTable) ttfFont.getTable(Table.hmtx);

		if (this.source.getDirection() == Direction.TB) {
			// 縦書モード
			GsubTable gsub = (GsubTable) ttfFont.getTable(Table.GSUB);
			ScriptList scriptList = gsub.getScriptList();
			Script script = scriptList.findScript(ScriptTags.SCRIPT_TAG_KANA);
			if (script == null) {
				script = scriptList.findScript(ScriptTags.SCRIPT_TAG_HANI);
			}
			if (script == null) {
				script = scriptList.findScript(ScriptTags.SCRIPT_TAG_LATN);
			}
			if (script == null) {
				script = scriptList.findScript(ScriptTags.SCRIPT_TAG_HANG);
			}
			if (script != null) {
				LangSys langSys = script.getDefaultLangSys();
				FeatureList featureList = gsub.getFeatureList();
				Feature feature = featureList.findFeature(langSys, FeatureTags.FEATURE_TAG_VERT);
				if (feature != null) {
					LookupList lookupList = gsub.getLookupList();
					Lookup lookup = lookupList.getLookup(feature, 0);
					this.vSubst = (SingleSubst) lookup.getSubtable(0);
					this.vmtx = (XmtxTable) ttfFont.getTable(Table.vmtx);
					return;
				}
			}
		}
		this.vSubst = null;
		this.vmtx = null;
	}

	protected final boolean isVertical() {
		return this.vmtx != null;
	}

	protected final Shape adjustShape(Shape shape, int gid) {
		if (!this.isVertical()) {
			return shape;
		}
		if (ADJUST_VERTICAL) {
			double advance = this.getAdvance(gid);
			Rectangle2D bound = shape.getBounds2D();
			double bottom = bound.getY() + bound.getHeight() + DEFAULT_VERTICAL_ORIGIN;
			if (bottom > advance) {
				// 字面が衝突しないように調整する
				GeneralPath path = new GeneralPath(shape);
				path.transform(AffineTransform.getTranslateInstance(0, advance - bottom));
				shape = path;
			}
		}
		int cid = this.toChar(gid);
		if (cid == 0xFF0D || cid == 0xFF1C || cid == 0xFF1E || cid == 0x2212 || cid == 0x226A || cid == 0x226B) {
			GeneralPath path = new GeneralPath(shape);
			Rectangle2D bound = shape.getBounds2D();
			path.transform(AffineTransform.getRotateInstance(Math.PI / 2.0, bound.getCenterX(), bound.getCenterY()));
			shape = path;
		}
		return shape;
	}

	protected final short getHAdvance(int gid) {
		final OpenTypeFontSource source = (OpenTypeFontSource) this.getFontSource();
		final short advance = (short) (this.hmtx.getAdvanceWidth(gid) * FontSource.DEFAULT_UNITS_PER_EM
				/ source.getUnitsPerEm());
		return advance;
	}

	protected final short getVAdvance(int gid) {
		if (this.vmtx == null) {
			return FontSource.DEFAULT_UNITS_PER_EM;
		}
		final OpenTypeFontSource source = (OpenTypeFontSource) this.getFontSource();
		final short advance = (short) (this.vmtx.getAdvanceWidth(gid) * FontSource.DEFAULT_UNITS_PER_EM
				/ source.getUnitsPerEm());
		return advance;
	}

	public FontSource getFontSource() {
		return this.source;
	}

	public int toGID(int c) {
		OpenTypeFontSource source = (OpenTypeFontSource) this.getFontSource();
		int gid = source.getCmapFormat().mapCharCode(c);
		if (this.vSubst != null) {
			gid = this.vSubst.substitute(gid);
		}
		return gid;
	}

	public Shape getShapeByGID(int gid) {
		OpenTypeFontSource source = (OpenTypeFontSource) this.getFontSource();
		Glyph glyph = source.getOpenTypeFont().getGlyph(gid);
		if (glyph == null) {
			return null;
		}
		Shape shape = glyph.getPath();
		shape = this.adjustShape(shape, gid);
		return shape;
	}

	public short getAdvance(int gid) {
		if (this.isVertical()) {
			return this.getVAdvance(gid);
		}
		return this.getHAdvance(gid);
	}

	public short getWidth(int gid) {
		return this.getHAdvance(gid);
	}

	public void drawTo(GC gc, Text text) throws IOException, GraphicsException {
		FontUtils.drawText(gc, this, text);
	}

	protected abstract int toChar(int gid);

	// 開始カッコ
	private static final CharacterSet CL01 = new BitSetCharacterSet("‘“（〔［｛〈《「『【⦅〖«〝");
	// 開始カッコ
	private static final CharacterSet CL02 = new BitSetCharacterSet("’”）〕］｝〉》」』】⦆〙〗»〟");
	// 句読点
	private static final CharacterSet CL0607 = new BitSetCharacterSet("。．、，");

	public short getKerning(int sgid, int gid) {
		int scid = this.toChar(sgid);
		// カッコ類のカーニング
		final short THRESHOLD = 750, KERNING = 500;
		if (CL01.contains((char) scid) && this.getWidth(sgid) > THRESHOLD) {
			int cid = this.toChar(gid);
			if (CL01.contains((char) cid) && this.getWidth(gid) > THRESHOLD) {
				return KERNING;
			}
		} else if (CL02.contains((char) scid) && this.getWidth(sgid) > THRESHOLD) {
			int cid = this.toChar(gid);
			if ((CL01.contains((char) cid) || CL02.contains((char) cid) || CL0607.contains((char) cid))
					&& this.getWidth(gid) > THRESHOLD) {
				return KERNING;
			}
		} else if (CL0607.contains((char) scid) && this.getWidth(sgid) > THRESHOLD) {
			int cid = this.toChar(gid);
			if ((CL01.contains((char) cid) || (CL02.contains((char) cid)) && this.getWidth(gid) > THRESHOLD)) {
				return KERNING;
			}
		}
		return 0;
	}

	public int getLigature(int gid, int cid) {
		return -1;
	}
}