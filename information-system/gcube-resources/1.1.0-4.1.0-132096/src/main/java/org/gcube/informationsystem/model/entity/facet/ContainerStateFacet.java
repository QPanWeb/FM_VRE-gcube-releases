/**
 * 
 */
package org.gcube.informationsystem.model.entity.facet;

import org.gcube.informationsystem.impl.entity.facet.ContainerStateFacetImpl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;


/**
 * @author Luca Frosini (ISTI - CNR) http://www.lucafrosini.com/
 * https://wiki.gcube-system.org/gcube/Facet_Based_Resource_Model#Container_State_Facet
 */
@JsonDeserialize(as=ContainerStateFacetImpl.class)
public interface ContainerStateFacet extends StateFacet {

	public static final String NAME = "ContainerStateFacet"; // ContainerStateFacet.class.getSimpleName();
	public static final String DESCRIPTION = "Container State Information";
	public static final String VERSION = "1.0.0";
	
}
