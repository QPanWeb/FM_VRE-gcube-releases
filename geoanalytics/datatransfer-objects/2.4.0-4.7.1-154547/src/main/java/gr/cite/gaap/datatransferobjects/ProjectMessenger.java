package gr.cite.gaap.datatransferobjects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.cite.geoanalytics.dataaccess.entities.project.Project.ProjectStatus;

public class ProjectMessenger {
	private static Logger logger = LoggerFactory.getLogger(ProjectMessenger.class);

	private String id = null;
	private String name = null;
	private String description = null;
	private long creationDate = -1;
	private String creator = null;
	private String extent = null;
	private boolean template = false;
	private String shape = null;
	private ProjectStatus status = null;
	
	

	public ProjectMessenger() {
		super();
		logger.trace("Initialized default contructor for ProjectMessenger");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(long creationDate) {
		this.creationDate = creationDate;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getExtent() {
		return extent;
	}

	public void setExtent(String extent) {
		this.extent = extent;
	}

	public boolean isTemplate() {
		return template;
	}

	public void setTemplate(boolean template) {
		this.template = template;
	}

	public String getShape() {
		return shape;
	}

	public void setShape(String shape) {
		this.shape = shape;
	}

	public ProjectStatus getStatus() {
		return status;
	}

	public void setStatus(ProjectStatus status) {
		this.status = status;
	}

}
