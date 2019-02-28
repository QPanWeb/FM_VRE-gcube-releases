/**
 * 
 */
package org.gcube.resourcemanagement.model.reference.relation.consistsof;

import org.gcube.informationsystem.model.reference.entity.Resource;
import org.gcube.resourcemanagement.model.impl.relation.consistsof.HasCuratorImpl;
import org.gcube.resourcemanagement.model.reference.entity.facet.ContactFacet;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author Luca Frosini (ISTI - CNR)
 * https://wiki.gcube-system.org/gcube/Facet_Based_Resource_Model#hasCurator
 */
@JsonDeserialize(as=HasCuratorImpl.class)
public interface HasCurator<Out extends Resource, In extends ContactFacet> 
	extends HasContact<Out, In> {

	public static final String NAME = "HasCurator"; // HasCurator.class.getSimpleName();
}
