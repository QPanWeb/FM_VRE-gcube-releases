/**
 * 
 */
package org.gcube.informationsystem.impl.embedded;

import org.gcube.informationsystem.model.embedded.AccessPolicy;
import org.gcube.informationsystem.model.embedded.ValueSchema;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Luca Frosini (ISTI - CNR) http://www.lucafrosini.com/
 */
@JsonTypeName(value=AccessPolicy.NAME)
public class AccessPolicyImpl implements AccessPolicy {

	/**
	 * Generated Serial Version UID
	 */
	private static final long serialVersionUID = -531371754061410537L;

	protected ValueSchema policy;
	
	protected String note;
	
	@Override
	public ValueSchema getPolicy() {
		return this.policy;
	}

	@Override
	public void setPolicy(ValueSchema policy) {
		this.policy = policy;
	}

	@Override
	public String getNote() {
		return this.note;
	}

	@Override
	public void setNote(String note) {
		this.note = note;
	}

}
