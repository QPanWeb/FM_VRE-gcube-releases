/**
 * 
 */
package org.gcube.documentstore.persistence;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Luca Frosini (ISTI - CNR) http://www.lucafrosini.com/
 */
public abstract class PersistenceBackendConfiguration {
	
	private static final Logger logger = LoggerFactory.getLogger(PersistenceBackendConfiguration.class);
	
	protected Map<String,String> properties;
	
	/**
	 * Used only for testing purpose
	 * @return
	 */
	protected static PersistenceBackendConfiguration getUnconfiguredInstance(){
		ServiceLoader<? extends PersistenceBackendConfiguration> serviceLoader = ServiceLoader.load(PersistenceBackendConfiguration.class);
		for (PersistenceBackendConfiguration foundConfiguration : serviceLoader) {
			Class<? extends PersistenceBackendConfiguration> configClass = foundConfiguration.getClass();
			String foundConfigurationClassName = configClass.getSimpleName();
			logger.debug("{} will be used.", foundConfigurationClassName);
			
			return foundConfiguration;
		}
		return null;
	}
	
	public static PersistenceBackendConfiguration getInstance(Class<? extends PersistenceBackend> clz){
		ServiceLoader<? extends PersistenceBackendConfiguration> serviceLoader = ServiceLoader.load(PersistenceBackendConfiguration.class);
		for (PersistenceBackendConfiguration foundConfiguration : serviceLoader) {
			try {
				Class<? extends PersistenceBackendConfiguration> configClass = foundConfiguration.getClass();
				String foundConfigurationClassName = configClass.getSimpleName();
				logger.debug("Testing {}", foundConfigurationClassName);
				
				@SuppressWarnings("rawtypes")
				Class[] configArgTypes = { Class.class };
				Constructor<? extends PersistenceBackendConfiguration> configurationConstructor = configClass.getDeclaredConstructor(configArgTypes);
				Object[] configArguments = {clz};
				PersistenceBackendConfiguration configuration = configurationConstructor.newInstance(configArguments);
				
				logger.debug("{} will be used.", foundConfigurationClassName);
				
				return configuration;
			} catch (Exception e) {
				logger.error(String.format("%s not initialized correctly. It will not be used. Trying the next one if any.", foundConfiguration.getClass().getSimpleName()), e);
			}
		}
		return null;
	}
	
	protected PersistenceBackendConfiguration(){
		properties = new HashMap<String, String>();
	}
	
	public PersistenceBackendConfiguration(Class<? extends PersistenceBackend> clz){
		this();
	}
	
	public void addProperty(String key, String value) {
		properties.put(key, value);
	}
	
	public String getProperty(String key) throws Exception {
		return properties.get(key);
	}
	
	public String toString() {
		return properties.toString();
	}
}
