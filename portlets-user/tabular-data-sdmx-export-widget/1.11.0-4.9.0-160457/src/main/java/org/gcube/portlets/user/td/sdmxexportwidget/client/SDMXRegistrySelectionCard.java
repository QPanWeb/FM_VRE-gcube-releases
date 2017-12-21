/**
 * 
 */
package org.gcube.portlets.user.td.sdmxexportwidget.client;

import org.gcube.portlets.user.td.gwtservice.client.rpc.TDGWTServiceAsync;
import org.gcube.portlets.user.td.gwtservice.shared.exception.TDGWTIsLockedException;
import org.gcube.portlets.user.td.gwtservice.shared.exception.TDGWTSessionExpiredException;
import org.gcube.portlets.user.td.gwtservice.shared.sdmx.SDMXExportSession;
import org.gcube.portlets.user.td.gwtservice.shared.source.SDMXRegistrySource;
import org.gcube.portlets.user.td.gwtservice.shared.tr.TabResource;
import org.gcube.portlets.user.td.widgetcommonevent.client.event.SessionExpiredEvent;
import org.gcube.portlets.user.td.widgetcommonevent.client.type.SessionExpiredType;
import org.gcube.portlets.user.td.widgetcommonevent.shared.tr.TableType;
import org.gcube.portlets.user.td.wizardwidget.client.WizardCard;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sencha.gxt.core.client.util.ToggleGroup;
import com.sencha.gxt.widget.core.client.box.AutoProgressMessageBox;
import com.sencha.gxt.widget.core.client.container.MarginData;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.Radio;
import com.sencha.gxt.widget.core.client.form.TextField;

/**
 * 
 * @author Giancarlo Panichi
 *
 * 
 */
public class SDMXRegistrySelectionCard extends WizardCard {

	protected SDMXExportSession exportSession;
	protected SDMXRegistrySelectionCard thisCard;

	final TextField url = new TextField();

	public SDMXRegistrySelectionCard(final SDMXExportSession exportSession) {
		super("SDMX Registry selection", "");
		thisCard = this;
		SDMXRegistrySource sdmxRegistrySource = new SDMXRegistrySource();
		exportSession.setSource(sdmxRegistrySource);
		this.exportSession = exportSession;
		
		final AutoProgressMessageBox box = new AutoProgressMessageBox("Wait", "Retrieving Information, please wait...");
		box.setProgressText("Retrieving...");
		box.auto();
		box.show();

		retrieveTabularDataInfo(box);
	}
	

	private void create(String defaultRegistryUrl) {

		VerticalPanel registrySelectionPanel = new VerticalPanel();
		registrySelectionPanel.setStylePrimaryName(res.wizardCSS().getImportSelectionSources());

		Radio radioStandardRegistry = new Radio();

		if (defaultRegistryUrl == null || defaultRegistryUrl.isEmpty()) {
			radioStandardRegistry.setBoxLabel("<p style='display:inline-table;'><b>Internal SDMX Registry</b>"
					+ "<br>The export will be performed on the Internal Register" + "</p>");
		} else {
			radioStandardRegistry.setBoxLabel("<p style='display:inline-table;'><b>Internal SDMX Registry</b>"
					+ "<br>The export will be performed on the <a href='" + defaultRegistryUrl
					+ "'>Internal Registry</a>" + "</p>");
		}
		radioStandardRegistry.setValue(true);
		radioStandardRegistry.setName("Default");
		radioStandardRegistry.setStylePrimaryName(res.wizardCSS().getImportSelectionSource());

		Radio radioUrlRegistry = new Radio();
		radioUrlRegistry.setBoxLabel(
				"<p style='display:inline-table;'><b>Another SDMX Registry</b><br><SPAN id='SDMXRegistryUrl'></SPAN></p>");
		radioUrlRegistry.setName("Url");
		radioUrlRegistry.setStylePrimaryName(res.wizardCSS().getImportSelectionSource());
		radioUrlRegistry.disable();
		radioUrlRegistry.setVisible(false);

		url.setName("sdmxRegistryUrlInpuntField");
		url.setId("sdmxRegistryUrlInputFieldId");
		url.setStylePrimaryName(res.wizardCSS().getSDMXRegistryUrlInputStyle());

		final VerticalLayoutContainer vcontainer = new VerticalLayoutContainer();
		vcontainer.add(new FieldLabel(url, "URL"), new VerticalLayoutData(-1, -1));
		vcontainer.setStylePrimaryName(res.wizardCSS().getSDMXRegistryUrlStyle());
		vcontainer.setVisible(false);

		NodeList<Element> nodel = radioUrlRegistry.getElement().getElementsByTagName("SPAN");
		Element span = nodel.getItem(0);
		span.appendChild(vcontainer.getElement());

		registrySelectionPanel.add(radioStandardRegistry);
		registrySelectionPanel.add(radioUrlRegistry);

		// we can set name on radios or use toggle group
		ToggleGroup toggle = new ToggleGroup();
		toggle.add(radioStandardRegistry);
		toggle.add(radioUrlRegistry);

		toggle.addValueChangeHandler(new ValueChangeHandler<HasValue<Boolean>>() {

			public void onValueChange(ValueChangeEvent<HasValue<Boolean>> event) {
				try {
					ToggleGroup group = (ToggleGroup) event.getSource();
					Radio radio = (Radio) group.getValue();
					Log.info("Registry Selected:" + radio.getName());
					if (radio.getName().compareTo("Url") == 0) {
						vcontainer.setVisible(true);
						NodeList<Element> nodel = radio.getElement().getElementsByTagName("SPAN");
						Element span = nodel.getItem(0);
						span.appendChild(vcontainer.getElement());

						Log.info("SDMXRegistry Url");
						Log.info("Input value: " + url.getCurrentValue());
						SDMXRegistrySource r = ((SDMXRegistrySource) exportSession.getSource());
						// TODO r.setUrl(url.getCurrentValue());
						r.setUrl(null);
						Log.info("" + exportSession.getSource());
					} else {
						vcontainer.setVisible(false);
						Log.info("SDMXRegistry Default");
						SDMXRegistrySource r = ((SDMXRegistrySource) exportSession.getSource());
						r.setUrl(null);
						Log.info("" + exportSession.getSource());
					}
				} catch (Exception e) {
					Log.error("ToggleGroup: onValueChange " + e.getLocalizedMessage());
				}

			}
		});

		setCenterWidget(registrySelectionPanel, new MarginData(0));

		if (exportSession != null && exportSession.getExportType() != null) {
			switch (exportSession.getExportType()) {
			case CODELIST:
				break;
			case DATASET:
				Log.debug("Set caution for dataset");
				setFooter(
						"Caution: when you publish a dataset on SDMX Registry, it will also be shared with all users of the current VRE!");
				break;
			case GENERIC:
			default:
				break;

			}
		}

		forceLayout();
	}
	
	
	private void retrieveTabularDataInfo(final AutoProgressMessageBox box) {
		TDGWTServiceAsync.INSTANCE.getTabResourceInformation(new AsyncCallback<TabResource>() {

			public void onFailure(Throwable caught) {
				if (caught instanceof TDGWTSessionExpiredException) {
					getEventBus().fireEvent(new SessionExpiredEvent(SessionExpiredType.EXPIREDONSERVER));
				} else {
					if (caught instanceof TDGWTIsLockedException) {
						Log.error(caught.getLocalizedMessage());
						showErrorAndHide("Error Locked", caught.getLocalizedMessage(), "", caught);
					} else {
						Log.error(
								"No Tabular Resource Information retrived from server " + caught.getLocalizedMessage());
						box.hide();
						showErrorAndHide("Error", "Error retrieving tabular resource information: ",
								caught.getLocalizedMessage(), caught);
					}
				}
			}

			public void onSuccess(TabResource result) {
				Log.debug("Tabular Resource Information retrieved");
				exportSession.setTabResource(result);
				exportSession.setExportType(TableType.getColumnDataTypeFromId(result.getTableTypeName()));
				box.hide();
				retrieveDefaultRegistryURL();
			}
		});
	}
	

	private void retrieveDefaultRegistryURL() {
		mask("Please Wait...");

		TDGWTServiceAsync.INSTANCE.getDefaultSDMXRegistryURL(new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				unmask();
				if (caught instanceof TDGWTSessionExpiredException) {
					getEventBus().fireEvent(new SessionExpiredEvent(SessionExpiredType.EXPIREDONSERVER));
				} else {

					Log.error("Error retrieving URL of default SDMX Registry " + caught.getLocalizedMessage());

				}

			}

			@Override
			public void onSuccess(String defaultURLRegistry) {
				unmask();
				create(defaultURLRegistry);
			}
		});
	}

	@Override
	public void setup() {
		Command sayNextCard = new Command() {
			public void execute() {
				try {
					Log.info("Input value: " + url.getCurrentValue());
					if (exportSession.getSource() instanceof SDMXRegistrySource) {
						((SDMXRegistrySource) exportSession.getSource()).setUrl(url.getCurrentValue());

						TDGWTServiceAsync.INSTANCE.setSDMXRegistrySource(
								((SDMXRegistrySource) exportSession.getSource()), new AsyncCallback<Void>() {

									public void onFailure(Throwable caught) {
										if (caught instanceof TDGWTSessionExpiredException) {
											getEventBus().fireEvent(
													new SessionExpiredEvent(SessionExpiredType.EXPIREDONSERVER));
										} else {
											Log.error(
													"SDMXRegistrySource do not stored " + caught.getLocalizedMessage());
											showErrorAndHide("Error", "SDMXRegistrySource do not stored.",
													caught.getLocalizedMessage(), caught);
										}
									}

									public void onSuccess(Void result) {
										Log.info("SDMXRegistrySource stored");
										goNext();

									}
								});
					} else {
						Log.error("There is a problem in source selection.Expected SDMXRegistrySource, and found"
								+ exportSession.getSource());
					}

				} catch (Exception e) {
					Log.error("sayNextCard :" + e.getLocalizedMessage());
				}
			}
		};

		getWizardWindow().setNextButtonCommand(sayNextCard);

		Command sayPreviousCard = new Command() {
			public void execute() {
				try {
					getWizardWindow().previousCard();
					getWizardWindow().removeCard(thisCard);
					Log.info("Remove SDMXRegistrySelectionCard");
				} catch (Exception e) {
					Log.error("sayPreviousCard :" + e.getLocalizedMessage());
				}
			}
		};

		getWizardWindow().setPreviousButtonCommand(sayPreviousCard);

		setEnableBackButton(false);
		setEnableNextButton(true);
		setBackButtonVisible(false);

	}

	protected void goNext() {
		try {
			SDMXAgenciesSelectionCard sdmxAgencyTypeCard = new SDMXAgenciesSelectionCard(exportSession);
			getWizardWindow().addCard(sdmxAgencyTypeCard);
			Log.info("NextCard SDMXAgenciesSelectionCard ");
			getWizardWindow().nextCard();
		} catch (Exception e) {
			Log.error("sayNextCard :" + e.getLocalizedMessage());
		}
	}

}
