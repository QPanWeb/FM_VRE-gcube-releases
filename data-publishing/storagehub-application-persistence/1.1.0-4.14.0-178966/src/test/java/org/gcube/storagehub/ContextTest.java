/**
 * 
 */
package org.gcube.storagehub;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.gcube.common.authorization.client.Constants;
import org.gcube.common.authorization.client.exceptions.ObjectNotFound;
import org.gcube.common.authorization.library.AuthorizationEntry;
import org.gcube.common.authorization.library.provider.AuthorizationProvider;
import org.gcube.common.authorization.library.provider.ClientInfo;
import org.gcube.common.authorization.library.provider.SecurityTokenProvider;
import org.gcube.common.authorization.library.utils.Caller;
import org.gcube.common.scope.api.ScopeProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Luca Frosini (ISTI - CNR)
 *
 */
public class ContextTest {
	
	private static final Logger logger = LoggerFactory.getLogger(ContextTest.class);
	
	protected static final String PROPERTIES_FILENAME = "token.properties"; 
	
	private static final String GCUBE_VARNAME = "GCUBE";
	public static final String GCUBE;
	
	private static final String GCUBE_DEVNEXT_VARNAME = "GCUBE_DEVNEXT";
	public static final String GCUBE_DEVNEXT;
	
	private static final String GCUBE_DEVNEXT_NEXTNEXT_VARNAME = "GCUBE_DEVNEXT_NEXTNEXT";
	public static final String GCUBE_DEVNEXT_NEXTNEXT;
	
	public static final String GCUBE_DEVSEC_VARNAME = "GCUBE_DEVSEC";
	public static final String GCUBE_DEVSEC;
	
	public static final String GCUBE_DEVSEC_DEVVRE_VARNAME = "GCUBE_DEVSEC_DEVVRE";
	public static final String GCUBE_DEVSEC_DEVVRE;
	
	
	private static final String GCUBE_DEVNEXT_ANOTHER_USER_VARNAME = "GCUBE_DEVNEXT_ANOTHER_USER";
	public static final String GCUBE_DEVNEXT_ANOTHER_USER;
	
	public static final String DEFAULT_TEST_SCOPE;
	
	public static final String GCUBE_PRE_PROD_PREVRE_VARNAME = "GCUBE_PRE_PROD_PREVRE";
	public static final String GCUBE_PRE_PROD_PREVRE;
	
	public static final String GCUBE_PRE_PROD_PARTHENOS_REGISTRY_VARNAME = "GCUBE_PRE_PROD_PARTHENOS_REGISTRY";
	public static final String GCUBE_PRE_PROD_PARTHENOS_REGISTRY;
	
	
	public static final String GCUBE_PRE_PROD_PREVRE_APP_TOKEN_VARNAME = "GCUBE_PRE_PROD_PREVRE_APP_TOKEN";
	public static final String GCUBE_PRE_PROD_PREVRE_APP_TOKEN;
	
	public static final String ROOT_VARNAME = "ROOT";
	public static final String ROOT;
	
	static {
		Properties properties = new Properties();
		InputStream input = ContextTest.class.getClassLoader().getResourceAsStream(PROPERTIES_FILENAME);

		try {
			// load the properties file
			properties.load(input);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		GCUBE = properties.getProperty(GCUBE_VARNAME);
		
		GCUBE_DEVNEXT = properties.getProperty(GCUBE_DEVNEXT_VARNAME);
		GCUBE_DEVNEXT_NEXTNEXT = properties.getProperty(GCUBE_DEVNEXT_NEXTNEXT_VARNAME);
		
		GCUBE_DEVSEC = properties.getProperty(GCUBE_DEVSEC_VARNAME);
		GCUBE_DEVSEC_DEVVRE = properties.getProperty(GCUBE_DEVSEC_DEVVRE_VARNAME);
		
		GCUBE_DEVNEXT_ANOTHER_USER = properties.getProperty(GCUBE_DEVNEXT_ANOTHER_USER_VARNAME);
		
		GCUBE_PRE_PROD_PARTHENOS_REGISTRY = properties.getProperty(GCUBE_PRE_PROD_PARTHENOS_REGISTRY_VARNAME);
		GCUBE_PRE_PROD_PREVRE = properties.getProperty(GCUBE_PRE_PROD_PREVRE_VARNAME);
		
		GCUBE_PRE_PROD_PREVRE_APP_TOKEN =  properties.getProperty(GCUBE_PRE_PROD_PREVRE_APP_TOKEN_VARNAME);
		
		
		ROOT = properties.getProperty(ROOT_VARNAME);
		
		DEFAULT_TEST_SCOPE = GCUBE_PRE_PROD_PREVRE;

	}
	
	public static String getCurrentScope(String token) throws ObjectNotFound, Exception{
		AuthorizationEntry authorizationEntry = Constants.authorizationService().get(token);
		String context = authorizationEntry.getContext();
		logger.info("Context of token {} is {}", token, context);
		return context;
	}
	
	
	public static void setContext(String token) throws ObjectNotFound, Exception{
		SecurityTokenProvider.instance.set(token);
		AuthorizationEntry authorizationEntry = Constants.authorizationService().get(token);
		ClientInfo clientInfo = authorizationEntry.getClientInfo();
		logger.debug("User : {} - Type : {}", clientInfo.getId(), clientInfo.getType().name());
		String qualifier = authorizationEntry.getQualifier();
		Caller caller = new Caller(clientInfo, qualifier);
		AuthorizationProvider.instance.set(caller);
		ScopeProvider.instance.set(getCurrentScope(token));
	}
	
	@BeforeClass
	public static void beforeClass() throws Exception{
		setContext(DEFAULT_TEST_SCOPE);
	}
	
	@AfterClass
	public static void afterClass() throws Exception{
		SecurityTokenProvider.instance.reset();
		ScopeProvider.instance.reset();
	}
	
}
