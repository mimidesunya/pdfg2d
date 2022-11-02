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
public class Encoding implements Serializable {
	private static final long serialVersionUID = 0L;

	public static class CodeMap implements Serializable {
		private static final long serialVersionUID = 0L;

		public final int gid;
		public final String name;

		public CodeMap(int gid, String name) {
			this.gid = gid;
			this.name = name;
		}
	}

	public final String name;

	public final CodeMap[] codeMaps;

	public final Map<String, CodeMap> nameToCodeMap;

	public Encoding(String name, Map<String, CodeMap> nameToCodeMap) {
		this.name = name;
		this.nameToCodeMap = nameToCodeMap;
		this.codeMaps = (CodeMap[]) nameToCodeMap.values().toArray(new CodeMap[nameToCodeMap.size()]);
	}

	/**
	 * キャラクタストリームからエンコーディングを読み込みます。
	 * 
	 * @param _in
	 * @return
	 * @throws IOException
	 */
	public static Encoding parse(InputStream _in) throws IOException {
		Map<String, CodeMap> map = new HashMap<String, CodeMap>();
		String name;
		try (BufferedReader in = new BufferedReader(new InputStreamReader(_in, "ISO-8859-1"))) {
			name = in.readLine();
			for (String line = in.readLine(); line != null; line = in.readLine()) {
				if (line.charAt(0) == '#') {
					continue;
				}
				String[] pair = line.split(";", 2);
				String gname = pair[0].trim();
				int gid = Integer.parseInt(pair[1], 16);
				CodeMap codeMap = new CodeMap(gid, gname);
				map.put(codeMap.name, codeMap);
			}
		}
		return new Encoding(name, Collections.unmodifiableMap(map));
	}
}
