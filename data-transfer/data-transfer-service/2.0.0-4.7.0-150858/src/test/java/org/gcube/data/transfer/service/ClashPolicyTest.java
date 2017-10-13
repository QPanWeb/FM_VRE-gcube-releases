package org.gcube.data.transfer.service;

import java.io.File;

import org.gcube.data.transfer.model.DestinationClashPolicy;
import org.gcube.data.transfer.service.transfers.engine.faults.DestinationAccessException;
import org.gcube.data.transfer.service.transfers.engine.impl.PersistenceProviderImpl;

public class ClashPolicyTest {

	public static void main(String[] args) throws DestinationAccessException {
		File dir=new File ("/home/fabio/workspaces/DT_TESTS");
		dir.mkdirs();
		//		System.out.println(RequestHandler.manageClash(DestinationClashPolicy.ADD_SUFFIX, new File(dir,"clashing.file")).getAbsolutePath());
		for(int i=0;i<5;i++){
			System.out.println(PersistenceProviderImpl.manageClash(DestinationClashPolicy.ADD_SUFFIX, new File("ciao")).getAbsolutePath());
			System.out.println(PersistenceProviderImpl.manageClash(DestinationClashPolicy.ADD_SUFFIX, new File("ciao.txt")).getAbsolutePath());
		}
	}

}
