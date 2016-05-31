package org.gcube.portlets.admin.accountingmanager.client;

import java.util.ArrayList;
import java.util.Date;

import org.gcube.portlets.admin.accountingmanager.client.event.AccountingMenuEvent;
import org.gcube.portlets.admin.accountingmanager.client.event.FiltersChangeEvent;
import org.gcube.portlets.admin.accountingmanager.client.event.SessionExpiredEvent;
import org.gcube.portlets.admin.accountingmanager.client.event.StateChangeEvent;
import org.gcube.portlets.admin.accountingmanager.client.event.UIStateEvent;
import org.gcube.portlets.admin.accountingmanager.client.monitor.AccountingMonitor;
import org.gcube.portlets.admin.accountingmanager.client.rpc.AccountingManagerServiceAsync;
import org.gcube.portlets.admin.accountingmanager.client.state.AccountingState;
import org.gcube.portlets.admin.accountingmanager.client.state.AccountingStateData;
import org.gcube.portlets.admin.accountingmanager.client.type.SessionExpiredType;
import org.gcube.portlets.admin.accountingmanager.client.type.StateChangeType;
import org.gcube.portlets.admin.accountingmanager.client.type.UIStateType;
import org.gcube.portlets.admin.accountingmanager.client.utils.UtilsGXT3;
import org.gcube.portlets.admin.accountingmanager.shared.Constants;
import org.gcube.portlets.admin.accountingmanager.shared.data.AccountingFilterBasic;
import org.gcube.portlets.admin.accountingmanager.shared.data.AccountingPeriod;
import org.gcube.portlets.admin.accountingmanager.shared.data.AccountingPeriodMode;
import org.gcube.portlets.admin.accountingmanager.shared.data.AccountingType;
import org.gcube.portlets.admin.accountingmanager.shared.data.FilterKey;
import org.gcube.portlets.admin.accountingmanager.shared.data.query.SeriesRequest;
import org.gcube.portlets.admin.accountingmanager.shared.data.response.SeriesResponse;
import org.gcube.portlets.admin.accountingmanager.shared.exception.AccountingManagerSessionExpiredException;
import org.gcube.portlets.admin.accountingmanager.shared.session.UserInfo;
import org.gcube.portlets.widgets.sessionchecker.client.CheckSession;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;

/**
 * 
 * @author "Giancarlo Panichi" <a
 *         href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a>
 * 
 */
public class AccountingManagerController {

	private SimpleEventBus eventBus;
	private UserInfo userInfo;
	private AccountingState accountingState;
	private AccountingType accountingType;
	@SuppressWarnings("unused")
	private BorderLayoutContainer mainPanel;
	private AccountingMonitor accountingMonitor;

	public AccountingManagerController() {
		eventBus = new SimpleEventBus();
		accountingType = AccountingType.STORAGE;
		accountingState = new AccountingState();
		AccountingStateData accountingStateData = new AccountingStateData(
				accountingType, null, null, null);
		accountingState.setState(accountingType, accountingStateData);
		init();
	}

	private void init() {
		callHello();
		checkSession();
		bindToEvents();
	}

	private void checkSession() {
		// if you do not need to something when the session expire
		//CheckSession.getInstance().startPolling();
	}

	private void sessionExpiredShow() {
		CheckSession.showLogoutDialog();
	}

	/**
	 * @return the eventBus
	 */
	public EventBus getEventBus() {
		return eventBus;
	}

	public void setMainPanelLayout(BorderLayoutContainer mainPanel) {
		this.mainPanel = mainPanel;
	}

	private void callHello() {
		AccountingManagerServiceAsync.INSTANCE
				.hello(new AsyncCallback<UserInfo>() {

					@Override
					public void onFailure(Throwable caught) {
						Log.info("No valid user found: " + caught.getMessage());
						if (caught instanceof AccountingManagerSessionExpiredException) {
							UtilsGXT3.alert("Error", "Expired Session");
							sessionExpiredShowDelayed();
						} else {
							UtilsGXT3.alert("Error", "No user found");
						}
					}

					@Override
					public void onSuccess(UserInfo result) {
						userInfo = result;
						Log.info("Hello: " + userInfo.getUsername());

					}

				});

	}

	private void sessionExpiredShowDelayed() {
		Timer timeoutTimer = new Timer() {
			public void run() {
				sessionExpiredShow();

			}
		};
		int TIMEOUT = 3; // 3 second timeout

		timeoutTimer.schedule(TIMEOUT * 1000); // timeout is in milliseconds

	}

	private void checkLocale() {
		String[] locales = LocaleInfo.getAvailableLocaleNames();

		for (String locale : locales) {
			Log.debug("Locale avaible:" + locale);
		}

		String currentLocaleCookie = Cookies.getCookie(LocaleInfo
				.getLocaleCookieName());
		Log.debug(Constants.AM_LANG_COOKIE + ":" + currentLocaleCookie);

		LocaleInfo currentLocaleInfo = LocaleInfo.getCurrentLocale();
		Log.debug("Current Locale:" + currentLocaleInfo.getLocaleName());

	}

	protected void changeLanguage(String localeName) {
		Date now = new Date();
		long nowLong = now.getTime();
		nowLong = nowLong + (1000 * 60 * 60 * 24 * 21);
		now.setTime(nowLong);
		String cookieLang = Cookies.getCookie(Constants.AM_LANG_COOKIE);
		if (cookieLang != null) {
			Cookies.removeCookie(Constants.AM_LANG_COOKIE);
		}
		Cookies.setCookie(Constants.AM_LANG_COOKIE, localeName, now);
		com.google.gwt.user.client.Window.Location.reload();
	}

	//
	public void restoreUISession() {
		checkLocale();
		showDefault();
	}

	// Bind Controller to events on bus
	private void bindToEvents() {
		eventBus.addHandler(SessionExpiredEvent.TYPE,
				new SessionExpiredEvent.SessionExpiredEventHandler() {

					@Override
					public void onSessionExpired(SessionExpiredEvent event) {
						Log.debug("Catch Event SessionExpiredEvent");
						doSessionExpiredCommand(event);

					}
				});

		eventBus.addHandler(AccountingMenuEvent.TYPE,
				new AccountingMenuEvent.AccountingMenuEventHandler() {

					public void onMenu(AccountingMenuEvent event) {
						Log.debug("Catch Event AccountingMenuEvent");
						doMenuCommand(event);

					}
				});

		eventBus.addHandler(FiltersChangeEvent.TYPE,
				new FiltersChangeEvent.FiltersChangeEventHandler() {

					public void onFiltersChange(FiltersChangeEvent event) {
						Log.debug("Catch Event FiltersChangeEvent");
						doFiltersChangeCommand(event);

					}
				});

		eventBus.fireEvent(new UIStateEvent(UIStateType.START));

	}

	private void doMenuCommand(AccountingMenuEvent event) {
		AccountingStateData accountingStateData = null;
		if (event == null || event.getAccountingType() == null) {
			return;
		}
		switch (event.getAccountingType()) {
		case PORTLET:
		case SERVICE:
		case STORAGE:
		case TASK:
		case JOB:
			Log.debug("AccountingType: " + event.getAccountingType());
			accountingStateData = accountingState.getState(event
					.getAccountingType());
			if (accountingStateData == null) {
				createDefaultChart(event.getAccountingType());
			} else {
				accountingType = event.getAccountingType();
				StateChangeEvent stateChangeEvent = new StateChangeEvent(
						StateChangeType.Restore, accountingStateData);
				eventBus.fireEvent(stateChangeEvent);
			}
			break;
		default:
			break;
		}

	}

	public void showDefault() {
		createDefaultChart(AccountingType.STORAGE);
	}

	private void createDefaultChart(AccountingType accountingType) {
		accountingMonitor = new AccountingMonitor();
		Date now = new Date();
		DateTimeFormat dtf=DateTimeFormat.getFormat(PredefinedFormat.YEAR_MONTH_DAY);
		String currentDate=dtf.format(now);
		Date date=dtf.parse(currentDate);
		Date lastMonth=new Date(date.getTime());
		CalendarUtil.addMonthsToDate(lastMonth, -1);
		SeriesRequest seriesRequest = new SeriesRequest(new AccountingPeriod(dtf.format(lastMonth),
				dtf.format(date), AccountingPeriodMode.DAILY), new AccountingFilterBasic());
		Log.debug("DefaultSeriesRequest: "+seriesRequest);
		Log.debug("LastMoth= "+dtf.format(lastMonth)+" , date="+dtf.format(date));
		this.accountingType = accountingType;

		AccountingStateData accountingStateData = new AccountingStateData(
				accountingType, seriesRequest, null, null);
		accountingState.setState(accountingType, accountingStateData);

		retrieveFilterKey();

	}

	private void retrieveFilterKey() {

		AccountingManagerServiceAsync.INSTANCE.getFilterKeys(accountingType,
				new AsyncCallback<ArrayList<FilterKey>>() {

					@Override
					public void onFailure(Throwable caught) {
						accountingMonitor.hide();
						if (caught instanceof AccountingManagerSessionExpiredException) {
							eventBus.fireEvent(new SessionExpiredEvent(
									SessionExpiredType.EXPIREDONSERVER));
						} else {
							Log.error("Error retrieving filter keys for "
									+ accountingType + ":"
									+ caught.getLocalizedMessage());
							UtilsGXT3.alert("Error retrieving filter keys",
									caught.getLocalizedMessage());
						}
						

					}

					@Override
					public void onSuccess(ArrayList<FilterKey> result) {
						Log.debug("FilterKeys: " + result);
						AccountingStateData accountingStateData = accountingState
								.getState(accountingType);
						accountingStateData.setAvailableFilterKeys(result);
						accountingState.setState(accountingType,
								accountingStateData);
						callDefaultSeriesRequest();
					}
				});

	}

	private void callDefaultSeriesRequest() {

		AccountingManagerServiceAsync.INSTANCE.getSeries(accountingType,
				accountingState.getState(accountingType).getSeriesRequest(),
				new AsyncCallback<SeriesResponse>() {

					@Override
					public void onSuccess(SeriesResponse seriesResponse) {
						Log.debug("SeriesResponse: " + seriesResponse);
						AccountingStateData accountingStateData = accountingState
								.getState(accountingType);
						accountingStateData.setSeriesResponse(seriesResponse);
						accountingState.setState(accountingType,
								accountingStateData);
						StateChangeEvent stateChangeEvent = new StateChangeEvent(
								StateChangeType.Restore, accountingStateData);
						eventBus.fireEvent(stateChangeEvent);
						accountingMonitor.hide();
					}

					@Override
					public void onFailure(Throwable caught) {
						accountingMonitor.hide();
						if (caught instanceof AccountingManagerSessionExpiredException) {
							eventBus.fireEvent(new SessionExpiredEvent(
									SessionExpiredType.EXPIREDONSERVER));
						} else {
							Log.error("Error:" + caught.getLocalizedMessage());
							caught.printStackTrace();
							UtilsGXT3.alert("Error",
									caught.getLocalizedMessage());
							StateChangeEvent stateChangeEvent = new StateChangeEvent(
									StateChangeType.Restore, accountingState
											.getState(accountingType));
							eventBus.fireEvent(stateChangeEvent);

						}

					}
				});

	}

	private void doFiltersChangeCommand(FiltersChangeEvent event) {
		if (event == null || event.getFiltersChangeType() == null) {
			return;
		}
		switch (event.getFiltersChangeType()) {
		case Update:
			SeriesRequest seriesRequest = event.getSeriesRequest();
			AccountingStateData accountingStateData = accountingState
					.getState(accountingType);
			if (accountingStateData != null) {
				accountingMonitor = new AccountingMonitor();
				accountingStateData.setSeriesRequest(seriesRequest);
				accountingState.setState(accountingType, accountingStateData);
				callSeriesRequest();

			}

			break;
		default:
			break;
		}

	}

	private void callSeriesRequest() {
		Log.debug("Call getSeries on server, params: " + accountingType + ", "
				+ accountingState.getState(accountingType).getSeriesRequest());

		AccountingManagerServiceAsync.INSTANCE.getSeries(accountingType,
				accountingState.getState(accountingType).getSeriesRequest(),
				new AsyncCallback<SeriesResponse>() {

					@Override
					public void onSuccess(SeriesResponse seriesResponse) {
						Log.debug("SeriesResponse: " + seriesResponse);
						AccountingStateData accountingStateData = accountingState
								.getState(accountingType);
						accountingStateData.setSeriesResponse(seriesResponse);
						accountingState.setState(accountingType,
								accountingStateData);
						StateChangeEvent stateChangeEvent = new StateChangeEvent(
								StateChangeType.Update, accountingStateData);
						eventBus.fireEvent(stateChangeEvent);
						accountingMonitor.hide();
					}

					@Override
					public void onFailure(Throwable caught) {
						accountingMonitor.hide();
						if (caught instanceof AccountingManagerSessionExpiredException) {
							eventBus.fireEvent(new SessionExpiredEvent(
									SessionExpiredType.EXPIREDONSERVER));
						} else {
							Log.error("Error:" + caught.getLocalizedMessage());
							UtilsGXT3.alert("Error",
									caught.getLocalizedMessage());
							caught.printStackTrace();
						}

					}
				});

	}

	private void doSessionExpiredCommand(SessionExpiredEvent event) {
		Log.debug("Session Expired Event: " + event.getSessionExpiredType());
		sessionExpiredShow();

	}

	@SuppressWarnings("unused")
	private void asyncCodeLoadingFailed(Throwable reason) {
		Log.error("Async code loading failed", reason);
		eventBus.fireEvent(new SessionExpiredEvent(
				SessionExpiredType.EXPIREDONSERVER));

	}

}
