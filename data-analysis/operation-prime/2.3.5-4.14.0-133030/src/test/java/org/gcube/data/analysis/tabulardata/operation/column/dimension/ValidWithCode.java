package org.gcube.data.analysis.tabulardata.operation.column.dimension;

public class ValidWithCode extends ChangeToDimensionColumnTest {

	@Override
	protected int getTargetCodelistReferenceColumnIndex() {
		return 1;
		
	}

	@Override
	protected int getTargetDatasetColumnIndex() {
		return 5;
	}

}
