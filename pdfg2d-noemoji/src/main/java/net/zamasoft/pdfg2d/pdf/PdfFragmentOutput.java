package net.zamasoft.pdfg2d.pdf;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: PdfFragmentOutput.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public abstract class PdfFragmentOutput extends PdfOutput {
	/** ストリームへ出力したデータをそのままPDFに書き込みます。 */
	public static final short STREAM_RAW = 0;

	/** バイナリデータに適した圧縮を行います。 */
	public static final short STREAM_BINARY = 1;

	/** テキストデータに適した圧縮を行います。 */
	public static final short STREAM_ASCII = 2;

	protected PdfFragmentOutput(OutputStream out, String nameEncoding) throws IOException {
		super(out, nameEncoding);
	}

	/**
	 * オブジェクトの開始を出力します。
	 * 
	 * @param ref
	 * @throws IOException
	 */
	public abstract void startObject(ObjectRef ref) throws IOException;

	/**
	 * オブジェクトの終端を出力します。
	 * 
	 * @throws IOException
	 */
	public abstract void endObject() throws IOException;

	/**
	 * ストリームの開始を出力します。
	 * 
	 * @param mode
	 *            STREAM_XXX値を渡します。
	 * @throws IOException
	 */
	public abstract OutputStream startStream(short mode) throws IOException;

	/**
	 * ハッシュ内からストリームの開始を出力します。 このメソッドはハッシュを閉じます。
	 * 
	 * @param mode
	 *            STREAM_XXX値を渡します。
	 * @throws IOException
	 */
	public abstract OutputStream startStreamFromHash(short mode) throws IOException;
}