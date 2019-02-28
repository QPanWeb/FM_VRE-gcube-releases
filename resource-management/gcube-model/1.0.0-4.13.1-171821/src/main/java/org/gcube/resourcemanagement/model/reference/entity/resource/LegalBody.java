/**
 * 
 */
package org.gcube.resourcemanagement.model.reference.entity.resource;

import org.gcube.resourcemanagement.model.impl.entity.resource.LegalBodyImpl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;


/**
 * @author Luca Frosini (ISTI - CNR)
 * https://wiki.gcube-system.org/gcube/Facet_Based_Resource_Model#Legal_Body
 */
@JsonDeserialize(as=LegalBodyImpl.class)
public interface LegalBody extends Actor {

	public static final String NAME = "LegalBody"; // LegalBody.class.getSimpleName();
	public static final String DESCRIPTION = "Actor";
	public static final String VERSION = "1.0.0";
		
}
