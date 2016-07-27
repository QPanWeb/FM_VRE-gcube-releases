package org.gcube.portlets.user.td.tablewidget.client.validation.tree;

import java.io.Serializable;

/**
 * 
 * @author giancarlo
 * email: <a href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a> 
 *
 */
public class BaseDto implements Serializable {

	private static final long serialVersionUID = -5535466371215737037L;
	protected String id;

	public BaseDto(){
		
	}
	
	public BaseDto(String id){
		this.id=id;
	}
	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "BaseDto [id=" + id + "]";
	}
	
	

}
