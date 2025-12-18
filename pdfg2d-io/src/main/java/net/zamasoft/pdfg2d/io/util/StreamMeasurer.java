package net.zamasoft.pdfg2d.io.util;

import java.io.IOException;

import net.zamasoft.pdfg2d.io.FragmentedStream;

/**
 * 構築中のデータ全体の大きさを計測します。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class StreamMeasurer extends StreamWrapper {
	protected long length;

	public StreamMeasurer(FragmentedStream builder) {
		super(builder);
	}

	public void write(int id, byte[] b, int off, int len) throws IOException {
		super.write(id, b, off, len);
		this.length += len;
	}

	/**
	 * コンテンツの大きさを返します。
	 * 
	 * @return 追加済みデータの合計バイト数。
	 */
	public long getLength() {
		return this.length;
	}
}
