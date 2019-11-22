package org.gcube.application.reporting.component;

import java.util.List;

import org.gcube.application.reporting.component.interfaces.IsMedia;
import org.gcube.application.reporting.component.interfaces.IsSequentiable;
import org.gcube.application.reporting.component.interfaces.ReportComponent;
import org.gcube.application.reporting.component.type.ReportComponentType;
import org.gcube.portlets.d4sreporting.common.shared.BasicComponent;
import org.gcube.portlets.d4sreporting.common.shared.ComponentType;

public class TextInput extends AbstractComponent implements IsMedia, IsSequentiable {

	private String content;
	private boolean isReadOnly;
		
	public TextInput() {
		super();
		this.content = "";
		this.isReadOnly = false;
	}
	
	public TextInput(String content) {
		super();
		this.content = content;
		this.isReadOnly = false;
	}
	public TextInput(String content, boolean isReadOnly) {
		super();
		this.content = content;
		this.isReadOnly = isReadOnly;
	}
	
	public boolean isReadOnly() {
		return isReadOnly;
	}
	@Override
	public ReportComponentType getType() {
		return ReportComponentType.TEXT_INPUT;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public List<ReportComponent> getChildren() {
		return null;
	}

	@Override
	public String getStringValue() {
		return content;
	}

	@Override
	public BasicComponent getModelComponent() {
		BasicComponent bc = new BasicComponent(0, 0, COMP_WIDTH, COMP_HEIGHT, 
				1, ComponentType.BODY_NOT_FORMATTED, "", content, false, isReadOnly, convertProperties());	
		bc.setId(getId());
		return bc;
	}

}
