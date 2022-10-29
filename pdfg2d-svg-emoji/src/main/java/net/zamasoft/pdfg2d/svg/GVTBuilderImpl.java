package net.zamasoft.pdfg2d.svg;

import java.awt.AlphaComposite;
import java.awt.Composite;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;

public class GVTBuilderImpl extends GVTBuilder {
	private final boolean forceVector;

	public GVTBuilderImpl(boolean forceVector) {
		this.forceVector = forceVector;
	}

	public GVTBuilderImpl() {
		this(false);
	}

	/**
	 * 透明化処理のためにCompositeのビルドメソッドをオーバーライドします。
	 */
	protected void buildComposite(BridgeContext ctx, Element e, CompositeGraphicsNode cgn) {
		super.buildComposite(ctx, e, cgn);
		for (int i = 0; i < cgn.size(); ++i) {
			GraphicsNode gn = (GraphicsNode) cgn.get(i);
			Composite c = gn.getComposite();
			if (!(c instanceof AlphaComposite)) {
				continue;
			}
			AlphaComposite ac = (AlphaComposite) c;
			if (ac.getRule() != AlphaComposite.SRC_OVER || ac.getAlpha() >= 1.0) {
				continue;
			}
			gn.setFilter(new PdfGraphicsNodeRable(gn, this.forceVector));
		}
	}
}
