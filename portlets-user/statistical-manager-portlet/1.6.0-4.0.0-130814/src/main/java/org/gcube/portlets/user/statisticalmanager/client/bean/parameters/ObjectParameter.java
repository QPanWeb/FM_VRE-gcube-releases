
package org.gcube.portlets.user.statisticalmanager.client.bean.parameters;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author ceras
 *
 */
public class ObjectParameter extends Parameter implements IsSerializable {
	
	private static final long serialVersionUID = 1058462575242430851L;
	private String type;
	private String defaultValue;
	private String value;
	
	
	/**
	 * 
	 */
	public ObjectParameter() {
		super();
		this.typology = ParameterTypology.OBJECT;
	}
	
	
	public ObjectParameter(String name, String description, String type, String defaultValue) {
		super(name, ParameterTypology.OBJECT, description);
		this.type = type;
		this.defaultValue = defaultValue;
	}
	
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * @return the defaultValue
	 */
	public String getDefaultValue() {
		return defaultValue;
	}
	
	/**
	 * @param defaultValue the defaultValue to set
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
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
