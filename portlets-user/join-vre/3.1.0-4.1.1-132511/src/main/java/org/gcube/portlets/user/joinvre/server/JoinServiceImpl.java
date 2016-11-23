package org.gcube.portlets.user.joinvre.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.gcube.application.framework.core.session.ASLSession;
import org.gcube.application.framework.core.session.SessionManager;
import org.gcube.common.portal.GCubePortalConstants;
import org.gcube.common.portal.PortalContext;
import org.gcube.portal.custom.scopemanager.scopehelper.ScopeHelper;
import org.gcube.portal.databook.server.DBCassandraAstyanaxImpl;
import org.gcube.portal.databook.server.DatabookStore;
import org.gcube.portal.databook.shared.Invite;
import org.gcube.portal.databook.shared.InviteStatus;
import org.gcube.portal.databook.shared.UserInfo;
import org.gcube.portlets.user.joinvre.client.JoinService;
import org.gcube.portlets.user.joinvre.shared.UserBelonging;
import org.gcube.portlets.user.joinvre.shared.VRE;
import org.gcube.portlets.user.joinvre.shared.VRECategory;
import org.gcube.portlets.user.joinvre.shared.VreMembershipType;
import org.gcube.vomanagement.usermanagement.GroupManager;
import org.gcube.vomanagement.usermanagement.UserManager;
import org.gcube.vomanagement.usermanagement.exception.GroupRetrievalFault;
import org.gcube.vomanagement.usermanagement.exception.UserManagementSystemException;
import org.gcube.vomanagement.usermanagement.exception.UserRetrievalFault;
import org.gcube.vomanagement.usermanagement.exception.VirtualGroupNotExistingException;
import org.gcube.vomanagement.usermanagement.impl.LiferayGroupManager;
import org.gcube.vomanagement.usermanagement.impl.LiferayUserManager;
import org.gcube.vomanagement.usermanagement.model.CustomAttributeKeys;
import org.gcube.vomanagement.usermanagement.model.GCubeGroup;
import org.gcube.vomanagement.usermanagement.model.GCubeMembershipRequest;
import org.gcube.vomanagement.usermanagement.model.GCubeUser;
import org.gcube.vomanagement.usermanagement.model.GroupMembershipType;
import org.gcube.vomanagement.usermanagement.model.MembershipRequestStatus;
import org.gcube.vomanagement.usermanagement.model.VirtualGroup;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.liferay.portal.kernel.cache.CacheRegistryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.VirtualHost;
import com.liferay.portal.security.auth.PrincipalThreadLocal;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.security.permission.PermissionThreadLocal;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.LayoutSetLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.service.VirtualHostLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;

/**
 * @author Massimiliano Assante, ISTI-CNR - massimiliano.assante@isti.cnr.it
 */
@SuppressWarnings("serial")
public class JoinServiceImpl extends RemoteServiceServlet implements JoinService {
	private static Log _log = LogFactoryUtil.getLog(JoinServiceImpl.class);
	public static final String TEST_USER = "test.user";
	private static final String TEST_SCOPE = "/gcube/devsec/devVRE";
	private static DatabookStore store;
	/**
	 * the current ASLSession
	 * @return the session
	 */
	private ASLSession getASLSession() {
		String sessionID = this.getThreadLocalRequest().getSession().getId();
		String user = (String) this.getThreadLocalRequest().getSession().getAttribute(ScopeHelper.USERNAME_ATTRIBUTE);
		if (user == null) {
			_log.warn("USER IS NULL setting test.user and Running OUTSIDE PORTAL");
			user = getDevelopmentUser();
			SessionManager.getInstance().getASLSession(sessionID, user).setScope("/gcube");
		}		
		return SessionManager.getInstance().getASLSession(sessionID, user);
	}

	/**
	 * when packaging test will fail if the user is not set to test.user
	 * @return .
	 */
	public String getDevelopmentUser() {
		String user = TEST_USER;
//				user = "andrea.rossi";
		return user;
	}

	/**
	 * 
	 * @return true if you're running into the portal, false if in development
	 */
	private boolean isWithinPortal() {
		try {
			UserLocalServiceUtil.getService();
			return true;
		} catch (com.liferay.portal.kernel.bean.BeanLocatorException ex) {			
			_log.trace("Development Mode ON");
			return false;
		}			
	}
	@Override
	public String joinVRE(Long vreID) {
		try {
			return PortalContext.getConfiguration().getSiteLandingPagePath(getThreadLocalRequest());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	/**
	 * 
	 * @param session the Asl Session
	 * @param withinPortal true when is on Liferay portal
	 * @return the users belonging to the current organization (scope)
	 */
	@Override
	public LinkedHashMap<VRECategory, ArrayList<VRE>> getVREs() {
		LinkedHashMap<VRECategory, ArrayList<VRE>> toReturn = new LinkedHashMap<VRECategory, ArrayList<VRE>>();

		try {
			if (isWithinPortal()) {
				toReturn = getPortalSitesMappedToVRE();
			} else {
				toReturn = getFakePortalVREs();			}
		} catch (Exception e) {
			_log.error("Error getting VREs", e);
		}


		return toReturn;
	}
	/**
	 * 
	 * @return the Virtual groups with their VREs in the order estabilished in the LR Control Panel
	 * @throws SystemException
	 * @throws PortalException
	 */
	public LinkedHashMap<VRECategory, ArrayList<VRE>> getPortalSitesMappedToVRE() throws Exception {

		ASLSession session = getASLSession();
		GroupManager gm = new LiferayGroupManager();
		LinkedHashMap<VRECategory, ArrayList<VRE>> toReturn = new LinkedHashMap<VRECategory, ArrayList<VRE>>();

		long currentSiteGroupId = getSiteFromServletRequest(getThreadLocalRequest()).getGroupId();
		List<VirtualGroup> currentSiteVGroups = getVirtualGroups(currentSiteGroupId);
		for (VirtualGroup vg : currentSiteVGroups) {
			ArrayList<VRE> toCreate = new ArrayList<VRE>();
			VRECategory cat = new VRECategory(1L, vg.getName(), vg.getDescription());
			toReturn.put(cat, toCreate);
		}

		GCubeGroup rootGroupVO = gm.getRootVO();

		try {
			_log.debug("root: " + rootGroupVO.getGroupName() );
		} catch (NullPointerException e) {
			_log.error("Cannot find root organziation, please check gcube-data.properties file in $CATALINA_HOME/conf folder, unless your installing the Bundle");
			return toReturn;
		}
		
		List<GCubeGroup> currUserGroups = new ArrayList<GCubeGroup>();
		if (session.getUsername().compareTo(TEST_USER) != 0) {
			GCubeUser currUser = new LiferayUserManager().getUserByUsername(session.getUsername());
			currUserGroups = gm.listGroupsByUser(currUser.getUserId());
		}
		
		//for each root sub organizations (VO)
		for (GCubeGroup vOrg : rootGroupVO.getChildren()) {
			for (GCubeGroup vreOrganization : vOrg.getChildren()) {
				long vreID =  vreOrganization.getGroupId();
				String vreName = vreOrganization.getGroupName();
				String vreDescription = vreOrganization.getDescription();

				long logoId = vreOrganization.getLogoId();
				String vreLogoURL = gm.getGroupLogoURL(logoId);
				String groupName = gm.getInfrastructureScope(vreOrganization.getGroupId());
				String friendlyURL =  GCubePortalConstants.PREFIX_GROUP_URL+vreOrganization.getFriendlyURL();

				String catName = gm.getVirtualGroup(vreID).getName();

				VRECategory toLookFor = null;
				for (VRECategory vre : toReturn.keySet()) {
					if (vre.getName().compareTo(catName)==0)
						toLookFor = vre;
				}
				if (toLookFor != null) {
					ArrayList<VRE> toUpdate = toReturn.get(toLookFor);
					UserBelonging belongs = UserBelonging.NOT_BELONGING;
					VRE toAdd = new VRE(vreID,vreName, vreDescription, vreLogoURL, groupName, friendlyURL, belongs, getVREMembershipType(vreOrganization.getMembershipType()));				
					if (session.getUsername().compareTo(TEST_USER) != 0) {
						GCubeUser currUser = new LiferayUserManager().getUserByUsername(session.getUsername());
						//check if the user belongs to it
						if (currUserGroups.contains(vreOrganization)) {
							toAdd.setUserBelonging(UserBelonging.BELONGING);
						}
						else if (checkPending(currUser.getUsername(), vreOrganization.getGroupId()))
							toAdd.setUserBelonging(UserBelonging.PENDING);
					}
					toUpdate.add(toAdd);
				}				
			}
		}

		//sort the vres in the groups
		for (VRECategory cat : toReturn.keySet()) {
			ArrayList<VRE> toSort = toReturn.get(cat);
			Collections.sort(toSort);
		}
		return toReturn;
	}



	/**
	 * useful method for development purpose
	 * @return
	 */
	private LinkedHashMap<VRECategory, ArrayList<VRE>> getFakePortalVREs() {
		LinkedHashMap<VRECategory, ArrayList<VRE>> toReturn = new LinkedHashMap<VRECategory, ArrayList<VRE>>();
		VRECategory devsecCategory = new VRECategory(1, "Z_Development", "designed to apply Data Mining techniques to biological data. "
				+ "The algorithms are executed in a distributed fashion on the e-Infrastructure nodes or on local multi-core machines.");
		ArrayList<VRE> vres = new ArrayList<VRE>();

		vres.add(new VRE(0, "BiodiversityLab", ""
				+ "<h2>BiodiversityLab</h2>"
				+ "The BiodiversityLab is a VRE designed to provide a collection of applications that allow scholars to perform complete experiments about "
				+ "single individuals or groups of marine species. The VRE allows to: <ul> <li> inspect species maps;<li> produce a species distribution map by means of either an expert system (AquaMaps) or a machine learning model (e.g. Neural Networks);"
				+ "<li> analyse species observation trends;"
				+ "<li> inspect species occurrence data;"
				+ "<li> inspect species descriptions and characteristics;"
				+ "<li> perform analysis of climatic changes and of their effects on species distribution;"
				+ "<li> produce GIS maps for geo-spatial datasets;"
				+ "<li> discover Taxa names;"
				+ "<li> cluster occurrence data;"
				+ "<li> estimate similarities among habitats."
				+ "</ul>"
				+ "", "", "http://placehold.it/200x200", "/group/devsec", UserBelonging.NOT_BELONGING));
		vres.add(new VRE(0, "Scalable Data", ""
				+ "<h2>Scalable Data Mining</h2>"
				+ "The Scalable Data Mining  is a VRE designed to apply Data Mining techniques to biological data. The algorithms are executed in a distributed fashion on the e-Infrastructure nodes or on local multi-core machines. Scalability is thus meant as distributed data processing but even as services dynamically provided to the users. The system is scalable in the number of users and in the size of the data to process. Statistical data processing can be applied to perform Niche Modelling or Ecological Modelling experiments. Other applications can use general purpose techniques like Bayesian models. Time series of observations can be managed as well, in order to classify trends, catch anomaly patterns and perform simulations. The idea under the distributed computation for data mining techniques is to overcome common limitations that can happen when using statistical algorithms: "
				+ "single individuals or groups of marine species. The VRE allows to: <ul> <li> inspect species maps;<li> produce a species distribution map by means of either an expert system (AquaMaps) or a machine learning model (e.g. Neural Networks);"
				+ "<li> analyse species observation trends;"
				+ "<li> inspect species occurrence data;"
				+ "<li> inspect species descriptions and characteristics;"
				+ "<li> perform analysis of climatic changes and of their effects on species distribution;"
				+ "<li> produce GIS maps for geo-spatial datasets;"
				+ "<li> discover Taxa names;"
				+ "<li> cluster occurrence data;"
				+ "<li> estimate similarities among habitats."
				+ "</ul>"
				+ "", "", "http://placehold.it/200x200", "/group/devsec", UserBelonging.NOT_BELONGING));
		toReturn.put(devsecCategory, vres);

		devsecCategory = new VRECategory(2, "Sailing", "Sailing prod desc");
		vres = new ArrayList<VRE>();
		vres.add(new VRE(2, "devmode", "devmode VRE description", "http://placehold.it/200x100", "https://placeholdit.imgix.net/~text?txtsize=19&txt=200%C3%97100&w=200&h=100", "/group/devmode", UserBelonging.NOT_BELONGING, VreMembershipType.PRIVATE));
		vres.add(new VRE(1, "StrategicInvestmentAnalysis", "devVRE VRE description", "", "https://placeholdit.imgix.net/~text?txtsize=19&txt=200%C3%97100&w=200&h=100", "/group/devVRE", UserBelonging.NOT_BELONGING, VreMembershipType.OPEN));
		vres.add(new VRE(2, "devmode2", "devmode VRE description", "http://placehold.it/200x100", "", "/group/devmode", UserBelonging.NOT_BELONGING, VreMembershipType.OPEN));
		vres.add(new VRE(1, "devVR3E", "devVRE VRE description", "http://placehold.it/200x200", "aaaa", "/group/devVRE", UserBelonging.NOT_BELONGING, VreMembershipType.PRIVATE));
		vres.add(new VRE(2, "devmode3", "devmode VRE description", "http://placehold.it/200x200", "", "/group/devmode", UserBelonging.NOT_BELONGING, VreMembershipType.PRIVATE));
		vres.add(new VRE(1, "devVRE4", "devVRE VRE description", "", "http://placehold.it/200x200", "/group/devVRE", UserBelonging.NOT_BELONGING, VreMembershipType.PRIVATE));
		vres.add(new VRE(2, "devmode4", "devmode VRE description", "", "http://placehold.it/200x200", "/group/devmode", UserBelonging.NOT_BELONGING, VreMembershipType.PRIVATE));
		vres.add(new VRE(1, "devVRE5", "devVRE VRE description", "", "http://placehold.it/200x200", "/group/devVRE", UserBelonging.NOT_BELONGING, VreMembershipType.PRIVATE));
		vres.add(new VRE(2, "devmode5", "devmode VRE description", "", "http://placehold.it/200x200", "/group/devmode", UserBelonging.NOT_BELONGING, VreMembershipType.PRIVATE));
		vres.add(new VRE(1, "devVRE6", "devVRE VRE description", "", "http://placehold.it/200x200", "/group/devVRE", UserBelonging.NOT_BELONGING, VreMembershipType.PRIVATE));
		vres.add(new VRE(2, "devmode6", "devmode VRE description", "", "http://placehold.it/200x200", "/group/devmode", UserBelonging.NOT_BELONGING, VreMembershipType.PRIVATE));
		vres.add(new VRE(1, "devVRE7", "devVRE VRE description", "", "http://placehold.it/200x200", "/group/devVRE", UserBelonging.NOT_BELONGING, VreMembershipType.PRIVATE));
		vres.add(new VRE(2, "devmod76", "devmode VRE description", "", "http://placehold.it/200x200", "/group/devmode", UserBelonging.NOT_BELONGING));
		toReturn.put(devsecCategory, vres);
		return toReturn;
	}

	@Override
	public VRE getSelectedVRE(Long groupId) {
		_log.debug("*getting Selected Research Environment from referral, site id = " + groupId);
		ASLSession session = getASLSession();
		VRE toReturn = null;
		try {
			GroupManager gm = new LiferayGroupManager();
			UserManager um = new LiferayUserManager();
			GCubeGroup selectedVRE = gm.getGroup(groupId);
			String vreName = selectedVRE.getGroupName();
			String vreDescription = selectedVRE.getDescription();

			
			long logoId = selectedVRE.getLogoId();
			String vreLogoURL = gm.getGroupLogoURL(logoId);
			String infraScope = gm.getInfrastructureScope(selectedVRE.getGroupId());
			String friendlyURL =  GCubePortalConstants.PREFIX_GROUP_URL+selectedVRE.getFriendlyURL();

			
			
			
			GCubeUser currUser = um.getUserByUsername(session.getUsername());
			//check if the user belongs to it
			UserBelonging belongEnum = UserBelonging.NOT_BELONGING;
			if (gm.listGroupsByUser(currUser.getUserId()).contains(selectedVRE)) 
				belongEnum = UserBelonging.BELONGING;
			else if (checkPending(session.getUsername(), selectedVRE.getGroupId()))
				belongEnum = UserBelonging.PENDING;
			//return the selected VRE for this user
			toReturn = new VRE(groupId, vreName, vreDescription, vreLogoURL, infraScope, friendlyURL, belongEnum, getVREMembershipType(selectedVRE.getMembershipType()));
		} catch (Exception e) {
			_log.error("Something wrong happened while trying to getSite by id, probably the group id is wrong. " + e.getMessage());
		}
		return toReturn;
	}
	
	/**
	 * 
	 * @param type
	 * @return the correspondent mapping to the gcube model
	 */
	private VreMembershipType getVREMembershipType(GroupMembershipType type) {
		switch (type) {
		case RESTRICTED:
			return VreMembershipType.RESTRICTED;
		case OPEN:
			return VreMembershipType.OPEN;
		default:
			return VreMembershipType.PRIVATE;
		}
	}
	/**
	 * 
	 * @param screenName
	 * @param groupId
	 * @return
	 * @throws UserRetrievalFault 
	 * @throws GroupRetrievalFault 
	 * @throws UserManagementSystemException 
	 */
	private static boolean checkPending(String screenName, long groupId) throws UserManagementSystemException, GroupRetrievalFault, UserRetrievalFault {
		UserManager um = new LiferayUserManager();
		List<GCubeMembershipRequest> requests  = um.listMembershipRequestsByGroup(groupId);
		for (GCubeMembershipRequest r : requests) {
			if ( r.getStatus() == MembershipRequestStatus.REQUEST && (r.getRequestingUser().getUsername().compareTo(screenName)==0))
				return true;
		}
		return false; 
	}

	@Override
	public void addMembershipRequest(String scope, String optionalMessage) {
		String username = getASLSession().getUsername();
		if (optionalMessage == null || optionalMessage.compareTo("") == 0) 
			optionalMessage = "none";
		try {
			CacheRegistryUtil.clear();
			GroupManager gm = new LiferayGroupManager();
			long groupId = gm.getGroupIdFromInfrastructureScope(scope);
			_log.debug("Look if a request exists already");
			List<GCubeGroup> userGroups = gm.listGroupsByUser(new LiferayUserManager().getUserId(username));
			for (GCubeGroup g : userGroups) {
				if (g.getGroupId() == groupId) {
					_log.warn("User already belongs to " + scope + " SKIP addMembershipRequest");
					return;
				}
			}	
			CacheRegistryUtil.clear();
			if (checkPending(username, groupId)) {
				_log.warn("User already asked for " + scope + " REQUEST IS IN PENDING - SKIP addMembershipRequest");
				return;
			}			
			_log.debug("Request does not exist, addMembershipRequest for user " + username);
			LoginServiceUtil.addMembershipRequest(username, scope, optionalMessage, getThreadLocalRequest());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	public String getPortalUrl() throws PortalException, SystemException {
		return PortalUtil.getPortalURL(this.getThreadLocalRequest());
	}

	/**
	 * the user to the VRE, plus send notifications to the vre manages of the vre
	 * in order to register a user i had to create a fake membership request because assigning a user to a group would have required
	 * the user to logout and login otherwise
	 */
	@Override
	public boolean registerUser(String scope, long groupId, boolean isInvitation) {
		UserManager um = new LiferayUserManager();
		try {
			ASLSession session = getASLSession();
			String username = session.getUsername();
			_log.debug("registerUser " +username + " to "+ scope);
			GCubeUser currUser = um.getUserByUsername(username);
			GroupManager gm = new LiferayGroupManager();
			um.requestMembership(currUser.getUserId(), gm.getGroupIdFromInfrastructureScope(scope), "Automatic Request at " + new Date());
			_log.info("fakeRequest sent");
			String replierUsername = LiferayUserManager.getAdmin().getScreenName();
			_log.trace("Sleep 1 second ...");
			Thread.sleep(1000);
			um.acceptMembershipRequest(currUser.getUserId(), groupId, true, replierUsername, "Automatic acceptance request at " + new Date());
			_log.info("fakeRequest accepted");
			if (isInvitation) {
				initStore();
				String inviteId = store.isExistingInvite(scope, session.getUserEmailAddress());
				if (inviteId != null) {
					Invite invite = store.readInvite(inviteId);
					store.setInviteStatus(scope, session.getUserEmailAddress(), InviteStatus.ACCEPTED);
					LoginServiceUtil.notifyUserAcceptedInvite(username, scope, invite, getThreadLocalRequest());
				}
			}
			else {			
				LoginServiceUtil.notifyUserSelfRegistration(username, scope, getThreadLocalRequest());
				_log.info("notifyUserSelfRegistration sent");
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * 
	 * @return the unique instance of the store
	 */
	public static synchronized DatabookStore initStore() {
		if (store == null) {
			store = new DBCassandraAstyanaxImpl();
		}
		return store;
	}

	@Override
	public String isExistingInvite(long groupId) {
		getASLSession();
		_log.debug("initiating Store");
		initStore();
		_log.debug("initStore OK");
		String infraScope = "";
		String email = "";
		if (! isWithinPortal()) {
			infraScope = TEST_SCOPE;
			email = "m.assante@gmail.com";
		} else {
			GroupManager gm = new LiferayGroupManager();	
			email = getASLSession().getUserEmailAddress();
			try {
				infraScope = gm.getInfrastructureScope(groupId);
			} catch (UserManagementSystemException | GroupRetrievalFault e) {
				e.printStackTrace();
			}
		}
		_log.debug("checking if invite exists for " + email + " on " +infraScope);

		return store.isExistingInvite(infraScope, email);
	}

	@Override
	public UserInfo readInvite(String inviteId) {
		initStore();		
		try {
			Invite invite = store.readInvite(inviteId);
			GCubeUser inviter = new LiferayUserManager().getUserByUsername(invite.getSenderUserId());
			return new UserInfo(
					inviter.getUsername(),
					inviter.getFullname(), 
					inviter.getUserAvatarURL(), 
					"", "", true, false, null); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}	

	/**
	 * read the list of virtual groups the current site (i-marine, services etc. ) should show up
	 * @param actualGroupId
	 * @return he list of virtual groups the current site (i-marine, services etc. ) should show up
	 * @throws GroupRetrievalFault
	 * @throws VirtualGroupNotExistingException
	 */
	private List<VirtualGroup> getVirtualGroups(long actualGroupId) throws GroupRetrievalFault, VirtualGroupNotExistingException {
		List<VirtualGroup> toReturn = new ArrayList<VirtualGroup>();
		try {
			long userId = LiferayUserManager.getAdmin().getUserId();
			PrincipalThreadLocal.setName(userId);
			PermissionChecker permissionChecker = PermissionCheckerFactoryUtil.create(UserLocalServiceUtil.getUser(userId));
			PermissionThreadLocal.setPermissionChecker(permissionChecker); 
			Group site = GroupLocalServiceUtil.getGroup(actualGroupId);
			_log.debug("Set Thread Permission done, getVirtual Group of " + site.getName());
			if (site.getExpandoBridge().getAttribute(CustomAttributeKeys.VIRTUAL_GROUP.getKeyName()) == null ||  site.getExpandoBridge().getAttribute(CustomAttributeKeys.VIRTUAL_GROUP.getKeyName()).equals("")) {
				String warningMessage = String.format("Attribute %s not initialized.", CustomAttributeKeys.VIRTUAL_GROUP.getKeyName());
				_log.warn(warningMessage); 
				throw new VirtualGroupNotExistingException(warningMessage);
			} else {
				String[] values = (String[]) site.getExpandoBridge().getAttribute(CustomAttributeKeys.VIRTUAL_GROUP.getKeyName());  
				VirtualGroup toAdd = new VirtualGroup();
				if (values != null && values.length > 0) {
					for (int i = 0; i < values.length; i++) {
						toAdd = new VirtualGroup();
						String[] splits = values[i].split("\\|");
						toAdd.setName(splits[0]);
						toAdd.setDescription(splits[1]);
						toReturn.add(toAdd);
						_log.debug("VirtualGroup selected found for " + site.getName() + " -> " + toAdd.getName());
					}					
				} else {
					toAdd.setName("NoVirtualGroupAssigned");
					toAdd.setDescription("NoVirtualGroupDescription");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return toReturn;
	}
	/**
	 * 
	 * @param request
	 * @return the current Group instance based on the request
	 * @throws PortalException
	 * @throws SystemException
	 */
	private Group getSiteFromServletRequest(final HttpServletRequest request) throws PortalException, SystemException {
		String serverName = request.getServerName();
		_log.debug("currentHost is " +  serverName);
		Group site = null;
		List<VirtualHost> vHosts = VirtualHostLocalServiceUtil.getVirtualHosts(0, VirtualHostLocalServiceUtil.getVirtualHostsCount());
		for (VirtualHost virtualHost : vHosts) {
			_log.debug("Found  " +  virtualHost.getHostname());
			if (virtualHost.getHostname().compareTo("localhost") != 0 && 
					virtualHost.getLayoutSetId() != 0 && 
					virtualHost.getHostname().compareTo(serverName) == 0) {
				long layoutSetId = virtualHost.getLayoutSetId();
				site = LayoutSetLocalServiceUtil.getLayoutSet(layoutSetId).getGroup();
				_log.debug("Found match! Your site is " +  site.getName());
				return site;
			}
		}
		return null;
	}
}
