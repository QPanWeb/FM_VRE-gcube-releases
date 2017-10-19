package org.gcube.portlets.admin.manageusers.server.portlet;




import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * ManageVREUsersPortlet Portlet Class
 * @author Massimiliano Assante - ISTI CNR
 */
public class ManageVREUsersPortlet extends GenericPortlet {
	
	public void doView(RenderRequest request, RenderResponse response)
			throws PortletException, IOException {

	    PortletRequestDispatcher dispatcher =
	        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/manageusers_view.jsp");
	    dispatcher.include(request, response);
		
	}
	
		/**
	 * 
	 */
	public void processAction(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {
	}

}
