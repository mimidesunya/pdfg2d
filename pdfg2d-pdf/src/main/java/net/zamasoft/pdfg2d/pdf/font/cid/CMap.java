package net.zamasoft.pdfg2d.pdf.font.cid;

import java.io.IOException;
import java.io.Serializable;

import jp.cssj.resolver.Source;

/**
 * 一般CIDフォントのキャラクタマッピング情報です。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class CMap implements Serializable {
	private static final long serialVersionUID = 0;

	protected final CIDTable cidTable;

	protected String encoding;

	protected String registry, ordering;

	protected int supplement;

	public CMap(Source source, String javaEncoding) throws IOException {
		this.cidTable = new CIDTable(source, javaEncoding);
		CMapParser parser = new CMapParser();
		parser.parse(source.getInputStream(), this);
	}

	/**
	 * CIDテーブルを返します。
	 * 
	 * @return
	 */
	public CIDTable getCIDTable() {
		return this.cidTable;
	}

	/**
	 * PDFエンコーディング名を返します。
	 * 
	 * @return
	 */
	public String getEncoding() {
		return this.encoding;
	}

	/**
	 * PDFレジストリ名を返します。
	 * 
	 * @return
	 */
	public String getRegistry() {
		return this.registry;
	}

	/**
	 * PDFオーダリングを返します。
	 * 
	 * @return
	 */
	public String getOrdering() {
		return this.ordering;
	}

	/**
	 * PDFサプリメント番号を返します。
	 * 
	 * @return
	 */
	public int getSupplement() {
		return this.supplement;
	}
}