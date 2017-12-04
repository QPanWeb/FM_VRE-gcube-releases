package org.gcube.datacatalogue.ckanutillibrary.server.utils.url;

/**
 * Entity context for uri resolver
 * @author Costantino Perciante at ISTI-CNR (costantino.perciante@isti.cnr.it)
 */
public enum EntityContext {

	PRODUCT("product"),
	GROUP("group"),
	ORGANIZATION("organization");
	
	private String entityAsString;

	private EntityContext(String entityAsString) {
		this.entityAsString = entityAsString;
	}
	
	@Override
	public String toString() {
		return this.entityAsString;
	}

}
