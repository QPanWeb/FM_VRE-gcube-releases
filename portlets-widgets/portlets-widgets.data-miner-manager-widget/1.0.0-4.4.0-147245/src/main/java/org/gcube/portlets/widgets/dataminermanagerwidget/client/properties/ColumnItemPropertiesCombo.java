package org.gcube.portlets.widgets.dataminermanagerwidget.client.properties;


import org.gcube.data.analysis.dataminermanagercl.shared.data.ColumnItem;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

/**
 * 
 * @author Giancarlo Panichi
 * email: <a href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a> 
 *
 */
public interface ColumnItemPropertiesCombo extends PropertyAccess<ColumnItem> {

	@Path("id")
	ModelKeyProvider<ColumnItem> id();

	LabelProvider<ColumnItem> label();

}