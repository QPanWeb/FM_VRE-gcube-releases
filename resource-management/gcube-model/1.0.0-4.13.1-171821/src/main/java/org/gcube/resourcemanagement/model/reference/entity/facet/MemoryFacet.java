/**
 * 
 */
package org.gcube.resourcemanagement.model.reference.entity.facet;

import org.gcube.informationsystem.model.reference.annotations.ISProperty;
import org.gcube.informationsystem.model.reference.entity.Facet;
import org.gcube.resourcemanagement.model.impl.entity.facet.MemoryFacetImpl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author Luca Frosini (ISTI - CNR)
 * https://wiki.gcube-system.org/gcube/Facet_Based_Resource_Model#Memory_Facet
 */
@JsonDeserialize(as=MemoryFacetImpl.class)
public interface MemoryFacet extends Facet {
	
	public static final String NAME = "MemoryFacet"; // MemoryFacet.class.getSimpleName();
	public static final String DESCRIPTION = "Memory information";
	public static final String VERSION = "1.0.0";
	
	public enum MemoryUnit {
		Byte, kB, MB, GB, TB, PB, EB, ZB, YB
	}
	
	@ISProperty
	public long getSize();
	
	public void setSize(long size);
	
	@ISProperty
	public long getUsed();
	
	public void setUsed(long used);
	
	@ISProperty
	public MemoryUnit getUnit();
	
	public void setUnit(MemoryUnit unit);
}
