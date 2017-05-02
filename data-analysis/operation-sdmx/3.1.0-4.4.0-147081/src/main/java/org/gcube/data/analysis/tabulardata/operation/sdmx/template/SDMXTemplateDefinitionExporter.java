package org.gcube.data.analysis.tabulardata.operation.sdmx.template;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gcube.data.analysis.tabulardata.commons.templates.model.ReferenceObject;
import org.gcube.data.analysis.tabulardata.commons.templates.model.Template;
import org.gcube.data.analysis.tabulardata.commons.templates.model.columns.ColumnCategory;
import org.gcube.data.analysis.tabulardata.commons.templates.model.columns.TemplateColumn;
import org.gcube.data.analysis.tabulardata.commons.utils.DimensionReference;
import org.gcube.data.analysis.tabulardata.cube.CubeManager;
import org.gcube.data.analysis.tabulardata.cube.data.connection.DatabaseConnectionProvider;
import org.gcube.data.analysis.tabulardata.model.resources.InternalURI;
import org.gcube.data.analysis.tabulardata.model.resources.ResourceType;
import org.gcube.data.analysis.tabulardata.model.table.Table;
import org.gcube.data.analysis.tabulardata.operation.invocation.OperationInvocation;
import org.gcube.data.analysis.tabulardata.operation.sdmx.WorkerUtils;
import org.gcube.data.analysis.tabulardata.operation.sdmx.codelist.SDMXCodelistGenerator;
import org.gcube.data.analysis.tabulardata.operation.sdmx.security.Credentials;
import org.gcube.data.analysis.tabulardata.operation.sdmx.template.conceptscheme.SDMXConceptSchemeGeneratorFromTemplate;
import org.gcube.data.analysis.tabulardata.operation.worker.exceptions.WorkerException;
import org.gcube.data.analysis.tabulardata.operation.worker.results.ResourcesResult;
import org.gcube.data.analysis.tabulardata.operation.worker.results.resources.ImmutableURIResult;
import org.gcube.data.analysis.tabulardata.operation.worker.types.ResourceCreatorWorker;
import org.gcube.datapublishing.sdmx.api.registry.SDMXRegistryClient;
import org.gcube.datapublishing.sdmx.impl.exceptions.SDMXRegistryClientException;
import org.gcube.datapublishing.sdmx.impl.exceptions.SDMXVersionException;
import org.sdmxsource.sdmx.api.constants.ATTRIBUTE_ATTACHMENT_LEVEL;
import org.sdmxsource.sdmx.api.constants.SDMX_STRUCTURE_TYPE;
import org.sdmxsource.sdmx.api.model.beans.codelist.CodelistBean;
import org.sdmxsource.sdmx.api.model.beans.reference.StructureReferenceBean;
import org.sdmxsource.sdmx.api.model.mutable.base.RepresentationMutableBean;
import org.sdmxsource.sdmx.api.model.mutable.codelist.CodelistMutableBean;
import org.sdmxsource.sdmx.api.model.mutable.conceptscheme.ConceptMutableBean;
import org.sdmxsource.sdmx.api.model.mutable.conceptscheme.ConceptSchemeMutableBean;
import org.sdmxsource.sdmx.api.model.mutable.datastructure.AttributeMutableBean;
import org.sdmxsource.sdmx.api.model.mutable.datastructure.DataStructureMutableBean;
import org.sdmxsource.sdmx.api.model.mutable.datastructure.DataflowMutableBean;
import org.sdmxsource.sdmx.api.model.mutable.datastructure.DimensionMutableBean;
import org.sdmxsource.sdmx.api.model.mutable.datastructure.PrimaryMeasureMutableBean;
import org.sdmxsource.sdmx.sdmxbeans.model.mutable.base.RepresentationMutableBeanImpl;
import org.sdmxsource.sdmx.sdmxbeans.model.mutable.datastructure.AttributeMutableBeanImpl;
import org.sdmxsource.sdmx.sdmxbeans.model.mutable.datastructure.DataStructureMutableBeanImpl;
import org.sdmxsource.sdmx.sdmxbeans.model.mutable.datastructure.DimensionMutableBeanImpl;
import org.sdmxsource.sdmx.sdmxbeans.model.mutable.datastructure.PrimaryMeasureMutableBeanImpl;
import org.sdmxsource.sdmx.sdmxbeans.model.mutable.metadatastructure.DataflowMutableBeanImpl;
import org.sdmxsource.sdmx.util.beans.reference.StructureReferenceBeanImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SDMXTemplateDefinitionExporter extends ResourceCreatorWorker {

	private Logger log;
	private Template template;
	private List<TemplateColumn<?>> templateColumns;
	private TemplateColumn<?> primaryMeasure;
	private OperationInvocation invocation;
	private String registryUrl;
	private String targetAgency;
	private String targetId;
	private String targetVersion;
	private CubeManager cubeManager;
	private final String ATTRIBUTE_ASSIGNMENT_STATUS_MANDATORY = "Mandatory";
	private static String errorMessage = "Unable to complete export procedure";;
	private DatabaseConnectionProvider connectionProvider;
	private SDMXConceptSchemeGeneratorFromTemplate conceptSchemeGenerator;
	private String username;
	private String password;
	
	
	public SDMXTemplateDefinitionExporter(Template template, DatabaseConnectionProvider connectionProvider, OperationInvocation invocation,CubeManager cubeManager) {
		super(invocation);
		this.log = LoggerFactory.getLogger(this.getClass());
		this.template = template;
		this.invocation = invocation;
		this.connectionProvider = connectionProvider;
		this.cubeManager = cubeManager;
		this.username = null;
		this.password = null;
		this.log.debug("Worker instantiated");

	}
	
	@Override
	protected ResourcesResult execute() throws WorkerException 
	{
		
		try 
		{
			log.debug("Execution");
			init();
			updateProgress(0.1f,"Creating beans");
			DataStructureMutableBean dataStructure = createDataStructureBean();
			ConceptSchemeMutableBean conceptScheme = conceptSchemeGenerator.createConceptSchemeBean();
			DataflowMutableBean dataFlow = createDataFlowBean(dataStructure);
			updateProgress(0.2f,"Populating data structure");
			Set<CodelistBean> codelists = new HashSet<>();
			populateDataStructure(dataStructure, conceptScheme,codelists);
			updateProgress(0.6f,"Publishing");
			publishData(dataStructure, conceptScheme,dataFlow,codelists);
			updateProgress(0.8f,"Finalizing");

			return new ResourcesResult(new ImmutableURIResult(new InternalURI(new URI(registryUrl)), "Dataset SDMX export" , 
					String.format("%s/%s/%s/%s/", registryUrl, targetAgency, targetId, targetVersion), ResourceType.SDMX));
		} catch (RuntimeException e) {
			log.error(errorMessage, e);
			throw new WorkerException(errorMessage, e);
		} catch (URISyntaxException e) {
			throw new WorkerException(String.format("exported url %s not valid",registryUrl),e);
		}
	}


	/**
	 * 
	 * @param datasourceConfigurationBean
	 */
	private void init () 
	{
		this.log.debug("Init internal variables");
		this.templateColumns = new ArrayList<>(template.getActualStructure());
		registryUrl = (String) invocation.getParameterInstances().get(TemplateWorkerUtils.REGISTRY_BASE_URL);
		targetAgency = (String) invocation.getParameterInstances().get(TemplateWorkerUtils.AGENCY);
		targetId = (String) invocation.getParameterInstances().get(TemplateWorkerUtils.ID);
		targetVersion = (String) invocation.getParameterInstances().get(TemplateWorkerUtils.VERSION);
		Credentials credentials = WorkerUtils.retrieveCredentials(registryUrl);
		username = credentials.getUsername();
		password = credentials.getPassword();
		String observationValue = (String) this.invocation.getParameterInstances().get(TemplateWorkerUtils.OBS_VALUE_COLUMN);
		primaryMeasure = getPrimaryMeasure(observationValue);
		this.conceptSchemeGenerator = new SDMXConceptSchemeGeneratorFromTemplate(this.template, this.targetId,this.targetAgency, this.targetVersion);
	}

	
	
	private TemplateColumn<?> getPrimaryMeasure (String observationValue)
	{
		log.debug("Loading primary measure");
		log.debug("For obs value "+observationValue);
		int size = this.templateColumns.size();
		TemplateColumn<?> response = null;
		
		for (int i=0; (response == null && i<size); i++)
		{
			TemplateColumn<?> templateColumn = this.templateColumns.get(i);
			String columnID = templateColumn.getId();
			ColumnCategory columnType = templateColumn.getColumnType();
			log.debug("Column label "+templateColumn.getLabel());
			log.debug("Column label "+templateColumn.getId());
			log.debug("Column type "+columnType);
			
			if (columnType == ColumnCategory.MEASURE &&columnID.equals(observationValue))
			{
				log.debug("Primary measure found");
				response = this.templateColumns.remove(i);
			}

		}
		return response;
	}
	

	/**
	 * 
	 * @param dsd
	 * @param concepts
	 * @param dataFlow
	 * @throws WorkerException
	 */
	
	private void publishData(DataStructureMutableBean dsd, ConceptSchemeMutableBean concepts,DataflowMutableBean dataFlow, Set<CodelistBean> associatedCodelists) throws WorkerException {
		String url = (String) invocation.getParameterInstances().get(TemplateWorkerUtils.REGISTRY_BASE_URL);
		SDMXRegistryClient registryClient = TemplateWorkerUtils.initSDMXClient(url,this.username, this.password);
		String currentType = null;
		
		try {
			
			log.debug("Publishing associated codelists...");
			currentType = "codelists";
			
			for (CodelistBean codelist : associatedCodelists)
			{
				log.debug("Publishing codelist "+codelist.getId());
				registryClient.publish(codelist);
				log.debug("Codelist published");
			}
		
			log.debug("Codelists published");
			currentType = "concepts";
			log.debug("Publishing concepts...");
			registryClient.publish(concepts.getImmutableInstance());
			log.debug("Concepts published");
			currentType = "data structure definitions";
			log.debug("Publishing dsd...");
			registryClient.publish(dsd.getImmutableInstance());
			log.debug("DSD published");
			currentType = "data flow";
			log.debug("Publishing data flow...");
			registryClient.publish(dataFlow.getImmutableInstance());
			log.debug("Data flow published");
		} 		
		catch (SDMXVersionException e) {
			throw new WorkerException(e.getMessage(), e);
		}
		 
		catch (SDMXRegistryClientException e) {
			throw new WorkerException("Unable to publish dsd on registry: error in the"+currentType, e);
		}
		

	}
	
	private TemplateColumn<?> getColumnsByType (List<TemplateColumn<?>> measureColumns,List<TemplateColumn<?>> dimensionColumns,List<TemplateColumn<?>> attributeColumns)
	{
		log.debug("Classifying columns per type");
		TemplateColumn<?> timeDimension = null;
		
		for (TemplateColumn<?> templateColumn : this.templateColumns)
		{
			ColumnCategory columnType = templateColumn.getColumnType();
			
			switch (columnType)
			{
			case ATTRIBUTE:
				log.debug("Attribute");
				attributeColumns.add(templateColumn);
				break;
			case TIMEDIMENSION:
				log.debug("Time dimension");
				timeDimension = templateColumn;
				break;
			case MEASURE:
				log.debug("Measure");
				measureColumns.add(templateColumn);
				break;
			case DIMENSION:
				log.debug("Dimension");
				dimensionColumns.add(templateColumn);
				break;
				default:
				
			}

		}
		return timeDimension;
	}
	

	private void populateDataStructure (DataStructureMutableBean dataStructure,ConceptSchemeMutableBean conceptScheme,Set<CodelistBean> codelists) throws WorkerException 
	{

		log.debug("Loading column data");
		List<TemplateColumn<?>> measureColumns = new ArrayList<>();
		List<TemplateColumn<?>> dimensionColumns = new ArrayList<>();
		List<TemplateColumn<?>> attributeColumns = new ArrayList<>();
		TemplateColumn<?> timeDimensionColumn = getColumnsByType(measureColumns, dimensionColumns, attributeColumns);
		log.debug("Columns loaded");

		ConceptMutableBean primaryMeasureConcept = this.conceptSchemeGenerator.createConceptBean(this.primaryMeasure);

		conceptScheme.addItem(primaryMeasureConcept);
		StructureReferenceBean conceptReferenceBean = new StructureReferenceBeanImpl (primaryMeasureConcept.getParentAgency(),conceptScheme.getId(),
				this.targetVersion,SDMX_STRUCTURE_TYPE.CONCEPT,primaryMeasureConcept.getId());
		
		PrimaryMeasureMutableBean primaryMeasureBean = new PrimaryMeasureMutableBeanImpl();
		primaryMeasureBean.setConceptRef(conceptReferenceBean);
		primaryMeasureBean.setId("OBS_VALUE");
		dataStructure.setPrimaryMeasure(primaryMeasureBean);
		addMeasureDimensions(dataStructure, measureColumns,conceptScheme);
		addGenericDimensions(dataStructure, dimensionColumns,conceptScheme,codelists);
		DimensionMutableBean timeDimensionBean = new DimensionMutableBeanImpl();
		timeDimensionBean.setId("TIME_PERIOD");
		ConceptMutableBean timeDimensionConcept = this.conceptSchemeGenerator.createConceptBean(timeDimensionColumn);
		conceptScheme.addItem(timeDimensionConcept);
		timeDimensionBean.setConceptRef(getConceptReference(conceptScheme, timeDimensionConcept));
		timeDimensionBean.setTimeDimension(true);
		dataStructure.addDimension(timeDimensionBean);
		addAttributes(dataStructure, attributeColumns,conceptScheme,codelists);
	}
	
	/**
	 * 
	 * @param dimensionListBean
	 * @param dimensionColumns
	 * @param concepts
	 */
	private void addGenericDimensions (DataStructureMutableBean dsd, List<TemplateColumn<?>> dimensionColumns,ConceptSchemeMutableBean concepts, Set<CodelistBean> codelists)
	{
		log.debug("Adding dimension list bean");
		for (TemplateColumn<?> column : dimensionColumns)
		{
			ConceptMutableBean columnConcept = this.conceptSchemeGenerator.createConceptBean(column);
			concepts.addItem(columnConcept);
			RepresentationMutableBean codedRepresentation = createCodeListRepresentation(column,codelists);
			
			if (codedRepresentation != null)
			{
				log.debug("Found a code list representation");
				columnConcept.setCoreRepresentation(codedRepresentation);			
			}
		
			StructureReferenceBean conceptsReference = getConceptReference(concepts, columnConcept);		
			DimensionMutableBean dimension = new DimensionMutableBeanImpl();
			dimension.setId(column.getId()+"_DSD");
			dimension.setConceptRef(conceptsReference);
			dsd.addDimension(dimension);
			}	
	}
	
	
	/**
	 * 
	 * @param dataStructure
	 * @param attributeColumns
	 * @param concepts
	 */
	private void addAttributes (DataStructureMutableBean dataStructure, List<TemplateColumn<?>> attributeColumns,ConceptSchemeMutableBean concepts,Set<CodelistBean> codelists)
	{
		log.debug("Adding attribute list bean");
		for (TemplateColumn<?> column : attributeColumns)
		{
			ConceptMutableBean columnConcept = this.conceptSchemeGenerator.createConceptBean(column);
			concepts.addItem(columnConcept);
			
			AttributeMutableBean attributeBean = new AttributeMutableBeanImpl();
			attributeBean.setAttachmentLevel(ATTRIBUTE_ATTACHMENT_LEVEL.OBSERVATION);
			attributeBean.setAssignmentStatus(ATTRIBUTE_ASSIGNMENT_STATUS_MANDATORY);
			RepresentationMutableBean codedRepresentation = createCodeListRepresentation(column,codelists);	
			
			
			if (codedRepresentation != null)
			{
				log.debug("Found a code list representation");
				columnConcept.setCoreRepresentation(codedRepresentation);
			}

			attributeBean.setConceptRef(getConceptReference(concepts, columnConcept));
			dataStructure.addAttribute(attributeBean);
		}	
	}
	
	/**
	 * 
	 * @param dimensionListBean
	 * @param measureColumns
	 * @param concepts
	 */
	private void addMeasureDimensions (DataStructureMutableBean dsd, List<TemplateColumn<?>> measureColumns,ConceptSchemeMutableBean concepts)
	{
		log.debug("Adding measure dimension list bean");
		for (TemplateColumn<?> column : measureColumns)
		{
			ConceptMutableBean columnConcept = this.conceptSchemeGenerator.createConceptBean(column);
			concepts.addItem(columnConcept);
			DimensionMutableBean dimensionBean = new DimensionMutableBeanImpl();
			dimensionBean.setMeasureDimension(true);
			dimensionBean.setConceptRef(getConceptReference(concepts, columnConcept));
			dsd.addDimension(dimensionBean);
		}	
	}
	
	/**
	 * 
	 * @param conceptScheme
	 * @param concept
	 * @return
	 */
	private StructureReferenceBean getConceptReference (ConceptSchemeMutableBean conceptScheme, ConceptMutableBean concept)
	{

		
		log.debug("Creating reference for concept "+concept.getId());
		return new StructureReferenceBeanImpl (concept.getParentAgency(),conceptScheme.getId(),
				this.targetVersion,SDMX_STRUCTURE_TYPE.CONCEPT,concept.getId());
	}

	/**
	 * 
	 * @param column
	 * @param codelists
	 * @return
	 */
	private RepresentationMutableBean createCodeListRepresentation (TemplateColumn<?> column,Set<CodelistBean> codelists)
	{
	
		log.debug("Creating codelist bean");
		RepresentationMutableBean response = null;
		Table codelist = getAssociatedCodelist(column);
		
		if (codelist != null)
		{
			log.debug("Table found "+codelist.getName());
			SDMXCodelistGenerator codeListGenerator = new SDMXCodelistGenerator(codelist, this.connectionProvider, this.targetAgency, codelist.getName()+"_CL", this.targetVersion);
			CodelistMutableBean codeListBean =codeListGenerator.createBaseCodelistBean();

			try
			{
				codeListGenerator.populateCodelistWithCodes(codeListBean);
				CodelistBean immutableCodelist = codeListBean.getImmutableInstance();
				codelists.add(immutableCodelist);
				response = new RepresentationMutableBeanImpl();
				response.setRepresentation(new StructureReferenceBeanImpl(immutableCodelist));
				
				
			} catch (Exception e)
			{
				log.warn("Codelist not loaded",e);
				
			}
		}
		
		return response;
	}

	
	/**
	 * 
	 * @return
	 */
	private DataStructureMutableBean createDataStructureBean() {
		DataStructureMutableBean dataStructure = new DataStructureMutableBeanImpl();
		dataStructure.setAgencyId(this.targetAgency);
		dataStructure.setVersion(this.targetVersion);
		String dsdId = this.targetId+"_DSD";
		dataStructure.setId(dsdId);
		dataStructure.addName("en", this.targetId+" Data Structure");
		return dataStructure;
	}
	
	/**
	 * 
	 * @param dataStructure
	 * @return
	 */
	private DataflowMutableBean createDataFlowBean (DataStructureMutableBean dataStructure)
	{
		DataflowMutableBean dataFlow = new DataflowMutableBeanImpl();
		log.debug("Populating data flow bean");
		dataFlow.setAgencyId(this.targetAgency);
		dataFlow.setDataStructureRef(new StructureReferenceBeanImpl (dataStructure.getAgencyId(),dataStructure.getId(), this.targetVersion,SDMX_STRUCTURE_TYPE.DSD));
		dataFlow.setId(this.targetId+"_dataFlow");
		dataFlow.setVersion(this.targetVersion);
		dataFlow.addName("en", this.targetId+" Data Flow");
		return dataFlow;
	}




	/**
	 * 
	 * @param column
	 * @return
	 */
	private Table getAssociatedCodelist (TemplateColumn<?> column)
	{

		log.debug("Looking for table associated to column "+column.getId());
		Table response = null;
		ReferenceObject reference = column.getReference();
    	
		if (reference instanceof DimensionReference)
		{
			log.debug("Found a table reference for Dimension column");
			DimensionReference dimensionReference = (DimensionReference) reference;
    		log.debug("Loading referenced table...");
			Table relatedTable = this.cubeManager.getTable(dimensionReference.getTableId());
			
			if (relatedTable.getTableType().getCode().equals("CODELIST"))
			{
				log.debug("Table found "+relatedTable.getName());
				response = relatedTable;
			}
			else
			{
				log.debug("Referenced table is not a codelist");
			}
		}    	
    	return response;
	}
	
	

}