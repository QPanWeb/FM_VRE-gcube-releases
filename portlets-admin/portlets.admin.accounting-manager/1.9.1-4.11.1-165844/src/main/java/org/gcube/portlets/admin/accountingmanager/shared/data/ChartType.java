package org.gcube.portlets.admin.accountingmanager.shared.data;

import java.util.Arrays;
import java.util.List;

/**
 * Chart Type
 * 
 * 
 * @author Giancarlo Panichi
 *
 *
 */

public enum ChartType {

	Basic("Basic"), Top("Top"), Context("Context"), Spaces("Spaces");

	/**
	 * 
	 * @param id
	 */
	private ChartType(final String id) {
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
	 *            id
	 * @return chart type
	 */
	public static ChartType getFromId(String id) {
		if (id == null || id.isEmpty())
			return null;

		for (ChartType columnDataType : values()) {
			if (columnDataType.id.compareToIgnoreCase(id) == 0) {
				return columnDataType;
			}
		}
		return null;
	}

	public static List<ChartType> asList() {
		List<ChartType> list = Arrays.asList(values());
		return list;
	}

}
