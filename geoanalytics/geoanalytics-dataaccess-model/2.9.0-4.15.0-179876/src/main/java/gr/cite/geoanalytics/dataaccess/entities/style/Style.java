package gr.cite.geoanalytics.dataaccess.entities.style;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Type;

import gr.cite.geoanalytics.dataaccess.entities.Identifiable;
import gr.cite.geoanalytics.dataaccess.entities.Stampable;
import gr.cite.geoanalytics.dataaccess.entities.principal.Principal;


@Entity
@Table(name = "\"Style\"")
@JsonIgnoreProperties(value= {"creator"})
public class Style implements gr.cite.geoanalytics.dataaccess.entities.Entity, Identifiable, Stampable, Serializable {
	
	@Id
	@Type(type="org.hibernate.type.PostgresUUIDType") 
	@Column(name = "\"STL_ID\"", nullable = false)
	private UUID id = null;

	@Column(name = "\"STL_Name\"", nullable = false, length = 250)
	private String name = null;
	
	@Column(name = "\"STL_Description\"", nullable = false, length = 1000)  
	private String description = null;
	
	@Type(type="gr.cite.geoanalytics.dataaccess.typedefinition.XMLType") //DEPWARN XML Type: Hibernate dependency, replace when JPA 2.1 annotation is available
	@Basic(fetch = FetchType.LAZY)
	@Column(name = "\"STL_Content\"", columnDefinition = "xml", nullable = true)
	private String content = null;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "\"STL_Creator\"", nullable = false) 
	private Principal creator = null;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "\"STL_CreationDate\"", nullable = false)
	private Date creationDate = null;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "\"STL_LastUpdate\"", nullable = false)
	private Date lastUpdate = null;

//	@Type(type = "org.hibernate.type.PostgresUUIDType")
//	@Column(name = "\"STL_CvrgID\"", nullable = true)
//	private UUID cvrgID = null;

	public Style() {}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Principal getCreator() {
		return creator;
	}

	public void setCreator(Principal creator) {
		this.creator = creator;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

//	public UUID getCvrgID() {
//		return cvrgID;
//	}
//
//	public void setCvrgID(UUID cvrgID) {
//		this.cvrgID = cvrgID;
//	}

	public Style withID(String id) {
		this.id = UUID.fromString(id);	
		return this;
	}
	
	public Style withName(String name) {
		this.name = name;		
		return this;
	}
	
	public Style withDescription(String description) {
		this.description = description;		
		return this;
	}
	
	public Style withContent(String content) {
		this.content = content;		
		return this;
	}
	
	public Style withCreator(Principal creator) {
		this.creator = creator;		
		return this;
	}

	@Override
	public String toString() {
		return "Style [id=" + id + ", name=" + name + ", content=" + content + ", creator=" + creator
				+ ", creationDate=" + creationDate + ", lastUpdate=" + lastUpdate + "]";
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) return true;
		if (other == null || other.getClass() != this.getClass()) return false;

		if(!id.equals(((Style)other).getId())) return false;
		return true;
	}

	public Map<String, String> toMap(){
		Map<String,String> styleMap = new HashMap<String,String>();
		styleMap.put("id",id.toString());
		styleMap.put("name",name);
		styleMap.put("content", content);
		styleMap.put("creator", creator.getName());
		styleMap.put("creationDate", creationDate.toString());
		styleMap.put("lastUpdate",lastUpdate.toString());

		return styleMap;
	}
	
	
}
