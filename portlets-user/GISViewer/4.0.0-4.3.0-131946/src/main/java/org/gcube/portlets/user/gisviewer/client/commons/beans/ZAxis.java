/**
 *
 */
package org.gcube.portlets.user.gisviewer.client.commons.beans;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;


/**
 * The Class LayerZAxis.
 *
 * @author Francesco Mangiacrapa francesco.mangiacrapa@isti.cnr.it
 * Feb 10, 2016
 */
public class ZAxis implements IsSerializable{

	private String units;
	private boolean positive;
	private List<Double> values;

	/**
	 * Instantiates a new layer z axis.
	 */
	public ZAxis() {
	}

	/**
	 * Instantiates a new layer z axis.
	 *
	 * @param units the units
	 * @param positive the positive
	 * @param values the values
	 */
	public ZAxis(String units, boolean positive, List<Double> values) {
		this.units = units;
		this.positive = positive;
		this.values = values;
	}


	/**
	 * Gets the units.
	 *
	 * @return the units
	 */
	public String getUnits() {

		return units;
	}


	/**
	 * Checks if is positive.
	 *
	 * @return the positive
	 */
	public boolean isPositive() {

		return positive;
	}


	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	public List<Double> getValues() {

		return values;
	}


	/**
	 * Sets the units.
	 *
	 * @param units the units to set
	 */
	public void setUnits(String units) {

		this.units = units;
	}


	/**
	 * Sets the positive.
	 *
	 * @param positive the positive to set
	 */
	public void setPositive(boolean positive) {

		this.positive = positive;
	}


	/**
	 * Sets the values.
	 *
	 * @param values the values to set
	 */
	public void setValues(List<Double> values) {

		this.values = values;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder();
		builder.append("ZAxis [units=");
		builder.append(units);
		builder.append(", positive=");
		builder.append(positive);
		builder.append(", values=");
		builder.append(values);
		builder.append("]");
		return builder.toString();
	}
}
