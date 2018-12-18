package org.gcube.common.storagehub.client.dsl;

import java.net.URL;
import java.util.List;

import org.gcube.common.storagehub.client.StreamDescriptor;
import org.gcube.common.storagehub.client.proxies.ItemManagerClient;
import org.gcube.common.storagehub.model.items.AbstractFileItem;
import org.gcube.common.storagehub.model.service.Version;

public class FileContainer extends ItemContainer<AbstractFileItem> {

	protected FileContainer(ItemManagerClient itemclient, AbstractFileItem item) {
		super(itemclient, item);
	}

	protected FileContainer(ItemManagerClient itemclient, String fileId) {
		super(itemclient, fileId);		
	}
	
	public ContainerType getType() {
		return ContainerType.FILE;
	}
	
	public URL getPublicLink() {
		return itemclient.getPublickLink(this.itemId);
	}
	
	public List<Version> getVersions() {
		return itemclient.getFileVersions(this.itemId);
	}
	
	public StreamDescriptor downloadSpecificVersion(String versionName) {
		return itemclient.downloadSpecificVersion(this.itemId, versionName);
	}
	
	public FileContainer copy(FolderContainer folder, String newFileName) {
		return new FileContainer(itemclient, itemclient.copy(this.itemId, folder.get().getId(), newFileName));
	}
}
