/**
 * 
 */
package org.gcube.resourcemanagement.model.reference.entity.facet;

import java.net.URI;
import java.util.Date;

import org.gcube.informationsystem.model.reference.ISConstants;
import org.gcube.informationsystem.model.reference.annotations.ISProperty;
import org.gcube.informationsystem.model.reference.entity.Facet;
import org.gcube.resourcemanagement.model.impl.entity.facet.EventFacetImpl;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author Luca Frosini (ISTI - CNR)
 * https://wiki.gcube-system.org/gcube/Facet_Based_Resource_Model#Event_Facet
 */
@JsonDeserialize(as=EventFacetImpl.class)
public interface EventFacet extends Facet {

	public static final String NAME = "EventFacet"; // EventFacet.class.getSimpleName();
	public static final String DESCRIPTION = "Event Facet";
	public static final String VERSION = "1.0.0";
	
	@JsonFormat(shape= JsonFormat.Shape.STRING, pattern = ISConstants.DATETIME_PATTERN)
	@ISProperty(mandatory=true, nullable=false)
	public Date getDate();
	
	public void setDate(Date date);
	
	@ISProperty(mandatory=true, nullable=false)
	public String getValue();
	
	public void setValue(String value);

	@ISProperty
	public URI getSchema();
	
	public void setSchema(URI schema);
}
