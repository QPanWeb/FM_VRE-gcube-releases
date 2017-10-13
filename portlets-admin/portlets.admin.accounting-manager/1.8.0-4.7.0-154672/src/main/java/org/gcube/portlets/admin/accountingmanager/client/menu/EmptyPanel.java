package org.gcube.portlets.admin.accountingmanager.client.menu;

import com.sencha.gxt.widget.core.client.ContentPanel;

/**
 * 
 * @author giancarlo
 * email: <a href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a> 
 *
 */
public class EmptyPanel extends ContentPanel {
	
	private String id;
	
	public EmptyPanel(String id){
		this.id=id;
		init();
	}
	
	private void init(){
		setId(id);
		setWidth(0);
		setHeight(0);
		setBodyBorder(false);
		setBorders(false);
		setHeaderVisible(false);
		setVisible(false);
		
	}

	public String getId() {
		return id;
	}
	
	
	

}
