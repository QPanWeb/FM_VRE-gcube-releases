package org.gcube.portlets.user.workspace.client.event;

import com.google.gwt.event.shared.EventHandler;

/**
 * 
 * @author Francesco Mangiacrapa francesco.mangiacrapa@isti.cnr.it
 *
 */
public interface GridRenameItemEventHandler extends EventHandler {
	void onGridItemRename(GridRenameItemEvent event);
}
