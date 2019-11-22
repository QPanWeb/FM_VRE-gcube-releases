package org.gcube.portlets.user.performfishanalytics.client.event;

import com.google.gwt.event.shared.EventHandler;


/**
 * The Interface SelectedPopulationTypeEventHandler.
 *
 * @author Francesco Mangiacrapa at ISTI-CNR (francesco.mangiacrapa@isti.cnr.it)
 * Jan 18, 2019
 */
public interface SelectedPopulationTypeEventHandler extends EventHandler {


	/**
	 * On selected population type.
	 *
	 * @param selectedPopulationTypeEvent the selected population type event
	 */
	void onSelectedPopulationType(
		SelectedPopulationTypeEvent selectedPopulationTypeEvent);
}