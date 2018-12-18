/**
 * 
 */
package org.gcube.resourcemanagement.model.impl.entity.facet;

import java.net.URI;

import org.gcube.informationsystem.model.impl.entity.FacetImpl;
import org.gcube.informationsystem.model.reference.embedded.ValueSchema;
import org.gcube.resourcemanagement.model.reference.entity.facet.AccessPointFacet;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Luca Frosini (ISTI - CNR)
 */
@JsonTypeName(value=AccessPointFacet.NAME)
public class AccessPointFacetImpl extends FacetImpl implements AccessPointFacet {
	
	/**
	 * Generated Serial version UID
	 */
	private static final long serialVersionUID = -2780577881844464254L;
	
	protected String entryName;
	protected URI endpoint;
	protected String protocol;
	protected String description;
	protected ValueSchema authorization;
	
	@Override
	public String getEntryName() {
		return this.entryName;
	}

	@Override
	public void setEntryName(String entryName) {
		this.entryName = entryName;
	}

	@Override
	public URI getEndpoint() {
		return this.endpoint;
	}

	@Override
	public void setEndpoint(URI endpoint) {
		this.endpoint = endpoint;
	}

	@Override
	public String getProtocol() {
		return this.protocol;
	}

	@Override
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public ValueSchema getAuthorization() {
		return this.authorization;
	}

	@Override
	public void setAuthorization(ValueSchema authorization) {
		this.authorization = authorization;
	}

}
