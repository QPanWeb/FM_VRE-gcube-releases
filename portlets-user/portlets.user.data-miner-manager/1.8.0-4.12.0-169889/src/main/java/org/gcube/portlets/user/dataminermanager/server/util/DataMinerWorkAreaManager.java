package org.gcube.portlets.user.dataminermanager.server.util;

import org.gcube.common.storagehub.model.items.Item;
import org.gcube.data.analysis.dataminermanagercl.server.util.ServiceCredentials;
import org.gcube.data.analysis.dataminermanagercl.shared.workspace.Computations;
import org.gcube.data.analysis.dataminermanagercl.shared.workspace.DataMinerWorkArea;
import org.gcube.data.analysis.dataminermanagercl.shared.workspace.InputDataSets;
import org.gcube.data.analysis.dataminermanagercl.shared.workspace.ItemDescription;
import org.gcube.data.analysis.dataminermanagercl.shared.workspace.OutputDataSets;
import org.gcube.portlets.user.dataminermanager.server.storage.StorageUtil;
import org.gcube.portlets.user.dataminermanager.shared.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Giancarlo Panichi
 *
 *
 */
public class DataMinerWorkAreaManager {
	private static final String DATA_MINER_FOLDER = "DataMiner";
	private static final String IMPORTED_DATA_FOLDER = "Input Data Sets";
	private static final String COMPUTED_DATA_FOLDER = "Output Data Sets";
	private static final String COMPUTATIONS_FOLDER = "Computations";

	public static final Logger logger = LoggerFactory.getLogger(DataMinerWorkAreaManager.class);

	private ServiceCredentials serviceCredentials;

	public DataMinerWorkAreaManager(ServiceCredentials serviceCredentials) {
		this.serviceCredentials = serviceCredentials;
	}

	public DataMinerWorkArea getDataMinerWorkArea() throws ServiceException {
		DataMinerWorkArea dataMinerWorkArea = null;
		StorageUtil storageUtil = new StorageUtil();
		try {

			Item wiDataMinerFolder = storageUtil.getItemInRootFolderOnWorkspace(serviceCredentials.getUserName(),
					DATA_MINER_FOLDER);

			if (wiDataMinerFolder == null) {
				dataMinerWorkArea = new DataMinerWorkArea(null);
				return dataMinerWorkArea;
			} else {
				ItemDescription dataMinerWorkAreaFolder = null;

				dataMinerWorkAreaFolder = new ItemDescription(wiDataMinerFolder.getId(), wiDataMinerFolder.getName(),
						wiDataMinerFolder.getOwner(), wiDataMinerFolder.getPath(),
						null);
				dataMinerWorkArea = new DataMinerWorkArea(dataMinerWorkAreaFolder);

			}

		} catch (Throwable e) {
			logger.debug("DataMiner Folder is set to null");
			e.printStackTrace();
			dataMinerWorkArea = new DataMinerWorkArea(null);
			return dataMinerWorkArea;
		}

		InputDataSets inputDataSets = null;
		try {
			Item wiImportedDataFolder = storageUtil.getItemInFolderOnWorkspace(serviceCredentials.getUserName(),
					dataMinerWorkArea.getDataMinerWorkAreaFolder().getId(), IMPORTED_DATA_FOLDER);
			ItemDescription importedDataFolder = null;

			importedDataFolder = new ItemDescription(wiImportedDataFolder.getId(), wiImportedDataFolder.getName(),
					wiImportedDataFolder.getOwner(), wiImportedDataFolder.getPath(),
					null);

			inputDataSets = new InputDataSets(importedDataFolder);

		} catch (Throwable e) {
			logger.debug("ImportedData Folder is set to null");
		}
		dataMinerWorkArea.setInputDataSets(inputDataSets);

		OutputDataSets outputDataSets = null;
		try {
			Item wiComputedDataFolder = storageUtil.getItemInFolderOnWorkspace(serviceCredentials.getUserName(),
					dataMinerWorkArea.getDataMinerWorkAreaFolder().getId(), COMPUTED_DATA_FOLDER);
			ItemDescription computedDataFolder = null;

			computedDataFolder = new ItemDescription(wiComputedDataFolder.getId(), wiComputedDataFolder.getName(),
					wiComputedDataFolder.getOwner(), wiComputedDataFolder.getPath(),
					null);
			outputDataSets = new OutputDataSets(computedDataFolder);

		} catch (Throwable e) {
			logger.debug("ComputedData Folder is set to null");
		}
		dataMinerWorkArea.setOutputDataSets(outputDataSets);

		Computations computations = null;
		try {
			Item wiComputationsDataFolder = storageUtil.getItemInFolderOnWorkspace(serviceCredentials.getUserName(),
					dataMinerWorkArea.getDataMinerWorkAreaFolder().getId(), COMPUTATIONS_FOLDER);
			ItemDescription computationsDataFolder = null;

			computationsDataFolder = new ItemDescription(wiComputationsDataFolder.getId(),
					wiComputationsDataFolder.getName(), wiComputationsDataFolder.getOwner(),
					wiComputationsDataFolder.getPath(), null);
			computations = new Computations(computationsDataFolder);

		} catch (Throwable e) {
			logger.debug("Computations Folder is set to null");
		}
		dataMinerWorkArea.setComputations(computations);

		logger.debug("DataMinerWorkArea: "+dataMinerWorkArea);
		return dataMinerWorkArea;

	}

}
