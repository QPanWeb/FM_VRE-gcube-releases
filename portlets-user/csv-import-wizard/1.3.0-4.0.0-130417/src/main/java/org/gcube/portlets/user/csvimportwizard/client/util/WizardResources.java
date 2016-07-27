/**
 * 
 */
package org.gcube.portlets.user.csvimportwizard.client.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * @author Federico De Faveri defaveri@isti.cnr.it
 *
 */
public interface WizardResources extends ClientBundle {
	
	public static final WizardResources INSTANCE =  GWT.create(WizardResources.class);

	@Source("org/gcube/portlets/user/csvimportwizard/client/util/resources/accept.png")
	ImageResource csvCheckSuccess();

	@Source("org/gcube/portlets/user/csvimportwizard/client/util/resources/error.png")
	ImageResource csvCheckFailure();

	@Source("org/gcube/portlets/user/csvimportwizard/client/util/resources/loading.gif")
	ImageResource loading();
	
	@Source("org/gcube/portlets/user/csvimportwizard/client/util/resources/information.png")
	ImageResource information();
	
	@Source("org/gcube/portlets/user/csvimportwizard/client/util/resources/Wizard.css")
	WizardCss wizardCss();

}
