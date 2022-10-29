package jp.cssj.resolver;

import java.io.IOException;
import java.net.URI;

/**
 * データのメタ情報です。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: MetaSource.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public interface MetaSource {
	/**
	 * データのURIを返します。
	 * 
	 * @return データの位置を示すURI。
	 */
	public URI getURI();

	/**
	 * データのMIME型を返します。不確定な場合はnull。
	 * 
	 * @return このデータのMIME型。
	 * @throws IOException
	 */
	public String getMimeType() throws IOException;

	/**
	 * キャラクタ・エンコーディングを返します。未定の場合はnullです。
	 * 
	 * @return このデータのキャラクタ・エンコーディング。
	 * @throws IOException
	 */
	public String getEncoding() throws IOException;

	/**
	 * データのサイズを返します。不明な場合は-1を返します。
	 * 
	 * @return データのバイト数。
	 * @throws IOException
	 */
	public long getLength() throws IOException;
}
