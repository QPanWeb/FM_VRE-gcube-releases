/**
 * 
 */
package org.gcube.resourcemanagement.model.impl.entity.resource;

import org.gcube.resourcemanagement.model.reference.entity.resource.Plugin;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Luca Frosini (ISTI - CNR)
 */
@JsonTypeName(value=Plugin.NAME)
public class PluginImpl extends SoftwareImpl implements Plugin {

	/**
	 * Generated Serial version UID
	 */
	private static final long serialVersionUID = 8531011342130252545L;

}
