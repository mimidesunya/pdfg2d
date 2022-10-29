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
 * @version $Id: SingleSubstFormat2.java 1034 2013-10-23 05:51:57Z miyabe $
 */
public class SingleSubstFormat2 extends SingleSubst {
	private static final long serialVersionUID = 0L;

	private int coverageOffset;

	private int glyphCount;

	private int[] substitutes;

	private Coverage coverage;

	/** Creates new SingleSubstFormat2 */
	protected SingleSubstFormat2(RandomAccessFile raf, int offset) throws IOException {
		this.coverageOffset = raf.readUnsignedShort();
		this.glyphCount = raf.readUnsignedShort();
		this.substitutes = new int[this.glyphCount];
		for (int i = 0; i < this.glyphCount; i++) {
			this.substitutes[i] = raf.readUnsignedShort();
		}
		synchronized (raf) {
			raf.seek(offset + this.coverageOffset);
			this.coverage = Coverage.read(raf);
		}
	}

	public int getFormat() {
		return 2;
	}

	public int substitute(int glyphId) {
		int i = this.coverage.findGlyph(glyphId);
		if (i > -1) {
			return this.substitutes[i];
		}
		return glyphId;
	}

}
