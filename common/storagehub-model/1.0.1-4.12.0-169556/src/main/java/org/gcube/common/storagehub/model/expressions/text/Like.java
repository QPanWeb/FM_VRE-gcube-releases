package org.gcube.common.storagehub.model.expressions.text;

import org.gcube.common.storagehub.model.expressions.Expression;
import org.gcube.common.storagehub.model.expressions.SearchableField;

public class Like implements Expression<Boolean> {

	private SearchableField<String> searchableField;
	private String value;

	protected Like() {}
	
	public Like(SearchableField<String> searchableField, String value) {
		super();
		this.searchableField = searchableField;
		this.value = value;
	}

	public SearchableField<String> getSearchableField() {
		return searchableField;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "Contains [searchableField=" + searchableField + ", value=" + value + "]";
	}
	
	
	
}
