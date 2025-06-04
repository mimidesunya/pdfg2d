package net.zamasoft.pdfg2d.pdf;

/**
 * @author MIYABE Tatsuhiko
 * @since 1.0
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
