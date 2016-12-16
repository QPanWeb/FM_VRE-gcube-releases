package gr.cite.geoanalytics.dataaccess.entities.principal;

import gr.cite.geoanalytics.dataaccess.entities.Identifiable;
import gr.cite.geoanalytics.dataaccess.entities.Stampable;

import java.util.Date;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

@Entity
@Table(name="\"PrincipalMembership\"")
public class PrincipalMembership implements gr.cite.geoanalytics.dataaccess.entities.Entity, Identifiable, Stampable{
	
	@Id	
	@Type(type="org.hibernate.type.PostgresUUIDType") //DEPWARN dependency to Hibernate and PostgreSQL
	@Column(name="\"PRCM_ID\"", nullable = false)
	private UUID id = null;
	
	@ManyToOne(fetch = FetchType.LAZY , cascade=CascadeType.ALL)
	@JoinColumn(name="\"PRCM_Member\"", nullable = false)
	private Principal member = null;
	
	@ManyToOne(fetch = FetchType.LAZY , cascade=CascadeType.ALL)
	@JoinColumn(name="\"PRCM_Group\"", nullable = false)
	private Principal group = null;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="\"PRCM_CreationDate\"", nullable = false)
	private Date creationDate = null;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="\"PRCM_LastUpdate\"", nullable = false)
	private Date lastUpdate = null;
	
	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Principal getMember() {
		return member;
	}

	public void setMember(Principal member) {
		this.member = member;
	}

	public Principal getGroup() {
		return group;
	}

	public void setGroup(Principal group) {
		this.group = group;
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
}
