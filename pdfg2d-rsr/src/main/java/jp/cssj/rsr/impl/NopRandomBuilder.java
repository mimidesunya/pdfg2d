package jp.cssj.rsr.impl;

import java.io.IOException;

import jp.cssj.rsr.RandomBuilder;

/**
 * 何も生成しないRandomBuilderです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class NopRandomBuilder implements RandomBuilder {
	private static final PositionInfo NULL_INFO = new PositionInfo() {
		public long getPosition(int id) {
			return 0;
		}
	};

	public static final NopRandomBuilder SHARED_INSTANCE = new NopRandomBuilder();

	private NopRandomBuilder() {
		// private
	}

	public PositionInfo getPositionInfo() {
		return NULL_INFO;
	}

	public boolean supportsPositionInfo() {
		return true;
	}

	public void addBlock() throws IOException {
		// ignore
	}

	public void insertBlockBefore(int anchorId) throws IOException {
		// ignore
	}

	public void write(int id, byte[] b, int off, int len) throws IOException {
		// ignore
	}

	public void closeBlock(int id) throws IOException {
		// ignore
	}

	public void close() throws IOException {
		// ignore
	}
}