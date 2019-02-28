package org.gcube.common.storagehub.model.items.nodes.accounting;

import org.gcube.common.storagehub.model.annotations.Attribute;
import org.gcube.common.storagehub.model.annotations.AttributeRootNode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@AttributeRootNode("nthl:accountingEntryDelete")
public class AccountEntryDelete extends AccountEntry{

	@Attribute("hl:itemName")
	String itemName;

	@Attribute("hl:fromPath")
	String fromPath;
	
	AccountingEntryType type = AccountingEntryType.DELETE;
}
