/**
 * 
 */
package org.gcube.portlets.widgets.netcdfbasicwidgets.client.resource;

import com.google.gwt.resources.client.CssResource;

/**
 * 
 * @author Giancarlo Panichi
 *
 *
 */
public interface NetCDFBasicCSS extends CssResource {

	@ClassName("area-selection-panel")
	public String getAreaSelectionPanel();

	@ClassName("area-selection-content")
	public String getAreaSelectionContent();

	@ClassName("dialog-tool-button-text")
	public String getDialogToolButtonText();

	@ClassName("dialog-tool-button-icon")
	public String getDialogToolButtonIcon();

	@ClassName("progress-bar-container")
	public String getProgressBarContainer();

	@ClassName("progress-bar")
	public String getProgressBar();

	@ClassName("progress-bar-text")
	public String getProgressBarText();

	@ClassName("wkt-geometry-text-area")
	public String getWKTGeometryTextArea();

	@ClassName("combo-geometry-type")
	public String getComboGeometryType();

	@ClassName("sample-data-text-area")
	public String getSampleDataTextArea();

	@ClassName("cell-word-wrap")
	public String getCellWordWrap();
}
