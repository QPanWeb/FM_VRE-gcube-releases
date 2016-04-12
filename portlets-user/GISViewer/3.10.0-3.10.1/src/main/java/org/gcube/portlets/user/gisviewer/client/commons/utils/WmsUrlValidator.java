package org.gcube.portlets.user.gisviewer.client.commons.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.gwt.core.shared.GWT;



/**
 * The Class WmsUrlValidator.
 *
 * @author Francesco Mangiacrapa francesco.mangiacrapa@isti.cnr.it
 * Dec 4, 2015
 */
public class WmsUrlValidator {

	private HashMap<String, String> parametersValue = new HashMap<String, String>();
	private String wmsRequest;
	private String baseWmsServiceUrl;
	private String wmsParameters;
	private String wmsNotStandardParameters = "";
	private Map<String, String> mapWmsNotStandardParams;

	/**
	 * Instantiates a new wms url validator.
	 *
	 * @param wmsRequest the wms request
	 */
	public WmsUrlValidator(String wmsRequest){
		this.wmsRequest = wmsRequest;
		int indexStart = wmsRequest.indexOf("?");
		if(indexStart==-1){
			this.baseWmsServiceUrl = wmsRequest;
			this.wmsParameters = null;
		}else{
			this.baseWmsServiceUrl=wmsRequest.substring(0, indexStart);
			this.baseWmsServiceUrl.trim();
			this.wmsParameters = wmsRequest.substring(indexStart+1, this.wmsRequest.length());
			this.wmsParameters.trim();
		}
	}

	/**
	 * Parses the wms request.
	 *
	 * @param returnEmptyParameter the return empty parameter
	 * @param fillEmptyParameterAsDefaultValue the fill empty parameter as default
	 * @return
	 * @throws Exception
	 */
	public String parseWmsRequest(boolean returnEmptyParameter, boolean fillEmptyParameterAsDefaultValue) throws Exception{

		if(wmsParameters==null || wmsParameters.isEmpty()){
			String msg = "IT IS NOT POSSIBLE TO PARSE WMS URL, 'WMS PARAMETERS' not found!";
			GWT.log(msg);
			throw new Exception(msg);
		}

		for (WmsParameters wmsParam : WmsParameters.values()) {

			if(wmsParam.equals(WmsParameters.BBOX)){
				String value = validateValueOfParameter(WmsParameters.BBOX, wmsParameters, fillEmptyParameterAsDefaultValue);
				parametersValue.put(wmsParam.getParameter(), value);
			}

			if(wmsParam.equals(WmsParameters.FORMAT)){
				String value = validateValueOfParameter(WmsParameters.FORMAT, wmsParameters, fillEmptyParameterAsDefaultValue);
				parametersValue.put(wmsParam.getParameter(), value);
			}

			if(wmsParam.equals(WmsParameters.HEIGHT)){
				String value =  validateValueOfParameter( WmsParameters.HEIGHT, wmsParameters, fillEmptyParameterAsDefaultValue);
				parametersValue.put(wmsParam.getParameter(), value);
			}

			if(wmsParam.equals(WmsParameters.CRS)){
				String crs = validateValueOfParameter(WmsParameters.CRS, wmsParameters, fillEmptyParameterAsDefaultValue);
				parametersValue.put(wmsParam.getParameter(), crs);
			}

			if(wmsParam.equals(WmsParameters.WIDTH)){
				String value = validateValueOfParameter(WmsParameters.WIDTH, wmsParameters, fillEmptyParameterAsDefaultValue);
				parametersValue.put(wmsParam.getParameter(), value);
			}

			if(wmsParam.equals(WmsParameters.REQUEST)){
				String value = validateValueOfParameter(WmsParameters.REQUEST, wmsParameters, fillEmptyParameterAsDefaultValue);
				parametersValue.put(wmsParam.getParameter(), value);
			}

			if(wmsParam.equals(WmsParameters.SERVICE)){
				String value = validateValueOfParameter(WmsParameters.SERVICE, wmsParameters,fillEmptyParameterAsDefaultValue);
				parametersValue.put(wmsParam.getParameter(), value);
			}

			if(wmsParam.equals(WmsParameters.SRS)){
				String value = validateValueOfParameter(WmsParameters.SRS, wmsParameters, fillEmptyParameterAsDefaultValue);
				parametersValue.put(wmsParam.getParameter(), value);
			}

			if(wmsParam.equals(WmsParameters.STYLES)){
				String styles = validateValueOfParameter(WmsParameters.STYLES, wmsParameters, fillEmptyParameterAsDefaultValue);
				parametersValue.put(wmsParam.getParameter(), styles);
			}

			if(wmsParam.equals(WmsParameters.VERSION)){
				String version = validateValueOfParameter(WmsParameters.VERSION, wmsParameters, fillEmptyParameterAsDefaultValue);
				parametersValue.put(wmsParam.getParameter(), version);
			}

			if(wmsParam.equals(WmsParameters.LAYERS)){
				String layers = validateValueOfParameter(WmsParameters.LAYERS, wmsParameters, fillEmptyParameterAsDefaultValue);
				parametersValue.put(wmsParam.getParameter(), layers);
			}
		}

		String parsedWmsRequest = baseWmsServiceUrl+"?";

		String[] params = wmsParameters.split("&");

		//CREATING MAP TO RETURN WMS PARAMETERS NOT STANDARD
		mapWmsNotStandardParams = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

		for (String param : params) {
			String key = param.substring(0, param.indexOf("="));
			String value = param.substring(param.indexOf("=")+1, param.length());
			mapWmsNotStandardParams.put(key, value);
		}

		//CREATE WMS REQUEST
		for (String key : parametersValue.keySet()) {

			String value = parametersValue.get(key);
			if(returnEmptyParameter && value.isEmpty()){
				parsedWmsRequest+=key+"="+value;
				//not add the parameter
			}else{
				parsedWmsRequest+=key+"="+value;
				parsedWmsRequest+="&";
				String exist = mapWmsNotStandardParams.get(key);

				if(exist!=null)
					mapWmsNotStandardParams.remove(key); //REMOVE WMS STANDARD PARAMETER FROM MAP
			}
		}

		for (String key : mapWmsNotStandardParams.keySet()) {
			wmsNotStandardParameters+=key+"="+mapWmsNotStandardParams.get(key) + "&";
		}

		if(wmsNotStandardParameters.length()>0)
			wmsNotStandardParameters = wmsNotStandardParameters.substring(0, wmsNotStandardParameters.length()-1); //REMOVE LAST &

		GWT.log("wmsNotStandardParameters: "+wmsNotStandardParameters);

		String fullWmsUrlBuilded;

		if(!wmsNotStandardParameters.isEmpty()){
			fullWmsUrlBuilded = parsedWmsRequest +  wmsNotStandardParameters; //remove last &
			GWT.log("full wms url builded + not wms standard parameters: "+fullWmsUrlBuilded);
		}else{
			fullWmsUrlBuilded = parsedWmsRequest.substring(0, parsedWmsRequest.length()-1); //remove last &
			GWT.log("full wms url builded: "+fullWmsUrlBuilded);
		}

		return fullWmsUrlBuilded;
	}


	/**
	 * @return the wmsRequest
	 */
	public String getWmsRequest() {
		return wmsRequest;
	}

	/**
	 * @return the baseWmsServiceUrl
	 */
	public String getBaseWmsServiceUrl() {
		return baseWmsServiceUrl;
	}

	/**
	 * Gets the wms not standard parameters.
	 *
	 * @return the wms not standard parameters
	 */
	public String getWmsNotStandardParameters() {
		return wmsNotStandardParameters;
	}


	/**
	 * Gets the value of parsed wms parameter.
	 *
	 * @param parameter the parameter
	 * @return the value of parsed wms parameter parsed from wms request.
	 */
	public String getValueOfParsedWMSParameter(WmsParameters parameter){
		return parametersValue.get(parameter.getParameter());
	}


	/**
	 * Validate value of parameter.
	 *
	 * @param wmsParam the wms param
	 * @param valueOfParameter the value of parameter
	 * @param fillEmptyParameterAsDefaultValue the fill empty parameter as default value
	 * @return the string
	 */
	public static String validateValueOfParameter(WmsParameters wmsParam, String valueOfParameter, boolean fillEmptyParameterAsDefaultValue){

		try{

			String value = getValueOfParameter(wmsParam, valueOfParameter);

			if(fillEmptyParameterAsDefaultValue && (value==null || value.isEmpty())){
				GWT.log("setting empty value for parameter: "+wmsParam.getParameter() +", as default value: "+wmsParam.getValue());
				value = wmsParam.getValue();
			}
			return value;
		}catch(Exception e){
			//silent
			return null;
		}
	}

	/**
	 * Gets the value of parameter.
	 *
	 * @param wmsParam the wms param
	 * @param wmsRequestParamaters the url wms parameters
	 * @return the value of parameter or null if parameter not exists
	 */
	public static String getValueOfParameter(WmsParameters wmsParam, String wmsRequestParamaters) {
//		logger.trace("finding: "+wmsParam +" into "+url);
		int index = wmsRequestParamaters.toLowerCase().indexOf(wmsParam.getParameter().toLowerCase());
//		logger.trace("start index of "+wmsParam+ " is: "+index);
		String value = "";
		if(index > -1){

			int start = index + wmsParam.getParameter().length()+1; //add +1 for char '='
			String sub = wmsRequestParamaters.substring(start, wmsRequestParamaters.length());
			int indexOfSeparator = sub.indexOf("&");
			int end = indexOfSeparator!=-1?indexOfSeparator:sub.length();
			value = sub.substring(0, end);
		}else
			return null;

//		logger.trace("return value: "+value);
		return value;
	}


	/**
	 * Sets the value of parameter.
	 *
	 * @param wmsParam the wms param
	 * @param wmsRequestParameters the wms url parameters
	 * @param newValue the new value
	 * @param addIfNotExists add the parameter if not exists
	 * @return the string
	 */
	public static String setValueOfParameter(WmsParameters wmsParam, String wmsRequestParameters, String newValue, boolean addIfNotExists){
		String toLowerWmsUrlParameters = wmsRequestParameters.toLowerCase();
		String toLowerWmsParam = wmsParam.getParameter().toLowerCase();

		int index = toLowerWmsUrlParameters.indexOf(toLowerWmsParam+"="); //+ "=" SECURE TO BE PARAMETER
//		logger.trace("start index of "+wmsParam+ " is: "+index);
		if(index > -1){
			int indexStartValue = index + toLowerWmsParam.length()+1; //add +1 for char '='
			int indexOfSeparator = toLowerWmsUrlParameters.indexOf("&", indexStartValue); //GET THE FIRST "&" STARTING FROM INDEX VALUE
//			logger.trace("indexOfSeparator index of "+wmsParam+ " is: "+indexOfSeparator);
			int indexEndValue = indexOfSeparator!=-1?indexOfSeparator:toLowerWmsUrlParameters.length();
//			logger.trace("end: "+indexEndValue);
			return wmsRequestParameters.substring(0, indexStartValue) + newValue +wmsRequestParameters.substring(indexEndValue, wmsRequestParameters.length());
		}else if (addIfNotExists){
			wmsRequestParameters+="&"+wmsParam.getParameter()+"="+newValue;
		}
//		logger.trace("return value: "+value);
		return wmsRequestParameters;
	}

	/**
	 * Gets the styles as list.
	 *
	 * @return the styles as list
	 */
	public List<String> getStylesAsList() {

		List<String> listStyles = new ArrayList<String>();
		String styles = getValueOfParsedWMSParameter(WmsParameters.STYLES);

		if(styles!=null && !styles.isEmpty()){

			String[] arrayStyle = styles.split(",");
			for (String style : arrayStyle) {
				if(style!=null && !style.isEmpty())
					listStyles.add(style);
			}
		}
		return listStyles;
	}

	/**
	 * Gets the map wms not standard params.
	 *
	 * @return the mapWmsNotStandardParams
	 */
	public Map<String, String> getMapWmsNotStandardParams() {
		return mapWmsNotStandardParams;
	}


	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {


//		String baseGeoserverUrl = "http://repoigg.services.iit.cnr.it:8080/geoserver/IGG/ows";
//		String baseGeoserverUrl = "http://www.fao.org/figis/geoserver/species";
//		String fullPath = "http://www.fao.org/figis/geoserver/species?SERVICE=WMS&BBOX=-176.0,-90.0,180.0,90&styles=Species_prob, puppa&layers=layerName&FORMAT=image/gif";
//		String fullPath = "http://repoigg.services.iit.cnr.it:8080/geoserver/IGG/ows?service=wms&version=1.1.0&request=GetMap&layers==IGG:area_temp_1000&width=676&height=330&srs=EPSG:4326&crs=EPSG:4326&format=application/openlayers&bbox=-85.5,-180.0,90.0,180.0";
//		String baseGeoserverUrl = "http://thredds-d-d4s.d4science.org/thredds/wms/public/netcdf/test20.nc";
//		String fullPath = "http://thredds-d-d4s.d4science.org/thredds/wms/public/netcdf/test20.nc?service=wms&version=1.3.0&request=GetMap&layers=analyzed_field&bbox=-85.0,-180.0,85.0,180.0&styles=&width=640&height=480&srs=EPSG:4326&CRS=EPSG:4326&format=image/png&COLORSCALERANGE=auto";
//		WmsUrlValidator validator = new WmsUrlValidator(baseGeoserverUrl, fullPath , "", false);
//		logger.trace("base wms service url: "+validator.getBaseWmsServiceUrl());
//		logger.trace("layer name: "+validator.getLayerName());
//		logger.trace("full wms url: "+validator.getFullWmsUrlRequest(false, true));
//		logger.trace("style: "+validator.getStyles());
//		logger.trace("not standard parameter: "+validator.getWmsNotStandardParameters());
//		String[] arrayStyle = validator.getStyles().split(",");
//
//		if(arrayStyle!=null && arrayStyle.length>0){
//
//			for (String style : arrayStyle) {
//				if(style!=null && !style.isEmpty())
//
//					System.out.println("Style: "+style.trim());
//			}
//		}
//
		String fullPath = "http://thredds-d-d4s.d4science.org/thredds/wms/public/netcdf/test20.nc?service=wms&version=1.3.0&request=GetMap&layers=analyzed_field&styles=&width=640&height=480&srs=EPSG:4326&CRS=EPSG:4326&format=image/png&COLORSCALERANGE=auto&bbox=-85.0,-180.0,85.0,180.0";
//		fullPath = WmsUrlValidator.setValueOfParameter(WmsParameters.STYLES, fullPath, "123", true);
//
	}

}
