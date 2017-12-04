package org.apache.jackrabbit.j2ee.workspacemanager.items;

import java.util.Calendar;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.j2ee.workspacemanager.JCRWorkspaceItem;
import org.gcube.common.homelibary.model.items.type.NodeProperty;

import com.thoughtworks.xstream.XStream;

public class JCRReportTemplate extends JCRWorkspaceItem {

	public JCRReportTemplate(Node node, String login) throws RepositoryException {
		super(node, login);

		Node contentNode = node.getNode(NodeProperty.CONTENT.toString());
		item.setContent(new JCRFile(contentNode).getMap());
		
		String author = contentNode.getProperty(NodeProperty.AUTHOR.toString()).getString();
		Calendar created = contentNode.getProperty(NodeProperty.RT_CREATED.toString()).getDate();
		Calendar lastEdit = contentNode.getProperty(NodeProperty.LAST_EDIT.toString()).getDate();
		String lastEditBy = contentNode.getProperty(NodeProperty.LAST_EDIT_BY.toString()).getString();
		int numberOfSections = (int) contentNode.getProperty(NodeProperty.NUMBER_OF_SECTION.toString()).getLong();
		String status = contentNode.getProperty(NodeProperty.STATUS.toString()).getString();
		
		Map<NodeProperty, String> content = item.getContent();
		
		content.put(NodeProperty.AUTHOR,author);
		content.put(NodeProperty.RT_CREATED, xstream.toXML(created));
		content.put(NodeProperty.LAST_EDIT, xstream.toXML(lastEdit));
		content.put(NodeProperty.LAST_EDIT_BY, lastEditBy);
		content.put(NodeProperty.NUMBER_OF_SECTION, xstream.toXML(numberOfSections));
		content.put(NodeProperty.STATUS, status);
	}


}
