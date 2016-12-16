package org.gcube.portlets.admin.accountingmanager.client.state;

import java.io.Serializable;
import java.util.ArrayList;

import org.gcube.portlets.admin.accountingmanager.shared.data.AccountingType;
import org.gcube.portlets.admin.accountingmanager.shared.data.Context;
import org.gcube.portlets.admin.accountingmanager.shared.data.FilterKey;
import org.gcube.portlets.admin.accountingmanager.shared.data.query.SeriesRequest;
import org.gcube.portlets.admin.accountingmanager.shared.data.response.SeriesResponse;

/**
 * 
 * @author giancarlo email: <a
 *         href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a>
 *
 */
public class AccountingClientStateData implements Serializable {

	private static final long serialVersionUID = -2080165745912743812L;
	private AccountingType accountingType;
	private SeriesRequest seriesRequest;
	private SeriesResponse seriesResponse;
	private ArrayList<FilterKey> availableFilterKeys;
	private Context availableContext;

	public AccountingClientStateData() {
		super();
	}

	public AccountingClientStateData(AccountingType accountingType,
			SeriesRequest seriesRequest, SeriesResponse seriesResponse,
			ArrayList<FilterKey> availableFilterKeys) {
		super();
		this.accountingType = accountingType;
		this.seriesRequest = seriesRequest;
		this.seriesResponse = seriesResponse;
		this.availableFilterKeys = availableFilterKeys;
	}

	public AccountingType getAccountingType() {
		return accountingType;
	}

	public void setAccountingType(AccountingType accountingType) {
		this.accountingType = accountingType;
	}

	public SeriesRequest getSeriesRequest() {
		return seriesRequest;
	}

	public void setSeriesRequest(SeriesRequest seriesRequest) {
		this.seriesRequest = seriesRequest;
	}

	public SeriesResponse getSeriesResponse() {
		return seriesResponse;
	}

	public void setSeriesResponse(SeriesResponse seriesResponse) {
		this.seriesResponse = seriesResponse;
	}

	public ArrayList<FilterKey> getAvailableFilterKeys() {
		return availableFilterKeys;
	}

	public void setAvailableFilterKeys(ArrayList<FilterKey> availableFilterKeys) {
		this.availableFilterKeys = availableFilterKeys;
	}

	public Context getAvailableContext() {
		return availableContext;
	}

	public void setAvailableContext(Context availableContext) {
		this.availableContext = availableContext;
	}

	@Override
	public String toString() {
		return "AccountingClientStateData [accountingType=" + accountingType
				+ ", seriesRequest=" + seriesRequest + ", seriesResponse="
				+ seriesResponse + ", availableFilterKeys="
				+ availableFilterKeys + ", availableContext="
				+ availableContext + "]";
	}

}
