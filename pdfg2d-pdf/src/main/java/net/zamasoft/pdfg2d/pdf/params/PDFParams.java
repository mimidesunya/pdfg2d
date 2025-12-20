package net.zamasoft.pdfg2d.pdf.params;

import net.zamasoft.pdfg2d.font.FontSourceManager;
import net.zamasoft.pdfg2d.pdf.PDFMetaInfo;
import net.zamasoft.pdfg2d.pdf.action.Action;
import net.zamasoft.pdfg2d.pdf.font.ConfigurablePDFFontSourceManager;

/**
 * Parameters for generating PDF documents.
 */
public class PDFParams {
	private FontSourceManager fsm;

	/** PDF Version */
	private Version version = Version.V_1_4;

	public enum Version {
		V_1_2(1200), V_1_3(1300), V_1_4(1400), V_PDFA1B(1412), V_PDFX1A(1421), V_1_5(1500), V_1_6(1600), V_1_7(1700);

		public final int v;

		Version(int v) {
			this.v = v;
		}
	}

	/** Compression method. */
	private Compression compression = Compression.BINARY;

	public enum Compression {
		NONE, BINARY, ASCII
	}

	/** JPEG image handling. */
	private JPEGImage jpegImage = JPEGImage.RAW;

	public enum JPEGImage {
		RAW, RECOMPRESS
	}

	/** Image compression method. */
	private ImageCompression imageCompression = ImageCompression.FLATE;

	public enum ImageCompression {
		FLATE, JPEG, JPEG2000
	}

	private int imageCompressionLossless = 200;

	// Name encoding
	// Must be MS932 for PDFs targeting Windows to ensure correct font selection.
	private String platformEncoding = "UTF-8";

	private boolean bookmarks = false;

	private EncryptionParams encryption = null;

	private ColorMode colorMode = ColorMode.PRESERVE;

	public enum ColorMode {
		PRESERVE, GRAY, CMYK
	}

	private int maxImageWidth = 0;
	private int maxImageHeight = 0;

	/**
	 * Precision for real numbers (decimal places).
	 * Default is 2.
	 */
	private int precision = 2;

	private byte[] fileId = null;

	private PDFMetaInfo metaInfo = new PDFMetaInfo();

	private ViewerPreferences viewerPreferences = new ViewerPreferences();

	private Action openAction = null;

	public synchronized FontSourceManager getFontSourceManager() {
		if (this.fsm == null) {
			this.fsm = ConfigurablePDFFontSourceManager.getDefaultFontSourceManager();
		}
		return this.fsm;
	}

	/**
	 * Sets the font source manager.
	 * 
	 * @param fsm the font source manager
	 */
	public void setFontSourceManager(final FontSourceManager fsm) {
		this.fsm = fsm;
	}

	public Compression getCompression() {
		return this.compression;
	}

	/**
	 * Sets the compression method.
	 * 
	 * @param compression the compression method
	 */
	public void setCompression(final Compression compression) {
		this.compression = compression;
	}

	/**
	 * Sets the handling of JPEG images.
	 * 
	 * @param jpegImage the JPEG image handling mode
	 */
	public void setJPEGImage(final JPEGImage jpegImage) {
		this.jpegImage = jpegImage;
	}

	public JPEGImage getJPEGImage() {
		return this.jpegImage;
	}

	/**
	 * Sets the image re-compression format.
	 * 
	 * @param imageCompression the image compression format
	 */
	public void setImageCompression(final ImageCompression imageCompression) {
		this.imageCompression = imageCompression;
	}

	public ImageCompression getImageCompression() {
		return this.imageCompression;
	}

	/**
	 * Sets the size threshold (width + height) for lossy compression.
	 * Images larger than this value will apply lossy compression.
	 * 
	 * @param imageCompressionLossless the threshold value
	 */
	public void setImageCompressionLossless(final int imageCompressionLossless) {
		this.imageCompressionLossless = imageCompressionLossless;
	}

	public int getImageCompressionLossless() {
		return this.imageCompressionLossless;
	}

	public int getMaxImageWidth() {
		return this.maxImageWidth;
	}

	public void setMaxImageWidth(final int maxImageWidth) {
		this.maxImageWidth = maxImageWidth;
	}

	public int getMaxImageHeight() {
		return this.maxImageHeight;
	}

	public void setMaxImageHeight(final int maxImageHeight) {
		this.maxImageHeight = maxImageHeight;
	}

	public Version getVersion() {
		return this.version;
	}

	/**
	 * Sets the PDF version.
	 * 
	 * @param version the PDF version
	 */
	public void setVersion(final Version version) {
		this.version = version;
	}

	public String getPlatformEncoding() {
		return this.platformEncoding;
	}

	/**
	 * Sets the platform encoding.
	 * 
	 * @param platformEncoding the encoding name
	 */
	public void setPlatformEncoding(final String platformEncoding) {
		this.platformEncoding = platformEncoding;
	}

	public boolean isBookmarks() {
		return bookmarks;
	}

	/**
	 * Sets whether to generate bookmarks.
	 * 
	 * @param bookmarks true to generate bookmarks
	 */
	public void setBookmarks(final boolean bookmarks) {
		this.bookmarks = bookmarks;
	}

	public EncryptionParams getEncryption() {
		return this.encryption;
	}

	/**
	 * Sets the encryption parameters.
	 * 
	 * @param encryption the encryption parameters
	 */
	public void setEncryption(final EncryptionParams encryption) {
		this.encryption = encryption;
	}

	public ColorMode getColorMode() {
		return this.colorMode;
	}

	/**
	 * Sets the color mode.
	 * 
	 * @param colorMode the color mode
	 */
	public void setColorMode(final ColorMode colorMode) {
		this.colorMode = colorMode;
	}

	/**
	 * Sets the file ID. Must be a 16-byte array.
	 * 
	 * @param fileId the file ID
	 * @throws IllegalArgumentException if fileId is not 16 bytes
	 */
	public void setFileId(final byte[] fileId) {
		if (fileId != null && fileId.length != 16) {
			throw new IllegalArgumentException("File ID must be a 16-byte array.");
		}
		this.fileId = fileId;
	}

	/**
	 * Returns the file ID.
	 * 
	 * @return the file ID
	 */
	public byte[] getFileId() {
		return this.fileId;
	}

	/**
	 * Sets the document metadata.
	 * 
	 * @param metaInfo the metadata
	 */
	public void setMetaInfo(final PDFMetaInfo metaInfo) {
		this.metaInfo = metaInfo;
	}

	/**
	 * Returns the document metadata.
	 * 
	 * @return the metadata
	 */
	public PDFMetaInfo getMetaInfo() {
		return this.metaInfo;
	}

	/**
	 * Sets the action to be performed when the document is opened.
	 * 
	 * @param openAction the open action
	 */
	public void setOpenAction(final Action openAction) {
		if (openAction != null) {
			openAction.setParams(this);
		}
		this.openAction = openAction;
	}

	public Action getOpenAction() {
		return this.openAction;
	}

	/**
	 * Sets the viewer preferences.
	 * 
	 * @param viewerPreferences the viewer preferences
	 */
	public void setViewerPreferences(final ViewerPreferences viewerPreferences) {
		this.viewerPreferences = viewerPreferences;
	}

	/**
	 * Returns the viewer preferences.
	 * 
	 * @return the viewer preferences
	 */
	public ViewerPreferences getViewerPreferences() {
		return this.viewerPreferences;
	}

	/**
	 * Returns the precision for real numbers.
	 * 
	 * @return the precision
	 */
	public int getPrecision() {
		return this.precision;
	}

	/**
	 * Sets the precision for real numbers.
	 * 
	 * @param precision the precision
	 */
	public void setPrecision(final int precision) {
		this.precision = precision;
	}
}
