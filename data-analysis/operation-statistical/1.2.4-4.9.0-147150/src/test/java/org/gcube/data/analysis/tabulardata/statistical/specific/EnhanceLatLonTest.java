package org.gcube.data.analysis.tabulardata.statistical.specific;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.gcube.data.analysis.tabulardata.cube.CubeManager;
import org.gcube.data.analysis.tabulardata.cube.tablemanagers.TableCreator;
import org.gcube.data.analysis.tabulardata.model.column.ColumnLocalId;
import org.gcube.data.analysis.tabulardata.model.column.factories.BaseColumnFactory;
import org.gcube.data.analysis.tabulardata.model.column.factories.ColumnFactory;
import org.gcube.data.analysis.tabulardata.model.column.type.AttributeColumnType;
import org.gcube.data.analysis.tabulardata.model.datatype.NumericType;
import org.gcube.data.analysis.tabulardata.model.metadata.common.ImmutableLocalizedText;
import org.gcube.data.analysis.tabulardata.model.table.Table;
import org.gcube.data.analysis.tabulardata.model.table.TableId;
import org.gcube.data.analysis.tabulardata.model.table.type.GenericTableType;
import org.gcube.data.analysis.tabulardata.operation.parameters.Parameter;
import org.gcube.data.analysis.tabulardata.operation.test.OperationTester;
import org.gcube.data.analysis.tabulardata.operation.test.util.CopyHandler;
import org.gcube.data.analysis.tabulardata.operation.worker.WorkerFactory;
import org.gcube.data.analysis.tabulardata.statistical.TokenSetter;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.googlecode.jeeunit.JeeunitRunner;

@RunWith(JeeunitRunner.class)
public class EnhanceLatLonTest extends OperationTester<EnhanceLatLonFactory> {

	@Inject
	private EnhanceLatLonFactory factory;
	
	
	@Inject
	private CopyHandler copyHandler;
	
	@Inject
	private CubeManager cm;
	
	private Table testTable;
	
	@BeforeClass
	public static void init(){
		TokenSetter.set("/gcube/devNext/NextNext");
	}
	
	@Before
	public void setupTestTable(){
		testTable = createTable();
	}
	
	@Override
	protected WorkerFactory getFactory() {
		return factory;
	}

	@Override
	protected Map getParameterInstances() {
		HashMap<String,Object> params= new HashMap<String, Object>();
		params.put(EnhanceLatLonFactory.LATITUDE_COLUMN_PARAM.getIdentifier(), testTable.getColumnReference(testTable.getColumnsByType(AttributeColumnType.class).get(0)));
		params.put(EnhanceLatLonFactory.LONGITUTE_COLUMN_PARAM.getIdentifier(), testTable.getColumnReference(testTable.getColumnsByType(AttributeColumnType.class).get(1)));
		params.put(EnhanceLatLonFactory.TO_ADD_FEATURE_PARAM.getIdentifier(), LatLongFeature.CSQUARECODE.toString());
		params.put(EnhanceLatLonFactory.DELETE_GENERATED.getIdentifier(), true);
		params.put(EnhanceLatLonFactory.DELETE_REMOTE.getIdentifier(), true);
//		params.put(EnhanceLatLonFactory.USER.getIdentifier(), "fabio.sinibaldi");
		return params;
	}

	@Override
	protected ColumnLocalId getTargetColumnId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected TableId getTargetTableId() {
		return testTable.getId();
	}

	
	private Table createTable(){
		ColumnFactory colFactory=BaseColumnFactory.getFactory(new AttributeColumnType());
		TableCreator tc=cm.createTable(new GenericTableType());
		Table table=null;
		try{
			tc.addColumn(colFactory.create(new ImmutableLocalizedText("Csquare_code"),new NumericType()));
			tc.addColumn(colFactory.create(new ImmutableLocalizedText("Center Long"),new NumericType()));
			table=tc.create();
		}catch(Exception e){
			e.printStackTrace();
		}
//		copyHandler.copy("Lat_lon_hcaf_s.csv", table);
		copyHandler.copy("GeospatialTest.csv", table);
		return table;
	}
	
	
	@Test
	public void printParams(){
		for(Parameter p:getFactory().getOperationDescriptor().getParameters())
		System.out.println(p);
	}
}
