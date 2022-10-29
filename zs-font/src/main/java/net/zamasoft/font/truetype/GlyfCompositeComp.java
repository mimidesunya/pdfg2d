/*

 Copyright 2001,2003  The Apache Software Foundation 

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */
package net.zamasoft.font.truetype;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @version $Id: GlyfCompositeComp.java 1356 2014-12-27 08:56:49Z miyabe $
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public class GlyfCompositeComp {

	public static final short ARG_1_AND_2_ARE_WORDS = 0x0001;

	public static final short ARGS_ARE_XY_VALUES = 0x0002;

	public static final short ROUND_XY_TO_GRID = 0x0004;

	public static final short WE_HAVE_A_SCALE = 0x0008;

	public static final short MORE_COMPONENTS = 0x0020;

	public static final short WE_HAVE_AN_X_AND_Y_SCALE = 0x0040;

	public static final short WE_HAVE_A_TWO_BY_TWO = 0x0080;

	public static final short WE_HAVE_INSTRUCTIONS = 0x0100;

	public static final short USE_MY_METRICS = 0x0200;

	private int firstIndex;

	private int firstContour;

	private short argument1;

	private short argument2;

	private short flags;

	private short glyphIndex;

	private double xscale = 1.0;

	private double yscale = 1.0;

	private double scale01 = 0.0;

	private double scale10 = 0.0;

	private int xtranslate = 0;

	private int ytranslate = 0;

	protected GlyfCompositeComp(int firstIndex, int firstContour, RandomAccessFile raf) throws IOException {
		this.firstIndex = firstIndex;
		this.firstContour = firstContour;
		flags = (short) (raf.read() << 8 | raf.read());
		glyphIndex = (short) Math.abs((short) (raf.read() << 8 | raf.read()));

		// Get the arguments as just their raw values
		if ((flags & ARG_1_AND_2_ARE_WORDS) != 0) {
			argument1 = (short) (raf.read() << 8 | raf.read());
			argument2 = (short) (raf.read() << 8 | raf.read());
		} else {
			argument1 = (short) raf.read();
			argument2 = (short) raf.read();
		}

		// Assign the arguments according to the flags
		if ((flags & ARGS_ARE_XY_VALUES) != 0) {
			xtranslate = argument1;
			ytranslate = argument2;
		}

		// Get the scale values (if any)
		if ((flags & WE_HAVE_A_SCALE) != 0) {
			int i = (short) (raf.read() << 8 | raf.read());
			xscale = yscale = (double) i / (double) 0x4000;
		} else if ((flags & WE_HAVE_AN_X_AND_Y_SCALE) != 0) {
			short i = (short) (raf.read() << 8 | raf.read());
			xscale = (double) i / (double) 0x4000;
			i = (short) (raf.read() << 8 | raf.read());
			yscale = (double) i / (double) 0x4000;
		} else if ((flags & WE_HAVE_A_TWO_BY_TWO) != 0) {
			int i = (short) (raf.read() << 8 | raf.read());
			xscale = (double) i / (double) 0x4000;
			i = (short) (raf.read() << 8 | raf.read());
			scale01 = (double) i / (double) 0x4000;
			i = (short) (raf.read() << 8 | raf.read());
			scale10 = (double) i / (double) 0x4000;
			i = (short) (raf.read() << 8 | raf.read());
			yscale = (double) i / (double) 0x4000;
		}
	}

	public int getFirstIndex() {
		return firstIndex;
	}

	public int getFirstContour() {
		return firstContour;
	}

	public short getArgument1() {
		return argument1;
	}

	public short getArgument2() {
		return argument2;
	}

	public short getFlags() {
		return flags;
	}

	public short getGlyphIndex() {
		return glyphIndex;
	}

	public double getScale01() {
		return scale01;
	}

	public double getScale10() {
		return scale10;
	}

	public double getXScale() {
		return xscale;
	}

	public double getYScale() {
		return yscale;
	}

	public int getXTranslate() {
		return xtranslate;
	}

	public int getYTranslate() {
		return ytranslate;
	}

	/**
	 * Transforms an x-coordinate of a point for this component.
	 * 
	 * @param x
	 *            The x-coordinate of the point to transform
	 * @param y
	 *            The y-coordinate of the point to transform
	 * @return The transformed x-coordinate
	 */
	public int scaleX(int x, int y) {
		return Math.round((float) (x * xscale + y * scale10));
	}

	/**
	 * Transforms a y-coordinate of a point for this component.
	 * 
	 * @param x
	 *            The x-coordinate of the point to transform
	 * @param y
	 *            The y-coordinate of the point to transform
	 * @return The transformed y-coordinate
	 */
	public int scaleY(int x, int y) {
		return Math.round((float) (x * scale01 + y * yscale));
	}
}
