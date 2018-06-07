package org.gcube.portlets.admin.accountingmanager.client.maindata.charts.space;

import java.util.ArrayList;
import java.util.Date;

import org.gcube.portlets.admin.accountingmanager.client.event.ExportRequestEvent;
import org.gcube.portlets.admin.accountingmanager.client.maindata.charts.utils.ByteUnitMeasure;
import org.gcube.portlets.admin.accountingmanager.client.maindata.charts.utils.ChartDateTimeData;
import org.gcube.portlets.admin.accountingmanager.client.maindata.charts.utils.ChartTimeMeasure;
import org.gcube.portlets.admin.accountingmanager.client.maindata.charts.utils.DownloadConstants;
import org.gcube.portlets.admin.accountingmanager.client.maindata.charts.utils.SpaceChartMeasure;
import org.gcube.portlets.admin.accountingmanager.client.resource.AccountingManagerResources;
import org.gcube.portlets.admin.accountingmanager.client.state.AccountingClientStateData;
import org.gcube.portlets.admin.accountingmanager.shared.data.AccountingPeriodMode;
import org.gcube.portlets.admin.accountingmanager.shared.data.response.SeriesSpace;
import org.gcube.portlets.admin.accountingmanager.shared.data.response.space.SeriesSpaceSpaces;
import org.gcube.portlets.admin.accountingmanager.shared.data.response.space.SeriesSpaceData;
import org.gcube.portlets.admin.accountingmanager.shared.export.ExportType;

import com.allen_sauer.gwt.log.client.Log;
import com.github.highcharts4gwt.client.view.widget.HighchartsLayoutPanel;
import com.github.highcharts4gwt.model.array.api.Array;
import com.github.highcharts4gwt.model.array.api.ArrayNumber;
import com.github.highcharts4gwt.model.array.api.ArrayString;
import com.github.highcharts4gwt.model.factory.api.HighchartsOptionFactory;
import com.github.highcharts4gwt.model.factory.jso.JsoHighchartsOptionFactory;
import com.github.highcharts4gwt.model.highcharts.option.api.ChartOptions;
import com.github.highcharts4gwt.model.highcharts.option.api.SeriesColumn;
import com.github.highcharts4gwt.model.highcharts.option.api.seriescolumn.Data;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.sencha.gxt.cell.core.client.ButtonCell.ButtonArrowAlign;
import com.sencha.gxt.cell.core.client.ButtonCell.IconAlign;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutData;
import com.sencha.gxt.widget.core.client.container.MarginData;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

/**
 * 
 * @author Giancarlo Panichi
 *
 *
 */
public class SpaceChartSpacesPanel extends SimpleContainer {

	private static final String DATA_VOLUME_UNIT = "Data Volume Unit";

	private DateTimeFormat dtf = DateTimeFormat
			.getFormat(PredefinedFormat.YEAR_MONTH_DAY);

	private EventBus eventBus;
	private AccountingClientStateData accountingStateData;
	private HighchartsLayoutPanel highchartsLayoutPanel;

	// Download Menu
	private MenuItem downloadCSVItem;
	private MenuItem downloadXMLItem;
	private MenuItem downloadJSONItem;
	private MenuItem downloadPNGItem;
	private MenuItem downloadJPGItem;
	private MenuItem downloadPDFItem;
	private MenuItem downloadSVGItem;

	// Unit Menu
	private MenuItem kBItem;
	private MenuItem MBItem;
	private MenuItem GBItem;
	private MenuItem TBItem;

	private long unitMeasure = ByteUnitMeasure.getMegaByteDimForStorage();
	private String unitMeasureLabel = ByteUnitMeasure.MB;
	private TextButton unitButton;

	private ChartOptions options;

	private SpaceChartMeasure measure = SpaceChartMeasure.DataVolume;
	private TextButton measureButton;

	private VerticalLayoutContainer vert;

	public SpaceChartSpacesPanel(EventBus eventBus,
			AccountingClientStateData accountingStateData) {
		this.eventBus = eventBus;
		this.accountingStateData = accountingStateData;
		forceLayoutOnResize = true;
		create();

	}

	private void create() {
		ToolBar toolBar = new ToolBar();
		toolBar.setSpacing(2);
		// Download
		final TextButton downloadButton = new TextButton(
				DownloadConstants.DOWNLOAD,
				AccountingManagerResources.INSTANCE.accountingDownload24());
		// downloadButton.setScale(ButtonScale.MEDIUM);
		downloadButton.setIconAlign(IconAlign.RIGHT);
		downloadButton.setArrowAlign(ButtonArrowAlign.RIGHT);
		downloadButton.setMenu(createDownloadMenu());

		// Unit
		unitButton = new TextButton(DATA_VOLUME_UNIT,
				AccountingManagerResources.INSTANCE.accountingUnitMB24());
		unitButton.setIconAlign(IconAlign.RIGHT);
		unitButton.setArrowAlign(ButtonArrowAlign.RIGHT);
		unitButton.setMenu(createUnitMenu());

		toolBar.add(downloadButton, new BoxLayoutData(new Margins(0)));
		toolBar.add(unitButton, new BoxLayoutData(new Margins(0)));

		//
		createChart();

		highchartsLayoutPanel = new HighchartsLayoutPanel();
		highchartsLayoutPanel.renderChart(options);

		//
		vert = new VerticalLayoutContainer();
		vert.add(toolBar, new VerticalLayoutData(1, -1, new Margins(0)));
		vert.add(highchartsLayoutPanel, new VerticalLayoutData(1, 1,
				new Margins(0)));

		add(vert, new MarginData(0));

	}

	private void updateChart() {
		createChart();
		highchartsLayoutPanel.renderChart(options);
		forceLayout();
	}

	private Menu createDownloadMenu() {
		Menu menuDownload = new Menu();
		downloadCSVItem = new MenuItem(DownloadConstants.DOWNLOAD_CSV,
				AccountingManagerResources.INSTANCE.accountingFileCSV24());
		downloadCSVItem.setHeight(30);
		downloadXMLItem = new MenuItem(DownloadConstants.DOWNLOAD_XML,
				AccountingManagerResources.INSTANCE.accountingFileXML24());
		downloadXMLItem.setHeight(30);
		downloadJSONItem = new MenuItem(DownloadConstants.DOWNLOAD_JSON,
				AccountingManagerResources.INSTANCE.accountingFileJSON24());
		downloadJSONItem.setHeight(30);
		downloadPNGItem = new MenuItem(DownloadConstants.DOWNLOAD_PNG,
				AccountingManagerResources.INSTANCE.accountingFilePNG24());
		downloadPNGItem.setHeight(30);
		downloadJPGItem = new MenuItem(DownloadConstants.DOWNLOAD_JPG,
				AccountingManagerResources.INSTANCE.accountingFileJPG24());
		downloadJPGItem.setHeight(30);
		downloadPDFItem = new MenuItem(DownloadConstants.DOWNLOAD_PDF,
				AccountingManagerResources.INSTANCE.accountingFilePDF24());
		downloadPDFItem.setHeight(30);
		downloadSVGItem = new MenuItem(DownloadConstants.DOWNLOAD_SVG,
				AccountingManagerResources.INSTANCE.accountingFileSVG24());
		downloadSVGItem.setHeight(30);

		downloadCSVItem.addSelectionHandler(new SelectionHandler<Item>() {

			@Override
			public void onSelection(SelectionEvent<Item> event) {
				onDownloadCSV();

			}

		});

		downloadXMLItem.addSelectionHandler(new SelectionHandler<Item>() {

			@Override
			public void onSelection(SelectionEvent<Item> event) {
				onDownloadXML();

			}

		});

		downloadJSONItem.addSelectionHandler(new SelectionHandler<Item>() {

			@Override
			public void onSelection(SelectionEvent<Item> event) {
				onDownloadJSON();

			}

		});

		downloadPNGItem.addSelectionHandler(new SelectionHandler<Item>() {

			@Override
			public void onSelection(SelectionEvent<Item> event) {
				String id = highchartsLayoutPanel.getElement().getId();
				onDownloadPNG(id);

			}
		});

		downloadJPGItem.addSelectionHandler(new SelectionHandler<Item>() {

			@Override
			public void onSelection(SelectionEvent<Item> event) {
				String id = highchartsLayoutPanel.getElement().getId();
				onDownloadJPG(id);
			}
		});

		downloadPDFItem.addSelectionHandler(new SelectionHandler<Item>() {

			@Override
			public void onSelection(SelectionEvent<Item> event) {
				String id = highchartsLayoutPanel.getElement().getId();
				onDownloadPDF(id);
			}
		});

		downloadSVGItem.addSelectionHandler(new SelectionHandler<Item>() {

			@Override
			public void onSelection(SelectionEvent<Item> event) {
				String id = highchartsLayoutPanel.getElement().getId();
				onDownloadSVG(id);
			}
		});

		menuDownload.add(downloadCSVItem);
		menuDownload.add(downloadXMLItem);
		menuDownload.add(downloadJSONItem);
		menuDownload.add(downloadPNGItem);
		menuDownload.add(downloadJPGItem);
		menuDownload.add(downloadPDFItem);
		menuDownload.add(downloadSVGItem);
		return menuDownload;

	}

	private Menu createUnitMenu() {
		Menu menuUnit = new Menu();
		kBItem = new MenuItem(ByteUnitMeasure.KILOBYTE,
				AccountingManagerResources.INSTANCE.accountingUnitkB24());
		kBItem.setHeight(30);
		MBItem = new MenuItem(ByteUnitMeasure.MEGABYTE,
				AccountingManagerResources.INSTANCE.accountingUnitMB24());
		MBItem.setHeight(30);
		GBItem = new MenuItem(ByteUnitMeasure.GIGABYTE,
				AccountingManagerResources.INSTANCE.accountingUnitGB24());
		GBItem.setHeight(30);
		TBItem = new MenuItem(ByteUnitMeasure.TERABYTE,
				AccountingManagerResources.INSTANCE.accountingUnitTB24());
		TBItem.setHeight(30);

		kBItem.addSelectionHandler(new SelectionHandler<Item>() {

			@Override
			public void onSelection(SelectionEvent<Item> event) {
				unitMeasure = ByteUnitMeasure.getKiloByteDimForStorage();
				unitMeasureLabel = ByteUnitMeasure.kB;
				unitButton.setIcon(AccountingManagerResources.INSTANCE
						.accountingUnitkB24());
				updateChart();
			}
		});

		MBItem.addSelectionHandler(new SelectionHandler<Item>() {

			@Override
			public void onSelection(SelectionEvent<Item> event) {
				unitMeasure = ByteUnitMeasure.getMegaByteDimForStorage();
				unitMeasureLabel = ByteUnitMeasure.MB;
				unitButton.setIcon(AccountingManagerResources.INSTANCE
						.accountingUnitMB24());
				updateChart();
			}
		});

		GBItem.addSelectionHandler(new SelectionHandler<Item>() {

			@Override
			public void onSelection(SelectionEvent<Item> event) {
				unitMeasure = ByteUnitMeasure.getGigaByteDimForStorage();
				unitMeasureLabel = ByteUnitMeasure.GB;
				unitButton.setIcon(AccountingManagerResources.INSTANCE
						.accountingUnitGB24());
				updateChart();
			}
		});

		TBItem.addSelectionHandler(new SelectionHandler<Item>() {

			@Override
			public void onSelection(SelectionEvent<Item> event) {
				unitMeasure = ByteUnitMeasure.getTeraByteDimForStorage();
				unitMeasureLabel = ByteUnitMeasure.TB;
				unitButton.setIcon(AccountingManagerResources.INSTANCE
						.accountingUnitTB24());
				updateChart();
			}
		});

		menuUnit.add(kBItem);
		menuUnit.add(MBItem);
		menuUnit.add(GBItem);
		menuUnit.add(TBItem);
		return menuUnit;

	}

	private void onDownloadCSV() {
		ExportRequestEvent event = new ExportRequestEvent(ExportType.CSV,
				accountingStateData.getAccountingType());
		eventBus.fireEvent(event);
	}

	private void onDownloadXML() {
		ExportRequestEvent event = new ExportRequestEvent(ExportType.XML,
				accountingStateData.getAccountingType());
		eventBus.fireEvent(event);
	}

	private void onDownloadJSON() {
		ExportRequestEvent event = new ExportRequestEvent(ExportType.JSON,
				accountingStateData.getAccountingType());
		eventBus.fireEvent(event);
	}

	// chart.options.exporting.buttons.contextButton.menuItems[0].onclick();

	public static native void onDownloadPNG(String id) /*-{
		console.log(id);
		var chart = $wnd
				.jQuery('#' + id)
				.highcharts(
						this.@org.gcube.portlets.admin.accountingmanager.client.maindata.charts.space.SpaceChartSpacesPanel::options);
		console.log(chart);
		chart.exportChart();

	}-*/;

	public static native void onDownloadJPG(String id) /*-{
		console.log(id);
		var chart = $wnd
				.jQuery('#' + id)
				.highcharts(
						this.@org.gcube.portlets.admin.accountingmanager.client.maindata.charts.space.SpaceChartSpacesPanel::options);
		console.log(chart);
		chart.exportChart({
			type : 'image/jpeg'
		});

	}-*/;

	public static native void onDownloadPDF(String id) /*-{
		console.log(id);
		var chart = $wnd
				.jQuery('#' + id)
				.highcharts(
						this.@org.gcube.portlets.admin.accountingmanager.client.maindata.charts.space.SpaceChartSpacesPanel::options);
		console.log(chart);
		chart.exportChart({
			type : 'application/pdf'
		});

	}-*/;

	public static native void onDownloadSVG(String id) /*-{
		console.log(id);
		var chart = $wnd
				.jQuery('#' + id)
				.highcharts(
						this.@org.gcube.portlets.admin.accountingmanager.client.maindata.charts.space.SpaceChartSpacesPanel::options);
		console.log(chart);
		chart.exportChart({
			type : 'image/svg+xml'
		});

	}-*/;

	private void createChart() {
		SeriesSpace seriesSpace = (SeriesSpace) accountingStateData
				.getSeriesResponse();

		if (!(seriesSpace.getSerieSpaceDefinition() instanceof SeriesSpaceSpaces)) {
			Log.debug("Invalid SeriesSpaceCategories!");
			return;
		}
		SeriesSpaceSpaces seriesSpaceCategories = (SeriesSpaceSpaces) seriesSpace
				.getSerieSpaceDefinition();

		HighchartsOptionFactory highchartsFactory = new JsoHighchartsOptionFactory();

		options = highchartsFactory.createChartOptions();
		options.chart().zoomType("xy");
		options.exporting().buttons().contextButton().enabled(false);
		options.exporting().filename("AccountingSpaceCategories");
		options.title().text("Accounting Space");

		/*
		 * options.subtitle().text("Click and drag in the plot area to zoom in");
		 */

		ArrayString colors = options.colors();
		// colors.setValue(0, "#cc0038");
		// colors.setValue(1, "#32cd32");

		// yAxis
		options.setFieldAsJsonObject("yAxis", retrieveYAxis(colors));

		options.plotOptions().setFieldAsJsonObject(
				"column",
				"{ " + "\"stacking\": \"normal\"," + "\"dataLabels\": { "
						+ "    \"enabled\": \"true\","
						+ "    \"color\": \"white\", " + "    \"style\": {"
						+ "        \"textShadow\": \"0 0 3px black\"" + "    }"
						+ " }" + " }");

		ArrayList<SeriesColumn> seriesColumn = new ArrayList<>();
		/*
		 * if (seriesServiceContext.getShowOthers()) {
		 * createSeriesColumnShowOthers(seriesServiceContext, highchartsFactory,
		 * colors, seriesColumn); } else {
		 */
		createSeriesColumnSimple(seriesSpaceCategories, highchartsFactory,
				colors, seriesColumn);
		/* } */
		for (SeriesColumn serie : seriesColumn) {
			options.series().addToEnd(serie);
		}

		options.chart().showAxes(true);

		options.legend().enabled(true);
		return;
	}

	private void createSeriesColumnShowOthers(
			SeriesSpaceSpaces seriesSpaceCategories,
			HighchartsOptionFactory highchartsFactory, ArrayString colors,
			ArrayList<SeriesColumn> seriesColumn) {
		Log.debug("Series ShowOthers");
		if (accountingStateData.getSeriesRequest().getAccountingPeriod()
				.getPeriod().compareTo(AccountingPeriodMode.DAILY) == 0
				|| accountingStateData.getSeriesRequest().getAccountingPeriod()
						.getPeriod().compareTo(AccountingPeriodMode.HOURLY) == 0
				|| accountingStateData.getSeriesRequest().getAccountingPeriod()
						.getPeriod().compareTo(AccountingPeriodMode.MINUTELY) == 0) {

			double minRange = ChartTimeMeasure
					.calculateMinRange(accountingStateData.getSeriesRequest()
							.getAccountingPeriod());

			double interval = ChartTimeMeasure
					.calculateInterval(accountingStateData.getSeriesRequest()
							.getAccountingPeriod());

			Date dateStart = dtf.parse(accountingStateData.getSeriesRequest()
					.getAccountingPeriod().getStartDate());

			dateStart.setTime(dateStart.getTime()
					+ ChartTimeMeasure.timeZoneOffset()
					* ChartTimeMeasure.MINUTE);

			Log.debug("BuildChart DateStart: "
					+ DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL)
							.format(dateStart));

			// xAxis
			options.xAxis().type("datetime");
			options.xAxis().minRange(minRange);
			options.tooltip().xDateFormat("Selected Data");

			int i = 0;
			for (; i < seriesSpaceCategories.getSeriesSpaceDataCategoriesList()
					.size(); i++) {
				SeriesColumn seriesColumnData = highchartsFactory
						.createSeriesColumn();

				seriesColumnData.name(seriesSpaceCategories
						.getSeriesSpaceDataCategoriesList().get(i).getSpace());
				seriesColumnData.color(colors.get(i));
				seriesColumnData.type("column");

				ArrayNumber data = seriesColumnData.dataAsArrayNumber();

				for (SeriesSpaceData seriesSpaceData : seriesSpaceCategories
						.getSeriesSpaceDataCategoriesList().get(i).getSeries()) {
					switch (measure) {
					case DataVolume:
						data.push(seriesSpaceData.getDataVolume() / unitMeasure);
						break;
					default:
						data.push(seriesSpaceData.getDataVolume());
						break;

					}

				}

				seriesColumnData.pointInterval(interval).pointStart(
						dateStart.getTime());
				seriesColumn.add(seriesColumnData);

			}

			if (i < seriesSpaceCategories.getSeriesSpaceDataCategoriesList()
					.size()) {
				SeriesColumn seriesColumnData = highchartsFactory
						.createSeriesColumn();

				seriesColumnData.name("Others");
				seriesColumnData.color(colors.get(i));
				seriesColumnData.type("column");

				ArrayList<Long> othersData = new ArrayList<>();
				for (int j = i; j < seriesSpaceCategories
						.getSeriesSpaceDataCategoriesList().size(); j++) {
					Long value;
					for (int k = 0; k < seriesSpaceCategories
							.getSeriesSpaceDataCategoriesList().get(j)
							.getSeries().size(); k++) {
						SeriesSpaceData seriesSpaceData = seriesSpaceCategories
								.getSeriesSpaceDataCategoriesList().get(j)
								.getSeries().get(k);
						if (j == i) {
							switch (measure) {
							case DataVolume:
								othersData.add(seriesSpaceData.getDataVolume()
										/ unitMeasure);
								break;
							default:
								othersData.add(seriesSpaceData.getDataVolume()
										/ unitMeasure);
								break;

							}
						} else {
							switch (measure) {
							case DataVolume:

								value = seriesSpaceData.getDataVolume()
										/ unitMeasure + othersData.get(k);
								othersData.set(k, value);
								break;
							default:
								value = seriesSpaceData.getDataVolume()
										/ unitMeasure + othersData.get(k);
								othersData.set(k, value);
								break;

							}
						}

					}
				}

				ArrayNumber data = seriesColumnData.dataAsArrayNumber();

				for (Long value : othersData) {
					data.push(value);
				}

				seriesColumnData.pointInterval(interval).pointStart(
						dateStart.getTime());
				seriesColumn.add(seriesColumnData);
			}

		} else {
			// xAxis
			options.xAxis().type("datetime");

			if (accountingStateData.getSeriesRequest().getAccountingPeriod()
					.getPeriod().compareTo(AccountingPeriodMode.MONTHLY) == 0) {
				// options.tooltip().xDateFormat("%b, %Y");
				options.tooltip().xDateFormat("Selected Data");

			} else {
				if (accountingStateData.getSeriesRequest()
						.getAccountingPeriod().getPeriod()
						.compareTo(AccountingPeriodMode.YEARLY) == 0) {
					// options.tooltip().xDateFormat("%Y");
					options.tooltip().xDateFormat("Selected Data");

				} else {
					options.tooltip().xDateFormat("Selected Data");
				}

			}

			int i = 0;
			for (; i < seriesSpaceCategories.getSeriesSpaceDataCategoriesList()
					.size(); i++) {
				SeriesColumn seriesColumnData = highchartsFactory
						.createSeriesColumn();

				seriesColumnData.name(seriesSpaceCategories
						.getSeriesSpaceDataCategoriesList().get(i).getSpace());
				seriesColumnData.color(colors.get(i));
				seriesColumnData.type("column");

				Array<Data> arrayData = seriesColumnData.dataAsArrayObject();

				for (SeriesSpaceData seriesSpaceData : seriesSpaceCategories
						.getSeriesSpaceDataCategoriesList().get(i).getSeries()) {
					long dateFrom1970 = seriesSpaceData.getDate().getTime();

					Log.debug("SeriersSpaceData: " + seriesSpaceData.getDate());
					Log.debug("SeriersSpaceData: " + dateFrom1970);

					Data data = highchartsFactory.createSeriesColumnData();

					switch (measure) {
					case DataVolume:
						data.x(dateFrom1970);
						data.y(seriesSpaceData.getDataVolume() / unitMeasure);
						arrayData.addToEnd(data);
						break;
					default:
						data.x(dateFrom1970);
						data.y(seriesSpaceData.getDataVolume() / unitMeasure);
						arrayData.addToEnd(data);
						break;

					}

				}

				seriesColumn.add(seriesColumnData);

			}

			if (i < seriesSpaceCategories.getSeriesSpaceDataCategoriesList()
					.size()) {
				SeriesColumn seriesColumnData = highchartsFactory
						.createSeriesColumn();

				seriesColumnData.name("Others");
				seriesColumnData.color(colors.get(i));
				seriesColumnData.type("column");

				ArrayList<ChartDateTimeData> othersData = new ArrayList<>();
				for (int j = i; j < seriesSpaceCategories
						.getSeriesSpaceDataCategoriesList().size(); j++) {
					ChartDateTimeData chartDateTimeData;
					for (int k = 0; k < seriesSpaceCategories
							.getSeriesSpaceDataCategoriesList().get(j)
							.getSeries().size(); k++) {
						SeriesSpaceData seriesSpaceData = seriesSpaceCategories
								.getSeriesSpaceDataCategoriesList().get(j)
								.getSeries().get(k);
						if (j == i) {
							switch (measure) {
							case DataVolume:
								chartDateTimeData = new ChartDateTimeData(
										seriesSpaceData.getDate(),
										seriesSpaceData.getDataVolume()
												/ unitMeasure);
								othersData.add(chartDateTimeData);
								break;

							default:
								chartDateTimeData = new ChartDateTimeData(
										seriesSpaceData.getDate(),
										seriesSpaceData.getDataVolume()
												/ unitMeasure);
								othersData.add(chartDateTimeData);
								break;
							}
						} else {
							switch (measure) {
							case DataVolume:
								chartDateTimeData = new ChartDateTimeData(
										seriesSpaceData.getDate(),
										(seriesSpaceData.getDataVolume() / unitMeasure)
												+ othersData.get(k).getValue());
								othersData.set(k, chartDateTimeData);
								break;
							default:
								chartDateTimeData = new ChartDateTimeData(
										seriesSpaceData.getDate(),
										(seriesSpaceData.getDataVolume() / unitMeasure)
												+ othersData.get(k).getValue());
								othersData.set(k, chartDateTimeData);
								break;

							}
						}

					}
				}

				Array<Data> arrayData = seriesColumnData.dataAsArrayObject();

				for (ChartDateTimeData chartDateTimeData : othersData) {
					long dateFrom1970 = chartDateTimeData.getDate().getTime();

					Log.debug("SeriersOthersData: "
							+ chartDateTimeData.getDate());
					Log.debug("SeriersOthersData: " + dateFrom1970);

					Data data = highchartsFactory.createSeriesColumnData();

					data.x(dateFrom1970);
					data.y(chartDateTimeData.getValue());
					arrayData.addToEnd(data);

				}

				seriesColumn.add(seriesColumnData);
			}

		}

	}

	private void createSeriesColumnSimple(
			SeriesSpaceSpaces seriesSpaceCategories,
			HighchartsOptionFactory highchartsFactory, ArrayString colors,
			ArrayList<SeriesColumn> seriesColumn) {

		Log.debug("Series Simple");
		if (accountingStateData.getSeriesRequest().getAccountingPeriod()
				.getPeriod().compareTo(AccountingPeriodMode.DAILY) == 0
				|| accountingStateData.getSeriesRequest().getAccountingPeriod()
						.getPeriod().compareTo(AccountingPeriodMode.HOURLY) == 0
				|| accountingStateData.getSeriesRequest().getAccountingPeriod()
						.getPeriod().compareTo(AccountingPeriodMode.MINUTELY) == 0) {

			double minRange = ChartTimeMeasure
					.calculateMinRange(accountingStateData.getSeriesRequest()
							.getAccountingPeriod());

			double interval = ChartTimeMeasure
					.calculateInterval(accountingStateData.getSeriesRequest()
							.getAccountingPeriod());

			Date dateStart = dtf.parse(accountingStateData.getSeriesRequest()
					.getAccountingPeriod().getStartDate());

			dateStart.setTime(dateStart.getTime()
					+ ChartTimeMeasure.timeZoneOffset()
					* ChartTimeMeasure.MINUTE);

			Log.debug("BuildChart DateStart: "
					+ DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL)
							.format(dateStart));

			// xAxis
			options.xAxis().type("datetime");
			options.xAxis().minRange(minRange);
			options.tooltip().xDateFormat("Selected Data");

			for (int i = 0; i < seriesSpaceCategories
					.getSeriesSpaceDataCategoriesList().size(); i++) {
				SeriesColumn seriesColumnData = highchartsFactory
						.createSeriesColumn();

				seriesColumnData.name(seriesSpaceCategories
						.getSeriesSpaceDataCategoriesList().get(i).getSpace());
				seriesColumnData.color(colors.get(i));
				seriesColumnData.type("column");

				ArrayNumber data = seriesColumnData.dataAsArrayNumber();

				for (SeriesSpaceData seriesSpaceData : seriesSpaceCategories
						.getSeriesSpaceDataCategoriesList().get(i).getSeries()) {
					switch (measure) {
					case DataVolume:
						data.push(seriesSpaceData.getDataVolume() / unitMeasure);
						break;
					default:
						data.push(seriesSpaceData.getDataVolume());
						break;

					}

				}

				seriesColumnData.pointInterval(interval).pointStart(
						dateStart.getTime());
				seriesColumn.add(seriesColumnData);

			}

		} else {
			// xAxis
			options.xAxis().type("datetime");

			if (accountingStateData.getSeriesRequest().getAccountingPeriod()
					.getPeriod().compareTo(AccountingPeriodMode.MONTHLY) == 0) {
				// options.tooltip().xDateFormat("%b, %Y");
				options.tooltip().xDateFormat("Selected Data");

			} else {
				if (accountingStateData.getSeriesRequest()
						.getAccountingPeriod().getPeriod()
						.compareTo(AccountingPeriodMode.YEARLY) == 0) {
					// options.tooltip().xDateFormat("%Y");
					options.tooltip().xDateFormat("Selected Data");

				} else {
					options.tooltip().xDateFormat("Selected Data");
				}

			}

			for (int i = 0; i < seriesSpaceCategories
					.getSeriesSpaceDataCategoriesList().size(); i++) {
				SeriesColumn seriesColumnData = highchartsFactory
						.createSeriesColumn();

				seriesColumnData.name(seriesSpaceCategories
						.getSeriesSpaceDataCategoriesList().get(i).getSpace());
				seriesColumnData.color(colors.get(i));
				seriesColumnData.type("column");

				Array<Data> arrayData = seriesColumnData.dataAsArrayObject();

				for (SeriesSpaceData seriesSpaceData : seriesSpaceCategories
						.getSeriesSpaceDataCategoriesList().get(i).getSeries()) {
					long dateFrom1970 = seriesSpaceData.getDate().getTime();

					Log.debug("SeriersSpaceData: " + seriesSpaceData.getDate());
					Log.debug("SeriersStorageData: " + dateFrom1970);

					Data data = highchartsFactory.createSeriesColumnData();

					switch (measure) {
					case DataVolume:
						data.x(dateFrom1970);
						data.y(seriesSpaceData.getDataVolume() / unitMeasure);
						arrayData.addToEnd(data);
						break;
					default:
						data.x(dateFrom1970);
						data.y(seriesSpaceData.getDataVolume() / unitMeasure);
						arrayData.addToEnd(data);
						break;

					}

				}

				seriesColumn.add(seriesColumnData);

			}

		}
	}

	private String retrieveYAxis(ArrayString colors) {
		switch (measure) {
		case DataVolume:
			return "[{" + " \"id\": \"" + SpaceChartMeasure.DataVolume.name()
					+ "\"," + " \"labels\": {" + "    \"format\": \"{value} "
					+ unitMeasureLabel + "\"," + "    \"style\": {"
					+ "       \"color\": \"" + colors.get(1) + "\"" + "     }"
					+ " }," + " \"stackLabels\": {" + " \"enabled\": \"true\","
					+ " \"style\": {" + "     \"fontWeight\": \"bold\","
					+ "    \"color\": \"gray\"" + "   } " + "   },"
					+ " \"title\": { " + "    \"text\": \""
					+ SpaceChartMeasure.DataVolume.getLabel() + "\","
					+ "    \"style\": {" + "       \"color\": \""
					+ colors.get(1) + "\"" + "    }" + " }" + "}]";

		default:
			return "[{" + " \"id\": \"" + SpaceChartMeasure.DataVolume.name()
					+ "\"," + " \"labels\": {" + "    \"format\": \"{value} "
					+ unitMeasureLabel + "\"," + "    \"style\": {"
					+ "       \"color\": \"" + colors.get(1) + "\"" + "     }"
					+ " }," + " \"stackLabels\": {" + " \"enabled\": \"true\","
					+ " \"style\": {" + "     \"fontWeight\": \"bold\","
					+ "    \"color\": \"gray\"" + "   } " + "   },"
					+ " \"title\": { " + "    \"text\": \""
					+ SpaceChartMeasure.DataVolume.getLabel() + "\","
					+ "    \"style\": {" + "       \"color\": \""
					+ colors.get(1) + "\"" + "    }" + " }" + "}]";

		}

	}

}
