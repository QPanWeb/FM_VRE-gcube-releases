package org.gcube.data.spd.gbifplugin.search.query;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ResultType{
	Occurrence("occurrence"),
	Taxon("species");
	
	@Getter
	private @NonNull String queryEntry;

}