package jp.cssj.rsr.helpers;

import java.io.IOException;

import jp.cssj.rsr.RandomBuilder;

/**
 * RandomBuilder のラッパークラスです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class RandomBuilderWrapper implements RandomBuilder {
	protected final RandomBuilder builder;

	public RandomBuilderWrapper(RandomBuilder builder) {
		this.builder = builder;
	}

	public void addBlock() throws IOException {
		this.builder.addBlock();
	}

	public void insertBlockBefore(int anchorId) throws IOException {
		this.builder.insertBlockBefore(anchorId);
	}

	public void write(int id, byte[] b, int off, int len) throws IOException {
		this.builder.write(id, b, off, len);
	}

	public void closeBlock(int id) throws IOException {
		this.builder.closeBlock(id);
	}

	public void close() throws IOException {
		this.builder.close();
	}

	public PositionInfo getPositionInfo() {
		return this.builder.getPositionInfo();
	}

	public boolean supportsPositionInfo() {
		return this.builder.supportsPositionInfo();
	}
}