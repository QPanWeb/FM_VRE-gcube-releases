package org.gcube.portlets.user.td.gwtservice.shared.tr.resources;

import org.gcube.portlets.user.td.widgetcommonevent.shared.thumbnail.ThumbnailTD;

/**
 * 
 * @author giancarlo email: <a
 *         href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a>
 *
 */
public class InternalURITD extends ResourceTD {
	private static final long serialVersionUID = -3230438212164027113L;

	private String id;

	private String mimeType;

	private ThumbnailTD thumbnailTD;

	public InternalURITD() {
		super();
	}

	/**
	 * 
	 * @param fileId
	 * @param id
	 */
	public InternalURITD(String id, String mimeType, ThumbnailTD thumbnailTD) {
		super(id);
		this.id = id;
		this.mimeType = mimeType;
		this.thumbnailTD = thumbnailTD;
	}

	@Override
	public String getStringValue() {
		return id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public ThumbnailTD getThumbnailTD() {
		return thumbnailTD;
	}

	public void setThumbnailTD(ThumbnailTD thumbnailTD) {
		this.thumbnailTD = thumbnailTD;
	}

	@Override
	public String toString() {
		return "InternalURITD [id=" + id + ", mimeType=" + mimeType
				+ ", thumbnailTD=" + thumbnailTD + "]";
	}

}
