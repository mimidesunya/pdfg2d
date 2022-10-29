package net.zamasoft.pdfg2d.pdf.font.type1;

import java.io.Serializable;

/**
 * 1文字についての情報です。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: GlyphInfo.java 1564 2018-07-04 11:50:51Z miyabe $
 */
class GlyphInfo implements Serializable {
	private static final long serialVersionUID = 0;

	public final short advance;

	private final int[] sgidToLigature;

	private final short[] sgidToKerning;

	public GlyphInfo(short advance, int[] sgidToLigature, short[] sgidToKerning) {
		this.advance = advance;
		this.sgidToLigature = sgidToLigature;
		this.sgidToKerning = sgidToKerning;
	}

	public short getKerning(int sgid) {
		if (this.sgidToKerning == null || sgid >= this.sgidToKerning.length) {
			return 0;
		}
		short kerning = this.sgidToKerning[sgid];
		return kerning;
	}

	public int getLigature(int sgid) {
		if (this.sgidToLigature == null || sgid >= this.sgidToLigature.length) {
			return -1;
		}
		int lgid = this.sgidToLigature[sgid];
		return lgid;
	}
}