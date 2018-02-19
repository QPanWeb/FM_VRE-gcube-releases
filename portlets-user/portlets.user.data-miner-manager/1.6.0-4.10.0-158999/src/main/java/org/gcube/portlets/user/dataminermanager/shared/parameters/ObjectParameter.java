package org.gcube.portlets.user.dataminermanager.shared.parameters;

/**
 * 
 * @author Giancarlo Panichi
 *
 *
 */
public class ObjectParameter extends Parameter {

	private static final long serialVersionUID = 1058462575242430851L;
	private String type;
	private String defaultValue;

	/**
	 * 
	 */
	public ObjectParameter() {
		super();
		this.typology = ParameterType.OBJECT;
	}

	/**
	 * 
	 * @param name
	 *            name
	 * @param description
	 *            description
	 * @param type
	 *            type
	 * @param defaultValue
	 *            default value
	 */
	public ObjectParameter(String name, String description, String type, String defaultValue) {
		super(name, ParameterType.OBJECT, description);
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
	 * @param type
	 *            the type to set
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
	 * @param defaultValue
	 *            the defaultValue to set
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public String toString() {
		return "ObjectParameter [type=" + type + ", defaultValue=" + defaultValue + ", value=" + value + ", name="
				+ name + ", description=" + description + ", typology=" + typology + "]";
	}

}
