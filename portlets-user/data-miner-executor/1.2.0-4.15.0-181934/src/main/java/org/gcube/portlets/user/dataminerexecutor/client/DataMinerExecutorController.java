package org.gcube.portlets.user.dataminerexecutor.client;

import org.gcube.data.analysis.dataminermanagercl.shared.data.OutputData;
import org.gcube.data.analysis.dataminermanagercl.shared.data.computations.ComputationData;
import org.gcube.data.analysis.dataminermanagercl.shared.data.computations.ComputationId;
import org.gcube.data.analysis.dataminermanagercl.shared.service.ServiceInfo;
import org.gcube.data.analysis.dataminermanagercl.shared.workspace.ItemDescription;
import org.gcube.portlets.user.dataminerexecutor.client.common.EventBusProvider;
import org.gcube.portlets.user.dataminerexecutor.client.events.CancelComputationExecutionRequestEvent;
import org.gcube.portlets.user.dataminerexecutor.client.events.CancelExecutionFromComputationsRequestEvent;
import org.gcube.portlets.user.dataminerexecutor.client.events.ComputationDataEvent;
import org.gcube.portlets.user.dataminerexecutor.client.events.ComputationDataRequestEvent;
import org.gcube.portlets.user.dataminerexecutor.client.events.DeleteItemRequestEvent;
import org.gcube.portlets.user.dataminerexecutor.client.events.InvocationModelEvent;
import org.gcube.portlets.user.dataminerexecutor.client.events.InvocationModelRequestEvent;
import org.gcube.portlets.user.dataminerexecutor.client.events.MenuEvent;
import org.gcube.portlets.user.dataminerexecutor.client.events.MenuSwitchEvent;
import org.gcube.portlets.user.dataminerexecutor.client.events.OutputDataEvent;
import org.gcube.portlets.user.dataminerexecutor.client.events.OutputDataRequestEvent;
import org.gcube.portlets.user.dataminerexecutor.client.events.ResubmitComputationExecutionEvent;
import org.gcube.portlets.user.dataminerexecutor.client.events.ResubmitComputationExecutionRequestEvent;
import org.gcube.portlets.user.dataminerexecutor.client.events.ServiceInfoEvent;
import org.gcube.portlets.user.dataminerexecutor.client.events.ServiceInfoRequestEvent;
import org.gcube.portlets.user.dataminerexecutor.client.events.SessionExpiredEvent;
import org.gcube.portlets.user.dataminerexecutor.client.events.StartComputationExecutionEvent;
import org.gcube.portlets.user.dataminerexecutor.client.events.StartComputationExecutionRequestEvent;
import org.gcube.portlets.user.dataminerexecutor.client.monitor.StatusMonitor;
import org.gcube.portlets.user.dataminerexecutor.client.rpc.DataMinerExecutorServiceAsync;
import org.gcube.portlets.user.dataminerexecutor.client.type.MenuType;
import org.gcube.portlets.user.dataminerexecutor.client.util.InfoMessageBox;
import org.gcube.portlets.user.dataminerexecutor.client.util.UtilsGXT3;
import org.gcube.portlets.user.dataminerexecutor.shared.Constants;
import org.gcube.portlets.user.dataminerexecutor.shared.exception.SessionExpiredServiceException;
import org.gcube.portlets.user.dataminerexecutor.shared.process.InvocationModel;
import org.gcube.portlets.user.dataminerexecutor.shared.session.UserInfo;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.widget.core.client.box.AutoProgressMessageBox;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;

/**
 * 
 * @author Giancarlo Panichi
 *
 *
 */
public class DataMinerExecutorController {
	private UserInfo userInfo;
	private String invocationModelFileUrl;

	public DataMinerExecutorController() {
		restoreUISession();
		bind();
		callHello();
		checkSession();
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public String getDataMinerInvocationModelFileUrl() {
		return invocationModelFileUrl;
	}

	private void checkSession() {
		// if you do not need to something when the session expire
		// CheckSession.getInstance().startPolling();
	}

	private void sessionExpiredShow() {
		// CheckSession.showLogoutDialog();
	}

	/*
	 * private void sessionExpiredShowDelayed() { Timer timeoutTimer = new
	 * Timer() { public void run() { sessionExpiredShow();
	 * 
	 * } }; int TIMEOUT = 3; // 3 second timeout
	 * 
	 * timeoutTimer.schedule(TIMEOUT * 1000); // timeout is in milliseconds
	 * 
	 * }
	 */

	private void bind() {

		EventBusProvider.INSTANCE.addHandler(SessionExpiredEvent.TYPE,
				new SessionExpiredEvent.SessionExpiredEventHandler() {

					@Override
					public void onChange(SessionExpiredEvent event) {
						Log.debug("Catch SessionExpiredEvent");
						sessionExpiredShow();

					}
				});

		EventBusProvider.INSTANCE.addHandler(MenuEvent.TYPE, new MenuEvent.MenuEventHandler() {

			@Override
			public void onSelect(MenuEvent event) {
				Log.debug("Catch MenuEvent:" + event);
				// manageMenuEvent(event);

			}
		});

		EventBusProvider.INSTANCE.addHandler(StartComputationExecutionRequestEvent.TYPE,
				new StartComputationExecutionRequestEvent.StartComputationExecutionRequestEventHandler() {

					@Override
					public void onStart(StartComputationExecutionRequestEvent event) {
						Log.debug("Catch StartComputationExecutionRequestEvent: " + event);
						startComputationRequest(event);

					}
				});

		EventBusProvider.INSTANCE.addHandler(CancelComputationExecutionRequestEvent.TYPE,
				new CancelComputationExecutionRequestEvent.CancelComputationExecutionRequestEventHandler() {

					@Override
					public void onCancel(CancelComputationExecutionRequestEvent event) {
						Log.debug("Catch CancelComputationRequestEvent: " + event);
						cancelComputationRequest(event);

					}
				});

		EventBusProvider.INSTANCE.addHandler(CancelExecutionFromComputationsRequestEvent.TYPE,
				new CancelExecutionFromComputationsRequestEvent.CancelExecutionFromComputationsRequestEventHandler() {

					@Override
					public void onCancel(CancelExecutionFromComputationsRequestEvent event) {
						Log.debug("Catch CancelExecutionFromComputationsRequestEvent: " + event);
						cancelExecutionFromComputationsRequest(event);

					}

				});

		EventBusProvider.INSTANCE.addHandler(ResubmitComputationExecutionRequestEvent.TYPE,
				new ResubmitComputationExecutionRequestEvent.ResubmitComputationExecutionRequestEventHandler() {

					@Override
					public void onResubmit(ResubmitComputationExecutionRequestEvent event) {
						Log.debug("Catch ResubmitComputationExecutionRequestEvent: " + event);
						resubmitComputationRequest(event);

					}

				});

		EventBusProvider.INSTANCE.addHandler(OutputDataRequestEvent.TYPE,
				new OutputDataRequestEvent.OutputDataRequestEventHandler() {

					@Override
					public void onOutputRequest(OutputDataRequestEvent event) {
						Log.debug("Catch OutputDataRequestEvent: " + event);
						manageOutputDataRequestEvent(event);

					}

				});

		EventBusProvider.INSTANCE.addHandler(ComputationDataRequestEvent.TYPE,
				new ComputationDataRequestEvent.ComputationDataRequestEventHandler() {

					@Override
					public void onComputationDataRequest(ComputationDataRequestEvent event) {
						Log.debug("Catch ComputationDataRequestEvent: " + event);
						manageComputationDataRequestEvent(event);

					}

				});

		EventBusProvider.INSTANCE.addHandler(InvocationModelRequestEvent.TYPE,
				new InvocationModelRequestEvent.InvocationModelRequestEventHandler() {

					@Override
					public void onInvocationRequest(InvocationModelRequestEvent event) {
						Log.debug("Catch InvocationModelRequestEvent: " + event);
						getDataMinerInvocationModelRequest();

					}
				});

		EventBusProvider.INSTANCE.addHandler(DeleteItemRequestEvent.TYPE,
				new DeleteItemRequestEvent.DeleteItemRequestEventHandler() {

					@Override
					public void onDeleteRequest(DeleteItemRequestEvent event) {
						Log.debug("Catch DeleteItemRequestEvent: " + event);
						// deleteItemRequest(event);

					}

				});

		EventBusProvider.INSTANCE.addHandler(ServiceInfoRequestEvent.TYPE,
				new ServiceInfoRequestEvent.ServiceInfoRequestEventHandler() {

					@Override
					public void onRequest(ServiceInfoRequestEvent event) {
						Log.debug("Catch EnvironmentRequestEvent: " + event);
						retrieveEnvironment(event);

					}
				});

	}

	private void restoreUISession() {
		// checkLocale();
		invocationModelFileUrl = com.google.gwt.user.client.Window.Location
				.getParameter(Constants.DATA_MINER_EXECUTOR_INVOCATION_MODEL);
	}

	private void callHello() {

		DataMinerExecutorServiceAsync.INSTANCE.hello(new AsyncCallback<UserInfo>() {

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof SessionExpiredServiceException) {
					UtilsGXT3.alert("Error", "Expired Session!");
					EventBusProvider.INSTANCE.fireEvent(new SessionExpiredEvent());
				} else {
					UtilsGXT3.alert("Error", "No user found: " + caught.getLocalizedMessage());
				}
			}

			@Override
			public void onSuccess(UserInfo result) {
				userInfo = result;
				Log.info("Hello: " + userInfo.getUsername());
			}

		});

	}

	/*
	 * private void manageMenuEvent(MenuEvent event) {
	 * Log.debug("CurrentVisualization=" + currentVisualization); if (event ==
	 * null || event.getMenuType() == null || (currentVisualization == null &&
	 * event.getMenuType().compareTo(MenuType.HOME) == 0) ||
	 * (currentVisualization != null &&
	 * event.getMenuType().compareTo(currentVisualization) == 0)) {
	 * 
	 * return; } currentVisualization = event.getMenuType(); MenuSwitchEvent
	 * menuSwitchEvent = new MenuSwitchEvent(event.getMenuType());
	 * EventBusProvider.INSTANCE.fireEvent(menuSwitchEvent); }
	 */

	private void startComputationRequest(final StartComputationExecutionRequestEvent event) {
		DataMinerExecutorServiceAsync.INSTANCE.startComputation(event.getOp(), new AsyncCallback<ComputationId>() {

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof SessionExpiredServiceException) {
					UtilsGXT3.alert("Error", "Expired Session!");
					EventBusProvider.INSTANCE.fireEvent(new SessionExpiredEvent());
				} else {
					UtilsGXT3.alert("Error", "Failed start computation " + event.getOp().getName() + "! "
							+ caught.getLocalizedMessage());
					caught.printStackTrace();
				}
			}

			@Override
			public void onSuccess(ComputationId computationId) {
				if (computationId == null)
					UtilsGXT3.alert("Error",
							"Failed start computation " + event.getOp().getName() + ", the computation id is null!");
				else {
					startComputation(computationId, event.getComputationStatusPanelIndex());
				}
			}
		});

	}

	private void startComputation(ComputationId computationId, int computationStatusPanelIndex) {
		StartComputationExecutionEvent event = new StartComputationExecutionEvent(computationId,
				computationStatusPanelIndex);
		EventBusProvider.INSTANCE.fireEvent(event);
	}

	private void cancelExecutionFromComputationsRequest(CancelExecutionFromComputationsRequestEvent event) {
		final ItemDescription itemDescription = event.getItemDescription();
		DataMinerExecutorServiceAsync.INSTANCE.cancelComputation(itemDescription, new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof SessionExpiredServiceException) {
					UtilsGXT3.alert("Error", "Expired Session!");
					EventBusProvider.INSTANCE.fireEvent(new SessionExpiredEvent());
				} else {
					Log.error("Error in cancenExecutionFromComputations:" + caught.getLocalizedMessage());
					UtilsGXT3.alert("Error", "Error in cancel computation " + itemDescription.getName() + ": "
							+ caught.getLocalizedMessage());
				}

			}

			@Override
			public void onSuccess(String result) {
				Log.debug("Computation Cancelled!");
				final InfoMessageBox d = new InfoMessageBox("Info",
						"Computation cancellation request has been accepted!");
				d.addHideHandler(new HideHandler() {

					public void onHide(HideEvent event) {
						// fireRefreshDataMinerWorkAreaEvent(DataMinerWorkAreaElementType.Computations);
					}
				});
				d.show();

			}
		});

	}

	private void cancelComputationRequest(CancelComputationExecutionRequestEvent event) {
		final ComputationId computationId = event.getComputationId();
		DataMinerExecutorServiceAsync.INSTANCE.cancelComputation(computationId, new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof SessionExpiredServiceException) {
					UtilsGXT3.alert("Error", "Expired Session!");
					EventBusProvider.INSTANCE.fireEvent(new SessionExpiredEvent());
				} else {
					UtilsGXT3.alert("Error", "Error in cancel computation " + computationId.getId() + ": "
							+ caught.getLocalizedMessage());
				}

			}

			@Override
			public void onSuccess(String result) {
				Log.debug("Computation Cancelled!");

			}
		});
	}

	private void resubmitComputationRequest(final ResubmitComputationExecutionRequestEvent event) {
		MenuSwitchEvent menuSwitchEvent = new MenuSwitchEvent(MenuType.EXPERIMENT);
		EventBusProvider.INSTANCE.fireEvent(menuSwitchEvent);

		DataMinerExecutorServiceAsync.INSTANCE.resubmit(event.getItemDescription(), new AsyncCallback<ComputationId>() {

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof SessionExpiredServiceException) {
					UtilsGXT3.alert("Error", "Expired Session!");
					EventBusProvider.INSTANCE.fireEvent(new SessionExpiredEvent());
				} else {
					UtilsGXT3.alert("Error", "Failed to resubmit computation: " + caught.getMessage());
				}
			}

			@Override
			public void onSuccess(ComputationId result) {
				if (result == null)
					UtilsGXT3.alert("Error", "Failed to resubmit computation, the computation id is null!");
				else {
					resubmitComputation(result);
				}

			}
		});
	}

	private void resubmitComputation(ComputationId computationId) {
		ResubmitComputationExecutionEvent event = new ResubmitComputationExecutionEvent(computationId);
		EventBusProvider.INSTANCE.fireEvent(event);
	}

	private void getDataMinerInvocationModelRequest() {
		if (invocationModelFileUrl == null || invocationModelFileUrl.isEmpty()) {
			Log.error("Invalid request, the invocation model has not been specified correctly: "
					+ invocationModelFileUrl);
			UtilsGXT3.alert("Error", "Invalid request, the invocation model has not been specified correctly!");
		} else {
			final AutoProgressMessageBox messageBox = new AutoProgressMessageBox("Retrieve Info",
					"Retrieving information, please wait...");
			messageBox.setProgressText("Loading...");
			messageBox.auto();
			messageBox.show();

			DataMinerExecutorServiceAsync.INSTANCE.getInvocationModel(invocationModelFileUrl,
					new AsyncCallback<InvocationModel>() {

						@Override
						public void onFailure(Throwable caught) {
							messageBox.hide();
							if (caught instanceof SessionExpiredServiceException) {
								UtilsGXT3.alert("Error", "Expired Session");
								EventBusProvider.INSTANCE.fireEvent(new SessionExpiredEvent());
							} else {
								UtilsGXT3.alert("Error",
										"The VRE currently has problems loading the required operator using the invocation model specified!");
								Log.error(
										"The VRE currently has problems loading the required operator using the invocation model specified: "
												+ caught.getLocalizedMessage(),
										caught);

							}

						}

						@Override
						public void onSuccess(InvocationModel invocationModel) {
							messageBox.hide();
							Log.debug("Invocation Retrieved: " + invocationModel);
							InvocationModelEvent event = new InvocationModelEvent(invocationModel);
							Log.debug("Fire: " + event);
							EventBusProvider.INSTANCE.fireEvent(event);
						}

					});
		}
	}

	private void manageOutputDataRequestEvent(OutputDataRequestEvent event) {
		if (event == null) {
			UtilsGXT3.alert("Error", "Invalid output request!");
		} else {
			if (event.getComputationId() == null || event.getComputationId().getId() == null
					|| event.getComputationId().getId().isEmpty()) {
				UtilsGXT3.alert("Error", "Invalid output request, computation id: " + event.getComputationId());
			} else {
				final StatusMonitor monitor = new StatusMonitor();
				DataMinerExecutorServiceAsync.INSTANCE.getOutputDataByComputationId(event.getComputationId(),
						new AsyncCallback<OutputData>() {
							@Override
							public void onSuccess(OutputData outputData) {
								monitor.hide();
								fireOutputDataEvent(outputData);
							}

							@Override
							public void onFailure(Throwable caught) {
								monitor.hide();
								Log.error("Error in getResourceByComputationId: " + caught.getLocalizedMessage());
								UtilsGXT3.alert("Error",
										"Impossible to retrieve output info. " + caught.getLocalizedMessage());

							}
						});
			}
		}
	}

	private void fireOutputDataEvent(OutputData outputData) {
		OutputDataEvent event = new OutputDataEvent(outputData);
		EventBusProvider.INSTANCE.fireEvent(event);

	}

	private void manageComputationDataRequestEvent(ComputationDataRequestEvent event) {
		if (event == null) {
			UtilsGXT3.alert("Error", "Invalid computation info request!");
		} else {
			if (event.getItemDescription() == null || event.getItemDescription().getId() == null
					|| event.getItemDescription().getId().isEmpty()) {
				UtilsGXT3.alert("Error",
						"Invalid computation info request, item description: " + event.getItemDescription());
			} else {
				final StatusMonitor monitor = new StatusMonitor();
				DataMinerExecutorServiceAsync.INSTANCE.getComputationData(event.getItemDescription(),
						new AsyncCallback<ComputationData>() {
							@Override
							public void onSuccess(ComputationData computationData) {
								monitor.hide();
								fireComputationDataEvent(computationData);
							}

							@Override
							public void onFailure(Throwable caught) {
								monitor.hide();
								Log.error("Error in getComputationData: " + caught.getLocalizedMessage());
								caught.printStackTrace();
								UtilsGXT3.alert("Error",
										"Impossible to retrieve computation info. " + caught.getLocalizedMessage());

							}
						});
			}
		}
	}

	private void fireComputationDataEvent(ComputationData computationData) {
		ComputationDataEvent event = new ComputationDataEvent(computationData);
		EventBusProvider.INSTANCE.fireEvent(event);

	}
	
	private void retrieveEnvironment(final ServiceInfoRequestEvent event) {
		DataMinerExecutorServiceAsync.INSTANCE.getServiceInfo(new AsyncCallback<ServiceInfo>() {

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof SessionExpiredServiceException) {
					UtilsGXT3.alert("Error", "Expired Session");
					EventBusProvider.INSTANCE.fireEvent(new SessionExpiredEvent());
				} else {
					UtilsGXT3.alert("Error",
							"Error retrieving DataMiner service info: " + caught.getLocalizedMessage());
				}
			}

			@Override
			public void onSuccess(ServiceInfo serviceInfo) {
				Log.debug("DataMiner Service Info: " + serviceInfo);

				ServiceInfoEvent serviceInfoEvent = new ServiceInfoEvent(serviceInfo);
				EventBusProvider.INSTANCE.fireEvent(serviceInfoEvent);

			}

		});

	}

}