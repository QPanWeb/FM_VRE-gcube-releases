/**
 *
 */
package org.gcube.portlets.user.workspaceexplorerapp.shared;

import java.io.Serializable;


/**
 * The Class HandlerResultMessage.
 *
 * @author Francesco Mangiacrapa francesco.mangiacrapa@isti.cnr.it
 * Mar 7, 2016
 */
public class HandlerResultMessage implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = -817188198850449565L;

	/**
	 * The Enum Status.
	 *
	 * @author Francesco Mangiacrapa francesco.mangiacrapa@isti.cnr.it
	 * Mar 7, 2016
	 */
	public enum Status {
		/**
		 * If an error occurs.
		 */
		ERROR,
		/**
		 * If no error occurs.
		 */
		OK,
		/**
		 * If there was some problems.
		 */
		WARN,

		/**
		 * If the result is unknown.
		 */
		UNKNOWN;
	}

	/**
	 * Error result.
	 *
	 * @param message the message
	 * @return the handler result message
	 */
	public static HandlerResultMessage errorResult(String message){
		return new HandlerResultMessage(Status.ERROR, message);
	}

	/**
	 * Ok result.
	 *
	 * @param message the message
	 * @return the handler result message
	 */
	public static HandlerResultMessage okResult(String message){
		return new HandlerResultMessage(Status.OK, message);
	}

	/**
	 * Warn result.
	 *
	 * @param message the message
	 * @return the handler result message
	 */
	public static HandlerResultMessage warnResult(String message){
		return new HandlerResultMessage(Status.WARN, message);
	}

	/**
	 * Parses the result.
	 *
	 * @param result the result
	 * @return the handler result message
	 */
	public static HandlerResultMessage parseResult(String result){
		//expected 200:Upload complete
		String statusToken = null;
		String messageToken = null;

		int index = result.indexOf(':');
		if (index>0){
			statusToken = result.substring(0,index);
			if (index<result.length()){
				messageToken = result.substring(index+1);
			}
		}

		Status status = Status.UNKNOWN;
		if (statusToken!=null){
			status = Status.valueOf(statusToken);
		}

		String message = messageToken!=null?messageToken:"";

		return new HandlerResultMessage(status, message);
	}


	protected Status status;
	protected String message;

	/**
	 * Create a new result message.
	 * @param status the status.
	 * @param message the message.
	 */
	public HandlerResultMessage(Status status, String message) {
		this.status = status;
		this.message = message;
	}


	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(status);
		builder.append(":");
		builder.append(message);
		return builder.toString();
	}


}
