/**
 * 
 */
package org.gcube.portlets.user.td.gwtservice.server;

import java.util.ArrayList;

import org.gcube.datapublishing.sdmx.api.model.SDMXRegistryDescriptor;
import org.gcube.datapublishing.sdmx.api.model.SDMXRegistryInterfaceType;
import org.gcube.datapublishing.sdmx.api.registry.SDMXRegistryClient;
import org.gcube.datapublishing.sdmx.api.registry.SDMXRegistryClient.Detail;
import org.gcube.datapublishing.sdmx.api.registry.SDMXRegistryClient.References;
import org.gcube.datapublishing.sdmx.impl.model.GCubeSDMXRegistryDescriptor;
import org.gcube.datapublishing.sdmx.impl.model.SDMXRegistryDescriptorImpl;
import org.gcube.datapublishing.sdmx.impl.registry.FusionRegistryClient;
import org.gcube.portlets.user.td.gwtservice.shared.tr.type.Agencies;
import org.gcube.portlets.user.td.gwtservice.shared.tr.type.Codelist;
import org.gcube.portlets.user.td.gwtservice.shared.tr.type.Dataset;
import org.sdmxsource.sdmx.api.model.beans.SdmxBeans;
import org.sdmxsource.sdmx.api.model.beans.base.AgencyBean;
import org.sdmxsource.sdmx.api.model.beans.codelist.CodelistBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DataflowBean;

/**
 * 
 * @author "Giancarlo Panichi" 
 * <a href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a> 
 *
 */
public class SDMXClient {

	public static enum TYPE { INTERNAL, ANOTHER }
	
	protected SDMXRegistryClient client;
	protected TYPE type;
	protected String url;
	
    public SDMXClient(){	
		type=TYPE.INTERNAL;
		url=null;
    	SDMXRegistryDescriptor descriptor = new GCubeSDMXRegistryDescriptor();
		client = new FusionRegistryClient(descriptor);
		
	}
	
    public SDMXClient(String url){
    	type=TYPE.ANOTHER;
    	this.url=url;
		SDMXRegistryDescriptorImpl descriptor = new SDMXRegistryDescriptorImpl();
		descriptor.setUrl(SDMXRegistryInterfaceType.RESTV2_1, url);
		client = new FusionRegistryClient(descriptor);
	}
	
	
	public ArrayList<Codelist> getAllCodelists() throws Exception
	{
		SdmxBeans beans = client.getCodelist("all", "all", "all", Detail.allstubs, References.none);
		ArrayList<Codelist> codelists = new ArrayList<Codelist>();
		for (CodelistBean codelist:beans.getCodelists()) codelists.add(new Codelist(codelist.getId(), codelist.getName(), codelist.getAgencyId(), codelist.getVersion(), codelist.getDescription()));
		return codelists;
	}
	
	public ArrayList<Dataset> getAllDatasets() throws Exception
	{
		SdmxBeans beans = client.getProvisionAgreement("all", "all", "latest", Detail.full, References.children);
		ArrayList<Dataset> datasets = new ArrayList<Dataset>();
		for (DataflowBean dataflowBean:beans.getDataflows()) {
			datasets.add(new Dataset(dataflowBean.getId(), dataflowBean.getName(), dataflowBean.getAgencyId(),dataflowBean.getVersion(),dataflowBean.getDescription()));
		}
		return datasets;
	}
	
	public ArrayList<Agencies> getAllAgencies() throws Exception
	{
		SdmxBeans beans = client.getAgencyScheme("SDMX", "AGENCIES", "1.0", Detail.full, References.none);
		ArrayList<Agencies> agenciesList = new ArrayList<Agencies>();
		for (AgencyBean agency:beans.getAgencies()) agenciesList.add(new Agencies(agency.getId(), agency.getName(), agency.getDescription()));
		return agenciesList;
	}
	

}
