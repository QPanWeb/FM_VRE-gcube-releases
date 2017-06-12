package org.gcube.vremanagement.resourcemanager.impl.operators;

import org.gcube.common.core.utils.logging.GCUBELog;
import org.gcube.vremanagement.resourcemanager.impl.contexts.ServiceContext;
import org.gcube.vremanagement.resourcemanager.impl.state.ScopeState;
import org.gcube.vremanagement.resourcemanager.stubs.binder.RemoveResourcesParameters;
import org.gcube.vremanagement.resourcemanager.stubs.binder.ResourceList;

/**
 * A Resources Operator that coordinates the removal of resources from the scope
 * 
 * @author Manuele Simi (ISTI-CNR)
 *
 */
public class RemoveResourcesOperator extends Operator {

	protected final GCUBELog logger = new GCUBELog(this, ServiceContext.getContext());	
	
	private OperatorConfig configuration;
	
	private RemoveResourcesParameters resourceList;
	
	public RemoveResourcesOperator(ScopeState scopeState, OperatorConfig configuration, RemoveResourcesParameters resourceList) {
		this.configuration = configuration;
		this.resourceList = resourceList;
		this.scopeState = scopeState;
	}
	
	public void exec() throws Exception {
		
		//removes the resources from the PublishedScopeResource, if any
		ResourceList resources = resourceList.getResources();
		if ((resources == null) || (resources.getResource().length == 0)) 
			logger.warn("The list of resource to add is empty");
		else {
			try {
				new ScopedResourceManagerOperator(scopeState, configuration, resources, ACTION.REMOVE).run();
			}catch (Exception e) {
				logger.error("Unable to manage the given resource(s)", e);
				throw new Exception("Unable to manage the given resource(s)", e);
			}	
		}		
	}

}
