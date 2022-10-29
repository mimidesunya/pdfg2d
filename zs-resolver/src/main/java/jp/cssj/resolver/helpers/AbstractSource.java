package jp.cssj.resolver.helpers;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;

import jp.cssj.resolver.Source;

/**
 * 一般的な Source の抽象クラスです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: AbstractSource.java 1592 2019-12-03 06:59:47Z miyabe $
 */
public abstract class AbstractSource implements Source, Closeable {
	protected final URI uri;

	public AbstractSource(URI uri) {
		this.uri = uri;
	}

	public URI getURI() {
		return this.uri;
	}

	public boolean isInputStream() throws IOException {
		return false;
	}

	public boolean isFile() throws IOException {
		return false;
	}

	public boolean isReader() throws IOException {
		return false;
	}

	public void close() {
		// ignore
	}
	
	public String toString() {
		return this.uri.toString();
	}
}
