/**
 * 
 */
package org.gcube.portlets.user.td.sdmxexportwidget.client.template;

import org.gcube.portlets.user.td.gwtservice.shared.Constants;
import org.gcube.portlets.user.td.gwtservice.shared.sdmx.SDMXTemplateExportSession;
import org.gcube.portlets.user.td.gwtservice.shared.source.SDMXRegistrySource;
import org.gcube.portlets.user.td.wizardwidget.client.WizardCard;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sencha.gxt.core.client.util.ToggleGroup;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.HideEvent;
import com.sencha.gxt.widget.core.client.event.HideEvent.HideHandler;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.FieldSet;
import com.sencha.gxt.widget.core.client.form.Radio;
import com.sencha.gxt.widget.core.client.form.TextField;

/**
 * 
 * @author Giancarlo Panichi
 *         <a href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a>
 * 
 */
public class SDMXTemplateExportTableDetailCard extends WizardCard {

	private final String TABLEDETAILPANELWIDTH = "100%";
	private final String TABLEDETAILPANELHEIGHT = "100%";
	private final String FORMWIDTH = "838px";

	private SDMXTemplateExportSession sdmxTemplateExportSession;
	private SDMXTemplateExportTableDetailCard thisCard;

	private VerticalLayoutContainer p = new VerticalLayoutContainer();
	private VerticalPanel tableDetailPanel;

	private TextField id;
	private TextField agencyId;
	private TextField registryBaseUrl;
	private TextField version;
	private TextField templateName;
	private TextField measureColumn;
	private Radio excelTrue;


	public SDMXTemplateExportTableDetailCard(final SDMXTemplateExportSession sdmxTemplateExportSession) {
		super("SDMX Table Detail", "");

		this.sdmxTemplateExportSession = sdmxTemplateExportSession;
		thisCard = this;

		tableDetailPanel = new VerticalPanel();

		tableDetailPanel.setSpacing(4);
		tableDetailPanel.setWidth(TABLEDETAILPANELWIDTH);
		tableDetailPanel.setHeight(TABLEDETAILPANELHEIGHT);

		FramedPanel form = new FramedPanel();
		form.setHeadingText("Details");
		form.setWidth(FORMWIDTH);

		FieldSet fieldSet = new FieldSet();
		fieldSet.setHeadingText("Information");
		fieldSet.setCollapsible(false);
		form.add(fieldSet);

		templateViewConfig();

		fieldSet.add(p);

		tableDetailPanel.add(form);
		setContent(tableDetailPanel);

	}

	private void templateViewConfig() {
		id = new TextField();
		id.setAllowBlank(false);
		id.setEmptyText("Enter Id...");
		id.setValue(Constants.SDMX_TEMPLATE_EXPORT_DEFAULT_ID);
		p.add(new FieldLabel(id, "Id"), new VerticalLayoutData(1, -1));

		agencyId = new TextField();
		agencyId.setVisible(true);
		agencyId.setEmptyText("Enter Agency Id...");
		if (sdmxTemplateExportSession.getAgency().getId() == null
				|| sdmxTemplateExportSession.getAgency().getId().isEmpty()) {
			agencyId.setValue(Constants.SDMX_TEMPLATE_EXPORT_DEFAULT_AGENCY_ID);
		} else {
			agencyId.setValue(sdmxTemplateExportSession.getAgency().getId());
		}

		FieldLabel agencyNameLabel = new FieldLabel(agencyId, "Agency Id");
		p.add(agencyNameLabel, new VerticalLayoutData(1, -1));

		registryBaseUrl = new TextField();
		registryBaseUrl.setVisible(false);
		registryBaseUrl.setEmptyText("Enter Registry URL...");
		String urlRegistry = ((SDMXRegistrySource) sdmxTemplateExportSession.getSource()).getUrl();
		if (urlRegistry == null || urlRegistry.isEmpty()) {
			registryBaseUrl.setValue(null);
		} else {
			registryBaseUrl.setValue(urlRegistry);
		}
		FieldLabel registryBaseUrlLabel = new FieldLabel(registryBaseUrl, "Registry URL");
		registryBaseUrlLabel.setVisible(false);
		p.add(registryBaseUrlLabel, new VerticalLayoutData(1, -1));

		version = new TextField();
		version.setAllowBlank(false);
		version.setEmptyText("Enter Version...");
		version.setValue(Constants.SDMX_TEMPLATE_EXPORT_DEFAULT_VERSION);
		p.add(new FieldLabel(version, "Version"), new VerticalLayoutData(1, -1));

		templateName = new TextField();
		templateName.setValue(sdmxTemplateExportSession.getTemplateData().getName());
		templateName.setReadOnly(true);
		p.add(new FieldLabel(templateName, "Template"), new VerticalLayoutData(1, -1));

		measureColumn = new TextField();
		measureColumn.setValue(sdmxTemplateExportSession.getObsValueColumn().getLabel());
		measureColumn.setReadOnly(true);
		p.add(new FieldLabel(measureColumn, "Measure Column"), new VerticalLayoutData(1, -1));

		excelTrue = new Radio();
		excelTrue.setBoxLabel("True");
		excelTrue.setValue(true);

		Radio excelFalse = new Radio();
		excelFalse.setBoxLabel("False");

		ToggleGroup excelToggle = new ToggleGroup();
		excelToggle.add(excelTrue);
		excelToggle.add(excelFalse);
		excelToggle.addValueChangeHandler(new ValueChangeHandler<HasValue<Boolean>>() {

			@Override
			public void onValueChange(ValueChangeEvent<HasValue<Boolean>> event) {
				
			}

		});

		HorizontalPanel hp = new HorizontalPanel();
		hp.add(excelTrue);
		hp.add(excelFalse);

		p.add(new FieldLabel(hp, "Excel"), new VerticalLayoutData(-1, -1));

	}

	@Override
	public void setup() {
		Command sayNextCard = new Command() {

			public void execute() {
				checkData();
			}

		};

		getWizardWindow().setNextButtonCommand(sayNextCard);

		Command sayPreviousCard = new Command() {
			public void execute() {
				try {
					getWizardWindow().previousCard();
					getWizardWindow().removeCard(thisCard);
					Log.info("Remove SDMXTemplateExportTableDetailCard");
				} catch (Exception e) {
					Log.error("sayPreviousCard :" + e.getLocalizedMessage());
				}
			}
		};

		getWizardWindow().setPreviousButtonCommand(sayPreviousCard);
		getWizardWindow().setEnableNextButton(true);

	}

	protected void checkData() {
		getWizardWindow().setEnableNextButton(false);
		getWizardWindow().setEnableBackButton(false);
		AlertMessageBox d;
		HideHandler hideHandler = new HideHandler() {

			public void onHide(HideEvent event) {
				getWizardWindow().setEnableNextButton(true);
				getWizardWindow().setEnableBackButton(false);

			}
		};

		if (id.getValue() == null || id.getValue().isEmpty() || !id.isValid() || version.getValue() == null
				|| version.getValue().isEmpty() || !version.isValid() || agencyId.getValue() == null
				|| agencyId.getValue().isEmpty() || !agencyId.isValid()) {

			d = new AlertMessageBox("Attention!", "Fill in all fields");
			d.addHideHandler(hideHandler);
			d.show();
		} else {
			if (!version.getValue().matches("[0-9]+\\.[0-9]+")) {
				d = new AlertMessageBox("Attention!", "Version must match the regular expression [0-9]+\\.[0-9]+");
				d.addHideHandler(hideHandler);
				d.show();
			} else {
				id.setReadOnly(true);
				registryBaseUrl.setReadOnly(true);
				version.setReadOnly(true);
				agencyId.setReadOnly(true);
				goNext();
			}
		}

	}

	protected void goNext() {
		try {
			sdmxTemplateExportSession.setId(id.getCurrentValue());
			sdmxTemplateExportSession.setAgencyId(agencyId.getCurrentValue());
			sdmxTemplateExportSession.setVersion(version.getCurrentValue());
			sdmxTemplateExportSession.setRegistryBaseUrl(registryBaseUrl.getCurrentValue());
			sdmxTemplateExportSession.setExcel(excelTrue.getValue());
			SDMXTemplateExportOperationInProgressCard sdmxOperationInProgressCard = new SDMXTemplateExportOperationInProgressCard(
					sdmxTemplateExportSession);
			getWizardWindow().addCard(sdmxOperationInProgressCard);
			Log.info("NextCard SDMXTemplateExportOperationInProgressCard");
			getWizardWindow().nextCard();
		} catch (Exception e) {
			Log.error("sayNextCard :" + e.getLocalizedMessage());
		}
	}

}
