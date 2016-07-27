package org.gcube.dataanalysis.ecoengine.utils;

import java.math.BigInteger;
import java.util.List;

import org.gcube.contentmanagement.lexicalmatcher.utils.AnalysisLogger;
import org.gcube.dataanalysis.ecoengine.configuration.AlgorithmConfiguration;
import org.hibernate.SessionFactory;


/**
 * checks if two tables are equal
 * checks numbers at the second decimal position 
 */
public class TablesDiscrepanciesCalculator {

	private BigInteger numOfElements;
	private int errorCounter;
	//connection setup
	protected String LogFile = "ALog.properties";
	
	//fundamental: set a the chunk csquaresNumber and the maximum number of chunks to take
	int chunkSize = 7000;
	static double Threshold = 0.01;
	
	//change this defaults to change comparison
	public String referenceTable = "speciesrichness";
	public String analyzedTable = "crossspecies_nonreviewed";
	public String referenceCriteria = "csquarecode,speccount";
	public String destinationCriteria = "csquarecode,maxspeciescountinacell";
	public String referenceSelectedColumns = "csquarecode,speccount";
	public String destinationSelectedColumns = "csquarecode,maxspeciescountinacell";
	
	//selection query
	public static String selectElementsQuery = "select %1$s from %2$s order by %3$s";
	
	//database connections
	protected SessionFactory referencedbConnection;
	protected SessionFactory destinationdbConnection;
	
	
	//init connections 
	public TablesDiscrepanciesCalculator(AlgorithmConfiguration config) throws Exception {
		AnalysisLogger.setLogger(config.getConfigPath() + LogFile);
		referencedbConnection = DatabaseFactory.initDBConnection(config.getConfigPath() + AlgorithmConfiguration.defaultConnectionFile,config);
		AnalysisLogger.getLogger().debug("ReferenceDB initialized");
		destinationdbConnection = DatabaseFactory.initDBConnection(config.getConfigPath() + AlgorithmConfiguration.defaultConnectionFile,config);
		AnalysisLogger.getLogger().debug("OriginalDB initialized");
	}

	//counts the elements in a table
	 public BigInteger countElements(String tablename, SessionFactory session)
		    {
			BigInteger count = BigInteger.ZERO;
			String countingQuery = "select count(*) from "+tablename;
			AnalysisLogger.getLogger().debug("Getting DB elements by this query: "+countingQuery);
			List<Object> result = DatabaseFactory.executeSQLQuery(countingQuery, session);
			count = (BigInteger) result.get(0);
			return count;
		    }	
		 
	
	//takes a chunk of elements from the database, belonging to the set of 170 selected species
	public List<Object> takeChunkOfElements(String tablename,String selectedColumns,String criteria, int limit, int offset, SessionFactory session) {
		String query = String.format(selectElementsQuery,selectedColumns,tablename,criteria)+ " limit " + limit + " offset " + offset;
		AnalysisLogger.getLogger().debug("takeChunkOfElements-> executing query on DB: " + query);
		List<Object> results = DatabaseFactory.executeSQLQuery(query, session);
		return results;
	}

	//checks if a string is a number
	public double isNumber(String element){
		
		try{
			double d = Double.parseDouble(element);
			return d;
		}catch(Exception e){
			return -Double.MAX_VALUE;
		}
	}
	
	public static void main(String[] args) throws Exception {
		String configPath = "./cfg/";
		AlgorithmConfiguration config = new AlgorithmConfiguration();
		config.setParam("DistributionTable","hspec_suitable_automatic_local");
		config.setParam("ConfigPath","./cfg/");
		config.setParam("CsquarecodesTable","hcaf_d");
		config.setParam("EnvelopeTable","hspen_micro");
//		config.setEnvelopeTable("hspen");
		config.setParam("CreateTable","true");
		config.setNumberOfResources(2);
		config.setParam("DatabaseUserName","gcube");
		config.setParam("DatabasePassword","d4science2");
		config.setParam("DatabaseURL","jdbc:postgresql://localhost/testdb");
		
		TablesDiscrepanciesCalculator ec = new TablesDiscrepanciesCalculator(config);
		long t0 = System.currentTimeMillis();
		ec.runTest();
		long t1 = System.currentTimeMillis();
		float difference = (t1-t0);
		difference = difference /(float)(1000*60);
		System.out.println("Elapsed time : "+difference+" min");
	}
	
	//runs the test between the tables
	public boolean runTest() {

		long t0 = System.currentTimeMillis();
		
		// take the number of elements
		numOfElements = countElements(analyzedTable, destinationdbConnection);

		AnalysisLogger.getLogger().debug("Remote DB contains " + numOfElements + " elements.");
		int maxNumber = numOfElements.intValue();
		int numOfChunks = maxNumber / chunkSize;
		if ((maxNumber % chunkSize) > 0) {
			numOfChunks++;
		}

		int startIndex = 0;
		// reset error counter
		errorCounter = 0;
		boolean equal = true;
		for (int i = startIndex; i < numOfChunks; i++) {
			int offset = i * chunkSize;
			List<Object> referencechunk = takeChunkOfElements(referenceTable,referenceSelectedColumns,referenceCriteria, chunkSize, offset, referencedbConnection);
			List<Object> destinationchunk = takeChunkOfElements(analyzedTable,destinationSelectedColumns,destinationCriteria, chunkSize, offset, destinationdbConnection);
			int m = referencechunk.size();
			
			for (int j=0;j<m;j++){
				Object[] refrow = (Object[]) referencechunk.get(j);
				Object[] destrow = (Object[]) destinationchunk.get(j);
				int columns = destrow.length;
				for (int k=0;k<columns;k++){
					String refelem = ""+refrow[k];
					String destelem = ""+destrow[k];
					double d = isNumber(refelem);
//					System.out.print(refelem+" vs "+destelem+ " ");	
					
					if (d!=-Double.MAX_VALUE){
						if (Math.abs(d-isNumber(destelem))>Threshold){
							errorCounter++;
							equal = false;
							AnalysisLogger.getLogger().debug("ERROR - DISCREPANCY AT NUMBERS COMPARISON: "+refelem+" vs "+destelem);	
						}
					}
					else if (!refelem.equals(destelem)){
						errorCounter++;
						equal = false;
						AnalysisLogger.getLogger().debug("ERROR - DISCREPANCY AT STRING COMPARISON: "+refelem+" vs "+destelem);	
					}
					if (!equal)
						break;
				}
//				System.out.println();
				if (!equal)
					break;
			}
			
			if (!equal)
				break;
			else 
				AnalysisLogger.getLogger().debug("CHUNK NUMBER "+i+" OK!");
		}

		long t1 = System.currentTimeMillis();
		AnalysisLogger.getLogger().debug("ELAPSED TIME: " + (t1-t0) + " ms");
		
		//close connections
		referencedbConnection.close();
		destinationdbConnection.close();
		return equal;
	}

	
	
	

}
