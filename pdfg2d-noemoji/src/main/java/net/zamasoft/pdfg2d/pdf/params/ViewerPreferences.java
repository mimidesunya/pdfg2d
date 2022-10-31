package net.zamasoft.pdfg2d.pdf.params;

/**
 * 表示設定です。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class ViewerPreferences {
	private boolean hideToolbar = false;

	private boolean hideMenubar = false;

	private boolean hideWindowUI = false;

	private boolean fitWindow = false;

	private boolean centerWindow = false;

	// PDF 1.4
	private boolean displayDocTitle = false;

	/**
	 * しおりかサムネイルパネルを表示します。
	 */
	public static final byte NONE_FULL_SCREEN_PAGE_MODE_USE_NONE = 1;
	/**
	 * しおりパネルを表示します。
	 */
	public static final byte NONE_FULL_SCREEN_PAGE_MODE_USE_OUTLINES = 2;
	/**
	 * サムネイルパネルを表示します。
	 */
	public static final byte NONE_FULL_SCREEN_PAGE_MODE_USE_THUMBS = 3;
	/**
	 * レイヤーパネルを表示します。
	 */
	public static final byte NONE_FULL_SCREEN_PAGE_MODE_USE_OC = 4;

	private byte nonFullScreenPageMode = NONE_FULL_SCREEN_PAGE_MODE_USE_NONE;

	/**
	 * 左綴じ（日本語横書き、欧文など）の定数です。
	 */
	public static final byte DIRECTION_L2R = 1;
	/**
	 * 右綴じ（日本語縦書き、アラビア語など）定数です。
	 */
	public static final byte DIRECTION_R2L = 2;

	// PDF 1.3
	private byte direction = DIRECTION_L2R;

	public static final byte AREA_MEDIA_BOX = 1;
	public static final byte AREA_CROP_BOX = 2;
	public static final byte AREA_BLEED_BOX = 3;
	public static final byte AREA_TRIM_BOX = 4;
	public static final byte AREA_ART_BOX = 5;

	// PDF 1.4
	private byte viewArea = AREA_CROP_BOX;
	private byte viewClip = AREA_CROP_BOX;
	private byte printArea = AREA_CROP_BOX;
	private byte printClip = AREA_CROP_BOX;

	/**
	 * 拡大縮小をしない定数です。
	 */
	public static final byte PRINT_SCALING_NONE = 1;
	/**
	 * 拡大縮小をビューワに任せる定数です。
	 */
	public static final byte PRINT_SCALING_APP_DEFAULT = 2;

	// PDF 1.6
	private byte printScaling = PRINT_SCALING_APP_DEFAULT;

	/**
	 * ビューワのデフォルトの定数です。
	 */
	public static final byte DUPLEX_NONE = 1;
	/**
	 * 片面印刷の定数です。
	 */
	public static final byte DUPLEX_SIMPLEX = 2;
	/**
	 * 短辺綴じで両面印刷をする定数です。
	 */
	public static final byte DUPLEX_DUPLEX_FLIP_SHORT_EDGE = 3;
	/**
	 * 長辺綴じで両面印刷をする定数です。
	 */
	public static final byte DUPLEX_DUPLEX_FLIP_LONG_EDGE = 4;

	// PDF 1.7
	private byte duplex = DUPLEX_NONE;

	// PDF 1.7
	private boolean pickTrayByPDFSize;

	// PDF 1.7
	private int[] printPageRange = null;

	// PDF 1.7
	private int numCopies = 0;

	/**
	 * ページ進行方向を返します。
	 * 
	 * @return
	 */
	public byte getDirection() {
		return this.direction;
	}

	/**
	 * ページ進行方向を設定します。
	 * 
	 * @param direction
	 */
	public void setDirection(byte direction) {
		this.direction = direction;
	}

	public boolean isHideToolbar() {
		return this.hideToolbar;
	}

	/**
	 * <p>ビューワアプリケーションのツールバーの非表示、表示を設定します。</p>
	 * ※Adobe Readerでは動作はあまりあてにならないようです。
	 * 
	 * @param hideToolbar
	 */
	public void setHideToolbar(boolean hideToolbar) {
		this.hideToolbar = hideToolbar;
	}

	public boolean isHideMenubar() {
		return this.hideMenubar;
	}

	/**
	 * ビューワアプリケーションのメニューバーの非表示、表示を設定します。
	 * 
	 * @param hideMenubar
	 */
	public void setHideMenubar(boolean hideMenubar) {
		this.hideMenubar = hideMenubar;
	}

	public boolean isHideWindowUI() {
		return this.hideWindowUI;
	}

	/**
	 * ビューワアプリケーションのウィンドウ内UI(サムネール、添付など)の非表示、表示を設定します。
	 * 
	 * @param hideWindowUI
	 */
	public void setHideWindowUI(boolean hideWindowUI) {
		this.hideWindowUI = hideWindowUI;
	}

	public boolean isFitWindow() {
		return this.fitWindow;
	}

	/**
	 * 内容に合わせてビューワアプリケーションのウィンドウサイズをフィットさせるかどうかを設定します。
	 * 
	 * @param fitWindow
	 */
	public void setFitWindow(boolean fitWindow) {
		this.fitWindow = fitWindow;
	}

	public boolean isCenterWindow() {
		return this.centerWindow;
	}

	/**
	 * 内容に合わせてビューワアプリケーションのウィンドウサイズをスクリーンに対して中央表示させるかどうかを設定します。
	 * 
	 * @param centerWindow
	 */
	public void setCenterWindow(boolean centerWindow) {
		this.centerWindow = centerWindow;
	}

	public boolean isDisplayDocTitle() {
		return this.displayDocTitle;
	}

	/**
	 * ビューワアプリケーションのタイトルバーに文書のタイトルを表示させるかどうかを設定します。
	 * 
	 * @param displayDocTitle
	 */
	public void setDisplayDocTitle(boolean displayDocTitle) {
		this.displayDocTitle = displayDocTitle;
	}

	public byte getNonFullScreenPageMode() {
		return this.nonFullScreenPageMode;
	}

	/**
	 * <p>サイドパネルの表示内容を設定します。 </p>
	 * ※Adobe Readerでは動作はあまりあてにならないようです。
	 * 
	 * @param nonFullScreenPageMode
	 */
	public void setNonFullScreenPageMode(byte nonFullScreenPageMode) {
		this.nonFullScreenPageMode = nonFullScreenPageMode;
	}

	public byte getViewArea() {
		return this.viewArea;
	}

	public void setViewArea(byte viewArea) {
		this.viewArea = viewArea;
	}

	public byte getViewClip() {
		return this.viewClip;
	}

	public void setViewClip(byte viewClip) {
		this.viewClip = viewClip;
	}

	public byte getPrintArea() {
		return this.printArea;
	}

	public void setPrintArea(byte printArea) {
		this.printArea = printArea;
	}

	public byte getPrintClip() {
		return this.printClip;
	}

	public void setPrintClip(byte printClip) {
		this.printClip = printClip;
	}

	public byte getPrintScaling() {
		return this.printScaling;
	}

	/**
	 * 印刷時の拡大縮小を設定します。
	 * 
	 * @param printScaling
	 */
	public void setPrintScaling(byte printScaling) {
		this.printScaling = printScaling;
	}

	public byte getDuplex() {
		return this.duplex;
	}

	/**
	 * 文書の片面・両面印刷の方法を設定します。
	 * 
	 * @param duplex
	 */
	public void setDuplex(byte duplex) {
		this.duplex = duplex;
	}

	public boolean getPickTrayByPDFSize() {
		return this.pickTrayByPDFSize;
	}

	/**
	 * 「PDFのページサイズに合わせて用紙を選択」のチェック状態を設定します。
	 */
	public void setPickTrayByPDFSize(boolean pickTrayByPDFSize) {
		this.pickTrayByPDFSize = pickTrayByPDFSize;
	}

	public int[] getPrintPageRange() {
		return this.printPageRange;
	}

	/**
	 * 初期の印刷対象ページを設定します。 配列は必ず2の倍数で、開始ページと終了ページのペアを列挙したものです。
	 * 
	 * @param printPageRange
	 */
	public void setPrintPageRange(int[] printPageRange) {
		this.printPageRange = printPageRange;
	}

	public int getNumCopies() {
		return numCopies;
	}

	/**
	 * 初期の印刷枚数を設定します。0ではビューワのデフォルトで、その他は2から5が有効な値です。
	 * 
	 * @param numCopies
	 */
	public void setNumCopies(int numCopies) {
		if (!(numCopies == 0 || (numCopies >= 2 && numCopies <= 5))) {
			throw new IllegalArgumentException();
		}
		this.numCopies = numCopies;
	}
}
