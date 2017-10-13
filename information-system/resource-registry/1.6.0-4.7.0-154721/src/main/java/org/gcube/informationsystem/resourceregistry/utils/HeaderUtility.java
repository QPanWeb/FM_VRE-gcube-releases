/**
 * 
 */
package org.gcube.informationsystem.resourceregistry.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.gcube.common.authorization.library.provider.AuthorizationProvider;
import org.gcube.common.authorization.library.provider.ClientInfo;
import org.gcube.common.authorization.library.utils.Caller;
import org.gcube.informationsystem.impl.utils.ISMapper;
import org.gcube.informationsystem.model.embedded.Header;
import org.gcube.informationsystem.model.entity.Entity;
import org.gcube.informationsystem.model.entity.Resource;
import org.gcube.informationsystem.resourceregistry.api.exceptions.ResourceRegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;

/**
 * @author Luca Frosini (ISTI - CNR)
 * 
 */
public class HeaderUtility {

	private static final Logger logger = LoggerFactory
			.getLogger(HeaderUtility.class);

	public static Header createHeader(UUID uuid) {
		HeaderOrient header = new HeaderOrient();

		if (uuid == null) {
			uuid = UUID.randomUUID();
		}

		header.setUUID(uuid);

		String creator = org.gcube.informationsystem.model.embedded.Header.UNKNOWN_USER;
		try {
			Caller caller = AuthorizationProvider.instance.get();
			if(caller!=null){
				ClientInfo clientInfo = caller.getClient();
				String clientId = clientInfo.getId();
				if (clientId != null && clientId.compareTo("") != 0) {
					creator = clientId;
				} else {
					throw new Exception("Username null or empty");
				}
			}
		} catch (Exception e) {
			logger.error("Unable to retrieve user. {} will be used", creator);
		}
		header.setCreator(creator);

		Date date = Calendar.getInstance().getTime();
		SimpleDateFormat ft = new SimpleDateFormat ("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
		logger.trace("Setting Last Update and Creation Time to " + ft.format(date));
		
		header.setCreationTime(date);
		header.setLastUpdateTime(date);

		return header;
	}
	
	public static Header getHeader(JsonNode jsonNode, boolean creation)
			throws JsonParseException, JsonMappingException, IOException {
		if (jsonNode.has(Resource.HEADER_PROPERTY)) {
			JsonNode headerNode = jsonNode.get(Resource.HEADER_PROPERTY);
			if (headerNode.isNull()) {
				return null;
			}
			HeaderOrient header = null;
			if(creation){
				// If an header is provided MUST contains and UUID otherwise is
				// an invalid request so that let that an exception is raised
				UUID uuid = UUID.fromString(headerNode.get(Header.UUID_PROPERTY).asText());
				header = (HeaderOrient) createHeader(uuid);
			}else{
				header = new HeaderOrient();
				header.fromJSON(headerNode.toString());
			}
			return header;
		}
		return null;
	}
	
	public static HeaderOrient getHeaderOrient(ODocument oDocument) throws ResourceRegistryException{
		if (oDocument instanceof HeaderOrient) {
			return (HeaderOrient) oDocument;
		}else{
			try {
				HeaderOrient headerOrient = new HeaderOrient();
				Header header = ISMapper.unmarshal(Header.class, oDocument.toJSON());
				headerOrient.setUUID(header.getUUID());
				headerOrient.setCreator(header.getCreator());
				headerOrient.setCreationTime(header.getCreationTime());
				headerOrient.setLastUpdateTime(header.getLastUpdateTime());
				return headerOrient;	
			} catch (Exception e) {
				throw new ResourceRegistryException("Unable to recreate Header. This should not occur. Please contact Administrator.");
			}
		}
	}
	
	public static Header addHeader(Vertex vertex, UUID uuid) {
		Header header = createHeader(uuid);
		vertex.setProperty(Entity.HEADER_PROPERTY, header);
		return header;
	}

	
	public static Header addHeader(Edge edge, UUID uuid) {
		Header header = createHeader(uuid);
		edge.setProperty(Entity.HEADER_PROPERTY, header);
		return header;
	}

	public static Header getHeader(Element element)
			throws ResourceRegistryException {
		return Utility.getEmbedded(Header.class, element, Entity.HEADER_PROPERTY);
	}
	
}
