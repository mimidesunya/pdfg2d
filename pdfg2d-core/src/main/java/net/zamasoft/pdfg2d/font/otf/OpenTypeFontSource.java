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
import net.zamasoft.font.Glyph;
import net.zamasoft.font.table.CmapTable;
import net.zamasoft.font.table.GenericCmapFormat;
import net.zamasoft.font.table.HeadTable;
import net.zamasoft.font.table.HheaTable;
import net.zamasoft.font.table.NameRecord;
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
import net.zamasoft.pdfg2d.gc.font.FontStyle.Weight;
import net.zamasoft.pdfg2d.gc.font.Panose;
import net.zamasoft.pdfg2d.gc.text.util.TextUtils;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class OpenTypeFontSource extends AbstractFontSource {
	private static final Logger LOG = Logger.getLogger(OpenTypeFontSource.class.getName());

	private static final long serialVersionUID = 4L;

	protected static Map<File, FontFile> fileToFont = new WeakHashMap<File, FontFile>();

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

	public OpenTypeFontSource(File file, int index, Direction direction) throws IOException {
		this.index = index;
		this.file = file;
		net.zamasoft.font.OpenTypeFont ttFont = this.getOpenTypeFont();

		// フォントメトリック情報
		{
			// long time = System.currentTimeMillis();
			HeadTable head = (HeadTable) ttFont.getTable(Table.head);
			this.upm = head.getUnitsPerEm();
			short llx = (short) (head.getXMin() * FontSource.DEFAULT_UNITS_PER_EM / this.upm);
			short lly = (short) (head.getYMin() * FontSource.DEFAULT_UNITS_PER_EM / this.upm);
			short urx = (short) (head.getXMax() * FontSource.DEFAULT_UNITS_PER_EM / this.upm);
			short ury = (short) (head.getYMax() * FontSource.DEFAULT_UNITS_PER_EM / this.upm);
			BBox bbox = new BBox(llx, lly, urx, ury);
			this.bbox = bbox;
			this.setItalic((head.getMacStyle() & 2) != 0);
		}

		Set<String> aliases = new TreeSet<String>();
		String fontName = null;
		{
			// long time = System.currentTimeMillis();
			NameTable name = (NameTable) ttFont.getTable(Table.name);
			for (int i = 0; i < name.size(); ++i) {
				NameRecord record = name.get(i);
				short nameId = record.getNameId();
				if (nameId == 1 || nameId == 3 || nameId == 4) {
					aliases.add(record.getRecordString());
				} else if (nameId == 6) {
					fontName = record.getRecordString();
				}
			}
		}
		this.aliases = aliases.toArray(new String[aliases.size()]);

		if (fontName == null) {
			throw new NullPointerException();
		}
		this.fontName = fontName;

		{
			// long time = System.currentTimeMillis();
			Os2Table os2 = (Os2Table) ttFont.getTable(Table.OS_2);
			Weight weight = TextUtils.decodeFontWeight((short) os2.getWeightClass());
			this.setWeight(weight);
			short cFamilyClass = os2.getFamilyClass();
			net.zamasoft.font.table.Panose panose = os2.getPanose();
			this.panose = new Panose(cFamilyClass, panose.code);
		}

		{
			HheaTable hhea = (HheaTable) ttFont.getTable(Table.hhea);
			this.ascent = (short) (hhea.getAscender() * FontSource.DEFAULT_UNITS_PER_EM / this.upm);
			this.descent = (short) (-hhea.getDescender() * FontSource.DEFAULT_UNITS_PER_EM / this.upm);
		}

		CmapTable cmapt = (CmapTable) ttFont.getTable(Table.cmap);
		GenericCmapFormat cmap = (GenericCmapFormat) cmapt.getCmapFormat(Table.platformMicrosoft, Table.encodingUCS4);
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
			int gid = this.cmap.mapCharCode(' ');
			XmtxTable hmtx = (XmtxTable) ttFont.getTable(Table.hmtx);
			this.spaceAdvance = (short) (hmtx.getAdvanceWidth(gid) * FontSource.DEFAULT_UNITS_PER_EM / this.upm);
		}

		{
			net.zamasoft.font.OpenTypeFont font = this.getOpenTypeFont();
			Glyph gx = font.getGlyph(this.cmap.mapCharCode('x'));
			this.xHeight = (gx == null || gx.getPath() == null) ? DEFAULT_X_HEIGHT
					: (short) gx.getPath().getBounds().height;
			Glyph gh = font.getGlyph(this.cmap.mapCharCode('H'));
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

	public net.zamasoft.font.OpenTypeFont getOpenTypeFont() {
		return getOpenTypeFont(this.file, this.index);
	}

	public static synchronized net.zamasoft.font.OpenTypeFont getOpenTypeFont(File file, int index) {
		FontFile fontFile = fileToFont.get(file);
		try {
			if (fontFile != null && fontFile.timestamp == file.lastModified()) {
				return fontFile.getFont(index);
			}
			fontFile = new FontFile(file);
			fileToFont.put(file, fontFile);
			return fontFile.getFont(index);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Direction getDirection() {
		return this.direction;
	}

	public void setFontName(String fontName) {
		this.fontName = fontName;
	}

	public Panose getPanose() {
		return this.panose;
	}

	public void setPanose(Panose panose) {
		this.panose = panose;
	}

	public BBox getBBox() {
		return this.bbox;
	}

	public String getFontName() {
		return this.fontName;
	}

	public short getXHeight() {
		return this.xHeight;
	}

	public short getCapHeight() {
		return this.capHeight;
	}

	public short getSpaceAdvance() {
		return this.spaceAdvance;
	}

	public short getAscent() {
		return this.ascent;
	}

	public short getDescent() {
		return this.descent;
	}

	public short getStemH() {
		return this.stemH;
	}

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

	public boolean canDisplay(int c) {
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

	public Font createFont() {
		return new OpenTypeFontImpl(this);
	}
}
