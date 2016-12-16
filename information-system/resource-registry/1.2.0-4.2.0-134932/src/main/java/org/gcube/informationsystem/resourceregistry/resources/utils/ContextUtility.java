/**
 * 
 */
package org.gcube.informationsystem.resourceregistry.resources.utils;

import java.util.Iterator;
import java.util.UUID;

import org.gcube.common.authorization.client.Constants;
import org.gcube.common.authorization.library.AuthorizationEntry;
import org.gcube.common.authorization.library.provider.SecurityTokenProvider;
import org.gcube.common.scope.api.ScopeProvider;
import org.gcube.common.scope.impl.ScopeBean;
import org.gcube.informationsystem.model.embedded.Header;
import org.gcube.informationsystem.model.entity.Context;
import org.gcube.informationsystem.model.entity.Entity;
import org.gcube.informationsystem.resourceregistry.api.exceptions.ResourceRegistryException;
import org.gcube.informationsystem.resourceregistry.api.exceptions.context.ContextException;
import org.gcube.informationsystem.resourceregistry.api.exceptions.context.ContextNotFoundException;
import org.gcube.informationsystem.resourceregistry.context.SecurityContext;
import org.gcube.informationsystem.resourceregistry.context.SecurityContextMapper;
import org.gcube.informationsystem.resourceregistry.context.SecurityContextMapper.PermissionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.security.OSecurity;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;

/**
 * @author Luca Frosini (ISTI - CNR)
 * 
 */
public class ContextUtility {

	private static final Logger logger = LoggerFactory
			.getLogger(ContextUtility.class);

	public static UUID addToActualContext(OrientGraph orientGraph, Vertex vertex)
			throws ContextException {
		UUID contextUUID = ContextUtility.getActualContextUUID();
		SecurityContext.addToSecurityContext(orientGraph, vertex, contextUUID);
		return contextUUID;
	}

	public static UUID addToActualContex(OSecurity oSecurity, Vertex vertex)
			throws ContextException {
		UUID contextUUID = ContextUtility.getActualContextUUID();
		SecurityContext.addToSecurityContext(oSecurity, vertex, contextUUID);
		return contextUUID;
	}

	public static UUID addToActualContext(OrientGraph orientGraph, Edge edge)
			throws ContextException {
		UUID contextUUID = ContextUtility.getActualContextUUID();
		SecurityContext.addToSecurityContext(orientGraph, edge, contextUUID);
		return contextUUID;
	}

	public static UUID addToActualContext(OSecurity oSecurity, Edge edge)
			throws ContextException {
		UUID contextUUID = ContextUtility.getActualContextUUID();
		SecurityContext.addToSecurityContext(oSecurity, edge, contextUUID);
		return contextUUID;
	}

	protected static UUID getContextUUIDFromContextVertex(Vertex vertex) {
		ODocument header = vertex.getProperty(Entity.HEADER_PROPERTY);
		String contextID = header.field(Header.UUID_PROPERTY);
		return UUID.fromString(contextID);
	}
	
	public static String getCurrentContext(){
		String token = SecurityTokenProvider.instance.get();
		AuthorizationEntry authorizationEntry = null;
		try {
			authorizationEntry = Constants.authorizationService().get(token);
		} catch (Exception e) {
			return ScopeProvider.instance.get();
		}
		return authorizationEntry.getContext();
	}

	public static UUID getActualContextUUID() throws ContextException {
		OrientGraphFactory factory = null;
		OrientGraph orientGraph = null;
		
		try {
			String scope = getCurrentContext();
			if(scope==null){
				throw new ContextException("Null Token and Scope. Please set your token first.");
			}
			logger.trace("Trying to get context UUID for scope {}", scope);
			factory = SecurityContextMapper
					.getSecurityContextFactory(
							SecurityContextMapper.ADMIN_SECURITY_CONTEXT_UUID,
							PermissionMode.READER);
			orientGraph = factory.getTx();
			Vertex context = ContextUtility.getContextVertexByFullName(
					orientGraph, scope);
			return getContextUUIDFromContextVertex(context);
		} catch (ContextException e) {
			throw e;
		} catch (Exception e) {
			throw new ContextException(
					"Unable to restrive Context UUID from current Context", e);
		} finally{
			if(orientGraph!=null){
				orientGraph.shutdown();
			}
		}
	}

	public static OrientGraphFactory getFactory(PermissionMode permissionMode)
			throws ResourceRegistryException {
		try {
			UUID contextUUID = getActualContextUUID();
			return SecurityContextMapper.getSecurityContextFactory(contextUUID,
					permissionMode);
		} catch (ContextException ce) {
			logger.error("Unable to retrieve context.", ce);
			throw ce;
		} catch (Exception e) {
			logger.error("Unable to retrieve context.", e);
			throw new ResourceRegistryException(e);
		}
	}

	public static OrientGraph getActualSecurityContextGraph(
			PermissionMode permissionMode) throws ResourceRegistryException {
		try {
			OrientGraphFactory factory = getFactory(permissionMode);
			return factory.getTx();
		} catch (ContextException ce) {
			logger.error("Unable to retrieve context.", ce);
			throw ce;
		} catch (Exception e) {
			logger.error("Unable to retrieve context.", e);
			throw new ResourceRegistryException(e);
		}
	}

	public static OrientGraphNoTx getActualSecurityContextGraphNoTx(
			PermissionMode permissionMode) throws ResourceRegistryException {
		try {
			OrientGraphFactory factory = getFactory(permissionMode);
			return factory.getNoTx();
		} catch (ContextException ce) {
			logger.error("Unable to retrieve context.", ce);
			throw ce;
		} catch (Exception e) {
			logger.error("Unable to retrieve context.", e);
			throw new ResourceRegistryException(e);
		}
	}

	public static ODatabaseDocumentTx getActualSecurityContextDatabaseTx(
			PermissionMode permissionMode) throws ResourceRegistryException {
		try {
			OrientGraphFactory factory = getFactory(permissionMode);
			return factory.getDatabase();
		} catch (ContextException ce) {
			logger.error("Unable to retrieve context.", ce);
			throw ce;
		} catch (Exception e) {
			logger.error("Unable to retrieve context.", e);
			throw new ResourceRegistryException(e);
		}
	}

	public static Vertex getContextVertexByFullName(OrientGraph orientGraph,
			String fullName) throws ContextNotFoundException {

		logger.trace("Going to get {} {} from full name '{}'", Context.NAME, Vertex.class.getSimpleName(), fullName);
		
		ScopeBean scopeBean = new ScopeBean(fullName);
		String name = scopeBean.name();

		// TODO Rewrite the previous query using Gremlin
		// Please note that this query works because all the scope parts has a
		// different name
		String select = "SELECT FROM " + Context.class.getSimpleName()
				+ " WHERE " + Context.NAME_PROPERTY + " = \"" + name + "\"";
		;
		OSQLSynchQuery<Vertex> osqlSynchQuery = new OSQLSynchQuery<Vertex>(
				select);
		Iterable<Vertex> vertexes = orientGraph.command(osqlSynchQuery)
				.execute();

		if (vertexes == null || !vertexes.iterator().hasNext()) {
			throw new ContextNotFoundException(
					"Error retrieving context with name " + fullName);
		}

		Iterator<Vertex> iterator = vertexes.iterator();
		Vertex context = iterator.next();

		logger.trace("Context Representing Vertex : {}",
				Utility.toJsonString(context, true));

		if (iterator.hasNext()) {
			throw new ContextNotFoundException(
					"Found more than one context with name " + name
							+ "but required the one with path" + fullName
							+ ". Please Reimplement the query");
		}

		return context;
	}

	public static String getActualSecurityRoleOrUserName(
			SecurityContextMapper.PermissionMode permissionMode,
			SecurityContextMapper.SecurityType securityType)
			throws ContextException {
		UUID contextUUID = getActualContextUUID();
		return SecurityContextMapper.getSecurityRoleOrUserName(permissionMode,
				securityType, contextUUID);
	}

}
