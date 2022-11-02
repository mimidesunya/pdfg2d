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
public class Script {

	private int defaultLangSysOffset;

	private int langSysCount;

	private LangSysRecord[] langSysRecords;

	private LangSys defaultLangSys;

	private LangSys[] langSys;

	/** Creates new ScriptTable */
	protected Script(RandomAccessFile raf, int offset) throws IOException {
		synchronized (raf) {
			raf.seek(offset);
			this.defaultLangSysOffset = raf.readUnsignedShort();
			this.langSysCount = raf.readUnsignedShort();
			if (this.langSysCount > 0) {
				this.langSysRecords = new LangSysRecord[this.langSysCount];
				for (int i = 0; i < this.langSysCount; i++) {
					this.langSysRecords[i] = new LangSysRecord(raf);
				}
			}

			// Read the LangSys tables
			if (this.langSysCount > 0) {
				this.langSys = new LangSys[this.langSysCount];
				for (int i = 0; i < this.langSysCount; i++) {
					raf.seek(offset + this.langSysRecords[i].getOffset());
					this.langSys[i] = new LangSys(raf);
				}
			}
			if (this.defaultLangSysOffset > 0) {
				raf.seek(offset + this.defaultLangSysOffset);
				this.defaultLangSys = new LangSys(raf);
			}
		}
	}

	public LangSys getDefaultLangSys() {
		return this.defaultLangSys;
	}

	public String toString() {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < this.langSysRecords.length; i++) {
			int tag = this.langSysRecords[i].getTag();
			buff.append((char) ((tag >> 24) & 0xff)).append((char) ((tag >> 16) & 0xff))
					.append((char) ((tag >> 8) & 0xff)).append((char) ((tag) & 0xff)).append('/');
		}
		return buff.toString();
	}
}
