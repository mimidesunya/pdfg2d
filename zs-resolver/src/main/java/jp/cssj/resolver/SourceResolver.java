package jp.cssj.resolver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

/**
 * URIに対応するソースを取得します。
 * 
 * @author MIYABE Tatsuhiko
 * @version $Id: SourceResolver.java 1565 2018-07-04 11:51:25Z miyabe $
 */
public interface SourceResolver {
	/**
	 * URIに対応するソースを返します。
	 * 
	 * @param uri
	 *            ソースの位置を示すURI。
	 * @return URIに対応するソースソース。
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public Source resolve(URI uri) throws IOException, FileNotFoundException;

	/**
	 * 取得したソースを返却します。
	 * 
	 * @param source
	 *            同じSourceResolverのresolveによって取得したソース。
	 */
	public void release(Source source);
}
