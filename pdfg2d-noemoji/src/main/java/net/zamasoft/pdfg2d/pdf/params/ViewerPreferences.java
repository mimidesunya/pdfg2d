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

	private NonFullScreenPageMode nonFullScreenPageMode = NonFullScreenPageMode.NONE;

	public enum NonFullScreenPageMode {
		/**
		 * しおりかサムネイルパネルを表示します。
		 */
		NONE,
		/**
		 * しおりパネルを表示します。
		 */
		OUTLINES,
		/**
		 * サムネイルパネルを表示します。
		 */
		THUMBS,
		/**
		 * レイヤーパネルを表示します。
		 */
		OC;

	}

	// PDF 1.3
	private Direction direction = Direction.L2R;

	public enum Direction {
		/**
		 * 左綴じ（日本語横書き、欧文など）の定数です。
		 */
		L2R,
		/**
		 * 右綴じ（日本語縦書き、アラビア語など）定数です。
		 */
		R2L;

	}

	// PDF 1.4
	private AreaBox viewArea = AreaBox.CROP;
	private AreaBox viewClip = AreaBox.CROP;
	private AreaBox printArea = AreaBox.CROP;
	private AreaBox printClip = AreaBox.CROP;

	public enum AreaBox {
		MEDIA, CROP, BLEED, TRIM, ART
	}

	// PDF 1.6
	private PrintScaling printScaling = PrintScaling.APP_DEFAULT;

	public enum PrintScaling {
		/**
		 * 拡大縮小をしない定数です。
		 */
		NONE,
		/**
		 * 拡大縮小をビューワに任せる定数です。
		 */
		APP_DEFAULT;
	}

	// PDF 1.7
	private Duplex duplex = Duplex.NONE;

	public enum Duplex {
		/**
		 * ビューワのデフォルトの定数です。
		 */
		NONE,
		/**
		 * 片面印刷の定数です。
		 */
		SIMPLEX,
		/**
		 * 短辺綴じで両面印刷をする定数です。
		 */
		FLIP_SHORT_EDGE,
		/**
		 * 長辺綴じで両面印刷をする定数です。
		 */
		FLIP_LONG_EDGE;
	}

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
	public Direction getDirection() {
		return this.direction;
	}

	/**
	 * ページ進行方向を設定します。
	 * 
	 * @param direction
	 */
	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public boolean isHideToolbar() {
		return this.hideToolbar;
	}

	/**
	 * <p>
	 * ビューワアプリケーションのツールバーの非表示、表示を設定します。
	 * </p>
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

	public NonFullScreenPageMode getNonFullScreenPageMode() {
		return this.nonFullScreenPageMode;
	}

	/**
	 * <p>
	 * サイドパネルの表示内容を設定します。
	 * </p>
	 * ※Adobe Readerでは動作はあまりあてにならないようです。
	 * 
	 * @param nonFullScreenPageMode
	 */
	public void setNonFullScreenPageMode(NonFullScreenPageMode nonFullScreenPageMode) {
		this.nonFullScreenPageMode = nonFullScreenPageMode;
	}

	public AreaBox getViewArea() {
		return this.viewArea;
	}

	public void setViewArea(AreaBox viewArea) {
		this.viewArea = viewArea;
	}

	public AreaBox getViewClip() {
		return this.viewClip;
	}

	public void setViewClip(AreaBox viewClip) {
		this.viewClip = viewClip;
	}

	public AreaBox getPrintArea() {
		return this.printArea;
	}

	public void setPrintArea(AreaBox printArea) {
		this.printArea = printArea;
	}

	public AreaBox getPrintClip() {
		return this.printClip;
	}

	public void setPrintClip(AreaBox printClip) {
		this.printClip = printClip;
	}

	public PrintScaling getPrintScaling() {
		return this.printScaling;
	}

	/**
	 * 印刷時の拡大縮小を設定します。
	 * 
	 * @param printScaling
	 */
	public void setPrintScaling(PrintScaling printScaling) {
		this.printScaling = printScaling;
	}

	public Duplex getDuplex() {
		return this.duplex;
	}

	/**
	 * 文書の片面・両面印刷の方法を設定します。
	 * 
	 * @param duplex
	 */
	public void setDuplex(Duplex duplex) {
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
