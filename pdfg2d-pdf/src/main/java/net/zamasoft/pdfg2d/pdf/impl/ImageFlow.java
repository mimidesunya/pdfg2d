package net.zamasoft.pdfg2d.pdf.impl;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DeflaterOutputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileCacheImageInputStream;
import javax.imageio.stream.FileCacheImageOutputStream;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import com.twelvemonkeys.imageio.plugins.jpeg.JPEGImageReader;
import jp.cssj.resolver.Source;
import net.zamasoft.pdfg2d.g2d.util.G2DUtils;
import net.zamasoft.pdfg2d.gc.image.Image;
import net.zamasoft.pdfg2d.pdf.ObjectRef;
import net.zamasoft.pdfg2d.pdf.PDFFragmentOutput;
import net.zamasoft.pdfg2d.pdf.gc.PDFImage;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;
import net.zamasoft.pdfg2d.pdf.util.codec.ASCII85OutputStream;
import net.zamasoft.pdfg2d.pdf.util.codec.ASCIIHexOutputStream;
import net.zamasoft.pdfg2d.pdf.util.io.FastBufferedOutputStream;
import net.zamasoft.pdfg2d.util.ColorUtils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import java.awt.geom.AffineTransform;
import net.zamasoft.pdfg2d.gc.image.util.TransformedImage;

/**
 * 画像データのフローです。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
class ImageFlow {
	private final Logger LOG = Logger.getLogger(ImageFlow.class.getName());

	private final Map<String, ObjectRef> nameToResourceRef;

	private final PDFFragmentOutputImpl objectsFlow;

	private final XRefImpl xref;

	private final PDFParams params;

	/** 画像URI(String)から画像(PDFImage)へのマッピングです。 */
	private final Map<URI, Image> images = new HashMap<URI, Image>();

	private int imageNumber = 0;

	private static final short DEVICE_GRAY = 1;
	private static final short DEVICE_RGB = 2;
	private static final short DEVICE_CMYK = 3;

	public ImageFlow(Map<String, ObjectRef> nameToResourceRef, PDFFragmentOutputImpl objectsFlow, XRefImpl xref,
			PDFParams params) throws IOException {
		this.xref = xref;
		this.nameToResourceRef = nameToResourceRef;
		this.objectsFlow = objectsFlow;
		this.params = params;
	}

	public Image loadImage(Source source) throws IOException {
		URI uri = source.getURI();
		Image pdfImage = (Image) this.images.get(uri);
		if (pdfImage != null) {
			return pdfImage;
		}
		ImageInputStream in;
		if (source.isFile()) {
			in = new FileImageInputStream(source.getFile()) {
				public void flushBefore(long pos) throws IOException {
					// 再読み込み不可能になることを防止するため、flushを無視する
				}
			};
		} else {
			in = new FileCacheImageInputStream(source.getInputStream(), null) {
				public void flushBefore(long pos) throws IOException {
					// 再読み込み不可能になることを防止するため、flushを無視する
				}
			};
		}
		try {
			pdfImage = this.addImage(in, null);
			this.images.put(uri, pdfImage);
		} finally {
			in.close();
		}
		return pdfImage;
	}

	public Image addImage(BufferedImage image) throws IOException {
		return this.addImage(null, image);
	}

	private Image addImage(ImageInputStream imageIn, BufferedImage image) throws IOException {
		PDFImage pdfImage;
		ImageReader ir;
		int orientation = 1;
		if (imageIn != null) {
			JPEGImageReader cir = null;
			Iterator<ImageReader> iri = ImageIO.getImageReaders(imageIn);
			for (;;) {
				if (iri != null && iri.hasNext()) {
					ir = iri.next();
					ir.setInput(imageIn);
					try {
						Iterator<ImageTypeSpecifier> iti = ir.getImageTypes(0);
						if (iti != null && iti.hasNext()) {
							imageIn.seek(0);
							if (ir instanceof JPEGImageReader) {
								cir = (JPEGImageReader) ir;
								continue;
							}
							break;
						}
					} catch (IOException e) {
						// ignore
					}
					ir.dispose();
					imageIn.seek(0);
				} else {
					if (cir == null) {
						throw new IOException("画像ファイル内に読み込み可能な画像がありません:" + String.valueOf(iri));
					}
					ir = cir;
					break;
				}
			}
			if (cir != null) {
				cir.dispose();
			}
		} else {
			ir = null;
		}

		int width, height;
		try {

			PDFParams.ColorMode colorMode = this.params.getColorMode();
			PDFParams.Compression streamCompression = this.params.getCompression();
			PDFParams.ImageCompression imageCompression = this.params.getImageCompression();
			PDFParams.Version pdfVersion = this.params.getVersion();
			boolean softMaskSupport = pdfVersion.v >= PDFParams.Version.V_1_4.v
					&& pdfVersion.v != PDFParams.Version.V_PDFA1B.v && pdfVersion.v != PDFParams.Version.V_PDFX1A.v;
			boolean jpeg2000Support = pdfVersion.v >= PDFParams.Version.V_1_5.v;
			PDFParams.ImageCompression imageType = PDFParams.ImageCompression.FLATE;

			if (ir != null && colorMode == PDFParams.ColorMode.PRESERVE) {
				String formatName = ir.getFormatName();
				if (this.params.getJPEGImage() == PDFParams.JPEGImage.RAW) {
					// 元画像形式の検出
					if (formatName.equalsIgnoreCase("jpeg")) {
						imageType = PDFParams.ImageCompression.JPEG;
					} else if (jpeg2000Support) {
						if (formatName.equalsIgnoreCase("jpeg 2000")) {
							imageType = PDFParams.ImageCompression.JPEG2000;
						}
					}
				} else if (imageCompression == PDFParams.ImageCompression.JPEG && formatName.equalsIgnoreCase("jpeg")) {
					imageType = PDFParams.ImageCompression.JPEG;
				} else if (imageCompression == PDFParams.ImageCompression.JPEG2000) {
					if (formatName.equalsIgnoreCase("jpeg 2000")) {
						imageType = PDFParams.ImageCompression.JPEG2000;
					}
				}
			}

			boolean iccErrorHuck = false;
			boolean iccGray = false;
			int maxWidth = this.params.getMaxImageWidth();
			int maxHeight = this.params.getMaxImageHeight();

			if (image == null) {
				try {
					width = ir.getWidth(0);
					height = ir.getHeight(0);
				} catch (RuntimeException e) {
					// JDK 1.4でInvalid ICC Profile Data
					// エラーが発生することへの対処
					LOG.log(Level.WARNING, "Error(Probably JVM bug?)", e);
					imageIn.seek(2);
					for (;;) {
						iccErrorHuck = true;
						int code = (imageIn.readShort() & 0xFFFF);
						if ((code >> 8) != 0xFF) {
							throw e;
						}
						if ((code >= 0xFFC0 && code <= 0xFFC3) || (code >= 0xFFC5 && code <= 0xFFC7)
								|| (code >= 0xFFC9 && code <= 0xFFCB) || (code >= 0xFFCD && code <= 0xFFCF)) {
							// フレーム
							imageIn.skipBytes(3);
							height = (imageIn.readShort() & 0xFFFF);
							width = (imageIn.readShort() & 0xFFFF);
							byte comps = imageIn.readByte();
							iccGray = comps == 1;
							break;
						}
						int length = (imageIn.readShort() & 0xFFFF);
						if (length <= 2) {
							throw e;
						}
						imageIn.skipBytes(length - 2);
					}
					imageIn.seek(0);
					try {
						Metadata metadata = ImageMetadataReader.readMetadata(new ImageInputStreamProxy(imageIn));
						Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
						if (directory != null && directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
							orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
						}
					} catch (ImageProcessingException e) {
						// ignore
					} catch (MetadataException e) {
						// ignore
					}
				}
			} else {
				width = image.getWidth();
				height = image.getHeight();
			}
			boolean resize = (maxWidth > 0 && width > maxWidth) || (maxHeight > 0 && height > maxHeight);
			double orgWidth = width;
			double orgHeight = height;
			if (resize || imageType == PDFParams.ImageCompression.FLATE) {
				// 再圧縮します
				if (ir != null) {
					imageIn.seek(0);
					image = G2DUtils.loadImage(ir, imageIn);
				}
				if (resize) {
					// 縮小
					int type = image.getType();
					if (maxWidth > 0 && width > maxWidth) {
						// height = height * maxWidth / width;
						// 縦横の解像度を不均衡にするため、こうしない
						width = maxWidth;
					}
					if (maxHeight > 0 && height > maxHeight) {
						// width = width * maxHeight / height;
						// 縦横の解像度を不均衡にするため、こうしない
						height = maxHeight;
					}
					java.awt.Image scaled = image.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
					try {
						image.flush();
						switch (type) {
						case BufferedImage.TYPE_BYTE_BINARY:
						case BufferedImage.TYPE_BYTE_INDEXED:
							image = new BufferedImage(width, height, type, (IndexColorModel) image.getColorModel());
							break;

						case BufferedImage.TYPE_3BYTE_BGR:
						case BufferedImage.TYPE_4BYTE_ABGR:
						case BufferedImage.TYPE_4BYTE_ABGR_PRE:
						case BufferedImage.TYPE_INT_ARGB:
						case BufferedImage.TYPE_INT_ARGB_PRE:
						case BufferedImage.TYPE_INT_BGR:
						case BufferedImage.TYPE_INT_RGB:
						case BufferedImage.TYPE_USHORT_555_RGB:
						case BufferedImage.TYPE_USHORT_565_RGB:
						case BufferedImage.TYPE_BYTE_GRAY:
						case BufferedImage.TYPE_USHORT_GRAY:
							image = new BufferedImage(width, height, type);
							break;

						default:
							image = new BufferedImage(width, height,
									image.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB
											: BufferedImage.TYPE_INT_RGB);
							break;
						}
						Graphics2D g2d = image.createGraphics();
						g2d.drawImage(scaled, 0, 0, null);
					} finally {
						scaled.flush();
					}
					imageType = PDFParams.ImageCompression.FLATE;
				}

				// グレースケールフィルタ
				if (colorMode == PDFParams.ColorMode.GRAY && image.getType() != BufferedImage.TYPE_BYTE_GRAY
						&& image.getType() != BufferedImage.TYPE_USHORT_GRAY) {
					for (int y = 0; y < height; ++y) {
						for (int x = 0; x < width; ++x) {
							int rgb = image.getRGB(x, y);
							float r = ((rgb >> 16) & 0xFF) / 255f;
							float g = ((rgb >> 8) & 0xFF) / 255f;
							float b = (rgb & 0xFF) / 255f;
							float gr = ColorUtils.toGray(r, g, b);
							int octet = (int) (gr * 255f);
							rgb = (rgb & 0xFF000000) | (octet << 16) | (octet << 8) | octet;
							image.setRGB(x, y, rgb);
						}
					}
				}
			}
			try {
				String name = "I" + this.imageNumber;
				pdfImage = new PDFImage(name, orgWidth, orgHeight);

				ObjectRef imageRef = this.xref.nextObjectRef();
				this.nameToResourceRef.put(name, imageRef);

				this.objectsFlow.startObject(imageRef);
				this.objectsFlow.startHash();

				this.objectsFlow.writeName("Type");
				this.objectsFlow.writeName("XObject");
				this.objectsFlow.breakBefore();

				this.objectsFlow.writeName("Subtype");
				this.objectsFlow.writeName("Image");
				this.objectsFlow.breakBefore();

				this.objectsFlow.writeName("Name");
				this.objectsFlow.writeName(name);
				this.objectsFlow.breakBefore();

				this.objectsFlow.writeName("Width");
				this.objectsFlow.writeInt(width);
				this.objectsFlow.breakBefore();

				this.objectsFlow.writeName("Height");
				this.objectsFlow.writeInt(height);
				this.objectsFlow.breakBefore();

				if (imageType != PDFParams.ImageCompression.FLATE) {
					// 画像の生データを出力
					try {
						this.objectsFlow.writeName("BitsPerComponent");
						this.objectsFlow.writeInt(8);
						this.objectsFlow.breakBefore();

						final short deviceColor;
						if (iccErrorHuck && iccGray) {
							deviceColor = DEVICE_GRAY;
						} else if (ir instanceof JPEGImageReader) {
							deviceColor = DEVICE_CMYK;
						} else {
							Iterator<?> itr = ir.getImageTypes(0);
							ImageTypeSpecifier its = (ImageTypeSpecifier) itr.next();
							ColorModel cm = its.getColorModel();
							switch (cm.getNumComponents()) {
							case 1:
								deviceColor = DEVICE_GRAY;
								break;
							case 4:
								deviceColor = DEVICE_CMYK;
								break;
							default:
								deviceColor = DEVICE_RGB;
								break;
							}
						}

						this.objectsFlow.writeName("ColorSpace");
						switch (deviceColor) {
						case DEVICE_GRAY:
							this.objectsFlow.writeName("DeviceGray");
							break;
						case DEVICE_CMYK:
							this.objectsFlow.writeName("DeviceCMYK");
							break;
						default:
							this.objectsFlow.writeName("DeviceRGB");
							break;
						}
						this.objectsFlow.breakBefore();

						if (deviceColor == DEVICE_CMYK) {
							this.objectsFlow.writeName("Decode");
							this.objectsFlow.startArray();
							this.objectsFlow.writeInt(1);
							this.objectsFlow.writeInt(0);
							this.objectsFlow.writeInt(1);
							this.objectsFlow.writeInt(0);
							this.objectsFlow.writeInt(1);
							this.objectsFlow.writeInt(0);
							this.objectsFlow.writeInt(1);
							this.objectsFlow.writeInt(0);
							this.objectsFlow.endArray();
							this.objectsFlow.breakBefore();
						}

						this.objectsFlow.writeName("Filter");
						this.objectsFlow.startArray();
						switch (streamCompression) {
						case ASCII:
							this.objectsFlow.writeName("ASCII85Decode");
							break;
						case NONE:
							this.objectsFlow.writeName("ASCIIHexDecode");
							break;
						default:
							// ignore
						}
						switch (imageType) {
						case JPEG:
							this.objectsFlow.writeName("DCTDecode");
							break;
						case JPEG2000:
							this.objectsFlow.writeName("JPXDecode");
							break;
						default:
							throw new IllegalStateException();
						}
						this.objectsFlow.endArray();
						this.objectsFlow.breakBefore();

						OutputStream out = this.objectsFlow.startStreamFromHash(PDFFragmentOutput.Mode.RAW);
						try {
							switch (streamCompression) {
							case ASCII:
								out = new ASCII85OutputStream(out);
								break;
							case NONE:
								out = new ASCIIHexOutputStream(out);
								break;
							default:
								// ignore
							}
							imageIn.seek(0);
							byte[] buff = this.objectsFlow.getBuff();
							for (int len = imageIn.read(buff); len != -1; len = imageIn.read(buff)) {
								out.write(buff, 0, len);
							}
						} finally {
							out.close();
						}
					} finally {
						this.objectsFlow.endObject();
					}
				} else {
					// 再圧縮する
					ObjectRef imageMaskRef;
					ColorModel cm;
					try {
						cm = image.getColorModel();

						if ((width + height) > this.params.getImageCompressionLossless()) {
							imageType = imageCompression;
						}
						ImageWriter iw;
						ImageWriteParam iwParams;
						switch (imageType) {
						case FLATE:
							iw = null;
							iwParams = null;
							break;
						case JPEG: {
							Iterator<?> i = ImageIO.getImageWritersByFormatName("jpeg");
							if (i == null || !i.hasNext()) {
								throw new IOException("この環境ではJPEGの出力をサポートしていません。");
							}
							iw = (ImageWriter) i.next();
							iwParams = iw.getDefaultWriteParam();
							iwParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
							iwParams.setCompressionQuality(.8f);
						}
							break;
						case JPEG2000:
							Iterator<?> i = ImageIO.getImageWritersByFormatName("jpeg 2000");
							if (i == null || !i.hasNext()) {
								throw new IOException(
										"JPEG2000を出力するにはJava Advanced Imaging Image I/O Tools(JAI-ImageIO)が必要です。");
							}
							iw = (ImageWriter) i.next();
							iwParams = null;
							break;
						default:
							throw new IllegalStateException();
						}

						if (cm.hasAlpha()) {
							// 透明化GIF/PNG
							imageMaskRef = this.xref.nextObjectRef();
							if (softMaskSupport) {
								this.objectsFlow.writeName("SMask");
							} else {
								this.objectsFlow.writeName("Mask");
							}
							this.objectsFlow.writeObjectRef(imageMaskRef);
							this.objectsFlow.breakBefore();
						} else {
							// マスクなし
							imageMaskRef = null;
						}

						this.objectsFlow.writeName("BitsPerComponent");
						this.objectsFlow.writeInt(8);
						this.objectsFlow.breakBefore();

						boolean deviceGray = (cm.getNumComponents() == 1);
						this.objectsFlow.writeName("ColorSpace");
						this.objectsFlow.writeName(deviceGray ? "DeviceGray" : "DeviceRGB");
						this.objectsFlow.breakBefore();

						this.objectsFlow.writeName("Filter");
						this.objectsFlow.startArray();
						switch (streamCompression) {
						case ASCII:
							this.objectsFlow.writeName("ASCII85Decode");
							if (imageType == PDFParams.ImageCompression.FLATE) {
								this.objectsFlow.writeName("FlateDecode");
							}
							break;
						case NONE:
							this.objectsFlow.writeName("ASCIIHexDecode");
							break;
						default:
							if (imageType == PDFParams.ImageCompression.FLATE) {
								this.objectsFlow.writeName("FlateDecode");
							}
							break;
						}
						switch (imageType) {
						case FLATE:
							break;
						case JPEG:
							this.objectsFlow.writeName("DCTDecode");
							break;
						case JPEG2000:
							this.objectsFlow.writeName("JPXDecode");
							break;
						default:
							throw new IllegalStateException();
						}
						this.objectsFlow.endArray();
						this.objectsFlow.breakBefore();

						OutputStream out = this.objectsFlow.startStreamFromHash(PDFFragmentOutput.Mode.RAW);
						switch (streamCompression) {
						case ASCII:
							out = new ASCII85OutputStream(out);
							if (imageType == PDFParams.ImageCompression.FLATE) {
								out = new DeflaterOutputStream(out);
							}
							break;
						case NONE:
							out = new ASCIIHexOutputStream(out);
							break;
						default:
							if (imageType == PDFParams.ImageCompression.FLATE) {
								out = new DeflaterOutputStream(out);
							}
							break;
						}
						switch (imageType) {
						case JPEG:
						case JPEG2000: {
							// JPEG / JPEG 2000
							BufferedImage ximage = image;
							if (cm.hasAlpha()) {
								ximage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
								ximage.createGraphics().drawImage(image, 0, 0, null);
							}
							if (image.getType() == BufferedImage.TYPE_USHORT_GRAY) {
								ximage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
								ximage.createGraphics().drawImage(image, 0, 0, null);
							}
							try {
								try {
									FileCacheImageOutputStream iout = new FileCacheImageOutputStream(out, null);
									try {
										iw.setOutput(iout);
										iw.write(null, new IIOImage(ximage, null, null), iwParams);
									} finally {
										iout.close();
									}
								} finally {
									iw.dispose();
								}
							} finally {
								if (ximage != image) {
									ximage.flush();
								}
							}
						}
							break;
						case FLATE: {
							// 可逆圧縮
							Raster raster = image.getRaster();
							out = new FastBufferedOutputStream(out, this.objectsFlow.getBuff());
							if (deviceGray) {
								// グレースケール
								Object pixel = raster.getDataElements(0, 0, null);
								for (int y = 0; y < height; ++y) {
									for (int x = 0; x < width; ++x) {
										pixel = raster.getDataElements(x, y, pixel);
										out.write(cm.getGreen(pixel));
									}
								}
							} else {
								// カラー
								Object pixel = raster.getDataElements(0, 0, null);
								for (int y = 0; y < height; ++y) {
									for (int x = 0; x < width; ++x) {
										pixel = raster.getDataElements(x, y, pixel);
										out.write(cm.getRed(pixel));
										out.write(cm.getGreen(pixel));
										out.write(cm.getBlue(pixel));
									}
								}
							}
						}
							break;

						default:
							throw new IllegalStateException();
						}
						out.close();
					} finally {
						this.objectsFlow.endObject();
					}

					if (imageMaskRef != null) {
						// マスク
						this.objectsFlow.startObject(imageMaskRef);
						try {
							this.objectsFlow.startHash();

							this.objectsFlow.writeName("Type");
							this.objectsFlow.writeName("XObject");
							this.objectsFlow.breakBefore();

							this.objectsFlow.writeName("Subtype");
							this.objectsFlow.writeName("Image");
							this.objectsFlow.breakBefore();

							if (!softMaskSupport) {
								this.objectsFlow.writeName("ImageMask");
								this.objectsFlow.writeBoolean(true);
								this.objectsFlow.breakBefore();
							}

							this.objectsFlow.writeName("Width");
							this.objectsFlow.writeInt(width);
							this.objectsFlow.breakBefore();

							this.objectsFlow.writeName("Height");
							this.objectsFlow.writeInt(height);
							this.objectsFlow.breakBefore();

							if (softMaskSupport) {
								this.objectsFlow.writeName("ColorSpace");
								this.objectsFlow.writeName("DeviceGray");
								this.objectsFlow.breakBefore();
							}

							this.objectsFlow.writeName("BitsPerComponent");
							this.objectsFlow.writeInt(softMaskSupport ? 8 : 1);
							this.objectsFlow.breakBefore();

							this.objectsFlow.writeName("Filter");
							this.objectsFlow.startArray();
							switch (streamCompression) {
							case ASCII:
								this.objectsFlow.writeName("ASCII85Decode");
								this.objectsFlow.writeName("FlateDecode");
								break;
							case NONE:
								this.objectsFlow.writeName("ASCIIHexDecode");
								break;
							default:
								this.objectsFlow.writeName("FlateDecode");
								break;
							}
							this.objectsFlow.endArray();
							this.objectsFlow.breakBefore();

							OutputStream out = this.objectsFlow.startStreamFromHash(PDFFragmentOutput.Mode.RAW);
							switch (streamCompression) {
							case ASCII:
								out = new DeflaterOutputStream(new ASCII85OutputStream(out));
								break;
							case NONE:
								out = new ASCIIHexOutputStream(out);
								break;
							default:
								out = new DeflaterOutputStream(out);
								break;
							}
							out = new FastBufferedOutputStream(out, this.objectsFlow.getBuff());

							Raster raster = image.getRaster();
							Object pixel = raster.getDataElements(0, 0, null);
							if (softMaskSupport) {
								for (int y = 0; y < height; ++y) {
									for (int x = 0; x < width; ++x) {
										pixel = raster.getDataElements(x, y, pixel);
										out.write(cm.getAlpha(pixel) & 0xFF);
									}
								}
							} else {
								int b = 0;
								for (int y = 0; y < height; ++y) {
									for (int x = 0; x < width; ++x) {
										pixel = raster.getDataElements(x, y, pixel);
										int off = 7 - (x % 8);
										if ((cm.getAlpha(pixel) & 0xFF) <= 0x7F) {
											b |= 1 << off;
										}
										if (off == 0) {
											out.write(b);
											b = 0;
										}
									}
									if (width % 8 != 0) {
										out.write(b);
										b = 0;
									}
								}
							}
							out.close();
						} finally {
							this.objectsFlow.endObject();
						}
					}
				}
			} finally {
				if (image != null) {
					image.flush();
				}
			}
		} catch (IOException e) {
			LOG.log(Level.WARNING, "画像生成中のI/Oエラー", e);
			throw e;
		} catch (RuntimeException e) {
			LOG.log(Level.SEVERE, "画像生成中の予期しないエラー", e);
			throw e;
		} finally {
			if (ir != null) {
				ir.dispose();
			}
		}
		++this.imageNumber;
		if (orientation == 1) {
			return pdfImage;
		}
		final AffineTransform at = new AffineTransform();
		switch (orientation) {
			case 2: at.scale(-1, 1); at.translate(-width, 0); break;
			case 3: at.rotate(Math.PI, width / 2.0, height / 2.0); break;
			case 4: at.scale(1, -1); at.translate(0, -height); break;
			case 5: at.rotate(Math.PI / 2); at.scale(-1, 1); at.translate(0, -height); break;
			case 6: at.rotate(Math.PI / 2); at.translate(0, -height); break;
			case 7: at.rotate(-Math.PI / 2); at.scale(-1, 1); at.translate(-width, 0); break;
			case 8: at.rotate(-Math.PI / 2); at.translate(-width, 0); break;
			default: return pdfImage;
		}
		Image retImage = new TransformedImage(pdfImage, at);
		return retImage;
	}
}
