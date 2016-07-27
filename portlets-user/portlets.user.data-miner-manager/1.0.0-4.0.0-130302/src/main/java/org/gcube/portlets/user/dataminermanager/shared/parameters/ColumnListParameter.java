/**
 * 
 */
package org.gcube.portlets.user.dataminermanager.shared.parameters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @author Giancarlo Panichi
 * email: <a href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a> 
 *
 */
public class ColumnListParameter extends Parameter implements Serializable {

	private static final long serialVersionUID = -6743494426144267089L;
	String referredTabularParameterName;
	List<String> columnNames = new ArrayList<String>();
	String value;
	private String separator;
	
	public ColumnListParameter() {
		super();
		this.typology = ParameterTypology.COLUMN_LIST;
	}
	
	public ColumnListParameter(String name, String description, String referredTabularParameterName, String separator) {
		super(name, ParameterTypology.COLUMN_LIST, description);
		this.referredTabularParameterName = referredTabularParameterName;
		this.separator = separator;
	}

	/**
	 * @param referredTabularParameterName the referredTabularParameterName to set
	 */
	public void setReferredTabularParameterName(String referredTabularParameterName) {
		this.referredTabularParameterName = referredTabularParameterName;
	}
	
	/**
	 * @return the referredTabularParameterName
	 */
	public String getReferredTabularParameterName() {
		return referredTabularParameterName;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	
	
	@Override
	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * @return the separator
	 */
	public String getSeparator() {
		return separator;
	}

}
