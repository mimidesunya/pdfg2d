package net.zamasoft.pdfg2d.pdf.font.cid;

import java.io.IOException;
import java.io.OutputStream;

import net.zamasoft.pdfg2d.font.BBox;
import net.zamasoft.pdfg2d.gc.font.Panose;
import net.zamasoft.pdfg2d.pdf.ObjectRef;
import net.zamasoft.pdfg2d.pdf.PdfFragmentOutput;
import net.zamasoft.pdfg2d.pdf.PdfOutput;
import net.zamasoft.pdfg2d.pdf.XRef;
import net.zamasoft.pdfg2d.pdf.font.PdfEmbeddedFont;
import net.zamasoft.pdfg2d.pdf.font.cid.ToUnicode.Unicode;
import net.zamasoft.pdfg2d.pdf.font.type2.CffGenerator;
import net.zamasoft.pdfg2d.util.ArrayShortMapIterator;

public final class CIDUtils {
	public static final String ENCODING_H = "Identity-H";

	public static final String ENCODING_V = "Identity-V";

	public static final String REGISTRY = "Adobe";

	public static final String ORDERING = "Identity";

	public static final int SUPPLEMENT = 0;

	public static final int CID_FIXED_WIDTH = 1;

	public static final int CID_SERIF = 1 << 1;

	public static final int CID_SYMBOLIC = 1 << 2;

	public static final int CID_SCRIPT = 1 << 3;

	public static final int CID_ITALIC = 1 << 6;

	public static final int CID_ALL_CAP = 1 << 16;

	public static final int CID_SMALL_CAP = 1 << 17;

	public static final int CID_FORCE_BOLD = 1 << 18;

	public static final int DEFAULT_VERTICAL_ORIGIN = 880;

	public static final int DEFAULT_H = 500;

	private CIDUtils() {
		// ignore
	}

	public static void writeFlagsAndPanose(PdfOutput out, CIDFontSource source) throws IOException {
		int flags = CID_SYMBOLIC;
		Panose panose = source.getPanose();
		if (panose != null) {
			if (panose.proportion >= 8) {
				flags |= CID_FIXED_WIDTH;
			}
			if (panose.serifStyle <= 3) {
				flags |= CID_SERIF;
			}
			if (panose.familyType == 3) {
				flags |= CID_SCRIPT;
			}
			if (panose.letterForm >= 9) {
				flags |= CID_ITALIC;
			}
			if (panose.familyType == 4 && panose.xHeight == 4) {
				flags |= CID_ALL_CAP;
			}
			if (panose.familyType == 4 && panose.xHeight == 5) {
				flags |= CID_SMALL_CAP;
			}
			if (panose.weight >= 8) {
				flags |= CID_FORCE_BOLD;
			}
		} else {
			if (source.isItalic()) {
				flags |= CID_ITALIC;
			}
			if (source.getWeight() >= 500) {
				flags |= CID_FORCE_BOLD;
			}
		}

		out.writeName("Flags");
		out.writeInt(flags);
		out.lineBreak();

		if (panose != null) {
			byte[] bytes = new byte[12];
			bytes[0] = panose.familyClassId;
			bytes[1] = panose.familySubclass;
			bytes[2] = panose.familyType;
			bytes[3] = panose.serifStyle;
			bytes[4] = panose.weight;
			bytes[5] = panose.proportion;
			bytes[6] = panose.contrast;
			bytes[7] = panose.strokeVariation;
			bytes[8] = panose.armStyle;
			bytes[9] = panose.letterForm;
			bytes[10] = panose.midline;
			bytes[11] = panose.xHeight;
			out.writeName("Style");
			out.startHash();
			out.writeName("Panose");
			out.writeBytes8(bytes, 0, bytes.length);
			out.endHash();
			out.lineBreak();
		}
	}

	/**
	 * WD, Wを出力します。
	 */
	public static void writeWArray(PdfOutput out, WArray warray) throws IOException {
		out.writeName("DW");
		out.writeInt(warray.getDefaultWidth());
		out.lineBreak();
		Width[] widths = warray.getWidths();
		if (widths.length > 0) {
			out.writeName("W");
			out.startArray();
			out.lineBreak();
			for (int i = 0; i < widths.length; ++i) {
				Width w = widths[i];
				short[] shorts = w.getWidths();
				if (shorts.length == 1) {
					out.writeInt(w.getFirstCode());
					out.writeInt(w.getLastCode());
					out.writeInt(shorts[0]);
				} else {
					if (shorts.length <= (w.getLastCode() - w.getFirstCode())) {
						out.writeInt(w.getFirstCode());
						out.startArray();
						for (int j = 0; j < shorts.length - 1; ++j) {
							out.writeInt(shorts[j]);
						}
						out.endArray();
						out.writeInt(w.getFirstCode() + (shorts.length - 1));
						out.writeInt(w.getLastCode());
						out.writeInt(shorts[shorts.length - 1]);
					} else {
						out.writeInt(w.getFirstCode());
						out.startArray();
						for (int j = 0; j < shorts.length; ++j) {
							out.writeInt(shorts[j]);
						}
						out.endArray();
					}
				}
				out.lineBreak();
			}
			out.endArray();
		}
	}

	/**
	 * WD2, W2を出力します。 TODO vx, vy
	 */
	public static void writeWArray2(PdfOutput out, WArray warray) throws IOException {
		out.writeName("DW2");
		out.startArray();
		out.writeInt(DEFAULT_VERTICAL_ORIGIN);
		out.writeInt(-warray.getDefaultWidth());
		out.endArray();
		out.lineBreak();
		Width[] widths = warray.getWidths();
		if (widths.length > 0) {
			out.writeName("W2");
			out.startArray();
			out.lineBreak();
			for (int i = 0; i < widths.length; ++i) {
				Width w = widths[i];
				short[] shorts = w.getWidths();
				if (shorts.length == 1) {
					out.writeInt(w.getFirstCode());
					out.writeInt(w.getLastCode());
					out.writeInt(-shorts[0]);
					out.writeInt(DEFAULT_H);
					out.writeInt(DEFAULT_VERTICAL_ORIGIN);
				} else {
					if (shorts.length <= (w.getLastCode() - w.getFirstCode())) {
						out.writeInt(w.getFirstCode());
						out.startArray();
						for (int j = 0; j < shorts.length - 1; ++j) {
							out.writeInt(-shorts[j]);
							out.writeInt(DEFAULT_H);
							out.writeInt(DEFAULT_VERTICAL_ORIGIN);
						}
						out.endArray();
						out.writeInt(w.getFirstCode() + (shorts.length - 1));
						out.writeInt(w.getLastCode());
						out.writeInt(-shorts[shorts.length - 1]);
						out.writeInt(DEFAULT_H);
						out.writeInt(DEFAULT_VERTICAL_ORIGIN);
					} else {
						out.writeInt(w.getFirstCode());
						out.startArray();
						for (int j = 0; j < shorts.length; ++j) {
							out.writeInt(-shorts[j]);
							out.writeInt(DEFAULT_H);
							out.writeInt(DEFAULT_VERTICAL_ORIGIN);
						}
						out.endArray();
					}
				}
				out.lineBreak();
			}
			out.endArray();
		}
	}

	public static void writeIdentityFont(PdfFragmentOutput out, XRef xref, CIDFontSource source, ObjectRef fontRef,
			short[] w, short[] w2, int[] unicodeArray) throws IOException {
		// 主フォント
		String fontName = source.getFontName();
		out.startObject(fontRef);
		out.startHash();
		out.writeName("Type");
		out.writeName("Font");
		out.lineBreak();
		out.writeName("Subtype");
		out.writeName("Type0");
		out.lineBreak();
		out.writeName("BaseFont");
		out.writeName(fontName);
		out.lineBreak();
		out.writeName("DescendantFonts");
		out.startArray();
		ObjectRef xfontRef = xref.nextObjectRef();
		out.writeObjectRef(xfontRef);
		out.endArray();
		out.lineBreak();
		out.writeName("Encoding");
		out.writeName((w2 != null) ? ENCODING_V : ENCODING_H);

		// ToUnicode
		ObjectRef toUnicodeRef = xref.nextObjectRef();
		out.lineBreak();
		out.writeName("ToUnicode");
		out.writeObjectRef(toUnicodeRef);
		out.endHash();
		out.endObject();

		out.startObject(toUnicodeRef);
		PdfOutput pout = new PdfOutput(out.startStream(PdfFragmentOutput.STREAM_ASCII), "ISO-8859-1");
		CIDUtils.writeIdentityToUnicode(pout, unicodeArray);
		out.endObject();

		// 拡張フォント
		out.startObject(xfontRef);
		out.startHash();
		out.writeName("Type");
		out.writeName("Font");
		out.lineBreak();
		out.writeName("Subtype");
		out.writeName("CIDFontType2");
		out.lineBreak();
		out.writeName("BaseFont");
		out.writeName(fontName);
		out.lineBreak();
		out.writeName("FontDescriptor");
		ObjectRef fontDescRef = xref.nextObjectRef();
		out.writeObjectRef(fontDescRef);
		out.lineBreak();
		out.writeName("CIDSystemInfo");
		out.startHash();
		out.writeName("Registry");
		out.writeString(REGISTRY);
		out.writeName("Ordering");
		out.writeString(ORDERING);
		out.writeName("Supplement");
		out.writeInt(SUPPLEMENT);
		out.lineBreak();
		out.writeName("CIDToGIDMap");
		out.writeName("Identity");
		out.lineBreak();
		out.endHash();

		// WArray
		{
			WArray warray = WArray.buildFromWidths(new ArrayShortMapIterator(w));
			CIDUtils.writeWArray(out, warray);
		}
		if (w2 != null && w2.length > 0) {
			WArray warray = WArray.buildFromWidths(new ArrayShortMapIterator(w2));
			CIDUtils.writeWArray2(out, warray);
		}

		out.endHash();
		out.endObject();

		// フォント情報
		out.startObject(fontDescRef);
		out.startHash();
		out.writeName("Type");
		out.writeName("FontDescriptor");
		out.lineBreak();
		out.writeName("FontName");
		out.writeName(fontName);
		out.lineBreak();
		writeFlagsAndPanose(out, source);
		out.writeName("FontBBox");
		BBox bbox = source.getBBox();
		out.startArray();
		out.writeInt(bbox.llx);
		out.writeInt(bbox.lly);
		out.writeInt(bbox.urx);
		out.writeInt(bbox.ury);
		out.endArray();
		out.lineBreak();
		out.writeName("StemV");
		out.writeInt(source.getStemV());
		out.lineBreak();
		out.writeName("ItalicAngle");
		out.writeInt(0);
		out.lineBreak();
		out.writeName("CapHeight");
		out.writeInt(source.getCapHeight());
		out.lineBreak();
		out.writeName("XHeight");
		out.writeInt(source.getXHeight());
		out.lineBreak();
		out.writeName("Ascent");
		out.writeInt(source.getAscent());
		out.lineBreak();
		out.writeName("Descent");
		out.writeInt(-source.getDescent());
		out.lineBreak();
		out.endHash();
		out.endObject();
	}

	private static void writeIdentityToUnicode(PdfOutput pout, int[] unicodeArray) throws IOException {
		ToUnicode toUnicode = ToUnicode.buildFromChars(unicodeArray);

		pout.writeName("CIDInit");
		pout.writeName("ProcSet");
		pout.writeOperator("findresource");
		pout.writeOperator("begin");
		pout.lineBreak();

		pout.writeInt(12);
		pout.writeOperator("dict");
		pout.writeOperator("begin");
		pout.lineBreak();

		pout.writeOperator("begincmap");
		pout.lineBreak();

		pout.writeName("CIDSystemInfo");
		pout.lineBreak();

		pout.startHash();

		pout.writeName("Registry");
		pout.writeString("Adobe");
		pout.lineBreak();

		pout.writeName("Ordering");
		pout.writeString("UCS");
		pout.lineBreak();

		pout.writeName("Supplement");
		pout.writeInt(0);
		pout.lineBreak();

		pout.endHash();
		pout.writeOperator("def");
		pout.lineBreak();

		pout.writeName("CMapName");
		pout.writeName("Adobe-Identity-UCS");
		pout.writeOperator("def");
		pout.lineBreak();

		pout.writeName("CMapType");
		pout.writeInt(2);
		pout.writeOperator("def");
		pout.lineBreak();

		pout.writeInt(1);
		pout.writeOperator("begincodespacerange");
		pout.lineBreak();

		pout.writeBytes16(0);
		pout.writeBytes16(0xFFFF);
		pout.lineBreak();

		pout.writeOperator("endcodespacerange");
		pout.lineBreak();

		Unicode[] unicodes = toUnicode.getUnicodes();
		pout.writeInt(unicodes.length);
		pout.writeOperator("beginbfrange");
		pout.lineBreak();

		for (int i = 0; i < unicodes.length; ++i) {
			Unicode u = unicodes[i];
			int[] chars = u.getUnicodes();
			if (chars.length == 1) {
				pout.writeBytes16(u.getFirstCode());
				pout.writeBytes16(u.getLastCode());
				pout.writeBytes16(chars[0]);
			} else {
				pout.writeBytes16(u.getFirstCode());
				pout.writeBytes16(u.getLastCode());
				pout.startArray();
				for (int j = 0; j < chars.length; ++j) {
					pout.writeBytes16(chars[j]);
				}
				pout.endArray();
			}
			pout.lineBreak();
		}

		pout.writeOperator("endbfrange");
		pout.lineBreak();

		pout.writeOperator("endcmap");
		pout.lineBreak();

		pout.writeOperator("CMapName");
		pout.writeOperator("currentdict");
		pout.writeName("CMap");
		pout.writeOperator("defineresource");
		pout.writeOperator("pop");
		pout.lineBreak();

		pout.writeOperator("end");
		pout.writeOperator("end");
		pout.lineBreak();

		pout.close();
	}

	/**
	 * 埋め込みフォントを書き出します。
	 * 
	 * @param out
	 * @param xref
	 * @param source
	 * @param font
	 * @param fontRef
	 * @param w
	 * @param w2
	 * @param unicodeArray CIDからユニコードへのマッピング
	 * @throws IOException
	 */
	public static void writeEmbeddedFont(PdfFragmentOutput out, XRef xref, CIDFontSource source, PdfEmbeddedFont font,
			ObjectRef fontRef, short[] w, short[] w2, int[] unicodeArray) throws IOException {
		// 埋め込み擬似タグ
		String subsetName;
		{
			int on = fontRef.objectNumber;
			char a = (char) ('A' + (on & 0xF));
			char b = (char) ('A' + ((on >> 4) & 0xF));
			char c = (char) ('A' + ((on >> 8) & 0xF));
			char d = (char) ('A' + ((on >> 12) & 0xF));
			char e = (char) ('A' + ((on >> 16) & 0xF));
			char f = (char) ('A' + ((on >> 20) & 0xF));
			subsetName = "" + a + b + c + d + e + f + '+' + font.getPSName();
		}

		// 主フォント
		out.startObject(fontRef);
		out.startHash();
		out.writeName("Type");
		out.writeName("Font");
		out.lineBreak();
		out.writeName("Subtype");
		out.writeName("Type0");
		out.lineBreak();
		out.writeName("BaseFont");
		out.writeName(subsetName);
		out.lineBreak();
		out.writeName("DescendantFonts");
		out.startArray();
		ObjectRef xfontRef = xref.nextObjectRef();
		out.writeObjectRef(xfontRef);
		out.endArray();
		out.lineBreak();
		out.writeName("Encoding");
		out.writeName((w2 != null) ? ENCODING_V : ENCODING_H);

		// ToUnicode
		ObjectRef toUnicodeRef = xref.nextObjectRef();
		out.lineBreak();
		out.writeName("ToUnicode");
		out.writeObjectRef(toUnicodeRef);
		out.endHash();
		out.endObject();

		out.startObject(toUnicodeRef);
		PdfOutput pout = new PdfOutput(out.startStream(PdfFragmentOutput.STREAM_ASCII), "ISO-8859-1");
		CIDUtils.writeIdentityToUnicode(pout, unicodeArray);
		out.endObject();

		// 拡張フォント
		out.startObject(xfontRef);
		out.startHash();
		out.writeName("Type");
		out.writeName("Font");
		out.lineBreak();
		out.writeName("Subtype");
		out.writeName("CIDFontType0");
		out.lineBreak();
		out.writeName("BaseFont");
		out.writeName(subsetName);
		out.lineBreak();
		out.writeName("FontDescriptor");
		ObjectRef fontDescRef = xref.nextObjectRef();
		out.writeObjectRef(fontDescRef);
		out.lineBreak();
		out.writeName("CIDSystemInfo");
		out.startHash();
		out.writeName("Registry");
		out.writeString(CIDUtils.REGISTRY);
		out.writeName("Ordering");
		out.writeString(CIDUtils.ORDERING);
		out.writeName("Supplement");
		out.writeInt(CIDUtils.SUPPLEMENT);
		out.endHash();

		// WArray
		{
			WArray warray = WArray.buildFromWidths(new ArrayShortMapIterator(w));
			CIDUtils.writeWArray(out, warray);
		}
		if (w2 != null && w2.length > 0) {
			WArray warray = WArray.buildFromWidths(new ArrayShortMapIterator(w2));
			CIDUtils.writeWArray2(out, warray);
		}

		out.endHash();
		out.endObject();

		// フォント情報
		out.startObject(fontDescRef);
		out.startHash();
		out.writeName("Type");
		out.writeName("FontDescriptor");
		out.lineBreak();
		out.writeName("FontName");
		out.writeName(subsetName);
		out.lineBreak();
		writeFlagsAndPanose(out, source);
		out.writeName("FontBBox");
		BBox bbox = source.getBBox();
		out.startArray();
		out.writeInt(bbox.llx);
		out.writeInt(bbox.lly);
		out.writeInt(bbox.urx);
		out.writeInt(bbox.ury);
		out.endArray();
		out.lineBreak();
		out.writeName("StemV");
		out.writeInt(source.getStemV());
		out.lineBreak();
		out.writeName("ItalicAngle");
		out.writeInt(0);
		out.lineBreak();
		out.writeName("CapHeight");
		out.writeInt(source.getCapHeight());
		out.lineBreak();
		out.writeName("XHeight");
		out.writeInt(source.getXHeight());
		out.lineBreak();
		out.writeName("Ascent");
		out.writeInt(source.getAscent());
		out.lineBreak();
		out.writeName("Descent");
		out.writeInt(-source.getDescent());
		out.lineBreak();
		ObjectRef cidSetRef = xref.nextObjectRef();
		out.writeName("CIDSet");
		out.writeObjectRef(cidSetRef);
		out.lineBreak();
		ObjectRef fontFile3Ref = xref.nextObjectRef();
		out.writeName("FontFile3");
		out.writeObjectRef(fontFile3Ref);
		out.endHash();
		out.endObject();

		// CIDSet
		// 使用するCID
		out.startObject(cidSetRef);
		out.startHash();
		try (OutputStream sout = out.startStreamFromHash(PdfFragmentOutput.STREAM_BINARY)) {
			int bytes = (int) Math.ceil(unicodeArray.length / 8.0);
			for (int i = 0; i < bytes; ++i) {
				int start = i * 8;
				int end = start + 8;
				int b = 0;
				for (int j = start; j < end; ++j) {
					if (j < unicodeArray.length) {
						b |= (1 << (end - j - 1));
					}
				}
				sout.write(b);
			}
		}
		out.endObject();

		// CFF埋め込み
		out.startObject(fontFile3Ref);
		out.startHash();
		out.writeName("Subtype");
		out.writeName("CIDFontType0C");
		out.lineBreak();

		try (OutputStream cout = out.startStreamFromHash(PdfFragmentOutput.STREAM_BINARY)) {
//			InputStream in = new InflaterInputStream(new FileInputStream("/home/miyabe/workspaces/copper/CopperPDF.dev/files/misc/fontfile.bin"));
//			IOUtils.copy(in, cout);
			CffGenerator cff = new CffGenerator();
			cff.setSubsetName(subsetName);
			cff.setEmbedableFont(font);
			cff.writeTo(cout);
		}

		out.endObject();
	}
}
