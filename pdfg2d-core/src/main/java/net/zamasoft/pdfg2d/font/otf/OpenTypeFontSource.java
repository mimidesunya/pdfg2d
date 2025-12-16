package net.zamasoft.pdfg2d.font.otf;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.zamasoft.font.FontFile;
import net.zamasoft.font.table.CmapTable;
import net.zamasoft.font.table.GenericCmapFormat;
import net.zamasoft.font.table.HeadTable;
import net.zamasoft.font.table.HheaTable;
import net.zamasoft.font.table.NameTable;
import net.zamasoft.font.table.Os2Table;
import net.zamasoft.font.table.Table;
import net.zamasoft.font.table.UvsCmapFormat;
import net.zamasoft.font.table.XmtxTable;
import net.zamasoft.pdfg2d.font.AbstractFontSource;
import net.zamasoft.pdfg2d.font.BBox;
import net.zamasoft.pdfg2d.font.Font;
import net.zamasoft.pdfg2d.font.FontSource;
import net.zamasoft.pdfg2d.gc.font.FontStyle.Direction;
import net.zamasoft.pdfg2d.gc.font.Panose;
import net.zamasoft.pdfg2d.gc.text.util.TextUtils;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
/**
 * Represents a source of an OpenType font.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class OpenTypeFontSource extends AbstractFontSource {
	private static final Logger LOG = Logger.getLogger(OpenTypeFontSource.class.getName());

	private static final long serialVersionUID = 4L;

	protected static Map<File, FontFile> fileToFont = new WeakHashMap<>();

	protected final File file;

	protected final int index;

	protected final short upm;

	protected String fontName;

	protected final BBox bbox;

	protected final short ascent, descent, xHeight, capHeight, spaceAdvance, stemH, stemV;

	protected Panose panose;

	protected final Direction direction;

	protected final GenericCmapFormat cmap;

	protected final UvsCmapFormat uvsCmap;

	/**
	 * Creates a new OpenTypeFontSource.
	 * 
	 * @param file      the font file
	 * @param index     the font index within the file
	 * @param direction the layout direction
	 * @throws IOException if an error occurs while reading the font file
	 */
	public OpenTypeFontSource(final File file, final int index, final Direction direction) throws IOException {
		this.index = index;
		this.file = file;
		final var ttFont = this.getOpenTypeFont();

		// Font metric information
		{
			final var head = (HeadTable) ttFont.getTable(Table.head);
			this.upm = head.getUnitsPerEm();
			final short llx = (short) (head.getXMin() * FontSource.DEFAULT_UNITS_PER_EM / this.upm);
			final short lly = (short) (head.getYMin() * FontSource.DEFAULT_UNITS_PER_EM / this.upm);
			final short urx = (short) (head.getXMax() * FontSource.DEFAULT_UNITS_PER_EM / this.upm);
			final short ury = (short) (head.getYMax() * FontSource.DEFAULT_UNITS_PER_EM / this.upm);
			this.bbox = new BBox(llx, lly, urx, ury);
			this.setItalic((head.getMacStyle() & 2) != 0);
		}

		final Set<String> aliases = new TreeSet<>();
		String fontName = null;
		{
			final var name = (NameTable) ttFont.getTable(Table.name);
			for (int i = 0; i < name.size(); ++i) {
				final var record = name.get(i);
				final short nameId = record.getNameId();
				if (nameId == 1 || nameId == 3 || nameId == 4) {
					aliases.add(record.getRecordString());
				} else if (nameId == 6) {
					fontName = record.getRecordString();
				}
			}
		}
		this.aliases = aliases.toArray(new String[0]);

		if (fontName == null) {
			throw new NullPointerException();
		}
		this.fontName = fontName;

		{
			final var os2 = (Os2Table) ttFont.getTable(Table.OS_2);
			final var weight = TextUtils.decodeFontWeight((short) os2.getWeightClass());
			this.setWeight(weight);
			final short cFamilyClass = os2.getFamilyClass();
			final var panose = os2.getPanose();
			this.panose = new Panose(cFamilyClass, panose.code);
		}

		{
			final var hhea = (HheaTable) ttFont.getTable(Table.hhea);
			this.ascent = (short) (hhea.getAscender() * FontSource.DEFAULT_UNITS_PER_EM / this.upm);
			this.descent = (short) (-hhea.getDescender() * FontSource.DEFAULT_UNITS_PER_EM / this.upm);
		}

		final var cmapt = (CmapTable) ttFont.getTable(Table.cmap);
		var cmap = (GenericCmapFormat) cmapt.getCmapFormat(Table.platformMicrosoft, Table.encodingUCS4);
		if (cmap == null) {
			cmap = (GenericCmapFormat) cmapt.getCmapFormat(Table.platformMicrosoft, Table.encodingUCS2);
		}
		if (cmap == null) {
			cmap = (GenericCmapFormat) cmapt.getCmapFormat(Table.platformUnicode, Table.encodingBMP);
		}
		if (cmap == null) {
			cmap = (GenericCmapFormat) cmapt.getCmapFormat(Table.platformUnicode, Table.encodingNonBMP);
		}
		if (cmap == null) {
			cmap = (GenericCmapFormat) cmapt.getCmapFormat(Table.platformUnicode, Table.encodingUndefined);
		}
		if (cmap == null) {
			cmap = (GenericCmapFormat) cmapt.getCmapFormat(Table.platformUnicode, (short) -1);
		}
		this.cmap = cmap;

		this.uvsCmap = (UvsCmapFormat) cmapt.getCmapFormat(Table.platformUnicode, Table.encodingUVS);

		{
			final int gid = this.cmap.mapCharCode(' ');
			final var hmtx = (XmtxTable) ttFont.getTable(Table.hmtx);
			this.spaceAdvance = (short) (hmtx.getAdvanceWidth(gid) * FontSource.DEFAULT_UNITS_PER_EM / this.upm);
		}

		{
			final var font = this.getOpenTypeFont();
			final var gx = font.getGlyph(this.cmap.mapCharCode('x'));
			this.xHeight = (gx == null || gx.getPath() == null) ? DEFAULT_X_HEIGHT
					: (short) gx.getPath().getBounds().height;
			final var gh = font.getGlyph(this.cmap.mapCharCode('H'));
			this.capHeight = (gh == null || gh.getPath() == null) ? DEFAULT_CAP_HEIGHT
					: (short) gh.getPath().getBounds().height;
		}

		this.stemH = 0;
		this.stemV = 0;

		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("new font: " + this.getFontName());
		}

		this.direction = direction;
	}

	/**
	 * Returns the OpenType font instance.
	 * 
	 * @return the OpenType font
	 */
	public net.zamasoft.font.OpenTypeFont getOpenTypeFont() {
		return getOpenTypeFont(this.file, this.index);
	}

	/**
	 * Returns the OpenType font for the given file and index.
	 * 
	 * @param file  the font file
	 * @param index the font index
	 * @return the OpenType font
	 */
	public static synchronized net.zamasoft.font.OpenTypeFont getOpenTypeFont(final File file, final int index) {
		var fontFile = fileToFont.get(file);
		try {
			if (fontFile != null && fontFile.timestamp == file.lastModified()) {
				return fontFile.getFont(index);
			}
			fontFile = new FontFile(file);
			fileToFont.put(file, fontFile);
			return fontFile.getFont(index);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Direction getDirection() {
		return this.direction;
	}

	public void setFontName(final String fontName) {
		this.fontName = fontName;
	}

	public Panose getPanose() {
		return this.panose;
	}

	public void setPanose(final Panose panose) {
		this.panose = panose;
	}

	@Override
	public BBox getBBox() {
		return this.bbox;
	}

	@Override
	public String getFontName() {
		return this.fontName;
	}

	@Override
	public short getXHeight() {
		return this.xHeight;
	}

	@Override
	public short getCapHeight() {
		return this.capHeight;
	}

	@Override
	public short getSpaceAdvance() {
		return this.spaceAdvance;
	}

	@Override
	public short getAscent() {
		return this.ascent;
	}

	@Override
	public short getDescent() {
		return this.descent;
	}

	@Override
	public short getStemH() {
		return this.stemH;
	}

	@Override
	public short getStemV() {
		return this.stemV;
	}

	public short getUnitsPerEm() {
		return this.upm;
	}

	public GenericCmapFormat getCmapFormat() {
		return this.cmap;
	}

	public UvsCmapFormat getUvsCmapFormat() {
		return this.uvsCmap;
	}

	@Override
	public boolean canDisplay(final int c) {
		if (this.getDirection() == Direction.TB) {
			if (c <= 0xFF || (c >= 0xFF60 && c <= 0xFFDF)) {
				return false;
			}
		}
		if (this.cmap.mapCharCode(c) != 0) {
			return true;
		}
		if (this.uvsCmap != null && this.uvsCmap.isVarSelector(c)) {
			return true;
		}
		return false;
	}

	@Override
	public Font createFont() {
		return new OpenTypeFontImpl(this);
	}
}
