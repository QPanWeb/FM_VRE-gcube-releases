package org.gcube.portlets.user.dataminermanager.server.util;

import org.gcube.application.framework.core.session.ASLSession;
import org.gcube.common.homelibrary.home.workspace.WorkspaceItem;
import org.gcube.portlets.user.dataminermanager.server.storage.StorageUtil;
import org.gcube.portlets.user.dataminermanager.shared.exception.ServiceException;
import org.gcube.portlets.user.dataminermanager.shared.workspace.Computations;
import org.gcube.portlets.user.dataminermanager.shared.workspace.OutputDataSets;
import org.gcube.portlets.user.dataminermanager.shared.workspace.DataMinerWorkArea;
import org.gcube.portlets.user.dataminermanager.shared.workspace.InputDataSets;
import org.gcube.portlets.user.dataminermanager.shared.workspace.ItemDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Giancarlo Panichi email: <a
 *         href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a>
 *
 */
public class DataMinerWorkAreaManager {
	private static final String DATA_MINER_FOLDER = "DataMiner";
	private static final String IMPORTED_DATA_FOLDER = "Input Data Sets";
	private static final String COMPUTED_DATA_FOLDER = "Output Data Sets";
	private static final String COMPUTATIONS_FOLDER = "Computations";

	public static final Logger logger = LoggerFactory
			.getLogger(DataMinerWorkAreaManager.class);

	private ASLSession aslSession;

	public DataMinerWorkAreaManager(ASLSession aslSession) {
		this.aslSession = aslSession;
	}

	public DataMinerWorkArea getDataMinerWorkArea() throws ServiceException {
		DataMinerWorkArea dataMinerWorkArea = null;
		try {
			WorkspaceItem wiDataMinerFolder = StorageUtil
					.getItemInRootFolderOnWorkspace(aslSession.getUsername(),
							DATA_MINER_FOLDER);

			if (wiDataMinerFolder == null) {
				dataMinerWorkArea = new DataMinerWorkArea(null);
				return dataMinerWorkArea;
			} else {
				ItemDescription dataMinerWorkAreaFolder = null;

				dataMinerWorkAreaFolder = new ItemDescription(
						wiDataMinerFolder.getId(), wiDataMinerFolder.getName(),
						wiDataMinerFolder.getOwner().getPortalLogin(),
						wiDataMinerFolder.getPath(), wiDataMinerFolder
								.getType().name());
				dataMinerWorkArea = new DataMinerWorkArea(
						dataMinerWorkAreaFolder);

			}

		} catch (Throwable e) {
			logger.debug("DataMiner Folder is set to null");
			e.printStackTrace();
			dataMinerWorkArea = new DataMinerWorkArea(null);
			return dataMinerWorkArea;
		}

		InputDataSets inputDataSets = null;
		try {
			WorkspaceItem wiImportedDataFolder = StorageUtil
					.getItemInFolderOnWorkspace(aslSession.getUsername(),
							dataMinerWorkArea.getDataMinerWorkAreaFolder()
									.getId(), IMPORTED_DATA_FOLDER);
			ItemDescription importedDataFolder = null;

			importedDataFolder = new ItemDescription(
					wiImportedDataFolder.getId(),
					wiImportedDataFolder.getName(), wiImportedDataFolder
							.getOwner().getPortalLogin(),
					wiImportedDataFolder.getPath(), wiImportedDataFolder
							.getType().name());

			inputDataSets = new InputDataSets(importedDataFolder);

		} catch (Throwable e) {
			logger.debug("ImportedData Folder is set to null");
		}
		dataMinerWorkArea.setInputDataSets(inputDataSets);

		OutputDataSets outputDataSets = null;
		try {
			WorkspaceItem wiComputedDataFolder = StorageUtil
					.getItemInFolderOnWorkspace(aslSession.getUsername(),
							dataMinerWorkArea.getDataMinerWorkAreaFolder()
									.getId(), COMPUTED_DATA_FOLDER);
			ItemDescription computedDataFolder = null;

			computedDataFolder = new ItemDescription(
					wiComputedDataFolder.getId(),
					wiComputedDataFolder.getName(), wiComputedDataFolder
							.getOwner().getPortalLogin(),
					wiComputedDataFolder.getPath(), wiComputedDataFolder
							.getType().name());
			outputDataSets = new OutputDataSets(computedDataFolder);

		} catch (Throwable e) {
			logger.debug("ComputedData Folder is set to null");
		}
		dataMinerWorkArea.setOutputDataSets(outputDataSets);

		Computations computations = null;
		try {
			WorkspaceItem wiComputationsDataFolder = StorageUtil
					.getItemInFolderOnWorkspace(aslSession.getUsername(),
							dataMinerWorkArea.getDataMinerWorkAreaFolder()
									.getId(), COMPUTATIONS_FOLDER);
			ItemDescription computationsDataFolder = null;

			computationsDataFolder = new ItemDescription(
					wiComputationsDataFolder.getId(),
					wiComputationsDataFolder.getName(),
					wiComputationsDataFolder.getOwner().getPortalLogin(),
					wiComputationsDataFolder.getPath(),
					wiComputationsDataFolder.getType().name());
			computations = new Computations(computationsDataFolder);

		} catch (Throwable e) {
			logger.debug("Computations Folder is set to null");
		}
		dataMinerWorkArea.setComputations(computations);

		return dataMinerWorkArea;

	}

}
