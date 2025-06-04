package jp.cssj.resolver.helpers;

import java.io.IOException;
import java.net.URI;

import jp.cssj.resolver.Source;

/**
 * 一般的な Source の抽象クラスです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public abstract class AbstractSource implements Source {
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
