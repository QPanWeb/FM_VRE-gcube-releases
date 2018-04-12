/**
 * 
 */
package org.gcube.informationsystem.model.entity.facet;

import org.gcube.informationsystem.impl.entity.facet.SimpleFacetImpl;
import org.gcube.informationsystem.model.entity.Facet;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author Luca Frosini (ISTI - CNR)
 * https://wiki.gcube-system.org/gcube/Facet_Based_Resource_Model#Simple_Property_Facet
 */
@JsonDeserialize(as=SimpleFacetImpl.class)
public interface SimpleFacet extends Facet {
	
	public static final String NAME = "SimpleFacet"; // SimpleFacet.class.getSimpleName();
	public static final String DESCRIPTION = "A sort of catch all";
	public static final String VERSION = "1.0.0";
	
}
