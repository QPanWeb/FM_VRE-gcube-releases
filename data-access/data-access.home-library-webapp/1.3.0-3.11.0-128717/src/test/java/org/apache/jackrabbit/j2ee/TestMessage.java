package org.apache.jackrabbit.j2ee;
import static org.gcube.resources.discovery.icclient.ICFactory.clientFor;
import static org.gcube.resources.discovery.icclient.ICFactory.queryFor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.j2ee.workspacemanager.ItemDelegateWrapper;
import org.apache.jackrabbit.j2ee.workspacemanager.NodeManager;
import org.apache.jackrabbit.oak.jcr.query.QueryImpl;
import org.apache.jackrabbit.rmi.repository.URLRemoteRepository;
import org.apache.jackrabbit.util.ISO9075;
import org.gcube.common.encryption.StringEncrypter;
import org.gcube.common.homelibary.model.items.ItemDelegate;
import org.gcube.common.homelibary.model.items.SearchItemDelegate;
import org.gcube.common.homelibary.model.items.type.NodeProperty;
import org.gcube.common.resources.gcore.ServiceEndpoint;
import org.gcube.common.resources.gcore.ServiceEndpoint.AccessPoint;
import org.gcube.common.scope.api.ScopeProvider;
import org.gcube.resources.discovery.client.api.DiscoveryClient;
import org.gcube.resources.discovery.client.queries.api.SimpleQuery;

import com.thoughtworks.xstream.XStream;


public class TestMessage {
	private static final String nameResource 				= "HomeLibraryRepository";
	public static final String PATH_SEPARATOR 				= "/";
	public static final String HOME_FOLDER 					= "Home";
	public static final String SHARED_FOLDER				= "Share";	
	public static final String USERS 						= "hl:users";
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {



		String rootScope = "/gcube";
		//		String rootScope ="/d4science.research-infrastructures.eu";


		ScopeProvider.instance.set(rootScope);

		SimpleQuery query = queryFor(ServiceEndpoint.class);

		query.addCondition("$resource/Profile/Category/text() eq 'Database' and $resource/Profile/Name eq '"+ nameResource + "' ");

		DiscoveryClient<ServiceEndpoint> client = clientFor(ServiceEndpoint.class);

		List<ServiceEndpoint> resources = client.submit(query);


		try {
			ServiceEndpoint resource = resources.get(0);

			for (AccessPoint ap:resource.profile().accessPoints()) {

				if (ap.name().equals("JCR")) {

					String url = ap.address();
					//							url = "http://node11.d.d4science.research-infrastructures.eu:8080/jackrabbit-webapp-patched-2.4.3";

					String user = ap.username();						
					String pass = StringEncrypter.getEncrypter().decrypt(ap.password());


					//		String url = "http://node11.d.d4science.research-infrastructures.eu:8080/jackrabbit-webapp-2.8.0/";
					URLRemoteRepository repository = new URLRemoteRepository(url + "/rmi");
					Session session = repository.login( 
							new SimpleCredentials(user, pass.toCharArray()));
					
					Node node = session.getNodeByIdentifier("6fdea10b-6998-4a10-8a38-f64620e977f6");
					
					NodeManager wrap = new NodeManager(node, "valentina.marioli");
					ItemDelegate item = wrap.getItemDelegate();
					System.out.println(item.toString());
					
					item.getProperties().put(NodeProperty.READ, new XStream().toXML(true));
					
					
//					ItemDelegate item = new ItemDelegate();
//					item.setId("6fdea10b-6998-4a10-8a38-f64620e977f6");
//					item.setName("297e5f0e-2a8c-481a-9a7d-687a2446612c");
//					item.setParentId("3e20a62f-b121-451e-ac11-974a0ceee027");
//					item.setParentPath("/Home/valentina.marioli/InBox/297e5f0e-2a8c-481a-9a7d-687a2446612c");
//					item.setLastModificationTime(null);
//					Map<NodeProperty, String> properties = item.getProperties();
//					
//					properties.put(NodeProperty.ATTACHMENTS_ID, "6dfbf669-9888-43d8-9d01-5b9c4152bb97");
//					properties.put(NodeProperty.SUBJECT, "test");
//					properties.put(NodeProperty.OPEN, new XStream().toXML(false));
//					properties.put(NodeProperty.READ, new XStream().toXML(true));
//					properties.put(NodeProperty.CREATED, null);
//					List<String> addresses = new ArrayList<String>();
//					
//					
//					addresses.add("valentina.marioli");
//					
//					properties.put(NodeProperty.ADDRESSES, new XStream().toXML(addresses));
//					item.setProperties(properties);
					

					
//					  <string>valentina.marioli</string>
//					</list>, hl:owner=<map>
//					  <entry>
//					    <org.gcube.common.homelibary.model.items.type.NodeProperty>PORTAL_LOGIN</org.gcube.common.homelibary.model.items.type.NodeProperty>
//					    <string>valentina.marioli</string>
//					  </entry>
//					  <entry>
//					    <org.gcube.common.homelibary.model.items.type.NodeProperty>USER_ID</org.gcube.common.homelibary.model.items.type.NodeProperty>
//					    <string>9c578c9c-51eb-409c-bb11-f226842c1c92</string>
//					  </entry>
//					</map>}, path=/Home/valentina.marioli/InBox/297e5f0e-2a8c-481a-9a7d-687a2446612c, owner=9c578c9c-51eb-409c-bb11-f226842c1c92, primaryType=nthl:itemSentRequest, lastAction=null, shared=false, locked=false, accounting=null, metadata={}, content=null)
//
//					
					
					
					ItemDelegateWrapper wrapper = new ItemDelegateWrapper(item, "");
					
					ItemDelegate new_item = wrapper.save(session);
					System.out.println(new_item.toString());
				}
			}
		}finally{}
		
		
	}



	
}

