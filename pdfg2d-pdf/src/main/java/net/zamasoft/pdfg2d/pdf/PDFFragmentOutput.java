package net.zamasoft.pdfg2d.pdf;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public abstract class PDFFragmentOutput extends PDFOutput {
	public static enum Mode {
		/** ストリームへ出力したデータをそのままPDFに書き込みます。 */
		RAW,
		/** バイナリデータに適した圧縮を行います。 */
		BINARY,
		/** テキストデータに適した圧縮を行います。 */
		ASCII;
	}

	protected PDFFragmentOutput(OutputStream out, String nameEncoding) throws IOException {
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
	 * @throws IOException
	 */
	public abstract OutputStream startStream(Mode mode) throws IOException;

	/**
	 * ハッシュ内からストリームの開始を出力します。 このメソッドはハッシュを閉じます。
	 * 
	 * @param mode
	 * @throws IOException
	 */
	public abstract OutputStream startStreamFromHash(Mode mode) throws IOException;
}