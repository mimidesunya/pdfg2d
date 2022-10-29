package net.zamasoft.pdfg2d.font;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DefaultFontStore implements FontStore, Serializable {
	private static final long serialVersionUID = 0L;

	private Map<FontSource, Font> fonts = new HashMap<FontSource, Font>();

	public Font useFont(FontSource source) throws IOException {
		Font font = (Font) this.fonts.get(source);
		if (font == null) {
			font = source.createFont();
			this.fonts.put(source, font);
		}
		return font;
	}
}
