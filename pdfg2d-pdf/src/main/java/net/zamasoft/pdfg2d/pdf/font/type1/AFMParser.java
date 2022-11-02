package net.zamasoft.pdfg2d.pdf.font.type1;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.HashMap;

import net.zamasoft.pdfg2d.font.BBox;
import net.zamasoft.pdfg2d.pdf.font.type1.AFMFontInfo.AFMGlyphInfo;
import net.zamasoft.pdfg2d.pdf.util.PDFUtils;

public class AFMParser {
	private AFMFontInfo fontInfo;

	private Reader in;

	/** 現在の文字です。 */
	private int ch;

	/** 処理中の行です。 */
	private int line = 1;

	public AFMFontInfo parse(InputStream in) throws ParseException, IOException {
		this.fontInfo = new AFMFontInfo();
		this.in = new InputStreamReader(in, "ISO-8859-1");
		try {
			this.ch = this.in.read();

			String s;

			// StartFontMetricsブロック
			s = this.parseTitle();
			if (!s.equals("StartFontMetrics")) {
				throw new ParseException("AFMファイルではありません", this.line);
			}
			s = this.parseString();

			// Global Font Information
			FOR: for (;;) {
				this.skipLine();
				s = this.parseTitle();
				switch (s.charAt(0)) {
				case 'A':
					if (s.equals("Ascender")) {
						this.fontInfo.ascent = this.parseShort();
					}
					break;

				case 'C':
					if (s.equals("CapHeight")) {
						this.fontInfo.capHeight = this.parseShort();
					}
					break;

				case 'D':
					if (s.equals("Descender")) {
						this.fontInfo.descent = (short) -this.parseShort();
					}
					break;

				case 'E':
					if (s.equals("EndFontMetrics")) {
						break FOR;
					}
					break;

				case 'F':
					if (s.equals("FontBBox")) {
						this.fontInfo.bbox = new BBox(this.parseShort(), this.parseShort(), this.parseShort(),
								this.parseShort());
					} else if (s.equals("FontName")) {
						this.fontInfo.fontName = this.parseString();
					} else if (s.equals("FullName")) {
						this.fontInfo.fullName = this.parseString();
					} else if (s.equals("FamilyName")) {
						this.fontInfo.familyName = this.parseString();
					}
					break;

				case 'I':
					if (s.equals("ItalicAngle")) {
						double italicAngle = this.parseReal();
						if (italicAngle != 0f) {
							this.fontInfo.italic = true;
						}
					}
					break;

				case 'S':
					if (s.equals("StdHW")) {
						this.fontInfo.stemh = this.parseShort();
					} else if (s.equals("StdVW")) {
						this.fontInfo.stemv = this.parseShort();
					} else if (s.equals("StartCharMetrics")) {
						this.parseIndividualCharacterMetrics();

					} else if (s.equals("StartKernPairs")) {
						this.parseKerningPairs();
					}
					break;

				case 'W':
					if (s.equals("Weight")) {
						String weight = this.readLine().trim().toUpperCase();
						if (weight.equals("ULTRALIGHT")) {
							this.fontInfo.weight = 100;
						} else if (weight.equals("THIN")) {
							this.fontInfo.weight = 200;
						} else if (weight.equals("LIGHT") || weight.equals("EXTRALIGHT") || weight.equals("BOOK")) {
							this.fontInfo.weight = 300;
						} else if (weight.equals("REGULAR") || weight.equals("PLAIN") || weight.equals("ROMAN")
								|| weight.equals("MEDIUM")) {
							this.fontInfo.weight = 400;
						} else if (weight.equals("DEMI") || weight.equals("DEMIBOLD")) {
							this.fontInfo.weight = 500;
						} else if (weight.equals("SEMIBOLD")) {
							this.fontInfo.weight = 600;
						} else if (weight.equals("BOLD") || weight.equals("EXTRABOLD") || weight.equals("HERAVY")
								|| weight.equals("HEAVYFACE") || weight.equals("BLACK")) {
							this.fontInfo.weight = 700;
						} else if (weight.equals("ULTRA") || weight.equals("ULTRABLACK") || weight.equals("FAT")) {
							this.fontInfo.weight = 800;
						} else if (weight.equals("EXTRABLACK") || weight.equals("OBESE")) {
							this.fontInfo.weight = 900;
						}
					}
					break;
				}
			}
		} finally {
			this.in.close();
		}
		return this.fontInfo;
	}

	private void parseIndividualCharacterMetrics() throws ParseException, IOException {
		this.fontInfo.nameToGi = new HashMap<String, AFMGlyphInfo>();
		for (;;) {
			this.skipLine();
			String s = this.parseTitle();
			if (s.equals("EndCharMetrics")) {
				break;
			}

			AFMGlyphInfo gi = this.parseGlyphInfo(s);
			this.fontInfo.nameToGi.put(gi.name, gi);
		}
	}

	private AFMGlyphInfo parseGlyphInfo(String s) throws ParseException, IOException {
		AFMGlyphInfo gi = new AFMGlyphInfo();
		FOR: while (s != null) {
			switch (s.charAt(0)) {
			case 'C':
				if (s.equals("C")) {
					gi.gid = this.parseInt();
				} else if (s.equals("CH")) {
					gi.gid = this.parseHexInt();
				}
				break;

			case 'L':
				if (s.equals("L")) {
					String sname = this.parseString();
					String lname = this.parseString();
					gi.addLigature(sname, lname);
				}
				break;

			case 'N':
				if (s.equals("N")) {
					gi.name = this.parseString();
				}
				break;

			case 'W':
				if (s.equals("WX") || s.equals("W0X")) {
					gi.advance = this.parseShort();
				}
				break;

			default:
				for (;;) {
					s = this.parseString();
					if (s.equals(";")) {
						s = this.parseString();
						continue FOR;
					}
				}
			}
			s = this.parseString();
			if (s.equals(";")) {
				s = this.parseString();
				continue;
			}
			throw new ParseException("';'が見つかるはずでしたが'" + s + "'が見つかりました", this.line);
		}
		return gi;
	}

	private void parseKerningPairs() throws ParseException, IOException {
		for (;;) {
			this.skipLine();
			String s = this.parseTitle();
			if (s.equals("EndKernPairs")) {
				break;
			}

			switch (s.charAt(0)) {
			case 'K':
				if (s.equals("KPX")) {
					String pname = this.parseString();
					String sname = this.parseString();
					short kerning = this.parseShort();
					AFMGlyphInfo sci = (AFMGlyphInfo) this.fontInfo.nameToGi.get(sname);
					AFMGlyphInfo pci = (AFMGlyphInfo) this.fontInfo.nameToGi.get(pname);
					if (sci != null && pci != null) {
						pci.addKerning(sci.name, kerning);
					}
				}
				break;
			}
		}
	}

	private String parseTitle() throws IOException {
		for (;;) {
			while (this.skipWhiteSpace()) {
				this.skipLine();
			}
			StringBuffer buff = new StringBuffer();
			for (; this.ch != -1 && !Character.isWhitespace((char) this.ch); this.ch = this.in.read()) {
				buff.append((char) this.ch);
			}
			if (this.ch == -1) {
				throw new EOFException();
			}
			String title = buff.toString();
			if (title.equals("Comment")) {
				this.skipLine();
				continue;
			}
			return title;
		}
	}

	private void skipLine() throws IOException {
		boolean cr = false;
		FOR: for (; this.ch != -1; this.ch = this.in.read()) {
			switch (this.ch) {
			case '\n':
			case '\r':
				cr = true;
				break;

			default:
				if (cr) {
					break FOR;
				}
			}
		}
		if (this.ch == -1) {
			throw new EOFException();
		}
		this.line++;
	}

	private String readLine() throws IOException {
		StringBuffer buff = new StringBuffer();
		boolean cr = false;
		FOR: for (; this.ch != -1; this.ch = this.in.read()) {
			switch (this.ch) {
			case '\n':
				break FOR;

			case '\r':
				cr = true;
				break;

			default:
				if (cr) {
					break FOR;
				}
				buff.append((char) this.ch);
			}
		}
		if (this.ch == -1) {
			throw new EOFException();
		}
		this.line++;
		return buff.toString();
	}

	private boolean skipWhiteSpace() throws IOException {
		for (; this.ch != -1 && Character.isWhitespace((char) this.ch); this.ch = this.in.read()) {
			if (this.ch == '\n' || this.ch == '\r') {
				return true;
			}
		}
		if (this.ch == -1) {
			throw new EOFException();
		}
		return false;
	}

	private String parseString() throws ParseException, IOException {
		if (this.skipWhiteSpace()) {
			return null;
		}
		StringBuffer buff = new StringBuffer();
		for (; this.ch != -1 && !Character.isWhitespace((char) this.ch); this.ch = this.in.read()) {
			buff.append((char) this.ch);
		}
		if (this.ch == -1) {
			throw new EOFException();
		}
		String s = buff.toString();
		return PDFUtils.decodeName(s, "MS932");
	}

	private short parseShort() throws ParseException, IOException {
		String s = this.parseString();
		try {
			return Short.parseShort(s);
		} catch (NumberFormatException e) {
			throw new ParseException("整数が予想された場所です:" + s, this.line);
		}
	}

	private int parseInt() throws ParseException, IOException {
		String s = this.parseString();
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			throw new ParseException("整数が予想された場所です:" + s, this.line);
		}
	}

	private int parseHexInt() throws ParseException, IOException {
		String s = this.parseString();
		try {
			return Integer.parseInt(s.substring(1, s.length() - 1), 16);
		} catch (NumberFormatException e) {
			throw new ParseException("数値が予想された場所に文字列がありました:" + s, this.line);
		}
	}

	private double parseReal() throws ParseException, IOException {
		String s = this.parseString();
		try {
			return Float.parseFloat(s);
		} catch (NumberFormatException e) {
			throw new ParseException("実数が予想された場所です:" + s, this.line);
		}
	}
}