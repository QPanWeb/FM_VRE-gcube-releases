/**
 * 
 */
package org.gcube.informationsystem.model.embedded;

import org.gcube.informationsystem.impl.embedded.PropagationConstraintImpl;
import org.gcube.informationsystem.model.annotations.ISProperty;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author Luca Frosini (ISTI - CNR)
 * https://wiki.gcube-system.org/gcube/Facet_Based_Resource_Model#Propagation_Constraint
 */
@JsonDeserialize(as=PropagationConstraintImpl.class)
public interface PropagationConstraint extends Embedded {

	public static final String NAME = "PropagationConstraint"; //RelationProperty.class.getSimpleName();

	public static final String REMOVE_PROPERTY = "remove";
	public static final String ADD_PROPERTY = "add";
	
	public enum RemoveConstraint {
		
		/**
		 * When the source {@link Entity} is removed also the target
		 * {@link Entity} is removed but if and only if the latter has no other
		 * incoming {@link Relation}.
		 */
		cascadeWhenOrphan,
		
		/**
		 * When the source {@link Entity} is removed also the target
		 * {@link Entity} is removed.
		 */
		cascade, 
		
		/**
		 * When the source {@link Entity} is removed the target {@link Entity} 
		 * is keep.
		 */
		keep
		
	}
	
	public enum AddConstraint {
		
		propagate,
		
		unpropagate
			
	}
	
	@ISProperty(name=REMOVE_PROPERTY)
	public RemoveConstraint getRemoveConstraint();
	
	public void setRemoveConstraint(RemoveConstraint removeConstraint);
	
	
	@ISProperty(name=ADD_PROPERTY)
	public AddConstraint getAddConstraint();
	
	public void setAddConstraint(AddConstraint addConstraint);
	
	
	
}
