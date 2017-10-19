/**
 * 
 */
package org.gcube.informationsystem.impl;

import org.gcube.informationsystem.model.ER;
import org.gcube.informationsystem.model.embedded.Header;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Luca Frosini (ISTI - CNR)
 */
@JsonTypeName(value=ER.NAME)
public abstract class ERImpl extends ISManageableImpl implements ER {
	
	protected Header header;
	
	public ERImpl(){
		super();
	}
	
	@Override
	public Header getHeader() {
		return header;
	}
	
	@Override
	public void setHeader(Header header){
		this.header = header;
	}
}
