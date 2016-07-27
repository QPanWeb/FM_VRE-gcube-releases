package org.gcube.portlets.user.dataminermanager.shared.data.computations;

/**
 * 
 * @author Giancarlo Panichi email: <a
 *         href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a>
 *
 */
public class ComputationValueImage extends ComputationValue {
	private static final long serialVersionUID = -5845606225432949795L;

	private String fileName;
	private String mimeType;

	public ComputationValueImage(){
		super();
	}
	
	public ComputationValueImage(String url, String fileName, String mimeType) {
		super(ComputationValueType.Image, url);
		this.fileName = fileName;
		this.mimeType = mimeType;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	@Override
	public String toString() {
		return "ComputationValueImage [fileName=" + fileName + ", mimeType="
				+ mimeType + "]";
	}

}
