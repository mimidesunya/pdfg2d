package net.zamasoft.pdfg2d.gc.image;

import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.GraphicsException;

public interface GroupImageGC extends GC {
	public Image finish() throws GraphicsException;
}
