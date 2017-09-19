package org.gcube.portlets.user.workspace.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * 
 * @author Francesco Mangiacrapa francesco.mangiacrapa@isti.cnr.it
 * @May 23, 2013
 *
 */
public interface TrashEventHandler extends EventHandler {
	/**
	 * @param accountingHistoryEvent
	 */
	void onTrashEvent(TrashEvent trashEvent);
}