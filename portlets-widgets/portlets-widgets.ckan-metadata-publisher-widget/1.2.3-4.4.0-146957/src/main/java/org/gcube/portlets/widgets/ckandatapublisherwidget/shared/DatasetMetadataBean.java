package org.gcube.portlets.widgets.ckandatapublisherwidget.shared;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * This bean will contain during ckan metadata creation information related to the future build.
 * @author Costantino Perciante at ISTI-CNR (costantino.perciante@isti.cnr.it)
 */
@SuppressWarnings("serial")
public class DatasetMetadataBean implements Serializable {

	private String id;
	private String title;
	private String description;
	private String license; // chosen by the user
	private String source; // url of the folder in the workspace
	private String authorName; // author name
	private String authorSurname; // author surname
	private String authorFullName;
	private String authorEmail; // folder's email owner
	private String maintainer;
	private String maintainerEmail;
	private String ownerIdentifier; // owner of the folder into the workspace (e.g., andrea.rossi)
	private String chosenType; // the name of the MetaDataType chosen
	private String selectedOrganization;
	private long version; // version 1, 2 ...
	private boolean visibility; // Private (false) or Public(true)
	private List<OrganizationBean> organizationList; // list of organization in which the user is present and could create the dataset
	private ResourceElementBean resourceRoot; // in case of workspace, this is the directory root or the single file information
	private List<MetaDataProfileBean> metadataListTypes;
	private List<String> tags; // on retrieve, they are the keys of the product
	private List<String> tagsVocabulary; // when available
	private Map<String, List<String>> customFields;
	private List<GroupBean> groups;

	public DatasetMetadataBean(){
		super();
	}

	/** Create a metadata bean object.
	 * @param id
	 * @param title
	 * @param description
	 * @param customFields
	 * @param tags
	 * @param license
	 * @param visibility
	 * @param source
	 * @param version
	 * @param author
	 * @param authorEmail
	 * @param maintainer
	 * @param maintainerEmail
	 * @param ownerIdentifier
	 * @param organizationList
	 * @param selectedOrganization
	 * @param resourcesIds
	 * @param addResources
	 * @param metadataList
	 */
	public DatasetMetadataBean(String id, String title, String description,
			Map<String, List<String>> customFields, List<String> tags,
			String license, boolean visibility, String source, long version,
			String authorName, String authorSurname, String authorEmail, String maintainer,
			String maintainerEmail, String ownerIdentifier,
			List<OrganizationBean> organizationList, String selectedOrganization,
			ResourceElementBean resourceRoot,
			List<MetaDataProfileBean> metadataList, List<GroupBean> groups, List<String> tagsVocabulary) {
		super();
		this.id = id;
		this.title = title;
		this.description = description;
		this.customFields = customFields;
		this.tags = tags;
		this.license = license;
		this.visibility = visibility;
		this.source = source;
		this.version = version;
		this.authorName = authorName;
		this.authorSurname = authorSurname;
		this.authorEmail = authorEmail;
		this.maintainer = maintainer;
		this.maintainerEmail = maintainerEmail;
		this.ownerIdentifier = ownerIdentifier;
		this.organizationList = organizationList;
		this.selectedOrganization = selectedOrganization;
		this.resourceRoot = resourceRoot;
		this.metadataListTypes = metadataList;
		this.groups = groups;
		this.tagsVocabulary = tagsVocabulary;
	}

	public String getChosenType() {
		return chosenType;
	}

	public void setChosenType(String chosenType) {
		this.chosenType = chosenType;
	}

	public List<MetaDataProfileBean> getMetadataListTypes() {
		return metadataListTypes;
	}

	public void setMetadataListTypes(List<MetaDataProfileBean> metadataListTypes) {
		this.metadataListTypes = metadataListTypes;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOwnerIdentifier() {
		return ownerIdentifier;
	}

	public void setOwnerIdentifier(String ownerIdentifier) {
		this.ownerIdentifier = ownerIdentifier;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Map<String, List<String>> getCustomFields() {
		return customFields;
	}

	public void setCustomFields(Map<String, List<String>> customFields) {
		this.customFields = customFields;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public boolean getVisibility() {
		return visibility;
	}

	public void setVisibility(boolean visibility) {
		this.visibility = visibility;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public String getAuthorSurname() {
		return authorSurname;
	}

	public void setAuthorSurname(String authorSurname) {
		this.authorSurname = authorSurname;
	}

	public String getAuthorEmail() {
		return authorEmail;
	}

	public void setAuthorEmail(String authorEmail) {
		this.authorEmail = authorEmail;
	}

	public String getMaintainer() {
		return maintainer;
	}

	public void setMaintainer(String maintainer) {
		this.maintainer = maintainer;
	}

	public String getMaintainerEmail() {
		return maintainerEmail;
	}

	public void setMaintainerEmail(String maintainerEmail) {
		this.maintainerEmail = maintainerEmail;
	}

	public List<OrganizationBean> getOrganizationList() {
		return organizationList;
	}

	public void setOrganizationList(List<OrganizationBean> organizationList) {
		this.organizationList = organizationList;
	}

	public String getSelectedOrganization() {
		return selectedOrganization;
	}

	public void setSelectedOrganization(String selectedOrganization) {
		this.selectedOrganization = selectedOrganization;
	}

	public ResourceElementBean getResourceRoot() {
		return resourceRoot;
	}

	public void setResourceRoot(ResourceElementBean resourceRoot) {
		this.resourceRoot = resourceRoot;
	}

	public String getAuthorFullName() {
		return authorFullName;
	}

	public void setAuthorFullName(String authorFullName) {
		this.authorFullName = authorFullName;
	}

	public List<GroupBean> getGroups() {
		return groups;
	}

	public void setGroups(List<GroupBean> groups) {
		this.groups = groups;
	}

	public List<String> getTagsVocabulary() {
		return tagsVocabulary;
	}

	public void setTagsVocabulary(List<String> tagsVocabulary) {
		this.tagsVocabulary = tagsVocabulary;
	}

	@Override
	public String toString() {
		return "DatasetMetadataBean [id=" + id + ", title=" + title
				+ ", description=" + description + ", license=" + license
				+ ", source=" + source + ", authorName=" + authorName
				+ ", authorSurname=" + authorSurname + ", authorFullName="
				+ authorFullName + ", authorEmail=" + authorEmail
				+ ", maintainer=" + maintainer + ", maintainerEmail="
				+ maintainerEmail + ", ownerIdentifier=" + ownerIdentifier
				+ ", chosenType=" + chosenType + ", selectedOrganization="
				+ selectedOrganization + ", version=" + version
				+ ", visibility=" + visibility + ", organizationList="
				+ organizationList + ", resourceRoot=" + resourceRoot
				+ ", metadataListTypes=" + metadataListTypes + ", tags=" + tags
				+ ", tagsVocabulary=" + tagsVocabulary + ", customFields="
				+ customFields + ", groups=" + groups + "]";
	}

}