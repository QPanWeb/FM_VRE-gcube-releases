package gr.cite.bluebridge.analytics.resources;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;

import gr.cite.bluebridge.analytics.logic.Evaluator;
import gr.cite.bluebridge.analytics.model.Consumption;
import gr.cite.bluebridge.analytics.model.Economics;
import gr.cite.bluebridge.analytics.model.Fish;
import gr.cite.bluebridge.analytics.model.FryGeneration;
import gr.cite.bluebridge.analytics.model.ModelInput;
import gr.cite.bluebridge.analytics.utils.DatabaseCredentials;
import gr.cite.bluebridge.analytics.utils.DatabaseDiscovery;
import gr.cite.bluebridge.analytics.utils.ServiceDiscovery;
import gr.cite.bluebridge.analytics.utils.ServiceProfile;
import gr.cite.bluebridge.analytics.utils.SingletonHttpClient;

@Path("/")
public class AnalyticsResource {
	private final Logger logger = LoggerFactory.getLogger(AnalyticsResource.class);

	@GET
	@Path("ping")
	public String ping() {
		System.out.println("ping");
		return "pong";
	}

	@POST
	@Path("performAnalysis")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response performAnalysis(
			@HeaderParam("scope") String scope, 
			@HeaderParam("gcube-token") String token,
			Parameters params) {
		
		System.out.println("Debugging environment information........");
		System.out.println("SCOPE is -> " + scope);
		System.out.println("TOKEN is -> "+ token);
		
		ServiceProfile simulFishGrowthDataAPI = ServiceProfile.createSimulFishGrowthAPI();
		
		String consumptionString = null;
		try {			
			// Fetch Database Credentials 
			
			ServiceProfile simulFishGrowthDatabase = ServiceProfile.createSimulFishGrowthDatabase();
			DatabaseCredentials databaseCredentials = DatabaseDiscovery.fetchDatabaseCredentials(scope, simulFishGrowthDatabase);
			
			// Fetch Endpoint URL
			
			String serviceApiUrl = ServiceDiscovery.fetchServiceEndpoint(scope, simulFishGrowthDataAPI);	
			
			// Consumption Request
			
			String serviceConsumptionUrl = serviceApiUrl + "Scenario/execute/consumption/160101/170630/218/750000/" + params.getModelId();			
			
			Client client = SingletonHttpClient.getSingletonHttpClient().getClient();
			ClientResponse clientResponse = null;
			WebResource webResource = client.resource(serviceConsumptionUrl);			

			clientResponse = webResource.
								accept("application/json").
								type(MediaType.APPLICATION_JSON).
								header("gcube-token", token).
								header("scope", scope).
								header("dbname", databaseCredentials.getDbname()).
								header("dbuser", databaseCredentials.getDbuser()).
								header("dbhost", databaseCredentials.getDbhost()).
								header("dbpass", databaseCredentials.getDbpass()).
								get(ClientResponse.class);
			Status status = clientResponse.getClientResponseStatus();
			consumptionString = clientResponse.getEntity(String.class);					
		} catch (Exception e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
		
		ObjectMapper mapper = new ObjectMapper();
	    mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);	    

	    String substring = ",";
	    String replacement = "";
	    
	    int index = consumptionString.lastIndexOf(substring);
	    if (index > -1){
	    	consumptionString = consumptionString.substring(0, index) 
	    	+ replacement
	    	+ consumptionString.substring(index+substring.length());
	    }
	    
		Consumption consumption = null;
		try {
			consumption = mapper.readValue(consumptionString, Consumption.class);
		} catch (Exception e) {
			e.printStackTrace();
		}			
		
		ModelInput input = new ModelInput();
		
		Fish giltheadSeaBream = new Fish();
		giltheadSeaBream.setFish("giltheadSeaBream");
		giltheadSeaBream.setMixPercent(params.getFishMix());
		giltheadSeaBream.setInitialPrice(params.getSellingPrice());
		input.getFishes().add(giltheadSeaBream);
	
		input.setTaxRate(params.getTaxRate());
		input.setFeedPrice(params.getFeedPrice());
		input.setFryPrice(params.getFryPrice());
		input.setOffShoreAquaFarm(params.getIsOffShoreAquaFarm());
		input.setConsumption(consumption);		
		
		Map<Integer, FryGeneration> generationsPerYear = new HashMap<>();
		generationsPerYear.put(1,  new FryGeneration(750000, 2.18));
		generationsPerYear.put(4,  new FryGeneration(750000, 2.18));
		generationsPerYear.put(7,  new FryGeneration(750000, 2.18));
		generationsPerYear.put(10, new FryGeneration(750000, 2.18));
		input.setGenerationsPerYear(generationsPerYear);
		
		Economics economics = new Evaluator().calculate(input);
		//Evaluator.printValues(economics.getDepreciatedValues());

		return Response.ok(economics).build();
	}	
}
