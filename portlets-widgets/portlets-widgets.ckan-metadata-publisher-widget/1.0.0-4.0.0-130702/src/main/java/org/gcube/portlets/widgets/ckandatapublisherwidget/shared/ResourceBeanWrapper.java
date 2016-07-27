package org.gcube.portlets.widgets.ckandatapublisherwidget.shared;

import java.io.Serializable;

/**
 * A dataset's resource bean
 * @author Costantino Perciante at ISTI-CNR (costantino.perciante@isti.cnr.it)
 */
public class ResourceBeanWrapper implements Serializable{

	private static final long serialVersionUID = -6542455246456049712L;
	private String url;
	private String name;
	private String description;
	private String id;
	private boolean toBeAdded;
	private String mimeType;
	private String owner;
	private String organizationNameDatasetParent; // the organization name in which the parent dataset was created
	
	public ResourceBeanWrapper(){
		super();
	}
	
	/**
	 * @param url
	 * @param name
	 * @param description
	 * @param id
	 * @param toBeAdded
	 * @param mimeType
	 * @param owner
	 */
	public ResourceBeanWrapper(String url, String name, String description,
			String id, boolean toBeAdded, String mimeType, String owner, String organizationNameDatasetParent) {
		super();
		this.url = url;
		this.name = name;
		this.description = description;
		this.id = id;
		this.toBeAdded = toBeAdded;
		this.mimeType = mimeType;
		this.owner = owner;
		this.organizationNameDatasetParent = organizationNameDatasetParent;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
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
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the toBeAdded
	 */
	public boolean isToBeAdded() {
		return toBeAdded;
	}

	/**
	 * @param toBeAdded the toBeAdded to set
	 */
	public void setToBeAdded(boolean toBeAdded) {
		this.toBeAdded = toBeAdded;
	}

	/**
	 * @return the mimeType
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * @param mimeType the mimeType to set
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @param owner the owner to set
	 */
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * @return the organizationNameDatasetParent
	 */
	public String getOrganizationNameDatasetParent() {
		return organizationNameDatasetParent;
	}

	/**
	 * @param organizationNameDatasetParent the organizationNameDatasetParent to set
	 */
	public void setOrganizationNameDatasetParent(
			String organizationNameDatasetParent) {
		this.organizationNameDatasetParent = organizationNameDatasetParent;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ResourceBeanWrapper [url=" + url + ", name=" + name
				+ ", description=" + description + ", id=" + id
				+ ", toBeAdded=" + toBeAdded + ", mimeType=" + mimeType
				+ ", owner=" + owner + ", organizationNameDatasetParent="
				+ organizationNameDatasetParent + "]";
	}
}
