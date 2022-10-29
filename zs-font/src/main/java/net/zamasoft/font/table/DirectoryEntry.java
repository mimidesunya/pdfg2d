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
import java.io.Serializable;

/**
 * @version $Id: DirectoryEntry.java 1034 2013-10-23 05:51:57Z miyabe $
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public class DirectoryEntry implements Serializable {
	private static final long serialVersionUID = 0L;

	private int tag;

	private int checksum;

	private int offset;

	private int length;

	protected DirectoryEntry(RandomAccessFile raf) throws IOException {
		this.tag = raf.readInt();
		this.checksum = raf.readInt();
		this.offset = raf.readInt();
		this.length = raf.readInt();
	}

	public int getChecksum() {
		return this.checksum;
	}

	public int getLength() {
		return this.length;
	}

	public int getOffset() {
		return this.offset;
	}

	public int getTag() {
		return this.tag;
	}

	public String toString() {
		return new StringBuffer().append((char) ((this.tag >> 24) & 0xff)).append((char) ((this.tag >> 16) & 0xff))
				.append((char) ((this.tag >> 8) & 0xff)).append((char) ((this.tag) & 0xff)).append(", offset: ")
				.append(this.offset).append(", length: ").append(this.length).append(", checksum: 0x")
				.append(Integer.toHexString(this.checksum)).toString();
	}
}
