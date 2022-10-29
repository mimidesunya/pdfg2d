package net.zamasoft.pdfg2d.pdf;

/**
 * @author MIYABE Tatsuhiko
 * @version $Id: XRef.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public interface XRef {
	/**
	 * 次のオブジェクトIDを返します。
	 * 
	 * @return
	 */
	public ObjectRef nextObjectRef();

	/**
	 * 属性を追加します。
	 * 
	 * @param key
	 * @param value
	 */
	public void setAttribute(String key, Object value);

	/**
	 * 属性を返します。
	 * 
	 * @param key
	 * @return
	 */
	public Object getAttribute(String key);
}
