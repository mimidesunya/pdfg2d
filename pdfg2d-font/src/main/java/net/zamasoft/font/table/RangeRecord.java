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
 * Coverage Index (GlyphID) = StartCoverageIndex + GlyphID - Start GlyphID
 * 
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @since 1.0
 */
public class RangeRecord {

	private int start;

	private int end;

	private int startCoverageIndex;

	/** Creates new RangeRecord */
	public RangeRecord(RandomAccessFile raf) throws IOException {
		start = raf.readUnsignedShort();
		end = raf.readUnsignedShort();
		startCoverageIndex = raf.readUnsignedShort();
	}

	public boolean isInRange(int glyphId) {
		return (start <= glyphId && glyphId <= end);
	}

	public int getCoverageIndex(int glyphId) {
		if (isInRange(glyphId)) {
			return startCoverageIndex + glyphId - start;
		}
		return -1;
	}

}
