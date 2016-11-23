package org.gcube.portlets.admin.vredeployment.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class VREDeploymentApp implements EntryPoint {
	/**
	 * 
	 */
	public static final String CONTAINER_DIV = "DeploymentView";

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		VREDeploymentServiceAsync rpcService = GWT.create(VREDeploymentService.class);
		HandlerManager eventBus = new HandlerManager(null);
		AppController appViewer = new AppController(rpcService, eventBus);
		appViewer.go(RootPanel.get(CONTAINER_DIV));
	}
}
