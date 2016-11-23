package org.gcube.portlets.widgets.ckandatapublisherwidget.server;

import static org.gcube.common.authorization.client.Constants.authorizationService;

import java.util.ArrayList;
import java.util.List;

import org.gcube.common.authorization.library.provider.SecurityTokenProvider;
import org.gcube.common.authorization.library.provider.UserInfo;
import org.gcube.common.scope.api.ScopeProvider;
import org.gcube.portlets.widgets.ckandatapublisherwidget.server.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Let the Product Catalogue Manager write a post in a VRE and alert there is a new product
 * @author Costantino Perciante at ISTI-CNR 
 * (costantino.perciante@isti.cnr.it)
 */
public class WritePostCatalogueManagerThread extends Thread {

	private static final Logger logger = LoggerFactory.getLogger(WritePostCatalogueManagerThread.class);

	private String username;
	private String scope;
	private String productTitle; 
	private String productUrl;
	private boolean enableNotification;
	private List<String> hashtags;
	String userFullName;

	/**
	 * @param token
	 * @param scope
	 * @param productTitle
	 * @param productUrl
	 * @param enableNotification
	 * @param hashtags
	 * @param userFullName
	 */
	public WritePostCatalogueManagerThread(
			String username, String scope,
			String productTitle, String productUrl, boolean enableNotification,
			List<String> hashtags, String userFullName) {
		super();
		this.username = username;
		this.scope = scope;
		this.productTitle = productTitle;
		this.productUrl = productUrl;
		this.enableNotification = enableNotification;
		this.hashtags = hashtags;
		this.userFullName = userFullName;
	}

	@Override
	public void run() {

		try{
			// evaluate user's token for this scope
			String token = authorizationService().generateUserToken(new UserInfo(username, new ArrayList<String>()), scope);

			logger.info("Started request to write application post "
					+ "for new product created. Scope is " + scope + " and "
					+ "token is " + token.substring(0, 10) + "****************");

			// set token and scope
			ScopeProvider.instance.set(scope);
			SecurityTokenProvider.instance.set(token);

			// write
			Utils.writeProductPost(
					productTitle, 
					productUrl, 
					userFullName, 
					hashtags, 
					enableNotification
					);

		}catch(Exception e){
			logger.error("Failed to write the post because of the following error ", e);
		}finally{
			// remove token and scope
			SecurityTokenProvider.instance.reset();
			ScopeProvider.instance.reset();
		}
	}

}
