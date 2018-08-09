package org.gcube.contentmanagement.blobstorage.transport;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.gcube.contentmanagement.blobstorage.resource.MemoryType;
import org.gcube.contentmanagement.blobstorage.transport.backend.MongoOperationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import terrastore.client.TerrastoreClient;
/**
 * Transport manager factory
 * @author Roberto Cirillo (ISTI - CNR)
 *
 */
public class TransportManagerFactory {
	
	/**
	 * Logger for this class
	 */
//	private static final Logger logger = Logger.getLogger(OperationFactory.class);
	final Logger logger = LoggerFactory.getLogger(TransportManagerFactory.class);
//	TerrastoreClient client;
	String[] server;
	String user;
	String password;
	
	public TransportManagerFactory(String server[], String user, String password){
		this.server=server;
		this.user=user;
		this.password=password;
	}
	
	public TransportManager getTransport(String backendType, MemoryType memoryType, String[] dbNames, String writeConcern, String readConcern){
		if (logger.isDebugEnabled()) {
			logger.debug("getOperation(String) - start");
		}
		return load(backendType, memoryType, dbNames, writeConcern, readConcern);
	}
	
	private TransportManager  load(String backendType, MemoryType memoryType, String[] dbNames, String writeConcern, String readConcern){
		ServiceLoader<TransportManager> loader = ServiceLoader.load(TransportManager.class);
		Iterator<TransportManager> iterator = loader.iterator();
		List<TransportManager> impls = new ArrayList<TransportManager>();
		 while(iterator.hasNext())
			 impls.add(iterator.next());
		 int implementationCounted=impls.size();
//		 System.out.println("size: "+implementationCounted);
		 if(implementationCounted==0){
			 logger.info(" 0 implementation found. Load default implementation of TransportManager");
			 return new MongoOperationManager(server, user, password, memoryType, dbNames, writeConcern, readConcern);
		 }else if(implementationCounted==1){
			 TransportManager tm = impls.get(0);
			 logger.info("1 implementation of TransportManager found. Load it. "+tm.getName());
			 tm.initBackend(server, user, password, memoryType, dbNames, writeConcern, readConcern);
			 return tm;
		 }else{
			 logger.info("found "+implementationCounted+" implementations of TransportManager");
			 logger.info("search: "+backendType);
			 for(TransportManager tm : impls){
				 if(tm.getName().equalsIgnoreCase(backendType)){
					 logger.info("Found implementation "+backendType);
					 return tm;
				 }
			 }
			 throw new IllegalStateException("Mismatch Backend Type and RuntimeResource Type. The backend type expected is "+backendType);
		 }
		 
	}


}
