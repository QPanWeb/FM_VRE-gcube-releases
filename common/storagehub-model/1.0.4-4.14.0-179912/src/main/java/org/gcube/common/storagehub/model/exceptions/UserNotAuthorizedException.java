package org.gcube.common.storagehub.model.exceptions;

import org.gcube.common.clients.delegates.Unrecoverable;

@Unrecoverable
public class UserNotAuthorizedException extends StorageHubException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UserNotAuthorizedException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UserNotAuthorizedException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public UserNotAuthorizedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public UserNotAuthorizedException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getErrorMessage() {
		return "user not authorized";
	}

	@Override
	public int getStatus() {
		return 500;
	}
	
}
