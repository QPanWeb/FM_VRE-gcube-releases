package org.gcube.portlets.user.workspace.client.event;

import com.google.gwt.event.shared.EventHandler;

public interface DeleteItemEventHandler extends EventHandler {
	void onDeleteItem(DeleteItemEvent deleteItemEvent);
}