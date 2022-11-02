package net.zamasoft.pdfg2d.font;

public class FontSourceWrapper implements FontSource {
	private static final long serialVersionUID = 0L;

	protected final FontSource source;

	public FontSourceWrapper(FontSource source) {
		this.source = source;
	}

	public String[] getAliases() {
		return this.source.getAliases();
	}

	public boolean canDisplay(int c) {
		return this.source.canDisplay(c);
	}

	public Font createFont() {
		return this.source.createFont();
	}

	public byte getDirection() {
		return this.source.getDirection();
	}

	public short getAscent() {
		return this.source.getAscent();
	}

	public BBox getBBox() {
		return this.source.getBBox();
	}

	public short getCapHeight() {
		return this.source.getCapHeight();
	}

	public short getDescent() {
		return this.source.getDescent();
	}

	public String getFontName() {
		return this.source.getFontName();
	}

	public short getStemH() {
		return this.source.getStemH();
	}

	public short getStemV() {
		return this.source.getStemV();
	}

	public short getWeight() {
		return this.source.getWeight();
	}

	public short getXHeight() {
		return this.source.getXHeight();
	}

	public short getSpaceAdvance() {
		return this.source.getSpaceAdvance();
	}

	public boolean isItalic() {
		return this.source.isItalic();
	}

	public String toString() {
		return "FontSourceWrapper:" + this.source.toString();
	}
}
