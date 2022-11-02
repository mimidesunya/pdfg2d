package net.zamasoft.pdfg2d.pdf.font.cid.keyed;

import java.io.File;
import java.io.IOException;

import net.zamasoft.font.OpenTypeFont;
import net.zamasoft.font.table.Table;
import net.zamasoft.font.table.XmtxTable;
import net.zamasoft.pdfg2d.font.FontSource;
import net.zamasoft.pdfg2d.pdf.font.cid.CIDTable;
import net.zamasoft.pdfg2d.pdf.font.cid.CMap;
import net.zamasoft.pdfg2d.pdf.font.cid.WArray;
import net.zamasoft.pdfg2d.pdf.font.cid.identity.OpenTypeCIDIdentityFontSource;
import net.zamasoft.pdfg2d.util.ArrayShortMapIterator;
import net.zamasoft.pdfg2d.util.IntMapIterator;
import net.zamasoft.pdfg2d.util.ShortList;

/**
 * 等幅の一般フォントです。 このフォントはプラットフォームによって書体が変わります。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: GenericType0FontFace.java,v 1.2 2005/06/06 04:42:24 harumanx
 *          Exp $
 */
public class OpenTypeCIDKeyedFontSource extends CIDKeyedFontSource {
	private static final long serialVersionUID = 1L;

	protected final File otFile;

	protected final int index;

	public OpenTypeCIDKeyedFontSource(CMap hcmap, CMap vcmap, File otFile, int index) throws IOException {
		super(hcmap, vcmap);
		this.otFile = otFile;
		this.index = index;
		OpenTypeCIDIdentityFontSource fs = new OpenTypeCIDIdentityFontSource(this.otFile, this.index,
				this.getDirection());
		this.fontName = fs.getFontName();
		this.aliases = fs.getAliases();
		this.bbox = fs.getBBox();
		this.ascent = fs.getAscent();
		this.descent = fs.getDescent();
		this.capHeight = fs.getCapHeight();
		this.xHeight = fs.getXHeight();
		this.stemH = fs.getStemH();
		this.stemV = fs.getStemV();
		this.panose = fs.getPanose();
	}

	public WArray getWArray() {
		if (this.warray == null) {
			try {
				OpenTypeCIDIdentityFontSource fs = new OpenTypeCIDIdentityFontSource(this.otFile, this.index,
						this.getDirection());
				this.setWArray(otWArray(fs, this.hcmap));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return this.warray;
	}

	private static WArray otWArray(OpenTypeCIDIdentityFontSource fs, CMap cmap) {
		OpenTypeFont otFont = fs.getOpenTypeFont();
		XmtxTable hmtx = (XmtxTable) otFont.getTable(Table.hmtx);
		short upm = fs.getUnitsPerEm();

		ShortList cidToAdvance = new ShortList(Short.MIN_VALUE);
		CIDTable ct = cmap.getCIDTable();
		IntMapIterator i = ct.getIterator();
		while (i.next()) {
			int cid = i.value();
			int gid = fs.getCmapFormat().mapCharCode(i.key());
			short advance = (short) (hmtx.getAdvanceWidth(gid) * FontSource.DEFAULT_UNITS_PER_EM / upm);
			// CIDは重複することがあるので、広い方の幅を採用する
			if (advance > cidToAdvance.get(cid)) {
				cidToAdvance.set(cid, advance);
			}
		}
		short[] widths = cidToAdvance.toArray();
		WArray warray = WArray.buildFromWidths(new ArrayShortMapIterator(widths));
		return warray;
	}
}
