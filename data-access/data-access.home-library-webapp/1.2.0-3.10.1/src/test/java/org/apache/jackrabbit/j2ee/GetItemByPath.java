package org.apache.jackrabbit.j2ee;
import static org.gcube.resources.discovery.icclient.ICFactory.clientFor;
import static org.gcube.resources.discovery.icclient.ICFactory.queryFor;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.jackrabbit.j2ee.workspacemanager.NodeManager;
import org.apache.jackrabbit.rmi.repository.URLRemoteRepository;
import org.apache.jackrabbit.util.ISO9075;
import org.apache.jackrabbit.util.Text;
import org.gcube.common.encryption.StringEncrypter;
import org.gcube.common.homelibary.model.items.ItemDelegate;
import org.gcube.common.homelibary.model.items.type.NodeProperty;
import org.gcube.common.homelibrary.model.exceptions.InternalErrorException;
import org.gcube.common.resources.gcore.ServiceEndpoint;
import org.gcube.common.resources.gcore.ServiceEndpoint.AccessPoint;
import org.gcube.common.scope.api.ScopeProvider;
import org.gcube.resources.discovery.client.api.DiscoveryClient;
import org.gcube.resources.discovery.client.queries.api.SimpleQuery;
import org.globus.wsrf.client.GetProperties;

import com.thoughtworks.xstream.XStream;


public class GetItemByPath {
	private static final String nameResource 				= "HomeLibraryRepository";
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		ScopeProvider.instance.set("/d4science.research-infrastructures.eu");
		//		String rootScope = "/gcube";
		//	ScopeProvider.instance.set(rootScope);

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
					
					Node node = session.getNode("/Home/Wolfgang.Pempe/");
					node.remove();
					session.save();

//					String path = "/Home";
//					NodeIterator nodes = session.getNode(path).getNodes();
//					while(nodes.hasNext()){
//						Node node = nodes.nextNode();
//						if (node.getPrimaryNodeType().getName().equals("nthl:home")){
//
//							Node folders = session.getNode("/Home/" + node.getName() + "/Folders");
//							NodeIterator smartFolders = folders.getNodes();
//							if (smartFolders.getSize()>0){
//								System.out.println(node.getName() + " - " + node.getPrimaryNodeType().getName());
//								while(smartFolders.hasNext()){
//									Node smartFolder = smartFolders.nextNode();
//									System.out.println("****** " + smartFolder.getName()+ " - " + smartFolder.getPrimaryNodeType().getName());
//								
//								}
//							}
//						}
//					}

					//			System.out.println(node.getPath());



					//			for (PropertyIterator iterator = node.getProperties();
					//					iterator.hasNext();) {
					//				Property property = iterator.nextProperty();
					//				System.out.println(property.getName() + ": " + property.getString());
					//			}

					//				Date created = session.getNode(path).getProperty("jcr:created").getDate().getTime();
					//				System.out.println(created);
					//					String path = "/Home/valentina.marioli/Workspace/00000/test%2A%2Aòòò@-735e5267-439e-4291-b999-739a9eaf269a.jpg";
					//					String absPath = "/Home/valentina.marioli/Workspace/00000/";
					//					Node node = session.getNode((path));
					//			
					//				System.out.println(absPath);
					//					Node nodee = session.getNode((absPath));
					//					
					//					Node myNode = nodee.addNode(Text.escapeIllegalJcrChars("è$$*****çò@img-dce2e35e-8d"),nodee.getPrimaryNodeType().getName() );
					//					myNode.setProperty("hl:lastAction", node.getProperty("hl:lastAction").getString());
					//					session.save();
					//					Item myItem = session.getItem(Text.escapeIllegalJcrChars("/Home/valentina.marioli/Workspace/00000/è$$*****çò@img-dce2e35e-8d"));
					//					System.out.println(myItem.getPath());
					//					System.out.println(node.getPrimaryNodeType().getName());


					//					ItemDelegate item = null;
					//					NodeManager wrap = new NodeManager(node, "");
					//					
					//						item = wrap.getItemDelegate();
					//						System.out.println(item.getPath());
					//						System.out.println(item.isShared());
					//						System.out.println(item.getProperties().get(NodeProperty.SHARED_ROOT_ID));
					////						System.out.println(item.toString());

					//					System.out.println(getProp(root));

				}
			}
		}finally{}
	}

//	private static Map<String,String> getProp(Node itemNode) throws InternalErrorException {
//		Map<String,String> map = new HashMap<String,String>();
//		try {
//
//			Node propertiesNode = itemNode.getNode(NodeProperty.METADATA.toString());
//
//			for (PropertyIterator iterator = propertiesNode.getProperties();
//					iterator.hasNext();) {
//				Property property = iterator.nextProperty();
//				if(!property.getName().startsWith("jcr:")) {
//					String unescapeName = Text.unescape(property.getName());
//					map.put(unescapeName,
//							property.getValue().toString());
//				}
//			}
//
//			return map;
//		} catch (PathNotFoundException e) {
//			if (itemNode!= null) {
//				try {
//					itemNode.addNode(NodeProperty.METADATA.toString());
//					itemNode.getSession().save();
//					return map; 
//				} catch (RepositoryException e1) {
//					System.out.println("Error to add properties ");
//					throw new InternalErrorException(e1.getMessage());
//				}
//			} else {
//				throw new InternalErrorException(e.getMessage());
//			}
//		} catch (RepositoryException e) {
//			throw new InternalErrorException(e);
//		} 
//
//	}
}
