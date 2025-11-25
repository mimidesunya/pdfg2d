package net.zamasoft.pdfg2d.util;

public interface IntMap {
	void set(int key, int value);
	int get(int key);
	boolean contains(int key);
	IntMapIterator getIterator();
}
