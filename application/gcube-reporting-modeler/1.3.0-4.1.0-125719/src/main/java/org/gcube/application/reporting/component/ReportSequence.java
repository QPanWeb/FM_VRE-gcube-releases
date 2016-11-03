package org.gcube.application.reporting.component;

import java.util.ArrayList;
import java.util.List;

import org.gcube.application.reporting.component.interfaces.IsSequentiable;
import org.gcube.application.reporting.component.interfaces.Modeler;
import org.gcube.application.reporting.component.interfaces.ReportComponent;
import org.gcube.application.reporting.component.type.ReportComponentType;
import org.gcube.portlets.d4sreporting.common.shared.BasicComponent;
import org.gcube.portlets.d4sreporting.common.shared.ComponentType;
import org.gcube.portlets.d4sreporting.common.shared.Metadata;
import org.gcube.portlets.d4sreporting.common.shared.RepeatableSequence;

public class ReportSequence extends AbstractComponent implements Modeler<IsSequentiable> {
	
	private List<ReportComponent> children; 
	/**
	 * 
	 */
	public ReportSequence(String id) {
		setId(id);
		children = new ArrayList<ReportComponent>();
	}
	
	/* (non-Javadoc)
	 * @see org.gcube.application.reporting.component.Modeler#add(org.gcube.application.reporting.component.interfaces.ReportComponent)
	 */
	public boolean add(IsSequentiable component) {
		return children.add(component);
	}
		
	protected List<BasicComponent> getSequence() {
		List<BasicComponent> toReturn = new ArrayList<BasicComponent>();
		for (ReportComponent repCo : children) {
			toReturn.add(repCo.getModelComponent());
		}
		return toReturn;
	}

	@Override
	public ReportComponentType getType() {
		return ReportComponentType.SEQUENCE;
	}

	@Override
	public boolean hasChildren() {
		return true;
	}

	@Override
	public List<ReportComponent> getChildren() {
		return children;
	}

	@Override
	public String getStringValue() {
		return null;
	}

	@Override
	public BasicComponent getModelComponent() {
		ArrayList<BasicComponent> groupedComponents = new ArrayList<BasicComponent>();
		//need to add the delimiters at begin / end
		groupedComponents.add(getSequenceDelimiter(0));
		for (BasicComponent elem : getSequence()) {
			groupedComponents.add(elem);
		}
		groupedComponents.add(getSequenceDelimiter(0));
		
		RepeatableSequence toEmbed = new RepeatableSequence(groupedComponents, getId(), 0);
		BasicComponent bc = new BasicComponent(0, 0, COMP_WIDTH, COMP_HEIGHT, 
				1, ComponentType.REPEAT_SEQUENCE, "", toEmbed, false, true, convertProperties());	
		bc.setId(getId());
		return bc;
	}
	
	private BasicComponent getSequenceDelimiter(int height) {
		return new BasicComponent(0, 0, COMP_WIDTH, height, 
				1, ComponentType.REPEAT_SEQUENCE_DELIMITER, "", "", false, false, new ArrayList<Metadata>());	
	}
}
