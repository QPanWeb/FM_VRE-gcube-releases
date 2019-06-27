package org.gcube.portlets.user.workspace.client.gridevent;

import com.google.gwt.event.shared.EventHandler;


/**
 * The Interface FileVersioningEventHandler.
 *
 * @author Francesco Mangiacrapa francesco.mangiacrapa{@literal @}isti.cnr.it
 * Feb 20, 2017
 */
public interface FileVersioningEventHandler extends EventHandler {

	/**
	 * On file versioning.
	 *
	 * @param fileVersioningEvent the file versioning event
	 */
	void onFileVersioning(FileVersioningEvent fileVersioningEvent);
}