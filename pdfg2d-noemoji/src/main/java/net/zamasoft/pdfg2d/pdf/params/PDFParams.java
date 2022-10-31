package net.zamasoft.pdfg2d.pdf.params;

import net.zamasoft.pdfg2d.font.FontSourceManager;
import net.zamasoft.pdfg2d.pdf.PDFMetaInfo;
import net.zamasoft.pdfg2d.pdf.action.Action;
import net.zamasoft.pdfg2d.pdf.font.ConfigurablePDFFontSourceManager;

public class PDFParams {
	private FontSourceManager fsm;

	/** バージョン */
	private int version = VERSION_1_4;

	public static final int VERSION_1_2 = 1200;

	public static final int VERSION_1_3 = 1300;

	public static final int VERSION_1_4 = 1400;

	public static final int VERSION_PDFA1B = 1412;

	public static final int VERSION_PDFX1A = 1421;

	public static final int VERSION_1_5 = 1500;

	public static final int VERSION_1_6 = 1600;

	public static final int VERSION_1_7 = 1700;

	/** 圧縮方法。 */
	private short compression = COMPRESSION_BINARY;

	public static final short COMPRESSION_NONE = 0;

	public static final short COMPRESSION_BINARY = 1;

	public static final short COMPRESSION_ASCII = 2;

	/** JPEG画像の扱い。 */
	private short jpegImage = JPEG_IMAGE_RAW;

	public static final short JPEG_IMAGE_RAW = 0;

	public static final short JPEG_IMAGE_RECOMPRESS = 1;

	/** 画像の変換。 */
	private short imageCompression = IMAGE_COMPRESSION_FLATE;

	public static final short IMAGE_COMPRESSION_FLATE = 0;

	public static final short IMAGE_COMPRESSION_JPEG = 1;

	public static final short IMAGE_COMPRESSION_JPEG2000 = 2;

	private int imageCompressionLossless = 200;

	// 名前のエンコーディング
	// WindowsをターゲットとしたPDFではMS932にしないと、正しいフォントが選択されない現象が起こる。
	private String platformEncoding = "UTF-8";

	private boolean bookmarks = false;

	private EncryptionParams encription = null;

	private short colorMode = COLOR_MODE_PRESERVE;

	public static final short COLOR_MODE_PRESERVE = 0;

	public static final short COLOR_MODE_GRAY = 1;

	public static final short COLOR_MODE_CMYK = 2;

	private int maxImageWidth = 0, maxImageHeight = 0;

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
	 * フォントソースを指定します。
	 * 
	 * @param fsm
	 */
	public void setFontSourceManager(FontSourceManager fsm) {
		this.fsm = fsm;
	}

	public short getCompression() {
		return this.compression;
	}

	/**
	 * 圧縮方法を設定します。 COMPRESSION_XXXの値を使用してください。
	 * 
	 * @param compression
	 */
	public void setCompression(short compression) {
		this.compression = compression;
	}

	/**
	 * JPEG画像の扱いを設定します。 JPEG_IMAGE_XXXの値を使用してください。
	 * 
	 * @param jpegImage
	 */
	public void setJEGImage(short jpegImage) {
		this.jpegImage = jpegImage;
	}

	public short getJPEGImage() {
		return this.jpegImage;
	}

	/**
	 * 画像の再圧縮形式を設定します。 IMAGE_COMPRESSION_XXXの値を使用してください。
	 * 
	 * @param imageCompression
	 */
	public void setImageCompression(short imageCompression) {
		this.imageCompression = imageCompression;
	}

	public short getImageCompression() {
		return this.imageCompression;
	}

	/**
	 * 不可逆圧縮をおこなう画像の大きさ(縦+横)の閾値です。 この値より大きさが大きい場合に不可逆圧縮を適用します。
	 * 
	 * @param imageCompressionLossless
	 */
	public void setImageCompressionLossless(int imageCompressionLossless) {
		this.imageCompressionLossless = imageCompressionLossless;
	}

	public int getImageCompressionLossless() {
		return this.imageCompressionLossless;
	}

	public int getMaxImageWidth() {
		return this.maxImageWidth;
	}

	public void setMaxImageWidth(int maxImageWidth) {
		this.maxImageWidth = maxImageWidth;
	}

	public int getMaxImageHeight() {
		return this.maxImageHeight;
	}

	public void setMaxImageHeight(int maxImageHeight) {
		this.maxImageHeight = maxImageHeight;
	}

	public int getVersion() {
		return this.version;
	}

	/**
	 * PDFのバージョンを設定します。 VERSION_XXXの値を使用してください。
	 * 
	 * @param version
	 */
	public void setVersion(int version) {
		this.version = version;
	}

	public String getPlatformEncoding() {
		return this.platformEncoding;
	}

	/**
	 * プラットフォームのエンコーディングを設定します。
	 * 
	 * @param platformEncoding
	 */
	public void setPlatformEncoding(String platformEncoding) {
		this.platformEncoding = platformEncoding;
	}

	public boolean isBookmarks() {
		return bookmarks;
	}

	/**
	 * ブックマークを作成するかどうかを設定します。
	 * 
	 * @param bookmarks
	 */
	public void setBookmarks(boolean bookmarks) {
		this.bookmarks = bookmarks;
	}

	public EncryptionParams getEncription() {
		return this.encription;
	}

	/**
	 * 暗号化方法を設定します。
	 * 
	 * @see EncryptionParams
	 * @see V2EncryptionParams
	 * 
	 * @param encription
	 */
	public void setEncription(EncryptionParams encription) {
		this.encription = encription;
	}

	public short getColorMode() {
		return this.colorMode;
	}

	/**
	 * カラー変換オブジェクトを設定します。
	 * 
	 * @param colorMode
	 */
	public void setColorMode(short colorMode) {
		this.colorMode = colorMode;
	}

	/**
	 * ファイルIDを設定します。ファイルIDは16バイトのバイト列です。
	 * 
	 * @param fileId
	 */
	public void setFileId(byte[] fileId) {
		if (fileId != null && fileId.length != 16) {
			throw new IllegalArgumentException("ファイルIDは16バイトのバイト列でなければなりません");
		}
		this.fileId = fileId;
	}

	/**
	 * ファイルIDを返します。
	 * 
	 * @return
	 */
	public byte[] getFileId() {
		return this.fileId;
	}

	/**
	 * 文書情報を設定します。
	 * 
	 * @param metaInfo
	 */
	public void setMetaInfo(PDFMetaInfo metaInfo) {
		this.metaInfo = metaInfo;
	}

	/**
	 * 文書情報を返します。
	 * 
	 * @return
	 */
	public PDFMetaInfo getMetaInfo() {
		return this.metaInfo;
	}

	/**
	 * 文書を開いた時に実行されるアクションを設定します。
	 * 
	 * @param openAction
	 */
	public void setOpenAction(Action openAction) {
		if (openAction != null) {
			openAction.setParams(this);
		}
		this.openAction = openAction;
	}

	public Action getOpenAction() {
		return this.openAction;
	}

	/**
	 * 表示設定を設定します。
	 * 
	 * @param viewerPreferences
	 */
	public void setViewerPreferences(ViewerPreferences viewerPreferences) {
		this.viewerPreferences = viewerPreferences;
	}

	/**
	 * 表示設定を返します。
	 * 
	 * @return
	 */
	public ViewerPreferences getViewerPreferences() {
		return this.viewerPreferences;
	}
}
