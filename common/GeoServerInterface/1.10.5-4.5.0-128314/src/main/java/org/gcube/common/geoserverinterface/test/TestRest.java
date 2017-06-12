package org.gcube.common.geoserverinterface.test;


public class TestRest {



//	private static final String TRANSFER_STATE_DONE = "DONE";
//	private static final Object GEOTIFF_TYPE = "GeoTIFF";
//	static String geonetworkUrl = "http://geoserver-dev.d4science-ii.research-infrastructures.eu/geonetwork";
//	static String geonetworkUsername = "admin";
//	static String geonetworkPassword = "admin";
//
//	static String geoserverUrl = "http://geoserver-dev.d4science-ii.research-infrastructures.eu/geoserver";
//	static String geoserverUsername = "admin";
//	static String geoserverPassword = "gcube@geo2010";
//
//	public static void main(String[] args) {
//
//		addGeoTiffTest();
//		
////		geoServerManagerTest();
//
////		geoCallerRestTest();
//
////		dataTransferTest();
//
//	}
//
//	/**
//	 * 
//	 */
//	private static void addGeoTiffTest2() {
//		try {
//			GeoCaller geoCaller = new GeoCaller(geonetworkUrl, geonetworkUsername, geonetworkPassword, geoserverUrl, geoserverUsername, geoserverPassword, GeoserverMethodResearch.MOSTUNLOAD);
//			boolean b = geoCaller.addPreExistentGeoTiff("p_edulis_map.tiff", "newEdulis1", "newEdulis1", "aquamaps", GeonetworkCategory.DATASETS, "descr", "");
//			System.out.println("b="+b);
////			geoCaller.addCoverageStore("myGeoTiff.tif", "myGeoTiffLayerName", "myGeoTiffLayer Title", "aquamaps", "descr", "abstr", "/gcube/devsec/");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	/**
//	 * 
//	 */
//	private static void addGeoTiffTest() {
//		try {
//			GeoCaller geoCaller = new GeoCaller(geonetworkUrl, geonetworkUsername, geonetworkPassword, geoserverUrl, geoserverUsername, geoserverPassword, GeoserverMethodResearch.MOSTUNLOAD);
//			String workspace = "aquamapstest";
//			String geoTiffUrl = "https://www.dropbox.com/s/ec68ssrkbm759ba/albers27.tif";
////			String geoTiffUrl = "https://dl.dropbox.com/u/24368142/cea.tif";
////			String geoTiffUrl = "https://dl.dropbox.com/u/12809149/p_edulis_map.tiff";
//			String description = "albers test geotiff 0";
//			String scope = "/gcube/devsec/";
//			String layerName = "albers";
//			String layerTitle = "albers title";
//			String abstr = "albers abstr";
//			geoCaller.addGeoTiff(geoTiffUrl, layerName, layerTitle, workspace, GeonetworkCategory.DATASETS, description, abstr, scope);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	static String SRS = "EPSG:4326";
//	
//	@SuppressWarnings("serial")
//	private static void createLayerTest() {
//		//select the geo network choice method for writing layers
//		GeoserverMethodResearch geoserverMethodReserch = GeoserverMethodResearch.MOSTUNLOAD;
//		//		GeoserverMethodResearch geoserverMethodReserch = GeoserverMethodResearch.RANDOM;
//		
//		GeoCaller geoCaller = null;
//		
//		try {
//			
//			GeoserverCaller geoserverCaller = new GeoserverCaller(geoserverUrl, geoserverUsername, geoserverPassword);
//			LayerRest layer = geoserverCaller.getLayer("eezall");
//			System.out.println("name="+layer.getName());
//			System.out.println("ws="+layer.getWorkspace());
//			System.out.println("ds="+layer.getDatastore());
//			
//			HttpMethodCall hmc = initHmc();
//			
//			final String workspaceName = "aquamaps";
//			final String storeName = "testGiamp";
//			final String fileName = "NLCD.2km.21600x10800.tif";
//
////			StringBuilder body = new StringBuilder();
////			body.append("");
////			body.append("{");
////			body.append(	"\"coverageStore\": {");
////			body.append(		"\"name\": \"" + storeName + "\",");
////			body.append(		"\"type\": \"GeoTIFF\",");
////			body.append(		"\"enabled\": true,");
////			body.append(		"\"workspace\": {");
////			body.append(			"\"name\": \"" + workspaceName + "\",");
////			body.append(			"\"href\": \"" + geoserverUrl + "/rest/workspaces/" + workspaceName + ".json\"");
////			body.append(		"},");
//////			body.append(		"\"__default\": false,");
////			body.append(		"\"url\": \"file:data/" + workspaceName + "/NLCD.2km.21600x10800.tif\",");
//////			body.append(		"\"coverages\": \"" + geoserverUrl + "/rest/workspaces/" + ws + "/coveragestores/" + ds + "/coverages.json\"");
////			body.append(	"}");
////			body.append("}");
////			System.out.println("BODY: \n"+body);
//
//
//			
//			JSONObject jsObj = new JSONObject();
//			jsObj.put("coverageStore", new HashMap<String, Object>(){{
//				put("name", storeName);
//				put("type", GEOTIFF_TYPE);
//				put("enabled", true);
//				put("workspace", new HashMap<String, Object>(){{
//					put("name", workspaceName);
//					put("href", geoserverUrl + "/rest/workspaces/" + workspaceName + ".json");
//				}});
//				put("url", "file:data/" + workspaceName + "/" + fileName);
//			}});
//
//			System.out.println("JSOBJ:\n"+jsObj.toString());
//
//			String ris = hmc.CallPost("rest/workspaces/"+workspaceName+"/coveragestores", jsObj.toString(), "application/json");
//			System.out.println("RIS: \n"+ris);
//			
//		} catch (Exception e) {
//			System.out.println("Error, "+e.getMessage());
////			e.printStackTrace();
//		}
//	}
//
//
//	private static void geoServerManagerTest() {
//		try {
//			GeoServerRESTPublisher publisher = new GeoServerRESTPublisher(geoserverUrl, geoserverUsername, geoserverPassword);
//
////			GSCoverageEncoder ce = new GSCoverageEncoder();
////			
////			ce.setName("coverageName1");
////			ce.setSRS(SRS);
//////			double minx;
//////			double miny;
//////			double maxy;
//////			double maxx;
//////			ce.setLatLonBoundingBox(minx, miny, maxx, maxy, SRS);
////			
////			boolean bris = publisher.createCoverage(ce, "aquamaps", "coverageName1");
//			
//			File geotiff = new File("/usr/share/apache-tomcat-6.0.33/webapps/geoserver/data/data/aquamaps/p_edulis_map1.tiff");
//			boolean b = publisher.publishExternalGeoTIFF("aquamaps", "p_edulis3_cs_P", geotiff, "p_edulis3_P", "EPSG:4326", ProjectionPolicy.REPROJECT_TO_DECLARED, "raster");
////			GSLayerEncoder le = new GSLayerEncoder();
////			le.setEnabled(true);
////			le.setDefaultStyle("raster");
////			le.setQueryable(true);
////			GSCoverageEncoder ce = new GSCoverageEncoder();
////			ce.setEnabled(true);
////			ce.setProjectionPolicy(ProjectionPolicy.REPROJECT_TO_DECLARED);
////			ce.setNativeCRS("EPSG:4326");
////			ce.setSRS("EPSG:4326");
////			ce.setName("p_edulis5_P");
////			GSDimensionInfoEncoder dimensionInfo = new GSDimensionInfoEncoder();
////			dimensionInfo.setPresentation(Presentation.CONTINUOUS_INTERVAL);
////			dimensionInfo.setEnabled(true);
////			ce.setMetadata("GRAY_INDEX", dimensionInfo);
////			RESTCoverageStore b = publisher.publishExternalGeoTIFF("aquamaps", "p_edulis5_cs_P", geotiff, ce, le);
//			System.out.println("\n\nBI = "+b);
//			
//			//////////////////////////////////
////			HttpMethodCall hmc = initHmc();
////			Map<String, Object> m = new HashMap<String, Object>();
////			m.put("name", "p_edulis3_P");
////			m.put("enabled", true);
////
////			{
////				JSONObject defStyle = new JSONObject();
////				defStyle.put("name", "raster");
////				m.put("defaultStyle", defStyle);
////			}
////			
////			JSONArray a = new JSONArray();
////			JSONObject n = new JSONObject();
////			n.put("name","raster");
////			a.put(n);
////			JSONObject l = new JSONObject();
////			l.put("style",a);
////			m.put("styles", l);
////			
////			JSONObject j = new JSONObject();
////			j.put("layer", m);
////			hmc.CallPut("rest/layers/p_edulis3_P", j.toString(), "application/json");
//			//////////////////////////////////
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	private static void geoCallerRestTest() {
//		GeoserverMethodResearch geoserverMethodReserch = GeoserverMethodResearch.MOSTUNLOAD;
//		GeoCaller geoCaller = null;
//		try {
//			
//			GeoserverCaller geoserverCaller = new GeoserverCaller(geoserverUrl, geoserverUsername, geoserverPassword);
//			LayerRest layer = geoserverCaller.getLayer("eezall");
//			System.out.println("name="+layer.getName());
//			System.out.println("ws="+layer.getWorkspace());
//			System.out.println("ds="+layer.getDatastore());
//			
//			HttpMethodCall hmc = initHmc();
//			
//			final String workspaceName = "aquamaps";
//			final String storeName = "testGiamp";
//			final String fileName = "NLCD.2km.21600x10800.tif";
//
//			String ris = hmc.Call("rest/workspaces/"+workspaceName+"/coveragestores.json");
//			System.out.println("RIS: \n"+ris);
//			
//		} catch (Exception e) {
//			System.out.println("Error, "+e.getMessage());
////			e.printStackTrace();
//		}
//	}
//	
//	@SuppressWarnings("serial")
//	private static void geoCallerRestCoverageStoreTest() {
//		//select the geo network choice method for writing layers
//		GeoserverMethodResearch geoserverMethodReserch = GeoserverMethodResearch.MOSTUNLOAD;
//		//		GeoserverMethodResearch geoserverMethodReserch = GeoserverMethodResearch.RANDOM;
//		
//		GeoCaller geoCaller = null;
//		
//		try {
//			
//			GeoserverCaller geoserverCaller = new GeoserverCaller(geoserverUrl, geoserverUsername, geoserverPassword);
//			LayerRest layer = geoserverCaller.getLayer("eezall");
//			System.out.println("name="+layer.getName());
//			System.out.println("ws="+layer.getWorkspace());
//			System.out.println("ds="+layer.getDatastore());
//			
//			HttpMethodCall hmc = initHmc();
//			
//			final String workspaceName = "aquamaps";
//			final String storeName = "testGiamp";
//			final String fileName = "NLCD.2km.21600x10800.tif";
//
////			StringBuilder body = new StringBuilder();
////			body.append("");
////			body.append("{");
////			body.append(	"\"coverageStore\": {");
////			body.append(		"\"name\": \"" + storeName + "\",");
////			body.append(		"\"type\": \"GeoTIFF\",");
////			body.append(		"\"enabled\": true,");
////			body.append(		"\"workspace\": {");
////			body.append(			"\"name\": \"" + workspaceName + "\",");
////			body.append(			"\"href\": \"" + geoserverUrl + "/rest/workspaces/" + workspaceName + ".json\"");
////			body.append(		"},");
//////			body.append(		"\"__default\": false,");
////			body.append(		"\"url\": \"file:data/" + workspaceName + "/NLCD.2km.21600x10800.tif\",");
//////			body.append(		"\"coverages\": \"" + geoserverUrl + "/rest/workspaces/" + ws + "/coveragestores/" + ds + "/coverages.json\"");
////			body.append(	"}");
////			body.append("}");
////			System.out.println("BODY: \n"+body);
//
//
//			
//			JSONObject jsObj = new JSONObject();
//			jsObj.put("coverageStore", new HashMap<String, Object>(){{
//				put("name", storeName);
//				put("type", GEOTIFF_TYPE);
//				put("enabled", true);
//				put("workspace", new HashMap<String, Object>(){{
//					put("name", workspaceName);
//					put("href", geoserverUrl + "/rest/workspaces/" + workspaceName + ".json");
//				}});
//				put("url", "file:data/" + workspaceName + "/" + fileName);
//			}});
//
//			System.out.println("JSOBJ:\n"+jsObj.toString());
//
//			String ris = hmc.CallPost("rest/workspaces/"+workspaceName+"/coveragestores", jsObj.toString(), "application/json");
//			System.out.println("RIS: \n"+ris);
//			
//		} catch (Exception e) {
//			System.out.println("Error, "+e.getMessage());
////			e.printStackTrace();
//		}
//	}
//
//	/**
//	 * 
//	 */
//	private static void dataTransferTest() {
//		try {
//			System.out.println("STARTED...");
//			
//			ScopeProvider.instance.set("/gcube/devsec/");
//
//			AgentLibrary library = transferAgent().at("geoserver-dev.d4science-ii.research-infrastructures.eu", 9000).build();
//
//			ArrayList<URI> inputs = new ArrayList<URI>();
//			inputs.add(new URI("http://img821.imageshack.us/img821/6658/gisviewerdiagram.png"));
//			inputs.add(new URI("http://img11.imageshack.us/img11/9008/geoexplorerdiagram.png"));
//			inputs.add(new URI("https://www.dropbox.com/s/ec68ssrkbm759ba/albers27.tif"));
//
//			String outPath = "./";
//
//			TransferOptions options = new TransferOptions();
//			options.setOverwriteFile(true);
//			options.setType(storageType.LocalGHN);
//			options.setUnzipFile(false);
//
//			String transferId = library.startTransfer(inputs, outPath, options);
//			//ArrayList<FileTransferOutcome> outcomes = library.startTransferSync(input, outPath, options);
//			System.out.println("Transfer started "+transferId);
//			
//			String transferState="";
//			while (!transferState.contentEquals(TRANSFER_STATE_DONE)) {
//				try {
//					
//					transferState = library.monitorTransfer(transferId);
//					System.out.print(".");
//					
//				} catch (MonitorTransferException e) {
//					e.printStackTrace();
//				}
//				Thread.sleep(500);
//			}
//			System.out.println("done!");
//			ArrayList<FileTransferOutcome> outcomes = library.getTransferOutcomes(transferId, FileTransferOutcome.class);
//
//			for (FileTransferOutcome outcome : outcomes)
//				System.out.println("file: "+outcome.getDest()+"; "+ (outcome.isSuccess() ? "SUCCESS" : "FAILURE"));
//			
//			
//		} catch (TransferException e) {
//			System.out.println("TRANSFER EXCEPTION");
//			e.printStackTrace();
//		} catch (ConfigurationException e) {
//			System.out.println("CONFIGURATION EXCEPTION");
//			e.printStackTrace();
//		} catch (URISyntaxException e) {
//			System.out.println("URI SYNTAX EXCEPTION");
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		} catch (GetTransferOutcomesException e) {
//			e.printStackTrace();
//		}
//	}
//
//	/**
//	 * @return
//	 */
//	private static HttpMethodCall initHmc() {
//		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
//		HttpMethodCall hmc = new HttpMethodCall(connectionManager, geoserverUrl, geoserverUsername, geoserverPassword);
//		return hmc;
//	}


}
