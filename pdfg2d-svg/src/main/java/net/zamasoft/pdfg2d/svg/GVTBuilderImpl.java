package net.zamasoft.pdfg2d.svg;

import java.awt.AlphaComposite;
import java.awt.Composite;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Element;

/**
 * Extended GVT builder that adds transparency handling for PDF output.
 * 
 * <p>
 * This class overrides the composite building process to wrap partially
 * transparent graphics nodes with {@link PDFGraphicsNodeRable}, enabling
 * proper transparency rendering in PDF documents.
 * 
 * @since 1.0
 */
public class GVTBuilderImpl extends GVTBuilder {

	/** Whether to force vector output even when rasterization might be used. */
	private final boolean forceVector;

	/**
	 * Creates a new GVT builder with vector forcing option.
	 *
	 * @param forceVector true to force vector output for transparent elements
	 */
	public GVTBuilderImpl(final boolean forceVector) {
		this.forceVector = forceVector;
	}

	/**
	 * Creates a new GVT builder with default settings (no forced vector).
	 */
	public GVTBuilderImpl() {
		this(false);
	}

	/**
	 * Overrides composite building to add transparency handling.
	 * 
	 * <p>
	 * For each child node with partial transparency (AlphaComposite with
	 * SRC_OVER rule and alpha < 1.0), this method wraps the node with a
	 * {@link PDFGraphicsNodeRable} filter to enable proper PDF transparency output.
	 *
	 * @param ctx the bridge context
	 * @param e   the DOM element being processed
	 * @param cgn the composite graphics node being built
	 */
	@Override
	protected void buildComposite(final BridgeContext ctx, final Element e,
			final CompositeGraphicsNode cgn) {
		super.buildComposite(ctx, e, cgn);

		for (int i = 0; i < cgn.size(); ++i) {
			final var gn = (GraphicsNode) cgn.get(i);
			final Composite c = gn.getComposite();

			// Skip non-alpha composites
			if (!(c instanceof final AlphaComposite ac)) {
				continue;
			}

			// Skip opaque or non-SRC_OVER composites
			if (ac.getRule() != AlphaComposite.SRC_OVER || ac.getAlpha() >= 1.0f) {
				continue;
			}

			// Wrap with PDF transparency filter
			gn.setFilter(new PDFGraphicsNodeRable(gn, this.forceVector));
		}
	}
}
