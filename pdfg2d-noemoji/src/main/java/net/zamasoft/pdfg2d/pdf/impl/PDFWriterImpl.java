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

import jp.cssj.resolver.Source;
import jp.cssj.rsr.RandomBuilder;
import jp.cssj.rsr.helpers.RandomBuilderOutputStream;
import jp.cssj.rsr.helpers.RandomBuilderPositionSupport;
import net.zamasoft.pdfg2d.font.Font;
import net.zamasoft.pdfg2d.font.FontSource;
import net.zamasoft.pdfg2d.font.FontStore;
import net.zamasoft.pdfg2d.gc.font.FontManager;
import net.zamasoft.pdfg2d.gc.image.Image;
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
import net.zamasoft.pdfg2d.util.NumberUtils;

/**
 * PDFデータを出力します。
 * 
 * @author MIYABE Tatsuhiko
 * @since 1.0
 */
public class PDFWriterImpl implements PDFWriter, FontStore {
	// private static final Log LOG = LogFactory.getLog(PDFWriterImpl.class
	// .getName());

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
	{
		for (int i = 0; i < 79; ++i) {
			XMP_PADDING[i] = ' ';
		}
		XMP_PADDING[79] = '\n';
	}

	final RandomBuilder builder;

	final PDFParams params;

	private FontManagerImpl fontManager = null;

	/** クロスリファレンステーブル。 */
	protected final XRefImpl xref;

	/** ユニークな断片IDを生成するため。 */
	private int sequence = 0;

	/** 暗号化。 */
	Encryption encryption = null;

	/** ファイルID。 */
	private final byte[][] fileid;

	/** 主フロー。 */
	final PDFFragmentOutputImpl mainFlow;

	/** カタログディクショナリフロー。 */
	final PDFFragmentOutputImpl catalogFlow;

	/** XMPメタデータフロー。 */
	final PDFFragmentOutputImpl xmpmetaFlow;

	/**
	 * 各種オブジェクトフロー。
	 */
	final PDFFragmentOutputImpl objectsFlow;

	final NameDictionaryFlow nameDict;

	/**
	 * ページから参照されるリソース。
	 */
	final ResourceFlow pageResourceFlow;

	final ObjectRef pageResourceRef;

	/**
	 * ページとXObjectの共通リソース。
	 */
	final Map<String, ObjectRef> nameToResourceRef = new HashMap<String, ObjectRef>();

	/**
	 * リソースタイプとカウント。
	 */
	private final Map<String, Integer> typeToCount = new HashMap<String, Integer>();

	private final Map<Object, Object> keyToValue = new HashMap<Object, Object>();

	/** 各ページ。 */
	private final PagesFlow pages;

	/** アウトライン。 */
	final OutlineFlow outline;

	/** アンカー。 */
	final NameTreeFlow fragments;

	/** 添付ファイル。 */
	private final NameTreeFlow embeddedFiles;

	/** 画像。 */
	private final ImageFlow images;

	/** フォント。 */
	private final FontFlow fonts;

	private List<ObjectRef> ocgs = null;

	public PDFWriterImpl(RandomBuilder builder, PDFParams params) throws IOException {
		assert builder != null;
		if (builder.supportsPositionInfo()) {
			this.builder = builder;
		} else {
			this.builder = new RandomBuilderPositionSupport(builder);
		}

		if (params == null) {
			params = new PDFParams();
		}
		this.params = params;

		int id = this.nextId();
		this.builder.addBlock();
		OutputStream out = new RandomBuilderOutputStream(this.builder, id);
		this.mainFlow = new PDFFragmentOutputImpl(out, this, id, -1, null);

		// ヘッダ
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
			// PDF/A-1 6.1.2 バイナリと識別するためのマーカ
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

		// ルート要素（カタログ）の開始
		this.xref = new XRefImpl(this.mainFlow);

		this.mainFlow.startHash();

		this.mainFlow.writeName("Type");
		this.mainFlow.writeName("Catalog");
		this.mainFlow.lineBreak();

		// バージョン
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

		// ページツリー
		this.mainFlow.writeName("Pages");
		ObjectRef rootPageRef = this.xref.nextObjectRef();
		this.mainFlow.writeObjectRef(rootPageRef);
		this.mainFlow.lineBreak();

		// XMPメタデータ
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

		// Tagged PDF
		/*
		 * if (params.getVersion() >= PdfParams.VERSION_1_4) {
		 * this.mainFlow.writeName("MarkInfo"); this.mainFlow.startHash();
		 * this.mainFlow.writeName("Marked"); this.mainFlow.writeBoolean(true);
		 * this.mainFlow.endHash(); this.mainFlow.lineBreak();
		 * 
		 * this.mainFlow.writeName("StructTreeRoot"); this.mainFlow.startHash();
		 * this.mainFlow.writeName("Type"); this.mainFlow.writeName("StructTreeRoot");
		 * this.mainFlow.endHash(); this.mainFlow.lineBreak(); }
		 */

		// カタログ内部
		this.catalogFlow = this.mainFlow.forkFragment();

		// ファイルID
		byte[] fileId = params.getFileId();
		if (fileId == null) {
			fileId = new byte[16];
			synchronized (RND) {
				RND.nextBytes(fileId);
			}
		}
		this.fileid = new byte[][] { fileId, fileId };

		// // カタログの終了
		this.mainFlow.endHash();
		this.mainFlow.endObject();

		// 暗号化
		EncryptionParams encriptionParams = this.params.getEncription();
		if (encriptionParams != null) {
			if (pdfVersion == PDFParams.Version.V_PDFA1B) {
				throw new IllegalArgumentException("PDF/A-1では暗号化は使用できません。");
			}
			EncryptionParams.Type encType = encriptionParams.getType();
			if (encType == EncryptionParams.Type.V2 && pdfVersion.v < PDFParams.Version.V_1_3.v) {
				throw new IllegalArgumentException("V2暗号化はPDF 1.3以降で使用できます。");
			}
			if (encType == EncryptionParams.Type.V4) {
				if (pdfVersion.v < PDFParams.Version.V_1_5.v) {
					throw new IllegalArgumentException("V4暗号化はPDF 1.5以降で使用できます。");
				}
				if (((V4EncryptionParams) encriptionParams).getCFM() == V4EncryptionParams.CFM.AESV2) {
					if (pdfVersion.v < PDFParams.Version.V_1_6.v) {
						throw new IllegalArgumentException("AESV2暗号化はPDF 1.6以降で使用できます。");
					}
				}
			}

			this.encryption = new Encryption(this.mainFlow, this.xref, this.fileid, encriptionParams);
		}

		// ページツリー
		this.pages = new PagesFlow(this, rootPageRef);

		// XMPメタデータ
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

			ObjectRef profRef = this.xref.nextObjectRef();
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

			try (OutputStream pout = this.mainFlow.startStreamFromHash(PDFFragmentOutput.Mode.BINARY);
					InputStream in = PDFWriterImpl.class.getResourceAsStream(iccFile)) {
				byte[] buff = this.mainFlow.getBuff();
				for (int len = in.read(buff); len != -1; len = in.read(buff)) {
					pout.write(buff, 0, len);
				}
			}
			this.mainFlow.endObject();
		}

		// アウトライン情報
		if (this.params.isBookmarks()) {
			this.outline = new OutlineFlow(this);
		} else {
			this.outline = null;
		}

		// // ネームディクショナリ
		this.nameDict = new NameDictionaryFlow(this);

		// フラグメント
		this.fragments = new NameTreeFlow(this, "Dests") {
			protected void writeEntry(Object entry) throws IOException {
				this.out.writeDestination((Destination) entry);
			}
		};

		// 添付ファイル
		if (pdfVersion.v >= PDFParams.Version.V_1_4.v && pdfVersion.v != PDFParams.Version.V_PDFA1B.v) {
			this.embeddedFiles = new NameTreeFlow(this, "EmbeddedFiles") {
				protected void writeEntry(Object entry) throws IOException {
					this.out.startHash();

					this.out.writeName("Type");
					this.out.writeName("Filespec");
					this.out.lineBreak();

					Filespec spec = (Filespec) entry;
					Attachment att = spec.attachment;

					this.out.writeName("F");
					this.out.writeFileName(new String[] { spec.name }, PDFWriterImpl.this.params.getPlatformEncoding());
					this.out.lineBreak();

					if (pdfVersion.v >= PDFParams.Version.V_1_7.v && att.description != null) {
						this.out.writeName("UF");
						this.out.writeUTF16(att.description);
						this.out.lineBreak();
					}

					this.out.writeName("EF");
					this.out.startHash();
					this.out.writeName("F");
					this.out.writeObjectRef(spec.ref);
					this.out.endHash();

					this.out.endHash();
				}
			};
		} else {
			this.embeddedFiles = null;
		}

		// ページリソース
		this.pageResourceRef = this.xref.nextObjectRef();
		this.mainFlow.startObject(this.pageResourceRef);
		this.pageResourceFlow = new ResourceFlow(this.mainFlow);
		this.mainFlow.endObject();

		// オブジェクト
		this.objectsFlow = this.mainFlow.forkFragment();
		this.fonts = new FontFlow(this.nameToResourceRef, this.objectsFlow, this.xref);
		this.images = new ImageFlow(this.nameToResourceRef, this.objectsFlow, this.xref, this.params);
	}

	public PDFWriterImpl(RandomBuilder builder) throws IOException {
		this(builder, new PDFParams());
	}

	public PDFParams getParams() {
		return this.params;
	}

	public RandomBuilder getBuilder() {
		return this.builder;
	}

	public Object getAttribute(Object key) {
		return this.keyToValue.get(key);
	}

	public void putAttribute(Object key, Object value) {
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
		ObjectRef ocgRef = this.xref.nextObjectRef();
		if (this.ocgs == null) {
			this.ocgs = new ArrayList<ObjectRef>();
		}
		this.ocgs.add(ocgRef);
		return ocgRef;
	}

	/**
	 * フォントの使用を宣言します。
	 * 
	 * @param source
	 * @return グラフィック命令から参照可能なフォント名。
	 * @throws IOException
	 */
	public Font useFont(FontSource source) throws IOException {
		return this.fonts.useFont(source);
	}

	public Image loadImage(Source source) throws IOException {
		return this.images.loadImage(source);
	}

	public Image addImage(BufferedImage image) throws IOException {
		return this.images.addImage(image);
	}

	/**
	 * オブジェクトの名前を生成します。
	 * 
	 * @param type
	 * @param prefix
	 * @param resourceRef
	 * @return
	 * @throws IOException
	 */
	protected String addResource(String type, String prefix, ObjectRef resourceRef) throws IOException {
		Integer num = (Integer) this.typeToCount.get(type);
		if (num == null) {
			num = NumberUtils.intValue(0);
		} else {
			num = NumberUtils.intValue(num.intValue() + 1);
		}
		this.typeToCount.put(type, num);
		String name = prefix + num;
		this.nameToResourceRef.put(name, resourceRef);
		return name;
	}

	public PDFNamedOutput createSpecialGraphicsState() throws IOException {
		ObjectRef gsRef = this.xref.nextObjectRef();
		final String name = this.addResource("ExtGState", "G", gsRef);
		final PDFFragmentOutputImpl gsOut = this.objectsFlow;
		gsOut.startObject(gsRef);
		gsOut.startHash();

		gsOut.writeName("Type");
		gsOut.writeName("ExtGState");
		gsOut.lineBreak();

		PDFNamedOutput sgs = new PDFNamedOutput(gsOut, this.params.getPlatformEncoding()) {
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

	public PDFGroupImage createGroupImage(double width, double height) throws IOException {
		// グループ
		if (this.getParams().getVersion().v < PDFParams.Version.V_1_4.v) {
			throw new UnsupportedOperationException("Form Type 1 Group feature requres PDF >= 1.4.");
		}
		ObjectRef imageRef = this.xref.nextObjectRef();
		String name = this.addResource("XObject", "T", imageRef);

		PDFFragmentOutputImpl objectsFlow = this.objectsFlow;

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
		ResourceFlow newResourceFlow = new ResourceFlow(objectsFlow);
		objectsFlow.lineBreak();

		// 変換を出力する際、
		// ページ下部基点のパターンをページ上部基点にするためにY位置を調整しています。
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

		PDFFragmentOutput formFlow = objectsFlow.forkFragment();
		PDFFragmentOutput groupFlow = objectsFlow.forkFragment();
		OutputStream groupOut = groupFlow.startStreamFromHash(PDFFragmentOutput.Mode.ASCII);
		objectsFlow.endObject();

		return new PDFGroupImageImpl(this, groupOut, groupFlow, newResourceFlow, width, height, name, imageRef,
				formFlow);
	}

	public PDFNamedGraphicsOutput createTilingPattern(double width, double height, double pageHeight,
			AffineTransform at) throws IOException {
		assert at == null || at.getScaleX() != 0;
		assert at == null || at.getScaleY() != 0;
		// パターンオブジェクト
		ObjectRef patternRef = this.xref.nextObjectRef();
		String name = this.addResource("Pattern", "P", patternRef);

		PDFFragmentOutputImpl objectsFlow = this.objectsFlow;
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
		ResourceFlow newResourceFlow = new ResourceFlow(objectsFlow);
		objectsFlow.lineBreak();

		objectsFlow.writeName("TilingType");
		objectsFlow.writeInt(1);
		objectsFlow.lineBreak();

		// 変換を出力する際、
		// ページ下部基点のパターンをページ上部基点にするためにY位置を調整しています。
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

		PDFFragmentOutput patternFlow = objectsFlow.forkFragment();
		OutputStream patternOut = patternFlow.startStreamFromHash(PDFFragmentOutput.Mode.ASCII);
		objectsFlow.endObject();

		return new PDFNamedGraphicsOutputImpl(this, patternOut, patternFlow, newResourceFlow, width, height, name);
	}

	public PDFNamedOutput createShadingPattern(double pageHeight, AffineTransform at) throws IOException {
		// シェーディングオブジェクト
		ObjectRef patternRef = this.xref.nextObjectRef();
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
		double scx = at.getScaleX();
		double scy = at.getScaleY();
		double shx = at.getShearX();
		double shy = at.getShearY();
		double tx = at.getTranslateX();
		double ty = at.getTranslateY();
		objectsFlow.writeReal(scx);
		objectsFlow.writeReal(shy);
		objectsFlow.writeReal(shx);
		objectsFlow.writeReal(scy);
		objectsFlow.writeReal(tx);
		objectsFlow.writeReal(ty);
		objectsFlow.endArray();
		objectsFlow.lineBreak();

		ObjectRef shadingRef = this.xref.nextObjectRef();
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

	public OutputStream addAttachment(String name, Attachment attachment) throws IOException {
		if (attachment.description == null && name == null) {
			throw new NullPointerException();
		}
		if (this.params.getVersion().v < PDFParams.Version.V_1_4.v) {
			throw new UnsupportedOperationException("ファイルの添付は PDF 1.4 以降で使用できます。");
		}
		if (this.params.getVersion() == PDFParams.Version.V_PDFA1B) {
			throw new UnsupportedOperationException("ファイルの添付は PDF/A では利用できません。");
		}
		if (this.params.getVersion() == PDFParams.Version.V_PDFX1A) {
			throw new UnsupportedOperationException("ファイルの添付は PDF/X では利用できません。");
		}

		String desc = attachment.description;
		if (desc == null) {
			desc = name;
		} else if (name == null) {
			name = desc;
		}

		ObjectRef fileRef = this.xref.nextObjectRef();

		final PDFFragmentOutputImpl objectsFlow = this.objectsFlow;
		objectsFlow.startObject(fileRef);
		objectsFlow.startHash();

		objectsFlow.writeName("Type");
		objectsFlow.writeName("EmbeddedFile");
		objectsFlow.lineBreak();

		if (attachment.mimeType != null) {
			objectsFlow.writeName("Subtype");
			objectsFlow.writeName(attachment.mimeType);
			objectsFlow.lineBreak();
		}

		Filespec filespac = new Filespec(attachment, name, fileRef);
		this.embeddedFiles.addEntry(desc, filespac);

		final PDFFragmentOutputImpl paramsFlow = objectsFlow.forkFragment();
		try {
			final MessageDigest md5 = MessageDigest.getInstance("md5");

			OutputStream out = objectsFlow.startStreamFromHash(PDFFragmentOutput.Mode.BINARY);
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

	/**
	 * ページを作成します。
	 * <p>
	 * このメソッドが返すPDFPageOutputは、ページの内容を書き出した後必ずクローズしてください。
	 * </p>
	 * 
	 * @param width
	 * @param height
	 * @return
	 * @throws IOException
	 */
	public PDFPageOutput nextPage(double width, double height) throws IOException {
		return this.pages.createPage(width, height);
	}

	public void close() throws IOException {
		try {
			// メタ情報
			PDFMetaInfo info = this.params.getMetaInfo();

			String author = info.getAuthor();
			String creator = info.getCreator();
			String producer = info.getProducer();
			String title = info.getTitle();
			String subject = info.getSubject();
			String keywords = info.getKeywords();
			TimeZone zone = TimeZone.getDefault();
			long create = info.getCreationDate();
			if (create == -1L) {
				create = System.currentTimeMillis();
			}
			long modify = info.getModDate();

			ObjectRef infoRef = this.xref.nextObjectRef();
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

			// XMLメタデータ
			if (this.xmpmetaFlow != null) {
				this.xmpmetaFlow.startHash();

				this.xmpmetaFlow.writeName("Type");
				this.xmpmetaFlow.writeName("Metadata");
				this.xmpmetaFlow.lineBreak();

				this.xmpmetaFlow.writeName("Subtype");
				this.xmpmetaFlow.writeName("XML");
				this.xmpmetaFlow.lineBreak();

				try (OutputStream xout = this.xmpmetaFlow.startStreamFromHash(PDFFragmentOutput.Mode.RAW)) {
					xout.write("<?xpacket begin='".getBytes("UTF-8"));
					xout.write("\u00EF\u00BB\u00BF".getBytes("ISO-8859-1"));
					xout.write("' id='W5M0MpCehiHzreSzNTczkc9d'?>\n".getBytes("UTF-8"));
					TransformerHandler handler = ((SAXTransformerFactory) SAXTransformerFactory.newInstance())
							.newTransformerHandler();
					handler.setResult(new StreamResult(xout));
					Transformer t = handler.getTransformer();
					t.setOutputProperty(OutputKeys.METHOD, "xml");
					t.setOutputProperty(OutputKeys.INDENT, "yes");
					t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
					t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

					AttributesImpl attsi = new AttributesImpl();
					String xURI = "adobe:ns:meta/";
					String rdfURI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
					String pdfaidURI = "http://www.aiim.org/pdfa/ns/id/";
					String pdfURI = "http://ns.adobe.com/pdf/1.3/";
					String dcURI = "http://purl.org/dc/elements/1.1/";
					String xmpURI = "http://ns.adobe.com/xap/1.0/";

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

					String format = "application/pdf";
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
					/*
					 * if (subject != null) { handler.startElement(dcURI, "subject", "dc:subject",
					 * attsi); handler.startElement(rdfURI, "Bag", "rdf:Bag", attsi);
					 * handler.startElement(rdfURI, "li", "rdf:li", attsi);
					 * handler.characters(subject.toCharArray(), 0, subject.length());
					 * handler.endElement(rdfURI, "li", "rdf:li"); handler.endElement(rdfURI, "Bag",
					 * "rdf:Bag"); handler.endElement(dcURI, "subject", "dc:subject"); }
					 */
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
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
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
					// XMP (p33) 2-4KBの余白
					for (int i = 0; i < 26; ++i) {
						xout.write(XMP_PADDING);
					}
					xout.write("<?xpacket end='w'?>\n".getBytes("UTF-8"));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				this.xmpmetaFlow.endObject();
			}

			// // カタログ
			// ページ情報
			this.pages.close();

			// アウトライン
			if (this.outline != null) {
				this.outline.close();
			}

			// アンカー
			this.fragments.close();

			// 添付ファイル
			if (this.embeddedFiles != null) {
				this.embeddedFiles.close();
			}

			// ネームディクショナリ
			this.nameDict.close();

			// OCGs
			if (this.ocgs != null) {
				ObjectRef ref = this.xref.nextObjectRef();
				this.catalogFlow.writeName("OCProperties");
				this.catalogFlow.writeObjectRef(ref);

				this.objectsFlow.startObject(ref);
				this.objectsFlow.startHash();
				this.objectsFlow.writeName("OCGs");
				this.objectsFlow.startArray();
				for (int i = 0; i < this.ocgs.size(); ++i) {
					ObjectRef ocgRef = (ObjectRef) this.ocgs.get(i);
					this.objectsFlow.writeObjectRef(ocgRef);
				}
				this.objectsFlow.endArray();

				this.objectsFlow.writeName("D");
				this.objectsFlow.startHash();
				this.objectsFlow.writeName("ON");
				this.objectsFlow.startArray();
				for (int i = 0; i < this.ocgs.size(); ++i) {
					ObjectRef ocgRef = (ObjectRef) this.ocgs.get(i);
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
					ObjectRef ocgRef = (ObjectRef) this.ocgs.get(i);
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
					ObjectRef ocgRef = (ObjectRef) this.ocgs.get(i);
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
			ViewerPreferences vp = this.params.getViewerPreferences();
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
						throw new UnsupportedOperationException("ViewerPreferenceのDisplayDocTitleは PDF 1.4 以降で使用できます。");
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
						throw new UnsupportedOperationException("ViewerPreferenceのDirectionは PDF 1.3 以降で使用できます。");
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
						throw new UnsupportedOperationException("ViewerPreferenceのViewAreaは PDF 1.4 以降で使用できます。");
					}
					this.catalogFlow.writeName("ViewArea");
					this.writeArea(vp.getViewArea());
					this.catalogFlow.lineBreak();
				}

				if (vp.getViewClip() != ViewerPreferences.AreaBox.CROP) {
					if (this.params.getVersion().v < PDFParams.Version.V_1_4.v) {
						throw new UnsupportedOperationException("ViewerPreferenceのViewClipは PDF 1.4 以降で使用できます。");
					}
					this.catalogFlow.writeName("ViewClip");
					this.writeArea(vp.getViewClip());
					this.catalogFlow.lineBreak();
				}

				if (vp.getPrintArea() != ViewerPreferences.AreaBox.CROP) {
					if (this.params.getVersion().v < PDFParams.Version.V_1_4.v) {
						throw new UnsupportedOperationException("ViewerPreferenceのPrintAreaは PDF 1.4 以降で使用できます。");
					}
					this.catalogFlow.writeName("PrintArea");
					this.writeArea(vp.getPrintArea());
					this.catalogFlow.lineBreak();
				}

				if (vp.getPrintClip() != ViewerPreferences.AreaBox.CROP) {
					if (this.params.getVersion().v < PDFParams.Version.V_1_4.v) {
						throw new UnsupportedOperationException("ViewerPreferenceのPrintClipは PDF 1.4 以降で使用できます。");
					}
					this.catalogFlow.writeName("PrintClip");
					this.writeArea(vp.getPrintClip());
					this.catalogFlow.lineBreak();
				}

				if (vp.getPrintScaling() != ViewerPreferences.PrintScaling.APP_DEFAULT) {
					if (this.params.getVersion().v < PDFParams.Version.V_1_6.v) {
						throw new UnsupportedOperationException("ViewerPreferenceのPrintScalingは PDF 1.6 以降で使用できます。");
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
						throw new UnsupportedOperationException("ViewerPreferenceのDuplexは PDF 1.7 以降で使用できます。");
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
								"ViewerPreferenceのPickTrayByPDFSizeは PDF 1.7 以降で使用できます。");
					}
					this.catalogFlow.writeName("PickTrayByPDFSize");
					this.catalogFlow.writeBoolean(true);
					this.catalogFlow.lineBreak();
				}

				int[] printPageRange = vp.getPrintPageRange();
				if (printPageRange != null) {
					if (this.params.getVersion().v < PDFParams.Version.V_1_7.v) {
						throw new UnsupportedOperationException("ViewerPreferenceのPrintPageRangeは PDF 1.7 以降で使用できます。");
					}
					this.catalogFlow.writeName("PrintPageRange");
					this.catalogFlow.startArray();
					for (int i = 0; i < printPageRange.length; ++i) {
						this.catalogFlow.writeInt(printPageRange[i]);
					}
					this.catalogFlow.endArray();
					this.catalogFlow.lineBreak();
				}

				int numCopies = vp.getNumCopies();
				if (numCopies > 0) {
					if (this.params.getVersion().v < PDFParams.Version.V_1_7.v) {
						throw new UnsupportedOperationException("ViewerPreferenceのNumCopiesは PDF 1.7 以降で使用できます。");
					}
					this.catalogFlow.writeName("NumCopies");
					this.catalogFlow.writeInt(numCopies);
					this.catalogFlow.lineBreak();
				}

				this.catalogFlow.endHash();

				this.catalogFlow.lineBreak();
			}

			// 文書を開いた時の動作
			Action action = this.params.getOpenAction();
			if (action != null) {
				this.catalogFlow.writeName("OpenAction");
				this.catalogFlow.startHash();
				action.writeTo(this.catalogFlow);
				this.catalogFlow.endHash();
				this.catalogFlow.lineBreak();
			}

			// カタログ
			this.catalogFlow.close();

			// リソース
			this.fonts.close();
			this.pageResourceFlow.close();
			this.objectsFlow.close();

			// クロスリファレンス
			this.xref.close(this.builder.getPositionInfo(), infoRef, this.fileid, this.encryption);

			this.mainFlow.close();
		} finally {
			this.builder.close();
		}
	}

	private void writeArea(ViewerPreferences.AreaBox area) throws IOException {
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
