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
package net.zamasoft.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @since 1.0
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public abstract class XheaTable implements Table {

	private short ascender;

	private short descender;

	private short lineGap;

	private short advanceWidthMax;

	private short minLeftSideBearing;

	private short minRightSideBearing;

	private short xMaxExtent;

	private short caretSlopeRise;

	private short caretSlopeRun;

	private short metricDataFormat;

	private int numberOfHMetrics;

	protected XheaTable(DirectoryEntry de, RandomAccessFile raf) throws IOException {
		synchronized (raf) {
			raf.seek(de.getOffset());
			raf.readInt(); // version
			ascender = raf.readShort();
			descender = raf.readShort();
			lineGap = raf.readShort();
			advanceWidthMax = raf.readShort();
			minLeftSideBearing = raf.readShort();
			minRightSideBearing = raf.readShort();
			xMaxExtent = raf.readShort();
			caretSlopeRise = raf.readShort();
			caretSlopeRun = raf.readShort();
			for (int i = 0; i < 5; i++) {
				raf.readShort();
			}
			metricDataFormat = raf.readShort();
			numberOfHMetrics = raf.readUnsignedShort();
		}
	}

	public short getAdvanceWidthMax() {
		return advanceWidthMax;
	}

	public short getAscender() {
		return ascender;
	}

	public short getCaretSlopeRise() {
		return caretSlopeRise;
	}

	public short getCaretSlopeRun() {
		return caretSlopeRun;
	}

	public short getDescender() {
		return descender;
	}

	public short getLineGap() {
		return lineGap;
	}

	public short getMetricDataFormat() {
		return metricDataFormat;
	}

	public short getMinLeftSideBearing() {
		return minLeftSideBearing;
	}

	public short getMinRightSideBearing() {
		return minRightSideBearing;
	}

	public int getNumberOfHMetrics() {
		return numberOfHMetrics;
	}

	public short getXMaxExtent() {
		return xMaxExtent;
	}
}
