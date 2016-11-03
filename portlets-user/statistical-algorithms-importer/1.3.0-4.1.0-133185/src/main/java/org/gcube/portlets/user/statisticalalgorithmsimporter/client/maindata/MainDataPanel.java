package org.gcube.portlets.user.statisticalalgorithmsimporter.client.maindata;

import org.gcube.portlets.user.statisticalalgorithmsimporter.client.event.ProjectStatusEvent;
import org.gcube.portlets.user.statisticalalgorithmsimporter.shared.project.Project;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.EventBus;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.container.MarginData;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;

/**
 * 
 * @author giancarlo email: <a
 *         href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a>
 *
 */
public class MainDataPanel extends SimpleContainer {

	private EventBus eventBus;
	private CodeEditPanel codeEditPanel;

	public MainDataPanel(EventBus eventBus) {
		super();
		this.eventBus = eventBus;
		init();
		create();
		bindToEvents();
	}

	protected void init() {
		forceLayoutOnResize = true;
		// setBodyBorder(false);
		setBorders(false);
		// setHeaderVisible(false);

	}

	protected void create() {

	}

	private void bindToEvents() {
		eventBus.addHandler(ProjectStatusEvent.TYPE,
				new ProjectStatusEvent.ProjectStatusEventHandler() {

					@Override
					public void onProjectStatus(ProjectStatusEvent event) {
						Log.debug("Catch ProjectStatusEvent");
						doProjectStatusCommand(event);

					}
				});
	}

	private void doProjectStatusCommand(ProjectStatusEvent event) {
		if (event.getProjectStatusEventType() == null) {
			return;
		}
		switch (event.getProjectStatusEventType()) {
		case DELETE_MAIN_CODE:	
		case MAIN_CODE_SET:
			codeEditPanel.codeUpdate(event.getProject());
			break;
		case OPEN:
			addCodeEditPanel(event.getProject());
			break;
		case SAVE:
		case START:
		case UPDATE:
		case SOFTWARE_CREATED:
		case SOFTWARE_PUBLISH:
		case SOFTWARE_REPACKAGE:	
		case ADD_RESOURCE:
		case DELETE_RESOURCE:	
		case EXPLORER_REFRESH:
			break;
		default:
			break;

		}

	}

	private void addCodeEditPanel(Project project) {
		codeEditPanel = new CodeEditPanel(project, eventBus);
		add(codeEditPanel, new MarginData(new Margins(0)));
		forceLayout();

	}

}
