package org.gcube.socialnetworking.social_data_search_client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder.Type;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.gcube.portal.databook.shared.EnhancedFeed;
import org.gcube.socialnetworking.social_data_indexing_common.utils.ElasticSearchRunningCluster;
import org.gcube.socialnetworking.social_data_indexing_common.utils.IndexFields;
import org.gcube.socialnetworking.social_data_indexing_common.utils.SearchableFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The elasticsearch client for gcube portlets.
 * @author Costantino Perciante at ISTI-CNR (costantino.perciante@isti.cnr.it)
 */
public class ElasticSearchClientImpl implements ElasticSearchClient{

	//logger
	public static final Logger logger = LoggerFactory.getLogger(ElasticSearchClientImpl.class);

	private TransportClient client;

	private String clusterName;
	private List<String> hostsToContact;
	private List<Integer> portNumbers;

	/**
	 * Build an elasticsearch client to be queried.
	 * @param scope the scope in the infrastructure
	 * @throws Exception 
	 */
	public ElasticSearchClientImpl(String scope) throws Exception {

		// retrieve ElasticSearch Endpoint and set hosts/port number
		ElasticSearchRunningCluster elasticCluster = new ElasticSearchRunningCluster(scope);

		// save info
		clusterName = elasticCluster.getClusterName();
		hostsToContact = elasticCluster.getHosts();
		portNumbers = elasticCluster.getPorts();

		logger.debug("Creating elasticsearch client for hosts = " + hostsToContact + ", port = " + portNumbers + " and "
				+ " cluster's name = " + clusterName);


		// set cluster's name to check and the sniff property to true.
		// Cluster's name: each node must have this name.
		// Sniff property: allows the client to recover cluster's structure.
		// Look at https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/transport-client.html
		Settings settings = Settings.settingsBuilder()
				.put("cluster.name", this.clusterName)
				.put("client.transport.sniff", true)
				.build();

		// build the client
		client = TransportClient.builder().settings(settings).build();

		// add the nodes to contact
		for (int i = 0; i < hostsToContact.size(); i++){
			try {

				client.addTransportAddress(
						new InetSocketTransportAddress(
								InetAddress.getByName(hostsToContact.get(i)), portNumbers.get(i))
						);

			} catch (UnknownHostException e) {

				logger.debug("Error while adding " + hostsToContact.get(i) + ":" + portNumbers.get(i) + " as host to be contacted.");

			}
		}

		logger.info("Connection to ElasticSearch cluster done.");
	}

	@Override
	public List<EnhancedFeed> search(String query, Set<String> vreIDS, int from, int quantity){

		List<EnhancedFeed> toReturn = new ArrayList<>();

		if(from < 0 || quantity <= 0 || vreIDS.isEmpty())
			return toReturn;

		// build the query
		MultiMatchQueryBuilder mmq = QueryBuilders.multiMatchQuery(
				query, 
				SearchableFields.POST_AUTHOR.toString() + "^3", // weight of 3 for feed's author
				SearchableFields.POST_TEXT.toString() + "^2", // weight of 2 for feed's description
				SearchableFields.ATTACHMENT_NAME.toString(),
				SearchableFields.PREVIEW_DESCRIPTION.toString(),
				SearchableFields.COMMENT_TEXT.toString() + "^2",
				SearchableFields.COMMENT_AUTHOR.toString())
				.type(Type.MOST_FIELDS);

		logger.debug(mmq.toString());

		// filter on vre
		BoolQueryBuilder filter = QueryBuilders.boolQuery();
		TermsQueryBuilder queryFilter = QueryBuilders.termsQuery(SearchableFields.POST_VRE_ID.toString(), vreIDS);
		filter.should(queryFilter);

		logger.debug(filter.toString());

		// final filtered query
		BoolQueryBuilder filteredQuery = QueryBuilders.boolQuery();
		filteredQuery.must(mmq);
		filteredQuery.filter(filter);

		logger.debug(filteredQuery.toString());

		SearchResponse response = client.prepareSearch(IndexFields.INDEX_NAME)
				.setQuery(filteredQuery)
				.setFrom(from)
				.setSize(quantity)
				.setExplain(true)
				.execute()
				.actionGet();

		logger.debug("The search took " + response.getTookInMillis() + " ms");

		SearchHit[] results = response.getHits().getHits();

		logger.debug("Number of hits is " + results.length);

		ObjectMapper mapper = new ObjectMapper();

		// rebuild objects
		for (SearchHit hit : results) {
			EnhancedFeed enhFeed;
			try {

				enhFeed = mapper.readValue(hit.getSourceAsString(), EnhancedFeed.class);
				toReturn.add(enhFeed);

			} catch (IOException e) {
				logger.error(e.toString());
			}
		}

		logger.debug("Returning " + toReturn.size() + " results");
		return toReturn;
	}

	@Override
	public List<EnhancedFeed> searchInField(String query, Set<String> vreIDS,
			int from, int quantity, SearchableFields field) {

		List<EnhancedFeed> toReturn = new ArrayList<>();

		if(from < 0 || quantity <= 0  || field == null || vreIDS.isEmpty())
			return toReturn;

		// build the query
		MatchQueryBuilder mq = QueryBuilders.matchQuery(field.toString(), query);

		logger.debug(mq.toString());

		// filter on vre
		BoolQueryBuilder filter = QueryBuilders.boolQuery();
		TermsQueryBuilder queryFilter = QueryBuilders.termsQuery(SearchableFields.POST_VRE_ID.toString(), vreIDS);
		filter.should(queryFilter);

		logger.debug(filter.toString());

		// final filtered query
		BoolQueryBuilder filteredQuery = QueryBuilders.boolQuery();
		filteredQuery.must(mq);
		filteredQuery.filter(filter);

		logger.debug(filteredQuery.toString());

		SearchResponse response = client.prepareSearch(IndexFields.INDEX_NAME)
				.setQuery(filteredQuery)
				.setFrom(from)
				.setSize(quantity)
				.setExplain(true)
				.execute()
				.actionGet();

		logger.debug("The search took " + response.getTookInMillis() + " ms");

		SearchHit[] results = response.getHits().getHits();

		logger.debug("Number of hits is " + results.length);

		ObjectMapper mapper = new ObjectMapper();

		// rebuild objects
		for (SearchHit hit : results) {
			EnhancedFeed enhFeed;
			try {

				enhFeed = mapper.readValue(hit.getSourceAsString(), EnhancedFeed.class);
				toReturn.add(enhFeed);

			} catch (IOException e) {
				logger.error(e.toString());
			}
		}

		logger.debug("Returning " + toReturn.size() + " results");
		return toReturn;

	}

	@Override
	public boolean deleteDocument(String docID) {

		if(docID == null || docID.isEmpty())
			return false;

		logger.debug("Removing doc with id " + docID);

		DeleteResponse response = client.prepareDelete(IndexFields.INDEX_NAME, IndexFields.EF_FEEDS_TABLE, docID).get();

		return response.isFound();	

	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		logger.info("Called finalize.. closing connection to elasticsearch");
		if(client != null)
			client.close();
	}

}
