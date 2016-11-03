package org.gcube.portlets.widgets.openlayerbasicwidgets.client.event;

import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author Giancarlo Panichi email: <a
 *         href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a>
 *
 */
public enum SelectAreaDialogEventType {
	Completed("Completed"), Failed("Failed"), Aborted("Aborted");

	/**
	 * @param text
	 */
	private SelectAreaDialogEventType(final String id) {
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

	public String getId() {
		return id;
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public static SelectAreaDialogEventType getTypeFromId(String id) {
		if (id == null || id.isEmpty())
			return null;

		for (SelectAreaDialogEventType type : values()) {
			if (type.id.compareToIgnoreCase(id) == 0) {
				return type;
			}
		}
		return null;
	}

	public static List<SelectAreaDialogEventType> asList() {
		List<SelectAreaDialogEventType> list = Arrays.asList(values());
		return list;
	}

}
