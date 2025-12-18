package net.zamasoft.pdfg2d.font;

import java.awt.geom.GeneralPath;

/**
 * An individual glyph within a font.
 * 
 * @param path       the outline path of the glyph
 * @param charString the raw charstring data for CFF fonts
 * @since 1.0
 * @author <a href="mailto:david@steadystate.co.uk">David Schweinsberg</a>
 */
public record Glyph(GeneralPath path, byte[] charString) {
}
