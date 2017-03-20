package org.gcube.portlets.user.newsfeed.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletContext;

import org.gcube.applicationsupportlayer.social.ApplicationNotificationsManager;
import org.gcube.applicationsupportlayer.social.NotificationsManager;
import org.gcube.applicationsupportlayer.social.shared.SocialNetworkingSite;
import org.gcube.applicationsupportlayer.social.shared.SocialNetworkingUser;
import org.gcube.common.portal.GCubePortalConstants;
import org.gcube.common.portal.PortalContext;
import org.gcube.portal.databook.server.DBCassandraAstyanaxImpl;
import org.gcube.portal.databook.server.DatabookStore;
import org.gcube.portal.databook.shared.Attachment;
import org.gcube.portal.databook.shared.Comment;
import org.gcube.portal.databook.shared.EnhancedFeed;
import org.gcube.portal.databook.shared.Feed;
import org.gcube.portal.databook.shared.Like;
import org.gcube.portal.databook.shared.RangeFeeds;
import org.gcube.portal.databook.shared.ShowUserStatisticAction;
import org.gcube.portal.databook.shared.UserInfo;
import org.gcube.portal.databook.shared.ex.ColumnNameNotFoundException;
import org.gcube.portal.databook.shared.ex.FeedIDNotFoundException;
import org.gcube.portal.databook.shared.ex.FeedTypeNotFoundException;
import org.gcube.portal.databook.shared.ex.LikeIDNotFoundException;
import org.gcube.portal.databook.shared.ex.PrivacyLevelTypeNotFoundException;
import org.gcube.portal.notifications.bean.GenericItemBean;
import org.gcube.portal.notifications.thread.CommentNotificationsThread;
import org.gcube.portal.notifications.thread.LikeNotificationsThread;
import org.gcube.portal.notifications.thread.MentionNotificationsThread;
import org.gcube.portlets.user.newsfeed.client.NewsService;
import org.gcube.portlets.user.newsfeed.shared.MoreFeedsBean;
import org.gcube.portlets.user.newsfeed.shared.NewsConstants;
import org.gcube.portlets.user.newsfeed.shared.OperationResult;
import org.gcube.portlets.user.newsfeed.shared.UserSettings;
import org.gcube.portlets.widgets.pickitem.shared.ItemBean;
import org.gcube.social_networking.socialutillibrary.Utils;
import org.gcube.socialnetworking.social_data_search_client.ElasticSearchClient;
import org.gcube.socialnetworking.social_data_search_client.ElasticSearchClientImpl;
import org.gcube.vomanagement.usermanagement.GroupManager;
import org.gcube.vomanagement.usermanagement.UserManager;
import org.gcube.vomanagement.usermanagement.exception.GroupRetrievalFault;
import org.gcube.vomanagement.usermanagement.exception.TeamRetrievalFault;
import org.gcube.vomanagement.usermanagement.exception.UserManagementSystemException;
import org.gcube.vomanagement.usermanagement.exception.UserRetrievalFault;
import org.gcube.vomanagement.usermanagement.exception.VirtualGroupNotExistingException;
import org.gcube.vomanagement.usermanagement.impl.LiferayGroupManager;
import org.gcube.vomanagement.usermanagement.impl.LiferayRoleManager;
import org.gcube.vomanagement.usermanagement.impl.LiferayUserManager;
import org.gcube.vomanagement.usermanagement.model.GCubeGroup;
import org.gcube.vomanagement.usermanagement.model.GCubeTeam;
import org.gcube.vomanagement.usermanagement.model.GCubeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.service.UserLocalServiceUtil;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class NewsServiceImpl extends RemoteServiceServlet implements NewsService {

	private static final Logger _log = LoggerFactory.getLogger(NewsServiceImpl.class);

	private static final String VRE_LABEL = "VRE_LABEL";
	private static final String SHOW_TIMELINE_SOURCE = "SHOW_TIMELINE_SOURCE";
	private static final String REFRESH_TIME = "REFRESH_TIME";

	private static final String SESSION_ADMIN_ATTR = "SESSION_ADMIN_ATTR";
	public static final String NF_ORGANIZATIONUSERS_CACHE = "NF_ORGANIZATIONUSERS_CACHE";
	public static final String LAST_RETRIEVED_TIME = "NF_LAST_RETRIEVED_TIME";

	private String APP_ID;

	/**
	 * Cassandra client
	 */
	private DatabookStore store;

	/**
	 * Elasticsearch client
	 */
	private ElasticSearchClient escl;

	private final static int MAX_FEEDS_NO = 30;

	public void init() {
		store = new DBCassandraAstyanaxImpl();

		try {
			escl = new ElasticSearchClientImpl(null);
			_log.info("Elasticsearch connection created");
		} catch (Exception e) {
			escl = null;
			_log.error("Unable to create elasticsearch client connection!!!", e);
		}

		APP_ID = this.getClass().getName();
	}

	public void destroy() {
		store.closeConnection();
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
	/**
	 * this is the first method called by the web app
	 */
	@Override
	public UserSettings getUserSettings() {
		try {
			PortalContext context = PortalContext.getConfiguration();
			GCubeUser currUser = context.getCurrentUser(getThreadLocalRequest());
			String username = currUser.getUsername();
			String email = currUser.getEmail();
			String fullName = currUser.getFullname();
			String thumbnailURL = currUser.getUserAvatarURL();

			String groupName = context.getCurrentGroupName(getThreadLocalRequest());


			final String profilePageURL = 
					GCubePortalConstants.PREFIX_GROUP_URL + 
					PortalContext.getConfiguration().getSiteLandingPagePath(getThreadLocalRequest())+
					GCubePortalConstants.USER_PROFILE_FRIENDLY_URL;
			String accountURL = profilePageURL;

			UserInfo userInfo = new UserInfo(username, fullName, thumbnailURL, email, accountURL, true, isAdmin(), null);
			CustomConfiguration config = getUserConfiguration();
			UserSettings toReturn = new UserSettings(userInfo, config.getRefreshTime(), groupName, config.getVreLabel(), isInfrastructureScope(), config.isShowTimelineSource());
			return toReturn;


		} catch (Exception e) {
			e.printStackTrace();
		}
		return new UserSettings();
	}

	/**
	 * Indicates whether the scope is the whole infrastructure.
	 * @return <code>true</code> if it is, <code>false</code> otherwise.
	 * @throws GroupRetrievalFault 
	 * @throws UserManagementSystemException 
	 */
	private boolean isInfrastructureScope() throws UserManagementSystemException, GroupRetrievalFault {
		PortalContext context = PortalContext.getConfiguration();
		String scope = context.getCurrentScope(getThreadLocalRequest());

		long groupId = context.getCurrentGroupId(getThreadLocalRequest());
		boolean isInfrastructureScope = new LiferayGroupManager().isRootVO(groupId);
		_log.debug("isInfrastructureScope? " + scope + " groupId=" + groupId + " ? i say " + isInfrastructureScope);
		return isInfrastructureScope;
	}

	@Override
	public ArrayList<EnhancedFeed> getAllUpdateUserFeeds(int feedsNoPerCategory) {

		ArrayList<Feed> toMerge = new ArrayList<Feed>();
		HashMap<String, Feed> feedsMap = new HashMap<String, Feed>();
		try {
			GCubeUser currUser = PortalContext.getConfiguration().getCurrentUser(getThreadLocalRequest());
			_log.info("****** retrieving posts for user: " + currUser.getUsername());
			/**
			 * this handles the case where the portlet is deployed outside of VREs (regular)
			 */
			if (isInfrastructureScope()) {
				_log.debug("****** risInfrastructureScope() = true");
				//VRE Feeds
				GroupManager gm = new LiferayGroupManager();
				for (GCubeGroup group : gm.listGroupsByUserAndSite(currUser.getUserId(), getThreadLocalRequest().getServerName())) {		
					if (gm.isVRE(group.getGroupId())) {
						String vreid = gm.getInfrastructureScope(group.getGroupId()); //get the scope 
						_log.debug("Reading feeds for VRE: " + vreid);
						ArrayList<Feed> OrganizationFeeds =  (ArrayList<Feed>) store.getRecentFeedsByVRE(vreid, feedsNoPerCategory); 
						for (Feed feed : OrganizationFeeds) {
							feedsMap.put(feed.getKey(), feed);							
						}
					}
				}

				//Portal Feeds
				ArrayList<Feed> portalFeeds = (ArrayList<Feed>) store.getAllPortalPrivacyLevelFeeds();	
				for (Feed feed : portalFeeds) {
					feedsMap.put(feed.getKey(), feed);
				}
			}
			//else must be in a VRE scope
			else {
				PortalContext context = PortalContext.getConfiguration();
				String vreid = context.getCurrentScope(getThreadLocalRequest());
				_log.trace("News Feed in VRE, Reading feeds for VRE: " + vreid);
				ArrayList<Feed> OrganizationFeeds =  (ArrayList<Feed>) store.getRecentFeedsByVRE(vreid, (NewsConstants.FEEDS_MAX_PER_CATEGORY)); 
				for (Feed feed : OrganizationFeeds) {
					feedsMap.put(feed.getKey(), feed);							
				}
			}

			for (String key: feedsMap.keySet()) {
				toMerge.add(feedsMap.get(key));
			}				

			//sort the feeds in reverse chronological order
			Collections.sort(toMerge, Collections.reverseOrder());

			ArrayList<Feed> toReturn = new ArrayList<Feed>();
			//return only <MAX_FEEDS_NO> feeds
			if (toMerge.size() > MAX_FEEDS_NO) 
				for (int i = 0; i < MAX_FEEDS_NO; i++) 
					toReturn.add(toMerge.get(i));
			else {
				return enhanceFeeds(toMerge, 2);
			}
			return enhanceFeeds(toReturn, 2);
		} catch (PrivacyLevelTypeNotFoundException e) {
			_log.error("Privacy Level not Found " + e.getMessage());
			e.printStackTrace();
		} catch (FeedTypeNotFoundException e) {
			_log.error("Feed Type not Found " + e.getMessage());
			e.printStackTrace();
		} catch (ColumnNameNotFoundException e) {
			_log.error("Column name not Found " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * return the feeds having the hashtag passed as param the user has access on
	 * @param hashtag the hashtag to look for including '#'
	 */
	@Override
	public ArrayList<EnhancedFeed> getFeedsByHashtag(String hashtag)  {
		PortalContext pContext = PortalContext.getConfiguration();
		String userName = pContext.getCurrentUser(getThreadLocalRequest()).getUsername();
		String currentScope = pContext.getCurrentScope(getThreadLocalRequest());

		//in case the portal is restarted and you have the social home open it will get test.user (no callback to set session info)
		//this check just return nothing if that happens
		if (userName.compareTo(NewsConstants.TEST_USER) == 0) {
			_log.debug("Found " + userName + " returning nothing");
			return null;
		}

		try {

			ArrayList<Feed> toMerge = new ArrayList<Feed>();
			HashMap<String, Feed> feedsMap = new HashMap<String, Feed>();
			String lowerCaseHashtag = hashtag.toLowerCase();

			// the contexts of the user
			List<String> contexts = new ArrayList<String>();

			//this handles the case where the portlet is deployed outside of VREs (regular)
			if (isInfrastructureScope()) {

				GroupManager gm = new LiferayGroupManager();
				UserManager um = new LiferayUserManager();
				GCubeUser user = um.getUserByUsername(userName);
				Set<GCubeGroup> vresInPortal = gm.listGroupsByUserAndSite(user.getUserId(), getThreadLocalRequest().getServerName());
				_log.debug("Contexts in this site are per user " + vresInPortal);

				// get the scopes associated with such groups
				for (GCubeGroup gCubeGroup : vresInPortal) {
					contexts.add(gm.getInfrastructureScope(gCubeGroup.getGroupId()));
				}				
			}
			else {

				contexts.add(currentScope);

			}

			_log.debug("Contexts for hashtags is " + contexts);

			for (String context : contexts) {
				ArrayList<Feed> feeds =  (ArrayList<Feed>) store.getVREFeedsByHashtag(context, lowerCaseHashtag);
				for (Feed feed : feeds) {
					feedsMap.put(feed.getKey(), feed);							
				}
			}

			for (String key: feedsMap.keySet()) {
				toMerge.add(feedsMap.get(key));
			}				

			//sort the feeds in reverse chronological order
			Collections.sort(toMerge, Collections.reverseOrder());

			ArrayList<Feed> toReturn = new ArrayList<Feed>();
			//return only <MAX_FEEDS_NO> feeds
			if (toMerge.size() > MAX_FEEDS_NO) 
				for (int i = 0; i < MAX_FEEDS_NO; i++) 
					toReturn.add(toMerge.get(i));
			else {
				return enhanceFeeds(toMerge, 2);
			}
			return enhanceFeeds(toReturn, 2);
		} catch (PrivacyLevelTypeNotFoundException e) {
			_log.error("Privacy Level not Found ", e);
		} catch (FeedTypeNotFoundException e) {
			_log.error("Feed Type not Found ", e);
		} catch (ColumnNameNotFoundException e) {
			_log.error("Column name not Found ", e);
		} catch (Exception e) {
			_log.error("Error while retrieving feeds for hashtag ", e);
		}
		return null;
	}

	@Override
	public ArrayList<EnhancedFeed> getFeedsByQuery(String query, int from, int quantity) {

		// TODO : check this error better
		if(escl == null){

			_log.warn("There is no connection to elasticsearch, sorry.");
			return null;

		}

		PortalContext pContext = PortalContext.getConfiguration();
		String userName = pContext.getCurrentUser(getThreadLocalRequest()).getUsername();
		String currentScope = pContext.getCurrentScope(getThreadLocalRequest());

		try {

			//in case the portal is restarted and you have the social home open it will get test.user (no callback to set session info)
			//this check just return nothing if that happens
			if (userName.compareTo(NewsConstants.TEST_USER) == 0) {
				_log.debug("Found " + userName + " returning nothing");
				return null;
			}

			// Retrieve user's vres in which we must search
			Set<String> vres = new HashSet<String>();

			if (isInfrastructureScope()) {
				UserManager um = new LiferayUserManager();
				GCubeUser currUser = um.getUserByUsername(userName);
				//VRE Feeds
				GroupManager gm = new LiferayGroupManager();
				for (GCubeGroup group : gm.listGroupsByUser(currUser.getUserId())) {		
					if (gm.isVRE(group.getGroupId())) {
						String vreid = gm.getInfrastructureScope(group.getGroupId()); //get the scope		
						vres.add(vreid);
					}
				}				
			}
			//else must be in a VRE scope
			else {
				vres.add(currentScope);
			}			

			// query elastic search
			List<EnhancedFeed> enhancedFeeds = escl.search(query, vres, from, quantity);

			// retrieve the ids of liked feeds by the user
			List<String> likedFeeds = store.getAllLikedFeedIdsByUser(userName);

			// update fields "liked" and "isuser"
			for (EnhancedFeed enhancedFeed : enhancedFeeds) {

				if(isUsers(enhancedFeed.getFeed(), userName))
					enhancedFeed.setUsers(true);

				if(likedFeeds.contains(enhancedFeed.getFeed().getKey()))
					enhancedFeed.setLiked(true);

			}

			return (ArrayList<EnhancedFeed>) enhancedFeeds;

		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * return only the user connection feeds
	 */
	@Override
	public ArrayList<EnhancedFeed> getOnlyConnectionsUserFeeds() {
		ArrayList<Feed> toMerge = new ArrayList<Feed>();
		HashMap<String, Feed> feedsMap = new HashMap<String, Feed>();
		PortalContext pContext = PortalContext.getConfiguration();
		String userName = pContext.getCurrentUser(getThreadLocalRequest()).getUsername();

		try {
			//UserFriends Feeds
			ArrayList<String> userFriendsIds = (ArrayList<String>)store.getFriends(userName);
			for (String userid : userFriendsIds) {
				for (Feed feed : store.getRecentFeedsByUser(userid, NewsConstants.FEEDS_NO_PER_CATEGORY)) {
					feedsMap.put(feed.getKey(), feed);
				}
			}
			for (String key: feedsMap.keySet()) {
				toMerge.add(feedsMap.get(key));
			}
			Collections.sort(toMerge, Collections.reverseOrder());
			ArrayList<Feed> toReturn = new ArrayList<Feed>();
			//return only <MAX_FEEDS_NO> feeds
			if (toMerge.size() > MAX_FEEDS_NO) 
				for (int i = 0; i < MAX_FEEDS_NO; i++) 
					toReturn.add(toMerge.get(i));
			else
				return enhanceFeeds(toMerge, 2);		

		} catch (PrivacyLevelTypeNotFoundException e) {
			_log.error("Privacy Level not Found " + e.getMessage());
			e.printStackTrace();
		} catch (FeedTypeNotFoundException e) {
			_log.error("Feed Type not Found " + e.getMessage());
			e.printStackTrace();
		} catch (ColumnNameNotFoundException e) {
			_log.error("Column name not Found " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * return only one feed with all the comments
	 */
	@Override
	public EnhancedFeed getSingleFeed(String feedKey) {
		Feed feed = null;
		try {
			if (feedKey != null) {
				feed = store.readFeed(feedKey);
				if (feed != null) {
					ArrayList<Feed> toEnhance = new ArrayList<Feed>();
					toEnhance.add(feed);
					return enhanceFeeds(toEnhance, -1).get(0); //-1 all the comments
				}
			}
		} catch (Exception e) {
			_log.debug("Error while trying to fetch feed with key " + feedKey + " returning nothing");
			return new EnhancedFeed();
		}
		return new EnhancedFeed();
	}
	/**
	 * MoreFeedsBean contains the timeline index of the last returned valid feed (because if you delete a feed is stays on in the timeline and is marked deleted)
	 * and contains the Feeds 
	 * @param strat the range start (most recent feeds for this vre) has to be greater than 0
	 * @param quantity the number of most recent feeds for this vre starting from "start" param
	 */
	@Override
	public MoreFeedsBean getMoreFeeds(int start, int quantity) {
		ArrayList<Feed> toMerge = new ArrayList<Feed>();
		HashMap<String, Feed> feedsMap = new HashMap<String, Feed>();
		PortalContext pContext = PortalContext.getConfiguration();
		String vreid = pContext.getCurrentScope(getThreadLocalRequest());
		_log.debug("\n\nAsking more feed for Timeline " + vreid + " from " + start + " get other " + quantity);
		ArrayList<Feed> organizationFeeds;
		RangeFeeds rangeFeeds = null;
		try {
			rangeFeeds = store.getRecentFeedsByVREAndRange(vreid, start, quantity);
			organizationFeeds = rangeFeeds.getFeeds();
			for (Feed feed : organizationFeeds) {
				feedsMap.put(feed.getKey(), feed);		
				//System.out.println("->\n"+feed.getDescription());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		} 

		for (String key: feedsMap.keySet()) {
			toMerge.add(feedsMap.get(key));
		}
		//sort the feeds in reverse chronological order
		Collections.sort(toMerge, Collections.reverseOrder());
		ArrayList<EnhancedFeed> toReturn = enhanceFeeds(toMerge, 2);
		return new MoreFeedsBean(rangeFeeds.getLastReturnedFeedTimelineIndex(), toReturn);
	}

	@Override
	public ArrayList<EnhancedFeed> getOnlyMyUserFeeds() {
		PortalContext pContext = PortalContext.getConfiguration();
		String userName = pContext.getCurrentUser(getThreadLocalRequest()).getUsername();
		_log.trace("getOnly UserFeeds for " + userName);
		ArrayList<Feed> userFeeds = null;
		try {
			userFeeds = (ArrayList<Feed>) store.getRecentFeedsByUser(userName, 15);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		Collections.sort(userFeeds, Collections.reverseOrder());
		return enhanceFeeds(userFeeds, 2);
	}

	@Override
	public ArrayList<EnhancedFeed> getOnlyLikedFeeds() {
		PortalContext pContext = PortalContext.getConfiguration();
		String userName = pContext.getCurrentUser(getThreadLocalRequest()).getUsername();
		_log.trace("getLiked Feeds for " + userName);
		ArrayList<Feed> userFeeds = null;
		try {
			userFeeds = (ArrayList<Feed>) store.getAllLikedFeedsByUser(userName, 25);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		Collections.sort(userFeeds, Collections.reverseOrder());
		return enhanceFeeds(userFeeds, 2);
	}

	@Override
	public boolean like(String feedid, String feedText, String feedOwnerId) {
		boolean likeCommitResult = false;
		UserInfo user = getUserSettings().getUserInfo();
		String username = user.getUsername();
		if (username.compareTo(NewsConstants.TEST_USER) == 0) {
			return false;
		}

		Like toLike = new Like(UUID.randomUUID().toString(), user.getUsername(), 
				new Date(), feedid, user.getFullName(), user.getAvatarId());
		try {
			likeCommitResult = store.like(toLike);
		} catch (FeedIDNotFoundException e) {
			_log.error("Feed not Found for this like " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		//if the like was correctly delivered notify users involved
		if (likeCommitResult) {
			PortalContext pContext = PortalContext.getConfiguration();
			String currScope = pContext.getCurrentScope(getThreadLocalRequest());
			//if the user who liked this post is not the user who posted it notify the poster user (Feed owner) 
			if (! user.getUsername().equals(feedOwnerId)) {
				NotificationsManager nm = new ApplicationNotificationsManager(
						new SocialNetworkingSite(getThreadLocalRequest()), 
						currScope, 
						new SocialNetworkingUser(username, user.getEmailaddress(), user.getFullName(), user.getAvatarId()),
						APP_ID);
				boolean nResult = nm.notifyLikedFeed(feedOwnerId, feedid, Utils.escapeHtml(feedText));
				_log.trace("Like Notification added? " + nResult);
			}
		}
		return likeCommitResult;
	}

	@Override
	public boolean unlike(String feedid, String feedText, String feedOwnerId) {		
		UserInfo user = getUserSettings().getUserInfo();		
		try {
			for (Like like : store.getAllLikesByFeed(feedid)) {
				if (like.getUserid().compareTo(user.getUsername()) == 0) {
					_log.trace("Trying unlike of " + feedText + " for " + user.getFullName());
					store.unlike(user.getUsername(), like.getKey(), feedid);
					return true;
				}
			}	
		} catch (FeedIDNotFoundException | PrivacyLevelTypeNotFoundException | FeedTypeNotFoundException | ColumnNameNotFoundException | LikeIDNotFoundException e) {
			_log.error("Either Feed or Like not Found " + e.getMessage());
			e.printStackTrace();
			return false;
		}		
		return false;
	}
	/**
	 * @param feedid the id of the commented feed
	 * @param commentText the comment text
	 * @param feedOwnerId the username of the user who created the post that was commented
	 */
	@Override
	public OperationResult comment(String feedid, String commentText, HashSet<String> mentionedUserFullNames, String feedOwnerId, boolean isAppFeed) {
		boolean commentCommitResult = false;
		_log.trace("Trying to add this comment " + commentText);
		UserInfo user = getUserSettings().getUserInfo();

		if (user.getUsername().compareTo(NewsConstants.TEST_USER) == 0) {
			return new OperationResult(false, "Session Expired", null);
		}

		String escapedCommentText = Utils.escapeHtmlAndTransformUrl(commentText);

		//copy the set into a list
		ArrayList<String> mentionedUserFullNamesList = new ArrayList<String>();
		mentionedUserFullNamesList.addAll(mentionedUserFullNames);

		ArrayList<ItemBean> mentionedUsers = null; 
		if (mentionedUserFullNames != null && ! mentionedUserFullNames.isEmpty()) {
			mentionedUsers = getSelectedUserIds(mentionedUserFullNamesList);
			escapedCommentText = Utils.convertMentionPeopleAnchorHTML(escapedCommentText, mentionedUsers, getThreadLocalRequest());
		}		

		Comment comment = new Comment(UUID.randomUUID().toString(), user.getUsername(), 
				new Date(), feedid, escapedCommentText, user.getFullName(), user.getAvatarId());
		try {
			if (store.addComment(comment)) 
				commentCommitResult = true;
		} catch (FeedIDNotFoundException e) {
			_log.error("Related post not found for this comment " + e.getMessage());
			e.printStackTrace();
			return new OperationResult(false, "Related post not found for this comment", comment);
		}
		//if the comment was correctly delivered && is not an app feed notify users involved
		if (commentCommitResult && isWithinPortal()) {
			PortalContext pContext = PortalContext.getConfiguration();
			String currScope = pContext.getCurrentScope(getThreadLocalRequest());
			//if the user who commented this post is not the user who posted it notify the poster user (Feed owner) 
			NotificationsManager nm = new ApplicationNotificationsManager(
					new SocialNetworkingSite(getThreadLocalRequest()), 
					currScope, 
					new SocialNetworkingUser(user.getUsername(), user.getEmailaddress(), user.getFullName(), user.getAvatarId()),
					APP_ID);
			if (! user.getUsername().equals(feedOwnerId) && (!isAppFeed)) {				
				boolean result = nm.notifyOwnCommentReply(feedOwnerId, feedid, escapedCommentText, comment.getKey());
				_log.trace("Comment Notification to post owner added? " + result);
			} 

			//if there are users who liked this post they get notified, asynchronously with this thread
			ArrayList<Like> favorites = getAllLikesByFeed(feedid);
			Thread likesThread = new Thread(new LikeNotificationsThread(commentText, nm, favorites, feedOwnerId, comment.getKey()));
			likesThread.start();

			//notify the other users who commented this post (excluding the ones above)
			Thread commentsNotificationthread = new Thread(new CommentNotificationsThread(store, new LiferayUserManager(), user.getUsername(), comment.getFeedid(), escapedCommentText, nm, feedOwnerId, comment.getKey(), favorites));
			commentsNotificationthread.start();	

			//send the notification to the mentioned users, if any
			if (mentionedUsers != null && mentionedUsers.size() > 0) {
				ArrayList<GenericItemBean> toPass = new ArrayList<GenericItemBean>();


				// among the mentionedUsers there could be groups of people
				Map<String, ItemBean> uniqueUsersToNotify = new HashMap<>();
				UserManager um = new LiferayUserManager();

				for (ItemBean bean : mentionedUsers) {

					if(bean.isItemGroup()){

						// retrieve the users of this group
						try {
							List<GCubeUser> teamUsers = um.listUsersByTeam(Long.parseLong(bean.getId()));

							for (GCubeUser userTeam : teamUsers) {
								if(!uniqueUsersToNotify.containsKey(userTeam.getUsername()))
									uniqueUsersToNotify.put(userTeam.getUsername(), new ItemBean(userTeam.getUserId()+"",
											userTeam.getUsername(), userTeam.getFullname(), userTeam.getUserAvatarURL()));
							}

						} catch (NumberFormatException
								| UserManagementSystemException
								| TeamRetrievalFault | UserRetrievalFault e) {
							_log.error("Unable to retrieve team information", e);
						}

					}else{
						// it is a user, just add to the hashmap
						if(!uniqueUsersToNotify.containsKey(bean.getName()))
							uniqueUsersToNotify.put(bean.getName(), bean);

					}
				}

				// iterate over the hashmap
				Iterator<Entry<String, ItemBean>> userMapIterator = uniqueUsersToNotify.entrySet().iterator();
				while (userMapIterator.hasNext()) {
					Map.Entry<String, ItemBean> userEntry = (Map.Entry<String, ItemBean>) userMapIterator
							.next();
					ItemBean userBean = userEntry.getValue();
					toPass.add(new GenericItemBean(userBean.getId(), userBean.getName(), userBean.getAlternativeName(), userBean.getThumbnailURL()));
				}

				Thread thread = new Thread(new MentionNotificationsThread(comment.getFeedid(), commentText, nm, null, toPass));
				thread.start();
			}
		}
		return new OperationResult(true, "OK", comment);
	}

	@Override
	public OperationResult editComment(Comment toEdit) {
		UserInfo user = getUserSettings().getUserInfo();
		if (user.getUsername().compareTo(NewsConstants.TEST_USER) == 0) {
			return new OperationResult(false, "Session Expired", null);
		}		

		String escapedCommentText = Utils.escapeHtmlAndTransformUrl(toEdit.getText());

		Comment edited = new Comment(toEdit.getKey(), toEdit.getUserid(), 
				toEdit.getTime(), toEdit.getFeedid(), escapedCommentText, user.getFullName(), user.getAvatarId(), true, new Date());
		try {
			store.editComment(edited);
		} catch (Exception e) {
			e.printStackTrace();
			return new OperationResult(false, "Exception on the server: " + e.getMessage(), null);
		} 
		return new OperationResult(true, "OK", edited);
	}	

	/**
	 * this method sorts the Feeds in Chronological Reversed order and adds additional informations such as comments and attachments
	 * @param toEnhance
	 * @param } catch (Exception e) { the max number of comments you want to get back, -1 to get all
	 *
	 * @return
	 */
	private ArrayList<EnhancedFeed> enhanceFeeds(ArrayList<Feed> toEnhance, int commentsNumberPerFeed) {
		ArrayList<EnhancedFeed> toReturn = new ArrayList<EnhancedFeed>();
		PortalContext pContext = PortalContext.getConfiguration();
		String userName = pContext.getCurrentUser(getThreadLocalRequest()).getUsername();


		//patch needed for maintaining mention link backward compatibility (they point to /group/data-e-infrastructure-gateway/profile)
		final String LINK_TO_REPLACE = "data-e-infrastructure-gateway";
		String siteLandinPagePath = PortalContext.getConfiguration().getSiteLandingPagePath(getThreadLocalRequest());
		String tokenTosubstitute = siteLandinPagePath.replace("/group/", "");

		ArrayList<String> likedFeeds = (ArrayList<String>) store.getAllLikedFeedIdsByUser(userName);
		boolean skip = false;
		for (Feed feed : toEnhance) {
			//patch needed for maintaining mention link backward compatibility (they point to /group/data-e-infrastructure-gateway/profile)
			String currPostText = feed.getDescription().replace(LINK_TO_REPLACE,tokenTosubstitute);
			feed.setDescription(currPostText);
			boolean isMultiFileUpload = feed.isMultiFileUpload();
			ArrayList<Attachment> attachments = new ArrayList<Attachment>();
			if (isMultiFileUpload) {
				try {
					attachments = (ArrayList<Attachment>) store.getAttachmentsByFeedId(feed.getKey());
				} catch (FeedIDNotFoundException e) {
					_log.error("It looks like sth wrong with this feedid having attachments, could not find feedId = " + feed.getKey() + "\n" + e.getMessage());
				}
			}			

			skip = false;
			if (! feed.isApplicationFeed()) {
				String thumb = getUserImagePortraitUrlLocal(feed.getEntityId());
				if (thumb == null) {
					_log.warn(feed.getEntityId() + " is not avaialble on this portal, skipping this feed: " + feed.getKey());
					skip = true;
				} else
					feed.setThumbnailURL(thumb);
			}
			//if likedFeeds contains this feed key it means the user already Liked it
			boolean liked = likedFeeds.contains(feed.getKey());
			int commentsNo = 0;
			try {
				commentsNo = Integer.parseInt(feed.getCommentsNo());
			}
			catch (NumberFormatException e) {
				commentsNo = 0;
				_log.error("NumberFormatException while reading comments number " + e.getMessage());
			}
			if (!skip) {
				if (commentsNo == 0) {
					EnhancedFeed toAdd = null;
					//create the enhanced feed
					if (feed.isApplicationFeed()) {
						toAdd = new EnhancedFeed(feed, liked, checkisAdminUser()); 
					} else
						toAdd = new EnhancedFeed(feed, liked, isUsers(feed, userName)); 
					toAdd.setAttachments(attachments);
					toReturn.add(toAdd);
				} else {
					ArrayList<Comment> comments = getAllCommentsByFeed(feed.getKey());
					//sort in chronological order
					Collections.sort(comments);

					int currCommentsNumber = comments.size();
					//if comments are less than $commentsNumberPerFeed they are the more recent, -1 return all the comments
					if (currCommentsNumber < commentsNumberPerFeed || commentsNumberPerFeed == -1) {
						EnhancedFeed toAdd = new EnhancedFeed(feed, liked, isUsers(feed, userName), comments, attachments); 
						toReturn.add(toAdd);
					} else {
						//need to get the last two
						ArrayList<Comment> comments2Attach = new ArrayList<Comment>();					
						for (int i = currCommentsNumber -commentsNumberPerFeed; i < currCommentsNumber; i++) {
							comments2Attach.add(comments.get(i));
						}
						EnhancedFeed toAdd = new EnhancedFeed(feed, liked, isUsers(feed, userName), comments2Attach, attachments); 
						toReturn.add(toAdd);
					}
				}					
			}

		}
		_log.trace("ENHANCED FEEDS TOTAL= " + toReturn.size() + " for user: " + userName);
		return toReturn;
	}

	/**
	 * this method is needed because user images portrait change id depending on the portal instance
	 * e.g. a post made from iMarine portal would not show the avatarIMage in D4Science.org
	 * @param screenname
	 * @return the url of the image portrait for this portal instance
	 */
	private String getUserImagePortraitUrlLocal(String screenName) {
		if (! isWithinPortal()) {
			return "";
		}
		String thumbnailURL = "";
		try {
			thumbnailURL = new LiferayUserManager().getUserByUsername(screenName).getUserAvatarURL();
		} catch (UserManagementSystemException | UserRetrievalFault e) {
			e.printStackTrace();
		}
		return thumbnailURL;
	}

	@Override
	public ArrayList<Like> getAllLikesByFeed(String feedid) {
		ArrayList<Like> toReturn = (ArrayList<Like>) store.getAllLikesByFeed(feedid);
		_log.trace("Asking likes for " + feedid);
		for (Like like : toReturn) {
			String thumb = getUserImagePortraitUrlLocal(like.getUserid());

			like.setThumbnailURL(thumb == null ? "" : thumb);
		}
		return toReturn;
	}

	@Override
	public ArrayList<Comment> getAllCommentsByFeed(String feedid) {
		_log.trace("Asking comments for " + feedid);
		ArrayList<Comment> toReturn =  (ArrayList<Comment>) store.getAllCommentByFeed(feedid);
		for (Comment comment : toReturn) {
			String thumb = getUserImagePortraitUrlLocal(comment.getUserid());
			comment.setThumbnailURL(thumb == null ? "" : thumb);
		}
		Collections.sort(toReturn);
		return toReturn;
	}
	@Override
	public boolean deleteComment(String commentid, String feedid) {
		_log.trace("Attempting to delete comment " + commentid);
		try {
			return store.deleteComment(commentid, feedid);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} 
	}
	@Override
	public boolean deleteFeed(String feedid) {
		_log.trace("Called delete feed " + feedid);
		try {
			Feed toDelete = store.readFeed(feedid);
			List<String> hashtags = Utils.getHashTags(toDelete.getDescription());
			if (hashtags != null && !hashtags.isEmpty()) {
				_log.trace("The feed has hashtags, attempting to delete them ... " +  hashtags.toString());
				boolean deletedHashtag = store.deleteHashTags(feedid, toDelete.getVreid(), hashtags);
				_log.trace("deletedHashtag? " +  deletedHashtag);
			}
			_log.trace("Attempting to delete feed " + feedid);
			return store.deleteFeed(feedid);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} 
	}
	@Override
	public ArrayList<ItemBean> getOrganizationUsers() {
		ArrayList<ItemBean> portalBeans = new ArrayList<ItemBean>();
		PortalContext pContext = PortalContext.getConfiguration();
		GCubeUser currentUser = pContext.getCurrentUser(getThreadLocalRequest());
		String userName = currentUser.getUsername();
		UserManager um = new LiferayUserManager();
		GroupManager gm = new LiferayGroupManager();
		long currentGroupId = pContext.getCurrentGroupId(getThreadLocalRequest());
		Set<GCubeUser> users = new HashSet<>();
		List<GCubeTeam> teams = new ArrayList<>(); //TODO: teams should be added also in root Mode
		try {
			if (gm.isRootVO(currentGroupId)) {
				Set<GCubeGroup> userGroups = gm.listGroupsByUserAndSite(currentUser.getUserId(), getThreadLocalRequest().getServerName());
				for (GCubeGroup userGroup : userGroups) {
					if (gm.isVRE(userGroup.getGroupId())) {
						users.addAll(um.listUsersByGroup(userGroup.getGroupId()));
						_log.debug("getOrganizationUsers added users of group " + userGroup.getGroupId());
					}
				}
			} else { //is a VRE
				users.addAll(um.listUsersByGroup(currentGroupId));
				teams = new LiferayRoleManager().listTeamsByGroup(currentGroupId);
			}
		} catch (UserManagementSystemException | GroupRetrievalFault | UserRetrievalFault | VirtualGroupNotExistingException e) {
			e.printStackTrace();
		}
		for (GCubeUser user : users) {
			if (user.getUsername().compareTo("test.user") != 0 && user.getUsername().compareTo(userName) != 0)  { //skip test.user & current user
				portalBeans.add(new ItemBean(user.getUserId()+"", user.getUsername(), user.getFullname(), user.getUserAvatarURL()));
			}
		}
		for (GCubeTeam gCubeTeam : teams) {
			portalBeans.add(new ItemBean(gCubeTeam.getTeamId()+"", gCubeTeam.getTeamName()));
		}
		_log.trace("Returning " + portalBeans.size() + " users for scope groupid =  " + currentGroupId);
		return portalBeans;
	}

	/**
	 * tell if a feed is belonging to the current user or not
	 * @param tocheck
	 * @param username
	 * @return true if this feed is of the current user
	 */
	private boolean isUsers(Feed tocheck, String username) {
		return (tocheck.getEntityId().equals(username));
	}

	/**
	 * 
	 * @return true if the user is a portal administrator or not
	 */
	private boolean checkisAdminUser() {
		if (getThreadLocalRequest().getSession().getAttribute(SESSION_ADMIN_ATTR) == null) {
			boolean isAdmin = false;
			try {
				isAdmin = isAdmin();
			} catch (Exception e) {
				e.printStackTrace();
			} 
			getThreadLocalRequest().getSession().setAttribute(SESSION_ADMIN_ATTR, isAdmin);
			return isAdmin;
		}
		return (Boolean) getThreadLocalRequest().getSession().getAttribute(SESSION_ADMIN_ATTR);
	}
	/**
	 * 
	 * @return the screennames of the addressee (user logins e.g. pino.pini)
	 */
	public ArrayList<ItemBean> getSelectedUserIds(ArrayList<String> fullNames) {
		if (fullNames == null) 
			return new ArrayList<ItemBean>();
		else {
			PortalContext pContext = PortalContext.getConfiguration();
			String userName = pContext.getCurrentUser(getThreadLocalRequest()).getUsername();
			ArrayList<ItemBean> allUsers = Utils.getDisplayableItemBeans("/"+PortalContext.getConfiguration().getInfrastructureName(), userName, isWithinPortal());
			ArrayList<ItemBean> toReturn = new ArrayList<ItemBean>();
			for (String fullName : fullNames) 
				for (ItemBean puser : allUsers) {					
					if (puser.getAlternativeName().compareTo(fullName) == 0) {
						toReturn.add(puser);
						break;
					}
				}
			return toReturn;
		}
	}
	/**
	 * tell if the user is a portal administrator or not
	 * @param username
	 * @return true if is admin
	 * @throws SystemException 
	 * @throws PortalException 
	 */
	private boolean isAdmin() throws PortalException, SystemException  {
		if (! isWithinPortal())
			return false;
		try {
			PortalContext pContext = PortalContext.getConfiguration();
			GCubeUser curUser =  pContext.getCurrentUser(getThreadLocalRequest());
			return new LiferayRoleManager().isAdmin(curUser.getUserId());
		}
		catch (Exception e) {
			_log.error("Could not check if the user is an Administrator, returning false");
			return false;
		}
	}

	/**
	 * read from the property file in /conf the refreshing time and other configurations needed
	 * @return CustomConfiguration
	 */
	private CustomConfiguration getUserConfiguration() {
		CustomConfiguration toReturn = null;
		_log.info("Trying to read custom config fr News Feed (REFRESH_TIME, VRE Label and show timeline source)");
		Properties props = new Properties();
		int minutes = 0;
		String label = "";
		boolean showTimelineSource = true;

		String propertyfile = "";
		try {
			ServletContext servletContext = getServletContext();
			String contextPath = servletContext.getRealPath(File.separator);
			propertyfile = contextPath + "conf" +  File.separator + "settings.properties";			
			File propsFile = new File(propertyfile);
			FileInputStream fis = new FileInputStream(propsFile);
			props.load( fis);
			try {
				minutes = Integer.parseInt(props.getProperty(REFRESH_TIME));
				minutes = minutes*60*1000;
			}
			//catch exception in case the property value isNot a Number
			catch (ClassCastException ex) {
				minutes = 300000; //5 minutes
				_log.error(REFRESH_TIME + " must be a number (in minutes) returning 5 minutes");
			}
			//the vre label
			label = props.getProperty(VRE_LABEL);
			try {
				showTimelineSource = Boolean.parseBoolean(props.getProperty(SHOW_TIMELINE_SOURCE));

			}
			//catch exception in case the property value isNot true or false
			catch (ClassCastException ex) {
				showTimelineSource = true;
				_log.error(showTimelineSource + " must be true or false, returning true");
			}

		}
		//catch exception in case properties file does not exist
		catch(IOException e) {
			minutes = 300000; //5 minutes
			_log.error("settings.properties file not found under " + propertyfile +", returning 5 minutes");
		}
		toReturn = new CustomConfiguration(minutes, label, showTimelineSource);
		_log.debug("Read Configuration from property file: " + toReturn);
		return toReturn;

	}

	@Override
	public ArrayList<EnhancedFeed> getFeedsRelatedToUserStatistics(
			ShowUserStatisticAction action, int from, int quantity) {

		PortalContext pContext = PortalContext.getConfiguration();
		String userid = pContext.getCurrentUser(getThreadLocalRequest()).getUsername();
		String scope = pContext.getCurrentScope(getThreadLocalRequest());

		if(userid == NewsConstants.TEST_USER)
			return null;

		try{

			// get reference time
			Calendar oneYearAgo = Calendar.getInstance();
			oneYearAgo.set(Calendar.YEAR, oneYearAgo.get(Calendar.YEAR) - 1);

			ArrayList<Feed> feeds = null;

			switch(action){
			case POSTS_MADE_BY_USER:
				feeds = (ArrayList<Feed>) store.getRecentFeedsByUserAndDate(userid, oneYearAgo.getTimeInMillis());
				break;
			case LIKES_MADE_BY_USER:
				feeds = (ArrayList<Feed>) store.getRecentLikedFeedsByUserAndDate(userid, oneYearAgo.getTimeInMillis());
				break;
			case COMMENTS_MADE_BY_USER:
				feeds = (ArrayList<Feed>) store.getRecentCommentedFeedsByUserAndDate(userid, oneYearAgo.getTimeInMillis());
				break;
			case LIKES_GOT_BY_USER:
				feeds = (ArrayList<Feed>) store.getRecentFeedsByUserAndDate(userid, oneYearAgo.getTimeInMillis());
				Iterator<Feed> feedsIteratorLikes = feeds.iterator();
				while (feedsIteratorLikes.hasNext()) {
					Feed feed = (Feed) feedsIteratorLikes.next();
					if(Integer.parseInt(feed.getLikesNo()) == 0)
						feedsIteratorLikes.remove();
				}
				break;
			case COMMENTS_GOT_BY_USER:
				feeds = (ArrayList<Feed>) store.getRecentFeedsByUserAndDate(userid, oneYearAgo.getTimeInMillis());
				Iterator<Feed> feedsIteratorComments = feeds.iterator();
				while (feedsIteratorComments.hasNext()) {
					Feed feed = (Feed) feedsIteratorComments.next();
					if(Integer.parseInt(feed.getCommentsNo()) == 0)
						feedsIteratorComments.remove();
				}
				break;
			default : return new ArrayList<EnhancedFeed>();
			}


			List<String> contexts = new ArrayList<String>();
			if(isInfrastructureScope()){

				LiferayGroupManager groupManager = new LiferayGroupManager();
				long userIdLong = new LiferayUserManager().getUserId(userid);

				// filter for site
				Set<GCubeGroup> vresInPortal = groupManager.listGroupsByUserAndSite(userIdLong, getThreadLocalRequest().getServerName());
				_log.debug("Contexts in this site are " + vresInPortal);

				// get the scopes associated with such groups
				for (GCubeGroup gCubeGroup : vresInPortal) {
					contexts.add(groupManager.getInfrastructureScope(gCubeGroup.getGroupId()));
				}

			}else{

				// just the current scope
				contexts.add(scope);

			}

			// filter
			Iterator<Feed> iteratorScope = feeds.iterator();
			while (iteratorScope.hasNext()) {
				Feed feed = (Feed) iteratorScope.next();
				if(!contexts.contains(feed.getVreid()))
					iteratorScope.remove();
			}

			// sort the list, retrieve elements in the range and enhance the feeds
			Collections.sort(feeds, Collections.reverseOrder());
			int upperIndex = (from + quantity) >= feeds.size() ? feeds.size() : from + quantity;
			feeds = new ArrayList<Feed>(feeds.subList(from, upperIndex));
			return enhanceFeeds(feeds, -1);

		}catch(Exception e){
			_log.error("Error while retrieving feeds for user " + userid + " and action " + action.toString(), e);
		}

		return null;
	}
}
