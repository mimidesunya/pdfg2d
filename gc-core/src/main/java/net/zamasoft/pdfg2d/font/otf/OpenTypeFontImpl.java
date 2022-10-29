package net.zamasoft.pdfg2d.font.otf;

import net.zamasoft.pdfg2d.util.CharList;

public class OpenTypeFontImpl extends OpenTypeFont {
	private static final long serialVersionUID = 1L;

	protected CharList unicodes = new CharList();

	public OpenTypeFontImpl(OpenTypeFontSource source) {
		super(source);
	}

	public int toGID(int c) {
		int gid = super.toGID(c);
		this.unicodes.set(gid, (char) c);
		return gid;
	}

	protected int toChar(int gid) {
		return this.unicodes.get(gid);
	}
}
