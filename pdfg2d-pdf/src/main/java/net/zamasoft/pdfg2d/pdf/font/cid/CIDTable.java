package net.zamasoft.pdfg2d.pdf.font.cid;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.zamasoft.pdfg2d.resolver.Source;
import net.zamasoft.pdfg2d.util.IntList;
import net.zamasoft.pdfg2d.util.IntMap;
import net.zamasoft.pdfg2d.util.IntMapIterator;
import net.zamasoft.pdfg2d.util.MapIntMap;

/**
 * CIDフォントのキャラクタマッピング情報です。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class CIDTable implements Serializable {
	private static final Logger LOG = Logger.getLogger(CIDTable.class.getName());

	private static final long serialVersionUID = 0;

	public static final char MISSING_CHAR = '?';

	protected final Source cmapSource;

	protected final String javaEncoding;

	transient protected WeakReference<IntMap> toCID = null;

	transient protected int missingCID = 0;

	transient protected Charset charset = null;

	public CIDTable(Source cmapSource, String javaEncoding) {
		this.cmapSource = cmapSource;
		this.javaEncoding = javaEncoding;
	}

	private synchronized IntMap getToCid() {
		IntMap toCid = null;
		if (this.toCID != null) {
			toCid = this.toCID.get();
			if (toCid != null) {
				return toCid;
			}
		}
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("load cid table:" + this.cmapSource);
		}

		CIDTableParser parser = new CIDTableParser();
		try {
			Charset charset = this.getCharset();
			// UTF-16BEの場合は、Javaの内部コードと一致する
			if (charset.name().equalsIgnoreCase("UTF-16BE")) {
				toCid = new MapIntMap();
				parser.parse(this.cmapSource, toCid);
			} else {
				toCid = new IntList();
				parser.parse(this.cmapSource, toCid);
				IntList intList = new IntList();
				CharsetDecoder decoder = charset.newDecoder();
				ByteBuffer in = ByteBuffer.allocate(4);
				CharBuffer out = CharBuffer.allocate(1);
				IntMapIterator i = toCid.getIterator();
				while (i.next()) {
					int cid = i.value();
					int c = i.key();
					if (cid != 0) {
						in.clear();
						if (c < 0xFF) {
							in.put((byte) (c & 0xFF));
						} else if (c <= 0xFFFF) {
							in.put((byte) ((c >> 8) & 0xFF));
							in.put((byte) (c & 0xFF));
						} else if (c <= 0xFFFFFF) {
							in.put((byte) ((c >> 16) & 0xFF));
							in.put((byte) ((c >> 8) & 0xFF));
							in.put((byte) (c & 0xFF));
						} else {
							in.put((byte) ((c >> 24) & 0xFF));
							in.put((byte) ((c >> 16) & 0xFF));
							in.put((byte) ((c >> 8) & 0xFF));
							in.put((byte) (c & 0xFF));
						}
						in.flip();
						out.clear();
						decoder.reset();
						decoder.decode(in, out, true);
						decoder.flush(out);
						intList.set(out.get(0), cid);
					}
				}
				toCid = intList;
			}
			// if (!toCid.contains(MISSING_CHAR)) {
			// System.err.println("missing missing char");
			// }
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		this.toCID = new WeakReference<IntMap>(toCid);
		return toCid;
	}

	/**
	 * Javaエンコーディングを返します。
	 * 
	 * @return
	 */
	public Charset getCharset() {
		if (this.charset == null) {
			this.charset = Charset.forName(this.javaEncoding);
		}
		return this.charset;
	}

	/**
	 * 不明な文字のCIDを返します。
	 * 
	 * @return
	 */
	public int getMissingCID() {
		if (this.missingCID == 0) {
			this.missingCID = this.toCID(CIDTable.MISSING_CHAR);
		}
		return this.missingCID;
	}

	/**
	 * 文字をCIDに変換します。
	 * 
	 * @param c 文字
	 * @return cid
	 */
	public int toCID(int c) {
		IntMap toCid = this.getToCid();
		if (!toCid.contains(c)) {
			return this.getMissingCID();
		}
		return toCid.get(c);
	}

	/**
	 * 文字を含んでいればtrueを返します。
	 * 
	 * @param c
	 * @return
	 */
	public boolean containsChar(int c) {
		IntMap toCid = this.getToCid();
		return toCid.contains(c);
	}

	public IntMapIterator getIterator() {
		IntMap toCid = this.getToCid();
		return toCid.getIterator();
	}
}
