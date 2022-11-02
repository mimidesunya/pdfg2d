package net.zamasoft.pdfg2d.gc.font;

import java.io.Serializable;

public class Panose implements Serializable {
	private static final long serialVersionUID = 0L;

	public final byte familyClassId;

	public final byte familySubclass;

	public final byte familyType;

	public final byte serifStyle;

	public final byte weight;

	public final byte proportion;

	public final byte contrast;

	public final byte strokeVariation;

	public final byte armStyle;

	public final byte letterForm;

	public final byte midline;

	public final byte xHeight;

	public Panose(int sFamilyClass, byte[] panose) {
		this.familyClassId = (byte) ((sFamilyClass >> 8) & 0xFF);
		this.familySubclass = (byte) (sFamilyClass & 0xFF);
		this.familyType = panose[0];
		this.serifStyle = panose[1];
		this.weight = panose[2];
		this.proportion = panose[3];
		this.contrast = panose[4];
		this.strokeVariation = panose[5];
		this.armStyle = panose[6];
		this.letterForm = panose[7];
		this.midline = panose[8];
		this.xHeight = panose[9];
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(String.valueOf(this.familyClassId)).append(" ").append(String.valueOf(this.familySubclass))
				.append(" ").append(String.valueOf(this.familyType)).append(" ").append(String.valueOf(this.serifStyle))
				.append(" ").append(String.valueOf(this.weight)).append(" ").append(String.valueOf(this.proportion))
				.append(" ").append(String.valueOf(this.contrast)).append(" ")
				.append(String.valueOf(this.strokeVariation)).append(" ").append(String.valueOf(this.armStyle))
				.append(" ").append(String.valueOf(this.letterForm)).append(" ").append(String.valueOf(this.midline))
				.append(" ").append(String.valueOf(this.xHeight));
		return sb.toString();
	}
}
