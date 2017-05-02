package org.gcube.data.spd.obisplugin.search.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class QueryCount {
	
	private static Logger log = LoggerFactory.getLogger(QueryCount.class);
	
	private @NonNull String baseUri;
	
	List<QueryCondition> conditions = new ArrayList<QueryCondition>();
	
	private @NonNull ResultType resultType;

	public void setConditions(QueryCondition ... conditions){
		this.conditions.addAll(Arrays.asList(conditions));
	}
	
	public List<QueryCondition> getConditions() {
		return conditions;
	}
	
	
	public int getCount(){
		Map<String, Object> mapping;
		try {
			mapping = MappingUtils.getObjectMapping(this.build());
			if (mapping.get("count")==null) return 0;
			return (Integer)mapping.get("count");
		} catch (Exception e) {
			log.error("error computing count, returning 0",e);
			return 0;
		}
				
	}
	
	
	private String build(){
		StringBuilder query = new StringBuilder(baseUri);
		if (!baseUri.endsWith("/")) query.append("/");
		query.append(this.resultType.getQueryEntry()).append("/");
		query.append("?limit=0");

		if (conditions.size()>0)
			for (QueryCondition queryCond: conditions)
				query.append("&").append(queryCond.getKey().replaceAll(" ", "%20")).append("=").append(queryCond.getValue().replaceAll(" ", "%20"));
		return query.toString();
	}



	
}
