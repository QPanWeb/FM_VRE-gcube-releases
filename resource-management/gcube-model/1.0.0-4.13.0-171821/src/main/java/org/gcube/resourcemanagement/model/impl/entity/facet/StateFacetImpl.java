/**
 * 
 */
package org.gcube.resourcemanagement.model.impl.entity.facet;

import org.gcube.informationsystem.model.impl.entity.FacetImpl;
import org.gcube.resourcemanagement.model.reference.entity.facet.StateFacet;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Luca Frosini (ISTI - CNR)
 */
@JsonTypeName(value=StateFacet.NAME)
public class StateFacetImpl extends FacetImpl implements StateFacet {
	
	/**
	 * Generated Serial version UID
	 */
	private static final long serialVersionUID = -6149286208701421212L;
	
	protected String value;
	
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
	@Override
	public void setValue(String value) {
		this.value = value;
	}

}
