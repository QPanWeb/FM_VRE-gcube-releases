package org.gcube.common.homelibrary.jcr.workspace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpException;
import org.apache.jackrabbit.util.Text;
import org.gcube.common.homelibary.model.items.ItemDelegate;
import org.gcube.common.homelibary.model.items.type.NodeProperty;
import org.gcube.common.homelibary.model.items.type.PrimaryNodeType;
import org.gcube.common.homelibary.model.items.type.WorkspaceItemType;
import org.gcube.common.homelibary.model.util.WorkspaceItemAction;
import org.gcube.common.homelibrary.home.Home;
import org.gcube.common.homelibrary.home.HomeLibrary;
import org.gcube.common.homelibrary.home.HomeManager;
import org.gcube.common.homelibrary.home.exceptions.HomeNotFoundException;
import org.gcube.common.homelibrary.home.exceptions.InternalErrorException;
import org.gcube.common.homelibrary.home.exceptions.UserNotFoundException;
import org.gcube.common.homelibrary.home.workspace.WorkspaceFolder;
import org.gcube.common.homelibrary.home.workspace.WorkspaceSharedFolder;
import org.gcube.common.homelibrary.home.workspace.accessmanager.ACLType;
import org.gcube.common.homelibrary.home.workspace.exceptions.InsufficientPrivilegesException;
import org.gcube.common.homelibrary.home.workspace.exceptions.ItemAlreadyExistException;
import org.gcube.common.homelibrary.home.workspace.exceptions.ItemNotFoundException;
import org.gcube.common.homelibrary.home.workspace.exceptions.WorkspaceFolderNotFoundException;
import org.gcube.common.homelibrary.home.workspace.exceptions.WrongDestinationException;
import org.gcube.common.homelibrary.home.workspace.exceptions.WrongItemTypeException;
import org.gcube.common.homelibrary.home.workspace.folder.FolderItem;
import org.gcube.common.homelibrary.home.workspace.usermanager.GCubeGroup;
import org.gcube.common.homelibrary.home.workspace.usermanager.UserManager;
import org.gcube.common.homelibrary.jcr.workspace.accessmanager.JCRAccessManager;
import org.gcube.common.homelibrary.jcr.workspace.accessmanager.JCRPrivilegesInfo;
import org.gcube.common.homelibrary.jcr.workspace.accounting.JCRAccountingFolderEntryRemoval;
import org.gcube.common.homelibrary.jcr.workspace.servlet.JCRServlets;
import org.gcube.common.homelibrary.jcr.workspace.servlet.wrapper.DelegateManager;
import org.gcube.common.homelibrary.jcr.workspace.usermanager.JCRUserManager;
import org.gcube.common.homelibrary.model.exceptions.RepositoryException;
import org.gcube.common.homelibrary.util.WorkspaceUtil;
import org.gcube.contentmanagement.blobstorage.transport.backend.RemoteBackendException;

import com.thoughtworks.xstream.XStream;


public class JCRWorkspaceSharedFolder extends JCRAbstractWorkspaceFolder implements WorkspaceSharedFolder {

	private String applicationName;
	private String destinationFolderId;
	private String itemName;
	private List<String> users;

	public JCRWorkspaceSharedFolder(JCRWorkspace workspace, ItemDelegate delegate) throws RepositoryException, InternalErrorException {
		super(workspace,delegate);	

	}

	public JCRWorkspaceSharedFolder(JCRWorkspace workspace, ItemDelegate delegate,
			String name, String description, String originalDestinationFolderId, List<String> users, String applicationName, String itemName) throws RepositoryException, InternalErrorException {		

		super(workspace,delegate,name,description);

		this.destinationFolderId = originalDestinationFolderId;
		this.applicationName = applicationName;
		this.itemName = itemName;
		this.users = users;

	}

	public JCRWorkspaceSharedFolder(JCRWorkspace workspace, ItemDelegate delegate,
			String name, String description, String originalDestinationFolderId, List<String> users, String applicationName, String itemName, String displayName, boolean isVreFolder) throws RepositoryException, InternalErrorException {		

		this(workspace,delegate,name,description, originalDestinationFolderId, users, applicationName, itemName);

		delegate.getProperties().put(NodeProperty.IS_VRE_FOLDER, new XStream().toXML(isVreFolder));
		delegate.getProperties().put(NodeProperty.DISPLAY_NAME, displayName);

	}




	private void shareWithUses(List<String> users) throws RepositoryException {
		try {
			// The save method creates a clone of the sharable node
			//for the owner and all users below user roots.
			addUser(workspace.getOwner().getPortalLogin(), destinationFolderId);

			logger.trace("Share with " + users.toString());
			for (String user : users) {

				HomeManager homeManager = workspace.getHome().getHomeManager();
				Home home = homeManager.getHome(user);

				if (applicationName==null){
					if (isVreFolder())
						addUser(user, home.getWorkspace().getMySpecialFolders().getId());
					else
						addUser(user, home.getWorkspace().getRoot().getId());
				}
				else
					addUser(user, home.getDataArea().getApplicationRoot(applicationName).getId());
			}
		} catch (Exception e) {
			throw new RepositoryException(e.getMessage());
		}

	}



	/**
	 * Resolve groupIds and add user/group to member Node
	 * @param usersList
	 * @return a list of users (no group ids)
	 * @throws InternalErrorException
	 */
	@SuppressWarnings("unchecked")
	private List<String> listUsers(List<String> usersList) throws InternalErrorException {
		JCRServlets servlets = null;
		List<String> users = new ArrayList<String>();
		try {
			servlets = new JCRServlets(workspace.getOwner().getPortalLogin());
			List<String> groups = new ArrayList<String>();

			//get a list of groups
			JCRUserManager userManager = new JCRUserManager();
			List<GCubeGroup> groupsList = userManager.getGroups();		
			for (GCubeGroup group : groupsList){
				groups.add(group.getName());
			}

			//get members already in list
			List<String> memberIds = (List<String>) new XStream().fromXML(delegate.getProperties().get(NodeProperty.MEMBERS));
			if (memberIds==null)
				memberIds = new ArrayList<String>();			

			for (String user: usersList){	
				if (!isVreFolder()){				
					if(!user.endsWith("-Manager") && (!memberIds.contains(user))){
						memberIds.add(user);
						logger.info(user + " add to membersList");
					}
				}

				//resolve groups
				if (groups.contains(user)) {
					logger.info("User " + user + " is a Group, resolve group id");
					//if a user is a group, resolve group
					List<String> userList = workspace.resolveGroupId(user);
					users.addAll(userList);
				}else
					users.add(user);
			}

			//check here
			delegate.getProperties().put(NodeProperty.MEMBERS, new XStream().toXML(memberIds));

			servlets.saveItem(delegate);

		} catch (RepositoryException e) {
			throw new InternalErrorException(e);
		} finally {
			servlets.releaseSession();
		}
		return users;
	}


	@Override
	public ItemDelegate save() throws RepositoryException {

		return super.save();

	}


	public ItemDelegate getUserNode(String user) throws RepositoryException,
	InternalErrorException, ItemNotFoundException {

		//		ItemDelegate delegate = JCRRepository.getServlets().getItemById(getId());	
		ItemDelegate userNode = null;
		JCRServlets servlets = null;
		try{
			servlets = new JCRServlets(workspace.getOwner().getPortalLogin());
			@SuppressWarnings("unchecked")
			Map<String, String> usersNode = (Map<String, String>) new XStream().fromXML(delegate.getProperties().get(NodeProperty.USERS));
			//		logger.trace("Looking for user: " + user + " in node: " + usersNode.getPath());
			String value = usersNode.get(user);
			//		logger.info("value "+  value + " for delegate " + delegate.getPath());

			String[] values = value.split(workspace.getPathSeparator());
			if (values.length < 2)
				throw new InternalErrorException("Path node corrupt");

			String parentId = values[0];
			String nodeName = values[1];
			ItemDelegate parentNode = servlets.getItemById(parentId);

			userNode = servlets.getItemByPath(parentNode.getPath() + 
					workspace.getPathSeparator() + Text.escapeIllegalJcrChars((nodeName)));
		} catch (RepositoryException e) {
			throw new InternalErrorException(e);
		} finally {
			servlets.releaseSession();
		}
		return userNode;

	}



	private ItemDelegate getUserNode() throws RepositoryException,
	InternalErrorException, ItemNotFoundException {
		return getUserNode(workspace.getOwner().getPortalLogin());
	}

	private String getNodeName(ItemDelegate node) throws RepositoryException,
	InternalErrorException {

		String[] names = node.getPath().split(
				workspace.getPathSeparator());

		return names[names.length - 1];
	}

	@Override
	public String getName() throws InternalErrorException {

		//		try {
		//			ItemDelegate userNode = getUserNode(delegate);
		//			return getNodeName(userNode);
		//		} catch (RepositoryException | ItemNotFoundException e) {
		//			throw new InternalErrorException(e);
		//		} 		
		return delegate.getTitle();
	}

	@Override
	public void internalRename(JCRServlets servlets, String newName, String remotePath) throws ItemAlreadyExistException, InternalErrorException {

		//		JCRServlets servlets = new JCRServlets(workspace.getOwner().getPortalLogin());
		String nodeNewName = Text.escapeIllegalJcrChars(newName);
		try {

			ItemDelegate userNode = getUserNode();

			if (workspace.exists(nodeNewName, userNode.getParentId())) {
				logger.error("Item with name " + nodeNewName + " exists");
				throw new ItemAlreadyExistException("Item " + nodeNewName + " already exists");
			}
			ItemDelegate parent = servlets.getItemById(userNode.getParentId());
			String newPath = parent.getPath() 
					+ workspace.getPathSeparator() + nodeNewName;

			delegate.setLastModificationTime(Calendar.getInstance());
			delegate.setLastModifiedBy(workspace.getOwner().getPortalLogin());
			delegate.setLastAction(WorkspaceItemAction.RENAMED);
			try{
				delegate.getContent().put(NodeProperty.REMOTE_STORAGE_PATH, remotePath);
			}catch (Exception e) {
				logger.info("RemotePath not in " + delegate.getPath());
			}

			//there was a node.save(); here
			//			ItemDelegate newDelegate = null;
			String path = userNode.getPath();

			delegate = servlets.move(path, newPath);

			delegate.setTitle(newName);
			String value = userNode.getParentId() + workspace.getPathSeparator() + newName;
			
			Map<NodeProperty, String> properties = delegate.getProperties();
			@SuppressWarnings("unchecked")
			Map<String, String> usersMap = (Map<String, String>) new XStream().fromXML(properties.get(NodeProperty.USERS));
			usersMap.put(workspace.getOwner().getPortalLogin(), value);
			properties.put(NodeProperty.USERS, new XStream().toXML(usersMap));

			servlets.saveItem(delegate);

		} catch (RepositoryException e) {
			throw new InternalErrorException(e);
		} catch (ItemNotFoundException e) {
			throw new InternalErrorException(e);
		} catch (WrongItemTypeException e) {
			throw new InternalErrorException(e);
		} catch (HttpException e1) {
			throw new InternalErrorException(e1);
		} catch (IOException e1) {
			throw new InternalErrorException(e1);
		}

	}

	@Override
	public void internalMove(JCRServlets servlets, ItemDelegate destinationFolderNode, String remotePath) throws ItemAlreadyExistException,
	InternalErrorException, RepositoryException {
		//		JCRServlets servlets = null;
		try {

			logger.debug("Start internal move item with id " 
					+ getId() + " to destination item with id " + destinationFolderNode.getId());

			//			servlets = new JCRServlets(workspace.getOwner().getPortalLogin());
			ItemDelegate itemDelegate = servlets.getItemById(getId());

			if (workspace.exists(itemDelegate.getTitle(), destinationFolderNode.getId())) {
				logger.error("Item with name " + getName() + " exists");
				throw new ItemAlreadyExistException("Item " + itemDelegate.getTitle() + " already exists");
			}

			itemDelegate.setLastModificationTime(Calendar.getInstance());
			itemDelegate.setLastModifiedBy(workspace.getOwner().getPortalLogin());
			itemDelegate.setLastAction(WorkspaceItemAction.MOVED);

			ItemDelegate userNode = getUserNode();
			String userNodeName = getNodeName(userNode);

			String newPath = destinationFolderNode.getPath() 
					+ workspace.getPathSeparator() + userNodeName;

			String value = destinationFolderNode.getId() +
					workspace.getPathSeparator() + userNodeName;

			try {
				servlets.clone(itemDelegate.getPath(), newPath, false);
			} catch (HttpException e) {
				throw new InternalErrorException(e);
			} catch (IOException e) {
				throw new InternalErrorException(e);
			}

			Map<NodeProperty, String> properties = itemDelegate.getProperties();

			@SuppressWarnings("unchecked")
			Map<String, String> usersMap = (Map<String, String>) new XStream().fromXML(properties.get(NodeProperty.USERS));	
			usersMap.put(workspace.getOwner().getPortalLogin(), value);
			properties.put(NodeProperty.USERS, new XStream().toXML(usersMap));
			
			servlets.removeItem(userNode.getPath());
			servlets.saveItem(itemDelegate);

		} catch (RepositoryException e) {
			logger.error("Repository exception thrown by move operation",e);
			throw new RepositoryException(e.getMessage());
		} catch (WrongItemTypeException e) {
			logger.error("Unhandled Exception ");
			throw new InternalErrorException(e);
		} catch (ItemNotFoundException e) {
			logger.error("Unhandled Exception ");
			throw new InternalErrorException(e);
		} 
		//		finally{
		//			servlets.releaseSession();
		//		}
	}


	public ItemDelegate createUnsharedCopy(JCRServlets servlets, ItemDelegate sharedNode, String destinationNodeId) throws Exception {
		logger.debug("unShare Node: "+ sharedNode.getPath() + " -  by user: " + workspace.getOwner().getPortalLogin());

		ItemDelegate userNode = getUserNode();

		// shareNode parent it's the same of destinationNode
		if (destinationNodeId.equals(userNode.getParentId())) {
			removeUserSharedFolder(sharedNode);
		}

		WorkspaceFolder unsharedFolder = workspace.createFolder(getNodeName(userNode), getDescription(), destinationNodeId);
		ItemDelegate nodeFolder = servlets.getItemById(unsharedFolder.getId());

		DelegateManager wrap = new DelegateManager(sharedNode, workspace.getOwner().getPortalLogin());
		List<ItemDelegate> children = wrap.getNodes();
		for (ItemDelegate child: children){

			//			if (!child.getName().startsWith(JCRRepository.HL_NAMESPACE) 
			//					&& !child.getName().startsWith(JCRRepository.JCR_NAMESPACE)
			//					&& !child.getName().startsWith(JCRRepository.REP_NAMESPACE)) {			
			try {
				servlets.move(child.getPath(), nodeFolder.getPath() 
						+ workspace.getPathSeparator() + child.getName());
			} catch (HttpException e) {
				throw new InternalErrorException(e);
			} catch (IOException e) {
				throw new InternalErrorException(e);
			}
			//			}
		}


		ItemDelegate destinationNode = servlets.getItemById(destinationNodeId);
		logger.debug("copyremotecontent from "+  nodeFolder.getPath() + " to parent id " + destinationNode.getPath());
		workspace.moveRemoteContent(servlets, unsharedFolder, destinationNode.getPath());

		//		workspace.copyRemoteContent(servlets, nodeFolder, destinationNode);

		JCRWorkspaceItem itemUnshared = (JCRWorkspaceItem) workspace.getItem(unsharedFolder.getId());
		//add UNSHARE operation in History
		itemUnshared.setUnshareHistory(servlets, workspace.getOwner().getPortalLogin());
		//change owner
		itemUnshared.setOwnerToCurrentUser(itemUnshared);

		return nodeFolder;

	}



	@Override
	public ItemDelegate internalCopy(JCRServlets servlets, ItemDelegate delegateFolder, String newName) throws InternalErrorException,
	ItemAlreadyExistException, WrongDestinationException, RepositoryException{

		//		JCRServlets servlets = null;

		try {
			String pathNewNode = delegateFolder.getPath()
					+ workspace.getPathSeparator() + Text.escapeIllegalJcrChars(newName);
			//			servlets = new JCRServlets(workspace.getOwner().getPortalLogin());
			if(servlets.getItemByPath(pathNewNode) != null)
				throw new ItemAlreadyExistException(newName + " already exist");

			String description = getDescription();

			String unSharedFolderId = workspace.createFolder(Text.escapeIllegalJcrChars(newName), description, delegateFolder.getId()).getId();
			ItemDelegate newNodeFolder = servlets.getItemById(unSharedFolderId);
			DelegateManager wrap = new DelegateManager(newNodeFolder, workspace.getOwner().getPortalLogin());
			List<ItemDelegate> children = wrap.getNodes();

			for (ItemDelegate child: children){
				//				if (!child.getName().startsWith(JCRRepository.HL_NAMESPACE) 
				//						&& !child.getName().startsWith(JCRRepository.JCR_NAMESPACE)
				//						&& !child.getName().startsWith(JCRRepository.REP_NAMESPACE)) {				

				servlets.copy(child.getPath(), newNodeFolder.getPath() 
						+ workspace.getPathSeparator() + child.getName());
				//				}
			}

			return newNodeFolder;
		} catch (Exception e) {
			throw new InternalErrorException(e);
		} 

	}

	@Override
	public JCRAbstractWorkspaceFolder getParent() throws InternalErrorException {

		try {
			return workspace.getParent(getUserNode());
		} catch (RepositoryException | ItemNotFoundException e) {
			throw new InternalErrorException(e);
		} 
	}

	//	@Override
	//	public String getPath() throws InternalErrorException {
	////		String path = null;
	////		ItemDelegate userNode = null;
	////		try {
	////			userNode = getUserNode(delegate);
	//////			path = ((JCRWorkspaceFolder)getParent(userNode)).getPath()
	//////					+ workspace.getPathSeparator() + getNodeName(userNode);
	////		} catch (ItemNotFoundException | RepositoryException e) {
	////			throw new InternalErrorException(e);
	////		}
	////				return ((JCRWorkspaceFolder)getParent(userNode)).getPath(JCRRepository.getServlets().getItemById(userNode.getParentId()))
	////						+ workspace.getPathSeparator() + getNodeName(userNode);		
	////		System.out.println("SHARED NODE path: " + path); 
	////	return path;	
	//		
	//		String delPath = null;
	//		if (delegate.getParentPath().startsWith("/Home/" + workspace.getOwner().getPortalLogin())){
	//			delPath = delegate.getParentPath().replace("/Home/" + workspace.getOwner().getPortalLogin(), "");
	//		}
	//		return delPath;
	//		System.out.println("SHARED NODE: PARENTPATH " + delegate.getParentPath());
	//		return delegate.getParentPath();
	//	}


	@Override
	public void remove() throws InternalErrorException, InsufficientPrivilegesException {

		logger.debug("Remove shared item " + getPath());
		JCRServlets servlets = null;
		try {
			servlets = new JCRServlets(workspace.getOwner().getPortalLogin());

			if (!JCRPrivilegesInfo.canDelete(getOwner().getPortalLogin(), workspace.getOwner().getPortalLogin(), getAbsolutePath(), true)) 
				throw new InsufficientPrivilegesException("Insufficient Privileges to remove the node");
			if (isVreFolder())
				throw new InternalErrorException("A VRE folder cannot be removed");
			if (delegate.getPath().equals(workspace.mySpecialFoldersPath))
				throw new InternalErrorException("This folder cannot be removed");

			try {

				JCRWorkspaceItem unsharedFolder = (JCRWorkspaceItem) workspace.unshare(getId());

				logger.trace("unsharedFolder: " + unsharedFolder.getPath());
				//				ItemDelegate usharedNode = servlets.getItemById(unsharedFolder.getId());

				workspace.moveToTrash(servlets, unsharedFolder);

				//Add removal accounting entry to folder item parent
				try{
					JCRAccountingFolderEntryRemoval entry = new JCRAccountingFolderEntryRemoval(unsharedFolder.getDelegate().getParentId(), getOwner().getPortalLogin(),
							Calendar.getInstance(),
							getType(),
							(getType() == WorkspaceItemType.FOLDER_ITEM)?((FolderItem)this).getFolderItemType():null,
									getName(),
									(getType() == WorkspaceItemType.FOLDER_ITEM)?((FolderItem)this).getMimeType():null);
					entry.save(servlets);
				}catch (Exception e) {
					logger.error("Impossible to set Removal Accounting Entry to parent of " + unsharedFolder.getDelegate().getPath());
				}

			} catch (WrongDestinationException | ItemAlreadyExistException
					| WorkspaceFolderNotFoundException | ItemNotFoundException e) {
				throw new InternalErrorException(e);
			}

		} catch (RepositoryException e) {
			throw new InternalErrorException(e);
		} catch (RemoteBackendException e) {
			throw new InternalErrorException(e);
		} finally{
			servlets.releaseSession();
		}
	}

	@Override
	public String getPath(ItemDelegate delegate) throws InternalErrorException, RepositoryException {
		ItemDelegate userNode = null;
		String path;
		JCRServlets servlets = null;
		try {
			servlets = new JCRServlets(workspace.getOwner().getPortalLogin());
			userNode = getUserNode();
			path = ((JCRWorkspaceFolder)getParent(userNode)).getPath(servlets.getItemById(userNode.getParentId()))
					+ workspace.getPathSeparator() + getNodeName(userNode);	
		} catch (ItemNotFoundException e) {
			throw new InternalErrorException(e);
		} catch (RepositoryException e) {
			throw new RepositoryException(e.getMessage());
		} finally{
			servlets.releaseSession();
		}
		return path;
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<String> getMembers() throws InternalErrorException {

		List<String> list = null;
		try {
			list = (ArrayList<String>) new XStream().fromXML(delegate.getProperties().get(NodeProperty.MEMBERS));
		} catch (Exception e) {
			throw new InternalErrorException(e);
		} 
		return list;
	}
	
	/**
	 * Set members
	 * @return
	 * @throws InternalErrorException
	 */
	public void setMembers(List<String> members) throws InternalErrorException {

		JCRServlets servlets = null;
		try {
			servlets = new JCRServlets(workspace.getOwner().getPortalLogin());
			delegate.getProperties().put(NodeProperty.MEMBERS, new XStream().toXML(members));
			servlets.saveItem(delegate);
		} catch (RepositoryException e) {
			throw new InternalErrorException(e);
		} catch (Exception e) {
			throw new InternalErrorException(e);
		} finally {
			servlets.releaseSession();
		}
		
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<String> getUsers() throws InternalErrorException {

		List<String> list = new ArrayList<String>();
		try {
			Map<String, String> users = (Map<String, String>) new XStream().fromXML(delegate.getProperties().get(NodeProperty.USERS));
			Set<String> set = users.keySet();
			for (String key:set)
				list.add(key);
		} catch (Exception e) {
			throw new InternalErrorException(e);
		} 
		return list;
	}


	@SuppressWarnings("unchecked")
	private void addUser(String user, String destinationFolderId) throws InternalErrorException, RepositoryException {

		logger.trace("addUser("+delegate.getPath()+", " + user +", " + destinationFolderId +");");
		//	System.out.println("addUser("+delegate.getPath()+", " + user +", " + destinationFolderId +");");

		JCRServlets servlets = null;
		try {
			servlets = new JCRServlets(workspace.getOwner().getPortalLogin());

			HomeManager homeManager = workspace.getHome().getHomeManager();
			WorkspaceFolder userRoot = (WorkspaceFolder)homeManager.getHome(user).getWorkspace().getItem(destinationFolderId);

			ItemDelegate rootNode = servlets.getItemById(userRoot.getId());
			String sharedFolderName = userRoot.getUniqueName(delegate.getTitle(), false);

			Map<NodeProperty, String> properties = delegate.getProperties();
			Map<String, String> usersNode = null;

			try{
				usersNode = (Map<String, String>) new XStream().fromXML(properties.get(NodeProperty.USERS));
			}catch (Exception e) {
				logger.info("USERS not set on " + delegate.getPath());
				usersNode = new HashMap<String, String>();
			}

			String pathUser = null;
			if (applicationName != null){

				pathUser = rootNode.getPath() + workspace.getPathSeparator() + sharedFolderName;
				logger.trace("clone from " + delegate.getPath()+ workspace.getPathSeparator() + itemName + " to "+ pathUser);
				ItemDelegate cloned = servlets.clone(delegate.getPath()+ workspace.getPathSeparator() + itemName, pathUser, false);
				logger.trace("CLONE " + cloned.getPath());

			}else {		
				pathUser = rootNode.getPath() + workspace.getPathSeparator() + sharedFolderName;
				try {
					if (usersNode.get(user) != null){
						return;
					}
				} catch (Exception e) {
					logger.debug("User is not present");
				}

				servlets.clone(delegate.getPath(), pathUser, false);
				logger.trace("Clone from " + delegate.getPath() + " to "+ pathUser);
			}

			String value = userRoot.getId() + workspace.getPathSeparator() 
					+ sharedFolderName;

			logger.trace("usersNode: " + delegate.getPath() + " - set value " + value + " to: " + user);

			usersNode.put(user, value);

			properties.put(NodeProperty.USERS, new XStream().toXML(usersNode));

			servlets.saveItem(delegate);

		} catch (RepositoryException e) {
			throw new RepositoryException(e.getMessage());
		} catch (ItemNotFoundException e) {
			throw new InternalErrorException(e);
		} catch (WorkspaceFolderNotFoundException e) {
			throw new InternalErrorException(e);
		} catch (HomeNotFoundException e) {
			throw new InternalErrorException(e);
		} catch (UserNotFoundException e) {
			throw new InternalErrorException(e);
		} catch (Exception e) {
			throw new InternalErrorException(e);
		} finally{
			servlets.releaseSession();
		}

	}


	@Override
	public void addUser(String user) throws InsufficientPrivilegesException,
	InternalErrorException {

		JCRServlets servlets = null;
		try {	 
			servlets = new JCRServlets(workspace.getOwner().getPortalLogin());
			ItemDelegate sharedNode = servlets.getItemById(getId());

			@SuppressWarnings("unchecked")
			Map<String, String> usersMap = (Map<String, String>) new XStream().fromXML(sharedNode.getProperties().get(NodeProperty.USERS));

			try {
				if (usersMap.get(user) != null){
					logger.trace(user + " is already in share");
					return;
				}
			} catch (Exception e) {
				logger.debug("User "+ user + " is not present");
			}

			HomeManager homeManager = workspace.getHome().getHomeManager();
			Home home = homeManager.getHome(user);

			if (isVreFolder())
				addUser(user, home.getWorkspace().getMySpecialFolders().getId());
			else
				addUser(user, home.getWorkspace().getRoot().getId());

		} catch (RepositoryException e) {
			throw new InternalErrorException(e);
		} catch (HomeNotFoundException e) {
			throw new InternalErrorException(e);
		} catch (UserNotFoundException e) {
			throw new InternalErrorException(e);
		} catch (ItemNotFoundException e) {
			throw new InternalErrorException(e);
		} catch (WorkspaceFolderNotFoundException e) {
			throw new InternalErrorException(e);
		} finally{
			servlets.releaseSession();
		}
	}

	@Override
	public WorkspaceItemType getType() {
		return WorkspaceItemType.SHARED_FOLDER;
	}

	@Override
	public WorkspaceFolder unShare() throws InternalErrorException {
		return unShare(workspace.getOwner().getPortalLogin());
	}




	@Override
	public WorkspaceFolder unShare(String user) throws InternalErrorException {
		logger.info("Unsharing folder " + getName() + " by user " + user);
		JCRServlets servlets = null;
		JCRWorkspaceFolder folder = null;
		try {
			servlets = new JCRServlets(workspace.getOwner().getPortalLogin());
			ItemDelegate sharedFolder = servlets.getItemById(getId());	
			ItemDelegate userNode = getUserNode();	

			if (getOwner().getPortalLogin().equals(user) || (getACLByUser(user).equals(ACLType.ADMINISTRATOR))){
				ItemDelegate unsharedNode = createUnsharedCopy(servlets, sharedFolder, userNode.getParentId());
				folder = new JCRWorkspaceFolder(workspace, unsharedNode);

				//				logger.trace("Remove clones");
				removeClones(servlets, sharedFolder);

				logger.trace("Remove sharedNode: " + sharedFolder.getPath());	
				servlets.removeItem(sharedFolder.getPath());

			}else{
				//remove clone
				ItemDelegate cloneNode = getUserNode(user);
				logger.trace("Remove clone " + cloneNode.getPath());
				servlets.removeItem(cloneNode.getPath());

				//remove user in userNode
				@SuppressWarnings("unchecked")
				Map<String, String> usersMap = (Map<String, String>) new XStream().fromXML(delegate.getProperties().get(NodeProperty.USERS));
				usersMap.put(user, (String)null);
				delegate.getProperties().put(NodeProperty.USERS, new XStream().toXML(usersMap));
				servlets.saveItem(delegate);

				logger.trace(user + " has been deleted from share " + delegate.getPath());

				//remove user from ACL
				JCRAccessManager accessManager = new JCRAccessManager();
				List<String> userToRemove = new ArrayList<String>();
				userToRemove.add(user);
				boolean flag = accessManager.deleteAces(getAbsolutePath(), userToRemove);	
				if (!flag)
					throw new InternalErrorException("Error deleting aces in " + getAbsolutePath() + " for users "+ userToRemove.toString() );										

				//set history			
				setUnshareHistory(servlets, user);	
			}
			return folder;
		} catch (Exception e) {
			throw new InternalErrorException(e);
		}finally{
			servlets.releaseSession();
		}

	}


	@Override
	public WorkspaceSharedFolder share(List<String> usersList) throws InsufficientPrivilegesException,
	WrongDestinationException, InternalErrorException {

		List<String> userIds = listUsers(usersList);

		for (String user : userIds) {
			addUser(user);
		}

		return (WorkspaceSharedFolder) this;
	}



	@Override
	public String getName(String user) throws InternalErrorException {

		try {
			ItemDelegate userNode = getUserNode(user);
			return getNodeName(userNode);
		} catch (RepositoryException | ItemNotFoundException e) {
			throw new InternalErrorException(e);
		} 
	}




	@Override
	public ACLType getPrivilege() throws InternalErrorException {

		String absPath = null;
		JCRAccessManager accessManager = null;
		Map<String, List<String>> aclMap = null;		

		try{
			accessManager = new JCRAccessManager();
			absPath = getAbsolutePath();
			aclMap  = accessManager.getEACL(absPath);

			Set<String> keys = aclMap.keySet();

			for (final String user : keys){

				JCRUserManager um = new JCRUserManager();
				GCubeGroup group = null;
				try{

					//if the user is a group and this is empty, skip
					group = um.getGroup(user);
					if (group!=null){
						if (group.getMembers().isEmpty()){
							continue;	
						}
					}

					ACLType aclType = WorkspaceUtil.getACLTypeByKey(aclMap.get(user));

					if (!aclType.equals(ACLType.ADMINISTRATOR))
						return aclType;

				}catch (Exception e) {
					logger.error(e.getMessage());
				}
			} 

		}catch (Exception e) {
			logger.error("an error occurred setting ACL on: " + absPath);
		}
		return ACLType.WRITE_OWNER;

	}








	public void removeClones(JCRServlets servlets, ItemDelegate sharedNode) throws InternalErrorException, RepositoryException {

		try {
			Map<NodeProperty, String> properties = sharedNode.getProperties();
			@SuppressWarnings("unchecked")
			Map<String, String> userNode = (Map<String, String>) new XStream().fromXML(properties.get(NodeProperty.USERS));
			Set<String> usersList = userNode.keySet();
			for (String user: usersList){

				if (user.startsWith("jcr:"))
					continue;
				//				System.out.println("remove clone for user " + user);
				logger.trace("user " + user);
				logger.trace("workspace.getOwner().getPortalLogin() " + workspace.getOwner().getPortalLogin());

				//				if (!user.startsWith(JCRRepository.JCR_NAMESPACE) &&
				//						!user.startsWith(JCRRepository.HL_NAMESPACE))	{	
				//remove clones
				try{
					ItemDelegate cloneNode = getUserNode(user);

					if (cloneNode.getPrimaryType().equals(PrimaryNodeType.NT_WORKSPACE_SHARED_FOLDER)){
						servlets.removeItem(cloneNode.getPath());
						logger.trace("Removed Clone " + cloneNode.getPath() + " - "+ cloneNode.getPrimaryType());
					}
				}catch (Exception e) {
					logger.error("Error removing clone " + e);
				}
				//				}
			}

		} catch (Exception e) {
			throw new InternalErrorException(e);
		}


	}


	public void removeUserSharedFolder(ItemDelegate sharedNode) throws InternalErrorException, RepositoryException {
		JCRServlets servlets = null;
		try {
			servlets = new JCRServlets(workspace.getOwner().getPortalLogin());

			ItemDelegate userNode = getUserNode();

			// Remove sharedNode from user workspace
			servlets.removeItem(userNode.getPath());

			// Remove user in sharingSet
			try{
				@SuppressWarnings("unchecked")
				Map<String,String> usersNode = (Map<String, String>) new XStream().fromXML(sharedNode.getProperties().get(NodeProperty.USERS));
				usersNode.put(workspace.getOwner().getPortalLogin(), new XStream().toXML((String)null));
				sharedNode.getProperties().put(NodeProperty.USERS, new XStream().toXML(usersNode));
			}catch (Exception e) {
				logger.error("Error removing user from users node");
			}

			// Remove user in member node
			try{
				@SuppressWarnings("unchecked")
				Map<String,String> memberNode = (Map<String, String>) new XStream().fromXML(sharedNode.getProperties().get(NodeProperty.MEMBERS));
				memberNode.remove(workspace.getOwner().getPortalLogin());
			}catch (Exception e) {
				logger.error("Error removing user from members node");
			}

			servlets.saveItem(sharedNode);

		}catch (Exception e) {
			throw new InternalErrorException(e);
		} finally {
			servlets.releaseSession();
		}
	}

	@Override
	public boolean isVreFolder() {
		Boolean flag = false;
		try{
			flag = (Boolean) new XStream().fromXML(delegate.getProperties().get(NodeProperty.IS_VRE_FOLDER));
		}catch (Exception e) {}

		return flag;
	}

	@Override
	public String getDisplayName() {
		String display = "";
		try{
			display = delegate.getProperties().get(NodeProperty.DISPLAY_NAME);
		}catch (Exception e) {
		}
		return display;
	}


	@Override
	public boolean addAdmin(final String username) throws InsufficientPrivilegesException,
	InternalErrorException {
		if (isAdmin(workspace.getOwner().getPortalLogin())){
			//		if (!getUsers().contains(username))
			try {
				share(new ArrayList<String>(){/**
				 * 
				 */
					private static final long serialVersionUID = 1L;

					{add(username);}});
			} catch (WrongDestinationException e) {
				throw new InternalErrorException(e);
			}

			try {
				List<String> administator = new ArrayList<String>();
				administator.add(username);
				this.setACL(administator, ACLType.ADMINISTRATOR);

			}catch (Exception e) {
				logger.error(e.getMessage());
				return false;
			}
			return true;
		}
		throw new InsufficientPrivilegesException("Insufficient Privileges to set administrators");

	}

	@Override
	public List<String> getAdministrators() throws InternalErrorException {
		List<String> list = null;
		try{
			list = getACLOwner().get(ACLType.ADMINISTRATOR);
		}catch (Exception e) {
			logger.error("no administrators");
		}
		return list;
	}


	public boolean isAdmin(String username) throws InternalErrorException {
		if (getACLUser().equals(ACLType.ADMINISTRATOR))
			return true;
		return false;
	}



	@Override
	public boolean setAdmins(List<String> logins)
			throws InsufficientPrivilegesException, InternalErrorException {
		logger.trace("setAdmins: " + logins.toString() + " on shared folder: " + getAbsolutePath() );
		if (isAdmin(workspace.getOwner().getPortalLogin())){
			try{
				List<String> notAdmins = getAdministrators();

				try{
					notAdmins.removeAll(logins);
				}catch (Exception e) {
					logger.trace("Admins not alredy set on " + getAbsolutePath());
				}
				try{
					notAdmins.remove(getOwner().getPortalLogin());
				}catch (Exception e) {
					logger.trace("Admins not alredy set on " + getAbsolutePath());
				}

				ACLType privilege = getPrivilege();

				logger.trace("Set " + privilege + " on users " +  notAdmins);
				JCRAccessManager accessManager = new JCRAccessManager();

				//set default ACL for users not more included in share
				logger.trace("Setting " + privilege + " on users " +  notAdmins);
				try{
					accessManager.deleteAces(getAbsolutePath(), notAdmins);
				}catch (Exception e) {
					logger.error("Error deleting aces on " + getAbsolutePath());
				}
				try{
					this.setACL(notAdmins, privilege);
				}catch (Exception e) {
					logger.error("Error setting aces on " + getAbsolutePath());
				}

				try{
					//set admin ACL for new admins
					logins.removeAll(notAdmins);
				}catch (Exception e) {
					logger.error("error removing all");
				}
				logger.trace("Setting Admin on users " +  logins);

				for (String user: logins)
					addAdmin(user);

				return true;
			}catch (Exception e) {
				logger.error("Error setting admins on node " +getAbsolutePath(), e);
				return false;
			}
		}
		throw new InsufficientPrivilegesException("Insufficient Privileges to edit administrators");


	}


	@Override
	public void setACL(List<String> users, ACLType privilege) throws InternalErrorException {

		List<String> notAdmins = new ArrayList<String>(users);
		List<String> admins = getAdministrators();
		if (admins!=null){
			notAdmins.removeAll(admins);
			logger.info("notAdmin users " + notAdmins.toString());
		}else
			logger.info("No Administrators on " + getAbsolutePath());

		//		String absPath = null;


		boolean flag = false;
		JCRAccessManager accessManager = new JCRAccessManager();

		int i = 0;
		while ((flag==false) && (i<3)){
			i++;
			try{
				//				absPath = getAbsolutePath();

				if (getAbsolutePath() == null)
					throw new InternalErrorException("Absolute path cannot be null setting ACL");

				switch(privilege){

				case READ_ONLY:
					if (notAdmins.size()>0)
						flag = accessManager.setReadOnlyACL(notAdmins, getAbsolutePath());		
					break;
				case WRITE_OWNER:	
					if (notAdmins.size()>0)
						flag = accessManager.setWriteOwnerACL(notAdmins, getAbsolutePath());		
					break;
				case WRITE_ALL:
					if (notAdmins.size()>0)
						flag = accessManager.setWriteAllACL(notAdmins, getAbsolutePath());	
					break;
				case ADMINISTRATOR:
					flag = accessManager.setAdminACL(users, getAbsolutePath());	
					break;
				default:
					break;
				}

				if (flag==false)
					Thread.sleep(1000);

			}catch (Exception e) {
				logger.error("an error occurred setting ACL on: " + getAbsolutePath());
			}
		}


		logger.info("Has ACL been modified correctly for users " + users.toString() + "in path " + getAbsolutePath() + "? " + flag);
		//set administators

		setAdministrators(accessManager, admins);

	}

	private void setAdministrators(JCRAccessManager accessManager, List<String> admins) throws InternalErrorException {
		if (!isVreFolder()){
			if (admins==null){	
				boolean isSet = false;

				int j = 0;
				while ((isSet==false) && (j<3)){
					j++;
					try{
						String owner = workspace.getOwner().getPortalLogin();
						List<String> adminList =new ArrayList<String>();
						adminList.add(owner);
						logger.info("Set " + owner + " ad administrator");
						isSet = accessManager.setAdminACL(adminList, getAbsolutePath());
						logger.info("Has ACL been modified correctly for users " + adminList.toString() + "in path " + getAbsolutePath() + "? " + isSet);
						if (isSet==false)
							Thread.sleep(1000);
					}catch (Exception e) {
						logger.error("Error setting administators on " + getAbsolutePath());
					}
				}
			}
		}

	}

	@Override
	public List<String> getGroups() throws InternalErrorException {
		List<String> groups = new ArrayList<String>();

		UserManager gm = HomeLibrary
				.getHomeManagerFactory().getUserManager();

		List<String> members = getMembers();
		for (String member: members){
			if (gm.isGroup(member))
				groups.add(member);					
		}
		return groups;	
	}


	/**
	 * Share folder
	 * @throws RepositoryException
	 * @throws InternalErrorException 
	 */
	public void share() throws RepositoryException, InternalErrorException {
		//resolve groupIds
		List<String> usersList = listUsers(users);
		shareWithUses(usersList);

	}


}
