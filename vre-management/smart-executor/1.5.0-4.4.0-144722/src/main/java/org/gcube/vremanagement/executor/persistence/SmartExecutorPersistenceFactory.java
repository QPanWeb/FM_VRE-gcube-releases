/**
 * 
 */
package org.gcube.vremanagement.executor.persistence;

import java.util.HashMap;
import java.util.Map;

import org.gcube.vremanagement.executor.SmartExecutorInitializator;
import org.gcube.vremanagement.executor.persistence.orientdb.OrientDBPersistenceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Luca Frosini (ISTI - CNR)
 *
 */
public abstract class SmartExecutorPersistenceFactory {

	private static final Logger logger = LoggerFactory.getLogger(SmartExecutorPersistenceFactory.class);
	
	private static Map<String, SmartExecutorPersistenceConnector> persistenceConnectors;
	
	static {
		persistenceConnectors = new HashMap<String, SmartExecutorPersistenceConnector>();
	}
	
	public static SmartExecutorPersistenceConnector getPersistenceConnector(String scope){
		if(scope==null){
			String error = "No Scope available.";
			logger.error(error);
			throw new RuntimeException(error); 
		}

		logger.trace("Retrieving {} for scope {}", 
				SmartExecutorPersistenceConnector.class.getSimpleName(), scope);
		
		return persistenceConnectors.get(scope);
	}
	
	/**
	 * @return the persistenceConnector
	 */
	public static synchronized SmartExecutorPersistenceConnector getPersistenceConnector() throws Exception {
		String scope = SmartExecutorInitializator.getCurrentScope();
		SmartExecutorPersistenceConnector persistence = 
				getPersistenceConnector(scope);
		
		if(persistence==null){
			logger.trace("Retrieving {} for scope {} not found on internal {}. Intializing it.", 
					SmartExecutorPersistenceConnector.class.getSimpleName(), 
					scope, Map.class.getSimpleName());
			
			String className = OrientDBPersistenceConnector.class.getSimpleName();
			SmartExecutorPersistenceConfiguration configuration = 
					new SmartExecutorPersistenceConfiguration(className);
			
			persistence = new OrientDBPersistenceConnector(scope, configuration);
			persistenceConnectors.put(SmartExecutorInitializator.getCurrentScope(), 
					persistence);
		}
		
		return persistence;
	}
	
	public static synchronized void closePersistenceConnector() throws Exception {
		String scope = SmartExecutorInitializator.getCurrentScope();
		SmartExecutorPersistenceConnector persistence = 
				getPersistenceConnector(scope);
		if(persistence!=null){
			persistence.close();
			persistenceConnectors.remove(scope);
		}
	}

}
