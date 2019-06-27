package org.gcube.data.analysis.tabulardata.operation.datatype;

import java.util.List;

import org.gcube.data.analysis.tabulardata.expression.evaluator.sql.SQLExpressionEvaluatorFactory;
import org.gcube.data.analysis.tabulardata.model.ValueFormat;
import org.gcube.data.analysis.tabulardata.model.column.Column;
import org.gcube.data.analysis.tabulardata.model.table.Table;
import org.gcube.data.analysis.tabulardata.operation.SQLHelper;

public class NumericToIntegerSQLHandler extends TypeTransitionSQLHandler {

	public NumericToIntegerSQLHandler(SQLExpressionEvaluatorFactory evaluator) {
		super(evaluator);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getCopyDataSQLCommand(Table newTable, Table targetTable, Column targetColumn, ValueFormat format) {
		StringBuilder sqlBuilder = new StringBuilder();
		List<Column> columnsToCopy = newTable.getColumns();
		String columnNamesSnippet = SQLHelper.generateColumnNameSnippet(columnsToCopy);
		sqlBuilder.append(String.format("INSERT INTO %s (%s) ", newTable.getName(), columnNamesSnippet));
		columnNamesSnippet = generateTypedColumnNameSnippet(newTable, targetColumn);
		sqlBuilder.append(String.format("SELECT %s FROM %s;", columnNamesSnippet, targetTable.getName()));
		return sqlBuilder.toString();
	}

	private String generateTypedColumnNameSnippet(Table newTable, Column targetColumn) {
		StringBuilder sb = new StringBuilder();
		for (Column column : newTable.getColumns()) {
			if (column.getName().equals(targetColumn.getName())) {
				sb.append(String.format("%s::integer", column.getName()));
			} else
				sb.append(column.getName());
			sb.append(", ");
		}
		sb.delete(sb.length() - 2, sb.length() - 1);
		return sb.toString();
	}

	
}
