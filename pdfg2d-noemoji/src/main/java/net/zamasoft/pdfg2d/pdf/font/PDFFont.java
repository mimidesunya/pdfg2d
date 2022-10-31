package net.zamasoft.pdfg2d.pdf.font;

import java.io.IOException;
import java.io.Serializable;

import net.zamasoft.pdfg2d.font.Font;
import net.zamasoft.pdfg2d.pdf.PDFFragmentOutput;
import net.zamasoft.pdfg2d.pdf.XRef;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface PDFFont extends Font, Serializable {
	/**
	 * PDF文書内での識別に使われるフォント名を返します。
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * フォント情報をPDFオブジェクトとして出力します。
	 * 
	 * @param out
	 * @param xref
	 * @throws IOException
	 */
	public void writeTo(PDFFragmentOutput out, XRef xref) throws IOException;
}
