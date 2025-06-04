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
 * 
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @since 1.0
 */
public class SingleSubstFormat1 extends SingleSubst {
	private static final long serialVersionUID = 0L;

	private int coverageOffset;

	private short deltaGlyphID;

	private Coverage coverage;

	/** Creates new SingleSubstFormat1 */
	protected SingleSubstFormat1(RandomAccessFile raf, int offset) throws IOException {
		this.coverageOffset = raf.readUnsignedShort();
		this.deltaGlyphID = raf.readShort();
		synchronized (raf) {
			raf.seek(offset + this.coverageOffset);
			this.coverage = Coverage.read(raf);
		}
	}

	public int getFormat() {
		return 1;
	}

	public int substitute(int glyphId) {
		int i = this.coverage.findGlyph(glyphId);
		if (i > -1) {
			return glyphId + this.deltaGlyphID;
		}
		return glyphId;
	}

}
