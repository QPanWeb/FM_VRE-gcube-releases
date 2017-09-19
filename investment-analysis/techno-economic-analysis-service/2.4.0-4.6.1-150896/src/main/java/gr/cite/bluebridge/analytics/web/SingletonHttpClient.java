package gr.cite.bluebridge.analytics.web;

import java.net.SocketTimeoutException;
import java.util.Map;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class SingletonHttpClient{
	
	private static Client singletonHttpClient;		

	private static Integer HTTP_CONNECTION_TIMEOUT;	
	
	public static Client getSingletonHttpClient(){
		return singletonHttpClient == null ? singletonHttpClient = ClientBuilder.newClient() : singletonHttpClient;
	}	
	
	public Response doGet(String serviceUrl, Map<String, Object> headers){
		Client client = SingletonHttpClient.getSingletonHttpClient();
		client.property(ClientProperties.CONNECT_TIMEOUT, HTTP_CONNECTION_TIMEOUT);
		client.register(JacksonFeature.class);
		
		WebTarget webTarget = client.target(serviceUrl);
		Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON);
		
		builder.accept(MediaType.APPLICATION_JSON);			
		for(Map.Entry<String, Object> entry : headers.entrySet()){
			builder.header(entry.getKey(), entry.getValue());
		}		

		Response response = builder.get();
		
		return response;
	}
	
	public Response doPost(String serviceUrl, Map<String, Object> headers, Object bodyObject){		
		Client client = SingletonHttpClient.getSingletonHttpClient();
		client.property(ClientProperties.CONNECT_TIMEOUT, HTTP_CONNECTION_TIMEOUT);
		client.register(JacksonFeature.class);
		
		WebTarget webTarget = client.target(serviceUrl);
		Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON);
		
		builder.accept(MediaType.APPLICATION_JSON);			
		for(Map.Entry<String, Object> entry : headers.entrySet()){
			builder.header(entry.getKey(), entry.getValue());
		}
		
		ObjectMapper mapper = new ObjectMapper();
		String body = null;
		
		try {
			body = mapper.writeValueAsString(bodyObject);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		Invocation invocation = builder.buildPost(Entity.entity(body, MediaType.APPLICATION_JSON_TYPE));
		Response response = invocation.invoke();
		
		return response;
	}
	
	public Integer exceptionHandler(Exception e){
		if(e instanceof BadRequestException){						
			return 400;										// 400	BAD_REQUEST				
		}else if(e instanceof NotAuthorizedException){				
			return 401;										// 401	UNAUTHORIZED				
		}else if(e instanceof ForbiddenException){					
			return 403;										// 403	FORBIDDEN			
		}else if(e instanceof NotFoundException){	
			return 404;										// 404	NOT_FOUND
		}else if(e instanceof NotAllowedException){
			return 405; 									// 405	METHOD_NOT_ALLOWED
		}else if(e instanceof NotAcceptableException){
			return 406;										// 406	NOT_ACCEPTABLE
		}else if(e instanceof NotSupportedException){
			return 415; 									// 415	UNSUPPORTED_MEDIA_TYPE
		}else if(e instanceof InternalServerErrorException){
			return 500;										// 500	INTERNAL_SERVER_ERROR
		}else if(e instanceof ServiceUnavailableException){
			return 503;										// 503	SERVICE_UNAVAILABLE
		}else if(e instanceof SocketTimeoutException){		
			return 504;										// 504	GATEWAY_TIMEOUT
		}else{														
			return 500;										// 500  For unexpected exceptions
		}	
	}
	
	public Integer getHTTP_CONNECTION_TIMEOUT() {
		return HTTP_CONNECTION_TIMEOUT;
	}

	public void setHTTP_CONNECTION_TIMEOUT(Integer httpConnectionTimeout) {
		HTTP_CONNECTION_TIMEOUT = httpConnectionTimeout;
	}
}