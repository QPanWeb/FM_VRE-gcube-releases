package org.gcube.data.analysis.dataminermanagercl.server.dmservice;

import org.gcube.data.analysis.dataminermanagercl.server.util.ServiceCredentials;
import org.gcube.data.analysis.dataminermanagercl.shared.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder of Client 4 WPS Service
 * 
 * @author "Giancarlo Panichi" email: <a
 *         href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a>
 * 
 */
public class SClient4WPSBuilder extends SClientBuilder {
	private static Logger logger = LoggerFactory
			.getLogger(SClient4WPSBuilder.class);
	private ServiceCredentials serviceCredendials;
	private String serviceAddressUrl;
	private boolean useUrl;

	/**
	 * 
	 * @param serviceCredentials
	 */
	public SClient4WPSBuilder(ServiceCredentials serviceCredentials) {
		this.serviceCredendials = serviceCredentials;
		this.useUrl = false;
	}

	/**
	 * 
	 * @param serviceCredentials
	 * @param serviceAddressUrl
	 *            valid url for example:
	 *            <span>http://dataminer1-devnext.d4science.org/wps/</span>
	 */
	public SClient4WPSBuilder(ServiceCredentials serviceCredentials,
			String serviceAddressUrl) {
		this.serviceCredendials = serviceCredentials;
		this.serviceAddressUrl = serviceAddressUrl;
		this.useUrl = true;
	}

	@Override
	public void buildSClient() throws ServiceException {
		try {
			logger.debug("Build SC4WPS");
			logger.debug("ServiceCredentials: " + serviceCredendials);
			SClient sClient;
			if (useUrl) {
				logger.debug("Use Url: " + useUrl);
				logger.debug("DataMiner Service Address: " + serviceAddressUrl);
				sClient = new SClient4WPS(serviceCredendials, serviceAddressUrl);
			} else {
				logger.debug("Use Url: " + useUrl);
				sClient = new SClient4WPS(serviceCredendials);
			}
			sClientSpec.setSClient(sClient);
		} catch (Throwable e) {
			logger.error(e.getLocalizedMessage());
			e.printStackTrace();
			throw new ServiceException(e.getLocalizedMessage(), e);
		}
	}

}
