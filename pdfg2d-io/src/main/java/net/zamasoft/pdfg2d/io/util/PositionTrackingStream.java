package net.zamasoft.pdfg2d.io.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.zamasoft.pdfg2d.io.FragmentedStream;

/**
 * getPositionInfo を実装しない RandomBuilder を getPositionInfo を実装するようにするための
 * RandomBuilder のラッパーです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: RandomBuilderPositionSupport.java 656 2011-09-03 15:42:28Z
 *          miyabe $
 */
public class PositionTrackingStream extends StreamWrapper {
	private static final boolean DEBUG = false;

	private final List<Fragment> frgs = new ArrayList<>();

	private Fragment first = null, last = null;

	private static class Fragment {
		public final int id;

		public Fragment prev = null, next = null;

		public long length = 0;

		public Fragment(int id) {
			this.id = id;
		}
	}

	public PositionTrackingStream(FragmentedStream builder) {
		super(builder);
		assert !builder.supportsPositionInfo();
	}

	protected int nextId() {
		return this.frgs.size();
	}

	protected Fragment getFragment(int id) {
		return (Fragment) this.frgs.get(id);
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
			// System.out.println(frg.id+"/"+position);
			idToPosition[frg.id] = position;
			position += frg.length;
			frg = frg.next;
		}
		return id -> idToPosition[id];
	}

	public void addFragment() throws IOException {
		int id = this.nextId();
		var frg = new Fragment(id);
		if (this.first == null) {
			this.first = frg;
		} else {
			this.last.next = frg;
			frg.prev = this.last;
		}
		this.putFragment(id, frg);
		this.last = frg;
		super.addFragment();
	}

	public void insertFragmentBefore(int anchorId) throws IOException {
		int id = this.nextId();
		Fragment anchor = this.getFragment(anchorId);
		var frg = new Fragment(id);
		this.putFragment(id, frg);
		frg.prev = anchor.prev;
		frg.next = anchor;
		anchor.prev.next = frg;
		anchor.prev = frg;
		if (this.first == anchor) {
			this.first = frg;
		}
		super.insertFragmentBefore(anchorId);
	}

	public void write(int id, byte[] b, int off, int len) throws IOException {
		Fragment frg = this.getFragment(id);
		frg.length += len;
		super.write(id, b, off, len);
	}

	public void finishFragment(int id) throws IOException {
		super.finishFragment(id);
	}

	public void close() throws IOException {
		try {
			if (DEBUG) {
				int total = this.frgs.size();
				System.out.println(total + "個のフラグメントが生成されました。");
			}
			this.first = null;
			this.last = null;
			this.frgs.clear();
			super.close();
		} finally {
			this.builder.close();
		}
	}
}