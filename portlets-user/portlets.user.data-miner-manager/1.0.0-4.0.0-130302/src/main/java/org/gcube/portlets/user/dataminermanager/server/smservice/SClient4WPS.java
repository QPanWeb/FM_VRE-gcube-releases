package org.gcube.portlets.user.dataminermanager.server.smservice;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.CapabilitiesDocument;
import net.opengis.wps.x100.ComplexDataType;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.ExecuteResponseDocument.ExecuteResponse.ProcessOutputs;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessBriefType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ResponseDocumentType;
import net.opengis.wps.x100.StatusType;
import net.opengis.wps.x100.SupportedComplexDataInputType;
import net.opengis.wps.x100.impl.ExecuteResponseDocumentImpl;

import org.apache.xmlbeans.XmlString;
import org.gcube.portlets.user.dataminermanager.client.bean.ComputationStatus;
import org.gcube.portlets.user.dataminermanager.client.bean.ComputationStatus.Status;
import org.gcube.portlets.user.dataminermanager.client.bean.Operator;
import org.gcube.portlets.user.dataminermanager.client.bean.OperatorCategory;
import org.gcube.portlets.user.dataminermanager.client.bean.OperatorsClassification;
import org.gcube.portlets.user.dataminermanager.server.is.InformationSystemUtils;
import org.gcube.portlets.user.dataminermanager.server.smservice.wps.ProcessInformations;
import org.gcube.portlets.user.dataminermanager.server.smservice.wps.ResponseWPS;
import org.gcube.portlets.user.dataminermanager.server.smservice.wps.StatWPSClientSession;
import org.gcube.portlets.user.dataminermanager.server.smservice.wps.WPS2SM;
import org.gcube.portlets.user.dataminermanager.server.smservice.wps.computationsvalue.ComputationValueBuilder;
import org.gcube.portlets.user.dataminermanager.server.storage.StorageUtil;
import org.gcube.portlets.user.dataminermanager.server.util.ServiceCredential;
import org.gcube.portlets.user.dataminermanager.shared.Constants;
import org.gcube.portlets.user.dataminermanager.shared.data.OutputData;
import org.gcube.portlets.user.dataminermanager.shared.data.computations.ComputationData;
import org.gcube.portlets.user.dataminermanager.shared.data.computations.ComputationId;
import org.gcube.portlets.user.dataminermanager.shared.data.computations.ComputationValue;
import org.gcube.portlets.user.dataminermanager.shared.data.output.FileResource;
import org.gcube.portlets.user.dataminermanager.shared.data.output.ImageResource;
import org.gcube.portlets.user.dataminermanager.shared.data.output.MapResource;
import org.gcube.portlets.user.dataminermanager.shared.data.output.ObjectResource;
import org.gcube.portlets.user.dataminermanager.shared.data.output.Resource;
import org.gcube.portlets.user.dataminermanager.shared.exception.DataMinerServiceException;
import org.gcube.portlets.user.dataminermanager.shared.parameters.ObjectParameter;
import org.gcube.portlets.user.dataminermanager.shared.parameters.Parameter;
import org.gcube.portlets.user.dataminermanager.shared.parameters.TabularListParameter;
import org.n52.wps.client.ExecuteRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Client 4 WPS
 * 
 * 
 * @author Giancarlo Panichi email: <a
 *         href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a>
 *
 */
public class SClient4WPS extends SClient {

	private static final long serialVersionUID = 1909871837115147159L;
	private static Logger logger = LoggerFactory.getLogger(SClient4WPS.class);
	// private static final int OPERATOR_BRIEF_DESCRIPTION_MAX_LENGHT = 170;
	private static final String OTHERS = "OTHERS";

	private String wpsToken;
	private String wpsUser;
	// private String wpsServiceURL;
	private String wpsProcessingServlet;
	private String wpsCancelComputationServlet;

	private ProcessDescriptionType[] processesDescriptionType;
	private ProcessBriefType[] processesBriefs;

	private HashMap<String, ProcessInformations> process;
	private HashMap<ComputationId, ProcessInformations> runningProcess;

	private StatWPSClientSession wpsClient;

	public SClient4WPS(ServiceCredential serviceCredential) throws Exception {
		super();
		process = new HashMap<>();
		runningProcess = new HashMap<>();
		if (serviceCredential == null) {
			logger.error("Error credetials are null!");
			throw new Exception("Error credetials are null!");
		} else {
			String token = serviceCredential.getToken();
			if (token == null || token.isEmpty()) {
				logger.error("Error authorization token invalid: " + token);
				throw new Exception("Error authorization token invalid: "
						+ token);
			} else {
				wpsToken = token;
			}

			String userName = serviceCredential.getUsername();
			if (userName == null || userName.isEmpty()) {
				logger.error("Error invalid user name: " + userName);
				throw new Exception("Error invalid user name: " + userName);
			} else {
				wpsUser = userName;
			}
			List<String> serviceAddress = InformationSystemUtils
					.retrieveServiceAddress(
							Constants.DATAMINER_SERVICE_CATEGORY,
							Constants.DATA_MINER_SERVICE_NAME,
							serviceCredential.getScope());
			logger.debug("Service Address retrieved:" + serviceAddress);
			if (serviceAddress == null || serviceAddress.size() < 1) {
				logger.error("No DataMiner service address available!");
				throw new Exception("No DataMiner service address available!");
			} else {
				logger.info("DataMiner service address found: "
						+ serviceAddress.get(0));
				wpsProcessingServlet = serviceAddress.get(0);

				int wpsWebProcessingServiceIndex = wpsProcessingServlet
						.indexOf(Constants.WPSWebProcessingService);
				if (wpsWebProcessingServiceIndex > 0) {
					String wpsServiceUrl = wpsProcessingServlet.substring(0,
							wpsWebProcessingServiceIndex);
					wpsCancelComputationServlet = wpsServiceUrl
							+ Constants.WPSCancelComputationServlet;
					logger.debug("Cancel computation servlet: "
							+ wpsCancelComputationServlet);

				} else {
					logger.error("Cancel computation servlet not available!");
					throw new Exception(
							"Cancel computation servlet not available!");
				}

			}

		}

	}

	private StatWPSClientSession createWPSClientSession() {
		if (wpsClient == null) {
			wpsClient = new StatWPSClientSession(wpsUser, wpsToken);
			logger.debug("Created StatWPSClientSession");
			return wpsClient;
		} else {
			logger.debug("Use already created StatWPSClientSession");
			return wpsClient;
		}
	}

	@Override
	public List<OperatorsClassification> getOperatorsClassifications()
			throws Exception {

		LinkedHashMap<String, String> operatorsToCategoriesMap = new LinkedHashMap<>();
		LinkedHashMap<String, List<Operator>> categoriesToOperatorsMap = new LinkedHashMap<>();

		requestCapability();
		if (processesBriefs == null || processesDescriptionType == null) {
			throw new Exception("Algorithms WPS not available!");
		}

		for (ProcessBriefType processBrief : processesBriefs) {
			String categoryTitle = processBrief.getTitle().getStringValue();
			String categoryName;
			if (categoryTitle == null || categoryTitle.isEmpty()) {
				categoryName = OTHERS;
			} else {
				String[] categorySplitted = categoryTitle.split(":");
				if (categorySplitted.length < 1) {
					categoryName = OTHERS;
				} else {
					categoryName = categorySplitted[0];
				}
			}
			operatorsToCategoriesMap.put(processBrief.getIdentifier()
					.getStringValue(), categoryName);
		}

		String briefDescription;
		for (ProcessBriefType processDescriptionType : processesDescriptionType) {
			briefDescription = processDescriptionType.getAbstract()
					.getStringValue();

			String categoryName = operatorsToCategoriesMap
					.get(processDescriptionType.getIdentifier()
							.getStringValue());
			if (categoryName == null || categoryName.isEmpty()) {
				categoryName = OTHERS;
			}

			List<Operator> listOperators = categoriesToOperatorsMap
					.get(categoryName);
			if (listOperators == null) {
				listOperators = new ArrayList<>();
			}

			listOperators
					.add(new Operator(processDescriptionType.getIdentifier()
							.getStringValue(), processDescriptionType
							.getTitle().getStringValue(), briefDescription,
							processDescriptionType.getAbstract()
									.getStringValue(), null));

			categoriesToOperatorsMap.put(categoryName, listOperators);

		}

		List<OperatorCategory> categories = new ArrayList<>();
		List<Operator> operators = new ArrayList<>();

		Comparator<OperatorCategory> categoriesComparator = new Comparator<OperatorCategory>() {
			public int compare(OperatorCategory c1, OperatorCategory c2) {
				return c1.getName().compareTo(c2.getName()); // use your logic
			}
		};

		Comparator<Operator> operatorsComparator = new Comparator<Operator>() {
			public int compare(Operator c1, Operator c2) {
				return c1.getName().compareTo(c2.getName()); // use your logic
			}
		};

		for (String categoryName : categoriesToOperatorsMap.keySet()) {
			OperatorCategory category = new OperatorCategory(categoryName,
					categoryName, categoryName);
			List<Operator> listOperators = categoriesToOperatorsMap
					.get(categoryName);
			for (Operator operator : listOperators) {
				operator.setCategory(category);
			}
			Collections.sort(listOperators, operatorsComparator);
			category.setOperators(listOperators);
			operators.addAll(listOperators);
			categories.add(category);
		}

		Collections.sort(operators, operatorsComparator);
		Collections.sort(categories, categoriesComparator);

		List<OperatorsClassification> operatorsClass = new ArrayList<>();

		OperatorsClassification op = new OperatorsClassification(
				Constants.UserClassificationName, categories, operators);

		operatorsClass.add(op);

		//logger.debug("OperatorClass: " + operatorsClass);
		return operatorsClass;
	}

	private ProcessInformations describeProcess(String processId)
			throws Exception {
		return describeProcess(processId, null);
	}

	private ProcessInformations describeProcess(String processId,
			URL processDescriptionURL) throws Exception {
		if (process.containsKey(processId)) {
			return process.get(processId);
		}

		StatWPSClientSession wpsClient = null;
		try {
			wpsClient = createWPSClientSession();

			logger.debug("Describe Process WPS URL: " + wpsProcessingServlet);
			ProcessDescriptionType processDescription = null;
			for (int k = 0; k <= 3; k++) {
				try {
					processDescription = wpsClient.getProcessDescription(
							wpsProcessingServlet, processId);
				} catch (Throwable e) {
					logger.error("Error getProcessDescription for process "
							+ processId + " with WPS URL: "
							+ wpsProcessingServlet);
					if (k == 3)
						throw e;
				}
				if (processDescription != null)
					break;
			}

			ProcessInformations processInformations = new ProcessInformations(
					processDescription);

			if (processDescriptionURL != null)
				processDescription.set(XmlString.Factory
						.parse(processDescriptionURL));

			logger.debug("ProcessDescription: " + processDescription);

			InputDescriptionType[] inputList = processDescription
					.getDataInputs().getInputArray();
			logger.debug("WPSClient->Fetching Inputs");
			for (InputDescriptionType input : inputList) {
				logger.debug("WPSClient->Input: " + input);
			}

			OutputDescriptionType[] outputList = processDescription
					.getProcessOutputs().getOutputArray();
			logger.debug("WPSClient->Fetching Outputs");
			for (OutputDescriptionType output : outputList) {
				logger.debug("WPSClient->Output: " + output);
			}

			processInformations.setInputs(inputList);
			processInformations.setOutputs(outputList);

			process.put(processId, processInformations);

			return processInformations;
		} catch (Throwable e) {
			logger.error(e.getLocalizedMessage());
			e.printStackTrace();
			throw new Exception(e.getLocalizedMessage(), e);
		} finally {
			wpsClient.disconnect(wpsProcessingServlet);
		}
	}

	private void requestCapability() throws Exception {
		StatWPSClientSession wpsClient = null;
		processesDescriptionType = null;
		processesBriefs = null;

		try {
			wpsClient = createWPSClientSession();

			wpsClient.connect(wpsProcessingServlet);

			processesDescriptionType = wpsClient
					.getAllProcessDescriptions(wpsProcessingServlet);

			CapabilitiesDocument capabilitiesDocument = wpsClient
					.getWPSCaps(wpsProcessingServlet);

			processesBriefs = capabilitiesDocument.getCapabilities()
					.getProcessOfferings().getProcessArray();
			return;

		} catch (Throwable e) {
			logger.error("RequestCapability(): "+e.getLocalizedMessage());
			e.printStackTrace();
			throw new Exception(e.getLocalizedMessage(), e);
		} finally {
			if (wpsClient != null) {
				wpsClient.disconnect(wpsProcessingServlet);
			}
		}

	}

	private String executeProcessAsync(ExecuteRequestBuilder executeBuilder,
			ProcessDescriptionType processDescription) throws Exception {
		StatWPSClientSession wpsClient = null;
		try {
			try {

				OutputDescriptionType[] odts = processDescription
						.getProcessOutputs().getOutputArray();
				for (OutputDescriptionType odt : odts) {
					// executeBuilder.setMimeTypeForOutput("text/xml",
					// "result");
					if (odt.isSetComplexOutput())
						executeBuilder.setMimeTypeForOutput("text/xml", odt
								.getIdentifier().getStringValue());
				}
			} catch (Exception e) {
				logger.debug("Execute Process-> Warning, no xml structured objects will be provided: "
						+ e.getLocalizedMessage());
				e.printStackTrace();
			}
			// executeBuilder.setSchemaForOutput("http://schemas.opengis.net/gml/3.1.1/base/feature.xsd",
			// "result");

			ExecuteDocument execute = executeBuilder.getExecute();
			execute.getExecute().setService("WPS");
			// System.out.println("RESPONSE FORM:"+execute.getExecute().getResponseForm());
			wpsClient = createWPSClientSession();
			wpsClient.connect(wpsProcessingServlet);
			logger.debug("Sending: " + execute);
			if (execute.getExecute().getResponseForm() != null) {
				ResponseDocumentType documentType = execute.getExecute()
						.getResponseForm().getResponseDocument();
				documentType.setStoreExecuteResponse(true);
				documentType.setStatus(true);
				documentType.setLineage(false);
				execute.getExecute().getResponseForm()
						.setResponseDocument(documentType);
			}
			Object responseObject = wpsClient.execute(wpsProcessingServlet,
					execute);
			String processLocation = null;
			Date creationData = null;
			if (responseObject != null) {
				if (responseObject instanceof ExecuteResponseDocumentImpl) {
					ExecuteResponseDocumentImpl executeResponseDocumentImpl = ((ExecuteResponseDocumentImpl) responseObject);
					processLocation = executeResponseDocumentImpl
							.getExecuteResponse().getStatusLocation();
					creationData = executeResponseDocumentImpl
							.getExecuteResponse().getStatus().getCreationTime()
							.getTime();
				} else {
					throw new Exception(
							"Invalid response from service, "
									+ "response isn't instance of ExecuteResponseDocumentImpl, class is "
									+ responseObject.getClass());
				}
			} else {
				throw new Exception("Invalid Response from service, "
						+ responseObject);
			}
			logger.debug("Retrieved: [ProcessLocation=" + processLocation
					+ ", CreationDate=" + creationData + "]");
			return processLocation;

		} catch (Throwable e) {
			logger.error("ExecuteProcessAsync: " + e.getLocalizedMessage());
			e.printStackTrace();
			throw new Exception(e.getLocalizedMessage(), e);
		} finally {
			try {
				if (wpsClient != null)
					wpsClient.disconnect(wpsProcessingServlet);
			} catch (Exception e) {
				logger.debug("Problems in wps disconnect! "
						+ e.getLocalizedMessage());
			}
		}
	}

	private ProcessOutputs retrieveProcessResult(String processLocation)
			throws Exception {
		StatWPSClientSession wpsClient = null;
		try {
			logger.debug("RetrieveProcessResult: " + processLocation);
			wpsClient = createWPSClientSession();
			// wpsClient.connectForMonitoring(webProcessingService);
			// wpsClient.connect(url)

			Object responseObject = null;
			if (processLocation != null && processLocation.length() > 0)
				responseObject = wpsClient.executeViaGET(processLocation, "");
			else
				throw new Exception("Process Location is null!");

			logger.debug("Response:\n" + responseObject);
			return ((ExecuteResponseDocument) responseObject)
					.getExecuteResponse().getProcessOutputs();

		} catch (Throwable e) {
			logger.debug("RetrieveProcessResult: " + e.getLocalizedMessage());
			e.printStackTrace();
			throw new Exception(e.getLocalizedMessage(), e);
		} finally {
			wpsClient.disconnect(wpsProcessingServlet);
		}
	}

	private static int calculateBBDimensions(String bbstring) {
		String[] bbinput = bbstring.split(",");
		int dimcounter = 0;
		try {
			for (int i = 0; i < bbinput.length; i++) {
				Double.parseDouble(bbinput[i]);
				dimcounter++;
			}
		} catch (Exception e) {
			logger.debug("Dimensions Count: " + dimcounter);
		}
		return dimcounter;
	}

	private static void addBoundingBoxInput(
			org.n52.wps.client.ExecuteRequestBuilder executeBuilder,
			String identifier, String BBstring) {

		ExecuteDocument executor = executeBuilder.getExecute();
		InputType input1 = executor.getExecute().getDataInputs().addNewInput();
		input1.addNewIdentifier().setStringValue(identifier);

		net.opengis.ows.x11.BoundingBoxType bbtype = input1.addNewData()
				.addNewBoundingBoxData();

		// bboxInput=46,102,47,103,urn:ogc:def:crs:EPSG:6.6:4326,2
		String[] bbinput = BBstring.split(",");
		int dimensions = calculateBBDimensions(BBstring);
		List<String> lc = new ArrayList<String>();
		for (int i = 0; i < dimensions / 2; i++) {
			lc.add(bbinput[i]);
		}
		List<String> uc = new ArrayList<String>();
		for (int i = dimensions / 2; i < dimensions; i++) {
			uc.add(bbinput[i]);
		}

		bbtype.setLowerCorner(lc);
		bbtype.setUpperCorner(uc);

		// int crsidx = bbinput[dimensions].indexOf("crs:");
		String crs = bbinput[dimensions];
		/*
		 * if (crsidx>=0) crs = bbinput[dimensions].substring(crsidx+4);
		 */
		bbtype.setCrs(crs);
		bbtype.setDimensions(new BigInteger("" + dimensions / 2));

	}

	private static LinkedHashMap<String, ResponseWPS> retrieveURLsFromWPSResponse(
			ComplexDataType cdt) {
		org.w3c.dom.Node node = cdt.getDomNode();
		LinkedHashMap<String, ResponseWPS> urls = new LinkedHashMap<>();
		getURLFromXML(node, urls);
		for (String key : urls.keySet()) {
			logger.debug("ResponseWPS Map: " + key + "-->" + urls.get(key));
		}
		return urls;
	}

	/*
	 * private static String getExceptionText(String exceptionText) { try {
	 * String excText = "ExceptionText>"; int idx =
	 * exceptionText.indexOf(excText); if (idx >= 0) { String exception =
	 * exceptionText.substring(idx + excText.length()); exception =
	 * exception.substring(0, exception.indexOf("</")); exception = exception
	 * .replace("<", "") .replace(">", "") .replace("/", " ") .replace("\\",
	 * " ") .replaceAll( "[\\]\\[!\"#$%&'\\(\\)*+/:;<=>?@\\^_`{\\|}~-]",
	 * "_").trim(); exception = exception.replaceAll("[ ]+", " "); if
	 * (exception.length() > 200) exception = exception.substring(0, 200) +
	 * "..."; return exception; } else return "Process error in WPS Execution";
	 * } catch (Exception e) { e.printStackTrace(); return
	 * "Backend error in WPS Execution"; } }
	 */

	private static void getURLFromXML(org.w3c.dom.Node node,
			ResponseWPS responseWPS) {
		if (node == null)
			return;

		logger.debug("Node Name: " + node.getNodeName() + "- Value:"
				+ node.getFirstChild().getNodeValue());

		if (node.getNodeName() == null) {
			return;
		}

		String text;
		switch (node.getNodeName()) {
		case "d4science:Data":
			text = node.getFirstChild().getNodeValue();
			responseWPS.setData(text);
			break;
		case "d4science:Description":
			text = node.getFirstChild().getNodeValue();
			responseWPS.setDescription(text);
			break;
		case "d4science:MimeType":
			text = node.getFirstChild().getNodeValue();
			responseWPS.setMimeType(text);
			break;
		default:
			break;
		}
		/* logger.debug("ResponseWPS:"+responseWPS); */

	}

	private static void getURLFromXML(org.w3c.dom.Node node,
			LinkedHashMap<String, ResponseWPS> urls) {
		if (node == null)
			return;

		logger.debug("Node Name: " + node.getNodeName());

		if (node.getNodeName() == null) {
			return;
		}

		ResponseWPS responseWPS = null;
		NodeList listnodes = node.getChildNodes();
		int nChildren = listnodes.getLength();

		switch (node.getNodeName()) {
		case "ogr:Result":
			NamedNodeMap attrs = node.getAttributes();
			Node n = attrs.getNamedItem("fid");
			String key = n.getNodeValue();
			responseWPS = new ResponseWPS();
			urls.put(key, responseWPS);
			if (nChildren == 0) {

			} else {
				for (int i = 0; i < nChildren; i++) {
					getURLFromXML(listnodes.item(i), responseWPS);

				}
			}
			break;
		default:
			if (nChildren == 0) {

			} else {
				for (int i = 0; i < nChildren; i++) {
					getURLFromXML(listnodes.item(i), urls);

				}
			}
			break;

		}

		/*
		 * 
		 * NodeList listnodes = node.getChildNodes(); int nChildren =
		 * listnodes.getLength();
		 * 
		 * if (nChildren == 0) { String text = node.getNodeValue(); if (text !=
		 * null && (text.startsWith("https:") || text.startsWith("http:") ||
		 * text.startsWith("ftp:") || text.startsWith("smp:") || text
		 * .startsWith("file:"))) urls.add(text.trim()); else if (text != null
		 * && text.trim().length() > 0) urls.add(text.trim()); } else { for (int
		 * i = 0; i < nChildren; i++) { List<String> childrenurls =
		 * getURLFromXML(listnodes.item(i)); urls.addAll(childrenurls);
		 * 
		 * } } return urls;
		 */
	}

	// TODO
	@Override
	public List<Parameter> getInputParameters(Operator operator)
			throws Exception {
		try {
			logger.info("Parameters of algorithm " + operator.getId());

			ProcessInformations processInformations;
			try {
				processInformations = describeProcess(operator.getId());
			} catch (Throwable e) {
				logger.error("GetParameters: " + e.getLocalizedMessage());
				e.printStackTrace();
				throw new Exception(e.getLocalizedMessage());
			}

			logger.debug("ProcessInformation: " + processInformations);

			List<Parameter> parameters = new ArrayList<>();

			Parameter inputParameter;
			for (InputDescriptionType inputDesc : processInformations
					.getInputs()) {
				inputParameter = WPS2SM.convert2SMType(inputDesc);
				logger.debug("InputParameter: " + inputParameter);
				parameters.add(inputParameter);
			}

			logger.debug("Parameters: " + parameters);
			return parameters;

		} catch (Throwable e) {
			logger.error(e.getLocalizedMessage());
			e.printStackTrace();
			throw new Exception(e.getLocalizedMessage());
		}

	}

	@Override
	public ComputationId startComputation(Operator operator) throws Exception {
		ProcessInformations processInformations;
		try {
			processInformations = describeProcess(operator.getId());
		} catch (Throwable e) {
			logger.error("GetParameters: " + e.getLocalizedMessage());
			e.printStackTrace();
			throw new Exception(e.getLocalizedMessage(), e);
		}

		LinkedHashMap<String, Parameter> inputParameters = new LinkedHashMap<>();

		Parameter inputParameter;
		for (InputDescriptionType inputDesc : processInformations.getInputs()) {
			inputParameter = WPS2SM.convert2SMType(inputDesc);
			logger.debug("InputParameter: " + inputParameter);
			inputParameters.put(inputParameter.getName(), inputParameter);
		}

		List<Parameter> params = operator.getOperatorParameters();
		Map<String, String> userInputs = new LinkedHashMap<>();
		for (Parameter parm : params) {
			userInputs.put(parm.getName(), parm.getValue());
			logger.debug("UserInputs[key=" + parm.getName() + ", value="
					+ parm.getValue() + "]");
		}

		LinkedHashMap<String, String> equivalentRequestMap = new LinkedHashMap<>();
		String processUrl = compute(processInformations, userInputs,
				inputParameters, equivalentRequestMap);
		logger.debug("Stated Computation ProcessLocation:" + processUrl);

		int idIndex = processUrl.lastIndexOf("?id=");
		String id;
		if (idIndex > -1) {
			id = processUrl.substring(idIndex + 4, processUrl.length());
		} else {
			logger.error("Invalid processLocation: " + processUrl);
			throw new Exception("Invalid processLocation: " + processUrl);
		}

		String equivalentRequest = wpsProcessingServlet + "?"
				+ "request=Execute&service=WPS&Version=1.0.0&gcube-token="
				+ wpsToken + "&lang=en-US&Identifier=" + operator.getId()
				+ "&DataInputs=";

		for (String key : equivalentRequestMap.keySet()) {
			equivalentRequest = equivalentRequest + key + "="
					+ equivalentRequestMap.get(key) + ";";
		}

		ComputationId computationId = new ComputationId(id, processUrl,
				operator.getId(), operator.getName(), equivalentRequest);
		logger.debug("ComputationId: " + computationId);

		runningProcess.put(computationId, processInformations);

		return computationId;
	}

	private String compute(ProcessInformations processInformations,
			Map<String, String> userInputs,
			Map<String, Parameter> inputParameters,
			LinkedHashMap<String, String> equivalentRequestMap)
			throws Exception {
		try {
			// setup the inputs
			org.n52.wps.client.ExecuteRequestBuilder executeBuilder = new org.n52.wps.client.ExecuteRequestBuilder(
					processInformations.getProcessDescription());
			for (InputDescriptionType input : processInformations.getInputs()) {
				String value = userInputs.get(input.getIdentifier()
						.getStringValue());
				if (value != null && value.trim().length() > 0) {
					if (input.isSetLiteralData()) {
						logger.debug("Configuring Literal: "
								+ input.getIdentifier().getStringValue()
								+ " to: " + value);
						equivalentRequestMap.put(input.getIdentifier()
								.getStringValue(), value);
						executeBuilder.addLiteralData(input.getIdentifier()
								.getStringValue(), value);

					} else if (input.isSetBoundingBoxData()) {
						logger.debug("Configuring Bounding Box: "
								+ input.getIdentifier().getStringValue()
								+ " to: " + value);
						equivalentRequestMap.put(input.getIdentifier()
								.getStringValue(), value);
						addBoundingBoxInput(executeBuilder, input
								.getIdentifier().getStringValue(), value);

					} else {
						if (input.isSetComplexData()) {
							logger.debug("Configuring Complex: "
									+ input.getIdentifier().getStringValue()
									+ " to: " + value);
							SupportedComplexDataInputType complex = input
									.getComplexData();
							Parameter par = inputParameters.get(input
									.getIdentifier().getStringValue());

							String publicLink;
							if (par instanceof TabularListParameter) {
								// TabularListParameter tabularListParameter =
								// ((TabularListParameter) par);
								InputStream tablesStream = new ByteArrayInputStream(
										value.getBytes());
								publicLink = StorageUtil
										.saveOnStorageInTemporalFile(tablesStream);
							} else {
								publicLink = value;
							}
							equivalentRequestMap.put(input.getIdentifier()
									.getStringValue(), value);
							executeBuilder.addComplexDataReference(input
									.getIdentifier().getStringValue(),
									publicLink, complex.getDefault()
											.getFormat().getSchema(), complex
											.getDefault().getFormat()
											.getEncoding(), complex
											.getDefault().getFormat()
											.getMimeType());

						} else {
							logger.error("This input parameter type is not supported by client library: "
									+ input);
							throw new Exception(
									"This input parameter is not supported by client library: "
											+ input);
						}

					}
				}
			}

			// Submit the execution
			String statusLocation = executeProcessAsync(executeBuilder,
					processInformations.getProcessDescription());
			logger.debug("Starting Process: " + statusLocation);
			return statusLocation;

		} catch (Throwable e) {
			logger.error(e.getLocalizedMessage());
			e.printStackTrace();
			throw new Exception(e.getLocalizedMessage(), e);
		}
	}

	@Override
	public ComputationStatus getComputationStatus(ComputationId computationId)
			throws Exception {
		StatWPSClientSession wpsClient = null;
		try {
			logger.debug("GetComputationStatus(): ComputationId="
					+ computationId);
			wpsClient = createWPSClientSession();
			wpsClient.connectForMonitoring(wpsProcessingServlet);

			Object responseObject = null;
			if (computationId == null || computationId.getUrlId() == null
					|| computationId.getUrlId().isEmpty()) {
				throw new Exception("Process Location is null!");
			} else {
				ComputationStatus computationStatus = null;
				try {

					responseObject = wpsClient.executeViaGET(
							computationId.getUrlId(), "");

					logger.debug("ComputationStatus ResponseObject: "
							+ responseObject);

					if (responseObject instanceof ExecuteResponseDocumentImpl) {
						if (((ExecuteResponseDocumentImpl) responseObject)
								.getExecuteResponse() == null) {
							logger.debug("WPS FAILURE: ExecuteResponse is null");

							computationStatus = new ComputationStatus(
									Status.FAILED, 100f);

						} else {
							StatusType statusType = ((ExecuteResponseDocumentImpl) responseObject)
									.getExecuteResponse().getStatus();
							if (statusType == null) {
								logger.debug("WPS FAILURE: Status Type is null");
								computationStatus = null;
							} else {

								String failure = statusType.getProcessFailed() == null ? null
										: statusType.getProcessFailed()
												.getExceptionReport()
												.toString();
								if ((failure != null && !failure.isEmpty())) {
									logger.debug("WPS FAILURE: " + failure);
									computationStatus = new ComputationStatus(
											new DataMinerServiceException(
													failure));
								} else {
									String paused = statusType
											.getProcessPaused() == null ? null
											: statusType.getProcessPaused()
													.getStringValue();
									if (paused != null && !paused.isEmpty()) {
										logger.debug("WPS PAUSED: " + paused);
										computationStatus = new ComputationStatus(
												new DataMinerServiceException(
														paused));
									} else {
										String success = statusType
												.getProcessSucceeded() == null ? null
												: statusType
														.getProcessSucceeded();

										if (success != null
												&& !success.isEmpty()) {
											logger.debug("WPS SUCCESS");
											computationStatus = new ComputationStatus(
													Status.COMPLETE, 100f);
										} else {
											String accepted = statusType
													.getProcessAccepted() == null ? null
													: statusType
															.getProcessAccepted();
											if (accepted != null
													&& !accepted.isEmpty()) {
												logger.debug("WPS ACCEPTED");
												computationStatus = new ComputationStatus(
														Status.ACCEPTED, 0f);
											} else {
												int status = statusType
														.getProcessStarted() == null ? -1
														: statusType
																.getProcessStarted()
																.getPercentCompleted();

												if (status >= 0) {
													Float statusd = (float) status;
													try {
														statusd = Float
																.parseFloat(statusType
																		.getProcessStarted()
																		.getStringValue());
													} catch (Exception e) {

													}
													logger.debug("WPS STATUS:"
															+ statusd);
													computationStatus = new ComputationStatus(
															Status.RUNNING,
															statusd);
												} else {
													if (status == -1) {
														logger.debug("WPS STATUS: Computation cancelled, "
																+ statusType
																		.getProcessStarted());
														computationStatus = new ComputationStatus(
																Status.CANCELLED,
																-1);

													} else {

														logger.debug("WPS STATUS: Not Started, "
																+ statusType
																		.getProcessStarted());
													}
												}
											}
										}
									}
								}
							}
						}
						logger.debug("ComputationStatus: " + computationStatus);
						return computationStatus;
					} else {
						logger.error("Error in ResponceObject: "
								+ responseObject);
						logger.error("WPS FAILURE: ");
						computationStatus = new ComputationStatus(
								Status.FAILED, 100f);
						return computationStatus;
					}
				} catch (Throwable e) {
					logger.error("WPS FAILURE: " + e.getLocalizedMessage());
					e.printStackTrace();
					computationStatus = new ComputationStatus(Status.FAILED,
							100f);
					return computationStatus;

				}
			}

		} catch (Throwable e) {
			logger.error("MonitorProcess: " + e.getLocalizedMessage());
			e.printStackTrace();
			throw new Exception(e.getLocalizedMessage(), e);
		} finally {
			wpsClient.disconnect(wpsProcessingServlet);
		}

	}

	@Override
	public OutputData getOutputDataByComputationId(ComputationId computationId)
			throws Exception {
		Map<String, Resource> resources = retrieveOutput(computationId);
		MapResource mapResource = new MapResource("mapResource", "Resources",
				"Resources", resources);
		OutputData outputData = new OutputData(computationId, mapResource);

		return outputData;
	}

	private Map<String, Resource> retrieveOutput(ComputationId computationId)
			throws Exception {
		Map<String, Resource> outputResource = new LinkedHashMap<>();
		Map<String, Parameter> outputParameters = new LinkedHashMap<>();
		ProcessInformations processInformations = runningProcess
				.get(computationId);

		Parameter outputParameter;
		if (processInformations != null
				&& processInformations.getOutputs() != null) {
			for (OutputDescriptionType outputDesc : processInformations
					.getOutputs()) {
				outputParameter = WPS2SM.convert2SMType(outputDesc);
				logger.debug("OutputParameter: " + outputParameter);
				outputParameters
						.put(outputParameter.getName(), outputParameter);
			}
		}

		retrieveProcessOutput(computationId.getUrlId(), outputParameters,
				outputResource);

		return outputResource;

	}

	private void retrieveProcessOutput(String processLocation,
			Map<String, Parameter> outputParameters,
			Map<String, Resource> outputResource) throws Exception {
		ProcessOutputs outs = retrieveProcessResult(processLocation);
		logger.debug("Process Executed");
		// retrieve the output objs
		if (outs == null)
			throw new Exception(
					"Error during the execution of the WPS process: returned an empty document");
		else {
			OutputDataType[] outputData = outs.getOutputArray();

			for (OutputDataType out : outputData) {
				String outputID = out.getIdentifier().getStringValue();
				String value = "";
				if (out.getData().isSetLiteralData()) {
					value = out.getData().getLiteralData().getStringValue();
					Parameter paramLiteral = outputParameters.get(outputID);
					if (paramLiteral != null) {
						paramLiteral.setValue(value);
						logger.debug("Assigning value: " + value
								+ " to output named: " + outputID);
						Resource resource = new ObjectResource(outputID,
								paramLiteral.getName(),
								paramLiteral.getDescription(),
								paramLiteral.getValue());
						outputResource.put(outputID, resource);
					}
				} else {
					if (out.getData().isSetComplexData()) {
						if (out.getReference() != null) {
							value = out.getReference().getHref();
							Parameter paramComplexData = outputParameters
									.get(outputID);
							if (paramComplexData != null) {
								paramComplexData.setValue(value);
								logger.debug("Assigning value: " + value
										+ " to output named: " + outputID);
								Resource resource = new ObjectResource(
										outputID, paramComplexData.getName(),
										paramComplexData.getDescription(),
										paramComplexData.getValue());
								outputResource.put(outputID, resource);
							}
						} else
							// remove the element name, which is not useful
							outputParameters.remove(outputID);

						ComplexDataType cdt = out.getData().getComplexData();
						LinkedHashMap<String, ResponseWPS> urls = retrieveURLsFromWPSResponse(cdt);

						for (String key : urls.keySet()) {
							logger.debug("Adding OBJ:" + key);
							ResponseWPS responseWPS = urls.get(key);
							ObjectParameter objP = new ObjectParameter(key,
									responseWPS.getDescription(),
									String.class.getName(), " ");
							objP.setValue(responseWPS.getData());
							logger.debug("ObjectParameter: " + objP);
							outputParameters.put(key, objP);
							if (responseWPS != null
									&& responseWPS.getMimeType() != null) {
								Resource resource;
								switch (responseWPS.getMimeType()) {
								case "image/bmp":
								case "image/gif":
								case "image/jpeg":
								case "image/png":
									resource = new ImageResource(key,
											responseWPS.getDescription(),
											responseWPS.getDescription(),
											responseWPS.getData(),
											responseWPS.getMimeType());
									outputResource.put(key, resource);
									break;
								case "text/csv":
									if (responseWPS.getData() != null
											&& !responseWPS.getData().isEmpty()) {
										if (responseWPS.getData().startsWith(
												"http:")
												|| responseWPS.getData()
														.startsWith("https:")
												|| responseWPS.getData()
														.startsWith("smp:")) {
											resource = new FileResource(key,
													responseWPS
															.getDescription(),
													responseWPS
															.getDescription(),
													responseWPS.getData(),
													responseWPS.getMimeType());
											outputResource.put(key, resource);
										} else {
											resource = new ObjectResource(key,
													responseWPS
															.getDescription(),
													responseWPS
															.getDescription(),
													responseWPS.getData());
											outputResource.put(key, resource);
										}
									} else {
										resource = new ObjectResource(key,
												responseWPS.getDescription(),
												responseWPS.getDescription(),
												responseWPS.getData());
										outputResource.put(key, resource);
									}

									break;
								default:
									if (responseWPS.getData() != null
											&& !responseWPS.getData().isEmpty()) {
										if (responseWPS.getData().startsWith(
												"http:")
												|| responseWPS.getData()
														.startsWith("https:")
												|| responseWPS.getData()
														.startsWith("smp:")) {
											resource = new FileResource(key,
													responseWPS
															.getDescription(),
													responseWPS
															.getDescription(),
													responseWPS.getData(),
													responseWPS.getMimeType());
											outputResource.put(key, resource);
										} else {
											resource = new ObjectResource(key,
													responseWPS
															.getDescription(),
													responseWPS
															.getDescription(),
													responseWPS.getData());
											outputResource.put(key, resource);
										}
									} else {
										resource = new ObjectResource(key,
												responseWPS.getDescription(),
												responseWPS.getDescription(),
												responseWPS.getData());
										outputResource.put(key, resource);
									}
									break;
								}

							}
						}
					} else {
						value = out.getData().getLiteralData().getStringValue();

					}
				}
			}
		}
	}

	@Override
	public String cancelComputation(ComputationId computationId)
			throws Exception {
		StatWPSClientSession wpsClient = null;
		try {
			wpsClient = createWPSClientSession();

			String result = wpsClient.cancelComputation(
					wpsCancelComputationServlet, computationId.getId());

			return result;

		} catch (Throwable e) {
			e.printStackTrace();
			throw new Exception(e.getLocalizedMessage(), e);
		} finally {
			if (wpsClient != null) {
				wpsClient.disconnect(wpsProcessingServlet);
			}
		}

	}

	@Override
	public ComputationId resubmitComputation(
			Map<String, String> computationProperties) throws Exception {
		ProcessInformations processInformations;
		if (computationProperties == null || computationProperties.isEmpty()) {
			throw new Exception("Invalid computation properties: "
					+ computationProperties);
		}
		try {
			processInformations = describeProcess(computationProperties
					.get("operator_id"));
		} catch (Throwable e) {
			logger.error("GetParameters: " + e.getLocalizedMessage());
			e.printStackTrace();
			throw new Exception(e.getLocalizedMessage(), e);
		}

		LinkedHashMap<String, Parameter> inputParameters = new LinkedHashMap<>();

		Parameter inputParameter;
		for (InputDescriptionType inputDesc : processInformations.getInputs()) {
			inputParameter = WPS2SM.convert2SMType(inputDesc);
			logger.debug("InputParameter: " + inputParameter);
			inputParameters.put(inputParameter.getName(), inputParameter);
		}

		Map<String, String> userInputs = new LinkedHashMap<>();
		for (String key : computationProperties.keySet()) {
			if (key.startsWith("input")) {
				int inputSeparatorIndex = key.indexOf("_");
				String inputKey = key.substring(inputSeparatorIndex + 1);
				if (inputKey.compareToIgnoreCase("user.name") != 0
						&& inputKey.compareToIgnoreCase("scope") != 0) {
					userInputs.put(inputKey, computationProperties.get(key));
					logger.debug("UserInputs[key=" + inputKey + ", value="
							+ computationProperties.get(key) + "]");
				}

			}
		}

		if (userInputs.isEmpty()) {
			logger.error("Attention no inputs parameters retrieved for this computation: "
					+ computationProperties);
			throw new Exception(
					"Attention no inputs parameters retrieved for this computation: "
							+ computationProperties);
		}

		LinkedHashMap<String, String> equivalentRequestMap = new LinkedHashMap<>();
		String processUrl = compute(processInformations, userInputs,
				inputParameters, equivalentRequestMap);
		logger.debug("Stated Computation ProcessLocation:" + processUrl);

		int idIndex = processUrl.lastIndexOf("?id=");
		String id;
		if (idIndex > -1) {
			id = processUrl.substring(idIndex + 4, processUrl.length());
		} else {
			logger.error("Invalid processLocation: " + processUrl);
			throw new Exception("Invalid processLocation: " + processUrl);
		}

		String equivalentRequest = wpsProcessingServlet + "?"
				+ "request=Execute&service=WPS&Version=1.0.0&gcube-token="
				+ wpsToken + "&lang=en-US&Identifier="
				+ computationProperties.get("operator_id") + "&DataInputs=";

		for (String key : equivalentRequestMap.keySet()) {
			equivalentRequest = equivalentRequest + key + "="
					+ equivalentRequestMap.get(key) + ";";
		}

		ComputationId computationId = new ComputationId(id, processUrl,
				computationProperties.get("operator_id"),
				computationProperties.get("operator_name"), equivalentRequest);
		logger.debug("ComputationId: " + computationId);

		runningProcess.put(computationId, processInformations);

		return computationId;
	}

	@Override
	public ComputationData getComputationDataByComputationProperties(
			Map<String, String> computationProperties) throws Exception {
		if (computationProperties == null || computationProperties.isEmpty()) {
			throw new Exception("Invalid computation properties: "
					+ computationProperties);
		}

		String compId = computationProperties.get("computation_id");

		String operatorId = computationProperties.get("operator_id");
		String operatorName = computationProperties.get("operator_name");
		String operatorDescritpion = computationProperties
				.get("operator_description");
		String vre = computationProperties.get("VRE");
		String startDate = computationProperties.get("start_date");
		String endDate = computationProperties.get("end_date");
		String status = computationProperties.get("status");
		String executionType = computationProperties.get("execution_type");

		ComputationId computationId = new ComputationId();
		computationId.setId(compId);
		computationId.setOperatorId(operatorId);
		computationId.setOperatorName(operatorName);

		LinkedHashMap<String, String> inputValueParameters = new LinkedHashMap<>();
		LinkedHashMap<String, String> outputValueParameters = new LinkedHashMap<>();

		for (String key : computationProperties.keySet()) {
			if (key != null) {
				if (key.startsWith("input")) {
					int inputSeparatorIndex = key.indexOf("_");
					String inputKey = key.substring(inputSeparatorIndex + 1);
					if (inputKey.compareToIgnoreCase("user.name") != 0
							&& inputKey.compareToIgnoreCase("scope") != 0) {
						inputValueParameters.put(inputKey,
								computationProperties.get(key));

					}

				} else {
					if (key.startsWith("output")) {
						int outputSeparatorIndex = key.indexOf("_");
						String outputKey = key
								.substring(outputSeparatorIndex + 1);
						outputValueParameters.put(outputKey,
								computationProperties.get(key));

					}

				}

			}
		}

		ComputationValueBuilder computationValueBuilder = new ComputationValueBuilder(
				inputValueParameters);
		LinkedHashMap<String, ComputationValue> inputParameters = computationValueBuilder
				.create();
		computationValueBuilder = new ComputationValueBuilder(
				outputValueParameters);
		LinkedHashMap<String, ComputationValue> outputParameters = computationValueBuilder
				.create();

		ComputationData computationData = new ComputationData(computationId,
				inputParameters, outputParameters, operatorDescritpion,
				startDate, endDate, status, executionType, vre);

		logger.debug("ComputationData: " + computationData);
		return computationData;

	}
}
