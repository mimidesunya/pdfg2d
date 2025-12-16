# pdfg2d

## What is this?
pdfg2d is the fastest PDF generator for Java.

## How to dowload
pdfg2d is available on Maven Central Repository.

[pdfg2d](https://search.maven.org/artifact/io.github.mimidesunya/pdfg2d-pdf)

[Emoji support](https://search.maven.org/artifact/io.github.mimidesunya/pdfg2d-svg)

You can get Jars with Maven, Gradle etc.

Maven:
```
<dependency>
<groupId>io.github.mimidesunya</groupId>
<artifactId>pdfg2d-pdf</artifactId>
<version>1.2.0</version>
</dependency>
```

Gradle:
```
implementation 'io.github.mimidesunya:pdfg2d-pdf:1.2.0'
```

## List of features
* Java 1.8 Support
* PDF Versions
	* 1.2 to 1.7
	* PDF/A-1b
	* PDF/X-1a
* PDF Features
	* Bookmarks
	* Permissions
	* Arcfour(RC4) cipher
	* AES cipher
	* Meta Information
	* Viewer Preferences
	* Open JavaScript Action
	* RGB / Gray /CMYK Color Mode
	* File Attachments
	* Hyper Links
* Graphics Features
	* java.awt.Grahics2D Bridge
	* Group Images
	* Tiling Patterns
	* Shading Patterns
	* SVG Images [demo](https://github.com/mimidesunya/pdfg2d/blob/main/pdfg2d-demo/src/main/java/net/zamasoft/pdfg2d/demo/SVGTigerApp.java)
	* Emoji [demo](https://github.com/mimidesunya/pdfg2d/blob/main/pdfg2d-demo/src/main/java/net/zamasoft/pdfg2d/demo/EmojiApp.java)
* PDF Compressoion
	* No compression
	* Deflate
	* Deflate + Ascii85
* Image Compression
	* Deflate
	* JPEG
	* JPEG2000
* Fonts
	* Core 14 Fonts
	* CID-Keyed Fonts (Chinese, Japanese, Korean, HK/Taiwanese)
	* Embedded Fonts (TrueType, OpenType/CFF, WOFF)

## Example
[Program:](https://github.com/mimidesunya/pdfg2d/blob/main/pdfg2d-demo/src/main/java/net/zamasoft/pdfg2d/demo/DrawApp.java)

```
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.File;

import net.zamasoft.pdfg2d.PDFGraphics2D;
import net.zamasoft.pdfg2d.pdf.util.PDFUtils;

public class DrawApp {
	public static void main(String[] args) throws Exception {
		try (PDFGraphics2D g2d = new PDFGraphics2D(new File("out/draw.pdf"))) {
			g2d.setColor(Color.WHITE);
			g2d.fill(new Rectangle2D.Double(0, 0, PDFUtils.mmToPt(PDFUtils.PAPER_A4_WIDTH_MM),
					PDFUtils.mmToPt(PDFUtils.PAPER_A4_HEIGHT_MM)));

			g2d.setColor(Color.RED);
			g2d.fill(new Rectangle2D.Double(PDFUtils.mmToPt(51), 0, PDFUtils.mmToPt(PDFUtils.PAPER_A4_WIDTH_MM - 51),
					PDFUtils.mmToPt(154)));

			g2d.setColor(Color.BLUE);
			g2d.fill(new Rectangle2D.Double(0, PDFUtils.mmToPt(154), PDFUtils.mmToPt(51),
					PDFUtils.mmToPt(PDFUtils.PAPER_A4_HEIGHT_MM) - 154));

			g2d.setColor(Color.YELLOW);
			g2d.fill(new Rectangle2D.Double(PDFUtils.mmToPt(187), PDFUtils.mmToPt(182), PDFUtils.mmToPt(PDFUtils.PAPER_A4_WIDTH_MM - 187),
					PDFUtils.mmToPt(PDFUtils.PAPER_A4_HEIGHT_MM) - 182));
			
			g2d.setStroke(new BasicStroke((float)PDFUtils.mmToPt(7)));
			g2d.setColor(Color.BLACK);
			g2d.draw(new Line2D.Double(PDFUtils.mmToPt(51), 0, PDFUtils.mmToPt(51), PDFUtils.mmToPt(PDFUtils.PAPER_A4_HEIGHT_MM)));
			g2d.draw(new Line2D.Double(0, PDFUtils.mmToPt(154), PDFUtils.mmToPt(PDFUtils.PAPER_A4_WIDTH_MM), PDFUtils.mmToPt(154)));
			g2d.draw(new Line2D.Double(PDFUtils.mmToPt(187), PDFUtils.mmToPt(154), PDFUtils.mmToPt(187), PDFUtils.mmToPt(PDFUtils.PAPER_A4_HEIGHT_MM)));

			g2d.setStroke(new BasicStroke((float)PDFUtils.mmToPt(14)));
			g2d.draw(new Line2D.Double(0, PDFUtils.mmToPt(70), PDFUtils.mmToPt(51), PDFUtils.mmToPt(70)));
			g2d.draw(new Line2D.Double(PDFUtils.mmToPt(187), PDFUtils.mmToPt(182), PDFUtils.mmToPt(PDFUtils.PAPER_A4_WIDTH_MM), PDFUtils.mmToPt(182)));
		}
	}
}

```
Result:

[PDF](draw.pdf?raw=true)

![Mondriaan](draw.png)
