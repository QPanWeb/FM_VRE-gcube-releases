package gr.cite.geoanalytics.mvc;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import gr.cite.gaap.datatransferobjects.layeroperations.UserWorkspaceLayerDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import gr.cite.clustermanager.actuators.layers.DataMonitor;
import gr.cite.gaap.datatransferobjects.ExternalLayerDTO;
import gr.cite.gaap.datatransferobjects.GenericResponse;
import gr.cite.gaap.datatransferobjects.GenericResponse.Status;
import gr.cite.gaap.datatransferobjects.GeoNetworkPublishDataDTO;
import gr.cite.gaap.datatransferobjects.LayerMessengerForAdminPortlet;
import gr.cite.gaap.datatransferobjects.layeroperations.LayerAttributeInfo;
import gr.cite.gaap.datatransferobjects.layeroperations.LayerAttributeInfoWrapper;
import gr.cite.gaap.datatransferobjects.request.GeoNetworkMetadataDTO;
import gr.cite.gaap.servicelayer.ConfigurationManager;
import gr.cite.geoanalytics.dataaccess.entities.layer.DownloadableLayer;
import gr.cite.geoanalytics.dataaccess.entities.layer.Layer;
import gr.cite.geoanalytics.dataaccess.entities.tenant.Tenant;
import gr.cite.geoanalytics.dataaccess.geoserverbridge.elements.Bounds;
import gr.cite.geoanalytics.manager.ImportManager;
import gr.cite.geoanalytics.manager.LayerManager;
import gr.cite.geoanalytics.ows.client.OwsLayer;
import gr.cite.geoanalytics.ows.client.WmsClient;
import gr.cite.geoanalytics.security.SecurityContextAccessor;
import gr.cite.geoanalytics.util.http.CustomException;
import gr.cite.geoanalytics.util.http.CustomResponseEntity;

@Controller
public class LayerController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(LayerController.class);

	@Autowired	private LayerManager layerManager;
	@Autowired	private ImportManager importManager;
	@Autowired	private SecurityContextAccessor securityContextAccessor;
	@Autowired	private DataMonitor dataMonitor;
	@Autowired 	private ConfigurationManager configurationManager;


	//	private static ObjectMapper mapper = new ObjectMapper();

	@RequestMapping(method = RequestMethod.GET, value = { "/userworkspacelayers" }, produces = { "application/json" })
	public @ResponseBody List<UserWorkspaceLayerDto> getUserWorkspaceLayers(HttpServletRequest request) throws Exception {
		logger.debug("Retrieving layer-files from user\'s workspace");

		String token = request.getHeader(this.GCUBE_TOKEN_HEADER_KEY);

		String scope = this.getSecurityContextAccessor().getTenant().getName();
		logger.debug("Token: " + token	+ " scope: " + scope );

		return this.layerManager.getLayerFilesFromWorkspaceFolder(scope, token);
	}

	@RequestMapping(method = RequestMethod.GET, value = { "/getLayerById" })
	public @ResponseBody Layer getLayerById(@RequestParam String layerId) throws Exception {
		logger.debug("Finding layer by id: " + layerId);
		return layerManager.findLayerById(UUID.fromString(layerId));
	}

	@RequestMapping(method = RequestMethod.POST, value = { "/createLayerSpark" }, consumes = { "application/json", "application/x-www-form-urlencoded", "application/xml" })//"application/x-www-form-urlencoded",
	public @ResponseBody String createLayerSpark(RequestEntity<String> requestEntity) throws UnsupportedEncodingException {

		Layer layer = new Gson().fromJson(requestEntity.getBody(), Layer.class);
		
		String layerID = layerManager.createLayerSpark(layer);

		return layerID;
	}

	@RequestMapping(method = RequestMethod.POST, value = { "/deleteLayerSpark" }, consumes = { "application/json", "application/x-www-form-urlencoded", "application/xml" })//"application/x-www-form-urlencoded",
	public @ResponseBody String deleteLayerSpark(RequestEntity<String> requestEntity) {

		Layer layer = new Gson().fromJson(requestEntity.getBody(), Layer.class);

		logger.debug("Deleting layer with id: " + layer.getId().toString());
		try {
			layerManager.deleteLayerFromInfra(layer.getId().toString());
			//			configurationManager.removeLayerConfig(layer.getId());
		} catch (Exception e1) {
			return "";
		}
		logger.debug("Layer deleted!");
		return layer.getId().toString();

	}

	@RequestMapping(method = RequestMethod.POST, value = { "/layers/createLayer" }, consumes = "application/json")
	public @ResponseBody void createLayer(@RequestBody Layer layer) throws Exception {
		Date now = new Date();
		layer.setCreationDate(now);
		layer.setLastUpdate(now);
		logger.debug("Creating layer...");
		String layerID = layerManager.createLayer(layer);
		logger.debug("Layer created! LayerID=" + layerID);
	}

	@RequestMapping(value = "/layers/listLayersByTenant", method = RequestMethod.GET, produces = { "application/json" })
	public @ResponseBody Set<LayerMessengerForAdminPortlet> getLayersByTenant(HttpServletRequest request) throws Exception {
		logger.debug("Getting Layers by Tenant...");
		Tenant tenant = securityContextAccessor.getTenant();
		try {
			Set<LayerMessengerForAdminPortlet> response = layerManager.getLayersInfoOfTenant(tenant);

			logger.debug("Getting Layers by Tenant has been succeeded");
			return response;

		} catch (Exception e) {
			logger.error("Error while retrieving layers for tenant " + tenant.getName(), e);
			return null;
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = { "/layers/deleteLayer" }, consumes = "application/json", produces = {"application/json"})
	public @ResponseBody ResponseEntity<?> deleteLayer(@RequestBody LayerMessengerForAdminPortlet lmfa) {
		logger.info("Deleting Layer [" + lmfa.getId() + ", " + lmfa.getName() + "] and any related information...");

		try {
			this.layerManager.deleteLayerFromInfra(lmfa.getId());
		} catch (CustomException e) {
			return new CustomResponseEntity<String>(e.getStatusCode(), e.getMessage(), e);
		} catch (Exception e) {
			return new CustomResponseEntity<String>(INTERNAL_SERVER_ERROR, "Failed to delete Layer " + lmfa.getName(), e);
		}

		logger.info("Layer [" + lmfa.getId() + ", " + lmfa.getName() + "] has been deleted successfully!");

		return new CustomResponseEntity<String>(OK, "Layer " + lmfa.getName() + " has been deleted successfully!");
	}

	@RequestMapping(value = "/layers/updateLayer", method = RequestMethod.POST, consumes = { "application/json" })
	public @ResponseBody ResponseEntity<?> editLayer(@RequestBody LayerMessengerForAdminPortlet lmfa) {
		logger.info("Updating Layer [" + lmfa.getId() + ", " + lmfa.getName() + "]");

		try {
			UUID layerId = UUID.fromString(lmfa.getId());
			Layer layer = layerManager.findLayerById(layerId);

			if (layer == null) {
				throw new CustomException(HttpStatus.NOT_FOUND, "Failed to update Layer " + lmfa.getName() + ". Layer does not exist");
			}

			this.importManager.editLayer(layerId, lmfa);
		} catch (CustomException e) {
			return new CustomResponseEntity<String>(e.getStatusCode(), e.getMessage(), e);
		} catch (Exception e) {
			return new CustomResponseEntity<String>(INTERNAL_SERVER_ERROR, "Failed to update Layer " + lmfa.getName(), e);
		}

		logger.info("Layer [" + lmfa.getId() + ", " + lmfa.getName() + "] and any related information has been updaetd successfully");

		return new CustomResponseEntity<String>(OK, "Layer " + lmfa.getName() + " has been updated successfully!");
	}

	@RequestMapping(value = "/layers/getLayerStyle", method = RequestMethod.POST, consumes = { "application/json" }, produces = { "application/json" })
	public @ResponseBody ResponseEntity<?> getLayerStyle(@RequestBody String layerID) {

		try {
			UUID layerId = UUID.fromString(layerID);
			logger.debug("Getting layer's style with layer id: " + layerId + "...");

			Layer layer = layerManager.findLayerById(layerId);
			if (layer == null)
				return new CustomResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR, "Layer " + layerId + " not found");
			String style = layer.getStyle();

			logger.debug("Layer's style : " + style + "  has been retrUUID layerIDieved successfully");
			return new CustomResponseEntity<String>(HttpStatus.OK, style);
		} catch (Exception e) {
			logger.error("An error has occurred while getting layer's style", e);
			return new CustomResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR, "An error has occurred while getting layer's style", e);
		}
	}

	@RequestMapping(value = "/layers/listGeocodeSystems", method = RequestMethod.POST, consumes = { "application/json" }, produces = { "application/json" })
	public @ResponseBody GenericResponse getAllGeocodeSystems(HttpServletRequest request) {
		logger.debug("Getting Geocodes...");
		try {
			return new GenericResponse(Status.Success, layerManager.listGeocodeSystmes(), "geocodeSystems");
		} catch (Exception e) {
			e.printStackTrace();
			return new GenericResponse(Status.Failure, null, "geocodeSystems failure");
		}

	}

	@RequestMapping(value = "/layers/listLayerAttributesByLayerID", method = RequestMethod.POST, consumes = {
			"application/json" } , produces={"application/json"} )
	public @ResponseBody List<LayerAttributeInfo> listLayerAttributesByLayerID(@RequestBody UUID layerID) {
		logger.debug("Listing layer attributes for layer with ID: " + layerID);
		List<LayerAttributeInfo> attrs = new ArrayList<LayerAttributeInfo>();
		try {
			logger.debug("Getting Layer attributes of lyaer with ID: " + layerID);

			attrs = layerManager.getLayerAttributesForVisualizationByLayerID(layerID);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return attrs;
	}

	@RequestMapping(value = "/layers/editLayerAttributes", method = RequestMethod.POST, consumes = {
			"application/json" } , produces={"application/json"} )
	public @ResponseBody GenericResponse editLayerAttributes(@RequestBody LayerAttributeInfoWrapper editedAttrs) throws JAXBException {
		logger.debug("Updating layer attributes for layer with ID: " + editedAttrs.getLayerID());

		layerManager.updateLayerAttributesVisualizationEntries(editedAttrs);

		logger.debug("Updated layer attributes for layer with ID: " + editedAttrs.getLayerID());

		return new GenericResponse(Status.Success, "", null);
	}

	@RequestMapping(value = "/layers/getMaxReplicationFactor", method = RequestMethod.GET, produces = { "application/json" })
	public @ResponseBody ResponseEntity<?> getAllStyles(HttpServletRequest request) throws Exception {
		logger.debug("Retrieving number of GOS in the infrastructure");

		int gosNumber = 0;

		try {

			gosNumber = dataMonitor.getAllGosEndpoints().size();

		} catch (Exception e) {
			return new CustomResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve replication factor. Please try again later.", e);
		}

		logger.debug("Number of GOS retrieved successfully!");

		return new CustomResponseEntity<Integer>(HttpStatus.OK, gosNumber);
	}

	@RequestMapping(value = "/layers/addExternalLayer", method = RequestMethod.POST, consumes = { "application/json" }, produces = {"application/json" } )
	public @ResponseBody ResponseEntity<?> addExternalLayer(@RequestBody ExternalLayerDTO externalLayer) {
		logger.info("Adding external layer ...");

		String geoserverUrl = externalLayer.getGeoserverUrl();
		String workspace = externalLayer.getWorkspace();
		String name = externalLayer.getName();
		
		logger.debug("URL : " + externalLayer.getGeoserverUrl());
		logger.debug("Workspace : " + externalLayer.getWorkspace());
		logger.debug("Name: = " + externalLayer.getName());
		
		try{
			Assert.isTrue(geoserverUrl != null && !geoserverUrl.trim().isEmpty(), "Geoserver url cannot be empty");
			Assert.isTrue(workspace != null && !workspace.trim().isEmpty(), "Workspace cannot be empty");
			Assert.isTrue(name != null && !name.trim().isEmpty(), "External layer name cannot be empty");
		} catch (IllegalArgumentException e){
			return new CustomResponseEntity<String>(BAD_REQUEST, e.getMessage(), e);
		}
		
		try {
			WmsClient wmsClient = new WmsClient();
			
			OwsLayer owsLayer = wmsClient.getOwsLayer(geoserverUrl, workspace, name);	
			
			this.layerManager.createLayerFromWmsRequest(owsLayer);
		} catch (CustomException e) {
			return new CustomResponseEntity<String>(e.getStatusCode(), e.getMessage(), e);
		} catch (Exception e) {
			return new CustomResponseEntity<String>(INTERNAL_SERVER_ERROR, "Failed to add external layer ", e);
		}

		logger.info("External Layer added successfully!");

		return new CustomResponseEntity<String>(OK, "External Layer added successfully!");
	}	
	
	@RequestMapping(value = "/layers/downloadLayer", method = RequestMethod.GET)
	public ResponseEntity<?> downloadLayer(@RequestParam String layerId) {
		Layer layer = null;

		try {	
			layer = this.layerManager.findLayerById(UUID.fromString(layerId));
		} catch (Exception e) {
			logger.error("Could not find selected layer", e);
			return ResponseEntity.status(NOT_FOUND).body("Could not download selected layer. Layer is missing");		
		}
		
		logger.info("Downloading layer source data of layer [" + layer.getId() + ", " + layer.getName() + "]");
		
		try {
			DownloadableLayer downloadableLayer = this.layerManager.downloadLayerFromGeoserver(layer);
			byte[] data = downloadableLayer.getData();
			
			logger.info("Layer [" + layer.getId() + ", " + layer.getName() + "] has been downloaded successfully!");

			return ResponseEntity.ok()
				    .header("Content-Disposition", "attachment;filename=" + downloadableLayer.getFilename())
		            .header("Content-Type", downloadableLayer.getContentType())
				    .header("filename", downloadableLayer.getFilename())
		            .contentLength(data.length)
				    .body(data);
		} catch (Exception e) {
			return ResponseEntity.status(NOT_FOUND).body("Could not download layer " + layer.getName() + ". Maybe layer data is corrupted");	
		}
	}	
	
	@RequestMapping(value = "/layers/publishLayerOnGeoNetwork", method = RequestMethod.POST)
	public ResponseEntity<?> publishLayerOnGeoNetwork(@RequestBody GeoNetworkPublishDataDTO geoNetworkPublishDataDTO, HttpServletResponse response) {
		try{
			Tenant tenant = this.getSecurityContextAccessor().getTenant();

			String layerId = geoNetworkPublishDataDTO.getLayerId();
			GeoNetworkMetadataDTO geoNetworkMetadataDTO = geoNetworkPublishDataDTO.getGeoNetworkMetadataDTO();

			Layer layer = this.getLayerById(layerId);
			Bounds bounds = new Bounds(this.configurationManager.getLayerConfig(UUID.fromString(layerId)).getBoundingBox());
			
			this.importManager.publishLayerToGeoNetwork(layer, geoNetworkMetadataDTO, bounds, tenant);
		} catch (CustomException e) {
			return new CustomResponseEntity<String>(e.getStatusCode(), e.getMessage(), e);
		} catch (Exception e) {
			return new CustomResponseEntity<String>(INTERNAL_SERVER_ERROR, "Something went wrong with the publishing. Please try again", e);
		}
		
		return new CustomResponseEntity<String>(OK, "Layer has been published on GeoNetwork successfully!");
	}
	
	@RequestMapping(value = "/layers/unpublishLayerFromGeoNetwork", method = RequestMethod.POST)
	public ResponseEntity<?> unpublishLayerFromGeoNetwork(@RequestBody String layerId, HttpServletResponse response) {
		try{	
			Layer layer = this.getLayerById(layerId);
			
			this.layerManager.unpublishLayerFromGeoNetwork(layer);			
		} catch (CustomException e) {
			return new CustomResponseEntity<String>(e.getStatusCode(), e.getMessage(), e);
		} catch (Exception e) {
			return new CustomResponseEntity<String>(INTERNAL_SERVER_ERROR, "Something went wrong with the unpublishing. Please try again", e);
		}
		
		return new CustomResponseEntity<String>(OK, "Layer has been unpublished from GeoNetwork successfully!");
	}
}
