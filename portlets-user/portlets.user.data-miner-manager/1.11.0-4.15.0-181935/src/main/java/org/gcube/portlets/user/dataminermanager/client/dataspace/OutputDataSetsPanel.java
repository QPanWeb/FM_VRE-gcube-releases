package org.gcube.portlets.user.dataminermanager.client.dataspace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gcube.data.analysis.dataminermanagercl.shared.workspace.ItemDescription;
import org.gcube.portlets.user.dataminermanager.client.DataMinerManager;
import org.gcube.portlets.user.dataminermanager.client.common.EventBusProvider;
import org.gcube.portlets.user.dataminermanager.client.events.DataMinerWorkAreaEvent;
import org.gcube.portlets.user.dataminermanager.client.events.DeleteItemRequestEvent;
import org.gcube.portlets.user.dataminermanager.client.events.RefreshDataMinerWorkAreaEvent;
import org.gcube.portlets.user.dataminermanager.client.events.SyncRefreshUploadDataSpaceEvent;
import org.gcube.portlets.user.dataminermanager.client.events.UIStateEvent;
import org.gcube.portlets.user.dataminermanager.client.type.DataMinerWorkAreaElementType;
import org.gcube.portlets.user.dataminermanager.client.util.UtilsGXT3;
import org.gcube.portlets.user.dataminermanager.client.workspace.DownloadWidget;
import org.gcube.portlets.widgets.workspaceuploader.client.WorkspaceUploadNotification.WorskpaceUploadNotificationListener;
import org.gcube.portlets.widgets.workspaceuploader.client.uploader.DialogUpload.UPLOAD_TYPE;
import org.gcube.portlets.widgets.workspaceuploader.client.uploader.dragdrop.MultipleDNDUpload;
import org.gcube.portlets.widgets.wsexplorer.client.explore.WorkspaceResourcesExplorerPanel;
import org.gcube.portlets.widgets.wsexplorer.client.notification.WorkspaceExplorerSelectNotification.WorskpaceExplorerSelectNotificationListener;
import org.gcube.portlets.widgets.wsexplorer.client.view.grid.ItemsTable.DISPLAY_FIELD;
import org.gcube.portlets.widgets.wsexplorer.shared.FilterCriteria;
import org.gcube.portlets.widgets.wsexplorer.shared.Item;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonScale;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutData;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

/**
 *
 * @author Giancarlo Panichi
 *
 *
 */
public class OutputDataSetsPanel extends FramedPanel {

	private Item selectedItem;
	private MultipleDNDUpload dnd;
	// private WorkspaceResourcesExplorerPanelPaginated
	// wsResourcesExplorerPanel;
	private WorkspaceResourcesExplorerPanel wsResourcesExplorerPanel;
	private TextButton btnDownload;
	private TextButton btnDelete;
	private TextButton btnRefresh;
	private VerticalLayoutContainer v;

	public OutputDataSetsPanel() {
		super();
		Log.debug("OutputDataSetsPanel");

		// msgs = GWT.create(ServiceCategoryMessages.class);
		init();
		bindToEvents();

	}

	private void init() {
		setItemId("OutputDataSetsPanel");
		forceLayoutOnResize = true;
		setBodyBorder(false);
		setBorders(false);
		setHeaderVisible(false);
		setResize(true);
		setAnimCollapse(false);
		setCollapsible(false);
		setHeadingText("Output Data Sets");
		setBodyStyle("backgroundColor:white;");
	}

	private void bindToEvents() {

		EventBusProvider.INSTANCE.addHandler(UIStateEvent.TYPE, new UIStateEvent.UIStateEventHandler() {

			@Override
			public void onChange(UIStateEvent event) {
				manageStateEvents(event);

			}
		});

		EventBusProvider.INSTANCE.addHandler(DataMinerWorkAreaEvent.TYPE,
				new DataMinerWorkAreaEvent.DataMinerWorkAreaEventHandler() {

					@Override
					public void onChange(DataMinerWorkAreaEvent event) {
						manageDataMinerWorkAreaEvents(event);

					}

				});

		EventBusProvider.INSTANCE.addHandler(RefreshDataMinerWorkAreaEvent.TYPE,
				new RefreshDataMinerWorkAreaEvent.RefreshDataMinerWorkAreaEventHandler() {

					@Override
					public void onRefresh(RefreshDataMinerWorkAreaEvent event) {
						manageRefreshDataMinerWorkAreaEvents(event);

					}

				});

		EventBusProvider.INSTANCE.addHandler(SyncRefreshUploadDataSpaceEvent.TYPE,
				new SyncRefreshUploadDataSpaceEvent.SyncRefreshUploadDataSpaceEventHandler() {

					@Override
					public void onRefresh(SyncRefreshUploadDataSpaceEvent event) {
						manageSyncRefreshUploadDataSpaceEvents();

					}

				});

	}

	private void manageStateEvents(UIStateEvent event) {
		Log.debug("OutputDataSetsPanel recieved UIStateEvent: " + event);
		if (event == null) {
			Log.error("UIStateEvent");
			return;
		}
		switch (event.getUiStateType()) {
		case START:
			break;
		case WAITING:
			break;
		case UPDATE:
			break;
		default:
			break;
		}
	}

	private void manageDataMinerWorkAreaEvents(DataMinerWorkAreaEvent event) {
		Log.debug("OutputDataSetsPanel recieved DataMinerWorkAreaEvent: " + event);
		if (event == null) {
			Log.error("DataMinerWorkAreaEvent");
			return;
		}
		switch (event.getDataMinerWorkAreaRegionType()) {
		case Computations:
			break;
		case DataSets:
			manageMyDataMinerWorkAreaEvents(event);
			break;
		default:
			break;

		}
	}

	private void manageMyDataMinerWorkAreaEvents(DataMinerWorkAreaEvent event) {
		switch (event.getDataMinerWorkAreaEventType()) {
		case OPEN:
		case UPDATE:
			refreshWSResourceExplorerPanel(event);
			break;
		default:
			break;
		}

	}

	private void manageRefreshDataMinerWorkAreaEvents(RefreshDataMinerWorkAreaEvent event) {
		Log.debug("OutputDataSetsPanel recieved RefreshDataMinerWorkAreaEvent: " + event);
		if (event == null) {
			Log.error("RefreshDataMinerWorkAreaEvent");
			return;
		}
		switch (event.getDataMinerWorkAreaElementType()) {
		case Computations:
			break;
		case InputDataSets:
			break;
		case OutputDataSets:
			refreshWSResourceExplorerPanel();
			break;
		default:
			break;
		}

	}

	private void manageSyncRefreshUploadDataSpaceEvents() {
		Log.debug("OutputDataSetsPanel received SyncRefreshUploadDataSpaceEvents: ");
		refreshWSResourceExplorerPanel();
	}

	private void create(DataMinerWorkAreaEvent event) {
		Log.debug("Create OutputDataSetsPanel");
		Log.debug("DataMinerWorkAreaEvent: " + event);
		try {

			if (event == null || event.getDataMinerWorkArea() == null
					|| event.getDataMinerWorkArea().getOutputDataSets() == null
					|| event.getDataMinerWorkArea().getOutputDataSets().getFolder() == null
					|| event.getDataMinerWorkArea().getOutputDataSets().getFolder().getId() == null
					|| event.getDataMinerWorkArea().getOutputDataSets().getFolder().getId().isEmpty()) {
				if (v != null) {
					remove(v);
					forceLayout();
				}
				return;

			}

			List<String> showProperties = new ArrayList<String>();
			for (DataSpacePropertiesType dataSpaceProperties : DataSpacePropertiesType.values()) {
				showProperties.add(dataSpaceProperties.getLabel());
			}
			FilterCriteria filterCriteria = new FilterCriteria();
			Map<String, String> map = new HashMap<String, String>();
			filterCriteria.setRequiredProperties(map);

			Log.debug("Create wsResourcesExplorerPanel for Output: ["
					+ event.getDataMinerWorkArea().getOutputDataSets().getFolder().getId() + ", false, "
					+ showProperties + ", " + filterCriteria + ", true, " + DISPLAY_FIELD.CREATION_DATE + "]");

			wsResourcesExplorerPanel = new WorkspaceResourcesExplorerPanel(
					event.getDataMinerWorkArea().getOutputDataSets().getFolder().getId(), false, showProperties,
					filterCriteria, true, DISPLAY_FIELD.CREATION_DATE);

			WorskpaceExplorerSelectNotificationListener wsResourceExplorerListener = new WorskpaceExplorerSelectNotificationListener() {
				@Override
				public void onSelectedItem(Item item) {
					Log.debug("Listener Selected Item " + item);
					selectedItem = item;

				}

				@Override
				public void onFailed(Throwable throwable) {
					Log.error(throwable.getLocalizedMessage());
					throwable.printStackTrace();
				}

				@Override
				public void onAborted() {

				}

				@Override
				public void onNotValidSelection() {
					selectedItem = null;
				}
			};

			wsResourcesExplorerPanel.addWorkspaceExplorerSelectNotificationListener(wsResourceExplorerListener);
			wsResourcesExplorerPanel.ensureDebugId("wsResourceExplorerPanelForOutput");
			wsResourcesExplorerPanel.setHeightToInternalScroll(300);

			Log.debug("Define DND for OutputDataSetsPanel");

			// DND
			dnd = new MultipleDNDUpload();

			dnd.setParameters(event.getDataMinerWorkArea().getOutputDataSets().getFolder().getId(), UPLOAD_TYPE.File);
			dnd.addUniqueContainer(wsResourcesExplorerPanel);
			WorskpaceUploadNotificationListener workspaceUploaderListenerOutputDataSetsPanel = new WorskpaceUploadNotificationListener() {

				@Override
				public void onUploadCompleted(String parentId, String itemId) {
					Log.debug("OutputDataSetsPanel Upload completed: [parentID: " + parentId + ", itemId: " + itemId
							+ "]");
					SyncRefreshUploadDataSpaceEvent ev = new SyncRefreshUploadDataSpaceEvent();
					EventBusProvider.INSTANCE.fireEvent(ev);
					// wsResourcesExplorerPanel.refreshRootFolderView();
					// forceLayout();

				}

				@Override
				public void onUploadAborted(String parentId, String itemId) {
					Log.debug(
							"OutputDataSetsPanel Upload Aborted: [parentID: " + parentId + ", itemId: " + itemId + "]");
				}

				@Override
				public void onError(String parentId, String itemId, Throwable throwable) {
					Log.debug("OutputDataSetsPanel Upload Error: [parentID: " + parentId + ", itemId: " + itemId + "]");
					throwable.printStackTrace();
				}

				@Override
				public void onOverwriteCompleted(String parentId, String itemId) {
					Log.debug("OutputDataSetsPanel Upload Override Completed: [parentID: " + parentId + ", itemId: "
							+ itemId + "]");
					SyncRefreshUploadDataSpaceEvent ev = new SyncRefreshUploadDataSpaceEvent();
					EventBusProvider.INSTANCE.fireEvent(ev);
					// wsResourcesExplorerPanel.refreshRootFolderView();
					// forceLayout();
				}
			};

			dnd.addWorkspaceUploadNotificationListener(workspaceUploaderListenerOutputDataSetsPanel);

			Log.debug("Create ToolBar for OutputDatasetsPanel");
			// ToolBar
			btnDownload = new TextButton("Download");
			btnDownload.setIcon(DataMinerManager.resources.download());
			btnDownload.setScale(ButtonScale.SMALL);
			btnDownload.setIconAlign(IconAlign.LEFT);
			btnDownload.setToolTip("Download");
			btnDownload.addSelectHandler(new SelectHandler() {

				@Override
				public void onSelect(SelectEvent event) {
					downloadItem();
				}

			});

			btnDelete = new TextButton("Delete");
			btnDelete.setIcon(DataMinerManager.resources.deleteCircle());
			btnDelete.setScale(ButtonScale.SMALL);
			btnDelete.setIconAlign(IconAlign.LEFT);
			btnDelete.setToolTip("Delete");
			btnDelete.addSelectHandler(new SelectHandler() {

				@Override
				public void onSelect(SelectEvent event) {
					deleteItem(event);
				}

			});

			btnRefresh = new TextButton("Refresh");
			btnRefresh.setIcon(DataMinerManager.resources.refresh());
			btnRefresh.setScale(ButtonScale.SMALL);
			btnRefresh.setIconAlign(IconAlign.LEFT);
			btnRefresh.setToolTip("Refresh");
			btnRefresh.addSelectHandler(new SelectHandler() {

				@Override
				public void onSelect(SelectEvent event) {
					refreshWSResourceExplorerPanel();
				}

			});

			final ToolBar toolBar = new ToolBar();
			toolBar.add(btnDownload, new BoxLayoutData(new Margins(0)));
			toolBar.add(btnDelete, new BoxLayoutData(new Margins(0)));
			toolBar.add(btnRefresh, new BoxLayoutData(new Margins(0)));

			v = new VerticalLayoutContainer();
			v.setItemId("OutputDataSetsPanelVert");

			v.add(toolBar, new VerticalLayoutData(1, -1, new Margins(0)));
			v.add(dnd, new VerticalLayoutData(1, 1, new Margins(0)));
			add(v);

			v.addResizeHandler(new ResizeHandler() {

				@Override
				public void onResize(ResizeEvent event) {
					int scrollBarHeight = event.getHeight() - toolBar.getElement().getHeight(false);
					Log.debug("ScrollBarHeight: " + scrollBarHeight);
					wsResourcesExplorerPanel.setHeightToInternalScroll(scrollBarHeight);
					forceLayout();

				}
			});
			forceLayout();

		} catch (Throwable e) {
			Log.error("Error opening wsResourceExplorerPanel: " + e.getLocalizedMessage(), e);
			e.printStackTrace();
		}
	}

	private void refreshWSResourceExplorerPanel() {
		if (wsResourcesExplorerPanel != null) {
			try {
				wsResourcesExplorerPanel.refreshRootFolderView();
			} catch (Exception e) {
				Log.error("Error retrieving data: " + e.getLocalizedMessage());

			}
		}
	}

	private void refreshWSResourceExplorerPanel(DataMinerWorkAreaEvent event) {
		try {

			if (v != null) {
				remove(v);
				create(event);
			} else {
				create(event);
			}

		} catch (Throwable e) {
			Log.error("Error in OutputDataSetsPanel: " + e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	private void deleteItem(SelectEvent event) {
		Log.debug("Selected Item: " + selectedItem);
		if (selectedItem != null) {

			ItemDescription itemDescription = new ItemDescription(selectedItem.getId(), selectedItem.getName(),
					selectedItem.getOwner(), selectedItem.getPath(), selectedItem.getType().name());
			DeleteItemRequestEvent deleteItemEvent = new DeleteItemRequestEvent(
					DataMinerWorkAreaElementType.OutputDataSets, itemDescription);
			EventBusProvider.INSTANCE.fireEvent(deleteItemEvent);
			Log.debug("Fired: " + deleteItemEvent);
		} else {
			UtilsGXT3.info("Attention", "Select a item!");
		}
	}

	private void downloadItem() {
		if (selectedItem != null) {
			DownloadWidget downloadWidget = new DownloadWidget();
			downloadWidget.download(selectedItem.getId());
		} else {
			UtilsGXT3.info("Attention", "Select a element!");
		}
	}

}
