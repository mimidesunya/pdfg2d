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
package net.zamasoft.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;

/**
 * @version $Id: XmtxTable.java 1034 2013-10-23 05:51:57Z miyabe $
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public abstract class XmtxTable implements Table, Serializable {
	private static final long serialVersionUID = 0L;

	private DirectoryEntry de;

	private RandomAccessFile raf;

	private int numberOfHMetrics, lsbCount;

	private int[] xMetrics = null;

	private short[] leftSideBearing = null;

	protected XmtxTable(DirectoryEntry de, RandomAccessFile raf) throws IOException {
		this.de = de;
		this.raf = raf;
	}

	public void init(int numberOfHMetrics, int lsbCount) throws IOException {
		this.numberOfHMetrics = numberOfHMetrics;
		this.lsbCount = lsbCount;
	}

	private void load() {
		if (this.raf == null) {
			return;
		}
		synchronized (this.raf) {
			try {
				this.xMetrics = new int[this.numberOfHMetrics];
				this.raf.seek(this.de.getOffset());
				for (int i = 0; i < this.numberOfHMetrics; i++) {
					this.xMetrics[i] = (this.raf.read() << 24 | this.raf.read() << 16 | this.raf.read() << 8
							| this.raf.read());
				}
				if (this.lsbCount > 0) {
					this.leftSideBearing = new short[this.lsbCount];
					for (int i = 0; i < this.lsbCount; i++) {
						this.leftSideBearing[i] = (short) (this.raf.read() << 8 | this.raf.read());
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			this.raf = null;
		}
	}

	public synchronized int getAdvanceWidth(int i) {
		this.load();
		int advance;
		if (i < this.xMetrics.length) {
			advance = this.xMetrics[i] >> 16;
		} else {
			advance = this.xMetrics[this.xMetrics.length - 1] >> 16;
		}
		return advance;
	}

	public synchronized short getLeftSideBearing(int i) {
		this.load();
		short lsb;
		if (i < this.xMetrics.length) {
			lsb = (short) (this.xMetrics[i] & 0xffff);
		} else {
			i -= this.xMetrics.length;
			lsb = this.leftSideBearing[i];
		}
		return lsb;
	}
}
