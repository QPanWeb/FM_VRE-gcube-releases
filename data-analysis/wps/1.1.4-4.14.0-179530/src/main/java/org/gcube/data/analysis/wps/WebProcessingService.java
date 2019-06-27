package org.gcube.data.analysis.wps;
/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *       • Apache License, version 2.0
 *       • Apache Software License, version 1.0
 *       • GNU Lesser General Public License, version 3
 *       • Mozilla Public License, versions 1.0, 1.1 and 2.0
 *       • Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
org.gcube.dataanalysis.wps.statisticalmanager.synchserver.capabilities52.wps.server;
 */
// FvK: added Property Change Listener support
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.xmlbeans.XmlException;
import org.gcube.data.analysis.wps.repository.AlgorithmUpdater;
import org.gcube.data.analysis.wps.repository.GcubeAlgorithmRepository;
import org.gcube.dataanalysis.wps.statisticalmanager.synchserver.mapping.EnvironmentVariableManager;
import org.gcube.smartgears.ContextProvider;
import org.gcube.smartgears.context.application.ApplicationContext;
import org.n52.wps.GeneratorDocument.Generator;
import org.n52.wps.ParserDocument.Parser;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.server.CapabilitiesConfiguration;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.database.DatabaseFactory;
import org.n52.wps.util.XMLBeansHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This WPS supports HTTP GET for describeProcess and getCapabilities and XML-POST for execute.
 *
 * @author foerster
 *
 */
public class WebProcessingService extends HttpServlet {

	// Universal version identifier for a Serializable class.
	// Should be used here, because HttpServlet implements the java.io.Serializable
	private static final long serialVersionUID = 8943233273641771839L;
	public static String PROPERTY_NAME_WEBAPP_PATH = "webappPath";
	public static String BASE_DIR = null;
	public static String WEBAPP_PATH = null;
	public static String SERVLET_PATH = "WebProcessingService";
	public static String WPS_NAMESPACE = "http://www.opengis.net/wps/1.0.0";
	public static String DEFAULT_LANGUAGE = "en-US";
	protected static Logger LOGGER = LoggerFactory.getLogger(WebProcessingService.class);

	private static final String DIRECTORY_PARAM = "algorithmDirectory";
	
	private static final String MAX_COMPUTATION_PARAM = "maxComputation";
	private static final String SAVEONSTORAGE_PARAM = "saveOnStorage";
	private static final String SIMULATION_MODE_PARAM = "simulationMode";
	private static final String USER_WIRTE_EXCLUDED_PARAM = "excludeWritesOnSH";
	
	private ApplicationContext context = ContextProvider.get();
	private EnvironmentVariableManager env ;
	
		
	@Override
	public void init() throws ServletException {
		LOGGER.info("initialising WPS");
		String  dir = (String)context.application().getInitParameter(DIRECTORY_PARAM);
		GcubeAlgorithmRepository.setUpdater(new AlgorithmUpdater(dir));
		
		int maxComp = Integer.parseInt(context.application().getInitParameter(MAX_COMPUTATION_PARAM));
		boolean saveOnStorage = Boolean.parseBoolean(context.application().getInitParameter(SAVEONSTORAGE_PARAM));
		boolean simulationMode = Boolean.parseBoolean(context.application().getInitParameter(SIMULATION_MODE_PARAM));
		
		String userWriteExcludedValue = context.application().getInitParameter(USER_WIRTE_EXCLUDED_PARAM);
		
		
		
		List<String> excludedUserForWrite = null;
		if (userWriteExcludedValue.isEmpty() || userWriteExcludedValue==null) {
			excludedUserForWrite = Collections.emptyList(); 
			LOGGER.debug("none is excluded in write");
		}else if(!userWriteExcludedValue.equals("*")){
			excludedUserForWrite = Arrays.asList(userWriteExcludedValue.split(","));
			excludedUserForWrite.stream().forEach(i -> i.trim());
			LOGGER.debug("users excluded in write are {}",excludedUserForWrite);
		} else 
			LOGGER.debug("all users excluded in write");
		
		env = new EnvironmentVariableManager(maxComp, saveOnStorage, simulationMode, excludedUserForWrite);
		LOGGER.info("WPS initialised");
	}

	/**
	 *
	 * Returns a preconfigured OutputStream It takes care of: - caching - content-Encoding
	 *
	 * @param hsRequest
	 *        the HttpServletRequest
	 * @param hsResponse
	 *        the HttpServlerResponse
	 * @return the preconfigured OutputStream
	 * @throws IOException
	 *         a task of the tomcat
	 */
	@SuppressWarnings("unused")
	private static OutputStream getConfiguredOutputStream(HttpServletRequest hsRequest, HttpServletResponse hsResponse) throws IOException {
		/*
		 * Forbids clients to cache the response May solve problems with proxies and bad implementations
		 */

		hsResponse.setHeader("Expires", "0");
		if (hsRequest.getProtocol().equals("HTTP/1.1")) {
			hsResponse.setHeader("Cache-Control", "no-cache");
		} else if (hsRequest.getProtocol().equals("HTTP/1.0")) {
			hsResponse.setHeader("Pragma", "no-cache");
		}

		// Enable/disable gzip compression
		if (hsRequest.getHeader("Accept-Encoding") != null
				&& hsRequest.getHeader("Accept-Encoding").indexOf("gzip") >= 0) {
			hsResponse.setHeader("Content-Encoding", "gzip");
			LOGGER.info("gzip-Compression for output enabled");
			return new GZIPOutputStream(hsResponse.getOutputStream());
		} // else {
		LOGGER.info("gzip-Compression for output disabled");
		return hsResponse.getOutputStream();
		// }
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		// this is important to set the lon lat support for correct CRS transformation.

		System.setProperty("org.geotools.referencing.forceXY", "true");

		LOGGER.info("WebProcessingService initializing...");

		try {
			if (WPSConfig.getInstance(config) == null) {
				LOGGER.error("Initialization failed! Please look at the properties file!");
				return;
			}
		}
		catch (Exception e) {
			LOGGER.error("Initialization failed! Please look at the properties file!", e);
			return;
		}
		LOGGER.info("Initialization of wps properties successful!");

		BASE_DIR = this.getServletContext().getRealPath("");

		Parser[] parsers = WPSConfig.getInstance().getActiveRegisteredParser();
		ParserFactory.initialize(parsers);

		Generator[] generators = WPSConfig.getInstance().getActiveRegisteredGenerator();
		GeneratorFactory.initialize(generators);

		// call RepositoyManager to initialize
		LOGGER.info("Algorithms initialized");

		// String customWebappPath = WPSConfiguration.getInstance().getProperty(PROPERTY_NAME_WEBAPP_PATH);
		String customWebappPath = WPSConfig.getInstance().getWPSConfig().getServer().getWebappPath();
		if (customWebappPath != null) {
			WEBAPP_PATH = customWebappPath;
		}
		else {
			WEBAPP_PATH = "wps";
			LOGGER.warn("No custom webapp path found, use default wps");
		}
		LOGGER.info("webappPath is set to: " + customWebappPath);

		try {
			CapabilitiesConfiguration.getInstance(BASE_DIR + File.separator + "config"
					+ File.separator + "wpsCapabilitiesSkeleton.xml");
		}
		catch (IOException e) {
			LOGGER.error("error while initializing capabilitiesConfiguration", e);
		}
		catch (XmlException e) {
			LOGGER.error("error while initializing capabilitiesConfiguration", e);
		}

		// Get an instance of the database for initialization of the database
		DatabaseFactory.getDatabase();

		LOGGER.info("WPS up and running!");

		// FvK: added Property Change Listener support
		// creates listener and register it to the wpsConfig instance.
		// it will listen to changes of the wpsCapabilities
		WPSConfig.getInstance().addPropertyChangeListener(org.n52.wps.commons.WPSConfig.WPSCAPABILITIES_SKELETON_PROPERTY_EVENT_NAME,
				new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent propertyChangeEvent) {
				LOGGER.info(this.getClass().getName()
						+ ": Received Property Change Event: "
						+ propertyChangeEvent.getPropertyName());
				try {
					CapabilitiesConfiguration.reloadSkeleton();
				}
				catch (IOException e) {
					LOGGER.error("error while initializing capabilitiesConfiguration",
							e);
				}
				catch (XmlException e) {
					LOGGER.error("error while initializing capabilitiesConfiguration",
							e);
				}
			}
		});

		// FvK: added Property Change Listener support
		// creates listener and register it to the wpsConfig instance.
		// it will listen to changes of the wpsConfiguration
		WPSConfig.getInstance().addPropertyChangeListener(org.n52.wps.commons.WPSConfig.WPSCONFIG_PROPERTY_EVENT_NAME,
				new PropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent propertyChangeEvent) {
				LOGGER.info(this.getClass().getName()
						+ ": Received Property Change Event: "
						+ propertyChangeEvent.getPropertyName());
				try {
					CapabilitiesConfiguration.reloadSkeleton();
				}
				catch (IOException e) {
					LOGGER.error("error while initializing capabilitiesConfiguration",
							e);
				}
				catch (XmlException e) {
					LOGGER.error("error while initializing capabilitiesConfiguration",
							e);
				}
			}
		});

	}

	protected void setServerParameters(){
		//String endpoint = CapabilitiesConfiguration.ENDPOINT_URL;

		String webapp = context.application().getContextPath().replace("//", "");
		WPSConfig.getInstance().getWPSConfig().getServer().setWebappPath(webapp);
		
		
		String host = WPSConfig.getInstance().getWPSConfig().getServer().getHostname();
		String port = WPSConfig.getInstance().getWPSConfig().getServer().getHostport();

		if (host==null || host.toLowerCase().equals("localhost")){
			LOGGER.info("resolving hostname and port from container.xml");
			host = context.container().configuration().hostname();
			port = Integer.toString(context.container().configuration().port());
			WPSConfig.getInstance().getWPSConfig().getServer().setHostname(host);
			WPSConfig.getInstance().getWPSConfig().getServer().setHostport(port);
		}


		LOGGER.debug("Setting server parameters: Host: {}, Port: {} , Webapp: {} ", host, port,webapp);
		//TODO: CHANGE the porotocol to enable https via conainer configuration
		String webPath = "http://" + host + ":" + port + "/" + webapp + "/WebProcessingService";
		if (CapabilitiesConfiguration.ENDPOINT_URL.contains("localhost"))
			CapabilitiesConfiguration.ENDPOINT_URL=webPath;

	}

	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		try {
			setServerParameters();
			OutputStream out = res.getOutputStream(); // closed by res.flushBuffer();
			
			@SuppressWarnings("unchecked")
			RequestHandler handler = new RequestHandler((Map<String, String[]>) req.getParameterMap(), out, env);
			String mimeType = handler.getResponseMimeType();
			res.setContentType(mimeType);
			handler.handle();

			res.setStatus(HttpServletResponse.SC_OK);
		}
		catch (ExceptionReport e) {
			handleException(e, res);
		}
		catch (RuntimeException e) {
			ExceptionReport er = new ExceptionReport("Error handing request: " + e.getMessage(),
					ExceptionReport.NO_APPLICABLE_CODE,
					e);
			handleException(er, res);
		}
		finally {
			if (res != null) {
				res.flushBuffer();
			}
			// out.flush();
			// out.close();
		}
	}

	public final static int MAXIMUM_REQUEST_SIZE = 128 << 20;
	public final static String SPECIAL_XML_POST_VARIABLE = "request";
	private static final String XML_CONTENT_TYPE = "text/xml";

	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		setServerParameters();
		BufferedReader reader = null;
		LOGGER.debug("Incoming Post Request");
		try {
			String contentType = req.getContentType();
			String characterEncoding = req.getCharacterEncoding();
			if (characterEncoding == null || characterEncoding.length() == 0) {
				characterEncoding = "UTF-8"; // default character encoding if unspecified
			}
			LOGGER.debug("Set Encoding to {}",characterEncoding);
			int contentLength = req.getContentLength();
			if (contentLength > MAXIMUM_REQUEST_SIZE) {
				LOGGER.warn("POST request rejected, request size of " + contentLength + " too large.");
				ExceptionReport er = new ExceptionReport("Request body too large, limited to " + MAXIMUM_REQUEST_SIZE
						+ " bytes", ExceptionReport.NO_APPLICABLE_CODE);
				handleException(er, res);
			}

			LOGGER.debug("Received POST: Content-Type = {}, Character-Encoding = {} , Content-Length = {} " , contentType, characterEncoding, contentLength);

			int requestSize = 0;

			StringWriter writer = contentLength > 0 ? new StringWriter(contentLength) : new StringWriter();
			reader = req.getReader();
			char[] buffer = new char[8192];
			int read;
			while ( (read = reader.read(buffer)) != -1 && requestSize < MAXIMUM_REQUEST_SIZE) {
				writer.write(buffer, 0, read);
				requestSize += read;
			}

			LOGGER.debug("POST request contained  {} characters", requestSize);

			// Protect against denial of service attacks.
			if (requestSize >= MAXIMUM_REQUEST_SIZE && reader.read() > -1) {
				LOGGER.warn("POST request rejected, request size of {} too large.",requestSize);
				ExceptionReport er = new ExceptionReport("Request body too large, limited to " + MAXIMUM_REQUEST_SIZE
						+ " bytes", ExceptionReport.NO_APPLICABLE_CODE);
				handleException(er, res);
			}

			String documentString = writer.toString();
			// Perform URL decoding, if necessary
			// if ("application/x-www-form-urlencoded".equals(contentType)) {
			if ( (contentType).startsWith("application/x-www-form-urlencoded")) {
				if (documentString.startsWith(SPECIAL_XML_POST_VARIABLE + "=")) {
					// This is a hack to permit xml to be easily submitted via a form POST.
					// By convention, we are allowing users to post xml if they name it
					// with a POST parameter "request" although this is not
					// valid per the specification.
					documentString = documentString.substring(SPECIAL_XML_POST_VARIABLE.length() + 1);
					LOGGER.debug("POST request form variable removed");
				}
				documentString = URLDecoder.decode(documentString, characterEncoding);
				LOGGER.debug("Decoded of POST:\n" + documentString + "\n");
			}
			else
				LOGGER.info("This is a standard xml document");

			RequestHandler handler = new RequestHandler(new ByteArrayInputStream(documentString.getBytes("UTF-8")),
					res.getOutputStream(), env);
			LOGGER.debug("POST Request Handler created");
			String mimeType = handler.getResponseMimeType();
			LOGGER.debug("Request mimeType: "+mimeType);
			res.setContentType(mimeType);
			LOGGER.debug("Handling document");

			handler.handle();

			LOGGER.debug("STATUS OK!");
			res.setStatus(HttpServletResponse.SC_OK);
		}
		catch (ExceptionReport e) {
			handleException(e, res);
		}
		catch (Exception e) {
			ExceptionReport er = new ExceptionReport("Error handing request: " + e.getMessage(), ExceptionReport.NO_APPLICABLE_CODE, e);
			handleException(er, res);
		}
		finally {
			if (res != null) {
				res.flushBuffer();
			}

			if (reader != null) {
				reader.close();
			}
			LOGGER.trace("Flushing request");
		}
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (SERVLET_PATH == null) {
			req.getContextPath();
		}
		super.service(req, res);
	}

	private static void handleException(ExceptionReport exception, HttpServletResponse res) {
		res.setContentType(XML_CONTENT_TYPE);
		try {
			LOGGER.debug(exception.toString());
			// DO NOT MIX getWriter and getOuputStream!
			exception.getExceptionDocument().save(res.getOutputStream(),
					XMLBeansHelper.getXmlOptions());

			res.setStatus(exception.getHTTPStatusCode());
		}
		catch (IOException e) {
			LOGGER.warn("exception occured while writing ExceptionReport to stream");
			try {
				res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"error occured, while writing OWS Exception output");
			}
			catch (IOException ex) {
				LOGGER.error("error while writing error code to client!");
				res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		DatabaseFactory.getDatabase().shutdown();
	}
}