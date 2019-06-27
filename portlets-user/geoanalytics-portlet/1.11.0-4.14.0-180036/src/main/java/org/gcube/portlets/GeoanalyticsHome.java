package org.gcube.portlets;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletRequest;

import org.gcube.common.portal.PortalContext;
import org.gcube.vomanagement.usermanagement.UserManager;
import org.gcube.vomanagement.usermanagement.exception.UserManagementSystemException;
import org.gcube.vomanagement.usermanagement.exception.UserRetrievalFault;
import org.gcube.vomanagement.usermanagement.impl.LiferayUserManager;
import org.gcube.vomanagement.usermanagement.model.GCubeUser;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;

import gr.cite.bluebridge.endpoint.EndpointManager;
import gr.cite.bluebridge.endpoint.ServiceProfile;
import gr.cite.bluebridge.endpoint.exceptions.ServiceDiscoveryException;

/**
 * Portlet implementation class Home
 */
public class GeoanalyticsHome extends GenericPortlet {
	protected String staticEndpoint;
	protected String viewTemplate;
	protected String backendURL;
	protected String user;
	protected String pass;
	
	private ServiceProfile geoanalyticsProfile;
	private EndpointManager endpointManager;
	
	private static Log logger = LogFactoryUtil.getLog(GeoanalyticsHome.class);

	@Override
	public void init() {
		viewTemplate = getInitParameter("views");
		staticEndpoint = getInitParameter("back-end-url");

		geoanalyticsProfile = new ServiceProfile();
		geoanalyticsProfile.setServiceClass("geoanalytics");
		geoanalyticsProfile.setServiceName("geoanalytics-main-service");
		geoanalyticsProfile.setPathEndsWith("/");

		endpointManager = new EndpointManager();

		Authenticator.setDefault(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(user, pass.toCharArray());
			}
		});
	}

	@Override
	public void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {	
		include(viewTemplate, renderRequest, renderResponse);
	}
	
	@Override
	public void processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException, IOException {
		super.processAction(actionRequest, actionResponse);
	}
	
	@Override
	public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws PortletException, IOException {
		logger.debug("User is attempting a conncetion to the backend service");
		
		PortalContext pContext = PortalContext.getConfiguration();
		HttpServletRequest httpServletRequest = PortalUtil.getHttpServletRequest(resourceRequest);
		String scope = pContext.getCurrentScope(httpServletRequest);

		Integer status = null;

		try {
			List<String> endpoints = endpointManager.getServiceEndpoints(scope, geoanalyticsProfile);

			for (String endpoint : endpoints) {
				try {
					status = doRequest(endpoint, resourceRequest, resourceResponse);
				} catch (Exception e) {
					endpointManager.removeServiceEndpoint(scope, geoanalyticsProfile, endpoint);
					logger.warn("Cannot reach endpoint : " + status, e);
				}

				if (status != null && status == 200) {
					break;
				}
			}			
		} catch (ServiceDiscoveryException e) {
			try {
				status = doRequest(staticEndpoint, resourceRequest, resourceResponse);
			} catch (Exception ex) {
				resourceResponse.getWriter().write("Service is currently unavailable");
				resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE, "500");
			}
		}
	}
	
	public Integer doRequest(String endpoint, ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws IOException {
		logger.debug("User is connecting to " + endpoint + " service");
		
		PortalContext pContext = PortalContext.getConfiguration();
		HttpServletRequest httpServletRequest = PortalUtil.getHttpServletRequest(resourceRequest);
		String tenant = pContext.getCurrentScope(httpServletRequest); 		
		String theUserName = pContext.getCurrentUser(httpServletRequest).getUsername();
		String token = pContext.getCurrentUserToken(tenant, theUserName);	
		
		UserManager um = new LiferayUserManager();
		GCubeUser user = null;
		try {
			user = um.getUserByUsername(theUserName);
		} catch (UserManagementSystemException e) {
			e.printStackTrace();
		} catch (UserRetrievalFault e) {
			e.printStackTrace();
		}
		String usernameForController = "";
		String email = "";
		String initials = "";
		email =user.getEmail();
		usernameForController = user.getFullname();
		String username = "";
		String userUUID="";
		
		username = usernameForController;
		
		long userid = Long.parseLong(resourceRequest.getRemoteUser());
		try {
			User user2 = UserLocalServiceUtil.getUser(userid);
			initials = calculateInitials(theUserName);
			userUUID = user2.getUserUuid();
		} catch (PortalException e1) {
			e1.printStackTrace();
		} catch (SystemException e1) {
			e1.printStackTrace();
		}
		
		boolean fetchTenantAndUsername = ParamUtil.getBoolean(resourceRequest, "fetchTenantAndUsername");
		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
		if(fetchTenantAndUsername){
			jsonObject = retrieveTenantAndUsername(resourceRequest, tenant, usernameForController, email);
		}
		
		Integer status = null;
		
		if(resourceRequest.getResourceID() != null){
			InputStream is = resourceRequest.getPortletInputStream();
			byte[] postData = ByteStreams.toByteArray(is);
			is.close();
			int postDataLength = postData.length;
			
			String resourceUrl = buildResourceUrl(endpoint, resourceRequest);

			URL url = new URL(resourceUrl);
			logger.debug("User is connecting to the path: " + resourceRequest.getResourceID());
			HttpURLConnection cox = (HttpURLConnection) url.openConnection();

			cox.setRequestProperty("gcube-token", token);
			cox.setRequestProperty("username", username);
			cox.setRequestProperty("useruuid", userUUID);
			cox.setRequestProperty("email", email);
			cox.setRequestProperty("initials", initials);
			cox.setRequestProperty("tenant", tenant);
			cox.setDoOutput(true);
			cox.setDoInput(true);
			cox.setInstanceFollowRedirects(false);
			cox.setRequestMethod(resourceRequest.getMethod());
			if (resourceRequest.getContentType() != null)
				cox.setRequestProperty("Content-Type", resourceRequest.getContentType());
			cox.setRequestProperty("charset", resourceRequest.getCharacterEncoding());
			cox.setUseCaches(false);
			if (postDataLength > 0) {
				cox.setRequestProperty("Content-Length", Integer.toString(postDataLength));
				DataOutputStream wr = new DataOutputStream(cox.getOutputStream());
				wr.write(postData);
			}

			try {
				cox.connect();
				status = cox.getResponseCode();

				resourceResponse.setProperty(ResourceResponse.HTTP_STATUS_CODE, Integer.toString(status));

				if (cox.getResponseCode() != 200) {
					String response = CharStreams.toString(new InputStreamReader(cox.getErrorStream(), Charsets.UTF_8));
					resourceResponse.getWriter().write(response);
				} else {
					resourceResponse.setCharacterEncoding(cox.getContentEncoding());
					resourceResponse.setContentLength(cox.getContentLength());
					if (cox.getContentType() != null) {
						resourceResponse.setContentType(cox.getContentType());
					}
					if (cox.getHeaderField("Content-Disposition") != null) {
						resourceResponse.setProperty("Content-Disposition", cox.getHeaderField("Content-Disposition"));
					}

					ByteStreams.copy(cox.getInputStream(), resourceResponse.getPortletOutputStream());
				}
			} catch (Exception e) {
				logger.error(null, e);
				logger.error("An error occured for endpoint: " + endpoint);
			}
		}
		
		if(fetchTenantAndUsername) {
			resourceResponse.getWriter().println(jsonObject);
		}
		
		return status;		
	}

	
	@Override
	protected void doDispatch(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		super.doDispatch(request, response);
	}

	protected void include(String path, RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {
		String url = null;
		if (renderRequest.getParameter("jspPage") == null || renderRequest.getParameter("jspPage").equals("./"))
			url = path + "view.jsp";
		else
			url = path + renderRequest.getParameter("jspPage");

		PortletRequestDispatcher portletRequestDispatcher = getPortletContext().getRequestDispatcher(url);

		if (portletRequestDispatcher == null) {
			logger.error(url + " is not a valid include");
		} else {

			portletRequestDispatcher.include(renderRequest, renderResponse);
		}
	}
	
	public String calculateInitials(String fullname) {
		
		logger.debug("Finding user\'s initials for user: " + fullname);
		
		StringBuilder stringBuilder = new StringBuilder();
		String[] splittedString = fullname.trim().split("\\s+");
		
		for(int i=0;i<splittedString.length;i++){
			if(splittedString[i].length() > 0)
				stringBuilder.append(splittedString[i].charAt(0));
		}
		
		return stringBuilder.toString();
	}
	
	protected String buildResourceUrl(String endpoint, ResourceRequest resourceRequest) {
		StringBuilder resourceUrl = new StringBuilder(endpoint + resourceRequest.getResourceID());
		
		if(resourceRequest.getMethod().toUpperCase().equals("GET")){		
			addQueryParameters(resourceUrl, resourceRequest);
		}

		return resourceUrl.toString();
	}
	
	protected void addQueryParameters(StringBuilder resourceUrl, ResourceRequest resourceRequest){
		if(!resourceUrl.toString().contains("?")){
			resourceUrl.append("?");
		}		
	
		resourceRequest.getParameterMap().entrySet().stream().forEach(entry -> {
			resourceUrl.append("&" + entry.getKey() + "=" + entry.getValue()[0]);	
		});		
	}
	
	private JSONObject retrieveTenantAndUsername(
			ResourceRequest resourceRequest, String tenant,
			String usernameForController, String email) {
		
		logger.debug("Retrieving info of user: " + usernameForController);
		
		JSONObject jo = JSONFactoryUtil.createJSONObject();
		
		try{
			jo.put("tenant", tenant);
			jo.put("fullname", usernameForController);
			jo.put("email" ,email);
			jo.put("initials", "");
			jo.put("locale", PortalUtil.getHttpServletRequest(resourceRequest).getLocale().toString());

			InputStream input = null;

			try {
				Properties props = new Properties();
				input = Thread.currentThread().getContextClassLoader().getResourceAsStream("configuration.properties");
				props.load(input);

				String tabName = (String)props.get("layer.visualization.tab.name");
				String layerProjectTabName = (String) props.get("project.and.layer.organization.tab.name");
				String layerProjectTitle = (String) props.get("project.and.layer.organization.title");
				String layerProjectSubtitle = (String) props.get("project.and.layer.organization.subtitle");

                jo.put("tabName", tabName);
                jo.put("layerProjectTabName", layerProjectTabName);
				jo.put("layerProjectTitle", layerProjectTitle);
				jo.put("layerProjectSubtitle", layerProjectSubtitle);
			} catch (Exception ex) {
			    logger.error("Failed to read tab name from properties file");
                jo.put("tabName", "");
            } finally {

			    if(input != null)
			        input.close();

            }

		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return jo;
	}
}