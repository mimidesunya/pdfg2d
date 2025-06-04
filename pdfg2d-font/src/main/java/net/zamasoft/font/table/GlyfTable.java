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

import net.zamasoft.font.truetype.GlyfCompositeDescript;
import net.zamasoft.font.truetype.GlyfDescript;
import net.zamasoft.font.truetype.GlyfSimpleDescript;

/**
 * @since 1.0
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public class GlyfTable implements Table {

	private DirectoryEntry de;

	private RandomAccessFile raf;

	private LocaTable loca;

	protected GlyfTable(DirectoryEntry de, RandomAccessFile raf) throws IOException {
		this.de = de;
		this.raf = raf;
	}

	public void init(LocaTable loca) {
		this.loca = loca;
	}

	public GlyfDescript getDescription(int i) {
		GlyfDescript desc = null;
		try {
			int len = this.loca.getOffset((i + 1)) - this.loca.getOffset(i);
			if (len <= 0) {
				return null;
			}
			synchronized (this.raf) {
				this.raf.seek(de.getOffset() + this.loca.getOffset(i));
				int numberOfContours = this.raf.readShort();
				if (numberOfContours >= 0) {
					desc = new GlyfSimpleDescript(this, numberOfContours, this.raf);
				} else {
					desc = new GlyfCompositeDescript(this, this.raf);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return desc;
	}

	public int getType() {
		return glyf;
	}
}
