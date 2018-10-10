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

import org.gcube.datacatalogue.catalogue.utils.Constants;
import org.gcube.datacatalogue.catalogue.utils.Delegator;


@Path(Constants.USERS)
/**
 * User service endpoint.
 * @author Costantino Perciante (ISTI - CNR)
 * @author Luca Frosini (ISTI - CNR)
 */
public class User {

	@GET
	@Path(Constants.SHOW_METHOD)
	@Produces(MediaType.TEXT_PLAIN)
	public String show(@Context UriInfo uriInfo){
		// see http://docs.ckan.org/en/latest/api/#ckan.logic.action.get.user_show
		return Delegator.delegateGet(Constants.USER_SHOW, uriInfo);

	}

	@POST
	@Path(Constants.CREATE_METHOD)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String create(String json, @Context UriInfo uriInfo){
		// see http://docs.ckan.org/en/latest/api/#ckan.logic.action.create.user_create
		return Delegator.delegatePost(Constants.USER_CREATE, json, uriInfo);

	}

	@DELETE
	@Path(Constants.DELETE_METHOD)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String delete(String json, @Context UriInfo uriInfo){
		// see http://docs.ckan.org/en/latest/api/#ckan.logic.action.delete.user_delete
		return Delegator.delegatePost(Constants.USER_DELETE, json, uriInfo);

	}

	@POST
	@Path(Constants.UPDATE_METHOD)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String update(String json, @Context UriInfo uriInfo){
		// see http://docs.ckan.org/en/latest/api/#ckan.logic.action.update.user_update
		return Delegator.delegatePost(Constants.USER_UPDATE, json, uriInfo);

	}

}
