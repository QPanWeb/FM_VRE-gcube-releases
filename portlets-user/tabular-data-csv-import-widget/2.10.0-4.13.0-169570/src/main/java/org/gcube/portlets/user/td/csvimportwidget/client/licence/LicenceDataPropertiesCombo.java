package org.gcube.portlets.user.td.csvimportwidget.client.licence;

import org.gcube.portlets.user.td.gwtservice.shared.licenses.LicenceData;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;


/**
 * 
 * @author "Giancarlo Panichi" 
 *  
 *
 */
public interface LicenceDataPropertiesCombo extends
		PropertyAccess<LicenceData> {
	
	@Path("id")
	ModelKeyProvider<LicenceData> id();
	
	LabelProvider<LicenceData> licenceName();
	

}