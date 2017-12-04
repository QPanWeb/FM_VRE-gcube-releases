package org.gcube.portlets.user.td.csvimportwidget.client.csvgrid;

import com.google.gwt.i18n.client.Messages;

/**
 * 
 * @author giancarlo
 * email: <a href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a> 
 *
 */
public interface CSVGridMessages extends Messages {

	@DefaultMessage("No data")
	String noData();
	
	@DefaultMessage("Error creating the store: {0}")
	String errorCreatingTheStore(String localizedMessage);
	
	@DefaultMessage("Include")
	String itmInclude();
	
	@DefaultMessage("Exclude")
	String itmExclude();

}
