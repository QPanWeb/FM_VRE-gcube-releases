package org.apache.jackrabbit.j2ee.workspacemanager.servlets.get;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.j2ee.ConfigRepository;
import org.apache.jackrabbit.j2ee.RepositoryAccessServlet;
import org.apache.jackrabbit.j2ee.workspacemanager.NodeManager;
import org.apache.jackrabbit.j2ee.workspacemanager.ServletParameter;
import org.apache.jackrabbit.j2ee.workspacemanager.SessionManager;
import org.gcube.common.homelibary.model.items.ItemDelegate;
import org.gcube.common.homelibary.model.items.type.NodeProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class GetChildrenById extends HttpServlet {

	private static Logger logger = LoggerFactory.getLogger(GetChildrenById.class);
	private static final long serialVersionUID = 1L;


	public GetChildrenById() {
		super();
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();

		final String login = request.getParameter(ServletParameter.LOGIN);
		String sessionId = request.getParameter(ServletParameter.UUID);
		final String user = request.getParameter(ConfigRepository.USER);
		final char[] pass = request.getParameter(ConfigRepository.PASSWORD).toCharArray();

		String id = request.getParameter(ServletParameter.ID);	
		Boolean showHidden = Boolean.parseBoolean(request.getParameter(ServletParameter.SHOW_HIDDEN));

		logger.info("Servlet GetChildrenById called with parameters: [id: "+ id + "]");

		Repository rep = RepositoryAccessServlet.getRepository(getServletContext());
		SessionImpl session = null;
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
				session = sessionManager.newSession(login, user, pass);
				sessionId = session.toString();
			}

			List<ItemDelegate> children = getChildren(session, id, login, showHidden);
			xmlConfig = xstream.toXML(children);
			out.println(xmlConfig);

		} catch (Exception e) {
			if (!exist)
				sessionManager.releaseSession(sessionId);
			throw new ServletException(e);

		} finally {
			if (!exist)
				sessionManager.releaseSession(sessionId);

			out.close();
			out.flush();
		}
	}



	/**
	 * Get children by id
	 * @param session
	 * @param identifier
	 * @param login
	 * @return
	 * @throws Exception
	 */
	private List<ItemDelegate> getChildren(SessionImpl session, String identifier, String login, Boolean showHidden) throws Exception {

		Node folderNode = session.getNodeByIdentifier(identifier);

		NodeIterator iterator = folderNode.getNodes();
		List<ItemDelegate> children = new ArrayList<ItemDelegate>();
		while(iterator.hasNext()) {

			Node node = iterator.nextNode();
			Boolean isHidden = false;

			try {
				if (node.hasProperty(NodeProperty.HIDDEN.toString()))
					isHidden = node.getProperty(NodeProperty.HIDDEN.toString()).getBoolean();

				String path = node.getPath();
				String name = path.substring(path.lastIndexOf('/') + 1);
				if ((isHidden && !showHidden) || (name.equals("Trash") || (name.equals("MySpecialFolders") ||(name.startsWith("rep:")) || (name.startsWith("hl:")))))
					continue;

				ItemDelegate item = null;
				NodeManager wrap = new NodeManager(node, login);

				item = wrap.getItemDelegate();
				children.add(item);
			} catch (Exception e) {
				logger.error("Problem adding child for node ID " + identifier  +": ", e.getMessage());
			}


		}
		return children;
	}


}
