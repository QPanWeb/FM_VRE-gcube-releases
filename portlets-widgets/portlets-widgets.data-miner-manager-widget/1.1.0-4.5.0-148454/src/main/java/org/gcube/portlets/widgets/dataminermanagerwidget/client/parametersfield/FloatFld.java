package org.gcube.portlets.widgets.dataminermanagerwidget.client.parametersfield;

import org.gcube.data.analysis.dataminermanagercl.shared.parameters.ObjectParameter;
import org.gcube.data.analysis.dataminermanagercl.shared.parameters.Parameter;

import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutData;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutPack;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HtmlLayoutContainer;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.form.FloatField;

/**
 * 
 * @author Giancarlo Panichi
 *
 *
 */
public class FloatFld extends AbstractFld {

	private SimpleContainer fieldContainer;
	private FloatField numberField;

	/**
	 * 
	 * @param parameter
	 *            parameter
	 */
	public FloatFld(Parameter parameter) {
		super(parameter);
		fieldContainer = new SimpleContainer();
		HBoxLayoutContainer horiz = new HBoxLayoutContainer();
		horiz.setPack(BoxLayoutPack.START);
		horiz.setEnableOverflow(false);

		ObjectParameter p = (ObjectParameter) parameter;

		numberField = new FloatField();

		if (p.getDefaultValue() != null)
			numberField.setValue(Float.parseFloat(p.getDefaultValue()));
		numberField.setAllowBlank(false);

		HtmlLayoutContainer descr;

		if (p.getDescription() == null) {
			descr = new HtmlLayoutContainer("<p style='margin-left:5px !important;'></p>");
			descr.addStyleName("workflow-fieldDescription");

		} else {
			// numberField.setToolTip(p.getDescription());
			descr = new HtmlLayoutContainer("<p style='margin-left:5px !important;'>" + p.getDescription() + "</p>");
			descr.addStyleName("workflow-fieldDescription");
		}

		SimpleContainer vContainer = new SimpleContainer();
		VerticalLayoutContainer vField = new VerticalLayoutContainer();
		HtmlLayoutContainer typeDescription = new HtmlLayoutContainer("Float Value");
		typeDescription.setStylePrimaryName("workflow-parameters-description");
		vField.add(numberField, new VerticalLayoutData(-1, -1, new Margins(0)));
		vField.add(typeDescription, new VerticalLayoutData(-1, -1, new Margins(0)));
		vContainer.add(vField);

		horiz.add(vContainer, new BoxLayoutData(new Margins()));
		horiz.add(descr, new BoxLayoutData(new Margins()));

		fieldContainer.add(horiz);
		fieldContainer.forceLayout();

	}

	@Override
	public String getValue() {
		Float f = numberField.getCurrentValue();
		return f.toString();
	}

	@Override
	public Widget getWidget() {
		return fieldContainer;
	}

	@Override
	public boolean isValid() {
		return numberField.isValid();
	}
}
