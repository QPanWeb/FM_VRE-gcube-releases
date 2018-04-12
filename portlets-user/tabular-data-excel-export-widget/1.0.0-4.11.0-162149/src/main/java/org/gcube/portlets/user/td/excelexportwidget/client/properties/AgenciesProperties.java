/**
 * 
 */
package org.gcube.portlets.user.td.excelexportwidget.client.properties;



import org.gcube.portlets.user.td.gwtservice.shared.tr.type.Agencies;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

/**
 * 
 * @author Giancarlo Panichi 
 * 
 *
 */
public interface AgenciesProperties extends PropertyAccess<Agencies> {
	
	@Path("id")
	ModelKeyProvider<Agencies> key();
	LabelProvider<Agencies> nameLabel();
	
	ValueProvider<Agencies, String> id();
	ValueProvider<Agencies, String> name();
	ValueProvider<Agencies, String> description();
	
	

}
