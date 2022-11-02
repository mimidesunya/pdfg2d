package jp.cssj.sakae.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class BuildEmojiIndexTool {

	public static void main(String[] args) throws Exception {
		File dir = new File("src/main/resources/jp/cssj/sakae/font/emoji");
		File indexFile = new File(dir, "INDEX");
		try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(indexFile), "ISO8859-1"))) {
			Set<String> codes = new HashSet<String>();
			for (String code : dir.list()) {
				if (!code.endsWith(".svg")) {
					continue;
				}
				code = code.substring(7, code.length() - 4);
				for (;;) {
					if (!codes.contains(code)) {
						out.println(code);
						codes.add(code);
					}
					int ub = code.lastIndexOf('_');
					if (ub == -1) {
						break;
					}
					code = code.substring(0, ub);
				}
			}
			out.println("200d");
			
			for (String code : dir.list()) {
				if (!code.endsWith(".svg")) {
					continue;
				}
				code = code.substring(7, code.length() - 4);
				for (String c : code.split("_")) {
					if (!codes.contains(c)) {
						System.err.println(c);
						codes.add(c);
					}
				}
			}
		}
	}

}
