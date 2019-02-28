/**
 * 
 */
package org.gcube.portlets.user.workspace.client.workspace;

import java.io.Serializable;

/**
 * @author Federico De Faveri defaveri@isti.cnr.it
 *
 */
public interface GWTItemDescription extends Serializable {
	
	public String getIconClass() ;
	public String getLabel();
}
