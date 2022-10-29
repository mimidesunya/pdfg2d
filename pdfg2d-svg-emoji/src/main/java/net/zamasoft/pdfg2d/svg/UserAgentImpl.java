package net.zamasoft.pdfg2d.svg;

import java.awt.geom.Dimension2D;

import org.apache.batik.bridge.UserAgentAdapter;

public class UserAgentImpl extends UserAgentAdapter {
	protected final Dimension2D viewport;

	public UserAgentImpl(Dimension2D viewport) {
		this.viewport = viewport;
		this.addStdFeatures();
	}

	public Dimension2D getViewportSize() {
		return this.viewport;
	}
}