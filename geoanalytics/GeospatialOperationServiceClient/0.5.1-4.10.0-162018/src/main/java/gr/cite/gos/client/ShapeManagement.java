package gr.cite.gos.client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import gr.cite.gaap.datatransferobjects.ShapeMessenger;
import gr.cite.geoanalytics.dataaccess.entities.shape.Shape;
import gr.cite.geoanalytics.dataaccess.entities.shape.Shape.Attribute;
import gr.cite.gos.client.helpers.Transforming;


public class ShapeManagement extends GosManagement{

	
	public ShapeManagement(String authenticationStr){
		super(authenticationStr);
	}
	
	public List<String> getLayers(String gosEndpoint) throws IOException{
    	String jsonLayerIds = getJerseyClient().resource(gosEndpoint)
    			.path("/ShapeManagement/existingLayerIDs")
    			.header(super.HEADER_AUTHENTICATION_PARAM_NAME, authenticationStr)
    			.accept(MediaType.APPLICATION_JSON)
    			.get(String.class);
    	TypeReference<List<String>> mappingType = new TypeReference<List<String>>() {};
		return (List<String>)getMapper().readValue(jsonLayerIds, mappingType);
	}
	
	
	
	
	public Shape getShapeByID(String gosEndpoint, String shapeID) throws Exception{
		String shapeMessengerJSON = getJerseyClient().resource(gosEndpoint)
						 .path("/ShapeManagement/getShapeByID/"+shapeID)
						 .header(super.HEADER_AUTHENTICATION_PARAM_NAME, authenticationStr)
						 .accept(MediaType.APPLICATION_JSON)
						 .get(String.class);
		return (shapeMessengerJSON!=null && !shapeMessengerJSON.isEmpty()) ? getMapper().readValue(shapeMessengerJSON, ShapeMessenger.class).toShape() : null;
	}
	
	public boolean deleteShape(String gosEndpoint, String shapeID) throws Exception{
		ClientResponse resp = getJerseyClient().resource(gosEndpoint)
						 .path("/ShapeManagement/deleteShape/"+shapeID)
						 .header(super.HEADER_AUTHENTICATION_PARAM_NAME, authenticationStr)
						 .accept(MediaType.APPLICATION_JSON)
						 .delete(ClientResponse.class);
		return (resp.getStatus()==200);
	}
	
	
	public boolean deleteShapes(String gosEndpoint, List<String> shapeIDs) throws JsonProcessingException {
		MultivaluedMap formData = new MultivaluedMapImpl();
		formData.add(super.HEADER_AUTHENTICATION_PARAM_NAME, authenticationStr);
		formData.add("shapeIDs", getMapper().writeValueAsString(shapeIDs));
		ClientResponse resp = getJerseyClient().resource(gosEndpoint)
						 .path("/ShapeManagement/deleteShapes")
						 .accept(MediaType.APPLICATION_JSON)
						 .delete(ClientResponse.class, formData);
		return (resp.getStatus()==200);
	}
	
	
	public List<Shape> searchShapes(String gosEndpoint, List<String> terms) throws IOException {
		MultivaluedMap formData = new MultivaluedMapImpl();
		formData.add(super.HEADER_AUTHENTICATION_PARAM_NAME, authenticationStr);
		formData.add("termsListJSON", getMapper().writeValueAsString(terms));
		ClientResponse resp = getJerseyClient().resource(gosEndpoint)
						 .path("/ShapeManagement/searchShapes")
						 .accept(MediaType.APPLICATION_JSON)
						 .post(ClientResponse.class, formData);
		
		TypeReference<ArrayList<ShapeMessenger>> mappingType = new TypeReference<ArrayList<ShapeMessenger>>() {};
		return Transforming.fromShapeMessenger((List<ShapeMessenger>)getMapper().readValue((String)resp.getEntity(String.class), mappingType));
	}
	
	
	public List<Shape> searchShapesWithinByAttributes(String gosEndpoint, Map<String, Attribute> attrs, Shape shape) throws IOException{
		MultivaluedMap formData = new MultivaluedMapImpl();
		formData.add(super.HEADER_AUTHENTICATION_PARAM_NAME, authenticationStr);
		formData.add("attrsJSON", getMapper().writeValueAsString(attrs));
		formData.add("shapeMessengerJSON", getMapper().writeValueAsString(new ShapeMessenger(shape)));
		ClientResponse resp = getJerseyClient().resource(gosEndpoint)
				 .path("/ShapeManagement/searchShapesWithinByAttributes")
				 .accept(MediaType.APPLICATION_JSON)
				 .post(ClientResponse.class, formData);
		TypeReference<List<ShapeMessenger>> mappingType = new TypeReference<List<ShapeMessenger>>() {};
		return Transforming.fromShapeMessenger((List<ShapeMessenger>)getMapper().readValue((String)resp.getEntity(String.class), mappingType));
		
	}
	
	public List<Shape> findContains(String gosEndpoint, Shape shape) throws Exception{
		MultivaluedMap formData = new MultivaluedMapImpl();
		formData.add(super.HEADER_AUTHENTICATION_PARAM_NAME, authenticationStr);
		formData.add("shapeMessengerJSON", getMapper().writeValueAsString(new ShapeMessenger(shape)));
		String shapeMessengersJSON = getJerseyClient().resource(gosEndpoint)
				 .path("/ShapeManagement/findContains")
				 .accept(MediaType.APPLICATION_JSON)
				 .post(String.class, formData);
		TypeReference<List<ShapeMessenger>> mappingType = new TypeReference<List<ShapeMessenger>>() {};
		return Transforming.fromShapeMessenger((List<ShapeMessenger>)getMapper().readValue(shapeMessengersJSON, mappingType));
		
	}
	
	
	public List<Shape> findWithin(String gosEndpoint, Shape shape) throws IOException{
		MultivaluedMap formData = new MultivaluedMapImpl();
		formData.add(super.HEADER_AUTHENTICATION_PARAM_NAME, authenticationStr);
		formData.add("shapeMessengerJSON", getMapper().writeValueAsString(new ShapeMessenger(shape)));
		String shapeMessengersJSON = getJerseyClient().resource(gosEndpoint)
				 .path("/ShapeManagement/findWithin")
				 .accept(MediaType.APPLICATION_JSON)
				 .post(String.class, formData);
		TypeReference<List<Shape>> mappingType = new TypeReference<List<Shape>>() {};
		return getMapper().readValue(shapeMessengersJSON, mappingType);
	}
	
	
	public boolean insertShapes(String gosEndpoint, Collection<Shape> shapes) throws Exception{
		
		int chunkSize = 5000; //please do not change
		
		String layerID = null;
		
		String tempDir = System.getProperty("java.io.tmpdir");
		if(tempDir==null || tempDir.isEmpty())
			tempDir = "/tmp";
		
		Iterator<Shape> iter = shapes.iterator();
		
		long iterIdx = 0;
		
		DB filelistDB = DBMaker.tempFileDB().closeOnJvmShutdown().fileMmapEnableIfSupported().fileDeleteAfterClose().make();
		List<Object> shapesSublist = filelistDB.indexTreeList("shapesSublist:"+UUID.randomUUID()).create();
		
		while(iter.hasNext()) {
			
			Shape sh = iter.next();
			if(sh!=null) 
				layerID = sh.getLayerID().toString();
			
			shapesSublist.add(sh);
			
			if(iterIdx%chunkSize==0 && !shapesSublist.isEmpty()){
				//send shapeSublist
				try{
					MultivaluedMap formData = new MultivaluedMapImpl();
					formData.add(super.HEADER_AUTHENTICATION_PARAM_NAME, authenticationStr);
//					formData.add("shapeMessengerListJSON", getGoogleMapper().toJson(shapesSublist.parallelStream().map(shape -> { return new ShapeMessenger((Shape)shape);}).collect(Collectors.toList())));
					formData.add("shapeMessengerListJSON", getMapper().writeValueAsString(shapesSublist.parallelStream().map(shape -> { return new ShapeMessenger((Shape)shape);}).collect(Collectors.toList())));
					ClientResponse resp = getJerseyClient().resource(gosEndpoint)
						.path("/ShapeManagement/insertShapes")
			    		.accept(MediaType.APPLICATION_JSON)
			    		.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
			    		.post(ClientResponse.class, formData);
					
					if(resp.getStatus()!=201){
						if(layerID!=null)
							try{ new File(tempDir+"/"+layerID).delete(); } catch(Exception ex){ex.printStackTrace();}
						if(!filelistDB.isClosed())
							filelistDB.close();
						return false; //one chunk failed, so caller of the function will rollback 
					}
				}
				catch(Exception e){ //one chunk failed, so caller of the function will rollback 
					e.printStackTrace();
					if(layerID!=null)
						try{ new File(tempDir+"/"+layerID).delete(); } catch(Exception ex){ex.printStackTrace();}
					if(!filelistDB.isClosed())
						filelistDB.close();
					return false; 
				}
				//empty shapeSublist
				shapesSublist.clear();
				
			}
			
			iterIdx++;
			
		}
		
		if(!shapesSublist.isEmpty()){ //if there are any remaining shapes (i.e. happens 99,99%), send them too
			//send shapeSublist
			try{
				MultivaluedMap formData = new MultivaluedMapImpl();
				formData.add(super.HEADER_AUTHENTICATION_PARAM_NAME, authenticationStr);
//				formData.add("shapeMessengerListJSON", getGoogleMapper().toJson(shapesSublist.parallelStream().map(shape -> { return new ShapeMessenger((Shape)shape);}).collect(Collectors.toList())));
				formData.add("shapeMessengerListJSON", getMapper().writeValueAsString(shapesSublist.parallelStream().map(shape -> { return new ShapeMessenger((Shape)shape);}).collect(Collectors.toList())));
				ClientResponse resp = getJerseyClient().resource(gosEndpoint)
					.path("/ShapeManagement/insertShapes")
		    		.accept(MediaType.APPLICATION_JSON)
		    		.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
		    		.post(ClientResponse.class, formData);
				
				if(resp.getStatus()!=201){
					if(layerID!=null)
						try{ new File(tempDir+"/"+layerID).delete(); } catch(Exception ex){ex.printStackTrace();}
					if(!filelistDB.isClosed())
						filelistDB.close();
					return false; //one chunk failed, so caller of the function will rollback 
				}
			}
			catch(Exception e){
				e.printStackTrace();
				if(layerID!=null)
					try{ new File(tempDir+"/"+layerID).delete(); } catch(Exception ex){ex.printStackTrace();}
				if(!filelistDB.isClosed())
					filelistDB.close();
				return false; //one chunk failed, so caller of the function will rollback 
			}
			//empty shapeSublist
			shapesSublist.clear();
		}
		
		try{ new File(tempDir+"/"+layerID).delete(); } catch(Exception ex){ex.printStackTrace();}
		
		
//		shapes.clear();
		
		//if reached at this point, means that everything's ok
		return true;
		
	}
	
	public boolean insertShape(String gosEndpoint, Shape shape) throws Exception{
		MultivaluedMap formData = new MultivaluedMapImpl();
		formData.add(super.HEADER_AUTHENTICATION_PARAM_NAME, authenticationStr);
		formData.add("shapeMessengerJSON", getMapper().writeValueAsString(new ShapeMessenger(shape)));
		ClientResponse resp = getJerseyClient().resource(gosEndpoint)
			.path("/ShapeManagement/insertShape")
    		.accept(MediaType.APPLICATION_JSON)
    		.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
    		.post(ClientResponse.class, formData);
		return resp.getStatus()==201;
	}
	
	public boolean updateShape(String gosEndpoint, Shape shape) throws Exception{
		MultivaluedMap formData = new MultivaluedMapImpl();
		formData.add(super.HEADER_AUTHENTICATION_PARAM_NAME, authenticationStr);
		formData.add("shapeMessengerJSON", getMapper().writeValueAsString(new ShapeMessenger(shape)));
		ClientResponse resp = getJerseyClient().resource(gosEndpoint)
			.path("/ShapeManagement/updateShape")
    		.accept(MediaType.APPLICATION_JSON)
    		.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
    		.post(ClientResponse.class, formData);
		return resp.getStatus()==201;
	}
	
	
	
	public List<Shape> getShapesOfLayerID(String gosEndpoint, String layerID) throws Exception{
		String shapeMessengerListJSON = getJerseyClient().resource(gosEndpoint)
						 .path("/ShapeManagement/shapesOfLayerID/"+layerID)
						 .header(super.HEADER_AUTHENTICATION_PARAM_NAME, authenticationStr)
						 .accept(MediaType.APPLICATION_JSON)
						 .get(String.class);
		TypeReference<ArrayList<ShapeMessenger>> mappingType = new TypeReference<ArrayList<ShapeMessenger>>() {};
		return Transforming.fromShapeMessenger((List<ShapeMessenger>)getMapper().readValue(shapeMessengerListJSON, mappingType));
	}
	
	
	public Set<String> getAttributeValuesOfShapesByLayer(String gosEndpoint, String layerID, Attribute attribute) throws Exception{
		MultivaluedMap formData = new MultivaluedMapImpl();
		formData.add(super.HEADER_AUTHENTICATION_PARAM_NAME, authenticationStr);
		formData.add("attributeJSON", getMapper().writeValueAsString(attribute));
		ClientResponse resp = getJerseyClient().resource(gosEndpoint)
			.path("/ShapeManagement/getAttributeValuesOfShapesByLayer/"+layerID)
    		.accept(MediaType.APPLICATION_JSON)
    		.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
    		.post(ClientResponse.class, formData);
		
		TypeReference<Set<String>> mappingType = new TypeReference<Set<String>>() {};
		return (Set<String>)getMapper().readValue(resp.getEntity(String.class), mappingType);
		
	}
	
	
	public boolean deleteShapesOfLayer(String gosEndpoint, String layerID){
		ClientResponse resp = getJerseyClient().resource(gosEndpoint)
						 .path("/ShapeManagement/deleteLayer/"+layerID)
						 .header(super.HEADER_AUTHENTICATION_PARAM_NAME, authenticationStr)
						 .accept(MediaType.APPLICATION_JSON)
						 .delete(ClientResponse.class);
		return (resp.getStatus()==200);
	}
	
	
	public long countShapesOfLayer(String gosEndpoint, String layerID){
		return getJerseyClient().resource(gosEndpoint)
				 .path("/ShapeManagement/countShapesOfLayer/"+layerID)
				 .header(super.HEADER_AUTHENTICATION_PARAM_NAME, authenticationStr)
				 .accept(MediaType.APPLICATION_JSON)
				 .get(Long.class)
				 .longValue();
	}
	
	
	
	public boolean applyOnView(String gosEndpoint, String statement) throws Exception{
		MultivaluedMap formData = new MultivaluedMapImpl();
		formData.add(super.HEADER_AUTHENTICATION_PARAM_NAME, authenticationStr);
		formData.add("statement", statement);
		ClientResponse resp = getJerseyClient().resource(gosEndpoint)
				.path("/ShapeManagement/applyOnView")
	    		.accept(MediaType.APPLICATION_JSON)
	    		.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
	    		.post(ClientResponse.class, formData);
		return (resp.getStatus()==Status.CREATED.getStatusCode());
	}
	
	
	
}
