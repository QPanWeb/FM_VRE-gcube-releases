/**
 * 
 */
package org.gcube.informationsystem.impl.entity.facet;

import java.net.URI;

import org.gcube.informationsystem.impl.entity.FacetImpl;
import org.gcube.informationsystem.model.entity.facet.CoverageFacet;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Luca Frosini (ISTI - CNR) http://www.lucafrosini.com/
 */
@JsonTypeName(value=CoverageFacet.NAME)
public class CoverageFacetImpl extends FacetImpl implements CoverageFacet {

	/**
	 * Generated Serial version UID
	 */
	private static final long serialVersionUID = -7590997566136002521L;
	
	protected String value;
	protected URI schema;

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

	/**
	 * @return the schema
	 */
	@Override
	public URI getSchema() {
		return schema;
	}

	/**
	 * @param schema the schema to set
	 */
	@Override
	public void setSchema(URI schema) {
		this.schema = schema;
	}


}
