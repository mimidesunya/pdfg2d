package net.zamasoft.pdfg2d.pdf.font;

import net.zamasoft.pdfg2d.font.FontSource;
import net.zamasoft.pdfg2d.pdf.ObjectRef;

public interface PDFFontSource extends FontSource {
	public static enum Type {
	/**
	 * 不明なフォントです。
	 */
	MISSING,

	/**
	 * コアフォントです。
	 */
	CORE,

	/**
	 * 埋め込みフォントです。
	 */
	EMBEDDED,

	/**
	 * 外部フォントです。
	 */
	CID_IDENTITY,

	/**
	 * CID-Keyedフォントです。
	 */
	CID_KEYED;
	}

	/**
	 * フォントの種類を返します。
	 * 
	 * @return
	 */
	public Type getType();

	public PDFFont createFont(String name, ObjectRef fontRef);
}
