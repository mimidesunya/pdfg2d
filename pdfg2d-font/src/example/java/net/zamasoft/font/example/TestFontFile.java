package net.zamasoft.font.example;

import java.io.File;

import net.zamasoft.font.FontFile;
import net.zamasoft.font.OpenTypeFont;
import net.zamasoft.font.table.CmapFormat;
import net.zamasoft.font.table.CmapTable;
import net.zamasoft.font.table.Table;

public class TestFontFile {
	public static void main(String[] args) throws Exception {
		File file = new File("src/example/data/test.otf");
		FontFile font = new FontFile(file);
		OpenTypeFont otf = font.getFont();
		CmapTable cmapt = (CmapTable)otf.getTable(Table.cmap);
		CmapFormat cmap = cmapt.getCmapFormat(Table.platformUnicode,
				Table.encodingUVS);
		System.out.println(cmap);
	}

}
