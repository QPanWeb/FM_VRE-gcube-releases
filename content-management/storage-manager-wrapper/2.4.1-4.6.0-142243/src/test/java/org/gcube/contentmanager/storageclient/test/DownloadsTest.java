package org.gcube.contentmanager.storageclient.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.bson.types.ObjectId;
import org.gcube.common.scope.api.ScopeProvider;
import org.gcube.contentmanagement.blobstorage.resource.StorageObject;
import org.gcube.contentmanagement.blobstorage.service.IClient;
import org.gcube.contentmanagement.blobstorage.transport.backend.RemoteBackendException;
import org.gcube.contentmanager.storageclient.wrapper.AccessType;
import org.gcube.contentmanager.storageclient.wrapper.MemoryType;
import org.gcube.contentmanager.storageclient.wrapper.StorageClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DownloadsTest {

	private String owner="rcirillo";
	private String localPath="src/test/resources/CostaRica1.jpg";
	private String remotePath="/test/img/CostaRica1.jpg";
	private String remotePath1="/test/img/CostaRica2.jpg";
	private String absoluteLocalPath;
	private String newFilePath="src/test/resources";
	private IClient client;
	private String scope="/d4science.research-infrastructures.eu";//"/gcube/devsec";//"/d4science.research-infrastructures.eu";//"/d4science.research-infrastructures.eu";//"/d4science.research-infrastructures.eu";// //"/CNR.it";// ///gcube/devsec/devVRE"; //"/CNR.it/ISTI";//"/gcube/devsec/devVRE"; // /d4science.research-infrastructures.eu"; //"/d4science.research-infrastructures.eu"; //"/CNR.it/ISTI";//
	private String serviceClass="JUnitTest";
	private String serviceName="StorageManager";
	private String id;
	private String id1;
	private String encryptedId="bEVydmtsOHhCclZMZGd4cEtnTVQzZXQ5UVNxWHZURGJHbWJQNStIS0N6Yz0";//"bEVydmtsOHhCclZMZGd4cEtnTVQz";//"cHEvek1sbjdBaWJkc0s4SzZpSUJpU0c2ZEgyOEVyUGJHbWJQNStIS0N6Yz0";//"RnpoMy9ZaVRoRkZjbk8ybGx0QnlRR";//"L1pWTlV3ZWxPbHRyMloxZ0JnWUVHdHYvUnZDVHJiMTBHbWJQNStIS0N6Yz0";
	
	@Before
	public void getClient() throws RemoteBackendException{
		ScopeProvider.instance.set(scope);
		try {
			client=new StorageClient(serviceClass, serviceName, owner, AccessType.SHARED, MemoryType.VOLATILE).getClient();
			assertNotNull(client);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setLocalResources();
		id=client.put(true).LFile(absoluteLocalPath).RFile(remotePath);
		System.out.println("upload done ");
		id1=client.put(true).LFile(absoluteLocalPath).RFile(remotePath1);
		System.out.println("upload done ");
		assertNotNull(id);
		
	}
	
	
	@Test
	public void getId(){
		String id= client.getId(encryptedId);
		System.out.println("id: "+id);
		assertNotNull(id);
		assertTrue(ObjectId.isValid(id));
	}
	
	
	@Test
	public void downloadByPath() throws RemoteBackendException {
		String idFound=client.get().LFile(newFilePath).RFile(remotePath);
		System.out.println("downloadByPath id: "+idFound+" id orig "+id );
		File f =new File(newFilePath);
		System.out.println("path new File downloaded: "+f.getAbsolutePath());
		assertTrue(f.exists());
		removeLocalFile();
	}

	@Test
	public void downloadByPathWithBackendTypeReturned() throws RemoteBackendException {
		String id=client.get().LFile(newFilePath).RFile(remotePath, true);
		System.out.println("downloadByPath id+backendType: "+id);
		File f =new File(newFilePath);
		System.out.println("path new File downloaded: "+f.getAbsolutePath());
		assertTrue(f.exists());
		removeLocalFile();
	}

	

	@Test
	public void downloadByInputStream() throws IOException, InterruptedException {
		downloadByIS(id);
		downloadByIS(id1);
		
	}


	private void downloadByIS(String id) throws FileNotFoundException, IOException,
			InterruptedException {
		System.out.println("download by InputStream with id: "+id);
		InputStream is=client.get().RFileAsInputStream(id);
		System.out.println("store in: "+newFilePath);
		File file=new File(newFilePath);
		FileOutputStream out=new FileOutputStream(file);
		byte buf[]=new byte[1024];
		int len=0;
		while((len=is.read(buf))>0){
			out.write(buf,0,len);
		}	  
		out.close();
		is.close();
		System.out.println("path new File downloaded: "+file.getAbsolutePath());
		assertTrue(file.exists());
		removeLocalFile();
	}

	@Test
	public void downloadById() throws RemoteBackendException{
//		String idReturned=client.get().LFile(newFilePath).RFileById(id);
		String idReturned=client.get().LFile(newFilePath).RFile(id);
		System.out.println("downloadById id: "+idReturned);
//		File f =new File(newFilePath);
		File f =new File(newFilePath);
		System.out.println("path new File downloaded: "+f.getAbsolutePath());
		assertTrue(f.exists());
		removeLocalFile();
	}

	
	@Test
	public void downloadByIdWithBackendTypeReturned() throws RemoteBackendException{
		String idReturned=client.get().LFile(newFilePath).RFileById(id, true);
//		String idReturned=client.get().LFile(newFilePath).RFile(id, true);
		System.out.println("downloadById id+backendType: "+idReturned);
		File f =new File(newFilePath);
		System.out.println("path new File downloaded: "+f.getAbsolutePath());
		assertTrue(f.exists());
		removeLocalFile();
	}

	
	@After
	public void removeRemoteFile() throws RemoteBackendException{
		String id=client.remove().RFile(remotePath);
		System.out.println("removeRemotePath id: "+id);
		String id1=client.remove().RFile(remotePath1);
		System.out.println("removeRemotePath id: "+id1);
		List<StorageObject> list=client.showDir().RDir("test/img");
		assertTrue(list.isEmpty());
	}

	private void removeLocalFile(){
		File f=new File(newFilePath);
		f.delete();
		assertFalse(f.exists());
	}


	private void setLocalResources() {
		absoluteLocalPath=new File(localPath).getAbsolutePath();
		String dir=new File(absoluteLocalPath).getParent();
		newFilePath=dir+"/Junit.jpg";
		absoluteLocalPath=new File(localPath).getAbsolutePath();
	}

}
