/**
 * 
 */
package org.gcube.resourcemanagement.model.reference.entity.resource;

import org.gcube.informationsystem.model.reference.entity.Resource;
import org.gcube.resourcemanagement.model.impl.entity.resource.DatasetImpl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author Luca Frosini (ISTI - CNR)
 * https://wiki.gcube-system.org/gcube/Facet_Based_Resource_Model#Dataset
 */
@JsonDeserialize(as=DatasetImpl.class)
public interface Dataset extends Resource {

	public static final String NAME = "Dataset"; // Dataset.class.getSimpleName();
	public static final String DESCRIPTION = "Collect Dataset information through the list of its facets";
	public static final String VERSION = "1.0.0";
	
}
