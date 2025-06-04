package jp.cssj.resolver;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;

/**
 * ファイル、ウェブ上の文書等のデータの源です。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface Source extends MetaSource, Closeable {
	/**
	 * データのURIを返します。
	 * 
	 * @return データの位置を示すURI。
	 */
	public URI getURI();

	/**
	 * データが存在すればtrueを返します。
	 * 
	 * @return データが存在すればtrue、そうでなければfalse。
	 * @throws IOException
	 */
	public boolean exists() throws IOException;

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
	 * バイナリデータとして取得可能ならtrueを返します。
	 * 
	 * @return バイナリデータを取得できる場合はtrue。
	 * @throws IOException
	 */
	public boolean isInputStream() throws IOException;

	/**
	 * バイナリストリームを返します。
	 * 
	 * @return データのバイナリストリーム。
	 * @throws IOException
	 */
	public InputStream getInputStream() throws IOException,
			UnsupportedOperationException;

	/**
	 * テキストデータとして取得可能ならtrueを返します。
	 * 
	 * @return テキストデータとして取得できる場合はtrue。
	 * @throws IOException
	 */
	public boolean isReader() throws IOException;

	/**
	 * テキストストリームを返します。
	 * 
	 * @return データのテキストストリーム。
	 * @throws IOException
	 */
	public Reader getReader() throws IOException, UnsupportedOperationException;

	/**
	 * ファイルならtrueを返します。
	 * 
	 * @return データがファイルであればtrue。
	 * @throws IOException
	 */
	public boolean isFile() throws IOException;

	/**
	 * ファイルとして返します。
	 * 
	 * @return データが格納されたファイル。
	 */
	public File getFile() throws UnsupportedOperationException;

	/**
	 * データの変更情報を返します。
	 * 
	 * @return データの変更状況を表すSourceValidity。
	 * @throws IOException
	 */
	public SourceValidity getValidity() throws IOException;

	/**
	 * データのサイズを返します。不明な場合は-1を返します。
	 * 
	 * @return データのバイト数。
	 * @throws IOException
	 */
	public long getLength() throws IOException;
}
