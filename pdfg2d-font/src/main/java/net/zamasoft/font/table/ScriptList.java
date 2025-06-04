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
public class ScriptList {

	private final int scriptCount;

	private final ScriptRecord[] scriptRecords;

	private final Script[] scripts;

	/** Creates new ScriptList */
	protected ScriptList(RandomAccessFile raf, int offset) throws IOException {
		synchronized (raf) {
			raf.seek(offset);
			this.scriptCount = raf.readUnsignedShort();
			this.scriptRecords = new ScriptRecord[this.scriptCount];
			this.scripts = new Script[this.scriptCount];
			for (int i = 0; i < this.scriptCount; i++) {
				this.scriptRecords[i] = new ScriptRecord(raf);
			}
			for (int i = 0; i < this.scriptCount; i++) {
				this.scripts[i] = new Script(raf, offset + this.scriptRecords[i].getOffset());
			}
		}
	}

	public int getScriptCount() {
		return this.scriptCount;
	}

	public ScriptRecord getScriptRecord(int i) {
		return this.scriptRecords[i];
	}

	public Script findScript(String tag) {
		if (tag.length() != 4) {
			return null;
		}
		int tagVal = ((tag.charAt(0) << 24) | (tag.charAt(1) << 16) | (tag.charAt(2) << 8) | tag.charAt(3));
		for (int i = 0; i < this.scriptCount; i++) {
			if (this.scriptRecords[i].getTag() == tagVal) {
				return this.scripts[i];
			}
		}
		return null;
	}

	public String toString() {
		StringBuffer buff = new StringBuffer(this.scriptCount + ":");
		for (int i = 0; i < this.scriptCount; i++) {
			int tag = this.scriptRecords[i].getTag();
			buff.append((char) ((tag >> 24) & 0xff)).append((char) ((tag >> 16) & 0xff))
					.append((char) ((tag >> 8) & 0xff)).append((char) ((tag) & 0xff)).append('/');
		}
		return buff.toString();
	}
}
