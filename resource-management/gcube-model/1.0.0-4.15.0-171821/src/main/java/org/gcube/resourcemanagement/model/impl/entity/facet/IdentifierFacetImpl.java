/**
 * 
 */
package org.gcube.resourcemanagement.model.impl.entity.facet;

import org.gcube.informationsystem.model.impl.entity.FacetImpl;
import org.gcube.resourcemanagement.model.reference.entity.facet.IdentifierFacet;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Luca Frosini (ISTI - CNR)
 */
@JsonTypeName(value=IdentifierFacet.NAME)
public class IdentifierFacetImpl extends FacetImpl implements IdentifierFacet {

	/**
	 * Generated Serial version UID
	 */
	private static final long serialVersionUID = 5711870008624673728L;
	
	protected String value;
	protected IdentificationType type;
	protected boolean persistent;
	
	/**
	 * @return the value
	 */
	@Override
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the type
	 */
	@Override
	public IdentificationType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(IdentificationType type) {
		this.type = type;
	}

	/**
	 * @return the persistent
	 */
	@Override
	public boolean isPersistent() {
		return persistent;
	}

	/**
	 * @param persistent the persistent to set
	 */
	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

}
