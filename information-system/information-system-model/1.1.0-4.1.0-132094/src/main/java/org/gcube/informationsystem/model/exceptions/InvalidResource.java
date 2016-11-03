/**
 * 
 */
package org.gcube.informationsystem.model.exceptions;

/**
 * @author Luca Frosini (ISTI - CNR) http://www.lucafrosini.com/
 *
 */
public class InvalidResource extends InvalidEntity {

	/**
	 * Generated Serial Version UID
	 */
	private static final long serialVersionUID = 1420299795193950903L;

	public InvalidResource(String message) {
		super(message);
	}
	
	public InvalidResource(Throwable cause) {
		super(cause);
	}
	
	public InvalidResource(String message, Throwable cause) {
		super(message, cause);
	}
	
}
