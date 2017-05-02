package org.gcube.data.analysis.dataminermanagercl.test;

/**
 * 
 * @author Giancarlo Panichi email: <a
 *         href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a>
 *
 */
public enum OperatorId {
	DBSCAN(
			"org.gcube.dataanalysis.wps.statisticalmanager.synchserver.mappedclasses.clusterers.DBSCAN"), CSQUARE_COLUMN_CREATOR(
			"org.gcube.dataanalysis.wps.statisticalmanager.synchserver.mappedclasses.transducerers.CSQUARE_COLUMN_CREATOR"), BIONYM_LOCAL(
			"org.gcube.dataanalysis.wps.statisticalmanager.synchserver.mappedclasses.transducerers.BIONYM_LOCAL"), LISTDBINFO(
			"org.gcube.dataanalysis.wps.statisticalmanager.synchserver.mappedclasses.transducerers.LISTDBINFO"), LISTDBNAMES(
			"org.gcube.dataanalysis.wps.statisticalmanager.synchserver.mappedclasses.transducerers.LISTDBNAMES"), LISTDBSCHEMA(
			"org.gcube.dataanalysis.wps.statisticalmanager.synchserver.mappedclasses.transducerers.LISTDBSCHEMA"), LISTTABLES(
			"org.gcube.dataanalysis.wps.statisticalmanager.synchserver.mappedclasses.transducerers.LISTTABLES");
	;
	/**
	 * @param text
	 */
	private OperatorId(final String id) {
		this.id = id;
	}

	private final String id;

	@Override
	public String toString() {
		return id;
	}

	public String getLabel() {
		return id;
	}

	public static OperatorId getOperatorIdFromId(String id) {
		for (OperatorId operatorId : values()) {
			if (operatorId.id.compareToIgnoreCase(id) == 0) {
				return operatorId;
			}
		}
		return null;
	}

}
