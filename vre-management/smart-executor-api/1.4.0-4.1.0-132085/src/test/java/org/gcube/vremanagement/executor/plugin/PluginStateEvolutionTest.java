/**
 * 
 */
package org.gcube.vremanagement.executor.plugin;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.gcube.vremanagement.executor.exception.InvalidPluginStateEvolutionException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Luca Frosini (ISTI - CNR) http://www.lucafrosini.com/
 *
 */
public class PluginStateEvolutionTest {

	private static final Logger logger = LoggerFactory.getLogger(PluginStateEvolutionTest.class);
	
	@Test
	public void testToString() throws InvalidPluginStateEvolutionException{
		UUID uuid = UUID.randomUUID();
		int iteration = 2;
		long timestamp = Calendar.getInstance().getTimeInMillis();
		PluginDeclaration pluginDeclaration = new PluginDeclaration(){

			@Override
			public void init() throws Exception {}

			@Override
			public String getName() {
				return PluginDeclaration.class.getSimpleName();
			}

			@Override
			public String getDescription() {
				return PluginDeclaration.class.getSimpleName() + " Description";
			}

			@Override
			public String getVersion() {
				return "1.0.0";
			}

			@Override
			public Map<String, String> getSupportedCapabilities() {
				return new HashMap<String, String>();
			}

			@Override
			public Class<? extends Plugin<? extends PluginDeclaration>> getPluginImplementation() {
				return null;
			}
			
			@Override
			public String toString(){
				return String.format("%s :{ %s - %s - %s }", 
						PluginDeclaration.class.getSimpleName(), 
						getName(), getVersion(), getDescription());
			}
			
		};
		PluginState pluginState = PluginState.DISCARDED;
		PluginStateEvolution pluginStateEvolution = new PluginStateEvolution(uuid, iteration, timestamp, pluginDeclaration, pluginState, 0);
		logger.debug(pluginStateEvolution.toString());
	}
	
}
