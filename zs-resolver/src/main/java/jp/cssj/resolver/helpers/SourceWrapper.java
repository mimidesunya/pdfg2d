package jp.cssj.resolver.helpers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;

import jp.cssj.resolver.Source;
import jp.cssj.resolver.SourceValidity;

/**
 * Source のラッパクラスです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: SourceWrapper.java 1592 2019-12-03 06:59:47Z miyabe $
 */
public class SourceWrapper implements Source {
	protected final Source source;

	public SourceWrapper(Source source) {
		this.source = source;
	}

	public boolean exists() throws IOException {
		return this.source.exists();
	}

	public String getEncoding() throws IOException {
		return this.source.getEncoding();
	}

	public File getFile() {
		return this.source.getFile();
	}

	public InputStream getInputStream() throws IOException {
		return this.source.getInputStream();
	}

	public long getLength() throws IOException {
		return this.source.getLength();
	}

	public String getMimeType() throws IOException {
		return this.source.getMimeType();
	}

	public Reader getReader() throws IOException {
		return this.source.getReader();
	}

	public URI getURI() {
		return this.source.getURI();
	}

	public SourceValidity getValidity() throws IOException {
		return this.source.getValidity();
	}

	public boolean isFile() throws IOException {
		return this.source.isFile();
	}

	public boolean isInputStream() throws IOException {
		return this.source.isInputStream();
	}

	public boolean isReader() throws IOException {
		return this.source.isReader();
	}
	
	public String toString() {
		return this.source.toString();
	}
}
