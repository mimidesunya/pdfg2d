/*

 Copyright 2001-2003  The Apache Software Foundation 

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
package net.zamasoft.font;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.zamasoft.font.cff.CFFGlyphList;
import net.zamasoft.font.cff.CFFTable;
import net.zamasoft.font.table.DirectoryEntry;
import net.zamasoft.font.table.GlyfTable;
import net.zamasoft.font.table.HeadTable;
import net.zamasoft.font.table.HheaTable;
import net.zamasoft.font.table.HmtxTable;
import net.zamasoft.font.table.LocaTable;
import net.zamasoft.font.table.MaxpTable;
import net.zamasoft.font.table.Table;
import net.zamasoft.font.table.TableDirectory;
import net.zamasoft.font.table.TableFactory;
import net.zamasoft.font.table.VheaTable;
import net.zamasoft.font.table.VmtxTable;
import net.zamasoft.font.truetype.TrueTypeGlyphList;

/**
 * The TrueType font.
 * 
 * @since 1.0
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public class OpenTypeFont {
	private Logger LOG = Logger.getLogger(OpenTypeFont.class.getName());

	private TableDirectory tableDirectory = null;

	private Table[] tables;

	private HeadTable head;

	private HheaTable hhea;

	private HmtxTable hmtx;

	private VheaTable vhea;

	private VmtxTable vmtx;

	private MaxpTable maxp;

	private GlyphList glyphList;

	private RandomAccessFile raf;

	public OpenTypeFont(RandomAccessFile raf) throws IOException {
		this.raf = raf;
		this.tableDirectory = new TableDirectory(raf);
		this.tables = new Table[this.tableDirectory.getNumTables()];

		// Get references to commonly used tables
		this.head = (HeadTable) getTable(Table.head);
		this.hhea = (HheaTable) getTable(Table.hhea);
		this.hmtx = (HmtxTable) getTable(Table.hmtx);
		this.vhea = (VheaTable) getTable(Table.vhea);
		this.vmtx = (VmtxTable) getTable(Table.vmtx);
		this.maxp = (MaxpTable) getTable(Table.maxp);

		// Initialize the tables that require it
		this.hmtx.init(this.hhea.getNumberOfHMetrics(), this.maxp.getNumGlyphs() - this.hhea.getNumberOfHMetrics());
		if (this.vmtx != null) {
			this.vmtx.init(this.vhea.getNumberOfHMetrics(), this.maxp.getNumGlyphs() - this.vhea.getNumberOfHMetrics());
		}

		GlyfTable glyf = (GlyfTable) getTable(Table.glyf);
		if (glyf != null) {
			// TrueType
			LocaTable loca = (LocaTable) getTable(Table.loca);
			glyf.init(loca);
			loca.init(this.maxp.getNumGlyphs(), this.head.getIndexToLocFormat() == 0);
			this.glyphList = new TrueTypeGlyphList(glyf, this.head, this.maxp);
		} else {
			CFFTable cff = (CFFTable) getTable(Table.CFF);
			if (cff != null) {
				// CFF
				cff.init();
				this.glyphList = new CFFGlyphList(cff, this.head, this.maxp);
			} else {
				throw new IOException("Unsupported font file format.");
			}
		}
	}

	public Table getTable(int tableType) {
		// Load each of the tables
		for (int i = 0; i < this.tableDirectory.getNumTables(); i++) {
			DirectoryEntry entry = this.tableDirectory.getEntry(i);
			if (entry.getTag() == tableType) {
				if (this.tables[i] == null) {
					try {
						this.tables[i] = TableFactory.create(this, entry, this.raf);
					} catch (IOException e) {
						LOG.log(Level.WARNING, "Can't read font file.", e);
						return null;
					}
				}
				return this.tables[i];
			}
		}
		return null;
	}

	public int getAscent() {
		return this.hhea.getAscender();
	}

	public int getDescent() {
		return this.hhea.getDescender();
	}

	public int getNumGlyphs() {
		return this.maxp.getNumGlyphs();
	}

	public Glyph getGlyph(int i) {
		return this.glyphList.getGlyph(i);
	}

	public TableDirectory getTableDirectory() {
		return this.tableDirectory;
	}

	protected void finalize() throws Throwable {
		super.finalize();
		try {
			this.raf.close();
		} catch (Exception e) {
			// ignore
		}
	}
}
