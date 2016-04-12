package org.gcube.dataanalysis.ecoengine.test;

import org.gcube.dataanalysis.ecoengine.user.GeneratorT;
import org.gcube.dataanalysis.ecoengine.user.ModelerT;

public class ProduceTestMap {

	public static void main2(String[] args) throws Exception {
		StringBuffer create = new StringBuffer("CREATE TABLE mahoutclustering("+System.getProperty("line.separator"));
		int max =60;
		for (int i=0;i<max;i++){
			create.append("field"+i+" real");
			if (i<max-1)
				create.append(","+System.getProperty("line.separator"));
		}
		create.append(")");
		System.out.println(create);
	}
	
	public static void main(String[] args) throws Exception {
		StringBuffer create = new StringBuffer("");
		int max =60;
		for (int i=0;i<max;i++){
			create.append("field"+i);
			if (i<max-1)
				create.append("#");
		}
		
		System.out.println(create);
	}
	
	
	public static void main1(String[] args) throws Exception {
		String configPath = "./cfg/";
		String presenceTable = "presence_data_baskingshark";
		String absenceTable = "absence_data_baskingshark_random";
		String csquareTable = "hcaf_d";
		String preprocessedTable = "maxminlat_hspen";
		String envelopeTable = "hspen_baskingshark";
		String commonkeycolumn = "csquarecode";
		String probabilitycolumn = "probability";
		String positiveClassificationThreshold = "0.8";
		String negativeClassificationThreshold = "0.3";
		String comparisonThreshold = "0.1";
		int numberOfResources = 4;
		String speciesCode = "Fis-22747";
		String userName = "gianpaolo.coro";
		String modelName = "AQUAMAPSNN";
		String generatorName = "AQUAMAPS_SUITABLE_NEURALNETWORK";
		String qualityOperationName = "QUALITY_ANALYSIS";
		String discrepancyOperationName = "DISCREPANCY_ANALYSIS";

		String manualTableOfComparison = "hspec_native_baskingshark";
		String manualTableCsquareColumn = "csquare";
		String secondTableOfComparison = "hspec_native_baskingshark_aquamaps";

		String neuralNetworkLayers = "300";

		// String finalDistributionTable = "hspec_nn_baskingshark_close_best";
		// String finalDistributionKeyColumn= "csquarecode";

		// String finalDistributionTable = "hspec_nn_baskingshark_random_best2";
		// String finalDistributionKeyColumn= "csquarecode";

		// String finalDistributionTable = "hspec_native_baskingshark";
		// String finalDistributionKeyColumn= "csquare";

		String finalDistributionTable = "hspec_native_baskingshark_aquamaps";
		String finalDistributionKeyColumn = "csquarecode";

		// Train
		 ModelerT.train(ModelerT.getTrainingConfig(modelName, absenceTable, presenceTable, speciesCode, userName, neuralNetworkLayers, configPath));

		// Generate
		GeneratorT.generate(GeneratorT.getGenerationConfig(numberOfResources, generatorName, envelopeTable, preprocessedTable, speciesCode, userName, csquareTable, finalDistributionTable, configPath));

	}

}
