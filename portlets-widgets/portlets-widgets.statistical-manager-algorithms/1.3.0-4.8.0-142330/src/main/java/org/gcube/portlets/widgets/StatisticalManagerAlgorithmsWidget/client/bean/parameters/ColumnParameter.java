/**
 * 
 */
package org.gcube.portlets.widgets.StatisticalManagerAlgorithmsWidget.client.bean.parameters;

import java.io.Serializable;


/**
 * @author ceras
 *
 */
public class ColumnParameter extends Parameter implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5084557326770554659L;
	String referredTabularParameterName;
	String defaultColumn;
	String value;

	/**
	 * 
	 */
	public ColumnParameter() {
		super();
		this.typology = ParameterTypology.COLUMN;
	}

	/**
	 * 
	 */
	public ColumnParameter(String name, String description, String referredTabularParameterName, String defaultColumn) {
		super(name, ParameterTypology.COLUMN, description);
		this.referredTabularParameterName = referredTabularParameterName;
		this.defaultColumn = defaultColumn;
	}

	/**
	 * @param referredTabularParameterName the referredTabularParameterName to set
	 */
	public void setReferredTabularParameterName(
			String referredTabularParameterName) {
		this.referredTabularParameterName = referredTabularParameterName;
	}
	
	/**
	 * @return the referredTabularParameterName
	 */
	public String getReferredTabularParameterName() {
		return referredTabularParameterName;
	}

	/**
	 * @return the defaultValue
	 */
	public String getDefaultColumn() {
		return defaultColumn;
	}
	
	/**
	 * @param defaultValue the defaultValue to set
	 */
	public void setDefaultColumn(String defaultColumn) {
		this.defaultColumn = defaultColumn;
	}
	
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	
	/* (non-Javadoc)
	 * @see org.gcube.portlets.user.statisticalmanager.client.bean.Parameter#setValue(java.lang.String)
	 */
	@Override
	public void setValue(String value) {
		this.value = value;
	}

}
