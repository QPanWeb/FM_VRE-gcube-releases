/**
 * 
 */
package org.gcube.informationsystem.model.reference.entity;

import java.io.Serializable;

import org.gcube.informationsystem.model.reference.ER;
import org.gcube.informationsystem.model.reference.annotations.Abstract;
import org.gcube.informationsystem.model.reference.annotations.ISProperty;
import org.gcube.informationsystem.model.reference.embedded.Header;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * @author Luca Frosini (ISTI - CNR)
 * https://wiki.gcube-system.org/gcube/Facet_Based_Resource_Model#Basic_Concepts
 */
@Abstract
@JsonIgnoreProperties(ignoreUnknown=true)
//@JsonDeserialize(as=EntityImpl.class) Do not uncomment to manage subclasses
public interface Entity extends ER, Serializable {
	
	public static final String NAME = "Entity"; //Entity.class.getSimpleName();
	
	/* Overriding getHeader method to create Header property in type */
	@ISProperty(name=HEADER_PROPERTY, mandatory=true, nullable=false)
	@Override
	public Header getHeader();
	
}
