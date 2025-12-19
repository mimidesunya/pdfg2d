package net.zamasoft.pdfg2d.pdf.impl;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.helpers.AttributesImpl;

import net.zamasoft.pdfg2d.font.Font;
import net.zamasoft.pdfg2d.font.FontSource;
import net.zamasoft.pdfg2d.font.FontStore;
import net.zamasoft.pdfg2d.gc.font.FontManager;
import net.zamasoft.pdfg2d.gc.image.Image;
import net.zamasoft.pdfg2d.io.FragmentedOutput;
import net.zamasoft.pdfg2d.io.util.FragmentOutputAdapter;
import net.zamasoft.pdfg2d.io.util.PositionTrackingOutput;
import net.zamasoft.pdfg2d.pdf.Attachment;
import net.zamasoft.pdfg2d.pdf.ObjectRef;
import net.zamasoft.pdfg2d.pdf.PDFFragmentOutput;
import net.zamasoft.pdfg2d.pdf.PDFMetaInfo;
import net.zamasoft.pdfg2d.pdf.PDFNamedGraphicsOutput;
import net.zamasoft.pdfg2d.pdf.PDFNamedOutput;
import net.zamasoft.pdfg2d.pdf.PDFOutput.Destination;
import net.zamasoft.pdfg2d.pdf.PDFPageOutput;
import net.zamasoft.pdfg2d.pdf.PDFWriter;
import net.zamasoft.pdfg2d.pdf.action.Action;
import net.zamasoft.pdfg2d.pdf.font.FontManagerImpl;
import net.zamasoft.pdfg2d.pdf.gc.PDFGroupImage;
import net.zamasoft.pdfg2d.pdf.params.EncryptionParams;
import net.zamasoft.pdfg2d.pdf.params.PDFParams;
import net.zamasoft.pdfg2d.pdf.params.V4EncryptionParams;
import net.zamasoft.pdfg2d.pdf.params.ViewerPreferences;
import net.zamasoft.pdfg2d.pdf.util.encryption.Encryption;
import net.zamasoft.pdfg2d.resolver.Source;
import net.zamasoft.pdfg2d.util.NumberUtils;

/**
 * Implementation of PDFWriter that outputs PDF data.
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class PDFWriterImpl implements PDFWriter, FontStore {

	protected static final Random RND = new Random();

	protected static final int BUFFER_SIZE = 8192;

	private static final byte[] HEADER = { '%', 'P', 'D', 'F', '-' };

	private static final byte[] PDF12 = { '1', '.', '2' };

	private static final byte[] PDF13 = { '1', '.', '3' };

	private static final byte[] PDF14 = { '1', '.', '4' };

	private static final byte[] PDF15 = { '1', '.', '5' };

	private static final byte[] PDF16 = { '1', '.', '6' };

	private static final byte[] PDF17 = { '1', '.', '7' };

	private static final byte[] XMP_PADDING = new byte[80];

	static {
		Arrays.fill(XMP_PADDING, (byte) ' ');
		XMP_PADDING[79] = '\n';
	}

	final FragmentedOutput builder;

	final PDFParams params;

	private FontManagerImpl fontManager = null;

	/** XRef Table. */
	protected final XRefImpl xref;

	/** For generating unique fragment IDs. */
	private int sequence = 0;

	/** Encryption. */
	Encryption encryption = null;

	/** File ID. */
	private final byte[][] fileid;

	/** Main flow. */
	final PDFFragmentOutputImpl mainFlow;

	/** Catalog dictionary flow. */
	final PDFFragmentOutputImpl catalogFlow;

	/** XMP metadata flow. */
	final PDFFragmentOutputImpl xmpmetaFlow;

	/**
	 * Object flows.
	 */
	final PDFFragmentOutputImpl objectsFlow;

	final NameDictionaryFlow nameDict;

	/**
	 * Resources referenced from pages.
	 */
	final ResourceFlow pageResourceFlow;

	final ObjectRef pageResourceRef;

	/**
	 * Common resources for pages and XObjects.
	 */
	final Map<String, ObjectRef> nameToResourceRef = new HashMap<>();

	/**
	 * Resource type and count.
	 */
	private final Map<String, Integer> typeToCount = new HashMap<>();

	private final Map<Object, Object> keyToValue = new HashMap<>();

	/** Pages. */
	private final PagesFlow pages;

	/** Outline. */
	final OutlineFlow outline;

	/** Anchor. */
	final NameTreeFlow fragments;

	/** Attachments. */
	private final NameTreeFlow embeddedFiles;

	/** Images. */
	private final ImageFlow images;

	/** Fonts. */
	private final FontFlow fonts;

	private List<ObjectRef> ocgs = null;

	public PDFWriterImpl(final FragmentedOutput builder, PDFParams params) throws IOException {
		assert builder != null;
		if (builder.supportsPositionInfo()) {
			this.builder = builder;
		} else {
			this.builder = new PositionTrackingOutput(builder);
		}

		if (params == null) {
			params = new PDFParams();
		}
		this.params = params;

		final int id = this.nextId();
		this.builder.addFragment();
		final var out = new FragmentOutputAdapter(this.builder, id);
		this.mainFlow = new PDFFragmentOutputImpl(out, this, id, -1, null);

		// Header
		final PDFParams.Version pdfVersion = this.params.getVersion();
		this.mainFlow.write(HEADER);
		switch (pdfVersion) {
			case V_1_2:
				this.mainFlow.write(PDF12);
				break;

			case V_1_3:
				this.mainFlow.write(PDF13);
				break;

			case V_1_4:
			case V_PDFX1A:
				this.mainFlow.write(PDF14);
				break;

			case V_PDFA1B:
				this.mainFlow.write(PDF14);
				this.mainFlow.lineBreak();
				// PDF/A-1 6.1.2 Marker for binary identification
				this.mainFlow.write('%');
				for (int i = 0; i < 4; ++i) {
					this.mainFlow.write(RND.nextInt(128) + 127);
				}
				break;

			case V_1_5:
				this.mainFlow.write(PDF15);
				break;

			case V_1_6:
				this.mainFlow.write(PDF16);
				break;

			case V_1_7:
				this.mainFlow.write(PDF17);
				break;
			default:
				throw new IllegalStateException();
		}
		this.mainFlow.lineBreak();

		// Start root element (Catalog)
		this.xref = new XRefImpl(this.mainFlow);

		this.mainFlow.startHash();

		this.mainFlow.writeName("Type");
		this.mainFlow.writeName("Catalog");
		this.mainFlow.lineBreak();

		// Version
		if (pdfVersion.v >= PDFParams.Version.V_1_4.v) {
			this.mainFlow.writeName("Version");
			switch (pdfVersion) {
				case V_1_4:
				case V_PDFA1B:
				case V_PDFX1A:
					this.mainFlow.writeName("1.4");
					break;

				case V_1_5:
					this.mainFlow.writeName("1.5");
					break;

				case V_1_6:
					this.mainFlow.writeName("1.6");
					break;

				case V_1_7:
					this.mainFlow.writeName("1.7");
					break;
				default:
					throw new IllegalStateException();
			}
			this.mainFlow.lineBreak();
		}

		// Page Tree
		this.mainFlow.writeName("Pages");
		final ObjectRef rootPageRef = this.xref.nextObjectRef();
		this.mainFlow.writeObjectRef(rootPageRef);
		this.mainFlow.lineBreak();

		// XMP Metadata
		ObjectRef xmpmetaRef = null;
		if (params.getVersion().v >= PDFParams.Version.V_1_4.v) {
			xmpmetaRef = this.xref.nextObjectRef();
			this.mainFlow.writeName("Metadata");
			this.mainFlow.writeObjectRef(xmpmetaRef);
			this.mainFlow.lineBreak();
		}

		// OutputIntents
		ObjectRef outputIntentRef = null;
		if (params.getVersion().v >= PDFParams.Version.V_1_4.v) {
			outputIntentRef = this.xref.nextObjectRef();
			this.mainFlow.writeName("OutputIntents");
			this.mainFlow.startArray();
			this.mainFlow.writeObjectRef(outputIntentRef);
			this.mainFlow.endArray();
			this.mainFlow.lineBreak();
		}

		// Inside Catalog
		this.catalogFlow = this.mainFlow.forkFragment();

		// File ID
		byte[] fileId = params.getFileId();
		if (fileId == null) {
			fileId = new byte[16];
			RND.nextBytes(fileId);
		}
		this.fileid = new byte[][] { fileId, fileId };

		// End Catalog
		this.mainFlow.endHash();
		this.mainFlow.endObject();

		// Encryption
		final EncryptionParams encryptionParams = this.params.getEncryption();
		if (encryptionParams != null) {
			if (pdfVersion == PDFParams.Version.V_PDFA1B) {
				throw new IllegalArgumentException("Encryption cannot be used in PDF/A-1.");
			}
			final EncryptionParams.Type encType = encryptionParams.getType();
			if (encType == EncryptionParams.Type.V2 && pdfVersion.v < PDFParams.Version.V_1_3.v) {
				throw new IllegalArgumentException("V2 encryption requires PDF 1.3 or later.");
			}
			if (encType == EncryptionParams.Type.V4) {
				if (pdfVersion.v < PDFParams.Version.V_1_5.v) {
					throw new IllegalArgumentException("V4 encryption requires PDF 1.5 or later.");
				}
				if (((V4EncryptionParams) encryptionParams).getCFM() == V4EncryptionParams.CFM.AESV2) {
					if (pdfVersion.v < PDFParams.Version.V_1_6.v) {
						throw new IllegalArgumentException("AESV2 encryption requires PDF 1.6 or later.");
					}
				}
			}

			this.encryption = new Encryption(this.mainFlow, this.xref, this.fileid, encryptionParams);
		}

		// Page Tree
		this.pages = new PagesFlow(this, rootPageRef);

		// XMP Metadata
		if (xmpmetaRef != null) {
			this.xmpmetaFlow = this.mainFlow.forkFragment();
			this.xmpmetaFlow.startObject(xmpmetaRef);
		} else {
			this.xmpmetaFlow = null;
		}

		// OutputIntents
		if (outputIntentRef != null) {
			this.mainFlow.startObject(outputIntentRef);
			this.mainFlow.startHash();
			this.mainFlow.writeName("Type");
			this.mainFlow.writeName("OutputIntent");
			this.mainFlow.lineBreak();

			this.mainFlow.writeName("S");
			if (params.getVersion() == PDFParams.Version.V_PDFA1B) {
				this.mainFlow.writeName("GTS_PDFA1");
			} else {
				this.mainFlow.writeName("GTS_PDFX");
			}
			this.mainFlow.lineBreak();

			String iccName, iccFile;
			int colors;
			if (pdfVersion == PDFParams.Version.V_PDFX1A) {
				iccName = "Probe Profile";
				iccFile = "Probev1_ICCv2.icc";
				colors = 4;
			} else {
				iccName = "sRGB IEC61966-2.1";
				iccFile = "sRGB_IEC61966-2-1_no_black_scaling.icc";
				colors = 3;
			}

			this.mainFlow.writeName("OutputConditionIdentifier");
			this.mainFlow.writeString(iccName);
			this.mainFlow.lineBreak();

			final ObjectRef profRef = this.xref.nextObjectRef();
			this.mainFlow.writeName("DestOutputProfile");
			this.mainFlow.writeObjectRef(profRef);
			this.mainFlow.lineBreak();

			this.mainFlow.endHash();
			this.mainFlow.endObject();

			this.mainFlow.startObject(profRef);
			this.mainFlow.startHash();

			this.mainFlow.writeName("N");
			this.mainFlow.writeInt(colors);
			this.mainFlow.lineBreak();

			try (final OutputStream pout = this.mainFlow.startStreamFromHash(PDFFragmentOutput.Mode.BINARY);
					final InputStream in = PDFWriterImpl.class.getResourceAsStream(iccFile)) {
				final byte[] buff = this.mainFlow.getBuff();
				for (int len = in.read(buff); len != -1; len = in.read(buff)) {
					pout.write(buff, 0, len);
				}
			}
			this.mainFlow.endObject();
		}

		// Outline Info
		if (this.params.isBookmarks()) {
			this.outline = new OutlineFlow(this);
		} else {
			this.outline = null;
		}

		// Name Dictionary
		this.nameDict = new NameDictionaryFlow(this);

		// Fragments
		this.fragments = new NameTreeFlow(this, "Dests") {
			protected void writeEntry(final Object entry) throws IOException {
				this.out.writeDestination((Destination) entry);
			}
		};

		// Attachments
		if (pdfVersion.v >= PDFParams.Version.V_1_4.v && pdfVersion.v != PDFParams.Version.V_PDFA1B.v) {
			this.embeddedFiles = new NameTreeFlow(this, "EmbeddedFiles") {
				protected void writeEntry(final Object entry) throws IOException {
					this.out.startHash();

					this.out.writeName("Type");
					this.out.writeName("Filespec");
					this.out.lineBreak();

					final var spec = (Filespec) entry;
					final var att = spec.attachment();

					this.out.writeName("F");
					this.out.writeFileName(new String[] { spec.name() },
							PDFWriterImpl.this.params.getPlatformEncoding());
					this.out.lineBreak();

					if (pdfVersion.v >= PDFParams.Version.V_1_7.v && att.description() != null) {
						this.out.writeName("UF");
						this.out.writeUTF16(att.description());
						this.out.lineBreak();
					}

					this.out.writeName("EF");
					this.out.startHash();
					this.out.writeName("F");
					this.out.writeObjectRef(spec.ref());
					this.out.endHash();

					this.out.endHash();
				}
			};
		} else {
			this.embeddedFiles = null;
		}

		// Page Resources
		this.pageResourceRef = this.xref.nextObjectRef();
		this.mainFlow.startObject(this.pageResourceRef);
		this.pageResourceFlow = new ResourceFlow(this.mainFlow);
		this.mainFlow.endObject();

		// Objects
		this.objectsFlow = this.mainFlow.forkFragment();
		this.fonts = new FontFlow(this.nameToResourceRef, this.objectsFlow, this.xref);
		this.images = new ImageFlow(this.nameToResourceRef, this.objectsFlow, this.xref, this.params);
	}

	public PDFWriterImpl(final FragmentedOutput builder) throws IOException {
		this(builder, new PDFParams());
	}

	public PDFParams getParams() {
		return this.params;
	}

	public FragmentedOutput getBuilder() {
		return this.builder;
	}

	public Object getAttribute(final Object key) {
		return this.keyToValue.get(key);
	}

	public void putAttribute(final Object key, final Object value) {
		this.keyToValue.put(key, value);
	}

	public FontManager getFontManager() {
		if (this.fontManager == null) {
			this.fontManager = new FontManagerImpl(this.params.getFontSourceManager(), this);
		}
		return this.fontManager;
	}

	protected int nextId() {
		return this.sequence++;
	}

	protected ObjectRef nextOCG() {
		final ObjectRef ocgRef = this.xref.nextObjectRef();
		if (this.ocgs == null) {
			this.ocgs = new ArrayList<>();
		}
		this.ocgs.add(ocgRef);
		return ocgRef;
	}

	/**
	 * Declares use of font.
	 * 
	 * @param source font source
	 * @return font name usable in graphics operations
	 * @throws IOException in case of I/O error
	 */
	public Font useFont(final FontSource source) throws IOException {
		return this.fonts.useFont(source);
	}

	public Image loadImage(final Source source) throws IOException {
		return this.images.loadImage(source);
	}

	public Image addImage(final BufferedImage image) throws IOException {
		return this.images.addImage(image);
	}

	/**
	 * Generates object name.
	 * 
	 * @param type        resource type
	 * @param prefix      name prefix
	 * @param resourceRef resource reference
	 * @return generated name
	 * @throws IOException in case of I/O error
	 */
	protected String addResource(final String type, final String prefix, final ObjectRef resourceRef)
			throws IOException {
		Integer num = this.typeToCount.get(type);
		if (num == null) {
			num = NumberUtils.intValue(0);
		} else {
			num = NumberUtils.intValue(num.intValue() + 1);
		}
		this.typeToCount.put(type, num);
		final String name = prefix + num;
		this.nameToResourceRef.put(name, resourceRef);
		return name;
	}

	public PDFNamedOutput createSpecialGraphicsState() throws IOException {
		final ObjectRef gsRef = this.xref.nextObjectRef();
		final String name = this.addResource("ExtGState", "G", gsRef);
		final PDFFragmentOutputImpl gsOut = this.objectsFlow;
		gsOut.startObject(gsRef);
		gsOut.startHash();

		gsOut.writeName("Type");
		gsOut.writeName("ExtGState");
		gsOut.lineBreak();

		final PDFNamedOutput sgs = new PDFNamedOutput(gsOut, this.params.getPlatformEncoding()) {
			public String getName() {
				return name;
			}

			public void close() throws IOException {
				this.flush();
				gsOut.endHash();
				gsOut.endObject();
			}
		};
		return sgs;
	}

	public PDFGroupImage createGroupImage(final double width, final double height) throws IOException {
		// Group
		if (this.getParams().getVersion().v < PDFParams.Version.V_1_4.v) {
			throw new UnsupportedOperationException("Form Type 1 Group feature requires PDF >= 1.4.");
		}
		final ObjectRef imageRef = this.xref.nextObjectRef();
		final String name = this.addResource("XObject", "T", imageRef);

		final PDFFragmentOutputImpl objectsFlow = this.objectsFlow;

		objectsFlow.startObject(imageRef);
		objectsFlow.startHash();

		objectsFlow.writeName("Type");
		objectsFlow.writeName("XObject");
		objectsFlow.lineBreak();
		objectsFlow.writeName("Subtype");
		objectsFlow.writeName("Form");
		objectsFlow.lineBreak();
		objectsFlow.writeName("FormType");
		objectsFlow.writeInt(1);
		objectsFlow.lineBreak();

		objectsFlow.writeName("Group");
		objectsFlow.startHash();
		objectsFlow.writeName("Type");
		objectsFlow.writeName("Group");
		objectsFlow.writeName("S");
		objectsFlow.writeName("Transparency");
		objectsFlow.endHash();

		objectsFlow.writeName("Resources");
		final ResourceFlow newResourceFlow = new ResourceFlow(objectsFlow);
		objectsFlow.lineBreak();

		// Adjusts Y position to convert bottom-left origin pattern to top-left origin
		// when outputting transform.
		objectsFlow.writeName("Matrix");
		objectsFlow.startArray();
		objectsFlow.writeReal(1 / width);
		objectsFlow.writeReal(0);
		objectsFlow.writeReal(0);
		objectsFlow.writeReal(1 / height);
		objectsFlow.writeReal(0);
		objectsFlow.writeReal(0);
		objectsFlow.endArray();
		objectsFlow.lineBreak();

		objectsFlow.writeName("BBox");
		objectsFlow.startArray();
		objectsFlow.writeInt(0);
		objectsFlow.writeReal(0);
		objectsFlow.writeReal(width);
		objectsFlow.writeReal(height);
		objectsFlow.endArray();
		objectsFlow.lineBreak();

		final PDFFragmentOutput formFlow = objectsFlow.forkFragment();
		final PDFFragmentOutput groupFlow = objectsFlow.forkFragment();
		final OutputStream groupOut = groupFlow.startStreamFromHash(PDFFragmentOutput.Mode.ASCII);
		objectsFlow.endObject();

		return new PDFGroupImageImpl(this, groupOut, groupFlow, newResourceFlow, width, height, name, imageRef,
				formFlow);
	}

	public PDFNamedGraphicsOutput createTilingPattern(final double width, final double height, final double pageHeight,
			final AffineTransform at) throws IOException {
		assert at == null || at.getScaleX() != 0;
		assert at == null || at.getScaleY() != 0;
		// Pattern Object
		final ObjectRef patternRef = this.xref.nextObjectRef();
		final String name = this.addResource("Pattern", "P", patternRef);

		final PDFFragmentOutputImpl objectsFlow = this.objectsFlow;
		objectsFlow.startObject(patternRef);
		objectsFlow.startHash();

		objectsFlow.writeName("Type");
		objectsFlow.writeName("Pattern");
		objectsFlow.lineBreak();

		objectsFlow.writeName("PatternType");
		objectsFlow.writeInt(1);
		objectsFlow.lineBreak();

		objectsFlow.writeName("PaintType");
		objectsFlow.writeInt(1);
		objectsFlow.lineBreak();

		objectsFlow.writeName("Resources");
		final ResourceFlow newResourceFlow = new ResourceFlow(objectsFlow);
		objectsFlow.lineBreak();

		objectsFlow.writeName("TilingType");
		objectsFlow.writeInt(1);
		objectsFlow.lineBreak();

		// Adjusts Y position to convert bottom-left origin pattern to top-left origin
		// when outputting transform.
		objectsFlow.writeName("Matrix");
		objectsFlow.startArray();
		double scx, scy, shx, shy, tx, ty;
		if (at != null) {
			scx = at.getScaleX();
			scy = at.getScaleY();
			shx = at.getShearX();
			shy = at.getShearY();
			tx = at.getTranslateX();
			ty = at.getTranslateY();
		} else {
			scx = 1;
			scy = 1;
			shx = 0;
			shy = 0;
			tx = 0;
			ty = 0;
		}
		objectsFlow.writeReal(scx);
		objectsFlow.writeReal(shy);
		objectsFlow.writeReal(shx);
		objectsFlow.writeReal(scy);
		objectsFlow.writeReal(tx);
		objectsFlow.writeReal(-ty + pageHeight % (height * scy));
		objectsFlow.endArray();
		objectsFlow.lineBreak();

		objectsFlow.writeName("BBox");
		objectsFlow.startArray();
		objectsFlow.writeInt(0);
		objectsFlow.writeReal(0);
		objectsFlow.writeReal(width);
		objectsFlow.writeReal(height);
		objectsFlow.endArray();
		objectsFlow.lineBreak();

		objectsFlow.writeName("XStep");
		objectsFlow.writeReal(width);
		objectsFlow.lineBreak();

		objectsFlow.writeName("YStep");
		objectsFlow.writeReal(height);
		objectsFlow.lineBreak();

		final PDFFragmentOutput patternFlow = objectsFlow.forkFragment();
		final OutputStream patternOut = patternFlow.startStreamFromHash(PDFFragmentOutput.Mode.ASCII);
		objectsFlow.endObject();

		return new PDFNamedGraphicsOutputImpl(this, patternOut, patternFlow, newResourceFlow, width, height, name);
	}

	public PDFNamedOutput createShadingPattern(final double pageHeight, AffineTransform at) throws IOException {
		// Shading Object
		final ObjectRef patternRef = this.xref.nextObjectRef();
		final String name = this.addResource("Pattern", "P", patternRef);

		final PDFFragmentOutputImpl objectsFlow = this.objectsFlow;
		objectsFlow.startObject(patternRef);
		objectsFlow.startHash();

		objectsFlow.writeName("Type");
		objectsFlow.writeName("Pattern");
		objectsFlow.lineBreak();

		objectsFlow.writeName("PatternType");
		objectsFlow.writeInt(2);
		objectsFlow.lineBreak();

		objectsFlow.writeName("Matrix");
		objectsFlow.startArray();
		if (at != null) {
			at = new AffineTransform(at);
		} else {
			at = new AffineTransform();
		}
		at.preConcatenate(new AffineTransform(1, 0, 0, -1, 0, pageHeight));
		final double scx = at.getScaleX();
		final double scy = at.getScaleY();
		final double shx = at.getShearX();
		final double shy = at.getShearY();
		final double tx = at.getTranslateX();
		final double ty = at.getTranslateY();
		objectsFlow.writeReal(scx);
		objectsFlow.writeReal(shy);
		objectsFlow.writeReal(shx);
		objectsFlow.writeReal(scy);
		objectsFlow.writeReal(tx);
		objectsFlow.writeReal(ty);
		objectsFlow.endArray();
		objectsFlow.lineBreak();

		final ObjectRef shadingRef = this.xref.nextObjectRef();
		objectsFlow.writeName("Shading");
		objectsFlow.writeObjectRef(shadingRef);
		objectsFlow.lineBreak();

		objectsFlow.endHash();
		objectsFlow.endObject();

		objectsFlow.startObject(shadingRef);
		objectsFlow.startHash();
		return new PDFNamedOutput(objectsFlow, this.getParams().getPlatformEncoding()) {
			public String getName() {
				return name;
			}

			public void close() throws IOException {
				this.flush();
				objectsFlow.endHash();
				objectsFlow.endObject();
			}
		};
	}

	public OutputStream addAttachment(String name, final Attachment attachment) throws IOException {
		if (attachment.description() == null && name == null) {
			throw new NullPointerException();
		}
		if (this.params.getVersion().v < PDFParams.Version.V_1_4.v) {
			throw new UnsupportedOperationException("File attachment requires PDF 1.4 or later.");
		}
		if (this.params.getVersion() == PDFParams.Version.V_PDFA1B) {
			throw new UnsupportedOperationException("File attachment cannot be used in PDF/A.");
		}
		if (this.params.getVersion() == PDFParams.Version.V_PDFX1A) {
			throw new UnsupportedOperationException("File attachment cannot be used in PDF/X.");
		}

		String desc = attachment.description();
		if (desc == null) {
			desc = name;
		} else if (name == null) {
			name = desc;
		}

		final ObjectRef fileRef = this.xref.nextObjectRef();

		final PDFFragmentOutputImpl objectsFlow = this.objectsFlow;
		objectsFlow.startObject(fileRef);
		objectsFlow.startHash();

		objectsFlow.writeName("Type");
		objectsFlow.writeName("EmbeddedFile");
		objectsFlow.lineBreak();

		if (attachment.mimeType() != null) {
			objectsFlow.writeName("Subtype");
			objectsFlow.writeName(attachment.mimeType());
			objectsFlow.lineBreak();
		}

		final Filespec filespec = new Filespec(attachment, name, fileRef);
		this.embeddedFiles.addEntry(desc, filespec);

		final PDFFragmentOutputImpl paramsFlow = objectsFlow.forkFragment();
		try {
			final MessageDigest md5 = MessageDigest.getInstance("md5");

			final OutputStream out = objectsFlow.startStreamFromHash(PDFFragmentOutput.Mode.BINARY);
			return new FilterOutputStream(out) {
				private int size = 0;

				public void write(byte[] buff, int off, int len) throws IOException {
					this.out.write(buff, off, len);
					md5.update(buff, off, len);
					this.size += len;
				}

				public void write(byte[] buff) throws IOException {
					this.out.write(buff);
					md5.update(buff);
					this.size += buff.length;
				}

				public void write(int b) throws IOException {
					this.out.write(b);
					md5.update((byte) b);
					++this.size;
				}

				public void close() throws IOException {
					this.out.close();
					objectsFlow.endObject();

					paramsFlow.writeName("Params");
					paramsFlow.startHash();

					paramsFlow.writeName("Size");
					paramsFlow.writeInt(this.size);
					paramsFlow.lineBreak();

					paramsFlow.writeName("CheckSum");
					byte[] hash = md5.digest();
					paramsFlow.writeBytes8(hash, 0, hash.length);
					paramsFlow.lineBreak();

					paramsFlow.endHash();
					paramsFlow.close();
				};
			};
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public PDFPageOutput nextPage(final double width, final double height) throws IOException {
		return this.pages.createPage(width, height);
	}

	public void close() throws IOException {
		try {
			// Meta Info
			final PDFMetaInfo info = this.params.getMetaInfo();

			final String author = info.getAuthor();
			final String creator = info.getCreator();
			final String producer = info.getProducer();
			String title = info.getTitle();
			final String subject = info.getSubject();
			final String keywords = info.getKeywords();
			final TimeZone zone = TimeZone.getDefault();
			long create = info.getCreationDate();
			if (create == -1L) {
				create = System.currentTimeMillis();
			}
			long modify = info.getModDate();

			final ObjectRef infoRef = this.xref.nextObjectRef();
			this.objectsFlow.startObject(infoRef);
			this.objectsFlow.startHash();

			if (this.params.getVersion() == PDFParams.Version.V_PDFX1A) {
				if (title == null || title.length() == 0) {
					title = "Untitled";
				}
				this.objectsFlow.writeName("GTS_PDFXVersion");
				this.objectsFlow.writeText("PDF/X-1a:2003");
				this.objectsFlow.lineBreak();
			}

			if (author != null) {
				this.objectsFlow.writeName("Author");
				this.objectsFlow.writeText(author);
				this.objectsFlow.lineBreak();
			}

			this.objectsFlow.writeName("CreationDate");
			this.objectsFlow.writeDate(create, zone);
			this.objectsFlow.lineBreak();

			if (modify == -1L) {
				modify = create;
			}
			this.objectsFlow.writeName("ModDate");
			this.objectsFlow.writeDate(modify, zone);
			this.objectsFlow.lineBreak();

			if (creator != null) {
				this.objectsFlow.writeName("Creator");
				this.objectsFlow.writeText(creator);
				this.objectsFlow.lineBreak();
			}

			if (producer != null) {
				this.objectsFlow.writeName("Producer");
				this.objectsFlow.writeText(producer);
				this.objectsFlow.lineBreak();
			}

			if (title != null) {
				this.objectsFlow.writeName("Title");
				this.objectsFlow.writeText(title);
				this.objectsFlow.lineBreak();
			}

			if (subject != null) {
				this.objectsFlow.writeName("Subject");
				this.objectsFlow.writeText(subject);
				this.objectsFlow.lineBreak();
			}

			if (keywords != null) {
				this.objectsFlow.writeName("Keywords");
				this.objectsFlow.writeText(keywords);
				this.objectsFlow.lineBreak();
			}

			this.objectsFlow.writeName("Trapped");
			this.objectsFlow.writeName("False");
			this.objectsFlow.lineBreak();

			this.objectsFlow.endHash();
			this.objectsFlow.endObject();

			// XML Metadata
			if (this.xmpmetaFlow != null) {
				this.xmpmetaFlow.startHash();

				this.xmpmetaFlow.writeName("Type");
				this.xmpmetaFlow.writeName("Metadata");
				this.xmpmetaFlow.lineBreak();

				this.xmpmetaFlow.writeName("Subtype");
				this.xmpmetaFlow.writeName("XML");
				this.xmpmetaFlow.lineBreak();

				try (final OutputStream xout = this.xmpmetaFlow.startStreamFromHash(PDFFragmentOutput.Mode.RAW)) {
					xout.write("<?xpacket begin='".getBytes("UTF-8"));
					xout.write("\u00EF\u00BB\u00BF".getBytes("ISO-8859-1"));
					xout.write("' id='W5M0MpCehiHzreSzNTczkc9d'?>\n".getBytes("UTF-8"));
					final TransformerHandler handler = ((SAXTransformerFactory) SAXTransformerFactory.newInstance())
							.newTransformerHandler();
					handler.setResult(new StreamResult(xout));
					final Transformer t = handler.getTransformer();
					t.setOutputProperty(OutputKeys.METHOD, "xml");
					t.setOutputProperty(OutputKeys.INDENT, "yes");
					t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
					t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

					final AttributesImpl attsi = new AttributesImpl();
					final String xURI = "adobe:ns:meta/";
					final String rdfURI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
					final String pdfaidURI = "http://www.aiim.org/pdfa/ns/id/";
					final String pdfURI = "http://ns.adobe.com/pdf/1.3/";
					final String dcURI = "http://purl.org/dc/elements/1.1/";
					final String xmpURI = "http://ns.adobe.com/xap/1.0/";

					handler.startDocument();
					attsi.addAttribute("", "x", "xmlns:x", "CDATA", xURI);

					handler.startElement(xURI, "xmpmeta", "x:xmpmeta", attsi);
					attsi.clear();
					attsi.addAttribute("", "rdf", "xmlns:rdf", "CDATA", rdfURI);
					handler.startElement(rdfURI, "RDF", "rdf:RDF", attsi);
					attsi.clear();

					// PDF/A ID
					if (this.params.getVersion() == PDFParams.Version.V_PDFA1B) {
						attsi.addAttribute("", "pdfaid", "xmlns:pdfaid", "CDATA", pdfaidURI);
						attsi.addAttribute(rdfURI, "about", "rdf:about", "CDATA", "");
						handler.startElement(rdfURI, "Description", "rdf:Description", attsi);
						attsi.clear();
						handler.startElement(pdfaidURI, "part", "pdfaid:part", attsi);
						handler.characters("1".toCharArray(), 0, 1);
						handler.endElement(pdfaidURI, "part", "pdfaid:part");
						handler.startElement(pdfaidURI, "conformance", "pdfaid:conformance", attsi);
						handler.characters("A".toCharArray(), 0, 1);
						handler.endElement(pdfaidURI, "conformance", "pdfaid:conformance");
						handler.endElement(rdfURI, "Description", "rdf:Description");
					}

					// PDF
					attsi.addAttribute("", "pdf", "xmlns:pdf", "CDATA", pdfURI);
					attsi.addAttribute(rdfURI, "about", "rdf:about", "CDATA", "");
					handler.startElement(rdfURI, "Description", "rdf:Description", attsi);
					attsi.clear();
					if (keywords != null) {
						handler.startElement(pdfURI, "Keywords", "pdf:Keywords", attsi);
						handler.characters(keywords.toCharArray(), 0, keywords.length());
						handler.endElement(pdfURI, "Keywords", "pdf:Keywords");
					}
					if (producer != null) {
						handler.startElement(pdfURI, "Producer", "pdf:Producer", attsi);
						handler.characters(producer.toCharArray(), 0, producer.length());
						handler.endElement(pdfURI, "Producer", "pdf:Producer");
					}
					handler.endElement(rdfURI, "Description", "rdf:Description");

					// DC
					attsi.addAttribute(rdfURI, "about", "rdf:about", "CDATA", "");
					attsi.addAttribute("", "dc", "xmlns:dc", "CDATA", dcURI);
					handler.startElement(rdfURI, "Description", "rdf:Description", attsi);
					attsi.clear();

					final String format = "application/pdf";
					handler.startElement(dcURI, "format", "dc:format", attsi);
					handler.characters(format.toCharArray(), 0, format.length());
					handler.endElement(dcURI, "format", "dc:format");

					if (title != null) {
						handler.startElement(dcURI, "title", "dc:title", attsi);
						handler.startElement(rdfURI, "Alt", "rdf:Alt", attsi);
						attsi.addAttribute("", "lang", "xml:lang", "CDATA", "x-default");
						handler.startElement(rdfURI, "li", "rdf:li", attsi);
						attsi.clear();
						handler.characters(title.toCharArray(), 0, title.length());
						handler.endElement(rdfURI, "li", "rdf:li");
						handler.endElement(rdfURI, "Alt", "rdf:Alt");
						handler.endElement(dcURI, "title", "dc:title");
					}

					if (author != null) {
						handler.startElement(dcURI, "creator", "dc:creator", attsi);
						handler.startElement(rdfURI, "Seq", "rdf:Seq", attsi);
						handler.startElement(rdfURI, "li", "rdf:li", attsi);
						handler.characters(author.toCharArray(), 0, author.length());
						handler.endElement(rdfURI, "li", "rdf:li");
						handler.endElement(rdfURI, "Seq", "rdf:Seq");
						handler.endElement(dcURI, "creator", "dc:creator");
					}
					attsi.clear();
					handler.endElement(rdfURI, "Description", "rdf:Description");

					// XMP
					attsi.addAttribute("", "xmp", "xmlns:xmp", "CDATA", xmpURI);
					attsi.addAttribute(rdfURI, "about", "rdf:about", "CDATA", "");
					handler.startElement(rdfURI, "Description", "rdf:Description", attsi);
					attsi.clear();
					if (creator != null) {
						handler.startElement(xmpURI, "CreatorTool", "xmp:CreatorTool", attsi);
						handler.characters(creator.toCharArray(), 0, creator.length());
						handler.endElement(xmpURI, "CreatorTool", "xmp:CreatorTool");
					}
					final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
					handler.startElement(xmpURI, "CreateDate", "xmp:CreateDate", attsi);
					String createStr = dateFormat.format(new Date(create));
					createStr = createStr.substring(0, createStr.length() - 2) + ':'
							+ createStr.substring(createStr.length() - 2);
					handler.characters(createStr.toCharArray(), 0, createStr.length());
					handler.endElement(xmpURI, "CreateDate", "xmp:CreateDate");
					if (modify != -1L) {
						handler.startElement(xmpURI, "ModifyDate", "xmp:ModifyDate", attsi);
						String modifyStr = dateFormat.format(new Date(modify));
						modifyStr = modifyStr.substring(0, modifyStr.length() - 2) + ':'
								+ modifyStr.substring(modifyStr.length() - 2);
						handler.characters(modifyStr.toCharArray(), 0, modifyStr.length());
						handler.endElement(xmpURI, "ModifyDate", "xmp:ModifyDate");
					}
					handler.endElement(rdfURI, "Description", "rdf:Description");

					handler.endElement(rdfURI, "RDF", "rdf:RDF");
					handler.endElement(xURI, "xmpmeta", "x:xmpmeta");

					handler.endDocument();
					// XMP (p33) 2-4KB padding
					for (int i = 0; i < 26; ++i) {
						xout.write(XMP_PADDING);
					}
					xout.write("<?xpacket end='w'?>\n".getBytes("UTF-8"));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				this.xmpmetaFlow.endObject();
			}

			// Catalog - Page Info
			this.pages.close();

			// Outline
			if (this.outline != null) {
				this.outline.close();
			}

			// Anchor
			this.fragments.close();

			// Attachments
			if (this.embeddedFiles != null) {
				this.embeddedFiles.close();
			}

			// Name Dictionary
			this.nameDict.close();

			// OCGs
			if (this.ocgs != null) {
				final ObjectRef ref = this.xref.nextObjectRef();
				this.catalogFlow.writeName("OCProperties");
				this.catalogFlow.writeObjectRef(ref);

				this.objectsFlow.startObject(ref);
				this.objectsFlow.startHash();
				this.objectsFlow.writeName("OCGs");
				this.objectsFlow.startArray();
				for (int i = 0; i < this.ocgs.size(); ++i) {
					final ObjectRef ocgRef = (ObjectRef) this.ocgs.get(i);
					this.objectsFlow.writeObjectRef(ocgRef);
				}
				this.objectsFlow.endArray();

				this.objectsFlow.writeName("D");
				this.objectsFlow.startHash();
				this.objectsFlow.writeName("ON");
				this.objectsFlow.startArray();
				for (int i = 0; i < this.ocgs.size(); ++i) {
					final ObjectRef ocgRef = (ObjectRef) this.ocgs.get(i);
					this.objectsFlow.writeObjectRef(ocgRef);
				}
				this.objectsFlow.endArray();
				this.objectsFlow.writeName("AS");
				this.objectsFlow.startArray();

				this.objectsFlow.startHash();
				this.objectsFlow.writeName("Event");
				this.objectsFlow.writeName("View");
				this.objectsFlow.writeName("OCGs");
				this.objectsFlow.startArray();
				for (int i = 0; i < this.ocgs.size(); ++i) {
					final ObjectRef ocgRef = (ObjectRef) this.ocgs.get(i);
					this.objectsFlow.writeObjectRef(ocgRef);
				}
				this.objectsFlow.endArray();
				this.objectsFlow.writeName("Category");
				this.objectsFlow.startArray();
				this.objectsFlow.writeName("View");
				this.objectsFlow.endArray();
				this.objectsFlow.endHash();

				this.objectsFlow.startHash();
				this.objectsFlow.writeName("Event");
				this.objectsFlow.writeName("Print");
				this.objectsFlow.writeName("OCGs");
				this.objectsFlow.startArray();
				for (int i = 0; i < this.ocgs.size(); ++i) {
					final ObjectRef ocgRef = (ObjectRef) this.ocgs.get(i);
					this.objectsFlow.writeObjectRef(ocgRef);
				}
				this.objectsFlow.endArray();
				this.objectsFlow.writeName("Category");
				this.objectsFlow.startArray();
				this.objectsFlow.writeName("Print");
				this.objectsFlow.endArray();
				this.objectsFlow.endHash();

				this.objectsFlow.endArray();
				this.objectsFlow.endHash();

				this.objectsFlow.endHash();
				this.objectsFlow.endObject();
			}

			// ViewerPreferences
			final ViewerPreferences vp = this.params.getViewerPreferences();
			if (vp != null) {
				this.catalogFlow.writeName("ViewerPreferences");
				this.catalogFlow.startHash();

				if (vp.isHideToolbar()) {
					this.catalogFlow.writeName("HideToolbar");
					this.catalogFlow.writeBoolean(true);
					this.catalogFlow.lineBreak();
				}

				if (vp.isHideMenubar()) {
					this.catalogFlow.writeName("HideMenubar");
					this.catalogFlow.writeBoolean(true);
					this.catalogFlow.lineBreak();
				}

				if (vp.isHideWindowUI()) {
					this.catalogFlow.writeName("HideWindowUI");
					this.catalogFlow.writeBoolean(true);
					this.catalogFlow.lineBreak();
				}

				if (vp.isFitWindow()) {
					this.catalogFlow.writeName("FitWindow");
					this.catalogFlow.writeBoolean(true);
					this.catalogFlow.lineBreak();
				}

				if (vp.isCenterWindow()) {
					this.catalogFlow.writeName("CenterWindow");
					this.catalogFlow.writeBoolean(true);
					this.catalogFlow.lineBreak();
				}

				if (vp.isDisplayDocTitle()) {
					if (this.params.getVersion().v < PDFParams.Version.V_1_4.v) {
						throw new UnsupportedOperationException(
								"ViewerPreference DisplayDocTitle requires PDF 1.4 or later.");
					}
					this.catalogFlow.writeName("DisplayDocTitle");
					this.catalogFlow.writeBoolean(true);
					this.catalogFlow.lineBreak();
				}

				if (vp.getNonFullScreenPageMode() != ViewerPreferences.NonFullScreenPageMode.NONE) {
					this.catalogFlow.writeName("NonFullScreenPageMode");
					switch (vp.getNonFullScreenPageMode()) {
						case OUTLINES:
							this.catalogFlow.writeName("UseOutlines");
							break;
						case THUMBS:
							this.catalogFlow.writeName("UseThumbs");
							break;
						case OC:
							this.catalogFlow.writeName("UseOC");
							break;
						case NONE:
							// this.catalogFlow.writeName("UseNone");
							// break;
						default:
							throw new IllegalStateException();
					}
					this.catalogFlow.lineBreak();
				}

				if (vp.getDirection() != ViewerPreferences.Direction.L2R) {
					if (this.params.getVersion().v < PDFParams.Version.V_1_3.v) {
						throw new UnsupportedOperationException(
								"ViewerPreference Direction requires PDF 1.3 or later.");
					}
					this.catalogFlow.writeName("Direction");
					switch (vp.getDirection()) {
						case R2L:
							this.catalogFlow.writeName("R2L");
							break;
						case L2R:
							// this.catalogFlow.writeName("L2R");
							// break;
						default:
							throw new IllegalStateException();
					}
					this.catalogFlow.lineBreak();
				}

				if (vp.getViewArea() != ViewerPreferences.AreaBox.CROP) {
					if (this.params.getVersion().v < PDFParams.Version.V_1_4.v) {
						throw new UnsupportedOperationException(
								"ViewerPreference ViewArea requires PDF 1.4 or later.");
					}
					this.catalogFlow.writeName("ViewArea");
					this.writeArea(vp.getViewArea());
					this.catalogFlow.lineBreak();
				}

				if (vp.getViewClip() != ViewerPreferences.AreaBox.CROP) {
					if (this.params.getVersion().v < PDFParams.Version.V_1_4.v) {
						throw new UnsupportedOperationException(
								"ViewerPreference ViewClip requires PDF 1.4 or later.");
					}
					this.catalogFlow.writeName("ViewClip");
					this.writeArea(vp.getViewClip());
					this.catalogFlow.lineBreak();
				}

				if (vp.getPrintArea() != ViewerPreferences.AreaBox.CROP) {
					if (this.params.getVersion().v < PDFParams.Version.V_1_4.v) {
						throw new UnsupportedOperationException(
								"ViewerPreference PrintArea requires PDF 1.4 or later.");
					}
					this.catalogFlow.writeName("PrintArea");
					this.writeArea(vp.getPrintArea());
					this.catalogFlow.lineBreak();
				}

				if (vp.getPrintClip() != ViewerPreferences.AreaBox.CROP) {
					if (this.params.getVersion().v < PDFParams.Version.V_1_4.v) {
						throw new UnsupportedOperationException(
								"ViewerPreference PrintClip requires PDF 1.4 or later.");
					}
					this.catalogFlow.writeName("PrintClip");
					this.writeArea(vp.getPrintClip());
					this.catalogFlow.lineBreak();
				}

				if (vp.getPrintScaling() != ViewerPreferences.PrintScaling.APP_DEFAULT) {
					if (this.params.getVersion().v < PDFParams.Version.V_1_6.v) {
						throw new UnsupportedOperationException(
								"ViewerPreference PrintScaling requires PDF 1.6 or later.");
					}
					this.catalogFlow.writeName("PrintScaling");
					switch (vp.getPrintScaling()) {
						case NONE:
							this.catalogFlow.writeName("None");
							break;
						case APP_DEFAULT:
							// this.catalogFlow.writeName("AppDefault");
							// break;
						default:
							throw new IllegalStateException();
					}
					this.catalogFlow.lineBreak();
				}

				if (vp.getDuplex() != ViewerPreferences.Duplex.NONE) {
					if (this.params.getVersion().v < PDFParams.Version.V_1_7.v) {
						throw new UnsupportedOperationException(
								"ViewerPreference Duplex requires PDF 1.7 or later.");
					}
					this.catalogFlow.writeName("Duplex");
					switch (vp.getDuplex()) {
						case SIMPLEX:
							this.catalogFlow.writeName("Simplex");
							break;
						case FLIP_SHORT_EDGE:
							this.catalogFlow.writeName("DuplexFlipShortEdge");
							break;
						case FLIP_LONG_EDGE:
							this.catalogFlow.writeName("DuplexFlipLongEdge");
							break;
						case NONE:
							// break;
						default:
							throw new IllegalStateException();
					}
					this.catalogFlow.lineBreak();
				}

				if (vp.getPickTrayByPDFSize()) {
					if (this.params.getVersion().v < PDFParams.Version.V_1_7.v) {
						throw new UnsupportedOperationException(
								"ViewerPreference PickTrayByPDFSize requires PDF 1.7 or later.");
					}
					this.catalogFlow.writeName("PickTrayByPDFSize");
					this.catalogFlow.writeBoolean(true);
					this.catalogFlow.lineBreak();
				}

				final int[] printPageRange = vp.getPrintPageRange();
				if (printPageRange != null) {
					if (this.params.getVersion().v < PDFParams.Version.V_1_7.v) {
						throw new UnsupportedOperationException(
								"ViewerPreference PrintPageRange requires PDF 1.7 or later.");
					}
					this.catalogFlow.writeName("PrintPageRange");
					this.catalogFlow.startArray();
					for (int i = 0; i < printPageRange.length; ++i) {
						this.catalogFlow.writeInt(printPageRange[i]);
					}
					this.catalogFlow.endArray();
					this.catalogFlow.lineBreak();
				}

				final int numCopies = vp.getNumCopies();
				if (numCopies > 0) {
					if (this.params.getVersion().v < PDFParams.Version.V_1_7.v) {
						throw new UnsupportedOperationException(
								"ViewerPreference NumCopies requires PDF 1.7 or later.");
					}
					this.catalogFlow.writeName("NumCopies");
					this.catalogFlow.writeInt(numCopies);
					this.catalogFlow.lineBreak();
				}

				this.catalogFlow.endHash();

				this.catalogFlow.lineBreak();
			}

			// Open Action
			final Action action = this.params.getOpenAction();
			if (action != null) {
				this.catalogFlow.writeName("OpenAction");
				this.catalogFlow.startHash();
				action.writeTo(this.catalogFlow);
				this.catalogFlow.endHash();
				this.catalogFlow.lineBreak();
			}

			// Catalog
			this.catalogFlow.close();

			// Resources
			this.fonts.close();
			this.pageResourceFlow.close();
			this.objectsFlow.close();

			// XRef
			this.xref.close(this.builder.getPositionInfo(), infoRef, this.fileid, this.encryption);

			this.mainFlow.close();
		} finally {
			this.builder.close();
		}
		if (this.fontManager != null) {
			this.fontManager.close();
		}
	}

	private void writeArea(final ViewerPreferences.AreaBox area) throws IOException {
		switch (area) {
			case MEDIA:
				this.catalogFlow.writeName("MediaBox");
				break;
			case CROP:
				// this.catalogFlow.writeName("CropBox");
				break;
			case BLEED:
				this.catalogFlow.writeName("BleedBox");
				break;
			case TRIM:
				this.catalogFlow.writeName("TrimBox");
				break;
			case ART:
				this.catalogFlow.writeName("ArtBox");
				break;
			default:
				throw new IllegalStateException();
		}
	}
}
