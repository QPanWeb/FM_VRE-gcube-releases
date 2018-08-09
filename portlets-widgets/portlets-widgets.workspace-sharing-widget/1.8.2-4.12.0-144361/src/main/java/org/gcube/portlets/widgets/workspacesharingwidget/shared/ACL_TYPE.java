/**
 * 
 */
package org.gcube.portlets.widgets.workspacesharingwidget.shared;

/**
 * @author Francesco Mangiacrapa francesco.mangiacrapa@isti.cnr.it
 * @Feb 27, 2014
 *
 */
public enum ACL_TYPE {
	
	READ_ONLY, // "Users can read any file but cannot update/delete"
	WRITE_OWNER, //"Users can update/delete only their files"
	WRITE_ANY; //"Any user can update/delete any file"

}
