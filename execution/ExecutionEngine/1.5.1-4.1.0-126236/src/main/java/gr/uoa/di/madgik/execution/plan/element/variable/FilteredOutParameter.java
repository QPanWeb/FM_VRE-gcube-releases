package gr.uoa.di.madgik.execution.plan.element.variable;

import gr.uoa.di.madgik.commons.utils.XMLUtils;
import gr.uoa.di.madgik.execution.engine.ExecutionHandle;
import gr.uoa.di.madgik.execution.exception.ExecutionRunTimeException;
import gr.uoa.di.madgik.execution.exception.ExecutionSerializationException;
import gr.uoa.di.madgik.execution.exception.ExecutionValidationException;
import gr.uoa.di.madgik.execution.plan.element.filter.ParameterFilterBase;
import gr.uoa.di.madgik.execution.utils.ParameterUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class FilteredOutParameter implements IOutputParameter, Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public List<ParameterFilterBase> Filters=new ArrayList<ParameterFilterBase>();
	//IOutputParameter
	public String UpdateVariableName=null;
	
	private Set<String> GetFilterInputVariableNames()
	{
		Set<String> vars=new HashSet<String>();
		for(ParameterFilterBase filter: this.Filters)
		{
			vars.addAll(filter.GetInputVariableNames());
		}
		return vars;
	}
	
	public boolean CanSuggestParameterValueType(ExecutionHandle Handle)
	{
		return false;
	}
	public Class<?> SuggestParameterValueType(ExecutionHandle Handle)
	{
		return null;
	}
	
	private void UpdateParameterValue(ExecutionHandle Handle) throws ExecutionRunTimeException,ExecutionValidationException
	{
		Collections.sort(this.Filters);
		Object val=null;
		for(ParameterFilterBase filter: this.Filters)
		{
			val=filter.Process(Handle);
			if(filter.StoreOutput())
			{
				for(String storeOutputVarName : filter.GetStoreOutputVariableName())
				{
					Handle.GetPlan().Variables.Update(storeOutputVarName, val);
				}
			}
		}
	}
	
	public void SetParameterValue(ExecutionHandle Handle, Object Value) throws ExecutionRunTimeException,ExecutionValidationException
	{
		for(String varName : this.GetFilterInputVariableNames())
		{
			if(varName.equals(this.UpdateVariableName))
			{
				Handle.GetPlan().Variables.Update(this.UpdateVariableName, Value);
				break;
			}
		}
		this.UpdateParameterValue(Handle);
	}
	
	public void Validate()throws ExecutionValidationException
	{
		if(this.Filters==null || this.Filters.size()==0) throw new ExecutionValidationException("No filters have been defined");
		for(ParameterFilterBase filter : this.Filters)
		{
			filter.Validate();
		}
		boolean varFound=false;
		for(String varName : this.GetFilterInputVariableNames())
		{
			if(varName.equals(this.UpdateVariableName))
			{
				varFound=true;
				break;
			}
		}
		if(!varFound) throw new ExecutionValidationException("Defined output variable name not included in filter inputs");
	}
	
	public void ValidatePreExecution(ExecutionHandle Handle, Set<String> ExcludeAvailableConstraint)  throws ExecutionValidationException
	{
		this.Validate();
		for(ParameterFilterBase filter : this.Filters)
		{
			filter.ValidatePreExecution(Handle, ExcludeAvailableConstraint);
		}
	}

	public void FromXML(String XML) throws ExecutionSerializationException
	{
		Document doc = null;
		try
		{
			doc = XMLUtils.Deserialize(XML);
		} catch (Exception ex)
		{
			throw new ExecutionSerializationException("Could not deserialize provided XML serialization", ex);
		}
		this.FromXML(doc.getDocumentElement());
	}

	public void FromXML(Node XML) throws ExecutionSerializationException
	{
		try
		{
			if (!XMLUtils.AttributeExists((Element) XML, "direction") || !XMLUtils.AttributeExists((Element) XML, "process")) throw new ExecutionSerializationException("Provided serialization is not valid");
			if(!ParameterDirectionType.valueOf(XMLUtils.GetAttribute((Element)XML, "direction")).equals(this.GetDirectionType())) throw new ExecutionSerializationException("Provided serialization is not valid");
			if(!ParameterProcessType.valueOf(XMLUtils.GetAttribute((Element)XML, "process")).equals(this.GetProcessType())) throw new ExecutionSerializationException("Provided serialization is not valid");
			Element updvarelem=XMLUtils.GetChildElementWithName(XML, "updateVar");
			if(updvarelem==null) throw new ExecutionSerializationException("provided serialization is not valid serialization of element");
			if(!XMLUtils.AttributeExists(updvarelem, "name")) throw new ExecutionSerializationException("provided serialization is not valid serialization of element");
			this.UpdateVariableName=XMLUtils.GetAttribute(updvarelem, "name");
			List<Element> fltrs=XMLUtils.GetChildElementsWithName(XML, "filter");
			this.Filters.clear();
			for(Element fltr : fltrs) this.Filters.add(ParameterUtils.GetParameterFilter(fltr));
		} catch (Exception ex)
		{
			throw new ExecutionSerializationException("Could not deserialize provided XML serialization", ex);
		}
	}

	public ParameterDirectionType GetDirectionType()
	{
		return ParameterDirectionType.Out;
	}

	public ParameterProcessType GetProcessType()
	{
		return  ParameterProcessType.Filter;
	}

	public String ToXML() throws ExecutionSerializationException
	{
		StringBuilder buf = new StringBuilder();
		buf.append("<param direction=\"" + this.GetDirectionType().toString()+ "\" process=\"" + this.GetProcessType().toString()+ "\">");
		buf.append("<updateVar name=\""+this.UpdateVariableName+"\"/>");
		for (ParameterFilterBase filter : this.Filters)
		{
			buf.append(filter.ToXML());
		}
		buf.append("</param>");
		return buf.toString();
	}

	public Set<String> GetModifiedVariableNames()
	{
		Set<String> vars=new HashSet<String>();
		for (ParameterFilterBase filter : this.Filters)
		{
			if(filter.StoreOutput()) vars.addAll(filter.GetStoreOutputVariableName());
		}
		vars.add(this.UpdateVariableName);
		return vars;
	}

	public Set<String> GetNeededVariableNames()
	{
		Set<String> vars=new HashSet<String>();
		for (String s : this.GetFilterInputVariableNames())
		{
			vars.add(s);
		}
		for (ParameterFilterBase filter : this.Filters)
		{
			if(filter.StoreOutput()) vars.addAll(filter.GetStoreOutputVariableName());
		}
		vars.add(this.UpdateVariableName);
		return vars;
	}
}
