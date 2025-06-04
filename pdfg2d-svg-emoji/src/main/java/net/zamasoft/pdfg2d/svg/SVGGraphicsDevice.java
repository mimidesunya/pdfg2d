package net.zamasoft.pdfg2d.svg;

import java.awt.GraphicsConfigTemplate;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;

class SVGGraphicsDevice extends GraphicsDevice {

	private final GraphicsConfiguration config;

	SVGGraphicsDevice(SVGGraphicsConfiguration config) {
		this.config = config;
	}

	public GraphicsConfiguration getBestConfiguration(GraphicsConfigTemplate gct) {
		return this.config;
	}

	public GraphicsConfiguration[] getConfigurations() {
		return new GraphicsConfiguration[] { this.config };
	}

	public GraphicsConfiguration getDefaultConfiguration() {
		return this.config;
	}

	public String getIDstring() {
		return toString();
	}

	public int getType() {
		return GraphicsDevice.TYPE_PRINTER;
	}

}
