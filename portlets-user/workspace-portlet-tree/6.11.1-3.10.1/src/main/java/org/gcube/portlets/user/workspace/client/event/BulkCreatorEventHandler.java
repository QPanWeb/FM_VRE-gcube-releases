package org.gcube.portlets.user.workspace.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface BulkCreatorEventHandler extends EventHandler {
	void onBulkCreator(BulkCreatorEvent bulkCreatorEvent);
}