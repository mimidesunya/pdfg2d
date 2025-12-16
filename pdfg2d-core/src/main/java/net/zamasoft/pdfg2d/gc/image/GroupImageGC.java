package net.zamasoft.pdfg2d.gc.image;

import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.GraphicsException;

/**
 * A graphics context for creating grouped images.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface GroupImageGC extends GC {
	/**
	 * Finishes the group image creation and returns the resulting image.
	 * 
	 * @return the created image
	 * @throws GraphicsException if a graphics error occurs
	 */
	public Image finish() throws GraphicsException;
}
