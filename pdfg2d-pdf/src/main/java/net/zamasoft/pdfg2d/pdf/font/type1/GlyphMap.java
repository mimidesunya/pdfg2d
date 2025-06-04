package net.zamasoft.pdfg2d.pdf.font.type1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * グリフ名とグリフコードの対応表です。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class GlyphMap implements Serializable {
	private static final long serialVersionUID = 0L;

	public static class Codes implements Serializable {
		private static final long serialVersionUID = 0L;
		public final int[] codes;
		public final String name;

		public Codes(int[] codes, String name) {
			this.codes = codes;
			this.name = name;
		}
	}

	public final Map<String, Codes> nameToCodes;

	public GlyphMap(Map<String, Codes> nameToCodes) {
		this.nameToCodes = nameToCodes;
	}

	/**
	 * キャラクタストリームからエンコーディングを読み込みます。
	 * 
	 * @param _in
	 * @return
	 * @throws IOException
	 */
	public static GlyphMap parse(InputStream _in) throws IOException {
		Map<String, Codes> map = new HashMap<String, Codes>();
		try (BufferedReader in = new BufferedReader(new InputStreamReader(_in, "ISO-8859-1"))) {
			for (String line = in.readLine(); line != null; line = in.readLine()) {
				if (line.charAt(0) == '#') {
					continue;
				}
				String[] pair = line.split(";", 2);
				String gname = pair[0].trim();
				String[] s = pair[1].trim().split("[\\s]+");
				int[] gids = new int[s.length];
				for (int i = 0; i < s.length; ++i) {
					gids[i] = Integer.parseInt(s[i], 16);
				}
				Codes codes = new Codes(gids, gname);
				map.put(codes.name, codes);
			}
		}
		return new GlyphMap(Collections.unmodifiableMap(map));
	}
}
