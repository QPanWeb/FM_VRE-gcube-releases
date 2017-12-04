package org.gcube.portlets.user.statisticalalgorithmsimporter.shared.input;

import java.io.Serializable;

/**
 * 
 * @author Giancarlo Panichi
 *
 *
 */
public class ProjectInfo implements Serializable {

	private static final long serialVersionUID = 7304965177776383842L;
	private String algorithmName;
	private String algorithmDescription;
	private String algorithmCategory;

	// private ArrayList<RequestedVRE> listRequestedVRE;

	public ProjectInfo() {
		super();
	}

	public ProjectInfo(String algorithmName, String algorithmDescription,
			String algorithmCategory) {
		super();
		this.algorithmName = algorithmName;
		this.algorithmDescription = algorithmDescription;
		this.algorithmCategory = algorithmCategory;
	}

	public String getAlgorithmName() {
		return algorithmName;
	}

	public String getAlgorithmNameToUpper() {
		return algorithmName.toUpperCase();
	}

	public String getAlgorithmNameToClassName() {
		return algorithmName.replaceAll("_", "");
	}

	public void setAlgorithmName(String algorithmName) {
		this.algorithmName = algorithmName;
	}

	public String getAlgorithmDescription() {
		return algorithmDescription;
	}

	public void setAlgorithmDescription(String algorithmDescription) {
		this.algorithmDescription = algorithmDescription;
	}

	public String getAlgorithmCategory() {
		return algorithmCategory;
	}

	public void setAlgorithmCategory(String algorithmCategory) {
		this.algorithmCategory = algorithmCategory;
	}

	@Override
	public String toString() {
		return "ProjectInfo [algorithmName=" + algorithmName
				+ ", algorithmDescription=" + algorithmDescription
				+ ", algorithmCategory=" + algorithmCategory + "]";
	}

}
