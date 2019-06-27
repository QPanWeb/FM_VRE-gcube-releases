package org.gcube.data.access.storagehub.services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.JackrabbitAccessControlList;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.Query;
import org.apache.jackrabbit.api.security.user.QueryBuilder;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.commons.jackrabbit.authorization.AccessControlUtils;
import org.gcube.common.authorization.control.annotations.AuthorizationControl;
import org.gcube.common.authorization.library.provider.AuthorizationProvider;
import org.gcube.common.gxrest.response.outbound.GXOutboundErrorResponse;
import org.gcube.common.storagehub.model.acls.AccessType;
import org.gcube.common.storagehub.model.exceptions.BackendGenericError;
import org.gcube.common.storagehub.model.types.NodeProperty;
import org.gcube.common.storagehub.model.types.PrimaryNodeType;
import org.gcube.data.access.storagehub.Constants;
import org.gcube.data.access.storagehub.Utils;
import org.gcube.data.access.storagehub.exception.MyAuthException;
import org.gcube.data.access.storagehub.handlers.CredentialHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("groups")
public class GroupManager {

	@Context ServletContext context;

	private static final Logger log = LoggerFactory.getLogger(GroupManager.class);

	@Inject 
	RepositoryInitializer repository;

	@GET
	@Path("")
	@Produces(MediaType.APPLICATION_JSON)
	@AuthorizationControl(allowed={"lucio.lelii"}, exception=MyAuthException.class)
	public List<String> getGroups(){

		JackrabbitSession session = null;
		List<String> groups= new ArrayList<>();
		try {
			session = (JackrabbitSession) repository.getRepository().login(CredentialHandler.getAdminCredentials(context));

			Iterator<Authorizable> result = session.getUserManager().findAuthorizables(new Query() {

				@Override
				public <T> void build(QueryBuilder<T> builder) {
					builder.setSelector(Group.class);
				}
			});

			while (result.hasNext()) {
				Authorizable group = result.next();
				log.info("group {} found",group.getPrincipal().getName());
				groups.add(group.getPrincipal().getName());
			}
		}catch(Exception e) {
			log.error("jcr error getting groups", e);
			GXOutboundErrorResponse.throwException(new BackendGenericError(e));
		} finally {
			if (session!=null)
				session.logout();
		}
		return groups;
	}

	@POST
	@Path("")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@AuthorizationControl(allowed={"lucio.lelii"}, exception=MyAuthException.class)
	public String createGroup(@FormParam("group") String group, @FormParam("accessType") AccessType accessType){

		JackrabbitSession session = null;
		String groupId = null;
		try {
			session = (JackrabbitSession) repository.getRepository().login(CredentialHandler.getAdminCredentials(context));

			org.apache.jackrabbit.api.security.user.UserManager usrManager = session.getUserManager();

			Group createdGroup = usrManager.createGroup(group);
			groupId = createdGroup.getID();

			createVreFolder(groupId, session, accessType!=null?accessType:AccessType.WRITE_OWNER);

			session.save();
		}catch(Exception e) {
			log.error("jcr error creating group {}", group, e);
			GXOutboundErrorResponse.throwException(new BackendGenericError(e));
		} finally {
			if (session!=null)
				session.logout();
		}

		return groupId;
	}

	@DELETE
	@Path("{group}")
	@AuthorizationControl(allowed={"lucio.lelii"}, exception=MyAuthException.class)
	public String deleteGroup(@PathParam("group") String group){

		JackrabbitSession session = null;
		try {
			session = (JackrabbitSession) repository.getRepository().login(CredentialHandler.getAdminCredentials(context));

			org.apache.jackrabbit.api.security.user.UserManager usrManager = session.getUserManager();

			try {
				Node sharedRootNode = session.getNode(Constants.SHARED_FOLDER_PATH);
				sharedRootNode.getNode(group).removeSharedSet();
			}catch (Exception e) {
				log.warn("vreFolder {} not found, removing only the group", group);
			}
			Authorizable authorizable = usrManager.getAuthorizable(group);
			if (authorizable.isGroup())
				authorizable.remove();
			session.save();
		}catch(Exception e) {
			log.error("jcr error getting users", e);
			GXOutboundErrorResponse.throwException(new BackendGenericError(e));
		} finally {
			if (session!=null)
				session.logout();
		}

		return group;
	}

	@PUT
	@Path("{id}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@AuthorizationControl(allowed={"lucio.lelii"}, exception=MyAuthException.class)
	public boolean addUserToGroup(@PathParam("id") String groupId, @FormParam("userId") String userId){

		JackrabbitSession session = null;
		boolean success = false;
		try {
			session = (JackrabbitSession) repository.getRepository().login(CredentialHandler.getAdminCredentials(context));

			org.apache.jackrabbit.api.security.user.UserManager usrManager = session.getUserManager();

			Group group = (Group)usrManager.getAuthorizable(groupId);
			User user = (User)usrManager.getAuthorizable(userId);

			success = group.addMember(user);

			String folderName =  group.getPrincipal().getName();
			Node sharedRootNode = session.getNode(Constants.SHARED_FOLDER_PATH);
			Node folder = sharedRootNode.getNode(folderName);

			String userPath = String.format("%s%s/%s",Utils.getWorkspacePath(user.getPrincipal().getName()).toPath(),Constants.VRE_FOLDER_PARENT_NAME, folderName);
			log.debug("creating folder in user path {}", userPath );
			session.getWorkspace().clone(session.getWorkspace().getName(), folder.getPath(),userPath , false);

			session.save();
		}catch(Exception e) {
			log.error("jcr error adding user {} to group {}", userId, groupId, e);
			GXOutboundErrorResponse.throwException(new BackendGenericError(e));
		} finally {
			if (session!=null)
				session.logout();
		}

		return success;
	}

	@DELETE
	@Path("{groupId}/users/{userId}")
	@AuthorizationControl(allowed={"lucio.lelii"}, exception=MyAuthException.class)
	public boolean removeUserFromGroup(@PathParam("groupId") String groupId, @PathParam("userId") String userId){

		JackrabbitSession session = null;
		boolean success = false;
		try {
			session = (JackrabbitSession) repository.getRepository().login(CredentialHandler.getAdminCredentials(context));

			org.apache.jackrabbit.api.security.user.UserManager usrManager = session.getUserManager();

			Group group = (Group)usrManager.getAuthorizable(groupId);
			User user = (User)usrManager.getAuthorizable(userId);

			//delete folder on user
			String folderName =  group.getPrincipal().getName();
			Node sharedRootNode = session.getNode(Constants.SHARED_FOLDER_PATH);
			Node folder = sharedRootNode.getNode(folderName);

			NodeIterator ni = folder.getSharedSet();
			while (ni.hasNext()) {
				Node node = ni.nextNode();
				if (node.getPath().startsWith(Utils.getWorkspacePath(user.getPrincipal().getName()).toPath())) {
					node.removeShare();
					break;
				}
			}

			success = group.removeMember(user);

			session.save();
		}catch(Exception e) {
			log.error("jcr error adding user {} to group {}", userId, groupId, e);
			GXOutboundErrorResponse.throwException(new BackendGenericError(e));
		} finally {
			if (session!=null)
				session.logout();
		}

		return success;
	}

	@GET
	@Path("{groupId}/users")
	@Produces(MediaType.APPLICATION_JSON)
	@AuthorizationControl(allowed={"lucio.lelii"}, exception=MyAuthException.class)
	public List<String> getUsersOfGroup(@PathParam("groupId") String groupId){

		JackrabbitSession session = null;
		List<String> users = new ArrayList<>();
		try {
			session = (JackrabbitSession) repository.getRepository().login(CredentialHandler.getAdminCredentials(context));

			org.apache.jackrabbit.api.security.user.UserManager usrManager = session.getUserManager();

			Group group = (Group)usrManager.getAuthorizable(groupId);

			Iterator<Authorizable> it = group.getMembers();

			while (it.hasNext()) {
				Authorizable user = it.next();
				users.add(user.getPrincipal().getName());
			}


		}catch(Exception e) {
			log.error("jcr error getting users of group {}", groupId, e);
			GXOutboundErrorResponse.throwException(new BackendGenericError(e));
		} finally {
			if (session!=null)
				session.logout();
		}

		return users;
	}

	private void createVreFolder(String groupId, JackrabbitSession session, AccessType defaultAccessType) throws Exception{

		Node sharedRootNode = session.getNode(Constants.SHARED_FOLDER_PATH);

		String name = groupId;

		String title = groupId.substring(groupId.lastIndexOf("-")+1);

		Node folder= Utils.createFolderInternally(session, sharedRootNode, name, "VREFolder for "+groupId, false, AuthorizationProvider.instance.get().getClient().getId(), null);
		folder.setPrimaryType(PrimaryNodeType.NT_WORKSPACE_SHARED_FOLDER);
		folder.setProperty(NodeProperty.IS_VRE_FOLDER.toString(), true);
		folder.setProperty(NodeProperty.TITLE.toString(), name);
		folder.setProperty(NodeProperty.DISPLAY_NAME.toString(), title);
		session.save();

		AccessControlManager acm = session.getAccessControlManager();
		JackrabbitAccessControlList acls = AccessControlUtils.getAccessControlList(acm, folder.getPath());
		Privilege[] adminPrivileges = new Privilege[] { acm.privilegeFromName(AccessType.ADMINISTRATOR.getValue()) };
		acls.addAccessControlEntry(AccessControlUtils.getPrincipal(session, AuthorizationProvider.instance.get().getClient().getId()), adminPrivileges );

		Privilege[] usersPrivileges = new Privilege[] { acm.privilegeFromName(defaultAccessType.getValue()) };
		acls.addAccessControlEntry(AccessControlUtils.getPrincipal(session,groupId), usersPrivileges );
		acm.setPolicy(folder.getPath(), acls);

	}

}
