package org.gcube.portlets.user.td.gwtservice.shared.tr.resources;

/**
 * 
 * @author Giancarlo Panichi
 *
 *
 */
public class TableResourceTD extends ResourceTD {
	private static final long serialVersionUID = -3230438212164027113L;

	private long tableId;
	private String tableStringValue;

	public TableResourceTD() {
		super();
	}

	public TableResourceTD(long tableId, String tableStringValue) {
		super(tableStringValue);
		this.tableId = tableId;
		this.tableStringValue = tableStringValue;

	}

	@Override
	public String getStringValue() {
		return tableStringValue;
	}

	public long getTableId() {
		return tableId;
	}

	public void setTableId(long tableId) {
		this.tableId = tableId;
	}

	@Override
	public String toString() {
		return "TableResourceTD [tableId=" + tableId + ", tableStringValue=" + tableStringValue + "]";
	}

}
