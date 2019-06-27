/**
 *
 */
package org.gcube.portlets.user.performfishanalytics.shared;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.eclipse.persistence.annotations.CascadeOnDelete;


/**
 * The Class Area.
 *
 * @author Francesco Mangiacrapa francesco.mangiacrapa@isti.cnr.it
 * Jan 11, 2019
 */
@Entity
@CascadeOnDelete
public class Area implements GenericDao, ReferencePopulationType, PopulationTypeProperties, Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 970775589404463528L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int internalId; //PRIMARY KEY

	private String id;
	private String name;
	private String description;

	@CascadeOnDelete
	private PopulationType populationType;


	public Area() {

	}


	/**
	 * Instantiates a new area.
	 *
	 * @param id the id
	 * @param name the name
	 * @param description the description
	 * @param populationType the population type
	 */
	public Area(String id, String name, String description, PopulationType populationType) {

		this.id = id;
		this.name = name;
		this.description = description;
		this.populationType = populationType;
	}


	/**
	 * @return the id
	 */
	public String getId() {

		return id;
	}



	/**
	 * @return the name
	 */
	public String getName() {

		return name;
	}



	/**
	 * @return the description
	 */
	public String getDescription() {

		return description;
	}



	/**
	 * @return the populationType
	 */
	public PopulationType getPopulationType() {

		return populationType;
	}



	/**
	 * @param id the id to set
	 */
	public void setId(String id) {

		this.id = id;
	}



	/**
	 * @param name the name to set
	 */
	public void setName(String name) {

		this.name = name;
	}



	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {

		this.description = description;
	}



	/**
	 * @param populationType the populationType to set
	 */
	public void setPopulationType(PopulationType populationType) {

		this.populationType = populationType;
	}

	/* (non-Javadoc)
	 * @see org.gcube.portlets.user.performfishanalytics.shared.PopulationTypeProperties#getInternalId()
	 */
	@Override
	public int getInternalId() {

		return internalId;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();
		builder.append("Area [internalId=");
		builder.append(internalId);
		builder.append(", id=");
		builder.append(id);
		builder.append(", name=");
		builder.append(name);
		builder.append(", description=");
		builder.append(description);
		builder.append("]");
		return builder.toString();
	}




}
