package org.gcube.data.analysis.tabulardata.operation.sdmx.excel.impl.types.formats;

import java.util.Arrays;

import org.gcube.data.analysis.excel.metadata.format.DataFormat;
import org.gcube.data.analysis.excel.metadata.format.GenericMeasureFormat;
import org.gcube.data.analysis.tabulardata.commons.templates.model.columns.TemplateColumn;
import org.gcube.data.analysis.tabulardata.commons.utils.FormatReference;
import org.gcube.data.analysis.tabulardata.operation.sdmx.excel.impl.beans.ColumnBean;
import org.gcube.data.analysis.tabulardata.operation.sdmx.excel.impl.beans.TemplateBean;
import org.gcube.data.analysis.tabulardata.operation.sdmx.excel.impl.types.formats.data.DataFormatGeneratorData;
import org.gcube.data.analysis.tabulardata.operation.sdmx.excel.impl.types.formats.metadata.DataFormatGeneratorMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeasureDataFormatGenerator implements DataFormatGeneratorData, DataFormatGeneratorMetadata
{
	private Logger logger;
	
	public MeasureDataFormatGenerator() {
		this.logger = LoggerFactory.getLogger(this.getClass());
		
	}
	
	
	@Override
	public DataFormat getDataFormat (ColumnBean columnBean,String locale)
	{
		this.logger.debug("Attribute data format: data type measure ");
		String dataType = columnBean.getColumn().getDataType().getName();
		this.logger.debug("Data type "+dataType);
		return new GenericMeasureFormat(columnBean.getName(locale)+"_reference", Arrays.asList(dataType));
	}
	
	@Override
	public DataFormat getDataFormat (TemplateColumn<?> column,TemplateBean templateBean)
	{
		this.logger.debug("Attribute data format: data type measure ");
		String formatReference = ((FormatReference) column.getReference()).getFormatIdentifier();
		this.logger.debug("Data format "+formatReference);
		return new GenericMeasureFormat(column.getLabel()+"_reference", Arrays.asList(formatReference));
	}

}
