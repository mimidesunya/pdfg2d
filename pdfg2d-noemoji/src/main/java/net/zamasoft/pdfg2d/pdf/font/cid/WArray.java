package net.zamasoft.pdfg2d.pdf.font.cid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.zamasoft.pdfg2d.util.IntList;
import net.zamasoft.pdfg2d.util.ShortMapIterator;

/**
 * フォントのDW,W属性です。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class WArray implements Serializable {
	private static final long serialVersionUID = 0;

	private static final Width[] EMPTY_WIDTHS = new Width[0];

	private final short defaultWidth;

	private final Width[] widths;

	/**
	 * 
	 * @param defaultWidth
	 *            デフォルト幅(DW)
	 * @param widths
	 *            W要素の配列(W)
	 */
	public WArray(short defaultWidth, Width[] widths) {
		this.defaultWidth = defaultWidth;
		this.widths = widths == null ? EMPTY_WIDTHS : widths;
	}

	public WArray(short defaultWidth) {
		this(defaultWidth, null);
	}

	/**
	 * デフォルト幅(DW)を返します。
	 * 
	 * @return
	 */
	public short getDefaultWidth() {
		return this.defaultWidth;
	}

	/**
	 * W要素の配列(W)を返します。
	 * 
	 * @return
	 */
	public Width[] getWidths() {
		return this.widths;
	}

	/**
	 * グリフの幅を返します。
	 * 
	 * @param gid
	 * @return
	 */
	public short getWidth(int gid) {
		for (int i = 0; i < this.widths.length; ++i) {
			Width width = this.widths[i];
			if (width.getFirstCode() <= gid && width.getLastCode() >= gid) {
				return width.getWidth(gid);
			}
		}
		return this.defaultWidth;
	}

	/**
	 * 幅のリストから最適なWArrayを構築します。
	 * 
	 * @param widths
	 * @return
	 */
	public static WArray buildFromWidths(ShortMapIterator widths) {
		final List<Width> list = new ArrayList<Width>();
		final IntList widthCounts = new IntList();// 最も多い幅をデフォルトとする
		final short[] runWidths = new short[255];

		int position = 0;
		int startCid = -1;
		boolean run = false;
		int cid = widths.key();
		while (widths.next()) {
			cid = widths.key();
			short advance = widths.value();
			// Short.MIN_VALUEはデフォルトの幅とする
			if (advance == Short.MIN_VALUE) {
				if (position == 0) {
					continue;
				}
				advance = runWidths[position - 1];
			}

			int count = widthCounts.get(advance);
			++count;
			widthCounts.set(advance, count);

			if (startCid == -1) {
				// 最初
				startCid = cid;
				runWidths[position++] = advance;
				continue;
			} else {
				if (runWidths[position - 1] == advance) {
					// ランの開始/継続
					run = true;
					continue;
				} else if (startCid == cid - 1 || (!run && position < runWidths.length)) {
					// 配列の開始/継続
					runWidths[position++] = advance;
					continue;
				}
			}
			// ランの終了
			short[] temp = new short[position];
			System.arraycopy(runWidths, 0, temp, 0, position);
			list.add(new Width(startCid, cid - 1, temp));
			startCid = cid;
			runWidths[0] = advance;
			position = 1;
			run = false;
		}
		if (startCid != -1) {
			short[] temp = new short[position];
			System.arraycopy(runWidths, 0, temp, 0, position);
			list.add(new Width(startCid, cid, temp));
		}
		short defaultWidth = 0;
		if (!widthCounts.isEmpty()) {
			int maxCount = 0;
			for (short i = 0; i < widthCounts.size(); ++i) {
				int count = widthCounts.get(i);
				if (count > maxCount) {
					defaultWidth = i;
					maxCount = count;
				}
			}
		}
		List<Width> newList = new ArrayList<Width>();
		for (int i = 0; i < list.size(); ++i) {
			Width width = (Width) list.get(i);
			if (width.widths.length == 1 && width.widths[0] == defaultWidth) {
				continue;
			}
			newList.add(width);
		}
		return new WArray(defaultWidth, (Width[]) newList.toArray(new Width[newList.size()]));
	}

	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append(this.defaultWidth).append('\n');
		for (int i = 0; i < this.widths.length; ++i) {
			buff.append(this.widths[i]).append('\n');
		}
		return buff.toString();
	}
}
