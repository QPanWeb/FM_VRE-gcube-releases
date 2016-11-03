package org.gcube.portlets.user.contactinformation.server;

import java.util.HashMap;

import org.gcube.application.framework.core.session.ASLSession;
import org.gcube.application.framework.core.session.SessionManager;
import org.gcube.common.scope.impl.ScopeBean;
import org.gcube.common.scope.impl.ScopeBean.Type;
import org.gcube.portal.custom.communitymanager.SiteManagerUtil;
import org.gcube.portal.custom.scopemanager.scopehelper.ScopeHelper;
import org.gcube.portal.databook.shared.UserInfo;
import org.gcube.portlets.user.contactinformation.client.ContactInfoService;
import org.gcube.portlets.user.contactinformation.shared.ContactType;
import org.gcube.portlets.user.contactinformation.shared.UserContext;
import org.gcube.vomanagement.usermanagement.UserManager;
import org.gcube.vomanagement.usermanagement.impl.LiferayUserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.liferay.portal.model.Contact;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class ContactInfoServiceImpl extends RemoteServiceServlet implements ContactInfoService {

	private static final Logger _log = LoggerFactory.getLogger(ContactInfoServiceImpl.class);

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
		}		

		_log.debug("Username in ASLSESSION is " + user);
		return SessionManager.getInstance().getASLSession(sessionID, user);
	}

	/**
	 * when packaging test will fail if the user is not set to test.user
	 * @return .
	 */
	public String getDevelopmentUser() {
		String user = "test.user";
		//		user = "costantino.perciante";
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
		} 
		catch (com.liferay.portal.kernel.bean.BeanLocatorException ex) {			
			_log.trace("Development Mode ON");
			return false;
		}			
	}

	@Override
	public UserContext getUserContext(String userid) {
		if (userid == null || userid.equals("") || userid.equals(getASLSession().getUsername())) {			
			_log.info("Own Profile");
			return getOwnProfile();
		}
		else {		
			_log.info(userid + " Reading Profile");
			return getUserProfile(userid);
		}
	}

	@Override
	public boolean updateContactInformation(
			HashMap<ContactType, String> contactInfo) {

		if(isWithinPortal()){
			// get usermanager
			UserManager userManager = new LiferayUserManager();

			String username = getASLSession().getUsername();
			String mySpacesn = contactInfo.get(ContactType.IN); // mySpace field used for Linkedin 
			String twittersn = contactInfo.get(ContactType.TWITTER);
			String facebooksn = contactInfo.get(ContactType.FB);
			String skypesn = contactInfo.get(ContactType.SKYPE);
			String jabbersn = contactInfo.get(ContactType.GOOGLE);
			String aimsn = contactInfo.get(ContactType.AIM);

			return userManager.updateContactInformation(username, mySpacesn, twittersn, facebooksn, skypesn, jabbersn, aimsn);
		}else
			return true;
	}

	private UserContext getOwnProfile() {
		try {
			ASLSession session = getASLSession();
			String username = session.getUsername();

			String fullName = username+" FULL";
			String thumbnailURL = "images/Avatar_default.png";

			if (isWithinPortal()) {
				com.liferay.portal.model.UserModel user = UserLocalServiceUtil.getUserByScreenName(SiteManagerUtil.getCompany().getCompanyId(), username);
				thumbnailURL = "/image/user_male_portrait?img_id="+user.getPortraitId();
				fullName = user.getFirstName() + " " + user.getLastName();
				HashMap<String, String> vreNames = new HashMap<String, String>();
				UserInfo userInfo = new UserInfo(username, fullName, thumbnailURL, user.getEmailAddress(), null, true, false, vreNames);
				User theUser = SiteManagerUtil.validateUser(session.getUsername());
				return new UserContext(userInfo, getInformations(theUser), true, isInfrastructureScope());
			}
			else {
				_log.info("Returning test USER");
				HashMap<String, String> fakeVreNames = new  HashMap<String, String>();
				fakeVreNames.put("/gcube/devsec/devVRE","devVRE");
				UserInfo user =  new UserInfo(username, username+ "FULL", thumbnailURL, "", "fakeAccountUrl", true, false, fakeVreNames);
				HashMap<ContactType, String> info = new HashMap<>();
				info.put(ContactType.IN, "");
				info.put(ContactType.TWITTER, "");
				info.put(ContactType.FB, "francesco.ciappi");
				info.put(ContactType.SKYPE, "");
				info.put(ContactType.GOOGLE, "francesco.ciappi@gmail.com");		
				info.put(ContactType.AIM, "");
				return new UserContext(user, info, true, false);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new UserContext();
	}

	/**
	 * Retrieve current information
	 * @param user
	 * @return
	 * @throws Exception
	 */
	private HashMap<ContactType, String> getInformations(User user) throws Exception {
		Contact theContact = user.getContact();
		HashMap<ContactType, String> toReturn = new HashMap<ContactType, String>();
		toReturn.put(ContactType.IN, theContact.getMySpaceSn() == null? "" : theContact.getMySpaceSn());
		toReturn.put(ContactType.TWITTER, theContact.getTwitterSn() == null? "" : theContact.getTwitterSn());
		toReturn.put(ContactType.FB, theContact.getFacebookSn() == null? "" : theContact.getFacebookSn());
		toReturn.put(ContactType.SKYPE, theContact.getSkypeSn() == null? "" : theContact.getSkypeSn());
		toReturn.put(ContactType.GOOGLE, theContact.getJabberSn() == null? "" : theContact.getJabberSn());		
		toReturn.put(ContactType.AIM, theContact.getAimSn() == null? "" : theContact.getAimSn());
		return toReturn;
	}

	private UserContext getUserProfile(String username) {
		String fullName = username+" FULL";
		String thumbnailURL = "images/Avatar_default.png";
		if (isWithinPortal()) {		
			try {
				com.liferay.portal.model.UserModel user = UserLocalServiceUtil.getUserByScreenName(SiteManagerUtil.getCompany().getCompanyId(), username);
				thumbnailURL = "/image/user_male_portrait?img_id="+user.getPortraitId();
				fullName = user.getFirstName() + " " + user.getLastName();
				HashMap<String, String> vreNames = new HashMap<String, String>();
				UserInfo userInfo = new UserInfo(username, fullName, thumbnailURL, user.getEmailAddress(), "", true, false, vreNames);
				User theUser = SiteManagerUtil.validateUser(username);
				return new UserContext(userInfo, getInformations(theUser), false, isInfrastructureScope());

			} catch (Exception e) {
				e.printStackTrace();
				return new UserContext();
			} 
		} else {
			_log.info("Returning test USER");
			HashMap<String, String> fakeVreNames = new  HashMap<String, String>();
			fakeVreNames.put("/gcube/devsec/devVRE","devVRE");
			return null;
		}		
	}

	/**
	 * Indicates whether the scope is the whole infrastructure.
	 * @return <code>true</code> if it is, <code>false</code> otherwise.
	 */
	private boolean isInfrastructureScope() {
		ScopeBean scope = new ScopeBean(getASLSession().getScope());
		return 	scope.is(Type.INFRASTRUCTURE);
	}
}
