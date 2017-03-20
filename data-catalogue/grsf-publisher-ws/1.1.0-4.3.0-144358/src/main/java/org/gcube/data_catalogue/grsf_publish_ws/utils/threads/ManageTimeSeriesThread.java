package org.gcube.data_catalogue.grsf_publish_ws.utils.threads;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.gcube.common.authorization.library.provider.SecurityTokenProvider;
import org.gcube.common.homelibrary.home.HomeLibrary;
import org.gcube.common.homelibrary.home.exceptions.HomeNotFoundException;
import org.gcube.common.homelibrary.home.exceptions.InternalErrorException;
import org.gcube.common.homelibrary.home.exceptions.UserNotFoundException;
import org.gcube.common.homelibrary.home.workspace.Workspace;
import org.gcube.common.homelibrary.home.workspace.WorkspaceFolder;
import org.gcube.common.homelibrary.home.workspace.WorkspaceSharedFolder;
import org.gcube.common.homelibrary.home.workspace.catalogue.WorkspaceCatalogue;
import org.gcube.common.homelibrary.home.workspace.exceptions.ItemNotFoundException;
import org.gcube.common.homelibrary.home.workspace.exceptions.WorkspaceFolderNotFoundException;
import org.gcube.common.homelibrary.home.workspace.folder.items.ExternalFile;
import org.gcube.common.scope.api.ScopeProvider;
import org.gcube.data_catalogue.grsf_publish_ws.custom_annotations.CustomField;
import org.gcube.data_catalogue.grsf_publish_ws.custom_annotations.TimeSeries;
import org.gcube.data_catalogue.grsf_publish_ws.json.input.Common;
import org.gcube.data_catalogue.grsf_publish_ws.json.input.FisheryRecord;
import org.gcube.data_catalogue.grsf_publish_ws.json.input.StockRecord;
import org.gcube.data_catalogue.grsf_publish_ws.utils.CSVHelpers;
import org.gcube.data_catalogue.grsf_publish_ws.utils.HelperMethods;
import org.gcube.data_catalogue.grsf_publish_ws.utils.cache.CacheImpl;
import org.gcube.data_catalogue.grsf_publish_ws.utils.cache.CacheInterface;
import org.gcube.datacatalogue.ckanutillibrary.server.DataCatalogue;
import org.slf4j.LoggerFactory;

import eu.trentorise.opendata.jackan.model.CkanResourceBase;

/**
 * Extract the time series present in the record, load them as resource on ckan and on the .catalogue
 * folder under the vre folder.
 * @author Costantino Perciante at ISTI-CNR (costantino.perciante@isti.cnr.it)
 */
public class ManageTimeSeriesThread extends Thread{

	private static final String CSV_FILE_FORMAT = ".csv";
	private static final String PATH_SEPARATOR = "/";

	// Logger
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ManageTimeSeriesThread.class);

	// try to attach the source at most CANCHES times ..
	private static final int CANCHES = 3;

	private static CacheInterface<String, WorkspaceCatalogue> vreFolderCache = new CacheImpl<String, WorkspaceCatalogue>(1000 * 60 * 120);

	private Common record;
	private String uuidKB;
	private String username;
	private DataCatalogue catalogue;
	private String context;
	private String token;

	/**
	 * @param record
	 * @param packageId
	 * @param username
	 * @param catalogue
	 * @param context
	 */
	public ManageTimeSeriesThread(Common record, String packageName,
			String username, DataCatalogue catalogue, String context, String token) {
		super();
		this.record = record;
		this.uuidKB = packageName;
		this.username = username;
		this.catalogue = catalogue;
		this.context = context;
		this.token = token;
	}

	@Override
	public void run() {
		logger.info("Time series manager thread started");

		ScopeProvider.instance.set(context);
		SecurityTokenProvider.instance.set(token);

		try {
			manageTimeSeries(record, uuidKB, username, catalogue);
			logger.info("The time series manager thread ended correctly");
			return;
		} catch (IllegalAccessException e) {
			logger.error("Error was " + e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error("Error was " + e.getMessage());
		} catch (InvocationTargetException e) {
			logger.error("Error was " + e.getMessage());
		} catch (WorkspaceFolderNotFoundException e) {
			logger.error("Error was " + e.getMessage());
		} catch (ItemNotFoundException e) {
			logger.error("Error was " + e.getMessage());
		} catch (IntrospectionException e) {
			logger.error("Error was " + e.getMessage());
		} catch (InternalErrorException e) {
			logger.error("Error was " + e.getMessage());
		} catch (HomeNotFoundException e) {
			logger.error("Error was " + e.getMessage());
		} catch (UserNotFoundException e) {
			logger.error("Error was " + e.getMessage());
		}finally{
			ScopeProvider.instance.reset();
			SecurityTokenProvider.instance.reset();
		}

		logger.error("Failed to attach csv files to the product...");
	}

	/**
	 * Manage the time series bean within a resource (e.g., catches or landings, exploitation rate and so on).
	 * The method save the time series as csv on ckan, and also save the file in the .catalogue area of the shared vre folder.
	 * @param record
	 * @throws IntrospectionException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws UserNotFoundException 
	 * @throws HomeNotFoundException 
	 * @throws InternalErrorException 
	 * @throws WorkspaceFolderNotFoundException 
	 * @throws ItemNotFoundException 
	 */
	@SuppressWarnings("rawtypes")
	public static void manageTimeSeries(Common record, String uuidKB, String username, DataCatalogue catalogue) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, IntrospectionException, WorkspaceFolderNotFoundException, InternalErrorException, HomeNotFoundException, UserNotFoundException, ItemNotFoundException{

		if(record == null)
			throw new IllegalArgumentException("The given record is null!!");

		String token = SecurityTokenProvider.instance.get();

		WorkspaceCatalogue catalogueFolder = null;
		if((catalogueFolder = vreFolderCache.get(token)) == null){
			Workspace ws = HomeLibrary.getHomeManagerFactory().getHomeManager().getHome().getWorkspace();
			WorkspaceSharedFolder vreFolder = ws.getVREFolderByScope(ScopeProvider.instance.get());
			catalogueFolder = vreFolder.getVRECatalogue();
			vreFolderCache.insert(token, catalogueFolder);
		}

		logger.debug("Catalogue folder in vre has path " + catalogueFolder.getPath());

		// the structure under the .catalogue will be as follows:
		// .catalogue:
		//	- stock:
		//		- first_letter_of_the_product (knowledge base uuid)
		//			- knowledge base uuid
		//				- type of files (e.g., csv)
		//					-files (e.g, kbuuid.csv)
		//	- fishery 
		//		- first_letter_of_the_product (knowledge base uuid)
		//			- knowledge base uuid
		//				- type of files (e.g., csv)
		//					-files (e.g, kbuuid.csv)

		String recordTypeFolderName = record.getGrsfType().toLowerCase();
		String productName = record.getClass().equals(StockRecord.class) ? ((StockRecord)record).getStockName() : ((FisheryRecord)record).getFisheryName();
		char firstLetter = uuidKB.charAt(0);

		// the whole path of the directory is going to be...
		String csvDirectoryForThisProduct = recordTypeFolderName + PATH_SEPARATOR + firstLetter + PATH_SEPARATOR + replaceIllegalChars(uuidKB) + PATH_SEPARATOR + "csv";
		logger.debug("The path under which the time series are going to be saved is " + csvDirectoryForThisProduct);
		WorkspaceFolder csvFolder = HelperMethods.createOrGetSubFoldersByPath(catalogueFolder, csvDirectoryForThisProduct);

		if(csvFolder != null){

			String apiKeyUser = catalogue.getApiKeyFromUsername(username);

			Class<?> current = record.getClass();
			do{
				Field[] fields = current.getDeclaredFields();
				for (Field field : fields) {
					if (field.isAnnotationPresent(TimeSeries.class)) {

						Object f = new PropertyDescriptor(field.getName(), current).getReadMethod().invoke(record);
						if(f != null){

							List asList = (List)f;

							if(!asList.isEmpty()){

								CustomField customAnnotation = field.getAnnotation(CustomField.class);
								logger.debug("A time series has been just found (from field " + customAnnotation.key() + ")");
								String resourceToAttachOnCkanName = (replaceIllegalChars(productName) + "_" + customAnnotation.key()).replaceAll("\\s", "_").replaceAll("[_]+", "_") + CSV_FILE_FORMAT;
								String resourceToAttachOnCkanDescription = productName + " : " +  customAnnotation.key() + " time series";

								File csvFile = CSVHelpers.listToCSV(asList);

								CkanResourceBase ckanResource = null;
								ExternalFile createdFileOnWorkspace = null;
								if(csvFile != null){

									for (int i = 0; i < CANCHES; i++) {

										// upload this file on ckan
										if(ckanResource == null)
											ckanResource = uploadFileOnCatalogue(csvFile, uuidKB, catalogue, username, resourceToAttachOnCkanName, resourceToAttachOnCkanDescription, apiKeyUser);

										//upload this file on the folder of the vre (under .catalogue) and change the url of the resource
										if(ckanResource != null){

											if(createdFileOnWorkspace == null)
												createdFileOnWorkspace = HelperMethods.uploadExternalFile(csvFolder, uuidKB + CSV_FILE_FORMAT, resourceToAttachOnCkanDescription, csvFile);

											if(createdFileOnWorkspace != null){

												String publicUrlToSetOnCkan = createdFileOnWorkspace.getPublicLink(true);
												logger.info("going to patch the created resource with id " + ckanResource.getId() + " with url " + publicUrlToSetOnCkan);
												boolean updated = catalogue.patchResource(ckanResource.getId(), publicUrlToSetOnCkan, resourceToAttachOnCkanName, resourceToAttachOnCkanDescription, "", apiKeyUser);

												if(updated){
													logger.info("Resource has been updated with the new url");
													break;
												}

											}
										}

									}

									// delete the file
									csvFile.delete();
								}
							}
						}
					}
				}
			}
			while((current = current.getSuperclass())!=null); // iterate from the inherited class up to the Object.class
		}
	}

	/**
	 * Replace chars
	 * @param productName
	 * @return
	 */
	private static String replaceIllegalChars(String productName) {
		return productName.replaceAll("[/\\[\\],|:*.+]", "_");
	}

	/**
	 * Upload a resource on ckan
	 * @param csvFile
	 * @param packageName
	 * @param catalogue
	 * @param username
	 * @param resourceToAttachName
	 * @param description
	 * @return a ckan resource on success, null otherwise
	 */
	private static CkanResourceBase uploadFileOnCatalogue(File csvFile,
			String uuidKB, DataCatalogue catalogue, String username,
			String resourceToAttachName, String description, String apiKey) {
		return catalogue.uploadResourceFile(
				csvFile, 
				uuidKB, 
				apiKey, 
				resourceToAttachName, 
				description);
	}
}
