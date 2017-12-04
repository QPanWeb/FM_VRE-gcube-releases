/**
 * 
 */
package org.gcube.portlets.user.td.gwtservice.shared.csv;

import java.io.Serializable;
import java.util.ArrayList;

import org.gcube.portlets.user.td.gwtservice.shared.source.Source;
import org.gcube.portlets.user.td.gwtservice.shared.tr.TabResource;

/**
 * 
 * @author Giancarlo Panichi
 *
 * 
 */
public class CSVImportSession implements Serializable {

	private static final long serialVersionUID = 4176034045408445284L;

	private String id;
	private Source source;

	private TabResource tabResource;

	private ArrayList<String> headers = new ArrayList<String>();

	private boolean skipInvalidLines = false;

	private ArrayList<Boolean> columnToImportMask = new ArrayList<Boolean>();

	private String localFileName;
	private String itemId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	public TabResource getTabResource() {
		return tabResource;
	}

	public void setTabResource(TabResource tabResource) {
		this.tabResource = tabResource;
	}

	public ArrayList<String> getHeaders() {
		return headers;
	}

	public void setHeaders(ArrayList<String> headers) {
		this.headers = headers;
	}

	public boolean isSkipInvalidLines() {
		return skipInvalidLines;
	}

	public void setSkipInvalidLines(boolean skipInvalidLines) {
		this.skipInvalidLines = skipInvalidLines;
	}

	public ArrayList<Boolean> getColumnToImportMask() {
		return columnToImportMask;
	}

	public void setColumnToImportMask(ArrayList<Boolean> columnToImportMask) {
		this.columnToImportMask = columnToImportMask;
	}

	public String getLocalFileName() {
		return localFileName;
	}

	public void setLocalFileName(String localFileName) {
		this.localFileName = localFileName;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	@Override
	public String toString() {
		return "CSVImportSession [id=" + id + ", source=" + source
				+ ", tabResource=" + tabResource + ", headers=" + headers
				+ ", skipInvalidLines=" + skipInvalidLines
				+ ", columnToImportMask=" + columnToImportMask
				+ ", localFileName=" + localFileName + ", itemId=" + itemId
				+ "]";
	}

}
