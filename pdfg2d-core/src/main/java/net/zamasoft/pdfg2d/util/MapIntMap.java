package net.zamasoft.pdfg2d.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class MapIntMap implements IntMap {
	protected final Map<Integer, Integer> map = new TreeMap<>();

	@Override
	public void set(int key, int value) {
		map.put(key, value);
	}

	@Override
	public int get(int key) {
		return map.get(key);
	}

	@Override
	public boolean contains(int key) {
		return map.containsKey(key);
	}

	@Override
	public IntMapIterator getIterator() {
		var i = map.entrySet().iterator();
		return new IntMapIterator() {
			private Entry<Integer, Integer> e;

			@Override
			public boolean next() {
				if (!i.hasNext()) return false;
				e = i.next();
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
