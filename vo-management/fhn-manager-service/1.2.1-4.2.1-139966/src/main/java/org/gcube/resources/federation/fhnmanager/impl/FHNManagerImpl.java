package org.gcube.resources.federation.fhnmanager.impl;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.io.monitor.FileEntry;
import org.bouncycastle.math.ec.ECConstants;
import org.gcube.common.authorization.library.provider.SecurityTokenProvider;
import org.gcube.common.scope.api.ScopeProvider;
import org.gcube.resources.federation.fhnmanager.api.FHNManager;
import org.gcube.resources.federation.fhnmanager.api.exception.ConnectorException;
import org.gcube.resources.federation.fhnmanager.api.exception.FHNManagerException;
import org.gcube.resources.federation.fhnmanager.api.type.OccopusInfrastructure;
import org.gcube.resources.federation.fhnmanager.api.type.OccopusInfrastructureTemplate;
import org.gcube.resources.federation.fhnmanager.api.type.OccopusInstanceSet;
import org.gcube.resources.federation.fhnmanager.api.type.OccopusScalingParams;
import org.gcube.resources.federation.fhnmanager.api.type.Node;
import org.gcube.resources.federation.fhnmanager.api.type.NodeDefinition;
import org.gcube.resources.federation.fhnmanager.api.type.NodeTemplate;
import org.gcube.resources.federation.fhnmanager.api.type.ResourceReference;
import org.gcube.resources.federation.fhnmanager.api.type.ResourceTemplate;
import org.gcube.resources.federation.fhnmanager.api.type.ServiceProfile;
import org.gcube.resources.federation.fhnmanager.api.type.VMProvider;
import org.gcube.resources.federation.fhnmanager.is.ISProxyImpl;
import org.gcube.resources.federation.fhnmanager.is.ISProxyInterface;
import org.gcube.resources.federation.fhnmanager.is.ISProxyLocalYaml;
import org.gcube.resources.federation.fhnmanager.occopus.OccopusClient;
import org.gcube.resources.federation.fhnmanager.occopus.OccopusNodeDefinitionImporter;
import org.gcube.resources.federation.fhnmanager.occopus.model.CreateInfraResponse;
import org.gcube.resources.federation.fhnmanager.occopus.model.GetInfraResponse;
import org.gcube.resources.federation.fhnmanager.utils.NodeHelper;
import org.gcube.resources.federation.fhnmanager.utils.Props;
import org.gcube.vomanagement.occi.FHNConnector;
import org.gcube.vomanagement.occi.datamodel.cloud.OSTemplate;
import org.gcube.vomanagement.occi.datamodel.cloud.VM;
import org.gcube.vomanagement.occi.utils.ScriptUtil;
import org.glassfish.jersey.process.internal.RequestScope.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cesnet.cloud.occi.api.exception.CommunicationException;

public class FHNManagerImpl implements FHNManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(FHNManagerImpl.class);
	static Props a = new Props();
	private static final String URL = a.getOccopusURL();

	private ConnectorFactory connectorFactory;

	private OccopusClient occpusClient;

	private ISProxyInterface isProxy;

	public FHNManagerImpl() {
		this.connectorFactory = new ConnectorFactory();
		// this.isProxy = new ISProxyLocalYaml(); //to change with isproxyimpl
		this.isProxy = new ISProxyImpl();
		this.occpusClient = new OccopusClient(URL);

	}

	// public void getInfrastructureById(String infraid) {
	// try {
	// javax.ws.rs.client.Client client = ClientBuilder.newClient();
	// WebTarget target = client.target(URL+infraid);
	// Response response = target.request().accept("application/json").get();
	// String entity = response.readEntity(String.class);
	// if (response.getStatus() != 200) {
	// throw new RuntimeException("Failed : HTTP error code : " +
	// response.getStatus());
	// }
	// System.out.println(entity);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	
	
	
	public String resourceTemplateName(String vmpId, String rtId){
		String name = "";
		for (ResourceTemplate a: this.findResourceTemplate(vmpId)){
			if (a.getId().equals(rtId)){
				name += "occi_"+a.getName();
			}
		}
		return name;		
	}
	
	
		
	public String buildOccopusDescription(OccopusInfrastructureTemplate it) {
		//Set<NodeDefinition> setnd = new HashSet<NodeDefinition>();
		String builded = "infra_name: occi_infra_test"+"\n"
						+ "user_id: somebody@somewhere.com" + "\n"
						+ "nodes:";
		String builded2 = ""; 
		String finalbuild = "";
		for (NodeDefinition b : it.getSetnd()){	
			builded2 +="\n"+" -"+"\n"
					+"  name: " + this.resourceTemplateName(b.getVmproviderId(), b.getResourceTemplateId()).trim().replace(":", "") + "_" + b.getNodetemplateId() +"_" +this.isProxy.findVMProviderbyId(b.getVmproviderId()).getCredentials().getVo() + "\n"  
					+"  type: " + this.resourceTemplateName(b.getVmproviderId(), b.getResourceTemplateId()).trim().replace(":", "") + "_" + b.getNodetemplateId() +"_" +this.isProxy.findVMProviderbyId(b.getVmproviderId()).getCredentials().getVo() + "\n"
					+"  scaling: " + "\n" 
					+"   min: "+ b.getScaling().getMin() +"\n"
					+"   max: "+ b.getScaling().getMax(); 
		}
		finalbuild=builded+builded2;
		return finalbuild;
	}
	
	
	
	public OccopusInfrastructure createInfrastructureByTemplate(String infraTemplate) {
		try {
			this.isProxy.setD4ScienceOccopusAuth();
			//this.isProxy.setFedCloudOccopusAuth();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		OccopusInfrastructureTemplate it = this.isProxy.returnInfraTemplate(infraTemplate);

		CreateInfraResponse cir = this.occpusClient.createInfrastructure(this.buildOccopusDescription(it));

		String infraId = cir.getInfraid();

		if (infraId == null) {
			LOGGER.error("A problem occurred during the infrastructure creation. Operation aborted");
			return null;
		}

		OccopusInfrastructure infra = new OccopusInfrastructure();
		// Infrastructure infra = this.isProxy.returnInfra(it);
		infra.setId(infraId);
		infra.setInfrastructureTemplate(it.getId());

		LOGGER.info("Infrastructure created with id=" + infraId + " from template " + it.getId());

		this.isProxy.addInfra(infra);
		return infra;
	}
	
	

	public Node createNode(String vmProviderId, String serviceProfileId, String resourceTemplateId) {
		VMProvider vmp = this.isProxy.getVMProviderById(vmProviderId);
		FHNConnector connector = this.connectorFactory.getConnector(vmp);

		try {

			connector.connect();

			// 1. collect parameters needed to create the VM
			NodeTemplate nt = this.isProxy.getNodeTemplate(serviceProfileId, vmProviderId);
			LOGGER.debug("NodeTemplate found: " + nt);
			String osTemplateId = nt.getOsTemplateId();
			LOGGER.debug("OSTemplateId found: " + osTemplateId);
			ResourceTemplate resourceTemplate = connector.getResourceTemplate(new URI(resourceTemplateId));
			LOGGER.debug("ResourceTemplate found: " + nt);
			OSTemplate osTemplate = connector.getOSTemplate(new URI(osTemplateId));
			LOGGER.debug("OSTemplate found: " + nt);
			ServiceProfile sp = this.isProxy.getServiceProfileById(serviceProfileId);

			// 2. crete the VM
			String scriptNew = ScriptUtil.getScriptFromURL(nt.getScript());
			System.out.println(scriptNew);
			String script2 = scriptNew.replace("export SMARTGEARS_TOKEN='token'", "export SMARTGEARS_TOKEN="+ SecurityTokenProvider.instance.get());
			System.out.println(script2);
			
			URI vmId = connector.createVM("vm"+UUID.randomUUID(), osTemplate, resourceTemplate, /*nt.getScript()*/script2);
			VM vm = connector.getVM(vmId);

			// 3. update the IS
			Node node = NodeHelper.createNode(vm, vmp, sp, nt, resourceTemplate);
			this.isProxy.addNode(node);
			// this.isProxy.updateIs();
			return node;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Set<Node> findNodes(String serviceProfileId, String vmProviderId) throws UnknownHostException {
		return this.isProxy.findNodes(serviceProfileId, vmProviderId);
	}

	@Override
	public Set<VMProvider> findVMProviders(String serviceProfileId) throws FHNManagerException {
		return this.isProxy.findVMProvidersbyServiceProfile(serviceProfileId);
	}

	public VMProvider getVMProviderbyId(String vmProviderid) {
		return this.isProxy.findVMProviderbyId(vmProviderid);
	}

	@Override
	public Set<ServiceProfile> allServiceProfiles() throws FHNManagerException {
		return this.isProxy.getAllServiceProfiles();

	}

	@Override
	public Collection<ResourceTemplate> findResourceTemplate(String vmProviderid) {
		boolean success = false;
		if (vmProviderid == null) {
			Collection<ResourceTemplate> listvmp = new HashSet<ResourceTemplate>();
			for (VMProvider vmp2 : findVMProviders(null)) {
				listvmp.addAll(findResourceTemplate(vmp2.getId()));
			}
			return listvmp;
		}

		VMProvider vmp = this.isProxy.getVMProviderById(vmProviderid);
		FHNConnector connector = this.connectorFactory.getConnector(vmp);
		Collection<ResourceTemplate> list = new HashSet<ResourceTemplate>();

		try {
			connector.connect();
			list = connector.listResourceTemplates();
			success = true;
		} catch (Exception a) {
		}

		if (success) {
			ResourceReference<VMProvider> rr = new ResourceReference<VMProvider>(vmProviderid);
			for (ResourceTemplate a : list) {
				a.setVmProvider(rr);
			}
		}
		return list;
	}

	public Node getNodeById(String nodeId) {
		return this.isProxy.getNodeById(nodeId);
	}

	public void startNode(String NodeId) throws FHNManagerException {
		Node n = this.isProxy.getNodeById(NodeId);

		VMProvider vmp = this.isProxy.getVMProviderById(n.getVmProvider().getRefId());
		FHNConnector connector = this.connectorFactory.getConnector(vmp);
		try {
			connector.startVM(URI.create(NodeHelper.getVMId(n.getId())));
		} catch (CommunicationException e) {
			throw new ConnectorException("Exception received from the connector: " + e.getMessage());
		}
	}

	public void stopNode(String NodeId) throws FHNManagerException {
		Node n = this.isProxy.getNodeById(NodeId);

		VMProvider vmp = this.isProxy.getVMProviderById(n.getVmProvider().getRefId());
		FHNConnector connector = this.connectorFactory.getConnector(vmp);
		try {
			System.out.println(NodeHelper.getVMId(n.getId()));
			connector.stopVM(URI.create(NodeHelper.getVMId(n.getId())));
		} catch (CommunicationException e) {
			throw new ConnectorException("Exception received from the connector: " + e.getMessage());
		}
	}

	public void deleteNode(String NodeId) throws FHNManagerException {
		Node n = this.isProxy.getNodeById(NodeId);
		VMProvider vmp = this.isProxy.getVMProviderById(n.getVmProvider().getRefId());
		FHNConnector connector = this.connectorFactory.getConnector(vmp);
		try {
			connector.destroyVM(URI.create(NodeHelper.getVMId(n.getId())));
			this.isProxy.deleteNode(n);
			if (!n.getHostname().equals("")){
			this.isProxy.deleteHostingNode(n.getHostname());
 
			}
		} catch (CommunicationException e) {
			throw new ConnectorException("Exception received from the connector: " + e.getMessage());
		}
	}
 

	@Override
	public void destroyInfrastructure(String infrastructureId) {
		javax.ws.rs.client.Client client = ClientBuilder.newClient();
		WebTarget target = client.target(URL + "/infrastructures/" + infrastructureId);
		Response response = target.request().accept("application/json").delete();
		if (response.getStatus() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		}
		this.isProxy.deleteInfrastructure(infrastructureId);
		System.out.println("Infrastructure " + infrastructureId + " correctly deleted");
	}

	public void getInfrastructures() {
		try {
			javax.ws.rs.client.Client client = ClientBuilder.newClient();
			WebTarget target = client.target(URL + "/infrastructures");
			Response response = target.request().accept("application/json").get();
			String entity = response.readEntity(String.class);
			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			}
			System.out.println(entity);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public OccopusInfrastructure getInfrastructureById(String infraId) {
		if (infraId == null) {
			LOGGER.error("A problem occurred during the infrastructure returning. Operation aborted");
			return null;
		}
		return this.isProxy.getInfrastructureById(infraId);
	}

	public void updateOccopusInfra() {
		for (OccopusInfrastructure a : this.isProxy.getAllInfrastructuresList()) {
			try {
				GetInfraResponse cir = this.occpusClient.getInfrastructure(a.getId());
				if (cir == null) {
					return;
				}
				a.setInstanceSets(cir.getInstanceSets());
				this.isProxy.updateInfra(a);
				//this.isProxy.addOccopusNode();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	
//	public void updateOccopusInfra() {
//		for (OccopusInfrastructure a : this.isProxy.getAllInfrastructures()) {
//			try {
//				try{
//				GetInfraResponse cir = this.occpusClient.getInfrastructure(a.getId());
//				if (cir == null) {
//					return;
//				}
//				a.setInstanceSets(cir.getInstanceSets());
//				}catch(Exception excpt){}
//				this.isProxy.updateInfra(a);
//			} catch (Exception ex) {
//				ex.printStackTrace();
//			}
//		}
//	}
//	
	
	
	
	
	public void getOccopusInfrastructureById(String infraid) {
		try {
			javax.ws.rs.client.Client client = ClientBuilder.newClient();
			WebTarget target = client.target(URL + "/infrastructures/" + infraid);
			Response response = target.request().accept("application/json").get();
			String entity = response.readEntity(String.class);
			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			}
			System.out.println(entity);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Set<OccopusInfrastructure> getAllInfrastructures() {
		// TODO Auto-generated method stub
		LOGGER.info("OccopusInfra:");
		this.getInfrastructures();
		LOGGER.info("ISInfra:");
		return this.isProxy.getAllInfrastructures();
	}

	//
	// public void unmarshall(String x) throws IOException {
	//
	// //Read the JSON string
	// JsonElement root = new JsonParser().parse(x);
	//
	// //Get the content of the first map
	// JsonObject object =
	// root.getAsJsonObject().get("nodename").getAsJsonObject();
	//
	// //Iterate over this map
	// Gson gson = new Gson();
	// for (Entry<String, JsonElement> entry : object.entrySet()) {
	// GetInfraResponse shoppingList = gson.fromJson(entry.getValue(),
	// GetInfraResponse.class);
	// System.out.println(shoppingList.getDetails());
	//
	//
	// }
	//
	//
	// }
	

	public static void main(String[] args) {
		FHNManagerImpl a = new FHNManagerImpl();
		ScopeProvider.instance.set("/gcube/devsec");
		// System.out.println(a.getInfrastructureById("cc360be3-7b7b-425d-ac88-71f7bdf95fc3"));
		// a.updateOccopusInfra();
		// a.updateOccopusInfra();
		//a.updateOccopusInfra();
		// a.destroyInfrastructure("e03f75e1-970b-4f5a-a7f1-2388e59cf201");
		// a.getOccopusInfrastructureById("e03f75e1-970b-4f5a-a7f1-2388e59cf201");
		// System.out.println(a.getAllInfrastructures());
		// System.out.println(a.getAllInfrastructures());
		// a.destroyInfrastructure("27ca06df-ffc8-429a-ab1f-1668ee3e2d35@88a66f67-c831-4aed-ad0f-6f0e9036e17c");
		// a.stopNode("2c17727a-daec-4e04-810d-a6c6fb6ad2a2");
		// a.startNode("NodeHelper.getVMId(n.getId()))");
		// a.getInfrastructure();
		// a.createInfrastructureByTemplate("88a66f67-c831-4aed-ad0f-6f0e9036e17c");
		// a.getInfrastructureById("52e9337c-a299-490c-b87a-ee5ee8ac4976");
		// a.getInfrastructure();
		// a.destroyInfrastructure("6d1a05e6-e12f-4109-95a8-fb3b680de52c");
		// a.getInfrastructureById("f094473c-4dcd-4dd8-881d-d09f264a2587");
		
//		OccopusInfrastructureTemplate b = new OccopusInfrastructureTemplate();
//		Set<NodeDefinition> nd = new HashSet<NodeDefinition>();
//		NodeDefinition nd1 = new NodeDefinition();
//		NodeDefinition nd2 = new NodeDefinition();
//
//		nd1.setNodetemplateId("123");
//		nd1.setResourceTemplateId("123");
//		nd1.setVmproviderId("123");
//		OccopusScalingParams os = new OccopusScalingParams();
//		//os.setActual(1);
//		//os.setActual(2);
//		os.setMax(3);
//		os.setMin(4);
//		nd1.setScaling(os);
//
//
//		nd2.setNodetemplateId("1234");
//		nd2.setResourceTemplateId("1234");
//		nd2.setVmproviderId("1234");
//		OccopusScalingParams os2 = new OccopusScalingParams();
//		os2.setMax(5);
//		os2.setMin(6);
//		nd2.setScaling(os2);
//		
//		nd.add(nd1);	
//		nd.add(nd2);	
//		b.setSetnd(nd);
//		
//	
		
		//OccopusInfrastructureTemplate b = a.isProxy.returnInfraTemplate("occopusInfraTemplate132093e8-a6d2-4221-8371-074f426a7792");
		//System.out.println(b.getId());
		OccopusInfrastructureTemplate b = a.isProxy.returnInfraTemplate("occopusInfraTemplate3ee7cda3-0171-42a7-8a42-38c89b6912c2");
		//a.buildOccopusDescription(b);
		//System.out.println(b.getSetnd());
		a.createInfrastructureByTemplate(b.getId());
		
		//b.setId("occopusInfraTemplate132093e8-a6d2-4221-8371-074f426a7792");
		//System.out.println(a.isProxy.returnInfraTemplate(b.getId()));
		//System.out.println(b.getSetnd());
		//System.out.println(a.buildOccopusDescription(b));
		
		
		//a.resourceTemplateName("0b9f4509-0d45-4f28-aa59-50afd5557877", "http://schemas.openstack.org/template/resource#6");
		
		//a.createInfrastructureByTemplate("occopusInfraTemplate7b6d0543-c9f4-4eac-9018-666bb4b29f5e");
		// a.getInfrastructureById("8e024492-dc93-44c9-9ff1-ac1d25ff55a9");
		// a.destroyInfrastructure("8e024492-dc93-44c9-9ff1-ac1d25ff55a9");
		// a.getInfrastructure();
		// a.destroyInfrastructure("aae9a7a1-ac24-4753-9559-5a01c7540d00");
		// a.getInfrastructureById("850a79fc-3b00-4268-936b-267b6b54eb3d");
		// a.getInfrastructureById("d5a339d8-1e32-4f99-9abe-3ca644b370ae");
	}

}