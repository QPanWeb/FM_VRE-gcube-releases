/**
 *
 */
package org.gcube.datatransfer.resolver.gis.geonetwork;


/**
 *
 * @author Francesco Mangiacrapa francesco.mangiacrapa@isti.cnr.it
 * Aug 30, 2017
 */
public interface GeonetworkRequestFilterParameters {

	public static enum MODE {HARVEST, VRE};
	public static enum VISIBILITY {PUB, PRV};
	public static String REQUEST_DELIMITIER = "/$$";
}
