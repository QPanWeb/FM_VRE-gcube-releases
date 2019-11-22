package org.gcube.portlets.widgets.ckandatapublisherwidget.server.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gcube.datacatalogue.ckanutillibrary.server.DataCatalogue;
import org.gcube.datacatalogue.ckanutillibrary.shared.RolesCkanGroupOrOrg;
import org.gcube.portlets.widgets.ckandatapublisherwidget.server.CKANPublisherServicesImpl;
import org.gcube.portlets.widgets.ckandatapublisherwidget.shared.OrganizationBean;
import org.gcube.vomanagement.usermanagement.GroupManager;
import org.gcube.vomanagement.usermanagement.RoleManager;
import org.gcube.vomanagement.usermanagement.UserManager;
import org.gcube.vomanagement.usermanagement.exception.GroupRetrievalFault;
import org.gcube.vomanagement.usermanagement.exception.UserManagementSystemException;
import org.gcube.vomanagement.usermanagement.impl.LiferayGroupManager;
import org.gcube.vomanagement.usermanagement.impl.LiferayRoleManager;
import org.gcube.vomanagement.usermanagement.impl.LiferayUserManager;
import org.gcube.vomanagement.usermanagement.model.GCubeGroup;
import org.gcube.vomanagement.usermanagement.model.GCubeRole;
import org.gcube.vomanagement.usermanagement.model.GatewayRolesNames;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import eu.trentorise.opendata.jackan.model.CkanOrganization;

/**
 * Facilities to check roles into the catalogue.
 */
public class CatalogueRoleManager {
	
	private static final Log logger = LogFactoryUtil.getLog(CatalogueRoleManager.class);

	/**
	 * Retrieve the highest ckan role the user has and also retrieve the list of organizations (scopes) in which the user has the ckan-admin or ckan-editor role
	 * @param currentScope
	 * @param username
	 * @param groupName
	 * @param gcubeCkanDataCatalogServiceImpl
	 * @param orgsInWhichAtLeastEditorRole
	 * @return the highest among the roles
	 */
	public static RolesCkanGroupOrOrg getHighestRole(String currentScope, String username, String groupName, CKANPublisherServicesImpl gcubeCkanDataCatalogServiceImpl, List<OrganizationBean> orgsInWhichAtLeastEditorRole, String gatewayHostname){

		// base role as default value
		RolesCkanGroupOrOrg toReturn = RolesCkanGroupOrOrg.MEMBER;

		try{

			UserManager userManager = new LiferayUserManager();
			RoleManager roleManager = new LiferayRoleManager();
			GroupManager groupManager = new LiferayGroupManager();

			// user id
			long userid = userManager.getUserId(username);

			// retrieve current group id
			long currentGroupId = groupManager.getGroupIdFromInfrastructureScope(currentScope);

			logger.debug("Group id is " + currentGroupId + " and scope is " + currentScope + "and gateway is "+gatewayHostname);
			
			Set<GCubeGroup> groups;
			//Updated by Francesco see Task #12480
			if(gatewayHostname!=null && !gatewayHostname.isEmpty()) {
				// retrieve the list of organizations for the current user filtered for gateway
				groups = groupManager.listGroupsByUserAndSite(userid, gatewayHostname);
				for (GCubeGroup gCubeGroup : groups) {
					logger.info("Found group (alias VRE) belonging to "+gatewayHostname+": "+gCubeGroup.getGroupName());
				}
			}else {
				// retrieve the flat list of organizations for the current user
				List<GCubeGroup> listGroups = groupManager.listGroupsByUser(userid);
				groups = new HashSet<GCubeGroup>(listGroups);
			}

			// root (so check into the root, the VOs and the VRES)
			if(groupManager.isRootVO(currentGroupId)){

				logger.info("The current scope is the Root Vo, so the list of organizations of the user " + username + " has " + groups.size() + " group/s");

				for (GCubeGroup gCubeGroup : groups) {
					
					if(!groupManager.isVRE(gCubeGroup.getGroupId()))
						continue;

					// get the name of this group
					String gCubeGroupName = gCubeGroup.getGroupName();
					
					logger.info("Cheking role of the user " + username + " in the VRE " + gCubeGroupName);
					
					// get the role of the users in this group
					List<GCubeRole> roles = roleManager.listRolesByUserAndGroup(userid, groupManager.getGroupId(gCubeGroupName));

					// get highest role
					RolesCkanGroupOrOrg correspondentRoleToCheck = getLiferayHighestRoleInOrg(roles);

					// be sure it is so
					checkIfRoleIsSetInCkanInstance(username, gCubeGroupName, gCubeGroup.getGroupId(), 
							correspondentRoleToCheck, groupManager, gcubeCkanDataCatalogServiceImpl, orgsInWhichAtLeastEditorRole);

					toReturn = RolesCkanGroupOrOrg.getHigher(toReturn, correspondentRoleToCheck);
					
					logger.info("Found the role "+toReturn+" for " + username + " in the VRE " + gCubeGroupName);

				}

			}else if(groupManager.isVO(currentGroupId)){

				logger.debug("The list of organizations of the user " + username + " to scan is the one under the VO " + groupName);

				for (GCubeGroup gCubeGroup : groups) {

					// if the gCubeGroup is not under the VO or it is not the VO continue
					if(currentGroupId != gCubeGroup.getParentGroupId() || currentGroupId != gCubeGroup.getGroupId())
						continue;

					String gCubeGroupName = gCubeGroup.getGroupName();

					List<GCubeRole> roles = roleManager.listRolesByUserAndGroup(userid, groupManager.getGroupId(gCubeGroupName));

					// get highest role
					RolesCkanGroupOrOrg correspondentRoleToCheck = getLiferayHighestRoleInOrg(roles);

					// be sure it is so
					checkIfRoleIsSetInCkanInstance(username, gCubeGroupName, gCubeGroup.getGroupId(), 
							correspondentRoleToCheck, groupManager, gcubeCkanDataCatalogServiceImpl, orgsInWhichAtLeastEditorRole);

					toReturn = RolesCkanGroupOrOrg.getHigher(toReturn, correspondentRoleToCheck);
				}

			}else if(groupManager.isVRE(currentGroupId)){
				List<GCubeRole> roles = roleManager.listRolesByUserAndGroup(userManager.getUserId(username), groupManager.getGroupId(groupName));

				logger.debug("The current scope is the vre " + groupName);

				// get highest role
				RolesCkanGroupOrOrg correspondentRoleToCheck = getLiferayHighestRoleInOrg(roles);

				// be sure it is so
				checkIfRoleIsSetInCkanInstance(username, groupName, currentGroupId, 
						correspondentRoleToCheck, groupManager, gcubeCkanDataCatalogServiceImpl, orgsInWhichAtLeastEditorRole);

				toReturn = correspondentRoleToCheck;

			}
		}catch(Exception e){
			logger.error("Unable to retrieve the role information for this user. Returning member role", e);
			return RolesCkanGroupOrOrg.MEMBER;
		}

		// return the role
		logger.debug("Returning role "  + toReturn + " for user " + username);
		return toReturn;
	}

	/**
	 * Check if the role admin is set or must be set into the ckan instance at this scope
	 * @param username
	 * @param gCubeGroupName
	 * @param groupId
	 * @param correspondentRoleToCheck
	 * @param toReturn
	 * @param groupManager
	 * @param ckanPublisherServicesImpl
	 * @throws UserManagementSystemException
	 * @throws GroupRetrievalFault
	 */
	private static void checkIfRoleIsSetInCkanInstance(String username,
			String gCubeGroupName, long groupId,
			RolesCkanGroupOrOrg correspondentRoleToCheck,
			GroupManager groupManager, CKANPublisherServicesImpl ckanPublisherServicesImpl, List<OrganizationBean> orgs) throws UserManagementSystemException, GroupRetrievalFault {

		// with this invocation, we check if the role is present in ckan and if it is not it will be added
		DataCatalogue catalogue = ckanPublisherServicesImpl.getCatalogue(groupManager.getInfrastructureScope(groupId));

		// if there is an instance of ckan in this scope..
		if(catalogue != null){
			boolean res = catalogue.checkRoleIntoOrganization(username, gCubeGroupName, correspondentRoleToCheck);
			if(res && !correspondentRoleToCheck.equals(RolesCkanGroupOrOrg.MEMBER)){
				// get the orgs of the user and retrieve its title and name
				CkanOrganization organization = catalogue.getOrganizationByName(gCubeGroupName.toLowerCase());
				orgs.add(new OrganizationBean(organization.getTitle(), organization.getName(), true));
			}
		}else
			logger.warn("It seems there is no ckan instance into scope " + groupManager.getInfrastructureScope(groupId));
	}

	/**
	 * Retrieve the ckan roles among a list of liferay roles
	 * @param roles
	 * @return
	 */
	private static RolesCkanGroupOrOrg getLiferayHighestRoleInOrg(
			List<GCubeRole> roles) {
		// NOTE: it is supposed that there is just one role for this person correspondent to the one in the catalog
		for (GCubeRole gCubeRole : roles) {
			if(gCubeRole.getRoleName().equalsIgnoreCase(GatewayRolesNames.CATALOGUE_ADMIN.getRoleName())){
				return RolesCkanGroupOrOrg.ADMIN;
			}
			if(gCubeRole.getRoleName().equalsIgnoreCase(GatewayRolesNames.CATALOGUE_EDITOR.getRoleName())){
				return RolesCkanGroupOrOrg.EDITOR;
			}
		}
		return RolesCkanGroupOrOrg.MEMBER;
	}
	
}
