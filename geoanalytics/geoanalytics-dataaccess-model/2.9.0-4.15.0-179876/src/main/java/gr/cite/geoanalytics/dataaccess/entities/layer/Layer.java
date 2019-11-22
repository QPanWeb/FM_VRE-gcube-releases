package gr.cite.geoanalytics.dataaccess.entities.layer;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.*;

import org.hibernate.annotations.Type;

import gr.cite.geoanalytics.dataaccess.entities.Identifiable;
import gr.cite.geoanalytics.dataaccess.entities.Stampable;
import gr.cite.geoanalytics.dataaccess.entities.geocode.GeocodeSystem;
import gr.cite.geoanalytics.dataaccess.entities.principal.Principal;
import gr.cite.geoanalytics.dataaccess.entities.project.ProjectLayer;

@Entity
@Table(name = "\"Layer\"")
public class Layer implements gr.cite.geoanalytics.dataaccess.entities.Entity, Identifiable, Stampable, Serializable {

	private static final long serialVersionUID = -403566445767699950L;

	@Id
	@Type(type = "org.hibernate.type.PostgresUUIDType") // DEPWARN dependency to Hibernate and PostgreSQL
	@Column(name = "\"L_ID\"", nullable = false)
	private UUID id = null;

	@Column(name = "\"L_Name\"", nullable = true, length = 200)
	private String name = null;

	@Column(name = "\"L_Workspace\"", nullable = true, length = 200)
	private String workspace = null;

	@Enumerated(EnumType.STRING)
	@Column(name = "\"L_DataSource\"", nullable = true, length = 100)
	private DataSource dataSource = null;

	@Column(name = "\"L_Description\"", nullable = true)
	private String description = null;

	@Column(name = "\"L_ExternalGeoserverUrl\"", nullable = true)
	private String externalGeoserverUrl = null;

	@Type(type = "gr.cite.geoanalytics.dataaccess.typedefinition.XMLType")
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "\"L_ExtraData\"", columnDefinition = "xml") // DEPWARN possible db portability issue
	private String extraData;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "\"L_CreationDate\"", nullable = false)
	private Date creationDate = null;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "\"L_EditDate\"", nullable = false)
	private Date lastUpdate = null;

	@Column(name = "\"L_RepFactor\"", nullable = false)
	private Integer replicationFactor = null;

	@ManyToOne
	@JoinColumn(name = "\"L_Creator\"", nullable = false)
	private Principal creator = null;

	@Column(name = "\"L_IsActive\"", nullable = false)
	private Short isActive = 1;

	@Column(name = "\"L_IsTemplate\"", nullable = false)
	private Short isTemplate = 0;

	@Column(name = "\"L_IsExternal\"", nullable = false)
	private Short isExternal = 0;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "\"L_GeocodeSystem\"", nullable = true)
	private GeocodeSystem geocodeSystem = null;

	@Column(name = "\"L_Style\"", nullable = true, length = 200)
	private String style = null;

	@Column(name = "\"L_Geonetwork\"", nullable = true)
	private Long geonetwork = null;

//	@Column(name = "\"L_ImportResource\"", nullable = true)
//	private String importResource = null;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "layer", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<LayerTenant> layerTenants = new HashSet<LayerTenant>(0);

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "layer", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<LayerTag> layerTags = new HashSet<LayerTag>(0);

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "layer", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<LayerVisualization> layerVisualizations = new HashSet<LayerVisualization>(0);

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "layer", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ProjectLayer> projectLayers = new HashSet<ProjectLayer>(0);

	@OneToOne(mappedBy = "layer", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
	private UserWorkspaceLayer userWorkspaceLayer = null;

	public Layer() {}

	public Layer(UUID layerID) {
		this.id = layerID;
		this.creationDate = new Date();
		this.lastUpdate = new Date();
	}

	public Layer(UUID layerID, String name) {
		this.id = layerID;
		this.name = name;
		this.creationDate = new Date();
		this.lastUpdate = new Date();
	}

	public Layer(UUID layerID, String name, Principal creator, short isActive) {
		this.id = layerID;
		this.name = name;
		this.creator = creator;
		this.isActive = isActive;
	}

	public Layer(UUID layerID, String name, Principal creator, String uri, DataSource dataSource, String extraData, short isActive) {
		this.id = layerID;
		this.name = name;
		this.creator = creator;
		this.externalGeoserverUrl = uri;
		this.dataSource = dataSource;
		this.extraData = extraData;
		this.isActive = isActive;
	}

	public GeocodeSystem getGeocodeSystem() {
		return geocodeSystem;
	}

	public void setGeocodeSystem(GeocodeSystem geocodeSystem) {
		this.geocodeSystem = geocodeSystem;
	}

	public short getIsTemplate() {
		return isTemplate;
	}

	public void setIsTemplate(short isTemplate) {
		this.isTemplate = isTemplate;
	}

	public Principal getCreator() {
		return creator;
	}

	public void setCreator(Principal creator) {
		this.creator = creator;
	}

	public short getIsActive() {
		return isActive;
	}

	public void setIsActive(short isActive) {
		this.isActive = isActive;
	}

	public Integer getReplicationFactor() {
		return replicationFactor;
	}

	public void setReplicationFactor(Integer replicationFactor) {
		this.replicationFactor = replicationFactor;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getLastUpdate() {
		return lastUpdate;
	}

	@Override
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getExternalGeoserverUrl() {
		return externalGeoserverUrl;
	}

	public void setExternalGeoserverUrl(String uri) {
		this.externalGeoserverUrl = uri;
	}

	public String getExtraData() {
		return extraData;
	}

	public void setExtraData(String extraData) {
		this.extraData = extraData;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public Long getGeonetwork() {
		return geonetwork;
	}

	public void setGeonetwork(Long geonetwork) {
		this.geonetwork = geonetwork;
	}

	public void setLayerTenants(Set<LayerTenant> layerTenants) {
		this.layerTenants = layerTenants;
	}

	public Set<LayerTenant> getLayerTenants() {
		return layerTenants;
	}

	public Set<LayerTag> getLayerTags() {
		return layerTags;
	}

	public void setLayerTags(Set<LayerTag> layerTags) {
		this.layerTags = layerTags;
	}

	public Set<LayerVisualization> getLayerVisualizations() {
		return layerVisualizations;
	}

	public void setLayerVisualizations(Set<LayerVisualization> layerVisualizations) {
		this.layerVisualizations = layerVisualizations;
	}

	public Set<ProjectLayer> getProjectLayers() {
		return projectLayers;
	}

	public void setProjectLayers(Set<ProjectLayer> projectLayers) {
		this.projectLayers = projectLayers;
	}

	public Short getIsExternal() {
		return isExternal;
	}

	public void setIsExternal(Short isExternal) {
		this.isExternal = isExternal;
	}

	public boolean isExternal() {
		return isExternal == 1;
	}

	public String getWorkspace() {
		return workspace;
	}

	public void setWorkspace(String workspace) {
		this.workspace = workspace;
	}

	public UserWorkspaceLayer getUserWorkspaceLayer() { return userWorkspaceLayer; }

	public void setUserWorkspaceLayer(UserWorkspaceLayer userWorkspaceLayer) { this.userWorkspaceLayer = userWorkspaceLayer; }

	//	public String getImportResource() {
//		return importResource;
//	}
//
//	public void setImportResource(String importResource) {
//		this.importResource = importResource;
//	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Layer other = (Layer) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Layer [id=" + id + ", name=" + name + ", workspace=" + workspace + ", dataSource=" + dataSource + ", description=" + description + ", uri=" + externalGeoserverUrl
				+ ", extraData=" + extraData + ", creationDate=" + creationDate + ", lastUpdate=" + lastUpdate + ", replicationFactor=" + replicationFactor + ", isActive="
				+ isActive + ", isTemplate=" + isTemplate + ", geocodeSystem=" + geocodeSystem + ", style=" + style + ", geonetwork=" + geonetwork + "]"; //", importResource" + importResource +
	}
}
