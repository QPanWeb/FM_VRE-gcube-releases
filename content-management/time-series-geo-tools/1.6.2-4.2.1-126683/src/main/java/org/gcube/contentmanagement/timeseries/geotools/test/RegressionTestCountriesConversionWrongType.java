package org.gcube.contentmanagement.timeseries.geotools.test;

import java.util.ArrayList;
import java.util.List;

import org.gcube.contentmanagement.timeseries.geotools.engine.TSGeoToolsConfiguration;
import org.gcube.contentmanagement.timeseries.geotools.engine.TimeSeriesGISConverter;
import org.gcube.contentmanagement.timeseries.geotools.filters.AFilter;
import org.gcube.contentmanagement.timeseries.geotools.filters.SpaceFilter;
import org.gcube.contentmanagement.timeseries.geotools.gisconnectors.GISInformation;

public class RegressionTestCountriesConversionWrongType {


	public static void main(String[] args) {
		TSGeoToolsConfiguration configuration = new TSGeoToolsConfiguration();
		configuration.setConfigPath("./cfg/");
		
		configuration.setTimeSeriesDatabase("jdbc:postgresql://localhost/testdb");
		configuration.setTimeSeriesUserName("gcube");
		configuration.setTimeSeriesPassword("d4science2");

		configuration.setGeoServerDatabase("jdbc:postgresql://geoserver-test.d4science-ii.research-infrastructures.eu/timeseriesgisdb");
		configuration.setGeoServerUserName("postgres");
		configuration.setGeoServerPassword("d4science2");
		
		configuration.setAquamapsDatabase("jdbc:postgresql://geoserver-test.d4science-ii.research-infrastructures.eu/essentialaquamaps");
		configuration.setAquamapsUserName("postgres");
		configuration.setAquamapsPassword("d4science2");
		
		GISInformation gisInfo = new GISInformation();
		gisInfo.setGeoNetworkUrl("http://geoserver-dev.d4science-ii.research-infrastructures.eu/geonetwork");
		gisInfo.setGeoNetworkUserName("admin");
		gisInfo.setGeoNetworkPwd("admin");
		
		gisInfo.setGisDataStore("timeseriesgisdb");
		gisInfo.setGisPwd("gcube@geo2010");
		gisInfo.setGisWorkspace("aquamaps");
		gisInfo.setGisUrl("http://geoserver-dev.d4science-ii.research-infrastructures.eu/geoserver");
		gisInfo.setGisUserName("admin");
		
		String timeSeriesName = "ts_test_types";
		String informationColumn = "field0";
//		String quantitiesColumn = "field4s";
		String quantitiesColumn = "field4r";
		
		AFilter filter = new SpaceFilter(timeSeriesName,informationColumn,quantitiesColumn);
		
		ArrayList<AFilter> filters = new ArrayList<AFilter>();
		filters.add(filter);
		
		try{
			TimeSeriesGISConverter converter = new TimeSeriesGISConverter(configuration);
			long t0 = System.currentTimeMillis();
			List<String> producedlayers = converter.TimeSeriesToGIS(filters,gisInfo,false);
			System.out.println("Produced layers: "+producedlayers);
			long t1 = System.currentTimeMillis();
			System.out.println("ELAPSED TIME : "+(t1-t0));
		}catch(Exception e){e.printStackTrace();}
	}

}
