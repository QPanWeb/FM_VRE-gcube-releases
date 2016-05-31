package org.gcube.portlets.admin.accountingmanager.server.amservice.query;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.gcube.accounting.analytics.Filter;
import org.gcube.accounting.analytics.TemporalConstraint;
import org.gcube.accounting.datamodel.aggregation.AggregatedStorageUsageRecord;
import org.gcube.portlets.admin.accountingmanager.server.amservice.PeriodModeMap;
import org.gcube.portlets.admin.accountingmanager.shared.data.AccountingFilter;
import org.gcube.portlets.admin.accountingmanager.shared.data.AccountingFilterBasic;
import org.gcube.portlets.admin.accountingmanager.shared.data.AccountingFilterTop;
import org.gcube.portlets.admin.accountingmanager.shared.data.query.SeriesRequest;
import org.gcube.portlets.admin.accountingmanager.shared.exception.AccountingManagerServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Accounting Query 4 Storage
 * 
 * @author "Giancarlo Panichi" email: <a
 *         href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a>
 * 
 */
public class AccountingQuery4Storage extends AccountingQueryBuilder {
	protected static Logger logger = LoggerFactory
			.getLogger(AccountingQuery4Storage.class);
	private SeriesRequest seriesRequest;

	public AccountingQuery4Storage(SeriesRequest seriesRequest) {
		this.seriesRequest = seriesRequest;
	}

	@Override
	public void buildOpEx() throws AccountingManagerServiceException {

		Calendar startCalendar = GregorianCalendar
				.getInstance(TemporalConstraint.DEFAULT_TIME_ZONE);
		try {
			startCalendar.setTime(sdf.parse(seriesRequest.getAccountingPeriod()
					.getStartDate()));
		} catch (ParseException e) {
			e.printStackTrace();
			throw new AccountingManagerServiceException("Start Date not valid!");
		}

		Calendar endCalendar = GregorianCalendar
				.getInstance(TemporalConstraint.DEFAULT_TIME_ZONE);
		try {
			endCalendar.setTime(sdf.parse(seriesRequest.getAccountingPeriod()
					.getEndDate()));
		} catch (ParseException e) {
			e.printStackTrace();
			throw new AccountingManagerServiceException("End Date not valid!");
		}

		endCalendar.set(GregorianCalendar.HOUR_OF_DAY, 23);
		endCalendar.set(GregorianCalendar.MINUTE, 59);
		endCalendar.set(GregorianCalendar.SECOND, 59);
		endCalendar.set(GregorianCalendar.MILLISECOND, 999);

		TemporalConstraint temporalConstraint = new TemporalConstraint(
				startCalendar.getTimeInMillis(), endCalendar.getTimeInMillis(),
				PeriodModeMap.getMode(seriesRequest.getAccountingPeriod()
						.getPeriod()));

		ArrayList<Filter> filters = null;
		ArrayList<AccountingFilter> accountingFilters = null;
		AccountingQuery invocation = null;

		if (seriesRequest != null
				&& seriesRequest.getAccountingFilterDefinition() != null) {
			if (seriesRequest.getAccountingFilterDefinition() instanceof AccountingFilterBasic) {
				AccountingFilterBasic accountingFilterBasic = (AccountingFilterBasic) seriesRequest
						.getAccountingFilterDefinition();
				accountingFilters = accountingFilterBasic.getFilters();
				filters = new ArrayList<Filter>();
				if (accountingFilters != null) {
					for (AccountingFilter accountigFilters : accountingFilters) {
						Filter filter = new Filter(accountigFilters
								.getFilterKey().getKey(),
								accountigFilters.getFilterValue());
						filters.add(filter);
					}
				}
				invocation = new AccountingQueryBasic(
						AggregatedStorageUsageRecord.class, temporalConstraint,
						filters);

			} else {
				if (seriesRequest.getAccountingFilterDefinition() instanceof AccountingFilterTop) {
					AccountingFilterTop accountingFilterTop = (AccountingFilterTop) seriesRequest
							.getAccountingFilterDefinition();
					accountingFilters = accountingFilterTop.getFilters();
					filters = new ArrayList<Filter>();
					if (accountingFilters != null) {
						for (AccountingFilter accountigFilters : accountingFilters) {
							Filter filter = new Filter(accountigFilters
									.getFilterKey().getKey(),
									accountigFilters.getFilterValue());
							filters.add(filter);
						}
					}
					invocation = new AccountingQueryTop(
							AggregatedStorageUsageRecord.class,
							accountingFilterTop.getFilterKey(),
							accountingFilterTop.getTopNumber(),
							temporalConstraint, filters);
				} else {
					logger.error("Invalid Request: " + seriesRequest);
					throw new AccountingManagerServiceException(
							"Invalid Request!");
				}

			}
		} else {
			logger.error("Invalid Request: " + seriesRequest);
			throw new AccountingManagerServiceException("Invalid Request!");
		}

		accountingQuerySpec.setOp(invocation);

	}
}
