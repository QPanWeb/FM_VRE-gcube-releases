package org.gcube.portlets.user.dataminermanager.server.dmservice;

import org.gcube.portlets.user.dataminermanager.server.util.ServiceCredentials;
import org.gcube.portlets.user.dataminermanager.shared.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder of Client 4 WPS Service
 * 
 * @author Giancarlo Panichi
 *
 * 
 */
public class SClient4WPSBuilder extends SClientBuilder {
	private static Logger logger = LoggerFactory
			.getLogger(SClient4WPSBuilder.class);
	private ServiceCredentials serviceCredendial;

	public SClient4WPSBuilder(ServiceCredentials serviceCredential) {
		this.serviceCredendial= serviceCredential;

	}

	@Override
	public void buildSClient() throws ServiceException {
		try {
			logger.debug("Build SC4WPS");
			logger.debug("ServiceCredential: " + serviceCredendial);
			SClient sClient = new SClient4WPS(serviceCredendial);

			sClientSpec.setSClient(sClient);
		} catch (Throwable e) {
			logger.error(e.getLocalizedMessage());
			e.printStackTrace();
			throw new ServiceException(e.getLocalizedMessage());
		}
	}

}
