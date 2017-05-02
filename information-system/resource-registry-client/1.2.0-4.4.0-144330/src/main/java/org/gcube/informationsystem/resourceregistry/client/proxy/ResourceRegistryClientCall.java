package org.gcube.informationsystem.resourceregistry.client.proxy;

import java.io.IOException;
import java.net.URL;

import javax.xml.ws.EndpointReference;

import org.gcube.common.clients.Call;
import org.gcube.informationsystem.resourceregistry.api.rest.httputils.HTTPCall;

public class ResourceRegistryClientCall<C> implements Call<EndpointReference, C> {
	
	protected final Class<C> clazz;
	protected final HTTPCall<C> httpCall;

	public ResourceRegistryClientCall(Class<C> clazz, HTTPCall<C> httpCall) {
		this.clazz = clazz;
		this.httpCall = httpCall;
	}

	protected String getURLStringFromEndpointReference(
			EndpointReference endpoint) throws IOException {
		JaxRSEndpointReference jaxRSEndpointReference = new JaxRSEndpointReference(
				endpoint);
		return jaxRSEndpointReference.toString();
	}

	@Override
	public C call(EndpointReference endpoint) throws Exception {
		String urlFromEndpointReference = getURLStringFromEndpointReference(endpoint);
		StringBuilder callUrl = new StringBuilder(urlFromEndpointReference);
		callUrl.append(httpCall.getPath());
		URL url = new URL(callUrl.toString());
		return httpCall.call(clazz, url, ResourceRegistryClient.class.getSimpleName());
	}

}
