package gr.cite.geoanalytics.dataaccess.entities.sysconfig.xml.layer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import gr.cite.geoanalytics.dataaccess.entities.sysconfig.SysConfigData;

@XmlRootElement
@XmlSeeAlso(LayerBounds.class)
public class LayerConfig implements SysConfigData
{
	private String name = null;
	private String termId = null;
	private String boundaryTermId = null;
	private Integer minScale = null;
	private Integer maxScale = null;
	
	private LayerBounds boundingBox;

	public LayerConfig() { }
	
	public LayerConfig(LayerConfig other)
	{
		this.name = other.name;
		this.termId = other.termId;
		this.boundaryTermId = other.boundaryTermId;
		this.minScale = other.minScale;
		this.maxScale = other.maxScale;
		this.boundingBox = new LayerBounds(other.boundingBox);
	}
	
	public String getName()
	{
		return name;
	}

	@XmlElement
	public void setName(String name)
	{
		this.name = name;
	}

	public Integer getMinScale()
	{
		return minScale;
	}

	@XmlElement
	public void setMinScale(Integer minScale)
	{
		this.minScale = minScale;
	}

	public Integer getMaxScale()
	{
		return maxScale;
	}

	@XmlElement
	public void setMaxScale(Integer maxScale)
	{
		this.maxScale = maxScale;
	}

	public LayerBounds getBoundingBox()
	{
		return boundingBox;
	}
	
	@XmlElement(name="bounds")
	public void setBoundingBox(LayerBounds boundingBox)
	{
		this.boundingBox = boundingBox;
	}

	public String getTermId()
	{
		return termId;
	}

	@XmlElement
	public void setTermId(String termId)
	{
		this.termId = termId;
	}
	
	 public String getBoundaryTermId()
	{
		return boundaryTermId;
	}

	@XmlElement(required=false)
	public void setBoundaryTermId(String boundaryTermId)
	{
		this.boundaryTermId = boundaryTermId;
	}

	@Override
	 public boolean equals(Object other) 
	 {
		 if (other == this) return true;
		 if (other == null || other.getClass() != this.getClass()) return false;

        return termId.equals(((LayerConfig)other).getTermId());
    }
   
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(name);
		if(minScale != null)
			sb.append(" minScale: " + minScale);
		if(maxScale != null)
			sb.append(" maxScale: " + maxScale);
		if(boundingBox != null)
			sb.append(" bbox: (" + boundingBox.toString() + ")");
		if(termId != null)
			sb.append(" termId: " + termId);
		if(boundaryTermId != null)
			sb.append(" boundaryTermId: " + boundaryTermId);
		return sb.toString();
	}
	
    @Override
    public int hashCode() 
    {
        return this.toString().hashCode();
    }
}
