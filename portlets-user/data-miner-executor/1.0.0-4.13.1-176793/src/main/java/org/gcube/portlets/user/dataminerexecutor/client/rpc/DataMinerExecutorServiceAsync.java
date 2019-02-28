package org.gcube.portlets.user.dataminerexecutor.client.rpc;

import java.util.List;

import org.gcube.data.analysis.dataminermanagercl.shared.data.OutputData;
import org.gcube.data.analysis.dataminermanagercl.shared.data.TableItemSimple;
import org.gcube.data.analysis.dataminermanagercl.shared.data.computations.ComputationData;
import org.gcube.data.analysis.dataminermanagercl.shared.data.computations.ComputationId;
import org.gcube.data.analysis.dataminermanagercl.shared.parameters.Parameter;
import org.gcube.data.analysis.dataminermanagercl.shared.process.ComputationStatus;
import org.gcube.data.analysis.dataminermanagercl.shared.process.Operator;
import org.gcube.data.analysis.dataminermanagercl.shared.process.OperatorsClassification;
import org.gcube.data.analysis.dataminermanagercl.shared.workspace.DataMinerWorkArea;
import org.gcube.data.analysis.dataminermanagercl.shared.workspace.ItemDescription;
import org.gcube.portlets.user.dataminerexecutor.shared.session.UserInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * 
 * @author Giancarlo Panichi
 *
 *
 */
public interface DataMinerExecutorServiceAsync {

	public static DataMinerExecutorServiceAsync INSTANCE = (DataMinerExecutorServiceAsync) GWT
			.create(DataMinerExecutorService.class);

	void hello(AsyncCallback<UserInfo> callback);

	void getOperatorsClassifications(boolean refresh, AsyncCallback<List<OperatorsClassification>> callback);

	void getParameters(Operator operator, AsyncCallback<List<Parameter>> callback);

	void startComputation(Operator op, AsyncCallback<ComputationId> asyncCallback);

	void getComputationStatus(ComputationId computationId, AsyncCallback<ComputationStatus> asyncCallback);

	void resubmit(ItemDescription itemDescription, AsyncCallback<ComputationId> callback);

	void retrieveTableInformation(ItemDescription item, AsyncCallback<TableItemSimple> callback);

	void getDataMinerWorkArea(AsyncCallback<DataMinerWorkArea> asyncCallback);

	void getPublicLink(ItemDescription itemDescription, AsyncCallback<String> callback);

	void cancelComputation(ComputationId computationId, AsyncCallback<String> asyncCallback);

	void deleteItem(ItemDescription itemDescription, AsyncCallback<Void> callback);

	void getOutputDataByComputationId(ComputationId computationId, AsyncCallback<OutputData> callback);

	void getComputationData(ItemDescription itemDescription, AsyncCallback<ComputationData> callback);

	void cancelComputation(ItemDescription itemDescription, AsyncCallback<String> asyncCallback);

	void getItemDescription(String itemId, AsyncCallback<ItemDescription> asyncCallback);

	void getInvocationModel(String invocationModelFileUrl, AsyncCallback<Operator> callback);

}
