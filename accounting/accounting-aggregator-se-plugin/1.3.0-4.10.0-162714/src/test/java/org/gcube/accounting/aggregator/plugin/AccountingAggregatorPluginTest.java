package org.gcube.accounting.aggregator.plugin;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.gcube.accounting.aggregator.aggregation.AggregationType;
import org.gcube.accounting.aggregator.plugin.AccountingAggregatorPlugin.ElaborationType;
import org.gcube.accounting.aggregator.utility.Utility;
import org.gcube.accounting.datamodel.usagerecords.StorageUsageRecord;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AccountingAggregatorPluginTest extends ScopedTest {

	private static Logger logger = LoggerFactory.getLogger(AccountingAggregatorPluginTest.class);

	@Test
	public void aggregate() throws Exception {
		setContext(ROOT);
		
		Map<String, Object> inputs = new HashMap<String, Object>(); 	
		
		//type aggregation
		inputs.put(AccountingAggregatorPlugin.AGGREGATION_TYPE_INPUT_PARAMETER, AggregationType.MONTHLY.name());
		
		inputs.put(AccountingAggregatorPlugin.ELABORATION_TYPE_INPUT_PARAMETER, ElaborationType.AGGREGATE.name());
		
		
		inputs.put(AccountingAggregatorPlugin.PERSIST_START_TIME_INPUT_PARAMETER, Utility.getPersistTimeParameter(8, 0));
		inputs.put(AccountingAggregatorPlugin.PERSIST_END_TIME_INPUT_PARAMETER, Utility.getPersistTimeParameter(20, 30));
		
		
		inputs.put(AccountingAggregatorPlugin.RECORD_TYPE_INPUT_PARAMETER, StorageUsageRecord.class.newInstance().getRecordType());
			
		inputs.put(AccountingAggregatorPlugin.RESTART_FROM_LAST_AGGREGATION_DATE_INPUT_PARAMETER, false);
		
		Calendar aggregationStartCalendar = Utility.getAggregationStartCalendar(2016, Calendar.NOVEMBER, 1);
		String aggregationStartDate = AccountingAggregatorPlugin.AGGREGATION_START_DATE_DATE_FORMAT.format(aggregationStartCalendar.getTime());
		logger.trace("{} : {}", AccountingAggregatorPlugin.AGGREGATION_START_DATE_INPUT_PARAMETER, aggregationStartDate);
		inputs.put(AccountingAggregatorPlugin.AGGREGATION_START_DATE_INPUT_PARAMETER, aggregationStartDate);
		
		Calendar aggregationEndCalendar = Utility.getAggregationStartCalendar(2016, Calendar.DECEMBER, 1);
		String aggregationEndDate = AccountingAggregatorPlugin.AGGREGATION_START_DATE_DATE_FORMAT.format(aggregationEndCalendar.getTime());
		logger.trace("{} : {}", AccountingAggregatorPlugin.AGGREGATION_START_DATE_INPUT_PARAMETER, aggregationEndDate);
		inputs.put(AccountingAggregatorPlugin.AGGREGATION_END_DATE_INPUT_PARAMETER, aggregationEndDate);
		
		AccountingAggregatorPlugin plugin = new AccountingAggregatorPlugin(null);
		logger.debug("Going to launch {} with inputs {}", AccountingAggregatorPluginDeclaration.NAME, inputs);
					
		plugin.launch(inputs);
		
	}
	
	@Test
	public void testRecovery() throws Exception {
		setContext(ROOT);
		Map<String, Object> inputs = new HashMap<String, Object>(); 	
		
		inputs.put(AccountingAggregatorPlugin.ELABORATION_TYPE_INPUT_PARAMETER, ElaborationType.RECOVERY.name());
		
		inputs.put(AccountingAggregatorPlugin.PERSIST_START_TIME_INPUT_PARAMETER, Utility.getPersistTimeParameter(8, 0));
		inputs.put(AccountingAggregatorPlugin.PERSIST_END_TIME_INPUT_PARAMETER, Utility.getPersistTimeParameter(20, 30));
		
		Calendar aggregationStartCalendar = Utility.getAggregationStartCalendar(2016, Calendar.JANUARY, 1);
		String aggregationStartDate = AccountingAggregatorPlugin.AGGREGATION_START_DATE_DATE_FORMAT.format(aggregationStartCalendar.getTime());
		logger.trace("{} : {}", AccountingAggregatorPlugin.AGGREGATION_START_DATE_INPUT_PARAMETER, aggregationStartDate);
		inputs.put(AccountingAggregatorPlugin.AGGREGATION_START_DATE_INPUT_PARAMETER, aggregationStartDate);
		
		Calendar aggregationEndCalendar = Utility.getAggregationStartCalendar(2016, Calendar.FEBRUARY, 1);
		String aggregationEndDate = AccountingAggregatorPlugin.AGGREGATION_START_DATE_DATE_FORMAT.format(aggregationEndCalendar.getTime());
		logger.trace("{} : {}", AccountingAggregatorPlugin.AGGREGATION_START_DATE_INPUT_PARAMETER, aggregationEndDate);
		inputs.put(AccountingAggregatorPlugin.AGGREGATION_END_DATE_INPUT_PARAMETER, aggregationEndDate);
		
		
		AccountingAggregatorPlugin plugin = new AccountingAggregatorPlugin(null);
		logger.debug("Going to launch {} with inputs {}", AccountingAggregatorPluginDeclaration.NAME, inputs);
			
		plugin.launch(inputs);
		
	}
		
}
