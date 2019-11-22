/**
 * 
 */
package org.gcube.informationsystem.model.reference.embedded;

import java.util.Date;
import java.util.UUID;

import org.gcube.informationsystem.model.impl.embedded.HeaderImpl;
import org.gcube.informationsystem.model.reference.ISConstants;
import org.gcube.informationsystem.model.reference.annotations.ISProperty;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author Luca Frosini (ISTI - CNR)
 * https://wiki.gcube-system.org/gcube/Facet_Based_Resource_Model#Header
 */
@JsonDeserialize(as=HeaderImpl.class)
public interface Header extends Embedded {

	public static final String NAME = "Header"; // Header.class.getSimpleName();

	/**
	 * Used to set Creator when the user is not known
	 */
	public static final String UNKNOWN_USER = "UNKNOWN_USER";

	public static final String UUID_PROPERTY = "uuid";
	public static final String CREATOR_PROPERTY = "creator";
	public static final String MODIFIED_BY_PROPERTY = "modifiedBy";
	public static final String CREATION_TIME_PROPERTY = "creationTime";
	public static final String LAST_UPDATE_TIME_PROPERTY = "lastUpdateTime";

	@ISProperty(name = UUID_PROPERTY, readonly = true, mandatory = true, nullable = false)
	public UUID getUUID();

	public void setUUID(UUID uuid);

	@ISProperty(name = CREATOR_PROPERTY, readonly = true, mandatory = true, nullable = false)
	public String getCreator();

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ISConstants.DATETIME_PATTERN)
	@ISProperty(name = CREATION_TIME_PROPERTY, readonly = true, mandatory = true, nullable = false)
	public Date getCreationTime();

	@ISProperty(name = MODIFIED_BY_PROPERTY, mandatory = true, nullable = false)
	public String getModifiedBy();

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ISConstants.DATETIME_PATTERN)
	@ISProperty(name = LAST_UPDATE_TIME_PROPERTY, mandatory = true, nullable = false)
	public Date getLastUpdateTime();

}