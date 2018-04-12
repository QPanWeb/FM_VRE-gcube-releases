package org.gcube.data.transfer.service.transfers.engine.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import javax.inject.Singleton;

import org.gcube.data.transfer.model.ExecutionReport;
import org.gcube.data.transfer.model.PluginDescription;
import org.gcube.data.transfer.model.PluginInvocation;
import org.gcube.data.transfer.plugin.AbstractPlugin;
import org.gcube.data.transfer.plugin.AbstractPluginFactory;
import org.gcube.data.transfer.plugin.fails.PluginException;
import org.gcube.data.transfer.plugin.fails.PluginExecutionException;
import org.gcube.data.transfer.plugin.model.DataTransferContext;
import org.gcube.data.transfer.service.transfers.engine.PluginManager;
import org.gcube.data.transfer.service.transfers.engine.faults.PluginNotFoundException;
import org.gcube.smartgears.ContextProvider;

import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PluginManagerImpl implements PluginManager {

	private static ServiceLoader<AbstractPluginFactory> abstractFactoryLoader   = null;
	
	private static Map<String,PluginDescription> installedPlugins=null;
	
	private static PluginManagerImpl instance=null;
	
	public static synchronized PluginManagerImpl get() {
		if(instance==null)
			instance=new PluginManagerImpl();
		return instance;
	}
	

	
	// INSTANCE
	
	
	public PluginManagerImpl() {
		load();
	}
	@Override
	public Map<String, PluginDescription> getInstalledPlugins() {
		return installedPlugins;
	}
	
	
	@Synchronized
	private static Map<String, PluginDescription> load() {
		if(installedPlugins==null){
			Map<String,PluginDescription> toSet=new HashMap<String,PluginDescription>(); 
			log.trace("Loading plugins descriptors..");
			abstractFactoryLoader=ServiceLoader.load(AbstractPluginFactory.class);
			for(AbstractPluginFactory factory:abstractFactoryLoader){
				toSet.put(factory.getID(), new PluginDescription(factory.getID(), factory.getDescription(), factory.getParameters()));				
			}
			installedPlugins=toSet;
		}
		
		return installedPlugins;
	}

	
	@Override
	public void initPlugins() {
		for(AbstractPluginFactory factory:abstractFactoryLoader){
			log.debug("Initializing {}, under {} ",factory.getID(),TokenUtils.getCurrentScope());
			try{
				factory.init(new DataTransferContext(ContextProvider.get()));
			}catch(Throwable e){
				log.warn("Unable to initialize plugin {} ",factory.getID(),e);
			}
		}
	}
	
	
	@Override
	public ExecutionReport execute(PluginInvocation invocation,String transferredFile)throws PluginException, PluginNotFoundException {
		log.debug("Executing invocation {} ",invocation);
		
		if(!getInstalledPlugins().containsKey(invocation.getPluginId())) throw new PluginNotFoundException("Plugin with ID "+invocation.getPluginId()+" is not available.");
		AbstractPluginFactory factory=getFactory(invocation.getPluginId());
		log.debug("Loaded factory {} ",factory.getClass());
		AbstractPlugin plugin=null;
		try{
			log.debug("Checking invocation {} ",invocation);
			PluginInvocation modifiedInvocation=factory.checkInvocation(invocation,transferredFile);
			plugin=factory.createWorker(modifiedInvocation);
			ExecutionReport report=plugin.execute();
			log.debug("Plugin execution report is {} ",report);
			switch(report.getFlag()){
			case FAILED_EXECUTION:
			case WRONG_PARAMETER:
			case UNABLE_TO_EXECUTE: throw new PluginException("Wrong status after plugin execution. Report is "+report);
			case FAILED_CLEANUP : log.warn("Plugin failed to clean up. ");
			case SUCCESS : 
			}
			return report;
		}catch(PluginException e){
			log.error("Unable to execute plguin invocation {} ",invocation,e);
			throw e;
		}
	}

	
	private AbstractPluginFactory getFactory(String pluginId) throws PluginNotFoundException{
		log.debug("Getting factory by ID {} ",pluginId);
		for(AbstractPluginFactory factory:abstractFactoryLoader){
			if(factory.getID().equals(pluginId)) return factory;
		}
		throw new PluginNotFoundException("Plugin with ID "+pluginId+" not found");
	}


	@Override
	public void shutdown() {
		log.trace("Shutting down plugins..");
		for(PluginDescription desc:getInstalledPlugins().values()){
			try{
				AbstractPluginFactory factory=getFactory(desc.getId());
				log.debug("Shutting down {} ",desc.getId());
				factory.shutDown();
			}catch(Throwable e){
				log.warn("Unexpected error while shutting down {} ",desc.getId(),e);
			}
		}
		installedPlugins=null;
	}
	
	@Override
	public Object getPluginInfo(String pluginID) throws PluginNotFoundException, PluginExecutionException {
		Object toReturn=getFactory(pluginID).getInfo();
		log.trace("Serving plugin {} info {} ",pluginID,toReturn);
		return toReturn;
	}
	
}
