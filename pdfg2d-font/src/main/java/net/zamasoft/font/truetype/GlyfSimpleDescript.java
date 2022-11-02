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

import net.zamasoft.font.table.GlyfTable;

/**
 * @since 1.0
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public class GlyfSimpleDescript extends GlyfDescript {

	private int[] endPtsOfContours;

	private byte[] flags;

	private short[] xCoordinates;

	private short[] yCoordinates;

	private int count;

	public GlyfSimpleDescript(GlyfTable parentTable, int numberOfContours, RandomAccessFile raf) throws IOException {

		super(parentTable, numberOfContours, raf);

		// Simple glyph description
		this.endPtsOfContours = new int[numberOfContours];
		for (int i = 0; i < numberOfContours; i++) {
			this.endPtsOfContours[i] = (raf.read() << 8 | raf.read());
		}

		// The last end point index reveals the total number of points
		this.count = this.endPtsOfContours[numberOfContours - 1] + 1;
		this.flags = new byte[this.count];
		this.xCoordinates = new short[this.count];
		this.yCoordinates = new short[this.count];

		int instructionCount = (raf.read() << 8 | raf.read());
		this.readInstructions(raf, instructionCount);
		this.readFlags(this.count, raf);
		this.readCoords(this.count, raf);
	}

	public int getEndPtOfContours(int i) {
		return this.endPtsOfContours[i];
	}

	public byte getFlags(int i) {
		return this.flags[i];
	}

	public short getXCoordinate(int i) {
		return this.xCoordinates[i];
	}

	public short getYCoordinate(int i) {
		return this.yCoordinates[i];
	}

	public boolean isComposite() {
		return false;
	}

	public int getPointCount() {
		return this.count;
	}

	public int getContourCount() {
		return this.getNumberOfContours();
	}

	/**
	 * The table is stored as relative values, but we'll store them as absolutes
	 */
	private void readCoords(int count, RandomAccessFile raf) throws IOException {
		short x = 0;
		short y = 0;
		for (int i = 0; i < count; i++) {
			if ((this.flags[i] & xDual) != 0) {
				if ((this.flags[i] & xShortVector) != 0) {
					x += (short) raf.read();
				}
			} else {
				if ((this.flags[i] & xShortVector) != 0) {
					x += (short) -((short) raf.read());
				} else {
					x += (short) (raf.read() << 8 | raf.read());
				}
			}
			this.xCoordinates[i] = x;
		}

		for (int i = 0; i < count; i++) {
			if ((this.flags[i] & yDual) != 0) {
				if ((this.flags[i] & yShortVector) != 0) {
					y += (short) raf.read();
				}
			} else {
				if ((this.flags[i] & yShortVector) != 0) {
					y += (short) -((short) raf.read());
				} else {
					y += (short) (raf.read() << 8 | raf.read());
				}
			}
			this.yCoordinates[i] = y;
		}
	}

	/**
	 * The flags are run-length encoded
	 */
	private void readFlags(int flagCount, RandomAccessFile raf) throws IOException {
		try {
			for (int index = 0; index < flagCount; index++) {
				this.flags[index] = (byte) raf.read();
				if ((this.flags[index] & repeat) != 0) {
					int repeats = raf.read();
					for (int i = 1; i <= repeats; i++) {
						this.flags[index + i] = this.flags[index];
					}
					index += repeats;
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("error: array index out of bounds");
		}
	}
}
