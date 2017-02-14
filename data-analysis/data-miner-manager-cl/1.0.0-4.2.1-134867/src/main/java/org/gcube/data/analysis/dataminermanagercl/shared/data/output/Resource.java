/**
 * 
 */
package org.gcube.data.analysis.dataminermanagercl.shared.data.output;

import java.io.Serializable;

/**
 * 
 * @author Giancarlo Panichi
 * email: <a href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a> 
 *
 */
public class Resource implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1417885805472591661L;

	public enum ResourceType {
		OBJECT, FILE, TABULAR, MAP, IMAGE
	};

	private String resourceId, name, description;
	private ResourceType resourceType;

	/**
	 * 
	 */
	public Resource() {
		super();
	}

	/**
	 * 
	 * @param resourceId
	 * @param name
	 * @param description
	 * @param resourceType
	 */
	public Resource(String resourceId, String name, String description,
			ResourceType resourceType) {
		super();
		this.resourceId = resourceId;
		this.name = name;
		this.description = description;
		this.resourceType = resourceType;
	}

	/**
	 * @return the resourceId
	 */
	public String getResourceId() {
		return resourceId;
	}

	/**
	 * @param resourceId
	 *            the resourceId to set
	 */
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the resourceType
	 */
	public ResourceType getResourceType() {
		return resourceType;
	}

	/**
	 * @param resourceType
	 *            the resourceType to set
	 */
	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}

	public boolean isTabular() {
		return this.resourceType == ResourceType.TABULAR;
	}

	public boolean isObject() {
		return this.resourceType == ResourceType.OBJECT;
	}

	public boolean isFile() {
		return this.resourceType == ResourceType.FILE;
	}

	public boolean isMap() {
		return this.resourceType == ResourceType.MAP;
	}

	public boolean isImages() {
		return this.resourceType == ResourceType.IMAGE;
	}

	

	@Override
	public String toString() {
		return "Resource [resourceId=" + resourceId + ", name=" + name
				+ ", description=" + description + ", resourceType="
				+ resourceType + "]";
	}

}
