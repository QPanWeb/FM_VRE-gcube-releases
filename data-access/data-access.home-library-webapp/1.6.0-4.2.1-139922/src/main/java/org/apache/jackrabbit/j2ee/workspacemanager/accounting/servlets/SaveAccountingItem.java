package org.apache.jackrabbit.j2ee.workspacemanager.accounting.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.j2ee.RepositoryAccessServlet;
import org.apache.jackrabbit.j2ee.workspacemanager.accounting.AccountingDelegateWrapper;
import org.apache.jackrabbit.j2ee.workspacemanager.session.SessionManager;
import org.gcube.common.homelibary.model.items.accounting.AccountingDelegate;
import org.gcube.common.homelibary.model.servlet.ServletParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;


public class SaveAccountingItem extends HttpServlet {

	private static Logger logger = LoggerFactory.getLogger(SaveAccountingItem.class);
	private static final long serialVersionUID = 1L;

	public SaveAccountingItem() {
		super();
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		//		response.setContentType("text/plain");
		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();

		String sessionId = request.getParameter(ServletParameter.UUID);

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
				//				logger.info(sessionId + " already exists, get it");
			}
			else{				 
				session = sessionManager.newSession(request);
				sessionId = session.toString();
				//				logger.info(sessionId + " does not exist, a new session has been created " + session.toString());
			}


				try{
					AccountingDelegate item = (AccountingDelegate) xstream.fromXML(request.getInputStream());
					logger.info("Servlet SaveAccountingItem called for entry " + item.getEntryType().toString());

					AccountingDelegateWrapper wrapper = new AccountingDelegateWrapper(item, "");
					AccountingDelegate new_item = wrapper.save(session);

					xmlConfig = xstream.toXML(new_item);
					response.setContentLength(xmlConfig.length());
					out.println(xmlConfig);

				} catch (Exception e) {
					logger.error("Error repository ex " + e);
					xmlConfig = xstream.toXML(e.toString());
					response.setContentLength(xmlConfig.length());
					out.println(xmlConfig);

				}

			

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error repository ex " + e.getMessage());
			xmlConfig = xstream.toXML(e.toString());
			response.setContentLength(xmlConfig.length());
			out.println(xmlConfig);

		} finally {
			if (!exist){
				sessionManager.releaseSession(sessionId);
				//				logger.info("Released session " + sessionId);
			}
			out.close();
			out.flush();
		}
	}



}
