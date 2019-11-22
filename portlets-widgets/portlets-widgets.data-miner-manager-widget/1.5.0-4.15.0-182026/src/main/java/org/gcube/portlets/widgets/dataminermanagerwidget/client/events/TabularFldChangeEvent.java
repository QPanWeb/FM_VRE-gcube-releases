package org.gcube.portlets.widgets.dataminermanagerwidget.client.events;


import org.gcube.data.analysis.dataminermanagercl.shared.data.TableItemSimple;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 * TabularFldChangeEvent
 * 
 * 
 * @author Giancarlo Panichi
 *
 *
 */
public class TabularFldChangeEvent extends
		GwtEvent<TabularFldChangeEvent.TabularFldChangeEventHandler> {

	public static Type<TabularFldChangeEventHandler> TYPE = new Type<TabularFldChangeEventHandler>();
	private TableItemSimple tableItemSimple;

	public interface TabularFldChangeEventHandler extends EventHandler {
		void onChange(TabularFldChangeEvent event);
	}

	public interface HasTabularFldChangeEventHandler extends HasHandlers {
		public HandlerRegistration addTabularFldChangeEventHandler(
				TabularFldChangeEventHandler handler);
	}

	public TabularFldChangeEvent(TableItemSimple tableItemSimple) {
		this.tableItemSimple = tableItemSimple;

	}

	@Override
	protected void dispatch(TabularFldChangeEventHandler handler) {
		handler.onChange(this);
	}

	@Override
	public Type<TabularFldChangeEventHandler> getAssociatedType() {
		return TYPE;
	}

	public static Type<TabularFldChangeEventHandler> getType() {
		return TYPE;
	}

	public static void fire(HasHandlers source, TabularFldChangeEvent event) {
		source.fireEvent(event);
	}

	public TableItemSimple getTableItemSimple() {
		return tableItemSimple;
	}

	@Override
	public String toString() {
		return "TabularFldChangeEvent [tableItemSimple=" + tableItemSimple
				+ "]";
	}

}
