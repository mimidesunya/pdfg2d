package net.zamasoft.pdfg2d.pdf.impl;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.IdentityHashMap;
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

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.twelvemonkeys.imageio.plugins.jpeg.JPEGImageReader;

import net.zamasoft.pdfg2d.g2d.util.G2DUtils;
import net.zamasoft.pdfg2d.gc.image.Image;
import net.zamasoft.pdfg2d.gc.image.util.TransformedImage;
import net.zamasoft.pdfg2d.pdf.ObjectRef;
import net.zamasoft.pdfg2d.pdf.PDFFragmentOutput;
import net.zamasoft.pdfg2d.pdf.gc.PDFImage;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;
import net.zamasoft.pdfg2d.pdf.util.codec.ASCII85OutputStream;
import net.zamasoft.pdfg2d.pdf.util.codec.ASCIIHexOutputStream;
import net.zamasoft.pdfg2d.pdf.util.io.FastBufferedOutputStream;
import net.zamasoft.pdfg2d.resolver.Source;
import net.zamasoft.pdfg2d.util.ColorUtils;

/**
 * Handles the loading, scaling, and serialization of image resources into PDF
 * XObjects.
 * Supports various formats (JPEG, PNG, etc.), color mode conversions, and EXIF
 * orientation.
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

	/** Mapping from image URI (String) to image (PDFImage). */
	private final Map<URI, Image> images = new HashMap<>();

	/** Mapping from BufferedImage instance to image (PDFImage). */
	private final Map<BufferedImage, Image> bufferedImages = new IdentityHashMap<>();

	private int imageNumber = 0;

	private static final short DEVICE_GRAY = 1;
	private static final short DEVICE_RGB = 2;
	private static final short DEVICE_CMYK = 3;

	public ImageFlow(final Map<String, ObjectRef> nameToResourceRef, final PDFFragmentOutputImpl objectsFlow,
			final XRefImpl xref, final PDFParams params) throws IOException {
		this.xref = xref;
		this.nameToResourceRef = nameToResourceRef;
		this.objectsFlow = objectsFlow;
		this.params = params;
	}

	public Image loadImage(final Source source) throws IOException {
		final var uri = source.getURI();
		var pdfImage = this.images.get(uri);
		if (pdfImage != null) {
			return pdfImage;
		}
		// Wrap stream to prevent ImageIO from closing the underlying source too early
		final ImageInputStream in = source.isFile() ? new FileImageInputStream(source.getFile()) {
			@Override
			public void flushBefore(final long pos) throws IOException {
				// Prevent reloading issues
			}
		} : new FileCacheImageInputStream(source.getInputStream(), null) {
			@Override
			public void flushBefore(final long pos) throws IOException {
				// Prevent reloading issues
			}
		};

		try {
			pdfImage = this.addImage(in, null);
			this.images.put(uri, pdfImage);
			return pdfImage;
		} finally {
			in.close();
		}
	}

	public Image addImage(final BufferedImage image) throws IOException {
		final var pdfImage = this.bufferedImages.get(image);
		if (pdfImage != null) {
			return pdfImage;
		}
		final var res = this.addImage(null, image);
		this.bufferedImages.put(image, res);
		return res;
	}

	private Image addImage(final ImageInputStream imageIn, final BufferedImage originalImage) throws IOException {
		var image = originalImage;
		int orientation = 1;
		ImageReader ir = null;

		if (imageIn != null) {
			JPEGImageReader cir = null;
			final var iri = ImageIO.getImageReaders(imageIn);
			while (iri.hasNext()) {
				final var reader = iri.next();
				reader.setInput(imageIn);
				try {
					final var iti = reader.getImageTypes(0);
					if (iti != null && iti.hasNext()) {
						imageIn.seek(0);
						if (reader instanceof final JPEGImageReader jr) {
							cir = jr;
							continue;
						}
						ir = reader;
						break;
					}
				} catch (final IOException e) {
					// Ignore and try next reader
				}
				reader.dispose();
				imageIn.seek(0);
			}
			if (ir == null) {
				if (cir == null) {
					throw new IOException("No readable images found in the stream.");
				}
				ir = cir;
			} else if (cir != null) {
				cir.dispose();
			}
		}

		int width, height;
		final var name = "I" + this.imageNumber;
		PDFImage pdfImage = null;
		try {

			final PDFParams.ColorMode colorMode = this.params.getColorMode();
			final PDFParams.Compression streamCompression = this.params.getCompression();
			final PDFParams.ImageCompression imageCompression = this.params.getImageCompression();
			final PDFParams.Version pdfVersion = this.params.getVersion();
			final boolean softMaskSupport = pdfVersion.v >= PDFParams.Version.V_1_4.v
					&& pdfVersion.v != PDFParams.Version.V_PDFA1B.v && pdfVersion.v != PDFParams.Version.V_PDFX1A.v;
			final boolean jpeg2000Support = pdfVersion.v >= PDFParams.Version.V_1_5.v;
			PDFParams.ImageCompression imageType = PDFParams.ImageCompression.FLATE;

			if (ir != null && colorMode == PDFParams.ColorMode.PRESERVE) {
				final String formatName = ir.getFormatName();
				if (this.params.getJPEGImage() == PDFParams.JPEGImage.RAW) {
					// Detection of original image format
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
			final int maxWidth = this.params.getMaxImageWidth();
			final int maxHeight = this.params.getMaxImageHeight();

			if (image == null) {
				try {
					width = ir.getWidth(0);
					height = ir.getHeight(0);
				} catch (final RuntimeException e) {
					// Workaround for Invalid ICC Profile Data error in JDK 1.4
					LOG.log(Level.WARNING, "Error(Probably JVM bug?)", e);
					imageIn.seek(2);
					for (;;) {
						iccErrorHuck = true;
						final int code = (imageIn.readShort() & 0xFFFF);
						if ((code >> 8) != 0xFF) {
							throw e;
						}
						if ((code >= 0xFFC0 && code <= 0xFFC3) || (code >= 0xFFC5 && code <= 0xFFC7)
								|| (code >= 0xFFC9 && code <= 0xFFCB) || (code >= 0xFFCD && code <= 0xFFCF)) {
							// Frame
							imageIn.skipBytes(3);
							height = (imageIn.readShort() & 0xFFFF);
							width = (imageIn.readShort() & 0xFFFF);
							final byte comps = imageIn.readByte();
							iccGray = comps == 1;
							break;
						}
						final int length = (imageIn.readShort() & 0xFFFF);
						if (length <= 2) {
							throw e;
						}
						imageIn.skipBytes(length - 2);
					}
					imageIn.seek(0);
					try {
						final Metadata metadata = ImageMetadataReader.readMetadata(new ImageInputStreamProxy(imageIn));
						final Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
						if (directory != null && directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
							orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
						}
					} catch (final ImageProcessingException | MetadataException ex) {
						// ignore
					}
				}
			} else {
				width = image.getWidth();
				height = image.getHeight();
			}
			final boolean resize = (maxWidth > 0 && width > maxWidth) || (maxHeight > 0 && height > maxHeight);
			final double orgWidth = width;
			final double orgHeight = height;
			if (resize || imageType == PDFParams.ImageCompression.FLATE) {
				// Re-load image if we only had the reader
				if (ir != null) {
					imageIn.seek(0);
					image = G2DUtils.loadImage(ir, imageIn);
				}
				if (resize) {
					final var type = image.getType();
					if (maxWidth > 0 && width > maxWidth)
						width = maxWidth;
					if (maxHeight > 0 && height > maxHeight)
						height = maxHeight;

					final var scaled = image.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
					try {
						image.flush();
						image = switch (type) {
							case BufferedImage.TYPE_BYTE_BINARY, BufferedImage.TYPE_BYTE_INDEXED ->
								new BufferedImage(width, height, type, (IndexColorModel) image.getColorModel());
							case BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_4BYTE_ABGR,
									BufferedImage.TYPE_4BYTE_ABGR_PRE, BufferedImage.TYPE_INT_ARGB,
									BufferedImage.TYPE_INT_ARGB_PRE, BufferedImage.TYPE_INT_BGR,
									BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_USHORT_555_RGB,
									BufferedImage.TYPE_USHORT_565_RGB, BufferedImage.TYPE_BYTE_GRAY,
									BufferedImage.TYPE_USHORT_GRAY ->
								new BufferedImage(width, height, type);
							default ->
								new BufferedImage(width, height,
										image.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB
												: BufferedImage.TYPE_INT_RGB);
						};
						final var g2d = image.createGraphics();
						g2d.drawImage(scaled, 0, 0, null);
						g2d.dispose();
					} finally {
						scaled.flush();
					}
					imageType = PDFParams.ImageCompression.FLATE;
				}

				// Apply grayscale filter if requested
				if (colorMode == PDFParams.ColorMode.GRAY && image.getType() != BufferedImage.TYPE_BYTE_GRAY
						&& image.getType() != BufferedImage.TYPE_USHORT_GRAY) {
					final var raster = image.getRaster();
					final var cm = image.getColorModel();
					final var pixel = raster.getDataElements(0, 0, null);
					for (var y = 0; y < height; ++y) {
						for (var x = 0; x < width; ++x) {
							raster.getDataElements(x, y, pixel);
							final var r = cm.getRed(pixel) / 255.0f;
							final var g = cm.getGreen(pixel) / 255.0f;
							final var b = cm.getBlue(pixel) / 255.0f;
							final var gr = ColorUtils.toGray(r, g, b);
							final var octet = (int) (gr * 255.0f);
							image.setRGB(x, y, (cm.getAlpha(pixel) << 24) | (octet << 16) | (octet << 8) | octet);
						}
					}
				}
			}
			pdfImage = new PDFImage(name, orgWidth, orgHeight);
			try {
				final var imageRef = this.xref.nextObjectRef();
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
					// Output raw image data
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
							final Iterator<?> itr = ir.getImageTypes(0);
							final ImageTypeSpecifier its = (ImageTypeSpecifier) itr.next();
							final ColorModel cm = its.getColorModel();
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
							final byte[] buff = this.objectsFlow.getBuff();
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
					// Recompress
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
								final Iterator<?> i = ImageIO.getImageWritersByFormatName("jpeg");
								if (i == null || !i.hasNext()) {
									throw new IOException("JPEG output is not supported in this environment.");
								}
								iw = (ImageWriter) i.next();
								iwParams = iw.getDefaultWriteParam();
								iwParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
								iwParams.setCompressionQuality(.8f);
							}
								break;
							case JPEG2000:
								final Iterator<?> i = ImageIO.getImageWritersByFormatName("jpeg 2000");
								if (i == null || !i.hasNext()) {
									throw new IOException(
											"Java Advanced Imaging Image I/O Tools (JAI-ImageIO) is required to output JPEG2000.");
								}
								iw = (ImageWriter) i.next();
								iwParams = null;
								break;
							default:
								throw new IllegalStateException();
						}

						if (cm.hasAlpha()) {
							// Transparent GIF/PNG
							imageMaskRef = this.xref.nextObjectRef();
							if (softMaskSupport) {
								this.objectsFlow.writeName("SMask");
							} else {
								this.objectsFlow.writeName("Mask");
							}
							this.objectsFlow.writeObjectRef(imageMaskRef);
							this.objectsFlow.breakBefore();
						} else {
							// No mask
							imageMaskRef = null;
						}

						this.objectsFlow.writeName("BitsPerComponent");
						this.objectsFlow.writeInt(8);
						this.objectsFlow.breakBefore();

						final boolean deviceGray = (cm.getNumComponents() == 1);
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
							case JPEG, JPEG2000 -> {
								var ximage = image;
								if (cm.hasAlpha()) {
									ximage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
									final var g = ximage.createGraphics();
									g.drawImage(image, 0, 0, null);
									g.dispose();
								} else if (image.getType() == BufferedImage.TYPE_USHORT_GRAY) {
									ximage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
									final var g = ximage.createGraphics();
									g.drawImage(image, 0, 0, null);
									g.dispose();
								}
								try {
									final var iout = new FileCacheImageOutputStream(out, null);
									try {
										iw.setOutput(iout);
										iw.write(null, new IIOImage(ximage, null, null), iwParams);
									} finally {
										iout.close();
										iw.dispose();
									}
								} finally {
									if (ximage != image) {
										ximage.flush();
									}
								}
							}
							case FLATE -> {
								final var raster = image.getRaster();
								final var fastOut = new FastBufferedOutputStream(out, this.objectsFlow.getBuff());
								final var pixel = raster.getDataElements(0, 0, null);
								if (deviceGray) {
									for (var y = 0; y < height; ++y) {
										for (var x = 0; x < width; ++x) {
											fastOut.write(cm.getGreen(raster.getDataElements(x, y, pixel)));
										}
									}
								} else {
									for (var y = 0; y < height; ++y) {
										for (var x = 0; x < width; ++x) {
											raster.getDataElements(x, y, pixel);
											fastOut.write(cm.getRed(pixel));
											fastOut.write(cm.getGreen(pixel));
											fastOut.write(cm.getBlue(pixel));
										}
									}
								}
								fastOut.flush();
							}
							default -> throw new IllegalStateException();
						}
						out.close();
					} finally {
						this.objectsFlow.endObject();
					}

					if (imageMaskRef != null) {
						// Mask
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

							final Raster raster = image.getRaster();
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
		} catch (final IOException e) {
			LOG.log(Level.WARNING, "I/O error during image generation", e);
			throw e;
		} catch (final RuntimeException e) {
			LOG.log(Level.SEVERE, "Unexpected error during image generation", e);
			throw e;
		} finally {
			if (ir != null) {
				ir.dispose();
			}
		}
		++this.imageNumber;
		final var at = switch (orientation) {
			case 2 -> new AffineTransform(-1, 0, 0, 1, width, 0);
			case 3 -> AffineTransform.getRotateInstance(Math.PI, width / 2.0, height / 2.0);
			case 4 -> new AffineTransform(1, 0, 0, -1, 0, height);
			case 5 -> {
				final var res = AffineTransform.getRotateInstance(Math.PI / 2.0);
				res.scale(-1, 1);
				res.translate(0, -height);
				yield res;
			}
			case 6 -> {
				final var res = AffineTransform.getRotateInstance(Math.PI / 2.0);
				res.translate(0, -height);
				yield res;
			}
			case 7 -> {
				final var res = AffineTransform.getRotateInstance(-Math.PI / 2.0);
				res.scale(-1, 1);
				res.translate(-width, 0);
				yield res;
			}
			case 8 -> {
				final var res = AffineTransform.getRotateInstance(-Math.PI / 2.0);
				res.translate(-width, 0);
				yield res;
			}
			default -> null;
		};

		return (at == null) ? pdfImage : new TransformedImage(pdfImage, at);
	}
}
