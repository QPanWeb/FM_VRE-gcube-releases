package org.gcube.portlets.user.td.widgetcommonevent.shared.dataview;

import java.io.Serializable;

/**
 * 
 * @author giancarlo
 * email: <a href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a> 
 *
 */
public class DataView implements Serializable {

	private static final long serialVersionUID = 2683307379197083971L;
	
	
	protected DataViewType dataViewType;

	public DataView(){
	
	}
	
	public DataView(DataViewType dataViewType){
		this.dataViewType=dataViewType;
	}
	
	
	
	public DataViewType getDataViewType() {
		return dataViewType;
	}


	public void setDataViewType(DataViewType dataViewType) {
		this.dataViewType = dataViewType;
	}


	@Override
	public String toString() {
		return "DataView [dataViewType=" + dataViewType + "]";
	}
	
	
	
}
