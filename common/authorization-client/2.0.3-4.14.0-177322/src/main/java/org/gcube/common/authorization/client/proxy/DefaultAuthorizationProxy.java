package org.gcube.common.authorization.client.proxy;

import static org.gcube.common.authorization.client.Constants.CONTEXT_PARAM;
import static org.gcube.common.authorization.client.Constants.CLIENT_ID_PARAM;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;

import org.gcube.common.authorization.client.Binder;
import org.gcube.common.authorization.client.Constants;
import org.gcube.common.authorization.client.exceptions.ObjectNotFound;
import org.gcube.common.authorization.library.AuthorizationEntry;
import org.gcube.common.authorization.library.ExternalServiceList;
import org.gcube.common.authorization.library.Policies;
import org.gcube.common.authorization.library.QualifiersList;
import org.gcube.common.authorization.library.enpoints.AuthorizationEndpoint;
import org.gcube.common.authorization.library.enpoints.AuthorizationEndpointScanner;
import org.gcube.common.authorization.library.enpoints.EndpointsContainer;
import org.gcube.common.authorization.library.policies.Policy;
import org.gcube.common.authorization.library.provider.ContainerInfo;
import org.gcube.common.authorization.library.provider.SecurityTokenProvider;
import org.gcube.common.authorization.library.provider.ServiceInfo;
import org.gcube.common.authorization.library.provider.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultAuthorizationProxy implements AuthorizationProxy {

	private static Logger log = LoggerFactory.getLogger(AuthorizationProxy.class);

	private static Map<String, AuthorizationEntryCache> cache = Collections.synchronizedMap(new HashMap<String, AuthorizationEntryCache>());

	private static EndpointsContainer endpoints;

	public DefaultAuthorizationProxy() {
		if (endpoints==null)
			endpoints = AuthorizationEndpointScanner.endpoints();
	}

	private String getInternalEnpoint(int infrastructureHash){
		AuthorizationEndpoint ae = getEndpoint(infrastructureHash);
		StringBuilder endpoint = new StringBuilder(ae.isSecureConnection()?"https://":"http://").append(ae.getHost()).append(":")
				.append(ae.getPort()).append("/authorization-service/gcube/service");
		return endpoint.toString();
	}

	@Override
	public String generateServiceToken(ServiceInfo client) throws Exception {

		String methodPath = "/token/service"; 

		int infrastructureHash = Utils.getInfrastructureHashFromToken(SecurityTokenProvider.instance.get(), endpoints.getDefaultInfrastructure());

		StringBuilder callUrl = new StringBuilder(getInternalEnpoint(infrastructureHash)).append(methodPath);

		URL url = new URL(callUrl.toString());
		HttpURLConnection connection = makeRequest(url, "PUT", true);
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestProperty("Content-type", "application/xml");
		
		try(OutputStream os = new BufferedOutputStream(connection.getOutputStream())){
			Binder.getContext().createMarshaller().marshal(client, os);
		}

		log.debug("response code for "+callUrl.toString()+" is "+connection.getResponseCode()+" "+connection.getResponseMessage());

		if (connection.getResponseCode()!=200) throw new Exception("error contacting authorization service");
		String token= "";
		try(BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream)connection.getContent()))){
			StringBuilder result = new StringBuilder();
			String line;
			while((line = reader.readLine()) != null) 
				result.append(line);
			token =  result.toString();
		}

		return Utils.addInfrastructureHashToToken(token, infrastructureHash);
	}

	@Override
	public String generateExternalServiceToken(String serviceId) throws Exception {

		String methodPath = "/token/external/"; 

		int infrastructureHash = Utils.getInfrastructureHashFromToken(SecurityTokenProvider.instance.get(), endpoints.getDefaultInfrastructure());

		StringBuilder callUrl = new StringBuilder(getInternalEnpoint(infrastructureHash)).append(methodPath).append(serviceId);

		URL url = new URL(callUrl.toString());
		HttpURLConnection connection = makeRequest(url, "PUT", true);
		connection.setDoInput(true);
		connection.setRequestProperty("Content-type", "application/xml");

		log.debug("response code for "+callUrl.toString()+" is "+connection.getResponseCode()+" "+connection.getResponseMessage());

		if (connection.getResponseCode()!=200) throw new Exception("error contacting authorization service");
		String token= "";
		try(BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream)connection.getContent()))){
			StringBuilder result = new StringBuilder();
			String line;
			while((line = reader.readLine()) != null) 
				result.append(line);
			token =  result.toString();
		}

		return Utils.addInfrastructureHashToToken(token, infrastructureHash);
	}
	
	@Override
	public String resolveTokenByUserAndContext(String user, String context) throws ObjectNotFound, Exception {

		String methodPath = "/token/resolve/"; 

		int infrastructureHash = Utils.getInfrastructureHashfromContext(context);

		StringBuilder callUrl = new StringBuilder(getInternalEnpoint(infrastructureHash)).append(methodPath).append(user).append("?context=").append(context);

		URL url = new URL(callUrl.toString());
		HttpURLConnection connection = makeRequest(url, "GET", false);
		
		log.debug("response code for "+callUrl.toString()+" is "+connection.getResponseCode()+" "+connection.getResponseMessage());

		if (connection.getResponseCode()==404) throw new ObjectNotFound("token not found");
		if (connection.getResponseCode()!=200) throw new Exception("error contacting authorization service (error code is "+connection.getResponseCode()+")");
		if (connection.getContentLengthLong()==0) return null;
		String token= "";
		try(BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream)connection.getContent()))){
			StringBuilder result = new StringBuilder();
			String line;
			while((line = reader.readLine()) != null) 
				result.append(line);
			token =  result.toString();
		}

		return Utils.addInfrastructureHashToToken(token, infrastructureHash);
	}
	
	@Override
	public String generateUserToken(UserInfo client, String context) throws Exception {

		String methodPath = "/token/user"; 

		int infrastructureHash = Utils.getInfrastructureHashfromContext(context);

		StringBuilder callUrl = new StringBuilder(getInternalEnpoint(infrastructureHash)).append(methodPath).append("?")
				.append(CONTEXT_PARAM).append("=").append(context);

		URL url = new URL(callUrl.toString());
		HttpURLConnection connection = makeRequest(url, "PUT", false);
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestProperty("Content-type", "application/xml");

		try(OutputStream os = new BufferedOutputStream(connection.getOutputStream())){
			Binder.getContext().createMarshaller().marshal(client, os);
		}

		log.debug("response code for "+callUrl.toString()+" is "+connection.getResponseCode()+" "+connection.getResponseMessage());

		if (connection.getResponseCode()!=200) throw new Exception("error contacting authorization service");
		String token= "";
		try(BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream)connection.getContent()))){
			StringBuilder result = new StringBuilder();
			String line;
			while((line = reader.readLine()) != null) 
				result.append(line);
			token =  result.toString();
		}

		return Utils.addInfrastructureHashToToken(token, infrastructureHash);
	}

	@Override
	public void removeAllReleatedToken(String clientId, String context) throws Exception{
		String methodPath = "/token/user";
		int infrastructureHash = Utils.getInfrastructureHashfromContext(context);
		StringBuilder callUrl = new StringBuilder(getInternalEnpoint(infrastructureHash)).append(methodPath).append("?")
				.append(CONTEXT_PARAM).append("=").append(context).append("&").append(CLIENT_ID_PARAM).append("=").append(clientId);
		
		URL url = new URL(callUrl.toString());
		HttpURLConnection connection = makeRequest(url, "DELETE", false);
		//connection.setDoOutput(false);
		connection.setDoInput(true);

		log.debug("response code for "+callUrl.toString()+" is "+connection.getResponseCode()+" "+connection.getResponseMessage());

		if (connection.getResponseCode()!=200 && connection.getResponseCode()!=204) throw new Exception("error contacting authorization service");
		
	}

	@Override
	public String generateApiKey(String apiQualifier) throws Exception {


		String methodPath = String.format("/apikey?qualifier=%s",apiQualifier); 

		int infrastructureHash = Utils.getInfrastructureHashFromToken(SecurityTokenProvider.instance.get(), endpoints.getDefaultInfrastructure());

		StringBuilder callUrl = new StringBuilder(getInternalEnpoint(infrastructureHash)).append(methodPath);

		URL url = new URL(callUrl.toString());
		HttpURLConnection connection = makeRequest(url, "PUT", true);
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setFixedLengthStreamingMode(0);
		connection.setRequestProperty("Content-type", "application/xml");


		log.debug("response code for "+callUrl.toString()+" is "+connection.getResponseCode()+" "+connection.getResponseMessage());

		if (connection.getResponseCode()!=200) throw new Exception("error contacting authorization service");
		String token= "";
		try(BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream)connection.getContent()))){
			StringBuilder result = new StringBuilder();
			String line;
			while((line = reader.readLine()) != null) 
				result.append(line);
			token =  result.toString();
		}

		return Utils.addInfrastructureHashToToken(token, infrastructureHash);
	}

	@Override
	/**
	 * return a map with key qualifier and value token
	 */
	public Map<String, String> retrieveApiKeys() throws Exception{
		String methodPath = "/apikey/";

		int infrastructureHash = Utils.getInfrastructureHashFromToken(SecurityTokenProvider.instance.get(), endpoints.getDefaultInfrastructure());


		StringBuilder callUrl = new StringBuilder(getInternalEnpoint(infrastructureHash)).append(methodPath);

		URL url = new URL(callUrl.toString());
		HttpURLConnection connection = makeRequest(url, "GET", true);
		connection.setDoInput(true);
		connection.setDoOutput(true);

		if (connection.getResponseCode()!=200) throw new Exception("error retrieving keys (error code is "+connection.getResponseCode()+")");
		if (connection.getContentLengthLong()==0) return Collections.emptyMap();

		Map<String, String> tokensQulifiersMap;
		try(InputStream stream = (InputStream)connection.getContent();){
			QualifiersList entries = (QualifiersList)Binder.getContext().createUnmarshaller().unmarshal(stream);
			tokensQulifiersMap = entries.getQualifiers();
		}
		
		if (tokensQulifiersMap!=null && !tokensQulifiersMap.isEmpty()){
			Map<String, String> toReturnMap = new HashMap<String, String>();
			for (Entry<String, String> entry: tokensQulifiersMap.entrySet())
				toReturnMap.put(entry.getKey(), Utils.addInfrastructureHashToToken(entry.getValue(), infrastructureHash));
			return toReturnMap;
		} else return Collections.emptyMap();
		
	}
	
	@Override
	/**
	 * return a map with key external service id and value token
	 */
	public Map<String, String> retrieveExternalServiceGenerated() throws Exception{
		String methodPath = "/token/external";

		int infrastructureHash = Utils.getInfrastructureHashFromToken(SecurityTokenProvider.instance.get(), endpoints.getDefaultInfrastructure());


		StringBuilder callUrl = new StringBuilder(getInternalEnpoint(infrastructureHash)).append(methodPath);

		URL url = new URL(callUrl.toString());
		HttpURLConnection connection = makeRequest(url, "GET", true);
		connection.setDoInput(true);
		connection.setDoOutput(true);

		if (connection.getResponseCode()!=200) throw new Exception("error retrieving externalServices (error code is "+connection.getResponseCode()+")");
		if (connection.getContentLengthLong()==0) return Collections.emptyMap();

		Map<String, String> externalServiceMap;
		try(InputStream stream = (InputStream)connection.getContent();){
			ExternalServiceList entries = (ExternalServiceList)Binder.getContext().createUnmarshaller().unmarshal(stream);
			externalServiceMap = entries.getExternalServiceMap();
		}
		
		if (externalServiceMap!=null && !externalServiceMap.isEmpty()){
			Map<String, String> toReturnMap = new HashMap<String, String>();
			for (Entry<String, String> entry: externalServiceMap.entrySet())
				toReturnMap.put(entry.getKey(), Utils.addInfrastructureHashToToken(entry.getValue(), infrastructureHash));
			return toReturnMap;
		} else return Collections.emptyMap();
		
	}

	@Override
	public String requestActivation(ContainerInfo container, String context) throws Exception {

		String methodPath = "/token/node"; 

		int infrastructureHash = Utils.getInfrastructureHashfromContext(context);

		StringBuilder callUrl;

		callUrl = new StringBuilder(getInternalEnpoint(infrastructureHash)).append(methodPath).append("?context=").append(context);

		URL url = new URL(callUrl.toString());
		HttpURLConnection connection = makeRequest(url, "PUT", false);
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestProperty("Content-type", "application/xml");

		try(OutputStream os = new BufferedOutputStream(connection.getOutputStream())){
			Binder.getContext().createMarshaller().marshal(container, os);
		}

		log.debug("response code is "+connection.getResponseCode());

		if (connection.getResponseCode()!=200) throw new Exception("error contacting authorization service");
		String token= "";
		try(BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream)connection.getContent()))){
			StringBuilder result = new StringBuilder();
			String line;
			while((line = reader.readLine()) != null) 
				result.append(line);
			token =  result.toString();
		}
		return Utils.addInfrastructureHashToToken(token, infrastructureHash);
	}


	@Override
	public String requestActivation(ContainerInfo container) throws Exception {

		String methodPath = "/token/node"; 

		int infrastructureHash = Utils.getInfrastructureHashFromToken(SecurityTokenProvider.instance.get(), endpoints.getDefaultInfrastructure());

		StringBuilder callUrl;

		callUrl = new StringBuilder(getInternalEnpoint(infrastructureHash)).append(methodPath);

		URL url = new URL(callUrl.toString());
		HttpURLConnection connection = makeRequest(url, "PUT", true);
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestProperty("Content-type", "application/xml");

		try(OutputStream os = new BufferedOutputStream(connection.getOutputStream())){
			Binder.getContext().createMarshaller().marshal(container, os);
		}

		log.debug("response code is "+connection.getResponseCode());

		if (connection.getResponseCode()!=200) throw new Exception("error contacting authorization service");
		String token= "";
		try(BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream)connection.getContent()))){
			StringBuilder result = new StringBuilder();
			String line;
			while((line = reader.readLine()) != null) 
				result.append(line);
			token =  result.toString();
		}
		return Utils.addInfrastructureHashToToken(token, infrastructureHash);
	}

	@Override
	public AuthorizationEntry get(String token) throws ObjectNotFound, Exception{
		String realToken = Utils.getRealToken(token);
		String maskedToken= String.format("%s********",realToken.substring(0, realToken.length()-8));
		int infrastructureHashFromToken = Utils.getInfrastructureHashFromToken(token, endpoints.getDefaultInfrastructure());
		AuthorizationEndpoint endpoint = getEndpoint(infrastructureHashFromToken);

		if (cache.containsKey(realToken) && cache.get(realToken).isValid(endpoint.getClientCacheValidity())){
			log.trace("valid entry found in cache for token {}, returning it",maskedToken);
			return cache.get(realToken).getEntry();
		} else
			log.trace("invalid entry found in cache for token {}, contacting auth service",maskedToken);

		final String methodPath = "/token/"; 

		StringBuilder callUrl = new StringBuilder(getInternalEnpoint(infrastructureHashFromToken))
		.append(methodPath).append(realToken);

		URL url = new URL(callUrl.toString());
		HttpURLConnection connection = makeRequest(url, "GET", false);
		connection.setDoInput(true);

		if (connection.getResponseCode()==404) throw new ObjectNotFound("token "+maskedToken+" not found");
		if (connection.getResponseCode()!=200) throw new Exception("error contacting authorization service (error code is "+connection.getResponseCode()+")");
		if (connection.getContentLengthLong()==0) return null;

		try(InputStream stream = (InputStream)connection.getContent();){
			AuthorizationEntry entry = (AuthorizationEntry)Binder.getContext().createUnmarshaller().unmarshal(stream);
			if (entry!=null) cache.put(realToken, new AuthorizationEntryCache(entry));
			return entry;
		}
	}


	@Override
	public void addPolicies(List<Policy> policies) throws Exception {
		final String methodPath = "/policyManager"; 

		StringBuilder callUrl = new StringBuilder(getInternalEnpoint(Utils.getInfrastructureHashFromToken(SecurityTokenProvider.instance.get(), endpoints.getDefaultInfrastructure()))).append(methodPath);

		URL url = new URL(callUrl.toString());
		HttpURLConnection connection = makeRequest(url, "POST", true);
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-type", "application/xml");

		try(OutputStream os = new BufferedOutputStream(connection.getOutputStream())){
			Binder.getContext().createMarshaller().marshal(new Policies(policies), os);
		}

		if (connection.getResponseCode()!=200) throw new Exception("error adding policies");

	}

	@Override
	public void removePolicies(long... ids) throws Exception {
		final String methodPath = "/policyManager/"; 
		StringBuilder callUrl = new StringBuilder(getInternalEnpoint(Utils.getInfrastructureHashFromToken(SecurityTokenProvider.instance.get(), endpoints.getDefaultInfrastructure()))).append(methodPath);
		List<Long> errorIds = new ArrayList<Long>();
		for (long id: ids){
			URL url = new URL(callUrl.toString()+id);
			HttpURLConnection connection = makeRequest(url, "DELETE", true);
			if (connection.getResponseCode()!=200) errorIds.add(id);
		}
		if (!errorIds.isEmpty())
			throw new Exception("error removing policies with ids: "+errorIds);
	}

	@Override
	public List<Policy> getPolicies(String context) throws Exception{
		final String methodPath = "/policyManager/"; 

		StringBuilder callUrl = new StringBuilder(getInternalEnpoint(Utils.getInfrastructureHashfromContext(context))).append(methodPath).append("?").append(CONTEXT_PARAM).append("=").append(context);

		URL url = new URL(callUrl.toString());
		HttpURLConnection connection = makeRequest(url, "GET", true);
		connection.setDoInput(true);		
		if (connection.getResponseCode()!=200){
			log.info("response code is not 200");
			throw new Exception("error retrieving policies");
		}
		if (connection.getContentLengthLong()==0)
			return Collections.emptyList();

		try(InputStreamReader stream = new InputStreamReader((InputStream)connection.getContent())){
			Policies policies = (Policies)Binder.getContext().createUnmarshaller().unmarshal(stream);
			return policies.getPolicies();
		}
	}


	@Override
	public File getSymmKey(String filePath) throws Exception{
		final String methodPath = "/symmKey/"; 

		StringBuilder callUrl = new StringBuilder(getInternalEnpoint(Utils.getInfrastructureHashFromToken(SecurityTokenProvider.instance.get(), endpoints.getDefaultInfrastructure())))
		.append(methodPath);

		URL url = new URL(callUrl.toString());
		HttpURLConnection connection = makeRequest(url, "GET", true);
		connection.setDoInput(true);		
		if (connection.getResponseCode()!=200) throw new Exception("error retrieving key");
		if (connection.getContentLengthLong()<=0) return null;

		String resourceName = (String)connection.getHeaderField("resource-name");
		File toReturnFile = new File(filePath+"/"+resourceName);
		toReturnFile.createNewFile();

		try(InputStream stream = (InputStream)connection.getContent();
				OutputStream os = new FileOutputStream(toReturnFile)){

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = stream.read(bytes)) != -1) {
				os.write(bytes, 0, read);
			}
		}
		return toReturnFile;

	}

	private HttpURLConnection makeRequest(URL url, String method, boolean includeTokenInHeader) throws Exception{
		HttpURLConnection connection;
		if (url.toString().startsWith("https://"))
			connection = (HttpsURLConnection)url.openConnection();
		else connection = (HttpURLConnection)url.openConnection();

		if (includeTokenInHeader){
			if (SecurityTokenProvider.instance.get()==null) throw new RuntimeException("null token passed");
			connection.setRequestProperty(Constants.TOKEN_HEADER_ENTRY,Utils.getRealToken(SecurityTokenProvider.instance.get()));
		}
		connection.setRequestMethod(method);
		return connection;
	}

	@Override
	public AuthorizationEndpoint getEndpoint(int infrastructureHash) {
		for (String infra: endpoints.getEndpoints().keySet()) {
			if (Utils.getInfrastructureHashfromContext(infra)==infrastructureHash)
				return endpoints.getEndpoints().get(infra);
		}
		throw new RuntimeException("Authorization Endpoint not found for the required infrastructure"); 
	}

	@Override
	public void setEndpoint(EndpointsContainer newEndpoints) {
		endpoints = newEndpoints;		
	}


}
