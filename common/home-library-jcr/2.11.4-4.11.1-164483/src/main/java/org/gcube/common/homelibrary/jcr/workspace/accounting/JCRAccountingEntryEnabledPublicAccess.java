/**
 * 
 */
package org.gcube.common.homelibrary.jcr.workspace.accounting;

import java.util.Calendar;
import java.util.Map;

import org.gcube.common.homelibary.model.items.accounting.AccountingDelegate;
import org.gcube.common.homelibary.model.items.accounting.AccountingEntryType;
import org.gcube.common.homelibary.model.items.accounting.AccountingProperty;
import org.gcube.common.homelibrary.home.workspace.accounting.AccountingEntryEnabledPublicAccess;
import org.gcube.common.homelibrary.model.exceptions.RepositoryException;

import com.thoughtworks.xstream.XStream;


public class JCRAccountingEntryEnabledPublicAccess extends JCRAccountingEntry implements
AccountingEntryEnabledPublicAccess{

	protected String itemName;

	public JCRAccountingEntryEnabledPublicAccess(AccountingDelegate entryNode) throws RepositoryException {
		super(entryNode);
		this.itemName =  (String) new XStream().fromXML(entryNode.getAccountingProperties().get(AccountingProperty.ITEM_NAME));
	}


	public JCRAccountingEntryEnabledPublicAccess(String id, String user, Calendar date, String itemName) {
		super(id, user, date);	
		this.itemName = itemName;
		
		Map<AccountingProperty, String> properties = entryDelegate.getAccountingProperties();
		properties.put(AccountingProperty.ITEM_NAME, new XStream().toXML(itemName));
		
		entryDelegate.setEntryType(AccountingEntryType.ENABLED_PUBLIC_ACCESS);
	}

	@Override
	public AccountingEntryType getEntryType() {
		return AccountingEntryType.ENABLED_PUBLIC_ACCESS;
	}
	
	@Override
	public String toString() {
		String parentValue = super.toString();
		return String.format("[%s [%s, itemName:%s]]",parentValue, getEntryType(), getItemName());
	}


	@Override
	public String getItemName() {
		return itemName;
	}
}
