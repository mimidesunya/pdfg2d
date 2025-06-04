package net.zamasoft.pdfg2d.pdf.font.cid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class ToUnicode implements Serializable {
	private static final long serialVersionUID = 0;

	protected final Unicode[] unicodes;

	public ToUnicode(Unicode[] unicodes) {
		this.unicodes = unicodes;
	}

	public Unicode[] getUnicodes() {
		return this.unicodes;
	}

	public static class Unicode implements Serializable {
		private static final long serialVersionUID = 0;

		/** 最初のコードと最後のコードです。 */
		int firstCode, lastCode;

		/** 文字のリストです。 */
		int[] unicodes;

		/**
		 * ある範囲の文字に対するエントリを構築します。
		 * 
		 * @param firstCode 最初の文字のコード。
		 * @param lastCode  最後の文字のコード。
		 * @param unicodes  文字の幅のリスト。
		 */
		public Unicode(int firstCode, int lastCode, int[] unicodes) {
			this.firstCode = firstCode;
			this.lastCode = lastCode;
			this.unicodes = unicodes;
		}

		public Unicode(int code, int[] unicodes) {
			this(code, code, unicodes);
		}

		public Unicode(int[] unicodes) {
			this(0, 0, unicodes);
		}

		public int getFirstCode() {
			return this.firstCode;
		}

		public int getLastCode() {
			return this.lastCode;
		}

		public int[] getUnicodes() {
			return this.unicodes;
		}

		public int getUnicode(int code) {
			if (code < this.firstCode || code > this.lastCode) {
				throw new ArrayIndexOutOfBoundsException(code);
			}
			int index = code - this.firstCode;
			if (index >= this.unicodes.length) {
				return this.unicodes[this.unicodes.length - 1];
			}
			return this.unicodes[index];
		}
	}

	/**
	 * 文字配列から最適なToUnicodeを構築します。
	 * 
	 * @param unicodes
	 * @return
	 */
	public static ToUnicode buildFromChars(int[] unicodes) {
		List<ToUnicode.Unicode> list = new ArrayList<ToUnicode.Unicode>();
		int[] runUnicodes = new int[256];
		int position = 0;
		int startCid = -1;
		for (int cid = 0; cid < unicodes.length; ++cid) {
			int unicode = unicodes[cid];
			if (unicode == 0) {
				if (position == 0) {
					continue;
				}
				unicode = (runUnicodes[position - 1] + (cid - startCid));
			}

			if (startCid == -1) {
				// 最初
				startCid = cid;
				runUnicodes[position++] = unicode;
				continue;
			} else if (cid % 256 != 0) {// SPEC PDF 7.10.1 (ランはバイトをまたがることができない)
				runUnicodes[position++] = unicode;
				continue;
			}
			// ランの終了
			int[] temp = new int[position];
			System.arraycopy(runUnicodes, 0, temp, 0, position);
			list.add(new ToUnicode.Unicode(startCid, cid - 1, temp));
			startCid = cid;
			runUnicodes[0] = unicode;
			position = 1;
		}
		if (startCid != -1) {
			int[] temp = new int[position];
			System.arraycopy(runUnicodes, 0, temp, 0, position);
			list.add(new ToUnicode.Unicode(startCid, unicodes.length - 1, temp));
		}
		return new ToUnicode((ToUnicode.Unicode[]) list.toArray(new ToUnicode.Unicode[list.size()]));
	}
}
