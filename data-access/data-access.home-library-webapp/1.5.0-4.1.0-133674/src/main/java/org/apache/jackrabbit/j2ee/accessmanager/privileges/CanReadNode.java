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

public class CanReadNode extends HttpServlet {

	private static Logger logger = LoggerFactory.getLogger(CanReadNode.class);
	private static final long serialVersionUID = 1L;

	public CanReadNode() {
		super();
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		//		response.setContentType("text/plain");
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();

		String login = request.getParameter(ServletParameter.PORTAL_LOGIN);
		String absPath = new String(request.getParameter(ServletParameter.ABS_PATH).getBytes("iso-8859-1"), "UTF-8");

		Repository rep = RepositoryAccessServlet.getRepository(getServletContext());
		Session session = null;
		XStream xstream = null;
		String xmlConfig = null;
		SessionManager sessionManager = null;

		String sessionId = null;
		try {
			xstream = new XStream(new DomDriver("UTF-8"));

			sessionManager = SessionManager.getInstance(rep);
			session = sessionManager.newSession(login);
			sessionId = session.toString();

			Boolean flag = canReadNode(absPath, session);
			
			logger.info("Check if " + session.getUserID() + " can read node " + absPath + "? " + flag);
			xmlConfig = xstream.toXML(flag);
			response.setContentLength(xmlConfig.length());
			out.println(xmlConfig);
			out.close();
			out.flush();
		} catch (Exception e) {
			
			logger.error("Error repository ex " + e);
			xmlConfig = xstream.toXML(e.toString());
			response.setContentLength(xmlConfig.length());
			out.println(xmlConfig);
			out.close();
			out.flush();
			
		} finally {
			
			if (session!=null)
				sessionManager.releaseSession(sessionId);
		}
	}




	public static boolean canReadNode(String absPath, Session session) throws Exception {

		try {
			AccessControlManager accessControlManager = session.getAccessControlManager();

			boolean canRead = accessControlManager.hasPrivileges(absPath, new Privilege[] {
					accessControlManager.privilegeFromName(CustomPrivilege.JCR_READ)
			});
			return canRead;
		} catch (RepositoryException e) {
			return false;
		}
	}




}

