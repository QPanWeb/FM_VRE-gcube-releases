package org.gcube.portlets.user.trainingcourse.client.dialog;

import com.google.gwt.core.client.JavaScriptObject;


// TODO: Auto-generated Javadoc
/**
 * The Class NewBrowserWindow.
 *
 * @author Francesco Mangiacrapa francesco.mangiacrapa@isti.cnr.it
 * Feb 8, 2018
 */
public final class NewBrowserWindow extends JavaScriptObject {
	// All types that extend JavaScriptObject must have a protected,
	/**
	 * Instantiates a new new browser window.
	 */
	// no-args constructor.
	protected NewBrowserWindow() {
	}

	/**
	 * Open.
	 *
	 * @param url the url
	 * @param target the target
	 * @param options the options
	 * @return the new browser window
	 */
	public static native NewBrowserWindow open(String url, String target,
			String options) /*-{
		return $wnd.open(url, target, options);
	}-*/;

	/**
	 * Close.
	 */
	public native void close() /*-{
		this.close();
	}-*/;

	/**
	 * Sets the url.
	 *
	 * @param url the new url
	 */
	public native void setUrl(String url) /*-{
		if (this.location) {
			this.location = url;
		}
	}-*/;
}
