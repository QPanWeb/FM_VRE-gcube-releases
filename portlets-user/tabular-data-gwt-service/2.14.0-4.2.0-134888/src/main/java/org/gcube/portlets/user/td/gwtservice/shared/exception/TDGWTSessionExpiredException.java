package org.gcube.portlets.user.td.gwtservice.shared.exception;

/**
 * ASL Session Expired Exception
 * 
 * @author "Giancarlo Panichi"
 *
 */
public class TDGWTSessionExpiredException  extends TDGWTServiceException {

	
	private static final long serialVersionUID = 4306091799912937920L;
	
	/**
	 * 
	 */
	public TDGWTSessionExpiredException() {
		super();
	}

	/**
	 * @param message
	 */
	public TDGWTSessionExpiredException(String message) {
		super(message);
	}
	
	/**
	 * 
	 * @param message
	 * @param t
	 */
	public TDGWTSessionExpiredException(String message,Throwable t) {
		super(message,t);
	}
	
}
