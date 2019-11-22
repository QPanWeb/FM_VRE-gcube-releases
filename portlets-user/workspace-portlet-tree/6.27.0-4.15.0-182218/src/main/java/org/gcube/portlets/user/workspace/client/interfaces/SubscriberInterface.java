package org.gcube.portlets.user.workspace.client.interfaces;

import java.util.ArrayList;
import java.util.List;

import org.gcube.portlets.user.workspace.client.ConstantsExplorer.ViewSwitchType;
import org.gcube.portlets.user.workspace.client.ConstantsExplorer.WS_UPLOAD_TYPE;
import org.gcube.portlets.user.workspace.client.model.FileModel;
import org.gcube.portlets.user.workspace.client.model.FolderModel;
import org.gcube.portlets.user.workspace.shared.WorkspaceTrashOperation;


// Implements this interface to receive events by tree async
/**
 * The Interface SubscriberInterface.
 *
 * @author Francesco Mangiacrapa francesco.mangiacrapa{@literal @}isti.cnr.it
 * Oct 6, 2015
 */
public interface SubscriberInterface {


	/**
	 * Added file.
	 *
	 * @param itemIdentifier the item identifier
	 * @param parentId the parent
	 * @param file the file
	 * @param isOverwrite the is overwrite
	 */
	void addedFile(String itemIdentifier, String parentId, WS_UPLOAD_TYPE file, boolean isOverwrite);

	/**
	 * Selected item.
	 *
	 * @param item the item
	 * @param parents the parents
	 */
	void selectedItem(FileModel item, List<FileModel> parents);

	/**
	 * Expand folder item.
	 *
	 * @param itemFolder the item folder
	 */
	void expandFolderItem(FolderModel itemFolder);

	/**
	 * Sets the parent item selected.
	 *
	 * @param listParents the new parent item selected
	 */
	void setParentItemSelected(ArrayList<FileModel> listParents);

	/**
	 * Rename item.
	 *
	 * @param itemIdentifier the item identifier
	 * @param newName the new name
	 * @param extension the extension
	 * @return true, if successful
	 */
	boolean renameItem(String itemIdentifier, String newName, String extension);


	/**
	 * Delete items.
	 *
	 * @param ids the ids
	 * @return true, if successful
	 */
	boolean deleteItems(List<String> ids);

	/**
	 * Added folder.
	 *
	 * @param itemIdentifier the item identifier
	 * @param parent the parent
	 */
	void addedFolder(String itemIdentifier, FileModel parent);

	/**
	 * Root loaded.
	 *
	 * @param root the root
	 */
	void rootLoaded(FileModel root);


	/**
	 * Smart folder selected.
	 *
	 * @param folderId the folder id
	 * @param gxtCategorySmartFolder the gxt category smart folder
	 */
	void smartFolderSelected(String folderId, GXTCategorySmartFolder gxtCategorySmartFolder);

	/**
	 * Moved items.
	 *
	 * @param sourceParentIdentifier the source parent identifier
	 * @param targetParent the target parent
	 */
	void movedItems(String sourceParentIdentifier, FileModel targetParent);

	/**
	 * Switch view.
	 *
	 * @param type the type
	 */
	void switchView(ViewSwitchType type);

	/**
	 * Refresh folder.
	 *
	 * @param fileModel the file model
	 * @param forceRefreshContent the force refresh content
	 * @param forceRefreshBreadcrumb the force refresh breadcrumb
	 */
	void refreshFolder(FileModel fileModel, boolean forceRefreshContent,
			boolean forceRefreshBreadcrumb);

	/**
	 * File downloaded.
	 *
	 * @param itemIdentifier the item identifier
	 */
	void fileDownloaded(String itemIdentifier);

	/**
	 * View session expired panel.
	 */
	void viewSessionExpiredPanel();


	/**
	 * Move event is completed.
	 *
	 * @param isTreeRefreshable the is tree refreshable
	 * @param folderDestinationId the folder destination id
	 */
	void moveEventIsCompleted(boolean isTreeRefreshable, String folderDestinationId);


	/**
	 * Copy event is completed.
	 *
	 * @param folderDestinationId the folder destination id
	 */
	void copyEventIsCompleted(String folderDestinationId);

	/**
	 * Trash event.
	 *
	 * @param trashOperation the trash operation
	 * @param targetFileModels the target file models
	 */
	void trashEvent(WorkspaceTrashOperation trashOperation,
			List<FileModel> targetFileModels);

	/**
	 * Updated VRE permissions.
	 *
	 * @param vreFolder the vre folder
	 */
	void updatedVREPermissions(FileModel vreFolder);

	/**
	 * Changed file model id.
	 *
	 * @param oldId the old id
	 * @param newId the new id
	 */
	void changedFileModelId(String oldId, String newId);

	/**
	 * Update worksapace size.
	 *
	 * @param delayCall the delay call
	 */
	void updateWorksapaceSize(boolean delayCall);



	/**
	 * Versioning history.
	 *
	 * @param file the file
	 */
	void versioningHistory(FileModel file);


	/**
	 * Load folder.
	 *
	 * @param folderTarget the folder target
	 */
	void loadFolder(FileModel folderTarget);

}
