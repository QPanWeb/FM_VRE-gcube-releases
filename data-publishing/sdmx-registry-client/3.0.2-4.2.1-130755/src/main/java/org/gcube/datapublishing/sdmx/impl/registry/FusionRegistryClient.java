package org.gcube.datapublishing.sdmx.impl.registry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.gcube.datapublishing.sdmx.SDMXSourceProvider;
import org.gcube.datapublishing.sdmx.api.model.SDMXRegistryDescriptor;
import org.gcube.datapublishing.sdmx.api.model.SDMXRegistryInterfaceType;
import org.gcube.datapublishing.sdmx.api.registry.SDMXRegistryClient;
import org.gcube.datapublishing.sdmx.impl.exceptions.RegistryClientExceptionFactory;
import org.gcube.datapublishing.sdmx.impl.exceptions.SDMXRegistryClientException;
import org.gcube.datapublishing.sdmx.impl.reports.OperationStatus;
import org.gcube.datapublishing.sdmx.impl.reports.SubmissionReport;
import org.sdmx.resources.sdmxml.schemas.v21.message.BaseHeaderType;
import org.sdmx.resources.sdmxml.schemas.v21.message.RegistryInterfaceDocument;
import org.sdmx.resources.sdmxml.schemas.v21.message.RegistryInterfaceType;
import org.sdmx.resources.sdmxml.schemas.v21.registry.QueryRegistrationRequestType;
import org.sdmx.resources.sdmxml.schemas.v21.registry.QueryTypeType;
import org.sdmxsource.sdmx.api.constants.STRUCTURE_OUTPUT_FORMAT;
import org.sdmxsource.sdmx.api.manager.output.StructureWriterManager;
import org.sdmxsource.sdmx.api.manager.parse.StructureParsingManager;
import org.sdmxsource.sdmx.api.model.StructureWorkspace;
import org.sdmxsource.sdmx.api.model.beans.SdmxBeans;
import org.sdmxsource.sdmx.api.model.beans.base.AgencySchemeBean;
import org.sdmxsource.sdmx.api.model.beans.base.DataProviderSchemeBean;
import org.sdmxsource.sdmx.api.model.beans.base.MaintainableBean;
import org.sdmxsource.sdmx.api.model.beans.codelist.CodelistBean;
import org.sdmxsource.sdmx.api.model.beans.conceptscheme.ConceptSchemeBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DataStructureBean;
import org.sdmxsource.sdmx.api.model.beans.datastructure.DataflowBean;
import org.sdmxsource.sdmx.api.model.beans.registry.ProvisionAgreementBean;
import org.sdmxsource.sdmx.api.model.beans.registry.RegistrationBean;
import org.sdmxsource.sdmx.api.util.ReadableDataLocation;
import org.sdmxsource.sdmx.sdmxbeans.model.SdmxStructureFormat;
import org.sdmxsource.util.io.ReadableDataLocationTmp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@FusionRegistry
public class FusionRegistryClient implements SDMXRegistryClient {
	
	private Logger log = LoggerFactory.getLogger(FusionRegistryClient.class);

	private SDMXRegistryDescriptor registry = null;
	private SDMXRegistryInterfaceType interfaceType = SDMXRegistryInterfaceType.RESTV2_1;

	private StructureWriterManager structureWriterManager;
	private StructureParsingManager structureParsingManager;
	
	private static SDMXSourceProvider sdmxSourceProvider = new SDMXSourceProvider();
	
	public FusionRegistryClient() {
		structureWriterManager = sdmxSourceProvider.getStructureWriterManager();
		structureParsingManager = sdmxSourceProvider.getStructureParsingManager();
	}
	
	public FusionRegistryClient(SDMXRegistryDescriptor registry) {
		this();
		this.registry = registry;
	}

	public void setInterfaceType(SDMXRegistryInterfaceType interfaceType) {
		switch(interfaceType){
		case SOAPV1:
		case SOAPV2:
		case SOAPV2_1:
			throw new IllegalArgumentException("Fusion Registry does only support REST protocols.");
		default:
			this.interfaceType = interfaceType;
		}
	}

	@Override
	public void setRegistry(SDMXRegistryDescriptor descriptor) {
		this.registry = descriptor;		
	}
	
	@Override
	public SDMXRegistryDescriptor getRegistry() {
		return registry;
	}

	@Override
	public SubmissionReport publish(AgencySchemeBean agencyScheme)
			throws SDMXRegistryClientException {
		return publishMaintanableArtefact(agencyScheme);
	}
	@Override
	public SubmissionReport publish(CodelistBean codelist)
			throws SDMXRegistryClientException {
		return publishMaintanableArtefact(codelist);
	}
	@Override
	public SubmissionReport publish(ConceptSchemeBean conceptscheme)
			throws SDMXRegistryClientException {
		return publishMaintanableArtefact(conceptscheme);
	}
	@Override
	public SubmissionReport publish(DataStructureBean datastructure)
			throws SDMXRegistryClientException {
		return publishMaintanableArtefact(datastructure);
	}
	@Override
	public SubmissionReport publish(DataflowBean dataflow)
			throws SDMXRegistryClientException {
		return publishMaintanableArtefact(dataflow);
	}
	@Override
	public SubmissionReport publish(DataProviderSchemeBean dataproviderscheme)
			throws SDMXRegistryClientException {
		return publishMaintanableArtefact(dataproviderscheme);
	}
	@Override
	public SubmissionReport publish(ProvisionAgreementBean provisionagreement)
			throws SDMXRegistryClientException {
		return publishMaintanableArtefact(provisionagreement);
	}
	@Override
	public SubmissionReport publish(RegistrationBean subscription)
			throws SDMXRegistryClientException {

		String xmlDocument = generateSDMXDocument(subscription);
		log.info("Submitting to registry Maintainable Artifact with URN: "
				+ subscription.getUrn());

		InputStream is = POSTQuery(getWebServiceUrl(), xmlDocument);

		String serverResponse;
		try {
			serverResponse = IOUtils.toString(is);
		} catch (IOException e) {
			String errorMsg = "Unable to read server response";
			log.error(errorMsg, e);
			throw new SDMXRegistryClientException(errorMsg);
		}

		logServerMessage(serverResponse);

		testForErrorMessage(serverResponse);

		XPath xpath = XPathFactory.newInstance().newXPath();

		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document document = dBuilder.parse(IOUtils
					.toInputStream(serverResponse));

			SubmissionReport report = new SubmissionReport();

			String id = (String) xpath
					.evaluate(
							"//RegistryInterface/SubmitRegistrationsResponse/RegistrationStatus[1]/Registration/@id",
							document, XPathConstants.STRING);
			report.setId(id);

			String operationStatus = (String) xpath
					.evaluate(
							"//RegistryInterface/SubmitRegistrationsResponse/RegistrationStatus[1]/StatusMessage/@status",
							document, XPathConstants.STRING);
			report.setStatus(OperationStatus.valueOf(operationStatus));

			NodeList nl = (NodeList) xpath
					.evaluate(
							"//RegistryInterface/SubmitRegistrationsResponse/RegistrationStatus[1]/StatusMessage/MessageText/Text",
							document, XPathConstants.NODESET);

			for (int i = 0; i < nl.getLength(); i++) {
				report.addMessage(nl.item(i).getTextContent());
			}

			log.debug("Submission report: " + report);
			return report;
		} catch (Exception e) {
			throw new SDMXRegistryClientException(
					"Unable to parse registry response");
		}
	}

	private SubmissionReport publishMaintanableArtefact(MaintainableBean bean)
			throws SDMXRegistryClientException {

		String xmlDocument = generateSDMXDocument(bean);
		log.info("Submitting to registry Maintainable Artifact with URN: "
				+ bean.getUrn());

		InputStream is = POSTQuery(getWebServiceUrl(), xmlDocument);

		String serverResponse;
		try {
			serverResponse = IOUtils.toString(is);
		} catch (IOException e) {
			String errorMsg = "Unable to read server response";
			log.error(errorMsg, e);
			throw new SDMXRegistryClientException(errorMsg);
		}

		logServerMessage(serverResponse);
		
		testForErrorMessage(serverResponse);

		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document document = dBuilder.parse(IOUtils
					.toInputStream(serverResponse));

			XPath xpath = XPathFactory.newInstance().newXPath();
			SubmissionReport report = new SubmissionReport();

			String id = (String) xpath
					.evaluate(
							"//RegistryInterface/SubmitStructureResponse/SubmissionResult[1]/SubmittedStructure/MaintainableObject/URN/text()",
							document, XPathConstants.STRING);
			report.setId(id);

			String operationStatus = (String) xpath
					.evaluate(
							"//RegistryInterface/SubmitStructureResponse/SubmissionResult[1]/StatusMessage/@status",
							document, XPathConstants.STRING);
			report.setStatus(OperationStatus.valueOf(operationStatus));

			NodeList nl = (NodeList) xpath
					.evaluate(
							"//RegistryInterface/SubmitRegistrationsResponse/RegistrationStatus/StatusMessage/MessageText/Text",
							document, XPathConstants.NODESET);
			for (int i = 0; i < nl.getLength(); i++) {
				report.addMessage(nl.item(i).getNodeValue());
			}

			log.trace("Registration report: " + report);
			return report;
		} catch(Exception e){
			String errorMsg = "Error occurred while parsing registry response";
			log.error(errorMsg, e);
			throw new SDMXRegistryClientException(errorMsg);
		} 

	}
	@Override
	public SdmxBeans getAgencyScheme(String agencyId, String id,
			String version, Detail details, References references)
			throws SDMXRegistryClientException {
		return getMaintainableArtifacts(agencyId, id, version, details,
				references, "agencyscheme");
	}
	@Override
	public SdmxBeans getCodelist(String agencyId, String id, String version,
			Detail details, References references)
			throws SDMXRegistryClientException {
		return getMaintainableArtifacts(agencyId, id, version, details,
				references, "codelist");
	}
	@Override
	public SdmxBeans getConceptScheme(String agencyId, String id,
			String version, Detail details, References references)
			throws SDMXRegistryClientException {
		return getMaintainableArtifacts(agencyId, id, version, details,
				references, "conceptscheme");
	}
	@Override
	public SdmxBeans getDataStructure(String agencyId, String id,
			String version, Detail details, References references)
			throws SDMXRegistryClientException {
		return getMaintainableArtifacts(agencyId, id, version, details,
				references, "datastructure");
	}
	@Override
	public SdmxBeans getDataFlow(String agencyId, String id, String version,
			Detail details, References references)
			throws SDMXRegistryClientException {
		return getMaintainableArtifacts(agencyId, id, version, details,
				references, "dataflow");
	}
	@Override
	public SdmxBeans getDataProviderScheme(String agencyId, String id,
			String version, Detail details, References references)
			throws SDMXRegistryClientException {
		return getMaintainableArtifacts(agencyId, id, version, details,
				references, "dataprovider");
	}
	@Override
	public SdmxBeans getProvisionAgreement(String agencyId, String id,
			String version, Detail details, References references)
			throws SDMXRegistryClientException {
		return getMaintainableArtifacts(agencyId, id, version, details,
				references, "provisionagreement");
	}
	@Override
	public SdmxBeans getAllDataSetRegistrations()
			throws SDMXRegistryClientException {
		RegistryInterfaceDocument document = RegistryInterfaceDocument.Factory
				.newInstance();
		RegistryInterfaceType registryInterfaceType = document
				.addNewRegistryInterface();
		BaseHeaderType baseHeader = registryInterfaceType.addNewHeader();
		baseHeader.setID("UNKNOWN");
		baseHeader.setTest(false);
		baseHeader.setPrepared(Calendar.getInstance());

		baseHeader.addNewSender().setId("sdmx-publisher");
		baseHeader.addNewReceiver().setId("REGISTRY");
		registryInterfaceType.setHeader(baseHeader);
		QueryRegistrationRequestType queryRegistrationRequestType = registryInterfaceType
				.addNewQueryRegistrationRequest();
		queryRegistrationRequestType.setQueryType(QueryTypeType.DATA_SETS);
		queryRegistrationRequestType.addNewAll();

		String xmlDocument = document.toString();

		log.trace("Generated registration query document:\n" + xmlDocument);

		InputStream is = POSTQuery(getWebServiceUrl(), xmlDocument);

		String serverResponse;
		try {
			serverResponse = IOUtils.toString(is);
		} catch (IOException e) {
			String errorMsg = "Unable to read server response";
			log.error(errorMsg, e);
			throw new SDMXRegistryClientException(errorMsg);
		}

		logServerMessage(serverResponse);

		testForErrorMessage(serverResponse);

		return getStructureBeansFromStream(IOUtils
				.toInputStream(serverResponse));
	}

	private SdmxBeans getMaintainableArtifacts(String agencyId, String id,
			String version, Detail details, References references,
			String typeCode) throws SDMXRegistryClientException {

		log.info("Retrieving Maintainable Artifact (" + typeCode
				+ ") with ref: " + agencyId + ", " + id + ", " + version
				+ " from Registry");

		String webService = getWebServiceUrl();

		webService += typeCode + "/" + agencyId + "/" + id + "/" + version
				+ "/?detail=" + details.toString() + "&references="
				+ references.toString();

		InputStream is = GETQuery(webService);

		ReadableDataLocation structureLocation = new ReadableDataLocationTmp(is);
		StructureWorkspace workspace = structureParsingManager
				.parseStructures(structureLocation);
		return workspace.getStructureBeans(false);
	}

	private String getWebServiceUrl() throws SDMXRegistryClientException {
		String webService = null;
		switch (interfaceType) {
		case RESTV1:
		case RESTV2:
		case RESTV2_1:
			webService = registry.getUrl(interfaceType);
			break;
		default:
			throw new SDMXRegistryClientException("Interface "
					+ interfaceType.getName() + " not Implemented");
		}
		log.debug("Using web service URL: " + webService);
		return webService;
	}

	private String generateSDMXDocument(MaintainableBean bean)
			throws SDMXRegistryClientException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		switch (interfaceType) {
		case RESTV1:
			structureWriterManager.writeStructure(bean, null,new SdmxStructureFormat(
					STRUCTURE_OUTPUT_FORMAT.SDMX_V1_STRUCTURE_DOCUMENT), baos);
			break;
		case RESTV2:
			structureWriterManager.writeStructure(bean, null,new SdmxStructureFormat(
					STRUCTURE_OUTPUT_FORMAT.SDMX_V2_STRUCTURE_DOCUMENT), baos);
			break;
		case RESTV2_1:
			structureWriterManager.writeStructure(bean, null,new SdmxStructureFormat(
					STRUCTURE_OUTPUT_FORMAT.SDMX_V21_STRUCTURE_DOCUMENT), baos);
			break;
		default:
			throw new SDMXRegistryClientException("Interface "
					+ interfaceType.getName() + " not Implemented");
		}

		return baos.toString();
	}

	private InputStream POSTQuery(String queryUrl, String queryDocument)
			throws SDMXRegistryClientException {

		log.trace("Submitting SDMX document to Registry URL: " + queryUrl
				+ ", Document: " + queryDocument);

		URL url;
		try {
			url = new URL(queryUrl);
		} catch (MalformedURLException e) {
			String msg = "Malformed query URL";
			log.error(msg, e);
			throw new SDMXRegistryClientException(msg);
		}
		URLConnection urlc;
		try {
			urlc = url.openConnection();
		} catch (IOException e) {
			String msg = "Unable to open a connection to the registry";
			log.error(msg, e);
			throw new SDMXRegistryClientException(msg);
		}
		urlc.setDoOutput(true);
		urlc.setAllowUserInteraction(false);
		urlc.addRequestProperty("Accept", "application/xml;version="
				+ interfaceType.getModelVersion());
		// urlc.addRequestProperty("Content-Type", "application/text;version="
		// + interfaceType.getModelVersion());
		urlc.addRequestProperty("Content-Type", "application/text");
		PrintStream ps;
		try {
			ps = new PrintStream(urlc.getOutputStream());
		} catch (IOException e) {
			String msg = "Unable to send message to the registry";
			log.error(msg, e);
			throw new SDMXRegistryClientException(msg);
		}
		ps.print(queryDocument);
		ps.close();
		InputStream is;
		try {
			is = urlc.getInputStream();
		} catch (IOException e) {
			String msg = "Unable to read response from registry";
			log.error(msg, e);
			throw new SDMXRegistryClientException(msg);
		}

		return is;
	}

	private InputStream GETQuery(String queryUrl)
			throws SDMXRegistryClientException {
		URL url;
		try {
			url = new URL(queryUrl);
		} catch (MalformedURLException e) {
			log.error("Invalid query URL was generated: " + queryUrl);
			throw new SDMXRegistryClientException("Syntax error");
		}
		URLConnection urlc;
		try {
			urlc = url.openConnection();
		} catch (IOException e) {
			log.error("Unable to open a connection to the registry", e);
			throw new SDMXRegistryClientException("Unable to contact registry");
		}
		urlc.setDoOutput(false);
		urlc.setAllowUserInteraction(false);
		urlc.setRequestProperty("Accept", "application/xml;version="
				+ interfaceType.getModelVersion());
		// urlc.addRequestProperty("Content-Type",
		// "application/xml;version="+interfaceType.getModelVersion());
		InputStream is;
		String response;
		try {
			log.trace("Performing GET Query with URL: " + urlc.getURL());
			is = urlc.getInputStream();
			response = IOUtils.toString(is);
			log.trace("Registry response:\n" + response);
		} catch (IOException e) {
			log.error("Unable to open a connection to the registry", e);
			throw new SDMXRegistryClientException(
					"Unable to read response message from registry");
		}

		return IOUtils.toInputStream(response);
	}

	private SdmxBeans getStructureBeansFromStream(InputStream is) {
		ReadableDataLocation structureLocation = new ReadableDataLocationTmp(is);
		StructureWorkspace workspace = structureParsingManager
				.parseStructures(structureLocation);
		return workspace.getStructureBeans(false);
	}

	private void logServerMessage(String serverResponse) {
		log.trace("Registry response:\n" + serverResponse);
	}

	private void testForErrorMessage(String serverResponse)
			throws SDMXRegistryClientException {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document document = dBuilder.parse(IOUtils.toInputStream(serverResponse));

			XPath xpath = XPathFactory.newInstance().newXPath();

			Node errorNode = (Node) xpath.evaluate("/Error", document,
					XPathConstants.NODE);

			if (errorNode == null)
				return;

			Double code = (Double) xpath.evaluate("/Error/ErrorMessage/@code",
					document, XPathConstants.NUMBER);
			log.trace("Error message code: " + String.valueOf(code.intValue()));
			String errorMsg = (String) xpath.evaluate(
					"/Error/ErrorMessage/Text/text()", document,
					XPathConstants.STRING);
			log.trace("Error message text: " + errorMsg);

			throw RegistryClientExceptionFactory.getException(errorMsg,
					code.intValue());
		} catch (SDMXRegistryClientException e) {
			log.info("Caught error message from registry: " + e);
			throw e;
		} catch (Exception e) {
			String msg = "Exception caught while checking for error messages in server response";
			log.error(msg, e);
			throw new SDMXRegistryClientException(msg);
		}

	}

}
