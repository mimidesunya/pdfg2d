package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

/**
 * PostScript table.
 * 
 * @param version        the table version
 * @param glyphNameIndex array of glyph name indices
 * @param psGlyphName    array of PostScript glyph names
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @since 1.0
 */
public record PostTable(int version, int[] glyphNameIndex, String[] psGlyphName) implements Table {

	/**
	 * Mac glyph names for standard Mac encoding.
	 */
	private static final String[] MAC_GLYPH_NAME = {
			".notdef", "null", "CR", "space", "exclam", "quotedbl", "numbersign", "dollar", "percent", "ampersand",
			"quotesingle", "parenleft", "parenright", "asterisk", "plus", "comma", "hyphen", "period", "slash", "zero",
			"one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "colon", "semicolon", "less",
			"equal",
			"greater", "question", "at", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P",
			"Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "bracketleft", "backslash", "bracketright", "asciicircum",
			"underscore", "grave", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q",
			"r", "s", "t", "u", "v", "w", "x", "y", "z", "braceleft", "bar", "braceright", "asciitilde", "Adieresis",
			"Aring", "Ccedilla", "Eacute", "Ntilde", "Odieresis", "Udieresis", "aacute", "agrave", "acircumflex",
			"adieresis", "atilde", "aring", "ccedilla", "eacute", "egrave", "ecircumflex", "edieresis", "iacute",
			"igrave",
			"icircumflex", "idieresis", "ntilde", "oacute", "ograve", "ocircumflex", "odieresis", "otilde", "uacute",
			"ugrave", "ucircumflex", "udieresis", "dagger", "degree", "cent", "sterling", "section", "bullet",
			"paragraph",
			"germandbls", "registered", "copyright", "trademark", "acute", "dieresis", "notequal", "AE", "Oslash",
			"infinity", "plusminus", "lessequal", "greaterequal", "yen", "mu", "partialdiff", "summation", "product",
			"pi",
			"integral'", "ordfeminine", "ordmasculine", "Omega", "ae", "oslash", "questiondown", "exclamdown",
			"logicalnot",
			"radical", "florin", "approxequal", "increment", "guillemotleft", "guillemotright", "ellipsis", "nbspace",
			"Agrave", "Atilde", "Otilde", "OE", "oe", "endash", "emdash", "quotedblleft", "quotedblright", "quoteleft",
			"quoteright", "divide", "lozenge", "ydieresis", "Ydieresis", "fraction", "currency", "guilsinglleft",
			"guilsinglright", "fi", "fl", "daggerdbl", "middot", "quotesinglbase", "quotedblbase", "perthousand",
			"Acircumflex", "Ecircumflex", "Aacute", "Edieresis", "Egrave", "Iacute", "Icircumflex", "Idieresis",
			"Igrave",
			"Oacute", "Ocircumflex", "", "Ograve", "Uacute", "Ucircumflex", "Ugrave", "dotlessi", "circumflex", "tilde",
			"overscore", "breve", "dotaccent", "ring", "cedilla", "hungarumlaut", "ogonek", "caron", "Lslash", "lslash",
			"Scaron", "scaron", "Zcaron", "zcaron", "brokenbar", "Eth", "eth", "Yacute", "yacute", "Thorn", "thorn",
			"minus", "multiply", "onesuperior", "twosuperior", "threesuperior", "onehalf", "onequarter",
			"threequarters",
			"franc", "Gbreve", "gbreve", "Idot", "Scedilla", "scedilla", "Cacute", "cacute", "Ccaron", "ccaron", ""
	};

	protected PostTable(final DirectoryEntry de, final RandomAccessFile raf) throws IOException {
		this(readData(de, raf));
	}

	private PostTable(PostTable other) {
		this(other.version, other.glyphNameIndex, other.psGlyphName);
	}

	private static PostTable readData(final DirectoryEntry de, final RandomAccessFile raf) throws IOException {
		synchronized (raf) {
			raf.seek(de.offset());
			final int version = raf.readInt();
			raf.readInt(); // italicAngle
			raf.readShort(); // underlinePosition
			raf.readShort(); // underlineThickness
			raf.readInt(); // isFixedPitch
			raf.readInt(); // minMemType42
			raf.readInt(); // maxMemType42
			raf.readInt(); // minMemType1
			raf.readInt(); // maxMemType1

			int[] glyphNameIndex = null;
			String[] psGlyphName = null;

			if (version == 0x00020000) {
				final int numGlyphs = raf.readUnsignedShort();
				glyphNameIndex = new int[numGlyphs];
				for (int i = 0; i < numGlyphs; i++) {
					glyphNameIndex[i] = raf.readUnsignedShort();
				}
				int high = 0;
				for (int i = 0; i < numGlyphs; i++) {
					if (high < glyphNameIndex[i]) {
						high = glyphNameIndex[i];
					}
				}
				if (high > 257) {
					final int h = high - 257;
					psGlyphName = new String[h];
					for (int i = 0; i < h; i++) {
						final int len = raf.readUnsignedByte();
						final byte[] buf = new byte[len];
						raf.readFully(buf);
						psGlyphName[i] = new String(buf, StandardCharsets.US_ASCII);
					}
				}
			}
			return new PostTable(version, glyphNameIndex, psGlyphName);
		}
	}

	public String getGlyphName(final int i) {
		if (this.version == 0x00020000) {
			return (this.glyphNameIndex[i] > 257)
					? this.psGlyphName[this.glyphNameIndex[i] - 258]
					: MAC_GLYPH_NAME[this.glyphNameIndex[i]];
		} else {
			return null;
		}
	}

	@Override
	public int getType() {
		return POST;
	}
}
