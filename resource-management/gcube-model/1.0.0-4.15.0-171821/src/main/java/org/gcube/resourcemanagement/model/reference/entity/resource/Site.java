/**
 * 
 */
package org.gcube.resourcemanagement.model.reference.entity.resource;

import org.gcube.informationsystem.model.reference.entity.Resource;
import org.gcube.resourcemanagement.model.impl.entity.resource.SiteImpl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author Luca Frosini (ISTI - CNR)
 * https://wiki.gcube-system.org/gcube/Facet_Based_Resource_Model#Site
 */
@JsonDeserialize(as=SiteImpl.class)
public interface Site extends Resource {

	public static final String NAME = "Site"; // Site.class.getSimpleName();
	public static final String DESCRIPTION = "Collect Site information through the list of its facets";
	public static final String VERSION = "1.0.0";
	
}
