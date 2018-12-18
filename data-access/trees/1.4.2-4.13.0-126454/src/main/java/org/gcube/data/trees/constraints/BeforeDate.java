/**
 * 
 */
package org.gcube.data.trees.constraints;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.gcube.data.trees.patterns.DatePattern;

/**
 * Accepts {@link Date} values that precede a given one.
 * 
 * @author Fabio Simeoni
 *
 */
@XmlRootElement 
public class BeforeDate extends BaseConstraint<java.util.Date> {

	private static final long serialVersionUID = 1L;
	
	@XmlElement 
	private Date than;
	
	public BeforeDate(){} //here for deserialisation
	
	/**
	 * Creates an instance from a given date.
	 * @param d the date
	 */
	public BeforeDate(Date d) {
		than=d;
	}
	
	/**{@inheritDoc}*/
	public boolean accepts(Date d) {
		return d.before(than);
	}
	
	/**{@inheritDoc}*/
	@Override public String toString() {
		return super.toString()+" "+DatePattern.ISO8601formatter.format(than);
	}
	
	/**{@inheritDoc}*/
	@Override public boolean equals(Object obj) {
		if (!(obj instanceof BeforeDate)) return false;
		else return than.equals(((BeforeDate) obj).than);
	}
	
	/**{@inheritDoc}*/
	@Override public int hashCode() {
		return than.hashCode()+31*17;
	}
}
