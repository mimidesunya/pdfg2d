/*

 Copyright 2001  The Apache Software Foundation 

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
import net.zamasoft.font.table.Program;

/**
 * @version $Id: GlyfDescript.java 1034 2013-10-23 05:51:57Z miyabe $
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public abstract class GlyfDescript extends Program {

	// flags
	public static final byte onCurve = 0x01;

	public static final byte xShortVector = 0x02;

	public static final byte yShortVector = 0x04;

	public static final byte repeat = 0x08;

	public static final byte xDual = 0x10;

	public static final byte yDual = 0x20;

	protected GlyfTable parentTable;

	private int numberOfContours;

	private short xMin;

	private short yMin;

	private short xMax;

	private short yMax;

	protected GlyfDescript(GlyfTable parentTable, int numberOfContours, RandomAccessFile raf) throws IOException {
		this.parentTable = parentTable;
		this.numberOfContours = numberOfContours;
		this.xMin = (short) (raf.read() << 8 | raf.read());
		this.yMin = (short) (raf.read() << 8 | raf.read());
		this.xMax = (short) (raf.read() << 8 | raf.read());
		this.yMax = (short) (raf.read() << 8 | raf.read());
	}

	public int getNumberOfContours() {
		return this.numberOfContours;
	}

	public short getXMaximum() {
		return this.xMax;
	}

	public short getXMinimum() {
		return this.xMin;
	}

	public short getYMaximum() {
		return this.yMax;
	}

	public short getYMinimum() {
		return this.yMin;
	}

	public abstract int getEndPtOfContours(int i);

	public abstract byte getFlags(int i);

	public abstract short getXCoordinate(int i);

	public abstract short getYCoordinate(int i);

	public abstract boolean isComposite();

	public abstract int getPointCount();

	public abstract int getContourCount();
}
