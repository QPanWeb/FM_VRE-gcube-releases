package org.gcube.portlets.admin.accountingmanager.shared.exception;

/**
 * ASL Session Expired Exception
 * 
 * @author "Giancarlo Panichi"
 *
 */
public class SessionExpiredException  extends ServiceException {

	private static final long serialVersionUID = -4831171355042165166L;

	/**
	 * 
	 */
	public SessionExpiredException() {
		super();
	}

	/**
	 * @param message
	 */
	public SessionExpiredException(String message) {
		super(message);
	}
	
	/**
	 * 
	 * @param message
	 * @param t
	 */
	public SessionExpiredException(String message,Throwable t) {
		super(message,t);
	}
	
}
