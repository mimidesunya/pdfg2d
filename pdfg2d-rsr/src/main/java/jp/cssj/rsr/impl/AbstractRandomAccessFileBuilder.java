package jp.cssj.rsr.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jp.cssj.rsr.RandomBuilder;

/**
 * 一時的に作成したランダムアクセスファイルを使って結果を構築する RandomBuilder です。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: AbstractRandomAccessFileBuilder.java 758 2011-11-13 14:05:46Z
 *          miyabe $
 */
public abstract class AbstractRandomAccessFileBuilder implements RandomBuilder {
	private static final Logger LOG = Logger.getLogger(AbstractRandomAccessFileBuilder.class.getName());

	/**
	 * セグメントのサイズです。
	 */
	private static final int SEGMENT_SIZE = 8192;

	/**
	 * フラグメントバッファの最大サイズ、その合計の最大サイズ、クローズ時にディスクに落とすときの敷居値
	 */
	private final int blockBufferSize, totalBufferSize, threshold;

	protected RandomAccessFile raf = null;

	protected File file = null;

	protected List<Block> frgs = null;

	protected Block first = null, last = null;

	protected long length = 0, onMemory = 0;

	protected int segment = 0;

	private byte[] buff = null;

	protected class Block {
		public Block prev = null, next = null;

		private final int id;

		private int len = 0;

		private byte[] buffer = null;

		private IntList segments;

		private int segLen = 0;

		public Block(int id) {
			this.id = id;
		}

		public int getId() {
			return this.id;
		}

		public int getLength() {
			return this.len;
		}

		public void write(byte[] buff, int pos, int len) throws IOException {
			if (this.segments == null && (this.len + len) < blockBufferSize
					&& (AbstractRandomAccessFileBuilder.this.onMemory + blockBufferSize) <= totalBufferSize) {
				if (this.buffer == null) {
					this.buffer = new byte[blockBufferSize];
					AbstractRandomAccessFileBuilder.this.onMemory += blockBufferSize;
				}
				System.arraycopy(buff, pos, this.buffer, this.len, len);
			} else {
				if (this.buffer != null) {
					this.rafWrite(this.buffer, 0, this.len);
					AbstractRandomAccessFileBuilder.this.onMemory -= blockBufferSize;
					this.buffer = null;
				}
				this.rafWrite(buff, pos, len);
			}
			this.len += len;
		}

		private void rafWrite(byte[] buff, int off, int len) throws IOException {
			if (this.segments == null) {
				this.segments = new IntList(10);
				this.segments.add(AbstractRandomAccessFileBuilder.this.segment++);
			}
			while (len > 0) {
				if (this.segLen == AbstractRandomAccessFileBuilder.SEGMENT_SIZE) {
					this.segments.add(AbstractRandomAccessFileBuilder.this.segment++);
					this.segLen = 0;
				}
				int seg = this.segments.get(this.segments.size() - 1);
				int wlen = Math.min(len, AbstractRandomAccessFileBuilder.SEGMENT_SIZE - this.segLen);
				long wpos = (long) seg * (long) AbstractRandomAccessFileBuilder.SEGMENT_SIZE + (long) this.segLen;
				AbstractRandomAccessFileBuilder.this.raf.seek(wpos);
				AbstractRandomAccessFileBuilder.this.raf.write(buff, off, wlen);
				this.segLen += wlen;
				off += wlen;
				len -= wlen;
			}
		}

		public void close() throws IOException {
			if (this.buffer != null) {
				if (this.len >= AbstractRandomAccessFileBuilder.this.threshold) {
					this.rafWrite(this.buffer, 0, this.len);
					AbstractRandomAccessFileBuilder.this.onMemory -= blockBufferSize;
					this.buffer = null;
				} else if (this.len < this.buffer.length) {
					byte[] temp = new byte[this.len];
					System.arraycopy(this.buffer, 0, temp, 0, temp.length);
					AbstractRandomAccessFileBuilder.this.onMemory -= (this.buffer.length - this.len);
					this.buffer = temp;
				}
			}
		}

		public void writeTo(OutputStream out) throws IOException {
			if (this.segments == null) {
				if (this.buffer != null) {
					out.write(this.buffer, 0, this.len);
				}
			} else {
				if (AbstractRandomAccessFileBuilder.this.buff == null) {
					AbstractRandomAccessFileBuilder.this.buff = new byte[AbstractRandomAccessFileBuilder.SEGMENT_SIZE];
				}
				byte[] buff = AbstractRandomAccessFileBuilder.this.buff;
				for (int i = 0; i < this.segments.size() - 1; ++i) {
					int seg = this.segments.get(i);
					long rpos = (long) seg * (long) AbstractRandomAccessFileBuilder.SEGMENT_SIZE;
					AbstractRandomAccessFileBuilder.this.raf.seek(rpos);
					AbstractRandomAccessFileBuilder.this.raf.readFully(buff);
					out.write(buff);
				}
				int seg = this.segments.get(this.segments.size() - 1);
				long rpos = (long) seg * (long) AbstractRandomAccessFileBuilder.SEGMENT_SIZE;
				AbstractRandomAccessFileBuilder.this.raf.seek(rpos);
				AbstractRandomAccessFileBuilder.this.raf.readFully(buff, 0, this.segLen);
				out.write(buff, 0, this.segLen);
			}
		}

		public void dispose() {
			this.buffer = null;
		}
	}

	public AbstractRandomAccessFileBuilder(int fragmentBufferSize, int totalBufferSize, int threshold) {
		this.blockBufferSize = fragmentBufferSize;
		this.totalBufferSize = totalBufferSize;
		this.threshold = threshold;
	}

	public AbstractRandomAccessFileBuilder() {
		this(8192, 1024 * 1024 * 2, 1024);
	}

	protected int nextId() throws IOException {
		if (this.frgs == null) {
			this.frgs = new ArrayList<Block>();
			this.file = File.createTempFile("cssj-rsr-", ".frgs");
			this.file.deleteOnExit();
			this.raf = new RandomAccessFile(this.file, "rw");
		}
		return this.frgs.size();
	}

	protected Block getBlock(int id) throws IOException {
		return (Block) this.frgs.get(id);
	}

	protected void putBlock(int id, Block frg) {
		assert (id == this.frgs.size());
		this.frgs.add(frg);
	}

	public PositionInfo getPositionInfo() {
		final long[] idToPosition = new long[this.frgs.size()];
		long position = 0;
		Block frg = this.first;
		while (frg != null) {
			// System.out.println(frg.getId()+"/"+position);
			idToPosition[frg.getId()] = position;
			position += frg.getLength();
			frg = frg.next;
		}
		return new PositionInfo() {
			public long getPosition(int id) {
				long position = idToPosition[id];
				return position;
			}
		};
	}

	public boolean supportsPositionInfo() {
		return true;
	}

	public void addBlock() throws IOException {
		int id = this.nextId();
		Block bk = new Block(id);
		if (this.first == null) {
			this.first = bk;
		} else {
			this.last.next = bk;
			bk.prev = this.last;
		}
		this.putBlock(id, bk);
		this.last = bk;
	}

	public void insertBlockBefore(int anchorId) throws IOException {
		int id = this.nextId();
		Block anchor = this.getBlock(anchorId);
		Block bk = new Block(id);
		this.putBlock(id, bk);
		bk.prev = anchor.prev;
		bk.next = anchor;
		anchor.prev.next = bk;
		anchor.prev = bk;
		if (this.first == anchor) {
			this.first = bk;
		}
	}

	public void write(int id, byte[] b, int off, int len) throws IOException {
		Block frg = this.getBlock(id);
		frg.write(b, off, len);
		this.length += len;
	}

	public void closeBlock(int id) throws IOException {
		Block frg = this.getBlock(id);
		frg.close();
	}

	protected void finish(OutputStream out) throws IOException {
		if (this.first == null) {
			// 空
			this.clean();
			return;
		}
		if (LOG.isLoggable(Level.FINE)) {
			int total = this.frgs.size();
			int onMemory = 0;
			for (int i = 0; i < total; ++i) {
				Block f = (Block) this.frgs.get(i);
				if (f.segments == null) {
					++onMemory;
				}
			}
			LOG.fine(total + "個のフラグメントが生成されました。");
			LOG.fine("うち" + onMemory + "個がオンメモリーで、" + (total - onMemory) + "個がディスク上にあります。");
		}

		Block bk = this.first;
		while (bk != null) {
			bk.writeTo(out);
			bk.dispose();
			bk = bk.next;
		}
		this.clean();
	}

	public long getLength() {
		return this.length;
	}

	private void clean() {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("リソースを片付けます。");
		}
		if (this.raf != null) {
			try {
				this.raf.close();
			} catch (Exception e) {
				LOG.log(Level.FINE, "一時ファイルをクローズできませんでした。", e);
			}
			this.raf = null;
		}
		if (this.file != null) {
			try {
				this.file.delete();
			} catch (Exception e) {
				LOG.log(Level.FINE, "一時ファイルを削除できませんでした。", e);
			}
			this.file = null;
		}
		this.first = null;
		this.last = null;
		this.frgs = null;
		this.length = 0;
		this.onMemory = 0;
		this.segment = 0;
	}

	public void close() throws IOException {
		if (this.first != null || this.raf != null || this.file != null) {
			this.clean();
		}
	}
}