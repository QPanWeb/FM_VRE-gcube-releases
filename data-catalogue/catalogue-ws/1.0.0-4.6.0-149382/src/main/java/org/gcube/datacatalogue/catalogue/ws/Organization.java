package org.gcube.datacatalogue.catalogue.ws;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.gcube.common.authorization.library.provider.AuthorizationProvider;
import org.gcube.common.authorization.library.utils.Caller;
import org.gcube.common.scope.api.ScopeProvider;
import org.gcube.datacatalogue.catalogue.utils.CatalogueUtils;
import org.gcube.datacatalogue.catalogue.utils.Constants;


@Path(Constants.ORGANIZATIONS)
/**
 * Organizations service endpoint.
 * @author Costantino Perciante at ISTI-CNR (costantino.perciante@isti.cnr.it)
 */
public class Organization {

	@GET
	@Path(Constants.SHOW_METHOD)
	@Produces(MediaType.APPLICATION_JSON)
	public String show(@Context UriInfo uriInfo){

		// see http://docs.ckan.org/en/latest/api/#ckan.logic.action.get.organization_show
		Caller caller = AuthorizationProvider.instance.get();
		String context = ScopeProvider.instance.get();
		return CatalogueUtils.delegateGet(caller, context, Constants.ORGANIZATION_SHOW, uriInfo);

	}
	
	@GET
	@Path(Constants.LIST_METHOD)
	@Produces(MediaType.APPLICATION_JSON)
	public String organizationList(@Context UriInfo uriInfo){

		// see http://docs.ckan.org/en/latest/api/#ckan.logic.action.get.organization_list
		Caller caller = AuthorizationProvider.instance.get();
		String context = ScopeProvider.instance.get();
		return CatalogueUtils.delegateGet(caller, context, Constants.ORGANIZATION_LIST, uriInfo);

	}

	@POST
	@Path(Constants.CREATE_METHOD)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String create(String json, @Context UriInfo uriInfo){

		// see http://docs.ckan.org/en/latest/api/#ckan.logic.action.get.organization_create
		Caller caller = AuthorizationProvider.instance.get();
		String context = ScopeProvider.instance.get();
		return CatalogueUtils.delegatePost(caller, context, Constants.ORGANIZATION_CREATE, json, uriInfo);

	}

	@DELETE
	@Path(Constants.DELETE_METHOD)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String delete(String json, @Context UriInfo uriInfo){

		// see http://docs.ckan.org/en/latest/api/#ckan.logic.action.get.organization_delete
		Caller caller = AuthorizationProvider.instance.get();
		String context = ScopeProvider.instance.get();
		return CatalogueUtils.delegatePost(caller, context, Constants.ORGANIZATION_DELETE, json, uriInfo);

	}

	@DELETE
	@Path(Constants.PURGE_METHOD)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String purge(String json, @Context UriInfo uriInfo){

		// see http://docs.ckan.org/en/latest/api/#ckan.logic.action.get.organization_create
		Caller caller = AuthorizationProvider.instance.get();
		String context = ScopeProvider.instance.get();
		return CatalogueUtils.delegatePost(caller, context, Constants.ORGANIZATION_PURGE, json, uriInfo);

	}

	@POST
	@Path(Constants.UPDATE_METHOD)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String update(String json, @Context UriInfo uriInfo){

		// see http://docs.ckan.org/en/latest/api/#ckan.logic.action.get.organization_update
		Caller caller = AuthorizationProvider.instance.get();
		String context = ScopeProvider.instance.get();
		return CatalogueUtils.delegatePost(caller, context, Constants.ORGANIZATION_UPDATE, json, uriInfo);

	}

	@POST
	@Path(Constants.PATCH_METHOD)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String patch(String json, @Context UriInfo uriInfo){

		// see http://docs.ckan.org/en/latest/api/#ckan.logic.action.get.organization_patch
		Caller caller = AuthorizationProvider.instance.get();
		String context = ScopeProvider.instance.get();
		return CatalogueUtils.delegatePost(caller, context, Constants.ORGANIZATION_PATCH, json, uriInfo);

	}

}
