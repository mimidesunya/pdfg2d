package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * GPOS (Glyph Positioning) table.
 * 
 * @param scriptList  the script list
 * @param featureList the feature list
 * @param lookupList  the lookup list
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @since 1.0
 */
public record GposTable(ScriptList scriptList, FeatureList featureList, LookupList lookupList)
		implements Table, LookupSubtableFactory {

	protected GposTable(final DirectoryEntry de, final RandomAccessFile raf) throws IOException {
		this(readData(de, raf));
	}

	private GposTable(GposTable other) {
		this(other.scriptList, other.featureList, other.lookupList);
	}

	private static GposTable readData(final DirectoryEntry de, final RandomAccessFile raf) throws IOException {
		synchronized (raf) {
			raf.seek(de.offset());

			// GPOS Header
			raf.readInt(); // version
			final int scriptListOffset = raf.readUnsignedShort();
			final int featureListOffset = raf.readUnsignedShort();
			final int lookupListOffset = raf.readUnsignedShort();

			// We need a temporary factory to read the lookup list
			final LookupSubtableFactory factory = (type, subRaf, offset1) -> null;

			// Script List
			final ScriptList scriptList = new ScriptList(raf, de.offset() + scriptListOffset);

			// Feature List
			final FeatureList featureList = new FeatureList(raf, de.offset() + featureListOffset);

			// Lookup List
			final LookupList lookupList = new LookupList(raf, de.offset() + lookupListOffset, factory);

			return new GposTable(scriptList, featureList, lookupList);
		}
	}

	@Override
	public LookupSubtable read(final int type, final RandomAccessFile raf, final int offset) throws IOException {
		// Currently no subtypes implemented
		return null;
	}

	@Override
	public int getType() {
		return GPOS;
	}

	public ScriptList getScriptList() {
		return this.scriptList;
	}

	public FeatureList getFeatureList() {
		return this.featureList;
	}

	public LookupList getLookupList() {
		return this.lookupList;
	}

	@Override
	public String toString() {
		return "GPOS";
	}
}
