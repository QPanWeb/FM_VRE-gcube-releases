package org.gcube.portal.social.networking.ws.methods.v2;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.gcube.applicationsupportlayer.social.ApplicationNotificationsManager;
import org.gcube.applicationsupportlayer.social.NotificationsManager;
import org.gcube.applicationsupportlayer.social.shared.SocialNetworkingSite;
import org.gcube.applicationsupportlayer.social.shared.SocialNetworkingUser;
import org.gcube.common.authorization.library.provider.AuthorizationProvider;
import org.gcube.common.authorization.library.utils.Caller;
import org.gcube.common.homelibrary.home.HomeLibrary;
import org.gcube.common.homelibrary.home.workspace.Workspace;
import org.gcube.common.homelibrary.home.workspace.sharing.WorkspaceMessage;
import org.gcube.common.scope.api.ScopeProvider;
import org.gcube.portal.notifications.bean.GenericItemBean;
import org.gcube.portal.notifications.thread.MessageNotificationsThread;
import org.gcube.portal.social.networking.ws.filters.RequestsAuthFilter;
import org.gcube.portal.social.networking.ws.inputs.MessageInputBean;
import org.gcube.portal.social.networking.ws.inputs.Recipient;
import org.gcube.portal.social.networking.ws.outputs.ResponseBean;
import org.gcube.portal.social.networking.ws.utils.ErrorMessages;
import org.gcube.vomanagement.usermanagement.UserManager;
import org.gcube.vomanagement.usermanagement.impl.LiferayUserManager;
import org.gcube.vomanagement.usermanagement.model.GCubeUser;
import org.slf4j.LoggerFactory;

/**
 * Messages services REST interface
 * @author Costantino Perciante at ISTI-CNR 
 * (costantino.perciante@isti.cnr.it)
 */
@Path("2/messages")
@Api(tags={"messages"}, protocols="https", authorizations={@Authorization(value=RequestsAuthFilter.AUTH_TOKEN)})
public class Messages {

	// Logger
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Messages.class);

	//	user manager
	private UserManager um = new LiferayUserManager();

	@POST
	@Path("write-message/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Write a message to another user", notes="Write a message to another user. The sender is the token's owner by default", 
	response=ResponseBean.class, nickname="write-message")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successful write a message. Its id is reported in the 'result' field of the returned object", response = ResponseBean.class),
			@ApiResponse(code = 500, message = ErrorMessages.errorMessageApiResult, response=ResponseBean.class)})
	public Response writeMessage(
			@NotNull(message="Message to send is missing") 
			@Valid 
			@ApiParam(name="input", required=true, allowMultiple=false, value="The message to write")
			MessageInputBean input,
			@Context HttpServletRequest httpServletRequest) throws ValidationException{

		logger.debug("Incoming message bean is " + input);

		Caller caller = AuthorizationProvider.instance.get();
		String senderId = caller.getClient().getId();
		String context = ScopeProvider.instance.get();
		ResponseBean responseBean = new ResponseBean();
		Status status = Status.CREATED;

		String sender = input.getSender();
		String body = input.getBody();
		String subject = input.getSubject();
		List<Recipient> recipientsIds = input.getRecipients(); // "recipients":[{"recipient":"id recipient"}, ......]

		// check on sender id
		if(sender == null || sender.isEmpty())
			logger.info("Sender is going to be the token's owner [" + senderId  + "]");
		else{
			logger.info("Sender is going to be " + sender);
			senderId = sender;
		}

		// get the recipients ids (simple check, trim)
		List<String> recipientsListFiltered = new ArrayList<String>();
		List<GenericItemBean> recipientsBeans = new ArrayList<GenericItemBean>();
		for (Recipient recipientId : recipientsIds) {
			try{
				String tempId = recipientId.getId().trim();
				if(tempId.isEmpty())
					continue;
				GCubeUser userRecipient = um.getUserByUsername(tempId);
				GenericItemBean beanUser = new GenericItemBean(userRecipient.getUsername(), userRecipient.getUsername(), userRecipient.getFullname(), userRecipient.getUserAvatarURL());
				recipientsBeans.add(beanUser);
				recipientsListFiltered.add(tempId);
			}catch(Exception e){
				logger.error("Unable to retrieve recipient information for recipient with id " + recipientId, e);
			}
		}

		if(recipientsListFiltered.isEmpty()){

			logger.error("Missing/wrong request parameters");
			status = Status.BAD_REQUEST;
			responseBean.setMessage(ErrorMessages.missingParameters);
			return Response.status(status).entity(responseBean).build();

		}

		try{

			logger.debug("Trying to send message with body " + body + " subject " + subject + " to user " + recipientsIds + " from " + senderId);

			// sender info
			GCubeUser senderUser = um.getUserByUsername(senderId);
			Workspace workspace = HomeLibrary.getUserWorkspace(senderId);

			// send message
			logger.debug("Sending message to " + recipientsListFiltered);
			String messageId = workspace.getWorkspaceMessageManager()
					.sendMessageToPortalLogins(subject, body,
							new ArrayList<String>(), recipientsListFiltered);

			// send notification
			logger.debug("Message sent to " + recipientsIds + ". Sending message notification to: " + recipientsIds);
			SocialNetworkingSite site = new SocialNetworkingSite(
					httpServletRequest);
			SocialNetworkingUser user = new SocialNetworkingUser(
					senderUser.getUsername(), senderUser.getEmail(),
					senderUser.getFullname(), senderUser.getUserAvatarURL());

			NotificationsManager nm = new ApplicationNotificationsManager(site, context, user);
			new Thread(new MessageNotificationsThread(recipientsBeans, messageId, subject, body, nm)).start();

			responseBean.setSuccess(true);
			responseBean.setResult(messageId);

		}catch(Exception e){

			logger.error("Unable to send message.", e);
			status = Status.INTERNAL_SERVER_ERROR;
			responseBean.setMessage(e.getMessage());

		}
		return Response.status(status).entity(responseBean).build();
	}

	@GET
	@Path("get-sent-messages")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Retrieve the list of sent messages", notes="Retrieve the list of sent messages. The user is the token's owner by default", 
	response=ResponseBean.class, nickname="get-sent-messages")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successful read of the sent messages, reported in the 'result' field of the returned object", response = ResponseBean.class),
			@ApiResponse(code = 500, message = ErrorMessages.errorMessageApiResult, response=ResponseBean.class)})
	public Response getSentMessages(){

		// TODO modify the bean WorkspaceMessage with no camel case names
		Caller caller = AuthorizationProvider.instance.get();
		String username = caller.getClient().getId();

		ResponseBean responseBean = new ResponseBean();
		Status status = Status.OK;

		logger.info("Request for retrieving sent messages by " + username);

		try{

			Workspace workspace = HomeLibrary.getUserWorkspace(username);
			List<WorkspaceMessage> sentMessages = workspace.getWorkspaceMessageManager().getReceivedMessages();
			Collections.reverse(sentMessages);
			responseBean.setSuccess(true);
			logger.debug("Result is " + sentMessages);
			responseBean.setResult(sentMessages);

		}catch(Exception e){
			logger.error("Unable to retrieve sent messages", e);
			responseBean.setMessage(e.getMessage());
			status = Status.INTERNAL_SERVER_ERROR;
		}

		return Response.status(status).entity(responseBean).build();
	}

	@GET
	@Path("get-received-messages")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Retrieve the list of received messages", notes="Retrieve the list of received messages. The user is the token's owner by default", 
	response=ResponseBean.class, nickname="get-received-messages")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successful read of the received messages, reported in the 'result' field of the returned object", response = ResponseBean.class),
			@ApiResponse(code = 500, message = ErrorMessages.errorMessageApiResult, response=ResponseBean.class)})
	public Response getReceivedMessages(){

		// TODO modify the bean WorkspaceMessage with no camel case names
		Caller caller = AuthorizationProvider.instance.get();
		String username = caller.getClient().getId();

		ResponseBean responseBean = new ResponseBean();
		Status status = Status.OK;

		logger.info("Request for retrieving received messages by " + username);

		try{
			Workspace workspace = HomeLibrary.getUserWorkspace(username);
			List<WorkspaceMessage> getMessages = workspace.getWorkspaceMessageManager().getSentMessages();
			Collections.reverse(getMessages);
			responseBean.setSuccess(true);
			responseBean.setResult(getMessages);

		}catch(Exception e){
			logger.error("Unable to retrieve sent messages", e);
			responseBean.setMessage(e.getMessage());
			status = Status.INTERNAL_SERVER_ERROR;
		}

		return Response.status(status).entity(responseBean).build();
	}
}
