package net.zamasoft.pdfg2d.font.table;

/**
 * Horizontal/Vertical header table interface.
 * 
 * @since 1.0
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public interface XheaTable extends Table {

	short getAdvanceWidthMax();

	short getAscender();

	short getCaretSlopeRise();

	short getCaretSlopeRun();

	short getDescender();

	short getLineGap();

	short getMetricDataFormat();

	short getMinLeftSideBearing();

	short getMinRightSideBearing();

	int getNumberOfHMetrics();

	short getXMaxExtent();
}
