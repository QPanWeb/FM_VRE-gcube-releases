/**
 * 
 */
package org.gcube.informationsystem.model.entity.facet;

import java.net.URL;

import org.gcube.informationsystem.impl.entity.facet.SchemaFacetImpl;
import org.gcube.informationsystem.model.annotations.ISProperty;
import org.gcube.informationsystem.model.annotations.Key;
import org.gcube.informationsystem.model.entity.Facet;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author Luca Frosini (ISTI - CNR) http://www.lucafrosini.com/
 * https://wiki.gcube-system.org/gcube/Facet_Based_Resource_Model#Schema_Facet
 */
@Key(fields={SchemaFacet.NAME_PROPERTY})
@JsonDeserialize(as=SchemaFacetImpl.class)
public interface SchemaFacet extends Facet {
	
	public static final String NAME = "SchemaFacet"; // SchemaFacet.class.getSimpleName();
	public static final String DESCRIPTION = "Provide a way to store a Schema";
	public static final String VERSION = "1.0.0";
	
	public static final String NAME_PROPERTY = "name";
	
	@ISProperty(name=NAME_PROPERTY, mandatory=true, nullable=false)
	public String getName();
	
	public void setName(String name);
	
	@ISProperty(mandatory=true, nullable=false)
	public String getDescription();
	
	public void setDescription(String description);
	
	@ISProperty
	public URL getSchemaURL();
	
	public void setSchemaURL(URL schemaURL);
	
}
