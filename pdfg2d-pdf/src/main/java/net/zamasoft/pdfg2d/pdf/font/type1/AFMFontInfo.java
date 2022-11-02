package net.zamasoft.pdfg2d.pdf.font.type1;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import net.zamasoft.pdfg2d.font.BBox;
import net.zamasoft.pdfg2d.util.NumberUtils;

public class AFMFontInfo implements Serializable {
	private static final long serialVersionUID = 0L;

	public static class AFMGlyphInfo implements Serializable {
		private static final long serialVersionUID = 0L;

		public int gid = -1;

		public String name;

		public short advance = 0;

		public Map<String, String> nameToLigature = null;

		public Map<String, Short> nameToKerning = null;

		public void addKerning(String sname, short kerning) {
			if (this.nameToKerning == null) {
				this.nameToKerning = new HashMap<String, Short>();
			}
			this.nameToKerning.put(sname, NumberUtils.shortValue(kerning));
		}

		public void addLigature(String sname, String lname) {
			if (this.nameToLigature == null) {
				this.nameToLigature = new HashMap<String, String>();
			}
			this.nameToLigature.put(sname, lname);
		}
	}

	public String fontName, fullName, familyName;

	public short ascent = 1000, descent = 0, capHeight = 700, stemv = 0, stemh = 0, xHeight = 500;

	public short weight = 400;

	public boolean italic = false;

	public BBox bbox;

	/** グリフ名からグリフ情報(AFMGlyphInfo)へのマッピングです。 */
	public Map<String, AFMGlyphInfo> nameToGi;
}
