package net.zamasoft.pdfg2d.svg;

import java.awt.geom.Dimension2D;

import org.apache.batik.bridge.UserAgentAdapter;

/**
 * User agent implementation for SVG rendering.
 * 
 * <p>
 * This class extends Batik's {@link UserAgentAdapter} to provide a custom
 * viewport size for SVG document rendering. It enables all standard SVG
 * features.
 * 
 * @since 1.0
 */
public class SVGUserAgent extends UserAgentAdapter {

	/** The viewport size for SVG rendering. */
	protected final Dimension2D viewport;

	/**
	 * Creates a new SVG user agent with the specified viewport.
	 *
	 * @param viewport the viewport dimensions for SVG rendering
	 */
	public SVGUserAgent(final Dimension2D viewport) {
		this.viewport = viewport;
		this.addStdFeatures();
	}

	/**
	 * Returns the viewport size.
	 *
	 * @return the viewport dimensions
	 */
	@Override
	public Dimension2D getViewportSize() {
		return this.viewport;
	}
}