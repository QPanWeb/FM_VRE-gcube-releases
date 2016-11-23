package org.apache.jackrabbit.j2ee.accessmanager.privileges;

import java.io.IOException;
import java.io.PrintWriter;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.j2ee.RepositoryAccessServlet;
import org.gcube.common.homelibary.model.servlet.ServletParameter;
import org.apache.jackrabbit.j2ee.workspacemanager.SessionManager;
import org.apache.jackrabbit.j2ee.workspacemanager.util.CustomPrivilege;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class CanDelete extends HttpServlet {

	private static Logger logger = LoggerFactory.getLogger(CanDelete.class);
	private static final long serialVersionUID = 1L;

	public CanDelete() {
		super();
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		//		response.setContentType("text/plain");
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		
		String login = request.getParameter(ServletParameter.PORTAL_LOGIN);
		String sessionId = request.getParameter(ServletParameter.UUID);
		String absPath = new String(request.getParameter(ServletParameter.ABS_PATH).getBytes("iso-8859-1"), "UTF-8");
		boolean isRoot = Boolean.valueOf(request.getParameter(ServletParameter.ISROOT));
		
		Repository rep = RepositoryAccessServlet.getRepository(getServletContext());
		Session session = null;
		XStream xstream = null;
		String xmlConfig = null;
		SessionManager sessionManager = null;
		boolean exist = false;
		try {
			xstream = new XStream(new DomDriver("UTF-8"));

			sessionManager = SessionManager.getInstance(rep);
			exist = sessionManager.sessionExists(sessionId); 
			if (exist){				
				session = sessionManager.getSession(sessionId);
			} else {
				session = sessionManager.newSession(login);
				sessionId = session.toString();
			}

			try{
				Boolean flag = canDelete(session.getUserID(), absPath, isRoot, session);
				xmlConfig = xstream.toXML(flag);
				response.setContentLength(xmlConfig.length());
				out.println(xmlConfig);
			} catch (Exception e) {
				logger.error("Error checking ACL CanAddChildren of item: " + absPath , e);
				xmlConfig = xstream.toXML(e.toString());
				response.setContentLength(xmlConfig.length());
				out.println(xmlConfig);
			}

		} catch (Exception e) {
			logger.error("Error repository ex " + e);
			xmlConfig = xstream.toXML(e.toString());
			response.setContentLength(xmlConfig.length());
			out.println(xmlConfig);

		} finally {
			if (!exist){
				sessionManager.releaseSession(sessionId);
			}
			out.close();
			out.flush();
		}
	}



	
	public static boolean canDelete(String login, String absPath, boolean isRoot, Session session) throws Exception {

		try {
			AccessControlManager accessControlManager = session.getAccessControlManager();
			String parentPath;
			int lastSlash = absPath.lastIndexOf('/');
			if (lastSlash == 0) {
				//the parent is the root folder.
				parentPath = "/";
			} else {
				//strip the last segment
				parentPath = absPath.substring(0, lastSlash);
			}

			try{

				accessControlManager.hasPrivileges(absPath, new Privilege[] {
						accessControlManager.privilegeFromName(CustomPrivilege.NO_LIMIT), accessControlManager.privilegeFromName(CustomPrivilege.JCR_REMOVE_NODE)
				});

			}catch (Exception e) {
				throw new Exception("Error retrieving privilege: " + e);
			}

			boolean canDelete = false;
			if (isRoot)
				canDelete = accessControlManager.hasPrivileges(absPath, new Privilege[] {
						accessControlManager.privilegeFromName(CustomPrivilege.JCR_REMOVE_NODE), accessControlManager.privilegeFromName(CustomPrivilege.NO_LIMIT), accessControlManager.privilegeFromName(CustomPrivilege.REMOVE_ROOT)
				});
			//				}) && canDeleteChildren(session, parentPath);
			else{
				canDelete = accessControlManager.hasPrivileges(absPath, new Privilege[] {
						accessControlManager.privilegeFromName(CustomPrivilege.JCR_REMOVE_NODE), accessControlManager.privilegeFromName(CustomPrivilege.NO_LIMIT)
				}) && CheckUtil.canDeleteChildren(parentPath, session);


			}
			//			System.out.println("canDelete? " + canDelete);
			//			System.out.println("canDeleteChildren? " + canDeleteChildren(session, parentPath));
			return canDelete;
		} catch (RepositoryException e) {
			return false;
		}
	}
	
	
	

}

