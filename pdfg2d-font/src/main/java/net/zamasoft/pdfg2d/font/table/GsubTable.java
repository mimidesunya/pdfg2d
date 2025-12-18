package net.zamasoft.pdfg2d.font.table;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * GSUB (Glyph Substitution) table.
 * 
 * @param scriptList  the script list
 * @param featureList the feature list
 * @param lookupList  the lookup list
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 * @since 1.0
 */
public record GsubTable(ScriptList scriptList, FeatureList featureList, LookupList lookupList)
		implements Table, LookupSubtableFactory {

	protected GsubTable(final DirectoryEntry de, final RandomAccessFile raf) throws IOException {
		this(readData(de, raf));
	}

	private GsubTable(GsubTable other) {
		this(other.scriptList, other.featureList, other.lookupList);
	}

	private static GsubTable readData(final DirectoryEntry de, final RandomAccessFile raf) throws IOException {
		synchronized (raf) {
			raf.seek(de.offset());

			// GSUB Header
			raf.readInt(); // version
			final int scriptListOffset = raf.readUnsignedShort();
			final int featureListOffset = raf.readUnsignedShort();
			final int lookupListOffset = raf.readUnsignedShort();

			// We need a temporary factory to read the lookup list
			final LookupSubtableFactory factory = (type, subRaf, offset1) -> {
				return switch (type) {
					case 1 -> SingleSubst.read(subRaf, offset1);
					case 4 -> LigatureSubst.read(subRaf, offset1);
					default -> null;
				};
			};

			// Script List
			final ScriptList scriptList = new ScriptList(raf, de.offset() + scriptListOffset);

			// Feature List
			final FeatureList featureList = new FeatureList(raf, de.offset() + featureListOffset);

			// Lookup List
			final LookupList lookupList = new LookupList(raf, de.offset() + lookupListOffset, factory);

			return new GsubTable(scriptList, featureList, lookupList);
		}
	}

	/**
	 * 1 - Single - Replace one glyph with one glyph 2 - Multiple - Replace one
	 * glyph
	 * with more than one glyph 3 - Alternate - Replace one glyph with one of many
	 * glyphs 4 - Ligature - Replace multiple glyphs with one glyph 5 - Context -
	 * Replace one or more glyphs in context 6 - Chaining - Context Replace one or
	 * more glyphs in chained context
	 */
	@Override
	public LookupSubtable read(final int type, final RandomAccessFile raf, final int offset) throws IOException {
		return switch (type) {
			case 1 -> SingleSubst.read(raf, offset);
			case 4 -> LigatureSubst.read(raf, offset);
			default -> null;
		};
	}

	@Override
	public int getType() {
		return GSUB;
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
		return "GSUB";
	}
}
