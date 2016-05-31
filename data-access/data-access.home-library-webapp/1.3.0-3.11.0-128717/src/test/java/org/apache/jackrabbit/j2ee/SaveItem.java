package org.apache.jackrabbit.j2ee;
import static org.gcube.resources.discovery.icclient.ICFactory.clientFor;
import static org.gcube.resources.discovery.icclient.ICFactory.queryFor;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.j2ee.workspacemanager.ItemDelegateWrapper;
import org.apache.jackrabbit.j2ee.workspacemanager.accounting.AccoutingNodeWrapper;
import org.apache.jackrabbit.rmi.repository.URLRemoteRepository;
import org.gcube.common.encryption.StringEncrypter;
import org.gcube.common.homelibary.model.items.ItemDelegate;
import org.gcube.common.homelibary.model.items.accounting.AccountingDelegate;
import org.gcube.common.homelibary.model.items.type.NodeProperty;
import org.gcube.common.homelibary.model.util.WorkspaceItemAction;
import org.gcube.common.resources.gcore.ServiceEndpoint;
import org.gcube.common.resources.gcore.ServiceEndpoint.AccessPoint;
import org.gcube.common.scope.api.ScopeProvider;
import org.gcube.resources.discovery.client.api.DiscoveryClient;
import org.gcube.resources.discovery.client.queries.api.SimpleQuery;

import com.thoughtworks.xstream.XStream;


public class SaveItem {
	private static final String nameResource 				= "HomeLibraryRepository";
	/**
	 * @param args
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException {



		String rootScope = "/gcube";


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

//				List<AccountingDelegate> list = getAccouting(session, "3d4787ba-8802-445e-a02e-cb4a15a0142b");
//				for(AccountingDelegate item: list){
//					System.out.println(item.getEntryType());
//				}
				

					
					
					
					ItemDelegate delegate = new ItemDelegate();
					delegate.setId("0fd97841-ca6a-4a04-a047-8dab11445180");
					delegate.setName("carousel-link.png");
					delegate.setTitle("carousel-link.png");
					delegate.setDescription(null);
					delegate.setLastModifiedBy("workspacerep.imarine");
					delegate.setParentId("a80a21e1-7488-4fee-8a13-606b6e00b5e6");
					delegate.setParentPath(null);
					delegate.setLastModificationTime(null);
					delegate.setCreationTime(null);
		
					Map<NodeProperty, String> properties = new HashMap<NodeProperty, String>();
					properties.put(NodeProperty.SHARED_ROOT_ID, "2efcd0d0-740f-45be-acb5-7e5875071e7d");
					Map<String, String> users = new HashMap<String, String>();
					users.put("valentina.marioli", "83a76ff1-7abb-43e6-b331-90f5f017e7b9/Test");
					users.put("roberto.cirillo", "b09be9bc-b5a9-4940-aee0-53332daf1283/Test");
					properties.put(NodeProperty.USERS, new XStream().toXML(users));
					properties.put(NodeProperty.SHARED_ROOT_ID, "2efcd0d0-740f-45be-acb5-7e5875071e7d");
					
					delegate.setProperties(properties);
					
					
					delegate.setPath("/Home/valentina.marioli/Workspace/AAAA/TERS/Test/tiled-gallery-carousel-without-jetpack/images/carousel-link.png");
					delegate.setOwner("valentina.marioli");
					delegate.setPrimaryType("nthl:externalImage");
					delegate.setLastAction(WorkspaceItemAction.CREATED);
					delegate.setTrashed(false);
					delegate.setShared(true);
					delegate.setHidden(false);
					delegate.setLocked(false);
					delegate.setAccounting(null);
					
					Map<NodeProperty, String> content = new HashMap<NodeProperty, String>();
					int widht = 12;
					content.put(NodeProperty.IMAGE_WIDTH, new XStream().toXML(widht));
					int height = 32;
					content.put(NodeProperty.IMAGE_HEIGHT, new XStream().toXML(height));
					int thumb_width = 400;
					content.put(NodeProperty.THUMBNAIL_WIDTH, new XStream().toXML(thumb_width));
					int thumb_height = 1066;
					content.put(NodeProperty.THUMBNAIL_HEIGHT, new XStream().toXML(thumb_height));
					
					long size = 431;
					content.put(NodeProperty.SIZE, new XStream().toXML(size));
					
					content.put(NodeProperty.MIME_TYPE, "image/png");		
					content.put(NodeProperty.REMOTE_STORAGE_PATH, "/Share/4bb803d5-bd61-4fd8-b7f2-5b575dec8fd2/tiled-gallery-carousel-without-jetpack/images/carousel-link.png");
					content.put(NodeProperty.CONTENT, new XStream().toXML(org.gcube.common.homelibary.model.items.type.ContentType.IMAGE));
					
					delegate.setContent(content);
					delegate.setMetadata(null);
					
					
					
//					System.out.println("start");
//					ItemDelegate delegate = new ItemDelegate();
//					delegate.setId(null);
//					delegate.setName("name");
//					delegate.setTitle("name");
//					delegate.setDescription("description");
//					delegate.setLastModifiedBy("valentina.marioli");
//					delegate.setParentId("cf6caaeb-09ba-48e3-9945-f22bbde9abe0");
//					delegate.setParentPath(null);
//					delegate.setLastModificationTime(null);
//					delegate.setCreationTime(null);
//					Map<NodeProperty, String> properties = new HashMap<NodeProperty, String>();
//					properties.put(NodeProperty.IS_SHARED, new XStream().toXML(false));
//					properties.put(NodeProperty.ITEM_TYPE, "myType");
//					properties.put(NodeProperty.SCOPES, new XStream().toXML(false));
//					
//					List<String> scopes = new ArrayList<String>();
//					scopes.add("/myscope/");					long size = 431;
//					content.put(NodeProperty.SIZE, new XStream().toXML(size));
//					properties.put(NodeProperty.IS_SHARED, new XStream().toXML(scopes));
//					properties.put(NodeProperty.CREATOR, "test.user");
//					
//					delegate.setPath(null);
//					delegate.setOwner("valentina.marioli");
//					delegate.setPrimaryType("nthl:gCubeItem");
//					delegate.setLastAction(WorkspaceItemAction.CREATED);
//					delegate.setTrashed(false);
//					delegate.setShared(false);
//					delegate.setHidden(false);
//					delegate.setLocked(false);
//					delegate.setAccounting(null);
//					Map<String, String> metadata = new HashMap<String, String>();
//					metadata.put("key01", "value01");
//					metadata.put("key00", "value00");
//					Map<NodeProperty, String> content = new HashMap<NodeProperty, String>();
//					delegate.setContent(content);
//					
//					delegate.setMetadata(metadata);
//					
//					delegate.setProperties(properties);
					
					System.out.println(delegate.toString());
					
					ItemDelegateWrapper wrapper = new ItemDelegateWrapper(delegate, delegate.getOwner());
					wrapper.save(session);

			
//					AccountingDelegate delegate = new AccountingDelegate();
//					delegate.setId("45e15eed-2779-42f0-9056-f5571db43b93");
//					delegate.setUser("valentina.marioli");
//					delegate.setDate(Calendar.getInstance());
//					delegate.setEntryType(AccountingEntryType.REMOVAL);
//					Map<AccountingProperty, String> accountingProperties = new HashMap<AccountingProperty, String>();
//					accountingProperties.put(AccountingProperty.FOLDER_ITEM_TYPE, new XStream().toXML(FolderItemType.EXTERNAL_IMAGE));
//					accountingProperties.put(AccountingProperty.ITEM_NAME, new XStream().toXML("2893036344_f51fb1c5a3_z.jpg"));
//					accountingProperties.put(AccountingProperty.ITEM_TYPE, new XStream().toXML(WorkspaceItemType.FOLDER_ITEM));
//					accountingProperties.put(AccountingProperty.MIME_TYPE, new XStream().toXML("image/jpeg"));
//
//					delegate.setAccountingProperties(accountingProperties);
//
//
//					System.out.println("item delegate: " + delegate);
//					AccountingDelegateWrapper wrapper = new AccountingDelegateWrapper(delegate, "");
//					wrapper.save(session);
					//					System.out.println("NEW ITEM: " + delegate.getPath());
					//					String absPath = "/Home/valentina.marioli/Workspace/testGcubeItem";
					//					NodeWrapper wrapNode = new NodeWrapper(session.getNode(absPath));
					//					System.out.println("----> " + wrapNode.getItemDelegate().toString());



				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}


	}
	


	private static List<AccountingDelegate> getAccouting(Session session, String identifier) throws Exception {

		List<AccountingDelegate> children = new ArrayList<AccountingDelegate>();
		
		Node node = session.getNodeByIdentifier(identifier);
		Node accountingNode = node.getNode("hl:accounting");
		for(NodeIterator iterator = accountingNode.getNodes();iterator.hasNext();) {
			Node entryNode = (Node)iterator.next();
			
			AccountingDelegate item = null;
			AccoutingNodeWrapper wrap = new AccoutingNodeWrapper(entryNode);
			try {
				item = wrap.getAccountingDelegate();
//				wrap.setProperties(item);
				children.add(item);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


		}
		return children;
	}



}
