package org.gcube.spatial.data.geonetwork.test;

import org.gcube.common.scope.api.ScopeProvider;
import org.gcube.spatial.data.geonetwork.GeoNetwork;
import org.gcube.spatial.data.geonetwork.GeoNetworkReader;
import org.gcube.spatial.data.geonetwork.LoginLevel;
import org.gcube.spatial.data.geonetwork.iso.GcubeISOMetadata;

public class GcubeIsoTest {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		ScopeProvider.instance.set("/gcube/devsec");
		GeoNetworkReader reader=GeoNetwork.get();
		reader.login(LoginLevel.SCOPE);
		System.out.println(reader.getById("0815e357-ebd7-4c02-8dc8-f945eceb870c"));
		
	}

}
