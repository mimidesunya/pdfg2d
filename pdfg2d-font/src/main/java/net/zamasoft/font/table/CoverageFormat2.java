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

/**
 * 
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @since 1.0
 */
public class CoverageFormat2 extends Coverage {
	private static final long serialVersionUID = 0L;

	private int rangeCount;

	private RangeRecord[] rangeRecords;

	/** Creates new CoverageFormat2 */
	protected CoverageFormat2(RandomAccessFile raf) throws IOException {
		rangeCount = raf.readUnsignedShort();
		rangeRecords = new RangeRecord[rangeCount];
		for (int i = 0; i < rangeCount; i++) {
			rangeRecords[i] = new RangeRecord(raf);
		}
	}

	public int getFormat() {
		return 2;
	}

	public int findGlyph(int glyphId) {
		for (int i = 0; i < rangeCount; i++) {
			int n = rangeRecords[i].getCoverageIndex(glyphId);
			if (n > -1) {
				return n;
			}
		}
		return -1;
	}

}
