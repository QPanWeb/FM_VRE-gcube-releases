package org.gcube.common.homelibrary.jcr.workspace.folder.items;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.gcube.common.homelibary.model.items.ItemDelegate;
import org.gcube.common.homelibary.model.items.type.FolderItemType;
import org.gcube.common.homelibrary.home.exceptions.InternalErrorException;
import org.gcube.common.homelibrary.home.workspace.folder.items.ExternalPDFFile;
import org.gcube.common.homelibrary.jcr.workspace.JCRWorkspace;
import org.gcube.common.homelibrary.jcr.workspace.servlet.JCRServlets;
import org.gcube.common.homelibrary.model.exceptions.RepositoryException;
import org.gcube.contentmanagement.blobstorage.transport.backend.RemoteBackendException;
import org.gcube.common.homelibary.model.items.type.ContentType;

public class JCRExternalPDFFile extends JCRExternalFile implements
		ExternalPDFFile {

	
	public JCRExternalPDFFile(JCRWorkspace workspace, ItemDelegate delegate) throws RepositoryException {
		super(workspace, delegate, ContentType.PDF);
			
	}
	
	
	public JCRExternalPDFFile(JCRWorkspace workspace, ItemDelegate node, String name,
			String description, String mimeType, InputStream fileData) throws RepositoryException, IOException, RemoteBackendException {
		super(workspace,node,name,description,mimeType,ContentType.PDF,fileData);
		
	}
	
	public JCRExternalPDFFile(JCRWorkspace workspace, ItemDelegate node, String name,
			String description, String mimeType, File tmpFile) throws RepositoryException, IOException, RemoteBackendException {
		super(workspace,node,name,description,mimeType,ContentType.PDF,tmpFile);
		
	}

	
	@Override
	public FolderItemType getFolderItemType() {
		return FolderItemType.EXTERNAL_PDF_FILE;	
	}

	@Override
	public int getNumberOfPages() {	
		return ((JCRPDFFile)file).getNumberOfPages();
	}

	@Override
	public String getVersion() {
		return ((JCRPDFFile)file).getVersion();
	}

	@Override
	public String getAuthor() {
		return ((JCRPDFFile)file).getAuthor();
	}

	@Override
	public String getTitle() {
		return ((JCRPDFFile)file).getTitle();
	}

	@Override
	public String getProducer() {
		return ((JCRPDFFile)file).getProducer();
	}

	@Override
	public void updateInfo(JCRServlets servlets, java.io.File tmp) throws InternalErrorException {
		super.updateInfo(servlets, tmp);
	}
	
}
