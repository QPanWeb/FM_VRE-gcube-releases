/**
 * 
 */
package org.gcube.portlets.widgets.StatisticalManagerAlgorithmsWidget.client.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gcube.portlets.user.tdw.client.TabularData;
import org.gcube.portlets.user.tdw.client.TabularDataGridPanel;
import org.gcube.portlets.user.tdw.client.util.ColumnPositionComparator;
import org.gcube.portlets.user.tdw.shared.model.ColumnDefinition;
import org.gcube.portlets.user.tdw.shared.model.TableDefinition;
import org.gcube.portlets.widgets.StatisticalManagerAlgorithmsWidget.client.Constants;
import org.gcube.portlets.widgets.StatisticalManagerAlgorithmsWidget.client.Services;
import org.gcube.portlets.widgets.StatisticalManagerAlgorithmsWidget.client.StatisticalManagerAlgorithmsWidget;
import org.gcube.portlets.widgets.StatisticalManagerAlgorithmsWidget.client.StatisticalManagerExperimentsWidget;
import org.gcube.portlets.widgets.StatisticalManagerAlgorithmsWidget.client.bean.TableItemSimple;
import org.gcube.portlets.widgets.StatisticalManagerAlgorithmsWidget.client.bean.parameters.Parameter;
import org.gcube.portlets.widgets.StatisticalManagerAlgorithmsWidget.client.bean.parameters.TabularParameter;
import org.gcube.portlets.widgets.StatisticalManagerAlgorithmsWidget.client.experimentArea.TableSelector;
import org.gcube.portlets.widgets.StatisticalManagerAlgorithmsWidget.client.resources.Images;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.widget.core.client.Dialog;



/**
 * @author ceras
 *
 */
public class TabularField extends AbstractField {

	private VerticalPanel vp = new VerticalPanel();
	String value = null;
	TableSelector tableSelector;
	Button selectButton, selectButton2, cancelButton;
	Html templatesList;
	TableItemSimple selectedTableItem = null;
	private List<AbstractField> listeners = new ArrayList<AbstractField>();

	/**
	 * @param parameter
	 */
	public TabularField(Parameter parameter) {
		super(parameter);

		TabularParameter p = (TabularParameter)parameter;
		List<String> templates = p.getTemplates();

		tableSelector = new TableSelector(templates) {
			@Override
			public void fireSelection(TableItemSimple tableItem) {
				super.fireSelection(tableItem);
				selectedTableItem = tableItem;
				showFieldWithSelection();

				loadTableMetadata(tableItem);
				// send change message to all listeners
				// it will be managed by all columnFields and columnListField that depends by tabular field				
			}
		};

		selectButton = new Button("Select Data Set", Images.folderExplore(), new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				tableSelector.show();
			}
		});
		selectButton.setToolTip("Select Data Set");

		selectButton2 = new Button("", Images.folderExplore(), new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				tableSelector.show();
			}
		});
		selectButton2.setToolTip("Select Another Data Set");

		cancelButton = new Button("", Images.cancel(), new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				selectedTableItem = null;
				showNoSelectionField();
				updateListeners(null);
			}
		}); 

		String list = "";
		boolean first = true;
		for (String template: templates) {
			list += (first ? "" : ", ") + template;
			first = false;
		}
		templatesList = new Html("Suitable Data Set Templates: <br>"+ list);
		templatesList.addStyleName("workflow-templatesList");

		showNoSelectionField();		
	}

	/**
	 * @param tableItem
	 */
	protected void loadTableMetadata(final TableItemSimple tableItem) {

		String tableId = tableItem.getId();

		if(tableItem.getTDFlag()==false){
			TabularData tabularData = Services.getTabularData();

			tabularData.getTableDefinition(tableId, new AsyncCallback<TableDefinition>(){

				@Override
				public void onFailure(Throwable caught) {
					vp.unmask();
					Info.display("ERROR", "");
				}

				@Override
				public void onSuccess(TableDefinition tableDefinition) {
					vp.unmask();
					List<ColumnDefinition> columns = tableDefinition.getColumnsAsList();
					Collections.sort(columns, new ColumnPositionComparator(false));
					for (ColumnDefinition column: columns)
						tableItem.addColumnName(column.getLabel());
					updateListeners(tableItem);
				}

			});

			vp.mask("Load Data Set Metadata...", Constants.maskLoadingStyle);
		}else
			for (String column: StatisticalManagerExperimentsWidget.instance().getColumns(tableId).values())
				tableItem.addColumnName(column);
		
		
		updateListeners(tableItem);

	}

	/**
	 * @param id
	 */
	protected void updateListeners(TableItemSimple tableItem) {
		for (AbstractField abstractField: listeners) {
			abstractField.fireEvent(tableItem);
		}
	}

	/**
	 * 
	 */
	private void showNoSelectionField() {
		vp.removeAll();
		vp.add(selectButton);
		vp.add(templatesList);
		vp.layout();
	}

	/**
	 * 
	 */
	private void showFieldWithSelection() {
		final String tableId = selectedTableItem.getId();
		final String tableName = selectedTableItem.getName();

		vp.removeAll();
		HorizontalPanel hp = new HorizontalPanel();
		hp.add(new Html("<div class='workflow-parameters-tableDescription'>"+ Format.ellipse(tableName, 30) +"</div>"));
		hp.add(selectButton2);
		hp.add(cancelButton);

		Button openTableButton = new Button("Open Data Set", new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent be) {
				TabularData tabularData = Services.getTabularData();
				TabularDataGridPanel gridPanel = tabularData.getGridPanel();
				gridPanel.setExpanded(true);
				gridPanel.setBorders(true);

				Dialog dialog = new Dialog();
				dialog.setMaximizable(true);
				dialog.setBodyBorder(false);
				dialog.setExpanded(true);

				dialog.setHeadingText("Data Set "+tableName);
				dialog.setWidth(640);  
				dialog.setHeight(480);  
				dialog.setHideOnButtonClick(true);  
				dialog.setModal(true);
				dialog.add(gridPanel);
				dialog.show();

				tabularData.openTable(tableId);
				gridPanel.setHeaderVisible(false);
			}
		});
		if(!selectedTableItem.getTDFlag())
			hp.add(openTableButton);
		
		
		vp.add(hp);
		vp.add(templatesList);
		vp.layout();
	}

	/* (non-Javadoc)
	 * @see org.gcube.portlets.user.statisticalmanager.client.form.AbstractField#getValue()
	 */
	@Override
	public String getValue() {
		return (selectedTableItem==null) ? null : selectedTableItem.getId();
	}

	/* (non-Javadoc)
	 * @see org.gcube.portlets.user.statisticalmanager.client.form.AbstractField#getComponent()
	 */
	@Override
	public Widget getWidget() {
		return vp;
	}

	/* (non-Javadoc)
	 * @see org.gcube.portlets.user.statisticalmanager.client.form.AbstractField#isValid()
	 */
	@Override
	public boolean isValid() {
		return (selectedTableItem!=null);
	}

	public void addChangeListener(AbstractField abstractField) {
		this.listeners.add(abstractField);
	}
	
	public void forceSelection(TableItemSimple toSelect){
		this.tableSelector.fireSelection(toSelect);
	}
}
