package gr.uoa.di.madgik.rr.element.execution;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Serialized;

import org.w3c.dom.Element;

import gr.uoa.di.madgik.commons.utils.XMLUtils;
import gr.uoa.di.madgik.rr.ResourceRegistryException;
import gr.uoa.di.madgik.rr.element.IDaoElement;

@PersistenceCapable(table="EXECUTIONSERVER", detachable="true")
@Queries(
		{@Query(
				name="all", 
				language="JDOQL", 
				value="SELECT this.ID FROM gr.uoa.di.madgik.rr.element.execution.ExecutionServerDao"
				),
		@Query(
				name="exists", 
				language="JDOQL", 
				value="SELECT this.ID FROM gr.uoa.di.madgik.rr.element.execution.ExecutionServerDao WHERE this.ID == :id"
				),
		}
		
)
public class ExecutionServerDao implements IDaoElement
{
	@PrimaryKey
	//@Column(name = "gcubeGUID")
	public String ID;
	
	//@Column(name = "gcubeTimestamp")
	public Long timestamp=Calendar.getInstance().getTimeInMillis();
	
	//@Column(name = "gcubeEndpointHostname")
	public String hostname;
	
	//@Column(name = "gcubeEndpointPort")
	public String port;
	
	//@Column(name = "gcubeFunctionality-ref")
	public String functionality;
	
	//@Column(name = "gcubeHostingNode-ref")
	public String hostingNode;
	
	//@Column(name = "gcubeScope")
	//@Persistent(defaultFetchGroup="true")
	@Persistent(defaultFetchGroup="true")
	@Column(jdbcType = "BLOB")
	@Serialized
	public Set<String> scopes=new HashSet<String>();

	public Set<String> getScopes()
	{
		return scopes;
	}

	public void setScopes(Set<String> scopes)
	{
		this.scopes = scopes;
	}

	public String getHostingNode()
	{
		return hostingNode;
	}

	public void setHostingNode(String hostingNode)
	{
		this.hostingNode = hostingNode;
	}
	
	public String getFunctionality()
	{
		return functionality;
	}

	public void setFunctionality(String functionality)
	{
		this.functionality = functionality;
	}

	public String getID()
	{
		return ID;
	}
	
	public Long getTimestamp()
	{
		if (timestamp ==  null)
			return 0l;
		return timestamp;
	}
	
	public void setID(String iD)
	{
		ID = iD;
	}

	public void setTimestamp(Long timestamp)
	{
		this.timestamp = timestamp;
	}
	
	public String getHostname()
	{
		return hostname;
	}
	
	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}
	
	public String getPort()
	{
		return port;
	}

	public void setPort(String port)
	{
		this.port = port;
	}
	
	public void apply(IDaoElement target) throws ResourceRegistryException
	{
		if(!(target instanceof ExecutionServerDao)) throw new ResourceRegistryException("cannot apply to target of "+target);
		this.setID(((ExecutionServerDao)target).getID());
		this.setFunctionality(((ExecutionServerDao)target).getFunctionality());
		this.setHostingNode(((ExecutionServerDao)target).getHostingNode());
		this.setPort(((ExecutionServerDao)target).getPort());
		this.setScopes(((ExecutionServerDao)target).getScopes());
	}
	
	public String deepToString()
	{
		StringBuilder buf=new StringBuilder();
		buf.append("ExecutionServer ID:"+this.ID+"\n");
		buf.append("Port:"+this.port+"\n");
		buf.append("Hosting Node:"+this.hostingNode+"\n");
		return buf.toString();
	}

	public void fromXML(Element element) throws ResourceRegistryException
	{
		try
		{
			if(!XMLUtils.AttributeExists(element, "id")) throw new ResourceRegistryException("id attribute not found in serialization");
			this.setID(XMLUtils.GetAttribute(element, "id"));
			this.setTimestamp(Long.parseLong(XMLUtils.GetChildText(XMLUtils.GetChildElementWithName(element, "timestamp"))));
			this.setHostname(XMLUtils.GetChildText(XMLUtils.GetChildElementWithName(element, "hostname")));
			this.setPort(XMLUtils.GetChildText(XMLUtils.GetChildElementWithName(element, "port")));
			this.setHostingNode(XMLUtils.GetChildText(XMLUtils.GetChildElementWithName(element, "hostingNode")));
			this.setFunctionality(XMLUtils.GetChildText(XMLUtils.GetChildElementWithName(element, "functionality")));
			Element scopesXML = XMLUtils.GetChildElementWithName(element, "scopes");
			if(scopesXML!=null)
			{
				List<Element> sXML = XMLUtils.GetChildElementsWithName(scopesXML, "scope");
				for(Element item : sXML) this.scopes.add(XMLUtils.GetChildText(item));
			}
		}catch(Exception ex)
		{
			throw new ResourceRegistryException("could not complete deserialization",ex);
		}
	}
	
	public String toXML()
	{
		StringBuilder buf=new StringBuilder();
		buf.append("<executionServer id=\""+this.getID()+"\">");
		buf.append("<timestamp>"+new Long(this.getTimestamp()).toString()+"</timestamp>");
		buf.append("<hostname>"+this.getHostname()+"</hostname>");
		buf.append("<port>"+this.getPort()+"</port>");
		buf.append("<hostingNode>"+this.getHostingNode()+"</hostingNode>");
		buf.append("<functionality>"+this.getFunctionality()+"</functionality>");
		if(!this.getScopes().isEmpty())
		{
			buf.append("<scopes>");
			for(String s : this.getScopes())
				buf.append("<scope>"+s+"</scope>");
			buf.append("</scopes>");
		}
		buf.append("</executionServer>");
		return buf.toString();
	}
}
