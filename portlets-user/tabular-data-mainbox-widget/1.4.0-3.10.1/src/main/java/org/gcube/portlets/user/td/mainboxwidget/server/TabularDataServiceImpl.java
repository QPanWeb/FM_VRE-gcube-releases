package org.gcube.portlets.user.td.mainboxwidget.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.gcube.application.framework.core.session.ASLSession;
import org.gcube.application.framework.core.session.SessionManager;
import org.gcube.portal.custom.scopemanager.scopehelper.ScopeHelper;
import org.gcube.portlets.user.td.gwtservice.shared.Constants;
import org.gcube.portlets.user.td.mainboxwidget.client.rpc.TabularDataService;
import org.gcube.portlets.user.tdwx.datasource.td.TDXDataSourceFactory;
import org.gcube.portlets.user.tdwx.server.datasource.DataSourceXFactoryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;


/**
 * 
 * @author giancarlo
 * email: <a href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a> 
 *
 */
@SuppressWarnings("serial")
public class TabularDataServiceImpl extends RemoteServiceServlet implements TabularDataService {

	protected static Logger logger = LoggerFactory.getLogger(TabularDataServiceImpl.class);
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init() throws ServletException {
		super.init();

		/*System.out.println("initializing the TabularDataImportManager");
		TabularDataImporterManager importerManager = new TabularDataImporterManager();
		//importerManager.scanAvailableImporters();
		importerManager.add(new CSVTDImporter());
		importerManager.setupImporters();
		 */
		//register the demo csv target
		//CSVTargetRegistry.getInstance().add(new DemoCSVTarget());
		//System.out.println("Registered DemoCSVTarget");

		//ScopeProvider.instance.get();
		System.out.println("Fix JAXP: jdk.xml.entityExpansionLimit=0");
		System.setProperty("jdk.xml.entityExpansionLimit", "0");
		
		System.out.println("initializing TDXDataSourceFactory");
		DataSourceXFactoryRegistry.getInstance().add(new TDXDataSourceFactory());
	}

	
	protected static ASLSession getAslSession(HttpSession httpSession)
	{
		String username = (String) httpSession
				.getAttribute(ScopeHelper.USERNAME_ATTRIBUTE);
		ASLSession session;
		if (username == null) {
			logger.warn("no user found in session, using test one");
			/*throw new Exception("Session Expired!");*/

			// Remove comment for Test
			
			username = Constants.DEFAULT_USER;
			String scope = Constants.DEFAULT_SCOPE;

			httpSession.setAttribute(ScopeHelper.USERNAME_ATTRIBUTE, username);
			session = SessionManager.getInstance().getASLSession(
					httpSession.getId(), username);
			session.setScope(scope);
		
		} else {
			session = SessionManager.getInstance().getASLSession(
					httpSession.getId(), username);

		}

		logger.info("SessionUtil: aslSession " + session.getUsername() + " "
				+ session.getScope());

		return session;

	}
	
	/**
	 * {@inheritDoc}
	 */
	public String hello() throws IllegalArgumentException {
		ASLSession aslSession=getAslSession(this.getThreadLocalRequest().getSession());
		return "Hello "+aslSession.getUsername();
	}
}
