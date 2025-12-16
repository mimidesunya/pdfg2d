package net.zamasoft.pdfg2d.font;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link FontStore}.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class DefaultFontStore implements FontStore, Serializable {
	private static final long serialVersionUID = 0L;

	private final Map<FontSource, Font> fonts = new HashMap<>();

	@Override
	public Font useFont(final FontSource source) throws IOException {
		var font = this.fonts.get(source);
		if (font == null) {
			font = source.createFont();
			this.fonts.put(source, font);
		}
		return font;
	}
}
