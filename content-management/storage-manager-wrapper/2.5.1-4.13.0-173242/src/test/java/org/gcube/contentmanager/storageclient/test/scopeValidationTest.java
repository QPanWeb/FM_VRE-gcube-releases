package org.gcube.contentmanager.storageclient.test;

import static org.junit.Assert.*;

import org.gcube.common.scope.impl.ServiceMapScannerMediator;
import org.gcube.contentmanager.storageclient.test.utils.Costants;
import org.junit.BeforeClass;
import org.junit.Test;

public class scopeValidationTest {

	static ServiceMapScannerMediator scanner;
//	String scope="/gcube";
	
	@BeforeClass
	public static void init(){
		scanner=new ServiceMapScannerMediator();
	}
	
	
	@Test
	public void test() {
		assertTrue(scanner.isValid(Costants.DEFAULT_SCOPE_STRING));
	}

}
