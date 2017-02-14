/**
 * 
 */
package org.gcube.portlets.user.dataminermanager.shared.parameters;


/**
 * 
 * @author Giancarlo Panichi email: <a
 *         href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a>
 *
 */
public class ColumnListParameter extends Parameter {

	private static final long serialVersionUID = -6743494426144267089L;
	private String referredTabularParameterName;
	// private List<String> columnNames = new ArrayList<String>();
	private String separator;

	public ColumnListParameter() {
		super();
		this.typology = ParameterType.COLUMN_LIST;
	}

	public ColumnListParameter(String name, String description,
			String referredTabularParameterName, String separator) {
		super(name, ParameterType.COLUMN_LIST, description);
		this.referredTabularParameterName = referredTabularParameterName;
		this.separator = separator;
	}

	/**
	 * @param referredTabularParameterName
	 *            the referredTabularParameterName to set
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
	 * @return the separator
	 */
	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	@Override
	public String toString() {
		return "ColumnListParameter [referredTabularParameterName="
				+ referredTabularParameterName + ", value=" + value
				+ ", separator=" + separator + ", name=" + name
				+ ", description=" + description + ", typology=" + typology
				+ "]";
	}

}
