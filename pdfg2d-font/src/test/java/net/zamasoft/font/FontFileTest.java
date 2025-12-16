package net.zamasoft.font;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import org.junit.jupiter.api.Test;

import net.zamasoft.font.table.CmapFormat;
import net.zamasoft.font.table.CmapTable;
import net.zamasoft.font.table.Table;

public class FontFileTest {
    @Test
    public void testLoadFont() throws Exception {
        File file = new File("src/test/resources/data/test.otf");

        FontFile font = new FontFile(file);
        OpenTypeFont otf = font.getFont();
        assertNotNull(otf, "OpenTypeFont should not be null");

        CmapTable cmapt = (CmapTable) otf.getTable(Table.cmap);
        assertNotNull(cmapt, "CmapTable should not be null");

        CmapFormat cmap = cmapt.getCmapFormat(Table.platformUnicode,
                Table.encodingUVS);
        assertNotNull(cmap, "CmapFormat should not be null");
    }
}
