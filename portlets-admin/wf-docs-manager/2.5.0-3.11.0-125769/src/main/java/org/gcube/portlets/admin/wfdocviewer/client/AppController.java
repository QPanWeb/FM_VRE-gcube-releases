package org.gcube.portlets.admin.wfdocviewer.client;

import java.util.ArrayList;

import org.gcube.portlets.admin.wfdocslibrary.shared.WfRoleDetails;
import org.gcube.portlets.admin.wfdocviewer.client.event.RolesAddedEvent;
import org.gcube.portlets.admin.wfdocviewer.client.event.RolesAddedEventHandler;
import org.gcube.portlets.admin.wfdocviewer.client.event.SelectedReportEvent;
import org.gcube.portlets.admin.wfdocviewer.client.event.SelectedReportEventHandler;
import org.gcube.portlets.admin.wfdocviewer.client.presenter.Presenter;
import org.gcube.portlets.admin.wfdocviewer.client.presenter.WorkflowDocsPresenter;
import org.gcube.portlets.admin.wfdocviewer.client.view.WorkflowDocsView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.HasWidgets;

/**
 * 
 * @author Massimiliano Assante, ISTI-CNR - massimiliano.assante@isti.cnr.it
 * @version May 2011 (0.1) 
 * 
 * This <class>AppController</class> contains the view transition logic, 
 * to handle logic that is not specific to any presenter and instead resides at the application layer
 */
public class AppController implements Presenter, ValueChangeHandler<String> {
	private final HandlerManager eventBus;
	private final WorkflowDocServiceAsync rpcService;
	private HasWidgets container;
	Presenter presenter;

	public AppController(WorkflowDocServiceAsync rpcService, HandlerManager eventBus) {
		this.eventBus = eventBus;
		this.rpcService = rpcService;
		bind();
	}

	private void bind() {
		eventBus.addHandler(SelectedReportEvent.TYPE, new SelectedReportEventHandler() {
			@Override
			public void onSelectedReport(SelectedReportEvent event) {
				doInstanciateNewWorkflow(event.getSelectedReportId(), event.getSelectedReportName());
			}
		});
		eventBus.addHandler(RolesAddedEvent.TYPE, new RolesAddedEventHandler() {			
			@Override
			public void onAddRoles(RolesAddedEvent rolesAddedEvent) {
				doAddRolesToSelectedStep(rolesAddedEvent.getRoles());
			}
		});

	    History.addValueChangeHandler(this);
	}

	@Override
	public void go(HasWidgets container) {
		this.container = container;
		History.fireCurrentHistoryState();
	}
	@Override
	public void onValueChange(ValueChangeEvent<String> event) {

		presenter = new WorkflowDocsPresenter(rpcService, eventBus, new WorkflowDocsView());

		if (presenter != null) {
			GWT.log("Container=null?"+(container==null));
			presenter.go(this.container);
		}
	}

	@Override
	public void doInstanciateNewWorkflow(String reportid, String reportName) {
		presenter.doInstanciateNewWorkflow(reportid, reportName);		
	}

	@Override
	public void doAddRolesToSelectedStep(ArrayList<WfRoleDetails> roles) {
		presenter.doAddRolesToSelectedStep(roles);		
	}
}
