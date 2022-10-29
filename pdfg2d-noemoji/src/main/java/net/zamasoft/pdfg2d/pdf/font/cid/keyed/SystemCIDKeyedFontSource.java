package net.zamasoft.pdfg2d.pdf.font.cid.keyed;

import java.awt.Font;

import net.zamasoft.pdfg2d.pdf.font.cid.CIDTable;
import net.zamasoft.pdfg2d.pdf.font.cid.CMap;
import net.zamasoft.pdfg2d.pdf.font.cid.WArray;
import net.zamasoft.pdfg2d.pdf.font.cid.identity.SystemCIDIdentityFontSource;
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
public class SystemCIDKeyedFontSource extends CIDKeyedFontSource {
	private static final long serialVersionUID = 0L;

	protected final Font awtFont;

	public SystemCIDKeyedFontSource(CMap hcmap, CMap vcmap, Font awtFont) {
		super(hcmap, vcmap);
		this.awtFont = awtFont = awtFont.deriveFont(1000f);
		SystemCIDIdentityFontSource fs = new SystemCIDIdentityFontSource(awtFont);
		this.fontName = fs.getFontName();
		this.aliases = fs.getAliases();
		this.bbox = fs.getBBox();
		this.ascent = fs.getAscent();
		this.descent = fs.getDescent();
		this.capHeight = fs.getCapHeight();
		this.xHeight = fs.getXHeight();
		this.panose = fs.getPanose();
	}

	public Font getAwtFont() {
		return this.awtFont;
	}

	public WArray getWArray() {
		if (this.warray == null) {
			SystemCIDIdentityFontSource fs = new SystemCIDIdentityFontSource(this.awtFont);
			this.setWArray(systemWArray(fs, this.hcmap));
		}
		return this.warray;
	}

	private static WArray systemWArray(SystemCIDIdentityFontSource fs, CMap cmap) {
		ShortList cidToAdvance = new ShortList(Short.MIN_VALUE);
		CIDTable ct = cmap.getCIDTable();
		IntMapIterator i = ct.getIterator();
		while (i.next()) {
			int cid = i.value();
			int gid = fs.toGID(i.key());
			short advance = (short) fs.getWidth(gid);
			cidToAdvance.set(cid, advance);
		}
		short[] widths = cidToAdvance.toArray();
		WArray warray = WArray.buildFromWidths(new ArrayShortMapIterator(widths));
		return warray;
	}
}
