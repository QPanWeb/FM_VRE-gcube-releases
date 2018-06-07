package org.gcube.portlets.user.speciesdiscovery.client.job.gislayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.gcube.portlets.user.speciesdiscovery.client.resources.Resources;
import org.gcube.portlets.user.speciesdiscovery.shared.JobGisLayerModel;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * The Class OccurrenceJobSpeciesPanel.
 *
 * @author Francesco Mangiacrapa francesco.mangiacrapa@isti.cnr.it
 */
public class GisLayerJobSpeciesPanel extends ContentPanel {


	/**
	 * This is a singleton
	 */
	private static GisLayerJobSpeciesPanel instance;
	private static ContentPanel cp;
	private ContentPanel vp;
	private static HashMap<String, GisLayerJobSpeciesProgressBar> hashProgressBars;
	private static HashMap<String, Boolean> hashLoadCompletedNotify;
	private static HashMap<String, ContentPanel> hashTableContentPanels;
	private static String LASTOPERATION = "Last Operation: ";
	private Html lastOperation = new Html(LASTOPERATION);
	private LayoutContainer lc;
	private static String WINDOWTITLE = "Gis Layer Jobs";
	private static final String FAILED = "failed";
	private static final String COMPLETED = "completed";
	private static final String LOADING = "loading";
	private static final String PENDING = "pending";
	private static final String OPERATIONONE = "none";
	private static final String SAVING = "saving";

	private GisLayerGridJob gridJob;
	private EventBus eventBus;

	protected Window gisLayerJobWindow = new Window();

	/**
	 * Gets the single instance of OccurrenceJobSpeciesPanel.
	 *
	 * @param eventBus the event bus
	 * @return single instance of OccurrenceJobSpeciesPanel
	 */
	public static synchronized GisLayerJobSpeciesPanel getInstance(EventBus eventBus) {
		if (instance == null)
			instance = new GisLayerJobSpeciesPanel(eventBus);
		return instance;
	}

	/**
	 * Instantiates a new occurrence job species panel.
	 *
	 * @param eventBus the event bus
	 */
	private GisLayerJobSpeciesPanel(EventBus eventBus) {
		this.eventBus = eventBus;
		this.gridJob = new GisLayerGridJob(eventBus);

		this.setHeaderVisible(false);
		this.setLayout(new FitLayout());
		createSpeciesJobWindow();
//		createToolBar();
	}

	/**
	 * Creates the species job window.
	 */
	private void createSpeciesJobWindow(){

		lc = new LayoutContainer();
		lc.setStyleAttribute("margin", "5px");
		cp = new ContentPanel();
		cp.setBodyBorder(true);
		cp.setStyleAttribute("padding", "5px");
		cp.setLayout(new FitLayout());

		cp.add(gridJob);

		cp.setHeight(550);
		cp.setHeaderVisible(false);
		hashProgressBars = new HashMap<String, GisLayerJobSpeciesProgressBar>();
		hashTableContentPanels = new HashMap<String, ContentPanel>();
		hashLoadCompletedNotify = new HashMap<String, Boolean>();
		cp.setScrollMode(Scroll.AUTO);

		lastOperation.setHtml(LASTOPERATION + OPERATIONONE);

		lc.add(lastOperation);
		lc.add(cp);

		gisLayerJobWindow.setHeading(WINDOWTITLE);
		gisLayerJobWindow.setIcon(AbstractImagePrototype.create(Resources.INSTANCE.getBluePlace16px()));
		gisLayerJobWindow.setSize(1020, 600);
		gisLayerJobWindow.setResizable(true);
		gisLayerJobWindow.setScrollMode(Scroll.AUTOX);
		gisLayerJobWindow.add(lc);
	}


	/**
	 * Adds the list job.
	 *
	 * @param jobsModel the jobs model
	 */
	public void addListJob(List<JobGisLayerModel> jobsModel){

		for(final JobGisLayerModel jobModel : jobsModel)
			addJob(jobModel);
	}



	/**
	 * Adds the job.
	 *
	 * @param jobModel the job model
	 * @return true, if successful
	 */
	public boolean addJob(final JobGisLayerModel jobModel){

		boolean isLoading = false;

		GisLayerJobSpeciesProgressBar jobsBar = hashProgressBars.get(jobModel.getJobIdentifier());

		if(jobsBar!=null){ //is update

			GisLayerWindowInfoJobsSpecies win = (GisLayerWindowInfoJobsSpecies) jobsBar.getData("win");

			if(win!=null){
				win.updateDescription(jobModel);
				win.layout();
				jobsBar.setData("win", win);
			}

			//IF job is COMPLETED OR FAILED OR COMPLETED WITH ERROR, IS NOT UPDATE
			if(jobsBar.isCompleted()){
				//FOR DEBUG
//				Log.trace("jobsBar " +jobModel.getName() + " completed, return" );
				return true;
			}

			updateProgressBarView(jobsBar, jobModel);
			gridJob.updateStatus(jobModel, jobsBar);
		}
		else{ //create new grid item that contains new progress bar

			GisLayerJobSpeciesProgressBar jobProgressBar = new GisLayerJobSpeciesProgressBar(jobModel.getJobIdentifier(), jobModel.getDownloadState().toString());
			gridJob.addJobIntoGrid(jobModel, jobProgressBar);
			updateProgressBarView(jobProgressBar, jobModel);
			hashProgressBars.put(jobModel.getJobIdentifier(), jobProgressBar); //add progressBar into hashProgressBars
			hashLoadCompletedNotify.put(jobModel.getJobIdentifier(), false); //add false (at load completed event) into hashLoadCompletedNotify
		}

		gridJob.layout();
		cp.layout();

		return isLoading;
	}


	/**
	 * Update progress bar view.
	 *
	 * @param jobsBar the jobs bar
	 * @param jobModel the job model
	 * @return true, if successful
	 */
	private boolean updateProgressBarView(GisLayerJobSpeciesProgressBar jobsBar, JobGisLayerModel jobModel){

			switch (jobModel.getDownloadState()) {

			case PENDING:{
				jobsBar.setProgressText(PENDING);
				break;
			}

			case SAVING:{
				lastOperation.setHtml(LASTOPERATION + jobModel.getJobName() + " saving");
				jobsBar.getElement().getStyle().setBorderColor("#7093DB");
				jobsBar.setProgressText(SAVING);
				break;
			}

			case COMPLETED:{

				lastOperation.setHtml(LASTOPERATION + jobModel.getJobName() + " completed");
				jobsBar.getElement().getStyle().setBorderColor("#000000");
				jobsBar.updateProgressWithoutPercentage(100);
				jobsBar.setCompleted(true);
				jobsBar.updateText(COMPLETED);
				break;
			}

			case ONGOING:{

				if(jobModel.getPercentage()<100)
					jobsBar.setProgressText(LOADING);
				else
					jobsBar.setProgressText(SAVING);

				jobsBar.updateProgressWithPercentage(jobModel.getPercentage());
				return true;
			}

			case SAVED:{

				lastOperation.setHtml(LASTOPERATION + jobModel.getJobName() + " saved");
				jobsBar.getElement().getStyle().setBorderColor("#000000");
				jobsBar.updateProgressWithoutPercentage(100);
				jobsBar.setCompleted(true);
				jobsBar.updateText(COMPLETED);
				break;
			}

			case ONGOINGWITHFAILURES: {

				jobsBar.getElement().getStyle().setBorderColor("#f00");
				jobsBar.updateText("loading " + jobModel.getJobName() + " with failures");
				break;
			}

			case FAILED:{

				jobsBar.getElement().getStyle().setBorderColor("#f00");
				jobsBar.setProgressText(FAILED);
				jobsBar.updateProgressWithPercentage(jobModel.getPercentage());
				jobsBar.setCompleted(true);
				jobsBar.updateText(FAILED);
				break;
			}

			case COMPLETEDWITHFAILURES:{

				jobsBar.getElement().getStyle().setBorderColor("#f00");
				jobsBar.updateText("loading " + jobModel.getJobName() + " with failures");
				jobsBar.updateProgressWithoutPercentage(100);
				jobsBar.setCompleted(true);
				break;
			}
		}

		return false;
	}

	/**
	 * Removes the species job.
	 *
	 * @param hashHPKey the hash hp key
	 */
	public void removeSpeciesJob(String hashHPKey) {

		ContentPanel cp = hashTableContentPanels.get(hashHPKey);
		vp.remove(cp);
		lastOperation.setHtml(LASTOPERATION + cp.getId() + " deleted");
		hashProgressBars.remove(hashHPKey); //remove progress bar from hash
		hashTableContentPanels.remove(hashHPKey); //remove hp from hash
		hashLoadCompletedNotify.remove(hashHPKey); //remove notify event
		vp.layout();
	}

	/**
	 * Delete progress completed.
	 *
	 * @param progressIdFound the progress id found
	 */
	@SuppressWarnings("unused")
	private void deleteProgressCompleted(List<String> progressIdFound){

		List<String> progressIdNotFound = new ArrayList<String>();

		for(String key : hashTableContentPanels.keySet()){
			if(!progressIdFound.contains(key)){ //if key isn't not found - progress is completed so is removed
				GisLayerJobSpeciesProgressBar bulkPB = hashProgressBars.get(key);
				lastOperation.setHtml(LASTOPERATION + bulkPB.getProgressText() + " uploading completed");
				progressIdNotFound.add(key);
			}
		}

		for(String key : progressIdNotFound){
			GisLayerJobSpeciesProgressBar bulkPB = hashProgressBars.get(key);
			lastOperation.setHtml(LASTOPERATION + bulkPB.getProgressText() + " uploading completed");
			hashProgressBars.remove(key); //remove progress bar from hash
			hashTableContentPanels.remove(key); //remove hp from hash
		}

		vp.layout();
		cp.layout();
	}

	/**
	 * Gets the species job window.
	 *
	 * @return the species job window
	 */
	public Window getGisLayerJobWindow() {
		return gisLayerJobWindow;
	}

	/**
	 * Reset structures.
	 */
	public void resetStructures(){

		this.gridJob.resetStore();

		hashProgressBars.clear();
		lastOperation.setHtml(LASTOPERATION);
		hashTableContentPanels.clear();
		hashLoadCompletedNotify.clear();
	}

	/**
	 * Gets the grid job.
	 *
	 * @return the grid job
	 */
	public GisLayerGridJob getGridJob() {
		return gridJob;
	}
}

