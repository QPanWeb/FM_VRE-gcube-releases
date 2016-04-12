package org.apache.jackrabbit.j2ee.usermanager;

import java.io.IOException;
import java.io.PrintWriter;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.SimpleCredentials;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.j2ee.ConfigRepository;
import org.apache.jackrabbit.j2ee.RepositoryAccessServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

public class GetVersionServlet extends HttpServlet {

	public static final String VERSION_LABEL					= "hl:version";
	public static final String USER								= "user";

	private static final long serialVersionUID = 1L;
	
	private Logger logger = LoggerFactory.getLogger(GetVersionServlet.class);

	public GetVersionServlet() {
		super();
	}


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		logger.info("Servlet GetDisplayNameServlet called ......");

		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();

		Repository rep = RepositoryAccessServlet.getRepository(getServletContext());
		SessionImpl session = null;

		String version = null;

		XStream xstream = null;
		String xmlConfig = null;

		try {
			xstream = new XStream();
			session = (SessionImpl) rep
					.login(new SimpleCredentials(request.getParameter(ConfigRepository.USER), request.getParameter(ConfigRepository.PASSWORD).toCharArray()));

			final String login = request.getParameter(USER);

			final UserManager userManager = session.getUserManager();

			Authorizable authorizable = userManager.getAuthorizable(login);

			if (authorizable.isGroup()) {			
				Group group = (Group) authorizable;
				Value[] versionValue = group.getProperty(VERSION_LABEL);
				version = getVersion(versionValue);

			}
			else{
				User user = (User) authorizable;
				Value[] versionValue = user.getProperty(VERSION_LABEL);
				version = getVersion(versionValue);
			}
			
//			System.out.println("version: " + version);

//			session.save();

			xmlConfig = xstream.toXML(version);
			response.setContentLength(xmlConfig.length()); 
			out.println(xmlConfig);

		} catch (RepositoryException e) {
			e.printStackTrace();
			xmlConfig = xstream.toXML(version);
			response.setContentLength(xmlConfig.length()); 
			out.println(xmlConfig);	

		} finally {
			if(session != null)
				session.logout();

			out.close();
			out.flush();
		}	
	}


	private String getVersion(Value[] versionValue) throws ValueFormatException, IllegalStateException, RepositoryException {

		String version = null;
		int size = versionValue.length;
		for (int i=0; i< size; i++){
			version = versionValue[i].getString();	
		}
		return version;

	}


}