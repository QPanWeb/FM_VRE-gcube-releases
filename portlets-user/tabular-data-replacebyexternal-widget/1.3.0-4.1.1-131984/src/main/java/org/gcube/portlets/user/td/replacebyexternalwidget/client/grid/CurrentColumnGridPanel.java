package org.gcube.portlets.user.td.replacebyexternalwidget.client.grid;

import java.util.ArrayList;
import java.util.List;

import org.gcube.portlets.user.td.gwtservice.client.rpc.TDGWTServiceAsync;
import org.gcube.portlets.user.td.gwtservice.shared.exception.TDGWTIsLockedException;
import org.gcube.portlets.user.td.gwtservice.shared.exception.TDGWTSessionExpiredException;
import org.gcube.portlets.user.td.replacebyexternalwidget.client.CurrentColumnSelectionCard;
import org.gcube.portlets.user.td.widgetcommonevent.client.event.SessionExpiredEvent;
import org.gcube.portlets.user.td.widgetcommonevent.client.type.SessionExpiredType;
import org.gcube.portlets.user.td.widgetcommonevent.shared.TRId;
import org.gcube.portlets.user.td.widgetcommonevent.shared.tr.column.ColumnData;
import org.gcube.portlets.user.td.wizardwidget.client.util.UtilsGXT3;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.dom.ScrollSupport.ScrollMode;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.event.StoreDataChangeEvent;
import com.sencha.gxt.data.shared.event.StoreDataChangeEvent.StoreDataChangeHandler;
import com.sencha.gxt.data.shared.loader.ListLoadConfig;
import com.sencha.gxt.data.shared.loader.ListLoadResult;
import com.sencha.gxt.data.shared.loader.ListLoadResultBean;
import com.sencha.gxt.data.shared.loader.ListLoader;
import com.sencha.gxt.data.shared.loader.LoadResultListStoreBinding;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.grid.CheckBoxSelectionModel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;

/**
 * 
 * @author giancarlo email: <a
 *         href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a>
 *
 */
public class CurrentColumnGridPanel extends ContentPanel implements
		HasSelectionHandlers<ColumnData> {
	// private static final String GRID_WIDTH ="524px";
	private static final String GRID_HEIGHT = "340px";
	private static final ColumnDataProperties props = GWT
			.create(ColumnDataProperties.class);
	private CheckBoxSelectionModel<ColumnData> sm;

	private Grid<ColumnData> grid;
	private CurrentColumnSelectionCard parent;
	private String columnName;
	private ColumnData columnSelected;
	private List<ColumnData> cols;

	/**
	 * 
	 * @param parent
	 */
	public CurrentColumnGridPanel(String columnName,
			CurrentColumnSelectionCard parent) {
		this.parent = parent;
		this.columnName = columnName;
		Log.debug("CurrentColumnGridPanel");
		setHeadingText("Columns");
		create();	
	}

	protected void create() {

		ColumnConfig<ColumnData, String> labelCol = new ColumnConfig<ColumnData, String>(
				props.label());

		IdentityValueProvider<ColumnData> identity = new IdentityValueProvider<ColumnData>();

		sm = new CheckBoxSelectionModel<ColumnData>(identity);

		List<ColumnConfig<ColumnData, ?>> l = new ArrayList<ColumnConfig<ColumnData, ?>>();
		l.add(sm.getColumn());
		l.add(labelCol);
		ColumnModel<ColumnData> cm = new ColumnModel<ColumnData>(l);

		ListStore<ColumnData> store = new ListStore<ColumnData>(props.id());

		store.addStoreDataChangeHandler(new StoreDataChangeHandler<ColumnData>() {

			@Override
			public void onDataChange(StoreDataChangeEvent<ColumnData> event) {
				List<ColumnData> cols = event.getSource().getAll();
				Log.debug("Columns:" + cols.size());
				dataChangeOnStore(cols);

			}
		});

		RpcProxy<ListLoadConfig, ListLoadResult<ColumnData>> proxy = new RpcProxy<ListLoadConfig, ListLoadResult<ColumnData>>() {

			public void load(ListLoadConfig loadConfig,
					final AsyncCallback<ListLoadResult<ColumnData>> callback) {
				loadData(loadConfig, callback);
			}
		};
		final ListLoader<ListLoadConfig, ListLoadResult<ColumnData>> loader = new ListLoader<ListLoadConfig, ListLoadResult<ColumnData>>(
				proxy);

		loader.setRemoteSort(false);
		loader.addLoadHandler(new LoadResultListStoreBinding<ListLoadConfig, ColumnData, ListLoadResult<ColumnData>>(
				store));

		grid = new Grid<ColumnData>(store, cm) {
			@Override
			protected void onAfterFirstAttach() {
				super.onAfterFirstAttach();
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					public void execute() {
						loader.load();
					}
				});
			}
		};

		sm.setSelectionMode(SelectionMode.SINGLE);
		grid.setSelectionModel(sm);
		grid.setLoader(loader);
		grid.setHeight(GRID_HEIGHT);
		// grid.setWidth(GRID_WIDTH);
		// grid.getView().setAutoExpandColumn(labelCol);
		grid.getView().setStripeRows(true);
		grid.getView().setColumnLines(true);
		grid.getView().setAutoFill(true);
		grid.setBorders(false);
		grid.setLoadMask(true);
		grid.setColumnReordering(false);

		VerticalLayoutContainer con = new VerticalLayoutContainer();
		con.setScrollMode(ScrollMode.AUTO);
		con.add(grid, new VerticalLayoutData(-1, -1, new Margins(0)));
		setWidget(con);

	}
	
	public Grid<ColumnData> getGrid() {
		return grid;
	}

	public ColumnData getSelectedItem() {
		return grid.getSelectionModel().getSelectedItem();

	}

	public HandlerRegistration addSelectionHandler(
			SelectionHandler<ColumnData> handler) {
		return grid.getSelectionModel().addSelectionHandler(handler);
	}

	protected void dataChangeOnStore(List<ColumnData> cols){
		this.cols=cols;
		if(columnName==null||columnName.isEmpty()){
			columnSelected=null;
		} else {
			retrieveColumnSelected();
		}
	}
	
	
	protected void updateInitialColumnSelected(){
		Log.debug("ColumnSelected: "+columnSelected);
		
		if (columnSelected != null) {
			String columnId;
			if(columnSelected.isViewColumn()){
				columnId=columnSelected.getColumnViewData().getSourceTableDimensionColumnId();
			} else {
				columnId=columnSelected.getColumnId();
			}
			for (ColumnData c : cols) {
				Log.debug("Column Retrieved: "+c);
				if (c.getColumnId().compareTo(columnId) == 0) {
					sm.select(c, true);
					sm.refresh();
					break;
				}
			}
		}
	}
	
	
	protected void loadData(ListLoadConfig loadConfig,
			final AsyncCallback<ListLoadResult<ColumnData>> callback) {
		TRId trId = parent.getReplaceByExternalSession().getTrId();

		TDGWTServiceAsync.INSTANCE.getColumns(trId,
				new AsyncCallback<ArrayList<ColumnData>>() {

					@Override
					public void onFailure(Throwable caught) {
						if (caught instanceof TDGWTSessionExpiredException) {

							parent.getEventBus()
									.fireEvent(
											new SessionExpiredEvent(
													SessionExpiredType.EXPIREDONSERVER));
						} else {
							if (caught instanceof TDGWTIsLockedException) {
								Log.error(caught.getLocalizedMessage());
								UtilsGXT3.alert("Error Locked",
										caught.getLocalizedMessage());

							} else {
								Log.debug("Error retrieving source columns: "
										+ caught.getLocalizedMessage());
								UtilsGXT3
										.alert("Error retrieving current columns",
												"Error retrieving current columns on server!");
							}
						}
						callback.onFailure(caught);

					}

					@Override
					public void onSuccess(ArrayList<ColumnData> result) {
						parent.getReplaceByExternalSession().setCurrentColumns(
								result);
						callback.onSuccess(new ListLoadResultBean<ColumnData>(
								result));

					}
				});

	}

	protected void retrieveColumnSelected() {
		TRId trId = parent.getReplaceByExternalSession().getTrId();
		TDGWTServiceAsync.INSTANCE.getColumn(trId, columnName,
				new AsyncCallback<ColumnData>() {

					@Override
					public void onFailure(Throwable caught) {
						if (caught instanceof TDGWTSessionExpiredException) {
							parent.getEventBus()
									.fireEvent(
											new SessionExpiredEvent(
													SessionExpiredType.EXPIREDONSERVER));
						} else {
							if (caught instanceof TDGWTIsLockedException) {
								Log.error(caught.getLocalizedMessage());
								UtilsGXT3.alert("Error Locked",
										caught.getLocalizedMessage());

							} else {
								Log.debug("Error retrieving column: "
										+ caught.getLocalizedMessage());
								UtilsGXT3
										.alert("Error retrieving current column",
												"Error retrieving current column on server!");
							}
						}

					}

					@Override
					public void onSuccess(ColumnData result) {
						columnSelected = result;
						updateInitialColumnSelected();
						
					}
				});

	}

}
