package org.gcube.portlets.user.td.widgetcommonevent.shared.tr;

/**
 * 
 * @author giancarlo
 * email: <a href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a> 
 *
 */
public enum TabResourceType {
	FLOW("Flow"), 
	STANDARD("Standard"),
	UNKNOWN("Unknown");
	
	
	/**
	 * @param text
	 */
	private TabResourceType(final String id) {
		this.id = id;
	}

	private final String id;
	
	@Override
	public String toString() {
		return id;
	}
	
	public String getTabResourceTypeLabel() {
		return id;
	}
	
	
	public static TabResourceType getTabResourceTypeFromId(String id) {
		for (TabResourceType tabResourceType : values()) {
			if (tabResourceType.id.compareToIgnoreCase(id) == 0) {
				return tabResourceType;
			}
		}
		return null;
	}
	
	
}
