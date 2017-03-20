package org.gcube.data.analysis.tabulardata.operation.data;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.gcube.data.analysis.tabulardata.model.column.ColumnLocalId;
import org.gcube.data.analysis.tabulardata.model.table.Table;
import org.gcube.data.analysis.tabulardata.model.table.TableId;
import org.gcube.data.analysis.tabulardata.operation.data.remove.DuplicateRowRemoverFactory;
import org.gcube.data.analysis.tabulardata.operation.test.OperationTester;
import org.gcube.data.analysis.tabulardata.operation.test.util.GenericHelper;
import org.gcube.data.analysis.tabulardata.operation.worker.WorkerFactory;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jeeunit.JeeunitRunner;

@RunWith(JeeunitRunner.class)
public class DuplicateRowRemoverTest extends OperationTester<DuplicateRowRemoverFactory> {

	private static final Logger log = LoggerFactory.getLogger(DuplicateRowRemoverTest.class);
	
	@Inject
	private DuplicateRowRemoverFactory factory;
	
	private Table testTable;
	
	@Inject
	GenericHelper helper;
	
	@Before
	public void setupTestTable(){
		testTable = helper.createSpeciesGenericTable();
	}
	
	@Override
	protected WorkerFactory getFactory() {
		return factory;
	}

	@Override
	protected Map<String,Object> getParameterInstances() {
		return new HashMap<String, Object>();
	}

	@Override
	protected ColumnLocalId getTargetColumnId() {
		return null;
	}

	@Override
	protected TableId getTargetTableId() {
		return testTable.getId();
	}

}
