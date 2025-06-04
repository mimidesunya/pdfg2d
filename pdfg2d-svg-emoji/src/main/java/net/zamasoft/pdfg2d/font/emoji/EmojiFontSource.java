package net.zamasoft.pdfg2d.font.emoji;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.zamasoft.pdfg2d.font.AbstractFontSource;
import net.zamasoft.pdfg2d.font.BBox;
import net.zamasoft.pdfg2d.font.Font;
import net.zamasoft.pdfg2d.font.FontSource;
import net.zamasoft.pdfg2d.gc.font.FontStyle.Direction;
import net.zamasoft.pdfg2d.gc.font.Panose;

/**
 * 
 * @author MIYABE Tatsuhiko
 */
public class EmojiFontSource extends AbstractFontSource {
	private static final long serialVersionUID = 1L;

	private static final BBox BBOX = new BBox((short) 0, DEFAULT_DESCENT, (short) 1000, DEFAULT_ASCENT);

	protected static final Map<String, Integer> codeToFgid;
	protected static final Map<Integer, String> fgidToCode;
	static {
		Map<String, Integer> ctog = new HashMap<String, Integer>();
		Map<Integer, String> gtoc = new HashMap<Integer, String>();
		try (BufferedReader in = new BufferedReader(
				new InputStreamReader(EmojiFontSource.class.getResourceAsStream("INDEX"), "ISO8859-1"))) {
			int gid = 0;
			for (String code = in.readLine(); code != null; code = in.readLine()) {
				++gid;
				ctog.put(code, gid);
				gtoc.put(gid, code);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		codeToFgid = Collections.unmodifiableMap(ctog);
		fgidToCode = Collections.unmodifiableMap(gtoc);
	}

	public static final EmojiFontSource INSTANCES_LTR = new EmojiFontSource(Direction.LTR);
	public static final EmojiFontSource INSTANCES_TB = new EmojiFontSource(Direction.TB);

	private final Direction direction;

	private EmojiFontSource(Direction direction) {
		this.direction = direction;
	}

	public Direction getDirection() {
		return this.direction;
	}

	public String getFontName() {
		return "emoji";
	}

	public BBox getBBox() {
		return BBOX;
	}

	public short getAscent() {
		return DEFAULT_ASCENT;
	}

	public short getDescent() {
		return DEFAULT_DESCENT;
	}

	public short getCapHeight() {
		return DEFAULT_CAP_HEIGHT;
	}

	public short getXHeight() {
		return DEFAULT_X_HEIGHT;
	}

	public short getSpaceAdvance() {
		return FontSource.DEFAULT_UNITS_PER_EM / 2;
	}

	public short getStemH() {
		return 0;
	}

	public short getStemV() {
		return 0;
	}

	public boolean canDisplay(int c) {
		return codeToFgid.containsKey(Integer.toHexString(c));
	}

	public Panose getPanose() {
		return null;
	}

	public Font createFont() {
		return new EmojiFont(this);
	}
}
