package jp.cssj.rsr.helpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.cssj.rsr.RandomBuilder;

/**
 * getPositionInfo を実装しない RandomBuilder を getPositionInfo を実装するようにするための
 * RandomBuilder のラッパーです。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: RandomBuilderPositionSupport.java 656 2011-09-03 15:42:28Z
 *          miyabe $
 */
public class RandomBuilderPositionSupport extends RandomBuilderWrapper implements RandomBuilder {
	private static final boolean DEBUG = false;

	private List<Fragment> frgs = new ArrayList<Fragment>();

	private Fragment first = null, last = null;

	private class Fragment {
		public final int id;

		public Fragment prev = null, next = null;

		public long length = 0;

		public Fragment(int id) {
			this.id = id;
		}
	}

	public RandomBuilderPositionSupport(RandomBuilder builder) {
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
		return new PositionInfo() {
			public long getPosition(int id) {
				long position = idToPosition[id];
				return position;
			}
		};
	}

	public void addBlock() throws IOException {
		int id = this.nextId();
		Fragment frg = new Fragment(id);
		if (this.first == null) {
			this.first = frg;
		} else {
			this.last.next = frg;
			frg.prev = this.last;
		}
		this.putFragment(id, frg);
		this.last = frg;
		super.addBlock();
	}

	public void insertBlockBefore(int anchorId) throws IOException {
		int id = this.nextId();
		Fragment anchor = this.getFragment(anchorId);
		Fragment frg = new Fragment(id);
		this.putFragment(id, frg);
		frg.prev = anchor.prev;
		frg.next = anchor;
		anchor.prev.next = frg;
		anchor.prev = frg;
		if (this.first == anchor) {
			this.first = frg;
		}
		super.insertBlockBefore(anchorId);
	}

	public void write(int id, byte[] b, int off, int len) throws IOException {
		Fragment frg = this.getFragment(id);
		frg.length += len;
		super.write(id, b, off, len);
	}

	public void closeBlock(int id) throws IOException {
		super.closeBlock(id);
	}

	public void finish() throws IOException {
		if (DEBUG) {
			int total = this.frgs.size();
			System.out.println(total + "個のフラグメントが生成されました。");
		}
		this.first = null;
		this.last = null;
		this.frgs.clear();
		super.finish();
	}
}