package org.gcube.portlets.user.td.rulewidget.client;

import org.gcube.portlets.user.td.gwtservice.shared.rule.description.RuleDescriptionData;
import org.gcube.portlets.user.td.gwtservice.shared.user.UserInfo;
import org.gcube.portlets.user.td.rulewidget.client.resources.ResourceBundle;
import org.gcube.portlets.user.td.sharewidget.client.RuleShare;
import org.gcube.portlets.user.td.widgetcommonevent.client.CommonMessages;
import org.gcube.portlets.user.td.wizardwidget.client.util.UtilsGXT3;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.shared.GWT;
import com.google.web.bindery.event.shared.EventBus;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;

/**
 * 
 * @author "Giancarlo Panichi" <a
 *         href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a>
 *
 */
public class RuleShareDialog extends Window {
	private static final String WIDTH = "770px";
	private static final String HEIGHT = "530px";

	private EventBus eventBus;
	private RuleShareMessages msgs;
	private UserInfo userInfo;
	private CommonMessages msgsCommon;

	public RuleShareDialog(UserInfo userInfo, EventBus eventBus) {
		this.eventBus = eventBus;
		this.userInfo = userInfo;
		initMessages();
		initWindow();
		RuleSharePanel templateDeletePanel = new RuleSharePanel(this, eventBus);
		add(templateDeletePanel);
	}

	protected void initMessages() {
		msgs = GWT.create(RuleShareMessages.class);
		msgsCommon = GWT.create(CommonMessages.class);
	}

	protected void initWindow() {
		setWidth(WIDTH);
		setHeight(HEIGHT);
		setBodyBorder(false);
		setResizable(false);
		setHeadingText(msgs.dialogRuleShareHead());
		setClosable(true);
		setModal(true);
		forceLayoutOnResize = true;
		getHeader().setIcon(ResourceBundle.INSTANCE.ruleShare());

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initTools() {
		super.initTools();

		closeBtn.addSelectHandler(new SelectHandler() {

			public void onSelect(SelectEvent event) {
				close();
			}
		});

	}

	protected void close() {
		hide();
	}

	public void ruleShare(RuleDescriptionData ruleDescriptionData) {
		Log.debug("Share Window");
		if (userInfo.getUsername().compareTo(
				ruleDescriptionData.getOwnerLogin()) == 0) {
			@SuppressWarnings("unused")
			RuleShare ruleShare = new RuleShare(userInfo, ruleDescriptionData, eventBus);
			close();
		} else {
			UtilsGXT3
					.info(msgsCommon.attention(), msgs.attentionNotOwnerRule());
		}

	}

}
