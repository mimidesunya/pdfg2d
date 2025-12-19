package net.zamasoft.pdfg2d.pdf;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

import net.zamasoft.pdfg2d.gc.font.FontManager;
import net.zamasoft.pdfg2d.gc.image.Image;
import net.zamasoft.pdfg2d.pdf.gc.PDFGroupImage;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;
import net.zamasoft.pdfg2d.resolver.Source;

/**
 * Interface for writing PDF documents.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface PDFWriter extends Closeable {
	/**
	 * Minimum page width.
	 */
	double MIN_PAGE_WIDTH = 3;

	/**
	 * Minimum page height.
	 */
	double MIN_PAGE_HEIGHT = 3;

	/**
	 * Maximum page width.
	 * <p>
	 * Limitation due to PDF implementation limits; larger pages may appear blank in
	 * Adobe Reader.
	 * </p>
	 */
	double MAX_PAGE_WIDTH = 14400;

	/**
	 * Maximum page height.
	 * <p>
	 * Limitation due to PDF implementation limits; larger pages may appear blank in
	 * Adobe Reader.
	 * </p>
	 */
	double MAX_PAGE_HEIGHT = 14400;

	PDFParams getParams();

	/**
	 * Returns the font manager for text drawing context.
	 * 
	 * @return the font manager
	 */
	FontManager getFontManager();

	/**
	 * Loads an image.
	 * 
	 * @param source the image source
	 * @return the PDF image information; the name can be used for referencing in
	 *         graphics operations
	 * @throws IOException in case of I/O error
	 */
	Image loadImage(Source source) throws IOException;

	/**
	 * Adds an image from a BufferedImage.
	 * 
	 * @param image the buffered image
	 * @return the PDF image information; the name can be used for referencing in
	 *         graphics operations
	 * @throws IOException in case of I/O error
	 */
	Image addImage(BufferedImage image) throws IOException;

	/**
	 * Adds an attachment file. At least one of name or desc in attachment parameter
	 * is required.
	 * <p>
	 * The returned stream should be used to write the file content immediately and
	 * then closed.
	 * </p>
	 * 
	 * @param name       the name
	 * @param attachment the attachment information
	 * @return the output stream for attachment content
	 * @throws IOException in case of I/O error
	 */
	OutputStream addAttachment(String name, Attachment attachment) throws IOException;

	/**
	 * Creates a special extended graphics state.
	 * 
	 * @return the output context for the graphics state
	 * @throws IOException in case of I/O error
	 */
	PDFNamedOutput createSpecialGraphicsState() throws IOException;

	/**
	 * Creates a group image, used for transparent images, annotations, etc.
	 * 
	 * @param width  the width
	 * @param height the height
	 * @return the group image
	 * @throws IOException in case of I/O error
	 */
	PDFGroupImage createGroupImage(double width, double height) throws IOException;

	/**
	 * Creates a tiling pattern.
	 * <p>
	 * The returned PDFNamedGraphicsOutput must be closed after writing the pattern.
	 * </p>
	 * 
	 * @param width      pattern width
	 * @param height     pattern height
	 * @param pageHeight page height
	 * @param at         transformation matrix
	 * @return the pattern output context; the name can be used for referencing
	 * @throws IOException in case of I/O error
	 */
	PDFNamedGraphicsOutput createTilingPattern(double width, double height, double pageHeight, AffineTransform at)
			throws IOException;

	/**
	 * Creates a shading pattern.
	 * <p>
	 * The returned PDFNamedOutput must be closed after writing the pattern.
	 * </p>
	 * 
	 * @param pageHeight page height
	 * @param at         transformation matrix
	 * @return the pattern output context; the name can be used for referencing
	 * @throws IOException in case of I/O error
	 */
	PDFNamedOutput createShadingPattern(double pageHeight, AffineTransform at) throws IOException;

	/**
	 * Creates a new page.
	 * <p>
	 * The returned PDFPageOutput must be closed after writing the page content.
	 * </p>
	 * 
	 * @param width  page width
	 * @param height page height
	 * @return the page output context
	 * @throws IOException in case of I/O error
	 */
	PDFPageOutput nextPage(double width, double height) throws IOException;

	Object getAttribute(Object key);

	void putAttribute(Object key, Object value);

	/**
	 * Finishes building the PDF.
	 * 
	 * @throws IOException in case of I/O error
	 */
	@Override
	void close() throws IOException;
}
