package org.gcube.portlets.admin.accountingmanager.client.maindata.charts.service;

import java.util.Date;

import org.gcube.portlets.admin.accountingmanager.client.event.ExportRequestEvent;
import org.gcube.portlets.admin.accountingmanager.client.maindata.charts.utils.ChartTimeMeasure;
import org.gcube.portlets.admin.accountingmanager.client.maindata.charts.utils.DownloadConstants;
import org.gcube.portlets.admin.accountingmanager.client.maindata.charts.utils.ServiceChartMeasure;
import org.gcube.portlets.admin.accountingmanager.client.maindata.charts.utils.TimeUnitMeasure;
import org.gcube.portlets.admin.accountingmanager.client.resource.AccountingManagerResources;
import org.gcube.portlets.admin.accountingmanager.client.state.AccountingClientStateData;
import org.gcube.portlets.admin.accountingmanager.shared.data.AccountingPeriodMode;
import org.gcube.portlets.admin.accountingmanager.shared.data.response.SeriesService;
import org.gcube.portlets.admin.accountingmanager.shared.data.response.service.SeriesServiceBasic;
import org.gcube.portlets.admin.accountingmanager.shared.data.response.service.SeriesServiceData;
import org.gcube.portlets.admin.accountingmanager.shared.export.ExportType;

import com.allen_sauer.gwt.log.client.Log;
import com.github.highcharts4gwt.client.view.widget.HighchartsLayoutPanel;
import com.github.highcharts4gwt.model.array.api.Array;
import com.github.highcharts4gwt.model.array.api.ArrayNumber;
import com.github.highcharts4gwt.model.array.api.ArrayString;
import com.github.highcharts4gwt.model.factory.api.HighchartsOptionFactory;
import com.github.highcharts4gwt.model.factory.jso.JsoHighchartsOptionFactory;
import com.github.highcharts4gwt.model.highcharts.option.api.ChartOptions;
import com.github.highcharts4gwt.model.highcharts.option.api.SeriesArea;
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
import com.sencha.gxt.widget.core.client.button.ToggleButton;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutData;
import com.sencha.gxt.widget.core.client.container.MarginData;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.Menu;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

/**
 * 
 * @author giancarlo email: <a
 *         href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a>
 *
 */
public class ServiceChartBasicPanel extends SimpleContainer {
	private DateTimeFormat dtf = DateTimeFormat
			.getFormat(PredefinedFormat.YEAR_MONTH_DAY);
	private static final String TIME_UNIT = "Time Unit";
	private static final String SINGLE_AXIS = "Single Axis";

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

	// Time Unit Menu
	private MenuItem msItem;
	private MenuItem sItem;
	private MenuItem mItem;
	private MenuItem hItem;

	private ChartOptions options;
	private VerticalLayoutContainer vert;

	private long unitMeasure = TimeUnitMeasure.getMilliseconds();
	private String unitMeasureLabel = TimeUnitMeasure.MS;
	private TextButton unitButton;
	private ToggleButton toggleButton;

	public ServiceChartBasicPanel(EventBus eventBus,
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

		unitButton = new TextButton(TIME_UNIT,
				AccountingManagerResources.INSTANCE.accountingUnitms24());
		unitButton.setIconAlign(IconAlign.RIGHT);
		unitButton.setArrowAlign(ButtonArrowAlign.RIGHT);
		unitButton.setMenu(createUnitMenu());

		// Single Axis
		toggleButton = new ToggleButton(SINGLE_AXIS);
		toggleButton.setIcon(AccountingManagerResources.INSTANCE
				.accountingChartVariableAxis24());
		toggleButton.setIconAlign(IconAlign.RIGHT);
		toggleButton.setValue(false);

		toggleButton.addSelectHandler(new SelectHandler() {

			@Override
			public void onSelect(SelectEvent event) {
				updateChart();
			}
		});

		toolBar.add(downloadButton, new BoxLayoutData(new Margins(0)));
		toolBar.add(unitButton, new BoxLayoutData(new Margins(0)));
		toolBar.add(toggleButton, new BoxLayoutData(new Margins(0)));

		//
		createMultiAxisChart();

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
		if (toggleButton.getValue()) {
			createSingleAxisChart();
			highchartsLayoutPanel.renderChart(options);
		} else {
			createMultiAxisChart();
			highchartsLayoutPanel.renderChart(options);
		}
		forceLayout();
	}

	private Menu createUnitMenu() {
		Menu menuUnit = new Menu();
		msItem = new MenuItem(TimeUnitMeasure.MILLISECONDS,
				AccountingManagerResources.INSTANCE.accountingUnitms24());
		msItem.setHeight(30);
		sItem = new MenuItem(TimeUnitMeasure.SECONDS,
				AccountingManagerResources.INSTANCE.accountingUnits24());
		sItem.setHeight(30);
		mItem = new MenuItem(TimeUnitMeasure.MINUTES,
				AccountingManagerResources.INSTANCE.accountingUnitm24());
		mItem.setHeight(30);
		hItem = new MenuItem(TimeUnitMeasure.HOURS,
				AccountingManagerResources.INSTANCE.accountingUnith24());
		hItem.setHeight(30);

		msItem.addSelectionHandler(new SelectionHandler<Item>() {

			@Override
			public void onSelection(SelectionEvent<Item> event) {
				unitMeasure = TimeUnitMeasure.getMilliseconds();
				unitMeasureLabel = TimeUnitMeasure.MS;
				unitButton.setIcon(AccountingManagerResources.INSTANCE
						.accountingUnitms24());
				updateChart();
			}
		});

		sItem.addSelectionHandler(new SelectionHandler<Item>() {

			@Override
			public void onSelection(SelectionEvent<Item> event) {
				unitMeasure = TimeUnitMeasure.getSeconds();
				unitMeasureLabel = TimeUnitMeasure.S;
				unitButton.setIcon(AccountingManagerResources.INSTANCE
						.accountingUnits24());
				updateChart();
			}
		});

		mItem.addSelectionHandler(new SelectionHandler<Item>() {

			@Override
			public void onSelection(SelectionEvent<Item> event) {
				unitMeasure = TimeUnitMeasure.getMinutes();
				unitMeasureLabel = TimeUnitMeasure.M;
				unitButton.setIcon(AccountingManagerResources.INSTANCE
						.accountingUnitm24());
				updateChart();
			}
		});

		hItem.addSelectionHandler(new SelectionHandler<Item>() {

			@Override
			public void onSelection(SelectionEvent<Item> event) {
				unitMeasure = TimeUnitMeasure.getHours();
				unitMeasureLabel = TimeUnitMeasure.H;
				unitButton.setIcon(AccountingManagerResources.INSTANCE
						.accountingUnith24());
				updateChart();
			}
		});

		menuUnit.add(msItem);
		menuUnit.add(sItem);
		menuUnit.add(mItem);
		menuUnit.add(hItem);
		return menuUnit;

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
						this.@org.gcube.portlets.admin.accountingmanager.client.maindata.charts.service.ServiceChartBasicPanel::options);
		console.log(chart);
		chart.exportChart();

	}-*/;

	public static native void onDownloadJPG(String id) /*-{
		console.log(id);
		var chart = $wnd
				.jQuery('#' + id)
				.highcharts(
						this.@org.gcube.portlets.admin.accountingmanager.client.maindata.charts.service.ServiceChartBasicPanel::options);
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
						this.@org.gcube.portlets.admin.accountingmanager.client.maindata.charts.service.ServiceChartBasicPanel::options);
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
						this.@org.gcube.portlets.admin.accountingmanager.client.maindata.charts.service.ServiceChartBasicPanel::options);
		console.log(chart);
		chart.exportChart({
			type : 'image/svg+xml'
		});

	}-*/;

	private void createMultiAxisChart() {
		SeriesService seriesService = (SeriesService) accountingStateData
				.getSeriesResponse();

		if (!(seriesService.getSerieServiceDefinition() instanceof SeriesServiceBasic)) {
			Log.error("Invalid SeriesServiceBasic!");
			return;
		}
		SeriesServiceBasic seriesServiceBasic = (SeriesServiceBasic) seriesService
				.getSerieServiceDefinition();

		HighchartsOptionFactory highchartsFactory = new JsoHighchartsOptionFactory();
		options = highchartsFactory.createChartOptions();
		options.chart().zoomType("xy");
		options.navigation().buttonOptions().enabled(false);
		options.exporting().filename("AccountingServiceBasic");
		options.title().text("Accounting Service");

		/*
		 * options.subtitle().text("Click and drag in the plot area to zoom in");
		 */

		ArrayString colors = options.colors();
		// colors.setValue(0, "#cc0038");
		// colors.setValue(1, "#32cd32");

		// yAxis
		String multiAxis = "[{" + " \"id\": \""
				+ ServiceChartMeasure.OperationCount.name() + "\","
				+ " \"labels\": { " + "    \"format\": \"{value}\","
				+ "    \"style\": { " + "      \"color\": \"" + colors.get(1)
				+ "\"" + "    }" + " }," + " \"title\": { "
				+ "    \"text\": \""
				+ ServiceChartMeasure.OperationCount.getLabel() + "\","
				+ "    \"style\": {" + "       \"color\": \"" + colors.get(1)
				+ "\"" + "    }" + " }" + "} , {" + " \"id\": \""
				+ ServiceChartMeasure.Duration.name() + "\", "
				+ " \"title\": {" + "    \"text\": \""
				+ ServiceChartMeasure.Duration.getLabel() + "\","
				+ "    \"style\": {" + "       \"color\": \"" + colors.get(0)
				+ "\"" + "    }" + " }," + " \"labels\": {"
				+ "    \"format\": \"{value} " + unitMeasureLabel + "\","
				+ "    \"style\": {" + "       \"color\": \"" + colors.get(0)
				+ "\"" + "     }" + " }," + " \"opposite\": \"true\""
				+ ", \"showFirstLabel\": \"false\"" + "} , {" + " \"id\": \""
				+ ServiceChartMeasure.MaxInvocationTime.name() + "\", "
				+ " \"title\": {" + "    \"text\": \""
				+ ServiceChartMeasure.MaxInvocationTime.getLabel() + "\","
				+ "    \"style\": {" + "       \"color\": \"" + colors.get(2)
				+ "\"" + "    }" + " }," + " \"labels\": {"
				+ "    \"format\": \"{value} " + unitMeasureLabel + "\","
				+ "    \"style\": {" + "       \"color\": \"" + colors.get(2)
				+ "\"" + "     }" + " }," + " \"opposite\": \"true\""
				+ ", \"showFirstLabel\": \"false\"" + "} , {" + " \"id\": \""
				+ ServiceChartMeasure.MinInvocationTime.name() + "\", "
				+ " \"title\": {" + "    \"text\": \""
				+ ServiceChartMeasure.MinInvocationTime.getLabel() + "\","
				+ "    \"style\": {" + "       \"color\": \"" + colors.get(3)
				+ "\"" + "    }" + " }," + " \"labels\": {"
				+ "    \"format\": \"{value} " + unitMeasureLabel + "\","
				+ "    \"style\": {" + "       \"color\": \"" + colors.get(3)
				+ "\"" + "     }" + " }," + " \"opposite\": \"true\""
				+ ", \"showFirstLabel\": \"false\"" + "}]";

		options.setFieldAsJsonObject("yAxis", multiAxis);

		String fillcolor = "{" + "\"linearGradient\": {" + "\"x1\": 0,"
				+ "\"y1\": 0," + "\"x2\": 0," + "\"y2\": 1" + "},"
				+ "\"stops\": [" + "[" + "0, \"#058DC7\"" + "]," + "["
				+ "1, \"#FFFFFF\"" + "]" + "]" + "}";

		options.plotOptions().area()
				.setFieldAsJsonObject("fillColor", fillcolor).marker()
				.radius(2).lineWidth(1).states().hover().lineWidth(1);

		SeriesColumn seriesOperationCount = highchartsFactory
				.createSeriesColumn();
		seriesOperationCount
				.name(ServiceChartMeasure.OperationCount.getLabel());
		seriesOperationCount.color(colors.get(1));
		seriesOperationCount.type("column");

		SeriesArea seriesDuration = highchartsFactory.createSeriesArea();
		seriesDuration.name(ServiceChartMeasure.Duration.getLabel());
		seriesDuration.color(colors.get(0));
		seriesDuration.yAxisAsString(ServiceChartMeasure.Duration.name());

		SeriesArea seriesMaxInvocationTime = highchartsFactory
				.createSeriesArea();
		seriesMaxInvocationTime.name(ServiceChartMeasure.MaxInvocationTime
				.getLabel());
		seriesMaxInvocationTime.color(colors.get(2));
		seriesMaxInvocationTime
				.yAxisAsString(ServiceChartMeasure.MaxInvocationTime.name());

		SeriesArea seriesMinInvocationTime = highchartsFactory
				.createSeriesArea();
		seriesMinInvocationTime.name(ServiceChartMeasure.MinInvocationTime
				.getLabel());
		seriesMinInvocationTime.color(colors.get(3));
		seriesMinInvocationTime
				.yAxisAsString(ServiceChartMeasure.MinInvocationTime.name());

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
			
			ArrayNumber dataOperationCount = seriesOperationCount
					.dataAsArrayNumber();

			seriesOperationCount.pointInterval(interval);
			seriesOperationCount.pointStart(dateStart.getTime());

			ArrayNumber dataDuration = seriesDuration.dataAsArrayNumber();

			seriesDuration.pointInterval(interval);
			seriesDuration.pointStart(dateStart.getTime());

			ArrayNumber dataMaxInvocationTime = seriesMaxInvocationTime
					.dataAsArrayNumber();

			seriesMaxInvocationTime.pointInterval(interval);
			seriesMaxInvocationTime.pointStart(dateStart.getTime());

			ArrayNumber dataMinInvocationTime = seriesMinInvocationTime
					.dataAsArrayNumber();

			seriesMinInvocationTime.pointInterval(interval);
			seriesMinInvocationTime.pointStart(dateStart.getTime());

			for (SeriesServiceData seriesServiceData : seriesServiceBasic
					.getSeries()) {
				dataOperationCount.push(seriesServiceData.getOperationCount());
				dataDuration
						.push(seriesServiceData.getDuration() / unitMeasure);
				dataMaxInvocationTime.push(seriesServiceData
						.getMaxInvocationTime() / unitMeasure);
				dataMinInvocationTime.push(seriesServiceData
						.getMinInvocationTime() / unitMeasure);
			}

		} else {
			// xAxis
			options.xAxis().type("datetime");

			if (accountingStateData.getSeriesRequest().getAccountingPeriod()
					.getPeriod().compareTo(AccountingPeriodMode.MONTHLY) == 0) {
				//options.tooltip().xDateFormat("%b, %Y");
				options.tooltip().xDateFormat("Selected Data");

			} else {
				if (accountingStateData.getSeriesRequest()
						.getAccountingPeriod().getPeriod()
						.compareTo(AccountingPeriodMode.YEARLY) == 0) {
					//options.tooltip().xDateFormat("%Y");
					options.tooltip().xDateFormat("Selected Data");

				} else {
					options.tooltip().xDateFormat("Selected Data");
				}

			}

			Array<Data> arrayDataOperationCount = seriesOperationCount
					.dataAsArrayObject();
			Array<com.github.highcharts4gwt.model.highcharts.option.api.seriesarea.Data> arrayDataDuration = seriesDuration
					.dataAsArrayObject();
			Array<com.github.highcharts4gwt.model.highcharts.option.api.seriesarea.Data> arrayDataMaxInvocationTime = seriesMaxInvocationTime
					.dataAsArrayObject();
			Array<com.github.highcharts4gwt.model.highcharts.option.api.seriesarea.Data> arrayDataMinInvocationTime = seriesMinInvocationTime
					.dataAsArrayObject();

			for (SeriesServiceData seriesServiceData : seriesServiceBasic
					.getSeries()) {
				long dateFrom1970 = seriesServiceData.getDate().getTime();

				Log.debug("SeriersServiceData: " + seriesServiceData.getDate());
				Log.debug("SeriersServiceData: " + dateFrom1970);
				// dateFrom1970=dateFrom1970+7200000;
				// Log.debug("SeriersServiceData: "+dateFrom1970);

				Data dataOperationCount = highchartsFactory
						.createSeriesColumnData();
				dataOperationCount.x(dateFrom1970);
				dataOperationCount.y(seriesServiceData.getOperationCount());
				arrayDataOperationCount.addToEnd(dataOperationCount);

				com.github.highcharts4gwt.model.highcharts.option.api.seriesarea.Data dataDuration = highchartsFactory
						.createSeriesAreaData();
				dataDuration.x(dateFrom1970);
				dataDuration.y(seriesServiceData.getDuration() / unitMeasure);
				arrayDataDuration.addToEnd(dataDuration);

				com.github.highcharts4gwt.model.highcharts.option.api.seriesarea.Data dataMaxInvocationTime = highchartsFactory
						.createSeriesAreaData();
				dataMaxInvocationTime.x(dateFrom1970);
				dataMaxInvocationTime.y(seriesServiceData
						.getMaxInvocationTime() / unitMeasure);
				arrayDataMaxInvocationTime.addToEnd(dataMaxInvocationTime);

				com.github.highcharts4gwt.model.highcharts.option.api.seriesarea.Data dataMinInvocationTime = highchartsFactory
						.createSeriesAreaData();
				dataMinInvocationTime.x(dateFrom1970);
				dataMinInvocationTime.y(seriesServiceData
						.getMinInvocationTime() / unitMeasure);
				arrayDataMinInvocationTime.addToEnd(dataMinInvocationTime);

			}

		}

		options.series().addToEnd(seriesOperationCount);
		options.series().addToEnd(seriesDuration);
		options.series().addToEnd(seriesMaxInvocationTime);
		options.series().addToEnd(seriesMinInvocationTime);

		options.chart().showAxes(true);
		options.legend().enabled(true);
		return;
	}

	private void createSingleAxisChart() {
		SeriesService seriesService = (SeriesService) accountingStateData
				.getSeriesResponse();

		if (!(seriesService.getSerieServiceDefinition() instanceof SeriesServiceBasic)) {
			Log.debug("Invalid SeriesServiceBasic!");
			return;
		}

		SeriesServiceBasic seriesServiceBasic = (SeriesServiceBasic) seriesService
				.getSerieServiceDefinition();

		HighchartsOptionFactory highchartsFactory = new JsoHighchartsOptionFactory();
		options = highchartsFactory.createChartOptions();

		options.navigation().buttonOptions().enabled(false);
		options.exporting().filename("AccountingService");
		options.chart().zoomType("xy");
		options.title().text("Accounting Service");

		/*
		 * options.subtitle().text("Click and drag in the plot area to zoom in");
		 */

		ArrayString colors = options.colors();
		// colors.setValue(0, "#cc0038");
		// colors.setValue(1, "#32cd32");

		// yAxis
		String multiAxis = "[{" + " \"id\": \""
				+ ServiceChartMeasure.OperationCount.name() + "\","
				+ " \"labels\": { " + "    \"format\": \"{value}\","
				+ "    \"style\": { " + "      \"color\": \"" + colors.get(1)
				+ "\"" + "    }" + " }," + " \"title\": { "
				+ "    \"text\": \""
				+ ServiceChartMeasure.OperationCount.getLabel() + "\","
				+ "    \"style\": {" + "       \"color\": \"" + colors.get(1)
				+ "\"" + "    }" + " }" + "} , {"
				+ " \"id\": \"ServiceData\", " + " \"linkedTo\": \"0\","
				+ "  \"gridLineWidth\": \"0\"," + " \"title\": {"
				+ "    \"text\": \"\"," + "    \"style\": {"
				+ "       \"color\": \"" + colors.get(1) + "\"" + "    }"
				+ " }," + " \"labels\": {" + "    \"format\": \"{value} "
				+ unitMeasureLabel + "\"," + "    \"style\": {"
				+ "       \"color\": \"" + colors.get(1) + "\"" + "     }"
				+ " }," + " \"opposite\": \"true\"" + // +
														// ", \"showFirstLabel\": \"false\""
														// +
				"}]";

		options.setFieldAsJsonObject("yAxis", multiAxis);

		// does not seem to be working
		String fillcolor = "{" + "\"linearGradient\": {" + "\"x1\": 0,"
				+ "\"y1\": 0," + "\"x2\": 0," + "\"y2\": 1" + "},"
				+ "\"stops\": [" + "[" + "0, \"#058DC7\"" + "]," + "["
				+ "1, \"#FFFFFF\"" + "]" + "]" + "}";

		options.plotOptions().area()
				.setFieldAsJsonObject("fillColor", fillcolor).marker()
				.radius(2).lineWidth(1).states().hover().lineWidth(1);

		SeriesColumn seriesOperationCount = highchartsFactory
				.createSeriesColumn();
		seriesOperationCount
				.name(ServiceChartMeasure.OperationCount.getLabel());
		seriesOperationCount.color(colors.get(1));
		seriesOperationCount.type("column");

		SeriesArea seriesDuration = highchartsFactory.createSeriesArea();
		seriesDuration.name(ServiceChartMeasure.Duration.getLabel());
		seriesDuration.color(colors.get(0));
		// seriesDuration.yAxisAsString("ServiceData");

		SeriesArea seriesMaxInvocationTime = highchartsFactory
				.createSeriesArea();
		seriesMaxInvocationTime.name(ServiceChartMeasure.MaxInvocationTime
				.getLabel());
		seriesMaxInvocationTime.color(colors.get(2));
		// seriesMaxInvocationTime.yAxisAsString("ServiceData");

		SeriesArea seriesMinInvocationTime = highchartsFactory
				.createSeriesArea();
		seriesMinInvocationTime.name(ServiceChartMeasure.MinInvocationTime
				.getLabel());
		seriesMinInvocationTime.color(colors.get(3));
		// seriesMinInvocationTime.yAxisAsString("ServiceData");

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

			ArrayNumber dataOperationCount = seriesOperationCount
					.dataAsArrayNumber();

			seriesOperationCount.pointInterval(interval).pointStart(
					dateStart.getTime());

			ArrayNumber dataDuration = seriesDuration.dataAsArrayNumber();

			seriesDuration.pointInterval(interval).pointStart(
					dateStart.getTime());

			ArrayNumber dataMaxInvocationTime = seriesMaxInvocationTime
					.dataAsArrayNumber();

			seriesMaxInvocationTime.pointInterval(interval).pointStart(
					dateStart.getTime());

			ArrayNumber dataMinInvocationTime = seriesMinInvocationTime
					.dataAsArrayNumber();

			seriesMinInvocationTime.pointInterval(interval).pointStart(
					dateStart.getTime());

			for (SeriesServiceData seriesServiceData : seriesServiceBasic
					.getSeries()) {
				dataOperationCount.push(seriesServiceData.getOperationCount());
				dataDuration
						.push(seriesServiceData.getDuration() / unitMeasure);
				dataMaxInvocationTime.push(seriesServiceData
						.getMaxInvocationTime() / unitMeasure);
				dataMinInvocationTime.push(seriesServiceData
						.getMinInvocationTime() / unitMeasure);
			}

		} else {
			// xAxis
			options.xAxis().type("datetime");

			if (accountingStateData.getSeriesRequest().getAccountingPeriod()
					.getPeriod().compareTo(AccountingPeriodMode.MONTHLY) == 0) {
				//options.tooltip().xDateFormat("%b, %Y");
				options.tooltip().xDateFormat("Selected Data");

			} else {
				if (accountingStateData.getSeriesRequest()
						.getAccountingPeriod().getPeriod()
						.compareTo(AccountingPeriodMode.YEARLY) == 0) {
					//options.tooltip().xDateFormat("%Y");
					options.tooltip().xDateFormat("Selected Data");

				} else {
					options.tooltip().xDateFormat("Selected Data");
				}

			}

			Array<Data> arrayDataOperationCount = seriesOperationCount
					.dataAsArrayObject();
			Array<com.github.highcharts4gwt.model.highcharts.option.api.seriesarea.Data> arrayDataDuration = seriesDuration
					.dataAsArrayObject();
			Array<com.github.highcharts4gwt.model.highcharts.option.api.seriesarea.Data> arrayDataMaxInvocationTime = seriesMaxInvocationTime
					.dataAsArrayObject();
			Array<com.github.highcharts4gwt.model.highcharts.option.api.seriesarea.Data> arrayDataMinInvocationTime = seriesMinInvocationTime
					.dataAsArrayObject();

			for (SeriesServiceData seriesServiceData : seriesServiceBasic
					.getSeries()) {
				long dateFrom1970 = seriesServiceData.getDate().getTime();

				Log.debug("SeriersServiceData: " + seriesServiceData.getDate());
				Log.debug("SeriersServiceData: " + dateFrom1970);
				// dateFrom1970=dateFrom1970+7200000;
				// Log.debug("SeriersServiceData: "+dateFrom1970);

				Data dataOperationCount = highchartsFactory
						.createSeriesColumnData();
				dataOperationCount.x(dateFrom1970);
				dataOperationCount.y(seriesServiceData.getOperationCount());
				arrayDataOperationCount.addToEnd(dataOperationCount);

				com.github.highcharts4gwt.model.highcharts.option.api.seriesarea.Data dataDuration = highchartsFactory
						.createSeriesAreaData();
				dataDuration.x(dateFrom1970);
				dataDuration.y(seriesServiceData.getDuration() / unitMeasure);
				arrayDataDuration.addToEnd(dataDuration);

				com.github.highcharts4gwt.model.highcharts.option.api.seriesarea.Data dataMaxInvocationTime = highchartsFactory
						.createSeriesAreaData();
				dataMaxInvocationTime.x(dateFrom1970);
				dataMaxInvocationTime.y(seriesServiceData
						.getMaxInvocationTime() / unitMeasure);
				arrayDataMaxInvocationTime.addToEnd(dataMaxInvocationTime);

				com.github.highcharts4gwt.model.highcharts.option.api.seriesarea.Data dataMinInvocationTime = highchartsFactory
						.createSeriesAreaData();
				dataMinInvocationTime.x(dateFrom1970);
				dataMinInvocationTime.y(seriesServiceData
						.getMinInvocationTime() / unitMeasure);
				arrayDataMinInvocationTime.addToEnd(dataMinInvocationTime);

			}

		}

		options.series().addToEnd(seriesOperationCount);
		options.series().addToEnd(seriesDuration);
		options.series().addToEnd(seriesMaxInvocationTime);
		options.series().addToEnd(seriesMinInvocationTime);

		options.chart().showAxes(true);
		options.legend().enabled(true);
		return;
	}

}
