package net.zamasoft.pdfg2d.font;

import net.zamasoft.pdfg2d.gc.font.FontStyle.Direction;
import net.zamasoft.pdfg2d.gc.font.FontStyle.Weight;
public class FontSourceWrapper implements FontSource {
	private static final long serialVersionUID = 0L;
	protected final FontSource source;

	public FontSourceWrapper(FontSource source) {
		this.source = source;
	}

	@Override
	public String[] getAliases() {
		return source.getAliases();
	}

	@Override
	public boolean canDisplay(int c) {
		return source.canDisplay(c);
	}

	@Override
	public Font createFont() {
		return source.createFont();
	}

	@Override
	public Direction getDirection() {
		return source.getDirection();
	}

	@Override
	public short getAscent() {
		return source.getAscent();
	}

	@Override
	public BBox getBBox() {
		return source.getBBox();
	}

	@Override
	public short getCapHeight() {
		return source.getCapHeight();
	}

	@Override
	public short getDescent() {
		return source.getDescent();
	}

	@Override
	public String getFontName() {
		return source.getFontName();
	}

	@Override
	public short getStemH() {
		return source.getStemH();
	}

	@Override
	public short getStemV() {
		return source.getStemV();
	}

	@Override
	public Weight getWeight() {
		return source.getWeight();
	}

	@Override
	public short getXHeight() {
		return source.getXHeight();
	}

	@Override
	public short getSpaceAdvance() {
		return source.getSpaceAdvance();
	}

	@Override
	public boolean isItalic() {
		return source.isItalic();
	}

	@Override
	public String toString() {
		return "FontSourceWrapper:" + source.toString();
	}
}
