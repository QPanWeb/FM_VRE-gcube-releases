/**
 * 
 */
package org.gcube.portlets.user.statisticalalgorithmsimporter.client.rpc;

import java.util.ArrayList;

import org.gcube.portlets.user.statisticalalgorithmsimporter.shared.code.CodeData;
import org.gcube.portlets.user.statisticalalgorithmsimporter.shared.file.FileUploadMonitor;
import org.gcube.portlets.user.statisticalalgorithmsimporter.shared.project.InputData;
import org.gcube.portlets.user.statisticalalgorithmsimporter.shared.project.Project;
import org.gcube.portlets.user.statisticalalgorithmsimporter.shared.session.UserInfo;
import org.gcube.portlets.user.statisticalalgorithmsimporter.shared.workspace.ItemDescription;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * 
 * @author "Giancarlo Panichi" <a
 *         href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a>
 *
 */
public interface StatAlgoImporterServiceAsync {

	public static StatAlgoImporterServiceAsync INSTANCE = (StatAlgoImporterServiceAsync) GWT
			.create(StatAlgoImporterService.class);

	/**
	 * 
	 * @param callback
	 */
	void hello(AsyncCallback<UserInfo> callback);

	void getFileUploadMonitor(AsyncCallback<FileUploadMonitor> callback);

	void getCode(AsyncCallback<ArrayList<CodeData>> callback);

	void createProjectOnWorkspace(ItemDescription itemDescription,
			AsyncCallback<Void> callback);
	
	void setMainCode(ItemDescription itemDescription,
			AsyncCallback<Project> callback);

	void addResourceToProject(ItemDescription itemDescription,
			AsyncCallback<Void> asyncCallback);

	void saveProject(InputData inputData,
			AsyncCallback<Void> asyncCallback);

	void deleteResourceOnProject(ItemDescription itemDescription,
			AsyncCallback<Project> asyncCallback);

	void openProjectOnWorkspace(ItemDescription newProjectFolder,
			AsyncCallback<Project> asyncCallback);

	void saveCode(String code, AsyncCallback<Void> asyncCallback);
	
	void setNewMainCode(ItemDescription itemDescription, String code,
			AsyncCallback<Project> asyncCallback);

	void createSoftware(InputData inputData, AsyncCallback<Void> callback);

	void getPublicLink(ItemDescription itemDescription,
			AsyncCallback<String> asyncCallback);

	void restoreUISession(String value, AsyncCallback<Project> asyncCallback);

	void publishSoftware(AsyncCallback<Void> asyncCallback);

	void repackageSoftware(AsyncCallback<Void> asyncCallback);

}
