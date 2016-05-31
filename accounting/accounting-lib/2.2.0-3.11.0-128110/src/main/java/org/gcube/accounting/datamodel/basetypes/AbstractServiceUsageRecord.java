/**
 * 
 */
package org.gcube.accounting.datamodel.basetypes;

import java.io.Serializable;
import java.util.Map;

import org.gcube.accounting.datamodel.BasicUsageRecord;
import org.gcube.documentstore.exception.InvalidValueException;
import org.gcube.documentstore.records.implementation.RequiredField;
import org.gcube.documentstore.records.implementation.validations.annotations.NotEmpty;
import org.gcube.documentstore.records.implementation.validations.annotations.ValidLong;

/**
 * @author Luca Frosini (ISTI - CNR) http://www.lucafrosini.com/
 *
 */
public abstract class AbstractServiceUsageRecord extends BasicUsageRecord {

	/**
	 * Generated Serial Version UID
	 */
	private static final long serialVersionUID = -4214891294699473587L;

	/**
	 * KEY for : hostname:port of the Hosting Node made the service call
	 */
	@RequiredField @NotEmpty
	public static final String CALLER_HOST = "callerHost";
	
	/**
	 * KEY for : hostname:port of the Hosting Node receiving the service call
	 */
	@RequiredField @NotEmpty
	public static final String HOST = "host";
	
	/**
	 * KEY for : Service Class
	 */
	@RequiredField @NotEmpty
	public static final String SERVICE_CLASS = "serviceClass";
	
	/**
	 * KEY for : Service Name
	 */
	@RequiredField @NotEmpty
	public static final String SERVICE_NAME = "serviceName";
	
	/**
	 * KEY for : Service Name
	 */
	@RequiredField @NotEmpty
	public static final String CALLED_METHOD = "calledMethod";
	
	/**
	 * KEY for : Service Name
	 */
	@RequiredField @ValidLong
	public static final String DURATION = "duration";
	
	public AbstractServiceUsageRecord(){
		super();
	}
	
	public AbstractServiceUsageRecord(Map<String, ? extends Serializable> properties) throws InvalidValueException {
		super(properties);
	}

	private static final String ABSTRACT_TO_REPLACE = "Abstract";
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String giveMeRecordType() {
		return AbstractServiceUsageRecord.class.getSimpleName().replace(ABSTRACT_TO_REPLACE, "");
	}
	
	public String getCallerHost() {
		return (String) this.resourceProperties.get(CALLER_HOST);
	}

	public void setCallerHost(String callerHost) throws InvalidValueException {
		setResourceProperty(CALLER_HOST, callerHost);
	}
	
	public String getHost() {
		return (String) this.resourceProperties.get(HOST);
	}

	public void setHost(String host) throws InvalidValueException {
		setResourceProperty(HOST, host);
	}
	
	public String getServiceClass() {
		return (String) this.resourceProperties.get(SERVICE_CLASS);
	}

	public void setServiceClass(String serviceClass) throws InvalidValueException {
		setResourceProperty(SERVICE_CLASS, serviceClass);
	}
	
	public String getServiceName() {
		return (String) this.resourceProperties.get(SERVICE_NAME);
	}

	public void setServiceName(String serviceName) throws InvalidValueException {
		setResourceProperty(SERVICE_NAME, serviceName);
	}
	
	public String getCalledMethod() {
		return (String) this.resourceProperties.get(CALLED_METHOD);
	}

	public void setCalledMethod(String calledMethod) throws InvalidValueException {
		setResourceProperty(CALLED_METHOD, calledMethod);
	}
	
	public Long getDuration() {
		return (Long) this.resourceProperties.get(DURATION);
	}
	
	public void setDuration(Long duration) throws InvalidValueException {
		setResourceProperty(DURATION, duration);
	}
}
