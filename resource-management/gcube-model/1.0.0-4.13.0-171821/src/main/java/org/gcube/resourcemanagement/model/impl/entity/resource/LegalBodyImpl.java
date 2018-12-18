/**
 * 
 */
package org.gcube.resourcemanagement.model.impl.entity.resource;

import org.gcube.resourcemanagement.model.reference.entity.resource.LegalBody;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Luca Frosini (ISTI - CNR)
 */
@JsonTypeName(value=LegalBody.NAME)
public class LegalBodyImpl extends ActorImpl implements LegalBody {

	/**
	 * Generated Serial version UID
	 */
	private static final long serialVersionUID = 6879797086260765029L;
	
}
