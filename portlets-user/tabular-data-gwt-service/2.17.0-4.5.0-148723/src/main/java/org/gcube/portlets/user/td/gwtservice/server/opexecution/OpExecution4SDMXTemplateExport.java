package org.gcube.portlets.user.td.gwtservice.server.opexecution;

import static org.gcube.resources.discovery.icclient.ICFactory.clientFor;
import static org.gcube.resources.discovery.icclient.ICFactory.queryFor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gcube.common.resources.gcore.ServiceEndpoint;
import org.gcube.common.resources.gcore.ServiceEndpoint.AccessPoint;
import org.gcube.common.resources.gcore.ServiceEndpoint.Profile;
import org.gcube.common.resources.gcore.utils.Group;
import org.gcube.data.analysis.tabulardata.commons.templates.model.Template;
import org.gcube.data.analysis.tabulardata.commons.webservice.exception.NoSuchTemplateException;
import org.gcube.data.analysis.tabulardata.commons.webservice.types.TemplateDescription;
import org.gcube.data.analysis.tabulardata.commons.webservice.types.operations.OperationDefinition;
import org.gcube.data.analysis.tabulardata.commons.webservice.types.operations.OperationExecution;
import org.gcube.data.analysis.tabulardata.service.TabularDataService;
import org.gcube.data.analysis.tabulardata.service.template.TemplateId;
import org.gcube.portlets.user.td.gwtservice.server.trservice.OperationDefinitionMap;
import org.gcube.portlets.user.td.gwtservice.shared.Constants;
import org.gcube.portlets.user.td.gwtservice.shared.exception.TDGWTServiceException;
import org.gcube.portlets.user.td.gwtservice.shared.sdmx.SDMXTemplateExportSession;
import org.gcube.portlets.user.td.widgetcommonevent.shared.operations.OperationsId;
import org.gcube.resources.discovery.client.api.DiscoveryClient;
import org.gcube.resources.discovery.client.queries.api.SimpleQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operation Execution for sdmx Template export
 * 
 * @author Giancarlo Panichi
 *
 * 
 */
public class OpExecution4SDMXTemplateExport extends OpExecutionBuilder {
	private static Logger logger = LoggerFactory
			.getLogger(OpExecution4SDMXTemplateExport.class);

	private TabularDataService service;
	private SDMXTemplateExportSession sdmxTemplateExportSession;

	public OpExecution4SDMXTemplateExport(TabularDataService service,
			SDMXTemplateExportSession sdmxTemplateExportSession) {
		this.service = service;
		this.sdmxTemplateExportSession = sdmxTemplateExportSession;
	}

	@Override
	public void buildOpEx() throws TDGWTServiceException {
		logger.debug(sdmxTemplateExportSession.toString());
		boolean internalRegistry = false;
		String destination = null;// Es:
									// http://pc-fortunati.isti.cnr.it:8080/FusionRegistry/ws/rest/

		if (sdmxTemplateExportSession != null) {
			if (sdmxTemplateExportSession.getRegistryBaseUrl() != null
					&& !sdmxTemplateExportSession.getRegistryBaseUrl()
							.isEmpty()) {
				destination = sdmxTemplateExportSession.getRegistryBaseUrl();
			} else {
				internalRegistry = true;
			}
		} else {
			internalRegistry = true;
		}

		if (internalRegistry) {
			SimpleQuery query = queryFor(ServiceEndpoint.class);
			query.addCondition("$resource/Profile/Category/text() eq 'SDMX'")
					.addCondition(
							"$resource/Profile/Name/text() eq 'SDMXRegistry'");
			DiscoveryClient<ServiceEndpoint> client = clientFor(ServiceEndpoint.class);
			List<ServiceEndpoint> listService = client.submit(query);
			if (listService.size() > 0) {
				ServiceEndpoint serviceEnd = listService.get(0);
				if (serviceEnd != null) {
					Profile prof = serviceEnd.profile();
					Group<AccessPoint> groupA = prof.accessPoints();
					for (AccessPoint acc : groupA) {
						if (acc.description().compareTo("REST Interface v2.1") == 0) {
							destination = acc.address();
							break;
						}
					}
				} else {

				}
			} else {

			}
		}
		if (destination == null) {
			logger.debug("Destination: " + destination);
			throw new TDGWTServiceException("SDMX Service not discovered");
		}

		TemplateDescription templateDescriptor;
		try {
			templateDescriptor = service.getTemplate(new TemplateId(
					sdmxTemplateExportSession.getTemplateData().getId()));
		} catch (NoSuchTemplateException e) {
			logger.debug(
					"Error retrieving template: " + e.getLocalizedMessage(), e);
			throw new TDGWTServiceException("Error retrieving template: "
					+ e.getLocalizedMessage());
		}

		OperationDefinition operationDefinition = OperationDefinitionMap.map(
				OperationsId.SDMXTemplateExport.toString(), service);
		
		HashMap<String,Template> templateMap=new HashMap<String,Template>();
		templateMap.put(Constants.PARAMETER_TEMPLATE,templateDescriptor.getTemplate());
		
		Map<String, Object> map = new HashMap<String, Object>();

		map.put(Constants.PARAMETER_REGISTRYBASEURL, destination);
		map.put(Constants.PARAMETER_AGENCY,
				sdmxTemplateExportSession.getAgencyId());
		map.put(Constants.PARAMETER_ID, sdmxTemplateExportSession.getId());
		map.put(Constants.PARAMETER_VERSION,
				sdmxTemplateExportSession.getVersion());
		map.put(Constants.PARAMETER_OBSVALUECOLUMN, sdmxTemplateExportSession
				.getObsValueColumn().getColumnId());
		map.put(Constants.PARAMETER_TEMPLATE, templateMap);
		OperationExecution invocation = new OperationExecution(
				operationDefinition.getOperationId(), map);

		operationExecutionSpec.setOp(invocation);
	}

}
