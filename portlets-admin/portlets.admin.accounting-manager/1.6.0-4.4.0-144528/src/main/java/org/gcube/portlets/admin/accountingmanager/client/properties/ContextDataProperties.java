package org.gcube.portlets.admin.accountingmanager.client.properties;

import org.gcube.portlets.admin.accountingmanager.shared.data.ContextData;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

/**
 * 
 * @author Giancarlo Panichi email: <a
 *         href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a>
 *
 */
public interface ContextDataProperties extends PropertyAccess<ContextData> {
	@Path("contextData")
	ModelKeyProvider<org.gcube.portlets.admin.accountingmanager.shared.data.ContextData> id();

	ValueProvider<ContextData, String> label();
}
