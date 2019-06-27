package org.gcube.application.perform.service.engine.utils;

import org.gcube.common.encryption.StringEncrypter;

public class CommonUtils {

	public static String decryptString(String toDecrypt){
		try{
			return StringEncrypter.getEncrypter().decrypt(toDecrypt);
		}catch(Exception e) {
			throw new RuntimeException("Unable to decrypt : "+toDecrypt,e);
		}
	}
	
	public static String encryptString(String toEncrypt){
		try{
			return StringEncrypter.getEncrypter().encrypt(toEncrypt);
		}catch(Exception e) {
			throw new RuntimeException("Unable to encrypt : "+toEncrypt,e);
		}
	}
}
