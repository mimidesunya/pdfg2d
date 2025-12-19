package net.zamasoft.pdfg2d.io.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.zamasoft.pdfg2d.io.FragmentedOutput;

/**
 * Wrapper for FragmentedOutput that adds position tracking support for
 * implementations
 * that don't support it natively.
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: RandomBuilderPositionSupport.java 656 2011-09-03 15:42:28Z
 *          miyabe $
 */
public class PositionTrackingOutput extends FragmentedOutputWrapper {
	private static final boolean DEBUG = false;

	private final List<FragmentInfo> fragments = new ArrayList<>();

	private FragmentInfo first = null, last = null;

	private static class FragmentInfo {
		public final int id;

		public FragmentInfo prev = null, next = null;

		public long fragmentLength = 0;

		public FragmentInfo(final int id) {
			this.id = id;
		}
	}

	public PositionTrackingOutput(final FragmentedOutput builder) {
		super(builder);
		assert !builder.supportsPositionInfo();
	}

	protected int nextId() {
		return this.fragments.size();
	}

	protected FragmentInfo getFragment(final int id) {
		return this.fragments.get(id);
	}

	protected void putFragment(final int id, final FragmentInfo fragment) {
		assert (id == this.fragments.size());
		this.fragments.add(fragment);
	}

	@Override
	public PositionInfo getPositionInfo() {
		final long[] idToPosition = new long[this.fragments.size()];
		long position = 0;
		var fragment = this.first;
		while (fragment != null) {
			idToPosition[fragment.id] = position;
			position += fragment.fragmentLength;
			fragment = fragment.next;
		}
		return id -> idToPosition[id];
	}

	@Override
	public boolean supportsPositionInfo() {
		return true;
	}

	@Override
	public void addFragment() throws IOException {
		final int id = this.nextId();
		final var fragment = new FragmentInfo(id);
		if (this.first == null) {
			this.first = fragment;
		} else {
			this.last.next = fragment;
			fragment.prev = this.last;
		}
		this.putFragment(id, fragment);
		this.last = fragment;
		super.addFragment();
	}

	@Override
	public void insertFragmentBefore(final int anchorId) throws IOException {
		final int id = this.nextId();
		final var anchor = this.getFragment(anchorId);
		final var fragment = new FragmentInfo(id);
		this.putFragment(id, fragment);
		fragment.prev = anchor.prev;
		fragment.next = anchor;
		if (anchor.prev != null) {
			anchor.prev.next = fragment;
		}
		anchor.prev = fragment;
		if (this.first == anchor) {
			this.first = fragment;
		}
		super.insertFragmentBefore(anchorId);
	}

	@Override
	public void write(final int id, final byte[] b, final int off, final int len) throws IOException {
		final var fragment = this.getFragment(id);
		fragment.fragmentLength += len;
		super.write(id, b, off, len);
	}

	@Override
	public void finishFragment(final int id) throws IOException {
		super.finishFragment(id);
	}

	@Override
	public void close() throws IOException {
		try {
			if (DEBUG) {
				final int total = this.fragments.size();
				System.out.println(total + " fragments were generated.");
			}
			this.first = null;
			this.last = null;
			this.fragments.clear();
		} finally {
			super.close();
		}
	}
}