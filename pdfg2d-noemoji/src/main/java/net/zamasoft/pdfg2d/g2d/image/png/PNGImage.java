/*
 * 
 * Copyright 2001,2003 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *  
 */
package net.zamasoft.pdfg2d.g2d.image.png;

import java.awt.Color;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferUShort;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Vector;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import net.zamasoft.pdfg2d.util.NumberUtils;

class PNGChunk {
	int length;

	int type;

	byte[] data;

	int crc;

	String typeString;

	public PNGChunk(int length, int type, byte[] data, int crc) {
		this.length = length;
		this.type = type;
		this.data = data;
		this.crc = crc;

		this.typeString = new String();
		this.typeString += (char) (type >> 24);
		this.typeString += (char) ((type >> 16) & 0xff);
		this.typeString += (char) ((type >> 8) & 0xff);
		this.typeString += (char) (type & 0xff);
	}

	public int getLength() {
		return this.length;
	}

	public int getType() {
		return this.type;
	}

	public String getTypeString() {
		return this.typeString;
	}

	public byte[] getData() {
		return this.data;
	}

	public byte getByte(int offset) {
		return this.data[offset];
	}

	public int getInt1(int offset) {
		return this.data[offset] & 0xff;
	}

	public int getInt2(int offset) {
		return ((this.data[offset] & 0xff) << 8) | (this.data[offset + 1] & 0xff);
	}

	public int getInt4(int offset) {
		return ((this.data[offset] & 0xff) << 24) | ((this.data[offset + 1] & 0xff) << 16)
				| ((this.data[offset + 2] & 0xff) << 8) | (this.data[offset + 3] & 0xff);
	}

	public String getString4(int offset) {
		String s = new String();
		s += (char) this.data[offset];
		s += (char) this.data[offset + 1];
		s += (char) this.data[offset + 2];
		s += (char) this.data[offset + 3];
		return s;
	}

	public boolean isType(String typeName) {
		return this.typeString.equals(typeName);
	}
}

/**
 * TO DO:
 * 
 * zTXt chunks
 * 
 */

public class PNGImage extends SimpleRenderedImage {

	public static final int PNG_COLOR_GRAY = 0;

	public static final int PNG_COLOR_RGB = 2;

	public static final int PNG_COLOR_PALETTE = 3;

	public static final int PNG_COLOR_GRAY_ALPHA = 4;

	public static final int PNG_COLOR_RGB_ALPHA = 6;

	private static final String[] colorTypeNames = { "Grayscale", "Error", "Truecolor", "Index", "Grayscale with alpha",
			"Error", "Truecolor with alpha" };

	public static final int PNG_FILTER_NONE = 0;

	public static final int PNG_FILTER_SUB = 1;

	public static final int PNG_FILTER_UP = 2;

	public static final int PNG_FILTER_AVERAGE = 3;

	public static final int PNG_FILTER_PAETH = 4;

	private int[][] bandOffsets = { null, { 0 }, // G
			{ 0, 1 }, // GA in GA order
			{ 0, 1, 2 }, // RGB in RGB order
			{ 0, 1, 2, 3 } // RGBA in RGBA order
	};

	private int bitDepth;

	private int colorType;

	private int compressionMethod;

	private int filterMethod;

	private int interlaceMethod;

	private int paletteEntries;

	private byte[] redPalette;

	private byte[] greenPalette;

	private byte[] bluePalette;

	private byte[] alphaPalette;

	private int bkgdRed;

	private int bkgdGreen;

	private int bkgdBlue;

	private int grayTransparentAlpha;

	private int redTransparentAlpha;

	private int greenTransparentAlpha;

	private int blueTransparentAlpha;

	private int maxOpacity;

	private int[] significantBits = null;

	// Parameter information

	// If true, the user wants destination alpha where applicable.
	private boolean suppressAlpha = false;

	// If true, perform palette lookup internally
	private boolean expandPalette = false;

	// If true, output < 8 bit gray images in 8 bit components format
	private boolean output8BitGray = false;

	// Create an alpha channel in the destination color model.
	private boolean outputHasAlphaPalette = false;

	// Perform gamma correction on the image
	private boolean performGammaCorrection = false;

	// Expand GA to GGGA for compatbility with Java2D
	private boolean expandGrayAlpha = false;

	private boolean emitProperties = true;

	private double fileGamma = 45455 / 100000.0F;

	private double userExponent = 1.0F;

	private double displayExponent = 2.2F;

	private double[] chromaticity = null;

	private int sRGBRenderingIntent = -1;

	// Post-processing step implied by above parameters
	private int postProcess = POST_NONE;

	// Possible post-processing steps

	// Do nothing
	private static final int POST_NONE = 0;

	// Gamma correct only
	private static final int POST_GAMMA = 1;

	// Push gray values through grayLut to expand to 8 bits
	private static final int POST_GRAY_LUT = 2;

	// Push gray values through grayLut to expand to 8 bits, add alpha
	private static final int POST_GRAY_LUT_ADD_TRANS = 3;

	// Push palette value through R,G,B lookup tables
	private static final int POST_PALETTE_TO_RGB = 4;

	// Push palette value through R,G,B,A lookup tables
	private static final int POST_PALETTE_TO_RGBA = 5;

	// Add transparency to a given gray value (w/ optional gamma)
	private static final int POST_ADD_GRAY_TRANS = 6;

	// Add transparency to a given RGB value (w/ optional gamma)
	private static final int POST_ADD_RGB_TRANS = 7;

	// Remove the alpha channel from a gray image (w/ optional gamma)
	private static final int POST_REMOVE_GRAY_TRANS = 8;

	// Remove the alpha channel from an RGB image (w/optional gamma)
	private static final int POST_REMOVE_RGB_TRANS = 9;

	// Mask to add expansion of GA -> GGGA
	private static final int POST_EXP_MASK = 16;

	// Expand gray to G/G/G
	private static final int POST_GRAY_ALPHA_EXP = POST_NONE | POST_EXP_MASK;

	// Expand gray to G/G/G through a gamma lut
	private static final int POST_GAMMA_EXP = POST_GAMMA | POST_EXP_MASK;

	// Push gray values through grayLut to expand to 8 bits, expand, add alpha
	private static final int POST_GRAY_LUT_ADD_TRANS_EXP = POST_GRAY_LUT_ADD_TRANS | POST_EXP_MASK;

	// Add transparency to a given gray value, expand
	private static final int POST_ADD_GRAY_TRANS_EXP = POST_ADD_GRAY_TRANS | POST_EXP_MASK;

	private Vector<ByteArrayInputStream> streamVec = new Vector<ByteArrayInputStream>();

	private DataInputStream dataStream;

	private int bytesPerPixel; // number of bytes per input pixel

	private int inputBands;

	private int outputBands;

	// Number of private chunks
	private int chunkIndex = 0;

	private Vector<String> textKeys = new Vector<String>();

	private Vector<String> textStrings = new Vector<String>();

	private Vector<String> ztextKeys = new Vector<String>();

	private Vector<String> ztextStrings = new Vector<String>();

	private WritableRaster theTile;

	private int[] gammaLut = null;

	private byte[] buff = new byte[8192];

	private void initGammaLut(int bits) {
		double exp = (double) this.userExponent / (this.fileGamma * this.displayExponent);
		int numSamples = 1 << bits;
		int maxOutSample = (bits == 16) ? 65535 : 255;

		this.gammaLut = new int[numSamples];
		for (int i = 0; i < numSamples; i++) {
			double gbright = (double) i / (numSamples - 1);
			double gamma = Math.pow(gbright, exp);
			int igamma = (int) (gamma * maxOutSample + 0.5);
			if (igamma > maxOutSample) {
				igamma = maxOutSample;
			}
			this.gammaLut[i] = igamma;
		}
	}

	private final byte[][] expandBits = { null, { (byte) 0x00, (byte) 0xff },
			{ (byte) 0x00, (byte) 0x55, (byte) 0xaa, (byte) 0xff }, null,
			{ (byte) 0x00, (byte) 0x11, (byte) 0x22, (byte) 0x33, (byte) 0x44, (byte) 0x55, (byte) 0x66, (byte) 0x77,
					(byte) 0x88, (byte) 0x99, (byte) 0xaa, (byte) 0xbb, (byte) 0xcc, (byte) 0xdd, (byte) 0xee,
					(byte) 0xff } };

	private int[] grayLut = null;

	private void initGrayLut(int bits) {
		int len = 1 << bits;
		this.grayLut = new int[len];

		if (this.performGammaCorrection) {
			for (int i = 0; i < len; i++) {
				this.grayLut[i] = this.gammaLut[i];
			}
		} else {
			for (int i = 0; i < len; i++) {
				this.grayLut[i] = this.expandBits[bits][i];
			}
		}
	}

	public PNGImage(InputStream stream, PNGDecodeParam decodeParam) {

		if (!stream.markSupported()) {
			stream = new BufferedInputStream(stream);
		}
		DataInputStream distream = new DataInputStream(stream);

		if (decodeParam == null) {
			decodeParam = new PNGDecodeParam();
		}
		// Get parameter values
		this.suppressAlpha = decodeParam.getSuppressAlpha();
		this.expandPalette = decodeParam.getExpandPalette();
		this.output8BitGray = decodeParam.getOutput8BitGray();
		this.expandGrayAlpha = decodeParam.getExpandGrayAlpha();
		if (decodeParam.getPerformGammaCorrection()) {
			this.userExponent = decodeParam.getUserExponent();
			this.displayExponent = decodeParam.getDisplayExponent();
			this.performGammaCorrection = true;
			this.output8BitGray = true;
		}

		if (this.emitProperties) {
			this.properties.put("file_type", "PNG v. 1.0");
		}

		try {
			long magic = distream.readLong();
			if (magic != 0x89504e470d0a1a0aL) {
				throw new RuntimeException();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		do {
			try {
				PNGChunk chunk;

				String chunkType = getChunkType(distream);
				if (chunkType.equals("IHDR")) {
					chunk = readChunk(distream);
					parse_IHDR_chunk(chunk);
				} else if (chunkType.equals("PLTE")) {
					chunk = readChunk(distream);
					parse_PLTE_chunk(chunk);
				} else if (chunkType.equals("IDAT")) {
					chunk = readChunk(distream);
					this.streamVec.add(new ByteArrayInputStream(chunk.getData()));
				} else if (chunkType.equals("IEND")) {
					chunk = readChunk(distream);
					parse_IEND_chunk(chunk);
					break; // fall through to the bottom
				} else if (chunkType.equals("bKGD")) {
					chunk = readChunk(distream);
					parse_bKGD_chunk(chunk);
				} else if (chunkType.equals("cHRM")) {
					chunk = readChunk(distream);
					parse_cHRM_chunk(chunk);
				} else if (chunkType.equals("gAMA")) {
					chunk = readChunk(distream);
					parse_gAMA_chunk(chunk);
				} else if (chunkType.equals("hIST")) {
					chunk = readChunk(distream);
					parse_hIST_chunk(chunk);
				} else if (chunkType.equals("iCCP")) {
					chunk = readChunk(distream);
					parse_iCCP_chunk(chunk);
				} else if (chunkType.equals("pHYs")) {
					chunk = readChunk(distream);
					parse_pHYs_chunk(chunk);
				} else if (chunkType.equals("sBIT")) {
					chunk = readChunk(distream);
					parse_sBIT_chunk(chunk);
				} else if (chunkType.equals("sRGB")) {
					chunk = readChunk(distream);
					parse_sRGB_chunk(chunk);
				} else if (chunkType.equals("tEXt")) {
					chunk = readChunk(distream);
					parse_tEXt_chunk(chunk);
				} else if (chunkType.equals("tIME")) {
					chunk = readChunk(distream);
					parse_tIME_chunk(chunk);
				} else if (chunkType.equals("tRNS")) {
					chunk = readChunk(distream);
					parse_tRNS_chunk(chunk);
				} else if (chunkType.equals("zTXt")) {
					chunk = readChunk(distream);
					parse_zTXt_chunk(chunk);
				} else {
					chunk = readChunk(distream);
					// Output the chunk data in raw form

					String type = chunk.getTypeString();
					byte[] data = chunk.getData();
					if (this.emitProperties) {
						String key = "chunk_" + this.chunkIndex++ + ":" + type;
						this.properties.put(key.toLowerCase(), data);
					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} while (true);

		// Final post-processing

		if (this.significantBits == null) {
			this.significantBits = new int[this.inputBands];
			for (int i = 0; i < this.inputBands; i++) {
				this.significantBits[i] = this.bitDepth;
			}

			if (this.emitProperties) {
				this.properties.put("significant_bits", this.significantBits);
			}
		}
	}

	private static String getChunkType(DataInputStream distream) throws IOException {
		distream.mark(8);
		/* int length = */distream.readInt();
		int type = distream.readInt();
		distream.reset();

		String typeString = new String();
		typeString += (char) (type >> 24);
		typeString += (char) ((type >> 16) & 0xff);
		typeString += (char) ((type >> 8) & 0xff);
		typeString += (char) (type & 0xff);
		return typeString;
	}

	private PNGChunk readChunk(DataInputStream distream) throws IOException {
		int length = distream.readInt();
		int type = distream.readInt();
		int remainder = length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int len;
		if (remainder > 0) {
			for (len = distream.read(this.buff, 0, Math.min(remainder, this.buff.length)); len != -1
					&& remainder > 0; len = distream.read(this.buff, 0, Math.min(remainder, this.buff.length))) {
				out.write(this.buff, 0, len);
				remainder -= length;
			}
		} else {
			len = 0;
		}
		byte[] data = out.toByteArray();
		int crc;
		if (len != -1) {
			try {
				crc = distream.readInt();
			} catch (EOFException e) {
				crc = 0;
			}
		} else {
			crc = 0;
		}
		return new PNGChunk(data.length, type, data, crc);
	}

	private void parse_IHDR_chunk(PNGChunk chunk) {
		this.tileWidth = this.width = chunk.getInt4(0);
		this.tileHeight = this.height = chunk.getInt4(4);

		this.bitDepth = chunk.getInt1(8);

		if ((this.bitDepth != 1) && (this.bitDepth != 2) && (this.bitDepth != 4) && (this.bitDepth != 8)
				&& (this.bitDepth != 16)) {
			// Error -- bad bit depth
			throw new RuntimeException();
		}
		this.maxOpacity = (1 << this.bitDepth) - 1;

		this.colorType = chunk.getInt1(9);

		if ((this.colorType == PNG_COLOR_RGB) && (this.bitDepth < 8)) {
			// Error -- RGB images must have 8 or 16 bits
			throw new RuntimeException();
		}

		if ((this.colorType == PNG_COLOR_PALETTE) && (this.bitDepth == 16)) {
			// Error -- palette images must have < 16 bits
			throw new RuntimeException();
		}

		if ((this.colorType == PNG_COLOR_GRAY_ALPHA) && (this.bitDepth < 8)) {
			// Error -- gray/alpha images must have >= 8 bits
			throw new RuntimeException();
		}

		if ((this.colorType == PNG_COLOR_RGB_ALPHA) && (this.bitDepth < 8)) {
			// Error -- RGB/alpha images must have >= 8 bits
			throw new RuntimeException();
		}

		if (this.emitProperties) {
			this.properties.put("color_type", colorTypeNames[this.colorType]);
		}

		if (this.emitProperties) {
			this.properties.put("bit_depth", NumberUtils.intValue(this.bitDepth));
		}

		if (this.performGammaCorrection) {
			// Assume file gamma is 1/2.2 unless we get a gAMA chunk
			double gamma = (1.0F / 2.2F) * (this.displayExponent / this.userExponent);
			if (this.emitProperties) {
				this.properties.put("gamma", new Float(gamma));
			}
		}

		this.compressionMethod = chunk.getInt1(10);
		if (this.compressionMethod != 0) {
			// Error -- only know about compression method 0
			throw new RuntimeException();
		}

		this.filterMethod = chunk.getInt1(11);
		if (this.filterMethod != 0) {
			// Error -- only know about filter method 0
			throw new RuntimeException();
		}

		this.interlaceMethod = chunk.getInt1(12);
		if (this.interlaceMethod == 0) {
			if (this.emitProperties) {
				this.properties.put("interlace_method", "None");
			}
		} else if (this.interlaceMethod == 1) {
			if (this.emitProperties) {
				this.properties.put("interlace_method", "Adam7");
			}
		} else {
			// Error -- only know about Adam7 interlacing
			throw new RuntimeException();
		}

		this.bytesPerPixel = (this.bitDepth == 16) ? 2 : 1;

		switch (this.colorType) {
		case PNG_COLOR_GRAY:
			this.inputBands = 1;
			this.outputBands = 1;

			if (this.output8BitGray && (this.bitDepth < 8)) {
				this.postProcess = POST_GRAY_LUT;
			} else if (this.performGammaCorrection) {
				this.postProcess = POST_GAMMA;
			} else {
				this.postProcess = POST_NONE;
			}
			break;

		case PNG_COLOR_RGB:
			this.inputBands = 3;
			this.bytesPerPixel *= 3;
			this.outputBands = 3;

			if (this.performGammaCorrection) {
				this.postProcess = POST_GAMMA;
			} else {
				this.postProcess = POST_NONE;
			}
			break;

		case PNG_COLOR_PALETTE:
			this.inputBands = 1;
			this.bytesPerPixel = 1;
			this.outputBands = this.expandPalette ? 3 : 1;

			if (this.expandPalette) {
				this.postProcess = POST_PALETTE_TO_RGB;
			} else {
				this.postProcess = POST_NONE;
			}
			break;

		case PNG_COLOR_GRAY_ALPHA:
			this.inputBands = 2;
			this.bytesPerPixel *= 2;

			if (this.suppressAlpha) {
				this.outputBands = 1;
				this.postProcess = POST_REMOVE_GRAY_TRANS;
			} else {
				if (this.performGammaCorrection) {
					this.postProcess = POST_GAMMA;
				} else {
					this.postProcess = POST_NONE;
				}
				if (this.expandGrayAlpha) {
					this.postProcess |= POST_EXP_MASK;
					this.outputBands = 4;
				} else {
					this.outputBands = 2;
				}
			}
			break;

		case PNG_COLOR_RGB_ALPHA:
			this.inputBands = 4;
			this.bytesPerPixel *= 4;
			this.outputBands = (!this.suppressAlpha) ? 4 : 3;

			if (this.suppressAlpha) {
				this.postProcess = POST_REMOVE_RGB_TRANS;
			} else if (this.performGammaCorrection) {
				this.postProcess = POST_GAMMA;
			} else {
				this.postProcess = POST_NONE;
			}
			break;
		}
	}

	private void parse_IEND_chunk(PNGChunk chunk) throws Exception {
		// Store text strings
		int textLen = this.textKeys.size();
		String[] textArray = new String[2 * textLen];
		for (int i = 0; i < textLen; i++) {
			String key = (String) this.textKeys.elementAt(i);
			String val = (String) this.textStrings.elementAt(i);
			textArray[2 * i] = key;
			textArray[2 * i + 1] = val;
			if (this.emitProperties) {
				String uniqueKey = "text_" + i + ":" + key;
				this.properties.put(uniqueKey.toLowerCase(), val);
			}
		}

		// Store compressed text strings
		int ztextLen = this.ztextKeys.size();
		String[] ztextArray = new String[2 * ztextLen];
		for (int i = 0; i < ztextLen; i++) {
			String key = (String) this.ztextKeys.elementAt(i);
			String val = (String) this.ztextStrings.elementAt(i);
			ztextArray[2 * i] = key;
			ztextArray[2 * i + 1] = val;
			if (this.emitProperties) {
				String uniqueKey = "ztext_" + i + ":" + key;
				this.properties.put(uniqueKey.toLowerCase(), val);
			}
		}

		// Parse prior IDAT chunks
		InputStream seqStream = new SequenceInputStream(this.streamVec.elements());
		InputStream infStream = new InflaterInputStream(seqStream, new Inflater());
		this.dataStream = new DataInputStream(infStream);

		// Create an empty WritableRaster
		int depth = this.bitDepth;
		if ((this.colorType == PNG_COLOR_GRAY) && (this.bitDepth < 8) && this.output8BitGray) {
			depth = 8;
		}
		if ((this.colorType == PNG_COLOR_PALETTE) && this.expandPalette) {
			depth = 8;
		}
		int bytesPerRow = (this.outputBands * this.width * depth + 7) / 8;
		int scanlineStride = (depth == 16) ? (bytesPerRow / 2) : bytesPerRow;

		this.theTile = createRaster(this.width, this.height, this.outputBands, scanlineStride, depth);

		if (this.performGammaCorrection && (this.gammaLut == null)) {
			initGammaLut(this.bitDepth);
		}
		if ((this.postProcess == POST_GRAY_LUT) || (this.postProcess == POST_GRAY_LUT_ADD_TRANS)
				|| (this.postProcess == POST_GRAY_LUT_ADD_TRANS_EXP)) {
			initGrayLut(this.bitDepth);
		}

		decodeImage(this.interlaceMethod == 1);
		this.sampleModel = this.theTile.getSampleModel();

		if ((this.colorType == PNG_COLOR_PALETTE) && !this.expandPalette) {
			if (this.outputHasAlphaPalette) {
				this.colorModel = new IndexColorModel(this.bitDepth, this.paletteEntries, this.redPalette,
						this.greenPalette, this.bluePalette, this.alphaPalette);
			} else {
				this.colorModel = new IndexColorModel(this.bitDepth, this.paletteEntries, this.redPalette,
						this.greenPalette, this.bluePalette);
			}
		} else if ((this.colorType == PNG_COLOR_GRAY) && (this.bitDepth < 8) && !this.output8BitGray) {
			byte[] palette = this.expandBits[this.bitDepth];
			this.colorModel = new IndexColorModel(this.bitDepth, palette.length, palette, palette, palette);
		} else {
			this.colorModel = createComponentColorModel(this.sampleModel);
		}
	}

	private static final int[] GrayBits8 = { 8 };

	private static final ComponentColorModel colorModelGray8 = new ComponentColorModel(
			ColorSpace.getInstance(ColorSpace.CS_GRAY), GrayBits8, false, false, Transparency.OPAQUE,
			DataBuffer.TYPE_BYTE);

	private static final int[] GrayAlphaBits8 = { 8, 8 };

	private static final ComponentColorModel colorModelGrayAlpha8 = new ComponentColorModel(
			ColorSpace.getInstance(ColorSpace.CS_GRAY), GrayAlphaBits8, true, false, Transparency.TRANSLUCENT,
			DataBuffer.TYPE_BYTE);

	private static final int[] GrayBits16 = { 16 };

	private static final ComponentColorModel colorModelGray16 = new ComponentColorModel(
			ColorSpace.getInstance(ColorSpace.CS_GRAY), GrayBits16, false, false, Transparency.OPAQUE,
			DataBuffer.TYPE_USHORT);

	private static final int[] GrayAlphaBits16 = { 16, 16 };

	private static final ComponentColorModel colorModelGrayAlpha16 = new ComponentColorModel(
			ColorSpace.getInstance(ColorSpace.CS_GRAY), GrayAlphaBits16, true, false, Transparency.TRANSLUCENT,
			DataBuffer.TYPE_USHORT);

	private static final int[] GrayBits32 = { 32 };

	private static final ComponentColorModel colorModelGray32 = new ComponentColorModel(
			ColorSpace.getInstance(ColorSpace.CS_GRAY), GrayBits32, false, false, Transparency.OPAQUE,
			DataBuffer.TYPE_INT);

	private static final int[] GrayAlphaBits32 = { 32, 32 };

	private static final ComponentColorModel colorModelGrayAlpha32 = new ComponentColorModel(
			ColorSpace.getInstance(ColorSpace.CS_GRAY), GrayAlphaBits32, true, false, Transparency.TRANSLUCENT,
			DataBuffer.TYPE_INT);

	private static final int[] RGBBits8 = { 8, 8, 8 };

	private static final ComponentColorModel colorModelRGB8 = new ComponentColorModel(
			ColorSpace.getInstance(ColorSpace.CS_sRGB), RGBBits8, false, false, Transparency.OPAQUE,
			DataBuffer.TYPE_BYTE);

	private static final int[] RGBABits8 = { 8, 8, 8, 8 };

	private static final ComponentColorModel colorModelRGBA8 = new ComponentColorModel(
			ColorSpace.getInstance(ColorSpace.CS_sRGB), RGBABits8, true, false, Transparency.TRANSLUCENT,
			DataBuffer.TYPE_BYTE);

	private static final int[] RGBBits16 = { 16, 16, 16 };

	private static final ComponentColorModel colorModelRGB16 = new ComponentColorModel(
			ColorSpace.getInstance(ColorSpace.CS_sRGB), RGBBits16, false, false, Transparency.OPAQUE,
			DataBuffer.TYPE_USHORT);

	private static final int[] RGBABits16 = { 16, 16, 16, 16 };

	private static final ComponentColorModel colorModelRGBA16 = new ComponentColorModel(
			ColorSpace.getInstance(ColorSpace.CS_sRGB), RGBABits16, true, false, Transparency.TRANSLUCENT,
			DataBuffer.TYPE_USHORT);

	private static final int[] RGBBits32 = { 32, 32, 32 };

	private static final ComponentColorModel colorModelRGB32 = new ComponentColorModel(
			ColorSpace.getInstance(ColorSpace.CS_sRGB), RGBBits32, false, false, Transparency.OPAQUE,
			DataBuffer.TYPE_INT);

	private static final int[] RGBABits32 = { 32, 32, 32, 32 };

	private static final ComponentColorModel colorModelRGBA32 = new ComponentColorModel(
			ColorSpace.getInstance(ColorSpace.CS_sRGB), RGBABits32, true, false, Transparency.TRANSLUCENT,
			DataBuffer.TYPE_INT);

	/**
	 * A convenience method to create an instance of
	 * <code>ComponentColorModel</code> suitable for use with the given
	 * <code>SampleModel</code>. The <code>SampleModel</code> should have a data
	 * type of <code>DataBuffer.TYPE_BYTE</code>, <code>TYPE_USHORT</code>, or
	 * <code>TYPE_INT</code> and between 1 and 4 bands. Depending on the number of
	 * bands of the <code>SampleModel</code>, either a gray, gray+alpha, rgb, or
	 * rgb+alpha <code>ColorModel</code> is returned.
	 */
	public static ColorModel createComponentColorModel(SampleModel sm) {
		int type = sm.getDataType();
		int bands = sm.getNumBands();
		ComponentColorModel cm = null;

		if (type == DataBuffer.TYPE_BYTE) {
			switch (bands) {
			case 1:
				cm = colorModelGray8;
				break;
			case 2:
				cm = colorModelGrayAlpha8;
				break;
			case 3:
				cm = colorModelRGB8;
				break;
			case 4:
				cm = colorModelRGBA8;
				break;
			}
		} else if (type == DataBuffer.TYPE_USHORT) {
			switch (bands) {
			case 1:
				cm = colorModelGray16;
				break;
			case 2:
				cm = colorModelGrayAlpha16;
				break;
			case 3:
				cm = colorModelRGB16;
				break;
			case 4:
				cm = colorModelRGBA16;
				break;
			}
		} else if (type == DataBuffer.TYPE_INT) {
			switch (bands) {
			case 1:
				cm = colorModelGray32;
				break;
			case 2:
				cm = colorModelGrayAlpha32;
				break;
			case 3:
				cm = colorModelRGB32;
				break;
			case 4:
				cm = colorModelRGBA32;
				break;
			}
		}

		return cm;
	}

	private void parse_PLTE_chunk(PNGChunk chunk) {
		this.paletteEntries = chunk.getLength() / 3;
		this.redPalette = new byte[this.paletteEntries];
		this.greenPalette = new byte[this.paletteEntries];
		this.bluePalette = new byte[this.paletteEntries];

		int pltIndex = 0;

		// gAMA chunk must precede PLTE chunk
		if (this.performGammaCorrection) {
			if (this.gammaLut == null) {
				initGammaLut(this.bitDepth == 16 ? 16 : 8);
			}

			for (int i = 0; i < this.paletteEntries; i++) {
				byte r = chunk.getByte(pltIndex++);
				byte g = chunk.getByte(pltIndex++);
				byte b = chunk.getByte(pltIndex++);

				this.redPalette[i] = (byte) this.gammaLut[r & 0xff];
				this.greenPalette[i] = (byte) this.gammaLut[g & 0xff];
				this.bluePalette[i] = (byte) this.gammaLut[b & 0xff];
			}
		} else {
			for (int i = 0; i < this.paletteEntries; i++) {
				this.redPalette[i] = chunk.getByte(pltIndex++);
				this.greenPalette[i] = chunk.getByte(pltIndex++);
				this.bluePalette[i] = chunk.getByte(pltIndex++);
			}
		}
	}

	private void parse_bKGD_chunk(PNGChunk chunk) {
		switch (this.colorType) {
		case PNG_COLOR_PALETTE:
			int bkgdIndex = chunk.getByte(0) & 0xff;

			this.bkgdRed = this.redPalette[bkgdIndex] & 0xff;
			this.bkgdGreen = this.greenPalette[bkgdIndex] & 0xff;
			this.bkgdBlue = this.bluePalette[bkgdIndex] & 0xff;

			break;
		case PNG_COLOR_GRAY:
		case PNG_COLOR_GRAY_ALPHA:
			int bkgdGray = chunk.getInt2(0);
			this.bkgdRed = this.bkgdGreen = this.bkgdBlue = bkgdGray;

			break;
		case PNG_COLOR_RGB:
		case PNG_COLOR_RGB_ALPHA:
			this.bkgdRed = chunk.getInt2(0);
			this.bkgdGreen = chunk.getInt2(2);
			this.bkgdBlue = chunk.getInt2(4);

			int[] bkgdRGB = new int[3];
			bkgdRGB[0] = this.bkgdRed;
			bkgdRGB[1] = this.bkgdGreen;
			bkgdRGB[2] = this.bkgdBlue;
			break;
		}

		int r = 0, g = 0, b = 0;
		if (this.bitDepth < 8) {
			r = this.expandBits[this.bitDepth][this.bkgdRed];
			g = this.expandBits[this.bitDepth][this.bkgdGreen];
			b = this.expandBits[this.bitDepth][this.bkgdBlue];
		} else if (this.bitDepth == 8) {
			r = this.bkgdRed;
			g = this.bkgdGreen;
			b = this.bkgdBlue;
		} else if (this.bitDepth == 16) {
			r = this.bkgdRed >> 8;
			g = this.bkgdGreen >> 8;
			b = this.bkgdBlue >> 8;
		}
		if (this.emitProperties) {
			this.properties.put("background_color", new Color(r, g, b));
		}
	}

	private void parse_cHRM_chunk(PNGChunk chunk) {
		// If an sRGB chunk exists, ignore cHRM chunks
		if (this.sRGBRenderingIntent != -1) {
			return;
		}

		this.chromaticity = new double[8];
		this.chromaticity[0] = chunk.getInt4(0) / 100000.0F;
		this.chromaticity[1] = chunk.getInt4(4) / 100000.0F;
		this.chromaticity[2] = chunk.getInt4(8) / 100000.0F;
		this.chromaticity[3] = chunk.getInt4(12) / 100000.0F;
		this.chromaticity[4] = chunk.getInt4(16) / 100000.0F;
		this.chromaticity[5] = chunk.getInt4(20) / 100000.0F;
		this.chromaticity[6] = chunk.getInt4(24) / 100000.0F;
		this.chromaticity[7] = chunk.getInt4(28) / 100000.0F;

		if (this.emitProperties) {
			this.properties.put("white_point_x", new Float(this.chromaticity[0]));
			this.properties.put("white_point_y", new Float(this.chromaticity[1]));
			this.properties.put("red_x", new Float(this.chromaticity[2]));
			this.properties.put("red_y", new Float(this.chromaticity[3]));
			this.properties.put("green_x", new Float(this.chromaticity[4]));
			this.properties.put("green_y", new Float(this.chromaticity[5]));
			this.properties.put("blue_x", new Float(this.chromaticity[6]));
			this.properties.put("blue_y", new Float(this.chromaticity[7]));
		}
	}

	private void parse_gAMA_chunk(PNGChunk chunk) {
		// If an sRGB chunk exists, ignore gAMA chunks
		if (this.sRGBRenderingIntent != -1) {
			return;
		}

		this.fileGamma = chunk.getInt4(0) / 100000.0F;

		double exp = this.performGammaCorrection ? this.displayExponent / this.userExponent : 1.0F;
		if (this.emitProperties) {
			this.properties.put("gamma", new Float(this.fileGamma * exp));
		}
	}

	private void parse_hIST_chunk(PNGChunk chunk) {
		if (this.redPalette == null) {
			throw new RuntimeException();
		}

		int length = this.redPalette.length;
		int[] hist = new int[length];
		for (int i = 0; i < length; i++) {
			hist[i] = chunk.getInt2(2 * i);
		}
	}

	private void parse_iCCP_chunk(PNGChunk chunk) {
		// String name = new String();
		// byte b;

		int textIndex = 0;
		while ((/* b = */chunk.getByte(textIndex++)) != 0) {
			// name += (char) b;
		}
	}

	private void parse_pHYs_chunk(PNGChunk chunk) {
		int xPixelsPerUnit = chunk.getInt4(0);
		int yPixelsPerUnit = chunk.getInt4(4);
		int unitSpecifier = chunk.getInt1(8);

		if (this.emitProperties) {
			this.properties.put("x_pixels_per_unit", NumberUtils.intValue(xPixelsPerUnit));
			this.properties.put("y_pixels_per_unit", NumberUtils.intValue(yPixelsPerUnit));
			this.properties.put("pixel_aspect_ratio", new Float((double) xPixelsPerUnit / yPixelsPerUnit));
			if (unitSpecifier == 1) {
				this.properties.put("pixel_units", "Meters");
			} else if (unitSpecifier != 0) {
				// Error -- unit specifier must be 0 or 1
				throw new RuntimeException();
			}
		}
	}

	private void parse_sBIT_chunk(PNGChunk chunk) {
		if (this.colorType == PNG_COLOR_PALETTE) {
			this.significantBits = new int[3];
		} else {
			this.significantBits = new int[this.inputBands];
		}
		for (int i = 0; i < this.significantBits.length; i++) {
			int bits = chunk.getByte(i);
			int depth = (this.colorType == PNG_COLOR_PALETTE) ? 8 : this.bitDepth;
			if (bits <= 0 || bits > depth) {
				// Error -- significant bits must be between 0 and
				// image bit depth.
				throw new RuntimeException();
			}
			this.significantBits[i] = bits;
		}

		if (this.emitProperties) {
			this.properties.put("significant_bits", this.significantBits);
		}
	}

	private void parse_sRGB_chunk(PNGChunk chunk) {
		this.sRGBRenderingIntent = chunk.getByte(0);

		// The presence of an sRGB chunk implies particular
		// settings for gamma and chroma.
		this.fileGamma = 45455 / 100000.0F;

		this.chromaticity = new double[8];
		this.chromaticity[0] = 31270 / 10000.0F;
		this.chromaticity[1] = 32900 / 10000.0F;
		this.chromaticity[2] = 64000 / 10000.0F;
		this.chromaticity[3] = 33000 / 10000.0F;
		this.chromaticity[4] = 30000 / 10000.0F;
		this.chromaticity[5] = 60000 / 10000.0F;
		this.chromaticity[6] = 15000 / 10000.0F;
		this.chromaticity[7] = 6000 / 10000.0F;

		if (this.performGammaCorrection) {
			// File gamma is 1/2.2
			double gamma = this.fileGamma * (this.displayExponent / this.userExponent);
			if (this.emitProperties) {
				this.properties.put("gamma", new Float(gamma));
				this.properties.put("white_point_x", new Float(this.chromaticity[0]));
				this.properties.put("white_point_y", new Float(this.chromaticity[1]));
				this.properties.put("red_x", new Float(this.chromaticity[2]));
				this.properties.put("red_y", new Float(this.chromaticity[3]));
				this.properties.put("green_x", new Float(this.chromaticity[4]));
				this.properties.put("green_y", new Float(this.chromaticity[5]));
				this.properties.put("blue_x", new Float(this.chromaticity[6]));
				this.properties.put("blue_y", new Float(this.chromaticity[7]));
			}
		}
	}

	private void parse_tEXt_chunk(PNGChunk chunk) {
		String key = new String();
		String value = new String();
		byte b;

		int textIndex = 0;
		while ((b = chunk.getByte(textIndex++)) != 0) {
			key += (char) b;
		}

		for (int i = textIndex; i < chunk.getLength(); i++) {
			value += (char) chunk.getByte(i);
		}

		this.textKeys.add(key);
		this.textStrings.add(value);
	}

	private void parse_tIME_chunk(PNGChunk chunk) {
		int year = chunk.getInt2(0);
		int month = chunk.getInt1(2) - 1;
		int day = chunk.getInt1(3);
		int hour = chunk.getInt1(4);
		int minute = chunk.getInt1(5);
		int second = chunk.getInt1(6);

		TimeZone gmt = TimeZone.getTimeZone("GMT");

		GregorianCalendar cal = new GregorianCalendar(gmt);
		cal.set(year, month, day, hour, minute, second);
		Date date = cal.getTime();

		if (this.emitProperties) {
			this.properties.put("timestamp", date);
		}
	}

	private void parse_tRNS_chunk(PNGChunk chunk) {
		if (this.colorType == PNG_COLOR_PALETTE) {
			int entries = chunk.getLength();
			if (entries > this.paletteEntries) {
				// Error -- mustn't have more alpha than RGB palette entries
				throw new RuntimeException();
			}

			// Load beginning of palette from the chunk
			this.alphaPalette = new byte[this.paletteEntries];
			for (int i = 0; i < entries; i++) {
				this.alphaPalette[i] = chunk.getByte(i);
			}

			// Fill rest of palette with 255
			for (int i = entries; i < this.paletteEntries; i++) {
				this.alphaPalette[i] = (byte) 255;
			}

			if (!this.suppressAlpha) {
				if (this.expandPalette) {
					this.postProcess = POST_PALETTE_TO_RGBA;
					this.outputBands = 4;
				} else {
					this.outputHasAlphaPalette = true;
				}
			}
		} else if (this.colorType == PNG_COLOR_GRAY) {
			this.grayTransparentAlpha = chunk.getInt2(0);

			if (!this.suppressAlpha) {
				if (this.bitDepth < 8) {
					this.output8BitGray = true;
					this.maxOpacity = 255;
					this.postProcess = POST_GRAY_LUT_ADD_TRANS;
				} else {
					this.postProcess = POST_ADD_GRAY_TRANS;
				}

				if (this.expandGrayAlpha) {
					this.outputBands = 4;
					this.postProcess |= POST_EXP_MASK;
				} else {
					this.outputBands = 2;
				}
			}
		} else if (this.colorType == PNG_COLOR_RGB) {
			this.redTransparentAlpha = chunk.getInt2(0);
			this.greenTransparentAlpha = chunk.getInt2(2);
			this.blueTransparentAlpha = chunk.getInt2(4);

			if (!this.suppressAlpha) {
				this.outputBands = 4;
				this.postProcess = POST_ADD_RGB_TRANS;
			}
		} else if (this.colorType == PNG_COLOR_GRAY_ALPHA || this.colorType == PNG_COLOR_RGB_ALPHA) {
			// Error -- GA or RGBA image can't have a tRNS chunk.
			throw new RuntimeException();
		}
	}

	private void parse_zTXt_chunk(PNGChunk chunk) throws IOException {
		String key = new String();
		String value = new String();
		byte b;

		int textIndex = 0;
		while ((b = chunk.getByte(textIndex++)) != 0) {
			key += (char) b;
		}
		/* int method = */chunk.getByte(textIndex++);

		int length = chunk.getLength() - textIndex;
		byte[] data = chunk.getData();
		InputStream cis = new ByteArrayInputStream(data, textIndex, length);
		InputStream iis = new InflaterInputStream(cis);

		int c;
		while ((c = iis.read()) != -1) {
			value += (char) c;
		}

		this.ztextKeys.add(key);
		this.ztextStrings.add(value);
	}

	private WritableRaster createRaster(int width, int height, int bands, int scanlineStride, int bitDepth) {

		DataBuffer dataBuffer;
		WritableRaster ras = null;
		Point origin = new Point(0, 0);
		if ((bitDepth < 8) && (bands == 1)) {
			dataBuffer = new DataBufferByte(height * scanlineStride);
			ras = Raster.createPackedRaster(dataBuffer, width, height, bitDepth, origin);
		} else if (bitDepth <= 8) {
			dataBuffer = new DataBufferByte(height * scanlineStride);
			ras = Raster.createInterleavedRaster(dataBuffer, width, height, scanlineStride, bands,
					this.bandOffsets[bands], origin);
		} else {
			dataBuffer = new DataBufferUShort(height * scanlineStride);
			ras = Raster.createInterleavedRaster(dataBuffer, width, height, scanlineStride, bands,
					this.bandOffsets[bands], origin);
		}

		return ras;
	}

	// Data filtering methods

	private static void decodeSubFilter(byte[] curr, int count, int bpp) {
		for (int i = bpp; i < count; i++) {
			int val;

			val = curr[i] & 0xff;
			val += curr[i - bpp] & 0xff;

			curr[i] = (byte) val;
		}
	}

	private static void decodeUpFilter(byte[] curr, byte[] prev, int count) {
		for (int i = 0; i < count; i++) {
			int raw = curr[i] & 0xff;
			int prior = prev[i] & 0xff;

			curr[i] = (byte) (raw + prior);
		}
	}

	private static void decodeAverageFilter(byte[] curr, byte[] prev, int count, int bpp) {
		int raw, priorPixel, priorRow;

		for (int i = 0; i < bpp; i++) {
			raw = curr[i] & 0xff;
			priorRow = prev[i] & 0xff;

			curr[i] = (byte) (raw + priorRow / 2);
		}

		for (int i = bpp; i < count; i++) {
			raw = curr[i] & 0xff;
			priorPixel = curr[i - bpp] & 0xff;
			priorRow = prev[i] & 0xff;

			curr[i] = (byte) (raw + (priorPixel + priorRow) / 2);
		}
	}

	private static int paethPredictor(int a, int b, int c) {
		int p = a + b - c;
		int pa = Math.abs(p - a);
		int pb = Math.abs(p - b);
		int pc = Math.abs(p - c);

		if ((pa <= pb) && (pa <= pc)) {
			return a;
		} else if (pb <= pc) {
			return b;
		} else {
			return c;
		}
	}

	private static void decodePaethFilter(byte[] curr, byte[] prev, int count, int bpp) {
		int raw, priorPixel, priorRow, priorRowPixel;

		for (int i = 0; i < bpp; i++) {
			raw = curr[i] & 0xff;
			priorRow = prev[i] & 0xff;

			curr[i] = (byte) (raw + priorRow);
		}

		for (int i = bpp; i < count; i++) {
			raw = curr[i] & 0xff;
			priorPixel = curr[i - bpp] & 0xff;
			priorRow = prev[i] & 0xff;
			priorRowPixel = prev[i - bpp] & 0xff;

			curr[i] = (byte) (raw + paethPredictor(priorPixel, priorRow, priorRowPixel));
		}
	}

	private void processPixels(int process, Raster src, WritableRaster dst, int xOffset, int step, int y, int width) {
		int srcX, dstX;

		// Create an array suitable for holding one pixel
		int[] ps = src.getPixel(0, 0, (int[]) null);
		int[] pd = dst.getPixel(0, 0, (int[]) null);

		dstX = xOffset;
		switch (process) {
		case POST_NONE:
			for (srcX = 0; srcX < width; srcX++) {
				src.getPixel(srcX, 0, ps);
				dst.setPixel(dstX, y, ps);
				dstX += step;
			}
			break;

		case POST_GAMMA:
			for (srcX = 0; srcX < width; srcX++) {
				src.getPixel(srcX, 0, ps);

				for (int i = 0; i < this.inputBands; i++) {
					int x = ps[i];
					ps[i] = this.gammaLut[x];
				}

				dst.setPixel(dstX, y, ps);
				dstX += step;
			}
			break;

		case POST_GRAY_LUT:
			for (srcX = 0; srcX < width; srcX++) {
				src.getPixel(srcX, 0, ps);

				pd[0] = this.grayLut[ps[0]];

				dst.setPixel(dstX, y, pd);
				dstX += step;
			}
			break;

		case POST_GRAY_LUT_ADD_TRANS:
			for (srcX = 0; srcX < width; srcX++) {
				src.getPixel(srcX, 0, ps);

				int val = ps[0];
				pd[0] = this.grayLut[val];
				if (val == this.grayTransparentAlpha) {
					pd[1] = 0;
				} else {
					pd[1] = this.maxOpacity;
				}

				dst.setPixel(dstX, y, pd);
				dstX += step;
			}
			break;

		case POST_PALETTE_TO_RGB:
			for (srcX = 0; srcX < width; srcX++) {
				src.getPixel(srcX, 0, ps);

				int val = ps[0];
				pd[0] = this.redPalette[val];
				pd[1] = this.greenPalette[val];
				pd[2] = this.bluePalette[val];

				dst.setPixel(dstX, y, pd);
				dstX += step;
			}
			break;

		case POST_PALETTE_TO_RGBA:
			for (srcX = 0; srcX < width; srcX++) {
				src.getPixel(srcX, 0, ps);

				int val = ps[0];
				pd[0] = this.redPalette[val];
				pd[1] = this.greenPalette[val];
				pd[2] = this.bluePalette[val];
				pd[3] = this.alphaPalette[val];

				dst.setPixel(dstX, y, pd);
				dstX += step;
			}
			break;

		case POST_ADD_GRAY_TRANS:
			for (srcX = 0; srcX < width; srcX++) {
				src.getPixel(srcX, 0, ps);

				int val = ps[0];
				if (this.performGammaCorrection) {
					val = this.gammaLut[val];
				}
				pd[0] = val;
				if (val == this.grayTransparentAlpha) {
					pd[1] = 0;
				} else {
					pd[1] = this.maxOpacity;
				}

				dst.setPixel(dstX, y, pd);
				dstX += step;
			}
			break;

		case POST_ADD_RGB_TRANS:
			for (srcX = 0; srcX < width; srcX++) {
				src.getPixel(srcX, 0, ps);

				int r = ps[0];
				int g = ps[1];
				int b = ps[2];
				if (this.performGammaCorrection) {
					pd[0] = this.gammaLut[r];
					pd[1] = this.gammaLut[g];
					pd[2] = this.gammaLut[b];
				} else {
					pd[0] = r;
					pd[1] = g;
					pd[2] = b;
				}
				if ((r == this.redTransparentAlpha) && (g == this.greenTransparentAlpha)
						&& (b == this.blueTransparentAlpha)) {
					pd[3] = 0;
				} else {
					pd[3] = this.maxOpacity;
				}

				dst.setPixel(dstX, y, pd);
				dstX += step;
			}
			break;

		case POST_REMOVE_GRAY_TRANS:
			for (srcX = 0; srcX < width; srcX++) {
				src.getPixel(srcX, 0, ps);

				int g = ps[0];
				if (this.performGammaCorrection) {
					pd[0] = this.gammaLut[g];
				} else {
					pd[0] = g;
				}

				dst.setPixel(dstX, y, pd);
				dstX += step;
			}
			break;

		case POST_REMOVE_RGB_TRANS:
			for (srcX = 0; srcX < width; srcX++) {
				src.getPixel(srcX, 0, ps);

				int r = ps[0];
				int g = ps[1];
				int b = ps[2];
				if (this.performGammaCorrection) {
					pd[0] = this.gammaLut[r];
					pd[1] = this.gammaLut[g];
					pd[2] = this.gammaLut[b];
				} else {
					pd[0] = r;
					pd[1] = g;
					pd[2] = b;
				}

				dst.setPixel(dstX, y, pd);
				dstX += step;
			}
			break;

		case POST_GAMMA_EXP:
			for (srcX = 0; srcX < width; srcX++) {
				src.getPixel(srcX, 0, ps);

				int val = ps[0];
				int alpha = ps[1];
				int gamma = this.gammaLut[val];
				pd[0] = gamma;
				pd[1] = gamma;
				pd[2] = gamma;
				pd[3] = alpha;

				dst.setPixel(dstX, y, pd);
				dstX += step;
			}
			break;

		case POST_GRAY_ALPHA_EXP:
			for (srcX = 0; srcX < width; srcX++) {
				src.getPixel(srcX, 0, ps);

				int val = ps[0];
				int alpha = ps[1];
				pd[0] = val;
				pd[1] = val;
				pd[2] = val;
				pd[3] = alpha;

				dst.setPixel(dstX, y, pd);
				dstX += step;
			}
			break;

		case POST_ADD_GRAY_TRANS_EXP:
			for (srcX = 0; srcX < width; srcX++) {
				src.getPixel(srcX, 0, ps);

				int val = ps[0];
				if (this.performGammaCorrection) {
					val = this.gammaLut[val];
				}
				pd[0] = val;
				pd[1] = val;
				pd[2] = val;
				if (val == this.grayTransparentAlpha) {
					pd[3] = 0;
				} else {
					pd[3] = this.maxOpacity;
				}

				dst.setPixel(dstX, y, pd);
				dstX += step;
			}
			break;

		case POST_GRAY_LUT_ADD_TRANS_EXP:
			for (srcX = 0; srcX < width; srcX++) {
				src.getPixel(srcX, 0, ps);

				int val = ps[0];
				int val2 = this.grayLut[val];
				pd[0] = val2;
				pd[1] = val2;
				pd[2] = val2;
				if (val == this.grayTransparentAlpha) {
					pd[3] = 0;
				} else {
					pd[3] = this.maxOpacity;
				}

				dst.setPixel(dstX, y, pd);
				dstX += step;
			}
			break;
		}
	}

	/**
	 * Reads in an image of a given size and returns it as a WritableRaster.
	 */
	private void decodePass(WritableRaster imRas, int xOffset, int yOffset, int xStep, int yStep, int passWidth,
			int passHeight) throws IOException {
		if ((passWidth == 0) || (passHeight == 0)) {
			return;
		}

		int bytesPerRow = (this.inputBands * passWidth * this.bitDepth + 7) / 8;
		int eltsPerRow = (this.bitDepth == 16) ? bytesPerRow / 2 : bytesPerRow;
		byte[] curr = new byte[bytesPerRow];
		byte[] prior = new byte[bytesPerRow];

		// Create a 1-row tall Raster to hold the data
		WritableRaster passRow = createRaster(passWidth, 1, this.inputBands, eltsPerRow, this.bitDepth);
		DataBuffer dataBuffer = passRow.getDataBuffer();
		int type = dataBuffer.getDataType();
		byte[] byteData = null;
		short[] shortData = null;
		if (type == DataBuffer.TYPE_BYTE) {
			byteData = ((DataBufferByte) dataBuffer).getData();
		} else {
			shortData = ((DataBufferUShort) dataBuffer).getData();
		}

		// Decode the (sub)image row-by-row
		int srcY, dstY;
		for (srcY = 0, dstY = yOffset; srcY < passHeight; srcY++, dstY += yStep) {
			// Read the filter type byte and a row of data
			int filter = this.dataStream.read();
			this.dataStream.readFully(curr, 0, bytesPerRow);

			switch (filter) {
			case PNG_FILTER_NONE:
				break;
			case PNG_FILTER_SUB:
				decodeSubFilter(curr, bytesPerRow, this.bytesPerPixel);
				break;
			case PNG_FILTER_UP:
				decodeUpFilter(curr, prior, bytesPerRow);
				break;
			case PNG_FILTER_AVERAGE:
				decodeAverageFilter(curr, prior, bytesPerRow, this.bytesPerPixel);
				break;
			case PNG_FILTER_PAETH:
				decodePaethFilter(curr, prior, bytesPerRow, this.bytesPerPixel);
				break;
			default:
				// Error -- uknown filter type
				throw new RuntimeException();
			}

			// Copy data into passRow byte by byte
			if (this.bitDepth < 16) {
				System.arraycopy(curr, 0, byteData, 0, bytesPerRow);
			} else {
				int idx = 0;
				for (int j = 0; j < eltsPerRow; j++) {
					shortData[j] = (short) ((curr[idx] << 8) | (curr[idx + 1] & 0xff));
					idx += 2;
				}
			}

			processPixels(this.postProcess, passRow, imRas, xOffset, xStep, dstY, passWidth);

			// Swap curr and prior
			byte[] tmp = prior;
			prior = curr;
			curr = tmp;
		}
	}

	private void decodeImage(boolean useInterlacing) throws IOException {
		if (!useInterlacing) {
			decodePass(this.theTile, 0, 0, 1, 1, this.width, this.height);
		} else {
			decodePass(this.theTile, 0, 0, 8, 8, (this.width + 7) / 8, (this.height + 7) / 8);
			decodePass(this.theTile, 4, 0, 8, 8, (this.width + 3) / 8, (this.height + 7) / 8);
			decodePass(this.theTile, 0, 4, 4, 8, (this.width + 3) / 4, (this.height + 3) / 8);
			decodePass(this.theTile, 2, 0, 4, 4, (this.width + 1) / 4, (this.height + 3) / 4);
			decodePass(this.theTile, 0, 2, 2, 4, (this.width + 1) / 2, (this.height + 1) / 4);
			decodePass(this.theTile, 1, 0, 2, 2, this.width / 2, (this.height + 1) / 2);
			decodePass(this.theTile, 0, 1, 1, 2, this.width, this.height / 2);
		}
	}

	// RenderedImage stuff

	public Raster getTile(int tileX, int tileY) {
		if (tileX != 0 || tileY != 0) {
			// Error -- bad tile requested
			throw new IllegalArgumentException();
		}
		return this.theTile;
	}
}
