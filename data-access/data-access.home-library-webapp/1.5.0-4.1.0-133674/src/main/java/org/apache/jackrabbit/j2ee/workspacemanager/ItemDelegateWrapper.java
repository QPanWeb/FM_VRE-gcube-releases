package org.apache.jackrabbit.j2ee.workspacemanager;

import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;

import org.apache.jackrabbit.util.Text;
import org.gcube.common.homelibary.model.items.ItemDelegate;
import org.gcube.common.homelibary.model.items.type.ContentType;
import org.gcube.common.homelibary.model.items.type.NodeProperty;
import org.gcube.common.homelibary.model.items.type.PrimaryNodeType;
import org.gcube.common.homelibary.model.util.WorkspaceItemAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;


public class ItemDelegateWrapper {

	protected static final String ACCOUNTING		= "hl:accounting";
	protected static final String NT_ACCOUNTING		= "nthl:accountingSet";

	//to remove
	private static String NT_CONTENT_LEAF = "nthl:workspaceLeafItemContent";

	private static Logger logger = LoggerFactory.getLogger(ItemDelegateWrapper.class);

	String login;
	ItemDelegate item;
	XStream xstream;

	public ItemDelegateWrapper(ItemDelegate item, String login) throws Exception {
		this.item = item;
		this.login = login;
		xstream = new XStream();
	}

	/**
	 * Add node to parentNode
	 * @param session
	 * @param item
	 * @param parentId
	 * @return
	 * @throws InternalErrorException 
	 * @throws RepositoryException 
	 * @throws Exception 
	 */
	public ItemDelegate addNode(Session session) throws RepositoryException {
		Node node = null;

		Node parentNode = null;
		ItemDelegate newNode = null;
		try {
			parentNode = session.getNodeByIdentifier(item.getParentId());

			String type = item.getPrimaryType();
			logger.info("Adding node " + item.getName() + " to parentNode " + parentNode.getPath() + " - type: " + type);
			//			System.out.println(("Adding node " + item.getName() + " to parentNode " + parentNode.getPath() + " - type: " + type));

			String escape = Text.escapeIllegalJcrChars(item.getName());
			node = parentNode.addNode(escape, type);
			//			item.setTitle(escape);
			item.setPath(node.getPath());
			item.setId(node.getIdentifier());

			setProperties(node);
			setCustomProperties(node, item);

			session.save();
			logger.info( item.getPath() + " saved.");

			newNode = getItemDelegate(node, login);
		} catch (Exception e) {
			logger.error("Impossibile to add new node " + item.getName() + " to " + parentNode.getPath(), e.getMessage());
			throw new RepositoryException(e.getMessage());
		} 

		return newNode;
	}

	/**
	 * Escape illegal JCR Chars
	 * @param name
	 * @return
	 */
	private String escapeIllegalJcrChars(String name) {
		String illegalChars = "/:[]|*";
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < name.length(); i++) {
			char ch = name.charAt(i);
			if (illegalChars.indexOf(ch) != -1 || 
					(ch == '.' && name.length() < 3)|| 
					(ch == ' ' && (i == 0 || i == name.length() - 1))) {
				buffer.append(escape(ch));                
			} else {
				buffer.append(ch);
			}
		}
		return buffer.toString();
	}


	private String escape(char ch) {
		return "_".toString();
	}


	private void setProperties(Node node) throws RepositoryException {

		//		System.out.println("SET PROPERTIES");
		//		System.out.println(node.getPath() + " - primary Type: " + item.getPrimaryType());
		//		try{
		//			node.setPrimaryType(item.getPrimaryType());
		//		}catch (Exception e) {
		//			logger.info("error setting PrimaryType");
		//		}

		try{
			node.setProperty(NodeProperty.HIDDEN.toString(), item.isHidden());
		}catch (Exception e) {
			logger.info("error setting " + NodeProperty.HIDDEN);
		}

		try{
			node.setProperty(NodeProperty.TITLE.toString(), item.getTitle());
		}catch (Exception e) {
			logger.info("error setting " + NodeProperty.TITLE);
		}
		try{
			node.setProperty(NodeProperty.PORTAL_LOGIN.toString(), item.getOwner());
		}catch (Exception e) {
			logger.info("error setting " + NodeProperty.PORTAL_LOGIN);
		}
		try{
			node.setProperty(NodeProperty.DESCRIPTION.toString(), item.getDescription());
		}catch (Exception e) {
			logger.info("error setting " + NodeProperty.DESCRIPTION);
		}

		try{
			if (!item.getLastAction().equals(WorkspaceItemAction.CREATED))
				node.setProperty(NodeProperty.LAST_MODIFIED.toString(), item.getLastModificationTime());

		}catch (Exception e) {
			logger.info("error setting " + NodeProperty.LAST_MODIFIED);
		}

		try{
			//			if (node.hasProperty(NodeProperty.LAST_ACTION.toString()))
			//				logger.info("update LAST_ACTION from: " + node.getProperty(NodeProperty.LAST_ACTION.toString()).getString() + " to "+ item.getLastAction().toString());
			//			else
			//				logger.info("setting LAST_ACTION to: " + item.getLastAction());

			node.setProperty(NodeProperty.LAST_ACTION.toString(), item.getLastAction().toString());
		}catch (Exception e) {
			logger.info("error setting " + NodeProperty.LAST_ACTION);
		}

		try {
			//			logger.info("update METADATA");

			Node metadata = node.getNode(NodeProperty.METADATA.toString());

			Map<String,String> map =  item.getMetadata();
			if(map!=null){
				Set<String> keys = map.keySet();
				for(String key: keys){
					String value = map.get(key);
					metadata.setProperty(key, value);
				}
			}

		} catch (RepositoryException e) {
			logger.info("error setting " + NodeProperty.METADATA);
		}


	}

	private ItemDelegate getItemDelegate(Node node, String login) throws RepositoryException {
		NodeManager myNode = null;
		try {
			myNode = new NodeManager(node, login);

		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return myNode.getItemDelegate();
	}

	/**
	 * Set properties on node
	 * @param node
	 * @param item
	 * @return
	 * @throws NoSuchNodeTypeException
	 * @throws VersionException
	 * @throws ConstraintViolationException
	 * @throws LockException
	 * @throws RepositoryException
	 */
	@SuppressWarnings("unchecked")
	private void setCustomProperties(Node node, ItemDelegate item) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {

		String type = item.getPrimaryType();

		Node contentNode = null;
		switch (type) {

		case PrimaryNodeType.NT_WORKSPACE_VRE_FOLDER:
			//			try{
			//				node.setProperty(NodeProperty.DISPLAY_NAME.toString(), item.getProperties().get(NodeProperty.DISPLAY_NAME));
			//			}catch (Exception e) {
			//				logger.error(NodeProperty.DISPLAY_NAME + " not present");
			//			}
			//			try{
			//				node.setProperty(NodeProperty.IS_VRE_FOLDER.toString(), (Boolean) xstream.fromXML(item.getProperties().get(NodeProperty.IS_VRE_FOLDER)));
			//			}catch (Exception e) {
			//				logger.error(NodeProperty.IS_VRE_FOLDER + " not present");
			//			}
			try{
				node.setProperty(NodeProperty.GROUP_ID.toString(), item.getProperties().get(NodeProperty.GROUP_ID));
			}catch (Exception e) {
				logger.error(NodeProperty.GROUP_ID + " not present");
			}
			try{
				node.setProperty(NodeProperty.SCOPE.toString(), (String) xstream.fromXML(item.getProperties().get(NodeProperty.SCOPE)));
			}catch (Exception e) {
				logger.error(NodeProperty.SCOPE + " not present");
			}

			//			break;

		case PrimaryNodeType.NT_WORKSPACE_SHARED_FOLDER:
			try{
				if (item.getProperties().containsKey(NodeProperty.DISPLAY_NAME))
					node.setProperty(NodeProperty.DISPLAY_NAME.toString(), item.getProperties().get(NodeProperty.DISPLAY_NAME));
//				else
//					throw new Exception("Property not present");
			}catch (Exception e) {
				logger.error("Problem setting " + NodeProperty.DISPLAY_NAME + " to node " + node.getPath()+ " - primary type " + node.getPrimaryNodeType().getName(), e.getMessage());
			}
			try{
				if (item.getProperties().containsKey(NodeProperty.IS_VRE_FOLDER))
					node.setProperty(NodeProperty.IS_VRE_FOLDER.toString(), (Boolean) xstream.fromXML(item.getProperties().get(NodeProperty.IS_VRE_FOLDER)));
//				else
//					throw new Exception("Property not present");
			}catch (Exception e) {
				logger.error("Problem setting " + NodeProperty.IS_VRE_FOLDER + " to node " + node.getPath() + " - primary type " + node.getPrimaryNodeType().getName(), e.getMessage());
			} 

			try{
				Node usersNode;
				if (node.hasNode(NodeProperty.USERS.toString())){
					usersNode = node.getNode(NodeProperty.USERS.toString());
//					System.out.println("USER NODE FOUND! " + node.getPath());
				}
				else				
					usersNode = node.addNode(NodeProperty.USERS.toString());

				Map<String, String> users = (Map<String,String>) xstream.fromXML(item.getProperties().get(NodeProperty.USERS));
				Set<String> set = users.keySet();
				for (String user: set){
//					System.out.println("SETTING " + user + " : " + users.get(user));
					usersNode.setProperty(user, users.get(user));
//					System.out.println("DONE!");
				}


			}catch (Exception e) {
//				e.printStackTrace();
				logger.error("Problem setting " + NodeProperty.USERS + " to node " + node.getPath()+ " - primary type " + node.getPrimaryNodeType().getName(), e.getMessage());
			}
			try{
				Node membersNode;
				if (node.hasNode(NodeProperty.USERS.toString()))
					membersNode = node.getNode(NodeProperty.MEMBERS.toString());
				else
					membersNode = node.addNode(NodeProperty.MEMBERS.toString());

				List<String> users = (List<String>) xstream.fromXML(item.getProperties().get(NodeProperty.MEMBERS));
				for (String user: users){
					if(!membersNode.hasNode(user))
						membersNode.addNode(user);
				}
			}catch (Exception e) {
				logger.error("Problem setting " + NodeProperty.MEMBERS + " to node " + node.getPath()+ " - primary type " + node.getPrimaryNodeType().getName(), e.getMessage());
			}


		case PrimaryNodeType.NT_WORKSPACE_FOLDER:
			try{
				node.setProperty(NodeProperty.IS_SYSTEM_FOLDER.toString(), (Boolean) xstream.fromXML(item.getProperties().get(NodeProperty.IS_SYSTEM_FOLDER)));
			}catch (Exception e) {
				logger.error(NodeProperty.IS_SYSTEM_FOLDER + " not present.");
			}	
			break;


		case PrimaryNodeType.NT_WORKSPACE_FILE:
			try{
				contentNode = node.getNode(NodeProperty.CONTENT.toString());			
			}catch (Exception e) {
				contentNode = node.addNode(NodeProperty.CONTENT.toString(), ContentType.GENERAL.toString());
			}
			setFileProperties(item.getContent(), contentNode);

			break;
		case PrimaryNodeType.NT_WORKSPACE_IMAGE:
			try{
				contentNode = node.getNode(NodeProperty.CONTENT.toString());			
			}catch (Exception e) {
				contentNode = node.addNode(NodeProperty.CONTENT.toString(), ContentType.IMAGE.toString());
			}
			//			System.out.println(item.getContent().toString());
			setPropOnImgFile(contentNode, item.getContent());

			break;
		case PrimaryNodeType.NT_WORKSPACE_PDF_FILE:

			try{
				contentNode = node.getNode(NodeProperty.CONTENT.toString());			
			}catch (Exception e) {
				contentNode = node.addNode(NodeProperty.CONTENT.toString(), ContentType.PDF.toString());
			}
			setPropOnPdfFile(contentNode, item.getContent());

			break;
		case PrimaryNodeType.NT_WORKSPACE_URL:
			try{
				contentNode = node.getNode(NodeProperty.CONTENT.toString());			
			}catch (Exception e) {
				contentNode = node.addNode(NodeProperty.CONTENT.toString(), ContentType.GENERAL.toString());
			}
			setPropOnUrl(contentNode, item.getContent());

			break;
		case PrimaryNodeType.NT_GCUBE_ITEM:

			List<String> scopes = (List<String>) xstream.fromXML(item.getProperties().get(NodeProperty.SCOPES));
			node.setProperty(NodeProperty.SCOPES.toString(), scopes.toArray(new String[0]));
			node.setProperty(NodeProperty.CREATOR.toString(), item.getProperties().get(NodeProperty.CREATOR));
			node.setProperty(NodeProperty.ITEM_TYPE.toString(), item.getProperties().get(NodeProperty.ITEM_TYPE));
			node.setProperty(NodeProperty.IS_SHARED.toString(), (Boolean) xstream.fromXML(item.getProperties().get(NodeProperty.IS_SHARED)));
			try{
				node.setProperty(NodeProperty.SHARED_ROOT_ID.toString(), item.getProperties().get(NodeProperty.SHARED_ROOT_ID));
			}catch (Exception e) {
				logger.error(NodeProperty.SHARED_ROOT_ID + " not present");
			}	
			break;
		case PrimaryNodeType.NT_TRASH_ITEM:
			node.setProperty(NodeProperty.TRASH_ITEM_NAME.toString(), item.getProperties().get(NodeProperty.TRASH_ITEM_NAME));
			node.setProperty(NodeProperty.DELETE_DATE.toString(), (Calendar) xstream.fromXML(item.getProperties().get(NodeProperty.DELETE_DATE)));
			node.setProperty(NodeProperty.DELETE_BY.toString(), item.getProperties().get(NodeProperty.DELETE_BY));
			node.setProperty(NodeProperty.DELETED_FROM.toString(), item.getProperties().get(NodeProperty.DELETED_FROM));		
			node.setProperty(NodeProperty.ORIGINAL_PARENT_ID.toString(), item.getProperties().get(NodeProperty.ORIGINAL_PARENT_ID));
			node.setProperty(NodeProperty.TRASH_ITEM_MIME_TYPE.toString(), item.getProperties().get(NodeProperty.TRASH_ITEM_MIME_TYPE));
			node.setProperty(NodeProperty.LENGTH.toString(), item.getProperties().get(NodeProperty.LENGTH));
			node.setProperty(NodeProperty.IS_FOLDER.toString(), (Boolean) xstream.fromXML(item.getProperties().get(NodeProperty.IS_FOLDER)));	
			break;


		case PrimaryNodeType.NT_TIMESERIES_ITEM:

			node.setProperty(NodeProperty.FOLDER_ITEM_TYPE.toString(), item.getProperties().get(NodeProperty.FOLDER_ITEM_TYPE));
			Node contentNodeTS;
			try{
				contentNodeTS = node.getNode(NodeProperty.CONTENT.toString());			
			}catch (Exception e) {
				contentNodeTS = node.addNode(NodeProperty.CONTENT.toString(), ContentType.TS.toString());
			}
			Map<NodeProperty, String> content = item.getContent();

			contentNodeTS.setProperty(NodeProperty.TIMESERIES_CREATED.toString(), content.get(NodeProperty.TIMESERIES_CREATED));
			contentNodeTS.setProperty(NodeProperty.TIMESERIES_CREATOR.toString(), content.get(NodeProperty.CREATOR));
			contentNodeTS.setProperty(NodeProperty.TIMESERIES_DESCRIPTION.toString(), content.get(NodeProperty.DESCRIPTION));
			contentNodeTS.setProperty(NodeProperty.TIMESERIES_DIMENSION.toString(), (long) xstream.fromXML(content.get(NodeProperty.TIMESERIES_DIMENSION)));
			contentNodeTS.setProperty(NodeProperty.TIMESERIES_ID.toString(), content.get(NodeProperty.TIMESERIES_ID));
			contentNodeTS.setProperty(NodeProperty.TIMESERIES_PUBLISHER.toString(), content.get(NodeProperty.TIMESERIES_PUBLISHER));
			contentNodeTS.setProperty(NodeProperty.TIMESERIES_RIGHTS.toString(), content.get(NodeProperty.TIMESERIES_RIGHTS));
			contentNodeTS.setProperty(NodeProperty.TIMESERIES_SOURCE_ID.toString(), content.get(NodeProperty.TIMESERIES_SOURCE_ID));
			contentNodeTS.setProperty(NodeProperty.TIMESERIES_SOURCE_NAME.toString(), content.get(NodeProperty.TIMESERIES_SOURCE_NAME));
			contentNodeTS.setProperty(NodeProperty.TIMESERIES_TITLE.toString(), content.get(NodeProperty.TIMESERIES_TITLE));
			List<String> headerLabels = (List<String>) xstream.fromXML(content.get(NodeProperty.HEADER_LABELS));

			contentNodeTS.setProperty(NodeProperty.HEADER_LABELS.toString(), headerLabels.toArray(new String[headerLabels.size()]));		
			break;

		case PrimaryNodeType.NT_QUERY:
			node.setProperty(NodeProperty.FOLDER_ITEM_TYPE.toString(), item.getProperties().get(NodeProperty.FOLDER_ITEM_TYPE));	
			Node contentNodeQuery;
			try{
				contentNodeQuery = node.getNode(NodeProperty.CONTENT.toString());			
			}catch (Exception e) {
				contentNodeQuery = node.addNode(NodeProperty.CONTENT.toString(), ContentType.QUERY.toString());
			}

			contentNodeQuery.setProperty(NodeProperty.QUERY.toString(), item.getContent().get(NodeProperty.QUERY));
			contentNodeQuery.setProperty(NodeProperty.QUERY_TYPE.toString(), item.getContent().get(NodeProperty.QUERY_TYPE));

			break;

		case PrimaryNodeType.NT_WORKSPACE_FOLDER_ITEM:
			Node contentNodeLeaf;
			try{
				contentNodeLeaf = node.getNode(NodeProperty.CONTENT.toString());			
			}catch (Exception e) {
				contentNodeLeaf = node.addNode(NodeProperty.CONTENT.toString(), item.getContent().get(NodeProperty.CONTENT));
			}

			//			if (item.getContent().get(NodeProperty.CONTENT).equals(ContentType.SMART.toString())){
			//				contentNodeLeaf.setProperty(NodeProperty.QUERY.toString(), item.getContent().get(NodeProperty.QUERY));
			//				try{
			//					contentNodeLeaf.setProperty(NodeProperty.FOLDER_ID.toString(), item.getContent().get(NodeProperty.FOLDER_ID));
			//				}catch (Exception e) {
			//					logger.info(NodeProperty.FOLDER_ID.toString() + "property not in " + item.getPath());}
			//			}
			break;

		case PrimaryNodeType.NT_WORKSPACE_SMART_FOLDER:
			Node contentNodeLeaf1;
			try{
				contentNodeLeaf1 = node.getNode(NodeProperty.CONTENT.toString());			
			}catch (Exception e) {
				contentNodeLeaf1 = node.addNode(NodeProperty.CONTENT.toString(), ContentType.SMART.toString());
			}
			contentNodeLeaf1.setProperty(NodeProperty.QUERY.toString(), item.getContent().get(NodeProperty.QUERY));
			contentNodeLeaf1.setProperty(NodeProperty.FOLDER_ID.toString(), item.getContent().get(NodeProperty.FOLDER_ID));

			break;


		case PrimaryNodeType.NT_WORKSPACE_REPORT:
			Node contentNodeR;
			try{
				contentNodeR = node.getNode(NodeProperty.CONTENT.toString());			
			}catch (Exception e) {
				contentNodeR = node.addNode(NodeProperty.CONTENT.toString(), ContentType.REPORT.toString());
			}

			setFileProperties(item.getContent(), contentNodeR);

			contentNodeR.setProperty(NodeProperty.TEMPLATE_NAME.toString(), item.getContent().get(NodeProperty.TEMPLATE_NAME));
			contentNodeR.setProperty(NodeProperty.AUTHOR.toString(), item.getContent().get(NodeProperty.AUTHOR));
			contentNodeR.setProperty(NodeProperty.RT_CREATED.toString(), (Calendar) xstream.fromXML(item.getContent().get(NodeProperty.RT_CREATED)));
			contentNodeR.setProperty(NodeProperty.LAST_EDIT.toString(),(Calendar) xstream.fromXML(item.getContent().get(NodeProperty.LAST_EDIT)));
			contentNodeR.setProperty(NodeProperty.LAST_EDIT_BY.toString(), item.getContent().get(NodeProperty.LAST_EDIT_BY));
			contentNodeR.setProperty(NodeProperty.NUMBER_OF_SECTION.toString(), (int) xstream.fromXML(item.getContent().get(NodeProperty.NUMBER_OF_SECTION)));
			contentNodeR.setProperty(NodeProperty.STATUS.toString(), item.getContent().get(NodeProperty.STATUS));	

			break;

		case PrimaryNodeType.NT_WORKSPACE_REPORT_TEMPLATE:
			Node contentNodeRT;
			try{
				contentNodeRT = node.getNode(NodeProperty.CONTENT.toString());			
			}catch (Exception e) {
				contentNodeRT = node.addNode(NodeProperty.CONTENT.toString(), ContentType.REPORT_TEMPLATE.toString());
			}

			setFileProperties(item.getContent(), contentNodeRT);

			contentNodeRT.setProperty(NodeProperty.AUTHOR.toString(), item.getContent().get(NodeProperty.AUTHOR));
			contentNodeRT.setProperty(NodeProperty.RT_CREATED.toString(), (Calendar) xstream.fromXML(item.getContent().get(NodeProperty.RT_CREATED)));
			contentNodeRT.setProperty(NodeProperty.LAST_EDIT.toString(), (Calendar) xstream.fromXML(item.getContent().get(NodeProperty.LAST_EDIT)));
			contentNodeRT.setProperty(NodeProperty.LAST_EDIT_BY.toString(), item.getContent().get(NodeProperty.LAST_EDIT_BY));
			contentNodeRT.setProperty(NodeProperty.NUMBER_OF_SECTION.toString(), (int) xstream.fromXML(item.getContent().get(NodeProperty.NUMBER_OF_SECTION)));
			contentNodeRT.setProperty(NodeProperty.STATUS.toString(), item.getContent().get(NodeProperty.STATUS));

			break;


		case PrimaryNodeType.NT_ITEM_SENT:			
			node.setProperty(NodeProperty.SUBJECT.toString(), item.getProperties().get(NodeProperty.SUBJECT));
			node.setProperty(NodeProperty.BODY.toString(), item.getProperties().get(NodeProperty.BODY));
			node.setProperty(NodeProperty.READ.toString(), (boolean) xstream.fromXML(item.getProperties().get(NodeProperty.READ)));	
			node.setProperty(NodeProperty.OPEN.toString(), (boolean) xstream.fromXML(item.getProperties().get(NodeProperty.OPEN)));	

			Map<NodeProperty, String> user = (Map<NodeProperty, String>) xstream.fromXML(item.getProperties().get(NodeProperty.OWNER));
			Node ownerNode =  node.getNode(NodeProperty.OWNER.toString());
			ownerNode.setProperty(NodeProperty.USER_ID.toString(),user.get(NodeProperty.USER_ID));
			ownerNode.setProperty(NodeProperty.PORTAL_LOGIN.toString(), user.get(NodeProperty.PORTAL_LOGIN));

			List<String> addresses = (List<String>) xstream.fromXML(item.getProperties().get(NodeProperty.ADDRESSES));
			node.setProperty(NodeProperty.ADDRESSES.toString(), addresses.toArray(new String[addresses.size()]));

			break;

		case PrimaryNodeType.NT_WORKSPACE_WORKFLOW_REPORT:
			Node contentWFR;
			try{
				contentWFR = node.getNode(NodeProperty.CONTENT.toString());			
			}catch (Exception e) {
				contentWFR = node.addNode(NodeProperty.CONTENT.toString(), NT_CONTENT_LEAF);
			}
			contentWFR.setProperty(NodeProperty.WORKFLOW_DATA.toString(), item.getContent().get(NodeProperty.WORKFLOW_DATA));
			contentWFR.setProperty(NodeProperty.WORKFLOW_ID.toString(), item.getContent().get(NodeProperty.WORKFLOW_ID));
			contentWFR.setProperty(NodeProperty.WORKFLOW_STATUS.toString(), item.getContent().get(NodeProperty.WORKFLOW_STATUS));
			break;

		default:
			break;			
		}

	}

	/**
	 * Setting properties on URL
	 * @param contentNode
	 * @param content
	 */
	private void setPropOnUrl(Node contentNode,	Map<NodeProperty, String> map) {
		try{
			setFileProperties(map, contentNode);
		}catch (Exception e) {
			logger.error("Error setting properties on file ");
		}

	}

	/**
	 * Setting Image properties
	 * @param contentNode
	 * @param map
	 * @throws RepositoryException 
	 */
	private void setPropOnImgFile(Node contentNode, Map<NodeProperty, String> map) throws RepositoryException {

		try{
			setFileProperties(map, contentNode);
		}catch (Exception e) {
			logger.error("Error setting properties on file ");
		}
		try{
			logger.info("Set dimensions for item " + item.getPath());
			//			System.out.println(contentNode.getProperty(NodeProperty.IMAGE_WIDTH.toString()).getString());
			if (map.containsKey(NodeProperty.IMAGE_WIDTH)){
				//			if (contentNode.hasProperty(NodeProperty.IMAGE_WIDTH.toString())){
				try{
					contentNode.setProperty(NodeProperty.IMAGE_WIDTH.toString(), (Integer) xstream.fromXML(map.get(NodeProperty.IMAGE_WIDTH)));
				}catch (Exception e) {
					logger.error("error setting propery " + NodeProperty.IMAGE_WIDTH, e);
				}
			}
			if (map.containsKey(NodeProperty.IMAGE_HEIGHT)){
				//			if (contentNode.hasProperty(NodeProperty.IMAGE_HEIGHT.toString())){
				try{
					contentNode.setProperty(NodeProperty.IMAGE_HEIGHT.toString(), (Integer) xstream.fromXML(map.get(NodeProperty.IMAGE_HEIGHT)));
				}catch (Exception e) {
					logger.error("error setting propery " + NodeProperty.IMAGE_HEIGHT, e);
				}
			}
			if (map.containsKey(NodeProperty.THUMBNAIL_WIDTH)){
				//			if (contentNode.hasProperty(NodeProperty.THUMBNAIL_WIDTH.toString())){
				try{
					contentNode.setProperty(NodeProperty.THUMBNAIL_WIDTH.toString(), (Integer) xstream.fromXML(map.get(NodeProperty.THUMBNAIL_WIDTH)));
				}catch (Exception e) {
					logger.error("error setting propery " + NodeProperty.THUMBNAIL_WIDTH, e);
				}
			}
			if (map.containsKey(NodeProperty.THUMBNAIL_HEIGHT)){
				//			if (contentNode.hasProperty(NodeProperty.THUMBNAIL_HEIGHT.toString())){
				try{
					contentNode.setProperty(NodeProperty.THUMBNAIL_HEIGHT.toString(), (Integer) xstream.fromXML(map.get(NodeProperty.THUMBNAIL_HEIGHT)));			
				}catch (Exception e) {
					logger.error("error setting propery " + NodeProperty.THUMBNAIL_HEIGHT, e);
				}
			}
		}catch (Exception e) {
			logger.error("Error setting properties on image " + contentNode.getPath(), e.getMessage());
		}

	}

	/**
	 * Setting pdf properties
	 * @param contentNode
	 * @param map
	 */
	private void setPropOnPdfFile(Node contentNode, Map<NodeProperty, String> map) {
		try{
			setFileProperties(map, contentNode);
		}catch (Exception e) {
			logger.error("Error setting properties on file ");
		}

		try{
			contentNode.setProperty(NodeProperty.NUMBER_OF_PAGES.toString(), (Integer) xstream.fromXML(map.get(NodeProperty.NUMBER_OF_PAGES)));
			contentNode.setProperty(NodeProperty.VERSION.toString(), (String) xstream.fromXML(map.get(NodeProperty.VERSION)));
			contentNode.setProperty(NodeProperty.AUTHOR.toString(), (String) xstream.fromXML(map.get(NodeProperty.AUTHOR)));
			contentNode.setProperty(NodeProperty.PDF_TITLE.toString(), (String) xstream.fromXML(map.get(NodeProperty.PDF_TITLE)));
			contentNode.setProperty(NodeProperty.PRODUCER.toString(), (String) xstream.fromXML(map.get(NodeProperty.PRODUCER)));
		}catch (Exception e) {
			logger.error("Error setting custom properties on pdf file ");
		}

	}

	/**
	 * Setting basic file properties
	 * @param delegateContent
	 * @param nodeContent
	 * @throws RepositoryException
	 */
	private void setFileProperties(Map<NodeProperty, String> delegateContent, Node nodeContent) throws RepositoryException {
		logger.info("map: " + delegateContent.toString());
		//		logger.info(nodeContent.getPath() + " added - type: " + nodeContent.getPrimaryNodeType().getName());
		try{
			nodeContent.setProperty(NodeProperty.MIME_TYPE.toString(), delegateContent.get(NodeProperty.MIME_TYPE));
		}catch (Exception e) {
			logger.error("error setting propery " + NodeProperty.MIME_TYPE, e);
		}
		try{

			Long size = (Long) new XStream().fromXML(delegateContent.get(NodeProperty.SIZE));
			nodeContent.setProperty(NodeProperty.SIZE.toString(), size);
		}catch (Exception e) {
			logger.error("error setting propery " + NodeProperty.SIZE, e);

		}
		try{
			nodeContent.setProperty(NodeProperty.STORAGE_ID.toString(), delegateContent.get(NodeProperty.STORAGE_ID));
		}catch (Exception e) {
			logger.error("error setting propery " + NodeProperty.STORAGE_ID, e);
		}
		try{
			if (nodeContent.hasProperty(NodeProperty.REMOTE_STORAGE_PATH.toString()))
				logger.info("update REMOTE_STORAGE_PATH from: " + nodeContent.getProperty(NodeProperty.REMOTE_STORAGE_PATH.toString()).getString() + " to "+ delegateContent.get(NodeProperty.REMOTE_STORAGE_PATH));
			else
				logger.info("setting REMOTE_STORAGE_PATH to: " + delegateContent.get(NodeProperty.REMOTE_STORAGE_PATH));

			String remotePath  = delegateContent.get(NodeProperty.REMOTE_STORAGE_PATH);
			nodeContent.setProperty(NodeProperty.REMOTE_STORAGE_PATH.toString(), remotePath);
		}catch (Exception e) {
			logger.error("error setting propery " + NodeProperty.REMOTE_STORAGE_PATH, e);
		}
		try{
			ByteArrayInputStream  binaryUrl = new ByteArrayInputStream(nodeContent.getPath().getBytes());
			Binary binary = nodeContent.getSession().getValueFactory().createBinary(binaryUrl);
			nodeContent.setProperty(NodeProperty.DATA.toString(), binary);
		}catch (Exception e) {
			logger.info("error setting propery " + NodeProperty.DATA);
		}

	}


	//	/**
	//	 * Get node by itemDelegate and modify properties
	//	 * @param session
	//	 * @return
	//	 * @throws javax.jcr.ItemNotFoundException
	//	 * @throws RepositoryException
	//	 */
	//	public ItemDelegate modifyNode(SessionImpl session) throws javax.jcr.ItemNotFoundException, RepositoryException {
	//		Node node = session.getNodeByIdentifier(item.getId());
	//		setProperties(node, item);
	//		return item;
	//
	//	}

	/**
	 * If the item already exists, update it otherwise add it
	 * @return
	 * @throws NoSuchNodeTypeException
	 * @throws VersionException
	 * @throws ConstraintViolationException
	 * @throws LockException
	 * @throws RepositoryException
	 */
	public ItemDelegate save(Session session) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
		Node node;
		try{
			String id = item.getId();
			if (id!=null){				
				logger.info("Modify an existing node, id: " +id);
				node = session.getNodeByIdentifier(id);
				setProperties(node);
				setCustomProperties(node, item);
				node.getSession().save();
			}else{
				logger.info("Create a new node");
				ItemDelegate myItem = addNode(session);
				return myItem;
			}

		}catch ( Exception e) {
			logger.error("impossible to save item delegate", e);
			throw new RepositoryException(e.getMessage());
		}
		return item;
	}


	public ItemDelegate addNodeToParent(Session session) throws NoSuchNodeTypeException, VersionException, ConstraintViolationException, LockException, RepositoryException {
		Node node;
		try{
			String id = item.getId();
			if (id!=null){				
				logger.info("Modify an existing node, id: " +id);
				node = session.getNodeByIdentifier(id);
				setProperties(node);
				setCustomProperties(node, item);
				node.getSession().save();
			}else{
				logger.info("Create a new node");
				ItemDelegate myItem;

				myItem = addNode(session);

				return myItem;
			}

		}catch (Exception e) {
			logger.error("Impossible to save item delegate", e);
			throw new RepositoryException(e.getMessage());
		}
		return item;
	}



}
