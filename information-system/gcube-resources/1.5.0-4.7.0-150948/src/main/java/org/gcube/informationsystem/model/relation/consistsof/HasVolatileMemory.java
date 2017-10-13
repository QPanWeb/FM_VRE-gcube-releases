/**
 * 
 */
package org.gcube.informationsystem.model.relation.consistsof;

import org.gcube.informationsystem.impl.relation.consistsof.HasVolatileMemoryImpl;
import org.gcube.informationsystem.model.entity.Resource;
import org.gcube.informationsystem.model.entity.facet.MemoryFacet;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author Luca Frosini (ISTI - CNR)
 * https://wiki.gcube-system.org/gcube/Facet_Based_Resource_Model#hasVolatileMemory
 */
@JsonDeserialize(as=HasVolatileMemoryImpl.class)
public interface HasVolatileMemory<Out extends Resource, In extends MemoryFacet> 
	extends HasMemory<Out, In> {

	public static final String NAME = "HasVolatileMemory"; // HasVolatileMemory.class.getSimpleName();
}
