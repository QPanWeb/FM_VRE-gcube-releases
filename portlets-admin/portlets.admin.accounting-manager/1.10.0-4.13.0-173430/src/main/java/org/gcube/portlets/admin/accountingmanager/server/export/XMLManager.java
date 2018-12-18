package org.gcube.portlets.admin.accountingmanager.server.export;

import java.beans.XMLEncoder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.gcube.portlets.admin.accountingmanager.server.export.model.AccountingDataModel;
import org.gcube.portlets.admin.accountingmanager.server.export.model.AccountingModel4Job;
import org.gcube.portlets.admin.accountingmanager.server.export.model.AccountingModel4Service;
import org.gcube.portlets.admin.accountingmanager.server.export.model.AccountingModel4Storage;
import org.gcube.portlets.admin.accountingmanager.server.export.model.AccountingModelBuilder;
import org.gcube.portlets.admin.accountingmanager.server.export.model.AccountingModelDirector;
import org.gcube.portlets.admin.accountingmanager.server.state.AccountingStateData;
import org.gcube.portlets.admin.accountingmanager.server.storage.StorageUtil;
import org.gcube.portlets.admin.accountingmanager.shared.exception.ServiceException;
import org.gcube.portlets.admin.accountingmanager.shared.export.ExportDescriptor;
import org.gcube.portlets.admin.accountingmanager.shared.workspace.ItemDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XML Manager
 * 
  * @author Giancarlo Panichi
 *
 *
 */
public class XMLManager {

	private static final String FILE_EXTENSION = ".xml";
	private static Logger logger = LoggerFactory.getLogger(XMLManager.class);
	private String userName;

	public XMLManager(String userName) {
		this.userName = userName;
	}

	public ItemDescription saveOnWorkspace(
			AccountingStateData accountingStateData) throws ServiceException {
		try {
			if (accountingStateData == null) {
				logger.error("AccountingStateData is null");
				throw new ServiceException("AccountingStateData is null!");
			}

			if (accountingStateData.getAccountingType() == null) {
				logger.error("Accounting Type is null");
				throw new ServiceException("Accounting Type is null!");
			}

			AccountingModelBuilder accountingModelBuilder = null;

			switch (accountingStateData.getAccountingType()) {
			case SERVICE:
				accountingModelBuilder = new AccountingModel4Service(
						accountingStateData);
				break;
			case STORAGE:
				accountingModelBuilder = new AccountingModel4Storage(
						accountingStateData);
				break;
			case JOB:
				accountingModelBuilder = new AccountingModel4Job(
						accountingStateData);
				break;
			case PORTLET:
			case TASK:
			default:
				logger.error("Accounting Type not supported!");
				throw new ServiceException("Accounting Type not supported!!");
			}

			AccountingModelDirector director = new AccountingModelDirector();
			director.setAccountingModelBuilder(accountingModelBuilder);
			director.constructAccountingModel();
			AccountingDataModel accountingDataModel = director
					.getAccountingModel();
			logger.debug("AccountingDataModel: " + accountingDataModel);

			if (accountingDataModel == null) {
				logger.error("Accounting data model created is null");
				throw new ServiceException(
						"Accounting data model created is null!");
			}

			Path tempFile = Files.createTempFile(accountingDataModel.getName(),
					FILE_EXTENSION);
			logger.debug("Temp File: " + tempFile.toString());

			try (OutputStream os = Files.newOutputStream(tempFile,
					StandardOpenOption.WRITE)) {
				XMLEncoder xmlEncoder = new XMLEncoder(os);
				xmlEncoder.writeObject(accountingDataModel);
				xmlEncoder.close();
				logger.debug("Archived:" + os);
			}

			StorageUtil storageUtil=new StorageUtil();
			String destinationFolderId = storageUtil
					.createAccountingFolderOnWorkspace(userName);
			ItemDescription itemDescription = null;
			try (InputStream is = Files.newInputStream(tempFile,
					StandardOpenOption.READ)) {
				itemDescription = storageUtil.saveOnWorkspace(userName,
						destinationFolderId, accountingDataModel.getName()
								+ FILE_EXTENSION,
						accountingDataModel.getName(), is);
			}

			try {
				Files.delete(tempFile);
			} catch (IOException e) {
				logger.error("Error in deleting temp file: "
						+ e.getLocalizedMessage());
				e.printStackTrace();
				throw new ServiceException("Error deleting temp file: "
						+ e.getLocalizedMessage(), e);
			}

			return itemDescription;

		} catch (ServiceException e) {
			throw e;
		} catch (Throwable e) {
			logger.error("Error saving xml data: " + e.getLocalizedMessage());
			e.printStackTrace();
			throw new ServiceException("Error saving xml data: "
					+ e.getLocalizedMessage());

		}

	}

	public ExportDescriptor download(AccountingStateData accountingStateData)
			throws ServiceException {
		try {
			logger.debug("AccountingStateData: " + accountingStateData);
			if (accountingStateData == null) {
				logger.error("AccountingStateData is null");
				throw new ServiceException("AccountingStateData is null!");
			}

			if (accountingStateData.getAccountingType() == null) {
				logger.error("Accounting Type is null");
				throw new ServiceException("Accounting Type is null!");
			}

			AccountingModelBuilder accountingModelBuilder = null;

			switch (accountingStateData.getAccountingType()) {
			case SERVICE:
				accountingModelBuilder = new AccountingModel4Service(
						accountingStateData);
				break;
			case STORAGE:
				accountingModelBuilder = new AccountingModel4Storage(
						accountingStateData);
				break;
			case JOB:
				accountingModelBuilder = new AccountingModel4Job(
						accountingStateData);
				break;
			case PORTLET:
			case TASK:
			default:
				logger.error("Accounting Type not supported!");
				throw new ServiceException("Accounting Type not supported!!");
			}

			logger.debug("AccountingModelBuilder: " + accountingModelBuilder);
			AccountingModelDirector director = new AccountingModelDirector();
			director.setAccountingModelBuilder(accountingModelBuilder);
			director.constructAccountingModel();
			AccountingDataModel accountingDataModel = director
					.getAccountingModel();
			logger.debug("AccountingDataModel: " + accountingDataModel);

			if (accountingDataModel == null) {
				logger.error("Accounting data model created is null");
				throw new ServiceException(
						"Accounting data model created is null!");
			}

			Path tempFile = Files.createTempFile(accountingDataModel.getName(),
					FILE_EXTENSION);
			logger.debug("Temp File: " + tempFile.toString());

			try (OutputStream os = Files.newOutputStream(tempFile,
					StandardOpenOption.WRITE)) {
				XMLEncoder xmlEncoder = new XMLEncoder(os);
				xmlEncoder.writeObject(accountingDataModel);
				xmlEncoder.close();
				logger.debug("Archived:" + os);
			}

			return new ExportDescriptor(tempFile, accountingDataModel,
					FILE_EXTENSION);

		} catch (ServiceException e) {
			throw e;
		} catch (Throwable e) {
			logger.error("Error downloading xml data: " + e.getLocalizedMessage());
			e.printStackTrace();
			throw new ServiceException("Error downloading xml data: "
					+ e.getLocalizedMessage());

		}

	}

}
