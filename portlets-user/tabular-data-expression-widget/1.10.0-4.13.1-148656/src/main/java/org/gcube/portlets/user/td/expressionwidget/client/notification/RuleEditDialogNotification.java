package org.gcube.portlets.user.td.expressionwidget.client.notification;


/**
 * 
 * @author Giancarlo Panichi
 *
 *
 */
public class RuleEditDialogNotification {


	public interface RuleEditDialogNotificationListener {
		void onNotification(RuleEditDialogNotification ruleEditDialogNotification);

		void aborted();

		void failed(Throwable throwable);
	}

	public interface HasRuleEditDialogNotificationListener {
		public void addRuleEditDialogNotificationListener(
				RuleEditDialogNotificationListener handler);

		public void removeRuleEditDialogNotificationListener(
				RuleEditDialogNotificationListener handler);

	}

	public RuleEditDialogNotification() {
	
	}

	@Override
	public String toString() {
		return "RuleEditDialogNotification []";
	}

	
}