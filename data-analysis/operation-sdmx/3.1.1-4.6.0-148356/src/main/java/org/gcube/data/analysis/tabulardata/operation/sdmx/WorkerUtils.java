package org.gcube.data.analysis.tabulardata.operation.sdmx;

import java.util.List;
import java.util.Map;

import org.gcube.data.analysis.tabulardata.operation.invocation.OperationInvocation;
import org.gcube.data.analysis.tabulardata.operation.parameters.Parameter;
import org.gcube.data.analysis.tabulardata.operation.parameters.leaves.RegexpStringParameter;
import org.gcube.data.analysis.tabulardata.operation.sdmx.security.Credentials;
import org.gcube.data.analysis.tabulardata.operation.sdmx.security.ISManager;
import org.gcube.data.analysis.tabulardata.operation.worker.exceptions.InvalidInvocationException;
import org.gcube.data.analysis.tabulardata.operation.worker.exceptions.InvalidParameterException;
import org.gcube.datapublishing.sdmx.api.model.SDMXRegistryInterfaceType;
import org.gcube.datapublishing.sdmx.api.registry.SDMXRegistryClient;
import org.gcube.datapublishing.sdmx.impl.model.SDMXRegistryDescriptorImpl;
import org.gcube.datapublishing.sdmx.impl.registry.FusionRegistryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class WorkerUtils {

	public static final String VERSION = "version";
	public static final String AGENCY = "agency";
	public static final String ID = "id";
	public static final String REGISTRY_BASE_URL = "registryBaseUrl";
	//public static final String REGISTRY_USERNAME = "account";
	//public static final String REGISTRY_PASSWORD = "password";
	public static final String SDMX_PROTOCOL = "RESTV2_1";
	private static final Logger log = LoggerFactory.getLogger(WorkerUtils.class);
	
	
	public static void checkParameters(List<Parameter> parameters, OperationInvocation invocation)
			throws InvalidInvocationException {
		Map<String, Object> parameterInstances = invocation.getParameterInstances();
		for (Parameter parameterDefinition : parameters) {
			checkParameter(parameterInstances.get(parameterDefinition.getIdentifier()), parameterDefinition, invocation);
		}
	}

	public static void checkParameter(Object parameterValue, Parameter parameterDefinition, OperationInvocation invocation)
			throws InvalidInvocationException {
		if (parameterValue == null)
			throw new InvalidParameterException(invocation, parameterDefinition.getIdentifier());		
		try {
			RegexpStringParameter regexpStringParameter = (RegexpStringParameter) parameterDefinition;
			regexpStringParameter.validateValue((String) parameterValue);
		} catch (Exception e) {
			throw new InvalidParameterException(invocation, parameterDefinition.getIdentifier());
		}		
	}

	public static SDMXRegistryClient initSDMXClient(String url,String username, String password) {
		
		try
		{
			log.debug("Generating registry client for url "+url);
			SDMXRegistryDescriptorImpl descriptor = new SDMXRegistryDescriptorImpl();
			descriptor.setUrl(SDMXRegistryInterfaceType.RESTV2_1, url);
			descriptor.setVersionAware(true);
			
			if (username != null && username.trim().length()>0 && password != null && password.length()>0)
																descriptor.setCredentials(username, password);
			//SDMXRegistryClient registryClient = new FusionRegistryClient(descriptor);
			SDMXRegistryClient registryClient = new FusionRegistryClient(descriptor);
			
			return registryClient;
		} catch (Throwable e)
		{
			log.error("Unable to complete the operation",e);
			throw e;
		}
		

	}

	public static Credentials retrieveCredentials (String endpoint)
	{
		ISManager isManager = new ISManager();
		Credentials credentials = isManager.getCredentials(endpoint, SDMX_PROTOCOL);
		log.debug(credentials.toString());
		return credentials;
		
	}

	public static void main(String[] args) {
		

		initSDMXClient("http://node8.d.d4science.research-infrastructures.eu:8080/FusionRegistry/",null, null);
		
	}
	
}
