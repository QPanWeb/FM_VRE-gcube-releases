package org.gcube.portlets.gcubeckan.gcubeckandatacatalog.server.manage;

import static org.gcube.resources.discovery.icclient.ICFactory.clientFor;
import static org.gcube.resources.discovery.icclient.ICFactory.queryFor;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.gcube.common.resources.gcore.ServiceEndpoint;
import org.gcube.common.resources.gcore.ServiceEndpoint.AccessPoint;
import org.gcube.common.scope.api.ScopeProvider;
import org.gcube.datacatalogue.ckanutillibrary.DataCatalogue;
import org.gcube.portlets.gcubeckan.gcubeckandatacatalog.shared.ManageProductBean;
import org.gcube.resources.discovery.client.api.DiscoveryClient;
import org.gcube.resources.discovery.client.queries.api.SimpleQuery;
import org.gcube.vomanagement.usermanagement.impl.LiferayUserManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.trentorise.opendata.jackan.internal.org.apache.http.HttpResponse;
import eu.trentorise.opendata.jackan.internal.org.apache.http.client.methods.HttpPost;
import eu.trentorise.opendata.jackan.internal.org.apache.http.entity.ContentType;
import eu.trentorise.opendata.jackan.internal.org.apache.http.entity.StringEntity;
import eu.trentorise.opendata.jackan.internal.org.apache.http.impl.client.CloseableHttpClient;
import eu.trentorise.opendata.jackan.internal.org.apache.http.impl.client.HttpClientBuilder;
import eu.trentorise.opendata.jackan.internal.org.apache.http.util.EntityUtils;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanGroup;
import eu.trentorise.opendata.jackan.model.CkanPair;
import eu.trentorise.opendata.jackan.model.CkanTag;

/**
 * Endpoint for sending update records information to GRSF KB
 * @author Costantino Perciante at ISTI-CNR (costantino.perciante@isti.cnr.it)
 */
public class GRSFNotificationService {

	private static Logger logger = LoggerFactory.getLogger(GRSFNotificationService.class);
	private static final String SERVICE_POST_METHOD = "/service/updater/post";
	private static final String ANNOTATION_KEY = "Annotation on update";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final String STATUS_CUSTOM_FIELD_KEY = "Status";
	private static final int MAX_TRIAL = 5;

	// request post fields
	private static final String CATALOGUE_ID = "catalog_id";
	private static final String KB_ID = "record_id";
	private static final String PRODUCT_TYPE = "type";
	private static final String STATUS = "status";
	private static final String ANNOTATION = "annotation_msg";
	private static final String ERROR = "error";

	// the error of the update on success
	private static final int STATUS_SUCCESS = 200;

	// GRSF update service information
	private static final String SERVICE_NAME = "GRSF Updater";
	private static final String SERVICE_CATEGORY = "Service";

	/**
	 * Discover the service endpoint and return its url
	 * @param context
	 * @return the url of the service on success, null otherwise
	 */
	public static String discoverEndPoint(String context){

		String oldContext = ScopeProvider.instance.get();
		ScopeProvider.instance.set(context);
		String toReturn = null;
		try{
			SimpleQuery query = queryFor(ServiceEndpoint.class);
			query.addCondition("$resource/Profile/Name/text() eq '"+ SERVICE_NAME +"'");
			query.addCondition("$resource/Profile/Category/text() eq '"+ SERVICE_CATEGORY +"'");
			DiscoveryClient<ServiceEndpoint> client = clientFor(ServiceEndpoint.class);
			List<ServiceEndpoint> resources = client.submit(query);

			if (resources.size() == 0){
				logger.error("There is no Runtime Resource having name " + SERVICE_NAME +" and Category " + SERVICE_CATEGORY + " in this scope.");
				throw new Exception("There is no Runtime Resource having name " + SERVICE_NAME +" and Category " + SERVICE_CATEGORY + " in this scope.");
			}
			else {

				for (ServiceEndpoint res : resources) {

					Iterator<AccessPoint> accessPointIterator = res.profile().accessPoints().iterator();

					while (accessPointIterator.hasNext()) {
						ServiceEndpoint.AccessPoint accessPoint = (ServiceEndpoint.AccessPoint) accessPointIterator
								.next();

						// return the path
						toReturn = accessPoint.address();
					}
				}
			}
		}catch(Exception e){
			logger.error("Unable to retrieve such service endpoint information!", e);
		}finally{
			if(oldContext != null && !oldContext.equals(context))
				ScopeProvider.instance.set(oldContext);
		}

		return toReturn;
	}

	/**
	 * Send an update for this bean
	 * @param baseUrl
	 * @param bean
	 * @param username 
	 * @param catalogue 
	 * @return true on success, false otherwise
	 */
	@SuppressWarnings("unchecked")
	public static String updateCatalogueRecord(String serviceUrl, ManageProductBean bean, DataCatalogue catalogue, String username){

		if(serviceUrl == null)
			throw new IllegalArgumentException("GRSF Updater service url cannot be null");

		if(bean == null)
			throw new IllegalArgumentException("Product bean to manage cannot be null");

		try(CloseableHttpClient httpClient = HttpClientBuilder.create().build();){

			JSONObject obj = new JSONObject();
			obj.put(CATALOGUE_ID, bean.getCatalogueIdentifier());
			obj.put(KB_ID, bean.getKnowledgeBaseIdentifier());
			obj.put(PRODUCT_TYPE, bean.getProductType().toLowerCase());
			obj.put(STATUS, bean.getNewStatus().toString().toLowerCase());

			String annotation = bean.getAnnotation();
			if(annotation != null)
				obj.put(ANNOTATION, annotation.replaceAll("\"", ""));

			logger.debug("Update request looks like " + obj.toJSONString());

			HttpPost request = new HttpPost(serviceUrl + SERVICE_POST_METHOD);
			request.setHeader("Accept", "application/json");
			request.setHeader("Content-type", "application/json");
			StringEntity params = new StringEntity(obj.toJSONString());
			request.setEntity(params);
			HttpResponse response = httpClient.execute(request);

			logger.debug("Response code is " + response.getStatusLine().getStatusCode() + " and response message is " + response.getStatusLine().getReasonPhrase());

			String result = EntityUtils.toString(response.getEntity());
			JSONParser parser = new JSONParser();
			JSONObject parsedJSON = (JSONObject)parser.parse(result);

			if(response.getStatusLine().getStatusCode() != STATUS_SUCCESS)
				throw new IllegalArgumentException(
						"Error while performing the update request: " + response.getStatusLine().getReasonPhrase() + 
						"and error in the result bean is " + parsedJSON.get(ERROR));

			// patch the catalogue product
			return patchProduct(catalogue, bean, username);

		}catch(Exception e){
			logger.error("Unable to update this record" + e.getMessage());
			return e.getMessage();
		}

	}

	/**
	 * Patch the product
	 * @param catalogue
	 * @param bean
	 * @param username
	 */
	@SuppressWarnings("unchecked")
	private static String patchProduct(DataCatalogue catalogue,
			ManageProductBean bean, String username) {

		logger.info("Going to patch record in the catalogue with identifier " + bean.getCatalogueIdentifier() + 
				" from user " + username);

		String apiKey = catalogue.getApiKeyFromUsername(username);
		CkanDataset dataset = catalogue.getDataset(bean.getCatalogueIdentifier(), apiKey);
		String errorMessage = null;

		for (int i = 0; i < MAX_TRIAL; i++) {

			try(CloseableHttpClient httpClient = HttpClientBuilder.create().build();){

				JSONObject jsonRequest = new JSONObject();
				JSONArray tagsAsJson = new JSONArray();
				JSONArray groupsAsJson = new JSONArray();
				JSONArray customFieldsAsJson = new JSONArray();

				// manage the custom fields
				List<CkanPair> extras = dataset.getExtras();
				for (CkanPair ckanPair : extras) {
					if(ckanPair.getKey().equals(STATUS_CUSTOM_FIELD_KEY) && ckanPair.getValue().equals(bean.getCurrentStatus().toString()))
						continue;

					JSONObject obj = new JSONObject();
					obj.put("key", ckanPair.getKey());
					obj.put("value", ckanPair.getValue());
					customFieldsAsJson.add(obj);
				}

				// add the new one and the annotation message
				JSONObject newStatus = new JSONObject();
				newStatus.put("key", STATUS_CUSTOM_FIELD_KEY);
				newStatus.put("value", bean.getNewStatus().toString());
				customFieldsAsJson.add(newStatus);

				JSONObject newAnnotation = new JSONObject();
				newAnnotation.put("key", ANNOTATION_KEY);
				newAnnotation.put("value", "date: " + DATE_FORMAT.format(new Date())
						+ ", admin: " +  new LiferayUserManager().getUserByUsername(username).getFullname()
						+ ", message: " + (bean.getAnnotation() != null ? bean.getAnnotation().replaceAll("\"", "") : "none")
						+ ", old status: " + bean.getCurrentStatus().toString()
						+ ", new status: " + bean.getNewStatus().toString()
						);
				customFieldsAsJson.add(newAnnotation);

				// manage the tags
				List<CkanTag> tags = dataset.getTags();

				for(CkanTag ckanTag : tags){
					if(!ckanTag.getName().equals(bean.getCurrentStatus().toString())){
						JSONObject obj = new JSONObject();
						obj.put("vocabulary_id", ckanTag.getVocabularyId());
						obj.put("state", ckanTag.getState().toString());
						obj.put("display_name", ckanTag.getDisplayName());
						obj.put("id", ckanTag.getId());
						obj.put("name", ckanTag.getName());
						tagsAsJson.add(obj);
					}
				}

				// add the new one
				JSONObject newTag = new JSONObject();
				newTag.put("name", bean.getNewStatus().toString());
				newTag.put("display_name", bean.getNewStatus().toString());
				tagsAsJson.add(newTag);

				// manage the groups
				List<CkanGroup> groups = dataset.getGroups();
				for (CkanGroup ckanGroup : groups) {
					if(!ckanGroup.getName().equals("grsf" + "-" + bean.getCurrentStatus().toString().toLowerCase())){
						JSONObject obj = new JSONObject();
						obj.put("name", ckanGroup.getName());
						groupsAsJson.add(obj);
					}
				}

				JSONObject newGroup = new JSONObject();
				newGroup.put("name", "grsf" + "-" + bean.getNewStatus().toString().toLowerCase());
				groupsAsJson.add(newGroup);

				// perform the request
				jsonRequest.put("id", bean.getCatalogueIdentifier());
				jsonRequest.put("tags", tagsAsJson);
				jsonRequest.put("extras", customFieldsAsJson);
				jsonRequest.put("groups", groupsAsJson);

				logger.debug("Request param is going to be " + jsonRequest);

				if((errorMessage = catalogue.patchProductWithJSON(bean.getCatalogueIdentifier(), jsonRequest, apiKey)) == null){
					logger.info("Record patched ...");
					break;
				}else
					continue; // retry

			}catch(Exception e){
				logger.error("Error while trying to patch grsf record (iteration " + i + " of " + MAX_TRIAL + ")" + e.getMessage());
				errorMessage = e.getMessage();
			}
		}
		return errorMessage;
	}
}
