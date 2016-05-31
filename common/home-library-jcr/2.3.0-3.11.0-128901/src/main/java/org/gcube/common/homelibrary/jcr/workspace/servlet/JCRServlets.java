package org.gcube.common.homelibrary.jcr.workspace.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.Validate;
import org.gcube.common.homelibary.model.items.ItemDelegate;
import org.gcube.common.homelibary.model.items.SearchItemDelegate;
import org.gcube.common.homelibary.model.items.accounting.AccountingDelegate;
import org.gcube.common.homelibrary.home.workspace.exceptions.ItemNotFoundException;
import org.gcube.common.homelibrary.jcr.repository.JCRRepository;
import org.gcube.common.homelibrary.jcr.workspace.lock.JCRLockManager;
import org.gcube.common.homelibrary.model.exceptions.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class JCRServlets {

	private String urlRepository;
	private Logger logger = LoggerFactory.getLogger(JCRServlets.class);
	private String login;
	private String sessionId;
	private JCRLockManager lockManager;


	public JCRServlets(String login, Boolean createSession) throws RepositoryException{
		this.login = login;
		this.urlRepository = JCRRepository.url;
		if (createSession)
			sessionId = getSession();
	}

	public JCRServlets() throws RepositoryException{
		this.login = null;
		this.urlRepository = JCRRepository.url;
		sessionId = getSession();
	}

	public String getLogin(){
		return this.login;
	}

	public String getSessionId(){
		return this.sessionId;
	}

	public String getUrlRepository(){
		return this.urlRepository;
	}
	
	private String userInfo(){
		StringBuilder info = new StringBuilder();
		info.append(JCRRepository.getCredentials());
		info.append("&login=" + login);
		return info.toString();
		
	}

	/**
	 * Get a new session
	 * @param login
	 * @return
	 * @throws RepositoryException 
	 */
	private String getSession() throws RepositoryException {
		//		logger.info("Calling servlet getSession by " + login);
		String uuid = null;
		GetMethod getMethod = null;
		XStream xstream = new XStream(new DomDriver("UTF-8"));
		HttpClient httpClient = new HttpClient(); 

		try {   
			getMethod =  new GetMethod(JCRRepository.url + "/CreateSession?" + userInfo());
			httpClient.executeMethod(getMethod);	
			uuid = (String) xstream.fromXML(getMethod.getResponseBodyAsStream());
			//			logger.info("Session " + uuid + " has been created by " + login);
		} catch (Exception e) {
			throw new RepositoryException(e.getMessage());
		} finally {
			if(getMethod != null)
				getMethod.releaseConnection();
		}

		return uuid;
	}

	/**
	 * Release session
	 * @throws RepositoryException 
	 */
	public void releaseSession() {
		//		logger.info("Calling servlet releaseSession " + sessionId +  " by " + login);

		if (sessionId!=null){
			GetMethod getMethod = null;
			HttpClient httpClient = new HttpClient(); 

			try {   
				getMethod =  new GetMethod(JCRRepository.url + "/ReleaseSession?" + userInfo() + "&uuid="+ sessionId);
				httpClient.executeMethod(getMethod);	
				//		xstream.fromXML(getMethod.getResponseBodyAsStream());
			} catch (Exception e) {
				logger.error(e.getMessage());
			} finally {
				if(getMethod != null)
					getMethod.releaseConnection();
			}
		}
	}

	//	/**
	//	 * Release all active sessions
	//	 */
	//	public void releaseAllSessions() {
	//		logger.info("Calling servlet releaseAllSessions by " + login);
	//
	//		GetMethod getMethod = null;
	//		HttpClient httpClient = new HttpClient(); 
	//
	//		try {   
	//			getMethod =  new GetMethod(JCRRepository.url + "/ReleaseAllSessions?" + userInfo() );
	//			httpClient.executeMethod(getMethod);	
	//			//		xstream.fromXML(getMethod.getResponseBodyAsStream());
	//		} catch (Exception e) {
	//			e.getStackTrace();
	//		} finally {
	//			if(getMethod != null)
	//				getMethod.releaseConnection();
	//		}
	//	}

	/**
	 * Get children by id using a GET servlet
	 * @param user
	 * @return
	 * @throws RepositoryException 
	 * @throws InternalErrorException
	 */
	@SuppressWarnings("unchecked")
	public List<ItemDelegate> getChildrenById(String id, Boolean showHidden) throws RepositoryException {
		logger.info("Calling servlet getChildrenById " + id + " by " + login);
		List<ItemDelegate> items = null;
		GetMethod getMethod = null;
		XStream xstream = new XStream(new DomDriver("UTF-8"));
		HttpClient httpClient = new HttpClient(); 

		try {  
//			System.out.println(JCRRepository.url + "/get/GetChildrenById?" + userInfo() + "&id=" + id +  "&uuid="+sessionId+ "&showHidden="+showHidden);
			getMethod =  new GetMethod(JCRRepository.url + "/get/GetChildrenById?" + userInfo() + "&id=" + id +  "&uuid="+sessionId+ "&showHidden="+showHidden);
			httpClient.executeMethod(getMethod);	
			items= (List<ItemDelegate>) xstream.fromXML(getMethod.getResponseBodyAsStream());
		} catch (Exception e) {
			throw new RepositoryException(e.getMessage());
		} finally {
			if(getMethod != null)
				getMethod.releaseConnection();
		}
		return items;
	}


	@SuppressWarnings("unchecked")
	public List<ItemDelegate> GetHiddenItemsById(String id) throws RepositoryException {
		logger.info("Calling servlet GetHiddenItemsById " + id + " by " + login);
		List<ItemDelegate> items = null;
		GetMethod getMethod = null;
		XStream xstream = new XStream(new DomDriver("UTF-8"));
		HttpClient httpClient = new HttpClient(); 

		try {   
			getMethod =  new GetMethod(JCRRepository.url + "/get/GetHiddenItemsById?" + userInfo() + "&id=" + id +  "&uuid="+sessionId);
			httpClient.executeMethod(getMethod);	
			items= (List<ItemDelegate>) xstream.fromXML(getMethod.getResponseBodyAsStream());
		} catch (Exception e) {
			throw new RepositoryException(e.getMessage());
		} finally {
			if(getMethod != null)
				getMethod.releaseConnection();
		}
		return items;
	}








	@SuppressWarnings("unchecked")
	public Map<String, String> moveToTrashIds(List<String> ids, String trashId) throws RepositoryException {

		Validate.notNull(trashId, "trashId must be not null");
		Validate.notNull(ids, "ids must be not null");

		logger.info("Calling Servlet MoveToTrashIds on " + ids.size() + " by " + login);

		//		ItemDelegate modifiedItem = null;
		Map<String, String> error = null;
		PostMethod post = null;
		HttpClient httpClient = new HttpClient(); 
		XStream xstream = new XStream(new DomDriver("UTF-8"));
		try {
			//			System.out.println("*************** " + item.toString());
			//						System.out.println(urlRepository + "/post/MoveToTrashIds?" + userInfo() + "&uuid="+sessionId + "&trashId="+ trashId);
			post =  new PostMethod(urlRepository + "/post/MoveToTrashIds?" + userInfo() + "&uuid="+sessionId + "&trashId="+ trashId);
			post.setRequestEntity(new StringRequestEntity(xstream.toXML(ids), "application/json", null));

			// execute the POST
			int response = httpClient.executeMethod(post);
			// Check response code
			if (response != HttpStatus.SC_OK)
				throw new HttpException("Received error status " + response);

			error = (Map<String, String>) xstream.fromXML(post.getResponseBodyAsStream());

		} catch (IOException e) {
			throw new RepositoryException(e.getMessage());
		} finally {
			if(post != null)
				post.releaseConnection();
		}

		return error;
	}



	/**
	 * Retrieve an ItemDelegate object by path using a GET servlet
	 * @param user
	 * @param path
	 * @return an ItemDelegate
	 * @throws ItemNotFoundException 
	 */
	public ItemDelegate getItemByPath(String path) throws ItemNotFoundException {
		logger.info("*** Calling Servlet GetItemByPath " + path + " by " + login);
		ItemDelegate item = null;
		GetMethod getMethod = null;
		XStream xstream = new XStream(new DomDriver("UTF-8"));

		HttpClient httpClient = new HttpClient();  

		try {

			//						System.out.println(urlRepository + "/get/GetItemByPath?" + userInfo() + "&path=" + URLEncoder.encode(path, "UTF-8")+  "&uuid="+sessionId);
			getMethod =  new GetMethod(urlRepository + "/get/GetItemByPath?" + userInfo() + "&path=" + URLEncoder.encode(path, "UTF-8")+  "&uuid="+sessionId);
			httpClient.executeMethod(getMethod);
			item = (ItemDelegate) xstream.fromXML(getMethod.getResponseBodyAsStream());
			//		item.getId();
		} catch (Exception e) {
			throw new ItemNotFoundException(e.getMessage());
		} finally {
			if(getMethod != null)
				getMethod.releaseConnection();
		}
		return item;
	}


	/**
	 * Retrieve and ItemDelegate by id using a GET servlet
	 * @param user
	 * @param id
	 * @return
	 */
	public ItemDelegate getItemById(String id) throws ItemNotFoundException{
		logger.info("Servlet getItemById " + id);
		ItemDelegate item = null;
		GetMethod getMethod = null;
		HttpClient httpClient = new HttpClient();   
		XStream xstream = new XStream(new DomDriver("UTF-8"));

		try {
//			System.out.println("---> "+  urlRepository + "/get/GetItemById?" + userInfo() + "&id=" + id );
			getMethod =  new GetMethod(urlRepository + "/get/GetItemById?" + userInfo() + "&id=" + id );
			httpClient.executeMethod(getMethod);
			item = (ItemDelegate) xstream.fromXML(getMethod.getResponseBodyAsStream());

		} catch (Exception e) {
			throw new ItemNotFoundException(e.getMessage());
		} finally {
			if(getMethod != null)
				getMethod.releaseConnection();
		}
		return item;
	}

	/**
	 * Retrieve and ItemDelegate by id using a GET servlet
	 * @param user
	 * @param id
	 * @return
	 */
	public ItemDelegate getParentById(String id) throws ItemNotFoundException{
		logger.info("Servlet getParentById " + id);
		ItemDelegate item = null;
		GetMethod getMethod = null;
		HttpClient httpClient = new HttpClient();   
		XStream xstream = new XStream(new DomDriver("UTF-8"));

		try {

			getMethod =  new GetMethod(urlRepository + "/get/GetParentById?" + userInfo() + "&id=" + id + "&uuid="+sessionId);
			httpClient.executeMethod(getMethod);
			item = (ItemDelegate) xstream.fromXML(getMethod.getResponseBodyAsStream());

		} catch (Exception e) {
			throw new ItemNotFoundException(e.getMessage());
		} finally {
			if(getMethod != null)
				getMethod.releaseConnection();
		}
		return item;
	}

	/**
	 * Save item: if it does not exist, create it, otherwise modify it
	 * @param item
	 * @return
	 * @throws IOException 
	 * @throws Exception
	 */
	public ItemDelegate saveItem(ItemDelegate item) throws RepositoryException {

		Validate.notNull(item, "item must be not null");

		logger.info("Calling Servlet SaveItem " + item.getName() + " by " + login);

		ItemDelegate modifiedItem = null;

		PostMethod post = null;
		HttpClient httpClient = new HttpClient(); 
		XStream xstream = new XStream(new DomDriver("UTF-8"));
		try {

			//			System.out.println("*************** " + item.toString());
//						System.out.println(urlRepository + "/post/SaveItem?" + userInfo() + "&uuid="+sessionId);
			post =  new PostMethod(urlRepository + "/post/SaveItem?" + userInfo() + "&uuid="+sessionId);
			post.setRequestEntity(new StringRequestEntity(xstream.toXML(item), "application/json", null));

			// execute the POST
			int response = httpClient.executeMethod(post);
			// Check response code
			if (response != HttpStatus.SC_OK)
				throw new HttpException("Received error status " + response);

			modifiedItem = (ItemDelegate) xstream.fromXML(post.getResponseBodyAsStream());

		} catch (IOException e) {
			throw new RepositoryException(e.getMessage());
		} finally {
			if(post != null)
				post.releaseConnection();
		}

		return modifiedItem;
	}

	/**
	 * Copy node, change name and set as owner the current user
	 * @param path
	 * @param pathDestination
	 * @param name
	 * @param owner 
	 * @return
	 */
	//	public ItemDelegate copy(String srcAbsPath, String destAbsPath, String name, String owner) {
	//	
	//		System.out.println("Call servlet copy");
	//		ItemDelegate item = null;
	//
	//		PostMethod post = new PostMethod();
	//		HttpClient httpClient = new HttpClient(); 
	//		XStream xstream = new XStream();
	//		try {
	////			srcAbsPath, String destAbsPath, boolean removeExisting)
	//			System.out.println(urlRepository + "/Copy?srcAbsPath=" + srcAbsPath + "&destAbsPath=" +destAbsPath + "&removeExisting="+removeExisting);
	//			post =  new PostMethod(urlRepository + "/Copy?srcAbsPath=" + srcAbsPath + "&destAbsPath=" +destAbsPath + "&removeExisting="+removeExisting);
	//
	//			// execute the POST
	//			int response = httpClient.executeMethod(post);
	//			System.out.println("status: " + response);
	//			// Check response code
	//			if (response != HttpStatus.SC_OK)
	//				throw new HttpException("Received error status " + response);
	//
	//			item = (ItemDelegate) xstream.fromXML(post.getResponseBodyAsStream());
	//
	//
	//		} finally {
	//			if(post != null)
	//				post.releaseConnection();
	//		}
	//		return item;
	//		
	//		//		workspace.copyRemoteContent(itemSaved,itemSaved);
	//	
	//	}


	/**
	 * Clone item
	 * @param srcAbsPath
	 * @param destAbsPath
	 * @param b
	 * @return
	 * @throws IOException 
	 * @throws HttpException 
	 */
	public ItemDelegate clone(String srcAbsPath, String destAbsPath, boolean removeExisting) throws HttpException, IOException {
		logger.info("Calling Servlet Clone from " + srcAbsPath + " to " +destAbsPath +" by " + login);
		ItemDelegate modifiedItem = null;

		GetMethod getMethod = new GetMethod();
		HttpClient httpClient = new HttpClient(); 
		XStream xstream = new XStream(new DomDriver("UTF-8"));
		try {
			getMethod =  new GetMethod(urlRepository + "/get/Clone?" + userInfo() + "&srcAbsPath=" + URLEncoder.encode(srcAbsPath, "UTF-8") + "&destAbsPath=" +URLEncoder.encode(destAbsPath, "UTF-8") + "&removeExisting="+removeExisting + "&uuid="+sessionId);

			// execute the POST
			httpClient.executeMethod(getMethod);

			modifiedItem = (ItemDelegate) xstream.fromXML(getMethod.getResponseBodyAsStream());

		} finally {
			if(getMethod != null)
				getMethod.releaseConnection();
		}
		return modifiedItem;

	}



	/**
	 * Moves the node at srcAbsPath (and its entire subtree) to the new location at destAbsPath. 
	 * @param srcAbsPath is an absolute path to the original location 
	 * @param destAbsPath is an absolute path to the parent node of the new location, appended with the new name desired for the moved node
	 * @return the item moved
	 * @throws HttpException
	 * @throws IOException
	 */
	public ItemDelegate move(String srcAbsPath, String destAbsPath) throws HttpException, IOException {
		logger.info("Calling Servlet Move from " + srcAbsPath + " to " +destAbsPath +" by " + login);
		ItemDelegate modifiedItem = null;

		GetMethod getMethod = null;
		HttpClient httpClient = new HttpClient(); 
		XStream xstream = new XStream(new DomDriver("UTF-8"));
		try {
			getMethod =  new GetMethod(urlRepository + "/get/Move?" + userInfo() + "&srcAbsPath=" + URLEncoder.encode(srcAbsPath, "UTF-8") + "&destAbsPath=" +URLEncoder.encode(destAbsPath, "UTF-8")+ "&uuid="+sessionId);

			// execute the POST
			int response = httpClient.executeMethod(getMethod);
			// Check response code
			if (response != HttpStatus.SC_OK)
				throw new HttpException("Received error status " + response);

			modifiedItem = (ItemDelegate) xstream.fromXML(getMethod.getResponseBodyAsStream());

		} finally {
			if(getMethod != null)
				getMethod.releaseConnection();
		}

		return modifiedItem;

	}

	public void removeItem(String absPath) throws RepositoryException{

		logger.info("Calling Servlet RemoveItem " + absPath +" by " + login);
		PostMethod post = new PostMethod();
		HttpClient httpClient = new HttpClient(); 
		//		XStream xstream = new XStream(new DomDriver("UTF-8"));
		//		Boolean removedItem = false;
		try {
			post =  new PostMethod(urlRepository + "/post/RemoveItem?" + userInfo() + "&absPath=" +  URLEncoder.encode(absPath, "UTF-8") + "&uuid=" + sessionId);

			// execute the POST
			int response = httpClient.executeMethod(post);
			// Check response code
			if (response != HttpStatus.SC_OK)
				throw new HttpException("Received error status " + response);

			//			removedItem = (Boolean) xstream.fromXML(post.getResponseBodyAsStream());

		} catch (IOException e) {
			//			removedItem = false;
			throw new RepositoryException("Exception removing item " + absPath);
		} finally {
			if(post != null)
				post.releaseConnection();
		}
		//		return removedItem;

	}

	public ItemDelegate copy(String srcAbsPath, String destAbsPath) throws IOException {
		logger.info("Calling Servlet Copy from " + srcAbsPath +" to "+ destAbsPath + " by " + login);

		ItemDelegate item = null;

		GetMethod post = new GetMethod();
		HttpClient httpClient = new HttpClient(); 
		XStream xstream = new XStream(new DomDriver("UTF-8"));
		try {
			post =  new GetMethod(urlRepository + "/get/Copy?" + userInfo() + "&srcAbsPath=" +  URLEncoder.encode(srcAbsPath, "UTF-8") + "&destAbsPath=" +  URLEncoder.encode(destAbsPath, "UTF-8") + "&uuid=" + sessionId);

			// execute the POST
			int response = httpClient.executeMethod(post);
			// Check response code
			if (response != HttpStatus.SC_OK)
				throw new HttpException("Received error status " + response);

			item = (ItemDelegate) xstream.fromXML(post.getResponseBodyAsStream());


		} finally {
			if(post != null)
				post.releaseConnection();
		}
		return item;

	}

	public ItemDelegate copyContent(String srcId, String destId) throws IOException {
		logger.info("Calling Servlet CopyContent from id " + srcId +" to id "+ destId + " by " + login);
		ItemDelegate item = null;

		PostMethod post = new PostMethod();
		HttpClient httpClient = new HttpClient(); 
		XStream xstream = new XStream(new DomDriver("UTF-8"));
		try {
			post =  new PostMethod(urlRepository + "/post/CopyContent?" + userInfo() + "&srcId=" + srcId + "&destId=" +destId+ "&uuid="+sessionId);

			// execute the POST
			int response = httpClient.executeMethod(post);
			// Check response code
			if (response != HttpStatus.SC_OK)
				throw new HttpException("Received error status " + response);

			item = (ItemDelegate) xstream.fromXML(post.getResponseBodyAsStream());


		} finally {
			if(post != null)
				post.releaseConnection();
		}
		return item;
	}


	@SuppressWarnings("unchecked")
	public List<SearchItemDelegate> executeQuery(String query, String lang, int limit) throws HttpException, IOException {
		logger.info("Calling Servlet ExecuteQuery - query: " + query +" - lang: "+ lang + " - limit: "+ limit +" by " + login);
		GetMethod get = null;
		HttpClient httpClient = new HttpClient(); 
		XStream xstream = new XStream(new DomDriver("UTF-8"));
		List<SearchItemDelegate> list = null;
		try {

			get =  new GetMethod(urlRepository + "/get/ExecuteQuery?" + userInfo() + "&query=" + URLEncoder.encode(query, "UTF-8")  + "&lang=" + lang + "&limit=" + limit + "&uuid=" + sessionId);
			//			System.out.println(urlRepository + "/get/ExecuteQuery?" + userInfo() + "&query=" + URLEncoder.encode(query, "UTF-8")  + "&lang=" + lang + "&login="+ login + "&limit="+ limit+ "&uuid="+sessionId);
			// execute the POST
			int response = httpClient.executeMethod(get);
			// Check response code
			if (response != HttpStatus.SC_OK)
				throw new HttpException("Received error status " + response);

			list = (List<SearchItemDelegate>) xstream.fromXML(get.getResponseBodyAsStream());


		} finally {
			if(get != null)
				get.releaseConnection();
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public List<ItemDelegate> searchItems(String query, String lang) throws HttpException, IOException {
		logger.info("Calling Servlet SearchItems - query: " + query +" - lang: "+ lang + " by " + login);
		GetMethod get = null;
		HttpClient httpClient = new HttpClient(); 
		XStream xstream = new XStream(new DomDriver("UTF-8"));
		List<ItemDelegate> list = null;
		try {
			get =  new GetMethod(urlRepository + "/get/SearchItems?" + userInfo() + "&query=" + URLEncoder.encode(query, "UTF-8")  + "&lang=" + lang +  "&uuid="+sessionId);
			// execute the POST
			int response = httpClient.executeMethod(get);
			// Check response code
			if (response != HttpStatus.SC_OK)
				throw new HttpException("Received error status " + response);

			list = (List<ItemDelegate>) xstream.fromXML(get.getResponseBodyAsStream());


		} finally {
			if(get != null)
				get.releaseConnection();
		}
		return list;
	}


	public void saveAccountingItem(AccountingDelegate item) throws RepositoryException {
		Validate.notNull(item, "item must be not null");

		logger.info("Calling Servlet SaveAccountingItem by " + login + " - " + item.getEntryType().toString() + " - " + item.getAccountingProperties().toString());

		PostMethod post = null;
		HttpClient httpClient = new HttpClient(); 
		XStream xstream = new XStream(new DomDriver("UTF-8"));
		try {
			
//			System.out.println("SAVE*** " + sessionId);
			post =  new PostMethod(urlRepository + "/post/SaveAccountingItem?" + JCRRepository.getCredentials()+ "&uuid="+sessionId);
			post.setRequestEntity(new StringRequestEntity(xstream.toXML(item), "application/json", null));

			// execute the POST
			int response = httpClient.executeMethod(post);
			// Check response code
			if (response != HttpStatus.SC_OK)
				throw new HttpException("Received error status " + response);

		} catch (IOException e) {
			throw new RepositoryException(e.getMessage());
		} finally {
			if(post != null)
				post.releaseConnection();
		}


	}

	@SuppressWarnings("unchecked")
	public List<AccountingDelegate> getAccountingById(String id) throws RepositoryException {

		logger.info("Calling Servlet GetAccountingById - id: " + id + " by " + login);

		List<AccountingDelegate> items = null;
		GetMethod getMethod = null;
		XStream xstream = new XStream(new DomDriver("UTF-8"));
		HttpClient httpClient = new HttpClient(); 

		try {   
			getMethod =  new GetMethod(JCRRepository.url + "/GetAccountingById?" + userInfo() + "&id=" + id + "&uuid="+sessionId);
			httpClient.executeMethod(getMethod);		
			items= (List<AccountingDelegate>) xstream.fromXML(getMethod.getResponseBodyAsStream());

		} catch (Exception e) {
			throw new RepositoryException(e.getMessage());
		} finally {
			if(getMethod != null)
				getMethod.releaseConnection();
		}
		return items;

	}

	public ItemDelegate createReference(String itemId, String destinationFolderId, String name) throws HttpException, IOException {
		logger.info("Calling Servlet CreateReference of Node Id " + itemId + " to destination folder ID" +destinationFolderId +" by " + login);
		ItemDelegate modifiedItem = null;

		PostMethod postMethod = null;
		HttpClient httpClient = new HttpClient(); 
		XStream xstream = new XStream(new DomDriver("UTF-8"));
		try {
			//System.out.println(urlRepository + "/post/CreateReference?" + userInfo() + "&srcId=" + itemId + "&destId=" +destinationFolderId +  "&uuid="+sessionId);
			postMethod =  new PostMethod(urlRepository + "/post/CreateReference?" + userInfo() + "&srcId=" + itemId + "&destId=" +destinationFolderId + "&name=" +name + "&uuid="+sessionId);

			// execute the POST
			int response = httpClient.executeMethod(postMethod);
			// Check response code
			if (response != HttpStatus.SC_OK)
				throw new HttpException("Received error status " + response);

			modifiedItem = (ItemDelegate) xstream.fromXML(postMethod.getResponseBodyAsStream());

		} finally {
			if(postMethod != null)
				postMethod.releaseConnection();
		}

		return modifiedItem;
	}



	@SuppressWarnings("unchecked")
	public List<String> getReferences(String itemId) throws HttpException, IOException {
		logger.info("Calling Servlet CreateReference of Node Id " + itemId +" by " + login);
		List<String> list = null;

		GetMethod getMethod = null;
		HttpClient httpClient = new HttpClient(); 
		XStream xstream = new XStream(new DomDriver("UTF-8"));
		try {
			//System.out.println(urlRepository + "/post/CreateReference?" + userInfo() + "&srcId=" + itemId + "&destId=" +destinationFolderId +  "&uuid="+sessionId);
			getMethod =  new GetMethod(urlRepository + "/post/GetReferences?" + userInfo() + "&srcId=" + itemId + "&uuid="+sessionId);

			// execute the POST
			int response = httpClient.executeMethod(getMethod);
			// Check response code
			if (response != HttpStatus.SC_OK)
				throw new HttpException("Received error status " + response);

			list = (List<String>) xstream.fromXML(getMethod.getResponseBodyAsStream());

		} finally {
			if(getMethod != null)
				getMethod.releaseConnection();
		}

		return list;
	}

	public JCRLockManager getLockManager() {
		if (lockManager==null)
			lockManager = new JCRLockManager(login, sessionId);

		return lockManager;

	}

	@SuppressWarnings("unchecked")
	public List<ItemDelegate> getParentsById(String id) throws HttpException, IOException {
		logger.info("Calling Servlet get Parents By Id " + id +" by " + login);

		List<ItemDelegate> parents = null;
		GetMethod getMethod = null;
		HttpClient httpClient = new HttpClient(); 
		XStream xstream = new XStream(new DomDriver("UTF-8"));
		try {

			getMethod =  new GetMethod(urlRepository + "/get/GetParentsById?" + userInfo() + "&id=" + id + "&uuid="+sessionId);

			// execute the POST
			int response = httpClient.executeMethod(getMethod);
			// Check response code
			if (response != HttpStatus.SC_OK)
				throw new HttpException("Received error status " + response);

			parents = (List<ItemDelegate>) xstream.fromXML(getMethod.getResponseBodyAsStream());

		} finally {
			if(getMethod != null)
				getMethod.releaseConnection();
		}

		return parents;
	}

	public ItemDelegate addNode(ItemDelegate parent, String id) throws HttpException, IOException {
		logger.info("Calling Servlet add node with id " + id +" to node " + parent.getPath());


		ItemDelegate item = null;

		PostMethod post = new PostMethod();
		HttpClient httpClient = new HttpClient(); 
		XStream xstream = new XStream(new DomDriver("UTF-8"));
		try {
			post =  new PostMethod(urlRepository + "/post/AddNode?" + userInfo() + "&id=" + id + "&parentId=" + parent.getId() + "&uuid="+sessionId);

			// execute the POST
			int response = httpClient.executeMethod(post);
			// Check response code
			if (response != HttpStatus.SC_OK)
				throw new HttpException("Received error status " + response);

			item = (ItemDelegate) xstream.fromXML(post.getResponseBodyAsStream());

		} finally {
			if(post != null)
				post.releaseConnection();
		}
		return item;

	}

}
