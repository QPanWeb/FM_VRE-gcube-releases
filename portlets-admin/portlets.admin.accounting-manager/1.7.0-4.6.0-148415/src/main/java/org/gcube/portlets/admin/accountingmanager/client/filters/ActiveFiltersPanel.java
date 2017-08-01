package org.gcube.portlets.admin.accountingmanager.client.filters;

import java.util.ArrayList;
import java.util.List;

import org.gcube.portlets.admin.accountingmanager.client.event.AccountingPeriodEvent;
import org.gcube.portlets.admin.accountingmanager.client.event.AccountingPeriodRequestEvent;
import org.gcube.portlets.admin.accountingmanager.client.event.SessionExpiredEvent;
import org.gcube.portlets.admin.accountingmanager.client.event.StateChangeEvent;
import org.gcube.portlets.admin.accountingmanager.client.monitor.AccountingMonitor;
import org.gcube.portlets.admin.accountingmanager.client.properties.AccountingFilterProperties;
import org.gcube.portlets.admin.accountingmanager.client.properties.GenresDataProperties;
import org.gcube.portlets.admin.accountingmanager.client.resource.AccountingManagerResources;
import org.gcube.portlets.admin.accountingmanager.client.rpc.AccountingManagerServiceAsync;
import org.gcube.portlets.admin.accountingmanager.client.state.AccountingClientStateData;
import org.gcube.portlets.admin.accountingmanager.client.type.SessionExpiredType;
import org.gcube.portlets.admin.accountingmanager.client.util.UtilsGXT3;
import org.gcube.portlets.admin.accountingmanager.shared.data.AccountingFilter;
import org.gcube.portlets.admin.accountingmanager.shared.data.AccountingFilterBasic;
import org.gcube.portlets.admin.accountingmanager.shared.data.AccountingFilterContext;
import org.gcube.portlets.admin.accountingmanager.shared.data.AccountingFilterDefinition;
import org.gcube.portlets.admin.accountingmanager.shared.data.AccountingFilterSpaces;
import org.gcube.portlets.admin.accountingmanager.shared.data.AccountingFilterTop;
import org.gcube.portlets.admin.accountingmanager.shared.data.ChartType;
import org.gcube.portlets.admin.accountingmanager.shared.data.FilterKey;
import org.gcube.portlets.admin.accountingmanager.shared.data.FilterValue;
import org.gcube.portlets.admin.accountingmanager.shared.data.FilterValuesRequest;
import org.gcube.portlets.admin.accountingmanager.shared.data.FilterValuesResponse;
import org.gcube.portlets.admin.accountingmanager.shared.data.GenresData;
import org.gcube.portlets.admin.accountingmanager.shared.data.Spaces;
import org.gcube.portlets.admin.accountingmanager.shared.exception.SessionExpiredException;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasValue;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.core.client.util.ToggleGroup;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import com.sencha.gxt.dnd.core.client.DND.Feedback;
import com.sencha.gxt.dnd.core.client.DndDragStartEvent;
import com.sencha.gxt.dnd.core.client.DndDragStartEvent.DndDragStartHandler;
import com.sencha.gxt.dnd.core.client.GridDragSource;
import com.sencha.gxt.dnd.core.client.GridDropTarget;
import com.sencha.gxt.widget.core.client.button.ButtonBar;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutData;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutPack;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer.HBoxLayoutAlign;
import com.sencha.gxt.widget.core.client.container.MarginData;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.BeforeStartEditEvent;
import com.sencha.gxt.widget.core.client.event.BeforeStartEditEvent.BeforeStartEditHandler;
import com.sencha.gxt.widget.core.client.event.CancelEditEvent;
import com.sencha.gxt.widget.core.client.event.CancelEditEvent.CancelEditHandler;
import com.sencha.gxt.widget.core.client.event.CompleteEditEvent;
import com.sencha.gxt.widget.core.client.event.CompleteEditEvent.CompleteEditHandler;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.FieldSet;
import com.sencha.gxt.widget.core.client.form.IntegerSpinnerField;
import com.sencha.gxt.widget.core.client.form.Radio;
import com.sencha.gxt.widget.core.client.form.StringComboBox;
import com.sencha.gxt.widget.core.client.grid.CheckBoxSelectionModel;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.Grid.GridCell;
import com.sencha.gxt.widget.core.client.grid.GridSelectionModel;
import com.sencha.gxt.widget.core.client.grid.editing.GridRowEditing;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

/**
 * 
 * @author Giancarlo Panichi
 *
 *
 */
public class ActiveFiltersPanel extends SimpleContainer {

	private static final int TOP_NUMBER_DEFAULT = 5;
	private static final int TOP_NUMBER_MAX = 10;
	private static final int TOP_NUMBER_MIN = 1;
	private EventBus eventBus;
	private Grid<AccountingFilter> gridFilter;
	private ListStore<AccountingFilter> storeFilter;
	private Grid<GenresData> gridGenres;
	private ListStore<GenresData> storeGenres;
	private CheckBoxSelectionModel<GenresData> smGenres;
	private ColumnConfig<GenresData, String> labelGenresCol;
	private boolean addStatus;
	private AccountingClientStateData accountingStateData;
	private ListStore<FilterKey> storeComboFilterKey;
	private ComboBox<FilterKey> comboFilterKey;
	private ListStore<String> storeComboFilterValue;
	private StringComboBox comboFilterValue;
	private GridRowEditing<AccountingFilter> editing;
	private TextButton addButton;
	private int seq;
	private FieldLabel comboChartTypeLabel;
	private ListStore<ChartType> storeComboChartType;
	private ComboBox<ChartType> comboChartType;
	private ListStore<FilterKey> storeComboTopFilterKey;
	private ComboBox<FilterKey> comboTopFilterKey;
	private FieldLabel comboTopFilterKeyLabel;
	private ToolBar toolBar;
	// private CheckBox showOthers;
	private FieldLabel showOthersLabel;
	private FieldLabel noContextLabel;
	private IntegerSpinnerField topNumber;
	private FieldLabel topNumberLabel;

	private FilterKey filterKey;
	private boolean cancelValue;
	private AccountingMonitor accountingMonitor;

	private Radio showOthersYes;
	private Radio showOthersNo;
	private ToggleGroup showOthersToggle;

	private Radio noContextYes;
	private Radio noContextNo;
	private ToggleGroup noContextToggle;

	public interface FilterKeyPropertiesCombo extends PropertyAccess<FilterKey> {

		@Path("id")
		ModelKeyProvider<FilterKey> id();

		LabelProvider<FilterKey> key();

	}

	/*
	 * public interface FilterValuePropertiesCombo extends
	 * PropertyAccess<FilterValue> {
	 * 
	 * @Path("id") ModelKeyProvider<FilterValue> id();
	 * 
	 * LabelProvider<FilterValue> value();
	 * 
	 * }
	 */

	interface FilterKeyTemplates extends XTemplates {
		@XTemplate("<span title=\"{value}\">{value}</span>")
		SafeHtml format(String value);
	}

	interface FilterValueTemplates extends XTemplates {
		@XTemplate("<span title=\"{value}\">{value}</span>")
		SafeHtml format(String value);
	}

	public ActiveFiltersPanel(EventBus eventBus) {
		super();
		Log.debug("ActiveFiltersPanel");
		this.eventBus = eventBus;
		init();
		create();
		bindToEvents();

	}

	private void init() {

	}

	private void create() {
		// Grid
		AccountingFilterProperties props = GWT
				.create(AccountingFilterProperties.class);

		ColumnConfig<AccountingFilter, FilterKey> keyColumn = new ColumnConfig<AccountingFilter, FilterKey>(
				props.filterKey(), 130, "Key");
		keyColumn.setMenuDisabled(true);
		keyColumn.setCell(new AbstractCell<FilterKey>() {

			@Override
			public void render(Context context, FilterKey filterKey,
					SafeHtmlBuilder sb) {
				FilterKeyTemplates filterKeyTemplates = GWT
						.create(FilterKeyTemplates.class);
				sb.append(filterKeyTemplates.format(filterKey.getKey()));
			}
		});

		/*
		 * ColumnConfig<AccountingFilter, FilterValue> valueColumn = new
		 * ColumnConfig<AccountingFilter, FilterValue>( props.filterValue(),
		 * 130, "Value"); valueColumn.setMenuDisabled(true);
		 * valueColumn.setCell(new AbstractCell<FilterValue>() {
		 * 
		 * @Override public void render(Context context, FilterValue
		 * filterValue, SafeHtmlBuilder sb) { FilterValueTemplates
		 * filterValueTemplates = GWT .create(FilterValueTemplates.class);
		 * sb.append(filterValueTemplates.format(filterValue.getValue())); } });
		 */

		ColumnConfig<AccountingFilter, String> valueColumn = new ColumnConfig<AccountingFilter, String>(
				props.filterValue(), 130, "Value");
		valueColumn.setMenuDisabled(true);
		valueColumn.setCell(new AbstractCell<String>() {

			@Override
			public void render(Context context, String filterValue,
					SafeHtmlBuilder sb) {
				FilterValueTemplates filterValueTemplates = GWT
						.create(FilterValueTemplates.class);
				sb.append(filterValueTemplates.format(filterValue));
			}
		});

		ArrayList<ColumnConfig<AccountingFilter, ?>> l = new ArrayList<ColumnConfig<AccountingFilter, ?>>();
		l.add(keyColumn);
		l.add(valueColumn);

		ColumnModel<AccountingFilter> columns = new ColumnModel<AccountingFilter>(
				l);

		storeFilter = new ListStore<AccountingFilter>(props.id());

		final GridSelectionModel<AccountingFilter> sm = new GridSelectionModel<AccountingFilter>();
		sm.setSelectionMode(SelectionMode.SINGLE);

		gridFilter = new Grid<AccountingFilter>(storeFilter, columns);
		gridFilter.setSelectionModel(sm);
		gridFilter.getView().setStripeRows(true);
		gridFilter.getView().setColumnLines(true);
		gridFilter.getView().setAutoFill(true);
		gridFilter.setBorders(true);
		gridFilter.setColumnReordering(false);
		gridFilter.getView().setAutoExpandColumn(valueColumn);
		gridFilter.getView().setSortingEnabled(false);

		GridDragSource<AccountingFilter> ds = new GridDragSource<AccountingFilter>(
				gridFilter);
		ds.addDragStartHandler(new DndDragStartHandler() {

			@Override
			public void onDragStart(DndDragStartEvent event) {
				@SuppressWarnings("unchecked")
				ArrayList<AccountingFilter> draggingSelection = (ArrayList<AccountingFilter>) event
						.getData();
				Log.debug("Start Drag: " + draggingSelection);

			}
		});
		GridDropTarget<AccountingFilter> dt = new GridDropTarget<AccountingFilter>(
				gridFilter);
		dt.setFeedback(Feedback.BOTH);
		dt.setAllowSelfAsSource(true);

		// EDITING //
		// Key
		FilterKeyPropertiesCombo filterKeyPropertiesCombo = GWT
				.create(FilterKeyPropertiesCombo.class);

		storeComboFilterKey = new ListStore<FilterKey>(
				filterKeyPropertiesCombo.id());

		comboFilterKey = new ComboBox<FilterKey>(storeComboFilterKey,
				filterKeyPropertiesCombo.key());
		comboFilterKey.setClearValueOnParseError(false);
		comboFilterKey.setEditable(false);

		comboFilterKey.setTriggerAction(TriggerAction.ALL);
		addHandlersForComboFilterKey(filterKeyPropertiesCombo.key());

		// Value
		/*
		 * FilterValuePropertiesCombo filterValuePropertiesCombo = GWT
		 * .create(FilterValuePropertiesCombo.class);
		 */
		storeComboFilterValue = new ListStore<String>(
				new ModelKeyProvider<String>() {

					@Override
					public String getKey(String item) {
						return item;
					}
				});

		comboFilterValue = new StringComboBox();
		comboFilterValue.setStore(storeComboFilterValue);
		comboFilterValue.setClearValueOnParseError(false);
		comboFilterValue.setEditable(true);
		comboFilterValue.setForceSelection(false);
		// comboFilterValue.setAllowBlank(false);

		// comboFilterValue.setFinishEditOnEnter(false);
		// comboFilterValue.setAutoValidate(true);
		// comboFilterValue.addValidator(new EmptyValidator<String>());
		comboFilterValue.setTriggerAction(TriggerAction.ALL);

		// final TextField valueField = new TextField();
		// valueField.addValidator(new EmptyValidator<String>());

		editing = new GridRowEditing<AccountingFilter>(gridFilter);
		editing.addEditor(keyColumn, comboFilterKey);
		editing.addEditor(valueColumn, comboFilterValue);

		addButton = new TextButton("Add Filter");
		addButton.setIcon(AccountingManagerResources.INSTANCE
				.accountingFilter24());
		addButton.setIconAlign(IconAlign.RIGHT);
		addButton.setToolTip("Add Filter");
		addButton.addSelectHandler(new SelectHandler() {

			@Override
			public void onSelect(SelectEvent event) {
				addNewFilter(event);
			}

		});

		TextButton deleteBtn = new TextButton("Delete");
		deleteBtn.addSelectHandler(new SelectEvent.SelectHandler() {
			public void onSelect(SelectEvent event) {
				GridCell cell = editing.getActiveCell();
				int rowIndex = cell.getRow();

				editing.cancelEditing();

				storeFilter.remove(rowIndex);
				storeFilter.commitChanges();

				editing.getCancelButton().setVisible(true);
				addButton.setEnabled(true);
				if (addStatus) {
					addStatus = false;
				}
			}
		});
		ButtonBar buttonBar = editing.getButtonBar();
		buttonBar.add(deleteBtn);

		editing.addBeforeStartEditHandler(new BeforeStartEditHandler<AccountingFilter>() {

			@Override
			public void onBeforeStartEdit(
					BeforeStartEditEvent<AccountingFilter> event) {
				editingBeforeStart(event);

			}
		});

		editing.addCancelEditHandler(new CancelEditHandler<AccountingFilter>() {

			@Override
			public void onCancelEdit(CancelEditEvent<AccountingFilter> event) {
				storeFilter.rejectChanges();
				addButton.setEnabled(true);

			}

		});

		editing.addCompleteEditHandler(new CompleteEditHandler<AccountingFilter>() {

			@Override
			public void onCompleteEdit(CompleteEditEvent<AccountingFilter> event) {
				try {
					// GridCell cell = event.getEditCell();
					// int rowIndex = cell.getRow();
					// AccountingFilter editingFilter = store.get(rowIndex);

					if (addStatus) {
						addStatus = false;
					}
					storeFilter.commitChanges();

					editing.getCancelButton().setVisible(true);
					addButton.setEnabled(true);

				} catch (Throwable e) {
					Log.error("Error in RuleOnTableNewDefinitionCard: "
							+ e.getLocalizedMessage());
					e.printStackTrace();
				}
			}
		});

		//
		toolBar = new ToolBar();
		toolBar.add(addButton, new BoxLayoutData(new Margins(0)));

		// ChartTypeSelection

		storeComboChartType = new ListStore<ChartType>(
				new ModelKeyProvider<ChartType>() {

					@Override
					public String getKey(ChartType item) {
						return item.name();
					}

				});

		LabelProvider<ChartType> comboChartTypeLabelProvider = new LabelProvider<ChartType>() {

			@Override
			public String getLabel(ChartType item) {

				return item.getLabel();
			}

		};

		// /Grid Context
		GenresDataProperties propsGenresData = GWT
				.create(GenresDataProperties.class);

		IdentityValueProvider<GenresData> identity = new IdentityValueProvider<GenresData>();
		smGenres = new CheckBoxSelectionModel<GenresData>(identity);

		labelGenresCol = new ColumnConfig<GenresData, String>(
				propsGenresData.label(), 356, "Genres");

		labelGenresCol.setCell(new AbstractCell<String>() {

			@Override
			public void render(Context context, String value, SafeHtmlBuilder sb) {
				sb.appendHtmlConstant("<span style='white-space: normal;' title='"
						+ SafeHtmlUtils.htmlEscape(value)
						+ "'>"
						+ SafeHtmlUtils.htmlEscape(value) + "</span>");

			}

		});

		List<ColumnConfig<GenresData, ?>> lGenres = new ArrayList<ColumnConfig<GenresData, ?>>();
		lGenres.add(smGenres.getColumn());
		lGenres.add(labelGenresCol);
		ColumnModel<GenresData> cmGenresData = new ColumnModel<GenresData>(
				lGenres);

		storeGenres = new ListStore<GenresData>(propsGenresData.id());

		gridGenres = new Grid<GenresData>(storeGenres, cmGenresData);

		sm.setSelectionMode(SelectionMode.MULTI);
		gridGenres.setHeight("130px");
		gridGenres.setSelectionModel(smGenres);
		gridGenres.getView().setStripeRows(true);
		gridGenres.getView().setColumnLines(true);
		gridGenres.getView().setAutoFill(true);
		gridGenres.setBorders(true);
		gridGenres.setColumnReordering(false);
		gridGenres.getView().setAutoExpandColumn(labelGenresCol);
		gridGenres.getView().setSortingEnabled(true);

		// /

		comboChartType = new ComboBox<>(storeComboChartType,
				comboChartTypeLabelProvider);
		comboChartType.setClearValueOnParseError(false);
		comboChartType.setEditable(false);
		comboChartType.setForceSelection(true);
		// comboFilterValue.setAllowBlank(false);
		comboChartType.setValue(ChartType.Basic);
		// comboFilterValue.setFinishEditOnEnter(false);
		// comboFilterValue.setAutoValidate(true);
		// comboFilterValue.addValidator(new EmptyValidator<String>());
		comboChartType.setTriggerAction(TriggerAction.ALL);
		addHandlersForComboChartType(comboChartTypeLabelProvider);

		comboChartTypeLabel = new FieldLabel(comboChartType, "Chart");

		// FileterKeyCombo for Top Chart
		storeComboTopFilterKey = new ListStore<FilterKey>(
				filterKeyPropertiesCombo.id());

		comboTopFilterKey = new ComboBox<FilterKey>(storeComboTopFilterKey,
				filterKeyPropertiesCombo.key());
		comboTopFilterKey.setClearValueOnParseError(false);
		comboTopFilterKey.setEditable(false);

		comboTopFilterKey.setTriggerAction(TriggerAction.ALL);
		addHandlersForComboTopFilterKey(filterKeyPropertiesCombo.key());

		comboTopFilterKeyLabel = new FieldLabel(comboTopFilterKey, "Key");

		// ShowOthers
		showOthersYes = new Radio();
		showOthersYes.setBoxLabel("Yes");

		showOthersNo = new Radio();
		showOthersNo.setBoxLabel("No");
		showOthersNo.setValue(true);

		showOthersToggle = new ToggleGroup();
		showOthersToggle.add(showOthersYes);
		showOthersToggle.add(showOthersNo);

		showOthersToggle
				.addValueChangeHandler(new ValueChangeHandler<HasValue<Boolean>>() {

					@Override
					public void onValueChange(
							ValueChangeEvent<HasValue<Boolean>> event) {
						ToggleGroup group = (ToggleGroup) event.getSource();
						Radio radio = (Radio) group.getValue();
						Log.debug("ShowOthers selected: " + radio.getBoxLabel());
					}
				});

		HBoxLayoutContainer hp = new HBoxLayoutContainer();
		hp.setHBoxLayoutAlign(HBoxLayoutAlign.MIDDLE);
		hp.setPack(BoxLayoutPack.START);
		hp.add(showOthersYes, new BoxLayoutData(new Margins(0)));
		hp.add(showOthersNo, new BoxLayoutData(new Margins(0)));

		showOthersLabel = new FieldLabel(hp, "Show Others");

		// TODO NoContext Flag
		noContextYes = new Radio();
		noContextYes.setBoxLabel("Yes");

		noContextNo = new Radio();
		noContextNo.setBoxLabel("No");
		noContextNo.setValue(true);

		noContextToggle = new ToggleGroup();
		noContextToggle.add(noContextYes);
		noContextToggle.add(noContextNo);

		noContextToggle
				.addValueChangeHandler(new ValueChangeHandler<HasValue<Boolean>>() {

					@Override
					public void onValueChange(
							ValueChangeEvent<HasValue<Boolean>> event) {
						ToggleGroup group = (ToggleGroup) event.getSource();
						Radio radio = (Radio) group.getValue();
						Log.debug("No Context selected: " + radio.getBoxLabel());
					}
				});

		HBoxLayoutContainer hpNoContext = new HBoxLayoutContainer();
		hpNoContext.setHBoxLayoutAlign(HBoxLayoutAlign.MIDDLE);
		hpNoContext.setPack(BoxLayoutPack.START);
		hpNoContext.add(noContextYes, new BoxLayoutData(new Margins(0)));
		hpNoContext.add(noContextNo, new BoxLayoutData(new Margins(0)));

		noContextLabel = new FieldLabel(hpNoContext, "No Context");

		//
		topNumber = new IntegerSpinnerField();
		topNumber.setMaxValue(TOP_NUMBER_MAX);
		topNumber.setMinValue(TOP_NUMBER_MIN);
		topNumber.setAllowNegative(false);
		topNumber.setAllowBlank(false);
		topNumber.setValue(TOP_NUMBER_DEFAULT);
		topNumber.setEditable(false);

		topNumberLabel = new FieldLabel(topNumber, "Number");

		// //
		VerticalLayoutContainer vlc = new VerticalLayoutContainer();

		vlc.add(comboChartTypeLabel, new VerticalLayoutData(1, -1, new Margins(
				0)));
		vlc.add(gridGenres, new VerticalLayoutData(1, -1, new Margins(0)));
		vlc.add(noContextLabel, new VerticalLayoutData(1, -1, new Margins(0)));
		vlc.add(comboTopFilterKeyLabel, new VerticalLayoutData(1, -1,
				new Margins(0)));
		vlc.add(showOthersLabel, new VerticalLayoutData(1, -1, new Margins(0)));
		vlc.add(topNumberLabel, new VerticalLayoutData(1, -1, new Margins(0)));
		vlc.add(toolBar, new VerticalLayoutData(1, -1, new Margins(0)));
		vlc.add(gridFilter, new VerticalLayoutData(1, 1, new Margins(0)));

		FieldSet fieldSet = new FieldSet();
		fieldSet.setHeight("360px");
		fieldSet.setHeadingHtml("<b>Active Filters</b>");
		fieldSet.setCollapsible(false);
		fieldSet.add(vlc);

		add(fieldSet, new MarginData(0));
		comboChartTypeLabel.setVisible(false);
		gridGenres.setVisible(false);
		noContextLabel.setVisible(false);
		comboTopFilterKeyLabel.setVisible(false);
		showOthersLabel.setVisible(false);
		topNumberLabel.setVisible(false);
	}

	// Bind to Events
	private void bindToEvents() {
		eventBus.addHandler(StateChangeEvent.TYPE,
				new StateChangeEvent.StateChangeEventHandler() {

					@Override
					public void onStateChange(StateChangeEvent event) {
						Log.debug("Catch State Change Event");
						doStateChangeCommand(event);

					}
				});

		eventBus.addHandler(AccountingPeriodEvent.TYPE,
				new AccountingPeriodEvent.AccountingPeriodEventHandler() {

					@Override
					public void onPeriod(AccountingPeriodEvent event) {
						Log.debug("Catch Accounting Period Event");
						manageAccountingPeriodEvent(event);

					}
				});
	}

	private void doStateChangeCommand(StateChangeEvent event) {
		if (event.getStateChangeType() == null) {
			return;
		}
		switch (event.getStateChangeType()) {
		case Restore:
			onRestoreStateChange(event);
			break;
		case Update:
			break;
		default:
			break;

		}

	}

	private void onRestoreStateChange(StateChangeEvent event) {
		Log.debug("onRestoreStateChange: " + event);
		if (event.getAccountingStateData() != null) {

			accountingStateData = event.getAccountingStateData();
			if (accountingStateData.getAccountingType() != null) {
				switch (accountingStateData.getAccountingType()) {
				case JOB:
				case PORTLET:
				case SERVICE:
				case STORAGE:
				case TASK:
					List<ChartType> chartsTypeForDefault = new ArrayList<>(ChartType.asList());
					Log.debug("ChartsType: " + chartsTypeForDefault);
					chartsTypeForDefault.remove(ChartType.Spaces);
					Log.debug("ChartsType after remove: " + chartsTypeForDefault);
					storeComboChartType.clear();
					storeComboChartType.addAll(chartsTypeForDefault);
					storeComboChartType.commitChanges();
					comboChartTypeLabel.setVisible(true);
					break;
				case SPACE:
					List<ChartType> chartsTypeForSpace = new ArrayList<>();
					chartsTypeForSpace.add(ChartType.Spaces);
					Log.debug("ChartsType: " + chartsTypeForSpace);
					storeComboChartType.clear();
					storeComboChartType.addAll(chartsTypeForSpace);
					storeComboChartType.commitChanges();
					comboChartTypeLabel.setVisible(false);
					break;
				default:
					storeComboChartType.clear();
					storeComboChartType.commitChanges();
					comboChartTypeLabel.setVisible(false);
					break;

				}
			} else {
				storeComboChartType.clear();
				storeComboChartType.commitChanges();
				comboChartTypeLabel.setVisible(false);
				
			}
			changeActiveFilters();

		} else {
			accountingStateData = null;
			seq = 0;
		}
		Log.debug("Set seq to: " + seq);
		forceLayout();
	}

	private void changeActiveFilters() {
		storeComboFilterKey.clear();
		storeComboFilterKey
				.addAll(accountingStateData.getAvailableFilterKeys());
		storeComboFilterKey.commitChanges();
		storeComboTopFilterKey.clear();
		storeComboTopFilterKey.addAll(accountingStateData
				.getAvailableFilterKeys());
		storeComboTopFilterKey.commitChanges();

		ChartType chartType = accountingStateData.getSeriesRequest()
				.getAccountingFilterDefinition().getChartType();

		switch (chartType) {
		case Basic:
			changeActiveFiltersForBasic();
			break;
		case Top:
			changeActiveFiltersForTop();
			break;
		case Context:
			changeActiveFiltersForContext();
			break;
		case Spaces:
			changeActiveFiltersForSpaces();
			break;

		default:
			changeActiveFiltersForBasic();
			break;

		}
	}

	private void changeActiveFiltersForBasic() {
		if (accountingStateData.getSeriesRequest() != null
				&& accountingStateData.getSeriesRequest()
						.getAccountingFilterDefinition() != null
				&& accountingStateData.getSeriesRequest()
						.getAccountingFilterDefinition() instanceof AccountingFilterBasic) {
			AccountingFilterBasic accountingFilterBasic = (AccountingFilterBasic) accountingStateData
					.getSeriesRequest().getAccountingFilterDefinition();

			if (accountingFilterBasic.getFilters() != null) {
				List<AccountingFilter> filters = accountingFilterBasic
						.getFilters();
				storeFilter.clear();
				storeFilter.addAll(filters);
				seq = 0;
				for (AccountingFilter filter : filters) {
					if (filter.getId() > seq) {
						seq = filter.getId();
					}
				}
				storeFilter.commitChanges();
			} else {
				storeFilter.clear();
				storeFilter.commitChanges();
				seq = 0;
			}

			if (accountingStateData.getRootScope()) {
				noContextToggle.reset();
				if (accountingFilterBasic.isNoContext()) {
					noContextYes.setValue(true);
				} else {
					noContextNo.setValue(true);
				}
				noContextLabel.setVisible(true);
			} else {
				noContextToggle.reset();
				noContextNo.setValue(true);
				noContextLabel.setVisible(false);
			}

		} else {
			storeFilter.clear();
			storeFilter.commitChanges();
			seq = 0;

			if (accountingStateData.getRootScope()) {
				noContextToggle.reset();
				noContextNo.setValue(true);
				noContextLabel.setVisible(true);
			} else {
				noContextToggle.reset();
				noContextNo.setValue(true);
				noContextLabel.setVisible(false);
			}

		}
		comboChartType.setValue(ChartType.Basic);
		gridGenres.setVisible(false);
		comboTopFilterKey.reset();
		comboTopFilterKeyLabel.setVisible(false);
		showOthersToggle.reset();
		showOthersNo.setValue(true);
		showOthersLabel.setVisible(false);

		topNumber.reset();
		topNumberLabel.setVisible(false);
		forceLayout();

	}

	private void changeActiveFiltersForTop() {
		if (accountingStateData.getSeriesRequest() != null
				&& accountingStateData.getSeriesRequest()
						.getAccountingFilterDefinition() != null
				&& accountingStateData.getSeriesRequest()
						.getAccountingFilterDefinition() instanceof AccountingFilterTop) {
			AccountingFilterTop accountingFilterTop = (AccountingFilterTop) accountingStateData
					.getSeriesRequest().getAccountingFilterDefinition();
			if (accountingFilterTop.getFilterKey() != null) {
				comboTopFilterKey.setValue(accountingFilterTop.getFilterKey());
				topNumber.setValue(accountingFilterTop.getTopNumber());

				// /////////
				if (accountingFilterTop.getFilters() != null) {
					List<AccountingFilter> filters = accountingFilterTop
							.getFilters();
					storeFilter.clear();
					storeFilter.addAll(filters);
					seq = 0;
					for (AccountingFilter filter : filters) {
						if (filter.getId() > seq) {
							seq = filter.getId();
						}
					}
					storeFilter.commitChanges();
				} else {
					storeFilter.clear();
					storeFilter.commitChanges();
					seq = 0;
				}

			} else {
				comboTopFilterKey.reset();
				showOthersToggle.reset();
				showOthersNo.setValue(true);
				topNumber.reset();
				topNumber.setValue(TOP_NUMBER_DEFAULT);
				storeFilter.clear();
				storeFilter.commitChanges();
				seq = 0;
			}
		} else {
			comboTopFilterKey.reset();
			showOthersToggle.reset();
			showOthersNo.setValue(true);
			topNumber.reset();
			topNumber.setValue(TOP_NUMBER_DEFAULT);
			storeFilter.clear();
			storeFilter.commitChanges();
			seq = 0;

		}

		comboChartType.setValue(ChartType.Top);
		gridGenres.setVisible(false);
		comboTopFilterKeyLabel.setVisible(true);
		showOthersLabel.setVisible(true);
		noContextToggle.reset();
		noContextNo.setValue(true);
		noContextLabel.setVisible(false);
		topNumberLabel.setVisible(true);
		forceLayout();
	}

	private void changeActiveFiltersForContext() {
		if (accountingStateData.getSeriesRequest() != null
				&& accountingStateData.getSeriesRequest()
						.getAccountingFilterDefinition() != null
				&& accountingStateData.getSeriesRequest()
						.getAccountingFilterDefinition() instanceof AccountingFilterContext) {
			AccountingFilterContext accountingFilterContext = (AccountingFilterContext) accountingStateData
					.getSeriesRequest().getAccountingFilterDefinition();
			if (accountingStateData.getAvailableContext() != null
					&& accountingStateData.getAvailableContext().getContexts() != null
					&& !accountingStateData.getAvailableContext().getContexts()
							.isEmpty()) {

				if (accountingFilterContext.getContext() != null
						&& accountingFilterContext.getContext().getContexts() != null
						&& !accountingFilterContext.getContext().getContexts()
								.isEmpty()) {
					ArrayList<String> contexts = accountingStateData
							.getAvailableContext().getContexts();
					ArrayList<GenresData> genres = new ArrayList<GenresData>();
					for (String context : contexts) {
						GenresData genresData = new GenresData(context);
						genres.add(genresData);
					}
					storeGenres.clear();
					storeGenres.addAll(genres);
					storeGenres.commitChanges();

					smGenres.deselectAll();

					if (accountingFilterContext.getContext().getContexts() == null
							|| accountingFilterContext.getContext()
									.getContexts().isEmpty()) {
					} else {
						ArrayList<GenresData> selected = new ArrayList<>();
						for (String c : accountingFilterContext.getContext()
								.getContexts()) {
							for (GenresData genresData : storeGenres.getAll()) {
								if (c.compareTo(genresData.getGenre()) == 0) {
									selected.add(genresData);
									break;
								}
							}
						}
						if (!selected.isEmpty()) {
							if (selected.size() == storeGenres.getAll().size()) {
								smGenres.selectAll();
							} else {
								smGenres.select(selected, false);
							}
						}
					}

					smGenres.refresh();

				} else {
					repopulatesContexts();
				}

			} else {
				resetContexts();
			}

			if (accountingFilterContext.getFilters() != null) {
				List<AccountingFilter> filters = accountingFilterContext
						.getFilters();
				storeFilter.clear();
				storeFilter.addAll(filters);
				seq = 0;
				for (AccountingFilter filter : filters) {
					if (filter.getId() > seq) {
						seq = filter.getId();
					}
				}
				storeFilter.commitChanges();
			} else {
				storeFilter.clear();
				storeFilter.commitChanges();
				seq = 0;
			}

		} else {
			reconfigureContext();
			storeFilter.clear();
			storeFilter.commitChanges();
			seq = 0;

		}
		comboChartType.setValue(ChartType.Context);
		gridGenres.setVisible(true);
		labelGenresCol.setHeader("Scope");
		gridGenres.getView().refresh(true);
		comboTopFilterKey.reset();
		comboTopFilterKeyLabel.setVisible(false);
		showOthersToggle.reset();
		showOthersNo.setValue(true);
		showOthersLabel.setVisible(false);
		noContextToggle.reset();
		noContextNo.setValue(true);
		noContextLabel.setVisible(false);
		topNumber.reset();
		topNumberLabel.setVisible(false);
		forceLayout();

	}

	private void reconfigureContext() {
		if (accountingStateData.getAvailableContext() != null
				&& accountingStateData.getAvailableContext().getContexts() != null
				&& !accountingStateData.getAvailableContext().getContexts()
						.isEmpty()) {
			repopulatesContexts();
		} else {
			resetContexts();
		}
	}

	private void resetContexts() {
		storeGenres.clear();
		storeGenres.commitChanges();
		smGenres.deselectAll();
		smGenres.refresh();
	}

	private void repopulatesContexts() {
		ArrayList<String> contexts = accountingStateData.getAvailableContext()
				.getContexts();
		ArrayList<GenresData> genres = new ArrayList<GenresData>();
		for (String context : contexts) {
			GenresData genresData = new GenresData(context);
			genres.add(genresData);
		}
		storeGenres.clear();
		storeGenres.addAll(genres);
		storeGenres.commitChanges();
		smGenres.selectAll();
		smGenres.refresh();
	}

	private void changeActiveFiltersForSpaces() {
		if (accountingStateData.getSeriesRequest() != null
				&& accountingStateData.getSeriesRequest()
						.getAccountingFilterDefinition() != null
				&& accountingStateData.getSeriesRequest()
						.getAccountingFilterDefinition() instanceof AccountingFilterSpaces) {
			AccountingFilterSpaces accountingFilterSapces = (AccountingFilterSpaces) accountingStateData
					.getSeriesRequest().getAccountingFilterDefinition();
			if (accountingStateData.getAvailableSpaces() != null
					&& accountingStateData.getAvailableSpaces().getSpacesList() != null
					&& !accountingStateData.getAvailableSpaces().getSpacesList()
							.isEmpty()) {

				if (accountingFilterSapces.getSpaces() != null
						&& accountingFilterSapces.getSpaces().getSpacesList() != null
						&& !accountingFilterSapces.getSpaces().getSpacesList()
								.isEmpty()) {
					ArrayList<String> spacesList = accountingStateData
							.getAvailableSpaces().getSpacesList();
					ArrayList<GenresData> genres = new ArrayList<GenresData>();
					for (String space : spacesList) {
						GenresData genresData = new GenresData(space);
						genres.add(genresData);
					}
					storeGenres.clear();
					storeGenres.addAll(genres);
					storeGenres.commitChanges();

					smGenres.deselectAll();

					if (accountingFilterSapces.getSpaces().getSpacesList() == null
							|| accountingFilterSapces.getSpaces()
									.getSpacesList().isEmpty()) {
					} else {
						ArrayList<GenresData> selected = new ArrayList<>();
						for (String s : accountingFilterSapces.getSpaces()
								.getSpacesList()) {
							for (GenresData genresData : storeGenres.getAll()) {
								if (s.compareTo(genresData.getGenre()) == 0) {
									selected.add(genresData);
									break;
								}
							}
						}
						if (!selected.isEmpty()) {
							if (selected.size() == storeGenres.getAll().size()) {
								smGenres.selectAll();
							} else {
								smGenres.select(selected, false);
							}
						}
					}

					smGenres.refresh();

				} else {
					repopulatesSpaces();
				}

			} else {
				resetSpaces();
			}

			if (accountingFilterSapces.getFilters() != null) {
				List<AccountingFilter> filters = accountingFilterSapces
						.getFilters();
				storeFilter.clear();
				storeFilter.addAll(filters);
				seq = 0;
				for (AccountingFilter filter : filters) {
					if (filter.getId() > seq) {
						seq = filter.getId();
					}
				}
				storeFilter.commitChanges();
			} else {
				storeFilter.clear();
				storeFilter.commitChanges();
				seq = 0;
			}

		} else {
			reconfigureSpaces();
			storeFilter.clear();
			storeFilter.commitChanges();
			seq = 0;

		}
		comboChartType.setValue(ChartType.Spaces);
		gridGenres.setVisible(true);
		labelGenresCol.setHeader("Space");
		gridGenres.getView().refresh(true);
		comboTopFilterKey.reset();
		comboTopFilterKeyLabel.setVisible(false);
		showOthersToggle.reset();
		showOthersNo.setValue(true);
		showOthersLabel.setVisible(false);
		noContextToggle.reset();
		noContextNo.setValue(true);
		noContextLabel.setVisible(false);
		topNumber.reset();
		topNumberLabel.setVisible(false);
		forceLayout();

	}

	private void reconfigureSpaces() {
		if (accountingStateData.getAvailableSpaces() != null
				&& accountingStateData.getAvailableSpaces().getSpacesList() != null
				&& !accountingStateData.getAvailableSpaces().getSpacesList()
						.isEmpty()) {
			repopulatesSpaces();
		} else {
			resetSpaces();
		}
	}

	private void resetSpaces() {
		storeGenres.clear();
		storeGenres.commitChanges();
		smGenres.deselectAll();
		smGenres.refresh();
	}

	private void repopulatesSpaces() {
		ArrayList<String> spaces = accountingStateData.getAvailableSpaces()
				.getSpacesList();
		ArrayList<GenresData> genres = new ArrayList<GenresData>();
		for (String space : spaces) {
			GenresData genresData = new GenresData(space);
			genres.add(genresData);
		}
		storeGenres.clear();
		storeGenres.addAll(genres);
		storeGenres.commitChanges();
		smGenres.selectAll();
		smGenres.refresh();
	}

	public AccountingFilterDefinition getActiveFilters() {
		try {
			ChartType chartType = comboChartType.getCurrentValue();

			switch (chartType) {
			case Basic:
				return getActiveFiltersForBasic();
			case Top:
				return getActiveFiltersForTop();
			case Context:
				return getActiveFiltersForContext();
			case Spaces:
				return getActiveFiltersForSpaces();
			default:
				return null;

			}

		} catch (Throwable e) {
			Log.error(e.getLocalizedMessage());
			e.printStackTrace();
			UtilsGXT3.alert("Attention", e.getLocalizedMessage());
			return null;
		}
	}

	// TODO
	private AccountingFilterDefinition getActiveFiltersForBasic() {
		Boolean noContextValue = false;
		if (noContextLabel != null && noContextLabel.isVisible()) {
			noContextValue = noContextYes.getValue();
		} else {
			noContextValue = false;
		}
		Log.debug("noContextValue: " + noContextValue);

		if (storeFilter == null || storeFilter.size() <= 0) {
			return new AccountingFilterBasic(noContextValue);
		} else {
			List<AccountingFilter> filtersActives = storeFilter.getAll();
			ArrayList<AccountingFilter> filtersReady = new ArrayList<AccountingFilter>();
			for (AccountingFilter filter : filtersActives) {
				if (filter.getFilterValue() != null
						&& !filter.getFilterValue().isEmpty()) {
					filtersReady.add(filter);
				}
			}

			if (filtersReady.size() > 0) {
				return new AccountingFilterBasic(filtersReady, noContextValue);
			} else {
				return new AccountingFilterBasic(noContextValue);
			}

		}
	}

	private AccountingFilterDefinition getActiveFiltersForTop() {
		Boolean showOthersValue = showOthersYes.getValue();
		Integer topN = topNumber.getCurrentValue();
		FilterKey filterKey = comboTopFilterKey.getCurrentValue();
		if (filterKey == null) {
			return new AccountingFilterTop(showOthersValue, topN);
		} else {
			if (storeFilter == null || storeFilter.size() <= 0) {
				return new AccountingFilterTop(filterKey, null,
						showOthersValue, topN);
			} else {
				List<AccountingFilter> filtersActives = storeFilter.getAll();
				ArrayList<AccountingFilter> filtersReady = new ArrayList<AccountingFilter>();
				for (AccountingFilter filter : filtersActives) {
					if (filter.getFilterValue() != null
							&& !filter.getFilterValue().isEmpty()) {
						filtersReady.add(filter);
					}
				}
				if (filtersReady.size() > 0) {
					return new AccountingFilterTop(filterKey, filtersReady,
							showOthersValue, topN);
				} else {
					return new AccountingFilterTop(filterKey, null,
							showOthersValue, topN);
				}

			}
		}

	}

	private AccountingFilterDefinition getActiveFiltersForContext() {
		ArrayList<String> contextsSelected = new ArrayList<String>();
		if (storeGenres != null && storeGenres.size() > 0 && smGenres != null) {
			List<GenresData> selected = smGenres.getSelectedItems();
			for (GenresData cd : selected) {
				contextsSelected.add(cd.getGenre());
			}
		}
		org.gcube.portlets.admin.accountingmanager.shared.data.Context context = new org.gcube.portlets.admin.accountingmanager.shared.data.Context(
				contextsSelected);

		List<AccountingFilter> filtersActives = storeFilter.getAll();
		ArrayList<AccountingFilter> filtersReady = new ArrayList<AccountingFilter>();
		for (AccountingFilter filter : filtersActives) {
			if (filter.getFilterValue() != null
					&& !filter.getFilterValue().isEmpty()) {
				filtersReady.add(filter);
			}
		}
		if (filtersReady.size() > 0) {
			return new AccountingFilterContext(context, filtersReady);
		} else {
			return new AccountingFilterContext(context, null);
		}

	}

	private AccountingFilterDefinition getActiveFiltersForSpaces() {
		ArrayList<String> spacesSelected = new ArrayList<String>();
		if (storeGenres != null && storeGenres.size() > 0 && smGenres != null) {
			List<GenresData> selected = smGenres.getSelectedItems();
			for (GenresData cd : selected) {
				spacesSelected.add(cd.getGenre());
			}
		}
		Spaces spaces = new Spaces(spacesSelected);

		List<AccountingFilter> filtersActives = storeFilter.getAll();
		ArrayList<AccountingFilter> filtersReady = new ArrayList<AccountingFilter>();
		for (AccountingFilter filter : filtersActives) {
			if (filter.getFilterValue() != null
					&& !filter.getFilterValue().isEmpty()) {
				filtersReady.add(filter);
			}
		}
		if (filtersReady.size() > 0) {
			return new AccountingFilterSpaces(spaces, filtersReady);
		} else {
			return new AccountingFilterSpaces(spaces, null);
		}

	}

	private void addNewFilter(SelectEvent event) {
		List<AccountingFilter> filtersSet = storeFilter.getAll();
		FilterKey fk = null;

		if (accountingStateData == null
				|| accountingStateData.getAvailableFilterKeys() == null
				|| accountingStateData.getAvailableFilterKeys().size() < 0) {
			UtilsGXT3.info("No key available", "No key available");

		} else {
			List<FilterKey> remainingFilterKeys = new ArrayList<FilterKey>(
					accountingStateData.getAvailableFilterKeys());
			List<FilterKey> removableFilterKeys = new ArrayList<FilterKey>();

			for (AccountingFilter filterSet : filtersSet) {
				removableFilterKeys.add(filterSet.getFilterKey());
			}

			if (comboChartType.getCurrentValue() != null
					&& comboChartType.getCurrentValue()
							.compareTo(ChartType.Top) == 0) {
				if (comboTopFilterKey.getCurrentValue() != null) {
					removableFilterKeys
							.add(comboTopFilterKey.getCurrentValue());
				}
			}

			remainingFilterKeys.removeAll(removableFilterKeys);

			if (remainingFilterKeys.size() > 0) {
				/*
				 * if (comboChartType.getCurrentValue() != null &&
				 * comboChartType.getCurrentValue().compareTo( ChartType.Top) ==
				 * 0) { if(!(store.getAll().size()<2)){
				 * UtilsGXT3.info("Attention",
				 * "You can add at most 2 filters for top chart!"); return; } }
				 */
				fk = remainingFilterKeys.get(0);
				seq++;
				AccountingFilter newAccountingFilter = new AccountingFilter(
						seq, fk, "");
				Log.debug("newAccountingFilter: " + newAccountingFilter);
				editing.cancelEditing();
				addStatus = true;
				editing.getCancelButton().setVisible(false);
				storeFilter.add(newAccountingFilter);
				int row = storeFilter.indexOf(newAccountingFilter);

				storeComboFilterKey.clear();
				storeComboFilterKey.addAll(remainingFilterKeys);
				storeComboFilterKey.commitChanges();

				editing.startEditing(new GridCell(row, 0));
			} else {
				UtilsGXT3.info("No key available", "No key available");

			}
		}
	}

	private void retrieveFilterValuesByKey(FilterKey filterKey,
			final boolean cancelValue) {
		this.filterKey = filterKey;
		this.cancelValue = cancelValue;
		AccountingPeriodRequestEvent event = new AccountingPeriodRequestEvent();
		eventBus.fireEvent(event);

	}

	private void manageAccountingPeriodEvent(AccountingPeriodEvent event) {
		if (event == null || event.getAccountingPeriod() == null) {
			Log.debug("AccountingPeriod not valid");
			return;
		}
		accountingMonitor = new AccountingMonitor();
		FilterValuesRequest requestFilterValue = new FilterValuesRequest(
				filterKey, accountingStateData.getAccountingType(),
				event.getAccountingPeriod());

		AccountingManagerServiceAsync.INSTANCE.getFilterValues(
				requestFilterValue, new AsyncCallback<FilterValuesResponse>() {

					@Override
					public void onFailure(Throwable caught) {
						accountingMonitor.hide();
						if (caught instanceof SessionExpiredException) {
							eventBus.fireEvent(new SessionExpiredEvent(
									SessionExpiredType.EXPIREDONSERVER));
						} else {
							Log.error("Error retrieving filter values:"
									+ caught.getLocalizedMessage());
							UtilsGXT3.alert("Error retrieving filter values",
									caught.getLocalizedMessage());
						}

					}

					@Override
					public void onSuccess(FilterValuesResponse result) {
						Log.debug("FilterValues: " + result);
						accountingMonitor.hide();
						ArrayList<String> values = new ArrayList<String>();
						for (FilterValue fv : result.getFilterValues()) {
							values.add(fv.getValue());
						}
						if (cancelValue) {
							comboFilterValue.clear();
						}
						storeComboFilterValue.clear();
						storeComboFilterValue.addAll(values);
						storeComboFilterValue.commitChanges();
					}
				});
	}

	private void editingBeforeStart(BeforeStartEditEvent<AccountingFilter> event) {
		GridCell cell = event.getEditCell();
		int rowIndex = cell.getRow();
		AccountingFilter editingFilter = storeFilter.get(rowIndex);

		List<AccountingFilter> filtersSet = storeFilter.getAll();

		List<FilterKey> remainingFilterKeys = null;
		if (accountingStateData == null
				|| accountingStateData.getAvailableFilterKeys() == null) {
			remainingFilterKeys = new ArrayList<FilterKey>();
		} else {
			remainingFilterKeys = new ArrayList<FilterKey>(
					accountingStateData.getAvailableFilterKeys());

		}

		List<FilterKey> setFilterKeys = new ArrayList<FilterKey>();

		if (comboChartType.getCurrentValue() != null
				&& comboChartType.getCurrentValue().compareTo(ChartType.Top) == 0) {
			if (comboTopFilterKey.getCurrentValue() != null) {
				setFilterKeys.add(comboTopFilterKey.getCurrentValue());
			}
		}

		for (AccountingFilter filterSet : filtersSet) {
			if (filterSet.getFilterKey().getKey()
					.compareTo(editingFilter.getFilterKey().getKey()) != 0) {
				setFilterKeys.add(filterSet.getFilterKey());
			}
		}

		remainingFilterKeys.removeAll(setFilterKeys);

		storeComboFilterKey.clear();
		storeComboFilterKey.addAll(remainingFilterKeys);
		storeComboFilterKey.commitChanges();

		if (editingFilter.getFilterKey() != null) {
			retrieveFilterValuesByKey(editingFilter.getFilterKey(), false);
		}

		addButton.setEnabled(false);

	}

	private void addHandlersForComboFilterKey(
			final LabelProvider<FilterKey> labelProvider) {
		comboFilterKey.addSelectionHandler(new SelectionHandler<FilterKey>() {
			public void onSelection(SelectionEvent<FilterKey> event) {
				Log.debug("FilterKey selected: " + event.getSelectedItem());
				updateFilterKey(event.getSelectedItem());
			}

		});
	}

	private void updateFilterKey(FilterKey selectedFilterKey) {
		retrieveFilterValuesByKey(selectedFilterKey, true);
	}

	private void addHandlersForComboChartType(
			final LabelProvider<ChartType> labelProvider) {
		comboChartType.addSelectionHandler(new SelectionHandler<ChartType>() {
			public void onSelection(SelectionEvent<ChartType> event) {
				Log.debug("FilterKey selected: " + event.getSelectedItem());
				updateComboChartType(event.getSelectedItem());
			}

		});
	}

	// TODO
	private void updateComboChartType(ChartType chartType) {
		if (chartType == null) {
			return;
		}
		switch (chartType) {
		case Basic:
			gridGenres.setVisible(false);
			comboTopFilterKey.reset();
			comboTopFilterKeyLabel.setVisible(false);
			showOthersToggle.reset();
			showOthersNo.setValue(true);
			showOthersLabel.setVisible(false);

			if (accountingStateData.getRootScope()) {
				noContextToggle.reset();
				noContextNo.setValue(true);
				noContextLabel.setVisible(true);
			} else {
				noContextToggle.reset();
				noContextNo.setValue(true);
				noContextLabel.setVisible(false);
			}

			topNumber.reset();
			topNumberLabel.setVisible(false);
			storeFilter.clear();
			storeFilter.commitChanges();
			seq = 0;
			forceLayout();
			break;
		case Top:
			gridGenres.setVisible(false);
			comboTopFilterKey.reset();
			if (accountingStateData != null
					&& accountingStateData.getAvailableFilterKeys() != null
					&& accountingStateData.getAvailableFilterKeys().size() > 0) {
				comboTopFilterKey.setValue(accountingStateData
						.getAvailableFilterKeys().get(0));
			}
			comboTopFilterKeyLabel.setVisible(true);
			showOthersToggle.reset();
			showOthersNo.setValue(true);
			showOthersLabel.setVisible(true);
			noContextToggle.reset();
			noContextYes.setValue(false);
			noContextNo.setValue(true);
			noContextLabel.setVisible(false);
			topNumber.reset();
			topNumber.setValue(TOP_NUMBER_DEFAULT);
			topNumberLabel.setVisible(true);
			storeFilter.clear();
			storeFilter.commitChanges();
			seq = 0;
			forceLayout();
			break;
		case Context:
			reconfigureContext();
			gridGenres.setVisible(true);
			labelGenresCol.setHeader("Scope");
			gridGenres.getView().refresh(true);
			comboTopFilterKey.reset();
			comboTopFilterKeyLabel.setVisible(false);
			showOthersToggle.reset();
			showOthersNo.setValue(true);
			showOthersLabel.setVisible(false);
			noContextToggle.reset();
			noContextYes.setValue(false);
			noContextNo.setValue(true);
			noContextLabel.setVisible(false);
			topNumber.reset();
			topNumberLabel.setVisible(false);
			storeFilter.clear();
			storeFilter.commitChanges();
			seq = 0;
			forceLayout();
			break;
		case Spaces:
			reconfigureSpaces();
			gridGenres.setVisible(true);
			labelGenresCol.setHeader("Space");
			gridGenres.getView().refresh(true);
			comboTopFilterKey.reset();
			comboTopFilterKeyLabel.setVisible(false);
			showOthersToggle.reset();
			showOthersNo.setValue(true);
			showOthersLabel.setVisible(false);
			noContextToggle.reset();
			noContextYes.setValue(false);
			noContextNo.setValue(true);
			noContextLabel.setVisible(false);
			topNumber.reset();
			topNumberLabel.setVisible(false);
			storeFilter.clear();
			storeFilter.commitChanges();
			seq = 0;
			forceLayout();
			break;
		default:
			break;

		}

	}

	private void addHandlersForComboTopFilterKey(
			final LabelProvider<FilterKey> labelProvider) {
		comboTopFilterKey
				.addSelectionHandler(new SelectionHandler<FilterKey>() {
					public void onSelection(SelectionEvent<FilterKey> event) {
						Log.debug("FilterKey selected: "
								+ event.getSelectedItem());
						updateTopFilterKey(event.getSelectedItem());
					}

				});
	}

	private void updateTopFilterKey(FilterKey selectedFilterKey) {
		storeFilter.clear();
		storeFilter.commitChanges();
		seq = 0;
	}

}
