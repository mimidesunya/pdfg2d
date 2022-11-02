package net.zamasoft.pdfg2d.pdf.font.cid;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;

import jp.cssj.resolver.Source;
import jp.cssj.resolver.SourceResolver;
import jp.cssj.resolver.composite.CompositeSourceResolver;
import jp.cssj.resolver.helpers.URIHelper;
import net.zamasoft.pdfg2d.pdf.util.PDFUtils;
import net.zamasoft.pdfg2d.util.IntMap;

/**
 * CMapファイルのCIDテーブルを解析します。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
class CIDTableParser {
	private Reader in;

	/** 現在の文字です。 */
	private int ch;

	private SourceResolver resolver;

	public void parse(Source source, IntMap toCID) throws IOException, CMapException {
		this.in = new InputStreamReader(new BufferedInputStream(source.getInputStream()), "ISO-8859-1");
		try {
			try {
				String ptoken = null;
				for (;;) {
					String token = this.nextToken();
					if (token.equals("usecmap")) {
						CIDTableParser parser = new CIDTableParser();
						String href = ptoken.substring(1);
						if (this.resolver == null) {
							this.resolver = CompositeSourceResolver.createGenericCompositeSourceResolver();
						}
						Source source2 = this.resolver.resolve(URIHelper.resolve("UTF-8", source.getURI(), href));
						try {
							parser.parse(source2, toCID);
						} finally {
							this.resolver.release(source2);
						}
					} else if (token.equals("begincidrange")) {
						this.parseCidRanges(toCID);
					} else if (token.equals("begincidchar")) {
						this.parseCidChars(toCID);
					} else if (token.equals("/CIDSystemInfo")) {
						this.parseCidSystemInfo();
					}
					ptoken = token;
				}
			} catch (EOFException e) {
				// ignore
			} catch (URISyntaxException e) {
				IOException ioe = new IOException();
				ioe.initCause(e);
				throw ioe;
			}
		} finally {
			this.in.close();
		}
	}

	private static int parseCode(String a) {
		int code;
		if (a.length() <= 4) {
			code = Integer.parseInt(a, 16);
		} else {
			// サロゲートペア
			int h = Integer.parseInt(a.substring(0, 4), 16);
			int l = Integer.parseInt(a.substring(4, 8), 16);
			code = 0x10000 + (h - 0xD800) * 0x400 + (l - 0xDC00);
		}
		return code;
	}

	private void parseCidChars(IntMap toCID) throws IOException, CMapException {
		for (;;) {
			String a = this.nextToken();
			if (a.equals("endcidchar")) {
				break;
			}
			String b = this.nextToken();
			a = a.substring(1, a.length() - 1).trim();
			try {
				int code = parseCode(a);
				int offset = Integer.parseInt(b);
				toCID.set(code, offset);
			} catch (NumberFormatException e) {
				// throw e;
			}
		}
	}

	private void parseCidRanges(IntMap toCID) throws IOException, CMapException {
		for (;;) {
			String a = this.nextToken();
			if (a.equals("endcidrange")) {
				break;
			}
			String b = this.nextToken();
			String c = this.nextToken();

			a = a.substring(1, a.length() - 1).trim();
			b = b.substring(1, b.length() - 1).trim();
			try {
				int start = parseCode(a);
				int end = parseCode(b);
				if (a.length() != b.length() || a.length() % 2 != 0) {
					throw new CMapException("開始位置と終了位置のキャラクターコードのバイト数が一致しないか、偶数桁の１16進数になっていません");
				}
				int offset = Integer.parseInt(c);

				int len = end - start + 1;
				for (int j = 0; j < len; ++j) {
					toCID.set(start + j, offset + j);
				}
			} catch (NumberFormatException e) {
				// throw e;
			}
		}
	}

	private void parseCidSystemInfo() throws IOException {
		this.nextToken();
		String dict = this.nextToken();
		if (!dict.equals("dict")) {
			return;
		}
		String dup = this.nextToken();
		if (!dup.equals("dup")) {
			return;
		}
		String begin = this.nextToken();
		if (!begin.equals("begin")) {
			return;
		}
		for (;;) {
			String key = this.nextToken();
			if (key.equals("/Registry")) {
				this.nextToken();
				String def = this.nextToken();
				if (!def.equals("def")) {
					return;
				}
			} else if (key.equals("/Ordering")) {
				this.nextToken();
				String def = this.nextToken();
				if (!def.equals("def")) {
					return;
				}
			} else if (key.equals("/Supplement")) {
				this.nextToken();
				String def = this.nextToken();
				if (!def.equals("def")) {
					return;
				}
			} else if (key.equals("end")) {
				break;
			}
		}
	}

	private void skipWhiteSpace() throws IOException {
		for (; this.ch != -1 && Character.isWhitespace((char) this.ch); this.ch = this.in.read()) {
			// do nothing
		}
		if (this.ch == -1) {
			throw new EOFException();
		}
	}

	private String nextToken() throws IOException {
		this.skipWhiteSpace();
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
}