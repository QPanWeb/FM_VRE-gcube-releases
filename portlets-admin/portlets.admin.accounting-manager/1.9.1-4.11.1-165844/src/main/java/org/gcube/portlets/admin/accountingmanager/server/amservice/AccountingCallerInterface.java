package org.gcube.portlets.admin.accountingmanager.server.amservice;

import java.util.ArrayList;

import org.gcube.portlets.admin.accountingmanager.shared.data.AccountingType;
import org.gcube.portlets.admin.accountingmanager.shared.data.Spaces;
import org.gcube.portlets.admin.accountingmanager.shared.data.FilterKey;
import org.gcube.portlets.admin.accountingmanager.shared.data.FilterValuesRequest;
import org.gcube.portlets.admin.accountingmanager.shared.data.FilterValuesResponse;
import org.gcube.portlets.admin.accountingmanager.shared.data.query.SeriesRequest;
import org.gcube.portlets.admin.accountingmanager.shared.data.response.SeriesResponse;
import org.gcube.portlets.admin.accountingmanager.shared.exception.ServiceException;

/**
 * 
  * @author Giancarlo Panichi
 *
 *
 */
public interface AccountingCallerInterface {

	public ArrayList<FilterKey> getFilterKeys(AccountingType accountingType)
			throws ServiceException;

	public FilterValuesResponse getFilterValues(
			FilterValuesRequest filterValuesRequest) throws ServiceException;

	public Spaces getSpaces() throws ServiceException;

	public SeriesResponse getSeries(AccountingType accountingType,
			SeriesRequest seriesRequest) throws ServiceException;

}