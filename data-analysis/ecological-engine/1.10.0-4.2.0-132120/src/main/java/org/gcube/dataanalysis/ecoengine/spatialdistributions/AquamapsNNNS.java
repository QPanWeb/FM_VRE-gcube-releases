package org.gcube.dataanalysis.ecoengine.spatialdistributions;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import org.gcube.contentmanagement.lexicalmatcher.utils.AnalysisLogger;
import org.gcube.dataanalysis.ecoengine.configuration.AlgorithmConfiguration;
import org.gcube.dataanalysis.ecoengine.datatypes.PrimitiveType;
import org.gcube.dataanalysis.ecoengine.datatypes.ServiceType;
import org.gcube.dataanalysis.ecoengine.datatypes.StatisticalType;
import org.gcube.dataanalysis.ecoengine.datatypes.enumtypes.PrimitiveTypes;
import org.gcube.dataanalysis.ecoengine.datatypes.enumtypes.ServiceParameters;
import org.gcube.dataanalysis.ecoengine.models.cores.neuralnetworks.neurosolutions.NeuralNet;
import org.hibernate.SessionFactory;

public class AquamapsNNNS extends AquamapsNative{

	private NeuralNet neuralnet;
	
	@Override
	public String getName() {
		return "AQUAMAPS_NEURAL_NETWORK_NS";
	}

	@Override
	public String getDescription() {
		return "Aquamaps Algorithm calculated by neural network";
	}

	@Override
	public List<StatisticalType> getInputParameters() {
		List<StatisticalType> parameters = super.getInputParameters();
		
		PrimitiveType p1 = new PrimitiveType(String.class.getName(), null, PrimitiveTypes.STRING, "SpeciesName","Name of the Species for which the distribution has to be produced","Fis-30189");
		ServiceType p2 = new ServiceType(ServiceParameters.USERNAME, "UserName","LDAP username");
	
		parameters.add(p1);
		parameters.add(p2);
		
		return parameters;
	}

	@Override
	public void init(AlgorithmConfiguration config, SessionFactory dbHibConnection) {
		super.init(config,dbHibConnection);
		String persistencePath = config.getPersistencePath();
		String filename = persistencePath + "neuralnetwork_" + config.getParam("SpeciesName") + "_" + config.getParam("UserName");
		neuralnet = loadNN(filename);
	}

	@Override
	public float calcProb(Object mainInfo, Object area) {
		String species = getMainInfoID(mainInfo);
		String csquarecode = (String) ((Object[]) area)[0];
		Object[] wholevector = (Object[]) area;
		Object[] inputvector = new Object[wholevector.length - 6];
		for (int i = 0; i < inputvector.length; i++) {
			inputvector[i] = wholevector[i + 1];
//			AnalysisLogger.getLogger().debug(i+": "+inputvector[i]);
		}
//		AnalysisLogger.getLogger().debug("species vs csquare:" + species + " , " + csquarecode);
		float probability = 0;

//		if (csquarecode.equals("1000:102:2"))
			probability = propagate(inputvector);

		return probability;
	}

	private synchronized float propagate(Object[] inputvector) {
		double[] output = new double[1];

		try {
			
			output = neuralnet.Output(NeuralNet.preprocessObjects(inputvector));
		} catch (Exception e) {
			e.printStackTrace();
		}
		// double [] output = new double[1];
		float probability = (((float) output[0])+1f)/2f ;
		
		if (probability>0.1)
//		if (probability<0)
			AnalysisLogger.getLogger().debug(" Probability " + probability);
		
//		System.exit(0);
		return probability;
	}

	@Override
	public float getInternalStatus() {
		return 100;
	}

	public static synchronized NeuralNet loadNN(String nomeFile) {

		NeuralNet nn = null;
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(nomeFile);
			ObjectInputStream ois = new ObjectInputStream(stream);
			nn = (NeuralNet) ois.readObject();
		} catch (Exception ex) {
			ex.printStackTrace();
			AnalysisLogger.getLogger().debug("Error in reading the object from file " + nomeFile + " .");
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
			}
		}

		return nn;
	}

}
