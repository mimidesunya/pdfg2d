package net.zamasoft.pdfg2d.pdf;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

import net.zamasoft.pdfg2d.resolver.Source;
import net.zamasoft.pdfg2d.gc.font.FontManager;
import net.zamasoft.pdfg2d.gc.image.Image;
import net.zamasoft.pdfg2d.pdf.gc.PDFGroupImage;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public interface PDFWriter extends Closeable {
	/**
	 * 最小ページ幅です。
	 */
	public static final double MIN_PAGE_WIDTH = 3;
	/**
	 * 最小ページ高さです。
	 */
	public static final double MIN_PAGE_HEIGHT = 3;
	/**
	 * 最大ページ幅です。
	 * <p>
	 * PDFの実装限界であり、これより大きなページを生成するとAdobe Readerで真っ白なページが表示されてしまうため、制限をかけています。
	 * </p>
	 */
	public static final double MAX_PAGE_WIDTH = 14400;
	/**
	 * 最大ページ高さです。
	 * <p>
	 * PDFの実装限界であり、これより大きなページを生成するとAdobe Readerで真っ白なページが表示されてしまうため、制限をかけています。
	 * </p>
	 */
	public static final double MAX_PAGE_HEIGHT = 14400;

	public PDFParams getParams();

	/**
	 * グラフィックコンテキストへのテキスト描画のためのフォントマネージャを返します。
	 * 
	 * @return
	 */
	public FontManager getFontManager();

	/**
	 * 画像を読み込みます。
	 * 
	 * @param source
	 * @return PDF画像情報。getNameで得られる名前はグラフック命令からの参照に利用可能です。
	 * @throws IOException
	 */
	public Image loadImage(Source source) throws IOException;

	/**
	 * 画像を読み込みます。
	 * 
	 * @param image
	 * @return PDF画像情報。getNameで得られる名前はグラフック命令からの参照に利用可能です。
	 * @throws IOException
	 */
	public Image addImage(BufferedImage image) throws IOException;

	/**
	 * 添付ファイルを追加します。 パラメータのうち、nameとdescのいずれかは必要です。
	 * <p>
	 * 返されたストリームを使って直ちにファイルの内容を出力し、ストリームを閉じてください。
	 * </p>
	 * 
	 * @param name       名前
	 * @param attachment 添付ファイル
	 * @return
	 * @throws IOException
	 */
	public abstract OutputStream addAttachment(String name, Attachment attachment) throws IOException;

	/**
	 * 拡張グラフィック状態を追加します。
	 * 
	 * @return
	 * @throws IOException
	 */
	public PDFNamedOutput createSpecialGraphicsState() throws IOException;

	/**
	 * 描画命令の組み合わせで作られる画像です。 透明化画像、アノテーション等に使います。
	 */
	public PDFGroupImage createGroupImage(double width, double height) throws IOException;

	/**
	 * 繰り返しパターンを作成します。
	 * <p>
	 * このメソッドが返すPDFNamedGraphicsOutputは、パターンを書き出した後、必ずクローズしてください。
	 * </p>
	 * 
	 * @param width
	 * @param height
	 * @return パターンの出力先。getNameで返される名前は、グラフィック命令からの参照に利用できます。
	 * @throws IOException
	 */
	public PDFNamedGraphicsOutput createTilingPattern(double width, double height, double pageHeight,
			AffineTransform at) throws IOException;

	/**
	 * ぼかしパターンを作成します。
	 * <p>
	 * このメソッドが返すPDFNamedOutputは、パターンを書き出した後、必ずクローズしてください。
	 * </p>
	 * 
	 * @param at TODO
	 * @return パターンの出力先。getNameで返される名前は、グラフィック命令からの参照に利用できます。
	 * @throws IOException
	 */
	public PDFNamedOutput createShadingPattern(double pageHeight, AffineTransform at) throws IOException;

	/**
	 * ページを作成します。
	 * <p>
	 * このメソッドが返すPDFPageOutputは、ページの内容を書き出した後必ずクローズしてください。
	 * </p>
	 * 
	 * @param width
	 * @param height
	 * @return
	 * @throws IOException
	 */
	public PDFPageOutput nextPage(double width, double height) throws IOException;

	public Object getAttribute(Object key);

	public void putAttribute(Object key, Object value);

	/**
	 * PDFの構築を終えます。
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException;
}

