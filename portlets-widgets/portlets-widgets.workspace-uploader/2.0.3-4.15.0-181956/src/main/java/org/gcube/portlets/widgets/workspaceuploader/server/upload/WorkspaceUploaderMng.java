/**
 *
 */
package org.gcube.portlets.widgets.workspaceuploader.server.upload;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.gcube.common.storagehub.model.exceptions.UserNotAuthorizedException;
import org.gcube.common.storagehubwrapper.server.StorageHubWrapper;
import org.gcube.common.storagehubwrapper.shared.tohl.WorkspaceFolder;
import org.gcube.common.storagehubwrapper.shared.tohl.WorkspaceItem;
import org.gcube.common.storagehubwrapper.shared.tohl.exceptions.InsufficientPrivilegesException;
import org.gcube.common.storagehubwrapper.shared.tohl.exceptions.InternalErrorException;
import org.gcube.common.storagehubwrapper.shared.tohl.exceptions.ItemAlreadyExistException;
import org.gcube.common.storagehubwrapper.shared.tohl.exceptions.WorkspaceFolderNotFoundException;
import org.gcube.common.storagehubwrapper.shared.tohl.exceptions.WrongDestinationException;
import org.gcube.common.storagehubwrapper.shared.tohl.items.FileItem;
import org.gcube.common.storagehubwrapper.shared.tohl.items.WorkspaceVersion;
import org.gcube.portlets.widgets.workspaceuploader.server.WorkspaceUploadServletStream;
import org.gcube.portlets.widgets.workspaceuploader.server.util.WsUtil;
import org.gcube.portlets.widgets.workspaceuploader.shared.WorkspaceUploaderItem;
import org.gcube.portlets.widgets.workspaceuploader.shared.WorkspaceUploaderItem.UPLOAD_STATUS;
import org.gcube.vomanagement.usermanagement.model.GCubeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class WorkspaceUploaderManager.
 *
 * @author Francesco Mangiacrapa francesco.mangiacrapa@isti.cnr.it
 * Sep 2, 2015
 */
public class WorkspaceUploaderMng {

	public static Logger logger = LoggerFactory.getLogger(WorkspaceUploaderMng.class);


	/**
	 * Instantiates a new workspace uploader manager.
	 */
	public WorkspaceUploaderMng() {
	}


	/**
	 * Creates the workspace uploader file.
	 *
	 * @param storageWrapper the storage wrapper
	 * @param currUser the curr user
	 * @param scopeGroupId the scope group id
	 * @param request the request
	 * @param workspaceUploader the workspace uploader
	 * @param httpSession the http session
	 * @param isOvewrite the is ovewrite
	 * @param uploadFile the upload file
	 * @param itemName the item name
	 * @param destinationFolder the destination folder
	 * @param contentType the content type
	 * @param totalBytes the total bytes
	 * @return the workspace uploader item
	 * @throws InternalErrorException the internal error exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static WorkspaceUploaderItem createWorkspaceUploaderFile(StorageHubWrapper storageWrapper, final GCubeUser currUser, final String scopeGroupId, final HttpServletRequest request, final WorkspaceUploaderItem workspaceUploader, final HttpSession httpSession, final boolean isOvewrite, final InputStream uploadFile, final String itemName,  final WorkspaceFolder destinationFolder, final String contentType, final long totalBytes) throws InternalErrorException, IOException{
		logger.debug("Creating WorkspaceUploaderFile to user: " +currUser.getUsername()+ ", filename: "+itemName+", destinationFolder: "+destinationFolder);

		workspaceUploader.setUploadStatus(UPLOAD_STATUS.IN_PROGRESS);
		workspaceUploader.setStatusDescription("Uploading "+itemName);
		WorkspaceItem createdItem = null;

		try{
			
			Long startTime = WorkspaceUploadServletStream.printStartTime();
			//UPLOAD
			createdItem = storageWrapper.getWorkspace().uploadFile(destinationFolder.getId(), uploadFile, itemName, "");

			if(createdItem!=null){
				WorkspaceUploadServletStream.printElapsedTime(startTime);
				logger.debug("StorageHub file: "+createdItem.getName() + " with id: "+createdItem.getId() + " uploaded correctly in "+destinationFolder.getPath());
				workspaceUploader.getFile().setItemId(createdItem.getId()); //SET STORAGEHUB ID
				workspaceUploader.getFile().setParentId(createdItem.getParentId());//SET STORAGEHUB PARENT ID

				//ADDING VERSION NAME
				if(createdItem instanceof FileItem){
					FileItem file = (FileItem) createdItem;
					WorkspaceVersion currVersion = file.getCurrentVersion();
					if(currVersion!=null){
						workspaceUploader.getFile().setVersionName(currVersion.getName());
						logger.debug("StorageHub file: "+createdItem.getName() + " has version: "+currVersion.getName());
					}
					else{
						//TODO NO VERSION
						//logger.debug("StorageHub file: "+createdItem.getName() + " has no current version");
						//workspaceUploader.getFile().setVersionName(file.getName());
					}

				}else
					logger.debug("StorageHub file: "+createdItem.getName() + " has not version");

				workspaceUploader.setStatusDescription("File \""+createdItem.getName()+"\" uploaded correctly in "+destinationFolder.getPath());
				workspaceUploader.setUploadStatus(UPLOAD_STATUS.COMPLETED);
				WorkspaceUploadServletStream.notifyUploadInSharedFolder(storageWrapper, currUser, scopeGroupId, request, httpSession, createdItem.getId(), createdItem.getParentId(), isOvewrite);
			}else{
				workspaceUploader.setStatusDescription("Error on uploading: \""+itemName + "\". Try again");
				workspaceUploader.setUploadStatus(UPLOAD_STATUS.FAILED);
			}
			try {
				WsUtil.putWorkspaceUploaderInSession(httpSession, workspaceUploader);
			} catch (Exception e1) {
				logger.error("Error during WorkspaceUploaderItem session update: ",e1);
			}
		//TODO TO BE REMOVED
		} catch (InternalErrorException e) {
			logger.error("Error during upload: ",e);
			workspaceUploader.setStatusDescription("Error on uploading: "+itemName+". "+e.getMessage());
			workspaceUploader.setUploadStatus(UPLOAD_STATUS.FAILED);
			try {
				WsUtil.putWorkspaceUploaderInSession(httpSession, workspaceUploader);
			} catch (Exception e1) {
				logger.error("Error during WorkspaceUploaderItem session update: ",e1);
			}
		//TODO TO BE REMOVED
		} catch (InsufficientPrivilegesException | ItemAlreadyExistException  | WorkspaceFolderNotFoundException | WrongDestinationException e) {
			logger.error("Error during file uploading: ",e);
			workspaceUploader.setUploadStatus(UPLOAD_STATUS.FAILED);

			if (e instanceof InsufficientPrivilegesException){
				String folderName = destinationFolder.getName();
				workspaceUploader.setStatusDescription("You have not permission to upload in the folder: "+folderName);
			}else{
				workspaceUploader.setStatusDescription("An error occurred during upload: "+itemName+". "+e.getMessage());
			}
			try {
				WsUtil.putWorkspaceUploaderInSession(httpSession, workspaceUploader);
			} catch (Exception e1) {
				logger.error("Error during WorkspaceUploaderItem session update: ",e1);
			}
			//IS unreachable
		}catch(UploadCanceledException e){
			logger.info("UploadCanceledException thrown by client..");
			workspaceUploader.setStatusDescription("Aborted upload: "+itemName);
			workspaceUploader.setUploadStatus(UPLOAD_STATUS.ABORTED);
			try {
//				WsUtil.putWorkspaceUploaderInSession(httpSession, workspaceUploader);
//				workspaceUploader.setErasable(true);
				WsUtil.forceEraseWorkspaceUploaderInSession(httpSession, workspaceUploader);
			} catch (Exception e1) {
				logger.error("Error during WorkspaceUploaderItem session update: ",e1);
			}
		
		//TO STORAGEHUB EXCEPTION
		}catch(Exception e){
			logger.error("Error occurred uploading the file: ",e);
			workspaceUploader.setUploadStatus(UPLOAD_STATUS.FAILED);
			workspaceUploader.setStatusDescription("An error occurred uploading the file: "+itemName+". "+e.getMessage());
			
			if (e instanceof UserNotAuthorizedException){
				String folderName = destinationFolder.getName();
				workspaceUploader.setStatusDescription("You have not permission to upload in the folder: "+folderName);
			}
			try {
				WsUtil.putWorkspaceUploaderInSession(httpSession, workspaceUploader);
			} catch (Exception e1) {
				logger.error("Error during WorkspaceUploaderItem session update: ",e1);
			}
		}finally{
			try {
//				StreamUtils.deleteTempFile(uploadFile);
				WsUtil.setErasableWorkspaceUploaderInSession(request, workspaceUploader.getIdentifier());
			} catch (Exception e2) {
				logger.error("Error during setErasableWorkspaceUploaderInSession session update: ",e2);
			}
		}

		return workspaceUploader;
	}


	/**
	 * Creates the workspace uploader archive.
	 *
	 * @param storageWrapper the storage wrapper
	 * @param currUser the curr user
	 * @param scopeGroupId the scope group id
	 * @param workspaceUploader the workspace uploader
	 * @param request the request
	 * @param uploadArchive the upload archive
	 * @param itemName the item name
	 * @param destinationFolder the destination folder
	 * @param totalBytes the total bytes
	 * @return the workspace uploader item
	 * @throws InternalErrorException the internal error exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static WorkspaceUploaderItem createWorkspaceUploaderArchive(StorageHubWrapper storageWrapper, final GCubeUser currUser, final String scopeGroupId, final WorkspaceUploaderItem workspaceUploader, final HttpServletRequest request, final InputStream uploadArchive, final String itemName, final WorkspaceFolder destinationFolder, final long totalBytes) throws InternalErrorException, IOException{
		HttpSession httpSession  = request.getSession();
		logger.info("calling upload archive - [itemName: "+itemName+"]");

		WorkspaceItem createdItem = null;
		try {
			workspaceUploader.setUploadStatus(UPLOAD_STATUS.IN_PROGRESS);
			workspaceUploader.setStatusDescription("Uploading "+itemName);
			createdItem = storageWrapper.getWorkspace().uploadArchive(destinationFolder.getId(), uploadArchive, itemName);

			if(createdItem!=null){
				logger.debug("StorageHub"+createdItem.getName() + " with id: "+createdItem.getId() + " uploaded correctly in "+destinationFolder.getPath());
				workspaceUploader.getFile().setItemId(createdItem.getId()); //SET STORAGEHUB ID
				workspaceUploader.getFile().setParentId(createdItem.getParentId());//SET STORAGEHUB PARENT ID

				//ADDING VERSION NAME
				if(createdItem instanceof FileItem){
					FileItem file = (FileItem) createdItem;
					WorkspaceVersion currVersion = file.getCurrentVersion();
					if(currVersion!=null){
						workspaceUploader.getFile().setVersionName(currVersion.getName());
						logger.debug("StorageHub file: "+createdItem.getName() + " has version: "+currVersion.getName());
					}
					else{
						//TODO NO VERSION
						//logger.debug("StorageHub file: "+createdItem.getName() + " has no current version");
						//workspaceUploader.getFile().setVersionName(file.getName());
					}

				}else
					logger.debug("StorageHub file: "+createdItem.getName() + " has not version");

				workspaceUploader.setStatusDescription("Archive \""+createdItem.getName()+"\" uploaded correctly in "+destinationFolder.getPath());
				workspaceUploader.setUploadStatus(UPLOAD_STATUS.COMPLETED);
				final boolean isOvewrite = false;
				WorkspaceUploadServletStream.notifyUploadInSharedFolder(storageWrapper, currUser, scopeGroupId, request, httpSession, createdItem.getId(), createdItem.getParentId(), isOvewrite);
			}else{
				workspaceUploader.setStatusDescription("Error on uploading: \""+itemName + "\". Try again");
				workspaceUploader.setUploadStatus(UPLOAD_STATUS.FAILED);
			}

			try {
				WsUtil.putWorkspaceUploaderInSession(httpSession, workspaceUploader);
			} catch (Exception e1) {
				logger.error("Error during WorkspaceUploaderItem session update: ",e1);
			}
		//TODO TO BE REMOVED
		} catch (InternalErrorException e) {
			logger.error("Error during upload: ",e);
			workspaceUploader.setStatusDescription("Error on uploading: "+itemName+". "+e.getMessage());
			workspaceUploader.setUploadStatus(UPLOAD_STATUS.FAILED);
			try {
				WsUtil.putWorkspaceUploaderInSession(httpSession, workspaceUploader);
			} catch (Exception e1) {
				logger.error("Error during WorkspaceUploaderItem session update: ",e1);
			}
		//TODO TO BE REMOVED
		} catch (InsufficientPrivilegesException | ItemAlreadyExistException  | WorkspaceFolderNotFoundException | WrongDestinationException e) {
			logger.error("Error during file uploading: ",e);
			workspaceUploader.setUploadStatus(UPLOAD_STATUS.FAILED);

			if (e instanceof InsufficientPrivilegesException){
				String folderName = destinationFolder.getName();
				workspaceUploader.setStatusDescription("You have not permission to upload in the folder: "+folderName);
			}else{
				workspaceUploader.setStatusDescription("An error occurred during upload: "+itemName+". "+e.getMessage());
			}
			try {
				WsUtil.putWorkspaceUploaderInSession(httpSession, workspaceUploader);
			} catch (Exception e1) {
				logger.error("Error during WorkspaceUploaderItem session update: ",e1);
			}
		//IS unreachable
		}catch(UploadCanceledException e){
			logger.info("UploadCanceledException thrown by client..");
			workspaceUploader.setStatusDescription("Aborted upload: "+itemName);
			workspaceUploader.setUploadStatus(UPLOAD_STATUS.ABORTED);
			try {
//				WsUtil.putWorkspaceUploaderInSession(httpSession, workspaceUploader);
//				workspaceUploader.setErasable(true);
				WsUtil.forceEraseWorkspaceUploaderInSession(httpSession, workspaceUploader);
			} catch (Exception e1) {
				logger.error("Error during WorkspaceUploaderItem session update: ",e1);
			}

		//TO STORAGEHUB EXCEPTION
		}catch(Exception e){
			logger.error("Error occurred uploading the archive: ",e);
			workspaceUploader.setUploadStatus(UPLOAD_STATUS.FAILED);
			workspaceUploader.setStatusDescription("An error occurred uploading the archive: "+itemName+". "+e.getMessage());
			
			if (e instanceof UserNotAuthorizedException){
				String folderName = destinationFolder.getName();
				workspaceUploader.setStatusDescription("You have not permission to upload in the folder: "+folderName);
			}
			try {
				WsUtil.putWorkspaceUploaderInSession(httpSession, workspaceUploader);
			} catch (Exception e1) {
				logger.error("Error during WorkspaceUploaderItem session update: ",e1);
			}
		}finally{
			try {
//				StreamUtils.deleteTempFile(uploadFile);
				WsUtil.setErasableWorkspaceUploaderInSession(request, workspaceUploader.getIdentifier());
			} catch (Exception e2) {
				logger.error("Error during setErasableWorkspaceUploaderInSession session update: ",e2);
			}
		}
		return workspaceUploader;
	}


	/**
 * Upload file.
 *
 * @param storageWrapper the storage wrapper
 * @param currUser the curr user
 * @param scopeGroupId the scope group id
 * @param request the request
 * @param workspaceUploader the workspace uploader
 * @param httpSession the http session
 * @param itemName the item name
 * @param file the file
 * @param destinationFolder the destination folder
 * @param contentType the content type
 * @param isOverwrite the is overwrite
 * @param totolaBytes the totola bytes
 * @return the workspace uploader item
 * @throws Exception the exception
 */
	public static WorkspaceUploaderItem uploadFile(StorageHubWrapper storageWrapper, GCubeUser currUser, String scopeGroupId, HttpServletRequest request, WorkspaceUploaderItem workspaceUploader, HttpSession httpSession, String itemName, InputStream file, WorkspaceFolder destinationFolder, String contentType, boolean isOverwrite, long totolaBytes) throws Exception {

		return createWorkspaceUploaderFile(storageWrapper, currUser, scopeGroupId, request, workspaceUploader, httpSession, isOverwrite, file, itemName, destinationFolder, contentType, totolaBytes);
	}


	/**
	 * Upload archive.
	 *
	 * @param storageWrapper the storage wrapper
	 * @param currUser the curr user
	 * @param scopeGroupId the scope group id
	 * @param workspaceUploader the workspace uploader
	 * @param request the request
	 * @param itemName the item name
	 * @param file the file
	 * @param destinationFolder the destination folder
	 * @param totalBytes the total bytes
	 * @return the workspace uploader item
	 * @throws Exception the exception
	 */
	public static WorkspaceUploaderItem uploadArchive(StorageHubWrapper storageWrapper, GCubeUser currUser, String scopeGroupId, WorkspaceUploaderItem workspaceUploader, HttpServletRequest request, String itemName, InputStream file, WorkspaceFolder destinationFolder, long totalBytes) throws Exception {

		return createWorkspaceUploaderArchive(storageWrapper, currUser, scopeGroupId, workspaceUploader, request, file, itemName, destinationFolder, totalBytes);
	}

	/**
	 * Upload file status.
	 *
	 * @param request the request
	 * @param workspaceUploader the workspace uploader
	 * @return the workspace uploader item
	 * @throws Exception the exception
	 */
	public static WorkspaceUploaderItem uploadFileStatus(HttpServletRequest request, WorkspaceUploaderItem workspaceUploader) throws Exception {
		return WsUtil.getWorkspaceUploaderInSession(request, workspaceUploader.getIdentifier());
	}

//	/**
//	 * Overwrite item.
//	 *
//	 * @param wa the wa
//	 * @param itemName the item name
//	 * @param fileData the file data
//	 * @param destinationFolder the destination folder
//	 * @return the folder item
//	 * @throws ItemNotFoundException the item not found exception
//	 * @throws WrongItemTypeException the wrong item type exception
//	 * @throws InternalErrorException the internal error exception
//	 * @throws InsufficientPrivilegesException the insufficient privileges exception
//	 * @throws WorkspaceFolderNotFoundException the workspace folder not found exception
//	 * @throws ItemAlreadyExistException the item already exist exception
//	 * @throws WrongDestinationException the wrong destination exception
//	 */
//	private static FileItem overwriteItem(Workspace wa, String itemName, InputStream fileData, WorkspaceFolder destinationFolder) throws ItemNotFoundException, WrongItemTypeException, InternalErrorException, InsufficientPrivilegesException, WorkspaceFolderNotFoundException, ItemAlreadyExistException, WrongDestinationException{
//
//		FileItem overwriteItem = null;
//		logger.debug("case overwriting item.. "+itemName);
//		overwriteItem = (FileItem) wa.find(itemName, destinationFolder.getId());
//		logger.debug("overwriteItem item was found, id is: "+overwriteItem.getId());
//		wa.updateItem(overwriteItem.getId(), fileData);
//		logger.debug("updateItem with id: "+overwriteItem.getId()+ ", is completed");
//		return overwriteItem;
//
//	}
}
