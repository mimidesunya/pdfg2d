package net.zamasoft.pdfg2d.io.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.zamasoft.pdfg2d.io.FragmentedStream;

/**
 * 一時的に作成したランダムアクセスファイルを使って結果を構築する RandomBuilder です。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: AbstractRandomAccessFileBuilder.java 758 2011-11-13 14:05:46Z
 *          miyabe $
 */
public abstract class AbstractTempFileStream implements FragmentedStream {
	private static final Logger LOG = Logger.getLogger(AbstractTempFileStream.class.getName());

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

	protected List<Fragment> frgs = null;

	protected Fragment first = null, last = null;

	protected long length = 0, onMemory = 0;

	protected int segment = 0;

	private byte[] buff = null;

	protected class Fragment {
		public Fragment prev = null, next = null;

		private final int id;

		private int len = 0;

		private byte[] buffer = null;

		private IntList segments;

		private int segLen = 0;

		public Fragment(int id) {
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
					&& (AbstractTempFileStream.this.onMemory + blockBufferSize) <= totalBufferSize) {
				if (this.buffer == null) {
					this.buffer = new byte[blockBufferSize];
					AbstractTempFileStream.this.onMemory += blockBufferSize;
				}
				System.arraycopy(buff, pos, this.buffer, this.len, len);
			} else {
				if (this.buffer != null) {
					this.rafWrite(this.buffer, 0, this.len);
					AbstractTempFileStream.this.onMemory -= blockBufferSize;
					this.buffer = null;
				}
				this.rafWrite(buff, pos, len);
			}
			this.len += len;
		}

		private void rafWrite(byte[] buff, int off, int len) throws IOException {
			if (this.segments == null) {
				this.segments = new IntList(10);
				this.segments.add(AbstractTempFileStream.this.segment++);
			}
			while (len > 0) {
				if (this.segLen == AbstractTempFileStream.SEGMENT_SIZE) {
					this.segments.add(AbstractTempFileStream.this.segment++);
					this.segLen = 0;
				}
				int seg = this.segments.get(this.segments.size() - 1);
				int wlen = Math.min(len, AbstractTempFileStream.SEGMENT_SIZE - this.segLen);
				long wpos = (long) seg * (long) AbstractTempFileStream.SEGMENT_SIZE + (long) this.segLen;
				AbstractTempFileStream.this.raf.seek(wpos);
				AbstractTempFileStream.this.raf.write(buff, off, wlen);
				this.segLen += wlen;
				off += wlen;
				len -= wlen;
			}
		}

		public void close() throws IOException {
			if (this.buffer != null) {
				if (this.len >= AbstractTempFileStream.this.threshold) {
					this.rafWrite(this.buffer, 0, this.len);
					AbstractTempFileStream.this.onMemory -= blockBufferSize;
					this.buffer = null;
				} else if (this.len < this.buffer.length) {
					byte[] temp = new byte[this.len];
					System.arraycopy(this.buffer, 0, temp, 0, temp.length);
					AbstractTempFileStream.this.onMemory -= (this.buffer.length - this.len);
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
				if (AbstractTempFileStream.this.buff == null) {
					AbstractTempFileStream.this.buff = new byte[AbstractTempFileStream.SEGMENT_SIZE];
				}
				byte[] buff = AbstractTempFileStream.this.buff;
				for (int i = 0; i < this.segments.size() - 1; ++i) {
					int seg = this.segments.get(i);
					long rpos = (long) seg * (long) AbstractTempFileStream.SEGMENT_SIZE;
					AbstractTempFileStream.this.raf.seek(rpos);
					AbstractTempFileStream.this.raf.readFully(buff);
					out.write(buff);
				}
				int seg = this.segments.get(this.segments.size() - 1);
				long rpos = (long) seg * (long) AbstractTempFileStream.SEGMENT_SIZE;
				AbstractTempFileStream.this.raf.seek(rpos);
				AbstractTempFileStream.this.raf.readFully(buff, 0, this.segLen);
				out.write(buff, 0, this.segLen);
			}
		}

		public void dispose() {
			this.buffer = null;
		}
	}

	public AbstractTempFileStream(int fragmentBufferSize, int totalBufferSize, int threshold) {
		this.blockBufferSize = fragmentBufferSize;
		this.totalBufferSize = totalBufferSize;
		this.threshold = threshold;
	}

	public AbstractTempFileStream() {
		this(8192, 1024 * 1024 * 2, 1024);
	}

	protected int nextId() throws IOException {
		if (this.frgs == null) {
			this.frgs = new ArrayList<>();
			this.file = File.createTempFile("cssj-rsr-", ".frgs");
			this.file.deleteOnExit();
			this.raf = new RandomAccessFile(this.file, "rw");
		}
		return this.frgs.size();
	}

	protected Fragment getFragment(int id) throws IOException {
		return this.frgs.get(id);
	}

	protected void putFragment(int id, Fragment frg) {
		assert (id == this.frgs.size());
		this.frgs.add(frg);
	}

	public PositionInfo getPositionInfo() {
		final long[] idToPosition = new long[this.frgs.size()];
		long position = 0;
		Fragment frg = this.first;
		while (frg != null) {
			// System.out.println(frg.getId()+"/"+position);
			idToPosition[frg.getId()] = position;
			position += frg.getLength();
			frg = frg.next;
		}
		return id -> idToPosition[id];
	}

	public boolean supportsPositionInfo() {
		return true;
	}

	public void addFragment() throws IOException {
		int id = this.nextId();
		var bk = new Fragment(id);
		if (this.first == null) {
			this.first = bk;
		} else {
			this.last.next = bk;
			bk.prev = this.last;
		}
		this.putFragment(id, bk);
		this.last = bk;
	}

	public void insertFragmentBefore(int anchorId) throws IOException {
		int id = this.nextId();
		var anchor = this.getFragment(anchorId);
		var bk = new Fragment(id);
		this.putFragment(id, bk);
		bk.prev = anchor.prev;
		bk.next = anchor;
		anchor.prev.next = bk;
		anchor.prev = bk;
		if (this.first == anchor) {
			this.first = bk;
		}
	}

	public void write(int id, byte[] b, int off, int len) throws IOException {
		var frg = this.getFragment(id);
		frg.write(b, off, len);
		this.length += len;
	}

	public void finishFragment(int id) throws IOException {
		var frg = this.getFragment(id);
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
				var f = this.frgs.get(i);
				if (f.segments == null) {
					++onMemory;
				}
			}
			LOG.fine(total + "個のフラグメントが生成されました。");
			LOG.fine("うち" + onMemory + "個がオンメモリーで、" + (total - onMemory) + "個がディスク上にあります。");
		}

		var bk = this.first;
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