package org.gcube.portlets.user.td.gwtservice.server.opexecution;

import java.util.HashMap;
import java.util.Map;

import org.gcube.data.analysis.tabulardata.commons.webservice.types.operations.OperationDefinition;
import org.gcube.data.analysis.tabulardata.commons.webservice.types.operations.OperationExecution;
import org.gcube.data.analysis.tabulardata.expression.Expression;
import org.gcube.data.analysis.tabulardata.service.TabularDataService;
import org.gcube.portlets.user.td.gwtservice.server.trservice.ExpressionGenerator;
import org.gcube.portlets.user.td.gwtservice.server.trservice.OperationDefinitionMap;
import org.gcube.portlets.user.td.gwtservice.shared.Constants;
import org.gcube.portlets.user.td.gwtservice.shared.exception.TDGWTServiceException;
import org.gcube.portlets.user.td.gwtservice.shared.tr.column.ReplaceColumnSession;
import org.gcube.portlets.user.td.widgetcommonevent.shared.operations.OperationsId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operation Execution for replace column
 * 
 * @author "Giancarlo Panichi" email: <a
 *         href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a>
 * 
 */
public class OpExecution4ReplaceColumn extends OpExecutionBuilder {
	private static Logger logger = LoggerFactory
			.getLogger(OpExecution4ReplaceColumn.class);

	private TabularDataService service;
	private ReplaceColumnSession replaceColumnSession;

	public OpExecution4ReplaceColumn(TabularDataService service,
			ReplaceColumnSession replaceColumnSession) {
		this.service = service;
		this.replaceColumnSession = replaceColumnSession;
	}

	@Override
	public void buildOpEx() throws TDGWTServiceException {

		OperationExecution invocation = null;

		logger.debug(replaceColumnSession.toString());
		OperationDefinition operationDefinition;
		Map<String, Object> map = new HashMap<String, Object>();

		if (replaceColumnSession.isReplaceDimension()) {
			logger.debug("Is a Replace of view column");
			operationDefinition = OperationDefinitionMap.map(
					OperationsId.ReplaceColumnByExpression.toString(), service);

			Expression condition = ExpressionGenerator
					.genReplaceValueParameterCondition(replaceColumnSession,service);
			Expression value = ExpressionGenerator
					.genReplaceValueParameterValue(replaceColumnSession);

			map.put(Constants.PARAMETER_REPLACE_BY_EXPRESSION_COLUMN_CONDITION,
					condition);
			map.put(Constants.PARAMETER_REPLACE_BY_EXPRESSION_COLUMN_VALUE,
					value);

			invocation = new OperationExecution(replaceColumnSession
					.getColumnData().getColumnViewData()
					.getSourceTableDimensionColumnId(),
					operationDefinition.getOperationId(), map);

		} else {
			logger.debug("Is a Replace of basic column");

			operationDefinition = OperationDefinitionMap.map(
					OperationsId.ReplaceColumnByExpression.toString(), service);

			Expression condition = ExpressionGenerator
					.genReplaceValueParameterCondition(replaceColumnSession,service);
			Expression value = ExpressionGenerator
					.genReplaceValueParameterValue(replaceColumnSession);

			map.put(Constants.PARAMETER_REPLACE_BY_EXPRESSION_COLUMN_CONDITION,
					condition);
			map.put(Constants.PARAMETER_REPLACE_BY_EXPRESSION_COLUMN_VALUE,
					value);

			invocation = new OperationExecution(replaceColumnSession
					.getColumnData().getColumnId(),
					operationDefinition.getOperationId(), map);
		}


		operationExecutionSpec.setOp(invocation);

	}

}
