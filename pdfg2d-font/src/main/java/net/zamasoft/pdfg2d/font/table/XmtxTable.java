package net.zamasoft.pdfg2d.font.table;

import java.io.Serializable;

/**
 * Horizontal/Vertical metrics table interface.
 * 
 * @since 1.0
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public interface XmtxTable extends Table, Serializable {

	int getAdvanceWidth(int i);

	short getLeftSideBearing(int i);
}
