/**
 * 
 */
package org.gcube.portlets.widgets.netcdfbasicwidgets.shared.exception;

/**
 * 
 * @author Giancarlo Panichi
 * 
 *
 */
public class ServiceException extends Exception {

	private static final long serialVersionUID = -2255657546267656458L;

	/**
	 * 
	 */
	public ServiceException() {
		super();
	}

	/**
	 * @param message
	 *            message
	 */
	public ServiceException(String message) {
		super(message);
	}

	/**
	 * 
	 * @param message
	 *            message
	 * @param throwable
	 *            error
	 */
	public ServiceException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
