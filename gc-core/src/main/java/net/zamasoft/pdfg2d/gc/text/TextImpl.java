package net.zamasoft.pdfg2d.gc.text;

import java.io.Serializable;

import net.zamasoft.pdfg2d.gc.font.FontMetrics;
import net.zamasoft.pdfg2d.gc.font.FontStyle;

/**
 * 描画可能なテキストです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: TextImpl.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public class TextImpl extends AbstractText implements Serializable {
	private static final long serialVersionUID = 0L;

	private static final int INIT_LEN = 50;

	/** テキストのフォントスタイル。 */
	public final FontStyle fontStyle;

	/** テキストのフォントメトリックス。 */
	public final FontMetrics fontMetrics;

	private int charOffset;

	/** 文字バッファ。 */
	public char[] chars;

	/** 文字数。 */
	public int clen;

	/** グリフバッファ。 */
	public int[] gids;

	/** グリフ毎の対応する文字数。 */
	public byte[] clens;

	/** グリフ数。 */
	public int glen;

	/**
	 * テキスト全体の幅。
	 */
	public double advance = 0;

	/**
	 * 各グリフの後に余計に加えられる余白です。
	 */
	public double letterSpacing = 0;

	/**
	 * 各グリフのごとに余計に加えられる余白です。
	 */
	public double[] xadvances = null;

	public TextImpl(int charOffset, FontStyle fontStyle, FontMetrics fontMetrics) {
		assert fontStyle != null : "FontStyle required.";
		assert fontMetrics != null : "FontMetrics required.";
		this.fontStyle = fontStyle;
		this.fontMetrics = fontMetrics;
		this.charOffset = charOffset;
		int len = Math.max(glen, INIT_LEN);
		this.gids = new int[len];
		this.clens = new byte[len];
		this.chars = new char[len];
	}

	public int getCharOffset() {
		return this.charOffset;
	}

	public double getAdvance() {
		double advance = this.advance + this.letterSpacing * this.glen;
		if (this.xadvances != null) {
			for (int i = 0; i < this.xadvances.length; ++i) {
				advance += this.xadvances[i];
			}
		}
		return advance;
	}

	public double[] getXAdvances(boolean make) {
		if (make) {
			this.xadvances = new double[this.glen];
		}
		return this.xadvances;
	}

	public double getAscent() {
		return this.fontMetrics.getAscent();
	}

	public double getDescent() {
		return this.fontMetrics.getDescent();
	}

	public FontStyle getFontStyle() {
		return this.fontStyle;
	}

	public FontMetrics getFontMetrics() {
		return this.fontMetrics;
	}

	public char[] getChars() {
		return this.chars;
	}

	public int getCLen() {
		return this.clen;
	}

	public int[] getGIDs() {
		return this.gids;
	}

	public byte[] getCLens() {
		return this.clens;
	}

	public int getGLen() {
		return this.glen;
	}

	public double getLetterSpacing() {
		return this.letterSpacing;
	}

	public void setLetterSpacing(double letterSpacing) {
		this.letterSpacing = letterSpacing;
	}

	public Text split(int goff) {
		assert goff > 0 : "goff <= 0 では分割できません: goff=" + goff;
		assert goff < this.glen : "goff が glen == " + this.glen + " 以上では分割できません: goff=" + goff;

		// 前のテキストがtextで、thisが後続となる
		TextImpl text = new TextImpl(this.charOffset, this.fontStyle, this.fontMetrics);
		text.glen = goff;
		text.gids = new int[text.glen];
		text.clens = new byte[text.glen];
		for (int i = 0; i < text.glen; ++i) {
			int gid = this.gids[i];
			text.gids[i] = gid;
			text.advance += this.fontMetrics.getAdvance(gid);
			if (i > 0) {
				text.advance -= this.fontMetrics.getKerning(text.gids[i - 1], gid);
			}
			int clen = (text.clens[i] = this.clens[i]);
			text.clen += clen;
		}
		text.letterSpacing = this.letterSpacing;

		text.chars = new char[text.clen];
		System.arraycopy(this.chars, 0, text.chars, 0, text.clen);

		this.glen -= text.glen;
		System.arraycopy(this.gids, text.glen, this.gids, 0, this.glen);
		System.arraycopy(this.clens, text.glen, this.clens, 0, this.glen);

		this.advance -= text.advance;
		// 分割場所のカーニングを戻す
		this.advance += this.fontMetrics.getKerning(text.gids[text.glen - 1], this.gids[0]);

		this.clen -= text.clen;
		this.charOffset += text.clen;
		System.arraycopy(this.chars, text.clen, this.chars, 0, this.clen);

		return text;
	}

	public double glyphAdvance(int gid) {
		double advance = this.fontMetrics.getAdvance(gid);
		if (this.glen > 0) {
			final double kerning = this.fontMetrics.getKerning(this.gids[this.glen - 1], gid);
			advance -= kerning;
		}
		return advance;
	}

	/**
	 * Append the glyph to tail of Text object.
	 * 
	 * @param ch
	 * @param coff
	 * @param clen
	 * @param gid
	 * @return
	 */
	public double appendGlyph(char[] ch, int coff, byte clen, int gid) {
		final double advance = this.glyphAdvance(gid);
		int glen = this.glen + 1;
		if (glen > this.gids.length) {
			int newLen = glen * 3 / 2;
			{
				int[] a = this.gids;
				this.gids = new int[newLen];
				System.arraycopy(a, 0, this.gids, 0, a.length);
			}
			{
				byte[] a = this.clens;
				this.clens = new byte[newLen];
				System.arraycopy(a, 0, this.clens, 0, a.length);
			}
		}

		int newClen = this.clen + clen;
		if (newClen > this.chars.length) {
			char[] a = this.chars;
			this.chars = new char[Math.max(INIT_LEN, newClen * 3 / 2)];
			System.arraycopy(a, 0, this.chars, 0, a.length);
		}
		System.arraycopy(ch, coff, this.chars, this.clen, clen);
		this.clen = newClen;

		this.gids[this.glen] = gid;
		this.clens[this.glen] = clen;
		this.glen = glen;
		this.advance += advance;
		return advance;
	}

	public void pack() {
		assert this.glen > 0 : "空のテキストです";
		if (this.gids.length != this.glen) {
			int[] gids = new int[this.glen];
			System.arraycopy(this.gids, 0, gids, 0, this.glen);
			this.gids = gids;

			byte[] clens = new byte[this.glen];
			System.arraycopy(this.clens, 0, clens, 0, this.glen);
			this.clens = clens;
		}
		if (this.clen != this.chars.length) {
			char[] chars = new char[this.clen];
			System.arraycopy(this.chars, 0, chars, 0, this.clen);
			this.chars = chars;
		}
	}
}
