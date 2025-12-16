package net.zamasoft.pdfg2d.svg;

import java.awt.GraphicsConfigTemplate;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;

/**
 * Graphics device for SVG rendering operations.
 * 
 * <p>
 * This class provides a minimal {@link GraphicsDevice} implementation
 * that wraps an {@link SVGGraphicsConfiguration}. It is classified as a
 * printer-type device.
 * 
 * @since 1.0
 */
class SVGGraphicsDevice extends GraphicsDevice {

	/** The graphics configuration for this device. */
	private final GraphicsConfiguration config;

	/**
	 * Creates a new SVG graphics device.
	 *
	 * @param config the graphics configuration to wrap
	 */
	SVGGraphicsDevice(final SVGGraphicsConfiguration config) {
		this.config = config;
	}

	/**
	 * Returns the best configuration matching the template.
	 *
	 * @param gct the configuration template (ignored)
	 * @return the wrapped configuration
	 */
	@Override
	public GraphicsConfiguration getBestConfiguration(final GraphicsConfigTemplate gct) {
		return this.config;
	}

	/**
	 * Returns all available configurations.
	 *
	 * @return an array containing only the wrapped configuration
	 */
	@Override
	public GraphicsConfiguration[] getConfigurations() {
		return new GraphicsConfiguration[] { this.config };
	}

	/**
	 * Returns the default configuration.
	 *
	 * @return the wrapped configuration
	 */
	@Override
	public GraphicsConfiguration getDefaultConfiguration() {
		return this.config;
	}

	/**
	 * Returns an identifier string for this device.
	 *
	 * @return the toString() representation
	 */
	@Override
	public String getIDstring() {
		return toString();
	}

	/**
	 * Returns the device type.
	 *
	 * @return {@link GraphicsDevice#TYPE_PRINTER}
	 */
	@Override
	public int getType() {
		return GraphicsDevice.TYPE_PRINTER;
	}
}
