package net.zamasoft.pdfg2d.font.emoji;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;

import net.zamasoft.pdfg2d.font.BBox;
import net.zamasoft.pdfg2d.font.FontSource;
import net.zamasoft.pdfg2d.font.ImageFont;
import net.zamasoft.pdfg2d.gc.GC;
import net.zamasoft.pdfg2d.gc.GraphicsException;
import net.zamasoft.pdfg2d.gc.font.util.FontUtils;
import net.zamasoft.pdfg2d.gc.text.Text;
import net.zamasoft.pdfg2d.pdf.gc.PDFGC;
import net.zamasoft.pdfg2d.pdf.gc.PDFGroupImage;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;
import net.zamasoft.pdfg2d.svg.Dimension2DImpl;
import net.zamasoft.pdfg2d.svg.GVTBuilderImpl;
import net.zamasoft.pdfg2d.svg.SVGBridgeGraphics2D;
import net.zamasoft.pdfg2d.svg.SVGUserAgentImpl;

class EmojiFont implements ImageFont {
	private static final long serialVersionUID = 2L;
	protected final EmojiFontSource source;
	protected final Map<Integer, GraphicsNode> gidToNode = new HashMap<Integer, GraphicsNode>();
	protected final Map<Integer, PDFGroupImage> gidToImage = new HashMap<Integer, PDFGroupImage>();
	protected static final Dimension2D VIEWPORT = new Dimension2DImpl(128, 128);

	public EmojiFont(EmojiFontSource source) {
		this.source = source;
	}

	public int toGID(final int c) {
		String code = Integer.toHexString(c);
		Integer fgid = EmojiFontSource.codeToFgid.get(code);
		if (fgid == null) {
			return 0;
		}
		return fgid;
	}

	public short getAdvance(int gid) {
		return 1000;
	}

	public short getWidth(int gid) {
		return 1000;
	}

	public BBox getBBox() {
		EmojiFontSource source = (EmojiFontSource) this.source;
		return source.getBBox();
	}

	public FontSource getFontSource() {
		return this.source;
	}

	public short getKerning(int sgid, int gid) {
		return 0;
	}

	public int getLigature(int gid, int cid) {
		if (gid == -1) {
			return -1;
		}
		String scode = EmojiFontSource.fgidToCode.get(gid);
		String code = scode + "_" + Integer.toHexString(cid);
		Integer fgid = EmojiFontSource.codeToFgid.get(code);
		if (fgid == null) {
			return -1;
		}
		return fgid;
	}

	public void drawTo(GC gc, Text text) throws IOException, GraphicsException {
		FontUtils.drawText(gc, this, text);
	}

	public void drawGlyphForGid(GC gc, int gid, AffineTransform at) {
		GraphicsNode gvtRoot = null;
		PDFGroupImage image = this.gidToImage.get(gid);
		if (image == null) {
			gvtRoot = this.gidToNode.get(gid);
			if (gvtRoot == null) {
				String code = EmojiFontSource.fgidToCode.get(gid);
				if (code.endsWith("_200d")) {
					code = code.substring(0, code.length() - 5);
				}
				String fileName = "emoji_u" + code + ".svg";
				try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(
						new java.io.BufferedInputStream(EmojiFontSource.class.getResourceAsStream("emoji.zip")))) {
					java.util.zip.ZipEntry entry;
					while ((entry = zis.getNextEntry()) != null) {
						if (fileName.equals(entry.getName())) {
							SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(
									XMLResourceDescriptor.getXMLParserClassName());
							SVGOMDocument doc = (SVGOMDocument) factory.createDocument(null, zis);
							UserAgent ua = new SVGUserAgentImpl(VIEWPORT);
							DocumentLoader loader = new DocumentLoader(ua);
							BridgeContext ctx = new BridgeContext(ua, loader);
							ctx.setDynamic(false);
							GVTBuilder gvt = new GVTBuilderImpl();
							gvtRoot = gvt.build(ctx, doc);
							if (gc instanceof PDFGC
									&& ((PDFGC) gc).getPDFGraphicsOutput().getPdfWriter().getParams()
											.getVersion().v >= PDFParams.Version.V_1_4.v) {
								image = ((PDFGC) gc).getPDFGraphicsOutput().getPdfWriter().createGroupImage(1000, 1000);
								PDFGC gc2 = new PDFGC(image);
								gc2.transform(AffineTransform.getScaleInstance(1000.0 / VIEWPORT.getWidth(),
										1000.0 / VIEWPORT.getHeight()));
								gc2.begin();
								Graphics2D g2d = new SVGBridgeGraphics2D(gc2);
								gvtRoot.paint(g2d);
								g2d.dispose();
								gc2.end();
								image.close();
								this.gidToImage.put(gid, image);
							} else {
								this.gidToNode.put(gid, gvtRoot);
							}
							break;
						}
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

		gc.begin();
		if (at != null) {
			gc.transform(at);
		}
		gc.transform(AffineTransform.getTranslateInstance(0, -this.source.getAscent()));
		if (image != null) {
			gc.begin();
			gc.drawImage(image);
		} else {
			gc.transform(AffineTransform.getScaleInstance(1000.0 / VIEWPORT.getWidth(), 1000.0 / VIEWPORT.getHeight()));
			gc.begin();
			Graphics2D g2d = new SVGBridgeGraphics2D(gc);
			gvtRoot.paint(g2d);
			g2d.dispose();
		}
		gc.end();
		gc.end();
	}
}
