package org.gcube.application.aquamaps.ecomodelling.generators.test;

import org.gcube.application.aquamaps.ecomodelling.generators.configuration.EngineConfiguration;
import org.gcube.application.aquamaps.ecomodelling.generators.connectors.EnvelopeModel;
import org.gcube.application.aquamaps.ecomodelling.generators.processing.EnvelopeGenerator;

public class TestHspen {
	/**
	 * example of parallel processing on a single machine
	 * the procedure will generate a new table for a distribution on suitable species
	 *  
	 */
	
	public static void main(String[] args) throws Exception{

		EngineConfiguration e = new EngineConfiguration();
		//path to the cfg directory containing default parameters
		e.setConfigPath("./cfg/");
		e.setDatabaseUserName("utente");
		e.setDatabasePassword("d4science");
		e.setDatabaseURL("jdbc:postgresql://dbtest.research-infrastructures.eu/aquamapsorgupdated");
		e.setHcafTable("hcaf_d");
		e.setHspenTable("hspen_new");
		e.setOriginHspenTable("hspen_validation");
		e.setOccurrenceCellsTable("occurrencecells");
		e.setEnvelopeGenerator(EnvelopeModel.AQUAMAPS);
		e.setNumberOfThreads(16);
		e.setCreateTable(true);
		
		EnvelopeGenerator eg = new EnvelopeGenerator(e);
		eg.reGenerateEnvelopes();
	}
}
