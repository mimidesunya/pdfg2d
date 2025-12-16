package net.zamasoft.pdfg2d.demo.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

/**
 * A utility class to inspect PDF text positions and attributes.
 */
public class TextInspector extends PDFTextStripper {

    public static class TextInfo {
        public final String text;
        public final float x;
        public final float y;
        public final float fontSize;
        public final String fontName;

        public TextInfo(String text, float x, float y, float fontSize, String fontName) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.fontSize = fontSize;
            this.fontName = fontName;
        }

        @Override
        public String toString() {
            return String.format("'%s' at (%.1f, %.1f) [%s %.1f]", text, x, y, fontName, fontSize);
        }
    }

    private final List<TextInfo> textInfos = new ArrayList<>();

    public TextInspector() throws IOException {
        super();
        setSortByPosition(true); // Process text in visual order
    }

    @Override
    protected void processTextPosition(TextPosition text) {
        textInfos.add(new TextInfo(
                text.getUnicode(),
                text.getXDirAdj(),
                text.getYDirAdj(),
                text.getFontSizeInPt(),
                text.getFont().getName()));
        super.processTextPosition(text);
    }

    public List<TextInfo> getTextInfos() {
        return textInfos;
    }
}
