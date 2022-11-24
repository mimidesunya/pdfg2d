package net.zamasoft.pdfg2d.util;

public interface IntMap {
	public void set(int key, int value);

	public int get(int key);

	public boolean contains(int key);

	public IntMapIterator getIterator();
}
