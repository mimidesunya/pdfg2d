package net.zamasoft.pdfg2d.font.table;

/**
 * Interface representing a font table.
 */
public interface Table {

	// Table constants
	int BASE = 0x42415345; // Baseline data [OpenType]
	int CFF = 0x43464620; // PostScript font program (compact font format) [PostScript]
	int DSIG = 0x44534947; // Digital signature
	int EBDT = 0x45424454; // Embedded bitmap data
	int EBLC = 0x45424c43; // Embedded bitmap location data
	int EBSC = 0x45425343; // Embedded bitmap scaling data
	int GDEF = 0x47444546; // Glyph definition data [OpenType]
	int GPOS = 0x47504f53; // Glyph positioning data [OpenType]
	int GSUB = 0x47535542; // Glyph substitution data [OpenType]
	int JSTF = 0x4a535446; // Justification data [OpenType]
	int LTSH = 0x4c545348; // Linear threshold table
	int MMFX = 0x4d4d4658; // Multiple master font metrics [PostScript]
	int MMSD = 0x4d4d5344; // Multiple master supplementary data [PostScript]
	int OS_2 = 0x4f532f32; // OS/2 and Windows specific metrics [r]
	int PCLT = 0x50434c54; // PCL5
	int VDMX = 0x56444d58; // Vertical Device Metrics table
	int CMAP = 0x636d6170; // character to glyph mapping [r]
	int CVT = 0x63767420; // Control Value Table
	int FPGM = 0x6670676d; // font program
	int FVAR = 0x66766172; // Apple's font variations table [PostScript]
	int GASP = 0x67617370; // grid-fitting and scan conversion procedure (grayscale)
	int GLYF = 0x676c7966; // glyph data [r]
	int HDMX = 0x68646d78; // horizontal device metrics
	int HEAD = 0x68656164; // font header [r]
	int HHEA = 0x68686561; // horizontal header [r]
	int HMTX = 0x686d7478; // horizontal metrics [r]
	int KERN = 0x6b65726e; // kerning
	int LOCA = 0x6c6f6361; // index to location [r]
	int MAXP = 0x6d617870; // maximum profile [r]
	int NAME = 0x6e616d65; // naming table [r]
	int PREP = 0x70726570; // CVT Program
	int POST = 0x706f7374; // PostScript information [r]
	int VHEA = 0x76686561; // Vertical Metrics header
	int VMTX = 0x766d7478; // Vertical Metrics
	int VORG = 0x564f5247; // Vertical Origin Table

	// Platform IDs
	short PLATFORM_UNICODE = 0;
	short PLATFORM_MACINTOSH = 1;
	short PLATFORM_ISO = 2;
	short PLATFORM_MICROSOFT = 3;

	// Unicode Encoding IDs
	short ENCODING_BMP = 3;
	short ENCODING_NON_BMP = 4;
	short ENCODING_UVS = 5;

	// Microsoft Encoding IDs
	short ENCODING_UNDEFINED = 0;
	short ENCODING_UCS2 = 1;
	short ENCODING_UCS4 = 10;

	// Macintosh Encoding IDs
	short ENCODING_ROMAN = 0;
	short ENCODING_JAPANESE = 1;
	short ENCODING_CHINESE = 2;
	short ENCODING_KOREAN = 3;
	short ENCODING_ARABIC = 4;
	short ENCODING_HEBREW = 5;
	short ENCODING_GREEK = 6;
	short ENCODING_RUSSIAN = 7;
	short ENCODING_R_SYMBOL = 8;
	short ENCODING_DEVANAGARI = 9;
	short ENCODING_GURMUKHI = 10;
	short ENCODING_GUJARATI = 11;
	short ENCODING_ORIYA = 12;
	short ENCODING_BENGALI = 13;
	short ENCODING_TAMIL = 14;
	short ENCODING_TELUGU = 15;
	short ENCODING_KANNADA = 16;
	short ENCODING_MALAYALAM = 17;
	short ENCODING_SINHALESE = 18;
	short ENCODING_BURMESE = 19;
	short ENCODING_KHMER = 20;
	short ENCODING_THAI = 21;
	short ENCODING_LAOTIAN = 22;
	short ENCODING_GEORGIAN = 23;
	short ENCODING_ARMENIAN = 24;
	short ENCODING_MALDIVIAN = 25;
	short ENCODING_TIBETAN = 26;
	short ENCODING_MONGOLIAN = 27;
	short ENCODING_GEEZ = 28;
	short ENCODING_SLAVIC = 29;
	short ENCODING_VIETNAMESE = 30;
	short ENCODING_SINDHI = 31;
	short ENCODING_UNINTERP = 32;

	// ISO Encoding IDs
	short ENCODING_ASCII = 0;
	short ENCODING_ISO10646 = 1;
	short ENCODING_ISO8859_1 = 2;

	// Name IDs
	short NAME_COPYRIGHT_NOTICE = 0;
	short NAME_FONT_FAMILY_NAME = 1;
	short NAME_FONT_SUBFAMILY_NAME = 2;
	short NAME_UNIQUE_FONT_IDENTIFIER = 3;
	short NAME_FULL_FONT_NAME = 4;
	short NAME_VERSION_STRING = 5;
	short NAME_POSTSCRIPT_NAME = 6;
	short NAME_TRADEMARK = 7;

	/**
	 * Get the table type, as a table directory value.
	 *
	 * @return The table type
	 */
	int getType();
}
