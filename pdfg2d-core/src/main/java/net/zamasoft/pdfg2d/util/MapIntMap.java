package net.zamasoft.pdfg2d.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class MapIntMap implements IntMap {
	protected Map<Integer, Integer> map = new TreeMap<Integer, Integer>();

	public void set(int key, int value) {
		this.map.put(key, value);
	}

	public int get(int key) {
		return this.map.get(key);
	}

	public boolean contains(int key) {
		return this.map.containsKey(key);
	}

	public IntMapIterator getIterator() {
		final Iterator<Entry<Integer, Integer>> i = this.map.entrySet().iterator();
		return new IntMapIterator() {
			private Entry<Integer, Integer> e;

			@Override
			public boolean next() {
				if (!i.hasNext()) {
					return false;
				}
				this.e = i.next();
				return true;
			}

			@Override
			public int key() {
				return e.getKey();
			}

			@Override
			public int value() {
				return e.getValue();
			}
		};
	}
}
