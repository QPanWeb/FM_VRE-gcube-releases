package org.gcube.resources.federation.fhnmanager.api.type;

public class VMProviderCredentials {

	private String type;

	private String encodedCredentails;
	
	private String vo;

	public String getVo() {
		return vo;
	}

	public void setVo(String vo) {
		this.vo = vo;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getEncodedCredentails() {
		return encodedCredentails;
	}

	public void setEncodedCredentails(String encodedCredentails) {
		this.encodedCredentails = encodedCredentails;
	}

}
